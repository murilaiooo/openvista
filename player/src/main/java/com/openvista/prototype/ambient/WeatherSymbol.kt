package com.openvista.prototype.ambient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import com.openvista.prototype.ui.TvDesign
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/** Original monochrome vector marks; unknown conditions use a neutral dash, never a fake sun. */
internal class WeatherSymbol(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = TvDesign.text
    }
    private val cloud = Path().apply {
        moveTo(8f, 21f); cubicTo(1f, 21f, 2f, 12f, 8f, 13f)
        cubicTo(8f, 5f, 20f, 5f, 21f, 13f)
        cubicTo(29f, 11f, 31f, 21f, 23f, 21f); close()
    }
    private var kind = Kind.UNKNOWN
    private enum class Kind { UNKNOWN, CLEAR, PARTLY, CLOUD, RAIN, SNOW, STORM, FOG }

    init { TvDesign.decorative(this) }

    fun bind(code: Int?, description: String?) {
        val next = when (code) {
            0, 1 -> Kind.CLEAR
            2 -> Kind.PARTLY
            3 -> Kind.CLOUD
            45, 48 -> Kind.FOG
            in 51..67, in 80..82 -> Kind.RAIN
            in 71..77, 85, 86 -> Kind.SNOW
            95, 96, 99 -> Kind.STORM
            else -> {
                val label = description.orEmpty().lowercase(Locale.US)
                when {
                    "thunder" in label -> Kind.STORM
                    "snow" in label || "sleet" in label || "ice" in label -> Kind.SNOW
                    "rain" in label || "shower" in label || "drizzle" in label -> Kind.RAIN
                    "fog" in label || "haze" in label || "smoke" in label -> Kind.FOG
                    "partly" in label || "mostly sunny" in label -> Kind.PARTLY
                    "cloud" in label || "overcast" in label -> Kind.CLOUD
                    label in setOf("sunny", "clear", "mostly clear", "fair") -> Kind.CLEAR
                    else -> Kind.UNKNOWN
                }
            }
        }
        if (kind != next) { kind = next; invalidate() }
    }

    fun tint(color: Int) { if (paint.color != color) { paint.color = color; invalidate() } }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        val scale = minOf(width, height) / 32f
        canvas.translate((width - scale * 32) / 2, (height - scale * 32) / 2)
        canvas.scale(scale, scale)
        if (kind == Kind.UNKNOWN) canvas.drawLine(11f, 16f, 21f, 16f, paint)
        if (kind == Kind.CLEAR || kind == Kind.PARTLY) {
            val cx = if (kind == Kind.PARTLY) 12f else 16f
            val cy = if (kind == Kind.PARTLY) 11f else 16f
            canvas.drawCircle(cx, cy, 5f, paint)
            for (i in 0 until 8) {
                val a = Math.PI * i / 4
                canvas.drawLine(cx + cos(a).toFloat() * 8, cy + sin(a).toFloat() * 8,
                    cx + cos(a).toFloat() * 10, cy + sin(a).toFloat() * 10, paint)
            }
        }
        if (kind in setOf(Kind.PARTLY, Kind.CLOUD, Kind.RAIN, Kind.SNOW, Kind.STORM)) canvas.drawPath(cloud, paint)
        if (kind == Kind.RAIN) for (x in listOf(10f, 16f, 22f)) canvas.drawLine(x, 25f, x - 2, 29f, paint)
        if (kind == Kind.SNOW) for (x in listOf(10f, 20f)) {
            canvas.drawLine(x, 24f, x, 30f, paint)
            canvas.drawLine(x - 3, 25f, x + 3, 29f, paint)
            canvas.drawLine(x + 3, 25f, x - 3, 29f, paint)
        }
        if (kind == Kind.STORM) {
            canvas.drawLine(18f, 22f, 14f, 27f, paint)
            canvas.drawLine(14f, 27f, 20f, 27f, paint)
            canvas.drawLine(20f, 27f, 16f, 32f, paint)
        }
        if (kind == Kind.FOG) for (y in listOf(10f, 16f, 22f)) canvas.drawLine(6f, y, 26f, y, paint)
        canvas.restore()
    }
}
