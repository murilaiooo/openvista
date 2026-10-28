package com.openvista.prototype.library

import android.content.Context
import android.net.Uri
import android.util.AtomicFile
import com.openvista.sources.StreamKind
import com.openvista.sources.UserSourceParser
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.Executors

/** Messages deliberately exclude private source URIs and provider exception messages. */
class LibraryStorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

data class ImportedCameraList(val added: Int, val duplicateCount: Int, val playlistId: String)

/**
 * App-private, versioned library. Reads use memory; one worker coalesces atomic writes.
 * Construct off the UI thread when restoring a large library. flush() is a worker-thread
 * durability barrier; callers must observe lastStorageError() after asynchronous mutations.
 * Invalid or newer files are retained and reported rather than replaced with an empty library.
 * Activity and DreamService must share get(context) and leave that process instance open.
 */
class LibraryStore(context: Context) : AutoCloseable {
    private data class State(
        val items: List<LibraryItem> = emptyList(),
        val playlists: List<SavedPlaylist> = emptyList(),
        val folders: List<FolderGrant> = emptyList(),
        val settings: AppSettings = AppSettings(),
        val checkpoints: List<PlaybackCheckpoint> = emptyList(),
        val migratedCheckpoints: List<PlaybackCheckpoint> = emptyList(),
        val lastItem: String? = null,
    )

    private val lock = Object()
    private val file = AtomicFile(File(context.applicationContext.filesDir, "openvista-library.json"))
    private val worker = Executors.newSingleThreadExecutor { task -> Thread(task, "OpenVista-library-store") }
    private var migrationRequired = false
    private var state = read()
    // Playback resolves every queued identifier on each camera switch and every checkpoint;
    // a lookup index keeps that constant-time for large libraries instead of a list scan.
    private var byId = index(state)
    private var generation = if (migrationRequired) 1L else 0L
    private var persistedGeneration = 0L
    private var pending = false
    private var closed = false
    private var storageError: LibraryStorageException? = null

    init {
        if (migrationRequired) synchronized(lock) { scheduleWrite() }
    }

    fun items(): List<LibraryItem> = synchronized(lock) { state.items.map(::detached) }
    fun item(id: String): LibraryItem? = synchronized(lock) { byId[id]?.let(::detached) }
    fun playlists(): List<SavedPlaylist> = synchronized(lock) {
        state.playlists.map { it.copy(itemIds = it.itemIds.toList()) }
    }
    fun folders(): List<FolderGrant> = synchronized(lock) { state.folders.toList() }
    fun settings(): AppSettings = synchronized(lock) { state.settings }
    fun checkpoint(itemId: String): PlaybackCheckpoint? = synchronized(lock) {
        val canonical = resolveId(state, itemId)
        state.checkpoints.find { it.itemId == canonical }
    }
    fun lastItemId(): String? = synchronized(lock) { state.lastItem }
    fun lastStorageError(): LibraryStorageException? = synchronized(lock) { storageError }

    fun upsert(item: LibraryItem) = change { old ->
        val normalized = normalize(item)
        val existing = old.items.find { it.id == item.id || item.id in it.formerIds }
        checkValue(existing == null || existing.family == normalized.family, "A source cannot change media family.")
        checkValue(existing == null || normalized.family != MediaFamily.VIDEOS || identity(existing) == identity(normalized),
            "A saved video identifier cannot be reassigned to another document.")
        val saved = if (existing == null) normalized else withRoutes(normalized.copy(id = existing.id,
            formerIds = (existing.formerIds + normalized.formerIds).distinct(),
        ), mergeRoutes(existing.accessRoutes, normalized.accessRoutes))
        old.copy(items = replace(old.items, saved) { it.id })
    }

    /** Adds a source import as one transaction, retaining existing metadata for duplicate URIs. */
    fun addItems(items: List<LibraryItem>): Int = synchronized(lock) {
        checkValue(items.size <= MAX_ITEMS, "The source import exceeds the library item limit.")
        val known = state.items.mapTo(hashSetOf()) { identity(it) }
        val normalized = items.map(::normalize)
        val additions = normalized.distinctBy(::identity).filterNot { identity(it) in known }
        // change validates the complete result before updating memory or scheduling a write.
        val routeUpdates = normalized.filter { it.family == MediaFamily.VIDEOS && identity(it) in known }
        change { old -> old.copy(items = old.items + additions + routeUpdates) }
        additions.size
    }

    /** Commits the imported collection and its ordered camera playlist together. */
    fun importCameraList(name: String, items: List<LibraryItem>): ImportedCameraList = synchronized(lock) {
        checkValue(items.isNotEmpty() && items.size <= MAX_ITEMS, "The source import is empty or exceeds the library item limit.")
        checkValue(items.all { it.family == MediaFamily.CAMERAS }, "A camera list can contain only camera sources.")
        val normalized = items.map(::normalize)
        val known = linkedMapOf<String, LibraryItem>()
        state.items.filter { it.family == MediaFamily.CAMERAS }.forEach { known.putIfAbsent(it.uri, it) }
        val additions = mutableListOf<LibraryItem>()
        val orderedIds = linkedSetOf<String>()
        normalized.forEach { imported ->
            val existing = known[imported.uri]
            if (existing != null) {
                orderedIds.add(existing.id)
            } else {
                additions.add(imported)
                known[imported.uri] = imported
                orderedIds.add(imported.id)
            }
        }
        val playlist = SavedPlaylist(
            id = UUID.randomUUID().toString(), name = text(name, 120).ifBlank { "Playlist" },
            family = MediaFamily.CAMERAS, itemIds = orderedIds.toList(), shuffle = true,
        )
        // All item, identifier, playlist and byte-budget checks precede the state swap.
        change { old -> old.copy(items = old.items + additions, playlists = old.playlists + playlist) }
        ImportedCameraList(additions.size, items.size - additions.size, playlist.id)
    }

