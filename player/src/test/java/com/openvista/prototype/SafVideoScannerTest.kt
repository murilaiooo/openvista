package com.openvista.prototype

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import android.provider.DocumentsContract
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowContentResolver
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SafVideoScannerTest {
    @Test fun findsOnlyVideosInsideSelectedTreeWithoutFullStorageAccess() {
        val app = RuntimeEnvironment.getApplication()
        val provider = TestTreeProvider()
        ShadowContentResolver.registerProviderInternal(AUTHORITY, provider)
        val tree = DocumentsContract.buildTreeDocumentUri(AUTHORITY, "root")
        val result = SafVideoScanner(app.contentResolver).scan(tree, CancellationSignal())
        assertTrue(result is SafVideoScanner.Result.Ready)
        val ready = result as SafVideoScanner.Result.Ready
        assertEquals(2, ready.videos.size)
        assertEquals(listOf("holiday.mp4", "surf.webm"), ready.videos.map { it.title })
        assertTrue(ready.videos.all { it.uri.authority == AUTHORITY })
        assertFalse(ready.capped)
    }

    @Test fun cancellationReturnsUnavailableWithoutReadingUserFiles() {
        val app = RuntimeEnvironment.getApplication()
        val provider = TestTreeProvider()
        ShadowContentResolver.registerProviderInternal(AUTHORITY, provider)
        val cancellation = CancellationSignal().apply { cancel() }
        val result = SafVideoScanner(app.contentResolver).scan(
            DocumentsContract.buildTreeDocumentUri(AUTHORITY, "root"),
            cancellation,
        )
        assertEquals(SafVideoScanner.Result.Unavailable, result)
    }

    class TestTreeProvider : ContentProvider() {
        override fun onCreate() = true
        override fun query(
            uri: Uri, projection: Array<out String>?, selection: String?,
            selectionArgs: Array<out String>?, sortOrder: String?,
        ): Cursor {
            val fields = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            )
            val rows = MatrixCursor(fields)
            when {
                uri.toString().contains("/document/root/children") -> {
                    rows.addRow(arrayOf("root/holiday", "video/mp4", "holiday.mp4"))
                    rows.addRow(arrayOf("root/photo", "image/jpeg", "photo.jpg"))
                    rows.addRow(arrayOf("root/folder", DocumentsContract.Document.MIME_TYPE_DIR, "folder"))
                }
                uri.toString().contains("/document/root%2Ffolder/children") -> {
                    rows.addRow(arrayOf("root/folder/surf", "video/webm", "surf.webm"))
                }
            }
            return rows
        }
        override fun getType(uri: Uri): String? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    }

    companion object { private const val AUTHORITY = "com.openvista.test.documents" }
}
