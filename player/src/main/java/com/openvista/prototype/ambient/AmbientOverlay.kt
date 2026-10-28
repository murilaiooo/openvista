package com.openvista.prototype.ambient

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.openvista.prototype.R
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import com.openvista.prototype.library.MediaFamily
import com.openvista.prototype.ui.OpenVistaLogo
import com.openvista.prototype.ui.TvDesign
import com.openvista.prototype.ui.TvIcon
import java.util.Date
import java.util.TimeZone
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Four quiet information zones over live video; no decoder, focus or input ownership.
 * Every group is built at its reference size on the 960 x 540 dp canvas and scaled as a whole,
 * so a long place name or a seven-day forecast shrinks instead of being cut off.
 */
class AmbientOverlay(context: Context, private val store: LibraryStore) : FrameLayout(context) {
    private val main = Handler(Looper.getMainLooper())
    private val presentation = AmbientPresentation(context)
    private val brand = OpenVistaLogo.create(context, 15.5f)
    private val source = SourceGroup()
    private val viewer = ViewerGroup()
    private val forecastPanel = ForecastGroup()
    private val sceneContrast = View(context).apply {
        background = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(0x66060B12, 0x24060B12, 0x00000000, 0x00000000))
    }
    private val haze = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = RadialGradient(0f, 0f, 1f, intArrayOf(0x5C060D16, 0x33060D16, 0x00060D16),
            floatArrayOf(0f, .55f, 1f), Shader.TileMode.CLAMP)
    }
    private val hazeGroups: List<View> = listOf(source, viewer, forecastPanel, brand)
    private val sourceWeather = WeatherSlot()
    private val viewerWeather = WeatherSlot()
    private var item: LibraryItem? = null
    private var running = false
    private var live = false
    private var playing = false
    private var buffering = true
    private var interactionAt = SystemClock.elapsedRealtime()
    private var movementAt = interactionAt
    private var cycle = 0
    private var shownCycle = 0
    private var motionEnabled = false
    private var motionAlpha = 1f
    private var transition: ValueAnimator? = null
    private var hidden = false
    private var controlsVisible = false
    private var forecastKey = ""
    private var settings = store.settings()
    private var safeX = 0
    private var safeY = 0
    private var travel = 0
    private var compositionWidth = 0
    private var compositionHeight = 0
    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            update()
            schedule()
        }
    }

    init {
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        setWillNotDraw(false)
        addView(sceneContrast, LayoutParams(-1, -1))
        addView(brand)
        addView(source)
        addView(viewer)
        addView(forecastPanel)
    }

    fun start() {
        if (running) return
        running = true
        interactionAt = SystemClock.elapsedRealtime()
        movementAt = interactionAt
        sourceWeather.requestAt = Long.MIN_VALUE
        viewerWeather.requestAt = Long.MIN_VALUE
        main.post(tick)
    }

    fun stop() {
        running = false
        main.removeCallbacksAndMessages(null)
        animate().cancel()
        transition?.cancel()
        transition = null
        motionAlpha = 1f
        shownCycle = cycle
        sourceWeather.repository.cancel()
        viewerWeather.repository.cancel()
    }

    fun updateItem(item: LibraryItem?) {
        if (this.item?.id != item?.id) { live = false; playing = false; buffering = true }
        this.item = item
        if (running) update()
    }

    /** LIVE comes from the actual Media3 timeline, never from an HLS file extension. */
    fun updatePlayback(isLive: Boolean, isPlaying: Boolean, isBuffering: Boolean = false) {
        if (live == isLive && playing == isPlaying && buffering == isBuffering) return
        live = isLive
        playing = isPlaying
        buffering = isBuffering
        if (running) update()
    }

    fun userInteraction() {
        interactionAt = SystemClock.elapsedRealtime()
        if (running) { update(); schedule() }
    }

    fun setControlsVisible(visible: Boolean) {
        controlsVisible = visible
        if (running) update()
    }

    private fun schedule() {
        main.removeCallbacks(tick)
        val now = SystemClock.elapsedRealtime()
        var delay = 60_000L - (System.currentTimeMillis() % 60_000L)
        if (settings.moveOverlay) delay = minOf(delay,
            (movementAt + settings.overlayMoveMinutes * 60_000L - now).coerceAtLeast(50))
        if (settings.hideOverlay && !hidden) delay = minOf(delay,
            (interactionAt + settings.overlayHideSeconds * 1000L - now).coerceAtLeast(50))
        main.postDelayed(tick, delay.coerceAtLeast(50))
    }

    private fun update() {
        settings = store.settings()
        val now = SystemClock.elapsedRealtime()
        val shouldHide = settings.hideOverlay && now - interactionAt >= settings.overlayHideSeconds * 1000L
        if (hidden != shouldHide) {
            hidden = shouldHide
            animate().cancel()
            if (hidden) animate().alpha(0f).setDuration(200).withEndAction { visibility = View.INVISIBLE }.start()
            else { visibility = View.VISIBLE; alpha = 0f; animate().alpha(1f).setDuration(200).start() }
        }
        if (settings.moveOverlay != motionEnabled) {
            motionEnabled = settings.moveOverlay
            movementAt = now
            cycle = 0
            shownCycle = 0
        } else if (motionEnabled && now - movementAt >= settings.overlayMoveMinutes * 60_000L) {
            cycle = (cycle + 1) % AmbientPlacement.CYCLE_LENGTH
            movementAt = now
            if (!hidden && isShown) crossFade() else shownCycle = cycle
        }
        val cameraLocation = item?.let { source ->
            val latitude = source.latitude
            val longitude = source.longitude
            if (latitude != null && longitude != null) WeatherLocation(
                listOf(source.city, source.country).filter(String::isNotBlank).joinToString(", "),
                latitude, longitude, source.timeZone) else null
        }
        val viewerLocation = settings.let { s ->
            if (s.viewerLatitude != null && s.viewerLongitude != null)
                WeatherLocation(s.viewerCity, s.viewerLatitude, s.viewerLongitude, s.viewerTimeZone) else null
        }
        val forecastCamera = settings.weatherContext == "camera" && cameraLocation != null
        val sharedWeather = cameraLocation != null && viewerLocation != null &&
            coordinateKey(cameraLocation) == coordinateKey(viewerLocation)
        // Assign by coordinates, not by screen zone. Entering/leaving a shared-location
        // composition must not cancel the one in-flight request that the other zone still needs.
        val slots = listOf(sourceWeather, viewerWeather)
        val existingSource = slots.firstOrNull { it.matches(cameraLocation) }
        val viewerSlot = slots.firstOrNull { it.matches(viewerLocation) }
            ?: existingSource?.let { reserved -> slots.first { it !== reserved } } ?: viewerWeather
        val sourceSlot = if (sharedWeather) viewerSlot else
            slots.firstOrNull { it !== viewerSlot && it.matches(cameraLocation) } ?: slots.first { it !== viewerSlot }
        val viewerNeeded = settings.showWeather && settings.showViewerInfo || settings.showForecast && !forecastCamera
        if (!hidden) {
            sourceSlot.load(cameraLocation, settings.showWeather && settings.showSourceInfo ||
                settings.showForecast && forecastCamera || sharedWeather && viewerNeeded, now)
            if (sourceSlot !== viewerSlot) viewerSlot.load(viewerLocation, viewerNeeded, now)
            slots.filter { it !== sourceSlot && it !== viewerSlot }.forEach { it.load(null, false, now) }
        }
        val current = Date()
        val sourceZone = item?.timeZone?.let(presentation::zone)
            ?: sourceSlot.forecast?.takeIf { forecast ->
                cameraLocation != null && coordinateKey(forecast.location) == coordinateKey(cameraLocation)
            }?.location?.timeZone?.let(presentation::zone)
        val viewerZone = presentation.zone(settings.viewerTimeZone) ?: TimeZone.getDefault()
        val sourcePlace = item?.let { placeLine(it, sourceZone) } ?: ("" to "")
        val liveStatus = live && playing && item?.family == MediaFamily.CAMERAS
        val sourceStatus = context.getString(when {
            liveStatus -> R.string.ambient_live
            playing -> R.string.ambient_playing
            buffering -> R.string.playback_connecting
            else -> R.string.ambient_paused
        })
        source.bind(sourceStatus, liveStatus,
            if (settings.showLocation) sourcePlace else ("" to ""),
            sourceZone, current, sourceSlot, settings.showSourceInfo && item != null)
        val viewerConfigured = settings.viewerCity.isNotBlank() || viewerLocation != null || settings.viewerTimeZone.isNotBlank()
        val deviceClockOnly = settings.showClock && !settings.showSourceInfo && !viewerConfigured
        viewer.bind(context.getString(if (viewerConfigured) R.string.ambient_your_location else R.string.ambient_device_time),
            if (settings.showLocation) settings.viewerCity else "", viewerZone, current, viewerSlot,
            settings.showViewerInfo && (viewerConfigured || deviceClockOnly))
        brand.visibility = if (settings.showBrand) View.VISIBLE else View.GONE
        bindForecast(if (forecastCamera) sourceSlot else viewerSlot, if (forecastCamera) sourceZone else viewerZone,
            if (forecastCamera) cameraLocation?.name else settings.viewerCity)
        // Contextual controls keep their own timeout and focus; information clears their space.
        if (controlsVisible) { viewer.visibility = View.GONE; forecastPanel.visibility = View.GONE }
        bindCredit(listOf(sourceSlot to (source.visibility == View.VISIBLE && settings.showWeather),
            viewerSlot to (viewer.visibility == View.VISIBLE && settings.showWeather),
            (if (forecastCamera) sourceSlot else viewerSlot) to (forecastPanel.visibility == View.VISIBLE)))
        applyAlpha()
        source.tint(settings.overlayColor)
        viewer.tint(settings.overlayColor)
        forecastPanel.tint(settings.overlayColor)
        requestLayout()
        invalidate()
    }

    /** Every provider whose data is on screen is credited once, under the lowest visible group. */
    private fun bindCredit(uses: List<Pair<WeatherSlot, Boolean>>) {
        val credit = uses.filter { it.second }.mapNotNull { (slot, _) ->
            slot.forecast?.takeIf { it.requiresVisibleAttribution }?.attribution
        }.distinct().joinToString("  ·  ")
        val owner = when {
            forecastPanel.visibility == View.VISIBLE -> forecastPanel.credit
            viewer.visibility == View.VISIBLE -> viewer.credit
            else -> source.credit
        }
        listOf(forecastPanel.credit, viewer.credit, source.credit).forEach { it.put(if (it === owner) credit else "") }
    }

    private fun applyAlpha() {
        val global = settings.overlayOpacity.coerceIn(10, 100) / 100f * motionAlpha
        brand.alpha = global * settings.brandOpacity / 100f
        source.alpha = global * settings.sourceOpacity / 100f
        viewer.alpha = global * settings.viewerOpacity / 100f
        forecastPanel.alpha = global * settings.forecastOpacity / 100f
        invalidate()
    }

    /** Fade out, move every group to the next composition and drift, fade back in; video keeps playing. */
    private fun crossFade() {
        transition?.cancel()
        var moved = false
        transition = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1_100L
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val t = animator.animatedValue as Float
                if (t >= .45f && !moved) { moved = true; shownCycle = cycle; requestLayout() }
                motionAlpha = when {
                    t < .4f -> 1f - t / .4f
                    t < .5f -> 0f
                    else -> (t - .5f) / .5f
                }
                applyAlpha()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    shownCycle = cycle; motionAlpha = 1f; transition = null; applyAlpha(); requestLayout()
                }
            })
            start()
        }
    }

    private fun bindForecast(slot: WeatherSlot, zone: TimeZone?, locationName: String?) {
        forecastPanel.visibility = if (settings.showForecast) View.VISIBLE else View.GONE
        val timeZone = zone ?: slot.forecast?.location?.timeZone?.let(presentation::zone) ?: TimeZone.getDefault()
        val candidates = presentation.days(slot.forecast, (settings.viewerForecastDays + 1).coerceAtMost(7), timeZone)
        val today = java.time.LocalDate.now(timeZone.toZoneId())
        val days = (if (candidates.firstOrNull()?.date == today.toString()) candidates.drop(1) else candidates)
            .take(settings.viewerForecastDays)
        val title = locationName?.takeIf { settings.showLocation && it.isNotBlank() }?.let { context.getString(R.string.ambient_forecast_for, it) }
            ?: context.getString(R.string.ambient_forecast_days, settings.viewerForecastDays)
        forecastPanel.title.put(if (days.isEmpty()) title else if (slot.stale) context.getString(R.string.weather_cached) else "")
        forecastPanel.state.put(if (days.isEmpty()) slot.message() else "")
        val key = days.hashCode().toString() + "/" + timeZone.id + "/" + settings.temperatureUnit + "/" +
            settings.overlayColor + "/" + today
        if (forecastKey != key) {
            forecastKey = key
            forecastPanel.bind(days, timeZone)
        }
    }

    /** Reference canvas: 960 x 540 dp, scaled with the overlay and the user's text size. */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, h)
        sceneContrast.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
        val canvas = min(w / dp(960f).toFloat(), h / dp(540f).toFloat()).coerceAtLeast(.1f)
        val scale = canvas * settings.overlaySize.coerceIn(14, 40) / 20f
        safeX = (dp(42f) * canvas).roundToInt()
        safeY = (dp(25f) * canvas).roundToInt()
        // Reserved drift travel: about 1.5 % of the shorter side, on every edge.
        travel = (h * .015f).roundToInt()
        compositionWidth = (w - 2 * (safeX + travel)).coerceAtLeast(0)
        compositionHeight = (h - 2 * (safeY + travel)).coerceAtLeast(0)
        val gap = (dp(24f) * scale).roundToInt()
        fit(brand, scale, compositionWidth * .28f, compositionHeight * .12f)
        fit(source, scale, compositionWidth * .44f, compositionHeight * .46f)
        fit(viewer, scale, compositionWidth * .46f, compositionHeight * .40f)
        val forecastRoom = (compositionWidth - drawnWidth(viewer) - gap).coerceAtLeast(0)
        forecastPanel.prepare(forecastRoom / scale)
        fit(forecastPanel, scale, forecastRoom.toFloat(), compositionHeight * .40f)
    }

    /** Measure at natural size, then scale the whole group to fit its allocation without clipping. */
    private fun fit(view: View, scale: Float, maxWidth: Float, maxHeight: Float) {
        val unspecified = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        if (view.visibility == View.GONE) {
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))
            return
        }
        // The same UNSPECIFIED spec would otherwise return a size cached before new data was bound.
        forceLayoutTree(view)
        view.measure(unspecified, unspecified)
        val fitted = minOf(scale,
            if (view.measuredWidth > 0) maxWidth / view.measuredWidth else scale,
            if (view.measuredHeight > 0) maxHeight / view.measuredHeight else scale).coerceAtLeast(0f)
        view.pivotX = 0f
        view.pivotY = 0f
        view.scaleX = fitted
        view.scaleY = fitted
    }

    private fun forceLayoutTree(view: View) {
        view.forceLayout()
        if (view is ViewGroup) for (index in 0 until view.childCount) forceLayoutTree(view.getChildAt(index))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        sceneContrast.layout(0, 0, measuredWidth, measuredHeight)
        val moving = settings.moveOverlay
        val positions = AmbientPlacement.calculate(
            left = safeX + travel,
            top = safeY + travel,
            width = compositionWidth,
            height = compositionHeight,
            brand = OverlayExtent(drawnWidth(brand), drawnHeight(brand)),
            source = OverlayExtent(drawnWidth(source), drawnHeight(source)),
            viewer = OverlayExtent(drawnWidth(viewer), drawnHeight(viewer)),
            forecast = OverlayExtent(drawnWidth(forecastPanel), drawnHeight(forecastPanel)),
            phase = if (moving) AmbientPlacement.phase(shownCycle) else 0,
            corner = settings.overlayCorner,
            controlsVisible = controlsVisible,
            insetX = (dp(18f) * settings.overlayX.coerceIn(0, 100) / 100f).roundToInt(),
            insetY = (dp(18f) * settings.overlayY.coerceIn(0, 100) / 100f).roundToInt(),
            drift = if (moving) AmbientPlacement.drift(shownCycle, travel) else OverlayOrigin(0, 0),
        )
        fun place(view: View, origin: OverlayOrigin) =
            view.layout(origin.x, origin.y, origin.x + view.measuredWidth, origin.y + view.measuredHeight)
        place(brand, positions.brand)
        place(source, positions.source)
        place(viewer, positions.viewer)
        place(forecastPanel, positions.forecast)
    }

    /** A feathered haze behind each group moves with it; it never covers the whole frame. */
    override fun dispatchDraw(canvas: Canvas) {
        sceneContrast.draw(canvas)
        hazeGroups.forEach { group ->
            if (group.visibility != View.VISIBLE || group.alpha <= 0f) return@forEach
            val width = drawnWidth(group).toFloat()
            val height = drawnHeight(group).toFloat()
            if (width <= 0f || height <= 0f) return@forEach
            haze.alpha = (255 * group.alpha.coerceIn(0f, 1f) * if (group === brand) .5f else 1f).roundToInt()
            val save = canvas.save()
            canvas.translate(group.left + width / 2, group.top + height / 2)
            canvas.scale(width * .78f + dp(24f), height * .82f + dp(24f))
            canvas.drawCircle(0f, 0f, 1f, haze)
            canvas.restoreToCount(save)
        }
        for (index in 1 until childCount) drawChild(canvas, getChildAt(index), drawingTime)
    }

    override fun onDetachedFromWindow() {
        stop()
        sourceWeather.repository.close()
        viewerWeather.repository.close()
        super.onDetachedFromWindow()
    }

    private inner class WeatherSlot {
        val repository = WeatherRepository(context)
        var target: WeatherLocation? = null
        var requestAt = Long.MIN_VALUE
        var forecast: Forecast? = null
        var state: WeatherState = WeatherState.NoLocation
        var stale = false
        fun matches(location: WeatherLocation?) = target?.let(::coordinateKey) == location?.let(::coordinateKey)
        fun load(location: WeatherLocation?, wanted: Boolean, now: Long) {
            val sameCoordinates = matches(location)
            if (!sameCoordinates) {
                repository.cancel()
                target = location; forecast = null; stale = false
                state = WeatherState.NoLocation; requestAt = Long.MIN_VALUE
            }
            // Names and selected timezone can change without restarting a coordinate request.
            target = location
            if (!wanted) return
            val refreshMs = if (state == WeatherState.Unavailable) 300_000 else 1_800_000
            if (requestAt != Long.MIN_VALUE && now - requestAt < refreshMs) return
            requestAt = now
            repository.load(location) { result ->
                if (!running || target?.let(::coordinateKey) != location?.let(::coordinateKey)) return@load
                state = result
                if (result is WeatherState.Ready) { forecast = result.forecast; stale = result.stale }
                else if (result != WeatherState.Loading) { forecast = null; stale = false }
                // Loading is synchronous; avoid reentrant source/viewer updates.
                main.post { if (running) update() }
            }
        }
        fun message(): String = context.getString(when (state) {
            WeatherState.Loading -> R.string.weather_loading
            WeatherState.NoLocation -> R.string.weather_no_location
            WeatherState.ProviderRequired -> R.string.weather_provider_required
            else -> R.string.weather_unavailable
        })
    }

    private fun clockText(zone: TimeZone, now: Date): CharSequence {
        val value = presentation.clock(zone, settings, now)
        // A 12-hour suffix sits small beside the digits, as in the reference clocks.
        val split = value.indexOfFirst { it.isLetter() }
        if (split <= 0) return value
        return SpannableString(value).apply {
            setSpan(RelativeSizeSpan(.3f), split, value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /** Source header, large thin clock, date, then current weather with humidity and wind. */
    private inner class SourceGroup : LinearLayout(context) {
        private val dot = View(context).apply {
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(TvDesign.live) }
        }
        private val status = label(10.3f, TvDesign.medium).apply { letterSpacing = .04f }
        private val divider = View(context).apply { setBackgroundColor(0x80FFFFFF.toInt()) }
        // A long city ellipsizes first; the country and its flag are always shown in full.
        private val city = label(12.6f).apply { maxWidth = dp(190f); maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END }
        private val country = label(12.6f).apply { maxLines = 1 }
        private val header = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        private val clock = label(65f, TvDesign.extraLight).apply { letterSpacing = -.02f; maxLines = 1 }
        private val date = label(12f)
        private val symbol = WeatherSymbol(context)
        private val temperature = label(15f, TvDesign.light)
        private val condition = label(10f).apply { maxWidth = dp(118f) }
        private val rule = View(context).apply { setBackgroundColor(0x66FFFFFF) }
        private val humidityIcon = PrecipitationDrop(context)
        private val humidity = label(11f)
        private val windIcon = View(context).apply { background = TvIcon(context, TvIcon.Symbol.WIND, 9f, 0xFFE8EDF0.toInt(), 1.8f) }
        private val wind = label(11f)
        private val metrics = LinearLayout(context).apply { orientation = VERTICAL }
        private val weatherRow = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        val credit = label(7.5f)

        init {
            orientation = VERTICAL
            header.addView(dot, LayoutParams(dp(6f), dp(6f)).apply { marginEnd = dp(6f) })
            header.addView(status)
            header.addView(divider, LayoutParams(dp(1f), dp(11f)).apply { marginStart = dp(10f); marginEnd = dp(10f) })
            header.addView(city)
            header.addView(country)
            // Wrap width: a default MATCH_PARENT header would be squeezed to the clock's width.
            addView(header, LayoutParams(-2, -2))
            addView(clock, LayoutParams(-2, -2).apply { topMargin = -dp(1f) })
            addView(date, LayoutParams(-2, -2).apply { topMargin = -dp(5f) })
            val temps = LinearLayout(context).apply { orientation = VERTICAL; addView(temperature); addView(condition) }
            weatherRow.addView(symbol, LayoutParams(dp(21f), dp(21f)).apply { marginEnd = dp(9f) })
            weatherRow.addView(temps)
            weatherRow.addView(rule, LayoutParams(dp(1f), dp(30f)).apply { marginStart = dp(16f); marginEnd = dp(14f) })
            metrics.addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(humidityIcon, LayoutParams(dp(6f), dp(7.5f)).apply { marginEnd = dp(7f) })
                addView(humidity)
            })
            metrics.addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(windIcon, LayoutParams(dp(9f), dp(9f)).apply { marginEnd = dp(5f) })
                addView(wind)
            }, LayoutParams(-2, -2).apply { topMargin = dp(3f) })
            weatherRow.addView(metrics)
            addView(weatherRow, LayoutParams(-2, -2).apply { topMargin = dp(12f) })
            addView(credit, LayoutParams(-2, -2).apply { topMargin = dp(6f) })
        }

        fun bind(caption: String, isLive: Boolean, location: Pair<String, String>, zone: TimeZone?, now: Date, slot: WeatherSlot, visible: Boolean) {
            visibility = if (visible) View.VISIBLE else View.GONE
            dot.visibility = if (isLive) View.VISIBLE else View.GONE
            status.put(caption)
            status.setTextColor(if (isLive) TvDesign.live else settings.overlayColor)
            city.put(location.first)
            country.put(when {
                location.second.isBlank() -> ""
                location.first.isBlank() -> location.second
                else -> ", " + location.second
            })
            divider.visibility = if (location.first.isBlank() && location.second.isBlank()) View.GONE else View.VISIBLE
            clock.put(if (settings.showClock && zone != null) clockText(zone, now) else "")
            date.put(if (settings.showDate && zone != null) presentation.date(zone, settings, now) else "")
            val current = slot.forecast?.current
            weatherRow.visibility = if (settings.showWeather) View.VISIBLE else View.GONE
            symbol.bind(current?.weatherCode, current?.description)
            symbol.tint(settings.overlayColor)
            symbol.visibility = if (current == null) View.GONE else View.VISIBLE
            temperature.put(current?.let { presentation.temperature(it.temperatureC, settings) }.orEmpty())
            condition.put(if (current == null) slot.message() else WeatherText.description(context, current) +
                (if (slot.stale) " · " + context.getString(R.string.weather_cached) else ""))
            humidity.put(current?.humidity?.let(presentation::percent).orEmpty())
            wind.put(current?.windKph?.let { presentation.speed(it, settings) }.orEmpty())
            humidityIcon.visibility = humidity.visibility
            windIcon.visibility = wind.visibility
            val metricsShown = humidity.visibility == View.VISIBLE || wind.visibility == View.VISIBLE
            metrics.visibility = if (metricsShown) View.VISIBLE else View.GONE
            rule.visibility = metrics.visibility
        }

        fun tint(color: Int) {
            listOf(city, country, clock, temperature, humidity, wind).forEach { it.setTextColor(color) }
            listOf(date, condition, credit).forEach { it.setTextColor(color and 0x00FFFFFF or (0xD9 shl 24)) }
            if (dot.visibility != View.VISIBLE) status.setTextColor(color)
        }
    }

    /** "Your location", city, clock and date, then a divider and the viewer's current weather. */
    private inner class ViewerGroup : LinearLayout(context) {
        private val eyebrowIcon = View(context).apply { background = TvIcon(context, TvIcon.Symbol.NAVIGATE, 8f, 0xFFE3E8EC.toInt(), 2f) }
        private val eyebrow = label(10f)
        private val place = label(18f, TvDesign.light).apply { maxWidth = dp(260f) }
        private val clock = label(57f, TvDesign.extraLight).apply { letterSpacing = -.02f; maxLines = 1 }
        private val date = label(12f)
        private val rule = View(context).apply { setBackgroundColor(0x66FFFFFF) }
        private val symbol = WeatherSymbol(context)
        private val temperature = label(24f, TvDesign.light)
        private val condition = label(11.5f).apply { maxWidth = dp(170f) }
        private val highs = label(11.5f).apply { fontFeatureSettings = "tnum" }
        private val weather = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        val credit = label(7.5f)

        init {
            orientation = VERTICAL
            val time = LinearLayout(context).apply { orientation = VERTICAL }
            time.addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(eyebrowIcon, LayoutParams(dp(8f), dp(8f)).apply { marginEnd = dp(6f) })
                addView(eyebrow)
            })
            time.addView(place, LayoutParams(-2, -2).apply { topMargin = dp(3f) })
            time.addView(clock, LayoutParams(-2, -2).apply { topMargin = dp(1f) })
            time.addView(date, LayoutParams(-2, -2).apply { topMargin = -dp(4f) })
            val texts = LinearLayout(context).apply { orientation = VERTICAL; addView(temperature); addView(condition); addView(highs) }
            weather.addView(symbol, LayoutParams(dp(36f), dp(36f)).apply { marginEnd = dp(12f) })
            weather.addView(texts)
            val row = LinearLayout(context).apply { gravity = Gravity.BOTTOM }
            row.addView(time)
            row.addView(rule, LayoutParams(dp(1f), dp(68f)).apply { marginStart = dp(26f); marginEnd = dp(30f); bottomMargin = dp(2f) })
            row.addView(weather, LayoutParams(-2, -2).apply { bottomMargin = dp(4f) })
            addView(row, LayoutParams(-2, -2))
            addView(credit, LayoutParams(-2, -2).apply { topMargin = dp(6f) })
        }

        fun bind(caption: String, location: String, zone: TimeZone, now: Date, slot: WeatherSlot, visible: Boolean) {
            visibility = if (visible) View.VISIBLE else View.GONE
            eyebrow.put(caption)
            place.put(location)
            clock.put(if (settings.showClock) clockText(zone, now) else "")
            date.put(if (settings.showDate) presentation.date(zone, settings, now) else "")
            val current = slot.forecast?.current
            val shown = settings.showWeather
            weather.visibility = if (shown) View.VISIBLE else View.GONE
            rule.visibility = weather.visibility
            symbol.bind(current?.weatherCode, current?.description)
            symbol.tint(settings.overlayColor)
            symbol.visibility = if (current == null) View.GONE else View.VISIBLE
            temperature.put(current?.let { presentation.temperature(it.temperatureC, settings) }.orEmpty())
            condition.put(if (current == null) slot.message() else WeatherText.description(context, current) +
                (if (slot.stale) " · " + context.getString(R.string.weather_cached) else ""))
            val today = slot.forecast?.days?.firstOrNull { day ->
                runCatching { java.time.LocalDate.parse(day.date) }.getOrNull() == java.time.LocalDate.now(zone.toZoneId())
            }
            highs.put(if (today == null) "" else context.getString(R.string.ambient_high_low,
                presentation.temperature(today.maximumC, settings, false), presentation.temperature(today.minimumC, settings, false)))
        }

        fun tint(color: Int) {
            listOf(place, clock, temperature, highs).forEach { it.setTextColor(color) }
            listOf(eyebrow, date, condition, credit).forEach { it.setTextColor(color and 0x00FFFFFF or (0xD9 shl 24)) }
        }
    }

    /** Upcoming days in equal columns separated by hairlines; the row narrows before it scales. */
    private inner class ForecastGroup : LinearLayout(context) {
        val title = label(11f)
        val state = label(12f).apply { maxWidth = dp(300f) }
        private val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        private val cells = List(7) { ForecastCell() }
        private val dividers = List(6) { View(context).apply { setBackgroundColor(0x4DFFFFFF) } }
        val credit = label(7.5f).apply { gravity = Gravity.END }
        private var shown = 0

        init {
            orientation = VERTICAL
            gravity = Gravity.END
            addView(title, LayoutParams(-2, -2))
            addView(state, LayoutParams(-2, -2))
            cells.forEachIndexed { index, cell ->
                if (index > 0) row.addView(dividers[index - 1], LayoutParams(dp(1f), dp(62f)))
                row.addView(cell, LayoutParams(dp(98f), -2))
            }
            // Explicit wrap width: a default MATCH_PARENT row would be squeezed to the credit line's width.
            addView(row, LayoutParams(-2, -2))
            addView(credit, LayoutParams(-2, -2).apply { topMargin = dp(6f) })
        }

        fun bind(days: List<ForecastDay>, zone: TimeZone) {
            shown = days.size
            cells.forEachIndexed { index, cell ->
                val day = days.getOrNull(index)
                cell.visibility = if (day == null) View.GONE else View.VISIBLE
                if (day != null) cell.bind(day, zone)
            }
            dividers.forEachIndexed { index, divider -> divider.visibility = if (index + 1 < days.size) View.VISIBLE else View.GONE }
            row.visibility = if (days.isEmpty()) View.GONE else View.VISIBLE
        }

        /** Columns use the reference width when there is room and narrow to 72 dp before scaling. */
        fun prepare(room: Float) {
            val count = shown.coerceAtLeast(1)
            val width = ((room - (count - 1) * dp(1f)) / count).coerceIn(dp(72f).toFloat(), dp(98f).toFloat()).roundToInt()
            cells.forEach { cell -> (cell.layoutParams as LayoutParams).width = width }
        }

        fun tint(color: Int) {
            title.setTextColor(color)
            listOf(state, credit).forEach { it.setTextColor(color and 0x00FFFFFF or (0xD9 shl 24)) }
            cells.forEach { it.tint(color) }
        }
    }

    private inner class ForecastCell : LinearLayout(context) {
        private val day = label(10.5f)
        private val symbol = WeatherSymbol(context)
        private val temperatures = label(12.5f, TvDesign.medium).apply { fontFeatureSettings = "tnum" }
        private val rain = label(10f).apply { fontFeatureSettings = "tnum" }
        private val rainMark = PrecipitationDrop(context)
        private var value: ForecastDay? = null

        init {
            orientation = VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            // Wrap-width children so day, icon, temperatures and rain share one centered column.
            addView(day, LayoutParams(-2, -2))
            addView(symbol, LayoutParams(dp(23f), dp(23f)).apply { topMargin = dp(5f); bottomMargin = dp(5f) })
            addView(temperatures, LayoutParams(-2, -2))
            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(rainMark, LayoutParams(dp(6f), dp(7.5f)).apply { marginEnd = dp(5f) })
                addView(rain)
            }, LayoutParams(-2, -2).apply { topMargin = dp(4f) })
        }

        fun bind(day: ForecastDay, zone: TimeZone) {
            value = day
            this.day.put(presentation.dayLabel(day.date, zone))
            rain.put(presentation.percent(day.precipitationChancePct))
            symbol.bind(day.weatherCode, day.description)
            symbol.tint(settings.overlayColor)
            tint(settings.overlayColor)
            contentDescription = listOf(this.day.text, day.description.orEmpty(), temperatures.text,
                context.getString(R.string.ambient_rain_chance, rain.text)).joinToString(", ")
        }

        fun tint(color: Int) {
            val day = value ?: return
            val highs = presentation.temperature(day.maximumC, settings, false)
            val lows = presentation.temperature(day.minimumC, settings, false)
            temperatures.text = SpannableString("$highs  $lows").apply {
                setSpan(ForegroundColorSpan(color and 0x00FFFFFF or (0xB3 shl 24)), highs.length + 2, length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            temperatures.setTextColor(color)
            this.day.setTextColor(color)
            rain.setTextColor(color and 0x00FFFFFF or (0xD9 shl 24))
        }
    }

    private fun label(size: Float, face: android.graphics.Typeface = TvDesign.regular) = TextView(context).apply {
        textSize = size
        typeface = face
        setTextColor(TvDesign.text)
        includeFontPadding = false
        setShadowLayer(dp(3f).toFloat(), 0f, dp(.5f).toFloat(), 0x8C000000.toInt())
    }
    private fun TextView.put(value: CharSequence) {
        if (text.toString() != value.toString() || text.javaClass != value.javaClass) text = value
        visibility = if (value.isBlank()) View.GONE else View.VISIBLE
    }
    /**
     * "City, Country 🇮🇹" from the camera's own metadata: the country may be saved in any language
     * or as a code, and otherwise comes from the camera's time zone. The name follows the UI locale.
     */
    private fun placeLine(item: LibraryItem, zone: TimeZone?): Pair<String, String> {
        val code = CountryResolver.resolve(item.country, item.timeZone.ifBlank { zone?.id })
        val name = CountryResolver.displayName(item.country, code, context.resources.configuration.locales[0])
        val flag = code?.let(CountryResolver::flag).orEmpty()
        return item.city.trim() to (name + if (flag.isNotEmpty()) "  $flag" else "").trim()
    }
    private fun dp(value: Float) = TvDesign.dp(context, value)
    private fun drawnHeight(view: View) = if (view.visibility == View.GONE) 0 else (view.measuredHeight * view.scaleY).roundToInt()
    private fun drawnWidth(view: View) = if (view.visibility == View.GONE) 0 else (view.measuredWidth * view.scaleX).roundToInt()
    private fun coordinateKey(location: WeatherLocation) = java.lang.String.format(java.util.Locale.US, "%.4f/%.4f", location.latitude, location.longitude)
}