    fun removeItem(id: String) = change { old ->
        val canonical = resolveId(old, id)
        old.copy(
            items = old.items.filterNot { it.id == canonical },
            playlists = old.playlists.map { it.copy(itemIds = it.itemIds.filterNot { itemId -> itemId == canonical }) },
            checkpoints = old.checkpoints.filterNot { it.itemId == canonical },
            migratedCheckpoints = old.migratedCheckpoints.filterNot { resolveId(old, it.itemId) == canonical },
            lastItem = old.lastItem.takeUnless { it == canonical },
        )
    }

    /** Keeps the dependency check and grant release indivisible with respect to store edits. */
    internal fun releaseDocumentIfUnused(uri: String, release: () -> Unit) {
        synchronized(lock) {
            if (state.items.none { item -> item.accessRoutes.any { it.uri == uri || it.folderUri == uri } } && state.folders.none { it.uri == uri }) release()
        }
    }

    fun savePlaylist(playlist: SavedPlaylist) = change { old ->
        val normalized = playlist.copy(
            id = identifier(playlist.id), name = text(playlist.name, 120).ifBlank { "Playlist" },
            itemIds = playlist.itemIds.map { resolveId(old, identifier(it)) }.distinct(),
        )
        old.copy(playlists = replace(old.playlists, normalized) { it.id })
    }

    fun deletePlaylist(id: String) = change { old ->
        old.copy(
            playlists = old.playlists.filterNot { it.id == id },
            settings = old.settings.copy(fallbackPlaylistId = old.settings.fallbackPlaylistId.takeUnless { it == id }),
        )
    }

    fun movePlaylist(id: String, delta: Int) = change { old ->
        val lists = old.playlists.toMutableList()
        val from = lists.indexOfFirst { it.id == id }
        if (from >= 0) {
            val to = (from.toLong() + delta).coerceIn(0, lists.lastIndex.toLong()).toInt()
            lists.add(to, lists.removeAt(from))
        }
        old.copy(playlists = lists)
    }

    /** Changes order within a source family without moving the other family's slots. */
    fun moveItem(id: String, delta: Int) = change { old ->
        val canonical = resolveId(old, id)
        val current = old.items.find { it.id == canonical }
        if (current == null) old else {
            val indices = old.items.indices.filter { old.items[it].family == current.family }
            val familyItems = indices.map { old.items[it] }.toMutableList()
            val from = familyItems.indexOfFirst { it.id == canonical }
            val to = (from.toLong() + delta).coerceIn(0, familyItems.lastIndex.toLong()).toInt()
            familyItems.add(to, familyItems.removeAt(from))
            val reordered = old.items.toMutableList()
            indices.forEachIndexed { index, slot -> reordered[slot] = familyItems[index] }
            old.copy(items = reordered)
        }
    }

    fun saveSettings(settings: AppSettings) = change { it.copy(settings = normalize(settings)) }

    fun saveFolder(folder: FolderGrant) = change { old ->
        val normalized = normalize(folder)
        old.copy(folders = replace(old.folders, normalized) { it.uri })
    }

    /** Forgets the grant, retaining disconnected media and playlist references. */
    fun removeFolder(uri: String) = change { old ->
        old.copy(
            folders = old.folders.filterNot { it.uri == uri },
            items = old.items.map { item ->
                if (item.accessRoutes.none { it.folderUri == uri }) item else {
                    val retained = item.accessRoutes.filterNot { it.folderUri == uri }
                    if (retained.isEmpty()) item.copy(availability = Availability.UNAVAILABLE,
                        accessRoutes = item.accessRoutes.map { it.copy(availability = Availability.UNAVAILABLE) })
                    else withRoutes(item, retained)
                }
            },
        )
    }

    /** Missing files remain saved. A rescan never erases names, favorites or playlist order. */
    fun replaceFolderItems(uri: String, items: List<LibraryItem>, complete: Boolean = true) = change { old ->
        contentUri(uri)
        checkValue(old.folders.any { it.uri == uri }, "The selected folder is not registered.")
        checkValue(items.size <= MAX_ITEMS, "Too many videos in this folder.")
        val found = items.map {
            checkValue(it.family == MediaFamily.VIDEOS && it.folderUri == uri, "Invalid folder video.")
            normalize(it)
        }.distinctBy(::identity)
        val foundByIdentity = found.associateBy(::identity)
        val known = old.items.mapTo(hashSetOf()) { identity(it) }
        val merged = old.items.map { existing ->
            val discovered = foundByIdentity[identity(existing)]
            when {
                discovered != null && existing.family == MediaFamily.VIDEOS -> withRoutes(existing.copy(
                    formerIds = (existing.formerIds + discovered.id).filterNot { it == existing.id }.distinct()),
                    mergeRoutes(existing.accessRoutes, discovered.accessRoutes))
                existing.accessRoutes.any { it.folderUri == uri } -> withRoutes(existing, existing.accessRoutes.map {
                    if (it.folderUri == uri) it.copy(availability = if (complete) Availability.UNAVAILABLE else Availability.UNKNOWN,
                        observedAt = System.currentTimeMillis()) else it
                })
                else -> existing
            }
        } + found.filterNot { identity(it) in known }
        old.copy(items = merged)
    }

