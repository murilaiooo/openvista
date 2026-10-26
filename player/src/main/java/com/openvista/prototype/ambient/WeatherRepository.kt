package com.openvista.prototype.ambient

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.abs

data class WeatherLocation(val name: String, val latitude: Double, val longitude: Double, val timeZone: String)
data class CurrentWeather(val temperatureC: Double?, val description: String?, val humidity: Double?,
                          val windKph: Double?, val precipitationMm: Double?)
data class ForecastDay(val date: String, val minimumC: Double?, val maximumC: Double?,
                       val precipitationMm: Double?, val windKph: Double?, val humidity: Double?,
                       val sunrise: String?, val sunset: String?)
data class Forecast(val location: WeatherLocation, val current: CurrentWeather?, val days: List<ForecastDay>,
                    val fetchedAt: Long, val attribution: String, val providerId: String)

/** An adapter may be installed only after its terms, attribution, request budget and privacy review. */
interface ForecastProvider {
    val id: String
    val attribution: String
    val minimumRefreshMs: Long
    val maximumCacheAgeMs: Long
    fun fetch(location: WeatherLocation): Forecast
}

interface CitySearchProvider {
    val attribution: String
    fun search(query: String): List<WeatherLocation>
}

sealed interface WeatherState {
    data object NoLocation : WeatherState
    data object ProviderRequired : WeatherState
    data object Loading : WeatherState
    data class Ready(val forecast: Forecast, val stale: Boolean) : WeatherState
    data object Unavailable : WeatherState
}

