package com.openvista.prototype.ambient

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.openvista.prototype.R
import com.openvista.prototype.library.LibraryItem
import kotlin.math.floor

/** D-pad map with aligned equirectangular markers and an offline coordinate/list fallback. */
class SourceMapView(context: Context, private val sources: List<LibraryItem>,
                    private val provider: MapBackdropProvider? = ProviderPolicy.map(context),
                    private val onStatus: (MapBackdropState) -> Unit = {},
                    private val choose: (LibraryItem) -> Unit) : FrameLayout(context) {
    private var repository = MapBackdropRepository(context, provider)
    private var backdrop: MapBackdrop? = null
    private var repositoryClosed = false
    private val grid = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF32516A.toInt(); strokeWidth = 1f }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFB5C8D7.toInt(); textSize = 12 * resources.displayMetrics.scaledDensity }
    private var level = 0
    private var centerLat = 0.0
    private var centerLon = 0.0

    private val request = Runnable { requestBackdrop() }
    init {
        setWillNotDraw(false); setBackgroundColor(0xFF0E2436.toInt()); clipChildren = true
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
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        backdrop?.let { canvas.drawBitmap(it.bitmap, null, Rect(0, 0, width, height), null); return }
        for (i in 1..5) canvas.drawLine(width * i / 6f, 0f, width * i / 6f, height.toFloat(), grid)
        for (i in 1..3) canvas.drawLine(0f, height * i / 4f, width.toFloat(), height * i / 4f, grid)
        val scale = (1 shl level).toDouble()
        canvas.drawText("%.1f°".format(centerLat + 90 / scale), 8f, label.textSize + 4, label)
        canvas.drawText("%.1f°".format(centerLat - 90 / scale), 8f, height - 8f, label)
    }

    private fun renderMarkers() {
        if (width == 0 || height == 0) return
        val focusKey = focusedChild?.tag
        removeAllViews()
        val scale = (1 shl level).toDouble()
        val groups = sources.mapNotNull { item ->
            val lat = item.latitude ?: return@mapNotNull null
            val lon = item.longitude ?: return@mapNotNull null
            val x = ((lon - centerLon) / 360 * scale + .5) * width
            val y = ((centerLat - lat) / 180 * scale + .5) * height
            if (x !in 0.0..width.toDouble() || y !in 0.0..height.toDouble()) null else Triple(item, x, y)
        }.groupBy { floor(it.second / dp(64)).toInt() to floor(it.third / dp(56)).toInt() }
        groups.values.forEach { group ->
            val item = group.first().first
            val button = Button(context).apply {
                tag = "marker_${item.id}"
                text = if (group.size == 1) "●" else group.size.toString()
                textSize = 16f
                isAllCaps = false
                setTextColor(Color.WHITE)
                backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF245D7E.toInt())
                contentDescription = if (group.size == 1) context.getString(R.string.map_source, item.title, item.latitude, item.longitude)
                    else context.getString(R.string.map_cluster, group.size)
                setOnFocusChangeListener { _, focused ->
                    backgroundTintList = android.content.res.ColorStateList.valueOf(if (focused) 0xFF8BDEF2.toInt() else 0xFF245D7E.toInt())
                    setTextColor(if (focused) Color.BLACK else Color.WHITE)
                }
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
            addView(button, LayoutParams(dp(60), dp(52), Gravity.TOP or Gravity.START).apply {
                leftMargin = (group.map { it.second }.average().toInt() - dp(30)).coerceIn(0, (width - dp(60)).coerceAtLeast(0))
                topMargin = (group.map { it.third }.average().toInt() - dp(26)).coerceIn(0, (height - dp(52)).coerceAtLeast(0))
            })
        }
        backdrop?.attribution?.let { credit -> addView(TextView(context).apply {
            text = credit; textSize = 12f; setTextColor(Color.WHITE); setBackgroundColor(0xCC000000.toInt())
            setPadding(dp(6), dp(3), dp(6), dp(3)); isFocusable = false
        }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)) }
        if (focusKey != null) findViewWithTag<android.view.View>(focusKey)?.requestFocus()
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
    private fun dp(n: Int) = (n * resources.displayMetrics.density + .5f).toInt()
}
