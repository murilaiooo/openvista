package com.openvista.prototype

import com.openvista.prototype.playback.PlaybackNotice
import com.openvista.prototype.playback.PlaybackSession
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackRecoveryPolicyTest {
    @Test
    fun automaticCameraRetryBacksOffToAFifteenMinuteCeiling() {
        val minutes = (0..7).map { PlaybackSession.automaticRetryDelayMs(it) / 60_000 }
        assertEquals(listOf(1L, 2L, 4L, 8L, 15L, 15L, 15L, 15L), minutes)
        assertEquals(60_000L, PlaybackSession.automaticRetryDelayMs(-3))
    }

    @Test
    fun routineStatesNeverCoverTheScene() {
        listOf(R.string.playback_connecting, R.string.playback_playing, R.string.playback_paused,
            R.string.playback_checking_camera).forEach {
            assertEquals(PlaybackNotice.Kind.QUIET, PlaybackNotice.of(it).kind)
        }
    }

    @Test
    fun skippedSourcesAreBriefAndDeadEndsPersist() {
        listOf(R.string.playback_camera_unavailable, R.string.playback_video_unavailable,
            R.string.playback_using_fallback, R.string.playback_storage_error).forEach {
            assertEquals(PlaybackNotice.Kind.BRIEF, PlaybackNotice.of(it).kind)
        }
        listOf(R.string.playback_offline, R.string.playback_all_cameras_retrying,
            R.string.playback_all_unavailable, R.string.playback_empty).forEach {
            assertEquals(PlaybackNotice.Kind.PERSISTENT, PlaybackNotice.of(it).kind)
        }
    }
}
