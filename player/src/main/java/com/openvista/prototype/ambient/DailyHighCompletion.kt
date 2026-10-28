package com.openvista.prototype.ambient

import java.time.LocalDate

/**
 * NWS publishes future forecast periods, so after the daytime period has ended,
 * today's daily high may no longer be present. Fill only the missing high with
 * the worldwide provider's daily forecast for the same local calendar date.
 *
 * Do not replace NWS measurements, copy tomorrow's high, invent a high from the
 * current temperature, or silently omit the attribution of the supplement.
 */
internal fun completeTodayHighWithWorldForecast(
    primary: Forecast,
    supplement: Forecast,
    localDate: LocalDate,
): Forecast {
    val key = localDate.toString()
    val existingIndex = primary.days.indexOfFirst { it.date == key }
    if (existingIndex < 0) return primary
    val existing = primary.days[existingIndex]
    if (existing.maximumC != null) return primary
    val high = supplement.days.firstOrNull { it.date == key }?.maximumC
        ?.takeIf { it.isFinite() && it in -100.0..70.0 } ?: return primary
    val filled = primary.days.toMutableList()
    filled[existingIndex] = existing.copy(maximumC = high)
    return primary.copy(
        days = filled,
        // Any partial use of Open-Meteo daily values needs a visible CC BY credit.
        attribution = supplement.attribution,
        requiresVisibleAttribution = true,
    )
}
