package com.openvista.prototype.pairing

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Base64
import com.openvista.prototype.R
import com.openvista.prototype.ui.AppLocale
import com.openvista.sources.StreamKind
import com.openvista.sources.UserSource
import com.openvista.sources.UserSourceParser
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InterfaceAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.security.SecureRandom
import java.util.Collections
import java.util.Locale
import java.util.concurrent.Executors

data class PairingOffer(val url: String, val expiresAtElapsedMs: Long)
data class PairingRequest(val id: String, val source: UserSource, val displayName: String)

/** The gate cannot be enabled through preferences, intents, network input or the phone page. */
internal object PairingSecurity {
    const val REVIEWED = false
    const val SESSION_MS = 120_000L
}

/** Call close when the pairing screen leaves the foreground. Nothing starts in the constructor. */
class PairingCoordinator(context: Context) : AutoCloseable {
    private val app = context.applicationContext
    private val localized get() = AppLocale.wrap(app)
    private val main = Handler(Looper.getMainLooper())
    private val lock = Any()
    private var session: LocalPairingSession? = null
    private var pending: PairingRequest? = null
    private var generation = 0L
    private var expires = 0L
    private var status: ((String) -> Unit)? = null
    private var expiry: Runnable? = null

    fun availabilityMessage(): String = localized.getString(if (PairingSecurity.REVIEWED)
        R.string.pairing_start_description else R.string.pairing_review_pending)

    fun start(onOffer: (PairingOffer) -> Unit, onRequest: (PairingRequest) -> Unit, onStatus: (String) -> Unit) {
        close()
        if (!PairingSecurity.REVIEWED) {
            val id = synchronized(lock) { generation }
            main.post { if (active(id)) onStatus(localized.getString(R.string.pairing_review_pending)) }
            return
        }
        val id = synchronized(lock) { status = onStatus; ++generation }
        val created = LocalPairingSession(localized, onOffer = { offer ->
            synchronized(lock) { if (generation == id) expires = offer.expiresAtElapsedMs }
            main.post {
                if (active(id)) {
                    onOffer(offer)
                    if (active(id)) onStatus(localized.getString(R.string.pairing_waiting))
                }
            }
        }, onSource = { source ->
            val request = PairingRequest(randomToken(16), source, source.title)
            synchronized(lock) {
                if (generation == id && SystemClock.elapsedRealtime() < expires) pending = request
            }
            main.post {
                if (active(id) && synchronized(lock) { pending?.id == request.id }) {
                    onRequest(request)
                    if (active(id)) onStatus(localized.getString(R.string.pairing_confirm_tv))
                }
            }
        }, onStopped = { failed ->
            main.post {
                if (active(id)) {
                    val sink = status
                    close()
                    sink?.invoke(localized.getString(if (failed) R.string.pairing_unavailable else R.string.pairing_expired))
                }
            }
        })
        val end = Runnable {
            if (active(id)) {
                val sink = status
                close()
                sink?.invoke(localized.getString(R.string.pairing_expired))
            }
        }
        synchronized(lock) {
            if (generation != id) {
                created.close()
                return
            }
            session = created
            expiry = end
            main.postDelayed(end, PairingSecurity.SESSION_MS)
            created.start()
        }
    }

    /** Return once, only in direct response to the TV confirmation button. Caller owns persistence. */
    fun approve(id: String): PairingRequest? {
        return synchronized(lock) {
            val request = pending
            if (request?.id != id || SystemClock.elapsedRealtime() >= expires || session == null) null
            else request.also { close() }
        }
    }

    fun deny(id: String) {
        synchronized(lock) { if (pending?.id == id) close() }
    }

    override fun close() {
        val current = synchronized(lock) {
            generation++
            pending = null
            expires = 0
            status = null
            expiry?.let { main.removeCallbacks(it) }
            expiry = null
            session.also { session = null }
        }
        current?.close()
    }

    private fun active(id: Long): Boolean = synchronized(lock) { generation == id }
}

