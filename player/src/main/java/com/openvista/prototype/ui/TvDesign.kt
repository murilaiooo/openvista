package com.openvista.prototype.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View

/** Shared, density-independent visual vocabulary for the native TV interface. */
object TvDesign {
    const val background = 0xFF07121F.toInt()
    const val surface = 0xD916283A.toInt()
    const val surfaceRaised = 0xE6203548.toInt()
    const val text = 0xFFF3F6F8.toInt()
    const val muted = 0xFFB9C8D5.toInt()
    const val accent = 0xFF91DFEA.toInt()
    const val line = 0x334F7891
    const val safeHorizontal = 40
    const val safeVertical = 28
    const val bodySize = 17f
    const val labelSize = 13f
    const val titleSize = 32f
    const val rowHeight = 56
    const val radius = 8
    const val focusStroke = 2
    const val motionMs = 140L
    val regular: Typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    val medium: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    val light: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)

    fun dp(context: Context, value: Int) = (value * context.resources.displayMetrics.density + .5f).toInt()

    fun panel(context: Context, color: Int = surface, stroke: Int = line, rounded: Boolean = true) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = if (rounded) dp(context, radius).toFloat() else 0f
        setStroke(dp(context, 1), stroke)
    }

    fun control(context: Context, selected: Boolean = false) = StateListDrawable().apply {
        addState(intArrayOf(-android.R.attr.state_enabled), panel(context, 0x4416283A, 0x22364A5A))
        addState(intArrayOf(android.R.attr.state_pressed), panel(context, 0xFF294B5B.toInt(), accent))
        addState(intArrayOf(android.R.attr.state_focused), panel(context, 0xF02B4657.toInt(), accent).apply {
            setStroke(dp(context, focusStroke), accent)
        })
        addState(intArrayOf(), panel(context, if (selected) 0xD9234054.toInt() else surface,
            if (selected) 0x8871B9C9.toInt() else line))
    }

    fun scrim(fromTop: Boolean = false) = GradientDrawable(
        if (fromTop) GradientDrawable.Orientation.TOP_BOTTOM else GradientDrawable.Orientation.BOTTOM_TOP,
        intArrayOf(0xD907121F.toInt(), 0x8807121F.toInt(), Color.TRANSPARENT),
    )

    fun decorative(view: View) {
        view.isFocusable = false
        view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
}
