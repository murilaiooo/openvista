package com.openvista.prototype

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DreamScreenPolicyTest {
    private fun enabled(
        dreaming: Boolean = true,
        attached: Boolean = true,
        startupFailed: Boolean = false,
        sessionAvailable: Boolean = true,
        playWhenReady: Boolean = true,
        playbackReadyOrBuffering: Boolean = true,
    ) = DreamScreenPolicy.shouldStayVisible(
        dreaming, attached, startupFailed, sessionAvailable, playWhenReady, playbackReadyOrBuffering,
    )

    @Test fun activelyPlayingDreamKeepsDisplayVisible() {
        assertTrue(enabled())
    }

    @Test fun pausedOrStoppedVideoAllowsNormalSleep() {
        assertFalse(enabled(playWhenReady = false))
        assertFalse(enabled(playbackReadyOrBuffering = false))
    }

    @Test fun unfinishedOrInvalidDreamCannotKeepDisplayAwake() {
        assertFalse(enabled(dreaming = false))
        assertFalse(enabled(attached = false))
        assertFalse(enabled(startupFailed = true))
        assertFalse(enabled(sessionAvailable = false))
    }
}
