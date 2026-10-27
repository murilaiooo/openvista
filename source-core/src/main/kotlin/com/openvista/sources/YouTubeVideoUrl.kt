package com.openvista.sources

import java.net.URI
import java.util.Locale

/**
 * Extracts a concrete video ID from a user-supplied YouTube link.
 * Channel live pages and playlists do not have a stable video ID and are intentionally rejected.
 * Never accepts arbitrary hosts or treats YouTube URLs as Media3 stream URLs.
 */
object YouTubeVideoUrl {
    private val idPattern = Regex("[A-Za-z0-9_-]{11}")
    private val youtubeHosts = setOf(
        "youtube.com", "www.youtube.com", "m.youtube.com",
        "youtube-nocookie.com", "www.youtube-nocookie.com",
    )
    private val shortHosts = setOf("youtu.be", "www.youtu.be")

    fun videoId(input: String): String? {
        val candidate = input.trim()
        if (candidate.isEmpty() || candidate.length > 2048 || candidate.any { it.isWhitespace() || it.isISOControl() }) {
            return null
        }
        val url = try { URI(candidate) } catch (_: Exception) { return null }
        if (!url.scheme.equals("https", ignoreCase = true) || url.rawUserInfo != null ||
            url.port != -1 || url.rawFragment != null
        ) return null
        val host = url.host?.lowercase(Locale.ROOT) ?: return null
        val parts = url.rawPath.orEmpty().trim('/').split('/').filter(String::isNotEmpty)
        val id = when {
            host in shortHosts && parts.size == 1 -> parts[0]
            host in youtubeHosts && parts.size == 2 && parts[0] in setOf("live", "embed", "shorts") -> parts[1]
            host in youtubeHosts && parts == listOf("watch") ->
                url.rawQuery?.split('&')?.mapNotNull { parameter ->
                    val key = parameter.substringBefore('=')
                    if (key == "v" && '=' in parameter) parameter.substringAfter('=') else null
                }?.singleOrNull()
            else -> null
        }
        return id?.takeIf { idPattern.matches(it) }
    }
}
