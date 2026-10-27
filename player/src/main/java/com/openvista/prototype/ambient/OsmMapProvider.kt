package com.openvista.prototype.ambient

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.AtomicFile
import java.io.File
import java.io.IOException
import java.net.URL
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

/**
 * Render only the viewport a person opens, reprojecting OSM's Mercator tiles to the
 * coordinate view. No region downloads, adjacent-tile prefetch or background scans.
 * A conservative seven-day tile TTL follows OSM's documented fallback cache policy.
 */
class OsmMapProvider(context: Context) : MapBackdropProvider {
    private val directory = File(context.applicationContext.filesDir, "osm-tiles-v1")
    override val id = "osm-standard-v1"
    override val attribution = "© OpenStreetMap contributors · https://www.openstreetmap.org/copyright"
    override val minimumRefreshMs = 0L
    override val maximumCacheAgeMs = 7 * 24 * 60 * 60_000L
    override val permitsDiskCache = true

    override fun render(viewport: MapViewport, width: Int, height: Int): MapBackdrop = synchronized(TILE_LOCK) {
        require(width in 1..MapBackdropRepository.MAX_WIDTH && height in 1..MapBackdropRepository.MAX_HEIGHT)
        require(viewport.centerLatitude.isFinite() && viewport.centerLongitude.isFinite() &&
            viewport.centerLatitude in -90.0..90.0 && viewport.centerLongitude in -180.0..180.0 &&
            viewport.longitudeSpan > 0 && viewport.longitudeSpan <= 360 &&
            viewport.latitudeSpan > 0 && viewport.latitudeSpan <= 180)
        ProviderHttp.interrupted()
        val west = max(-180.0, viewport.centerLongitude - viewport.longitudeSpan / 2)
        val east = min(180.0, viewport.centerLongitude + viewport.longitudeSpan / 2)
        val north = min(MAX_LATITUDE, viewport.centerLatitude + viewport.latitudeSpan / 2)
        val south = max(-MAX_LATITUDE, viewport.centerLatitude - viewport.latitudeSpan / 2)
        require(west < east && south < north) { "No map coverage in this viewport." }
        var zoom = floor(ln(width * 360.0 / (256 * viewport.longitudeSpan)) / ln(2.0)).toInt().coerceIn(0, 8)
        fun bounds(level: Int): IntArray {
            val side = 1 shl level
            return intArrayOf(tileX(west, side), tileX(east, side), tileY(north, side), tileY(south, side))
        }
        var area = bounds(zoom)
        while ((area[1] - area[0] + 1) * (area[3] - area[2] + 1) > MAX_VISIBLE_TILES && zoom > 0) { zoom--; area = bounds(zoom) }
        require((area[1] - area[0] + 1) * (area[3] - area[2] + 1) <= MAX_VISIBLE_TILES)
        val side = 1 shl zoom
        val tiles = hashMapOf<Pair<Int, Int>, IntArray>()
        var oldest = System.currentTimeMillis()
        for (y in area[2]..area[3]) for (x in area[0]..area[1]) {
            ProviderHttp.interrupted()
            val tile = tile(zoom, x, y)
            oldest = min(oldest, tile.second)
            tiles[x to y] = tile.first
        }
        val pixels = IntArray(width * height) { Color.rgb(14, 36, 54) }
        val xPixels = IntArray(width) { x ->
            val longitude = viewport.centerLongitude + ((x + .5) / width - .5) * viewport.longitudeSpan
            floor((longitude + 180) / 360 * side * 256).toInt().coerceIn(0, side * 256 - 1)
        }
        for (y in 0 until height) {
            ProviderHttp.interrupted()
            val latitude = viewport.centerLatitude + (.5 - (y + .5) / height) * viewport.latitudeSpan
            if (latitude !in -MAX_LATITUDE..MAX_LATITUDE) continue
            val sourceY = floor(mercatorY(latitude) * side * 256).toInt().coerceIn(0, side * 256 - 1)
            for (x in 0 until width) {
                val sourceX = xPixels[x]
                val tile = tiles[sourceX / 256 to sourceY / 256] ?: continue
                pixels[y * width + x] = tile[(sourceY % 256) * 256 + sourceX % 256]
            }
        }
        MapBackdrop(Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888), attribution, id, oldest)
    }

    private fun tile(zoom: Int, x: Int, y: Int): Pair<IntArray, Long> {
        require(directory.isDirectory || directory.mkdirs()) { "Map cache unavailable." }
        val file = AtomicFile(File(directory, "$zoom-$x-$y.png"))
        val now = System.currentTimeMillis()
        val cached = try {
            val age = now - file.baseFile.lastModified()
            if (age !in 0 until maximumCacheAgeMs || file.baseFile.length() !in 1..MAX_TILE_BYTES.toLong()) null
            else file.openRead().use { it.readBytes() }.let { decode(it) to file.baseFile.lastModified() }
        } catch (_: Exception) { null }
        if (cached != null) return cached
        // Retain fresh cache entries rather than cycling through uncached views at the server's expense.
        directory.listFiles()?.filter { now - it.lastModified() >= maximumCacheAgeMs }?.forEach { it.delete() }
        val files = directory.listFiles().orEmpty()
        require(files.size < MAX_CACHED_TILES && files.sumOf { it.length() } <= MAX_CACHE_BYTES - MAX_TILE_BYTES) {
            "Map cache capacity reached. Saved-source list remains available."
        }
        val bytes = ProviderHttp.bytes(URL("https://tile.openstreetmap.org/$zoom/$x/$y.png"),
            "tile.openstreetmap.org", MAX_TILE_BYTES, "image/png")
        val pixels = decode(bytes)
        ProviderHttp.interrupted()
        val output = file.startWrite()
        try { output.write(bytes); file.finishWrite(output) } catch (error: Exception) { file.failWrite(output); throw error }
        return pixels to System.currentTimeMillis()
    }

    private fun decode(bytes: ByteArray): IntArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth == 256 && bounds.outHeight == 256)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: throw IOException("Map tile unavailable.")
        return try { IntArray(256 * 256).also { bitmap.getPixels(it, 0, 256, 0, 0, 256, 256) } } finally { bitmap.recycle() }
    }

    private fun tileX(longitude: Double, side: Int) = floor((longitude + 180) / 360 * side).toInt().coerceIn(0, side - 1)
    private fun tileY(latitude: Double, side: Int) = floor(mercatorY(latitude) * side).toInt().coerceIn(0, side - 1)
    private fun mercatorY(latitude: Double): Double = (1 - ln(tan(PI / 4 + latitude * PI / 360)) / PI) / 2

    companion object {
        private val TILE_LOCK = Any()
        private const val MAX_LATITUDE = 85.05112878
        private const val MAX_VISIBLE_TILES = 24
        private const val MAX_CACHED_TILES = 256
        private const val MAX_TILE_BYTES = 262_144
        private const val MAX_CACHE_BYTES = 32 * 1024 * 1024L
    }
}
