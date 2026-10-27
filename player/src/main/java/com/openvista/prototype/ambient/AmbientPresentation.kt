package com.openvista.prototype.ambient

import android.content.Context
import android.text.format.DateFormat
import com.openvista.prototype.R
import com.openvista.prototype.library.AppSettings
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone

/** Formatting never derives a source place or timezone from a title, URL or viewer location. */
internal class AmbientPresentation(private val context: Context) {
    private val locale get() = context.resources.configuration.locales[0]
    private var formatKey = ""
    private var time = SimpleDateFormat("HH:mm", locale)
    private var date = SimpleDateFormat("EEE, MMM d", locale)

    fun zone(value: String): TimeZone? = runCatching {
        value.takeIf(String::isNotBlank)?.let { TimeZone.getTimeZone(ZoneId.of(it)) }
    }.getOrNull()

    fun clock(zone: TimeZone, settings: AppSettings, now: Date): String {
        prepare(settings)
        time.timeZone = zone
        return time.format(now)
    }

    fun date(zone: TimeZone, settings: AppSettings, now: Date): String {
        prepare(settings)
        date.timeZone = zone
        return date.format(now)
    }

    private fun prepare(settings: AppSettings) {
        val pattern = when (settings.clockFormat) {
            "12" -> "h:mm a"
            "24" -> "HH:mm"
            else -> if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
        }
        val key = "$locale/$pattern"
        if (formatKey == key) return
        formatKey = key
        time = SimpleDateFormat(pattern, locale)
        date = SimpleDateFormat("EEE, MMM d", locale)
    }

    fun fahrenheit(settings: AppSettings) = settings.temperatureUnit == "fahrenheit" ||
        (settings.temperatureUnit == "system" && context.applicationContext.resources.configuration.locales[0].country == "US")

    fun temperature(value: Double?, settings: AppSettings, unit: Boolean = true): String {
        if (value == null || !value.isFinite()) return context.getString(R.string.ambient_unknown)
        val fahrenheit = fahrenheit(settings)
        return String.format(locale, "%.0f°%s", if (fahrenheit) value * 9 / 5 + 32 else value,
            if (unit) { if (fahrenheit) "F" else "C" } else "")
    }

    fun metrics(current: CurrentWeather?, settings: AppSettings): String = listOfNotNull(
        current?.humidity?.let { context.getString(R.string.ambient_humidity, percent(it)) },
        current?.windKph?.let { speed ->
            val imperial = fahrenheit(settings)
            val value = String.format(locale, "%.0f", if (imperial) speed / 1.609344 else speed)
            context.getString(R.string.ambient_wind, context.getString(
                if (imperial) R.string.ambient_metric_mph else R.string.ambient_metric_kph, value))
        },
    ).joinToString("  ·  ")

    fun percent(value: Double?) = value?.takeIf(Double::isFinite)?.let {
        java.text.NumberFormat.getPercentInstance(locale).format(it.coerceIn(0.0, 100.0) / 100)
    } ?: context.getString(R.string.ambient_unknown)

    fun days(forecast: Forecast?, count: Int, zone: TimeZone): List<ForecastDay> {
        val today = LocalDate.now(zone.toZoneId())
        return forecast?.days.orEmpty().mapNotNull { day ->
            runCatching { LocalDate.parse(day.date) }.getOrNull()?.let { it to day }
        }.filter { (date, _) -> !date.isBefore(today) && date.isBefore(today.plusDays(7)) }
            .sortedBy { it.first }.distinctBy { it.first }.take(count.coerceIn(3, 7)).map { it.second }
    }

    fun dayLabel(value: String, zone: TimeZone): String = runCatching {
        val day = LocalDate.parse(value)
        if (day == LocalDate.now(zone.toZoneId())) context.getString(R.string.ambient_today)
        else day.format(DateTimeFormatter.ofPattern("EEE", locale))
    }.getOrElse { context.getString(R.string.ambient_unknown) }
}
