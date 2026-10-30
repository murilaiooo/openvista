package com.openvista.prototype.ambient

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.abs

data class WeatherLocation(val name: String, val latitude: Double, val longitude: Double, val timeZone: String)
data class CurrentWeather(val temperatureC: Double?, val description: String?, val humidity: Double?,
                          val windKph: Double?, val precipitationMm: Double?, val weatherCode: Int? = null)
data class ForecastDay(val date: String, val minimumC: Double?, val maximumC: Double?,
                       val precipitationMm: Double?, val windKph: Double?, val humidity: Double?,
                       val sunrise: String?, val sunset: String?, val precipitationChancePct: Double? = null,
                       val description: String? = null, val weatherCode: Int? = null)
data class Forecast(val location: WeatherLocation, val current: CurrentWeather?, val days: List<ForecastDay>,
                    val fetchedAt: Long, val attribution: String, val providerId: String,
                    val requiresVisibleAttribution: Boolean = false)

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
    /** Structured matches, optionally limited to one ISO 3166-1 country, in provider relevance order. */
    fun places(query: String, countryCode: String?): List<GeoPlace>
}

sealed interface WeatherState {
    data object NoLocation : WeatherState
    data object ProviderRequired : WeatherState
    data object Loading : WeatherState
    data class Ready(val forecast: Forecast, val stale: Boolean) : WeatherState
    data object Unavailable : WeatherState
}

