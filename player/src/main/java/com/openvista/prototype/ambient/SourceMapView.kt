package com.openvista.prototype.ambient

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.TextView
import com.openvista.prototype.R
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.ui.TvDesign
import kotlin.math.floor

/**
 * D-pad map with aligned equirectangular markers. When no licensed tile backdrop is configured it
 * draws original coarse continents in the Settings atlas style, so saved locations stay readable
 * without any network request.
 */
class SourceMapView(context: Context, private val sources: List<LibraryItem>,
                    private val provider: MapBackdropProvider? = ProviderPolicy.map(context),
                    private val onStatus: (MapBackdropState) -> Unit = {},
                    private val choose: (LibraryItem) -> Unit) : FrameLayout(context) {
    private var repository = MapBackdropRepository(context, provider)
    private var backdrop: MapBackdrop? = null
    private var repositoryClosed = false
    private val density = resources.displayMetrics.density
    private val grid = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x1FA9D8E8; strokeWidth = .7f * density; style = Paint.Style.STROKE }
    private val landFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xC8203F45.toInt(); style = Paint.Style.FILL; pathEffect = CornerPathEffect(4f * density) }
    private val landEdge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x5586B9C9; style = Paint.Style.STROKE; strokeWidth = .8f * density; pathEffect = CornerPathEffect(4f * density)
    }
    private val path = Path()
    private var level = 0
    private var centerLat = 0.0
    private var centerLon = 0.0

    private val request = Runnable { requestBackdrop() }
    init {
        setWillNotDraw(false)
        background = TvDesign.panel(context, 0xFF0A2232.toInt(), TvDesign.glassStroke, radiusDp = 6f)
        clipToOutline = true
        isFocusable = false; contentDescription = context.getString(R.string.map_pan_help)
    }
    fun pan(horizontal: Int, vertical: Int) {
        if (level == 0) { zoom(1); return }
        val scale = (1 shl level).toDouble()
        centerLon = (centerLon + horizontal * 90 / scale).coerceIn(-180 + 180 / scale, 180 - 180 / scale)
        centerLat = (centerLat + vertical * 45 / scale).coerceIn(-90 + 90 / scale, 90 - 90 / scale)
        backdrop = null; renderMarkers(); removeCallbacks(request); postDelayed(request, 350); invalidate()
    }
    fun zoom(delta: Int) {
        level = (level + delta).coerceIn(0, 5)
        val scale = (1 shl level).toDouble()
        centerLat = centerLat.coerceIn(-90 + 90 / scale, 90 - 90 / scale)
        centerLon = centerLon.coerceIn(-180 + 180 / scale, 180 - 180 / scale)
        renderMarkers(); requestBackdrop(); invalidate()
    }
    fun reset() { level = 0; centerLat = 0.0; centerLon = 0.0; renderMarkers(); requestBackdrop(); invalidate() }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) { super.onSizeChanged(w, h, oldw, oldh); renderMarkers(); requestBackdrop() }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (repositoryClosed) { repository = MapBackdropRepository(context, provider); repositoryClosed = false }
        requestBackdrop()
    }
    override fun onDetachedFromWindow() { removeCallbacks(request); repository.close(); repositoryClosed = true; super.onDetachedFromWindow() }

    private fun x(lon: Double) = ((lon - centerLon) / 360 * (1 shl level) + .5) * width
    private fun y(lat: Double) = ((centerLat - lat) / 180 * (1 shl level) + .5) * height

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        backdrop?.let { canvas.drawBitmap(it.bitmap, null, Rect(0, 0, width, height), null); return }
        val step = (30 shr level.coerceAtMost(2)).toDouble()
        var lon = -180.0
        while (lon <= 180) { val px = x(lon).toFloat(); canvas.drawLine(px, 0f, px, height.toFloat(), grid); lon += step }
        var lat = -90.0
        while (lat <= 90) { val py = y(lat).toFloat(); canvas.drawLine(0f, py, width.toFloat(), py, grid); lat += step }
        LAND.forEach { points ->
            path.reset()
            path.moveTo(x(points[0].toDouble()).toFloat(), y(points[1].toDouble()).toFloat())
            for (i in 2 until points.size step 2) path.lineTo(x(points[i].toDouble()).toFloat(), y(points[i + 1].toDouble()).toFloat())
            path.close()
            canvas.drawPath(path, landFill)
            canvas.drawPath(path, landEdge)
        }
    }

    private fun renderMarkers() {
        if (width == 0 || height == 0) return
        val focusKey = focusedChild?.tag
        removeAllViews()
        val mapWidth = width
        val mapHeight = height
        val groups = sources.mapNotNull { item ->
            val lat = item.latitude ?: return@mapNotNull null
            val lon = item.longitude ?: return@mapNotNull null
            val px = x(lon)
            val py = y(lat)
            if (px !in 0.0..mapWidth.toDouble() || py !in 0.0..mapHeight.toDouble()) null else Triple(item, px, py)
        }.groupBy { floor(it.second / dp(CELL)).toInt() to floor(it.third / dp(CELL)).toInt() }
        groups.values.forEach { group ->
            val item = group.first().first
            val marker = Marker(context, group.size).apply {
                tag = "marker_${item.id}"
                contentDescription = if (group.size == 1) context.getString(R.string.map_source, item.title, item.latitude, item.longitude)
                    else context.getString(R.string.map_cluster, group.size)
                setOnClickListener {
                    if (group.size == 1) choose(item) else AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.map_cluster, group.size))
                        .setItems(group.map { it.first.title }.toTypedArray()) { _, which -> choose(group[which].first) }
                        .setNeutralButton(R.string.zoom_in) { _, _ ->
                            centerLat = group.map { it.first.latitude!! }.average()
                            centerLon = group.map { it.first.longitude!! }.average()
                            zoom(1)
                        }.setNegativeButton(R.string.cancel, null).show()
                }
            }
            val size = dp(MARKER)
            // Centered on the group's mean position, clamped so the whole marker stays on the map.
            addView(marker, LayoutParams(size, size, Gravity.TOP or Gravity.START).apply {
                leftMargin = (group.map { it.second }.average().toInt() - size / 2).coerceIn(0, (mapWidth - size).coerceAtLeast(0))
                topMargin = (group.map { it.third }.average().toInt() - size / 2).coerceIn(0, (mapHeight - size).coerceAtLeast(0))
            })
        }
        backdrop?.attribution?.let { credit -> addView(TextView(context).apply {
            text = credit; textSize = 8f; setTextColor(TvDesign.muted); setBackgroundColor(0xCC061421.toInt())
            setPadding(dp(6), dp(3), dp(6), dp(3)); isFocusable = false
        }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)) }
        if (focusKey != null) findViewWithTag<View>(focusKey)?.requestFocus()
    }
    private fun requestBackdrop() {
        if (repositoryClosed || width <= 0 || height <= 0) return
        val scale = (1 shl level).toDouble()
        backdrop = null
        repository.load(MapViewport(centerLat, centerLon, 360 / scale, 180 / scale), width, height) { state ->
            backdrop = (state as? MapBackdropState.Ready)?.backdrop
            onStatus(state); renderMarkers(); invalidate()
        }
    }
    private fun dp(n: Int) = (n * density + .5f).toInt()

    /** A cyan location dot; clusters show their count. Focus adds the shared bright ring. */
    private class Marker(context: Context, private val count: Int) : View(context) {
        private val density = resources.displayMetrics.density
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = TvDesign.semibold; textSize = 8.5f * resources.displayMetrics.scaledDensity; textAlign = Paint.Align.CENTER
        }

        init { isFocusable = true; isClickable = true }

        override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
            super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
            invalidate()
        }

        override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(info)
            info.className = "android.widget.Button"
        }

        override fun onDraw(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f
            val radius = if (count == 1) 5f * density else 9.5f * density
            if (isFocused) {
                paint.style = Paint.Style.FILL
                paint.color = 0x447FDDE9
                canvas.drawCircle(cx, cy, radius + 6f * density, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f * density
                paint.color = TvDesign.accent
                canvas.drawCircle(cx, cy, radius + 6f * density, paint)
            }
            paint.style = Paint.Style.FILL
            paint.color = if (isFocused) TvDesign.accent else if (count == 1) TvDesign.accentStrong else 0xE6195060.toInt()
            canvas.drawCircle(cx, cy, radius, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f * density
            paint.color = 0xCCF3F5F7.toInt()
            canvas.drawCircle(cx, cy, radius, paint)
            if (count > 1) {
                label.color = if (isFocused) TvDesign.ink else TvDesign.text
                canvas.drawText(count.toString(), cx, cy - (label.descent() + label.ascent()) / 2, label)
            }
        }
    }

    private companion object {
        const val MARKER = 34
        const val CELL = 30

        /** Original coarse continent outlines as longitude/latitude pairs; decorative, not survey data. */
        val LAND: List<FloatArray> = listOf(
            // North America
            floatArrayOf(-166f,68f, -156f,71f, -140f,70f, -125f,70f, -110f,68f, -95f,72f, -85f,70f, -78f,64f, -64f,60f,
                -56f,52f, -66f,45f, -70f,42f, -76f,36f, -81f,30f, -80f,25f, -83f,29f, -90f,30f, -97f,27f, -97f,21f,
                -92f,18f, -88f,16f, -84f,11f, -79f,8f, -82f,8f, -86f,12f, -92f,15f, -98f,16f, -105f,20f, -110f,24f,
                -112f,29f, -115f,30f, -117f,33f, -121f,35f, -124f,40f, -124f,46f, -127f,50f, -133f,55f, -140f,59f,
                -150f,60f, -158f,57f, -165f,55f, -160f,59f, -165f,62f),
            // Greenland, Iceland
            floatArrayOf(-55f,60f, -44f,60f, -38f,66f, -22f,70f, -20f,78f, -40f,83f, -60f,80f, -68f,76f, -58f,68f),
            floatArrayOf(-24f,65f, -14f,66f, -14f,64f, -22f,63f),
            // South America
            floatArrayOf(-78f,8f, -72f,12f, -62f,10f, -52f,5f, -44f,-2f, -35f,-6f, -38f,-13f, -40f,-22f, -48f,-26f,
                -53f,-34f, -58f,-38f, -63f,-42f, -66f,-48f, -69f,-52f, -74f,-53f, -72f,-45f, -73f,-37f, -71f,-30f,
                -70f,-18f, -76f,-14f, -81f,-6f, -80f,0f, -77f,4f),
            // Eurasia
            floatArrayOf(-10f,36f, -9f,43f, -2f,44f, -5f,48f, 2f,51f, 5f,53f, 8f,57f, 5f,62f, 12f,66f, 18f,70f,
                28f,71f, 40f,67f, 60f,69f, 70f,73f, 80f,73f, 100f,77f, 115f,73f, 130f,71f, 140f,72f, 160f,70f, 170f,70f,
                180f,67f, 180f,65f, 170f,60f, 162f,57f, 156f,51f, 143f,59f, 137f,54f, 141f,48f, 135f,43f, 129f,40f,
                127f,35f, 122f,40f, 121f,37f, 120f,32f, 122f,30f, 117f,24f, 110f,21f, 108f,16f, 109f,12f, 105f,9f,
                104f,11f, 100f,13f, 100f,7f, 103f,2f, 101f,3f, 98f,8f, 98f,16f, 94f,17f, 92f,22f, 88f,22f, 80f,15f,
                78f,8f, 76f,10f, 73f,17f, 72f,21f, 67f,25f, 61f,25f, 57f,26f, 52f,28f, 48f,30f, 36f,36f, 26f,40f,
                28f,41f, 22f,37f, 20f,40f, 16f,38f, 18f,41f, 13f,44f, 12f,40f, 8f,44f, 3f,43f, 0f,39f, -6f,36f),
            // Great Britain, Ireland, Japan
            floatArrayOf(-5f,50f, 1f,51f, 0f,53f, -2f,56f, -5f,58f, -6f,56f, -4f,54f),
            floatArrayOf(-10f,52f, -6f,52f, -6f,55f, -9f,54f),
            floatArrayOf(130f,31f, 135f,34f, 140f,36f, 142f,40f, 141f,45f, 139f,40f, 136f,36f, 132f,34f),
            // Africa, Arabia, Madagascar
            floatArrayOf(-17f,21f, -16f,15f, -17f,12f, -12f,7f, -8f,4f, -2f,5f, 4f,6f, 9f,4f, 10f,-2f, 12f,-6f,
                13f,-12f, 12f,-17f, 15f,-27f, 18f,-34f, 22f,-34f, 27f,-33f, 32f,-28f, 35f,-23f, 36f,-18f, 40f,-15f,
                40f,-10f, 39f,-5f, 42f,0f, 46f,3f, 51f,11f, 44f,11f, 43f,12f, 39f,16f, 37f,20f, 35f,24f, 33f,30f,
                25f,32f, 20f,31f, 11f,33f, 10f,37f, 3f,37f, -2f,35f, -6f,36f, -10f,30f, -13f,27f),
            floatArrayOf(35f,30f, 39f,22f, 43f,13f, 45f,13f, 52f,16f, 58f,20f, 60f,23f, 60f,25f, 56f,24f, 51f,24f, 48f,29f),
            floatArrayOf(44f,-12f, 50f,-15f, 49f,-22f, 45f,-25f, 43f,-20f),
            // Southeast Asia and Oceania
            floatArrayOf(95f,5f, 98f,4f, 104f,-2f, 106f,-6f, 102f,-4f, 96f,2f),
            floatArrayOf(109f,2f, 111f,-3f, 116f,-4f, 119f,1f, 117f,7f, 113f,4f),
            floatArrayOf(105f,-6f, 114f,-8f, 108f,-8f),
            floatArrayOf(120f,18f, 122f,14f, 126f,7f, 124f,10f, 121f,14f),
            floatArrayOf(131f,-1f, 141f,-3f, 150f,-10f, 142f,-9f, 137f,-5f),
            floatArrayOf(114f,-22f, 114f,-34f, 118f,-35f, 124f,-34f, 131f,-31f, 138f,-35f, 141f,-38f, 147f,-38f,
                150f,-37f, 153f,-32f, 153f,-25f, 146f,-19f, 142f,-11f, 140f,-17f, 136f,-12f, 131f,-11f, 126f,-14f, 122f,-18f),
            floatArrayOf(166f,-46f, 172f,-41f, 174f,-37f, 178f,-38f, 175f,-42f, 171f,-44f),
            // Antarctica
            floatArrayOf(-180f,-72f, -120f,-74f, -60f,-64f, 0f,-70f, 90f,-66f, 150f,-68f, 180f,-70f, 180f,-90f, -180f,-90f),
        )
    }
}
