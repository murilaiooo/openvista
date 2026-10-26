package com.openvista.prototype.library

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.openvista.prototype.R
import com.openvista.prototype.SafVideoScanner
import com.openvista.sources.LocalVideoRules
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Owns only picker-granted read access. Provider calls, scans and durability barriers
 * run on one worker; cancelled or closed operations never deliver stale UI callbacks.
 */
class LibraryAccess(context: Context, private val store: LibraryStore) : AutoCloseable {
    private val context = context.applicationContext
    private val resolver = this.context.contentResolver
    private val scanner = SafVideoScanner(resolver)
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor { task -> Thread(task, "OpenVista-library-access") }
    private val lock = Any()
    private var generation = 0L
    private var signal: CancellationSignal? = null
    private var future: Future<*>? = null
    private var closed = false
    private val sessionUris = hashSetOf<String>()

    fun restore(onComplete: (String) -> Unit) = launch(onComplete) { cancellation ->
        val grants = persistedReads()
        migrateLegacyFolder(grants, cancellation)
        val sessions = synchronized(lock) { sessionUris.toSet() }
        val readableUris = grants + sessions
        var unavailable = 0
        var capped = false
        var sessionOnly = false
        for (folder in store.folders()) {
            cancellation.throwIfCanceled()
            if (folder.uri !in readableUris) {
                store.saveFolder(folder.copy(persisted = false))
                markFolderUnavailable(folder.uri)
                unavailable++
                continue
            }
            store.saveFolder(folder.copy(persisted = folder.uri in grants))
            when (val result = scanner.scan(Uri.parse(folder.uri), cancellation)) {
                is SafVideoScanner.Result.Ready -> {
                    cancellation.throwIfCanceled()
                    saveScan(folder.uri, result)
                    capped = capped || result.capped
                    sessionOnly = sessionOnly || folder.uri !in grants
                }
                SafVideoScanner.Result.Unavailable -> {
                    cancellation.throwIfCanceled()
                    markFolderUnavailable(folder.uri)
                    unavailable++
                }
            }
        }
        val registeredFolders = store.folders().mapTo(hashSetOf()) { it.uri }
        val refreshedRoutes = linkedMapOf<String, List<VideoAccessRoute>>()
        for (item in store.items().filter { it.family == MediaFamily.VIDEOS }) {
            cancellation.throwIfCanceled()
            val routes = item.accessRoutes.map { route ->
                val grant = route.folderUri ?: route.uri
                val authorized = grant in readableUris && (route.folderUri == null || grant in registeredFolders)
                val readable = authorized && canRead(Uri.parse(route.uri), cancellation)
                if (readable && grant !in grants) sessionOnly = true
                route.copy(availability = if (readable) Availability.AVAILABLE else Availability.UNAVAILABLE,
                    observedAt = System.currentTimeMillis())
            }
            cancellation.throwIfCanceled()
            refreshedRoutes[item.id] = routes
            if (routes.none { it.availability == Availability.AVAILABLE }) unavailable++
        }
        cancellation.throwIfCanceled()
        store.updateAccessRoutes(refreshedRoutes)
        store.flush()
        val status = when {
            unavailable > 0 -> message(R.string.library_restored_unavailable, unavailable)
            capped -> message(R.string.library_scan_limited)
            else -> message(R.string.library_restored)
        }
        if (sessionOnly) "$status ${message(R.string.library_session_only)}" else status
    }

