package com.openvista.prototype.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.widget.TextView
import com.openvista.prototype.R

/**
 * Shared visual vocabulary for every TV surface. Sizes are dp/sp on the 960 x 540 dp canvas
 * established by [AppLocale.wrap], so one value means the same fraction of every screen.
 */
object TvDesign {
    const val background = 0xFF061421.toInt()
    const val surface = 0xB80B1D2C.toInt()
    const val surfaceRaised = 0x9E132A3C.toInt()
    const val glass = 0xA60A1A28.toInt()
    const val glassStroke = 0x26FFFFFF
    const val rowStroke = 0x1CFFFFFF
    const val text = 0xFFF3F5F7.toInt()
    const val muted = 0xFFAAB8C3.toInt()
    const val faint = 0xFF7E8F9C.toInt()
    const val accent = 0xFF7FDDE9.toInt()
    const val accentStrong = 0xFF5CCBDB.toInt()
    const val selectedFill = 0xC2195060.toInt()
    const val toggleOn = 0xE6205F6E.toInt()
    const val live = 0xFF4CD368.toInt()
    const val homeFocus = 0xFFC4DCCB.toInt()
    const val cream = 0xFFF3F0E8.toInt()
    const val ink = 0xFF1B2320.toInt()
    const val line = 0x24FFFFFF

    const val safeHorizontal = 28
    const val safeVertical = 24
    const val pageTitleSize = 28f
    const val titleSize = 22f
    const val bodySize = 12f
    const val summarySize = 9f
    const val labelSize = 10f
    const val capsSize = 7.5f
    const val rowHeight = 40
    const val radius = 6
    const val focusStroke = 2
    const val motionMs = 140L

    var thin: Typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL); private set
    var extraLight: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); private set
    var light: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); private set
    var regular: Typeface = Typeface.create("sans-serif", Typeface.NORMAL); private set
    var medium: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); private set
    var semibold: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); private set
    var bold: Typeface = Typeface.create("sans-serif", Typeface.BOLD); private set
    @Volatile private var installed = false

    /** Loads the bundled Inter family once; system sans-serif stays as the fallback. */
    fun install(context: Context) {
        if (installed) return
        synchronized(this) {
            if (installed) return
            val resources = (context.applicationContext ?: context).resources
            fun font(id: Int, fallback: Typeface) = try { resources.getFont(id) } catch (_: RuntimeException) { fallback }
            thin = font(R.font.inter_thin, thin)
            extraLight = font(R.font.inter_extralight, extraLight)
            light = font(R.font.inter_light, light)
            regular = font(R.font.inter_regular, regular)
            medium = font(R.font.inter_medium, medium)
            semibold = font(R.font.inter_semibold, semibold)
            bold = font(R.font.inter_bold, bold)
            installed = true
        }
    }

    fun dp(context: Context, value: Int) = (value * context.resources.displayMetrics.density + .5f).toInt()
    fun dp(context: Context, value: Float) = (value * context.resources.displayMetrics.density + .5f).toInt()

    fun panel(context: Context, color: Int = surface, stroke: Int = line, rounded: Boolean = true,
              radiusDp: Float = radius.toFloat()) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = if (rounded) dp(context, radiusDp).toFloat() else 0f
        setStroke(dp(context, 1), stroke)
    }

    /** Remote focus is always the cyan stroke; selection alone is a quiet tinted fill. */
    fun control(context: Context, selected: Boolean = false, base: Int = surfaceRaised,
                stroke: Int = rowStroke, radiusDp: Float = radius.toFloat()) = StateListDrawable().apply {
        addState(intArrayOf(-android.R.attr.state_enabled), panel(context, 0x400B1D2C, 0x14FFFFFF, radiusDp = radiusDp))
        addState(intArrayOf(android.R.attr.state_pressed), panel(context, 0xE0226474.toInt(), accent, radiusDp = radiusDp))
        addState(intArrayOf(android.R.attr.state_focused), panel(context, selectedFill, accent, radiusDp = radiusDp).apply {
            setStroke(dp(context, focusStroke), accent)
        })
        addState(intArrayOf(), panel(context, if (selected) selectedFill else base,
            if (selected) 0x885CCBDB.toInt() else stroke, radiusDp = radiusDp))
    }

    fun scrim(fromTop: Boolean = false) = GradientDrawable(
        if (fromTop) GradientDrawable.Orientation.TOP_BOTTOM else GradientDrawable.Orientation.BOTTOM_TOP,
        intArrayOf(0xD906141F.toInt(), 0x8806141F.toInt(), Color.TRANSPARENT),
    )

    /** Tracked uppercase labels used for eyebrows, breadcrumbs and footers. */
    fun caps(view: TextView, tracking: Float = .18f) {
        view.isAllCaps = true
        view.letterSpacing = tracking
    }

    fun decorative(view: View) {
        view.isFocusable = false
        view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
}
