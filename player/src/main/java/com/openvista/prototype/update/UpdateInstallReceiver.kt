package com.openvista.prototype.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import java.io.File

/** Explicit, non-exported recipient for the installer-owned PendingIntent capability. */
class UpdateInstallReceiver : BroadcastReceiver() {
    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val session = prefs.getInt("session", -1)
        val nonce = prefs.getString("nonce", null) ?: return
        if (session < 0 || intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1) != session ||
            intent.getStringExtra(NONCE) != nonce || intent.data?.toString() != "openvista-update://session/$session/$nonce") return
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val state = when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmation = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                val target = confirmation?.resolveActivity(context.packageManager)
                val trusted = target?.let {
                    runCatching { context.packageManager.getApplicationInfo(it.packageName, 0) }
                        .getOrNull()?.let { info -> info.flags and ApplicationInfo.FLAG_SYSTEM != 0 } == true
                } == true
                if (confirmation != null && target != null && trusted) {
                    confirmation.component = target
                    if (prefs.edit().putString("confirmation", confirmation.toUri(Intent.URI_INTENT_SCHEME)).commit())
                        UpdateState.ConfirmationRequired(confirmation) else UpdateState.Failed
                } else UpdateState.Failed
            }
            PackageInstaller.STATUS_SUCCESS -> UpdateState.Installed
            else -> UpdateState.Failed
        }
        if (status != PackageInstaller.STATUS_PENDING_USER_ACTION || state == UpdateState.Failed) {
            prefs.edit().clear().putInt("last_status", status).commit()
        }
        if (state == UpdateState.Installed) clearInstalledDownload(context)
        listener?.invoke(state)
        if (status != PackageInstaller.STATUS_PENDING_USER_ACTION || state == UpdateState.Failed) listener = null
    }

    companion object {
        internal const val ACTION = "com.openvista.prototype.UPDATE_INSTALL_STATUS"
        internal const val NONCE = "installation_nonce"
        private const val PREFS = "update-install-session"
        @Volatile private var listener: ((UpdateState) -> Unit)? = null

        internal fun register(context: Context, session: Int, nonce: String, sink: (UpdateState) -> Unit) {
            check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .clear().putInt("session", session).putString("nonce", nonce).commit())
            listener = sink
        }

        internal fun forget(context: Context) {
            listener = null
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().commit()
        }

        internal fun attach(sink: (UpdateState) -> Unit) { listener = sink }
        internal fun detach() { listener = null }

        internal fun activeSession(context: Context): Int? {
            val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("session", -1)
            return value.takeIf { it >= 0 }
        }

        internal fun hasActiveSession(context: Context): Boolean = activeSession(context) != null

        private fun clearInstalledDownload(context: Context) {
            val provenance = context.getSharedPreferences("verified-update", Context.MODE_PRIVATE)
            val name = provenance.getString("file", null)
            if (name != null && name.matches(Regex("[a-f0-9-]{36}\\.apk"))) {
                runCatching {
                    val directory = File(context.cacheDir, "verified-updates")
                    val file = File(directory, name)
                    if (file.canonicalFile.parentFile == directory.canonicalFile) file.delete()
                }
            }
            provenance.edit().clear().commit()
        }

        /** Recover the narrow createSession/persist window without touching committed installations. */
        internal fun abandonUntrackedStaging(context: Context) {
            val installer = context.packageManager.packageInstaller
            val tracked = activeSession(context)
            runCatching { installer.mySessions }.getOrDefault(emptyList()).filter {
                it.sessionId != tracked && it.appPackageName == context.packageName && !it.isSealed
            }.forEach { runCatching { installer.abandonSession(it.sessionId) } }
        }

        @Suppress("DEPRECATION")
        internal fun savedState(context: Context): UpdateState? {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val active = activeSession(context)
            if (active != null) {
                val installer = context.packageManager.packageInstaller
                val info = runCatching { installer.getSessionInfo(active) }.getOrNull()
                // Retain the nonce for a possible delayed terminal result. Only explicit cancel forgets it.
                if (info == null) return UpdateState.Failed
                // A process can die while copying bytes, before commit. Such a session cannot finish.
                // API 26 isSealed distinguishes committed sessions on the Android 9 target.
                if (!info.isSealed) {
                    runCatching { installer.abandonSession(active) }
                    prefs.edit().clear().putInt("last_status", PackageInstaller.STATUS_FAILURE).commit()
                    return UpdateState.Failed
                }
            }
            val serialized = prefs.getString("confirmation", null)
            if (serialized != null && active != null) {
                val intent = runCatching { Intent.parseUri(serialized, Intent.URI_INTENT_SCHEME) }.getOrNull()
                val component = intent?.component
                val trusted = component?.let {
                    runCatching { context.packageManager.getApplicationInfo(it.packageName, 0) }
                        .getOrNull()?.let { info -> info.flags and ApplicationInfo.FLAG_SYSTEM != 0 } == true
                } == true
                return if (intent != null && trusted) UpdateState.ConfirmationRequired(intent) else UpdateState.Failed
            }
            if (active != null) return UpdateState.Installing
            val terminal = prefs.getInt("last_status", Int.MIN_VALUE)
            // A terminal notification is shown once; it must not block every future update check.
            if (terminal != Int.MIN_VALUE) prefs.edit().remove("last_status").commit()
            return when (terminal) {
                PackageInstaller.STATUS_SUCCESS -> UpdateState.Installed
                Int.MIN_VALUE -> null
                else -> UpdateState.Failed
            }
        }
    }
}
