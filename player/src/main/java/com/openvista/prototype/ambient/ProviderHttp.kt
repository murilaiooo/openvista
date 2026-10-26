package com.openvista.prototype.ambient

import java.io.ByteArrayOutputStream
import java.io.InterruptedIOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/** Bounded, identified HTTPS requests. Redirects never expand a provider's authority. */
internal object ProviderHttp {
    const val USER_AGENT = "OpenVista/0.2 (https://github.com/murilaiooo/openvista)"

    fun bytes(url: URL, host: String, limit: Int, accept: String): ByteArray {
        require(url.protocol == "https" && url.host == host && url.port == -1 && url.userInfo == null && url.ref == null)
        interrupted()
        val connection = url.openConnection() as HttpsURLConnection
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 7_000
        connection.readTimeout = 7_000
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.setRequestProperty("Accept", accept)
        try {
            require(connection.responseCode == 200) { "Provider unavailable." }
            require(connection.contentLengthLong < 0 || connection.contentLengthLong <= limit)
            return connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8_192)
                while (true) {
                    interrupted()
                    val count = input.read(buffer)
                    if (count < 0) break
                    require(count > 0 && count <= limit - output.size()) { "Provider response exceeds limit." }
                    output.write(buffer, 0, count)
                }
                interrupted()
                output.toByteArray()
            }
        } finally { connection.disconnect() }
    }

    fun interrupted() { if (Thread.currentThread().isInterrupted) throw InterruptedIOException("Provider request cancelled.") }
}
