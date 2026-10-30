package com.openvista.prototype.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * The single indeterminate activity mark (issue #34): a thin cyan arc on a faint ring.
 * It animates only while it is attached and shown, so a hidden indicator costs nothing.
 */
class TvLoading(context: Context) : View(context) {
    private val arc = LoadingDrawable(context)

    init {
        background = arc
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) arc.start() else arc.stop()
    }

    override fun onDetachedFromWindow() { arc.stop(); super.onDetachedFromWindow() }
}

/** Drawable form of [TvLoading], for compound drawables on feedback messages. */
class LoadingDrawable(context: Context, private val sizeDp: Float = 14f) : Drawable(), Animatable {
    private val density = context.resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 1.8f * density
    }
    private val box = RectF()
    private var angle = 0f
    private val animator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 900L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { angle = it.animatedValue as Float; invalidateSelf() }
    }

    override fun draw(canvas: Canvas) {
        val inset = paint.strokeWidth / 2 + density
        val side = minOf(bounds.width(), bounds.height()) - 2 * inset
        box.set(bounds.exactCenterX() - side / 2, bounds.exactCenterY() - side / 2,
            bounds.exactCenterX() + side / 2, bounds.exactCenterY() + side / 2)
        paint.color = 0x2EFFFFFF
        canvas.drawOval(box, paint)
        paint.color = TvDesign.accent
        canvas.drawArc(box, angle - 90f, 100f, false, paint)
    }

    override fun getIntrinsicWidth() = (sizeDp * density).toInt()
    override fun getIntrinsicHeight() = (sizeDp * density).toInt()
    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(filter: android.graphics.ColorFilter?) { paint.colorFilter = filter }
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = android.graphics.PixelFormat.TRANSLUCENT
    override fun start() { if (!animator.isStarted) animator.start() }
    override fun stop() { animator.cancel() }
    override fun isRunning() = animator.isRunning
}
