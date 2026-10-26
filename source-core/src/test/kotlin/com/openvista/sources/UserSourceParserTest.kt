package com.openvista.sources

import org.junit.Assert.*
import org.junit.Test

class UserSourceParserTest {
    private val parser = UserSourceParser()
    private val hls = "https://example.invalid/live.m3u8?token=private"
    private val dash = "https://example.invalid/manifest.mpd"

    @Test fun explicitSourceAcceptsSignedHlsUrlWithoutNetworkRequests() {
        val source = parser.manualUrl(hls, StreamKind.HLS, "Beach")
        assertNotNull(source)
        assertEquals(StreamKind.HLS, source?.kind)
        assertEquals(hls, source?.url)
        assertEquals("Beach", source?.title)
    }

    @Test fun localTestHlsUrlsAreValidWhenExplicitlyProvided() {
        assertNotNull(parser.manualUrl("http://127.0.0.1:18444/ov-hls-test.m3u8", StreamKind.HLS))
        assertNotNull(parser.manualUrl("http://localhost:18444/ov-hls-test.m3u8", StreamKind.HLS))
    }

    @Test fun manualSourceRejectsUnsafeSchemesAndEmbeddedCredentials() {
        for (url in listOf(
            "file:///storage/test.m3u8", "content://authority/path",
            "javascript:alert(1)", "ftp://example.invalid/live.m3u8",
            "https://user:secret@example.invalid/live.m3u8", "https://example.invalid/stream\nnext"
        )) assertNull(url, parser.manualUrl(url, StreamKind.HLS, "User stream"))
    }

    @Test fun signedUrlsWithoutExtensionsRequireExplicitProtocol() {
        assertNull(parser.detectKind("https://example.invalid/api/stream?token=value"))
        assertEquals(StreamKind.HLS, parser.manualUrl("https://example.invalid/api/stream?token=value", StreamKind.HLS)?.kind)
        assertEquals(StreamKind.DASH, parser.detectKind(dash))
    }

    @Test fun importsUserM3uEntriesWithTitlesAndDeduplication() {
        val content = """
            #EXTM3U
            #EXTINF:-1 tvg-id="one" group-title="City",City View
            $hls
            #EXTINF:-1,Repeated
            $hls
            #EXTINF:-1,Mountain
            $dash
        """.trimIndent()
        val result = parser.parseM3u(content.toByteArray(Charsets.UTF_8))
        assertTrue(result is PlaylistResult.Ready)
        val ready = result as PlaylistResult.Ready
        assertEquals(2, ready.sources.size)
        assertEquals(1, ready.duplicateEntries)
        assertEquals(0, ready.skippedEntries)
        assertEquals("City View", ready.sources.first().title)
        assertEquals(StreamKind.DASH, ready.sources.last().kind)
    }

    @Test fun rejectsHlsVariantManifestInsteadOfTreatingItAsList() {
        val manifest = "#EXTM3U\n#EXT-X-VERSION:3\n#EXT-X-STREAM-INF:BANDWIDTH=1000000\nhttps://example.invalid/variant.m3u8"
        assertEquals(PlaylistFailure.HLS_MANIFEST, (parser.parseM3u(manifest.toByteArray()) as PlaylistResult.Rejected).reason)
    }

    @Test fun rejectsOversizedPlaylistAndInvalidUtf8() {
        val huge = ByteArray(UserSourceParser.MAX_FILE_BYTES + 1) { 'A'.code.toByte() }
        assertEquals(PlaylistFailure.FILE_TOO_LARGE, (parser.parseM3u(huge) as PlaylistResult.Rejected).reason)
        assertEquals(PlaylistFailure.INVALID_UTF8, (parser.parseM3u(byteArrayOf(0xC3.toByte(), 0x28)) as PlaylistResult.Rejected).reason)
    }

    @Test fun rejectsMalformedOrUnsupportedEntriesWithoutExposingUrls() {
        val body = "#EXTM3U\n#EXTINF:-1,Invalid\nfile:///secret.m3u8\n#EXTINF:-1,Unknown format\nhttps://example.invalid/path\n$hls"
        val result = parser.parseM3u(body.toByteArray()) as PlaylistResult.Ready
        assertEquals(1, result.sources.size)
        assertEquals(2, result.skippedEntries)
    }

    @Test fun enforcesSourceCountAndUrlLengthBounds() {
        val content = buildString {
            append("#EXTM3U\n")
            repeat(UserSourceParser.MAX_ENTRIES + 1) { index ->
                append("https://example.invalid/$index.m3u8\n")
            }
        }
        assertEquals(PlaylistFailure.TOO_MANY_ENTRIES, (parser.parseM3u(content.toByteArray()) as PlaylistResult.Rejected).reason)
        assertNull(parser.manualUrl("https://example.invalid/" + "x".repeat(UserSourceParser.MAX_URL_LENGTH), StreamKind.HLS))
    }

    @Test fun limitsTitlesAndSupportsBomAndCommentLines() {
        val content = "\uFEFF#EXTM3U\n# ordinary comment\n#EXTINF:-1,${"Long".repeat(100)}\n$hls"
        val result = parser.parseM3u(content.toByteArray()) as PlaylistResult.Ready
        assertEquals(1, result.sources.size)
        assertEquals(UserSourceParser.MAX_TITLE_LENGTH, result.sources.first().title.length)
    }

    @Test fun readsSelectedDocumentWithinSizeLimit() {
        val body = "#EXTM3U tvg-url=\"metadata-only\"\n#EXTINF:-1,Coast\n$hls"
        val input = java.io.ByteArrayInputStream(body.toByteArray(Charsets.UTF_8))
        val result = parser.parseM3u(input) as PlaylistResult.Ready
        assertEquals(1, result.sources.size)
        assertEquals("Coast", result.sources.first().title)
    }

    @Test fun documentTooLargeFailsWithoutReadingAllBytes() {
        val data = ByteArray(UserSourceParser.MAX_FILE_BYTES + 100_000) { 65 }
        val result = parser.parseM3u(java.io.ByteArrayInputStream(data)) as PlaylistResult.Rejected
        assertEquals(PlaylistFailure.FILE_TOO_LARGE, result.reason)
    }

    @Test fun aProviderReturningOnlyZeroBytesDoesNotHang() {
        val input = object : java.io.InputStream() {
            override fun read(): Int = -1
            override fun read(buffer: ByteArray, off: Int, length: Int): Int = 0
        }
        val result = parser.parseM3u(input) as PlaylistResult.Rejected
        assertEquals(PlaylistFailure.READ_FAILED, result.reason)
    }

    @Test fun aFailingDocumentProviderDoesNotThrowOrExposeDetails() {
        val input = object : java.io.InputStream() {
            override fun read(): Int = throw java.io.IOException("private filename")
        }
        val result = parser.parseM3u(input) as PlaylistResult.Rejected
        assertEquals(PlaylistFailure.READ_FAILED, result.reason)
    }

    @Test fun noSupportedLinksIsNotReportedAsSuccess() {
        val body = "#EXTM3U\n#EXTINF:-1,Unknown\nrtsp://example.invalid/camera"
        assertEquals(PlaylistFailure.NO_SUPPORTED_SOURCES, (parser.parseM3u(body.toByteArray()) as PlaylistResult.Rejected).reason)
    }
}
