package com.openvista.prototype.pairing

import android.util.Base64
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Session-only authenticated envelopes. This does not authenticate an HTTP-delivered phone page. */
internal class PairingCipher : AutoCloseable {
    private val random = SecureRandom()
    private val key = ByteArray(32).also(random::nextBytes)
    val sessionId = encode(ByteArray(16).also(random::nextBytes))
    private var destroyed = false

    @Synchronized
    fun fragment(): String {
        check(!destroyed)
        return "v1.$sessionId.${encode(key)}"
    }

    @Synchronized
    fun open(envelope: JSONObject, authority: String): JSONObject {
        check(!destroyed && envelope.length() == 4)
        check(envelope.opt("v") == 1 && envelope.getString("sid") == sessionId)
        val iv = decode(envelope.getString("iv"), 12, 12)
        val encrypted = decode(envelope.getString("ciphertext"), 17, MAX_PLAINTEXT_BYTES + 16)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        cipher.updateAAD(aad(authority, "source"))
        val plaintext = cipher.doFinal(encrypted)
        try {
            check(plaintext.size in 1..MAX_PLAINTEXT_BYTES)
            val decoder = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            return JSONObject(decoder.decode(ByteBuffer.wrap(plaintext)).toString())
        } finally { plaintext.fill(0) }
    }

    @Synchronized
    fun acknowledgement(authority: String): String {
        check(!destroyed)
        val iv = ByteArray(12).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        cipher.updateAAD(aad(authority, "ack"))
        val encrypted = cipher.doFinal("pending-tv-approval".toByteArray(Charsets.UTF_8))
        return JSONObject().put("v", 1).put("sid", sessionId).put("iv", encode(iv))
            .put("ciphertext", encode(encrypted)).toString()
    }

    @Synchronized
    override fun close() {
        destroyed = true
        key.fill(0)
    }

    private fun aad(authority: String, direction: String) =
        "OpenVista pairing v1\n$sessionId\n$authority\n$direction".toByteArray(Charsets.UTF_8)

    private fun decode(value: String, minimum: Int, maximum: Int): ByteArray {
        check(value.length <= (maximum * 4 + 2) / 3 && value.matches(Regex("[A-Za-z0-9_-]+")))
        val decoded = Base64.decode(value, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        check(decoded.size in minimum..maximum && encode(decoded) == value)
        return decoded
    }

    companion object {
        const val MAX_PLAINTEXT_BYTES = 6144
        const val MAX_ENVELOPE_BYTES = 8704
        private fun encode(value: ByteArray) = Base64.encodeToString(value,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
