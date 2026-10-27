package com.openvista.prototype.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackAudioPolicyTest {
    @Test fun screensaverIsAlwaysSilentEvenWhenNormalSoundIsEnabled() {
        assertEquals(0f, PlaybackAudioPolicy.volume(userMuted = false, screensaverSilent = true), 0f)
        assertEquals(0f, PlaybackAudioPolicy.volume(userMuted = true, screensaverSilent = true), 0f)
    }

    @Test fun normalPlaybackStillHonorsItsOwnSoundPreference() {
        assertEquals(1f, PlaybackAudioPolicy.volume(userMuted = false, screensaverSilent = false), 0f)
        assertEquals(0f, PlaybackAudioPolicy.volume(userMuted = true, screensaverSilent = false), 0f)
    }
}
