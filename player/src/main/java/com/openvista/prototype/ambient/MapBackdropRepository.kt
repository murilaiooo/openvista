package com.openvista.prototype.ambient

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AtomicFile
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.Future

data class MapViewport(
    val centerLatitude: Double,
    val centerLongitude: Double,
    val longitudeSpan: Double,
    val latitudeSpan: Double,
)

data class MapBackdrop(
    val bitmap: Bitmap,
    val attribution: String,
    val providerId: String,
    val fetchedAt: Long,
)

/** Install an adapter only after its terms, attribution, budget and privacy review. */
interface MapBackdropProvider {
    val id: String
    val attribution: String
    val minimumRefreshMs: Long
    val maximumCacheAgeMs: Long
    val permitsDiskCache: Boolean
    fun render(viewport: MapViewport, width: Int, height: Int): MapBackdrop
}

sealed interface MapBackdropState {
    data object ProviderRequired : MapBackdropState
    data object Loading : MapBackdropState
    data class Ready(val backdrop: MapBackdrop, val stale: Boolean) : MapBackdropState
    data object Unavailable : MapBackdropState
}

/**
 * One explicitly requested viewport at a time, with no default endpoint or prefetch.
 * Metadata and PNG bytes share one AtomicFile transaction, including a content digest.
 * A provider's maximum age bounds both memory and disk fallback. Returned bitmaps belong
 * to the consumer; this repository never recycles a bitmap that a view may still draw.
 */
class MapBackdropRepository(context: Context, private val provider: MapBackdropProvider? = null) {
    private data class Request(val viewport: MapViewport, val width: Int, val height: Int, val providerId: String)
    private data class Cached(val request: Request, val backdrop: MapBackdrop)

    private val cache = AtomicFile(File(context.applicationContext.cacheDir, "map-backdrop-v1.cache"))
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor { task -> Thread(task, "OpenVista-map-backdrop") }
    private val lock = Any()
    private var task: Future<*>? = null
    private var generation = 0L
    private var closed = false
    // These fields are owned by the worker, including failed-attempt throttling.
    private var memory: Cached? = null
    private var lastAttemptAt: Long? = null

    fun load(viewport: MapViewport, width: Int, height: Int, callback: (MapBackdropState) -> Unit) {
        synchronized(lock) {
            if (closed) return
            cancel()
            val token = generation
            val adapter = provider
            if (adapter == null) {
                deliver(token, callback, MapBackdropState.ProviderRequired)
                return
            }
            if (!valid(viewport) || width <= 0 || height <= 0 || adapter.id.isBlank() ||
                adapter.id.length > 128 || adapter.id.any { it.isISOControl() }) {
                deliver(token, callback, MapBackdropState.Unavailable)
                return
            }
            val request = Request(viewport, width.coerceAtMost(MAX_WIDTH), height.coerceAtMost(MAX_HEIGHT), adapter.id)
            deliver(token, callback, MapBackdropState.Loading)
            task = worker.submit {
                if (!current(token)) return@submit
                if (!adapter.permitsDiskCache) {
                    // An adapter that no longer permits disk caching cannot reuse old disk data.
                    try { synchronized(CACHE_LOCK) { cache.delete() } } catch (_: RuntimeException) { }
                }
                val now = System.currentTimeMillis()
                val maxAge = adapter.maximumCacheAgeMs.coerceIn(0, MAX_CACHE_AGE_MS)
                val saved = memory?.takeIf { it.request == request && usable(it.backdrop, now, maxAge) }
                    ?: if (adapter.permitsDiskCache) readCache(request, adapter, now, maxAge) else null
                if (!current(token)) return@submit
                if (saved != null) memory = saved
                val minimumRefresh = adapter.minimumRefreshMs.coerceAtLeast(0)
                val fresh = saved != null && now - saved.backdrop.fetchedAt < minimumRefresh
                if (fresh) {
                    deliver(token, callback, MapBackdropState.Ready(saved!!.backdrop, false))
                    return@submit
                }
                val elapsed = SystemClock.elapsedRealtime()
                val lastAttempt = lastAttemptAt
                if (lastAttempt != null && elapsed - lastAttempt < minimumRefresh) {
                    deliver(token, callback, saved?.let { MapBackdropState.Ready(it.backdrop, true) } ?: MapBackdropState.Unavailable)
                    return@submit
                }
                lastAttemptAt = elapsed
                val result = try {
                    val received = adapter.render(request.viewport, request.width, request.height)
                    if (!current(token)) return@submit
                    require(received.providerId == adapter.id && validBitmap(received.bitmap, request))
                    val receivedAt = System.currentTimeMillis()
                    require(received.fetchedAt in 0..receivedAt && receivedAt - received.fetchedAt <= maxAge.coerceAtLeast(1_000))
                    val owned = received.bitmap.copy(Bitmap.Config.ARGB_8888, false)
                        ?: throw IOException("The map image could not be copied.")
                    val backdrop = MapBackdrop(owned, attribution(adapter.attribution), adapter.id, received.fetchedAt)
                    val stored = Cached(request, backdrop)
                    memory = stored
                    if (adapter.permitsDiskCache && maxAge > 0 && current(token)) {
                        try { writeCache(stored) } catch (_: Exception) {
                            // A failed cache write does not discard an otherwise usable render.
                        }
                    }
                    MapBackdropState.Ready(backdrop, false)
                } catch (_: Exception) {
                    if (!current(token)) return@submit
                    saved?.takeIf { usable(it.backdrop, System.currentTimeMillis(), maxAge) }
                        ?.let { MapBackdropState.Ready(it.backdrop, true) } ?: MapBackdropState.Unavailable
                }
                deliver(token, callback, result)
            }
        }
    }

