package com.openvista.sources

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.util.Locale

enum class StreamKind { HLS, DASH }

data class UserSource(val title: String, val url: String, val kind: StreamKind)

enum class PlaylistFailure {
    FILE_TOO_LARGE, INVALID_UTF8, INVALID_PLAYLIST, HLS_MANIFEST,
    TOO_MANY_ENTRIES, NO_SUPPORTED_SOURCES, READ_FAILED
}

sealed interface PlaylistResult {
    data class Ready(
        val sources: List<UserSource>,
        val skippedEntries: Int,
        val duplicateEntries: Int,
    ) : PlaylistResult

    data class Rejected(val reason: PlaylistFailure) : PlaylistResult
}

/**
 * Parses only bytes already selected and read by the caller.
 * This class does not open URLs, access storage, log credentials, or persist sources.
 */
class UserSourceParser {
    fun manualUrl(rawUrl: String, kind: StreamKind, title: String = "My Stream"): UserSource? {
        val url = validUrl(rawUrl) ?: return null
        return UserSource(cleanTitle(title, 1), url, kind)
    }

    fun detectKind(rawUrl: String): StreamKind? {
        val url = validUrl(rawUrl) ?: return null
        val path = try { URI(url).path?.lowercase(Locale.ROOT) } catch (_: Exception) { null }
        return when {
            path?.endsWith(".m3u8") == true -> StreamKind.HLS
            path?.endsWith(".mpd") == true -> StreamKind.DASH
            else -> null
        }
    }

    /**
     * Read at most MAX_FILE_BYTES + 1 bytes from a selected document.
     * The caller owns the document selection and this method closes the stream.
     */
    fun parseM3u(input: InputStream): PlaylistResult {
        return try {
            input.use { stream ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8_192)
                var emptyReads = 0
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    if (count == 0) {
                        if (++emptyReads >= MAX_EMPTY_READS) {
                            return PlaylistResult.Rejected(PlaylistFailure.READ_FAILED)
                        }
                        continue
                    }
                    emptyReads = 0
                    if (output.size() + count > MAX_FILE_BYTES) {
                        return PlaylistResult.Rejected(PlaylistFailure.FILE_TOO_LARGE)
                    }
                    output.write(buffer, 0, count)
                }
                parseM3u(output.toByteArray())
            }
        } catch (_: IOException) {
            PlaylistResult.Rejected(PlaylistFailure.READ_FAILED)
        } catch (_: SecurityException) {
            PlaylistResult.Rejected(PlaylistFailure.READ_FAILED)
        }
    }

    fun parseM3u(bytes: ByteArray): PlaylistResult {
        if (bytes.size > MAX_FILE_BYTES) {
            return PlaylistResult.Rejected(PlaylistFailure.FILE_TOO_LARGE)
        }
        val text = try {
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString().removePrefix("\uFEFF")
        } catch (_: CharacterCodingException) {
            return PlaylistResult.Rejected(PlaylistFailure.INVALID_UTF8)
        }
        val lines = text.lineSequence().map { it.trim() }.toList()
        if (lines.size > MAX_LINES) {
            return PlaylistResult.Rejected(PlaylistFailure.TOO_MANY_ENTRIES)
        }
        val first = lines.firstOrNull { it.isNotBlank() }
        if (first == null || (!first.equals("#EXTM3U", ignoreCase = true) && !first.startsWith("#EXTM3U ", ignoreCase = true))) {
            return PlaylistResult.Rejected(PlaylistFailure.INVALID_PLAYLIST)
        }
        // #EXT-X tags mark an HLS manifest, not a user-managed channel list.
        if (lines.any { it.startsWith("#EXT-X-", ignoreCase = true) }) {
            return PlaylistResult.Rejected(PlaylistFailure.HLS_MANIFEST)
        }

        val sources = mutableListOf<UserSource>()
        val seen = HashSet<String>()
        var skipped = 0
        var duplicates = 0
        var entries = 0
        var pendingTitle: String? = null

        for (line in lines.dropWhile { it.isBlank() }.drop(1)) {
            if (line.isBlank()) continue
            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                pendingTitle = if (line.contains(',')) line.substringAfterLast(',') else null
                continue
            }
            if (line.startsWith("#")) continue
            entries++
            if (entries > MAX_ENTRIES) {
                return PlaylistResult.Rejected(PlaylistFailure.TOO_MANY_ENTRIES)
            }
            val url = validUrl(line)
            val kind = if (url != null) detectKind(url) else null
            if (url == null || kind == null) {
                skipped++
            } else if (!seen.add(url)) {
                duplicates++
            } else {
                sources += UserSource(cleanTitle(pendingTitle, entries), url, kind)
            }
            pendingTitle = null
        }
        return if (sources.isEmpty()) {
            PlaylistResult.Rejected(PlaylistFailure.NO_SUPPORTED_SOURCES)
        } else {
            PlaylistResult.Ready(sources, skipped, duplicates)
        }
    }

    private fun validUrl(rawUrl: String): String? {
        val url = rawUrl.trim()
        if (url.isBlank() || url.length > MAX_URL_LENGTH) return null
        if (url.any { it.isISOControl() || it.isWhitespace() }) return null
        val parsed = try { URI(url) } catch (_: Exception) { return null }
        val scheme = parsed.scheme?.lowercase(Locale.ROOT)
        if (scheme != "http" && scheme != "https") return null
        if (parsed.host.isNullOrBlank() || parsed.userInfo != null) return null
        if (parsed.port > 65535 || parsed.port == 0) return null
        return url
    }

    private fun cleanTitle(raw: String?, index: Int): String {
        val value = raw.orEmpty()
            .filterNot { it.isISOControl() }
            .trim().take(MAX_TITLE_LENGTH)
        return value.ifBlank { "Stream $index" }
    }

    companion object {
        const val MAX_FILE_BYTES = 1_048_576
        const val MAX_URL_LENGTH = 4_096
        const val MAX_ENTRIES = 500
        private const val MAX_EMPTY_READS = 8
        const val MAX_LINES = 10_000
        const val MAX_TITLE_LENGTH = 100
    }
}
