package com.openvista.prototype

import android.app.AlertDialog
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.common.util.UnstableApi
import com.openvista.prototype.library.LibraryAccess
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import com.openvista.prototype.library.MediaFamily
import com.openvista.prototype.diagnostics.DiagnosticJournal
import com.openvista.prototype.playback.PlaybackSnapshot
import com.openvista.prototype.ui.AppLocale
import com.openvista.prototype.ui.LibraryScreens
import com.openvista.prototype.ui.PlayerScreen
import com.openvista.prototype.ui.SettingsScreens
import com.openvista.prototype.ui.TvUi
import com.openvista.sources.PlaylistResult
import com.openvista.sources.UserSourceParser
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.Future

data class LibraryRoute(val page: String, val id: String = "", val family: MediaFamily = MediaFamily.CAMERAS,
                        val offset: Int = 0, var focus: String = "")

/** TV launcher and navigation owner; media ownership lives in the shared PlaybackSession. */
@UnstableApi
class OpenVistaActivity : ComponentActivity() {
    lateinit var store: LibraryStore
        private set
    lateinit var ui: TvUi
        private set
    private lateinit var root: FrameLayout
    private lateinit var feedback: TextView
    private lateinit var access: LibraryAccess
    private lateinit var libraryScreens: LibraryScreens
    private lateinit var settingsScreens: SettingsScreens
    private lateinit var playerScreen: PlayerScreen
    private val history = ArrayList<LibraryRoute>()
    var route = LibraryRoute("home")
        private set
    private val worker = Executors.newSingleThreadExecutor()
    private var importTask: Future<*>? = null
    @Volatile private var importStream: java.io.InputStream? = null
    private var importGeneration = 0
    private var started = false
    private var ready = false
    private var activeDialog: AlertDialog? = null
    private var pendingMessage: String? = null
    private var playbackIds = emptyList<String>()
    private var playbackSelected: String? = null
    private var playbackShuffle = false
    private var playbackSnapshot: PlaybackSnapshot? = null
    private var pendingPicker: Triple<Int, Int, Intent?>? = null
    private var lastDiagnostics = ""

