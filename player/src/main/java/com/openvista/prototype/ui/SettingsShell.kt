package com.openvista.prototype.ui

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextClock
import androidx.media3.common.util.UnstableApi
import com.openvista.prototype.LibraryRoute
import com.openvista.prototype.OpenVistaActivity
import com.openvista.prototype.R

/** Bounded native TV settings: category rail and an independently scrolling content panel. */
@UnstableApi
class SettingsShell(private val host: OpenVistaActivity) {
    private val ui get() = host.ui
    private data class Category(val title: Int, val page: String, val icon: TvIcon.Symbol)
    private val categories = listOf(
        Category(R.string.playback, "playback_settings", TvIcon.Symbol.PLAYBACK),
        Category(R.string.settings_category_screensaver, "screensaver", TvIcon.Symbol.SCREENSAVER),
        Category(R.string.settings_category_weather_clock, "weather_settings", TvIcon.Symbol.WEATHER),
        Category(R.string.settings_category_library, "library_settings", TvIcon.Symbol.LIBRARY),
        Category(R.string.settings_category_appearance, "appearance", TvIcon.Symbol.APPEARANCE),
        Category(R.string.settings_category_advanced, "advanced", TvIcon.Symbol.ADVANCED),
        Category(R.string.settings_category_about, "about", TvIcon.Symbol.ABOUT),
    )

