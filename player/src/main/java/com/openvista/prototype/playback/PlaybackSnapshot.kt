package com.openvista.prototype.playback

import org.json.JSONArray
import org.json.JSONObject

/** Source identifiers only; URIs and media credentials never enter saved navigation state. */
data class PlaybackSnapshot(
    val itemIds: List<String>,
    val cameraIds: List<String>,
    val selectedId: String?,
    val fallback: Boolean,
    val failedIds: Set<String>,
    val recoveryAttempts: Int,
    val recoveryDelayMs: Long,
    val stayOnVideos: Boolean,
    val recoveryCursor: Int = 0,
    val playWhenReady: Boolean = true,
) {
    fun toJson(): String {
        val items = itemIds.filter(::validId).distinct().take(MAX_IDS)
        val itemSet = items.toSet()
        val cameras = cameraIds.filter(::validId).distinct().take(MAX_IDS)
        val failures = failedIds.filter { it in itemSet }.take(MAX_IDS)
        return JSONObject().put("version", VERSION)
            .put("items", JSONArray(items)).put("cameras", JSONArray(cameras))
            .put("selected", selectedId?.takeIf { it in itemSet } ?: JSONObject.NULL)
            .put("fallback", fallback).put("failed", JSONArray(failures))
            .put("attempts", recoveryAttempts.coerceIn(0, MAX_ATTEMPTS))
            .put("delay", recoveryDelayMs.coerceIn(0, MAX_DELAY_MS))
            .put("stayLocal", stayOnVideos).put("cursor", recoveryCursor.coerceIn(0, MAX_IDS))
            .put("playWhenReady", playWhenReady)
            .toString()
    }

    companion object {
        private const val VERSION = 1
        private const val MAX_IDS = 2_000
        private const val MAX_ID_LENGTH = 128
        private const val MAX_JSON_CHARS = 1_000_000
        private const val MAX_ATTEMPTS = 6
        private const val MAX_DELAY_MS = 300_000L

        fun fromJson(raw: String): PlaybackSnapshot? {
            if (raw.length > MAX_JSON_CHARS) return null
            return try {
                val json = JSONObject(raw)
                require(json.getInt("version") == VERSION)
                val items = ids(json.getJSONArray("items"))
                val cameras = ids(json.getJSONArray("cameras"))
                val failed = ids(json.getJSONArray("failed")).toSet()
                val selected = if (json.isNull("selected")) null else json.get("selected").let {
                    require(it is String && validId(it) && it in items)
                    it
                }
                val attempts = json.getInt("attempts").also { require(it in 0..MAX_ATTEMPTS) }
                val delay = json.getLong("delay").also { require(it in 0..MAX_DELAY_MS) }
                val cursor = json.getInt("cursor").also { require(it in 0..MAX_IDS) }
                val fallback = json.get("fallback").also { require(it is Boolean) } as Boolean
                val stay = json.get("stayLocal").also { require(it is Boolean) } as Boolean
                require(failed.all { it in items })
                val playing = if (json.has("playWhenReady")) json.get("playWhenReady").also { require(it is Boolean) } as Boolean else true
                PlaybackSnapshot(items, cameras, selected, fallback, failed, attempts, delay, stay, cursor, playing)
            } catch (_: Exception) { null }
        }

        private fun ids(array: JSONArray): List<String> {
            require(array.length() <= MAX_IDS)
            return List(array.length()) { index ->
                val value = array.get(index)
                require(value is String && validId(value))
                value
            }.also { require(it.distinct().size == it.size) }
        }

        private fun validId(id: String): Boolean =
            id.isNotBlank() && id.length <= MAX_ID_LENGTH && id.none(Char::isISOControl)
    }
}
