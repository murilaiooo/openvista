package com.openvista.prototype.playback

/** Screensaver silence overrides the normal player's independent mute preference. */
internal object PlaybackAudioPolicy {
    fun volume(userMuted: Boolean, screensaverSilent: Boolean): Float =
        if (screensaverSilent || userMuted) 0f else 1f
}
