package com.openvista.prototype.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.openvista.prototype.R

/**
 * The single OpenVista identity: a tilted lens frame with an inner aperture next to the bold
 * lowercase wordmark. Every screen builds its logo here so the mark can be swapped in one place.
 */
object OpenVistaLogo {
    /** Wordmark height drives everything; the mark is 1.56 times the wordmark text size. */
    fun create(context: Context, textSp: Float, color: Int = TvDesign.text): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            contentDescription = context.getString(R.string.app_name)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            val markSize = TvDesign.dp(context, textSp * 1.56f)
            addView(View(context).apply {
                background = Mark(color)
                TvDesign.decorative(this)
            }, LinearLayout.LayoutParams(markSize, markSize))
            addView(TextView(context).apply {
                text = WORDMARK
                textSize = textSp
                typeface = TvDesign.bold
                letterSpacing = -.03f
                includeFontPadding = false
                setTextColor(color)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                leftMargin = TvDesign.dp(context, textSp * .42f)
            })
        }

    const val WORDMARK = "openvista"

    /** 24-unit mark; the launcher icon and banner vectors trace the same geometry. */
    class Mark(private val tint: Int = TvDesign.text) : Drawable() {
        private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 1.7f
            color = tint
        }

        override fun draw(canvas: Canvas) {
            if (bounds.isEmpty) return
            canvas.save()
            canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
            val unit = minOf(bounds.width(), bounds.height()) / 24f
            canvas.scale(unit, unit)
            canvas.save()
            canvas.rotate(-14f, 12f, 12f)
            canvas.drawRoundRect(2.6f, 2.6f, 21.4f, 21.4f, 6.2f, 6.2f, stroke)
            canvas.restore()
            canvas.drawCircle(11.6f, 12.2f, 3.3f, stroke)
            canvas.restore()
        }

        override fun setAlpha(alpha: Int) { stroke.alpha = alpha; invalidateSelf() }
        override fun setColorFilter(colorFilter: ColorFilter?) { stroke.colorFilter = colorFilter; invalidateSelf() }
        @Deprecated("Deprecated in Java")
        override fun getOpacity() = PixelFormat.TRANSLUCENT
    }
}
