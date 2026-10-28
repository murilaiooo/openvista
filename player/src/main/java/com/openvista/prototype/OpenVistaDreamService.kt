package com.openvista.prototype

import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Build
import android.os.PowerManager
import android.service.dreams.DreamService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import com.openvista.prototype.library.LibraryStorageException
import com.openvista.prototype.library.MediaFamily
import com.openvista.prototype.playback.PlaybackSession
import com.openvista.prototype.ui.AppLocale
import com.openvista.prototype.ui.SceneView
import com.openvista.prototype.ui.TvUi
import java.util.Calendar
import java.util.Locale

/** Optional OS-selected screensaver. It never changes the device's dream or power settings. */
@UnstableApi
class OpenVistaDreamService : DreamService() {
    private val handler = Handler(Looper.getMainLooper())
    private var store: LibraryStore? = null
    private var session: PlaybackSession? = null
    private var scene: SceneView? = null
    private val overlay get() = scene?.ambient
    private var controls: LinearLayout? = null
    private var firstControl: View? = null
    private var ui: Context? = null
    private var attached = false
    private var dreaming = false
    private var loadingGeneration = 0
    private var startupFailed = false
    private var screenReceiverRegistered = false
    private val screenOff = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_SCREEN_OFF) return
            dreaming = false
            stopPlayback()
            handler.removeCallbacks(scheduleTick)
            handler.removeCallbacks(hideControls)
            finish()
        }
    }
    private val hideControls = Runnable {
        controls?.visibility = View.GONE
        overlay?.setControlsVisible(false)
        scene?.video?.requestFocus()
    }
    private val scheduleTick = object : Runnable {
        override fun run() {
            if (!dreaming || !attached) return
            updateSchedule()
            if (dreaming && attached) handler.postDelayed(this, 30_000)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attached = true
        startupFailed = false
        setInteractive(true)
        setFullscreen(true)
        setScreenBright(false)
        registerScreenOff()
        if (startupFailed) { finish(); return }
        val generation = ++loadingGeneration
        // The library can contain thousands of records; never restore it on the input thread.
        Thread {
            val loaded = try {
                LibraryStore.get(applicationContext)
            } catch (_: LibraryStorageException) {
                handler.post {
                    if (attached && generation == loadingGeneration) showLibraryError()
                }
                return@Thread
            }
            handler.post {
                if (!attached || generation != loadingGeneration) return@post
                store = loaded
                buildScreen(loaded)
                if (dreaming) updateSchedule()
            }
        }.start()
    }

    private fun showLibraryError() {
        val context = AppLocale.wrap(this)
        ui = context
        val message = TextView(context).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(dp(40), dp(40), dp(40), dp(40))
            setText(R.string.dream_library_error)
        }
        setContentView(message)
    }

    private fun buildScreen(library: LibraryStore) {
        val context = AppLocale.wrap(this)
        ui = context
        val root = FrameLayout(context).apply { setBackgroundColor(Color.BLACK) }
        scene = SceneView(context, library).also { root.addView(it, FrameLayout.LayoutParams(-1, -1)) }
        controls = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(0xE60C1727.toInt())
            visibility = View.GONE
        }.also { menu ->
            val top = TvUi(context).row().also(menu::addView)
            val bottom = TvUi(context).row().also(menu::addView)
            firstControl = addControl(top, R.string.dream_cameras) { chooseFamily(MediaFamily.CAMERAS) }
            addControl(top, R.string.dream_videos) { chooseFamily(MediaFamily.VIDEOS) }
            addControl(top, R.string.dream_pause) { session?.togglePause() }
            addControl(top, R.string.dream_sound) {
                session?.let { it.setMuted(it.player.volume > 0f) }
            }
            addControl(bottom, R.string.dream_audio) { cycleAudioTrack() }
            addControl(bottom, R.string.dream_retry) {
                if (startupFailed) { startupFailed = false; updateSchedule() } else session?.retry()
            }
            addControl(bottom, R.string.dream_stay_local) { session?.stayLocal() }
            addControl(bottom, R.string.dream_exit) { finish() }
            root.addView(menu, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        }
        setContentView(root)
        scene?.video?.requestFocus()
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        if ((getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isInteractive == false) {
            finish()
            return
        }
        dreaming = true
        handler.removeCallbacks(scheduleTick)
        handler.post(scheduleTick)
    }

    override fun onDreamingStopped() {
        dreaming = false
        stopPlayback()
        handler.removeCallbacks(scheduleTick)
        handler.removeCallbacks(hideControls)
        controls?.visibility = View.GONE
        super.onDreamingStopped()
    }

    private fun updateSchedule() {
        val library = store ?: return
        val settings = library.settings()
        val now = Calendar.getInstance()
        val minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val start = settings.scheduleStartMinute.coerceIn(0, 1439)
        val end = settings.scheduleEndMinute.coerceIn(0, 1439)
        // An equal start/end is an all-day window; overnight windows wrap midnight.
        val inside = !settings.scheduleEnabled || start == end ||
            if (start < end) minute in start until end else minute >= start || minute < end
        if (!inside) {
            dreaming = false
            stopPlayback()
            handler.removeCallbacks(scheduleTick)
            handler.removeCallbacks(hideControls)
            ui?.getString(R.string.dream_schedule_inactive)?.let { scene?.message(it, persistent = true) }
            finish()
            return
        }
        // A manual Activity may have taken decoder ownership while this service was attached.
        if (session?.isReleased == true) {
            stopPlayback()
            dreaming = false
            finish()
            return
        }
        if (session != null || startupFailed) return
        val context = ui ?: return
        val saved = PlaybackSession.lastSnapshot()
        val created = try { PlaybackSession(context, library, { notice ->
            scene?.show(notice)
        }, { item ->
            overlay?.updateItem(item)
        }, {
            scene?.detach()
            if (dreaming && attached) {
                dreaming = false
                handler.removeCallbacks(scheduleTick)
                handler.removeCallbacks(hideControls)
                finish()
            }
        }, dreamMode = true) } catch (_: RuntimeException) {
            startupFailed = true
            scene?.detach()
            scene?.message(context.getString(R.string.playback_engine_unavailable), persistent = true)
            return
        }
        session = created
        scene?.attach(created)
        val last = library.lastItemId()?.let(library::item)
        if (saved != null && saved.itemIds.any { library.item(it) != null }) {
            created.restore(saved)
        } else if (last == null) {
            scene?.message(context.getString(R.string.dream_no_saved_scene), persistent = true)
        } else playSaved(last)
    }

    private fun playSaved(item: LibraryItem) {
        val library = store ?: return
        val playlist = library.playlists().firstOrNull { it.family == item.family && item.id in it.itemIds }
        val items = playlist?.itemIds?.mapNotNull(library::item)
            ?: library.items().filter { it.family == item.family }
        session?.play(items, item.id, playlist?.shuffle ?: (item.family == MediaFamily.CAMERAS))
    }

    private fun chooseFamily(family: MediaFamily) {
        val library = store ?: return
        if (family == MediaFamily.CAMERAS && library.settings().stayLocal) {
            try {
                library.saveSettings(library.settings().copy(stayLocal = false))
            } catch (_: LibraryStorageException) {
                ui?.getString(R.string.playback_storage_error)?.let { scene?.message(it, persistent = false) }
            }
        }
        session?.play(library.items().filter { it.family == family }, shuffle = family == MediaFamily.CAMERAS)
        hideMenu()
    }

    private fun stopPlayback() {
        scene?.detach()
        session?.release()
        session = null
    }

    private fun registerScreenOff() {
        if (screenReceiverRegistered) return
        try {
            if (Build.VERSION.SDK_INT >= 33) registerReceiver(screenOff, IntentFilter(Intent.ACTION_SCREEN_OFF), Context.RECEIVER_NOT_EXPORTED)
            else {
                @Suppress("DEPRECATION")
                registerReceiver(screenOff, IntentFilter(Intent.ACTION_SCREEN_OFF))
            }
            screenReceiverRegistered = true
        } catch (_: RuntimeException) {
            // If the OEM rejects lifecycle observation, leave the ordinary app available.
            startupFailed = true
        }
    }

    private fun unregisterScreenOff() {
        if (!screenReceiverRegistered) return
        screenReceiverRegistered = false
        try { unregisterReceiver(screenOff) } catch (_: IllegalArgumentException) { }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) finish()
            return true
        }
        // Preserve wake and reserved-key handling, including on OEM builds.
        if (event.action != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event)
        }
        overlay?.userInteraction()
        if (controls?.visibility == View.VISIBLE) {
            handler.removeCallbacks(hideControls)
            handler.postDelayed(hideControls, 5_000)
        }
        when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (event.repeatCount == 0) session?.togglePause()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                session?.let { if (!it.player.playWhenReady) it.togglePause() }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                session?.let { if (it.player.playWhenReady) it.togglePause() }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> { if (event.repeatCount == 0) session?.next(); return true }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> { if (event.repeatCount == 0) session?.previous(); return true }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> { session?.seekBy(10_000); return true }
            KeyEvent.KEYCODE_MEDIA_REWIND -> { session?.seekBy(-10_000); return true }
        }
        if (controls?.visibility == View.VISIBLE) {
            return super.dispatchKeyEvent(event)
        }
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (event.repeatCount == 0) showMenu()
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (session?.currentItem?.family == MediaFamily.VIDEOS) session?.seekBy(-10_000)
                else if (event.repeatCount == 0) session?.previous()
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_UP -> {
                if (session?.currentItem?.family == MediaFamily.VIDEOS) session?.seekBy(10_000)
                else if (event.repeatCount == 0) session?.next()
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    private fun showMenu() {
        controls?.visibility = View.VISIBLE
        overlay?.setControlsVisible(true)
        firstControl?.requestFocus()
        handler.removeCallbacks(hideControls)
        handler.postDelayed(hideControls, 5_000)
    }

    private fun hideMenu() {
        handler.removeCallbacks(hideControls)
        controls?.visibility = View.GONE
        overlay?.setControlsVisible(false)
        scene?.video?.requestFocus()
    }

    private fun addControl(menu: LinearLayout, label: Int, click: () -> Unit): View =
        TvUi(menu.context).button(menu, menu.context.getString(label)) {
            click()
            handler.removeCallbacks(hideControls)
            if (controls?.visibility == View.VISIBLE) handler.postDelayed(hideControls, 5_000)
        }

    private fun cycleAudioTrack() {
        val current = session ?: return
        val context = ui ?: return
        val groups = current.player.currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
        val options = groups.flatMap { group ->
            (0 until group.length).filter { group.isTrackSupported(it) }.map { group to it }
        }
        if (options.isEmpty()) {
            scene?.message(context.getString(R.string.dream_no_audio), persistent = false)
        } else {
            val selected = options.indexOfFirst { (group, track) -> group.isTrackSelected(track) }
            val next = (selected + 1) % options.size
            val (group, track) = options[next]
            current.player.trackSelectionParameters = current.player.trackSelectionParameters.buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, track))
                .build()
            val language = group.getTrackFormat(track).language?.let {
                Locale.forLanguageTag(it).getDisplayLanguage(context.resources.configuration.locales[0])
            }?.takeIf { it.isNotBlank() } ?: context.getString(R.string.playback_unknown)
            scene?.message(context.getString(R.string.dream_audio_selected, next + 1, options.size, language), persistent = false)
        }
    }

    override fun onDetachedFromWindow() {
        unregisterScreenOff()
        attached = false
        dreaming = false
        loadingGeneration++
        stopPlayback()
        handler.removeCallbacksAndMessages(null)
        controls = null
        firstControl = null
        scene = null
        ui = null
        store = null
        super.onDetachedFromWindow()
    }

    override fun onDestroy() {
        unregisterScreenOff()
        dreaming = false
        attached = false
        loadingGeneration++
        stopPlayback()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + .5f).toInt()
}
