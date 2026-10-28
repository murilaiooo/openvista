package com.openvista.prototype.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.util.UnstableApi
import com.openvista.prototype.LibraryRoute
import com.openvista.prototype.OpenVistaActivity
import com.openvista.prototype.R
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** The single Settings menu: atlas backdrop, category rail and one scrolling glass panel. */
@UnstableApi
class SettingsShell(private val host: OpenVistaActivity) {
    private val ui get() = host.ui
    private data class Category(val title: Int, val page: String, val icon: TvIcon.Symbol)
    private val categories = listOf(
        Category(R.string.playback, "playback_settings", TvIcon.Symbol.PLAYBACK),
        Category(R.string.settings_category_screensaver, "screensaver", TvIcon.Symbol.SCREENSAVER),
        Category(R.string.settings_category_weather_clock, "weather_settings", TvIcon.Symbol.WEATHER),
        Category(R.string.settings_category_library, "library_settings", TvIcon.Symbol.LIBRARY),
        Category(R.string.settings_category_appearance, "appearance", TvIcon.Symbol.PALETTE),
        Category(R.string.settings_category_advanced, "advanced", TvIcon.Symbol.ADVANCED),
        Category(R.string.settings_category_about, "about", TvIcon.Symbol.ABOUT),
    )

    fun create(page: String, render: (LinearLayout, String) -> Unit): View {
        val activePage = category(page)
        val active = categories.first { it.page == activePage }
        val logo = OpenVistaLogo.create(host, 15.4f)
        val subtitle = ui.caps(host.getString(R.string.settings_brand_subtitle), 0xB3FFFFFF.toInt()).apply {
            textSize = 6.4f
            letterSpacing = .42f
            setPadding(0, 0, 0, 0)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setAccessibilityHeading(false)
        }
        val title = ui.text(host.getString(R.string.settings), 27.5f).apply {
            typeface = TvDesign.light
            setPadding(0, 0, 0, 0)
            setAccessibilityHeading(true)
        }
        val breadcrumb = ui.caps(host.getString(R.string.settings_breadcrumb, host.getString(R.string.app_name),
            host.getString(R.string.settings), host.getString(active.title)), 0xE6FFFFFF.toInt()).apply {
            textSize = 7.1f
            letterSpacing = .32f
            setPadding(0, 0, 0, 0)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setAccessibilityHeading(false)
        }
        val format = host.store.settings().clockFormat
        val twelve = when (format) { "12" -> true; "24" -> false; else -> !DateFormat.is24HourFormat(host) }
        val locale = host.resources.configuration.locales[0]
        val clock = ClockText(if (twelve) "h:mm a" else "HH:mm", locale, 12f)
        val date = ClockText(DateFormat.getBestDateTimePattern(locale, "EEEMMMd"), locale, 8.6f)
        val feedback = FrameLayout(host).apply { tag = "settings_feedback_container"; isFocusable = false }

        val rail = categories.map { category ->
            RailItem(host.getString(category.title), category.icon, category.page == activePage).apply {
                tag = "settings_category_${category.page}"
                setOnClickListener {
                    if (category.page != activePage) host.open(LibraryRoute(category.page))
                    else focusableChildren(contentHolder ?: return@setOnClickListener).firstOrNull()?.requestFocus()
                }
            }
        }
        val content = ui.column().apply { setPadding(ui.dp(17f), ui.dp(9f), ui.dp(15.5f), ui.dp(14f)) }
        render(content, contentPage(page))
        val scroll = ui.scroll(content).apply {
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength(ui.dp(14))
        }
        contentHolder = content
        val panel = FrameLayout(host).apply {
            background = TvDesign.panel(host, TvDesign.glass, TvDesign.glassStroke, radiusDp = 7f)
            clipToOutline = true
            addView(scroll, FrameLayout.LayoutParams(-1, -1))
        }
        val root = SettingsLayout(logo, subtitle, rail, title, breadcrumb, clock, date, feedback, panel)

        val selected = rail[categories.indexOf(active)]
        val controls = focusableChildren(content)
        rail.forEachIndexed { index, item ->
            item.nextFocusUpId = rail.getOrNull(index - 1)?.id ?: item.id
            item.nextFocusDownId = rail.getOrNull(index + 1)?.id ?: item.id
            item.nextFocusLeftId = item.id
            item.nextFocusRightId = controls.firstOrNull()?.id ?: item.id
            item.setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    controls.firstOrNull()?.requestFocus(); true
                } else false
            }
        }
        controls.forEachIndexed { index, view ->
            view.nextFocusLeftId = selected.id
            view.nextFocusRightId = view.id
            view.nextFocusUpId = controls.getOrNull(index - 1)?.id ?: view.id
            view.nextFocusDownId = controls.getOrNull(index + 1)?.id ?: view.id
        }
        return root
    }

    private var contentHolder: View? = null

    /** Minute-aligned clock in the app's language; TextClock would use the TV's default locale. */
    private inner class ClockText(pattern: String, locale: Locale, size: Float) : TextView(host) {
        private val format = java.text.SimpleDateFormat(pattern, locale)
        private val tick = object : Runnable {
            override fun run() {
                text = format.format(java.util.Date())
                postDelayed(this, 60_000L - System.currentTimeMillis() % 60_000L)
            }
        }

        init {
            textSize = size
            typeface = TvDesign.regular
            fontFeatureSettings = "tnum"
            includeFontPadding = false
            setTextColor(TvDesign.text)
            gravity = Gravity.END
            isFocusable = false
            text = format.format(java.util.Date())
        }

        override fun onAttachedToWindow() { super.onAttachedToWindow(); removeCallbacks(tick); tick.run() }
        override fun onDetachedFromWindow() { removeCallbacks(tick); super.onDetachedFromWindow() }
    }

    private fun focusableChildren(view: View): List<View> {
        if (view.isFocusable && view.isEnabled && view.visibility == View.VISIBLE) return listOf(view)
        return if (view is ViewGroup) (0 until view.childCount).flatMap { focusableChildren(view.getChildAt(it)) } else emptyList()
    }

    /** One category: icon and label; the open category keeps a teal glass box, focus adds the glow. */
    private inner class RailItem(label: String, symbol: TvIcon.Symbol, private val open: Boolean) : LinearLayout(host) {
        init {
            id = View.generateViewId()
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
            isSelected = open
            descendantFocusability = FOCUS_BLOCK_DESCENDANTS
            contentDescription = label
            setPadding(ui.dp(12f), 0, ui.dp(10f), 0)
            background = RailBackground()
            addView(View(host).apply { background = TvIcon(host, symbol, 23f, 0xFFDDE5EA.toInt(), 1.25f) },
                LayoutParams(ui.dp(23f), ui.dp(23f)))
            addView(TextView(host).apply {
                text = label
                textSize = 14.2f
                typeface = TvDesign.regular
                includeFontPadding = false
                setTextColor(TvDesign.text)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = ui.dp(17.5f) })
            if (open) addView(View(host).apply {
                background = TvIcon(host, TvIcon.Symbol.CHEVRON_RIGHT, 11f, TvDesign.text, 2f)
            }, LayoutParams(ui.dp(11f), ui.dp(11f)))
            accessibilityDelegate = object : AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = "android.widget.Button"
                    info.isSelected = open
                }
            }
        }
    }

    /** Glass fill for the open category; remote focus adds a cyan rim with a soft halo. */
    private inner class RailBackground : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val box = RectF()
        override fun isStateful() = true
        override fun onStateChange(state: IntArray): Boolean { invalidateSelf(); return true }

        override fun draw(canvas: Canvas) {
            val focused = state.contains(android.R.attr.state_focused)
            val selected = state.contains(android.R.attr.state_selected)
            if (!focused && !selected) return
            val radius = ui.dp(6f).toFloat()
            val stroke = ui.dp(if (focused) 1.6f else 1f).toFloat()
            box.set(bounds)
            box.inset(stroke / 2, stroke / 2)
            if (focused) {
                paint.style = Paint.Style.STROKE
                for (ring in 3 downTo 1) {
                    paint.strokeWidth = ui.dp(ring * 1.6f).toFloat()
                    paint.color = (0x10 * (4 - ring)) shl 24 or (TvDesign.accent and 0xFFFFFF)
                    canvas.drawRoundRect(box, radius, radius, paint)
                }
            }
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(box.left, box.top, box.right, box.bottom,
                if (focused) 0xD9216A7B.toInt() else 0xB31A5263.toInt(),
                if (focused) 0xC2154A5A.toInt() else 0x8C123E4C.toInt(), Shader.TileMode.CLAMP)
            canvas.drawRoundRect(box, radius, radius, paint)
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = stroke
            paint.color = if (focused) 0xFF8BE6F1.toInt() else 0x8C5CCBDB.toInt()
            canvas.drawRoundRect(box, radius, radius, paint)
        }

        override fun setAlpha(alpha: Int) { paint.alpha = alpha }
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
        @Deprecated("Deprecated in Java")
        override fun getOpacity() = PixelFormat.TRANSLUCENT
    }

    /** Reference positions on the 960 x 540 dp canvas, scaled to the measured screen. */
    private inner class SettingsLayout(
        private val logo: View, private val subtitle: View, private val rail: List<View>,
        private val title: View, private val breadcrumb: View, private val clock: View, private val date: View,
        private val feedback: View, private val panel: View,
    ) : FrameLayout(host) {
        init {
            background = SettingsAtlasDrawable(host)
            listOf(logo, subtitle, title, breadcrumb, clock, date, panel, feedback).forEach(::addView)
            rail.forEach(::addView)
        }

        private var kx = 1f
        private var ky = 1f
        private fun x(value: Float) = (ui.dp(value) * kx).roundToInt()
        private fun y(value: Float) = (ui.dp(value) * ky).roundToInt()

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)
            setMeasuredDimension(width, height)
            kx = width / ui.dp(960).toFloat()
            ky = height / ui.dp(540).toFloat()
            val loose = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            fun atMost(size: Int) = MeasureSpec.makeMeasureSpec(size.coerceAtLeast(0), MeasureSpec.AT_MOST)
            fun exact(size: Int) = MeasureSpec.makeMeasureSpec(size.coerceAtLeast(0), MeasureSpec.EXACTLY)
            logo.measure(loose, loose)
            subtitle.measure(atMost(x(200f)), loose)
            title.measure(atMost(x(400f)), loose)
            breadcrumb.measure(atMost(x(470f)), loose)
            clock.measure(loose, loose)
            date.measure(loose, loose)
            feedback.measure(exact(x(250f)), atMost(y(60f)))
            rail.forEach { it.measure(exact(x(196f)), exact(y(43f))) }
            panel.measure(exact(x(800.7f) - x(295.6f)), exact(y(509f) - y(98.7f)))
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val width = right - left
            fun place(view: View, viewLeft: Int, centerY: Int) {
                val top = centerY - view.measuredHeight / 2
                view.layout(viewLeft, top, viewLeft + view.measuredWidth, top + view.measuredHeight)
            }
            fun placeEnd(view: View, viewRight: Int, centerY: Int) = place(view, viewRight - view.measuredWidth, centerY)
            place(logo, x(34.4f), y(41f))
            place(subtitle, x(34.4f) + ui.dp(24f) + ui.dp(6.5f), y(60f))
            place(title, x(305f), y(43f))
            place(breadcrumb, x(305.5f), y(73.5f))
            placeEnd(clock, width - x(32.7f), y(39.6f))
            placeEnd(date, width - x(32.7f), y(55.7f))
            feedback.layout(width - x(32.7f) - feedback.measuredWidth, y(66f), width - x(32.7f), y(66f) + feedback.measuredHeight)
            rail.forEachIndexed { index, item ->
                val top = y(116f + index * 42.5f) - item.measuredHeight / 2
                item.layout(x(27.5f), top, x(27.5f) + item.measuredWidth, top + item.measuredHeight)
            }
            panel.layout(x(295.6f), y(98.7f), x(295.6f) + panel.measuredWidth, y(98.7f) + panel.measuredHeight)
        }
    }

    companion object {
        /** Legacy routes now open the category that owns their settings. */
        fun category(page: String) = when (page) {
            "settings" -> "playback_settings"
            "schedule" -> "screensaver"
            "overlays", "overlay_editor" -> "appearance"
            "privacy", "updates" -> "advanced"
            else -> page
        }

        fun contentPage(page: String) = category(page)
    }
}