/** Internal protocol implementation, reachable only after the reviewed build gate is enabled. */
private class LocalPairingSession(
    private val context: Context,
    private val onOffer: (PairingOffer) -> Unit,
    private val onSource: (UserSource) -> Unit,
    private val onStopped: (Boolean) -> Unit,
) : AutoCloseable {
    private val worker = Executors.newSingleThreadExecutor()
    private val cipher = PairingCipher()
    private val scriptNonce = randomToken(18)
    private val deadline = SystemClock.elapsedRealtime() + PairingSecurity.SESSION_MS
    private val parser = UserSourceParser()
    private val transportLock = Any()
    @Volatile private var closed = false
    @Volatile private var server: ServerSocket? = null
    @Volatile private var client: Socket? = null
    private var consumed = false
    private lateinit var address: InterfaceAddress
    private lateinit var authority: String

    fun start() {
        worker.execute {
            var failed = false
            try {
                check(!closed)
                address = privateInterface()
                val listening = synchronized(transportLock) {
                    check(!closed)
                    ServerSocket().also {
                        server = it
                        it.reuseAddress = false
                        it.soTimeout = 500
                        it.bind(java.net.InetSocketAddress(address.address, 0), 2)
                    }
                }
                check(!closed)
                authority = "${address.address.hostAddress}:${listening.localPort}"
                onOffer(PairingOffer("http://$authority/#${cipher.fragment()}", deadline))
                var requests = 0
                while (!closed && SystemClock.elapsedRealtime() < deadline && requests < 24) {
                    check(boundInterfacePresent())
                    val socket = try { listening.accept() } catch (_: SocketTimeoutException) { continue }
                    synchronized(transportLock) {
                        if (closed) { socket.close(); return@execute }
                        client = socket
                    }
                    requests++
                    socket.use {
                        it.soTimeout = 1500
                        if (sameSubnet(it.inetAddress)) runCatching { serve(it) }
                    }
                    client = null
                    if (consumed) break
                }
            } catch (_: Exception) { failed = !closed }
            finally { close(); if (!consumed) onStopped(failed) }
        }
    }

    override fun close() {
        synchronized(transportLock) {
            closed = true
            runCatching { client?.close() }
            runCatching { server?.close() }
            cipher.close()
        }
        worker.shutdownNow()
    }

    private fun serve(socket: Socket) {
        check(boundInterfacePresent())
        val requestDeadline = minOf(deadline, SystemClock.elapsedRealtime() + 2000)
        val input = socket.getInputStream().buffered()
        var headerBytes = 0
        fun line(): String {
            val bytes = ByteArrayOutputStream()
            while (true) {
                check(!closed && SystemClock.elapsedRealtime() < requestDeadline)
                val value = input.read()
                check(!closed && SystemClock.elapsedRealtime() < requestDeadline)
                check(value >= 0 && ++headerBytes <= 8192 && bytes.size() <= 1024)
                if (value == 10) {
                    val raw = bytes.toByteArray()
                    check(raw.isNotEmpty() && raw.last() == 13.toByte())
                    return String(raw, 0, raw.size - 1, Charsets.US_ASCII)
                }
                check(value in 32..126 || value == 13)
                bytes.write(value)
            }
        }
        val first = line().split(' ')
        check(first.size == 3 && first[2] == "HTTP/1.1")
        val headers = mutableMapOf<String, String>()
        while (true) {
            val next = line()
            if (next.isEmpty()) break
            val colon = next.indexOf(':')
            check(colon > 0 && headers.size < 32)
            val name = next.substring(0, colon).lowercase(Locale.ROOT)
            check(name.matches(Regex("[a-z0-9-]+")) && name !in headers)
            headers[name] = next.substring(colon + 1).trim()
        }
        check(headers["host"] == authority && "transfer-encoding" !in headers && "expect" !in headers)
        when {
            first[0] == "GET" && first[1] == "/" && headers["content-length"].orEmpty() in setOf("", "0") -> {
                respond(socket, 200, "text/html; charset=utf-8", PairingPage.render(context, scriptNonce), true)
            }
            first[0] == "POST" && first[1] == "/source" -> {
                check(!consumed && SystemClock.elapsedRealtime() < deadline)
                check(headers["origin"] == "http://$authority")
                check(headers["sec-fetch-site"] == null || headers["sec-fetch-site"] == "same-origin")
                check(headers["content-type"] == "application/json")
                check("authorization" !in headers && "cookie" !in headers)
                val lengthText = headers["content-length"].orEmpty()
                check(lengthText.matches(Regex("[1-9][0-9]{0,3}")))
                val length = lengthText.toInt()
                check(length in 1..PairingCipher.MAX_ENVELOPE_BYTES)
                val body = ByteArray(length)
                var read = 0
                while (read < length) {
                    check(!closed && SystemClock.elapsedRealtime() < requestDeadline)
                    val count = input.read(body, read, length - read)
                    check(count > 0 && !closed && SystemClock.elapsedRealtime() < requestDeadline)
                    read += count
                }
                val decoder = Charsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                val envelope = JSONObject(decoder.decode(java.nio.ByteBuffer.wrap(body)).toString())
                val json = cipher.open(envelope, authority)
                check(json.length() == 3 && json.has("url") && json.has("title") && json.has("kind"))
                val title = json.getString("title")
                check(title.length <= 120 && title.none { it.isISOControl() })
                val kind = StreamKind.valueOf(json.getString("kind"))
                val source = parser.manualUrl(json.getString("url"), kind,
                    title.ifBlank { context.getString(R.string.pairing_default_title) }) ?: error("Invalid source")
                check(!closed && SystemClock.elapsedRealtime() < deadline)
                consumed = true
                onSource(source)
                respond(socket, 202, "application/json", cipher.acknowledgement(authority), false)
                // Close the listener immediately after one accepted submission; TV approval remains pending.
                server?.close()
            }
            else -> respond(socket, 400, "application/json", "{}", false)
        }
    }

    private fun respond(socket: Socket, code: Int, contentType: String, body: String, page: Boolean) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        val policy = if (page) "default-src 'none'; script-src 'nonce-$scriptNonce'; connect-src 'self'; style-src 'unsafe-inline'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'" else "default-src 'none'; frame-ancestors 'none'"
        val headers = "HTTP/1.1 $code Response\r\nContent-Type: $contentType\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\nCache-Control: no-store\r\nReferrer-Policy: no-referrer\r\nX-Content-Type-Options: nosniff\r\nX-Frame-Options: DENY\r\nContent-Security-Policy: $policy\r\n\r\n"
        socket.getOutputStream().apply {
            write(headers.toByteArray(Charsets.US_ASCII))
            write(bytes)
            flush()
        }
    }

    private fun privateInterface(): InterfaceAddress {
        val candidates = Collections.list(NetworkInterface.getNetworkInterfaces()).filter {
            it.isUp && !it.isLoopback && !it.isVirtual
        }.flatMap { it.interfaceAddresses }.filter {
            it.address is Inet4Address && it.address.isSiteLocalAddress &&
                !it.address.isLoopbackAddress && it.networkPrefixLength in 8..30
        }
        check(candidates.size == 1)
        return candidates.single()
    }

    private fun sameSubnet(peer: InetAddress): Boolean {
        if (peer !is Inet4Address || !peer.isSiteLocalAddress) return false
        val ours = address.address.address
        val theirs = peer.address
        val bits = address.networkPrefixLength.toInt()
        return (0 until bits).all { bit ->
            val mask = 1 shl (7 - bit % 8)
            (ours[bit / 8].toInt() and mask) == (theirs[bit / 8].toInt() and mask)
        }
    }

    private fun boundInterfacePresent(): Boolean {
        val current = NetworkInterface.getByInetAddress(address.address) ?: return false
        return current.isUp && !current.isLoopback && !current.isVirtual &&
            current.interfaceAddresses.any {
                it.address == address.address && it.networkPrefixLength == address.networkPrefixLength
            }
    }
}

private fun randomToken(bytes: Int): String = ByteArray(bytes).also { SecureRandom().nextBytes(it) }
    .let { Base64.encodeToString(it, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP) }
