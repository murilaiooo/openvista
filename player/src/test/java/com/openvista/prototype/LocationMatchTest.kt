package com.openvista.prototype

import com.openvista.prototype.ambient.GeoPlace
import com.openvista.prototype.ambient.LocationMatch
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.MediaFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationMatchTest {
    private fun camera(city: String = "", country: String = "", zone: String = "", lat: Double? = null, lon: Double? = null) =
        LibraryItem("id", "Camera", "https://example.invalid/live.m3u8", MediaFamily.CAMERAS, "HLS",
            country = country, city = city, latitude = lat, longitude = lon, timeZone = zone)

    private val parisFr = GeoPlace("Paris", "Île-de-France", "FR", "France", 48.85, 2.35, "Europe/Paris", 2_138_551)
    private val parisTx = GeoPlace("Paris", "Texas", "US", "United States", 33.66, -95.56, "America/Chicago", 24_782)
    private val springfieldIl = GeoPlace("Springfield", "Illinois", "US", "United States", 39.80, -89.64, "America/Chicago", 114_394)
    private val springfieldMa = GeoPlace("Springfield", "Massachusetts", "US", "United States", 42.10, -72.59, "America/New_York", 155_929)

    @Test
    fun onlyCamerasNamingACityWithMissingFieldsAreLookedUp() {
        assertTrue(LocationMatch.needsLookup(camera(city = "Venice")))
        assertTrue(LocationMatch.needsLookup(camera(city = "Venice", country = "Italy", lat = 45.4, lon = 12.3)))
        assertFalse(LocationMatch.needsLookup(camera()))
        assertFalse(LocationMatch.needsLookup(camera(city = "Venice", country = "Italy", zone = "Europe/Rome", lat = 45.4, lon = 12.3)))
        assertFalse(LocationMatch.needsLookup(camera(city = "Venice").copy(family = MediaFamily.VIDEOS, streamKind = null)))
    }

    @Test
    fun queryUsesTheSavedCountryOrTheCountryWrittenAfterTheCity() {
        assertEquals("Venice" to "IT", LocationMatch.query(camera(city = "Venice", country = "Itália")))
        assertEquals("Venice" to "IT", LocationMatch.query(camera(city = "Venice, Veneto, Italy")))
        assertEquals("Paris" to null, LocationMatch.query(camera(city = "Paris")))
        assertNull(LocationMatch.query(camera(city = " , ")))
    }

    @Test
    fun aDominantPlaceIsAcceptedAndAnAmbiguousNameIsNot() {
        assertEquals(parisFr, LocationMatch.choose("Paris", null, listOf(parisFr, parisTx)))
        assertNull(LocationMatch.choose("Springfield", null, listOf(springfieldMa, springfieldIl)))
        assertEquals(springfieldIl, LocationMatch.choose("Springfield", "US", listOf(springfieldIl)))
        assertEquals(parisTx, LocationMatch.choose("Paris", "US", listOf(parisFr, parisTx)))
        assertNull(LocationMatch.choose("Paris", "BR", listOf(parisFr, parisTx)))
    }

    @Test
    fun completionNeverReplacesWhatTheUserEntered() {
        val filled = LocationMatch.completed(camera(city = "Paris"), parisFr, "France")
        assertEquals(listOf(48.85, 2.35), listOf(filled.latitude, filled.longitude))
        assertEquals("Europe/Paris", filled.timeZone)
        assertEquals("France", filled.country)
        val kept = LocationMatch.completed(camera(city = "Paris", country = "França", lat = 48.9, lon = 2.3), parisFr, "France")
        assertEquals(listOf(48.9, 2.3), listOf(kept.latitude, kept.longitude))
        assertEquals("França", kept.country)
        assertEquals("Europe/Paris", kept.timeZone)
    }
}