/** Provider data stays bounded, location-specific and usable when a refresh/cache write fails. */
class WeatherRepository(context: Context, private val provider: ForecastProvider? = ProviderPolicy.forecast(),
                        private val geocoder: CitySearchProvider? = ProviderPolicy.cities(context.resources.configuration.locales[0].language)) {
    private val cache = AtomicFile(File(context.applicationContext.cacheDir, "weather-v2.json"))
    private val cityCache = AtomicFile(File(context.applicationContext.cacheDir, "cities-v1.json"))
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var task: Future<*>? = null
    /** Rate-limit key of the forecast request in flight; released if that request is cancelled. */
    @Volatile private var pendingKey: String? = null
    @Volatile private var generation = 0
    @Volatile private var closed = false

    fun load(location: WeatherLocation?, callback: (WeatherState) -> Unit) {
        if (closed) return
        cancel()
        if (location == null || !valid(location)) { callback(WeatherState.NoLocation); return }
        val adapter = provider ?: run { callback(WeatherState.ProviderRequired); return }
        val request = generation
        callback(WeatherState.Loading)
        task = worker.submit {
            val saved = readCache(location, adapter)
            val now = System.currentTimeMillis()
            val fresh = saved != null && now - saved.fetchedAt in 0..adapter.minimumRefreshMs.coerceAtLeast(60_000)
            val key = "${adapter.id}/${coordinateKey(location)}"
            val allowed = fresh || allowAttempt(key, 300_000)
            if (!fresh && allowed) pendingKey = key
            val state = if (fresh) WeatherState.Ready(saved!!, false)
            else if (!allowed) saved?.let { WeatherState.Ready(it, true) } ?: WeatherState.Unavailable
            else try {
                val received = adapter.fetch(location)
                require(received.providerId == adapter.id && valid(received.location))
                require(abs(received.location.latitude - location.latitude) < .1 &&
                    abs(received.location.longitude - location.longitude) < .1)
                require(received.fetchedAt in (now - 86_400_000)..(System.currentTimeMillis() + 60_000))
                val forecast = sanitize(received.copy(attribution = received.attribution.filterNot(Char::isISOControl).take(1000)))
                pendingKey = null
                if (closed || generation != request) return@submit
                try { writeCache(forecast) } catch (_: Exception) { /* Cache storage is optional. */ }
                WeatherState.Ready(forecast, false)
            } catch (_: Exception) {
                // A request cancelled by a camera switch is retried freely; a real failure keeps its back-off.
                if (Thread.currentThread().isInterrupted || closed || generation != request) return@submit
                pendingKey = null
                if (saved != null) WeatherState.Ready(saved, true) else WeatherState.Unavailable
            }
            main.post { if (!closed && generation == request) callback(state) }
        }
    }

    fun searchCities(query: String, callback: (List<WeatherLocation>?, String?) -> Unit) {
        if (closed) return
        cancel()
        val adapter = geocoder ?: run { callback(null, null); return }
        val value = query.trim()
        if (value.length !in 2..100 || value.any(Char::isISOControl)) { callback(emptyList(), adapter.attribution); return }
        val request = generation
        task = worker.submit {
            val key = "${adapter.attribution}/${value.lowercase(java.util.Locale.ROOT)}"
            val places = readCities(key) ?: if (!allowAttempt("cities", 1_100)) null else try {
                adapter.search(value).filter(::valid).take(20).also {
                    if (!closed && generation == request) try { writeCities(key, it) } catch (_: Exception) { }
                }
            } catch (_: Exception) { null }
            main.post { if (!closed && generation == request) callback(places, adapter.attribution) }
        }
    }

    fun cancel() {
        generation++; task?.cancel(true); task = null
        pendingKey?.let(::releaseAttempt); pendingKey = null
    }
    fun close() { closed = true; cancel(); worker.shutdownNow() }
    fun clearCache() { cancel(); synchronized(CACHE_LOCK) { cache.delete(); cityCache.delete() } }

    private fun valid(location: WeatherLocation) = location.latitude.isFinite() && location.longitude.isFinite() &&
        location.latitude in -90.0..90.0 && location.longitude in -180.0..180.0 &&
        location.name.length <= 200 && location.name.none(Char::isISOControl) && location.timeZone.length <= 100

    private fun sanitize(value: Forecast): Forecast {
        fun bounded(number: Double?, minimum: Double, maximum: Double) =
            number?.takeIf { it.isFinite() && it in minimum..maximum }
        return value.copy(current = value.current?.let { c -> c.copy(
            temperatureC = bounded(c.temperatureC, -100.0, 70.0),
            description = c.description?.filterNot(Char::isISOControl)?.take(120),
            humidity = bounded(c.humidity, 0.0, 100.0), windKph = bounded(c.windKph, 0.0, 500.0),
            precipitationMm = bounded(c.precipitationMm, 0.0, 3000.0), weatherCode = c.weatherCode?.takeIf { it in 0..99 },
        ) }, days = value.days.distinctBy { it.date }.take(7).map { d -> d.copy(
            minimumC = bounded(d.minimumC, -100.0, 70.0), maximumC = bounded(d.maximumC, -100.0, 70.0),
            humidity = bounded(d.humidity, 0.0, 100.0), windKph = bounded(d.windKph, 0.0, 500.0),
            precipitationMm = bounded(d.precipitationMm, 0.0, 3000.0), precipitationChancePct = bounded(d.precipitationChancePct, 0.0, 100.0),
            sunrise = d.sunrise?.take(40), sunset = d.sunset?.take(40), date = d.date.take(40),
            description = d.description?.filterNot(Char::isISOControl)?.take(120),
            weatherCode = d.weatherCode?.takeIf { it in 0..99 },
        ) })
    }

    private fun readCache(location: WeatherLocation, adapter: ForecastProvider): Forecast? = synchronized(CACHE_LOCK) { try {
        if (cache.baseFile.length() > 524_288) null else cache.openRead().use { input ->
            val root = JSONObject(input.readBytes().toString(Charsets.UTF_8))
            require(root.getInt("schema") == 2)
            val value = root.getJSONObject("forecasts").getJSONObject("${adapter.id}/${coordinateKey(location)}")
            val at = value.getLong("fetchedAt")
            val age = System.currentTimeMillis() - at
            if (value.getString("provider") != adapter.id ||
                age !in 0..adapter.maximumCacheAgeMs.coerceIn(0, 604_800_000) ||
                abs(value.getDouble("latitude") - location.latitude) > .001 ||
                abs(value.getDouble("longitude") - location.longitude) > .001) null
            else {
                val current = value.optJSONObject("current")
                val days = value.getJSONArray("days")
                sanitize(Forecast(location.copy(timeZone = value.optString("timeZone", location.timeZone)),
                    current?.let { c -> CurrentWeather(c.number("temperature"), c.optString("description").ifBlank { null },
                        c.number("humidity"), c.number("wind"), c.number("rain"), c.number("code")?.toInt()) },
                    (0 until days.length().coerceAtMost(7)).map { index -> days.getJSONObject(index).let { d ->
                        ForecastDay(d.getString("date"), d.number("min"), d.number("max"), d.number("rain"),
                            d.number("wind"), d.number("humidity"), d.optString("sunrise").ifBlank { null },
                            d.optString("sunset").ifBlank { null }, d.number("rainChance"),
                            d.optString("description").ifBlank { null }, d.number("code")?.toInt())
                    } }, at, value.optString("attribution", adapter.attribution).take(1000), adapter.id, value.optBoolean("visibleCredit")))
            }
        }
    } catch (_: Exception) { null } }

    private fun writeCache(forecast: Forecast) = synchronized(CACHE_LOCK) {
        val value = JSONObject().put("provider", forecast.providerId).put("fetchedAt", forecast.fetchedAt)
            .put("timeZone", forecast.location.timeZone)
            .put("attribution", forecast.attribution).put("visibleCredit", forecast.requiresVisibleAttribution)
            .put("latitude", forecast.location.latitude).put("longitude", forecast.location.longitude)
            .put("days", JSONArray().apply { forecast.days.forEach { d -> put(JSONObject()
                .put("date", d.date).put("min", d.minimumC).put("max", d.maximumC).put("rain", d.precipitationMm)
                .put("wind", d.windKph).put("humidity", d.humidity).put("sunrise", d.sunrise).put("sunset", d.sunset)
                .put("rainChance", d.precipitationChancePct).put("description", d.description).put("code", d.weatherCode)) } })
        forecast.current?.let { c -> value.put("current", JSONObject().put("temperature", c.temperatureC)
            .put("description", c.description).put("humidity", c.humidity).put("wind", c.windKph).put("rain", c.precipitationMm).put("code", c.weatherCode)) }
        val entries = try {
            if (cache.baseFile.length() > 524_288) JSONObject() else cache.openRead().use {
                val root = JSONObject(it.readBytes().toString(Charsets.UTF_8))
                if (root.optInt("schema") == 2) root.getJSONObject("forecasts") else JSONObject()
            }
        } catch (_: Exception) { JSONObject() }
        entries.put("${forecast.providerId}/${coordinateKey(forecast.location)}", value)
        while (entries.length() > 8) {
            val oldest = entries.keys().asSequence().minByOrNull { entries.getJSONObject(it).optLong("fetchedAt") } ?: break
            entries.remove(oldest)
        }
        val stream = cache.startWrite()
        try {
            val bytes = JSONObject().put("schema", 2).put("forecasts", entries).toString().toByteArray(Charsets.UTF_8)
            require(bytes.size <= 524_288)
            stream.write(bytes)
            cache.finishWrite(stream)
        } catch (error: Exception) { cache.failWrite(stream); throw error }
    }

    private fun JSONObject.number(key: String): Double? =
        if (isNull(key) || !has(key)) null else optDouble(key).takeIf(Double::isFinite)

    private fun readCities(key: String): List<WeatherLocation>? = synchronized(CACHE_LOCK) { try {
        if (cityCache.baseFile.length() > 262_144) null else cityCache.openRead().use {
            val entry = JSONObject(it.readBytes().toString(Charsets.UTF_8)).getJSONObject(key)
            require(System.currentTimeMillis() - entry.getLong("at") in 0..604_800_000L)
            val places = entry.getJSONArray("places")
            (0 until places.length().coerceAtMost(20)).map { i -> places.getJSONObject(i).let { p ->
                WeatherLocation(p.getString("name"), p.getDouble("lat"), p.getDouble("lon"), p.getString("zone"))
            } }.filter(::valid)
        }
    } catch (_: Exception) { null } }

    private fun writeCities(key: String, places: List<WeatherLocation>) = synchronized(CACHE_LOCK) {
        val root = try {
            if (cityCache.baseFile.length() > 262_144) JSONObject() else cityCache.openRead().use { JSONObject(it.readBytes().toString(Charsets.UTF_8)) }
        } catch (_: Exception) { JSONObject() }
        root.put(key, JSONObject().put("at", System.currentTimeMillis()).put("places", JSONArray().apply {
            places.forEach { p -> put(JSONObject().put("name", p.name).put("lat", p.latitude).put("lon", p.longitude).put("zone", p.timeZone)) }
        }))
        while (root.length() > 20) root.remove(root.keys().asSequence().minByOrNull { root.getJSONObject(it).optLong("at") } ?: break)
        val bytes = root.toString().toByteArray(Charsets.UTF_8)
        require(bytes.size <= 262_144)
        val output = cityCache.startWrite()
        try { output.write(bytes); cityCache.finishWrite(output) } catch (error: Exception) { cityCache.failWrite(output); throw error }
    }

    private fun coordinateKey(location: WeatherLocation) = java.lang.String.format(java.util.Locale.US, "%.4f/%.4f", location.latitude, location.longitude)

    companion object {
        private val CACHE_LOCK = Any()
        private val attempts = linkedMapOf<String, Long>()
        private val budget = ArrayDeque<Long>()
        /** Returns an unfinished attempt's slot, so zapping through cameras cannot lock weather out for minutes. */
        private fun releaseAttempt(key: String) = synchronized(CACHE_LOCK) {
            attempts.remove(key)?.let { at -> budget.remove(at) }
        }
        private fun allowAttempt(key: String, interval: Long): Boolean = synchronized(CACHE_LOCK) {
            val now = SystemClock.elapsedRealtime()
            while (budget.isNotEmpty() && now - budget.first() >= 60_000) budget.removeFirst()
            if (attempts[key]?.let { now - it < interval } == true || budget.size >= 12) false else {
                attempts[key] = now
                while (attempts.size > 64) attempts.remove(attempts.keys.first())
                budget.addLast(now)
                true
            }
        }
    }
}
