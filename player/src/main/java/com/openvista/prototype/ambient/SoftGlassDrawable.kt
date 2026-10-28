package com.openvista.prototype.ambient

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * Feathered glass-like contrast haze. No card, stroke, or hard rectangular edge.
 *
 * Android 9 / Fire OS 7 cannot blur the live SurfaceView through an ordinary overlay
 * without repeated frame readback. This GPU-drawn radial softness stays transparent,
 * doesn't copy video, and moves with its text group. It is not a video-frame blur.
 */
internal class SoftGlassDrawable : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var alphaValue = 255

    override fun draw(canvas: Canvas) {
        val area = bounds
        if (area.isEmpty) return
        val w = area.width().toFloat()
        val h = area.height().toFloat()
        val radius = 1f
        paint.shader = RadialGradient(0f, 0f, radius,
            intArrayOf(0x6809121D, 0x390C1522, 0x100C1522, 0x000C1522),
            floatArrayOf(0f, .38f, .71f, 1f), Shader.TileMode.CLAMP)
        paint.alpha = alphaValue
        val checkpoint = canvas.save()
        canvas.translate(area.left + w / 2f, area.top + h / 2f)
        canvas.scale(w * .57f, h * .68f)
        canvas.drawCircle(0f, 0f, radius, paint)
        canvas.restoreToCount(checkpoint)
        paint.shader = null
    }

    override fun setAlpha(alpha: Int) { alphaValue = alpha.coerceIn(0, 255); invalidateSelf() }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
    @Suppress("DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
