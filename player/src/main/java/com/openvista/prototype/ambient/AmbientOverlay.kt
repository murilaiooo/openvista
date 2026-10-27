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
        background = TvDesign.scrim()
        setPadding(dp(10), dp(7), dp(10), dp(7))
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
        var delay = 60_000 - System.currentTimeMillis().mod(60_000)
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
            phase = (phase + 1) % MOVEMENT.size
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
            if (settings.showLocation && sourcePlace.isNotBlank()) sourcePlace else item?.title.orEmpty(),
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

    /** Reserve travel before measuring; glyphs, dividers and scrims share the same safe rectangle. */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, h)
        val preview = h < dp(320)
        safeX = maxOf(dp(if (preview) 12 else 32), (w * .05f).roundToInt())
        safeY = maxOf(dp(if (preview) 10 else 22), (h * .05f).roundToInt())
        travel = minOf(dp(12), (minOf(w, h) * .025f).roundToInt())
        compositionWidth = (w - 2 * (safeX + travel)).coerceAtLeast(0)
        compositionHeight = (h - 2 * (safeY + travel)).coerceAtLeast(0)
        val scale = settings.overlaySize.coerceIn(14, 40) / 20f *
            (h.toFloat() / dp(540)).coerceIn(if (preview) .45f else .7f, 1f)
        source.size(scale)
        viewer.size(scale * .82f)
        cells.forEach { it.size(if (preview) .7f else 1f) }
        val atMostWidth = MeasureSpec.makeMeasureSpec((compositionWidth * .46f).toInt(), MeasureSpec.AT_MOST)
        fun measure(view: View, widthSpec: Int, maxHeight: Int) {
            if (view.visibility == View.GONE) view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))
            else view.measure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            // Measure natural text before fitting the entire optional group, including its scrim.
            // Never constrain LinearLayout's height and then clip its last forecast/credit row.
            val fit = if (view.measuredHeight == 0) 1f else
                (maxHeight.coerceAtLeast(0).toFloat() / view.measuredHeight).coerceAtMost(1f)
            view.pivotX = 0f; view.pivotY = 0f
            view.scaleX = fit; view.scaleY = fit
        }
        measure(source, atMostWidth, (compositionHeight * .48f).toInt())
        measure(brand, atMostWidth, (compositionHeight * .12f).toInt())
        measure(forecastPanel, MeasureSpec.makeMeasureSpec(compositionWidth, MeasureSpec.EXACTLY),
            (compositionHeight * .36f).toInt())
        val lowerHeight = compositionHeight - drawnHeight(forecastPanel) - dp(12) -
            maxOf(drawnHeight(source), drawnHeight(brand))
        measure(viewer, atMostWidth, lowerHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val offset = if (settings.moveOverlay) MOVEMENT[phase] else (0 to 0)
        // Editor offsets consume only reserved allowance, never push forecast columns offscreen.
        val dx = (offset.first * travel + settings.overlayX.coerceIn(0, 100) / 100f * travel).roundToInt().coerceIn(-travel, travel)
        val dy = (offset.second * travel + settings.overlayY.coerceIn(0, 100) / 100f * travel).roundToInt().coerceIn(-travel, travel)
        val x = safeX + travel + dx
        val y = safeY + travel + dy
        val end = x + compositionWidth
        val lower = y + compositionHeight
        val mirror = settings.overlayCorner % 2 == 1
        fun place(view: View, px: Int, py: Int) {
            view.layout(px, py, px + view.measuredWidth, py + view.measuredHeight)
        }
        if (settings.overlayLayout == "compact") {
            val bottomAnchor = settings.overlayCorner >= 2
            val sourceY = if (bottomAnchor) lower - drawnHeight(forecastPanel) - drawnHeight(source) - dp(12) else y
            val sourceX = if (mirror) end - drawnWidth(source) else x
            // Percentage offsets retain their full safe travel in compact mode. The two
            // information groups occupy separate horizontal lanes, preventing collisions.
            val laneWidth = compositionWidth / 2
            val sourceTravelX = (laneWidth - drawnWidth(source)).coerceAtLeast(0)
            val sourceTravelY = (compositionHeight - drawnHeight(forecastPanel) - drawnHeight(source) - dp(12)).coerceAtLeast(0)
            val shiftX = settings.overlayX.coerceIn(0, 100) / 100f * sourceTravelX * if (mirror) -1 else 1
            val shiftY = settings.overlayY.coerceIn(0, 100) / 100f * sourceTravelY * if (bottomAnchor) -1 else 1
            place(source, sourceX + shiftX.roundToInt(), (sourceY + shiftY.roundToInt()).coerceAtLeast(y))
            place(brand, if (mirror) x else end - drawnWidth(brand), y)
            val viewerY = if (bottomAnchor) y + drawnHeight(brand) + dp(10)
                else lower - drawnHeight(forecastPanel) - drawnHeight(viewer) - dp(12)
            place(viewer, if (mirror) x else end - drawnWidth(viewer), viewerY.coerceAtLeast(y))
        } else {
            place(brand, if (mirror) end - drawnWidth(brand) else x, y)
            place(source, if (mirror) x else end - drawnWidth(source), y)
            place(viewer, if (mirror) end - drawnWidth(viewer) else x,
                (lower - drawnHeight(forecastPanel) - drawnHeight(viewer) - dp(12)).coerceAtLeast(y))
        }
        place(forecastPanel, x + (compositionWidth - drawnWidth(forecastPanel)) / 2, lower - drawnHeight(forecastPanel))
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
        private val clock = label("", 44f).apply { typeface = TvDesign.light; letterSpacing = -.025f }
        private val date = label("", 12f)
        private val conditions = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        private val symbol = WeatherSymbol(context)
        private val weather = label("", 14f).apply { maxLines = 2 }
        private val metrics = label("", 11f)
        private val credit = label("", 10f).apply { maxLines = 2 }
        init {
            orientation = VERTICAL
            background = TvDesign.scrim()
            setPadding(dp(9), dp(6), dp(9), dp(6))
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
            clock.textSize = 44f * scale
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
            background = if (divider) android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(0x22FFFFFF, 0x00FFFFFF)) else null
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
    private fun dp(value: Int) = TvDesign.dp(context, value)
    private fun drawnHeight(view: View) = (view.measuredHeight * view.scaleY).roundToInt()
    private fun drawnWidth(view: View) = (view.measuredWidth * view.scaleX).roundToInt()
    private fun coordinateKey(location: WeatherLocation) = java.lang.String.format(java.util.Locale.US, "%.4f/%.4f", location.latitude, location.longitude)
    companion object {
        private val MOVEMENT = listOf(0 to 0, -1 to -1, 0 to -1, 1 to -1, 1 to 0, 1 to 1, 0 to 1, -1 to 1, -1 to 0)
    }
}
