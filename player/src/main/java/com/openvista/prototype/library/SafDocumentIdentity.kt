package com.openvista.prototype.library

import android.net.Uri
import android.provider.DocumentsContract
import java.security.MessageDigest

/** Document IDs are opaque and meaningful only within the same provider authority. */
internal object SafDocumentIdentity {
    fun key(raw: String): String {
        val uri = Uri.parse(raw)
        val authority = uri.authority ?: return "uri:$raw"
        val segments = uri.pathSegments
        val documentShape = (segments.size == 2 && segments[0] == "document") ||
            (segments.size == 4 && segments[0] == "tree" && segments[2] == "document")
        if (uri.scheme != "content" || !documentShape || uri.fragment != null) return "uri:$raw"
        val id = try { DocumentsContract.getDocumentId(uri) } catch (_: RuntimeException) { return "uri:$raw" }
        // Length prefixes avoid ambiguity without interpreting provider-specific IDs as paths.
        return "document:${authority.length}:$authority:${id.length}:$id"
    }

    fun itemId(raw: String): String = "video-" + MessageDigest.getInstance("SHA-256")
        .digest(key(raw).toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