    fun create(page: String, render: (LinearLayout, String) -> Unit): View {
        val contentPage = if (page == "settings") "playback_settings" else page
        val activePage = when (page) {
            "settings" -> "playback_settings"
            "schedule" -> "screensaver"
            "overlays", "overlay_editor" -> "appearance"
            "privacy", "updates" -> "advanced"
            else -> page
        }
        val root = FrameLayout(host).apply { background = SettingsAtlasDrawable() }
        val layout = ui.column().apply {
            setPadding(ui.dp(TvDesign.safeHorizontal), ui.dp(TvDesign.safeVertical),
                ui.dp(TvDesign.safeHorizontal), ui.dp(TvDesign.safeVertical))
        }
        val header = ui.row().apply { minimumHeight = ui.dp(56) }
        val identity = ui.column()
        identity.addView(ui.text(host.getString(R.string.app_name), 23f).apply {
            typeface = TvDesign.medium
            setPadding(0, 0, 0, 0)
            letterSpacing = .08f
        })
        identity.addView(ui.text(host.getString(R.string.settings), 12f, TvDesign.muted).apply {
            setPadding(0, ui.dp(3), 0, 0)
            letterSpacing = .16f
        })
        header.addView(identity, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val clockFormat = host.store.settings().clockFormat
        val clock = TextClock(host).apply {
            format12Hour = if (clockFormat == "24") "HH:mm" else "h:mm a"
            format24Hour = if (clockFormat == "12") "h:mm a" else "HH:mm"
            textSize = 20f
            typeface = TvDesign.light
            setTextColor(TvDesign.text)
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
            setPadding(0, 0, ui.dp(22), 0)
            isFocusable = false
        }
        header.addView(clock)
        val headerActions = ui.row()
        val back = ui.button(headerActions, host.getString(R.string.back), "settings_back") {
            host.onBackPressedDispatcher.onBackPressed()
        }
        val home = ui.button(headerActions, host.getString(R.string.home), "settings_home") {
            host.goHome()
        }
        listOf(back, home).forEach { control ->
            control.minHeight = ui.dp(42)
            control.textSize = 14f
            control.setPadding(ui.dp(12), ui.dp(8), ui.dp(12), ui.dp(8))
            control.layoutParams = (control.layoutParams as LinearLayout.LayoutParams).apply {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                weight = 0f
            }
        }
        header.addView(headerActions, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        layout.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        // Feedback participates in measurement, including its full translated text height.
        layout.addView(FrameLayout(host).apply {
            tag = "settings_feedback_container"
            isFocusable = false
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = ui.dp(4)
            bottomMargin = ui.dp(8)
        })

        val columns = ui.row().apply { gravity = Gravity.TOP; isBaselineAligned = false }
        val rail = ui.column().apply { setPadding(0, 0, ui.dp(16), 0) }
        val railButtons = mutableListOf<Button>()
        categories.forEach { category ->
            val selected = category.page == activePage
            val button = ui.button(rail, host.getString(category.title), "settings_category_${category.page}") {
                if (category.page != contentPage) host.open(LibraryRoute(category.page))
                else firstFocusable(columns.getChildAt(1))?.requestFocus()
            }.apply {
                minHeight = ui.dp(42)
                textSize = 15f
                setPadding(ui.dp(14), ui.dp(6), ui.dp(10), ui.dp(6))
                background = TvDesign.control(host, selected)
                isSelected = selected
                setCompoundDrawablesWithIntrinsicBounds(TvIcon(host, category.icon), null, null, null)
                compoundDrawablePadding = ui.dp(10)
                if (selected) setTextColor(TvDesign.accent)
            }
            railButtons += button
        }
        columns.addView(ui.scroll(rail), LinearLayout.LayoutParams(ui.dp(200), ViewGroup.LayoutParams.MATCH_PARENT))
        val content = ui.column().apply {
            setPadding(ui.dp(22), ui.dp(18), ui.dp(22), ui.dp(24))
        }
        render(content, contentPage)
        val scroll = ui.scroll(content).apply { background = TvDesign.panel(host, 0xBF0D2132.toInt()) }
        columns.addView(scroll, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        layout.addView(columns, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(layout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val active = railButtons[categories.indexOfFirst { it.page == activePage }.coerceAtLeast(0)]
        val controls = focusableChildren(content)
        railButtons.forEachIndexed { index, button ->
            button.nextFocusUpId = railButtons.getOrNull(index - 1)?.id ?: back.id
            button.nextFocusDownId = railButtons.getOrNull(index + 1)?.id ?: home.id
            button.nextFocusRightId = controls.firstOrNull()?.id ?: home.id
            button.setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    (controls.firstOrNull() ?: home).requestFocus(); true
                } else false
            }
        }
        controls.forEachIndexed { index, view ->
            view.nextFocusLeftId = active.id
            view.nextFocusUpId = controls.getOrNull(index - 1)?.id ?: back.id
            view.nextFocusDownId = controls.getOrNull(index + 1)?.id ?: home.id
        }
        back.nextFocusDownId = active.id
        home.nextFocusDownId = controls.firstOrNull()?.id ?: active.id
        return root
    }

    private fun firstFocusable(view: View?): View? = view?.let { focusableChildren(it).firstOrNull() }
    private fun focusableChildren(view: View): List<View> {
        if (view.isFocusable && view.isEnabled && view.visibility == View.VISIBLE) return listOf(view)
        return if (view is ViewGroup) (0 until view.childCount).flatMap { focusableChildren(view.getChildAt(it)) } else emptyList()
    }
}

/** Original schematic geometry, used only as decoration; it is not a location or map service. */
private class SettingsAtlasDrawable : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val land = listOf(
        floatArrayOf(.06f,.22f, .12f,.14f, .23f,.13f, .29f,.22f, .24f,.28f, .21f,.38f, .16f,.39f, .13f,.31f, .08f,.31f),
        floatArrayOf(.22f,.39f, .29f,.45f, .31f,.56f, .28f,.65f, .25f,.78f, .21f,.66f, .20f,.51f),
        floatArrayOf(.38f,.18f, .43f,.13f, .47f,.19f, .45f,.26f, .40f,.25f),
        floatArrayOf(.44f,.31f, .48f,.27f, .56f,.31f, .59f,.39f, .55f,.51f, .51f,.65f, .46f,.59f, .43f,.46f),
        floatArrayOf(.48f,.24f, .54f,.17f, .66f,.14f, .76f,.18f, .86f,.17f, .94f,.28f, .88f,.38f, .79f,.36f, .74f,.44f, .68f,.37f, .62f,.43f, .58f,.31f),
        floatArrayOf(.74f,.60f, .82f,.55f, .90f,.62f, .91f,.71f, .83f,.75f, .76f,.70f),
        floatArrayOf(.08f,.88f, .25f,.85f, .44f,.87f, .63f,.84f, .81f,.88f, .94f,.87f, .96f,.93f, .06f,.93f),
    ).map { points -> Path().apply {
        moveTo(points[0], points[1])
        for (index in 2 until points.size step 2) lineTo(points[index], points[index + 1])
        close()
    } }

    override fun draw(canvas: Canvas) {
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        if (width <= 0 || height <= 0) return
        paint.shader = LinearGradient(0f, 0f, width, height,
            intArrayOf(0xFF07111D.toInt(), 0xFF152D40.toInt(), 0xFF071522.toInt()), null, Shader.TileMode.CLAMP)
        canvas.drawRect(bounds, paint)
        paint.shader = null
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(width, height)
        paint.style = Paint.Style.FILL
        paint.color = 0x354B839B
        land.forEach { canvas.drawPath(it, paint) }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f / width
        paint.color = 0x173E657D
        for (index in 1..11) canvas.drawLine(index / 12f, .1f, index / 12f, .94f, paint)
        for (index in 1..6) canvas.drawLine(.03f, index / 7f, .97f, index / 7f, paint)
        paint.color = 0x264B7B94
        land.forEach { canvas.drawPath(it, paint) }
        paint.style = Paint.Style.FILL
        canvas.restore()
    }
    override fun setAlpha(alpha: Int) { paint.alpha = alpha; invalidateSelf() }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
