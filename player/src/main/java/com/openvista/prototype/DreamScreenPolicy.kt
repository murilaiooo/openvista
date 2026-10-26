package com.openvista.prototype

/** Keep an active video dream visible without changing the TV's system power settings. */
internal object DreamScreenPolicy {
    fun shouldStayVisible(
        dreaming: Boolean,
        attached: Boolean,
        startupFailed: Boolean,
        sessionAvailable: Boolean,
        playWhenReady: Boolean,
        playbackReadyOrBuffering: Boolean,
    ): Boolean = dreaming && attached && !startupFailed && sessionAvailable &&
        playWhenReady && playbackReadyOrBuffering
}
