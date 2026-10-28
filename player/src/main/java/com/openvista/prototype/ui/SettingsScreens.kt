package com.openvista.prototype.ui

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.media3.common.util.UnstableApi
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.openvista.prototype.LibraryRoute
import com.openvista.prototype.OpenVistaActivity
import com.openvista.prototype.R
import com.openvista.prototype.diagnostics.DiagnosticJournal
import com.openvista.prototype.ambient.AmbientOverlay
import com.openvista.prototype.ambient.WeatherLocation
import com.openvista.prototype.ambient.WeatherRepository
import com.openvista.prototype.ambient.WeatherState
import com.openvista.prototype.ambient.ProviderPolicy
import com.openvista.prototype.ambient.WeatherText
import com.openvista.prototype.library.AppSettings
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.MediaFamily
import com.openvista.prototype.pairing.PairingCoordinator
import com.openvista.prototype.pairing.PairingSecurity
import com.openvista.prototype.update.UpdateChannel
import com.openvista.prototype.update.UpdateCoordinator
import com.openvista.prototype.update.UpdateState
import java.util.TimeZone

/**
 * Content of the single Settings menu. Every preference has exactly one home category;
 * legacy routes (overlays, schedule, privacy, updates) open the category that now owns them.
 */
@UnstableApi
class SettingsScreens(private val host: OpenVistaActivity) {
    private val ui get() = host.ui
    private val store get() = host.store
    private val weather = WeatherRepository(host)
    private val updater = UpdateCoordinator(host)
    private var updateState: UpdateState = UpdateState.Idle
    private var pairing: PairingCoordinator? = null
    private var automaticCheckDone = false
    private var preview: AmbientOverlay? = null
    private fun s(id: Int) = host.getString(id)

    fun ownsPage(page: String) = page in setOf("settings", "playback_settings", "overlays", "overlay_editor",
        "weather_settings", "advanced", "schedule", "privacy", "updates", "screensaver", "appearance", "library_settings", "about")

    fun createShell(page: String): View = SettingsShell(host).create(page, ::render)

    fun render(body: LinearLayout, page: String) {
        preview?.stop(); preview = null
        when (SettingsShell.category(page)) {
            "weather_settings" -> weatherSettings(body)
            "advanced" -> advanced(body)
            "screensaver" -> screensaver(body)
            "appearance" -> appearance(body)
            "library_settings" -> librarySettings(body)
            "about" -> about(body)
            else -> playback(body)
        }
    }

    private fun playback(body: LinearLayout) {
        pageHeading(body, R.string.playback, R.string.settings_playback_description)
        val settings = store.settings()
        val video = group(body, R.string.settings_group_video)
        number(video, R.string.rotation, settings.rotationMinutes, 1..60, TvIcon.Symbol.TIMER, R.string.summary_rotation,
            R.string.settings_minutes_value) { value, number -> value.copy(rotationMinutes = number) }
        choice(video, R.string.quality, listOf(0, 720, 1080, 2160), listOf(s(R.string.automatic), "720p", "1080p", "2160p"),
            settings.maxVideoHeight, TvIcon.Symbol.QUALITY, R.string.summary_quality) { value ->
            store.saveSettings(store.settings().copy(maxVideoHeight = value))
        }
        choice(video, R.string.aspect_ratio, listOf("crop", "fit"), listOf(s(R.string.crop), s(R.string.fit)), settings.resizeMode,
            TvIcon.Symbol.FIT, R.string.summary_fit) { store.saveSettings(store.settings().copy(resizeMode = it)) }
        choice(video, R.string.transitions, listOf(false, true), listOf(s(R.string.instant), s(R.string.smooth)),
            settings.smoothTransitions, TvIcon.Symbol.TRANSITION, R.string.summary_transition) {
            store.saveSettings(store.settings().copy(smoothTransitions = it))
        }
        val sound = group(body, R.string.settings_group_sound_power)
        toggle(sound, R.string.sound, !settings.muted, TvIcon.Symbol.VOLUME, R.string.summary_sound) { it.copy(muted = !it.muted) }
        toggle(sound, R.string.keep_awake, settings.keepAwake, TvIcon.Symbol.POWER, R.string.summary_keep_awake) {
            it.copy(keepAwake = !it.keepAwake)
        }
        note(sound, R.string.power_notice)
        val fallback = group(body, R.string.settings_group_fallback)
        val playlists = store.playlists().filter { it.family == MediaFamily.VIDEOS }
        choice(fallback, R.string.fallback_set, listOf<String?>(null) + playlists.map { it.id },
            listOf(s(R.string.off)) + playlists.map { it.name }, settings.fallbackPlaylistId, TvIcon.Symbol.PLAYLIST,
            R.string.summary_fallback) { store.saveSettings(store.settings().copy(fallbackPlaylistId = it)) }
        toggle(fallback, R.string.stay_local, settings.stayLocal, TvIcon.Symbol.FOLDER, R.string.summary_stay_local) {
            it.copy(stayLocal = !it.stayLocal)
        }
        note(fallback, R.string.fallback_notice)
    }