    fun cancel() {
        synchronized(lock) {
            generation++
            task?.cancel(true)
            task = null
        }
    }

    fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
            cancel()
            worker.shutdownNow()
        }
    }

    private fun current(token: Long): Boolean = synchronized(lock) { !closed && generation == token }
    private fun deliver(token: Long, callback: (MapBackdropState) -> Unit, state: MapBackdropState) {
        main.post { if (current(token)) callback(state) }
    }

    private fun valid(viewport: MapViewport): Boolean = viewport.centerLatitude.isFinite() &&
        viewport.centerLongitude.isFinite() && viewport.longitudeSpan.isFinite() && viewport.latitudeSpan.isFinite() &&
        viewport.centerLatitude in -90.0..90.0 && viewport.centerLongitude in -180.0..180.0 &&
        viewport.longitudeSpan > 0 && viewport.longitudeSpan <= 360 && viewport.latitudeSpan > 0 && viewport.latitudeSpan <= 180

    private fun validBitmap(bitmap: Bitmap, request: Request): Boolean = !bitmap.isRecycled &&
        bitmap.width == request.width && bitmap.height == request.height &&
        bitmap.width in 1..MAX_WIDTH && bitmap.height in 1..MAX_HEIGHT

    private fun usable(backdrop: MapBackdrop, now: Long, maxAge: Long): Boolean = !backdrop.bitmap.isRecycled &&
        backdrop.fetchedAt >= 0 && now >= backdrop.fetchedAt && now - backdrop.fetchedAt <= maxAge

    private fun attribution(raw: String): String {
        require(raw.length <= 2_048)
        return raw.filterNot { it.isISOControl() }.trim()
    }

    private fun readCache(request: Request, adapter: MapBackdropProvider, now: Long, maxAge: Long): Cached? = try {
        synchronized(CACHE_LOCK) {
            cache.openRead().use { input ->
                val all = ByteArrayOutputStream()
                val buffer = ByteArray(8_192)
                while (all.size() <= MAX_FILE_BYTES) {
                    val count = input.read(buffer, 0, minOf(buffer.size, MAX_FILE_BYTES + 1 - all.size()))
                    if (count < 0) break
                    if (count == 0) throw IOException("Incomplete map cache read.")
                    all.write(buffer, 0, count)
                }
                require(all.size() <= MAX_FILE_BYTES)
                val data = DataInputStream(ByteArrayInputStream(all.toByteArray()))
                require(data.readInt() == CACHE_MAGIC)
                val metadataSize = data.readInt()
                require(metadataSize in 1..MAX_METADATA_BYTES && metadataSize <= data.available())
                val metadata = ByteArray(metadataSize).also(data::readFully)
                val json = JSONObject(metadata.toString(Charsets.UTF_8))
                require(json.getInt("schema") == 1 && json.getString("provider") == request.providerId)
                require(json.getInt("width") == request.width && json.getInt("height") == request.height)
                require(json.getDouble("latitude") == request.viewport.centerLatitude &&
                    json.getDouble("longitude") == request.viewport.centerLongitude &&
                    json.getDouble("longitudeSpan") == request.viewport.longitudeSpan &&
                    json.getDouble("latitudeSpan") == request.viewport.latitudeSpan)
                val fetchedAt = json.getLong("fetchedAt")
                require(fetchedAt >= 0 && now >= fetchedAt && now - fetchedAt <= maxAge)
                val length = data.readInt()
                require(length in 1..MAX_IMAGE_BYTES && length == data.available())
                val png = ByteArray(length).also(data::readFully)
                require(digest(png) == json.getString("sha256"))
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(png, 0, png.size, bounds)
                require(bounds.outWidth == request.width && bounds.outHeight == request.height)
                val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                val bitmap = BitmapFactory.decodeByteArray(png, 0, png.size, options)
                    ?: throw IOException("The cached map image could not be decoded.")
                require(validBitmap(bitmap, request))
                Cached(request, MapBackdrop(bitmap, attribution(adapter.attribution), adapter.id, fetchedAt))
            }
        }
    } catch (_: Exception) {
        null
    }

    private fun writeCache(value: Cached) = synchronized(CACHE_LOCK) {
        val png = ByteArrayOutputStream()
        val bounded = object : OutputStream() {
            override fun write(byte: Int) {
                if (png.size() >= MAX_IMAGE_BYTES) throw IOException("Map cache image exceeds its limit.")
                png.write(byte)
            }
            override fun write(bytes: ByteArray, offset: Int, length: Int) {
                if (length > MAX_IMAGE_BYTES - png.size()) throw IOException("Map cache image exceeds its limit.")
                png.write(bytes, offset, length)
            }
        }
        require(value.backdrop.bitmap.compress(Bitmap.CompressFormat.PNG, 100, bounded))
        val bytes = png.toByteArray()
        val request = value.request
        val metadata = JSONObject().put("schema", 1).put("provider", request.providerId)
            .put("attribution", value.backdrop.attribution).put("fetchedAt", value.backdrop.fetchedAt)
            .put("width", request.width).put("height", request.height)
            .put("latitude", request.viewport.centerLatitude).put("longitude", request.viewport.centerLongitude)
            .put("longitudeSpan", request.viewport.longitudeSpan).put("latitudeSpan", request.viewport.latitudeSpan)
            .put("sha256", digest(bytes)).toString().toByteArray(Charsets.UTF_8)
        require(metadata.size <= MAX_METADATA_BYTES)
        val output = cache.startWrite()
        try {
            val data = DataOutputStream(output)
            data.writeInt(CACHE_MAGIC)
            data.writeInt(metadata.size)
            data.write(metadata)
            data.writeInt(bytes.size)
            data.write(bytes)
            data.flush()
            cache.finishWrite(output)
        } catch (failure: Exception) {
            cache.failWrite(output)
            throw failure
        }
    }

    private fun digest(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes)
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    companion object {
        private val CACHE_LOCK = Any()
        const val MAX_WIDTH = 1_024
        const val MAX_HEIGHT = 768
        private const val MAX_IMAGE_BYTES = 4 * 1_024 * 1_024
        private const val MAX_METADATA_BYTES = 16_384
        private const val MAX_FILE_BYTES = MAX_IMAGE_BYTES + MAX_METADATA_BYTES + 12
        private const val MAX_CACHE_AGE_MS = 7 * 24 * 60 * 60 * 1_000L
        private const val CACHE_MAGIC = 0x4F564D31
    }
}