    internal fun updateFolderAvailability(uri: String, availability: Availability) = change { old ->
        old.copy(items = old.items.map { item -> withRoutes(item, item.accessRoutes.map {
            if (it.folderUri == uri) it.copy(availability = availability, observedAt = System.currentTimeMillis()) else it
        }) })
    }

    internal fun updateAccessRoutes(routesById: Map<String, List<VideoAccessRoute>>) = change { old ->
        old.copy(items = old.items.map { item -> routesById[item.id]?.let { withRoutes(item, it) } ?: item })
    }

    fun saveCheckpoint(checkpoint: PlaybackCheckpoint) = change { old ->
        val canonical = resolveId(old, checkpoint.itemId)
        checkValue(old.items.any { it.id == canonical && it.family == MediaFamily.VIDEOS }, "Only saved videos can have a playback position.")
        val duration = checkpoint.durationMs.coerceAtLeast(0)
        val position = checkpoint.positionMs.coerceAtLeast(0).let { if (duration > 0) it.coerceAtMost(duration) else it }
        old.copy(checkpoints = replace(old.checkpoints, checkpoint.copy(itemId = canonical, positionMs = position, durationMs = duration)) { it.itemId })
    }

    fun setLastItem(id: String?) = change { old ->
        val canonical = id?.let { resolveId(old, it) }
        checkValue(canonical == null || old.items.any { it.id == canonical }, "The last source must belong to the library.")
        old.copy(lastItem = canonical)
    }

    fun updateAvailability(id: String, availability: Availability) = change { old ->
        val canonical = resolveId(old, id)
        old.copy(items = old.items.map {
            if (it.id == canonical) it.copy(availability = availability, observedAt = System.currentTimeMillis(),
                accessRoutes = it.accessRoutes.map { route -> route.copy(availability = availability, observedAt = System.currentTimeMillis()) }) else it
        })
    }

    /** Clears app-private records only; SAF media is never modified. */
    fun clearPrivateData() = change { State() }

    private fun change(transform: (State) -> State) {
        synchronized(lock) {
            checkValue(!closed, "The library is closed.")
            // Retry a transient write failure with the complete in-memory snapshot. Load failures
            // never create a store, so this cannot overwrite an unreadable or newer library.
            storageError?.let {
                storageError = null
                if (generation > persistedGeneration) scheduleWrite()
                throw it
            }
            val next = consolidate(transform(state))
            validate(next)
            if (next == state) return
            state = next
            byId = index(next)
            generation++
            scheduleWrite()
        }
    }

    private fun scheduleWrite() {
        if (pending) return
        pending = true
        worker.execute {
            while (true) {
                val snapshot = synchronized(lock) { state to generation }
                try {
                    write(snapshot.first)
                } catch (failure: Exception) {
                    synchronized(lock) {
                        storageError = LibraryStorageException("The library could not be saved. Existing saved data was retained.", failure)
                        pending = false
                        lock.notifyAll()
                    }
                    return@execute
                }
                synchronized(lock) {
                    persistedGeneration = snapshot.second
                    storageError = null
                    if (generation == persistedGeneration) {
                        pending = false
                        lock.notifyAll()
                        return@execute
                    }
                }
            }
        }
    }

    /** Blocks until all preceding changes are durable; call from a worker, never a UI callback. */
    fun flush() {
        synchronized(lock) {
            if (generation > persistedGeneration && !pending && !closed) {
                storageError = null
                scheduleWrite()
            }
            while (pending) {
                try { lock.wait() } catch (interrupted: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw LibraryStorageException("Waiting for library storage was interrupted.", interrupted)
                }
            }
            storageError?.let { throw it }
        }
    }

