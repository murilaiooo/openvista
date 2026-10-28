package com.openvista.prototype.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
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
import kotlin.math.min
import kotlin.math.roundToInt

/** Bounded TV landing page. Its scenery is decorative; all library state comes from LibraryStore. */
@UnstableApi
class HomeScreen(private val host: OpenVistaActivity) : AutoCloseable {
    private val artworkWorker = Executors.newSingleThreadExecutor { task -> Thread(task, "OpenVista-home-artwork") }
    private var artworkTask: Future<*>? = null
    private var artwork = arrayOfNulls<Bitmap>(3)
    private var artworkSize = 0 to 0
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
        val settings = settingsButton()
        val resume = lastItem?.let { item -> resumeControl(item) }
        val definitions = listOf(
            Panel("cameras", R.string.cameras, R.string.home_explore, TvIcon.Symbol.CAMERA,
                items.count { it.family == MediaFamily.CAMERAS }, R.string.home_cameras_empty,
                R.string.home_cameras_ready, R.string.home_add_cameras, R.plurals.home_action_cameras),
            Panel("videos", R.string.my_videos, R.string.home_revisit, TvIcon.Symbol.FILM,
                items.count { it.family == MediaFamily.VIDEOS }, R.string.home_videos_empty,
                R.string.home_videos_ready, R.string.home_add_videos, R.plurals.home_action_videos),
            Panel("favorites", R.string.favorites, R.string.home_remember, TvIcon.Symbol.HEART,
                items.count(LibraryItem::favorite), R.string.home_favorites_empty,
                R.string.home_favorites_ready, R.string.home_open_favorites, R.plurals.home_action_favorites),
        )
        val scenery = ArrayList<SceneryView>()
        val targets = definitions.mapIndexed { index, definition ->
            SceneryView(index).also(scenery::add).let { createPanel(definition, it) }
        }
        sceneryViews = scenery
        val root = HomeLayout(targets, header(settings), footer(resume), FrameLayout(host).apply {
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
                panel.invalidate()
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
        return root
    }

    fun focusTarget(root: View, desired: String): View? {
        val key = if (desired == "navigation_settings") "home_settings" else desired
        if (key in panelKeys) selectedPanel = key
        restoringFocus = false
        return root.findViewWithTag<View>(key) ?: root.findViewWithTag<View>(selectedPanel)
    }

    private fun createPanel(definition: Panel, scenery: SceneryView): HomePanel {
        val empty = definition.count == 0
        val description = s(if (empty) definition.emptyDescription else definition.description)
        val action = if (empty) s(definition.emptyAction)
            else host.resources.getQuantityString(definition.action, definition.count, definition.count)
        val title = host.getString(R.string.home_title, s(definition.title))
        val copy = LinearLayout(host).apply {
            orientation = LinearLayout.VERTICAL
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
        copy.addView(label(s(definition.eyebrow), 14.5f, 0xCCFFFFFF.toInt(), TvDesign.regular).apply {
            letterSpacing = .12f
            maxLines = 1
        })
        copy.addView(View(host).apply { setBackgroundColor(Color.WHITE); TvDesign.decorative(this) },
            LinearLayout.LayoutParams(dp(30f), dp(1.5f)).apply { topMargin = dp(12.5f) })
        copy.addView(View(host).apply {
            background = TvIcon(host, definition.icon, 11f, TvDesign.text, 1.6f)
            TvDesign.decorative(this)
        }, LinearLayout.LayoutParams(dp(11f), dp(11f)).apply { topMargin = dp(11f) })
        copy.addView(label(title, 36f, TvDesign.text, TvDesign.bold).apply {
            letterSpacing = -.035f
            maxLines = 2
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(2.5f)
        })
        copy.addView(label(description, 11.3f, 0xEBFFFFFF.toInt(), TvDesign.regular).apply {
            setLineSpacing(dp(2.1f).toFloat(), 1f)
            maxWidth = dp(206)
            maxLines = 4
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(6f)
        })
        val cta = LinearLayout(host).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            addView(View(host).apply {
                background = CircleArrow()
                TvDesign.decorative(this)
            }, LinearLayout.LayoutParams(dp(27f), dp(27f)))
            addView(label(action, 10f, TvDesign.text, TvDesign.semibold).apply { maxLines = 1 },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    leftMargin = dp(8f)
                })
        }
        return HomePanel(scenery, copy, cta).apply {
            id = View.generateViewId()
            tag = definition.page
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            contentDescription = listOf(s(definition.title), description, action).joinToString(". ")
            accessibilityDelegate = object : View.AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = "android.widget.Button"
                }
            }
            setOnClickListener { host.open(LibraryRoute(definition.page)) }
        }
    }

    private fun header(settings: View): Header {
        val brand = OpenVistaLogo.create(host, 15.4f).apply { setAccessibilityHeading(true) }
        val badge = label(s(R.string.home_badge), 7.6f, TvDesign.ink, TvDesign.semibold).apply {
            TvDesign.caps(this, .1f)
            gravity = Gravity.CENTER
            maxLines = 1
            setPadding(dp(10f), 0, dp(10f), 0)
            background = GradientDrawable().apply { setColor(TvDesign.cream); cornerRadius = dp(10f).toFloat() }
        }
        val tagline = label(s(R.string.home_tagline), 9.6f, 0xF0FFFFFF.toInt(), TvDesign.regular).apply {
            TvDesign.caps(this, .06f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        return Header(brand, badge, tagline, settings)
    }

    private fun footer(resume: View?): Footer {
        val note = label(s(R.string.home_illustrative_scenery), 7.8f, 0xD9FFFFFF.toInt(), TvDesign.regular).apply {
            TvDesign.caps(this, .15f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        return Footer(note, resume)
    }

    private fun settingsButton() = View(host).apply {
        id = View.generateViewId()
        tag = "home_settings"
        contentDescription = s(R.string.settings)
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        background = FocusRing(TvIcon(host, TvIcon.Symbol.SETTINGS, 12.5f, TvDesign.text, 1.7f))
        accessibilityDelegate = buttonRole
        setOnFocusChangeListener { view, _ -> view.invalidate() }
        setOnClickListener { host.open(LibraryRoute("settings")) }
    }

    private fun resumeControl(item: LibraryItem) = TextView(host).apply {
        id = View.generateViewId()
        tag = "resume_last"
        text = host.getString(R.string.home_resume_item, item.title)
        textSize = 7.8f
        typeface = TvDesign.semibold
        TvDesign.caps(this, .15f)
        setTextColor(TvDesign.text)
        gravity = Gravity.CENTER_VERTICAL
        maxLines = 1
        maxWidth = dp(300)
        ellipsize = TextUtils.TruncateAt.END
        includeFontPadding = false
        setCompoundDrawablesRelativeWithIntrinsicBounds(TvIcon(host, TvIcon.Symbol.PLAY, 8f, TvDesign.text, 1.8f), null, null, null)
        compoundDrawablePadding = dp(6f)
        setPadding(dp(9f), dp(5f), dp(9f), dp(5f))
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        background = TvDesign.control(host, base = Color.TRANSPARENT, stroke = Color.TRANSPARENT, radiusDp = 12f)
        contentDescription = "${s(R.string.resume_last)}. ${item.title}"
        accessibilityDelegate = buttonRole
        setOnClickListener {
            host.play(host.store.items().filter { it.family == item.family }, item.id, item.family == MediaFamily.CAMERAS)
        }
    }

    private val buttonRole = object : View.AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.className = "android.widget.Button"
        }
    }

    private fun label(value: String, size: Float, color: Int, face: android.graphics.Typeface) = TextView(host).apply {
        text = value
        textSize = size
        typeface = face
        setTextColor(color)
        includeFontPadding = false
        ellipsize = TextUtils.TruncateAt.END
        TvDesign.decorative(this)
    }

    private class Header(val brand: View, val badge: View, val tagline: View, val settings: View)
    private class Footer(val note: View, val resume: View?)

    /**
     * Fixed positions on the 960 x 540 dp canvas, scaled with the measured height so other
     * aspect ratios keep the same rhythm. Panel copy owns only the band between header and CTA.
     */
    private inner class HomeLayout(
        private val panels: List<HomePanel>,
        private val header: Header,
        private val footer: Footer,
        private val feedback: FrameLayout,
    ) : FrameLayout(host) {
        init {
            setBackgroundColor(TvDesign.background)
            panels.forEach(::addView)
            addView(header.brand); addView(header.badge); addView(header.tagline); addView(header.settings)
            addView(footer.note); footer.resume?.let(::addView)
            addView(feedback)
        }

        private fun k() = if (measuredHeight > 0) measuredHeight / dp(540).toFloat() else 1f
        private fun y(value: Float) = (dp(value) * k()).roundToInt()

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)
            setMeasuredDimension(width, height)
            val panelWidth = width / 3
            panels.forEachIndexed { index, panel ->
                val w = if (index == panels.lastIndex) width - panelWidth * 2 else panelWidth
                panel.measure(exact(w), exact(height))
            }
            val loose = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            header.brand.measure(loose, loose)
            header.badge.measure(loose, exact(dp(20f)))
            header.settings.measure(exact(dp(28f)), exact(dp(28f)))
            header.tagline.measure(atMost(width / 2), loose)
            footer.resume?.measure(atMost(width / 3), loose)
            val noteWidth = width - dp(37f) - dp(38f) - (footer.resume?.measuredWidth?.plus(dp(24)) ?: 0)
            footer.note.measure(atMost(noteWidth.coerceAtLeast(0)), loose)
            feedback.measure(exact((width - dp(37f) * 2).coerceAtLeast(0)), atMost(height / 4))
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val width = right - left
            var x = 0
            panels.forEach { panel -> panel.layout(x, 0, x + panel.measuredWidth, panel.measuredHeight); x += panel.measuredWidth }
            fun centered(view: View, viewLeft: Int, centerY: Int) =
                view.layout(viewLeft, centerY - view.measuredHeight / 2, viewLeft + view.measuredWidth, centerY + (view.measuredHeight + 1) / 2)
            centered(header.brand, dp(36.5f), y(37f))
            header.badge.layout(dp(37.4f), y(84f), dp(37.4f) + header.badge.measuredWidth, y(84f) + header.badge.measuredHeight)
            val settingsRight = width - dp(38f) + dp(7.75f)
            centered(header.settings, settingsRight - header.settings.measuredWidth, y(37f))
            centered(header.tagline, header.settings.left + dp(7.75f) - dp(21.5f) - header.tagline.measuredWidth, y(37f))
            centered(footer.note, dp(37f), y(523f))
            footer.resume?.let { centered(it, width - dp(38f) + dp(9f) - it.measuredWidth, y(523f)) }
            val feedbackTop = y(110f)
            feedback.layout(dp(37f), feedbackTop, dp(37f) + feedback.measuredWidth, feedbackTop + feedback.measuredHeight)
        }
    }

    /** Scenery fills the panel; copy and call to action use measured, non-overlapping bands. */
    private inner class HomePanel(
        private val scenery: SceneryView,
        private val copy: LinearLayout,
        private val cta: LinearLayout,
    ) : FrameLayout(host) {
        private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0x73FFFFFF
            strokeWidth = dp(1.5f).toFloat()
        }
        private val bar = Paint().apply { color = TvDesign.homeFocus }
        private val frame = RectF()

        init {
            setWillNotDraw(false)
            addView(scenery)
            addView(View(host).apply {
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0x66061018, 0x14061018, 0x00000000, 0x33061018, 0xB3061018.toInt()))
                TvDesign.decorative(this)
            })
            addView(copy)
            addView(cta)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)
            setMeasuredDimension(width, height)
            for (index in 0 until 2) getChildAt(index).measure(exact(width), exact(height))
            val textWidth = (width - dp(29f) - dp(24f)).coerceAtLeast(1)
            cta.measure(atMost(textWidth), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            copy.measure(exact(textWidth), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            val width = right - left
            val height = bottom - top
            val k = height / dp(540).toFloat()
            for (index in 0 until 2) getChildAt(index).layout(0, 0, width, height)
            val ctaCenter = (dp(482f) * k).roundToInt()
            cta.layout(dp(29f), ctaCenter - cta.measuredHeight / 2, dp(29f) + cta.measuredWidth,
                ctaCenter - cta.measuredHeight / 2 + cta.measuredHeight)
            val copyTop = (dp(144f) * k).roundToInt()
            // Very large accessibility text shrinks as one block rather than colliding with the CTA.
            val room = (cta.top - dp(20f) - copyTop).coerceAtLeast(1)
            val fit = min(1f, room / copy.measuredHeight.coerceAtLeast(1).toFloat())
            copy.pivotX = 0f
            copy.pivotY = 0f
            copy.scaleX = fit
            copy.scaleY = fit
            copy.layout(dp(29f), copyTop, dp(29f) + copy.measuredWidth, copyTop + copy.measuredHeight)
        }

        override fun dispatchDraw(canvas: Canvas) {
            super.dispatchDraw(canvas)
            if (!isFocused) return
            val barHeight = dp(6.5f).toFloat()
            val inset = dp(6.5f).toFloat()
            val half = outline.strokeWidth / 2
            frame.set(inset + half, inset + half, width - inset - half, height - barHeight)
            canvas.drawRect(frame, outline)
            canvas.drawRect(0f, height - barHeight, width.toFloat(), height.toFloat(), bar)
        }
    }

    private inner class CircleArrow : android.graphics.drawable.Drawable() {
        private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xCCFFFFFF.toInt()
            strokeWidth = dp(1f).toFloat()
        }
        private val arrow = TvIcon(host, TvIcon.Symbol.ARROW_RIGHT, 11f, TvDesign.text, 1.8f)

        override fun draw(canvas: Canvas) {
            val r = min(bounds.width(), bounds.height()) / 2f
            canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), r - ring.strokeWidth / 2, ring)
            val size = dp(11f)
            val cx = bounds.centerX()
            val cy = bounds.centerY()
            arrow.setBounds(cx - size / 2, cy - size / 2, cx - size / 2 + size, cy - size / 2 + size)
            arrow.draw(canvas)
        }
        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
        @Deprecated("Deprecated in Java")
        override fun getOpacity() = android.graphics.PixelFormat.TRANSLUCENT
    }

    /** Settings is an icon-only control; remote focus draws a soft disc and cyan ring. */
    private inner class FocusRing(private val icon: TvIcon) : android.graphics.drawable.Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun isStateful() = true
        override fun onStateChange(state: IntArray): Boolean { invalidateSelf(); return true }
        override fun draw(canvas: Canvas) {
            val focused = state.contains(android.R.attr.state_focused)
            val r = min(bounds.width(), bounds.height()) / 2f
            if (focused) {
                paint.style = Paint.Style.FILL
                paint.color = 0x40FFFFFF
                canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), r, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp(1.5f).toFloat()
                paint.color = TvDesign.accent
                canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), r - paint.strokeWidth / 2, paint)
            }
            val size = dp(12.5f)
            icon.setBounds(bounds.centerX() - size / 2, bounds.centerY() - size / 2,
                bounds.centerX() - size / 2 + size, bounds.centerY() - size / 2 + size)
            icon.draw(canvas)
        }
        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
        @Deprecated("Deprecated in Java")
        override fun getOpacity() = android.graphics.PixelFormat.TRANSLUCENT
    }

    private fun exact(size: Int) = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
    private fun atMost(size: Int) = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST)

    /** Renders all three backdrops once per panel size on a worker; drawing only blits. */
    private fun loadScenery(width: Int, height: Int) {
        if (closed || width <= 0 || height <= 0) return
        // The three panels differ by at most a pixel of width; one render serves all of them.
        if (artworkSize.second == height && (artwork.any { it != null } || artworkTask != null)) return
        artworkSize = width to height
        artworkTask?.cancel(true)
        val resources = host.resources
        artworkTask = artworkWorker.submit {
            val rendered = Array(3) { panel ->
                try { HomeArtwork.render(resources, panel, width, height) } catch (_: RuntimeException) { null }
                catch (_: OutOfMemoryError) { null }
            }
            host.runOnUiThread {
                artworkTask = null
                if (closed || host.isDestroyed || host.isFinishing || artworkSize.second != height) return@runOnUiThread
                artwork = arrayOfNulls<Bitmap>(3).also { rendered.copyInto(it) }
                sceneryViews.forEach(View::invalidate)
            }
        }
    }

    override fun close() {
        closed = true
        artworkTask?.cancel(true)
        artworkWorker.shutdownNow()
        sceneryViews = emptyList()
        artwork = arrayOfNulls(3)
    }

    private inner class SceneryView(private val panel: Int) : View(host) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val target = Rect()
        private val fallback = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, when (panel) {
            0 -> intArrayOf(0xFF33403F.toInt(), 0xFF1E2A28.toInt(), 0xFF0E1715.toInt())
            1 -> intArrayOf(0xFF0B3646.toInt(), 0xFF0C3B36.toInt(), 0xFF07150F.toInt())
            else -> intArrayOf(0xFF2E241F.toInt(), 0xFF6B4D3D.toInt(), 0xFF2A1F1A.toInt())
        })

        init { TvDesign.decorative(this) }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            loadScenery(w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val bitmap = artwork[panel]
            target.set(0, 0, width, height)
            if (bitmap == null) {
                fallback.bounds = target
                fallback.draw(canvas)
            } else canvas.drawBitmap(bitmap, null, target, paint)
        }
    }

    private fun s(resource: Int) = host.getString(resource)
    private fun dp(value: Int) = TvDesign.dp(host, value)
    private fun dp(value: Float) = TvDesign.dp(host, value)

    private data class Panel(
        val page: String, val title: Int, val eyebrow: Int, val icon: TvIcon.Symbol, val count: Int,
        val emptyDescription: Int, val description: Int, val emptyAction: Int, val action: Int,
    )
}
