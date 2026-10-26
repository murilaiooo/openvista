package com.openvista.prototype.ambient

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URL
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

/**
 * Official US National Weather Service point forecasts (api.weather.gov).
 *
 * Public-domain forecast data; no API key, analytics, advertising or user account.
 * The mandatory identifying User-Agent header is sent privately with each request.
 * A weather-provider credit is retained internally, not drawn over the owner's media.
 *
 * City geocoding, radar and locations outside NWS forecast coverage are not provided.
 * Hourly forecast temperature is an estimate, NOT a station observation.
 */
class NwsForecastProvider(
    private val fetchJson: (URL) -> JSONObject = ::requestJson,
    private val clock: () -> Instant = Instant::now,
) : ForecastProvider {
    override val id = "us-nws-v1"
    override val attribution = "U.S. National Weather Service"
    override val minimumRefreshMs = 30 * 60_000L
    override val maximumCacheAgeMs = 24 * 60 * 60_000L

    override fun fetch(location: WeatherLocation): Forecast {
        require(location.latitude.isFinite() && location.latitude in -90.0..90.0 &&
            location.longitude.isFinite() && location.longitude in -180.0..180.0)
        // The NWS points API accepts no more than four decimal places.
        val coordinates = String.format(Locale.US, "%.4f,%.4f", location.latitude, location.longitude)
        val point = fetchJson(URL("https://api.weather.gov/points/$coordinates")).getJSONObject("properties")
        val dailyUrl = nwsUrl(point.getString("forecast"))
        val hourlyUrl = nwsUrl(point.getString("forecastHourly"))
        val daily = fetchJson(dailyUrl).getJSONObject("properties").getJSONArray("periods")
        val hourly = try {
            fetchJson(hourlyUrl).getJSONObject("properties").getJSONArray("periods")
        } catch (_: Exception) {
            // A failing hourly endpoint must not mask a valid seven-day forecast.
            JSONArray()
        }
        val now = clock()
        val days = parseDays(daily, hourly, now)
        require(days.isNotEmpty()) { "NWS did not provide valid forecast periods." }
        return Forecast(location, parseCurrent(hourly, now), days, now.toEpochMilli(), attribution, id)
    }

    private fun parseCurrent(periods: JSONArray, now: Instant): CurrentWeather? {
        val current = (0 until periods.length().coerceAtMost(200))
            .mapNotNull { index -> periods.optJSONObject(index) }
            .firstOrNull { p ->
                val start = instant(p.optString("startTime")) ?: return@firstOrNull false
                val end = instant(p.optString("endTime")) ?: return@firstOrNull false
                !now.isBefore(start) && now.isBefore(end)
            } ?: return null
        return CurrentWeather(
            temperatureC = temperatureC(current),
            description = current.optString("shortForecast").takeIf(String::isNotBlank)?.take(120),
            humidity = current.optJSONObject("relativeHumidity")?.optNumber("value"),
            windKph = windKph(current.optString("windSpeed")),
            // NWS probability-of-precipitation is a percentage, NOT measured millimeters.
            precipitationMm = null,
        )
    }

    private fun parseDays(periods: JSONArray, hourly: JSONArray, now: Instant): List<ForecastDay> {
        data class Day(var min: Double? = null, var max: Double? = null,
                       var wind: Double? = null, var chance: Double? = null)
        val dates = linkedMapOf<String, Day>()
        val today = now.atOffset(java.time.ZoneOffset.UTC).toLocalDate().minusDays(1)
        for (i in 0 until periods.length().coerceAtMost(20)) {
            val p = periods.optJSONObject(i) ?: continue
            val start = runCatching { OffsetDateTime.parse(p.optString("startTime")) }.getOrNull() ?: continue
            if (start.toLocalDate().isBefore(today)) continue
            val key = start.toLocalDate().toString()
            val day = dates.getOrPut(key) { Day() }
            val temperature = temperatureC(p)
            if (p.optBoolean("isDaytime")) day.max = temperature ?: day.max
            else day.min = temperature ?: day.min
            day.wind = listOfNotNull(day.wind, windKph(p.optString("windSpeed"))).maxOrNull()
            val chance = p.optJSONObject("probabilityOfPrecipitation")?.optNumber("value")
                ?.takeIf { it in 0.0..100.0 }
            day.chance = listOfNotNull(day.chance, chance).maxOrNull()
        }

        val hourlyHumidity = mutableMapOf<String, MutableList<Double>>()
        for (i in 0 until hourly.length().coerceAtMost(200)) {
            val p = hourly.optJSONObject(i) ?: continue
            val start = runCatching { OffsetDateTime.parse(p.optString("startTime")) }.getOrNull() ?: continue
            val value = p.optJSONObject("relativeHumidity")?.optNumber("value")
                ?.takeIf { it in 0.0..100.0 } ?: continue
            hourlyHumidity.getOrPut(start.toLocalDate().toString()) { mutableListOf() }.add(value)
        }

        return dates.toSortedMap().entries.take(7).map { (date, values) ->
            val humidity = hourlyHumidity[date]?.takeIf { it.isNotEmpty() }?.average()
            ForecastDay(date, values.min, values.max, null, values.wind, humidity, null, null, values.chance)
        }
    }

    private fun temperatureC(period: JSONObject): Double? {
        val temp = period.optNumber("temperature") ?: return null
        return when (period.optString("temperatureUnit")) {
            "F" -> (temp - 32.0) * 5.0 / 9.0
            "C" -> temp
            else -> null
        }
    }

    private fun windKph(wind: String): Double? {
        if (wind.equals("Calm", ignoreCase = true)) return 0.0
        val speeds = Regex("\\d+(?:\\.\\d+)?").findAll(wind).mapNotNull { it.value.toDoubleOrNull() }.toList()
        val max = speeds.maxOrNull() ?: return null
        return when {
            wind.contains("mph", ignoreCase = true) -> max * 1.609344
            wind.contains("km/h", ignoreCase = true) -> max
            else -> null
        }
    }

    private fun instant(value: String): Instant? =
        try { OffsetDateTime.parse(value).toInstant() } catch (_: DateTimeParseException) { null }

    private fun nwsUrl(raw: String): URL {
        val url = URL(raw)
        require(url.protocol == "https" && url.host.equals("api.weather.gov", ignoreCase = true) &&
            url.port == -1 && url.userInfo == null && url.ref == null &&
            url.path.startsWith("/gridpoints/") && url.query == null) {
            "Forecast endpoint is not an official NWS gridpoint."
        }
        return url
    }

    private fun JSONObject.optNumber(name: String): Double? =
        if (!has(name) || isNull(name)) null else optDouble(name).takeIf(Double::isFinite)

    companion object {
        private const val MAX_RESPONSE = 1_048_576
        private const val USER_AGENT = "OpenVista/0.2 (https://github.com/murilaiooo/openvista)"

        private fun requestJson(url: URL): JSONObject {
            require(url.protocol == "https" && url.host.equals("api.weather.gov", ignoreCase = true) &&
                url.port == -1 && url.userInfo == null && url.ref == null)
            val conn = url.openConnection() as HttpsURLConnection
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 7000
            conn.readTimeout = 7000
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.setRequestProperty("Accept", "application/geo+json")
            try {
                require(conn.responseCode == 200) { "NWS service unavailable." }
                require(conn.contentLengthLong < 0 || conn.contentLengthLong <= MAX_RESPONSE)
                val bytes = conn.inputStream.use { input ->
                    val out = ByteArrayOutputStream()
                    val buffer = ByteArray(8_192)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        require(out.size() + count <= MAX_RESPONSE) { "Weather response exceeds limit." }
                        out.write(buffer, 0, count)
                    }
                    out.toByteArray()
                }
                return JSONObject(bytes.toString(Charsets.UTF_8))
            } finally {
                conn.disconnect()
            }
        }
    }
}
