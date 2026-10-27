package com.openvista.prototype.ambient

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Two transparent, independently refreshed pieces of information:
 * camera city/country/time/temperature and viewer location/time/3–7 day forecast.
 *
 * Both move as one pair every minute, always to opposite safe quadrants.
 * Video titles, raw URLs and user private data are never placed on screen.
 * No movement algorithm can guarantee protection against panel burn-in.
 */
class AmbientOverlay(context: Context, private val store: LibraryStore) : FrameLayout(context) {
    private val main = Handler(Looper.getMainLooper())
    private val random = Random()
    private val resolver = AmbientCityResolver(context)
    private val cameraWeather = WeatherRepository(context)
    private val viewerWeather = WeatherRepository(context)

    private fun vertical() = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        background = null
        gravity = Gravity.START
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    private val cameraGroup = vertical()
    private val cameraPlace = label(21f, Color.WHITE, true)
    private val cameraLine = row()
    private val cameraClock = label(45f, Color.WHITE, true)
    private val cameraDot = label(22f, 0xD9FFFFFF.toInt())
    private val cameraTemp = label(33f, Color.WHITE)
    private val cameraCredit = label(10f, 0xC8FFFFFF.toInt())

    private val viewerGroup = vertical()
    private val viewerPlace = label(21f, Color.WHITE, true)
    private val viewerLine = row()
    private val viewerClock = label(43f, Color.WHITE, true)
    private val viewerDot = label(22f, 0xD9FFFFFF.toInt())
    private val viewerTemp = label(33f, Color.WHITE)
    private val forecastRows = vertical()
    private val viewerCredit = label(10f, 0xC8FFFFFF.toInt())
    private val dayLabels = ArrayList<TextView>()
    private val tempLabels = ArrayList<TextView>()
    private val rainLabels = ArrayList<TextView>()

