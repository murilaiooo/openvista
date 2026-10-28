package com.openvista.prototype.ambient

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.openvista.prototype.R
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import com.openvista.prototype.library.MediaFamily
import com.openvista.prototype.ui.TvDesign
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToInt

/** Four quiet information zones; no decoder, focus or input ownership. */
class AmbientOverlay(context: Context, private val store: LibraryStore) : FrameLayout(context) {
    private val main = Handler(Looper.getMainLooper())
    private val presentation = AmbientPresentation(context)
    private val brand = label("openvista", 21f).apply { typeface = TvDesign.light; letterSpacing = .08f }
    private val source = LocationGroup()
    private val viewer = LocationGroup()
    private val forecastPanel = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        background = subtleAmbientScrim()
        setPadding(dp(8), dp(5), dp(8), dp(5))
    }
    private val forecastTitle = label("", 11f)
    private val forecastState = label("", 13f)
    private val forecastRow = LinearLayout(context)
    private val forecastCredit = label("", 10f)
    private val cells = List(7) { ForecastCell() }
    private val sourceWeather = WeatherSlot()
    private val viewerWeather = WeatherSlot()
    private var item: LibraryItem? = null
    private var running = false
    private var live = false
    private var playing = false
    private var buffering = true
    private var interactionAt = SystemClock.elapsedRealtime()
    private var movementAt = interactionAt
    private var phase = 0
    private var motionEnabled = false
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
        forecastPanel.addView(forecastTitle)
        forecastPanel.addView(forecastState)
        cells.forEach { forecastRow.addView(it, LinearLayout.LayoutParams(0, -2, 1f)) }
        forecastPanel.addView(forecastRow)
        forecastPanel.addView(forecastCredit)
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
            phase = 0
        } else if (motionEnabled && now - movementAt >= settings.overlayMoveMinutes * 60_000L) {
            phase = (phase + 1) % 4
            movementAt = now
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
        val sourcePlace = item?.let { listOf(it.city, it.country).filter(String::isNotBlank).joinToString(", ") }.orEmpty()
        val sourceMode = context.getString(if (item?.family == MediaFamily.VIDEOS) R.string.ambient_local_video else R.string.ambient_camera)
        val sourceStatus = context.getString(when {
            live && playing && item?.family == MediaFamily.CAMERAS -> R.string.ambient_live
            playing -> R.string.ambient_playing
            buffering -> R.string.playback_connecting
            else -> R.string.ambient_paused
        })
        source.bind(sourceMode + " · " + sourceStatus,
            if (settings.showLocation) sourcePlace else "",
            sourceZone, current, sourceSlot, settings.showSourceInfo && item != null)
        val viewerConfigured = settings.viewerCity.isNotBlank() || viewerLocation != null || settings.viewerTimeZone.isNotBlank()
        val deviceClockOnly = settings.showClock && !settings.showSourceInfo && !viewerConfigured
        viewer.bind(context.getString(if (viewerConfigured) R.string.ambient_your_location else R.string.ambient_device_time),
            if (settings.showLocation) settings.viewerCity else "", viewerZone, current, viewerSlot,
            settings.showViewerInfo && (viewerConfigured || deviceClockOnly))
        brand.visibility = if (settings.showBrand) View.VISIBLE else View.GONE
        val global = settings.overlayOpacity.coerceIn(10, 100) / 100f
        brand.alpha = global * settings.brandOpacity / 100f
        source.alpha = global * settings.sourceOpacity / 100f
        viewer.alpha = global * settings.viewerOpacity / 100f
        forecastPanel.alpha = global * settings.forecastOpacity / 100f
        source.tint(settings.overlayColor)
        viewer.tint(settings.overlayColor)
        brand.setTextColor(settings.overlayColor)
        bindForecast(if (forecastCamera) sourceSlot else viewerSlot, if (forecastCamera) sourceZone else viewerZone,
            if (forecastCamera) cameraLocation?.name else settings.viewerCity)
        // Contextual controls keep their own timeout and focus; information clears their space.
        if (controlsVisible) { viewer.visibility = View.GONE; forecastPanel.visibility = View.GONE }
        requestLayout()
    }

    private fun bindForecast(slot: WeatherSlot, zone: TimeZone?, locationName: String?) {
        forecastPanel.visibility = if (settings.showForecast) View.VISIBLE else View.GONE
        val timeZone = zone ?: slot.forecast?.location?.timeZone?.let(presentation::zone) ?: TimeZone.getDefault()
        val days = presentation.days(slot.forecast, settings.viewerForecastDays, timeZone)
        val title = locationName?.takeIf { settings.showLocation && it.isNotBlank() }?.let { context.getString(R.string.ambient_forecast_for, it) }
            ?: context.getString(R.string.ambient_forecast_days, settings.viewerForecastDays)
        forecastTitle.put(title + if (slot.stale) " · " + context.getString(R.string.weather_cached) else "")
        forecastState.put(if (days.isEmpty()) slot.message() else "")
        val key = days.hashCode().toString() + "/" + timeZone.id + "/" + settings.temperatureUnit + "/" +
            settings.overlayColor + "/" + java.time.LocalDate.now(timeZone.toZoneId())
        if (forecastKey != key) {
            forecastKey = key
            cells.forEachIndexed { index, cell ->
                val day = days.getOrNull(index)
                cell.visibility = if (day == null) View.GONE else View.VISIBLE
                if (day != null) cell.bind(day, timeZone, index > 0)
            }
        }
        forecastCredit.put(slot.forecast?.takeIf { it.requiresVisibleAttribution }?.attribution.orEmpty())
        listOf(forecastTitle, forecastState, forecastCredit).forEach { it.setTextColor(settings.overlayColor) }
    }

    /**
     * Measure each widget within one of two non-overlapping rows. In the cinematic
     * layout the forecast is a compact 62%-width panel, not a permanent full-width
     * footer. Measuring before positioning preserves all seven days and attribution.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, h)
        val preview = h < dp(320)
        safeX = maxOf(dp(if (preview) 12 else 32), (w * .05f).roundToInt())
        safeY = maxOf(dp(if (preview) 10 else 22), (h * .05f).roundToInt())
        travel = dp(if (preview) 2 else 8)
        compositionWidth = (w - 2 * (safeX + travel)).coerceAtLeast(0)
        compositionHeight = (h - 2 * (safeY + travel)).coerceAtLeast(0)
        val scale = settings.overlaySize.coerceIn(14, 40) / 20f *
            (h.toFloat() / dp(540)).coerceIn(if (preview) .45f else .7f, 1f)
        source.size(scale)
        viewer.size(scale * .82f)
        cells.forEach { it.size(if (preview) .7f else if (settings.viewerForecastDays >= 6) .84f else 1f) }
        fun atMost(fraction: Float) = MeasureSpec.makeMeasureSpec(
            (compositionWidth * fraction).roundToInt(), MeasureSpec.AT_MOST)
        fun measure(view: View, widthSpec: Int, maxHeight: Int) {
            if (view.visibility == View.GONE) view.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))
            else view.measure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val fit = if (view.measuredHeight == 0) 1f else
                (maxHeight.coerceAtLeast(0).toFloat() / view.measuredHeight).coerceAtMost(1f)
            view.pivotX = 0f
            view.pivotY = 0f
            view.scaleX = fit
            view.scaleY = fit
        }
        measure(source, atMost(.42f), (compositionHeight * .42f).toInt())
        measure(brand, atMost(.30f), (compositionHeight * .12f).toInt())
        measure(viewer, atMost(.32f), (compositionHeight * .38f).toInt())
        measure(forecastPanel, MeasureSpec.makeMeasureSpec(
            (compositionWidth * .62f).roundToInt(), MeasureSpec.EXACTLY),
            (compositionHeight * .38f).toInt())
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val positions = AmbientPlacement.calculate(
            left = safeX + travel,
            top = safeY + travel,
            width = compositionWidth,
            height = compositionHeight,
            brand = OverlayExtent(drawnWidth(brand), drawnHeight(brand)),
            source = OverlayExtent(drawnWidth(source), drawnHeight(source)),
            viewer = OverlayExtent(drawnWidth(viewer), drawnHeight(viewer)),
            forecast = OverlayExtent(drawnWidth(forecastPanel), drawnHeight(forecastPanel)),
            phase = if (settings.moveOverlay) phase else 0,
            corner = settings.overlayCorner,
            controlsVisible = controlsVisible,
            insetX = (dp(32) * settings.overlayX.coerceIn(0, 100) / 100f).roundToInt(),
            insetY = (dp(32) * settings.overlayY.coerceIn(0, 100) / 100f).roundToInt(),
        )
        fun place(view: View, origin: OverlayOrigin) {
            view.layout(origin.x, origin.y, origin.x + view.measuredWidth, origin.y + view.measuredHeight)
        }
        place(brand, positions.brand)
        place(source, positions.source)
        place(viewer, positions.viewer)
        place(forecastPanel, positions.forecast)
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

    private inner class LocationGroup : LinearLayout(context) {
        private val eyebrow = label("", 11f)
        private val place = label("", 19f).apply { typeface = TvDesign.medium; maxLines = 2 }
        private val clock = label("", 70f).apply { typeface = TvDesign.light; letterSpacing = -.025f }
        private val date = label("", 12f)
        private val conditions = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        private val symbol = WeatherSymbol(context)
        private val weather = label("", 14f).apply { maxLines = 2 }
        private val metrics = label("", 11f)
        private val credit = label("", 10f).apply { maxLines = 2 }
        init {
            orientation = VERTICAL
            background = subtleAmbientScrim()
            setPadding(dp(8), dp(5), dp(8), dp(5))
            conditions.addView(symbol, LinearLayout.LayoutParams(dp(26), dp(26)))
            conditions.addView(weather, LinearLayout.LayoutParams(0, -2, 1f))
            listOf(eyebrow, place, clock, date, conditions, metrics, credit).forEach { addView(it) }
        }
        fun bind(caption: String, location: String, zone: TimeZone?, now: Date, slot: WeatherSlot, visible: Boolean) {
            visibility = if (visible) View.VISIBLE else View.GONE
            eyebrow.put(caption)
            place.put(location)
            clock.put(if (settings.showClock && zone != null) presentation.clock(zone, settings, now) else "")
            date.put(if (settings.showDate && zone != null) presentation.date(zone, settings, now) else "")
            val current = slot.forecast?.current
            val condition = current?.let { WeatherText.description(context, it) }.orEmpty()
            weather.put(if (!settings.showWeather) "" else if (current == null) slot.message() else
                listOfNotNull(current.temperatureC?.let { presentation.temperature(it, settings) }, condition.takeIf(String::isNotBlank))
                    .joinToString("  ·  ").ifBlank { context.getString(R.string.ambient_condition_unavailable) } +
                    if (slot.stale) " · " + context.getString(R.string.weather_cached) else "")
            conditions.visibility = if (settings.showWeather) View.VISIBLE else View.GONE
            symbol.bind(current?.weatherCode, current?.description)
            metrics.put(if (settings.showWeather) presentation.metrics(current, settings) else "")
            credit.put(if (settings.showWeather) slot.forecast?.takeIf { it.requiresVisibleAttribution }?.attribution.orEmpty() else "")
        }
        fun tint(color: Int) {
            listOf(eyebrow, place, clock, date, weather, metrics, credit).forEach { it.setTextColor(color) }
            symbol.tint(color)
        }
        fun size(scale: Float) {
            place.textSize = 19f * scale
            clock.textSize = 70f * scale
            weather.textSize = 14f * scale.coerceAtLeast(.8f)
        }
    }

    private inner class ForecastCell : LinearLayout(context) {
        private val day = label("", 12f)
        private val symbol = WeatherSymbol(context)
        private val temperatures = label("", 17f).apply { typeface = TvDesign.medium }
        private val rain = label("", 11f)
        init {
            orientation = VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(6), dp(4), dp(6), dp(4))
            listOf(day, temperatures, rain).forEach { it.gravity = Gravity.CENTER; it.maxLines = 1 }
            addView(day)
            addView(symbol, LinearLayout.LayoutParams(dp(28), dp(28)))
            addView(temperatures)
            addView(rain)
        }
        fun bind(value: ForecastDay, zone: TimeZone, divider: Boolean) {
            day.put(presentation.dayLabel(value.date, zone))
            temperatures.put(presentation.temperature(value.maximumC, settings, false) + " / " +
                presentation.temperature(value.minimumC, settings, false))
            rain.put(presentation.percent(value.precipitationChancePct))
            symbol.bind(value.weatherCode, value.description)
            symbol.tint(settings.overlayColor)
            listOf(day, temperatures, rain).forEach { it.setTextColor(settings.overlayColor) }
            // Forecast icons and labels float over the scene without opaque day cards.
            background = null
            contentDescription = listOf(day.text, value.description.orEmpty(), temperatures.text,
                context.getString(R.string.ambient_rain_chance, rain.text)).joinToString(", ")
        }
        fun size(scale: Float) { day.textSize = 12f * scale; temperatures.textSize = 16f * scale; rain.textSize = 11f * scale }
    }

    private fun label(value: String, size: Float) = TextView(context).apply {
        text = value; textSize = size; typeface = TvDesign.regular
        setTextColor(TvDesign.text)
        includeFontPadding = false
        maxLines = 2; ellipsize = TextUtils.TruncateAt.END
        setPadding(dp(2), dp(2), dp(2), dp(2))
        setShadowLayer(dp(2).toFloat(), 0f, dp(1).toFloat(), 0xB3000000.toInt())
    }
    private fun TextView.put(value: String) {
        if (text.toString() != value) text = value
        visibility = if (value.isBlank()) View.GONE else View.VISIBLE
    }
    // No opaque cards: a shallow gradient and text shadow protect contrast over scenery.
    private fun subtleAmbientScrim() = android.graphics.drawable.GradientDrawable(
        android.graphics.drawable.GradientDrawable.Orientation.BOTTOM_TOP,
        intArrayOf(0x7107121F, 0x2C07121F, 0x0007121F),
    )
    private fun dp(value: Int) = TvDesign.dp(context, value)
    private fun drawnHeight(view: View) = (view.measuredHeight * view.scaleY).roundToInt()
    private fun drawnWidth(view: View) = (view.measuredWidth * view.scaleX).roundToInt()
    private fun coordinateKey(location: WeatherLocation) = java.lang.String.format(java.util.Locale.US, "%.4f/%.4f", location.latitude, location.longitude)
}
