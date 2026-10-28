package com.openvista.prototype.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.openvista.prototype.R
import com.openvista.prototype.ambient.AmbientOverlay
import com.openvista.prototype.library.LibraryStore
import com.openvista.prototype.playback.PlaybackNotice
import com.openvista.prototype.playback.PlaybackSession

/**
 * The full-screen scene shared by the app player and the screensaver: the video surface,
 * an optional fade-through-black veil, ambient information and playback notices.
 *
 * It never owns a decoder and never covers moving video with a captured frame. The veil only
 * darkens the surface while a new source connects or when nothing can play, so a stale last
 * frame is not left on screen behind a failure.
 */
// Created only in code with its library store; never inflated from XML.
@SuppressLint("ViewConstructor")
@UnstableApi
class SceneView(context: Context, private val store: LibraryStore) : FrameLayout(context) {
    private val main = Handler(Looper.getMainLooper())
    val video: PlayerView = PlayerView(context).apply {
        useController = false
        setKeepContentOnPlayerReset(false)
        resizeMode = if (store.settings().resizeMode == "fit") AspectRatioFrameLayout.RESIZE_MODE_FIT
            else AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        setShutterBackgroundColor(Color.TRANSPARENT)
        isFocusable = true
    }
    val ambient = AmbientOverlay(context, store)
    private val veil = View(context).apply {
        setBackgroundColor(Color.BLACK)
        alpha = 0f
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    private val notice = TextView(context).apply {
        visibility = View.GONE
        gravity = Gravity.CENTER
        textSize = 17f
        typeface = TvDesign.regular
        setTextColor(TvDesign.text)
        setLineSpacing(TvDesign.dp(context, 4).toFloat(), 1f)
        val horizontal = TvDesign.dp(context, 28)
        val vertical = TvDesign.dp(context, 18)
        setPadding(horizontal, vertical, horizontal, vertical)
        background = GradientDrawable().apply {
            setColor(0xD90B1724.toInt())
            cornerRadius = TvDesign.dp(context, 16).toFloat()
            setStroke(TvDesign.dp(context, 1), 0x33FFFFFF)
        }
        accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
    }
    private val hideNotice = Runnable { fadeNotice() }
    private var session: PlaybackSession? = null
    private var frameVisible = false

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // Fade the outgoing scene instead of freezing it or cutting through a decoder gap.
            if (frameVisible && store.settings().smoothTransitions) veilTo(1f, FADE_OUT_MS)
            frameVisible = false
        }

        override fun onRenderedFirstFrame() {
            frameVisible = true
            veilTo(0f, FADE_IN_MS)
        }

        override fun onEvents(player: Player, events: Player.Events) {
            val current = session ?: return
            if (current.isReleased) return
            val active = player.currentMediaItem?.mediaId == current.currentItem?.id
            ambient.updatePlayback(active && player.isCurrentMediaItemLive, active && player.isPlaying,
                player.playbackState == Player.STATE_BUFFERING)
            keepScreenOn = store.settings().keepAwake && player.playWhenReady &&
                player.playbackState in setOf(Player.STATE_READY, Player.STATE_BUFFERING)
        }
    }

    init {
        setBackgroundColor(Color.BLACK)
        addView(video, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(veil, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(ambient, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(notice, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        notice.maxWidth = (MeasureSpec.getSize(widthMeasureSpec) * .56f).toInt()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun attach(created: PlaybackSession) {
        detach()
        session = created
        frameVisible = false
        video.player = created.player
        created.player.addListener(listener)
        ambient.start()
    }

    fun detach() {
        session?.player?.removeListener(listener)
        session = null
        video.player = null
        ambient.stop()
        keepScreenOn = false
        main.removeCallbacksAndMessages(null)
        veil.animate().cancel()
        veil.alpha = 0f
        frameVisible = false
    }

    fun show(value: PlaybackNotice) {
        when (value.kind) {
            PlaybackNotice.Kind.QUIET -> {
                if (value.storageError) message(context.getString(R.string.playback_storage_error), persistent = false)
                else clearMessage()
            }
            PlaybackNotice.Kind.BRIEF, PlaybackNotice.Kind.PERSISTENT -> {
                val text = context.getString(value.message) +
                    if (value.storageError) "\n" + context.getString(R.string.playback_storage_error) else ""
                message(text, value.kind == PlaybackNotice.Kind.PERSISTENT)
            }
        }
    }

    /** A persistent message also darkens the surface, so no frozen frame is left behind it. */
    fun message(text: String, persistent: Boolean) {
        main.removeCallbacks(hideNotice)
        notice.animate().cancel()
        notice.text = text
        notice.alpha = 1f
        notice.visibility = View.VISIBLE
        if (persistent) {
            frameVisible = false
            veilTo(1f, PERSISTENT_FADE_MS)
        } else main.postDelayed(hideNotice, BRIEF_NOTICE_MS)
    }

    fun clearMessage() {
        main.removeCallbacks(hideNotice)
        if (notice.visibility == View.VISIBLE) fadeNotice()
    }

    private fun fadeNotice() {
        notice.animate().cancel()
        notice.animate().alpha(0f).setDuration(NOTICE_FADE_MS).withEndAction { notice.visibility = View.GONE }.start()
    }

    private fun veilTo(target: Float, duration: Long) {
        veil.animate().cancel()
        veil.animate().alpha(target).setDuration(duration).start()
    }

    override fun onDetachedFromWindow() {
        main.removeCallbacksAndMessages(null)
        veil.animate().cancel()
        notice.animate().cancel()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val FADE_OUT_MS = 280L
        const val FADE_IN_MS = 520L
        const val PERSISTENT_FADE_MS = 600L
        const val NOTICE_FADE_MS = 200L
        const val BRIEF_NOTICE_MS = 4_000L
    }
}
