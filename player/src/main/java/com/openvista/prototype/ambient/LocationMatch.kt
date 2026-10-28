package com.openvista.prototype.ambient

import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.MediaFamily
import java.text.Normalizer
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** One geocoder result: the place, its ISO 3166-1 country and its own region time zone. */
data class GeoPlace(
    val name: String,
    val region: String,
    val countryCode: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
    val population: Long = 0,
)

/**
 * Decides when a camera's saved city may complete its missing coordinates, time zone and
 * country, and which geocoder result is safe to use. A result is accepted only when it is
 * unambiguous: a far-away place of similar size with the same name ("Springfield") is left
 * for the user to choose in the source editor, because a wrong local time or forecast is
 * worse than a missing one.
 */
internal object LocationMatch {
    /** A camera needs a lookup only when it names a city but lacks a field the overlay uses. */
    fun needsLookup(item: LibraryItem): Boolean = item.family == MediaFamily.CAMERAS && query(item) != null &&
        (item.latitude == null || item.timeZone.isBlank() || item.country.isBlank())

    /** The city to search and, when the saved data names one, the country to limit it to. */
    fun query(item: LibraryItem): Pair<String, String?>? {
        val parts = item.city.split(',').map(String::trim).filter(String::isNotEmpty)
        val city = parts.firstOrNull()?.takeIf { it.length in 2..100 } ?: return null
        // "Venice, Italy" in the city field carries its own country.
        val country = CountryResolver.resolve(item.country, item.timeZone.ifBlank { null })
            ?: parts.drop(1).asReversed().firstNotNullOfOrNull { CountryResolver.resolve(it, null) { null } }
        return city to country
    }

    fun choose(city: String, countryCode: String?, places: List<GeoPlace>): GeoPlace? {
        val inCountry = if (countryCode == null) places else places.filter { it.countryCode == countryCode }
        val named = inCountry.filter { key(it.name) == key(city) }
        val pool = named.ifEmpty { inCountry }
        val top = pool.firstOrNull() ?: return null
        if (top.countryCode.isEmpty()) return null
        val rival = pool.drop(1).firstOrNull { distanceKm(top, it) > SAME_PLACE_KM } ?: return top
        return top.takeIf { it.population > 0 && it.population >= rival.population * DOMINANCE }
    }

    /** Fills only blank fields, so a value the user entered is never replaced. */
    fun completed(item: LibraryItem, place: GeoPlace, countryName: String): LibraryItem {
        val located = item.latitude != null && item.longitude != null
        return item.copy(
            latitude = if (located) item.latitude else place.latitude,
            longitude = if (located) item.longitude else place.longitude,
            timeZone = item.timeZone.ifBlank { place.timeZone },
            country = item.country.ifBlank { countryName },
        )
    }

    private fun distanceKm(a: GeoPlace, b: GeoPlace): Double {
        fun rad(value: Double) = Math.toRadians(value)
        val h = sin(rad(b.latitude - a.latitude) / 2).pow(2) +
            cos(rad(a.latitude)) * cos(rad(b.latitude)) * sin(rad(b.longitude - a.longitude) / 2).pow(2)
        return 2 * 6_371.0 * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    private fun key(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).trim()

    private const val SAME_PLACE_KM = 60.0
    private const val DOMINANCE = 10
}