/** No endpoint or provider is chosen by this module. Cached data never becomes invented weather. */
class WeatherRepository(context: Context, private val provider: ForecastProvider? = null,
                        private val geocoder: CitySearchProvider? = null) {
    private val cache = AtomicFile(File(context.cacheDir, "weather-v1.json"))
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var task: Future<*>? = null
    private var generation = 0

    fun load(location: WeatherLocation?, callback: (WeatherState) -> Unit) {
        cancel()
        if (location == null || !valid(location)) { callback(WeatherState.NoLocation); return }
        val adapter = provider ?: run { callback(WeatherState.ProviderRequired); return }
        val request = generation
        callback(WeatherState.Loading)
        task = worker.submit {
            val saved = readCache(location, adapter)
            val now = System.currentTimeMillis()
            val fresh = saved != null && now - saved.fetchedAt in 0..adapter.minimumRefreshMs.coerceAtLeast(60_000)
            val state = if (fresh) WeatherState.Ready(saved!!, false) else try {
                val received = adapter.fetch(location)
                require(received.providerId == adapter.id && valid(received.location))
                require(abs(received.location.latitude - location.latitude) < .1 &&
                    abs(received.location.longitude - location.longitude) < .1)
                require(received.fetchedAt in (now - 86_400_000)..(System.currentTimeMillis() + 60_000))
                val forecast = sanitize(received.copy(attribution = adapter.attribution.take(1000)))
                writeCache(forecast)
                WeatherState.Ready(forecast, false)
            } catch (_: Exception) {
                if (saved != null) WeatherState.Ready(saved, true) else WeatherState.Unavailable
            }
            main.post { if (generation == request) callback(state) }
        }
    }

    fun searchCities(query: String, callback: (List<WeatherLocation>?, String?) -> Unit) {
        cancel()
        val adapter = geocoder ?: run { callback(null, null); return }
        val value = query.trim().take(100)
        if (value.length < 2) { callback(emptyList(), adapter.attribution); return }
        val request = generation
        task = worker.submit {
            val places = try { adapter.search(value).filter(::valid).take(20) } catch (_: Exception) { null }
            main.post { if (generation == request) callback(places, adapter.attribution) }
        }
    }

    fun cancel() { generation++; task?.cancel(true); task = null }
    fun close() { cancel(); worker.shutdownNow() }
    fun clearCache() { cancel(); synchronized(CACHE_LOCK) { cache.delete() } }

    private fun valid(location: WeatherLocation) = location.latitude.isFinite() && location.longitude.isFinite() &&
        location.latitude in -90.0..90.0 && location.longitude in -180.0..180.0

    private fun sanitize(value: Forecast): Forecast {
        fun bounded(number: Double?, minimum: Double, maximum: Double) =
            number?.takeIf { it.isFinite() && it in minimum..maximum }
        return value.copy(current = value.current?.let { c -> c.copy(
            temperatureC = bounded(c.temperatureC, -100.0, 70.0),
            description = c.description?.filterNot(Char::isISOControl)?.take(120),
            humidity = bounded(c.humidity, 0.0, 100.0), windKph = bounded(c.windKph, 0.0, 500.0),
            precipitationMm = bounded(c.precipitationMm, 0.0, 3000.0),
        ) }, days = value.days.distinctBy { it.date }.take(7).map { d -> d.copy(
            minimumC = bounded(d.minimumC, -100.0, 70.0), maximumC = bounded(d.maximumC, -100.0, 70.0),
            humidity = bounded(d.humidity, 0.0, 100.0), windKph = bounded(d.windKph, 0.0, 500.0),
            precipitationMm = bounded(d.precipitationMm, 0.0, 3000.0),
            sunrise = d.sunrise?.take(40), sunset = d.sunset?.take(40), date = d.date.take(40),
        ) })
    }

    private fun readCache(location: WeatherLocation, adapter: ForecastProvider): Forecast? = synchronized(CACHE_LOCK) { try {
        if (cache.baseFile.length() > 131_072) null else cache.openRead().use { input ->
            val root = JSONObject(input.readBytes().toString(Charsets.UTF_8))
            val value = root.getJSONObject("forecast")
            val at = value.getLong("fetchedAt")
            val age = System.currentTimeMillis() - at
            if (root.getInt("schema") != 1 || value.getString("provider") != adapter.id ||
                age !in 0..adapter.maximumCacheAgeMs.coerceIn(0, 604_800_000) ||
                abs(value.getDouble("latitude") - location.latitude) > .001 ||
                abs(value.getDouble("longitude") - location.longitude) > .001) null
            else {
                val current = value.optJSONObject("current")
                val days = value.getJSONArray("days")
                sanitize(Forecast(location,
                    current?.let { c -> CurrentWeather(c.number("temperature"), c.optString("description").ifBlank { null },
                        c.number("humidity"), c.number("wind"), c.number("rain")) },
                    (0 until days.length().coerceAtMost(7)).map { index -> days.getJSONObject(index).let { d ->
                        ForecastDay(d.getString("date"), d.number("min"), d.number("max"), d.number("rain"),
                            d.number("wind"), d.number("humidity"), d.optString("sunrise").ifBlank { null },
                            d.optString("sunset").ifBlank { null })
                    } }, at, adapter.attribution, adapter.id))
            }
        }
    } catch (_: Exception) { null } }

    private fun writeCache(forecast: Forecast) = synchronized(CACHE_LOCK) {
        val value = JSONObject().put("provider", forecast.providerId).put("fetchedAt", forecast.fetchedAt)
            .put("latitude", forecast.location.latitude).put("longitude", forecast.location.longitude)
            .put("days", JSONArray().apply { forecast.days.forEach { d -> put(JSONObject()
                .put("date", d.date).put("min", d.minimumC).put("max", d.maximumC).put("rain", d.precipitationMm)
                .put("wind", d.windKph).put("humidity", d.humidity).put("sunrise", d.sunrise).put("sunset", d.sunset)) } })
        forecast.current?.let { c -> value.put("current", JSONObject().put("temperature", c.temperatureC)
            .put("description", c.description).put("humidity", c.humidity).put("wind", c.windKph).put("rain", c.precipitationMm)) }
        val stream = cache.startWrite()
        try {
            stream.write(JSONObject().put("schema", 1).put("forecast", value).toString().toByteArray())
            cache.finishWrite(stream)
        } catch (error: Exception) { cache.failWrite(stream); throw error }
    }

    private fun JSONObject.number(key: String): Double? =
        if (isNull(key) || !has(key)) null else optDouble(key).takeIf(Double::isFinite)

    companion object { private val CACHE_LOCK = Any() }
}
