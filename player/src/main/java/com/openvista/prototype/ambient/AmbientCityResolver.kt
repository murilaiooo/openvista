package com.openvista.prototype.ambient

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.openvista.prototype.library.LibraryItem
import org.json.JSONObject
import java.security.MessageDigest
import java.time.ZoneId
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Resolve only location-like camera/video labels with one-at-a-time, bounded requests.
 * Never sends media URIs, account data, playlists, or raw arbitrary descriptions.
 * Stores successful geocodes for seven days; failure is retried after 12 hours.
 */
internal class AmbientCityResolver(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("ambient-city-cache-v1", Context.MODE_PRIVATE)
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val provider = OpenMeteoCitySearchProvider("en")
    private var task: Future<*>? = null
    private var generation = 0
    private var closed = false

    fun resolve(item: LibraryItem?, callback: (WeatherLocation?) -> Unit) {
        generation++
        task?.cancel(true)
        if (closed || item == null) { callback(null); return }
        val candidates = CityLabelParser.queries(item)
        val lat = item.latitude
        val lon = item.longitude
        val zone = item.timeZone.takeIf { runCatching { ZoneId.of(it) }.isSuccess }
        if (lat != null && lon != null && zone != null &&
            lat.isFinite() && lat in -90.0..90.0 && lon.isFinite() && lon in -180.0..180.0) {
            val city = item.city.ifBlank { candidates.firstOrNull()?.substringBefore(',').orEmpty() }
            if (city.isNotBlank()) { callback(WeatherLocation(city, lat, lon, zone)); return }
        }
        if (candidates.isEmpty()) { callback(null); return }
        val request = generation
        val key = MessageDigest.getInstance("SHA-256").digest(candidates.joinToString("|").toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }.take(24)
        val cached = preferences.getString(key, null)
        val now = System.currentTimeMillis()
        if (cached != null) {
            val entry = runCatching { JSONObject(cached) }.getOrNull()
            if (entry != null) {
                val validFor = if (entry.optBoolean("missing")) 12 * 3_600_000L else 7 * 86_400_000L
                if (now - entry.optLong("at") in 0..validFor) {
                    callback(if (entry.optBoolean("missing")) null else readLocation(entry))
                    return
                }
            }
        }
        task = executor.submit {
            var result: WeatherLocation? = null
            var received = false
            for (query in candidates.take(2)) {
                if (Thread.currentThread().isInterrupted) break
                try {
                    val matches = provider.search(query)
                    received = true
                    result = matches.firstOrNull { CityLabelParser.matches(query, it.name) }
                    if (result != null) break
                } catch (_: Exception) { /* Don't invent a location on provider failure. */ }
            }
            if (!closed && generation == request && received) {
                val json = JSONObject().put("at", now)
                if (result == null) json.put("missing", true)
                else json.put("name", result.name).put("lat", result.latitude)
                    .put("lon", result.longitude).put("zone", result.timeZone)
                preferences.edit().putString(key, json.toString()).apply()
            }
            main.post { if (!closed && generation == request) callback(result) }
        }
    }

    private fun readLocation(json: JSONObject): WeatherLocation? = runCatching {
        val lat = json.getDouble("lat")
        val lon = json.getDouble("lon")
        val zone = json.getString("zone")
        require(lat in -90.0..90.0 && lon in -180.0..180.0 && ZoneId.of(zone).id == zone)
        WeatherLocation(json.getString("name"), lat, lon, zone)
    }.getOrNull()

    fun close() {
        closed = true
        generation++
        task?.cancel(true)
        executor.shutdownNow()
    }
}
