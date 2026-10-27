package com.openvista.prototype.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import kotlin.math.cos
import kotlin.math.sin

/** Original 24-unit line icons shared by navigation; labels provide their accessible names. */
class TvIcon(context: Context, private val symbol: Symbol) : Drawable() {
    enum class Symbol { PLAYBACK, SCREENSAVER, WEATHER, LIBRARY, APPEARANCE, ADVANCED, ABOUT, SETTINGS }
    private val size = TvDesign.dp(context, 22)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TvDesign.muted
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val path = Path()
    override fun getIntrinsicWidth() = size
    override fun getIntrinsicHeight() = size
    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(bounds.width() / 24f, bounds.height() / 24f)
        when (symbol) {
            Symbol.PLAYBACK -> {
                path.rewind(); path.moveTo(8f, 4f); path.lineTo(21f, 12f); path.lineTo(8f, 20f); path.close()
                canvas.drawPath(path, paint)
            }
            Symbol.SCREENSAVER -> {
                canvas.drawRoundRect(2f, 3f, 22f, 17f, 2f, 2f, paint)
                canvas.drawLine(12f, 17f, 12f, 21f, paint); canvas.drawLine(7f, 21f, 17f, 21f, paint)
            }
            Symbol.WEATHER -> {
                canvas.drawCircle(9f, 8f, 4f, paint)
                path.rewind(); path.moveTo(7f, 20f); path.cubicTo(0f, 20f, 1f, 12f, 8f, 13f)
                path.cubicTo(10f, 5f, 21f, 10f, 20f, 14f); path.cubicTo(26f, 17f, 22f, 21f, 18f, 20f)
                path.close(); canvas.drawPath(path, paint)
            }
            Symbol.LIBRARY -> {
                canvas.drawRoundRect(3f, 5f, 21f, 21f, 2f, 2f, paint)
                canvas.drawLine(7f, 2f, 17f, 2f, paint); canvas.drawLine(7f, 10f, 17f, 10f, paint)
                canvas.drawLine(7f, 15f, 14f, 15f, paint)
            }
            Symbol.APPEARANCE -> {
                canvas.drawCircle(12f, 12f, 9f, paint)
                canvas.drawArc(6f, 6f, 18f, 18f, -90f, 180f, false, paint)
                canvas.drawLine(12f, 3f, 12f, 21f, paint)
            }
            Symbol.ADVANCED -> for (i in 0..2) {
                val x = 5f + i * 7
                val y = if (i == 1) 15f else 8f
                canvas.drawLine(x, 3f, x, y - 3, paint); canvas.drawLine(x, y + 3, x, 21f, paint)
                canvas.drawCircle(x, y, 3f, paint)
            }
            Symbol.ABOUT -> {
                canvas.drawCircle(12f, 12f, 9f, paint)
                canvas.drawPoint(12f, 7f, paint); canvas.drawLine(12f, 11f, 12f, 17f, paint)
            }
            Symbol.SETTINGS -> {
                canvas.drawCircle(12f, 12f, 7f, paint); canvas.drawCircle(12f, 12f, 2.5f, paint)
                for (i in 0..7) {
                    val a = i * Math.PI / 4
                    canvas.drawLine(12f + cos(a).toFloat() * 8, 12f + sin(a).toFloat() * 8,
                        12f + cos(a).toFloat() * 10, 12f + sin(a).toFloat() * 10, paint)
                }
            }
        }
        canvas.restore()
    }
    override fun setAlpha(alpha: Int) { paint.alpha = alpha; invalidateSelf() }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
