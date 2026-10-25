package com.openvista.diagnostics

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object ReportPolicy {
    const val SCHEMA_VERSION = 1
    const val MAX_LABEL_LENGTH = 80
    const val EVIDENCE_NOTE = "Advertised capabilities only; successful playback and USB access are not verified."

    fun safeLabel(value: String?): String {
        if (value.isNullOrBlank()) return "unavailable"
        val clean = value.take(MAX_LABEL_LENGTH).map { char ->
            if (char.isLetterOrDigit() || char in " ._+:-") char else '_'
        }.joinToString("").trim()
        return clean.ifBlank { "unavailable" }
    }

    fun utcNow(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date())
}