/**
 * Original decorative atlas: coarse hand-drawn continents on an equirectangular grid, ocean
 * labels and a compass rose. It is not a geographic dataset, tile service or interactive map.
 */
private class SettingsAtlasDrawable(context: Context) : Drawable() {
    private val density = context.resources.displayMetrics.density
    private val scaled = context.resources.displayMetrics.scaledDensity
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labels = listOf(
        Label(context.getString(R.string.atlas_europe), 32f, 64f, false),
        Label(context.getString(R.string.atlas_africa), 40f, 8f, false),
        Label(context.getString(R.string.atlas_south_america), -63f, -29f, true),
        Label(context.getString(R.string.atlas_indian_ocean), 52f, -32f, true),
        Label(context.getString(R.string.atlas_southern_ocean), -5f, -56f, false),
    )
    private val compass = listOf(R.string.compass_north, R.string.compass_east, R.string.compass_south, R.string.compass_west)
        .map(context::getString)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = TvDesign.medium; letterSpacing = .45f }
    /** Rounds the coarse polygon corners into softer coastlines. */
    private val coastline = android.graphics.CornerPathEffect(18f * density)
    private val land: List<FloatArray> = listOf(
        // North America, clipped at the western edge of the view.
        floatArrayOf(-100f,72f, -85f,70f, -78f,64f, -64f,60f, -56f,52f, -66f,45f, -70f,42f, -76f,36f, -81f,30f,
            -80f,25f, -83f,29f, -90f,30f, -97f,27f, -97f,21f, -92f,18f, -88f,16f, -84f,11f, -79f,8f, -82f,9f,
            -88f,13f, -100f,18f),
        floatArrayOf(-55f,60f, -44f,60f, -38f,66f, -22f,70f, -20f,78f, -40f,83f, -60f,80f, -68f,76f, -58f,68f),
        floatArrayOf(-78f,8f, -72f,12f, -62f,10f, -52f,5f, -44f,-2f, -35f,-6f, -38f,-13f, -40f,-22f, -48f,-26f,
            -53f,-34f, -58f,-38f, -63f,-42f, -66f,-48f, -69f,-52f, -74f,-53f, -72f,-45f, -73f,-37f, -71f,-30f,
            -70f,-18f, -76f,-14f, -81f,-6f, -80f,0f, -77f,4f),
        floatArrayOf(-10f,36f, -9f,43f, -2f,44f, -5f,48f, 2f,51f, 5f,53f, 8f,57f, 5f,62f, 12f,66f, 18f,70f,
            28f,71f, 40f,67f, 60f,69f, 60f,45f, 56f,27f, 48f,30f, 44f,37f, 36f,33f, 36f,36f, 26f,40f, 28f,41f,
            22f,37f, 20f,40f, 16f,38f, 18f,41f, 13f,44f, 12f,40f, 8f,44f, 3f,43f, 0f,39f, -6f,36f),
        floatArrayOf(-5f,50f, 1f,51f, 0f,53f, -2f,56f, -5f,58f, -6f,56f, -4f,54f),
        floatArrayOf(-10f,52f, -6f,52f, -6f,55f, -9f,54f),
        floatArrayOf(-17f,21f, -16f,15f, -17f,12f, -12f,7f, -8f,4f, -2f,5f, 4f,6f, 9f,4f, 10f,-2f, 12f,-6f,
            13f,-12f, 12f,-17f, 15f,-27f, 18f,-34f, 22f,-34f, 27f,-33f, 32f,-28f, 35f,-23f, 36f,-18f, 40f,-15f,
            40f,-10f, 39f,-5f, 42f,0f, 46f,3f, 51f,11f, 44f,11f, 43f,12f, 39f,16f, 37f,20f, 35f,24f, 33f,30f,
            25f,32f, 20f,31f, 11f,33f, 10f,37f, 3f,37f, -2f,35f, -6f,36f, -10f,30f, -13f,27f),
        floatArrayOf(35f,30f, 39f,22f, 43f,13f, 45f,13f, 52f,16f, 58f,20f, 60f,23f, 60f,25f, 56f,24f, 51f,24f, 48f,29f),
        floatArrayOf(44f,-12f, 50f,-15f, 49f,-22f, 45f,-25f, 43f,-20f),
    )
    private class Label(val text: String, val lon: Float, val lat: Float, val stacked: Boolean)

    private fun px(lon: Float, width: Float) = (lon + 100f) / 160f * width
    private fun py(lat: Float, height: Float) = (72f - lat) / 132f * height

    override fun draw(canvas: Canvas) {
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        if (width <= 0 || height <= 0) return
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(0f, 0f, width, height,
            intArrayOf(0xFF0A2232.toInt(), 0xFF0B2A3B.toInt(), 0xFF071A28.toInt(), 0xFF05121D.toInt()),
            floatArrayOf(0f, .35f, .7f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = RadialGradient(width * .62f, height * .42f, width * .55f, 0x22378BA8, 0x00378BA8, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = density * .6f
        paint.color = 0x14A9D8E8
        for (lon in -90..60 step 15) canvas.drawLine(px(lon.toFloat(), width), 0f, px(lon.toFloat(), width), height, paint)
        for (lat in -60..70 step 15) canvas.drawLine(0f, py(lat.toFloat(), height), width, py(lat.toFloat(), height), paint)

        val paths = land.map { points ->
            Path().apply {
                moveTo(px(points[0], width), py(points[1], height))
                for (index in 2 until points.size step 2) lineTo(px(points[index], width), py(points[index + 1], height))
                close()
            }
        }
        paint.strokeJoin = Paint.Join.ROUND
        paint.pathEffect = coastline
        paint.strokeWidth = density * 9f
        paint.color = 0x0E7CC7DC
        paths.forEach { canvas.drawPath(it, paint) }
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(0f, 0f, width * .4f, height, 0xFF20403F.toInt(), 0xFF1A3036.toInt(), Shader.TileMode.MIRROR)
        paint.alpha = 0xB0
        paths.forEach { canvas.drawPath(it, paint) }
        paint.shader = null
        paint.alpha = 0xFF
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = density * .8f
        paint.color = 0x4D86B9C9
        paths.forEach { canvas.drawPath(it, paint) }
        paint.pathEffect = null

        text.textSize = 7.2f * scaled
        text.color = 0x66CDE3EC
        labels.forEach { label ->
            val x = px(label.lon, width)
            var y = py(label.lat, height)
            val lines = if (label.stacked) label.text.uppercase().split(' ') else listOf(label.text.uppercase())
            lines.forEach { line ->
                canvas.drawText(line, x - text.measureText(line) / 2, y, text)
                y += text.textSize * 1.55f
            }
        }
        drawCompass(canvas, 77f * width / 960f, 464f * height / 540f, 40f * density * (width / (960f * density)))
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(width / 2, height / 2, width * .75f, 0x00000000, 0x8C02080E.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null
        canvas.restore()
    }

    private fun drawCompass(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = density * .7f
        paint.color = 0x4DB7D4DF
        canvas.drawCircle(cx, cy, radius * .72f, paint)
        canvas.drawCircle(cx, cy, radius * .62f, paint)
        for (tick in 0 until 72) {
            val angle = Math.toRadians(tick * 5.0)
            val inner = radius * (if (tick % 18 == 0) .56f else .66f)
            canvas.drawLine(cx + cos(angle).toFloat() * inner, cy + sin(angle).toFloat() * inner,
                cx + cos(angle).toFloat() * radius * .72f, cy + sin(angle).toFloat() * radius * .72f, paint)
        }
        val star = Path()
        for (point in 0 until 16) {
            val angle = -Math.PI / 2 + point * Math.PI / 8
            val length = when { point % 4 == 0 -> radius; point % 2 == 0 -> radius * .55f; else -> radius * .16f }
            val x = cx + cos(angle).toFloat() * length
            val y = cy + sin(angle).toFloat() * length
            if (point == 0) star.moveTo(x, y) else star.lineTo(x, y)
        }
        star.close()
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(cx, cy - radius, cx, cy + radius, 0x8CC8DEE6.toInt(), 0x403E6B7C, Shader.TileMode.CLAMP)
        canvas.drawPath(star, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.color = 0x80C8DEE6.toInt()
        canvas.drawPath(star, paint)
        text.textSize = 7f * scaled
        text.color = 0x99D7E6EC.toInt()
        val offset = radius * 1.18f
        listOf(0f to -offset, offset to 0f, 0f to offset, -offset to 0f).forEachIndexed { index, (dx, dy) ->
            val label = compass[index]
            canvas.drawText(label, cx + dx - text.measureText(label) / 2, cy + dy + text.textSize * .35f, text)
        }
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha; invalidateSelf() }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
