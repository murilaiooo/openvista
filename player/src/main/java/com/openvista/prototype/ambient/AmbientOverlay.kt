package com.openvista.prototype.ambient

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.openvista.prototype.R
import com.openvista.prototype.library.AppSettings
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.TimeZone

/**
 * Two independent TV-safe informational panels. Camera identity/time and regional weather
 * never share one static box. Positions rotate each minute by default; randomized mode
 * moves to a different corner without affecting playback or provider refresh cadence.
 * This reduces persistent static UI exposure, but does not guarantee burn-in prevention.
 */
class AmbientOverlay(context: Context, private val store: LibraryStore) : FrameLayout(context) {
    private val main = Handler(Looper.getMainLooper())
    private val random = Random()
    private val weatherRepository = WeatherRepository(context)

    private val scenePanel = panel()
    private val weatherPanel = panel()
    private val sceneEyebrow = field(12f, 0xFF82CBE1.toInt(), true)
    private val sceneTitle = field(25f, Color.WHITE, true)
    private val cameraLocation = field(16f, 0xFFCFDAE3.toInt())
    private val timeEyebrow = field(12f, 0xFF82CBE1.toInt(), true)
    private val clock = field(33f, Color.WHITE, true)
    private val date = field(15f, 0xFFCFDAE3.toInt())
    private val weatherEyebrow = field(12f, 0xFF82CBE1.toInt(), true)
    private val weather = field(25f, Color.WHITE, true)
    private val weatherDetail = field(15f, 0xFFCFDAE3.toInt())
    private val credit = field(11f, 0xFFCFDAE3.toInt())

