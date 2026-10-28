package com.openvista.prototype.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import com.openvista.prototype.LibraryRoute
import com.openvista.prototype.OpenVistaActivity
import com.openvista.prototype.R
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import com.openvista.prototype.library.MediaFamily
import com.openvista.prototype.playback.PlaybackSession
import com.openvista.prototype.playback.PlaybackSnapshot

/** Focused controls consume arrows; hidden controls leave contextual arrows to the scene. */
@UnstableApi
class PlayerScreen(private val host: OpenVistaActivity, private val store: LibraryStore) {
    private val ui get() = host.ui
    private val main = Handler(Looper.getMainLooper())
    private var session: PlaybackSession? = null
    private var scene: SceneView? = null
    private val ambient get() = scene?.ambient
    private var menu: LinearLayout? = null
    private var diagnostics: TextView? = null
    private var attached = false
    private var dialog = false
    private val hide = Runnable { if (!dialog) dismissMenu() }
    val isAttached get() = attached && session?.isReleased != true
    val currentItemId get() = session?.currentItem?.id

    fun create(items: List<LibraryItem>, selected: String?, shuffle: Boolean, saved: PlaybackSnapshot? = null): View {
        stop()
        attached = true
        val frame = FrameLayout(host).apply { setBackgroundColor(Color.BLACK) }
        // Loading states stay silent; only failures the viewer can act on are shown.
        val createdScene = SceneView(host, store).also { frame.addView(it, FrameLayout.LayoutParams(-1, -1)) }
        scene = createdScene
        diagnostics = ui.description("").apply {
            setBackgroundColor(0xC0000000.toInt()); visibility = View.GONE
        }.also { frame.addView(it, FrameLayout.LayoutParams(-2, -2, Gravity.CENTER_VERTICAL or Gravity.END)) }
        val panel = ui.column().apply {
            setPadding(ui.dp(24), ui.dp(8), ui.dp(24), ui.dp(12))
            background = TvDesign.panel(host, 0xF007121F.toInt()); visibility = View.GONE
        }
        menu = panel
        val transport = ui.row()
        ui.button(transport, host.getString(R.string.pause_play)) { session?.togglePause(); interaction() }
        ui.button(transport, host.getString(R.string.previous)) { session?.previous(); interaction() }
        ui.button(transport, host.getString(R.string.next)) { session?.next(); interaction() }
        ui.button(transport, host.getString(R.string.sound)) { session?.setMuted(!store.settings().muted); interaction() }
        panel.addView(transport)
        val choices = ui.row()
        ui.button(choices, host.getString(R.string.cameras)) { switchFamily(MediaFamily.CAMERAS) }
        ui.button(choices, host.getString(R.string.my_videos)) { switchFamily(MediaFamily.VIDEOS) }
        ui.button(choices, host.getString(R.string.audio_track)) { selectAudio() }
        ui.button(choices, host.getString(R.string.forecast)) { host.openForecast(session?.currentItem) }
        panel.addView(choices)
        val more = ui.row()
        ui.button(more, host.getString(R.string.retry)) { session?.retry(); interaction() }
        ui.button(more, host.getString(R.string.stay_local)) { session?.stayLocal(); interaction() }
        ui.button(more, host.getString(R.string.library)) { host.open(LibraryRoute(if (session?.currentItem?.family == MediaFamily.VIDEOS) "videos" else "cameras")) }
        ui.button(more, host.getString(R.string.settings)) { host.open(LibraryRoute("settings")) }
        panel.addView(more)
        val content = FrameLayout(host).apply { addView(ui.scroll(panel), FrameLayout.LayoutParams(-1, -2)) }
        // The panel is capped to half the display so transport controls cannot cover the whole scene.
        frame.addView(content, FrameLayout.LayoutParams(-1, (host.resources.displayMetrics.heightPixels * .52).toInt(), Gravity.BOTTOM))
        val created = PlaybackSession(host, store, { notice ->
            scene?.show(notice)
            updateDiagnostics()
        }, { item ->
            ambient?.updateItem(item)
        }, {
            attached = false
            main.removeCallbacksAndMessages(null)
            scene?.detach()
        })
        session = created
        createdScene.attach(created)
        created.player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (session === created && !created.isReleased) updateDiagnostics()
            }
        })
        if (saved != null) created.restore(saved) else created.play(items, selected, shuffle)
        createdScene.video.requestFocus()
        return frame
    }

    fun dispatchKey(event: KeyEvent): Boolean {
        if (!attached || dialog) return false
        if (event.keyCode == KeyEvent.KEYCODE_BACK) return false
        if (event.action != KeyEvent.ACTION_DOWN) return false
        ambient?.userInteraction()
        when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> { if (event.repeatCount == 0) session?.togglePause(); interaction(); return true }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                session?.let { if (!it.isReleased && !it.player.playWhenReady) it.togglePause() }
                interaction(); return true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                session?.let { if (!it.isReleased && it.player.playWhenReady) it.togglePause() }
                interaction(); return true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> { if (event.repeatCount == 0) session?.next(); interaction(); return true }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> { if (event.repeatCount == 0) session?.previous(); interaction(); return true }
            KeyEvent.KEYCODE_MEDIA_REWIND -> { session?.seekBy(-10_000); interaction(); return true }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> { session?.seekBy(10_000); interaction(); return true }
        }
        if (menu?.visibility == View.VISIBLE) { interaction(); return false }
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> { if (event.repeatCount == 0) showMenu(); true }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (session?.currentItem?.family == MediaFamily.VIDEOS) session?.seekBy(-10_000) else if (event.repeatCount == 0) session?.previous()
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_UP -> {
                if (session?.currentItem?.family == MediaFamily.VIDEOS) session?.seekBy(10_000) else if (event.repeatCount == 0) session?.next()
                true
            }
            else -> false
        }
    }

    fun hideControls(): Boolean {
        if (menu?.visibility != View.VISIBLE) return false
        dismissMenu(); return true
    }
    fun dialogOpened() { dialog = true; main.removeCallbacks(hide) }
    fun dialogClosed() { dialog = false; interaction() }
    private fun showMenu() { menu?.visibility = View.VISIBLE; ambient?.setControlsVisible(true); (menu?.getChildAt(0) as? LinearLayout)?.getChildAt(0)?.requestFocus(); interaction() }
    private fun dismissMenu() { main.removeCallbacks(hide); menu?.visibility = View.GONE; ambient?.setControlsVisible(false); diagnostics?.visibility = View.GONE; scene?.video?.requestFocus() }
    private fun interaction() { ambient?.userInteraction(); main.removeCallbacks(hide); if (!dialog) main.postDelayed(hide, 5000); updateDiagnostics() }
    private fun switchFamily(family: MediaFamily) {
        val items = store.items().filter { it.family == family }
        if (items.isEmpty()) { scene?.message(host.getString(R.string.no_playable_items), persistent = false); interaction(); return }
        if (family == MediaFamily.CAMERAS) host.mutate { store.saveSettings(store.settings().copy(stayLocal = false)) }
        host.play(items, shuffle = family == MediaFamily.CAMERAS)
    }
    private fun selectAudio() {
        if (session?.isReleased != false) return
        val player = session?.player ?: return
        val tracks = player.currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }.flatMap { group ->
            (0 until group.length).filter { group.isTrackSupported(it) }.map { index -> group to index }
        }
        if (tracks.isEmpty()) {
            host.showDialog(AlertDialog.Builder(host).setMessage(R.string.no_audio).setPositiveButton(R.string.close, null).create()); return
        }
        val labels = listOf(host.getString(R.string.auto_audio)) + tracks.mapIndexed { i, (group, index) ->
            val format = group.getTrackFormat(index)
            host.getString(R.string.audio_track_name, i + 1, format.label ?: format.language ?: host.getString(R.string.unknown))
        }
        host.showDialog(AlertDialog.Builder(host).setTitle(R.string.audio_track).setItems(labels.toTypedArray()) { _, which ->
            if (session?.isReleased != false || session?.player !== player) return@setItems
            val builder = player.trackSelectionParameters.buildUpon().clearOverridesOfType(C.TRACK_TYPE_AUDIO)
            if (which > 0) tracks[which - 1].let { (group, index) -> builder.addOverride(TrackSelectionOverride(group.mediaTrackGroup, listOf(index))) }
            player.trackSelectionParameters = builder.build()
            interaction()
        }.setNegativeButton(R.string.cancel, null).create())
    }
    private fun updateDiagnostics() {
        diagnostics?.visibility = if (store.settings().diagnosticsEnabled && menu?.visibility == View.VISIBLE) View.VISIBLE else View.GONE
        diagnostics?.text = session?.diagnostics().orEmpty()
    }
    fun diagnosticText() = session?.diagnostics().orEmpty()
    fun snapshot(): PlaybackSnapshot? = session?.snapshot()
    fun suspendPlayback(): PlaybackSnapshot? = snapshot().also { stop() }
    fun stop() {
        attached = false; dialog = false
        main.removeCallbacksAndMessages(null)
        scene?.detach()
        session?.release(); session = null
        scene = null; menu = null; diagnostics = null
    }
}
