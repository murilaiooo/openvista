package com.openvista.prototype.ambient

import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.MediaFamily
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class AmbientForecastPresentationTest {
    private val location = WeatherLocation("Jurata, Pomeranian, Poland", 54.69, 18.7, "Europe/Warsaw")
    private fun forecast(days: List<ForecastDay>): Forecast = Forecast(
        WeatherLocation("Tucson", 32.2226, -110.9747, "America/Phoenix"), null, days,
        100L, "NWS", "us-nws-v1",
    )
    private fun day(date: String, lo: Double? = 13.0, hi: Double? = 29.0): ForecastDay =
        ForecastDay(date, lo, hi, null, null, null, null, null, 25.0)

    @Test fun cameraUsesGeocodedCountryButNeverShowsMediaTitle() {
        val source = LibraryItem("id", "Jurata — Pier cam 4K", "https://example.invalid/video",
            MediaFamily.CAMERAS, city = "", country = "")
        assertEquals("Jurata, Poland", AmbientForecastPresentation.cameraCityCountry(location, source))
        assertEquals("Jurata, Polska", AmbientForecastPresentation.cameraCityCountry(
            location, source.copy(country = "Polska")))
    }

    @Test fun forecastFiltersPreviousDatesAndCapsRequestedWindow() {
        val today = LocalDate.parse("2026-10-26")
        val all = (-2L..9L).map { day(today.plusDays(it).toString()) }
        assertEquals(3, AmbientForecastPresentation.visibleDays(forecast(all), "America/Phoenix", 3, today).size)
        val seven = AmbientForecastPresentation.visibleDays(forecast(all), "America/Phoenix", 7, today)
        assertEquals(7, seven.size)
        assertEquals("2026-10-26", seven.first().date)
        assertEquals("2026-11-01", seven.last().date)
        assertEquals(7, AmbientForecastPresentation.visibleDays(forecast(all), "America/Phoenix", 99, today).size)
        assertTrue(AmbientForecastPresentation.visibleDays(forecast(all), "Bad/Zone", 7, today).isEmpty())
    }

    @Test fun missingValuesAreNotFalsified() {
        assertEquals("–", AmbientForecastPresentation.temp(null, false))
        assertEquals("–", AmbientForecastPresentation.temp(100.0, false))
        assertEquals("29°", AmbientForecastPresentation.temp(29.2, false))
        assertEquals("84°", AmbientForecastPresentation.temp(29.0, true))
        assertEquals("25%", AmbientForecastPresentation.rain(25.4))
        assertEquals("", AmbientForecastPresentation.rain(null))
        assertTrue(AmbientForecastPresentation.dayName("2026-10-26", Locale.forLanguageTag("pt-BR")).isNotBlank())
    }
}

class AmbientLayoutPolicyTest {
    private fun inside(pos: AmbientLayoutPolicy.Position, size: AmbientLayoutPolicy.Size,
                       w: Int, h: Int, marginX: Int, marginY: Int) {
        assertTrue("left margin", pos.x >= marginX)
        assertTrue("top margin", pos.y >= marginY)
        assertTrue("right margin", pos.x + size.width <= w - marginX)
        assertTrue("bottom margin", pos.y + size.height <= h - marginY)
    }

    @Test fun marginsAndNonOverlapAtFullHdAndFourKWithSevenDays() {
        for ((width, height) in listOf(1920 to 1080, 3840 to 2160)) {
            val marginX = (width * .05).toInt()
            val marginY = (height * .05).toInt()
            val camera = AmbientLayoutPolicy.Size(width / 4, height / 7)
            val viewer = AmbientLayoutPolicy.Size(width / 3, height / 2)
            for (corner in 0..3) {
                val result = AmbientLayoutPolicy.layout(width, height, marginX, marginY,
                    camera, viewer, corner)
                assertNotNull("Layout unexpectedly refused corner $corner", result)
                val first = result!!.first
                val second = result.second
                inside(first, camera, width, height, marginX, marginY)
                inside(second, viewer, width, height, marginX, marginY)
                val crosses = first.x < second.x + viewer.width && first.x + camera.width > second.x &&
                    first.y < second.y + viewer.height && first.y + camera.height > second.y
                assertFalse("camera/forecast overlap", crosses)
            }
        }
    }

    @Test fun unsafePanelSizeOrIntersectionDoesNotReturnCoordinates() {
        val unavailable = AmbientLayoutPolicy.layout(1280, 720, 65, 48,
            AmbientLayoutPolicy.Size(1200, 500), AmbientLayoutPolicy.Size(1050, 620), 0)
        assertNull(unavailable)
        val safeViewerOnly = AmbientLayoutPolicy.layout(1920, 1080, 100, 70,
            AmbientLayoutPolicy.Size(0, 0), AmbientLayoutPolicy.Size(720, 550), 2)
        assertNotNull(safeViewerOnly)
    }
}