    private var item: LibraryItem? = null
    private var cameraLocation: WeatherLocation? = null
    private var localLocation: WeatherLocation? = null
    private var cameraForecast: Forecast? = null
    private var viewerForecast: Forecast? = null
    private var cameraFromGeocoder = false
    private var cameraWeatherLocation: WeatherLocation? = null
    private var viewerWeatherLocation: WeatherLocation? = null
    private var cameraAt = Long.MIN_VALUE
    private var viewerAt = Long.MIN_VALUE
    private var cameraStale = false
    private var viewerStale = false
    private var running = false
    private var currentMinute = Long.MIN_VALUE
    private var positionSettings = ""
    private var corner = 0
    private var lastPositions: Pair<AmbientLayoutPolicy.Position, AmbientLayoutPolicy.Position>? = null
    private var cameraSuppressedForSpace = false

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            update()
            main.postDelayed(this, 1_000)
        }
    }

    init {
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        clipChildren = false
        clipToPadding = false

        cameraDot.text = "   ·   "
        cameraLine.addView(cameraClock)
        cameraLine.addView(cameraDot)
        cameraLine.addView(cameraTemp)
        cameraGroup.addView(cameraPlace)
        cameraGroup.addView(cameraLine)
        cameraGroup.addView(cameraCredit)

        viewerDot.text = "   ·   "
        viewerLine.addView(viewerClock)
        viewerLine.addView(viewerDot)
        viewerLine.addView(viewerTemp)
        viewerGroup.addView(viewerPlace)
        viewerGroup.addView(viewerLine)
        viewerGroup.addView(forecastRows)
        viewerGroup.addView(viewerCredit)
        for (index in 0 until 7) {
            val line = row().apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(3), 0, dp(3))
            }
            val day = label(15f, 0xE7FFFFFF.toInt(), true)
            val temperatures = label(17f, Color.WHITE)
            val precipitation = label(14f, 0xDBD5EFFF.toInt())
            day.layoutParams = LinearLayout.LayoutParams(dp(88), LayoutParams.WRAP_CONTENT)
            temperatures.layoutParams = LinearLayout.LayoutParams(dp(147), LayoutParams.WRAP_CONTENT)
            precipitation.layoutParams = LinearLayout.LayoutParams(dp(66), LayoutParams.WRAP_CONTENT)
            line.addView(day)
            line.addView(temperatures)
            line.addView(precipitation)
            forecastRows.addView(line)
            dayLabels.add(day)
            tempLabels.add(temperatures)
            rainLabels.add(precipitation)
        }
        forecastRows.setPadding(0, dp(14), 0, dp(8))
        cameraGroup.visibility = View.INVISIBLE
        viewerGroup.visibility = View.INVISIBLE
        addView(cameraGroup, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(viewerGroup, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        cameraGroup.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> post { place() } }
        viewerGroup.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> post { place() } }
        cameraPlace.maxLines = 2
        viewerPlace.maxLines = 2
        cameraPlace.ellipsize = TextUtils.TruncateAt.END
        viewerPlace.ellipsize = TextUtils.TruncateAt.END
    }

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density + .5f).toInt()
    private fun row() = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = null
    }
    private fun label(size: Float, color: Int, heavy: Boolean = false) = TextView(context).apply {
        textSize = size
        setTextColor(color)
        typeface = Typeface.create("sans-serif", if (heavy) Typeface.BOLD else Typeface.NORMAL)
        includeFontPadding = false
        setPadding(0, dp(2), 0, dp(2))
        setShadowLayer(dp(3).toFloat(), 0f, dp(2).toFloat(), 0xC0000000.toInt())
        background = null
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun start() {
        if (!running) {
            running = true
            main.removeCallbacks(tick)
            loadViewer()
            resolver.resolve(item, ::onCameraLocation)
            main.post(tick)
        }
    }

    fun stop() {
        running = false
        main.removeCallbacks(tick)
        cameraWeather.cancel()
        viewerWeather.cancel()
    }

    fun updateItem(source: LibraryItem?) {
        item = source
        cameraLocation = null
        cameraForecast = null
        cameraWeatherLocation = null
        cameraAt = Long.MIN_VALUE
        cameraGroup.visibility = View.INVISIBLE
        if (running) resolver.resolve(source, ::onCameraLocation)
    }

    fun userInteraction() { /* The ambient display must continue moving. */ }

    private fun onCameraLocation(location: WeatherLocation?) {
        if (!running) return
        // A missing source city is hidden; viewer location always has its own panel.
        cameraLocation = location
        cameraFromGeocoder = location != null &&
            (item?.latitude == null || item?.longitude == null)
        cameraForecast = null
        cameraWeatherLocation = null
        cameraAt = Long.MIN_VALUE
        cameraGroup.visibility = if (location == null) View.INVISIBLE else View.VISIBLE
        update()
    }

    private fun configuredViewer(): WeatherLocation? {
        val s = store.settings()
        val lat = s.viewerLatitude ?: return null
        val lon = s.viewerLongitude ?: return null
        if (lat !in -90.0..90.0 || lon !in -180.0..180.0 || s.viewerCity.isBlank() ||
            runCatching { ZoneId.of(s.viewerTimeZone) }.isFailure) return null
        return WeatherLocation(s.viewerCity, lat, lon, s.viewerTimeZone)
    }

    private fun loadViewer() {
        val target = configuredViewer()
        if (target == localLocation) return
        localLocation = target
        viewerForecast = null
        viewerWeatherLocation = null
        viewerAt = Long.MIN_VALUE
        viewerGroup.visibility = if (target == null) View.INVISIBLE else View.VISIBLE
        viewerWeather.cancel()
    }

    private fun clock(zone: String, format: String, locale: Locale): String {
        val tz = runCatching { java.util.TimeZone.getTimeZone(ZoneId.of(zone)) }.getOrNull() ?: return ""
        val pattern = when (format) {
            "12" -> "h:mm a"
            "24" -> "HH:mm"
            else -> if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
        }
        return SimpleDateFormat(pattern, locale).apply { timeZone = tz }.format(Date())
    }

    private fun update() {
        val s = store.settings()
        val locale = resources.configuration.locales[0]
        val fahrenheit = s.temperatureUnit == "fahrenheit" ||
            (s.temperatureUnit == "system" && locale.country == "US")
        val scale = s.overlaySize.coerceIn(14, 36) / 20f
        val now = SystemClock.elapsedRealtime()

        loadViewer()

        cameraLocation?.let { location ->
            cameraPlace.text = AmbientForecastPresentation.cameraCityCountry(location, item)
            cameraClock.text = if (s.showClock) clock(location.timeZone, s.clockFormat, locale) else ""
            val value = if (s.showWeather) AmbientForecastPresentation.temp(cameraForecast?.current?.temperatureC, fahrenheit) else ""
            cameraTemp.text = value
            cameraTemp.visibility = if (value.isBlank() || value == "–") View.GONE else View.VISIBLE
            cameraDot.visibility = if (cameraClock.text.isNotBlank() && cameraTemp.visibility == View.VISIBLE) View.VISIBLE else View.GONE
            cameraCredit.text = when {
                cameraForecast?.requiresVisibleAttribution == true -> "open-meteo.com · GeoNames"
                cameraFromGeocoder -> "GeoNames · open-meteo.com"
                else -> ""
            } + if (cameraStale) " · cached" else ""
            cameraCredit.visibility = if (cameraCredit.text.isBlank()) View.GONE else View.VISIBLE
            cameraGroup.contentDescription = listOf(cameraPlace.text, cameraClock.text, cameraTemp.text).joinToString(", ")
            cameraGroup.visibility = if (cameraSuppressedForSpace) View.INVISIBLE else View.VISIBLE
        } ?: run { cameraGroup.visibility = View.INVISIBLE }
        cameraPlace.textSize = 21f * scale
        cameraClock.textSize = 45f * scale
        cameraTemp.textSize = 33f * scale

        localLocation?.let { location ->
            val city = location.name.substringBefore(',').trim().take(65)
            viewerPlace.text = if (locale.language == "pt") "$city · VOCÊ" else "$city · YOUR LOCATION"
            viewerClock.text = if (s.showClock) clock(location.timeZone, s.clockFormat, locale) else ""
            val currentTemp = if (s.showWeather) AmbientForecastPresentation.temp(viewerForecast?.current?.temperatureC, fahrenheit) else ""
            viewerTemp.text = currentTemp
            viewerTemp.visibility = if (currentTemp.isBlank() || currentTemp == "–") View.GONE else View.VISIBLE
            viewerDot.visibility = if (viewerClock.text.isNotBlank() && viewerTemp.visibility == View.VISIBLE) View.VISIBLE else View.GONE
            val today = runCatching { LocalDate.now(ZoneId.of(location.timeZone)) }.getOrElse { LocalDate.now() }
            val days = if (s.showWeather) AmbientForecastPresentation.visibleDays(
                viewerForecast, location.timeZone, s.viewerForecastDays, today
            ) else emptyList()
            dayLabels.forEachIndexed { i, dayLabel ->
                val row = forecastRows.getChildAt(i)
                val forecastDay = days.getOrNull(i)
                row.visibility = if (forecastDay == null) View.GONE else View.VISIBLE
                if (forecastDay != null) {
                    dayLabel.text = AmbientForecastPresentation.dayName(forecastDay.date, locale)
                    tempLabels[i].text = "${AmbientForecastPresentation.temp(forecastDay.maximumC, fahrenheit)}  /  " +
                        AmbientForecastPresentation.temp(forecastDay.minimumC, fahrenheit)
                    rainLabels[i].text = AmbientForecastPresentation.rain(forecastDay.precipitationChancePct)
                }
            }
            forecastRows.visibility = if (days.isEmpty()) View.GONE else View.VISIBLE
            viewerCredit.text = when {
                viewerForecast?.requiresVisibleAttribution == true -> "open-meteo.com"
                viewerStale -> if (locale.language == "pt") "Dados em cache" else "Cached forecast"
                else -> ""
            }
            viewerCredit.visibility = if (viewerCredit.text.isBlank()) View.GONE else View.VISIBLE
            viewerGroup.contentDescription = listOf(viewerPlace.text, viewerClock.text, viewerTemp.text, "${days.size} days").joinToString(", ")
            viewerGroup.visibility = View.VISIBLE
        } ?: run { viewerGroup.visibility = View.INVISIBLE }
        viewerPlace.textSize = 21f * scale
        viewerClock.textSize = 43f * scale
        viewerTemp.textSize = 33f * scale
        for (index in 0 until 7) {
            dayLabels[index].textSize = 15f * scale
            tempLabels[index].textSize = 17f * scale
            rainLabels[index].textSize = 14f * scale
        }
        cameraGroup.alpha = s.overlayOpacity.coerceIn(35, 100) / 100f
        viewerGroup.alpha = cameraGroup.alpha

        val minuteNow = now / 60_000L
        val settingsKey = "${s.overlayCorner}/${s.randomOverlayPosition}/${s.overlaySize}"
        if (minuteNow != currentMinute || positionSettings != settingsKey) {
            corner = if (currentMinute == Long.MIN_VALUE || positionSettings != settingsKey) s.overlayCorner.coerceIn(0,3)
                else OverlayMotionPolicy.nextCorner(corner, if (s.randomOverlayPosition) 1 + random.nextInt(3) else 1, s.randomOverlayPosition)
            currentMinute = minuteNow
            positionSettings = settingsKey
            post { place() }
        }

        cameraLocation?.let { location ->
            if (s.showWeather && (cameraWeatherLocation != location || cameraAt == Long.MIN_VALUE || now - cameraAt > 1_800_000L)) {
                cameraWeatherLocation = location
                cameraAt = now
                cameraWeather.load(location) { state ->
                    if (!running || location != cameraLocation) return@load
                    if (state is WeatherState.Ready) {
                        cameraForecast = state.forecast
                        cameraStale = state.stale
                        update()
                    }
                }
            }
        }
        localLocation?.let { location ->
            if (s.showWeather && (viewerWeatherLocation != location || viewerAt == Long.MIN_VALUE || now - viewerAt > 1_800_000L)) {
                viewerWeatherLocation = location
                viewerAt = now
                viewerWeather.load(location) { state ->
                    if (!running || location != localLocation) return@load
                    if (state is WeatherState.Ready) {
                        viewerForecast = state.forecast
                        viewerStale = state.stale
                        update()
                    }
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post { place() }
    }

    private fun place() {
        if (width <= 0 || height <= 0) return
        val safeX = max(dp(48), (width * .05f).roundToInt())
        val safeY = max(dp(40), (height * .05f).roundToInt())
        // Preserve at least a 5% TV-safe margin, independent of resolution/density.
        val maxPanelWidth = ((width - safeX * 2) * .50f).roundToInt().coerceAtLeast(dp(160))
        val maxTextWidth = (maxPanelWidth - dp(12)).coerceAtLeast(dp(130))
        if (cameraPlace.maxWidth != maxTextWidth) cameraPlace.maxWidth = maxTextWidth
        if (viewerPlace.maxWidth != maxTextWidth) viewerPlace.maxWidth = maxTextWidth
        val positionsWithBoth = AmbientLayoutPolicy.layout(
            width, height, safeX, safeY,
            if (cameraLocation != null) AmbientLayoutPolicy.Size(cameraGroup.width, cameraGroup.height)
                else AmbientLayoutPolicy.Size(0, 0),
            AmbientLayoutPolicy.Size(viewerGroup.width, viewerGroup.height),
            corner,
        )
        // If the camera text and the seven-day forecast cannot coexist safely,
        // preserve the user's forecast and suppress the optional camera label.
        val positions = positionsWithBoth ?: AmbientLayoutPolicy.layout(
            width, height, safeX, safeY, AmbientLayoutPolicy.Size(0, 0),
            AmbientLayoutPolicy.Size(viewerGroup.width, viewerGroup.height), corner,
        )
        cameraSuppressedForSpace = positionsWithBoth == null
        cameraGroup.visibility = if (cameraLocation != null && !cameraSuppressedForSpace) View.VISIBLE else View.INVISIBLE
        // Never draw unpositioned text against an overscan edge.
        if (positions == null) { cameraGroup.visibility = View.INVISIBLE; viewerGroup.visibility = View.INVISIBLE; return }
        val changed = positions != lastPositions
        if (!changed) return
        val animate = lastPositions != null
        lastPositions = positions
        fun position(view: View, pos: AmbientLayoutPolicy.Position) {
            if (animate) view.animate().translationX(pos.x.toFloat()).translationY(pos.y.toFloat()).setDuration(900).start()
            else { view.translationX = pos.x.toFloat(); view.translationY = pos.y.toFloat() }
        }
        position(cameraGroup, positions.first)
        position(viewerGroup, positions.second)
    }

    override fun onDetachedFromWindow() {
        stop()
        resolver.close()
        cameraWeather.close()
        viewerWeather.close()
        super.onDetachedFromWindow()
    }
}
