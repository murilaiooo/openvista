package com.openvista.prototype

import com.openvista.prototype.ambient.ProviderPolicy
import com.openvista.prototype.ambient.OpenMeteoForecastProvider
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldwideWeatherIntegrationTest {
    @Test fun worldwideWeatherIsEnabledForAdFreeNoncommercialBuild() {
        assertTrue(ProviderPolicy.WORLDWIDE_WEATHER_APPROVED)
        assertTrue(ProviderPolicy.forecast().id == "nws-open-meteo-v2")
    }

    @Test fun openMeteoCreditIsDisplayedForNonUsSources() {
        val api = OpenMeteoForecastProvider()
        assertTrue(api.attribution.contains("Open-Meteo"))
        assertTrue(api.attribution.contains("CC BY 4.0"))
        assertTrue(api.minimumRefreshMs >= 30 * 60_000L)
    }
}
