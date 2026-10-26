package com.openvista.prototype.ambient

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.openvista.prototype.library.AppSettings
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random
import java.util.TimeZone

/**
 * Minimal fullscreen-video overlay: text only, no containers, gradients or backgrounds.
 * A city is shown only after an explicit saved location or a successful online geocode.
 * User-supplied media titles are never presented as location labels.
 */
class AmbientOverlay(context: Context, private val store: LibraryStore) : FrameLayout(context) {
    private val handler = Handler(Looper.getMainLooper())
    private val random = Random()
    private val cityResolver = AmbientCityResolver(context)
    private val weatherRepository = WeatherRepository(context)

    private val group = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.START
        background = null
    }
    private val city = text(21f, Color.WHITE, true)
    private val line = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = null
    }
    private val clock = text(46f, Color.WHITE, true)
    private val separation = text(25f, 0xCCFFFFFF.toInt())
    private val temperature = text(35f, Color.WHITE)
    private val credit = text(10f, 0xCCFFFFFF.toInt())
    private var item: LibraryItem? = null
    private var resolved: WeatherLocation? = null
    private var activeWeather: WeatherLocation? = null
    private var latestForecast: Forecast? = null
    private var weatherAt = Long.MIN_VALUE
    private var activeFromGeocoder = false
    private var isViewerLocation = false
    private var running = false
    private var minute = Long.MIN_VALUE
    private var corner = 0
    private var placementKey = ""
    private var initialPlacement = true

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            refresh()
            handler.postDelayed(this, 1_000)
        }
    }

    init {
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        clipChildren = false
        clipToPadding = false
        setPadding(dp(42), dp(38), dp(42), dp(38))
        group.addView(city)
        separation.text = "   ·   "
        line.addView(clock)
        line.addView(separation)
        line.addView(temperature)
        group.addView(line)
        group.addView(credit)
        addView(group, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        group.visibility = View.INVISIBLE
        city.maxWidth = dp(540)
        credit.maxWidth = dp(540)
        group.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> post { place() } }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + .5f).toInt()

    private fun text(size: Float, color: Int, bold: Boolean = false) = TextView(context).apply {
        textSize = size
        setTextColor(color)
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        includeFontPadding = false
        setPadding(0, dp(2), 0, dp(2))
        setShadowLayer(dp(4).toFloat(), 0f, dp(2).toFloat(), 0xCC000000.toInt())
        background = null
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun start() {
        if (!running) {
            running = true
            handler.removeCallbacks(tick)
            handler.post(tick)
            resolve(item)
        }
    }

    fun stop() {
        running = false
        handler.removeCallbacks(tick)
        weatherRepository.cancel()
    }

    fun updateItem(source: LibraryItem?) {
        item = source
        resolved = null
        latestForecast = null
        activeWeather = null
        weatherAt = Long.MIN_VALUE
        group.visibility = View.INVISIBLE
        if (running) resolve(source)
    }

    fun userInteraction() { /* Playback remote controls must not freeze the ambient information. */ }

    private fun resolve(source: LibraryItem?) {
        cityResolver.resolve(source) { location ->
            if (!running || item?.id != source?.id) return@resolve
            resolved = location ?: viewerLocation()
            isViewerLocation = location == null && resolved != null
            activeFromGeocoder = location != null && (source?.latitude == null || source.longitude == null)
            activeWeather = null
            latestForecast = null
            weatherAt = Long.MIN_VALUE
            refresh()
        }
    }

    private fun viewerLocation(): WeatherLocation? {
        val s = store.settings()
        return if (s.viewerLatitude != null && s.viewerLongitude != null &&
            s.viewerLatitude in -90.0..90.0 && s.viewerLongitude in -180.0..180.0 &&
            s.viewerCity.isNotBlank()) WeatherLocation(s.viewerCity, s.viewerLatitude, s.viewerLongitude, s.viewerTimeZone)
        else null
    }

    private fun refresh() {
        val s = store.settings()
        val location = resolved
        if (location == null) {
            group.visibility = View.INVISIBLE
            return
        }
        val isViewer = isViewerLocation
        val name = CityLabelParser.displayCity(location.name)
        city.text = if (isViewer) "$name  ·  " + if (resources.configuration.locales[0].language == "pt") "LOCAL" else "YOUR LOCATION"
            else name
        val zone = runCatching { TimeZone.getTimeZone(java.time.ZoneId.of(location.timeZone)) }.getOrNull()
        clock.text = if (zone != null) {
            val format = when (s.clockFormat) {
                "12" -> "h:mm a"
                "24" -> "HH:mm"
                else -> if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
            }
            SimpleDateFormat(format, resources.configuration.locales[0]).apply { timeZone = zone }.format(Date())
        } else ""
        val temperatureC = if (s.showWeather) latestForecast?.current?.temperatureC else null
        if (temperatureC != null && temperatureC.isFinite()) {
            val fahrenheit = s.temperatureUnit == "fahrenheit" ||
                (s.temperatureUnit == "system" && resources.configuration.locales[0].country == "US")
            val value = if (fahrenheit) temperatureC * 9 / 5 + 32 else temperatureC
            temperature.text = String.format(resources.configuration.locales[0], "%.0f°", value)
            separation.visibility = if (clock.text.isNotBlank()) View.VISIBLE else View.GONE
            temperature.visibility = View.VISIBLE
        } else {
            temperature.text = ""
            temperature.visibility = View.GONE
            separation.visibility = View.GONE
        }
        credit.text = when {
            latestForecast?.requiresVisibleAttribution == true ->
                "open-meteo.com  ·  GeoNames"
            activeFromGeocoder -> "GeoNames  ·  open-meteo.com"
            else -> ""
        }
        credit.visibility = if (credit.text.isNotBlank()) View.VISIBLE else View.GONE
        group.visibility = View.VISIBLE
        group.alpha = s.overlayOpacity.coerceIn(35, 100) / 100f
        val scale = s.overlaySize.coerceIn(14, 36) / 20f
        city.textSize = 21f * scale
        clock.textSize = 46f * scale
        temperature.textSize = 35f * scale

        val now = SystemClock.elapsedRealtime()
        val nextMinute = now / 60_000L
        val key = "${s.overlayCorner}/${s.randomOverlayPosition}/${s.overlayX}/${s.overlayY}"
        if (nextMinute != minute || key != placementKey) {
            corner = if (minute == Long.MIN_VALUE || key != placementKey) s.overlayCorner.coerceIn(0, 3)
                else OverlayMotionPolicy.nextCorner(corner, if (s.randomOverlayPosition) 1 + random.nextInt(3) else 1, s.randomOverlayPosition)
            minute = nextMinute
            placementKey = key
            post { place() }
        }

        if (s.showWeather && (activeWeather != location ||
                weatherAt == Long.MIN_VALUE || now - weatherAt >= 1_800_000L)) {
            activeWeather = location
            weatherAt = now
            latestForecast = null
            weatherRepository.load(location) { state ->
                if (running && activeWeather == location) {
                    latestForecast = (state as? WeatherState.Ready)?.forecast
                    refresh()
                }
            }
        }
    }

    private fun place() {
        if (width <= 0 || height <= 0 || group.width <= 0 || group.height <= 0) return
        val s = store.settings()
        val maxX = (width - paddingLeft - paddingRight - group.width).coerceAtLeast(0)
        val maxY = (height - paddingTop - paddingBottom - group.height).coerceAtLeast(0)
        val right = corner % 2 == 1
        val bottom = corner >= 2
        val x = paddingLeft + if (right) maxX else 0
        val y = paddingTop + if (bottom) maxY else 0
        if (initialPlacement) {
            group.translationX = x.toFloat()
            group.translationY = y.toFloat()
            initialPlacement = false
        } else {
            group.animate().translationX(x.toFloat()).translationY(y.toFloat()).setDuration(950).start()
        }
    }

    override fun onDetachedFromWindow() {
        stop()
        cityResolver.close()
        weatherRepository.close()
        super.onDetachedFromWindow()
    }
}
