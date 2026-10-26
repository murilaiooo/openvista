package com.openvista.prototype.ambient

import android.content.Context
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
import com.openvista.prototype.R
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.LibraryStore
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Informational widgets only. Menu timeout is owned independently by the player screen. */
class AmbientOverlay(context: Context, private val store: LibraryStore) : FrameLayout(context) {
    private val main = Handler(Looper.getMainLooper())
    private val box = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val clock = TextView(context)
    private val date = TextView(context)
    private val location = TextView(context)
    private val weather = TextView(context)
    private var item: LibraryItem? = null
    private var interactionAt = SystemClock.elapsedRealtime()
    private var running = false
    private var forecast: Forecast? = null
    private val weatherRepository = WeatherRepository(context)
    private var locationKey: WeatherLocation? = null
    private var weatherAt = Long.MIN_VALUE
    private val tick = object : Runnable {
        override fun run() { if (running) { update(); main.postDelayed(this, 1000) } }
    }

    init {
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        val margin = (24 * resources.displayMetrics.density).toInt()
        setPadding(margin, margin, margin, margin)
        box.setPadding(margin / 2, margin / 3, margin / 2, margin / 3)
        box.setBackgroundColor(0x66000000)
        listOf(clock, date, location, weather).forEach { view ->
            view.setShadowLayer(3f, 1f, 1f, 0xFF000000.toInt()); box.addView(view)
        }
        clock.setTypeface(clock.typeface, Typeface.NORMAL)
        addView(box, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    fun start() { if (!running) { running = true; main.post(tick) } }
    fun stop() { running = false; main.removeCallbacks(tick); weatherRepository.cancel() }
    fun updateItem(item: LibraryItem?) { this.item = item; weatherAt = Long.MIN_VALUE; if (running) update() }
    fun userInteraction() { interactionAt = SystemClock.elapsedRealtime(); if (running) update() }
    override fun onDetachedFromWindow() { stop(); weatherRepository.close(); super.onDetachedFromWindow() }

    private fun update() {
        val settings = store.settings()
        val idle = SystemClock.elapsedRealtime() - interactionAt
        visibility = if (settings.hideOverlay && idle > settings.overlayHideSeconds.coerceIn(10, 3600) * 1000L)
            View.INVISIBLE else View.VISIBLE
        val zone = if (settings.weatherContext == "camera") item?.timeZone else settings.viewerTimeZone
        val timeZone = zone?.takeIf { it in TimeZone.getAvailableIDs() }?.let(TimeZone::getTimeZone) ?: TimeZone.getDefault()
        val pattern = when (settings.clockFormat) {
            "12" -> "h:mm a"; "24" -> "HH:mm"
            else -> if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
        }
        clock.text = java.text.SimpleDateFormat(pattern, resources.configuration.locales[0]).apply { this.timeZone = timeZone }.format(Date())
        date.text = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, resources.configuration.locales[0])
            .apply { this.timeZone = timeZone }.format(Date())
        location.text = if (settings.weatherContext == "camera") item?.let { listOf(it.city, it.country).filter(String::isNotBlank).joinToString(", ") }.orEmpty()
            else settings.viewerCity
        clock.visibility = if (settings.showClock) View.VISIBLE else View.GONE
        date.visibility = if (settings.showDate) View.VISIBLE else View.GONE
        location.visibility = if (settings.showLocation && location.text.isNotBlank()) View.VISIBLE else View.GONE
        weather.visibility = if (settings.showWeather) View.VISIBLE else View.GONE
        listOf(clock, date, location, weather).forEach {
            it.textSize = settings.overlaySize.coerceIn(14, 40).toFloat()
            it.setTextColor(settings.overlayColor)
        }
        box.alpha = settings.overlayOpacity.coerceIn(20, 100) / 100f
        val corner = if (settings.moveOverlay) ((SystemClock.elapsedRealtime() / 300_000).toInt() + settings.overlayCorner) % 4
            else settings.overlayCorner.coerceIn(0, 3)
        (box.layoutParams as LayoutParams).apply {
            gravity = when (corner) { 1 -> Gravity.TOP or Gravity.END; 2 -> Gravity.BOTTOM or Gravity.START
                3 -> Gravity.BOTTOM or Gravity.END; else -> Gravity.TOP or Gravity.START }
            box.layoutParams = this
        }
        // Percentage offsets remain inside measured safe margins, including long localized text.
        val availableX = (width - paddingLeft - paddingRight - box.width).coerceAtLeast(0)
        val availableY = (height - paddingTop - paddingBottom - box.height).coerceAtLeast(0)
        box.translationX = settings.overlayX.coerceIn(0, 100) / 100f * availableX * if (corner % 2 == 0) 1 else -1
        box.translationY = settings.overlayY.coerceIn(0, 100) / 100f * availableY * if (corner < 2) 1 else -1
        val target = if (settings.weatherContext == "camera") item?.let { source ->
            if (source.latitude == null || source.longitude == null) null else WeatherLocation(source.city.ifBlank { source.title }, source.latitude, source.longitude, source.timeZone)
        } else if (settings.viewerLatitude != null && settings.viewerLongitude != null)
            WeatherLocation(settings.viewerCity, settings.viewerLatitude, settings.viewerLongitude, settings.viewerTimeZone) else null
        if (settings.showWeather && (weatherAt == Long.MIN_VALUE || target != locationKey || SystemClock.elapsedRealtime() - weatherAt > 900_000)) {
            locationKey = target; weatherAt = SystemClock.elapsedRealtime()
            weatherRepository.load(target) { state ->
                forecast = (state as? WeatherState.Ready)?.forecast
                weather.text = when (state) {
                    WeatherState.Loading -> context.getString(R.string.weather_loading)
                    WeatherState.NoLocation -> context.getString(R.string.weather_no_location)
                    WeatherState.ProviderRequired -> context.getString(R.string.weather_provider_required)
                    is WeatherState.Ready -> state.forecast.current?.temperatureC?.let { temperature ->
                        val fahrenheit = settings.temperatureUnit == "fahrenheit" ||
                            (settings.temperatureUnit == "system" && context.applicationContext.resources.configuration.locales[0].country == "US")
                        val temperatureLabel = String.format(resources.configuration.locales[0], "%.0f°%s", if (fahrenheit) temperature * 9 / 5 + 32 else temperature,
                            if (fahrenheit) "F" else "C")
                        temperatureLabel + " " + state.forecast.current?.description.orEmpty() +
                            (if (state.stale) " · ${context.getString(R.string.weather_cached)}" else "") + "\n" + state.forecast.attribution
                    } ?: context.getString(R.string.weather_unavailable)
                    WeatherState.Unavailable -> context.getString(R.string.weather_unavailable)
                }
            }
        }
    }
}
