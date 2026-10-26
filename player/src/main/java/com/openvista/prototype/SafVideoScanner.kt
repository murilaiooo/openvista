package com.openvista.prototype

import android.content.ContentResolver
import android.database.Cursor
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
        val root = try { DocumentsContract.getTreeDocumentId(treeUri) }
        catch (_: RuntimeException) { return Result.Unavailable }
        val queue = ArrayDeque<Pair<String, Int>>()
        queue.add(root to 0)
        val visitedDirectories = hashSetOf(root)
        val visitedVideos = hashSetOf<String>()
        val videos = mutableListOf<Video>()
        var inspected = 0
        var capped = false
        return try {
            while (queue.isNotEmpty()) {
                if (cancellation.isCanceled) return Result.Unavailable
                val (parent, depth) = queue.removeFirst()
                val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parent)
                val cursor = resolver.query(children, PROJECTION, null, null, null, cancellation)
                    ?: return Result.Unavailable
                cursor.use { rows ->
                    val idColumn = rows.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val mimeColumn = rows.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val titleColumn = rows.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    while (!cancellation.isCanceled && rows.moveToNext()) {
                        inspected++
                        if (inspected > MAX_ENTRIES) {
                            capped = true
                            break
                        }
                        val id = rows.getString(idColumn) ?: continue
                        val mime = rows.getString(mimeColumn)
                        val title = rows.getString(titleColumn)
                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            if (depth < MAX_DIRECTORY_DEPTH && visitedDirectories.add(id)) {
                                queue.add(id to depth + 1)
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
                if (capped) break
            }
            Result.Ready(videos, capped)
        } catch (_: SecurityException) {
            Result.Unavailable
        } catch (_: RuntimeException) {
            Result.Unavailable
        }
    }

    companion object {
        const val MAX_VIDEOS = 100
        const val MAX_ENTRIES = 500
        const val MAX_DIRECTORY_DEPTH = 3
        private val PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        )
    }
}
