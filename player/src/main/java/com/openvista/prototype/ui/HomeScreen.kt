package com.openvista.prototype.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.util.UnstableApi
import com.openvista.prototype.LibraryRoute
import com.openvista.prototype.OpenVistaActivity
import com.openvista.prototype.R
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.MediaFamily
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Bounded TV landing page. Its scenery is decorative; all library state comes from LibraryStore. */
@UnstableApi
class HomeScreen(private val host: OpenVistaActivity) : AutoCloseable {
    private val artworkWorker = Executors.newSingleThreadExecutor { task -> Thread(task, "OpenVista-home-artwork") }
    private var artworkTask: Future<*>? = null
    private var scenery: Bitmap? = null
    private var sceneryViews = emptyList<SceneryView>()
    private var closed = false
    private var selectedPanel = "cameras"
    private var restoringFocus = false
    private val panelKeys = listOf("cameras", "videos", "favorites")

    fun saveState(state: Bundle) { state.putString("homeSelectedPanel", selectedPanel) }

    fun restoreState(state: Bundle?) {
        selectedPanel = state?.getString("homeSelectedPanel")?.takeIf { it in panelKeys } ?: "cameras"
    }

    fun create(): View {
        // Attach-time default focus must not replace the saved panel before route restoration.
        restoringFocus = true
        val items = host.store.items()
        val lastItem = host.store.lastItemId()?.let(host.store::item)
        val panels = LinearLayout(host).apply { orientation = LinearLayout.HORIZONTAL }
        val settings = compactControl(s(R.string.settings), "home_settings") { host.open(LibraryRoute("settings")) }
        val resume = lastItem?.let { item ->
            compactControl(host.getString(R.string.home_resume_item, item.title), "resume_last") {
                host.play(host.store.items().filter { it.family == item.family }, item.id, item.family == MediaFamily.CAMERAS)
            }.apply {
                maxWidth = dp(400)
                contentDescription = "${s(R.string.resume_last)}. ${item.title}"
            }
        }
        val definitions = listOf(
            Panel("cameras", R.string.cameras, R.string.home_explore, R.plurals.home_camera_count,
                items.count { it.family == MediaFamily.CAMERAS }, R.string.home_cameras_empty,
                R.string.home_cameras_ready, R.string.home_add_cameras, R.string.home_open_cameras, R.drawable.home_cameras),
            Panel("videos", R.string.my_videos, R.string.home_revisit, R.plurals.home_video_count,
                items.count { it.family == MediaFamily.VIDEOS }, R.string.home_videos_empty,
                R.string.home_videos_ready, R.string.home_add_videos, R.string.home_open_videos, R.drawable.home_videos),
            Panel("favorites", R.string.favorites, R.string.home_remember, R.plurals.home_favorite_count,
                items.count(LibraryItem::favorite), R.string.home_favorites_empty,
                R.string.home_favorites_ready, R.string.home_open_favorites, R.string.home_open_favorites, R.drawable.home_favorites),
        )
        val targets = ArrayList<HomePanel>()
        val artwork = ArrayList<SceneryView>()
        definitions.forEachIndexed { index, definition ->
            val picture = SceneryView(index, definition.fallback)
            artwork.add(picture)
            val panel = createPanel(definition, picture)
            targets.add(panel)
            panels.addView(panel, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        }
        sceneryViews = artwork
        val root = HomeLayout(panels, targets, header(settings), footer(resume), FrameLayout(host).apply {
            tag = "home_feedback_container"
        })

        fun rememberedPanel() = targets[panelKeys.indexOf(selectedPanel).coerceAtLeast(0)]
        targets.forEachIndexed { index, panel ->
            panel.nextFocusLeftId = targets[(index - 1).coerceAtLeast(0)].id
            panel.nextFocusRightId = targets[(index + 1).coerceAtMost(targets.lastIndex)].id
            panel.nextFocusUpId = settings.id
            panel.nextFocusDownId = resume?.id ?: panel.id
            panel.accessibilityTraversalAfter = if (index == 0) settings.id else targets[index - 1].id
            panel.setOnFocusChangeListener { _, focused ->
                panel.foreground = if (focused) focusOutline() else null
                artwork[index].alpha = if (focused) 1f else .82f
                if (focused && !restoringFocus) {
                    selectedPanel = definitions[index].page
                    host.route.focus = selectedPanel
                    settings.nextFocusDownId = panel.id
                    resume?.nextFocusUpId = panel.id
                }
            }
            panel.setOnKeyListener { _, key, event ->
                if (event.action != KeyEvent.ACTION_DOWN) false else when (key) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> { targets[(index - 1).coerceAtLeast(0)].requestFocus(); true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { targets[(index + 1).coerceAtMost(targets.lastIndex)].requestFocus(); true }
                    KeyEvent.KEYCODE_DPAD_UP -> { settings.requestFocus(); true }
                    KeyEvent.KEYCODE_DPAD_DOWN -> { (resume ?: panel).requestFocus(); true }
                    else -> false
                }
            }
        }
        settings.nextFocusDownId = rememberedPanel().id
        settings.nextFocusUpId = settings.id
        settings.nextFocusLeftId = settings.id
        settings.nextFocusRightId = settings.id
        settings.setOnKeyListener { _, key, event ->
            if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_DOWN) {
                rememberedPanel().requestFocus(); true
            } else false
        }
        resume?.apply {
            nextFocusUpId = rememberedPanel().id
            nextFocusDownId = id
            nextFocusLeftId = id
            nextFocusRightId = id
            accessibilityTraversalAfter = targets.last().id
            setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_UP) {
                    rememberedPanel().requestFocus(); true
                } else false
            }
        }
        loadScenery()
        return root
    }

    fun focusTarget(root: View, desired: String): View? {
        val key = if (desired == "navigation_settings") "home_settings" else desired
        if (key in panelKeys) selectedPanel = key
        restoringFocus = false
        return root.findViewWithTag<View>(key) ?: root.findViewWithTag<View>(selectedPanel)
    }

    private fun createPanel(definition: Panel, scenery: View): HomePanel {
        val count = host.resources.getQuantityString(definition.countLabel, definition.count, definition.count)
        val description = s(if (definition.count == 0) definition.emptyDescription else definition.description)
        val action = s(if (definition.count == 0) definition.emptyAction else definition.action)
        val copy = LinearLayout(host).apply {
            orientation = LinearLayout.VERTICAL
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
        copy.addView(label(s(definition.eyebrow), 12f, TvDesign.accent).apply {
            letterSpacing = .15f
            typeface = TvDesign.medium
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(14) })
        copy.addView(label(s(definition.title), 40f, TvDesign.text).apply {
            typeface = TvDesign.light
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        copy.addView(label(count, 13f, TvDesign.muted),
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(4) })
        copy.addView(label(description, 16f, TvDesign.text).apply { setLineSpacing(dp(3).toFloat(), 1f) },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(16) })
        copy.addView(label(host.getString(R.string.home_action_arrow, action), 15f, TvDesign.accent).apply {
            typeface = TvDesign.medium
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(24) })
        return HomePanel(FittedCopy(copy)).apply {
            id = View.generateViewId()
            tag = definition.page
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            contentDescription = listOf(s(definition.title), count, description, action).joinToString(". ")
            accessibilityDelegate = object : View.AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = "android.widget.Button"
                }
            }
            addView(scenery, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(View(host).apply {
                background = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(0xF507121F.toInt(), 0xB007121F.toInt(), 0x2207121F, Color.TRANSPARENT))
                TvDesign.decorative(this)
            }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(copyContent)
            setOnClickListener { host.open(LibraryRoute(definition.page)) }
        }
    }

    private fun header(settings: View): View = LinearLayout(host).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(TvDesign.safeHorizontal), dp(TvDesign.safeVertical), dp(TvDesign.safeHorizontal), 0)
        addView(label(s(R.string.app_name), 25f, TvDesign.text).apply {
            typeface = TvDesign.medium
            letterSpacing = -.025f
            setAccessibilityHeading(true)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(label(s(R.string.home_tagline), 14f, TvDesign.muted).apply {
            gravity = Gravity.END
            maxLines = 2
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            leftMargin = dp(24); rightMargin = dp(24)
        })
        addView(settings, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun footer(resume: View?): View = LinearLayout(host).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(TvDesign.safeHorizontal), 0, dp(TvDesign.safeHorizontal), dp(TvDesign.safeVertical))
        val note = LinearLayout(host).apply {
            orientation = LinearLayout.VERTICAL
            addView(label(s(R.string.home_development_preview), 11f, TvDesign.muted).apply { letterSpacing = .08f })
            addView(label(s(R.string.home_illustrative_scenery), 11f, TvDesign.muted))
        }
        addView(note, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        if (resume != null) addView(resume, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { leftMargin = dp(24) })
    }

    private fun compactControl(title: String, key: String, action: () -> Unit) = TextView(host).apply {
        id = View.generateViewId()
        tag = key
        text = title
        textSize = 15f
        typeface = TvDesign.medium
        setTextColor(TvDesign.text)
        gravity = Gravity.CENTER
        setPadding(dp(18), dp(8), dp(18), dp(8))
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        minWidth = dp(108)
        minimumHeight = dp(48)
        background = TvDesign.control(host)
        if (key == "home_settings") {
            setCompoundDrawablesWithIntrinsicBounds(TvIcon(host, TvIcon.Symbol.SETTINGS), null, null, null)
            compoundDrawablePadding = dp(10)
        }
        accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = "android.widget.Button"
            }
        }
        setOnClickListener { action() }
    }

    private fun label(value: String, size: Float, color: Int) = TextView(host).apply {
        text = value
        textSize = size
        typeface = TvDesign.regular
        setTextColor(color)
        includeFontPadding = false
        TvDesign.decorative(this)
    }

    private fun focusOutline() = InsetDrawable(GradientDrawable().apply {
        setColor(Color.TRANSPARENT)
        setStroke(dp(TvDesign.focusStroke), TvDesign.accent)
    }, dp(12))

    /** Chrome is measured first; panel text can use only the rectangle left between it. */
    private inner class HomeLayout(
        private val panels: LinearLayout,
        private val targets: List<HomePanel>,
        private val header: View,
        private val footer: View,
        private val feedback: FrameLayout,
    ) : FrameLayout(host) {
        private val topShade = View(host).apply {
            background = TvDesign.scrim(fromTop = true)
            TvDesign.decorative(this)
        }
        private var feedbackTop = 0

        init {
            setBackgroundColor(TvDesign.background)
            addView(panels)
            addView(topShade)
            addView(header)
            addView(footer)
            addView(feedback)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = View.MeasureSpec.getSize(widthMeasureSpec)
            val height = View.MeasureSpec.getSize(heightMeasureSpec)
            header.measure(exact(width), atMost(height))
            footer.measure(exact(width), atMost((height - header.measuredHeight).coerceAtLeast(0)))
            feedback.measure(exact((width - dp(TvDesign.safeHorizontal) * 2).coerceAtLeast(0)),
                atMost((height - header.measuredHeight - footer.measuredHeight).coerceAtLeast(0)))
            feedbackTop = header.measuredHeight + dp(8)
            val contentBottom = (height - footer.measuredHeight - dp(12)).coerceAtLeast(0)
            val contentTop = (feedbackTop + feedback.measuredHeight + dp(12)).coerceAtMost(contentBottom)
            targets.forEach { it.setCopyBounds(contentTop, contentBottom) }
            panels.forceLayout()
            panels.measure(exact(width), exact(height))
            topShade.measure(exact(width), exact(min(height, max(dp(160), contentTop))))
            setMeasuredDimension(width, height)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val width = right - left
            val height = bottom - top
            panels.layout(0, 0, width, height)
            topShade.layout(0, 0, width, topShade.measuredHeight)
            header.layout(0, 0, width, header.measuredHeight)
            footer.layout(0, height - footer.measuredHeight, width, height)
            feedback.layout(dp(TvDesign.safeHorizontal), feedbackTop,
                width - dp(TvDesign.safeHorizontal), feedbackTop + feedback.measuredHeight)
        }
    }

    /** Images retain full-height geometry while the copy uses the measured safe region. */
    private inner class HomePanel(val copyContent: FittedCopy) : FrameLayout(host) {
        private var copyTop = 0
        private var copyBottom = 0

        fun setCopyBounds(top: Int, bottom: Int) {
            if (copyTop == top && copyBottom == bottom) return
            copyTop = top
            copyBottom = bottom
            copyContent.forceLayout()
            forceLayout()
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = View.MeasureSpec.getSize(widthMeasureSpec)
            val height = View.MeasureSpec.getSize(heightMeasureSpec)
            for (index in 0 until childCount) {
                val child = getChildAt(index)
                child.measure(exact(width), exact(if (child === copyContent) (copyBottom - copyTop).coerceAtLeast(0) else height))
            }
            setMeasuredDimension(width, height)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            for (index in 0 until childCount) {
                val child = getChildAt(index)
                if (child === copyContent) child.layout(0, copyTop, right - left, copyBottom)
                else child.layout(0, 0, right - left, bottom - top)
            }
        }
    }

    /** Reduce decorative gaps first, then fit complete text without removing its accessible label. */
    private inner class FittedCopy(private val content: LinearLayout) : FrameLayout(host) {
        private val margins = List(content.childCount) { index ->
            (content.getChildAt(index).layoutParams as LinearLayout.LayoutParams).let { it.topMargin to it.bottomMargin }
        }
        private var fitScale = 1f

        init {
            setPadding(dp(TvDesign.safeHorizontal), dp(8), dp(28), dp(8))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            clipChildren = true
            clipToPadding = true
            addView(content)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = View.MeasureSpec.getSize(widthMeasureSpec)
            val height = View.MeasureSpec.getSize(heightMeasureSpec)
            val availableWidth = (width - paddingLeft - paddingRight).coerceAtLeast(1)
            val availableHeight = (height - paddingTop - paddingBottom).coerceAtLeast(0)
            fun measureAt(scale: Float) {
                val logicalWidth = (availableWidth / scale).toInt().coerceIn(1, dp(8192))
                content.measure(exact(logicalWidth), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            }
            fun spacing(ratio: Float) {
                margins.forEachIndexed { index, margin ->
                    (content.getChildAt(index).layoutParams as LinearLayout.LayoutParams).apply {
                        topMargin = (margin.first * ratio).roundToInt()
                        bottomMargin = (margin.second * ratio).roundToInt()
                    }
                }
                content.forceLayout()
            }

            spacing(1f)
            measureAt(1f)
            fitScale = 1f
            if (content.measuredHeight > availableHeight) {
                spacing(.35f)
                measureAt(1f)
                if (content.measuredHeight > availableHeight && availableHeight > 0) {
                    var low = .01f
                    var high = 1f
                    repeat(12) {
                        val candidate = (low + high) / 2f
                        measureAt(candidate)
                        if (content.measuredHeight * candidate <= availableHeight) low = candidate else high = candidate
                    }
                    measureAt(low)
                    fitScale = min(low, availableHeight.toFloat() / content.measuredHeight.coerceAtLeast(1))
                } else if (availableHeight == 0) fitScale = 0f
            }
            content.pivotX = 0f
            content.pivotY = 0f
            content.scaleX = fitScale
            content.scaleY = fitScale
            setMeasuredDimension(width, height)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val scaledHeight = (content.measuredHeight * fitScale).roundToInt()
            val contentTop = ((bottom - top) - paddingBottom - scaledHeight).coerceAtLeast(paddingTop)
            content.layout(paddingLeft, contentTop, paddingLeft + content.measuredWidth, contentTop + content.measuredHeight)
        }
    }

    private fun exact(size: Int) = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
    private fun atMost(size: Int) = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST)

    private fun loadScenery() {
        if (scenery != null || artworkTask != null || closed) return
        artworkTask = artworkWorker.submit {
            val bitmap = try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(host.resources, R.drawable.home_scenery, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 1920 || bounds.outHeight / sample > 1080) sample *= 2
                BitmapFactory.decodeResource(host.resources, R.drawable.home_scenery, BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.RGB_565
                })
            } catch (_: RuntimeException) { null }
            host.runOnUiThread {
                if (closed || host.isDestroyed || host.isFinishing) return@runOnUiThread
                scenery = bitmap
                sceneryViews.forEach(View::invalidate)
            }
        }
    }

    override fun close() {
        closed = true
        artworkTask?.cancel(true)
        artworkWorker.shutdownNow()
        sceneryViews = emptyList()
        scenery = null
    }

    /** Three source rectangles share one bounded bitmap; drawing never creates cropped copies. */
    private inner class SceneryView(private val column: Int, fallbackResource: Int) : View(host) {
        private val source = Rect()
        private val target = Rect()
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val fallback = host.getDrawable(fallbackResource)

        init { alpha = .82f; TvDesign.decorative(this) }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val bitmap = scenery
            if (bitmap == null) {
                fallback?.setBounds(0, 0, width, height)
                fallback?.draw(canvas)
                return
            }
            if (width == 0 || height == 0) return
            val left = bitmap.width * column / 3
            val right = bitmap.width * (column + 1) / 3
            val columnWidth = right - left
            val scale = max(width.toFloat() / columnWidth, height.toFloat() / bitmap.height)
            val visibleWidth = (width / scale).toInt().coerceIn(1, columnWidth)
            val visibleHeight = (height / scale).toInt().coerceIn(1, bitmap.height)
            val sourceLeft = left + (columnWidth - visibleWidth) / 2
            val sourceTop = (bitmap.height - visibleHeight) / 2
            source.set(sourceLeft, sourceTop, sourceLeft + visibleWidth, sourceTop + visibleHeight)
            target.set(0, 0, width, height)
            canvas.drawBitmap(bitmap, source, target, paint)
        }
    }

    private fun s(resource: Int) = host.getString(resource)
    private fun dp(value: Int) = TvDesign.dp(host, value)

    private data class Panel(
        val page: String, val title: Int, val eyebrow: Int, val countLabel: Int, val count: Int,
        val emptyDescription: Int, val description: Int, val emptyAction: Int, val action: Int, val fallback: Int,
    )
}
