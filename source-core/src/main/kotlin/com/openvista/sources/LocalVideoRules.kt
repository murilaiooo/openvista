package com.openvista.sources

import java.util.Locale

/**
 * Filters metadata from user-selected folders. Codec support still requires playback proof.
 */
object LocalVideoRules {
    private val supportedMimeTypes = setOf(
        "video/mp4",
        "video/x-matroska",
        "video/webm",
        "video/quicktime",
    )
    private val supportedExtensions = setOf("mp4", "mkv", "webm", "mov")

    fun isSupported(mimeType: String?, displayName: String?): Boolean {
        val mime = mimeType?.lowercase(Locale.ROOT)?.trim()
        if (mime in supportedMimeTypes) return true
        if (mime != null && mime != "application/octet-stream") return false
        val extension = displayName?.substringAfterLast('.', "")?.lowercase(Locale.ROOT)
        return extension in supportedExtensions
    }

    fun displayTitle(raw: String?): String {
        val basename = raw.orEmpty().substringAfterLast('/').substringAfterLast('\\')
        return basename.filterNot { it.isISOControl() }.trim().take(80).ifBlank { "file" }
    }
}