    private fun screensaver(body: LinearLayout) {
        pageHeading(body, R.string.settings_category_screensaver, R.string.settings_screensaver_description)
        val settings = store.settings()
        val care = group(body, R.string.settings_group_display_care)
        toggle(care, R.string.overlay_movement, settings.moveOverlay, TvIcon.Symbol.MOVE, R.string.summary_movement) {
            it.copy(moveOverlay = !it.moveOverlay)
        }
        number(care, R.string.settings_move_interval, settings.overlayMoveMinutes, 1..30, TvIcon.Symbol.TIMER,
            R.string.summary_move_interval, R.string.settings_minutes_value) { value, number -> value.copy(overlayMoveMinutes = number) }
        toggle(care, R.string.overlay_hiding, settings.hideOverlay, TvIcon.Symbol.EYE, R.string.summary_hiding) {
            it.copy(hideOverlay = !it.hideOverlay)
        }
        number(care, R.string.overlay_hide_delay, settings.overlayHideSeconds, 10..3600, TvIcon.Symbol.CLOCK,
            R.string.summary_hide_delay, R.string.settings_seconds_value) { value, number -> value.copy(overlayHideSeconds = number) }
        note(care, R.string.overlay_notice)
        val window = group(body, R.string.settings_group_schedule)
        toggle(window, R.string.schedule_enable, settings.scheduleEnabled, TvIcon.Symbol.CLOCK, R.string.summary_schedule) {
            it.copy(scheduleEnabled = !it.scheduleEnabled)
        }
        time(window, R.string.schedule_start, settings.scheduleStartMinute) { value, minute -> value.copy(scheduleStartMinute = minute) }
        time(window, R.string.schedule_end, settings.scheduleEndMinute) { value, minute -> value.copy(scheduleEndMinute = minute) }
        note(window, R.string.schedule_notice)
        val status = group(body, R.string.settings_group_system_status)
        ui.settingRow(status, s(R.string.screensaver), s(R.string.settings_kept_disabled), "screensaver_status",
            s(R.string.settings_screensaver_hold), false, TvIcon.Symbol.SCREENSAVER, TvUi.RowControl.NONE) {}
        ui.settingRow(status, s(R.string.open_screensaver_settings), key = "open_screensaver_settings",
            summary = s(R.string.summary_open_screensaver), icon = TvIcon.Symbol.SETTINGS) {
            host.showDialog(AlertDialog.Builder(host).setTitle(R.string.open_screensaver_settings)
                .setMessage(R.string.settings_system_settings_confirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.settings_open_system_settings) { _, _ ->
                    try { host.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)) }
                    catch (_: RuntimeException) { host.message(R.string.system_settings_unavailable) }
                }.create())
        }
        note(status, R.string.screensaver_notice)
    }

    private fun appearance(body: LinearLayout) {
        pageHeading(body, R.string.settings_category_appearance, R.string.settings_appearance_description)
        val settings = store.settings()
        val language = group(body, R.string.language)
        choice(language, R.string.language, listOf("system", "en", "pt-BR"),
            listOf(s(R.string.system_default), s(R.string.language_english), s(R.string.language_portuguese)), settings.language,
            TvIcon.Symbol.LANGUAGE, R.string.summary_language) { value ->
            store.saveSettings(store.settings().copy(language = value))
            AppLocale.save(host, value)
            host.recreate()
        }
        overlayPreview(body)
        val composition = group(body, R.string.settings_group_composition)
        choice(composition, R.string.settings_overlay_layout, listOf("cinematic", "compact"),
            listOf(s(R.string.settings_layout_cinematic), s(R.string.settings_layout_compact)), settings.overlayLayout,
            TvIcon.Symbol.LAYERS, R.string.summary_layout) { store.saveSettings(store.settings().copy(overlayLayout = it)) }
        val preset = when {
            !settings.showClock && !settings.showWeather && !settings.showDate && !settings.showLocation &&
                !settings.showBrand && !settings.showSourceInfo && !settings.showViewerInfo && !settings.showForecast -> "none"
            settings.showClock && !settings.showWeather && !settings.showDate && !settings.showLocation &&
                !settings.showBrand && !settings.showSourceInfo && settings.showViewerInfo && !settings.showForecast -> "clock"
            settings.showClock && settings.showWeather && settings.showDate && settings.showLocation &&
                settings.showBrand && settings.showSourceInfo && settings.showViewerInfo &&
                settings.showForecast && settings.overlayLayout == "cinematic" -> "cinematic"
            settings.showClock && settings.showWeather && !settings.showDate && settings.showLocation &&
                settings.showBrand && settings.showSourceInfo && settings.showViewerInfo &&
                !settings.showForecast && settings.overlayLayout == "compact" -> "compact"
            else -> "custom"
        }
        choice(composition, R.string.overlay_preset, listOf("cinematic", "compact", "clock", "none"),
            listOf(s(R.string.settings_layout_cinematic), s(R.string.settings_layout_compact), s(R.string.preset_minimal), s(R.string.preset_none)),
            preset, TvIcon.Symbol.LAYERS, R.string.summary_preset) { selected ->
            val all = selected == "cinematic" || selected == "compact"
            store.saveSettings(store.settings().copy(overlayLayout = if (selected == "compact") "compact" else "cinematic",
                showClock = selected != "none", showWeather = all, showDate = all, showLocation = all,
                showBrand = all, showSourceInfo = all, showViewerInfo = selected != "none", showForecast = selected == "cinematic"))
        }
        val visibility = group(body, R.string.settings_group_visibility)
        toggle(visibility, R.string.settings_show_brand, settings.showBrand, TvIcon.Symbol.EYE, R.string.summary_show_brand) {
            it.copy(showBrand = !it.showBrand)
        }
        toggle(visibility, R.string.settings_show_source, settings.showSourceInfo, TvIcon.Symbol.CAMERA, R.string.summary_show_source) {
            it.copy(showSourceInfo = !it.showSourceInfo)
        }
        toggle(visibility, R.string.settings_show_viewer, settings.showViewerInfo, TvIcon.Symbol.NAVIGATE, R.string.summary_show_viewer) {
            it.copy(showViewerInfo = !it.showViewerInfo)
        }
        toggle(visibility, R.string.settings_show_forecast, settings.showForecast, TvIcon.Symbol.WEATHER, R.string.summary_show_forecast) {
            it.copy(showForecast = !it.showForecast)
        }
        toggle(visibility, R.string.location, settings.showLocation, TvIcon.Symbol.PIN, R.string.summary_location_label) {
            it.copy(showLocation = !it.showLocation)
        }
        val position = group(body, R.string.settings_group_position)
        choice(position, R.string.overlay_corner, listOf(0, 1, 2, 3),
            listOf(s(R.string.top_left), s(R.string.top_right), s(R.string.bottom_left), s(R.string.bottom_right)), settings.overlayCorner,
            TvIcon.Symbol.MOVE, R.string.summary_corner) { store.saveSettings(store.settings().copy(overlayCorner = it)) }
        number(position, R.string.overlay_size, settings.overlaySize, 14..40, TvIcon.Symbol.TEXT_SIZE, R.string.summary_size) { value, number ->
            value.copy(overlaySize = number)
        }
        number(position, R.string.overlay_x, settings.overlayX, 0..100, TvIcon.Symbol.MOVE, R.string.summary_offset,
            R.string.settings_percent_value) { value, number -> value.copy(overlayX = number) }
        number(position, R.string.overlay_y, settings.overlayY, 0..100, TvIcon.Symbol.MOVE, R.string.summary_offset,
            R.string.settings_percent_value) { value, number -> value.copy(overlayY = number) }
        choice(position, R.string.overlay_color, listOf(-1, 0xFFFFE4B5.toInt(), 0xFFB5EBFF.toInt()),
            listOf(s(R.string.white), s(R.string.warm), s(R.string.cool)), settings.overlayColor, TvIcon.Symbol.PALETTE,
            R.string.summary_color) { store.saveSettings(store.settings().copy(overlayColor = it)) }
        val opacity = group(body, R.string.settings_group_opacity)
        number(opacity, R.string.overlay_opacity, settings.overlayOpacity, 20..100, TvIcon.Symbol.OPACITY, R.string.summary_opacity,
            R.string.settings_percent_value) { value, number -> value.copy(overlayOpacity = number) }
        number(opacity, R.string.settings_source_opacity, settings.sourceOpacity, 20..100, TvIcon.Symbol.OPACITY, null,
            R.string.settings_percent_value) { value, number -> value.copy(sourceOpacity = number) }
        number(opacity, R.string.settings_viewer_opacity, settings.viewerOpacity, 20..100, TvIcon.Symbol.OPACITY, null,
            R.string.settings_percent_value) { value, number -> value.copy(viewerOpacity = number) }
        number(opacity, R.string.settings_forecast_opacity, settings.forecastOpacity, 20..100, TvIcon.Symbol.OPACITY, null,
            R.string.settings_percent_value) { value, number -> value.copy(forecastOpacity = number) }
        number(opacity, R.string.settings_brand_opacity, settings.brandOpacity, 20..100, TvIcon.Symbol.OPACITY, null,
            R.string.settings_percent_value) { value, number -> value.copy(brandOpacity = number) }
    }

    private fun overlayPreview(body: LinearLayout) {
        val livePreview = group(body, R.string.settings_live_preview)
        note(livePreview, R.string.settings_preview_description)
        val overlay = AmbientOverlay(host, store)
        preview = overlay
        store.lastItemId()?.let(store::item)?.let(overlay::updateItem)
        val frame = object : FrameLayout(host) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val width = View.MeasureSpec.getSize(widthMeasureSpec)
                super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(width * 9 / 16, View.MeasureSpec.EXACTLY))
            }
        }.apply {
            isFocusable = false
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            background = TvDesign.panel(host, 0xFF173044.toInt(), TvDesign.rowStroke, radiusDp = 4.6f)
            clipToOutline = true
            addView(overlay, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        livePreview.addView(frame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = ui.dp(4)
        })
        overlay.start()
    }

    private fun weatherSettings(body: LinearLayout) {
        pageHeading(body, R.string.settings_category_weather_clock, R.string.settings_weather_description)
        val settings = store.settings()
        val location = group(body, R.string.settings_group_weather_location)
        note(location, if (ProviderPolicy.WORLDWIDE_WEATHER_APPROVED) R.string.weather_worldwide_notice else R.string.weather_nws_notice)
        if (!ProviderPolicy.WORLDWIDE_WEATHER_APPROVED) note(location, R.string.provider_approval_required)
        choice(location, R.string.weather_context, listOf("camera", "viewer"), listOf(s(R.string.camera_location), s(R.string.viewer_location)),
            settings.weatherContext, TvIcon.Symbol.PIN, R.string.summary_weather_context) {
            store.saveSettings(store.settings().copy(weatherContext = it))
        }
        choice(location, R.string.temperature, listOf("system", "celsius", "fahrenheit"), listOf(s(R.string.system_default), "°C", "°F"),
            settings.temperatureUnit, TvIcon.Symbol.SUN, R.string.summary_temperature) {
            store.saveSettings(store.settings().copy(temperatureUnit = it))
        }
        ui.settingRow(location, s(R.string.viewer_location_edit), settings.viewerCity.ifBlank { s(R.string.settings_not_set) },
            "viewer_location_edit", s(R.string.summary_viewer_location), icon = TvIcon.Symbol.NAVIGATE,
            control = TvUi.RowControl.CHEVRON) { editLocation() }
        ui.settingRow(location, s(R.string.city_search), if (ProviderPolicy.WORLDWIDE_WEATHER_APPROVED) "" else s(R.string.settings_unavailable),
            "city_search", s(if (ProviderPolicy.WORLDWIDE_WEATHER_APPROVED) R.string.summary_city_search else R.string.provider_approval_required),
            ProviderPolicy.WORLDWIDE_WEATHER_APPROVED, TvIcon.Symbol.SEARCH) { citySearch() }
        ui.settingRow(location, s(R.string.device_location), s(R.string.settings_unavailable), "device_location",
            s(R.string.device_location_notice), false, TvIcon.Symbol.NAVIGATE, TvUi.RowControl.NONE) {}
        ui.settingRow(location, s(R.string.forecast), key = "forecast", summary = s(R.string.summary_forecast),
            icon = TvIcon.Symbol.WEATHER) { forecast() }
        choice(location, R.string.settings_forecast_days, listOf(3, 4, 5, 6, 7), listOf("3", "4", "5", "6", "7"),
            settings.viewerForecastDays, TvIcon.Symbol.WEATHER, R.string.summary_forecast_days) {
            store.saveSettings(store.settings().copy(viewerForecastDays = it))
        }
        val clock = group(body, R.string.settings_group_clock)
        toggle(clock, R.string.clock, settings.showClock, TvIcon.Symbol.CLOCK, R.string.summary_show_clock) { it.copy(showClock = !it.showClock) }
        toggle(clock, R.string.date, settings.showDate, TvIcon.Symbol.CLOCK, R.string.summary_show_date) { it.copy(showDate = !it.showDate) }
        toggle(clock, R.string.weather, settings.showWeather, TvIcon.Symbol.SUN, R.string.summary_show_weather) {
            it.copy(showWeather = !it.showWeather)
        }
        choice(clock, R.string.clock_format, listOf("system", "12", "24"), listOf(s(R.string.system_default), "12 h", "24 h"),
            settings.clockFormat, TvIcon.Symbol.CLOCK, R.string.summary_clock_format) {
            store.saveSettings(store.settings().copy(clockFormat = it))
        }
    }

    private fun editLocation() {
        val settings = store.settings()
        val fields = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
        val city = ui.field(fields, s(R.string.city), settings.viewerCity)
        val lat = ui.field(fields, s(R.string.latitude), settings.viewerLatitude?.toString().orEmpty(), 16)
        val lon = ui.field(fields, s(R.string.longitude), settings.viewerLongitude?.toString().orEmpty(), 16)
        val zone = ui.field(fields, s(R.string.time_zone), settings.viewerTimeZone)
        val error = ui.description(""); fields.addView(error)
        val dialog = AlertDialog.Builder(host).setTitle(R.string.viewer_location_edit).setView(ui.scroll(fields))
            .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.save, null).create()
        host.showDialog(dialog)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val latitude = lat.text.toString().toDoubleOrNull(); val longitude = lon.text.toString().toDoubleOrNull()
            val tz = zone.text.toString().trim()
            val empty = lat.text.isBlank() && lon.text.isBlank()
            if ((!empty && (latitude == null || longitude == null || !latitude.isFinite() || !longitude.isFinite() ||
                    latitude !in -90.0..90.0 || longitude !in -180.0..180.0)) || (tz.isNotBlank() && tz !in TimeZone.getAvailableIDs())) {
                error.text = s(R.string.invalid_location); return@setOnClickListener
            }
            try {
                store.saveSettings(store.settings().copy(viewerCity = city.text.toString().trim(), viewerLatitude = latitude,
                    viewerLongitude = longitude, viewerTimeZone = tz))
                dialog.dismiss(); host.refresh(); host.persisted(s(R.string.saved))
            } catch (_: Exception) { error.text = s(R.string.operation_failed) }
        }
    }

    private fun citySearch() {
        if (!ProviderPolicy.WORLDWIDE_WEATHER_APPROVED) { host.message(R.string.provider_approval_required); return }
        val content = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
        content.addView(ui.description(s(R.string.city_search_privacy)))
        val query = ui.field(content, s(R.string.city))
        host.showDialog(AlertDialog.Builder(host).setTitle(R.string.city_search).setView(content)
            .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.search) { _, _ ->
                weather.searchCities(query.text.toString()) { places, attribution ->
                    if (places == null) host.message(R.string.city_search_unavailable)
                    else if (places.isEmpty()) host.message(R.string.no_matches)
                    else host.showDialog(AlertDialog.Builder(host).setTitle(R.string.city_search).setMessage(attribution)
                        .setItems(places.map { it.name }.toTypedArray()) { _, index ->
                            val location = places[index]
                            host.mutate { store.saveSettings(store.settings().copy(viewerCity = location.name,
                                viewerLatitude = location.latitude, viewerLongitude = location.longitude, viewerTimeZone = location.timeZone)) }
                            attribution?.let { host.message(host.getString(R.string.weather_attribution, it)) }
                        }.setNegativeButton(R.string.cancel, null).create())
                }
            }.create())
    }

    fun forecast(item: LibraryItem? = null) {
        val settings = store.settings()
        val source = item ?: store.lastItemId()?.let(store::item)
        val cameraLocation = source?.let {
            if (it.latitude == null || it.longitude == null) null else WeatherLocation(it.city.ifBlank { it.title }, it.latitude, it.longitude, it.timeZone)
        }
        val viewerLocation = if (settings.viewerLatitude != null && settings.viewerLongitude != null)
            WeatherLocation(settings.viewerCity, settings.viewerLatitude, settings.viewerLongitude, settings.viewerTimeZone) else null
        val target = if (settings.weatherContext == "camera") cameraLocation else viewerLocation
        val body = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
        val status = ui.description(s(R.string.weather_loading)); body.addView(status)
        val dialog = AlertDialog.Builder(host).setTitle(R.string.forecast).setView(ui.scroll(body)).setPositiveButton(R.string.close, null).create()
        host.showDialog(dialog)
        dialog.setOnCancelListener { weather.cancel() }
        weather.load(target) { state ->
            if (!dialog.isShowing) return@load
            body.removeAllViews()
            body.addView(ui.description(when (state) {
                WeatherState.NoLocation -> s(R.string.weather_no_location)
                WeatherState.ProviderRequired -> s(R.string.weather_unavailable)
                WeatherState.Loading -> s(R.string.weather_loading)
                WeatherState.Unavailable -> s(R.string.weather_unavailable)
                is WeatherState.Ready -> if (state.stale) s(R.string.stale_weather) else state.forecast.location.name
            }))
            if (state is WeatherState.Ready) {
                val forecast = state.forecast
                forecast.current?.let { current ->
                    body.addView(ui.description(listOf(temperature(current.temperatureC), WeatherText.description(host, current)).filter(String::isNotBlank).joinToString(" · ")))
                }
                forecast.days.forEach { d -> body.addView(ui.description(host.getString(R.string.weather_day,
                    d.date, temperature(d.minimumC), temperature(d.maximumC), metric(d.precipitationChancePct), metric(d.windKph),
                    metric(d.humidity))))
                    body.addView(ui.description(host.getString(R.string.weather_day_extra, metric(d.precipitationMm),
                        localTime(d.sunrise), localTime(d.sunset))))
                }
                if (forecast.requiresVisibleAttribution) body.addView(ui.description(forecast.attribution))
            }
        }
    }

    private fun advanced(body: LinearLayout) {
        pageHeading(body, R.string.settings_category_advanced, R.string.settings_advanced_description)
        val diagnostics = group(body, R.string.diagnostics)
        toggle(diagnostics, R.string.diagnostics_enable, store.settings().diagnosticsEnabled, TvIcon.Symbol.REPORT,
            R.string.summary_diagnostics) { it.copy(diagnosticsEnabled = !it.diagnosticsEnabled) }
        ui.settingRow(diagnostics, s(R.string.report), key = "report", summary = s(R.string.summary_report),
            icon = TvIcon.Symbol.REPORT) { report() }
        val privacy = group(body, R.string.privacy)
        note(privacy, R.string.privacy_body)
        note(privacy, R.string.provider_privacy)
        ui.settingRow(privacy, s(R.string.clear_provider_cache), key = "clear_provider_cache",
            summary = s(R.string.summary_clear_cache), icon = TvIcon.Symbol.REFRESH) { weather.clearCache(); host.message(R.string.saved) }
        val availability = group(body, R.string.settings_group_availability)
        updates(availability)
        ui.settingRow(availability, s(R.string.pairing), s(if (PairingSecurity.REVIEWED) R.string.settings_available else R.string.settings_disabled),
            "pairing", s(R.string.pairing_review_pending), PairingSecurity.REVIEWED, TvIcon.Symbol.LINK,
            TvUi.RowControl.NONE) { startPairing() }
        ui.settingRow(availability, s(R.string.setup), key = "setup", summary = s(R.string.summary_setup),
            icon = TvIcon.Symbol.HOME) { host.open(LibraryRoute("setup")) }
    }

    /** Update controls live in Advanced; they stay disabled until the release channel is approved. */
    private fun updates(body: LinearLayout) {
        if (!UpdateChannel.APPROVED) {
            ui.settingRow(body, s(R.string.check_updates), s(R.string.settings_disabled), "check_updates",
                s(R.string.settings_updates_disabled), false, TvIcon.Symbol.DOWNLOAD, TvUi.RowControl.NONE) {}
            return
        }
        note(body, R.string.update_notice)
        body.addView(ui.description(updateState.message(host)))
        ui.settingRow(body, s(R.string.check_updates), key = "check_updates", icon = TvIcon.Symbol.DOWNLOAD) { updater.discover(::onUpdate) }
        toggle(body, R.string.automatic_updates, store.settings().automaticUpdates, TvIcon.Symbol.DOWNLOAD) {
            it.copy(automaticUpdates = !it.automaticUpdates)
        }
        when (val state = updateState) {
            is UpdateState.Available -> ui.settingRow(body, s(R.string.download_update), key = "download_update",
                icon = TvIcon.Symbol.DOWNLOAD) { updater.download(state.release, ::onUpdate) }
            is UpdateState.Ready -> ui.settingRow(body, s(R.string.install_update), key = "install_update",
                icon = TvIcon.Symbol.DOWNLOAD) { updater.install(state.update, ::onUpdate) }
            UpdateState.NeedsPermission -> ui.settingRow(body, s(R.string.allow_installs), key = "allow_installs",
                icon = TvIcon.Symbol.SHIELD) { launchInstallIntent(updater.unknownAppsSettingsIntent()) }
            is UpdateState.ConfirmationRequired -> ui.settingRow(body, s(R.string.install_update), key = "confirm_update",
                icon = TvIcon.Symbol.DOWNLOAD) { launchInstallIntent(state.intent) }
            else -> Unit
        }
        ui.settingRow(body, s(R.string.cancel), key = "cancel_update", icon = TvIcon.Symbol.POWER) { updater.cancel() }
    }

    private fun librarySettings(body: LinearLayout) {
        pageHeading(body, R.string.settings_category_library, R.string.settings_library_description)
        val items = store.items()
        val sources = group(body, R.string.settings_group_add_sources)
        ui.settingRow(sources, s(R.string.add_stream), key = "library_add_stream", summary = s(R.string.summary_add_stream),
            icon = TvIcon.Symbol.LINK) { host.editSource() }
        ui.settingRow(sources, s(R.string.import_list), key = "library_import_list", summary = s(R.string.summary_import_list),
            icon = TvIcon.Symbol.UPLOAD) { host.pickList() }
        ui.settingRow(sources, s(R.string.choose_folder), key = "library_choose_folder", summary = s(R.string.summary_choose_folder),
            icon = TvIcon.Symbol.FOLDER) { host.pickFolder() }
        ui.settingRow(sources, s(R.string.choose_video), key = "library_choose_video", summary = s(R.string.summary_choose_video),
            icon = TvIcon.Symbol.FILM) { host.pickVideo() }
        val library = group(body, R.string.settings_group_organize)
        count(library, R.string.cameras, items.count { it.family == MediaFamily.CAMERAS }, "library_cameras", TvIcon.Symbol.CAMERA) {
            host.open(LibraryRoute("cameras"))
        }
        count(library, R.string.my_videos, items.count { it.family == MediaFamily.VIDEOS }, "library_videos", TvIcon.Symbol.FILM) {
            host.open(LibraryRoute("videos"))
        }
        count(library, R.string.favorites, items.count { it.favorite }, "library_favorites", TvIcon.Symbol.STAR) {
            host.open(LibraryRoute("favorites"))
        }
        count(library, R.string.settings_camera_playlists, store.playlists().count { it.family == MediaFamily.CAMERAS },
            "library_camera_playlists", TvIcon.Symbol.PLAYLIST) { host.open(LibraryRoute("playlists", family = MediaFamily.CAMERAS)) }
        count(library, R.string.settings_video_playlists, store.playlists().count { it.family == MediaFamily.VIDEOS },
            "library_video_playlists", TvIcon.Symbol.PLAYLIST) { host.open(LibraryRoute("playlists", family = MediaFamily.VIDEOS)) }
        val folders = group(body, R.string.manage_folders)
        count(folders, R.string.manage_folders, store.folders().size, "library_folders", TvIcon.Symbol.FOLDER,
            R.string.summary_manage_folders) { host.open(LibraryRoute("folders")) }
        ui.settingRow(folders, s(R.string.settings_rescan), key = "library_rescan", summary = s(R.string.settings_rescan_description),
            icon = TvIcon.Symbol.REFRESH) { host.rescan() }
        statusCard(body, items.size)
        note(body, R.string.local_only)
    }

    /** Real library facts in the reference's status card: user sources and the zero-bundle policy. */
    private fun statusCard(body: LinearLayout, sources: Int) {
        val card = ui.row().apply {
            gravity = Gravity.TOP
            setPadding(ui.dp(14), ui.dp(10), ui.dp(9), ui.dp(10))
            background = TvDesign.panel(host, 0x0EFFFFFF, TvDesign.rowStroke, radiusDp = 4.6f)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        card.addView(View(host).apply { background = TvIcon(host, TvIcon.Symbol.DATABASE, 18f, 0xFFC8D3DB.toInt(), 1.4f) },
            LinearLayout.LayoutParams(ui.dp(18), ui.dp(18)).apply { marginEnd = ui.dp(15.6f); topMargin = ui.dp(2) })
        val lines = ui.column()
        lines.addView(ui.text(s(R.string.settings_library_status), 11f).apply { setPadding(0, 0, 0, ui.dp(4)) })
        val count = host.resources.getQuantityString(R.plurals.settings_library_sources, sources, sources)
        listOf(count to TvDesign.live, s(R.string.settings_library_no_bundled) to 0xFF7D8A94.toInt()).forEach { (label, color) ->
            lines.addView(ui.row().apply {
                addView(View(host).apply {
                    background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(color) }
                }, LinearLayout.LayoutParams(ui.dp(7), ui.dp(7)).apply { marginEnd = ui.dp(10) })
                addView(ui.text(label, 9.6f, 0xFFE3E9ED.toInt()))
            })
        }
        card.addView(lines, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        for (index in 0 until card.childCount) {
            card.getChildAt(index).importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
        card.contentDescription = listOf(s(R.string.settings_library_status), count, s(R.string.settings_library_no_bundled)).joinToString(". ")
        body.addView(card, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = ui.dp(12)
        })
    }

    private fun about(body: LinearLayout) {
        pageHeading(body, R.string.settings_category_about, R.string.settings_about_description)
        val application = group(body, R.string.app_name)
        val info = try { host.packageManager.getPackageInfo(host.packageName, 0) } catch (_: Exception) { null }
        val version = info?.let { "${it.versionName.orEmpty()} (${it.longVersionCode})" } ?: s(R.string.not_available)
        ui.settingRow(application, host.getString(R.string.settings_version, version), key = "about_version",
            summary = s(R.string.settings_development_preview), enabled = false, icon = TvIcon.Symbol.ABOUT,
            control = TvUi.RowControl.NONE) {}
        note(application, R.string.app_unverified)
        val licenses = group(body, R.string.settings_group_licenses)
        ui.settingRow(licenses, s(R.string.settings_licenses_row), key = "about_licenses", icon = TvIcon.Symbol.REPORT) {
            host.showDialog(AlertDialog.Builder(host).setTitle(R.string.settings_licenses_row)
                .setMessage(R.string.settings_licenses_body).setPositiveButton(R.string.close, null).create())
        }
    }

    private fun launchInstallIntent(intent: Intent) {
        try { host.startActivity(intent) } catch (_: RuntimeException) { host.message(R.string.update_failed) }
    }
    private fun onUpdate(state: UpdateState) {
        updateState = state
        if (state == UpdateState.Failed) DiagnosticJournal.record(host, DiagnosticJournal.Event.UPDATE_ERROR)
        if (SettingsShell.category(host.route.page) == "advanced") host.refresh()
        host.message(state.message(host))
        // This preference cannot activate the publication gate or bypass Android confirmation.
        if (store.settings().automaticUpdates) when (state) {
            is UpdateState.Available -> updater.download(state.release, ::onUpdate)
            is UpdateState.Ready -> updater.install(state.update, ::onUpdate)
            else -> Unit
        }
    }
    fun onForeground() {
        if (!UpdateChannel.APPROVED) return
        val resumed = updater.resumeInstallation(::onUpdate)
        if (!resumed && !automaticCheckDone && store.settings().automaticUpdates) { automaticCheckDone = true; updater.discover(::onUpdate) }
    }
    fun onBackground() { preview?.stop(); pairing?.close(); pairing = null; weather.cancel(); updater.pause() }
    fun onScreenExit() { preview?.stop(); preview = null; pairing?.close(); pairing = null; weather.cancel() }
    fun close() { onBackground(); updater.close(); weather.close() }

    fun startPairing() {
        if (!PairingSecurity.REVIEWED) {
            host.showDialog(AlertDialog.Builder(host).setTitle(R.string.pairing).setMessage(R.string.pairing_review_pending)
                .setPositiveButton(R.string.close, null).create())
            return
        }
        pairing?.close()
        val coordinator = PairingCoordinator(host)
        pairing = coordinator
        var awaitingConsent = false
        val body = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
        body.addView(ui.description(s(R.string.pairing_notice)))
        val status = ui.description(coordinator.availabilityMessage()); body.addView(status)
        val dialog = AlertDialog.Builder(host).setTitle(R.string.pairing).setView(ui.scroll(body))
            .setPositiveButton(R.string.pairing_start, null).setNegativeButton(R.string.pairing_stop) { _, _ -> coordinator.close() }.create()
        host.showDialog(dialog) {
            if (!awaitingConsent) { coordinator.close(); if (pairing === coordinator) pairing = null }
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            coordinator.start({ offer ->
                val matrix = MultiFormatWriter().encode(offer.url, BarcodeFormat.QR_CODE, 400, 400)
                val pixels = IntArray(400 * 400) { index -> if (matrix[index % 400, index / 400]) 0xFF000000.toInt() else -1 }
                val bitmap = Bitmap.createBitmap(pixels, 400, 400, Bitmap.Config.ARGB_8888)
                body.addView(ImageView(host).apply { setImageBitmap(bitmap); contentDescription = s(R.string.pairing) }, LinearLayout.LayoutParams(ui.dp(180), ui.dp(180)))
                // The QR secret is never displayed as text or put in clipboard/logs.
            }, { request ->
                awaitingConsent = true
                host.showDialog(AlertDialog.Builder(host).setMessage(host.getString(R.string.pairing_request, request.displayName))
                    .setPositiveButton(R.string.pairing_approve) { _, _ ->
                        val approved = coordinator.approve(request.id)
                        if (approved == null) host.message(R.string.pairing_expired) else {
                            val source = approved.source
                            host.mutate { store.addItems(listOf(LibraryItem(
                                java.util.UUID.randomUUID().toString(), source.title, source.url, MediaFamily.CAMERAS, source.kind.name))) }
                        }
                    }.setNegativeButton(R.string.pairing_deny) { _, _ -> coordinator.deny(request.id) }
                    .setOnCancelListener { coordinator.deny(request.id) }.create()) {
                        coordinator.close(); if (pairing === coordinator) pairing = null
                    }
            }, { value -> status.text = value })
        }
    }

    private fun report() {
        fun safe(value: String) = value.filter { it.isLetterOrDigit() || it in " ._-" }.take(80)
        val info = try { host.packageManager.getPackageInfo(host.packageName, 0) } catch (_: Exception) { null }
        val report = listOf(s(R.string.report_header), "Package: ${host.packageName}",
            "Version: ${safe(info?.versionName.orEmpty())} (${info?.longVersionCode ?: 0})",
            "Android API: ${Build.VERSION.SDK_INT}", "Model: ${safe(Build.MODEL)}",
            host.playbackDiagnostics().ifBlank { s(R.string.not_available) },
            DiagnosticJournal.report(host),
            s(R.string.app_unverified)).joinToString("\n")
        host.showDialog(AlertDialog.Builder(host).setTitle(R.string.report).setMessage(report)
            .setNeutralButton(R.string.copy) { _, _ ->
                (host.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(s(R.string.report_header), report))
                host.message(R.string.copied)
            }.setPositiveButton(R.string.share) { _, _ ->
                try { host.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, report)
                }, s(R.string.share))) } catch (_: RuntimeException) { host.message(R.string.share_unavailable) }
            }.setNegativeButton(R.string.close, null).create())
    }

    private fun toggle(body: LinearLayout, title: Int, value: Boolean, icon: TvIcon.Symbol? = null, summary: Int? = null,
                       transform: (AppSettings) -> AppSettings) {
        ui.toggleRow(body, s(title), value, "setting_$title", summary?.let(::s), icon = icon) {
            host.mutate { store.saveSettings(transform(store.settings())) }
        }
    }
    private fun <T> choice(body: LinearLayout, title: Int, values: List<T>, labels: List<String>, selected: T,
                           icon: TvIcon.Symbol? = null, summary: Int? = null, action: (T) -> Unit) {
        val index = values.indexOf(selected)
        ui.settingRow(body, s(title), if (index >= 0) labels[index] else s(R.string.settings_custom), "setting_$title",
            summary?.let(::s), icon = icon, control = TvUi.RowControl.DROPDOWN) {
            host.showDialog(AlertDialog.Builder(host).setTitle(title).setSingleChoiceItems(labels.toTypedArray(), index) { dialog, which ->
                dialog.dismiss(); host.mutate { action(values[which]) }
            }.setNegativeButton(R.string.cancel, null).create())
        }
    }
    private fun number(body: LinearLayout, title: Int, value: Int, range: IntRange, icon: TvIcon.Symbol? = null,
                       summary: Int? = null, unit: Int? = null, change: (AppSettings, Int) -> AppSettings) {
        val shown = unit?.let { host.getString(it, value) } ?: value.toString()
        ui.settingRow(body, s(title), shown, "setting_$title", summary?.let(::s), icon = icon, control = TvUi.RowControl.DROPDOWN) {
            val fields = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
            val input = ui.field(fields, "${s(title)} (${range.first}–${range.last})", value.toString(), 5, InputType.TYPE_CLASS_NUMBER)
            val error = ui.description(""); fields.addView(error)
            val dialog = AlertDialog.Builder(host).setTitle(title).setView(fields).setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null).create()
            host.showDialog(dialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val number = input.text.toString().toIntOrNull()
                if (number == null || number !in range) error.text = "${range.first}–${range.last}"
                else { dialog.dismiss(); host.mutate { store.saveSettings(change(store.settings(), number)) } }
            }
        }
    }
    private fun time(body: LinearLayout, title: Int, value: Int, change: (AppSettings, Int) -> AppSettings) {
        ui.settingRow(body, s(title), "%02d:%02d".format(value / 60, value % 60), "setting_$title", icon = TvIcon.Symbol.TIMER,
            control = TvUi.RowControl.DROPDOWN) {
            val fields = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
            val input = ui.field(fields, s(title), "%02d:%02d".format(value / 60, value % 60), 5)
            val error = ui.description(""); fields.addView(error)
            val dialog = AlertDialog.Builder(host).setTitle(title).setView(fields).setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null).create()
            host.showDialog(dialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pieces = input.text.toString().split(':')
                val hour = pieces.getOrNull(0)?.toIntOrNull(); val minute = pieces.getOrNull(1)?.toIntOrNull()
                if (pieces.size != 2 || hour == null || minute == null || hour !in 0..23 || minute !in 0..59) error.text = s(R.string.invalid_time)
                else { dialog.dismiss(); host.mutate { store.saveSettings(change(store.settings(), hour * 60 + minute)) } }
            }
        }
    }
    private fun count(body: LinearLayout, title: Int, value: Int, key: String, icon: TvIcon.Symbol, summary: Int? = null,
                      action: () -> Unit) {
        ui.settingRow(body, s(title), value.toString(), key, summary?.let(::s), icon = icon, control = TvUi.RowControl.CHEVRON) { action() }
    }
    /** The open category's caps label and one-line description, as at the top of the reference panel. */
    private fun pageHeading(body: LinearLayout, title: Int, description: Int) {
        body.addView(ui.caps(s(title)).apply { setPadding(ui.dp(2), ui.dp(4), ui.dp(2), ui.dp(4)) })
        body.addView(ui.text(s(description), 10f, 0xFFC9D4DC.toInt()).apply { setPadding(ui.dp(2), ui.dp(2), ui.dp(2), ui.dp(4)) })
    }
    private fun group(body: LinearLayout, title: Int): LinearLayout {
        val group = ui.column()
        group.addView(ui.caps(s(title), 0xFF8FA3B1.toInt()).apply {
            textSize = 6.8f
            setPadding(ui.dp(2), ui.dp(11), ui.dp(2), ui.dp(5))
        })
        body.addView(group, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        return group
    }
    private fun note(body: LinearLayout, text: Int) {
        body.addView(ui.text(s(text), 8.6f, TvDesign.muted).apply { setPadding(ui.dp(2), ui.dp(5), ui.dp(2), ui.dp(3)) })
    }
    private fun temperature(value: Double?): String {
        if (value == null) return s(R.string.not_available)
        val fahrenheit = store.settings().temperatureUnit == "fahrenheit" ||
            (store.settings().temperatureUnit == "system" && host.applicationContext.resources.configuration.locales[0].country == "US")
        return "%.0f°%s".format(if (fahrenheit) value * 9 / 5 + 32 else value, if (fahrenheit) "F" else "C")
    }
    private fun metric(value: Double?) = value?.let { "%.1f".format(it) } ?: s(R.string.not_available)
    private fun localTime(value: String?): String = value?.let {
        runCatching { java.time.LocalDateTime.parse(it).toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm", host.resources.configuration.locales[0])) }.getOrNull()
    } ?: s(R.string.not_available)
}
