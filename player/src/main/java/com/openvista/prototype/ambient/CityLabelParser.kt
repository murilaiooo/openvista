package com.openvista.prototype.ambient

import com.openvista.prototype.library.LibraryItem
import java.util.Locale
import java.text.Normalizer

/** Only the location-like segment of a user title may be sent to the city search service. */
internal object CityLabelParser {
    private val resolution = Regex("""\([^)]*(?:\d{3,4}\s*[x×]\s*\d{3,4}|\d+[kK])[^)]*\)""")
    private val parenthetical = Regex("""\([^)]*\)""")
    private val unsafe = Regex("""(?i)(https?://|www\.|[/\\]|\?|=|@|\.m3u8?|\.mpd|\.mp4|\.mkv)""")
    private val garbage = Regex("""(?i)^(qa\b|test\b|demo\b|camera\s*\d+|video\b|live\b|unknown\b|untitled\b)""")
    private val suffix = Regex("""(?i)\b(?:cam|webcam|live|stream|4k|8k|hd|uhd)\b""")
    fun queries(item: LibraryItem): List<String> {
        val explicit = item.city.trim().takeIf { it.length in 2..64 }
        if (explicit != null) {
            val qualifier = item.country.trim().takeIf { it.length in 2..48 }
            return listOfNotNull(if (qualifier != null) "$explicit, $qualifier" else explicit, explicit).distinct()
        }
        val title = item.title.trim().takeIf { it.length in 2..100 && !unsafe.containsMatchIn(it) && !garbage.containsMatchIn(it) }
            ?: return emptyList()
        val base = suffix.replace(resolution.replace(title, "").substringBefore(" — ").substringBefore(" - "), "")
            .let { parenthetical.replace(it, "") }.trim(' ', '-', ',', '·', ':')
        if (base.length !in 3..65 || base.any { it.isDigit() } || base.split(' ').size > 4) return emptyList()
        val variants = mutableListOf(base)
        if (base.split(' ').size == 2) variants.add(base.substringBeforeLast(' '))
        return variants.distinct().filter { it.length >= 3 }
    }

    fun displayCity(geocoderName: String): String =
        geocoderName.substringBefore(',').trim().take(65)

    private fun normalize(value: String): String = Normalizer.normalize(
        value.lowercase(Locale.ROOT).replace('ł', 'l'), Normalizer.Form.NFD
    ).replace(Regex("""\p{Mn}+"""), "")

    fun matches(query: String, resultName: String): Boolean {
        val city = displayCity(resultName)
        return normalize(city) == normalize(query.substringBefore(',').trim())
    }
}
