package com.openvista.prototype

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

/** Remembers only an explicitly selected SAF tree with an OS-backed read grant. */
internal class SavedFolderAccess(private val context: Context) {
    private val resolver get() = context.contentResolver
    private val preferences get() = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun remember(uri: Uri): Boolean {
        if (uri.scheme != "content" || uri.authority.isNullOrBlank() || !DocumentsContract.isTreeUri(uri)) return false
        val alreadyGranted = try {
            resolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
        } catch (_: SecurityException) { false }
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            return false
        } catch (_: IllegalArgumentException) {
            return false
        }
        val previous = preferences.getString(KEY_FOLDER, null)
        if (!preferences.edit().putString(KEY_FOLDER, uri.toString()).commit()) {
            if (!alreadyGranted && previous != uri.toString()) release(uri)
            return false
        }
        if (previous != null && previous != uri.toString()) {
            release(Uri.parse(previous))
        }
        return true
    }

    fun restore(): Uri? {
        val stored = preferences.getString(KEY_FOLDER, null) ?: return null
        val uri = Uri.parse(stored)
        val granted = try {
            resolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
        } catch (_: SecurityException) {
            false
        }
        // A temporarily disconnected provider must not erase the saved folder selection.
        return if (granted) uri else null
    }

    fun forget() {
        val stored = preferences.getString(KEY_FOLDER, null)
        if (!preferences.edit().remove(KEY_FOLDER).commit()) return
        if (stored != null) release(Uri.parse(stored))
    }

    private fun release(uri: Uri) {
        try {
            resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // The OS or provider has already revoked this grant.
        } catch (_: IllegalArgumentException) {
            // The document provider no longer recognizes the URI.
        }
    }

    companion object {
        private const val PREFERENCES = "openvista_saved_folder"
        private const val KEY_FOLDER = "tree_uri"
    }
}
