package com.openvista.prototype.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller

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
                    prefs.edit().putString("confirmation", confirmation.toUri(Intent.URI_INTENT_SCHEME)).apply()
                    UpdateState.ConfirmationRequired(confirmation)
                } else UpdateState.Failed
            }
            PackageInstaller.STATUS_SUCCESS -> UpdateState.Installed
            else -> UpdateState.Failed
        }
        if (status != PackageInstaller.STATUS_PENDING_USER_ACTION || state == UpdateState.Failed) {
            prefs.edit().clear().putInt("last_status", status).apply()
        }
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
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        }

        internal fun attach(sink: (UpdateState) -> Unit) { listener = sink }
        internal fun detach() { listener = null }

        internal fun activeSession(context: Context): Int? {
            val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("session", -1)
            return value.takeIf { it >= 0 }
        }

        internal fun hasActiveSession(context: Context): Boolean = activeSession(context) != null

        @Suppress("DEPRECATION")
        internal fun savedState(context: Context): UpdateState? {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val active = activeSession(context)
            if (active != null) {
                val exists = runCatching { context.packageManager.packageInstaller.getSessionInfo(active) != null }
                    .getOrDefault(false)
                // Retain the nonce for a possible delayed terminal result. Only explicit cancel forgets it.
                if (!exists) return UpdateState.Failed
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
            return when (prefs.getInt("last_status", Int.MIN_VALUE)) {
                PackageInstaller.STATUS_SUCCESS -> UpdateState.Installed
                Int.MIN_VALUE -> null
                else -> UpdateState.Failed
            }
        }
    }
}
