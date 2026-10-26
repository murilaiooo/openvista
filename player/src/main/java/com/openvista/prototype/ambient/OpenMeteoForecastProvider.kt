package com.openvista.prototype.ambient

import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/** Original adapter for the documented JSON API; no Open-Meteo server code is incorporated. */
class OpenMeteoForecastProvider : ForecastProvider {
    override val id = "open-meteo-v1"
    override val attribution = "Weather: Open-Meteo (https://open-meteo.com/), CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/). Units converted and fields selected."
    override val minimumRefreshMs = 30 * 60_000L
    override val maximumCacheAgeMs = 24 * 60 * 60_000L

    override fun fetch(location: WeatherLocation): Forecast {
        require(location.latitude.isFinite() && location.latitude in -90.0..90.0 &&
            location.longitude.isFinite() && location.longitude in -180.0..180.0)
        val coordinates = String.format(Locale.US, "latitude=%.4f&longitude=%.4f", location.latitude, location.longitude)
        val url = URL("https://api.open-meteo.com/v1/forecast?$coordinates&timezone=auto&forecast_days=7" +
            "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m" +
            "&daily=temperature_2m_min,temperature_2m_max,precipitation_sum,precipitation_probability_max,wind_speed_10m_max,sunrise,sunset" +
            "&hourly=relative_humidity_2m&temperature_unit=celsius&wind_speed_unit=kmh&precipitation_unit=mm")
        val json = JSONObject(ProviderHttp.bytes(url, "api.open-meteo.com", 524_288, "application/json").toString(Charsets.UTF_8))
        require(!json.optBoolean("error"))
        val zone = ZoneId.of(json.getString("timezone"))
        val now = Instant.now()
        val units = json.getJSONObject("current_units")
        val current = json.optJSONObject("current")?.let { value ->
            val at = runCatching { java.time.LocalDateTime.parse(value.getString("time")).atZone(zone).toInstant() }.getOrNull()
            if (at == null || kotlin.math.abs(now.epochSecond - at.epochSecond) > 7_200) null
            else CurrentWeather(
                value.number("temperature_2m").takeIf { units.optString("temperature_2m") == "°C" }, null,
                value.number("relative_humidity_2m").takeIf { units.optString("relative_humidity_2m") == "%" },
                value.number("wind_speed_10m").takeIf { units.optString("wind_speed_10m") == "km/h" },
                value.number("precipitation").takeIf { units.optString("precipitation") == "mm" },
                value.number("weather_code")?.takeIf { it == it.toInt().toDouble() }?.toInt(),
            )
        }
        val hourly = json.optJSONObject("hourly")
        val hourlyUnits = json.optJSONObject("hourly_units")
        val humidity = mutableMapOf<String, MutableList<Double>>()
        if (hourly != null && hourlyUnits?.optString("relative_humidity_2m") == "%") {
            val times = hourly.optJSONArray("time") ?: JSONArray()
            val values = hourly.optJSONArray("relative_humidity_2m") ?: JSONArray()
            for (i in 0 until minOf(times.length(), values.length(), 192)) {
                val day = runCatching { java.time.LocalDateTime.parse(times.getString(i)).toLocalDate().toString() }.getOrNull() ?: continue
                val number = values.number(i)?.takeIf { it in 0.0..100.0 } ?: continue
                humidity.getOrPut(day) { mutableListOf() }.add(number)
            }
        }
        val daily = json.getJSONObject("daily")
        val dailyUnits = json.getJSONObject("daily_units")
        fun field(name: String, index: Int, unit: String): Double? =
            daily.optJSONArray(name)?.number(index).takeIf { dailyUnits.optString(name) == unit }
        val times = daily.getJSONArray("time")
        val today = now.atZone(zone).toLocalDate()
        val days = (0 until times.length().coerceAtMost(10)).mapNotNull { i ->
            val date = runCatching { LocalDate.parse(times.getString(i)) }.getOrNull() ?: return@mapNotNull null
            if (date.isBefore(today) || date.isAfter(today.plusDays(6))) return@mapNotNull null
            fun sun(name: String): String? = daily.optJSONArray(name)?.optString(i)?.takeIf { value ->
                runCatching { java.time.LocalDateTime.parse(value) }.isSuccess
            }
            ForecastDay(date.toString(), field("temperature_2m_min", i, "°C"), field("temperature_2m_max", i, "°C"),
                field("precipitation_sum", i, "mm"), field("wind_speed_10m_max", i, "km/h"),
                humidity[date.toString()]?.average(), sun("sunrise"), sun("sunset"), field("precipitation_probability_max", i, "%"))
        }
        require(days.isNotEmpty()) { "No valid forecast periods." }
        return Forecast(location.copy(timeZone = zone.id), current, days, now.toEpochMilli(), attribution, id, true)
    }

    private fun JSONObject.number(key: String): Double? = if (isNull(key) || !has(key)) null else optDouble(key).takeIf(Double::isFinite)
    private fun JSONArray.number(index: Int): Double? = if (isNull(index)) null else optDouble(index).takeIf(Double::isFinite)
}

/** Keep the existing NWS path first in its geographic coverage; worldwide use is deployment-gated. */
internal class RegionalForecastProvider : ForecastProvider {
    private val nws = NwsForecastProvider()
    private val worldwide = OpenMeteoForecastProvider()
    override val id = "nws-open-meteo-v1"
    override val attribution = "${nws.attribution}. ${worldwide.attribution}"
    override val minimumRefreshMs = worldwide.minimumRefreshMs
    override val maximumCacheAgeMs = worldwide.maximumCacheAgeMs
    override fun fetch(location: WeatherLocation): Forecast {
        val inNwsRegion = (location.latitude in 18.0..72.0 && location.longitude in -180.0..-60.0) ||
            (location.latitude in -15.0..-10.0 && location.longitude in -172.0..-168.0) ||
            (location.latitude in 12.0..22.0 && location.longitude in 140.0..146.0)
        val forecast = if (inNwsRegion) try { nws.fetch(location) } catch (error: Exception) {
            ProviderHttp.interrupted(); worldwide.fetch(location)
        } else worldwide.fetch(location)
        return forecast.copy(providerId = id)
    }
}
