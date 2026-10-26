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
        val lastItem: String? = null,
    )

    private val lock = Object()
    private val file = AtomicFile(File(context.applicationContext.filesDir, "openvista-library.json"))
    private val worker = Executors.newSingleThreadExecutor { task -> Thread(task, "OpenVista-library-store") }
    private var state = read()
    private var generation = 0L
    private var persistedGeneration = 0L
    private var pending = false
    private var closed = false
    private var storageError: LibraryStorageException? = null

    fun items(): List<LibraryItem> = synchronized(lock) { state.items.toList() }
    fun item(id: String): LibraryItem? = synchronized(lock) { state.items.find { it.id == id } }
    fun playlists(): List<SavedPlaylist> = synchronized(lock) {
        state.playlists.map { it.copy(itemIds = it.itemIds.toList()) }
    }
    fun folders(): List<FolderGrant> = synchronized(lock) { state.folders.toList() }
    fun settings(): AppSettings = synchronized(lock) { state.settings }
    fun checkpoint(itemId: String): PlaybackCheckpoint? = synchronized(lock) {
        state.checkpoints.find { it.itemId == itemId }
    }
    fun lastItemId(): String? = synchronized(lock) { state.lastItem }
    fun lastStorageError(): LibraryStorageException? = synchronized(lock) { storageError }

    fun upsert(item: LibraryItem) = change { old ->
        val normalized = normalize(item)
        old.copy(items = replace(old.items, normalized) { it.id })
    }

    /** Adds a source import as one transaction, retaining existing metadata for duplicate URIs. */
    fun addItems(items: List<LibraryItem>): Int = synchronized(lock) {
        checkValue(items.size <= MAX_ITEMS, "The source import exceeds the library item limit.")
        val known = state.items.mapTo(hashSetOf()) { it.family to it.uri }
        val additions = items.map(::normalize).distinctBy { it.family to it.uri }
            .filterNot { (it.family to it.uri) in known }
        // change validates the complete result before updating memory or scheduling a write.
        change { old -> old.copy(items = old.items + additions) }
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
        old.copy(
            items = old.items.filterNot { it.id == id },
            playlists = old.playlists.map { it.copy(itemIds = it.itemIds.filterNot { itemId -> itemId == id }) },
            checkpoints = old.checkpoints.filterNot { it.itemId == id },
            lastItem = old.lastItem.takeUnless { it == id },
        )
    }

    /** Keeps the dependency check and grant release indivisible with respect to store edits. */
    internal fun releaseDocumentIfUnused(uri: String, release: () -> Unit) {
        synchronized(lock) {
            if (state.items.none { it.uri == uri || it.folderUri == uri } && state.folders.none { it.uri == uri }) release()
        }
    }

    fun savePlaylist(playlist: SavedPlaylist) = change { old ->
        val normalized = playlist.copy(
            id = identifier(playlist.id), name = text(playlist.name, 120).ifBlank { "Playlist" },
            itemIds = playlist.itemIds.map(::identifier).distinct(),
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
        val current = old.items.find { it.id == id }
        if (current == null) old else {
            val indices = old.items.indices.filter { old.items[it].family == current.family }
            val familyItems = indices.map { old.items[it] }.toMutableList()
            val from = familyItems.indexOfFirst { it.id == id }
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
            items = old.items.map {
                if (it.folderUri == uri) it.copy(availability = Availability.UNAVAILABLE) else it
            },
        )
    }

    /** Missing files remain saved. A rescan never erases names, favorites or playlist order. */
    fun replaceFolderItems(uri: String, items: List<LibraryItem>) = change { old ->
        contentUri(uri)
        checkValue(old.folders.any { it.uri == uri }, "The selected folder is not registered.")
        checkValue(items.size <= MAX_ITEMS, "Too many videos in this folder.")
        val found = items.map {
            checkValue(it.family == MediaFamily.VIDEOS && it.folderUri == uri, "Invalid folder video.")
            normalize(it)
        }.distinctBy { it.uri }
        val foundByUri = found.associateBy { it.uri }
        val knownUris = old.items.mapTo(hashSetOf()) { it.uri }
        val merged = old.items.map { existing ->
            val discovered = foundByUri[existing.uri]
            when {
                discovered != null && existing.family == MediaFamily.VIDEOS -> existing.copy(
                    folderUri = uri, availability = discovered.availability, observedAt = discovered.observedAt,
                )
                existing.folderUri == uri -> existing.copy(availability = Availability.UNAVAILABLE)
                else -> existing
            }
        } + found.filterNot { it.uri in knownUris }
        old.copy(items = merged)
    }

    fun saveCheckpoint(checkpoint: PlaybackCheckpoint) = change { old ->
        checkValue(old.items.any { it.id == checkpoint.itemId && it.family == MediaFamily.VIDEOS }, "Only saved videos can have a playback position.")
        val duration = checkpoint.durationMs.coerceAtLeast(0)
        val position = checkpoint.positionMs.coerceAtLeast(0).let { if (duration > 0) it.coerceAtMost(duration) else it }
        old.copy(checkpoints = replace(old.checkpoints, checkpoint.copy(positionMs = position, durationMs = duration)) { it.itemId })
    }

    fun setLastItem(id: String?) = change { old ->
        checkValue(id == null || old.items.any { it.id == id }, "The last source must belong to the library.")
        old.copy(lastItem = id)
    }

    fun updateAvailability(id: String, availability: Availability) = change { old ->
        old.copy(items = old.items.map {
            if (it.id == id) it.copy(availability = availability, observedAt = System.currentTimeMillis()) else it
        })
    }

    /** Clears app-private records only; SAF media is never modified. */
    fun clearPrivateData() = change { State() }

    private fun change(transform: (State) -> State) {
        synchronized(lock) {
            checkValue(!closed, "The library is closed.")
            storageError?.let { throw it }
            val next = transform(state)
            validate(next)
            if (next == state) return
            state = next
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
            checkValue(version == SCHEMA_VERSION, if (version > SCHEMA_VERSION) {
                "This library belongs to a newer app version. Its data was retained."
            } else "The saved library schema is unsupported. Its data was retained.")
            val restored = State(
                items = json.getJSONArray("items").objects(MAX_ITEMS) { normalize(readItem(it)) },
                playlists = json.getJSONArray("playlists").objects(MAX_PLAYLISTS) { readPlaylist(it) },
                folders = json.getJSONArray("folders").objects(MAX_FOLDERS) {
                    normalize(FolderGrant(it.getString("uri"), it.getString("title"), it.getBoolean("persisted")))
                },
                settings = normalize(readSettings(json.getJSONObject("settings"))),
                checkpoints = json.getJSONArray("checkpoints").objects(MAX_ITEMS) {
                    PlaybackCheckpoint(it.getString("itemId"), it.getLong("positionMs"), it.getLong("durationMs"))
                },
                lastItem = json.nullableString("lastItemId"),
            )
            validate(restored)
            return restored
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
        checkValue(value.lastItem == null || value.lastItem in itemsById, "Invalid last source record.")
        val fallback = value.settings.fallbackPlaylistId
        checkValue(fallback == null || value.playlists.any { it.id == fallback && it.family == MediaFamily.VIDEOS }, "Fallback must be a saved video playlist.")
        checkValue(estimate <= MAX_BYTES, "The library has reached its storage limit.")
    }

    private fun normalize(item: LibraryItem): LibraryItem {
        val uri = item.uri.trim()
        val kind = if (item.family == MediaFamily.CAMERAS) {
            val kind = try { StreamKind.valueOf(item.streamKind.orEmpty().uppercase(java.util.Locale.ROOT)) }
                catch (_: IllegalArgumentException) { throw LibraryStorageException("Select HLS or DASH for a camera source.") }
            checkValue(UserSourceParser().manualUrl(uri, kind, item.title) != null, "The camera URL is invalid or unsupported.")
            checkValue(item.folderUri == null, "A camera source cannot belong to a video folder.")
            kind.name
        } else {
            contentUri(uri)
            item.folderUri?.let(::contentUri)
            null
        }
        checkValue((item.latitude == null) == (item.longitude == null), "Latitude and longitude must be supplied together.")
        coordinate(item.latitude, -90.0, 90.0)
        coordinate(item.longitude, -180.0, 180.0)
        return item.copy(
            id = identifier(item.id), title = text(item.title, 120).ifBlank { "Source" }, uri = uri,
            streamKind = kind, category = text(item.category, 120), country = text(item.country, 120),
            city = text(item.city, 120), timeZone = zone(item.timeZone), attribution = text(item.attribution, 1_024),
            observedAt = item.observedAt.coerceIn(0, System.currentTimeMillis()),
        )
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

    private fun readItem(j: JSONObject) = LibraryItem(
        j.getString("id"), j.getString("title"), j.getString("uri"), MediaFamily.valueOf(j.getString("family")),
        j.nullableString("streamKind"), j.nullableString("folderUri"), j.getString("category"), j.getString("country"),
        j.getString("city"), j.nullableDouble("latitude"), j.nullableDouble("longitude"), j.getString("timeZone"),
        j.getString("attribution"), j.getBoolean("favorite"), Availability.valueOf(j.getString("availability")), j.getLong("observedAt"),
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

    private fun readSettings(j: JSONObject) = AppSettings(
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
    )

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
        private const val SCHEMA_VERSION = 1
        private val TIME_ZONES = TimeZone.getAvailableIDs().toSet()
    }
}
