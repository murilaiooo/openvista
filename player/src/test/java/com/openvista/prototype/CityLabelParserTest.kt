package com.openvista.prototype.ambient

import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.MediaFamily
import org.junit.Assert.*
import org.junit.Test

class CityLabelParserTest {
    private fun item(title: String, city: String = "", country: String = "") =
        LibraryItem(id = "test", title = title, uri = "https://example.invalid/live",
            family = MediaFamily.CAMERAS, city = city, country = country)

    @Test fun removesResolutionAndSceneLabelWithoutExposingMediaTitle() {
        assertEquals(listOf("Jurata"), CityLabelParser.queries(item("Jurata — Pier (2560x1440)")))
        assertEquals(listOf("Costa da Caparica"), CityLabelParser.queries(item("Costa da Caparica — Riviera (2560x1440)")))
        assertEquals(listOf("Karpacz"), CityLabelParser.queries(item("Karpacz — panorama (1920x1080)")))
        assertEquals(listOf("Gdynia Orłowo", "Gdynia"), CityLabelParser.queries(item("Gdynia Orłowo — Beach (1920x1080)")))
    }

    @Test fun rejectsArbitraryFilesAndUrlsFromBeingSentToGeocoding() {
        assertTrue(CityLabelParser.queries(item("QA HLS 720")).isEmpty())
        assertTrue(CityLabelParser.queries(item("https://private.example/path/key")).isEmpty())
        assertTrue(CityLabelParser.queries(item("holiday.mp4")).isEmpty())
        assertTrue(CityLabelParser.queries(item("Camera 1")).isEmpty())
        assertTrue(CityLabelParser.queries(item("QA HLS 720", "https://private.example/city")).isEmpty())
    }

    @Test fun explicitMetadataTakesPriorityAndNeverDisplaysWholeCameraName() {
        assertEquals(listOf("Tucson, United States", "Tucson"), CityLabelParser.queries(item("test", "Tucson", "United States")))
        assertEquals("Jurata", CityLabelParser.displayCity("Jurata, Pomeranian, Poland"))
        assertTrue(CityLabelParser.matches("Jurata", "Jurata, Pomeranian, Poland"))
        assertFalse(CityLabelParser.matches("Jurata", "Juratarai, India"))
    }
}