    private val listPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        handlePicker(0, it.resultCode, it.data)
    }
    private val folderPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        handlePicker(1, it.resultCode, it.data)
    }
    private val videoPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        handlePicker(2, it.resultCode, it.data)
    }

    override fun attachBaseContext(newBase: Context) { super.attachBaseContext(AppLocale.wrap(newBase)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        ui = TvUi(this)
        root = FrameLayout(this).apply { setBackgroundColor(0xFF0B1929.toInt()) }
        setContentView(root)
        root.addView(ui.heading(getString(R.string.loading)))
        savedInstanceState?.getString("navigation")?.let(::restoreNavigation)
        playbackIds = savedInstanceState?.getStringArrayList("playbackIds")?.take(2000).orEmpty()
        playbackSelected = savedInstanceState?.getString("playbackSelected")
        playbackShuffle = savedInstanceState?.getBoolean("playbackShuffle") ?: false
        playbackSnapshot = savedInstanceState?.getString("playbackSnapshot")?.let { PlaybackSnapshot.fromJson(it) }
        worker.submit {
            try {
                val loaded = LibraryStore.get(applicationContext)
                runOnUiThread {
                    if (isDestroyed || isFinishing) return@runOnUiThread
                    store = loaded
                    access = LibraryAccess(this, store)
                    libraryScreens = LibraryScreens(this)
                    settingsScreens = SettingsScreens(this)
                    playerScreen = PlayerScreen(this, store)
                    ready = true
                    if (!store.settings().onboardingComplete && savedInstanceState == null) route = LibraryRoute("onboarding")
                    refresh()
                    if (started) resumeForeground()
                    pendingPicker?.let { pending -> pendingPicker = null; handlePicker(pending.first, pending.second, pending.third) }
                }
            } catch (_: Exception) {
                runOnUiThread {
                    if (isDestroyed) return@runOnUiThread
                    root.removeAllViews()
                    val body = ui.column().apply { setPadding(ui.dp(36), ui.dp(36), ui.dp(36), ui.dp(36)) }
                    body.addView(ui.heading(getString(R.string.app_name)))
                    body.addView(ui.description(getString(R.string.storage_unavailable)))
                    ui.button(body, getString(R.string.close)) { finish() }.requestFocus()
                    root.addView(body)
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!ready) { finish(); return }
                if (route.page == "player" && playerScreen.hideControls()) return
                if (history.isNotEmpty()) {
                    leaveScreen()
                    route = history.removeAt(history.lastIndex)
                    refresh()
                } else if (route.page != "home") {
                    leaveScreen(); route = LibraryRoute("home"); refresh()
                } else finish()
            }
        })
    }

    override fun onStart() { super.onStart(); started = true; if (ready) resumeForeground() }

    private fun resumeForeground() {
        if (route.page == "player") renderPlayer()
        access.restore { status ->
            if (route.page != "player") refresh()
            message(status)
        }
        settingsScreens.onForeground()
        if (store.lastStorageError() != null) {
            DiagnosticJournal.record(this, DiagnosticJournal.Event.LIBRARY_STORAGE_ERROR)
            message(R.string.operation_failed)
        }
    }

    override fun onStop() {
        started = false
        activeDialog?.dismiss()
        activeDialog = null
        if (ready) {
            rememberPlayback()
            playerScreen.stop()
            access.cancel()
            settingsScreens.onBackground()
        }
        cancelImport()
        super.onStop()
    }

    override fun onDestroy() {
        if (ready) { access.close(); playerScreen.stop(); settingsScreens.close() }
        activeDialog?.dismiss()
        cancelImport()
        worker.shutdownNow()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        rememberFocus()
        if (ready) rememberPlayback()
        outState.putString("navigation", JSONArray().apply {
            (history + route).takeLast(32).forEach { r -> put(JSONObject().put("page", r.page)
                .put("id", r.id).put("family", r.family.name).put("offset", r.offset).put("focus", r.focus)) }
        }.toString())
        outState.putStringArrayList("playbackIds", ArrayList(playbackIds))
        outState.putString("playbackSelected", playbackSelected)
        outState.putBoolean("playbackShuffle", playbackShuffle)
        outState.putString("playbackSnapshot", playbackSnapshot?.toJson())
        super.onSaveInstanceState(outState)
    }

    // Intercept remote keys before focused PlayerView receives them; AndroidX marks the inherited
    // method library-restricted, but a TV Activity must handle these input events here.
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (ready && route.page == "player" && playerScreen.dispatchKey(event)) return true
        return super.dispatchKeyEvent(event)
    }

    fun open(destination: LibraryRoute) {
        rememberFocus()
        leaveScreen()
        if (route.page != "onboarding" && route != destination) history.add(route)
        if (history.size > 32) history.removeAt(0)
        route = destination
        refresh()
    }

    fun refresh() {
        if (!ready || isDestroyed || isFinishing) return
        if (route.page == "player") { if (started) renderPlayer(); return }
        val focused = currentFocus?.tag as? String
        root.removeAllViews()
        val outer = ui.column().apply { setPadding(ui.dp(32), ui.dp(18), ui.dp(32), ui.dp(18)) }
        val navigation = ui.row()
        ui.button(navigation, getString(R.string.back), "navigation_back") { onBackPressedDispatcher.onBackPressed() }
        ui.button(navigation, getString(R.string.home), "navigation_home") {
            leaveScreen(); history.clear(); route = LibraryRoute("home"); refresh()
        }
        ui.button(navigation, getString(R.string.settings), "navigation_settings") { open(LibraryRoute("settings")) }
        outer.addView(navigation)
        feedback = ui.description(pendingMessage.orEmpty()).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        outer.addView(feedback)
        val body = ui.column()
        when (route.page) {
            "home" -> libraryScreens.home(body)
            "onboarding", "setup" -> libraryScreens.onboarding(body, route.page == "setup")
            "cameras", "videos", "favorites" -> libraryScreens.library(body, route)
            "details" -> libraryScreens.details(body, route.id)
            "playlists" -> libraryScreens.playlists(body, route.family)
            "playlist" -> libraryScreens.playlist(body, route.id)
            "folders" -> libraryScreens.folders(body)
            "map" -> libraryScreens.map(body)
            else -> settingsScreens.render(body, route.page)
        }
        outer.addView(ui.scroll(body), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(outer, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        val desired = focused ?: route.focus
        body.post { (body.findViewWithTag<View>(desired) ?: firstFocusable(body) ?: firstFocusable(navigation))?.requestFocus() }
    }

    fun play(items: List<LibraryItem>, selectedId: String? = null, shuffle: Boolean = false) {
        if (items.isEmpty()) { message(R.string.no_playable_items); return }
        playbackIds = items.map { it.id }
        playbackSelected = selectedId
        playbackShuffle = shuffle
        playbackSnapshot = null
        if (route.page == "player") playerScreen.stop() else { rememberFocus(); history.add(route) }
        route = LibraryRoute("player")
        renderPlayer()
    }

    private fun renderPlayer() {
        if (playerScreen.isAttached) return
        val items = (playbackSnapshot?.itemIds ?: playbackIds).mapNotNull(store::item)
        if (items.isEmpty()) { route = LibraryRoute("home"); refresh(); message(R.string.no_playable_items); return }
        root.removeAllViews()
        try {
            root.addView(playerScreen.create(items, playbackSelected, playbackShuffle, playbackSnapshot), FrameLayout.LayoutParams(-1, -1))
        } catch (_: RuntimeException) {
            playerScreen.stop(); route = LibraryRoute("home"); refresh(); message(R.string.player_unavailable)
        }
    }

    fun message(resource: Int) = message(getString(resource))
    fun message(value: String) {
        pendingMessage = value
        if (::feedback.isInitialized && route.page != "player") ui.announce(feedback, value)
    }

    fun mutate(change: () -> Unit) {
        try { change(); refresh(); persisted(getString(R.string.saved)) }
        catch (_: Exception) { DiagnosticJournal.record(this, DiagnosticJournal.Event.LIBRARY_STORAGE_ERROR); message(R.string.operation_failed) }
    }
    fun persisted(success: String) {
        message(R.string.loading)
        worker.submit {
            val saved = try { store.flush(); true } catch (_: Exception) { false }
            runOnUiThread {
                if (isDestroyed || isFinishing || !started) return@runOnUiThread
                if (saved) message(success) else {
                    DiagnosticJournal.record(this, DiagnosticJournal.Event.LIBRARY_STORAGE_ERROR)
                    message(R.string.operation_failed)
                }
            }
        }
    }

    fun showDialog(dialog: AlertDialog, onDismiss: () -> Unit = {}) {
        activeDialog?.dismiss()
        activeDialog = dialog
        if (route.page == "player") playerScreen.dialogOpened()
        dialog.setOnDismissListener {
            if (activeDialog === dialog) activeDialog = null
            if (route.page == "player") playerScreen.dialogClosed()
            onDismiss()
        }
        dialog.show()
    }

    fun confirm(title: String, action: () -> Unit) {
        showDialog(AlertDialog.Builder(this).setMessage(title).setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.remove) { _, _ -> action() }.create())
    }

    fun pickList() = launchPicker(listPicker, Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE); type = "*/*"; addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    })
    fun pickFolder() = launchPicker(folderPicker, Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    })
    fun pickVideo() = launchPicker(videoPicker, Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE); type = "video/*"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    })

    private fun launchPicker(launcher: androidx.activity.result.ActivityResultLauncher<Intent>, intent: Intent) {
        try { launcher.launch(intent) } catch (_: RuntimeException) { message(R.string.picker_unavailable) }
    }

    fun rescan() { message(R.string.loading); access.restore { status -> refresh(); message(status) } }
    fun forgetFolder(uri: String) {
        if (route.page == "player") { playerScreen.stop(); route = LibraryRoute("videos") }
        access.forgetFolder(uri) { status -> refresh(); message(status) }
    }
    fun removeItem(id: String) {
        access.removeItem(id) { status -> refresh(); message(status) }
    }
    fun playbackDiagnostics(): String = if (::playerScreen.isInitialized && playerScreen.isAttached) playerScreen.diagnosticText() else lastDiagnostics
    fun editSource(item: LibraryItem? = null) = libraryScreens.editSource(item)
    fun pairing() = settingsScreens.startPairing()
    fun openForecast(item: LibraryItem? = null) = settingsScreens.forecast(item)

    private fun importList(uri: Uri) {
        cancelImport()
        val generation = importGeneration
        message(R.string.reading_list)
        importTask = worker.submit {
            val listName = try {
                contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { rows ->
                    if (!rows.moveToFirst()) null else rows.getString(0)?.filterNot(Char::isISOControl)?.take(100)
                }
            } catch (_: Exception) { null }
            val parsed = try {
                contentResolver.openInputStream(uri)?.let { input -> importStream = input; UserSourceParser().parseM3u(input) }
            } catch (_: Exception) { null } finally { importStream = null }
            runOnUiThread {
                if (!started || isDestroyed || generation != importGeneration) return@runOnUiThread
                if (parsed is PlaylistResult.Ready) {
                    try {
                        val batch = parsed.sources.map { source -> LibraryItem(UUID.randomUUID().toString(), source.title, source.url,
                            MediaFamily.CAMERAS, streamKind = source.kind.name) }
                        val imported = store.importCameraList(listName?.takeIf(String::isNotBlank) ?: getString(R.string.imported_list), batch)
                        open(LibraryRoute("playlist", imported.playlistId, MediaFamily.CAMERAS))
                        persisted(getString(R.string.import_complete, imported.added, parsed.skippedEntries,
                            parsed.duplicateEntries + imported.duplicateCount))
                    } catch (_: Exception) { message(R.string.operation_failed) }
                } else message(R.string.import_failed)
            }
        }
    }

    private fun cancelImport() {
        importGeneration++; importTask?.cancel(true); importTask = null
        try { importStream?.close() } catch (_: Exception) { }
        importStream = null
    }
    private fun leaveScreen() { if (ready && route.page == "player") { rememberPlayback(); playerScreen.stop() } }
    private fun rememberPlayback() {
        if (ready && route.page == "player") {
            playerScreen.currentItemId?.let { playbackSelected = it }
            playerScreen.snapshot()?.let { playbackSnapshot = it }
            playerScreen.diagnosticText().takeIf(String::isNotBlank)?.let { lastDiagnostics = it }
        }
    }
    private fun handlePicker(kind: Int, resultCode: Int, data: Intent?) {
        if (!ready) { pendingPicker = Triple(kind, resultCode, data); return }
        val uri = data?.data
        if (resultCode != RESULT_OK || uri == null) { message(R.string.selection_cancelled); return }
        when (kind) {
            0 -> importList(uri)
            1 -> { message(R.string.loading); access.selectFolder(uri, data.flags) { status -> refresh(); message(status) } }
            2 -> access.selectVideo(uri, data.flags) { status -> refresh(); message(status) }
        }
    }
    private fun rememberFocus() { route.focus = currentFocus?.tag as? String ?: route.focus }
    private fun firstFocusable(view: View): View? {
        if (view.isFocusable && view.isEnabled && view.visibility == View.VISIBLE) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) firstFocusable(view.getChildAt(i))?.let { return it }
        return null
    }
    private fun restoreNavigation(value: String) {
        try {
            val array = JSONArray(value)
            val allowed = setOf("home", "onboarding", "setup", "cameras", "videos", "favorites", "details", "playlists",
                "playlist", "folders", "map", "player", "settings", "playback_settings", "overlays", "overlay_editor", "weather_settings", "advanced", "schedule", "privacy", "updates", "screensaver")
            for (i in 0 until array.length().coerceAtMost(32)) {
                val r = array.getJSONObject(i)
                if (r.getString("page") !in allowed) continue
                history.add(LibraryRoute(r.getString("page"), r.optString("id"),
                    MediaFamily.valueOf(r.getString("family")), r.optInt("offset").coerceIn(0, 2000), r.optString("focus")))
            }
            if (history.isNotEmpty()) route = history.removeAt(history.lastIndex)
        } catch (_: Exception) { history.clear(); route = LibraryRoute("home") }
    }
}
