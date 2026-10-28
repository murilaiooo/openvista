package com.openvista.prototype.playback

import com.openvista.prototype.R

/**
 * A playback state the screen may need to explain. Routine states stay silent so the
 * scene is never covered by loading text; failures are shown briefly or until resolved.
 */
data class PlaybackNotice(val message: Int, val kind: Kind, val storageError: Boolean = false) {
    enum class Kind {
        /** Connecting, playing and paused: nothing is drawn over the video. */
        QUIET,
        /** One source was skipped or the session changed mode; shown for a few seconds. */
        BRIEF,
        /** Nothing can play until the user or the network acts; stays until playback resumes. */
        PERSISTENT,
    }

    companion object {
        fun of(message: Int, storageError: Boolean = false) = PlaybackNotice(message, when (message) {
            R.string.playback_empty, R.string.playback_all_unavailable, R.string.playback_all_cameras_retrying,
            R.string.playback_offline, R.string.playback_engine_unavailable -> Kind.PERSISTENT
            R.string.playback_camera_unavailable, R.string.playback_video_unavailable, R.string.playback_using_fallback,
            R.string.playback_recovery_wait, R.string.playback_staying_local, R.string.playback_storage_error -> Kind.BRIEF
            else -> Kind.QUIET
        }, storageError)
    }
}
