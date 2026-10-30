package com.openvista.prototype.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.openvista.prototype.R
import com.openvista.prototype.library.Availability
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.playback.NetworkMonitor
import java.util.Locale

/**
 * Muted live preview of the focused saved camera (issue #33).
 *
 * It owns at most one small decoder: focus changes are debounced, every new selection stops the
 * previous load before preparing the next one, and the player is released whenever the pane
 * leaves the window or the window is hidden, so it never competes with full-screen playback or
 * the screensaver. Stream URLs are never shown or logged.
 */
@SuppressLint("ViewConstructor")
@UnstableApi
class CameraPreviewPane(context: Context, private val ui: TvUi, private val describe: (LibraryItem) -> Availability)
    : LinearLayout(context) {
    enum class State { IDLE, LOADING, PLAYING, UNAVAILABLE, OFFLINE, UNSUPPORTED }

    private val main = Handler(Looper.getMainLooper())
    private val network = NetworkMonitor(context) { online -> if (online && state == State.OFFLINE) reload() }
    private var player: ExoPlayer? = null
    private var item: LibraryItem? = null
    private var generation = 0
    var state = State.IDLE
        private set

    private val video = PlayerView(context).apply {
        useController = false
        setKeepContentOnPlayerReset(false)
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        setShutterBackgroundColor(Color.BLACK)
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    private val spinner = TvLoading(context)
    private val placeholderIcon = View(context).apply {
        background = TvIcon(context, TvIcon.Symbol.CAMERA, 26f, 0x99C8D3DB.toInt(), 1.3f)
    }
    private val status = ui.text("", 10f, TvDesign.muted).apply {
        gravity = Gravity.CENTER
        maxLines = 3
        ellipsize = TextUtils.TruncateAt.END
        accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
    }
    private val placeholder = LinearLayout(context).apply {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(0xF2061421.toInt())
        val pad = ui.dp(18)
        setPadding(pad, pad, pad, pad)
        addView(placeholderIcon, LayoutParams(ui.dp(26), ui.dp(26)).apply { bottomMargin = ui.dp(8) })
        addView(spinner, LayoutParams(ui.dp(22), ui.dp(22)).apply { bottomMargin = ui.dp(8) })
        addView(status, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
    private val badge = ui.caps(context.getString(R.string.preview_muted), TvDesign.text).apply {
        setPadding(ui.dp(7), ui.dp(3), ui.dp(7), ui.dp(3))
        background = TvDesign.panel(context, 0x99061421.toInt(), TvDesign.glassStroke, radiusDp = 3f)
        visibility = View.GONE
    }
    private val title = ui.text("", 12.5f).apply {
        typeface = TvDesign.medium
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    private val place = ui.text("", 9.5f, TvDesign.muted).apply { maxLines = 2; ellipsize = TextUtils.TruncateAt.END }
    private val availability = ui.text("", 9f, TvDesign.faint)
    private val actions = ui.row()
    private val retry = ui.button(actions, context.getString(R.string.preview_retry), "preview_retry") { reload() }
        .apply { visibility = View.GONE }
    val details: View = ui.button(actions, context.getString(R.string.preview_details), "preview_details") { item?.let(onDetails) }
    var onDetails: (LibraryItem) -> Unit = {}

    private val loadNext = Runnable { prepare() }
    private val timeout = Runnable { fail(if (network.isOnline()) State.UNAVAILABLE else State.OFFLINE) }

    private val listener = object : Player.Listener {
        override fun onRenderedFirstFrame() { render(State.PLAYING) }

        override fun onPlayerError(error: PlaybackException) {
            fail(when {
                !network.isOnline() -> State.OFFLINE
                error.errorCode in PlaybackException.ERROR_CODE_DECODER_INIT_FAILED..PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED ||
                    error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ||
                    error.errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED -> State.UNSUPPORTED
                else -> State.UNAVAILABLE
            })
        }
    }

    init {
        orientation = VERTICAL
        val frame = object : FrameLayout(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val width = MeasureSpec.getSize(widthMeasureSpec)
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(width * 9 / 16, MeasureSpec.EXACTLY))
            }
        }.apply {
            setBackgroundColor(Color.BLACK)
            addView(video, FrameLayout.LayoutParams(-1, -1))
            addView(placeholder, FrameLayout.LayoutParams(-1, -1))
            addView(badge, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END).apply { setMargins(ui.dp(8), ui.dp(8), ui.dp(8), ui.dp(8)) })
        }
        addView(frame, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(title, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = ui.dp(10) })
        addView(place)
        addView(availability)
        addView(actions, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = ui.dp(6) })
        render(State.IDLE)
    }

    /** Shows [next] after a short pause, so scrolling quickly through the list never opens a stream. */
    fun show(next: LibraryItem, anchor: View) {
        // Left from the preview actions returns to the row being previewed, not the nearest one.
        retry.nextFocusLeftId = anchor.id
        details.nextFocusLeftId = anchor.id
        if (next.id == item?.id && state != State.IDLE) return
        item = next
        generation++
        halt()
        title.text = next.title
        place.text = listOf(next.city, next.country, next.category).filter(String::isNotBlank).joinToString(" · ")
        place.visibility = if (place.text.isBlank()) View.GONE else View.VISIBLE
        availability.text = when (describe(next)) {
            Availability.AVAILABLE -> context.getString(R.string.available)
            Availability.UNAVAILABLE -> context.getString(R.string.unavailable)
            else -> ""
        }
        availability.visibility = if (availability.text.isBlank()) View.GONE else View.VISIBLE
        render(State.LOADING)
        main.postDelayed(loadNext, FOCUS_DEBOUNCE_MS)
    }

    private fun reload() { item?.let { generation++; halt(); render(State.LOADING); main.postDelayed(loadNext, 0) } }

    private fun prepare() {
        val target = item ?: return
        if (!isAttachedToWindow || windowVisibility != View.VISIBLE) return
        if (!network.isOnline()) { fail(State.OFFLINE); return }
        val current = player ?: create().also { player = it }
        val media = MediaItem.Builder().setMediaId(target.id).setUri(target.uri)
        when (target.streamKind?.uppercase(Locale.ROOT)) {
            "DASH" -> media.setMimeType(MimeTypes.APPLICATION_MPD)
            "HLS" -> media.setMimeType(MimeTypes.APPLICATION_M3U8)
        }
        current.setMediaItem(media.build())
        current.prepare()
        current.playWhenReady = true
        main.postDelayed(timeout, LOAD_TIMEOUT_MS)
    }

    private fun create(): ExoPlayer {
        val tracks = DefaultTrackSelector(context).apply {
            // A preview needs no more than 720p; this keeps the decoder budget small on Fire OS 7.
            parameters = parameters.buildUpon().setMaxVideoSize(1280, 720)
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true).build()
        }
        return ExoPlayer.Builder(context).setTrackSelector(tracks).build().also {
            it.volume = 0f
            it.repeatMode = Player.REPEAT_MODE_ONE
            it.addListener(listener)
            video.player = it
        }
    }

    private fun fail(next: State) {
        main.removeCallbacks(timeout)
        player?.stop()
        render(next)
    }

    /** Cancels a pending or running load; the decoder stays allocated for the next selection. */
    private fun halt() {
        main.removeCallbacks(loadNext)
        main.removeCallbacks(timeout)
        player?.run { stop(); clearMediaItems() }
    }

    fun release() {
        halt()
        player?.let { video.player = null; it.removeListener(listener); it.release() }
        player = null
        if (item != null && state != State.IDLE) render(State.LOADING)
    }

    private fun render(next: State) {
        state = next
        if (next == State.PLAYING) main.removeCallbacks(timeout)
        placeholder.visibility = if (next == State.PLAYING) View.GONE else View.VISIBLE
        spinner.visibility = if (next == State.LOADING) View.VISIBLE else View.GONE
        placeholderIcon.visibility = if (next == State.LOADING) View.GONE else View.VISIBLE
        badge.visibility = if (next == State.PLAYING) View.VISIBLE else View.GONE
        status.text = context.getString(when (next) {
            State.IDLE -> R.string.preview_idle
            State.LOADING -> R.string.preview_loading
            State.PLAYING -> R.string.preview_hint
            State.UNAVAILABLE -> R.string.preview_unavailable
            State.OFFLINE -> R.string.preview_offline
            State.UNSUPPORTED -> R.string.preview_unsupported
        })
        retry.visibility = if (next in setOf(State.UNAVAILABLE, State.OFFLINE, State.UNSUPPORTED)) View.VISIBLE else View.GONE
        details.visibility = if (item == null) View.GONE else View.VISIBLE
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); network.start() }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != View.VISIBLE) release()
        else if (item != null && player == null && state == State.LOADING) main.postDelayed(loadNext, FOCUS_DEBOUNCE_MS)
    }

    override fun onDetachedFromWindow() {
        release()
        network.stop()
        main.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    private companion object {
        const val FOCUS_DEBOUNCE_MS = 450L
        const val LOAD_TIMEOUT_MS = 15_000L
    }
}
