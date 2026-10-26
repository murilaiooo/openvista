package com.openvista.sources

import org.junit.Assert.*
import org.junit.Test

class LocalVideoRulesTest {
    @Test fun acceptsOnlyExplicitlySupportedVideoMimeTypes() {
        assertTrue(LocalVideoRules.isSupported("video/mp4", "clip.mp4"))
        assertTrue(LocalVideoRules.isSupported("video/x-matroska", "clip.mkv"))
        assertTrue(LocalVideoRules.isSupported("video/webm", "clip.webm"))
        assertTrue(LocalVideoRules.isSupported("video/quicktime", "clip.mov"))
        assertFalse(LocalVideoRules.isSupported("video/unknown", "clip.bin"))
        assertFalse(LocalVideoRules.isSupported("image/jpeg", "clip.mp4"))
        assertFalse(LocalVideoRules.isSupported("audio/mp4", "audio.mp4"))
    }

    @Test fun genericMimeTypeMayUseSafeExtensionFallback() {
        assertTrue(LocalVideoRules.isSupported("application/octet-stream", "Holiday.MP4"))
        assertTrue(LocalVideoRules.isSupported(null, "cam.webm"))
        assertFalse(LocalVideoRules.isSupported("application/octet-stream", "list.m3u8"))
        assertFalse(LocalVideoRules.isSupported("application/octet-stream", "unknown.file"))
    }

    @Test fun displayedTitlesNeverIncludeProviderPathOrControlCharacters() {
        assertEquals("clip.mp4", LocalVideoRules.displayTitle("../private/clip.mp4"))
        assertEquals("clip.webm", LocalVideoRules.displayTitle("folder\\clip.webm"))
        assertEquals("file", LocalVideoRules.displayTitle("\u0000"))
        assertEquals(80, LocalVideoRules.displayTitle("x".repeat(120)).length)
    }
}
