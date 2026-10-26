package com.openvista.prototype

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CancellationSignal
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.openvista.sources.PlaylistResult
import com.openvista.sources.StreamKind
import com.openvista.sources.UserSource
import com.openvista.sources.UserSourceParser

/**
 * A manually launched engineering player, not the final OpenVista library or DreamService.
 * URLs are supplied by the user, kept in memory and never sent anywhere except on Play.
 */
@UnstableApi
class PlayerPrototypeActivity : ComponentActivity() {
    private val parser = UserSourceParser()
    private var mediaPlayer: ExoPlayer? = null
    private var currentKind = StreamKind.HLS
    private var muted = true
    private var imported = emptyList<UserSource>()
    private var localVideos = emptyList<SafVideoScanner.Video>()
    private var scanGeneration = 0
    private var folderScanSignal: CancellationSignal? = null
    private lateinit var urlInput: EditText
    private lateinit var status: TextView
    private lateinit var video: PlayerView
    private lateinit var playImportedButton: Button
    private lateinit var playVideoButton: Button
    private lateinit var soundButton: Button

    // Register unconditionally so the result is delivered after Activity recreation.
    private val listPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onPickerResult(REQUEST_LIST, result.resultCode, result.data)
    }
    private val folderPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onPickerResult(REQUEST_FOLDER, result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
        buildScreen()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val player = mediaPlayer
                if (player != null && player.playWhenReady) {
                    player.stop()
                    setStatus("Playback stopped. Back again to exit.")
                } else {
                    finish()
                }
            }
        })
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(30), dp(18), dp(30), dp(18))
            setBackgroundColor(Color.rgb(12, 23, 39))
        }
        setContentView(root)
        root.addView(label("OPENVISTA / PLAYER PROTOTYPE", 27f, Color.WHITE))
        root.addView(label("Your sources only. Nothing plays until you choose a source and press Play.", 15f, 0xFFB5C8D7.toInt()))

        val actions = horizontal()
        root.addView(actions)
        val add = action("Add a stream", actions) { focusUrl() }
        action("Import a list", actions) { openM3uPicker() }
        action("Play my videos", actions) { openVideoFolderPicker() }

        urlInput = EditText(this).apply {
            hint = "Enter an HLS or DASH URL"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            isSingleLine = true
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(0xFFB5C8D7.toInt())
            setBackgroundColor(0xFF304356.toInt())
            setPadding(dp(12), 0, dp(12), 0)
        }
        root.addView(urlInput, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)).apply {
            topMargin = dp(10)
            bottomMargin = dp(10)
        })

        val sourceActions = horizontal()
        root.addView(sourceActions)
        action("HLS", sourceActions) { currentKind = StreamKind.HLS; setStatus("HLS selected. Press Play URL to connect.") }
        action("DASH", sourceActions) { currentKind = StreamKind.DASH; setStatus("DASH selected. Press Play URL to connect.") }
        action("Play URL", sourceActions) { playEnteredUrl() }
        soundButton = action("Sound: Off", sourceActions) { toggleSound() }

        status = label("No sources. Add a URL, import an M3U file, or select a video folder.", 15f, 0xFFE2EBF3.toInt())
        status.setPadding(dp(8), dp(8), dp(8), dp(8))
        root.addView(status)
        video = PlayerView(this).apply {
            useController = true
            controllerShowTimeoutMs = 5_000
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            setShutterBackgroundColor(Color.BLACK)
            setBackgroundColor(Color.BLACK)
            isFocusable = true
        }
        root.addView(video, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val footer = horizontal()
        root.addView(footer)
        playImportedButton = action("Play first imported", footer) { imported.firstOrNull()?.let(::playSource) }
        playImportedButton.isEnabled = false
        playVideoButton = action("Play first video", footer) {
            localVideos.firstOrNull()?.let(::playLocalVideo)
        }
        playVideoButton.isEnabled = false
        action("Stop", footer) { mediaPlayer?.stop(); setStatus("Playback stopped.") }
        action("Exit", footer) { finish() }
        add.requestFocus()
    }

    override fun onStart() {
        super.onStart()
        val player = ExoPlayer.Builder(this).build().apply {
            volume = if (muted) 0f else 1f
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    setStatus("Playback failed. The source may be offline, unsupported or unavailable.")
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> setStatus("Connecting to the selected source...")
                        Player.STATE_READY -> setStatus(if (muted) "Playing selected source (muted). Back or Stop returns to controls." else "Playing selected source with sound. Back or Stop returns to controls.")
                        Player.STATE_ENDED -> setStatus("Playback ended.")
                        Player.STATE_IDLE -> setStatus("Ready to play.")
                    }
                }
            })
        }
        mediaPlayer = player
        video.player = player
    }

    override fun onStop() {
        scanGeneration++
        folderScanSignal?.cancel()
        folderScanSignal = null
        video.player = null
        mediaPlayer?.release()
        mediaPlayer = null
        super.onStop()
    }

    private fun toggleSound() {
        muted = !muted
        mediaPlayer?.volume = if (muted) 0f else 1f
        soundButton.text = if (muted) "Sound: Off" else "Sound: On"
        setStatus(if (muted) "Sound muted." else "Sound enabled for the selected source.")
    }

    private fun playEnteredUrl() {
        val source = parser.manualUrl(urlInput.text.toString(), currentKind)
        if (source == null) {
            setStatus("Enter a valid HTTP(S) stream URL. Embedded username/password fields are not supported.")
        } else {
            playSource(source)
        }
    }

    private fun playSource(source: UserSource) {
        try {
            val mime = when (source.kind) {
                StreamKind.HLS -> MimeTypes.APPLICATION_M3U8
                StreamKind.DASH -> MimeTypes.APPLICATION_MPD
            }
            val item = MediaItem.Builder().setUri(source.url).setMimeType(mime).build()
            prepareAndPlay(item)
        } catch (_: RuntimeException) {
            setStatus("Unable to prepare this source.")
        }
    }

    private fun playLocalVideo(file: SafVideoScanner.Video) {
        try {
            prepareAndPlay(MediaItem.fromUri(file.uri))
        } catch (_: RuntimeException) {
            setStatus("Unable to prepare the selected video.")
        }
    }

    private fun prepareAndPlay(item: MediaItem) {
        val player = mediaPlayer ?: run {
            setStatus("Player not ready. Reopen the app and try again.")
            return
        }
        player.setMediaItem(item)
        player.prepare()
        player.playWhenReady = true
        video.requestFocus()
    }

    private fun openM3uPicker() {
        try {
            listPicker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        } catch (_: RuntimeException) {
            setStatus("File picker unavailable. Use Add a stream instead.")
        }
    }

    private fun openVideoFolderPicker() {
        try {
            folderPicker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        } catch (_: RuntimeException) {
            setStatus("Folder picker unavailable. Use Add a stream instead.")
        }
    }

    private fun onPickerResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) {
            setStatus("Selection cancelled. Your library was not changed.")
            return
        }
        val uri = data?.data
        if (uri == null) {
            setStatus("The system returned no document.")
            return
        }
        val generation = ++scanGeneration
        folderScanSignal?.cancel()
        folderScanSignal = null
        if (requestCode == REQUEST_FOLDER) {
            val signal = CancellationSignal()
            folderScanSignal = signal
            localVideos = emptyList()
            playVideoButton.isEnabled = false
            setStatus("Checking selected folder for supported video files...")
            Thread {
                val result = SafVideoScanner(contentResolver).scan(uri, signal)
                runOnUiThread {
                    if (folderScanSignal === signal) folderScanSignal = null
                    if (isFinishing || isDestroyed || generation != scanGeneration) return@runOnUiThread
                    when (result) {
                        is SafVideoScanner.Result.Ready -> {
                            localVideos = result.videos
                            playVideoButton.isEnabled = localVideos.isNotEmpty()
                            val extra = if (result.capped) " Search capped for TV performance." else ""
                            if (localVideos.isEmpty()) {
                                setStatus("No supported videos found. Choose another folder.$extra")
                            } else {
                                setStatus("Found ${localVideos.size} video(s). Press Play first video to start.$extra")
                            }
                        }
                        SafVideoScanner.Result.Unavailable -> {
                            setStatus("Could not read that folder. Select another folder or add a stream.")
                        }
                    }
                }
            }.start()
        } else if (requestCode == REQUEST_LIST) {
            imported = emptyList()
            playImportedButton.isEnabled = false
            setStatus("Reading the selected M3U file...")
            Thread {
                val result = try {
                    contentResolver.openInputStream(uri)?.let(parser::parseM3u)
                        ?: PlaylistResult.Rejected(com.openvista.sources.PlaylistFailure.READ_FAILED)
                } catch (_: Exception) {
                    PlaylistResult.Rejected(com.openvista.sources.PlaylistFailure.READ_FAILED)
                }
                runOnUiThread {
                    if (isFinishing || isDestroyed || generation != scanGeneration) return@runOnUiThread
                    when (result) {
                        is PlaylistResult.Ready -> {
                            imported = result.sources
                            playImportedButton.isEnabled = imported.isNotEmpty()
                            setStatus("Imported ${result.sources.size} stream(s), skipped ${result.skippedEntries} invalid and ${result.duplicateEntries} duplicate(s). Nothing has played yet.")
                        }
                        is PlaylistResult.Rejected -> setStatus("This file cannot be imported: ${result.reason.name.replace('_', ' ').lowercase()}.")
                    }
                }
            }.start()
        }
    }

    private fun focusUrl() {
        urlInput.requestFocus()
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(urlInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setStatus(message: String) {
        status.text = message
    }

    private fun horizontal(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(7), 0, dp(7))
    }

    private fun action(title: String, parent: LinearLayout, click: () -> Unit): Button {
        val button = Button(this).apply {
            text = title
            isAllCaps = false
            textSize = 16f
            minHeight = dp(55)
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(0xFF40566B.toInt())
            setOnFocusChangeListener { _, focused ->
                backgroundTintList = ColorStateList.valueOf(if (focused) 0xFF80D6F0.toInt() else 0xFF40566B.toInt())
                setTextColor(if (focused) 0xFF102135.toInt() else Color.WHITE)
            }
            setOnClickListener { click() }
        }
        parent.addView(button, LinearLayout.LayoutParams(0, dp(62), 1f).apply { marginEnd = dp(10) })
        return button
    }

    private fun label(value: String, size: Float, color: Int): TextView =
        TextView(this).apply { text = value; textSize = size; setTextColor(color) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        private const val REQUEST_LIST = 401
        private const val REQUEST_FOLDER = 402
    }
}
