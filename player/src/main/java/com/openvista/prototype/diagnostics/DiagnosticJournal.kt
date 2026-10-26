package com.openvista.prototype.diagnostics

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** A fixed vocabulary prevents a URL, title, location or platform exception from entering a report. */
object DiagnosticJournal {
    enum class Event { PLAYBACK_ERROR, LIBRARY_STORAGE_ERROR, UPDATE_ERROR }
    data class Entry(val at: Long, val event: Event, val code: Int?)
    private const val RETENTION_MS = 7 * 86_400_000L
    private const val MAX_ENTRIES = 64

    @Synchronized
    fun record(context: Context, event: Event, code: Int? = null) {
        val entries = (entries(context) + Entry(System.currentTimeMillis(), event, code)).takeLast(MAX_ENTRIES)
        val json = JSONArray().apply { entries.forEach { entry -> put(JSONObject().put("at", entry.at)
            .put("event", entry.event.name).put("code", entry.code ?: JSONObject.NULL)) } }
        context.applicationContext.getSharedPreferences("diagnostic-journal", Context.MODE_PRIVATE)
            .edit().putString("entries", json.toString()).apply()
    }

    @Synchronized
    fun entries(context: Context): List<Entry> {
        val raw = context.applicationContext.getSharedPreferences("diagnostic-journal", Context.MODE_PRIVATE)
            .getString("entries", null) ?: return emptyList()
        if (raw.length > 16_384) return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length().coerceAtMost(MAX_ENTRIES)).mapNotNull { index ->
                val value = array.getJSONObject(index)
                val at = value.getLong("at")
                if (System.currentTimeMillis() - at !in 0..RETENTION_MS) null else Entry(at,
                    Event.valueOf(value.getString("event")), if (value.isNull("code")) null else value.getInt("code"))
            }
        } catch (_: Exception) { emptyList() }
    }

    fun report(context: Context): String = entries(context).joinToString("\n") { entry ->
        "${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(entry.at))} · ${entry.event.name} · ${entry.code ?: "—"}"
    }
}
