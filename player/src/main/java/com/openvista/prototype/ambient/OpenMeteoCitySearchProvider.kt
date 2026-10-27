package com.openvista.prototype.ambient

import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.time.ZoneId
import java.util.Locale

/** Explicit user-submitted city search; no autocomplete, IP lookup or location permission. */
class OpenMeteoCitySearchProvider(language: String) : CitySearchProvider {
    private val language = if (language.startsWith("pt")) "pt" else "en"
    override val attribution = "City data: GeoNames via Open-Meteo (https://open-meteo.com/), CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/)."

    override fun search(query: String): List<WeatherLocation> {
        val value = query.trim()
        require(value.length in 2..100 && value.none(Char::isISOControl))
        val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=" +
            URLEncoder.encode(value, "UTF-8") + "&count=10&format=json&language=$language")
        val root = JSONObject(ProviderHttp.bytes(url, "geocoding-api.open-meteo.com", 131_072, "application/json").toString(Charsets.UTF_8))
        require(!root.optBoolean("error"))
        val results = root.optJSONArray("results") ?: return emptyList()
        return (0 until results.length().coerceAtMost(10)).mapNotNull { index ->
            val place = results.optJSONObject(index) ?: return@mapNotNull null
            val latitude = place.optDouble("latitude"); val longitude = place.optDouble("longitude")
            val zone = runCatching { ZoneId.of(place.getString("timezone")) }.getOrNull() ?: return@mapNotNull null
            if (!latitude.isFinite() || latitude !in -90.0..90.0 || !longitude.isFinite() || longitude !in -180.0..180.0) return@mapNotNull null
            val name = listOf("name", "admin1", "country").map { key ->
                place.optString(key).filterNot(Char::isISOControl).trim().take(100)
            }.filter(String::isNotBlank).distinct().joinToString(", ").take(100)
            if (name.isBlank()) null else WeatherLocation(name, latitude, longitude, zone.id)
        }.distinctBy { String.format(Locale.US, "%.4f/%.4f/%s", it.latitude, it.longitude, it.timeZone) }
    }
}
