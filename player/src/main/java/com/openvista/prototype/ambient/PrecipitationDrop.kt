package com.openvista.prototype.ambient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/** Small original cyan precipitation glyph used beside actual probability data. */
internal class PrecipitationDrop(context: Context) : View(context) {
    private val brush = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF77CFF5.toInt()
    }
    private val shape = Path().apply {
        moveTo(12f, 0f)
        cubicTo(8f, 7f, 3f, 13f, 3f, 18f)
        cubicTo(3f, 30f, 21f, 30f, 21f, 18f)
        cubicTo(21f, 13f, 16f, 7f, 12f, 0f)
        close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.scale(width / 24f, height / 28f)
        canvas.drawPath(shape, brush)
        canvas.restore()
    }
}
