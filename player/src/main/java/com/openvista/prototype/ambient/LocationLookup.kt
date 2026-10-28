package com.openvista.prototype.ambient

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.openvista.prototype.library.LibraryStore
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Completes camera locations from the city the user or their list saved, so imported and
 * paired sources show their real country, local time and weather instead of nothing.
 *
 * Only the saved city name (and its country, when known) is sent to the approved geocoder;
 * stream URLs and titles never are. Lookups are sequential, rate-limited, bounded per run and
 * remembered for a week, so a large list or an unknown city cannot cause a request storm.
 */
class LocationLookup(context: Context, private val store: LibraryStore,
                     private val provider: CitySearchProvider? = ProviderPolicy.cities(context.resources.configuration.locales[0].language)) {
    private val locale: Locale = context.resources.configuration.locales[0]
    private val attempts = context.applicationContext.getSharedPreferences("location-lookups", Context.MODE_PRIVATE)
    // Separate workers, so an editor search never waits behind a rate-limited library pass.
    private val worker = Executors.newSingleThreadExecutor()
    private val searcher = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var library: Future<*>? = null
    private var search: Future<*>? = null
    @Volatile private var closed = false

    val available: Boolean get() = provider != null

    /** Fills blank location fields for every camera that names a city; [onChanged] runs on the main thread. */
    fun completeLibrary(onChanged: () -> Unit) {
        val adapter = provider ?: return
        if (closed || library?.isDone == false) return
        val settings = store.settings()
        if (!settings.showLocation && !settings.showWeather && !settings.showClock) return
        library = worker.submit {
            val now = System.currentTimeMillis()
            forgetExpired(now)
            var requests = 0
            var changed = false
            val pending = store.items().filter(LocationMatch::needsLookup)
                .groupBy { LocationMatch.query(it) }.filterKeys { it != null }
            for ((query, items) in pending) {
                if (closed || Thread.currentThread().isInterrupted || requests >= MAX_REQUESTS_PER_RUN) break
                val (city, country) = query!!
                val key = (city + "|" + country.orEmpty()).lowercase(Locale.ROOT)
                if (now - attempts.getLong(key, 0) in 0..RETRY_AFTER_MS) continue
                if (requests++ > 0) try { Thread.sleep(REQUEST_SPACING_MS) } catch (_: InterruptedException) { break }
                val places = try { adapter.places(city, country) } catch (_: Exception) { break } // Offline: retry next run.
                attempts.edit().putLong(key, System.currentTimeMillis()).apply()
                val place = LocationMatch.choose(city, country, places)?.takeIf { it.timeZone in ZONES } ?: continue
                val countryName = Locale("", place.countryCode).getDisplayCountry(locale).ifBlank { place.country }
                items.forEach { item ->
                    try {
                        store.completeLocation(item.id) { current ->
                            if (LocationMatch.query(current) == query) LocationMatch.completed(current, place, countryName) else current
                        }
                        changed = true
                    } catch (_: Exception) { /* Storage failure is reported by the library itself. */ }
                }
            }
            if (changed) main.post { if (!closed) onChanged() }
        }
    }

    /** Explicit search from the source editor; null means the geocoder could not be reached. */
    fun search(query: String, countryCode: String?, callback: (List<GeoPlace>?) -> Unit) {
        val adapter = provider ?: run { callback(null); return }
        val value = query.trim()
        if (closed || value.length !in 2..100 || value.any(Char::isISOControl)) { callback(emptyList()); return }
        search?.cancel(true)
        search = searcher.submit {
            val places = try {
                adapter.places(value, countryCode).ifEmpty { if (countryCode != null) adapter.places(value, null) else emptyList() }
                    .filter { it.timeZone in ZONES && it.countryCode.isNotEmpty() }
            } catch (_: Exception) { null }
            main.post { if (!closed) callback(places) }
        }
    }

    fun displayCountry(place: GeoPlace): String = Locale("", place.countryCode).getDisplayCountry(locale).ifBlank { place.country }

    fun close() {
        closed = true
        library?.cancel(true)
        search?.cancel(true)
        worker.shutdownNow()
        searcher.shutdownNow()
    }

    private fun forgetExpired(now: Long) {
        val expired = attempts.all.filter { (_, value) -> value !is Long || now - value !in 0..RETRY_AFTER_MS }.keys
        if (expired.isNotEmpty()) attempts.edit().apply { expired.forEach { remove(it) } }.apply()
    }

    private companion object {
        val ZONES: Set<String> = TimeZone.getAvailableIDs().toSet()
        const val MAX_REQUESTS_PER_RUN = 40
        const val REQUEST_SPACING_MS = 1_100L
        const val RETRY_AFTER_MS = 7 * 24 * 60 * 60 * 1000L
    }
}
