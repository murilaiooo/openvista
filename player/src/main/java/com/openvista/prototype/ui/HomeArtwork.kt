package com.openvista.prototype.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import com.openvista.prototype.R
import kotlin.math.max

/**
 * Renders the three decorative Home backdrops once, off the main thread, at a bounded size.
 * Panel one grades the bundled photograph; panels two and three are original procedural art.
 * None of it represents a camera or a user's media.
 */
internal object HomeArtwork {
    /** Upper bound per panel; gradients and soft photography upscale without visible loss. */
    private const val MAX_HEIGHT = 1080

    fun render(resources: Resources, panel: Int, width: Int, height: Int): Bitmap? {
        if (width <= 0 || height <= 0) return null
        val scale = minOf(1f, MAX_HEIGHT.toFloat() / height)
        val w = max(1, (width * scale).toInt())
        val h = max(1, (height * scale).toInt())
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        when (panel) {
            0 -> photograph(resources, canvas, w, h)
            1 -> ribbon(canvas, w.toFloat(), h.toFloat())
            else -> dunes(canvas, w.toFloat(), h.toFloat())
        }
        return bitmap
    }

    private fun photograph(resources: Resources, canvas: Canvas, w: Int, h: Int) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, R.drawable.home_scenery, bounds)
        var sample = 1
        while (bounds.outHeight / (sample * 2) >= h && bounds.outWidth / 3 / (sample * 2) >= w / 2) sample *= 2
        val source = BitmapFactory.decodeResource(resources, R.drawable.home_scenery,
            BitmapFactory.Options().apply { inSampleSize = sample }) ?: return
        val columnWidth = source.width / 3
        val crop = max(width = columnWidth, height = source.height, targetW = w, targetH = h)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            // A cooler, quieter grade than the source sunset keeps the copy legible.
            val grade = ColorMatrix().apply { setSaturation(.5f) }
            grade.postConcat(ColorMatrix(floatArrayOf(
                .86f, 0f, 0f, 0f, 4f,
                0f, .92f, 0f, 0f, 8f,
                0f, 0f, .94f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f,
            )))
            colorFilter = ColorMatrixColorFilter(grade)
        }
        canvas.drawBitmap(source, Rect(crop.left, crop.top, crop.right, crop.bottom), Rect(0, 0, w, h), paint)
        source.recycle()
    }

    /** Center crop of the first third of the triptych into the target aspect ratio. */
    private fun max(width: Int, height: Int, targetW: Int, targetH: Int): Rect {
        val target = targetW.toFloat() / targetH
        return if (width.toFloat() / height > target) {
            val cropped = (height * target).toInt()
            Rect((width - cropped) / 2, 0, (width + cropped) / 2, height)
        } else {
            val cropped = (width / target).toInt()
            Rect(0, (height - cropped) / 2, width, (height + cropped) / 2)
        }
    }

    private fun ribbon(canvas: Canvas, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(0f, 0f, 0f, h,
            intArrayOf(0xFF0A2C3A.toInt(), 0xFF0C4458.toInt(), 0xFF0D3A3A.toInt(), 0xFF0B2A1E.toInt(), 0xFF06130F.toInt()),
            floatArrayOf(0f, .3f, .55f, .78f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        glow(canvas, paint, .72f * w, .28f * h, .75f * w, 0x992A9CC8.toInt())
        glow(canvas, paint, .25f * w, .78f * h, .55f * h, 0x6627633C)
        paint.shader = null

        val curve = Path().apply {
            moveTo(.66f * w, -.03f * h)
            cubicTo(.97f * w, .07f * h, 1.04f * w, .21f * h, .80f * w, .33f * h)
            cubicTo(.58f * w, .44f * h, .16f * w, .50f * h, -.06f * w, .70f * h)
        }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
        fun layer(width: Float, color: Int, blur: Float, dx: Float = 0f) {
            stroke.strokeWidth = width
            stroke.color = color
            stroke.maskFilter = if (blur > 0f) BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL) else null
            canvas.save(); canvas.translate(dx, 0f); canvas.drawPath(curve, stroke); canvas.restore()
        }
        layer(.24f * w, 0x2A2A9CC8, .10f * w, -.03f * w)
        layer(.12f * w, 0x44F0C27A, .06f * w)
        layer(.075f * w, 0x885BC0E6.toInt(), .02f * w, -.025f * w)
        layer(.048f * w, 0xB0E9BC78.toInt(), .01f * w, .006f * w)
        layer(.022f * w, 0xCCF6DDA8.toInt(), .004f * w)
        layer(.007f * w, 0x99FFF6E4.toInt(), .001f * w)
        // A low vignette keeps the lower half calm behind copy and the call to action.
        paint.shader = LinearGradient(0f, .45f * h, 0f, h, 0x00000000, 0x99030A08.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, .45f * h, w, h, paint)
    }

    private fun dunes(canvas: Canvas, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(0f, 0f, 0f, h,
            intArrayOf(0xFF241C1A.toInt(), 0xFF3E2F28.toInt(), 0xFF6E5040.toInt(), 0xFF5A4134.toInt(), 0xFF2A1F1A.toInt()),
            floatArrayOf(0f, .18f, .4f, .6f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        glow(canvas, paint, .62f * w, .36f * h, .8f * w, 0x77B9876A)

        data class Dune(val y0: Float, val c1x: Float, val c1y: Float, val c2x: Float, val c2y: Float, val y1: Float,
                        val top: Int, val bottom: Int)
        val layers = listOf(
            Dune(.40f, .30f, .30f, .62f, .19f, .24f, 0xFFA97D64.toInt(), 0xFF5E4437.toInt()),
            Dune(.55f, .25f, .50f, .62f, .44f, .50f, 0xFF9A715A.toInt(), 0xFF4F392D.toInt()),
            Dune(.64f, .32f, .58f, .64f, .60f, .57f, 0xFF845F4B.toInt(), 0xFF412F26.toInt()),
            Dune(.76f, .40f, .70f, .72f, .73f, .68f, 0xFF6B4D3D.toInt(), 0xFF31241E.toInt()),
            Dune(.90f, .38f, .84f, .74f, .87f, .80f, 0xFF513B30.toInt(), 0xFF221915.toInt()),
        )
        val ridge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = .012f * w
            maskFilter = BlurMaskFilter(.01f * w, BlurMaskFilter.Blur.NORMAL)
        }
        layers.forEach { dune ->
            val crest = Path().apply {
                moveTo(-.02f * w, dune.y0 * h)
                cubicTo(dune.c1x * w, dune.c1y * h, dune.c2x * w, dune.c2y * h, 1.02f * w, dune.y1 * h)
            }
            val body = Path(crest).apply { lineTo(1.02f * w, h); lineTo(-.02f * w, h); close() }
            val crestTop = minOf(dune.y0, dune.y1, dune.c1y, dune.c2y) * h
            paint.shader = LinearGradient(0f, crestTop, 0f, crestTop + .16f * h, dune.top, dune.bottom, Shader.TileMode.CLAMP)
            canvas.drawPath(body, paint)
            ridge.color = 0x40F2CFB4
            canvas.drawPath(crest, ridge)
        }
        paint.shader = LinearGradient(0f, .5f * h, 0f, h, 0x00000000, 0x80120C0A.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, .5f * h, w, h, paint)
    }

    private fun glow(canvas: Canvas, paint: Paint, x: Float, y: Float, radius: Float, color: Int) {
        paint.shader = RadialGradient(x, y, radius, color, color and 0x00FFFFFF, Shader.TileMode.CLAMP)
        canvas.drawCircle(x, y, radius, paint)
    }
}
