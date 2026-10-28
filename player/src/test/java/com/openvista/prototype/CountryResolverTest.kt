package com.openvista.prototype

import com.openvista.prototype.ambient.CountryResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class CountryResolverTest {
    private val noZone: (String) -> String? = { null }

    @Test
    fun savedCountryMatchesCodesAndNamesInAnySupportedLanguage() {
        listOf("IT", "it", "ITA", "Italy", "Italia", "Itália", "ITÁLIA", " italy ").forEach {
            assertEquals(it, "IT", CountryResolver.resolve(it, null, noZone))
        }
        listOf("USA", "United States", "United States of America", "Estados Unidos", "EUA").forEach {
            assertEquals(it, "US", CountryResolver.resolve(it, null, noZone))
        }
        assertEquals("GB", CountryResolver.resolve("UK", null, noZone))
        assertEquals("BR", CountryResolver.resolve("Brasil", null, noZone))
        assertEquals("JP", CountryResolver.resolve("Japão", null, noZone))
    }

    @Test
    fun missingCountryFallsBackToTheCameraTimeZoneOnly() {
        val regions = mapOf("Europe/Rome" to "IT", "Etc/UTC" to "001", "America/New_York" to "US")
        assertEquals("IT", CountryResolver.resolve("", "Europe/Rome", regions::get))
        assertEquals("US", CountryResolver.resolve("  ", "America/New_York", regions::get))
        assertNull(CountryResolver.resolve("", "Etc/UTC", regions::get))
        assertNull(CountryResolver.resolve("", null, regions::get))
        assertNull(CountryResolver.resolve("", "Mars/Base", regions::get))
    }

    @Test
    fun displayNameFollowsTheInterfaceLanguageAndKeepsUnmappedText() {
        val portuguese = Locale("pt", "BR")
        assertEquals("Itália", CountryResolver.displayName("Italy", "IT", portuguese))
        assertEquals("Italy", CountryResolver.displayName("Itália", "IT", Locale.ENGLISH))
        assertEquals("Italy", CountryResolver.displayName("", "IT", Locale.ENGLISH))
        // A region the user wrote on purpose is never replaced by the zone's country.
        assertEquals("Hawaii", CountryResolver.displayName("Hawaii", "US", Locale.ENGLISH))
        assertEquals("", CountryResolver.displayName("", null, Locale.ENGLISH))
    }

    @Test
    fun flagUsesRegionalIndicators() {
        assertEquals("🇮🇹", CountryResolver.flag("IT"))
        assertEquals("🇧🇷", CountryResolver.flag("BR"))
        assertEquals("", CountryResolver.flag("001"))
        assertEquals("", CountryResolver.flag("it"))
    }
}
