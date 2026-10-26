package com.openvista.prototype.ambient

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URL
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NwsForecastProviderTest {
    private val now = Instant.parse("2026-10-26T19:30:00Z")
    private val tucson = WeatherLocation("Tucson", 32.2226, -110.9747, "America/Phoenix")

    private fun period(date: String, daytime: Boolean, temp: Int, unit: String = "F",
                       rain: Int? = 20): JSONObject = JSONObject()
        .put("startTime", date).put("isDaytime", daytime).put("temperature", temp)
        .put("temperatureUnit", unit).put("windSpeed", "4 to 9 mph")
        .put("probabilityOfPrecipitation", JSONObject().put("value", rain ?: JSONObject.NULL))

    private fun hourly(): JSONObject = period("2026-10-26T12:00:00-07:00", true, 80)
        .put("endTime", "2026-10-26T13:00:00-07:00")
        .put("shortForecast", "Sunny")
        .put("relativeHumidity", JSONObject().put("value", 30))

    private fun mockProvider(hourlyFails: Boolean = false, badHost: Boolean = false): NwsForecastProvider {
        val point = JSONObject().put("properties", JSONObject()
            .put("forecast", if (badHost) "https://example.invalid/gridpoints/TWC/91,49/forecast" else "https://api.weather.gov/gridpoints/TWC/91,49/forecast")
            .put("forecastHourly", "https://api.weather.gov/gridpoints/TWC/91,49/forecast/hourly"))
        val daily = JSONObject().put("properties", JSONObject().put("periods", JSONArray()
            .put(period("2026-10-26T12:00:00-07:00", true, 85))
            .put(period("2026-10-26T18:00:00-07:00", false, 57, rain = 40))
            .put(period("2026-10-27T06:00:00-07:00", true, 86))
            .put(period("2026-10-27T18:00:00-07:00", false, 58))))
        val hours = JSONObject().put("properties", JSONObject().put("periods", JSONArray().put(hourly())))
        return NwsForecastProvider({ url ->
            when {
                url.path.startsWith("/points/") -> { assertEquals("/points/32.2226,-110.9747", url.path); point }
                url.path.endsWith("/forecast/hourly") -> {
                    if (hourlyFails) throw java.io.IOException("Hourly service unavailable")
                    hours
                }
                url.path.endsWith("/forecast") -> daily
                else -> error("Unexpected endpoint: $url")
            }
        }, { now })
    }

    @Test fun convertsTemperaturesAndBuildsDailyForecastWithRainChance() {
        val result = mockProvider().fetch(tucson)
        assertEquals("us-nws-v1", result.providerId)
        assertEquals(2, result.days.size)
        assertEquals(29.444, result.days[0].maximumC!!, 0.01)
        assertEquals(13.889, result.days[0].minimumC!!, 0.01)
        assertEquals(40.0, result.days[0].precipitationChancePct!!, 0.01)
        assertNull("Precipitation probability must not be reported as rainfall mm", result.days[0].precipitationMm)
        assertEquals(26.667, result.current!!.temperatureC!!, 0.01)
        assertEquals(30.0, result.current!!.humidity!!, 0.01)
        assertEquals("Sunny", result.current!!.description)
        assertNull(result.current!!.precipitationMm)
    }

    @Test fun hourlyOutageKeepsOfficialSevenDayForecast() {
        val result = mockProvider(hourlyFails = true).fetch(tucson)
        assertNull(result.current)
        assertEquals(2, result.days.size)
    }

    @Test fun rejectsUntrustedForecastEndpoint() {
        try {
            mockProvider(badHost = true).fetch(tucson)
            fail("Point discovery must not permit third-party endpoint injection")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun rejectsNonFiniteCoordinatesBeforeNetworkAccess() {
        try {
            mockProvider().fetch(tucson.copy(longitude = Double.NaN))
            fail("Invalid coordinates must fail")
        } catch (_: IllegalArgumentException) { }
    }
}
