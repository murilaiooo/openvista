package com.openvista.prototype.ambient

import java.text.Normalizer
import java.util.Locale

/**
 * Resolves a camera's country from its saved metadata, in whatever form the user's list used
 * ("IT", "ITA", "Italy", "Itália", "United States of America"…), and otherwise from the camera's
 * own time zone. It never uses the viewer's location or a stream title.
 *
 * The result is shown as a localized country name with its flag, so a list written in English
 * still reads correctly in the Portuguese interface and vice versa.
 */
internal object CountryResolver {
    private val isoCodes: Set<String> = Locale.getISOCountries().toSet()
    private val nameLocales = listOf(Locale.ENGLISH, Locale("pt"), Locale("es"), Locale.FRENCH,
        Locale.GERMAN, Locale.ITALIAN, Locale("nl"))
    private val aliases = mapOf(
        "usa" to "US", "us" to "US", "united states of america" to "US", "america" to "US", "eua" to "US",
        "estados unidos da america" to "US", "uk" to "GB", "great britain" to "GB", "britain" to "GB",
        "england" to "GB", "scotland" to "GB", "wales" to "GB", "northern ireland" to "GB",
        "inglaterra" to "GB", "escocia" to "GB", "holland" to "NL", "holanda" to "NL",
        "czech republic" to "CZ", "czechia" to "CZ", "republica tcheca" to "CZ", "south korea" to "KR", "korea" to "KR",
        "coreia do sul" to "KR", "russia" to "RU", "uae" to "AE", "emirados arabes" to "AE",
        "vatican" to "VA", "ivory coast" to "CI", "turkey" to "TR", "turquia" to "TR",
    )
    private val byName: Map<String, String> by lazy {
        val names = HashMap<String, String>()
        isoCodes.forEach { code ->
            val region = Locale("", code)
            names.putIfAbsent(key(code), code)
            runCatching { region.isO3Country }.getOrNull()?.takeIf(String::isNotBlank)?.let { names.putIfAbsent(key(it), code) }
            nameLocales.forEach { locale -> names.putIfAbsent(key(region.getDisplayCountry(locale)), code) }
        }
        aliases.forEach { (alias, code) -> names[key(alias)] = code }
        names
    }

    /** ISO 3166-1 alpha-2 code, or null when neither metadata nor time zone identifies a country. */
    fun resolve(country: String, timeZone: String?, zoneRegion: (String) -> String? = ::icuRegion): String? {
        val saved = country.trim()
        if (saved.isNotEmpty()) byName[key(saved)]?.let { return it }
        val zone = timeZone?.trim().orEmpty()
        if (zone.isEmpty()) return null
        return zoneRegion(zone)?.uppercase(Locale.ROOT)?.takeIf { it.length == 2 && it in isoCodes }
    }

    /**
     * The name to display: the localized name of the resolved country, or the saved text when it
     * names something we cannot map (a region or territory), so user metadata is never dropped.
     */
    fun displayName(country: String, code: String?, locale: Locale): String {
        val saved = country.trim()
        if (saved.isNotEmpty() && byName[key(saved)] == null) return saved
        return code?.let { Locale("", it).getDisplayCountry(locale) }?.takeIf(String::isNotBlank) ?: saved
    }

    /** Regional-indicator flag; rendered by the system emoji font. */
    fun flag(code: String): String {
        if (code.length != 2 || code.any { it !in 'A'..'Z' }) return ""
        return String(Character.toChars(0x1F1E6 + (code[0] - 'A'))) + String(Character.toChars(0x1F1E6 + (code[1] - 'A')))
    }

    private fun key(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()

    /** Android's ICU maps a canonical zone to its country ("Europe/Rome" → "IT", "Etc/UTC" → "001"). */
    private fun icuRegion(zone: String): String? = try {
        android.icu.util.TimeZone.getRegion(zone)
    } catch (_: RuntimeException) { null }
}
