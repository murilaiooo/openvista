package com.openvista.prototype.update

import android.content.Context
import android.content.Intent
import com.openvista.prototype.R
import java.io.File

/** Only the coordinator can issue a release or a verified artifact. */
data class UpdateRelease internal constructor(
    val tag: String,
    val versionCode: Long,
    val bytes: Long,
    internal val downloadUrl: String,
    internal val sha256: String,
)

data class DownloadedUpdate internal constructor(
    val release: UpdateRelease,
    internal val file: File,
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object Unavailable : UpdateState
    data class Available(val release: UpdateRelease) : UpdateState
    data class Downloading(val bytes: Long, val total: Long) : UpdateState
    data class Ready(val update: DownloadedUpdate) : UpdateState
    data object NeedsPermission : UpdateState
    data class ConfirmationRequired(val intent: Intent) : UpdateState
    data object Installing : UpdateState
    data object Installed : UpdateState
    data object Failed : UpdateState
    data object Cancelled : UpdateState

    fun message(context: Context): String = context.getString(when (this) {
        Idle -> R.string.update_idle
        Checking -> R.string.update_checking
        Unavailable -> R.string.update_unavailable
        is Available -> R.string.update_available
        is Downloading -> R.string.update_downloading
        is Ready -> R.string.update_ready
        NeedsPermission -> R.string.update_permission
        is ConfirmationRequired -> R.string.update_confirmation
        Installing -> R.string.update_installing
        Installed -> R.string.update_installed
        Failed -> R.string.update_failed
        Cancelled -> R.string.update_cancelled
    })
}

/** Activation requires owner-approved publication and the G-09 evidence in the release contract. */
internal object UpdateChannel {
    const val APPROVED = false
    const val API = "https://api.github.com/repos/murilaiooo/openvista/releases/latest"
    const val PROJECT = "https://github.com/murilaiooo/openvista/releases/download/"
    const val MAX_APK_BYTES = 268_435_456L
    const val MAX_JSON_BYTES = 524_288
}
