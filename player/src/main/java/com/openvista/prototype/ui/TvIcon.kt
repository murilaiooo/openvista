package com.openvista.prototype.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import kotlin.math.cos
import kotlin.math.sin

/**
 * Original 24-unit line icons shared by every screen. Labels provide accessible names, so the
 * drawable itself is always decorative.
 */
class TvIcon(
    context: Context,
    private val symbol: Symbol,
    sizeDp: Float = 16f,
    tint: Int = TvDesign.text,
    strokeUnits: Float = 1.5f,
) : Drawable() {
    enum class Symbol {
        PLAYBACK, SCREENSAVER, WEATHER, LIBRARY, APPEARANCE, ADVANCED, ABOUT, SETTINGS,
        CAMERA, FILM, HEART, STAR, ARROW_RIGHT, CHEVRON_RIGHT, CHEVRON_DOWN, NAVIGATE,
        DATABASE, UPLOAD, FOLDER, EYE, REFRESH, CLOCK, GLOBE, SEARCH, LINK, MOVE, TIMER,
        VOLUME, POWER, SHIELD, REPORT, DOWNLOAD, PLAYLIST, LAYERS, PALETTE, SUN, FIT, QUALITY,
        TRANSITION, HOME, LANGUAGE, TEXT_SIZE, OPACITY, PIN, PLAY, WIND,
    }

    private val size = TvDesign.dp(context, sizeDp)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = tint
        style = Paint.Style.STROKE
        strokeWidth = strokeUnits
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val path = Path()
    private val oval = RectF()

    override fun getIntrinsicWidth() = size
    override fun getIntrinsicHeight() = size

    fun tint(color: Int) { if (paint.color != color) { paint.color = color; invalidateSelf() } }

    override fun draw(canvas: Canvas) {
        if (bounds.isEmpty) return
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(bounds.width() / 24f, bounds.height() / 24f)
        drawSymbol(canvas)
        canvas.restore()
    }

    private fun line(canvas: Canvas, vararg points: Float) {
        path.rewind()
        path.moveTo(points[0], points[1])
        for (index in 2 until points.size step 2) path.lineTo(points[index], points[index + 1])
        canvas.drawPath(path, paint)
    }

    private fun gear(canvas: Canvas, cx: Float, cy: Float, outer: Float, inner: Float, hole: Float) {
        path.rewind()
        val teeth = 8
        for (i in 0 until teeth * 2) {
            val radius = if (i % 2 == 0) outer else inner
            for (step in 0..1) {
                val angle = Math.PI * (i + step * .55 - .27) / teeth
                val x = cx + cos(angle).toFloat() * radius
                val y = cy + sin(angle).toFloat() * radius
                if (i == 0 && step == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
        }
        path.close()
        canvas.drawPath(path, paint)
        canvas.drawCircle(cx, cy, hole, paint)
    }

    private fun drawSymbol(canvas: Canvas) {
        when (symbol) {
            Symbol.PLAYBACK -> {
                canvas.drawCircle(12f, 12f, 10f, paint)
                path.rewind(); path.moveTo(10f, 8f); path.lineTo(16f, 12f); path.lineTo(10f, 16f); path.close()
                canvas.drawPath(path, paint)
            }
            Symbol.PLAY -> {
                path.rewind(); path.moveTo(7f, 4.5f); path.lineTo(19f, 12f); path.lineTo(7f, 19.5f); path.close()
                canvas.drawPath(path, paint)
            }
            Symbol.SCREENSAVER -> {
                canvas.drawRoundRect(2.5f, 3.5f, 21.5f, 20.5f, 1.5f, 1.5f, paint)
                line(canvas, 3f, 18f, 9f, 11f, 13f, 15f, 16f, 12.5f, 21f, 17f)
                canvas.drawCircle(16f, 8f, 1.6f, paint)
            }
            Symbol.WEATHER -> {
                path.rewind(); path.moveTo(7f, 19f)
                path.cubicTo(3.5f, 19f, 2f, 16.8f, 2.4f, 14.5f); path.cubicTo(2.8f, 12.2f, 5f, 11f, 7f, 11.5f)
                path.cubicTo(7.8f, 7.6f, 11f, 5.5f, 14.2f, 6.2f); path.cubicTo(17.5f, 6.9f, 19.3f, 9.6f, 19f, 12.4f)
                path.cubicTo(21.3f, 12.8f, 22.4f, 14.6f, 22f, 16.4f); path.cubicTo(21.6f, 18.1f, 20.3f, 19f, 18.6f, 19f)
                path.close(); canvas.drawPath(path, paint)
            }
            Symbol.LIBRARY, Symbol.FOLDER -> {
                path.rewind(); path.moveTo(2.5f, 6f); path.lineTo(2.5f, 19f); path.lineTo(21.5f, 19f)
                path.lineTo(21.5f, 8f); path.lineTo(11.5f, 8f); path.lineTo(9.5f, 5f); path.lineTo(3.5f, 5f)
                path.close(); canvas.drawPath(path, paint)
            }
            Symbol.APPEARANCE, Symbol.PALETTE -> {
                path.rewind(); path.moveTo(12f, 2.5f)
                path.cubicTo(6.5f, 2.5f, 2.5f, 6.6f, 2.5f, 11.8f); path.cubicTo(2.5f, 17.1f, 6.8f, 21.5f, 12f, 21.5f)
                path.cubicTo(13.6f, 21.5f, 14.2f, 20.2f, 13.4f, 18.9f); path.cubicTo(12.6f, 17.6f, 13.4f, 16f, 15f, 16f)
                path.lineTo(17.4f, 16f); path.cubicTo(19.8f, 16f, 21.5f, 14.2f, 21.5f, 11.8f)
                path.cubicTo(21.5f, 6.6f, 17.3f, 2.5f, 12f, 2.5f); path.close(); canvas.drawPath(path, paint)
                canvas.drawCircle(7.5f, 11f, 1.4f, paint); canvas.drawCircle(10f, 6.8f, 1.4f, paint)
                canvas.drawCircle(14.8f, 6.8f, 1.4f, paint); canvas.drawCircle(17.3f, 10.8f, 1.4f, paint)
            }
            Symbol.ADVANCED, Symbol.SETTINGS -> gear(canvas, 12f, 12f, 10f, 7.6f, 3.2f)
            Symbol.ABOUT -> {
                canvas.drawCircle(12f, 12f, 10f, paint)
                canvas.drawLine(12f, 11f, 12f, 17f, paint); canvas.drawPoint(12f, 7.5f, paint)
            }
            Symbol.CAMERA -> {
                canvas.drawRoundRect(2.5f, 6.5f, 16f, 17.5f, 1.8f, 1.8f, paint)
                line(canvas, 16f, 10.5f, 21.5f, 7.5f, 21.5f, 16.5f, 16f, 13.5f)
            }
            Symbol.FILM -> {
                canvas.drawRoundRect(3.5f, 2.5f, 20.5f, 21.5f, 1.5f, 1.5f, paint)
                canvas.drawLine(7.5f, 2.5f, 7.5f, 21.5f, paint); canvas.drawLine(16.5f, 2.5f, 16.5f, 21.5f, paint)
                for (y in listOf(7f, 12f, 17f)) {
                    canvas.drawLine(3.5f, y, 7.5f, y, paint); canvas.drawLine(16.5f, y, 20.5f, y, paint)
                }
            }
            Symbol.HEART -> {
                path.rewind(); path.moveTo(12f, 20.5f)
                path.cubicTo(5f, 15.5f, 2.5f, 12.3f, 2.5f, 8.8f); path.cubicTo(2.5f, 6f, 4.7f, 4f, 7.3f, 4f)
                path.cubicTo(9.2f, 4f, 10.8f, 5f, 12f, 6.8f); path.cubicTo(13.2f, 5f, 14.8f, 4f, 16.7f, 4f)
                path.cubicTo(19.3f, 4f, 21.5f, 6f, 21.5f, 8.8f); path.cubicTo(21.5f, 12.3f, 19f, 15.5f, 12f, 20.5f)
                path.close(); canvas.drawPath(path, paint)
            }
            Symbol.STAR -> {
                path.rewind()
                for (i in 0 until 10) {
                    val radius = if (i % 2 == 0) 10f else 4.3f
                    val angle = -Math.PI / 2 + i * Math.PI / 5
                    val x = 12f + cos(angle).toFloat() * radius
                    val y = 12.8f + sin(angle).toFloat() * radius
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close(); canvas.drawPath(path, paint)
            }
            Symbol.ARROW_RIGHT -> {
                canvas.drawLine(5f, 12f, 19f, 12f, paint); line(canvas, 13f, 6f, 19f, 12f, 13f, 18f)
            }
            Symbol.CHEVRON_RIGHT -> line(canvas, 9f, 5f, 16f, 12f, 9f, 19f)
            Symbol.CHEVRON_DOWN -> line(canvas, 5f, 9f, 12f, 16f, 19f, 9f)
            Symbol.NAVIGATE -> {
                path.rewind(); path.moveTo(21f, 3f); path.lineTo(3f, 10.5f); path.lineTo(11f, 13f)
                path.lineTo(13.5f, 21f); path.close(); canvas.drawPath(path, paint)
            }
            Symbol.DATABASE -> {
                oval.set(4f, 2.5f, 20f, 7.5f); canvas.drawOval(oval, paint)
                canvas.drawLine(4f, 5f, 4f, 19f, paint); canvas.drawLine(20f, 5f, 20f, 19f, paint)
                for (y in listOf(9.7f, 14.4f, 19f)) { oval.set(4f, y - 2.5f, 20f, y + 2.5f); canvas.drawArc(oval, 0f, 180f, false, paint) }
            }
            Symbol.UPLOAD -> {
                line(canvas, 3.5f, 14f, 3.5f, 20.5f, 20.5f, 20.5f, 20.5f, 14f)
                canvas.drawLine(12f, 16f, 12f, 3.5f, paint); line(canvas, 7f, 8.5f, 12f, 3.5f, 17f, 8.5f)
            }
            Symbol.DOWNLOAD -> {
                line(canvas, 3.5f, 14f, 3.5f, 20.5f, 20.5f, 20.5f, 20.5f, 14f)
                canvas.drawLine(12f, 3.5f, 12f, 16f, paint); line(canvas, 7f, 11f, 12f, 16f, 17f, 11f)
            }
            Symbol.EYE -> {
                path.rewind(); path.moveTo(2f, 12f); path.cubicTo(5f, 6.5f, 8.5f, 5f, 12f, 5f)
                path.cubicTo(15.5f, 5f, 19f, 6.5f, 22f, 12f); path.cubicTo(19f, 17.5f, 15.5f, 19f, 12f, 19f)
                path.cubicTo(8.5f, 19f, 5f, 17.5f, 2f, 12f); path.close(); canvas.drawPath(path, paint)
                canvas.drawCircle(12f, 12f, 3.3f, paint)
            }
            Symbol.REFRESH -> {
                oval.set(3.5f, 3.5f, 20.5f, 20.5f)
                canvas.drawArc(oval, 200f, 130f, false, paint); canvas.drawArc(oval, 20f, 130f, false, paint)
                line(canvas, 20f, 3.8f, 20.1f, 8.3f, 15.6f, 8.3f)
                line(canvas, 4f, 20.2f, 3.9f, 15.7f, 8.4f, 15.7f)
            }
            Symbol.CLOCK, Symbol.TIMER -> {
                canvas.drawCircle(12f, 12.5f, 9f, paint); line(canvas, 12f, 7.5f, 12f, 12.5f, 15.5f, 14.5f)
                if (symbol == Symbol.TIMER) canvas.drawLine(9.5f, 2f, 14.5f, 2f, paint)
            }
            Symbol.GLOBE, Symbol.LANGUAGE -> {
                canvas.drawCircle(12f, 12f, 9.5f, paint)
                oval.set(7.5f, 2.5f, 16.5f, 21.5f); canvas.drawOval(oval, paint)
                canvas.drawLine(2.5f, 12f, 21.5f, 12f, paint)
                canvas.drawLine(4f, 7f, 20f, 7f, paint); canvas.drawLine(4f, 17f, 20f, 17f, paint)
            }
            Symbol.SEARCH -> { canvas.drawCircle(10.5f, 10.5f, 7f, paint); canvas.drawLine(15.5f, 15.5f, 21f, 21f, paint) }
            Symbol.LINK -> {
                canvas.drawRoundRect(2.5f, 8.5f, 13.5f, 15.5f, 3.5f, 3.5f, paint)
                canvas.drawRoundRect(10.5f, 8.5f, 21.5f, 15.5f, 3.5f, 3.5f, paint)
            }
            Symbol.MOVE -> {
                canvas.drawLine(12f, 2.5f, 12f, 21.5f, paint); canvas.drawLine(2.5f, 12f, 21.5f, 12f, paint)
                line(canvas, 9f, 5.5f, 12f, 2.5f, 15f, 5.5f); line(canvas, 9f, 18.5f, 12f, 21.5f, 15f, 18.5f)
                line(canvas, 5.5f, 9f, 2.5f, 12f, 5.5f, 15f); line(canvas, 18.5f, 9f, 21.5f, 12f, 18.5f, 15f)
            }
            Symbol.VOLUME -> {
                path.rewind(); path.moveTo(3f, 9f); path.lineTo(7f, 9f); path.lineTo(12f, 4.5f); path.lineTo(12f, 19.5f)
                path.lineTo(7f, 15f); path.lineTo(3f, 15f); path.close(); canvas.drawPath(path, paint)
                oval.set(10f, 7f, 18f, 17f); canvas.drawArc(oval, -50f, 100f, false, paint)
                oval.set(9f, 3.5f, 22f, 20.5f); canvas.drawArc(oval, -50f, 100f, false, paint)
            }
            Symbol.POWER -> {
                oval.set(3.5f, 4f, 20.5f, 21f); canvas.drawArc(oval, -50f, 280f, false, paint)
                canvas.drawLine(12f, 2.5f, 12f, 11f, paint)
            }
            Symbol.SHIELD -> {
                path.rewind(); path.moveTo(12f, 2.5f); path.lineTo(20f, 5.5f); path.lineTo(20f, 11.5f)
                path.cubicTo(20f, 16.5f, 16.5f, 19.8f, 12f, 21.5f); path.cubicTo(7.5f, 19.8f, 4f, 16.5f, 4f, 11.5f)
                path.lineTo(4f, 5.5f); path.close(); canvas.drawPath(path, paint)
                line(canvas, 8.5f, 12f, 11f, 14.5f, 15.8f, 9.5f)
            }
            Symbol.REPORT -> {
                path.rewind(); path.moveTo(5f, 2.5f); path.lineTo(14.5f, 2.5f); path.lineTo(19f, 7f)
                path.lineTo(19f, 21.5f); path.lineTo(5f, 21.5f); path.close(); canvas.drawPath(path, paint)
                line(canvas, 14.5f, 2.5f, 14.5f, 7f, 19f, 7f)
                canvas.drawLine(8.5f, 12f, 15.5f, 12f, paint); canvas.drawLine(8.5f, 16f, 15.5f, 16f, paint)
            }
            Symbol.PLAYLIST -> {
                for (y in listOf(6f, 11f, 16f)) canvas.drawLine(3f, y, 14f, y, paint)
                path.rewind(); path.moveTo(16.5f, 13f); path.lineTo(21.5f, 16f); path.lineTo(16.5f, 19f); path.close()
                canvas.drawPath(path, paint)
            }
            Symbol.LAYERS -> {
                line(canvas, 12f, 3f, 21.5f, 8f, 12f, 13f, 2.5f, 8f, 12f, 3f)
                line(canvas, 2.5f, 12f, 12f, 17f, 21.5f, 12f)
                line(canvas, 2.5f, 16f, 12f, 21f, 21.5f, 16f)
            }
            Symbol.SUN -> {
                canvas.drawCircle(12f, 12f, 4.5f, paint)
                for (i in 0 until 8) {
                    val a = Math.PI * i / 4
                    canvas.drawLine(12f + cos(a).toFloat() * 7.5f, 12f + sin(a).toFloat() * 7.5f,
                        12f + cos(a).toFloat() * 10f, 12f + sin(a).toFloat() * 10f, paint)
                }
            }
            Symbol.FIT -> {
                canvas.drawRoundRect(2.5f, 4.5f, 21.5f, 19.5f, 1.5f, 1.5f, paint)
                line(canvas, 6f, 10.5f, 6f, 8f, 8.5f, 8f); line(canvas, 18f, 13.5f, 18f, 16f, 15.5f, 16f)
            }
            Symbol.QUALITY -> {
                canvas.drawRoundRect(2.5f, 4.5f, 21.5f, 19.5f, 1.5f, 1.5f, paint)
                line(canvas, 6.5f, 9f, 6.5f, 15f); line(canvas, 10.5f, 9f, 10.5f, 15f); canvas.drawLine(6.5f, 12f, 10.5f, 12f, paint)
                path.rewind(); path.moveTo(13.5f, 9f); path.lineTo(15.5f, 9f)
                path.cubicTo(18.3f, 9f, 18.3f, 15f, 15.5f, 15f); path.lineTo(13.5f, 15f); path.close(); canvas.drawPath(path, paint)
            }
            Symbol.TRANSITION -> {
                canvas.drawRoundRect(2.5f, 6.5f, 13.5f, 17.5f, 1.5f, 1.5f, paint)
                line(canvas, 15.5f, 6.5f, 21.5f, 6.5f, 21.5f, 17.5f, 15.5f, 17.5f)
            }
            Symbol.HOME -> {
                line(canvas, 3f, 11f, 12f, 3.5f, 21f, 11f)
                line(canvas, 5.5f, 9.5f, 5.5f, 20.5f, 18.5f, 20.5f, 18.5f, 9.5f)
            }
            Symbol.TEXT_SIZE -> {
                line(canvas, 3f, 19f, 8.5f, 5f, 14f, 19f); canvas.drawLine(5f, 14f, 12f, 14f, paint)
                line(canvas, 15f, 19f, 18f, 11f, 21f, 19f)
            }
            Symbol.OPACITY -> {
                canvas.drawCircle(12f, 12f, 9.5f, paint)
                path.rewind(); path.moveTo(12f, 2.5f); path.arcTo(2.5f, 2.5f, 21.5f, 21.5f, -90f, 180f, false)
                path.close(); paint.style = Paint.Style.FILL; canvas.drawPath(path, paint); paint.style = Paint.Style.STROKE
            }
            Symbol.WIND -> {
                path.rewind(); path.moveTo(2.5f, 9f); path.lineTo(15f, 9f)
                path.cubicTo(18.5f, 9f, 18.5f, 4f, 15.5f, 4f); path.cubicTo(14f, 4f, 13f, 5f, 13f, 6.2f)
                canvas.drawPath(path, paint)
                path.rewind(); path.moveTo(2.5f, 14f); path.lineTo(19f, 14f)
                path.cubicTo(22.5f, 14f, 22.5f, 19.5f, 19f, 19.5f); path.cubicTo(17.5f, 19.5f, 16.5f, 18.5f, 16.5f, 17.3f)
                canvas.drawPath(path, paint)
                canvas.drawLine(2.5f, 18.5f, 11f, 18.5f, paint)
            }
            Symbol.PIN -> {
                path.rewind(); path.moveTo(12f, 21.5f); path.cubicTo(6.5f, 15.5f, 4.5f, 12.3f, 4.5f, 9.5f)
                path.cubicTo(4.5f, 5.4f, 7.8f, 2.5f, 12f, 2.5f); path.cubicTo(16.2f, 2.5f, 19.5f, 5.4f, 19.5f, 9.5f)
                path.cubicTo(19.5f, 12.3f, 17.5f, 15.5f, 12f, 21.5f); path.close(); canvas.drawPath(path, paint)
                canvas.drawCircle(12f, 9.5f, 2.7f, paint)
            }
        }
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha; invalidateSelf() }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
