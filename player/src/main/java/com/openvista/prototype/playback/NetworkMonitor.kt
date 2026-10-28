package com.openvista.prototype.playback

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper

/**
 * Reports whether the TV currently has an internet-capable default network.
 *
 * Playback uses this to tell a failing camera apart from a disconnected TV, so a
 * router restart does not mark every saved camera unavailable. Callbacks arrive on
 * the main thread. Fire OS does not always report VALIDATED, so INTERNET is enough.
 */
class NetworkMonitor(context: Context, private val onChange: (online: Boolean) -> Unit) {
    private val connectivity = context.applicationContext.getSystemService(ConnectivityManager::class.java)
    private val main = Handler(Looper.getMainLooper())
    private var registered = false
    private var lastOnline = true
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = post()
        override fun onLost(network: Network) = post()
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = post()
    }

    fun start() {
        if (registered || connectivity == null) return
        lastOnline = isOnline()
        try {
            connectivity.registerDefaultNetworkCallback(callback)
            registered = true
        } catch (_: RuntimeException) {
            // Without callbacks, playback keeps its timeout-based behavior.
        }
    }

    fun isOnline(): Boolean {
        val manager = connectivity ?: return true
        return try {
            val network = manager.activeNetwork ?: return false
            manager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (_: RuntimeException) { true }
    }

    fun stop() {
        main.removeCallbacksAndMessages(null)
        if (!registered) return
        registered = false
        try { connectivity?.unregisterNetworkCallback(callback) } catch (_: RuntimeException) { }
    }

    private fun post() {
        main.post {
            if (!registered) return@post
            val online = isOnline()
            if (online != lastOnline) {
                lastOnline = online
                onChange(online)
            }
        }
    }
}