    /** Schedules completion without blocking UI teardown. Use flush() first when durability is required. */
    override fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
            worker.shutdown()
        }
    }

    private fun read(): State {
        try {
            val bytes = file.openRead().use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8_192)
                while (output.size() <= MAX_BYTES) {
                    val count = input.read(buffer, 0, minOf(buffer.size, MAX_BYTES + 1 - output.size()))
                    if (count < 0) break
                    if (count == 0) throw LibraryStorageException("The saved library could not be read completely.")
                    output.write(buffer, 0, count)
                }
                val bytes = output.toByteArray()
                checkValue(bytes.size <= MAX_BYTES, "The saved library exceeds the supported storage limit.")
                bytes
            }
            val json = JSONObject(Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString())
            val version = json.getInt("schemaVersion")
            checkValue(version in 1..SCHEMA_VERSION, if (version > SCHEMA_VERSION) {
                "This library belongs to a newer app version. Its data was retained."
            } else "The saved library schema is unsupported. Its data was retained.")
            val restored = State(
                items = json.getJSONArray("items").objects(MAX_ITEMS) {
                    val item = normalize(readItem(it))
                    // A persisted observation is not proof that removable media is still mounted.
                    if (item.family == MediaFamily.VIDEOS) item.copy(availability = Availability.UNKNOWN,
                        accessRoutes = item.accessRoutes.map { route -> route.copy(availability = Availability.UNKNOWN) }) else item
                },
                playlists = json.getJSONArray("playlists").objects(MAX_PLAYLISTS) { readPlaylist(it) },
                folders = json.getJSONArray("folders").objects(MAX_FOLDERS) {
                    normalize(FolderGrant(it.getString("uri"), it.getString("title"), it.getBoolean("persisted")))
                },
                settings = normalize(readSettings(json.getJSONObject("settings"))),
                checkpoints = json.getJSONArray("checkpoints").objects(MAX_ITEMS) {
                    PlaybackCheckpoint(it.getString("itemId"), it.getLong("positionMs"), it.getLong("durationMs"))
                },
                migratedCheckpoints = if (json.has("migratedCheckpoints")) json.getJSONArray("migratedCheckpoints").objects(MAX_ITEMS) {
                    PlaybackCheckpoint(it.getString("itemId"), it.getLong("positionMs"), it.getLong("durationMs"))
                } else emptyList(),
                lastItem = json.nullableString("lastItemId"),
            )
            validate(restored)
            val migrated = consolidate(restored).also(::validate)
            migrationRequired = version != SCHEMA_VERSION || migrated != restored
            return migrated
        } catch (_: FileNotFoundException) {
            if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists()) return State()
            throw LibraryStorageException("The saved library cannot be opened. Its data was retained.")
        } catch (failure: LibraryStorageException) {
            throw failure
        } catch (failure: Exception) {
            throw LibraryStorageException("The saved library cannot be read safely. Its data was retained.", failure)
        }
    }

    private fun write(snapshot: State) {
        val json = JSONObject().put("schemaVersion", SCHEMA_VERSION)
            .put("items", JSONArray(snapshot.items.map(::writeItem)))
            .put("playlists", JSONArray(snapshot.playlists.map {
                JSONObject().put("id", it.id).put("name", it.name).put("family", it.family.name)
                    .put("itemIds", JSONArray(it.itemIds)).put("shuffle", it.shuffle)
            }))
            .put("folders", JSONArray(snapshot.folders.map {
                JSONObject().put("uri", it.uri).put("title", it.title).put("persisted", it.persisted)
            }))
            .put("settings", writeSettings(snapshot.settings))
            .put("checkpoints", JSONArray(snapshot.checkpoints.map {
                JSONObject().put("itemId", it.itemId).put("positionMs", it.positionMs).put("durationMs", it.durationMs)
            }))
            .put("migratedCheckpoints", JSONArray(snapshot.migratedCheckpoints.map {
                JSONObject().put("itemId", it.itemId).put("positionMs", it.positionMs).put("durationMs", it.durationMs)
            }))
            .put("lastItemId", snapshot.lastItem ?: JSONObject.NULL)
        val bytes = json.toString().toByteArray(Charsets.UTF_8)
        checkValue(bytes.size <= MAX_BYTES, "The library exceeds the supported storage limit.")
        val output = file.startWrite()
        try {
            output.write(bytes)
            file.finishWrite(output)
        } catch (failure: Exception) {
            file.failWrite(output)
            throw failure
        }
    }

    private fun validate(value: State) {
        checkValue(value.items.size <= MAX_ITEMS && value.playlists.size <= MAX_PLAYLISTS && value.folders.size <= MAX_FOLDERS, "The library has reached its storage limit.")
        checkValue(value.items.map { it.id }.distinct().size == value.items.size, "Duplicate source identifiers in the library.")
        checkValue(value.playlists.map { it.id }.distinct().size == value.playlists.size, "Duplicate playlist identifiers in the library.")
        checkValue(value.folders.map { it.uri }.distinct().size == value.folders.size, "Duplicate folders in the library.")
        val itemsById = value.items.associateBy { it.id }
        var estimate = 16_384L
        value.items.forEach { item ->
            checkValue(item.accessRoutes.size <= MAX_ROUTES && item.formerIds.size <= MAX_ITEMS, "Too many document access routes or prior identifiers.")
            item.formerIds.forEach { identifier(it) }
            estimate += item.accessRoutes.sumOf { 256L + 6L * (it.uri.length + it.folderUri.orEmpty().length) }
            estimate += item.formerIds.sumOf { 32L + 6L * it.length }
            estimate += 1_024 + 6L * listOf(item.id, item.title, item.uri, item.streamKind.orEmpty(), item.folderUri.orEmpty(), item.category,
                item.country, item.city, item.timeZone, item.attribution).sumOf { it.length }
        }
        value.playlists.forEach { list ->
            identifier(list.id)
            checkValue(list.name.length <= 120 && list.itemIds.size <= MAX_ITEMS, "The playlist exceeds its storage limit.")
            checkValue(list.itemIds.distinct().size == list.itemIds.size, "A playlist contains duplicate entries.")
            list.itemIds.forEach { id ->
                identifier(id)
                checkValue(itemsById[id]?.family?.let { it == list.family } ?: true, "Camera and video playlists must remain separate.")
            }
            estimate += 512 + 6L * (list.id.length + list.name.length + list.itemIds.sumOf { it.length + 4 })
        }
        value.folders.forEach { estimate += 256 + 6L * (it.uri.length + it.title.length) }
        checkValue(value.checkpoints.size <= MAX_ITEMS && value.checkpoints.map { it.itemId }.distinct().size == value.checkpoints.size, "Invalid playback position records.")
        value.checkpoints.forEach {
            checkValue(itemsById[it.itemId]?.family == MediaFamily.VIDEOS && it.positionMs >= 0 && it.durationMs >= 0 &&
                (it.durationMs == 0L || it.positionMs <= it.durationMs), "Invalid saved video position.")
            estimate += 512 + 6L * it.itemId.length
        }
        checkValue(value.migratedCheckpoints.size <= MAX_ITEMS, "Too many retained migration positions.")
        value.migratedCheckpoints.forEach {
            checkValue(itemsById[resolveId(value, it.itemId)]?.family == MediaFamily.VIDEOS && it.positionMs >= 0 && it.durationMs >= 0 &&
                (it.durationMs == 0L || it.positionMs <= it.durationMs), "Invalid retained migration position.")
            estimate += 512 + 6L * it.itemId.length
        }
        checkValue(value.lastItem == null || value.lastItem in itemsById, "Invalid last source record.")
        val fallback = value.settings.fallbackPlaylistId
        checkValue(fallback == null || value.playlists.any { it.id == fallback && it.family == MediaFamily.VIDEOS }, "Fallback must be a saved video playlist.")
        checkValue(estimate <= MAX_BYTES, "The library has reached its storage limit.")
    }

    /** Same precedence as a front-to-back search of current IDs and prior IDs. */
    private fun index(value: State): Map<String, LibraryItem> {
        val result = HashMap<String, LibraryItem>(value.items.size * 2)
        value.items.forEach { item ->
            result.putIfAbsent(item.id, item)
            item.formerIds.forEach { result.putIfAbsent(it, item) }
        }
        return result
    }

    private fun detached(item: LibraryItem): LibraryItem = item.copy(accessRoutes = item.accessRoutes.toList(), formerIds = item.formerIds.toList())

    private fun identity(item: LibraryItem): Pair<MediaFamily, String> = item.family to
        (if (item.family == MediaFamily.VIDEOS) SafDocumentIdentity.key(item.uri) else item.uri)

    private fun resolveId(value: State, id: String): String = value.items.find { it.id == id || id in it.formerIds }?.id ?: id

    private fun mergeRoutes(first: List<VideoAccessRoute>, second: List<VideoAccessRoute>): List<VideoAccessRoute> =
        (first + second).groupBy { it.uri to it.folderUri }.values.map { routes ->
            routes.reduce { previous, next -> if (next.observedAt >= previous.observedAt) next else previous }
        }

    private fun withRoutes(item: LibraryItem, routes: List<VideoAccessRoute>): LibraryItem {
        if (item.family != MediaFamily.VIDEOS || routes.isEmpty()) return item
        val selected = routes.filter { it.availability == Availability.AVAILABLE }.maxByOrNull { it.observedAt }
            ?: routes.filter { it.availability == Availability.UNKNOWN }.maxByOrNull { it.observedAt }
            ?: routes.maxBy { it.observedAt }
        return item.copy(uri = selected.uri, folderUri = selected.folderUri, availability = selected.availability,
            observedAt = selected.observedAt, accessRoutes = routes.toList())
    }

    /** Stable first ID wins; all internal references and IDs held by active callers remain usable. */
    private fun consolidate(value: State): State {
        value.items.groupBy { it.id }.values.filter { it.size > 1 }.forEach { duplicates ->
            checkValue(duplicates.all { it.family == MediaFamily.VIDEOS } && duplicates.map(::identity).distinct().size == 1,
                "Duplicate source identifiers in the library.")
        }
        val merged = linkedMapOf<Pair<MediaFamily, String>, LibraryItem>()
        value.items.forEach { item ->
            val key = identity(item)
            val existing = merged[key]
            if (existing == null || item.family != MediaFamily.VIDEOS) {
                // Camera duplicates retain their existing independent source identities.
                if (item.family == MediaFamily.CAMERAS) merged[item.family to item.id] = item else merged[key] = item
            } else {
                merged[key] = withRoutes(existing.copy(favorite = existing.favorite || item.favorite,
                    formerIds = (existing.formerIds + item.id + item.formerIds).filterNot { it == existing.id }.distinct()),
                    mergeRoutes(existing.accessRoutes, item.accessRoutes))
            }
        }
        val items = merged.values.toList()
        val ids = hashMapOf<String, String>()
        items.forEach { item -> (item.formerIds + item.id).forEach { id ->
            checkValue(ids.put(id, item.id)?.let { it == item.id } ?: true, "Conflicting prior source identifiers.")
        } }
        val checkpointGroups = value.checkpoints.groupBy { ids[it.itemId] ?: it.itemId }
        val checkpoints = checkpointGroups.map { (id, records) ->
            // Prefer the active scene's resume point, then the surviving record, then the furthest point.
            (records.find { it.itemId == value.lastItem } ?: records.find { it.itemId == id }
                ?: records.maxBy { it.positionMs }).copy(itemId = id)
        }
        return value.copy(items = items, playlists = value.playlists.map {
            it.copy(itemIds = it.itemIds.map { id -> ids[id] ?: id }.distinct())
        }, checkpoints = checkpoints,
            migratedCheckpoints = (value.migratedCheckpoints + checkpointGroups.values.filter { it.size > 1 }.flatten()).distinct(),
            lastItem = value.lastItem?.let { ids[it] ?: it })
    }

    private fun normalize(item: LibraryItem): LibraryItem {
        val uri = item.uri.trim()
        val kind = if (item.family == MediaFamily.CAMERAS) {
            val kind = try { StreamKind.valueOf(item.streamKind.orEmpty().uppercase(java.util.Locale.ROOT)) }
                catch (_: IllegalArgumentException) { throw LibraryStorageException("Select HLS or DASH for a camera source.") }
            checkValue(UserSourceParser().manualUrl(uri, kind, item.title) != null, "The camera URL is invalid or unsupported.")
            checkValue(item.folderUri == null, "A camera source cannot belong to a video folder.")
            checkValue(item.accessRoutes.isEmpty() && item.formerIds.isEmpty(), "Camera sources cannot have document grants or prior video identifiers.")
            kind.name
        } else {
            contentUri(uri)
            item.folderUri?.let(::contentUri)
            null
        }
        checkValue((item.latitude == null) == (item.longitude == null), "Latitude and longitude must be supplied together.")
        coordinate(item.latitude, -90.0, 90.0)
        coordinate(item.longitude, -180.0, 180.0)
        val routes = if (item.family == MediaFamily.VIDEOS) {
            checkValue(item.accessRoutes.size <= MAX_ROUTES, "Too many access routes for this video.")
            val provided = item.accessRoutes.ifEmpty { listOf(VideoAccessRoute(uri, item.folderUri, item.availability, item.observedAt)) }
            provided.map { route ->
                contentUri(route.uri)
                route.folderUri?.let(::contentUri)
                checkValue(SafDocumentIdentity.key(route.uri) == SafDocumentIdentity.key(uri), "A video access route identifies a different document.")
                route.copy(observedAt = route.observedAt.coerceIn(0, System.currentTimeMillis()))
            }.distinctBy { it.uri to it.folderUri }
        } else emptyList()
        return withRoutes(item.copy(
            id = identifier(item.id), title = text(item.title, 120).ifBlank { "Source" }, uri = uri,
            streamKind = kind, category = text(item.category, 120), country = text(item.country, 120),
            city = text(item.city, 120), timeZone = zone(item.timeZone), attribution = text(item.attribution, 1_024),
            observedAt = item.observedAt.coerceIn(0, System.currentTimeMillis()),
            accessRoutes = routes, formerIds = item.formerIds.map(::identifier).filterNot { it == item.id }.distinct(),
        ), routes)
    }

    private fun normalize(folder: FolderGrant): FolderGrant {
        contentUri(folder.uri)
        return folder.copy(title = text(folder.title, 120).ifBlank { "Video folder" })
    }

    private fun normalize(s: AppSettings): AppSettings {
        checkValue((s.viewerLatitude == null) == (s.viewerLongitude == null), "Viewer coordinates must be supplied together.")
        coordinate(s.viewerLatitude, -90.0, 90.0)
        coordinate(s.viewerLongitude, -180.0, 180.0)
        return s.copy(
            language = s.language.takeIf { it in setOf("system", "en", "pt-BR") } ?: "system",
            rotationMinutes = s.rotationMinutes.coerceIn(1, 60), maxVideoHeight = s.maxVideoHeight.coerceIn(0, 8_640),
            resizeMode = s.resizeMode.takeIf { it in setOf("crop", "fit") } ?: "crop",
            clockFormat = s.clockFormat.takeIf { it in setOf("system", "12", "24") } ?: "system",
            temperatureUnit = when (s.temperatureUnit) {
                "c", "celsius" -> "celsius"
                "f", "fahrenheit" -> "fahrenheit"
                else -> "system"
            },
            overlayCorner = s.overlayCorner.coerceIn(0, 3), overlaySize = s.overlaySize.coerceIn(12, 64),
            overlayOpacity = s.overlayOpacity.coerceIn(10, 100), overlayX = s.overlayX.coerceIn(-4_096, 4_096),
            overlayY = s.overlayY.coerceIn(-4_096, 4_096), overlayHideSeconds = s.overlayHideSeconds.coerceIn(5, 3_600),
            weatherContext = s.weatherContext.takeIf { it in setOf("camera", "viewer") } ?: "camera",
            viewerCity = text(s.viewerCity, 120), viewerTimeZone = zone(s.viewerTimeZone),
            overlayLayout = s.overlayLayout.takeIf { it in setOf("cinematic", "compact") } ?: "cinematic",
            viewerForecastDays = s.viewerForecastDays.coerceIn(3, 7),
            sourceOpacity = s.sourceOpacity.coerceIn(20, 100), viewerOpacity = s.viewerOpacity.coerceIn(20, 100),
            forecastOpacity = s.forecastOpacity.coerceIn(20, 100), brandOpacity = s.brandOpacity.coerceIn(20, 100),
            overlayMoveMinutes = s.overlayMoveMinutes.coerceIn(1, 30),
            scheduleStartMinute = s.scheduleStartMinute.coerceIn(0, 1_439), scheduleEndMinute = s.scheduleEndMinute.coerceIn(0, 1_439),
        )
    }

    private fun contentUri(raw: String) {
        checkValue(raw.length <= 4_096 && raw.none { it.isISOControl() }, "The selected document URI is invalid.")
        val uri = Uri.parse(raw)
        checkValue(uri.scheme == "content" && !uri.authority.isNullOrBlank() && uri.fragment == null, "Select local media through the system document picker.")
    }

    private fun identifier(raw: String): String {
        checkValue(raw.isNotBlank() && raw.length <= 128 && raw.none { it.isISOControl() }, "Invalid library identifier.")
        return raw
    }

    private fun text(raw: String, limit: Int): String = raw.filterNot { it.isISOControl() }.trim().take(limit)
    private fun zone(raw: String): String {
        val value = text(raw, 128)
        checkValue(value.isEmpty() || value in TIME_ZONES, "Choose a recognized time zone.")
        return value
    }
    private fun coordinate(value: Double?, min: Double, max: Double) {
        checkValue(value == null || (value.isFinite() && value in min..max), "Location coordinates are outside valid bounds.")
    }

    private fun writeItem(i: LibraryItem): JSONObject = JSONObject().put("id", i.id).put("title", i.title)
        .put("uri", i.uri).put("family", i.family.name).put("streamKind", i.streamKind ?: JSONObject.NULL)
        .put("folderUri", i.folderUri ?: JSONObject.NULL).put("category", i.category).put("country", i.country)
        .put("city", i.city).put("latitude", i.latitude ?: JSONObject.NULL).put("longitude", i.longitude ?: JSONObject.NULL)
        .put("timeZone", i.timeZone).put("attribution", i.attribution).put("favorite", i.favorite)
        .put("availability", i.availability.name).put("observedAt", i.observedAt)
        .put("accessRoutes", JSONArray(i.accessRoutes.map {
            JSONObject().put("uri", it.uri).put("folderUri", it.folderUri ?: JSONObject.NULL)
                .put("availability", it.availability.name).put("observedAt", it.observedAt)
        })).put("formerIds", JSONArray(i.formerIds))

    private fun readItem(j: JSONObject) = LibraryItem(
        j.getString("id"), j.getString("title"), j.getString("uri"), MediaFamily.valueOf(j.getString("family")),
        j.nullableString("streamKind"), j.nullableString("folderUri"), j.getString("category"), j.getString("country"),
        j.getString("city"), j.nullableDouble("latitude"), j.nullableDouble("longitude"), j.getString("timeZone"),
        j.getString("attribution"), j.getBoolean("favorite"), Availability.valueOf(j.getString("availability")), j.getLong("observedAt"),
        if (j.has("accessRoutes")) j.getJSONArray("accessRoutes").objects(MAX_ROUTES) {
            VideoAccessRoute(it.getString("uri"), it.nullableString("folderUri"), Availability.valueOf(it.getString("availability")), it.getLong("observedAt"))
        } else emptyList(),
        if (j.has("formerIds")) j.getJSONArray("formerIds").let { ids ->
            checkValue(ids.length() <= MAX_ITEMS, "Too many prior video identifiers.")
            List(ids.length()) { ids.getString(it) }
        } else emptyList(),
    )

    private fun readPlaylist(j: JSONObject): SavedPlaylist {
        val ids = j.getJSONArray("itemIds")
        checkValue(ids.length() <= MAX_ITEMS, "The saved playlist exceeds its storage limit.")
        return SavedPlaylist(j.getString("id"), j.getString("name"), MediaFamily.valueOf(j.getString("family")),
            List(ids.length()) { ids.getString(it) }, j.getBoolean("shuffle"))
    }

    private fun writeSettings(s: AppSettings): JSONObject = JSONObject()
        .put("language", s.language).put("muted", s.muted).put("rotationMinutes", s.rotationMinutes)
        .put("maxVideoHeight", s.maxVideoHeight).put("resizeMode", s.resizeMode).put("smoothTransitions", s.smoothTransitions)
        .put("keepAwake", s.keepAwake).put("fallbackPlaylistId", s.fallbackPlaylistId ?: JSONObject.NULL)
        .put("stayLocal", s.stayLocal).put("showClock", s.showClock).put("showDate", s.showDate)
        .put("showWeather", s.showWeather).put("showLocation", s.showLocation).put("clockFormat", s.clockFormat)
        .put("temperatureUnit", s.temperatureUnit).put("overlayCorner", s.overlayCorner).put("overlaySize", s.overlaySize)
        .put("overlayOpacity", s.overlayOpacity).put("overlayColor", s.overlayColor).put("overlayX", s.overlayX)
        .put("overlayY", s.overlayY).put("moveOverlay", s.moveOverlay).put("hideOverlay", s.hideOverlay)
        .put("overlayHideSeconds", s.overlayHideSeconds).put("weatherContext", s.weatherContext)
        .put("viewerCity", s.viewerCity).put("viewerLatitude", s.viewerLatitude ?: JSONObject.NULL)
        .put("viewerLongitude", s.viewerLongitude ?: JSONObject.NULL).put("viewerTimeZone", s.viewerTimeZone)
        .put("scheduleEnabled", s.scheduleEnabled).put("scheduleStartMinute", s.scheduleStartMinute)
        .put("scheduleEndMinute", s.scheduleEndMinute).put("onboardingComplete", s.onboardingComplete)
        .put("diagnosticsEnabled", s.diagnosticsEnabled).put("automaticUpdates", s.automaticUpdates)
        .put("overlayLayout", s.overlayLayout).put("showBrand", s.showBrand)
        .put("showSourceInfo", s.showSourceInfo).put("showViewerInfo", s.showViewerInfo)
        .put("showForecast", s.showForecast).put("viewerForecastDays", s.viewerForecastDays)
        .put("sourceOpacity", s.sourceOpacity).put("viewerOpacity", s.viewerOpacity)
        .put("forecastOpacity", s.forecastOpacity).put("brandOpacity", s.brandOpacity)
        .put("overlayMoveMinutes", s.overlayMoveMinutes)

    private fun readSettings(j: JSONObject): AppSettings {
        val legacyVisible = listOf("showClock", "showWeather", "showDate", "showLocation").any { j.getBoolean(it) }
        val legacyContext = j.getString("weatherContext")
        return AppSettings(
        language = j.getString("language"), muted = j.getBoolean("muted"), rotationMinutes = j.getInt("rotationMinutes"),
        maxVideoHeight = j.getInt("maxVideoHeight"), resizeMode = j.getString("resizeMode"), smoothTransitions = j.getBoolean("smoothTransitions"),
        keepAwake = j.getBoolean("keepAwake"), fallbackPlaylistId = j.nullableString("fallbackPlaylistId"), stayLocal = j.getBoolean("stayLocal"),
        showClock = j.getBoolean("showClock"), showDate = j.getBoolean("showDate"), showWeather = j.getBoolean("showWeather"),
        showLocation = j.getBoolean("showLocation"), clockFormat = j.getString("clockFormat"), temperatureUnit = j.getString("temperatureUnit"),
        overlayCorner = j.getInt("overlayCorner"), overlaySize = j.getInt("overlaySize"), overlayOpacity = j.getInt("overlayOpacity"),
        overlayColor = j.getInt("overlayColor"), overlayX = j.getInt("overlayX"), overlayY = j.getInt("overlayY"),
        moveOverlay = j.getBoolean("moveOverlay"), hideOverlay = j.getBoolean("hideOverlay"), overlayHideSeconds = j.getInt("overlayHideSeconds"),
        weatherContext = j.getString("weatherContext"), viewerCity = j.getString("viewerCity"), viewerLatitude = j.nullableDouble("viewerLatitude"),
        viewerLongitude = j.nullableDouble("viewerLongitude"), viewerTimeZone = j.getString("viewerTimeZone"), scheduleEnabled = j.getBoolean("scheduleEnabled"),
        scheduleStartMinute = j.getInt("scheduleStartMinute"), scheduleEndMinute = j.getInt("scheduleEndMinute"), onboardingComplete = j.getBoolean("onboardingComplete"),
        diagnosticsEnabled = j.getBoolean("diagnosticsEnabled"), automaticUpdates = j.getBoolean("automaticUpdates"),
        overlayLayout = j.optString("overlayLayout", "cinematic"), showBrand = j.optBoolean("showBrand", legacyVisible),
        showSourceInfo = j.optBoolean("showSourceInfo", legacyVisible),
        showViewerInfo = j.optBoolean("showViewerInfo", legacyVisible && (legacyContext == "viewer" || j.getString("viewerCity").isNotBlank())),
        showForecast = j.optBoolean("showForecast", j.getBoolean("showWeather")), viewerForecastDays = j.optInt("viewerForecastDays", 5),
        sourceOpacity = j.optInt("sourceOpacity", 100), viewerOpacity = j.optInt("viewerOpacity", 100),
        forecastOpacity = j.optInt("forecastOpacity", 100), brandOpacity = j.optInt("brandOpacity", 70),
        overlayMoveMinutes = j.optInt("overlayMoveMinutes", 5),
    )
    }

    private fun JSONObject.nullableString(key: String): String? = if (isNull(key)) null else getString(key)
    private fun JSONObject.nullableDouble(key: String): Double? = if (isNull(key)) null else getDouble(key)
    private fun <T> JSONArray.objects(limit: Int, read: (JSONObject) -> T): List<T> {
        checkValue(length() <= limit, "The saved library exceeds its record limit.")
        return List(length()) { read(getJSONObject(it)) }
    }
    private fun <T> replace(list: List<T>, value: T, id: (T) -> String): List<T> {
        val index = list.indexOfFirst { id(it) == id(value) }
        return if (index < 0) list + value else list.toMutableList().also { it[index] = value }.toList()
    }
    private fun checkValue(condition: Boolean, message: String) {
        if (!condition) throw LibraryStorageException(message)
    }

    companion object {
        @Volatile private var instance: LibraryStore? = null

        /** Process-wide shared owner; Activities and DreamService must not close it. */
        fun get(context: Context): LibraryStore = instance ?: synchronized(this) {
            instance ?: LibraryStore(context.applicationContext).also { instance = it }
        }

        const val MAX_ITEMS = 2_000
        const val MAX_PLAYLISTS = 100
        const val MAX_FOLDERS = 32
        private const val MAX_BYTES = 8 * 1_024 * 1_024
        private const val SCHEMA_VERSION = 2
        private const val MAX_ROUTES = 64
        private val TIME_ZONES = TimeZone.getAvailableIDs().toSet()
    }
}
