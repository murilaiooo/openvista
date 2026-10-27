package com.openvista.sources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeVideoUrlTest {
    private val videoId = "jfKfPfyJRdk"

    @Test fun supportsConcreteLiveAndWatchLinks() {
        listOf(
            "https://www.youtube.com/watch?v=$videoId",
            "https://www.youtube.com/watch?feature=share&v=$videoId&t=9",
            "https://youtube.com/live/$videoId",
            "https://m.youtube.com/live/$videoId?si=test",
            "https://youtu.be/$videoId?t=20",
            "https://www.youtube-nocookie.com/embed/$videoId",
            "https://youtube.com/embed/$videoId",
        ).forEach { assertEquals(it, videoId, YouTubeVideoUrl.videoId(it)) }
    }

    @Test fun rejectsUnresolvedOrSuspiciousLinks() {
        listOf(
            "", "https://www.youtube.com/@example/live", "https://youtube.com/playlist?list=LL",
            "https://youtube.com/watch?v=invalid", "https://youtube.com/watch?v=$videoId&v=$videoId",
            "http://youtube.com/live/$videoId", "https://youtube.com.evil.example/live/$videoId",
            "https://user:pass@youtube.com/watch?v=$videoId", "https://youtube.com:8080/live/$videoId",
            "https://youtube.com/live/$videoId#fragment", "javascript:alert(1)", "https://youtu.be/$videoId/path",
            "https://youtube.com/watch?v=$videoId%26bad",
        ).forEach { assertNull(it, YouTubeVideoUrl.videoId(it)) }
    }
}
