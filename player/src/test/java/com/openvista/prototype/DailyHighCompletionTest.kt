package com.openvista.prototype.ambient

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DailyHighCompletionTest {
    private val today = LocalDate.of(2026, 10, 27)
    private val location = WeatherLocation("Tucson, AZ", 32.2226, -110.9747, "America/Phoenix")
    private fun day(date: String, low: Double?, high: Double?) = ForecastDay(
        date, low, high, null, null, null, null, null,
    )
    private fun forecast(vararg days: ForecastDay, attribution: String = "U.S. National Weather Service") =
        Forecast(location, null, days.toList(), 0L, attribution, "test")

    @Test fun fillsMissingNighttimeHighWithoutChangingOfficialLow() {
        val nws = forecast(day("2026-10-27", 14.0, null), day("2026-10-28", 16.0, 31.0))
        val global = forecast(day("2026-10-27", 18.0, 29.0), attribution = "Weather: Open-Meteo · CC BY 4.0")
        val result = completeTodayHighWithWorldForecast(nws, global, today)
        assertEquals(29.0, result.days[0].maximumC!!, 0.01)
        assertEquals(14.0, result.days[0].minimumC!!, 0.01)
        assertEquals(31.0, result.days[1].maximumC!!, 0.01)
        assertTrue(result.requiresVisibleAttribution)
        assertTrue(result.attribution.contains("Open-Meteo"))
    }

    @Test fun doesNotOverwriteExistingHighOrTriggerCredit() {
        val nws = forecast(day("2026-10-27", 14.0, 33.0))
        val global = forecast(day("2026-10-27", 13.0, 29.0), attribution = "Open-Meteo")
        assertSame(nws, completeTodayHighWithWorldForecast(nws, global, today))
    }

    @Test fun neverTakesHighFromAnotherDay() {
        val nws = forecast(day("2026-10-27", 14.0, null))
        val global = forecast(day("2026-10-28", 16.0, 35.0))
        assertSame(nws, completeTodayHighWithWorldForecast(nws, global, today))
    }

    @Test fun unavailableOrInvalidHighIsNeverInvented() {
        val nws = forecast(day("2026-10-27", 14.0, null))
        for (value in listOf(null, Double.NaN, Double.POSITIVE_INFINITY, 150.0)) {
            val global = forecast(day("2026-10-27", 16.0, value))
            assertSame(nws, completeTodayHighWithWorldForecast(nws, global, today))
        }
    }

    @Test fun noLocalDayMeansNoSubstitution() {
        val nws = forecast(day("2026-10-28", 15.0, null))
        val global = forecast(day("2026-10-27", 14.0, 30.0))
        assertSame(nws, completeTodayHighWithWorldForecast(nws, global, today))
    }
}
