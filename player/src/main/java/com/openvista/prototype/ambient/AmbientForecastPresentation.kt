package com.openvista.prototype.ambient

import com.openvista.prototype.library.LibraryItem
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Presentation-only transformations; never manufactures days, coordinates or temperatures. */
internal object AmbientForecastPresentation {
    fun cameraCityCountry(location: WeatherLocation, source: LibraryItem?): String {
        val city = location.name.substringBefore(',').trim().take(64)
        val savedCountry = source?.country?.trim()?.takeIf { it.length in 2..70 && it.none(Char::isISOControl) }
        val components = location.name.split(',').map(String::trim).filter(String::isNotBlank)
        val country = savedCountry ?: components.lastOrNull()?.takeIf { components.size >= 2 && it != city }
        return listOfNotNull(city.takeIf(String::isNotBlank), country).joinToString(", ")
    }

    fun visibleDays(forecast: Forecast?, zone: String, requested: Int, today: LocalDate): List<ForecastDay> {
        if (forecast == null || runCatching { ZoneId.of(zone) }.isFailure) return emptyList()
        return forecast.days.mapNotNull { day ->
            val date = runCatching { LocalDate.parse(day.date) }.getOrNull() ?: return@mapNotNull null
            if (date < today || date > today.plusDays(6)) null else date to day
        }.distinctBy { it.first }.sortedBy { it.first }.take(requested.coerceIn(3,7)).map { it.second }
    }

    fun dayName(date: String, locale: Locale): String {
        val day = runCatching { LocalDate.parse(date) }.getOrNull() ?: return "—"
        return day.format(DateTimeFormatter.ofPattern("EEE d", locale)).replace(".", "")
            .uppercase(locale).take(12)
    }

    fun temp(value: Double?, fahrenheit: Boolean): String = value?.takeIf { it.isFinite() && it in -100.0..70.0 }?.let {
        val chosen = if (fahrenheit) it * 9.0 / 5 + 32 else it
        "${chosen.roundToInt()}°"
    } ?: "–"

    fun rain(chance: Double?): String = chance?.takeIf { it.isFinite() && it in 0.0..100.0 }?.let {
        "${it.roundToInt()}%"
    } ?: ""
}
