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
    private val brand = label("openvista", 29f).apply {
        typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
        letterSpacing = -.016f
    }
    private val source = LocationGroup(false)
    private val viewer = LocationGroup(true)
    private val sceneContrast = View(context).apply {
        background = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(0x71090E17, 0x19090E17, 0x00000000))
    }
    private val forecastPanel = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        background = SoftGlassDrawable()
        setPadding(0, 0, 0, 0)
    }
    private val forecastTitle = label("", 11f)
    private val forecastState = label("", 13f)
    private val forecastRow = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
    private val forecastDividers = List(6) { View(context).apply { setBackgroundColor(0x55FFFFFF) } }
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
        cells.forEachIndexed { index, cell ->
            if (index > 0) forecastRow.addView(forecastDividers[index - 1],
                LinearLayout.LayoutParams(dp(1), dp(88)))
            forecastRow.addView(cell, LinearLayout.LayoutParams(0, -2, 1f))
        }
        forecastPanel.addView(forecastRow)
        forecastPanel.addView(forecastCredit)
        addView(sceneContrast, FrameLayout.LayoutParams(-1, -1))
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
        // The country is resolved from any saved form, or from the camera's own zone, and shown localized with its flag.
        val countryCode = item?.let { CountryResolver.resolve(it.country, it.timeZone.ifBlank { null } ?: sourceZone?.id) }
        val countryName = item?.let { CountryResolver.displayName(it.country, countryCode, context.resources.configuration.locales[0]) }.orEmpty()
        val countryFlag = countryCode?.let(CountryResolver::flag).orEmpty()
        val sourcePlace = item?.let { listOf(it.city, countryName).filter(String::isNotBlank).joinToString(", ") }.orEmpty() +
            if (countryFlag.isNotEmpty()) "  $countryFlag" else ""
        val sourceStatus = context.getString(when {
            live && playing && item?.family == MediaFamily.CAMERAS -> R.string.ambient_live
            playing -> R.string.ambient_playing
            buffering -> R.string.playback_connecting
            else -> R.string.ambient_paused
        })
        val liveStatus = live && playing && item?.family == MediaFamily.CAMERAS
        source.bind(if (liveStatus) "● " + sourceStatus else sourceStatus,
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
        val candidates = presentation.days(slot.forecast,
            (settings.viewerForecastDays + 1).coerceAtMost(7), timeZone)
        val today = java.time.LocalDate.now(timeZone.toZoneId())
        val days = (if (candidates.firstOrNull()?.date == today.toString())
            candidates.drop(1) else candidates).take(settings.viewerForecastDays)
        val title = locationName?.takeIf { settings.showLocation && it.isNotBlank() }?.let { context.getString(R.string.ambient_forecast_for, it) }
            ?: context.getString(R.string.ambient_forecast_days, settings.viewerForecastDays)
        forecastTitle.put(if (days.isEmpty()) title else if (slot.stale)
            context.getString(R.string.weather_cached) else "")
        forecastState.put(if (days.isEmpty()) slot.message() else "")
        forecastDividers.forEachIndexed { index, divider ->
            divider.visibility = if (index + 1 < days.size) View.VISIBLE else View.GONE
        }
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

    /** Reference canvas: original positions scale with 16:9 safe space. */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, h)
        sceneContrast.measure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
        val preview = h < dp(320)
        safeX = maxOf(dp(if (preview) 14 else 36), (w * .043f).roundToInt())
        safeY = maxOf(dp(if (preview) 10 else 26), (h * .041f).roundToInt())
        travel = dp(if (preview) 1 else 2)
        compositionWidth = (w - 2 * (safeX + travel)).coerceAtLeast(0)
        compositionHeight = (h - 2 * (safeY + travel)).coerceAtLeast(0)
        val scale = settings.overlaySize.coerceIn(14, 40) / 20f *
            (h.toFloat() / dp(540)).coerceIn(if (preview) .45f else .7f, 1f)
        source.size(scale)
        viewer.size(scale)
        cells.forEach { it.size((if (preview) .68f else 1f) * scale.coerceIn(.8f, 1.25f)) }
        fun atMost(fraction: Float) = MeasureSpec.makeMeasureSpec(
            (compositionWidth * fraction).roundToInt(), MeasureSpec.AT_MOST)
        fun fit(view: View, widthSpec: Int, height: Int) {
            if (view.visibility == View.GONE) view.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))
            else view.measure(widthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val xFit = if (view.measuredWidth == 0) 1f else
                (MeasureSpec.getSize(widthSpec).toFloat() / view.measuredWidth).coerceAtMost(1f)
            val yFit = if (view.measuredHeight == 0) 1f else
                (height.coerceAtLeast(0).toFloat() / view.measuredHeight).coerceAtMost(1f)
            view.pivotX = 0f
            view.pivotY = 0f
            view.scaleX = minOf(xFit, yFit)
            view.scaleY = minOf(xFit, yFit)
        }
        fit(source, atMost(.425f), (compositionHeight * .45f).toInt())
        fit(brand, atMost(.28f), (compositionHeight * .11f).toInt())
        fit(viewer, atMost(.475f), (compositionHeight * .39f).toInt())
        fit(forecastPanel, MeasureSpec.makeMeasureSpec(
            (compositionWidth * .515f).roundToInt(), MeasureSpec.EXACTLY),
            (compositionHeight * .31f).toInt())
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        sceneContrast.layout(0, 0, measuredWidth, measuredHeight)
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
            insetX = (dp(18) * settings.overlayX.coerceIn(0, 100) / 100f).roundToInt(),
            insetY = (dp(18) * settings.overlayY.coerceIn(0, 100) / 100f).roundToInt(),
        )
        fun place(view: View, origin: OverlayOrigin) {
            view.layout(origin.x, origin.y, origin.x + view.measuredWidth, origin.y + view.measuredHeight)
        }
        place(brand, positions.brand)
        place(source, positions.source)
        val movementPhase = if (settings.moveOverlay) phase else 0
        val sourceBottom = !controlsVisible &&
            ((settings.overlayCorner >= 2) != (movementPhase >= 2))
        val infoOffset = if (sourceBottom) dp(9) else -dp(9)
        place(viewer, OverlayOrigin(positions.viewer.x, positions.viewer.y + infoOffset))
        // Complete information blocks mirror and rotate; upper-row tweaks stay inset.
        val sourceOnRight = (settings.overlayCorner % 2 == 0) !=
            (movementPhase % 2 == 1)
        val inwardForecast = if (sourceOnRight) -dp(45) else dp(45)
        val forecastOffset = if (sourceBottom) dp(8) else -dp(8)
        val adjustedForecast = OverlayOrigin(positions.forecast.x + inwardForecast,
            positions.forecast.y + forecastOffset)
        place(forecastPanel, adjustedForecast)
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

    /** The viewer owns a horizontal baseline composition; the source owns a vertical one. */
    private inner class LocationGroup(private val viewerMode: Boolean) : LinearLayout(context) {
        private val eyebrow = label("", if (viewerMode) 13f else 15f)
        private val place = label("", 24f).apply {
            typeface = TvDesign.light
            maxLines = 2
            ellipsize = null
        }
        private val sourceHeader = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        private val clock = label("", if (viewerMode) 70f else 92f).apply {
            typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
            letterSpacing = -.035f
            maxLines = 1
        }
        private val date = label("", 15f).apply { setTextColor(0xFFDEE1E6.toInt()); maxLines = 1 }
        private val symbol = WeatherSymbol(context)
        private val temperature = label("", if (viewerMode) 32f else 22f).apply {
            typeface = TvDesign.light
            maxLines = 1
        }
        private val condition = label("", 13.5f).apply { setTextColor(0xFFDDE0E5.toInt()); maxLines = 1 }
        private val metrics = label("", 12f).apply {
            setTextColor(0xFFDDE0E5.toInt()); maxLines = 2
            setLineSpacing(dp(6).toFloat(), 1f)
        }
        private val highs = label("", 16f).apply { maxLines = 1 }
        private val credit = label("", 9f).apply { maxLines = 1 }
        private val weatherStack = LinearLayout(context).apply { orientation = VERTICAL }
        private val weatherRow = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        private val timeStack = LinearLayout(context).apply { orientation = VERTICAL }
        private val detailRow = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        private val timeRule = View(context).apply { setBackgroundColor(0x55FFFFFF) }
        private val weatherRule = View(context).apply { setBackgroundColor(0x55FFFFFF) }

        init {
            orientation = VERTICAL
            background = SoftGlassDrawable()
            setPadding(0, 0, 0, 0)
            eyebrow.setTextColor(0xFFE3E7EA.toInt())
            if (viewerMode) {
                addView(eyebrow)
                addView(place)
                timeStack.addView(clock)
                timeStack.addView(date)
                detailRow.addView(timeStack, LinearLayout.LayoutParams(dp(230), -2))
                detailRow.addView(timeRule, LinearLayout.LayoutParams(dp(1), dp(90)).apply {
                    leftMargin = dp(8); rightMargin = dp(20)
                })
                weatherStack.addView(temperature)
                weatherStack.addView(condition)
                weatherStack.addView(highs)
                weatherRow.addView(symbol, LinearLayout.LayoutParams(dp(53), dp(53)).apply {
                    rightMargin = dp(10)
                })
                weatherRow.addView(weatherStack, LinearLayout.LayoutParams(dp(165), -2))
                detailRow.addView(weatherRow, LinearLayout.LayoutParams(dp(238), -2))
                addView(detailRow)
                detailRow.clipChildren = false
                weatherRow.translationY = -dp(15).toFloat()
                date.translationY = -dp(8).toFloat()
            } else {
                // The city, country and flag receive an independent text view.
                // A long city must never force the country to be truncated.
                eyebrow.maxLines = 1
                sourceHeader.addView(eyebrow, LinearLayout.LayoutParams(-2, -2))
                sourceHeader.addView(place, LinearLayout.LayoutParams(-2, -2).apply {
                    leftMargin = dp(9)
                })
                addView(sourceHeader)
                addView(clock)
                clock.layoutParams = LinearLayout.LayoutParams(-2, -2).apply {
                    topMargin = -dp(12)
                }
                addView(date)
                date.layoutParams = LinearLayout.LayoutParams(-2, -2).apply {
                    topMargin = -dp(6)
                }
                weatherStack.addView(temperature)
                weatherStack.addView(condition)
                weatherRow.addView(symbol, LinearLayout.LayoutParams(dp(35), dp(35)).apply {
                    rightMargin = dp(9)
                })
                weatherRow.addView(weatherStack, LinearLayout.LayoutParams(dp(116), -2))
                weatherRow.addView(weatherRule, LinearLayout.LayoutParams(dp(1), dp(43)).apply {
                    leftMargin = dp(10); rightMargin = dp(11)
                })
                weatherRow.addView(metrics, LinearLayout.LayoutParams(dp(135), -2))
                addView(weatherRow)
            }
            addView(credit)
        }

        fun bind(caption: String, location: String, zone: TimeZone?, now: Date, slot: WeatherSlot, visible: Boolean) {
            visibility = if (visible) View.VISIBLE else View.GONE
            if (viewerMode) {
                eyebrow.put(caption)
                place.put(location)
            } else {
                eyebrow.put(caption)
                place.put(location.takeIf(String::isNotBlank)?.let { "|  $it" }.orEmpty())
            }
            clock.put(if (settings.showClock && zone != null) presentation.clock(zone, settings, now) else "")
            date.put(if (settings.showDate && zone != null) presentation.date(zone, settings, now) else "")
            val current = slot.forecast?.current
            val description = current?.let { WeatherText.description(context, it) }.orEmpty()
            val weatherVisible = settings.showWeather
            weatherRow.visibility = if (weatherVisible) View.VISIBLE else View.GONE
            symbol.bind(current?.weatherCode, current?.description)
            symbol.tint(when (current?.weatherCode) {
                0, 1 -> 0xFFFFCE37.toInt()
                2 -> 0xFFFFD873.toInt()
                in 51..67, in 80..82 -> 0xFF88D6FA.toInt()
                else -> settings.overlayColor
            })
            temperature.put(if (!weatherVisible) "" else if (current == null) "" else presentation.temperature(current.temperatureC, settings))
            condition.put(if (!weatherVisible) "" else if (current == null) slot.message() else description +
                (if (slot.stale) " · " + context.getString(R.string.weather_cached) else ""))
            if (viewerMode) {
                val today = slot.forecast?.days?.firstOrNull { day ->
                    runCatching { java.time.LocalDate.parse(day.date) }.getOrNull() ==
                        java.time.LocalDate.now((zone ?: TimeZone.getDefault()).toZoneId())
                }
                highs.put(if (!weatherVisible || today == null) "" else
                    "H: " + presentation.temperature(today.maximumC, settings, false) +
                    "    L: " + presentation.temperature(today.minimumC, settings, false))
                metrics.visibility = View.GONE
                timeRule.visibility = if (weatherVisible) View.VISIBLE else View.GONE
            } else {
                val parts = presentation.metrics(current, settings).split("  ·  ").filter(String::isNotBlank)
                metrics.put(if (weatherVisible) parts.joinToString("\n") { it.substringAfter(" ", it) } else "")
                weatherRule.visibility = if (weatherVisible && parts.isNotEmpty()) View.VISIBLE else View.GONE
            }
            credit.put(if (weatherVisible) slot.forecast?.takeIf { it.requiresVisibleAttribution }?.attribution.orEmpty() else "")
        }

        fun tint(color: Int) {
            listOf(eyebrow, place, clock, date, temperature, condition, metrics, highs, credit)
                .forEach { it.setTextColor(color) }
            condition.setTextColor(0xFFE0E0E0.toInt())
            metrics.setTextColor(0xFFD6D9DD.toInt())
            date.setTextColor(0xFFDEE1E6.toInt())
            if (!viewerMode && eyebrow.text.toString().startsWith("●")) {
                val rendered = android.text.SpannableString(eyebrow.text)
                val end = rendered.length
                rendered.setSpan(android.text.style.ForegroundColorSpan(0xFF67DD74.toInt()),
                    0, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                eyebrow.text = rendered
            }
        }

        fun size(scale: Float) {
            clock.textSize = (if (viewerMode) 70f else 92f) * scale
            place.textSize = (if (viewerMode) 24f else 13f) * scale
            temperature.textSize = (if (viewerMode) 32f else 22f) * scale
            eyebrow.textSize = (if (viewerMode) 13f else 15f) * scale
            date.textSize = 15f * scale
        }
    }

    private inner class ForecastCell : LinearLayout(context) {
        private val day = label("", 15f)
        private val symbol = WeatherSymbol(context)
        private val temperatures = label("", 20f).apply { typeface = TvDesign.regular }
        private val rain = label("", 13f).apply { setTextColor(0xFFE0E3E7.toInt()) }
        private val rainMark = PrecipitationDrop(context)
        private val rainRow = LinearLayout(context).apply { gravity = Gravity.CENTER }
        init {
            orientation = VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(3), dp(1), dp(3), dp(1))
            listOf(day, temperatures, rain).forEach { it.gravity = Gravity.CENTER; it.maxLines = 1 }
            addView(day)
            addView(symbol, LinearLayout.LayoutParams(dp(51), dp(51)).apply {
                topMargin = dp(4); bottomMargin = dp(4)
            })
            addView(temperatures)
            rainRow.addView(rainMark, LinearLayout.LayoutParams(dp(10), dp(13)).apply {
                rightMargin = dp(5)
            })
            rainRow.addView(rain)
            addView(rainRow)
        }

        fun bind(value: ForecastDay, zone: TimeZone, divider: Boolean) {
            day.put(presentation.dayLabel(value.date, zone))
            val highs = presentation.temperature(value.maximumC, settings, false)
            val lows = presentation.temperature(value.minimumC, settings, false)
            val rich = android.text.SpannableString(highs + "  " + lows)
            rich.setSpan(android.text.style.ForegroundColorSpan(0xFFC9CDD2.toInt()),
                highs.length + 2, rich.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            temperatures.text = rich
            rain.put(presentation.percent(value.precipitationChancePct))
            symbol.bind(value.weatherCode, value.description)
            symbol.tint(when (value.weatherCode) {
                0, 1 -> 0xFFFFCA34.toInt()
                2 -> 0xFFFFD36B.toInt()
                in 51..67, in 80..82 -> 0xFF91D6F6.toInt()
                else -> settings.overlayColor
            })
            day.setTextColor(settings.overlayColor)
            rain.setTextColor(0xFFE0E3E7.toInt())
            contentDescription = listOf(day.text, value.description.orEmpty(), temperatures.text,
                context.getString(R.string.ambient_rain_chance, rain.text)).joinToString(", ")
        }

        fun size(scale: Float) {
            day.textSize = 15f * scale
            temperatures.textSize = 20f * scale
            rain.textSize = 13f * scale
        }
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
}
