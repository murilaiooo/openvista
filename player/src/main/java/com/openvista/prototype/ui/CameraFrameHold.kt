package com.openvista.prototype.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

/**
 * One low-resolution, in-memory snapshot of the previous camera's actual rendered frame.
 * Covers the SurfaceView only while the next stream prepares. Never starts a second
 * decoder, persists footage, captures continuously or touches user media.
 */
@UnstableApi
internal class CameraFrameHold(
    private val playerView: PlayerView,
    private val cover: ImageView,
) {
    private val main = Handler(Looper.getMainLooper())
    private var lastFrame: Bitmap? = null
    private var generation = 0
    private var released = false
    private val stale = Runnable { hide(immediate = true) }

    fun changingCamera() {
        if (released) return
        generation++
        main.removeCallbacksAndMessages(null)
        cover.animate().cancel()
        val image = lastFrame
        if (image != null && !image.isRecycled) {
            cover.setImageBitmap(image)
            cover.alpha = 1f
            cover.visibility = View.VISIBLE
            // A stalled/offline source must not leave a stale still on TV indefinitely.
            main.postDelayed(stale, 12_000L)
        } else hide(immediate = true)
    }

    fun firstFrame() {
        if (released) return
        val expected = ++generation
        hide(immediate = false)
        // SurfaceView may not have a copyable buffer in the same callback as its first frame.
        main.postDelayed({
            if (expected == generation && !released) capture(expected)
        }, 650L)
    }

    private fun capture(expected: Int) {
        if (Build.VERSION.SDK_INT < 26) return
        val surface = playerView.videoSurfaceView as? SurfaceView ?: return
        if (!surface.holder.surface.isValid || surface.width <= 0 || surface.height <= 0) return
        val width = 640
        val height = (width.toFloat() * surface.height / surface.width).toInt().coerceIn(180, 480)
        val copy = try { Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) }
            catch (_: OutOfMemoryError) { return }
        try {
            PixelCopy.request(surface, copy, { result ->
                if (result == PixelCopy.SUCCESS && expected == generation && !released) {
                    val previous = lastFrame
                    lastFrame = copy
                    if (previous !== copy && cover.visibility != View.VISIBLE) previous?.recycle()
                } else copy.recycle()
            }, main)
        } catch (_: RuntimeException) {
            copy.recycle()
        }
    }

    private fun hide(immediate: Boolean) {
        main.removeCallbacks(stale)
        cover.animate().cancel()
        if (immediate || cover.visibility != View.VISIBLE) {
            cover.alpha = 0f
            cover.visibility = View.GONE
            cover.setImageDrawable(null)
        } else {
            cover.animate().alpha(0f).setDuration(180L).withEndAction {
                cover.visibility = View.GONE
                cover.setImageDrawable(null)
            }.start()
        }
    }

    fun release() {
        released = true
        generation++
        main.removeCallbacksAndMessages(null)
        hide(immediate = true)
        lastFrame?.recycle()
        lastFrame = null
    }
}