    fun selectFolder(uri: Uri, flags: Int, onComplete: (String) -> Unit) = launch(onComplete) { cancellation ->
        if (!validContentUri(uri) || !DocumentsContract.isTreeUri(uri)) return@launch message(R.string.library_select_folder)
        if (!acceptRead(uri, flags)) return@launch message(R.string.library_persist_denied)
        cancellation.throwIfCanceled()
        val title = displayName(DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)), cancellation)
            ?: message(R.string.library_folder_name)
        val persisted = uri.toString() in persistedReads()
        store.saveFolder(FolderGrant(uri.toString(), title, persisted))
        val result = scanner.scan(uri, cancellation)
        cancellation.throwIfCanceled()
        when (result) {
            is SafVideoScanner.Result.Ready -> {
                saveScan(uri.toString(), result)
                store.flush()
                val status = if (result.capped) message(R.string.library_scan_limited)
                    else message(R.string.library_folder_scanned, result.videos.size)
                if (persisted) status else "$status ${message(R.string.library_session_only)}"
            }
            SafVideoScanner.Result.Unavailable -> {
                markFolderUnavailable(uri.toString())
                store.flush()
                message(R.string.library_folder_unavailable)
            }
        }
    }

    fun selectVideo(uri: Uri, flags: Int, onComplete: (String) -> Unit) = launch(onComplete) { cancellation ->
        if (!validContentUri(uri)) return@launch message(R.string.library_select_video)
        val title = displayName(uri, cancellation)
        val mime = resolver.getType(uri)
        if (!LocalVideoRules.isSupported(mime, title)) return@launch message(R.string.library_unsupported_video)
        if (!acceptRead(uri, flags)) return@launch message(R.string.library_persist_denied)
        if (!canRead(uri, cancellation)) {
            synchronized(lock) { sessionUris.remove(uri.toString()) }
            return@launch message(R.string.library_video_unavailable)
        }
        cancellation.throwIfCanceled()
        val existing = store.items().find { it.family == MediaFamily.VIDEOS && SafDocumentIdentity.key(it.uri) == SafDocumentIdentity.key(uri.toString()) }
        val route = VideoAccessRoute(uri.toString(), availability = Availability.AVAILABLE, observedAt = System.currentTimeMillis())
        store.upsert(existing?.copy(uri = uri.toString(), folderUri = null,
            accessRoutes = existing.accessRoutes.filterNot { it.uri == route.uri && it.folderUri == null } + route,
            availability = Availability.AVAILABLE, observedAt = route.observedAt) ?: LibraryItem(
            id = videoId(uri.toString()), title = title ?: message(R.string.library_video_name), uri = uri.toString(),
            family = MediaFamily.VIDEOS, availability = Availability.AVAILABLE, observedAt = route.observedAt,
            accessRoutes = listOf(route),
        ))
        store.flush()
        if (uri.toString() in persistedReads()) message(R.string.library_video_saved)
        else "${message(R.string.library_video_saved)} ${message(R.string.library_session_only)}"
    }

    fun forgetFolder(uri: String, onComplete: (String) -> Unit) = launch(onComplete) { cancellation ->
        cancellation.throwIfCanceled()
        val known = store.folders().any { it.uri == uri }
        store.removeFolder(uri)
        store.flush()
        synchronized(lock) { sessionUris.remove(uri) }
        // An unknown URI is never sufficient authority to release an unrelated grant.
        if (known) releaseRead(Uri.parse(uri))
        clearLegacyIfMatching(uri)
        message(R.string.library_folder_forgotten)
    }

    /** Removes library metadata only; a document grant is released after durable removal. */
    fun removeItem(id: String, onComplete: (String) -> Unit) = launch(onComplete) { cancellation ->
        cancellation.throwIfCanceled()
        val removed = store.item(id)
        store.removeItem(id)
        store.flush()
        if (removed?.family == MediaFamily.VIDEOS) {
            val legacyFolder = context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE).getString(LEGACY_TREE, null)
            removed.accessRoutes.filter { it.folderUri == null }.map { it.uri }.distinct().forEach { uri ->
                if (legacyFolder != uri) {
                    store.releaseDocumentIfUnused(uri) {
                        synchronized(lock) { sessionUris.remove(uri) }
                        if (uri in persistedReads()) releaseRead(Uri.parse(uri))
                    }
                }
            }
        }
        message(R.string.library_item_removed)
    }

    fun cancel() {
        synchronized(lock) {
            generation++
            signal?.cancel()
            future?.cancel(true)
            signal = null
            future = null
        }
    }

    override fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
            cancel()
            sessionUris.clear()
            worker.shutdownNow()
        }
    }

    private fun launch(onComplete: (String) -> Unit, action: (CancellationSignal) -> String) {
        synchronized(lock) {
            if (closed) return
            cancel()
            val token = generation
            val cancellation = CancellationSignal()
            signal = cancellation
            future = worker.submit {
                val result = try {
                    cancellation.throwIfCanceled()
                    action(cancellation)
                } catch (_: android.os.OperationCanceledException) {
                    return@submit
                } catch (_: LibraryStorageException) {
                    message(R.string.library_storage_failed)
                } catch (_: SecurityException) {
                    message(R.string.library_access_denied)
                } catch (_: Exception) {
                    message(R.string.library_access_failed)
                }
                main.post {
                    val current = synchronized(lock) { !closed && generation == token && !cancellation.isCanceled }
                    if (current) onComplete(result)
                }
            }
        }
    }

    private fun saveScan(uri: String, result: SafVideoScanner.Result.Ready) {
        val now = System.currentTimeMillis()
        store.replaceFolderItems(uri, result.videos.map {
            LibraryItem(videoId(it.uri.toString()), it.title, it.uri.toString(), MediaFamily.VIDEOS,
                folderUri = uri, availability = Availability.AVAILABLE, observedAt = now)
        }, complete = !result.capped)
    }

    private fun markFolderUnavailable(uri: String) {
        store.updateFolderAvailability(uri, Availability.UNAVAILABLE)
    }

    private fun persistedReads(): Set<String> = try {
        resolver.persistedUriPermissions.filter { it.isReadPermission }.mapTo(hashSetOf()) { it.uri.toString() }
    } catch (_: SecurityException) {
        emptySet()
    }

    private fun persistRead(uri: Uri, flags: Int): Boolean {
        if (uri.toString() in persistedReads()) return true
        if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0 || flags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION == 0) return false
        return try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uri.toString() in persistedReads()
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun acceptRead(uri: Uri, flags: Int): Boolean {
        if (persistRead(uri, flags)) {
            synchronized(lock) { sessionUris.remove(uri.toString()) }
            return true
        }
        if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) return false
        synchronized(lock) { sessionUris.add(uri.toString()) }
        return true
    }

    private fun releaseRead(uri: Uri) {
        try {
            resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // Revoked grants already deny access.
        } catch (_: IllegalArgumentException) {
            // A disconnected provider may no longer recognize its document tree.
        }
    }

    private fun canRead(uri: Uri, cancellation: CancellationSignal): Boolean = try {
        resolver.openAssetFileDescriptor(uri, "r", cancellation)?.use { true } ?: false
    } catch (cancelled: android.os.OperationCanceledException) {
        throw cancelled
    } catch (_: Exception) {
        false
    }

    private fun displayName(uri: Uri, cancellation: CancellationSignal): String? = try {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null, cancellation)?.use { rows ->
            val column = rows.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (column < 0 || !rows.moveToFirst()) null
            else rows.getString(column)?.filterNot { it.isISOControl() }?.trim()?.take(120)?.takeIf { it.isNotBlank() }
        }
    } catch (cancelled: android.os.OperationCanceledException) {
        throw cancelled
    } catch (_: RuntimeException) {
        null
    }

    private fun migrateLegacyFolder(grants: Set<String>, cancellation: CancellationSignal) {
        val preferences = context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
        val raw = preferences.getString(LEGACY_TREE, null) ?: return
        val uri = Uri.parse(raw)
        if (!validContentUri(uri) || !DocumentsContract.isTreeUri(uri)) return
        cancellation.throwIfCanceled()
        if (store.folders().none { it.uri == raw }) {
            val document = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
            val granted = raw in grants
            val title = if (granted) displayName(document, cancellation) else null
            store.saveFolder(FolderGrant(raw, title ?: message(R.string.library_folder_name), granted))
        }
        store.flush()
        cancellation.throwIfCanceled()
        if (!preferences.edit().remove(LEGACY_TREE).commit()) throw LibraryStorageException("The previous folder preference could not be migrated safely.")
    }

    private fun clearLegacyIfMatching(uri: String) {
        val preferences = context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
        if (preferences.getString(LEGACY_TREE, null) == uri && !preferences.edit().remove(LEGACY_TREE).commit()) {
            throw LibraryStorageException("The previous folder preference could not be removed.")
        }
    }

    private fun validContentUri(uri: Uri): Boolean = uri.scheme == "content" && !uri.authority.isNullOrBlank() &&
        uri.fragment == null && uri.toString().length <= 4_096 && uri.toString().none { it.isISOControl() }

    private fun message(id: Int, vararg arguments: Any): String {
        val language = store.settings().language
        val localized = if (language == "system") context else {
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(if (language == "pt-BR") Locale.forLanguageTag("pt-BR") else Locale.ENGLISH)
            context.createConfigurationContext(configuration)
        }
        return localized.getString(id, *arguments)
    }

    companion object {
        private const val LEGACY_PREFERENCES = "openvista_saved_folder"
        private const val LEGACY_TREE = "tree_uri"

        private fun videoId(uri: String): String = SafDocumentIdentity.itemId(uri)
    }
}
