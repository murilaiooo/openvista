package com.openvista.prototype.youtube

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.openvista.prototype.R
import com.openvista.prototype.ui.AppLocale
import com.openvista.prototype.ui.TvUi
import com.openvista.sources.YouTubeVideoUrl

/**
 * Opt-in hardware experiment. Not wired into the media library, DreamService, rotation or fallback.
 * Uses the official YouTube embed, including any ads or branding YouTube chooses to display.
 * No stream extraction, proxy, ad filtering or overlays on top of the embedded player.
 */
class YouTubeLiveTestActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private lateinit var ui: TvUi
    private var player: WebView? = null
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null
    private var lastLink = ""

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        ui = TvUi(this)
        root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        setContentView(root)
        lastLink = savedInstanceState?.getString("youtube_test_link").orEmpty()
        showEntry()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    fullscreenView != null -> hideFullscreen()
                    player != null -> { stopVideo(); showEntry() }
                    else -> finish()
                }
            }
        })
    }

    private fun showEntry(error: String = "") {
        root.removeAllViews()
        val body = ui.column().apply {
            setPadding(ui.dp(36), ui.dp(24), ui.dp(36), ui.dp(24))
        }
        body.addView(ui.heading(getString(R.string.youtube_test_title)))
        body.addView(ui.description(getString(R.string.youtube_test_description)))
        val input = ui.field(body, getString(R.string.youtube_test_url), lastLink, 2048,
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI)
        body.addView(ui.description(getString(R.string.youtube_test_warning)))
        val message = ui.description(error)
        body.addView(message)
        val actions = ui.row()
        val play = ui.button(actions, getString(R.string.youtube_test_play), "youtube_test_play") {
            val link = input.text.toString().trim()
            val videoId = YouTubeVideoUrl.videoId(link)
            if (videoId == null) {
                ui.announce(message, getString(R.string.youtube_test_invalid))
            } else {
                lastLink = link
                startVideo(videoId)
            }
        }
        ui.button(actions, getString(R.string.back), "youtube_test_back") { finish() }
        body.addView(actions)
        body.addView(ui.description(getString(R.string.youtube_test_back_hint)))
        root.addView(ui.scroll(body), FrameLayout.LayoutParams(-1, -1))
        play.requestFocus()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startVideo(videoId: String) {
        stopVideo()
        try {
            val view = WebView(this).apply {
                setBackgroundColor(Color.BLACK)
                overScrollMode = View.OVER_SCROLL_NEVER
                isFocusable = true
                isFocusableInTouchMode = true
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false)
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        if (!request.isForMainFrame) return false
                        val host = request.url.host?.lowercase().orEmpty()
                        return request.url.scheme != "https" || host !in setOf(
                            "youtube.com", "www.youtube.com",
                            "youtube-nocookie.com", "www.youtube-nocookie.com",
                            "m.youtube.com",
                        )
                    }

                    override fun onReceivedError(
                        view: WebView, request: WebResourceRequest, error: WebResourceError
                    ) {
                        if (request.isForMainFrame && player === view) {
                            root.post {
                                if (player === view) {
                                    stopVideo()
                                    showEntry(getString(R.string.youtube_test_web_error))
                                }
                            }
                        }
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                        if (fullscreenView != null) {
                            callback.onCustomViewHidden()
                            return
                        }
                        fullscreenView = view
                        fullscreenCallback = callback
                        root.removeAllViews()
                        root.addView(view, FrameLayout.LayoutParams(-1, -1))
                        view.requestFocus()
                    }

                    override fun onHideCustomView() {
                        hideFullscreen()
                    }
                }
            }
            player = view
            root.removeAllViews()
            root.addView(view, FrameLayout.LayoutParams(-1, -1))
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            view.requestFocus()

            // YouTube requires a stable app identity in Referer to avoid embedded-player error 153.
            // The package ID is the identifier installed by Android (not a spoofed website origin).
            val referrer = "https://${packageName}/"
            val url = "https://www.youtube.com/embed/$videoId?autoplay=1&mute=1&playsinline=1"
            view.loadUrl(url, mapOf("Referer" to referrer))
        } catch (_: RuntimeException) {
            stopVideo()
            showEntry(getString(R.string.youtube_test_web_error))
        }
    }

    private fun hideFullscreen() {
        if (fullscreenView == null) return
        root.removeAllViews()
        fullscreenView = null
        val callback = fullscreenCallback
        fullscreenCallback = null
        callback?.onCustomViewHidden()
        player?.let { root.addView(it, FrameLayout.LayoutParams(-1, -1)); it.requestFocus() }
    }

    private fun stopVideo() {
        if (fullscreenView != null) hideFullscreen()
        val old = player
        player = null
        if (old != null) {
            root.removeView(old)
            old.stopLoading()
            old.onPause()
            old.destroy()
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        player?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        player?.onResume()
    }

    override fun onStop() {
        if (player != null) {
            stopVideo()
            if (!isFinishing) showEntry(getString(R.string.youtube_test_session_ended))
        }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("youtube_test_link", lastLink)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        stopVideo()
        super.onDestroy()
    }
}
