package com.openvista.sources

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.time.ZoneId
import java.util.Locale

enum class StreamKind { HLS, DASH }

/**
 * Grouping and location a list declares for one entry. Every field is optional and only holds
 * what the list itself states; nothing is inferred from a title or URL.
 */
data class SourceMetadata(
    val category: String = "",
    val country: String = "",
    val city: String = "",
    val timeZone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class UserSource(val title: String, val url: String, val kind: StreamKind, val metadata: SourceMetadata = SourceMetadata())

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
        var pendingAttributes = emptyMap<String, String>()
        var pendingGroup: String? = null

        for (line in lines.dropWhile { it.isBlank() }.drop(1)) {
            if (line.isBlank()) continue
            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                val (attributes, title) = extinf(line.substring("#EXTINF:".length))
                pendingAttributes = attributes
                pendingTitle = title
                continue
            }
            if (line.startsWith("#EXTGRP:", ignoreCase = true)) {
                pendingGroup = line.substring("#EXTGRP:".length)
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
                sources += UserSource(cleanTitle(pendingTitle, entries), url, kind, metadata(pendingAttributes, pendingGroup))
            }
            pendingTitle = null
            pendingAttributes = emptyMap()
            pendingGroup = null
        }
        return if (sources.isEmpty()) {
            PlaylistResult.Rejected(PlaylistFailure.NO_SUPPORTED_SOURCES)
        } else {
            PlaylistResult.Ready(sources, skipped, duplicates)
        }
    }

    /**
     * Splits `-1 key="value" key=value,Title` at the first comma outside quotes, so attribute
     * values and titles may both contain commas ("Rialto Bridge, Venice").
     */
    private fun extinf(body: String): Pair<Map<String, String>, String?> {
        var quote: Char? = null
        var split = -1
        var previous = ' '
        for ((index, char) in body.withIndex()) {
            if (quote != null) { if (char == quote) quote = null; continue }
            // Only a quote that opens a value counts, so "O'Hare" in an unquoted value is plain text.
            if ((char == '"' || char == '\'') && previous == '=') quote = char
            else if (char == ',') { split = index; break }
            if (!char.isWhitespace()) previous = char
        }
        val head = if (split < 0) body else body.substring(0, split)
        val attributes = HashMap<String, String>()
        ATTRIBUTE.findAll(head).take(MAX_ATTRIBUTES).forEach { match ->
            val value = match.groupValues.drop(2).firstOrNull(String::isNotEmpty).orEmpty()
            attributes.putIfAbsent(match.groupValues[1].lowercase(Locale.ROOT), value)
        }
        return attributes to if (split < 0) null else body.substring(split + 1)
    }

    private fun metadata(attributes: Map<String, String>, group: String?): SourceMetadata {
        fun first(vararg keys: String) = keys.firstNotNullOfOrNull { key -> attributes[key]?.let(::cleanText)?.takeIf(String::isNotEmpty) }.orEmpty()
        // Lists such as iptv-org separate several countries with ';'; the first is the camera's own.
        val country = first("tvg-country", "country").split(';', '|').first().trim()
        val zone = first("tvg-timezone", "timezone", "time-zone", "tz").takeIf { it in REGION_ZONES }.orEmpty()
        val latitude = first("latitude", "tvg-latitude", "lat").toDoubleOrNull()?.takeIf { it.isFinite() && it in -90.0..90.0 }
        val longitude = first("longitude", "tvg-longitude", "lon", "lng").toDoubleOrNull()?.takeIf { it.isFinite() && it in -180.0..180.0 }
        val paired = latitude != null && longitude != null
        return SourceMetadata(
            category = first("group-title").ifEmpty { group?.let(::cleanText).orEmpty() },
            country = country, city = first("tvg-city", "city"), timeZone = zone,
            latitude = latitude.takeIf { paired }, longitude = longitude.takeIf { paired },
        )
    }

    private fun cleanText(raw: String): String = raw.filterNot { it.isISOControl() }.trim().take(MAX_METADATA_LENGTH)

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
        const val MAX_METADATA_LENGTH = 100
        private const val MAX_ATTRIBUTES = 32
        private val ATTRIBUTE = Regex("""([A-Za-z][A-Za-z0-9_-]*)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s"',]+))""")
        // Region IDs only ("Europe/Rome"), so a fixed offset cannot hide daylight-saving changes.
        private val REGION_ZONES: Set<String> = ZoneId.getAvailableZoneIds().filter { '/' in it && !it.startsWith("Etc/") }.toSet()
    }
}
