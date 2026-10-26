package com.openvista.prototype

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Remembers only an explicitly selected SAF tree with an OS-backed read grant. */
internal class SavedFolderAccess(private val context: Context) {
    private val resolver get() = context.contentResolver
    private val preferences get() = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun remember(uri: Uri): Boolean {
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            return false
        } catch (_: IllegalArgumentException) {
            return false
        }
        val previous = preferences.getString(KEY_FOLDER, null)
        preferences.edit().putString(KEY_FOLDER, uri.toString()).apply()
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
        if (!granted) preferences.edit().remove(KEY_FOLDER).apply()
        return if (granted) uri else null
    }

    fun forget() {
        val stored = preferences.getString(KEY_FOLDER, null)
        preferences.edit().remove(KEY_FOLDER).apply()
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
