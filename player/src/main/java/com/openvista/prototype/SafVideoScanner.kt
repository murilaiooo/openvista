package com.openvista.prototype

import android.content.ContentResolver
import android.net.Uri
import android.os.CancellationSignal
import android.provider.DocumentsContract
import com.openvista.sources.LocalVideoRules
import java.util.ArrayDeque

/** Enumerates only documents reachable under a user-selected SAF tree URI. */
internal class SafVideoScanner(private val resolver: ContentResolver) {
    data class Video(val uri: Uri, val title: String)
    sealed interface Result {
        data class Ready(val videos: List<Video>, val capped: Boolean) : Result
        data object Unavailable : Result
    }

    fun scan(treeUri: Uri, cancellation: CancellationSignal): Result {
        cancellation.throwIfCanceled()
        val root = try { DocumentsContract.getTreeDocumentId(treeUri) }
        catch (_: RuntimeException) { return Result.Unavailable }
        val queue = ArrayDeque<String>()
        queue.add(root)
        val visitedDirectories = hashSetOf(root)
        val visitedVideos = hashSetOf<String>()
        val videos = mutableListOf<Video>()
        var inspected = 0
        var capped = false
        var incomplete = false
        return try {
            while (queue.isNotEmpty()) {
                cancellation.throwIfCanceled()
                val parent = queue.removeFirst()
                val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parent)
                try {
                    val cursor = resolver.query(children, PROJECTION, null, null, null, cancellation)
                    if (cursor == null) {
                        if (parent == root) return Result.Unavailable
                        incomplete = true
                        continue
                    }
                    cursor.use { rows ->
                        if (rows.extras.getBoolean(DocumentsContract.EXTRA_LOADING, false)) incomplete = true
                        val idColumn = rows.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                        val mimeColumn = rows.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                        val titleColumn = rows.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                        while (rows.moveToNext()) {
                            cancellation.throwIfCanceled()
                            inspected++
                            if (inspected > MAX_ENTRIES) {
                                capped = true
                                break
                            }
                            val id = rows.getString(idColumn)?.takeIf { it.isNotEmpty() && it.length <= 4_096 && it.none { character -> character.isISOControl() } }
                            if (id == null) {
                                incomplete = true
                                continue
                            }
                            val mime = rows.getString(mimeColumn)
                            val title = rows.getString(titleColumn)
                            if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                                if (visitedDirectories.add(id)) {
                                    queue.add(id)
                                }
                            } else if (LocalVideoRules.isSupported(mime, title) && visitedVideos.add(id)) {
                                videos += Video(
                                    uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id),
                                    title = LocalVideoRules.displayTitle(title),
                                )
                                if (videos.size == MAX_VIDEOS) {
                                    capped = true
                                    break
                                }
                            }
                        }
                    }
                } catch (cancelled: android.os.OperationCanceledException) {
                    throw cancelled
                } catch (_: RuntimeException) {
                    if (parent == root) return Result.Unavailable
                    // One unreadable child folder does not hide accessible sibling folders.
                    incomplete = true
                }
                if (capped) break
            }
            cancellation.throwIfCanceled()
            Result.Ready(videos, capped || incomplete)
        } catch (cancelled: android.os.OperationCanceledException) {
            throw cancelled
        } catch (_: SecurityException) {
            Result.Unavailable
        } catch (_: RuntimeException) {
            Result.Unavailable
        }
    }

    companion object {
        const val MAX_VIDEOS = 2_000
        const val MAX_ENTRIES = 20_000
        private val PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        )
    }
}