    private var item: LibraryItem? = null
    private var forecast: Forecast? = null
    private var locationKey: WeatherLocation? = null
    private var weatherAt = Long.MIN_VALUE
    private var interactionAt = SystemClock.elapsedRealtime()
    private var running = false
    private var minute = Long.MIN_VALUE
    private var positionSlot = 0
    private var placementSettings = ""
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
        setClipChildren(false)
        setClipToPadding(false)
        setPadding(dp(28), dp(22), dp(28), dp(22))
        scenePanel.apply {
            minimumWidth = dp(300)
            listOf(sceneEyebrow, sceneTitle, cameraLocation, timeEyebrow, clock, date).forEach {
                it.layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            }
            addView(sceneEyebrow)
            addView(sceneTitle)
            addView(cameraLocation)
            addView(space(12))
            addView(timeEyebrow)
            addView(clock)
            addView(date)
        }
        weatherPanel.apply {
            minimumWidth = dp(280)
            listOf(weatherEyebrow, weather, weatherDetail, credit).forEach {
                it.layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            }
            addView(weatherEyebrow)
            addView(weather)
            addView(weatherDetail)
            addView(credit)
        }
        addView(scenePanel, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(weatherPanel, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        credit.visibility = View.GONE
        sceneTitle.maxLines = 2
        sceneTitle.ellipsize = TextUtils.TruncateAt.END
        weatherDetail.maxLines = 2
        weatherDetail.ellipsize = TextUtils.TruncateAt.END
    }

    private fun panel() = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(21), dp(15), dp(21), dp(16))
        background = GradientDrawable().apply {
            cornerRadius = dp(18).toFloat()
            setColor(0xDE0A1929.toInt())
            setStroke(dp(1), 0x6672A5BE)
        }
        elevation = dp(5).toFloat()
        clipToPadding = false
    }

    private fun field(size: Float, color: Int, bold: Boolean = false) = TextView(context).apply {
        textSize = size
        setTextColor(color)
        setTypeface(Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL))
        includeFontPadding = false
        maxWidth = dp(390)
        setPadding(0, dp(3), 0, dp(3))
        setShadowLayer(dp(2).toFloat(), 0f, 1f, 0xA8000000.toInt())
    }

    private fun space(height: Int) = View(context).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + .5f).toInt()

    fun start() {
        if (!running) {
            running = true
            main.removeCallbacks(tick)
            main.post(tick)
        }
    }

    fun stop() {
        running = false
        main.removeCallbacks(tick)
        weatherRepository.cancel()
    }

    fun updateItem(source: LibraryItem?) {
        item = source
        weatherAt = Long.MIN_VALUE
        forecast = null
        locationKey = null
        if (running) update()
    }

    fun userInteraction() {
        interactionAt = SystemClock.elapsedRealtime()
        if (running) update()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post { placePanels() }
    }

    override fun onDetachedFromWindow() {
        stop()
        weatherRepository.close()
        super.onDetachedFromWindow()
    }

    private fun update() {
        val s = store.settings()
        val nowElapsed = SystemClock.elapsedRealtime()
        val idle = nowElapsed - interactionAt
        visibility = if (s.hideOverlay && idle >= s.overlayHideSeconds.coerceIn(10, 3600) * 1_000L)
            View.INVISIBLE else View.VISIBLE

        val camera = item
        sceneEyebrow.setText(if (camera?.family == com.openvista.prototype.library.MediaFamily.CAMERAS)
            R.string.camera_label else R.string.video_label)
        sceneTitle.text = camera?.title?.takeIf(String::isNotBlank) ?: context.getString(R.string.app_name)
        val region = camera?.let { listOf(it.city, it.country).filter(String::isNotBlank).joinToString(", ") }.orEmpty()
        cameraLocation.text = region
        cameraLocation.visibility = if (s.showLocation && region.isNotBlank()) View.VISIBLE else View.GONE

        val knownZone = camera?.timeZone?.takeIf { it.isNotBlank() && it in TimeZone.getAvailableIDs() }
        val isCamera = camera?.family == com.openvista.prototype.library.MediaFamily.CAMERAS
        val timezone = when {
            isCamera -> knownZone
            else -> s.viewerTimeZone.takeIf { it.isNotBlank() && it in TimeZone.getAvailableIDs() }
                ?: TimeZone.getDefault().id
        }
        timeEyebrow.setText(R.string.local_time_label)
        val locale = resources.configuration.locales[0]
        val pattern = when (s.clockFormat) {
            "12" -> "h:mm a"
            "24" -> "HH:mm"
            else -> if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
        }
        clock.text = if (timezone == null) context.getString(R.string.time_zone_unavailable)
            else SimpleDateFormat(pattern, locale).apply { timeZone = TimeZone.getTimeZone(timezone) }.format(Date())
        date.text = if (timezone == null) ""
            else java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, locale)
                .apply { timeZone = TimeZone.getTimeZone(timezone) }.format(Date())
        clock.visibility = if (s.showClock) View.VISIBLE else View.GONE
        timeEyebrow.visibility = clock.visibility
        date.visibility = if (s.showDate && timezone != null) View.VISIBLE else View.GONE

        weatherPanel.visibility = if (s.showWeather) View.VISIBLE else View.GONE
        val cameraWeather = camera?.takeIf { it.latitude != null && it.longitude != null }?.let {
            WeatherLocation(it.city.ifBlank { it.country.ifBlank { it.title } }, it.latitude!!, it.longitude!!, it.timeZone)
        }
        val viewerWeather = if (s.viewerLatitude != null && s.viewerLongitude != null)
            WeatherLocation(s.viewerCity, s.viewerLatitude, s.viewerLongitude, s.viewerTimeZone) else null
        val target = if (s.weatherContext == "camera") cameraWeather ?: viewerWeather else viewerWeather
        val usesCameraWeather = s.weatherContext == "camera" && cameraWeather != null
        weatherEyebrow.setText(if (usesCameraWeather) R.string.weather_region_label else R.string.weather_viewer_label)
        weatherDetail.text = target?.name?.takeIf(String::isNotBlank) ?: context.getString(R.string.overlay_location_unknown)

        val minuteNow = nowElapsed / 60_000L
        val key = "${s.overlayCorner}/${s.moveOverlay}/${s.randomOverlayPosition}/${s.overlayX}/${s.overlayY}"
        if (minute != minuteNow || placementSettings != key) {
            if (minute == Long.MIN_VALUE || placementSettings != key || !s.moveOverlay) {
                positionSlot = s.overlayCorner.coerceIn(0, 3)
            } else if (s.randomOverlayPosition) {
                positionSlot = OverlayMotionPolicy.nextCorner(positionSlot, 1 + random.nextInt(3), randomized = true)
            } else {
                positionSlot = OverlayMotionPolicy.nextCorner(positionSlot, 1, randomized = false)
            }
            minute = minuteNow
            placementSettings = key
            post { placePanels() }
        }
        val scale = s.overlaySize.coerceIn(14, 40) / 20f
        sceneTitle.textSize = 25f * scale
        clock.textSize = 33f * scale
        weather.textSize = 25f * scale
        scenePanel.alpha = s.overlayOpacity.coerceIn(20, 100) / 100f
        weatherPanel.alpha = scenePanel.alpha

        if (s.showWeather && (weatherAt == Long.MIN_VALUE || target != locationKey ||
                nowElapsed - weatherAt >= 900_000L)) {
            locationKey = target
            weatherAt = nowElapsed
            forecast = null
            weather.text = context.getString(R.string.weather_loading)
            credit.visibility = View.GONE
            weatherRepository.load(target) { state ->
                if (!running || !store.settings().showWeather || target != locationKey) return@load
                forecast = (state as? WeatherState.Ready)?.forecast
                weather.text = weatherLabel(state, s)
                credit.text = forecast?.attribution.orEmpty()
                credit.visibility = if (forecast?.requiresVisibleAttribution == true) View.VISIBLE else View.GONE
                post { placePanels() }
            }
        }
    }

    private fun weatherLabel(state: WeatherState, s: AppSettings): String = when (state) {
        is WeatherState.Ready -> state.forecast.current?.temperatureC?.let { c ->
            val fahrenheit = s.temperatureUnit == "fahrenheit" ||
                (s.temperatureUnit == "system" && resources.configuration.locales[0].country == "US")
            val value = if (fahrenheit) c * 9 / 5 + 32 else c
            val unit = if (fahrenheit) "F" else "C"
            String.format(resources.configuration.locales[0], "%.0f°%s", value, unit) +
                "  " + (state.forecast.current?.let { WeatherText.description(context, it) }.orEmpty()) +
                if (state.stale) " · " + context.getString(R.string.weather_cached) else ""
        } ?: context.getString(R.string.weather_unavailable)
        WeatherState.NoLocation -> context.getString(R.string.weather_no_location)
        WeatherState.ProviderRequired -> context.getString(R.string.weather_provider_required)
        WeatherState.Unavailable -> context.getString(R.string.weather_unavailable)
        WeatherState.Loading -> context.getString(R.string.weather_loading)
    }

    private fun placePanels() {
        if (width <= 0 || height <= 0 || scenePanel.width == 0 || weatherPanel.width == 0) {
            // A first layout pass will arrive via onSizeChanged.
            return
        }
        val s = store.settings()
        // Opposite diagonal corners keep city/time separate from the weather card.
        val slot = if (s.moveOverlay) positionSlot else s.overlayCorner.coerceIn(0, 3)
        val opposite = OverlayMotionPolicy.oppositeCorner(slot)
        fun position(card: View, corner: Int) {
            val availX = (width - paddingLeft - paddingRight - card.width).coerceAtLeast(0)
            val availY = (height - paddingTop - paddingBottom - card.height).coerceAtLeast(0)
            val right = corner % 2 == 1
            val bottom = corner >= 2
            val shiftX = (availX * (s.overlayX.coerceIn(0, 100) / 100f) * .20f).toInt()
            val shiftY = (availY * (s.overlayY.coerceIn(0, 100) / 100f) * .20f).toInt()
            // Each card keeps a safe margin while changing quadrant every minute.
            val x = if (right) paddingLeft + availX - shiftX else paddingLeft + shiftX
            val y = if (bottom) paddingTop + availY - shiftY else paddingTop + shiftY
            card.translationX = x.toFloat()
            card.translationY = y.toFloat()
        }
        position(scenePanel, slot)
        position(weatherPanel, opposite)
    }
}
