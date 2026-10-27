package com.openvista.prototype.ui

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings
import android.text.InputType
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
import com.openvista.prototype.ambient.Forecast
import com.openvista.prototype.ambient.WeatherLocation
import com.openvista.prototype.ambient.WeatherRepository
import com.openvista.prototype.ambient.WeatherState
import com.openvista.prototype.library.AppSettings
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.MediaFamily
import com.openvista.prototype.pairing.PairingCoordinator
import com.openvista.prototype.update.UpdateCoordinator
import com.openvista.prototype.update.UpdateState
import com.openvista.prototype.youtube.YouTubeLiveTestActivity
import java.util.TimeZone

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

    fun render(body: LinearLayout, page: String) {
        preview?.stop(); preview = null
        when (page) {
            "playback_settings" -> playback(body)
            "overlays", "overlay_editor" -> overlays(body, page == "overlay_editor")
            "weather_settings" -> weatherSettings(body)
            "advanced" -> advanced(body)
            "schedule" -> schedule(body)
            "privacy" -> privacy(body)
            "updates" -> updates(body)
            "screensaver" -> screensaver(body)
            else -> basic(body)
        }
    }

    private fun basic(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.basic)))
        destination(body, R.string.playback, "playback_settings")
        destination(body, R.string.clock_overlays, "overlays")
        destination(body, R.string.weather, "weather_settings")
        choice(body, R.string.language, listOf("system", "en", "pt-BR"),
            listOf(s(R.string.system_default), s(R.string.language_english), s(R.string.language_portuguese)), store.settings().language) { language ->
            store.saveSettings(store.settings().copy(language = language))
            AppLocale.save(host, language)
            host.recreate()
        }
        destination(body, R.string.screensaver, "screensaver")
        ui.button(body, s(R.string.youtube_test_title), "youtube_live_test") {
            host.startActivity(Intent(host, YouTubeLiveTestActivity::class.java))
        }
        ui.button(body, s(R.string.setup)) { host.open(LibraryRoute("setup")) }
        ui.button(body, s(R.string.pairing)) { startPairing() }
        destination(body, R.string.advanced, "advanced")
    }

    private fun playback(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.playback)))
        toggle(body, R.string.sound, !store.settings().muted) { it.copy(muted = !it.muted) }
        number(body, R.string.rotation, store.settings().rotationMinutes, 1..60) { settings, value -> settings.copy(rotationMinutes = value) }
        choice(body, R.string.quality, listOf(0, 720, 1080, 2160), listOf(s(R.string.automatic), "720p", "1080p", "2160p"),
            store.settings().maxVideoHeight) { value -> store.saveSettings(store.settings().copy(maxVideoHeight = value)) }
        choice(body, R.string.aspect_ratio, listOf("crop", "fit"), listOf(s(R.string.crop), s(R.string.fit)), store.settings().resizeMode) {
            store.saveSettings(store.settings().copy(resizeMode = it))
        }
        choice(body, R.string.transitions, listOf(false, true), listOf(s(R.string.instant), s(R.string.smooth)), store.settings().smoothTransitions) {
            store.saveSettings(store.settings().copy(smoothTransitions = it))
        }
        toggle(body, R.string.keep_awake, store.settings().keepAwake) { it.copy(keepAwake = !it.keepAwake) }
        body.addView(ui.description(s(R.string.power_notice)))
        body.addView(ui.description(s(R.string.fallback_notice)))
        val playlists = store.playlists().filter { it.family == MediaFamily.VIDEOS }
        choice(body, R.string.fallback_set, listOf<String?>(null) + playlists.map { it.id },
            listOf(s(R.string.off)) + playlists.map { it.name }, store.settings().fallbackPlaylistId) {
            store.saveSettings(store.settings().copy(fallbackPlaylistId = it))
        }
        toggle(body, R.string.stay_local, store.settings().stayLocal) { it.copy(stayLocal = !it.stayLocal) }
    }

    private fun overlays(body: LinearLayout, editor: Boolean) {
        body.addView(ui.heading(s(if (editor) R.string.overlay_editor else R.string.clock_overlays)))
        choice(body, R.string.overlay_preset, listOf(0, 1, 2), listOf(s(R.string.preset_minimal), s(R.string.preset_weather), s(R.string.preset_none)), -1) { preset ->
            store.saveSettings(store.settings().copy(showClock = preset != 2, showWeather = preset == 1, showDate = false, showLocation = false))
        }
        toggle(body, R.string.clock, store.settings().showClock) { it.copy(showClock = !it.showClock) }
        toggle(body, R.string.date, store.settings().showDate) { it.copy(showDate = !it.showDate) }
        toggle(body, R.string.weather, store.settings().showWeather) { it.copy(showWeather = !it.showWeather) }
        toggle(body, R.string.location, store.settings().showLocation) { it.copy(showLocation = !it.showLocation) }
        choice(body, R.string.clock_format, listOf("system", "12", "24"), listOf(s(R.string.system_default), "12 h", "24 h"), store.settings().clockFormat) {
            store.saveSettings(store.settings().copy(clockFormat = it))
        }
        if (editor) {
            val overlay = AmbientOverlay(host, store)
            preview = overlay
            overlay.setBackgroundColor(0xFF173044.toInt())
            store.lastItemId()?.let(store::item)?.let(overlay::updateItem)
            body.addView(overlay, LinearLayout.LayoutParams(-1, ui.dp(200)))
            overlay.start()
            choice(body, R.string.overlay_corner, listOf(0, 1, 2, 3), listOf(s(R.string.top_left), s(R.string.top_right), s(R.string.bottom_left), s(R.string.bottom_right)), store.settings().overlayCorner) {
                store.saveSettings(store.settings().copy(overlayCorner = it))
            }
            number(body, R.string.overlay_size, store.settings().overlaySize, 14..40) { settings, value -> settings.copy(overlaySize = value) }
            number(body, R.string.overlay_opacity, store.settings().overlayOpacity, 20..100) { settings, value -> settings.copy(overlayOpacity = value) }
            number(body, R.string.overlay_x, store.settings().overlayX, 0..100) { settings, value -> settings.copy(overlayX = value) }
            number(body, R.string.overlay_y, store.settings().overlayY, 0..100) { settings, value -> settings.copy(overlayY = value) }
            choice(body, R.string.overlay_color, listOf(-1, 0xFFFFE4B5.toInt(), 0xFFB5EBFF.toInt()),
                listOf(s(R.string.white), s(R.string.warm), s(R.string.cool)), store.settings().overlayColor) {
                store.saveSettings(store.settings().copy(overlayColor = it))
            }
            toggle(body, R.string.overlay_movement, store.settings().moveOverlay) { it.copy(moveOverlay = !it.moveOverlay) }
            toggle(body, R.string.overlay_hiding, store.settings().hideOverlay) { it.copy(hideOverlay = !it.hideOverlay) }
            number(body, R.string.overlay_hide_delay, store.settings().overlayHideSeconds, 10..3600) { settings, value -> settings.copy(overlayHideSeconds = value) }
            body.addView(ui.description(s(R.string.overlay_notice)))
        } else destination(body, R.string.overlay_editor, "overlay_editor")
    }

    private fun weatherSettings(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.weather)))
        body.addView(ui.description(s(R.string.weather_nws_notice)))
        choice(body, R.string.weather_context, listOf("camera", "viewer"), listOf(s(R.string.camera_location), s(R.string.viewer_location)), store.settings().weatherContext) {
            store.saveSettings(store.settings().copy(weatherContext = it))
        }
        choice(body, R.string.temperature, listOf("system", "celsius", "fahrenheit"), listOf(s(R.string.system_default), "°C", "°F"), store.settings().temperatureUnit) {
            store.saveSettings(store.settings().copy(temperatureUnit = it))
        }
        ui.button(body, s(R.string.viewer_location_edit)) { editLocation() }
        ui.button(body, s(R.string.city_search)) { citySearch() }
        ui.button(body, s(R.string.device_location)) {
            host.showDialog(AlertDialog.Builder(host).setMessage(R.string.device_location_notice).setPositiveButton(R.string.close, null).create())
        }
        ui.button(body, s(R.string.forecast)) { forecast() }
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
        val content = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
        val query = ui.field(content, s(R.string.city))
        host.showDialog(AlertDialog.Builder(host).setTitle(R.string.city_search).setView(content)
            .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.search) { _, _ ->
                weather.searchCities(query.text.toString()) { places, attribution ->
                    if (places == null) host.message(R.string.city_search_unavailable)
                    else if (places.isEmpty()) host.message(R.string.no_matches)
                    else host.showDialog(AlertDialog.Builder(host).setTitle(R.string.city_search)
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
        val target = if (settings.weatherContext == "camera") cameraLocation ?: viewerLocation else viewerLocation
        val body = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
        val status = ui.description(s(R.string.weather_loading)); body.addView(status)
        val dialog = AlertDialog.Builder(host).setTitle(R.string.forecast).setView(ui.scroll(body)).setPositiveButton(R.string.close, null).create()
        host.showDialog(dialog)
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
                forecast.days.forEach { d -> body.addView(ui.description(host.getString(R.string.weather_day,
                    d.date, temperature(d.minimumC), temperature(d.maximumC), metric(d.precipitationChancePct), metric(d.windKph),
                    metric(d.humidity)))) }
                // The owner chose an uncluttered UI. Provider identification stays in API headers and repository documentation.
            }
        }
    }

    private fun advanced(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.advanced)))
        toggle(body, R.string.diagnostics_enable, store.settings().diagnosticsEnabled) { it.copy(diagnosticsEnabled = !it.diagnosticsEnabled) }
        ui.button(body, s(R.string.report)) { report() }
        destination(body, R.string.schedule, "schedule")
        destination(body, R.string.updates, "updates")
        destination(body, R.string.privacy, "privacy")
    }

    private fun schedule(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.schedule)))
        body.addView(ui.description(s(R.string.schedule_notice)))
        toggle(body, R.string.schedule_enable, store.settings().scheduleEnabled) { it.copy(scheduleEnabled = !it.scheduleEnabled) }
        time(body, R.string.schedule_start, store.settings().scheduleStartMinute) { settings, minute -> settings.copy(scheduleStartMinute = minute) }
        time(body, R.string.schedule_end, store.settings().scheduleEndMinute) { settings, minute -> settings.copy(scheduleEndMinute = minute) }
    }

    private fun screensaver(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.screensaver)))
        body.addView(ui.description(s(R.string.screensaver_notice)))
        body.addView(ui.description(s(R.string.power_notice)))
        ui.button(body, s(R.string.open_screensaver_settings)) {
            try { host.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)) }
            catch (_: RuntimeException) { host.message(R.string.system_settings_unavailable) }
        }
        destination(body, R.string.schedule, "schedule")
    }

    private fun privacy(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.privacy)))
        body.addView(ui.description(s(R.string.privacy_body)))
        ui.button(body, s(R.string.manage_folders)) { host.open(LibraryRoute("folders")) }
        ui.button(body, s(R.string.report)) { report() }
    }

    private fun updates(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.updates)))
        body.addView(ui.description(s(R.string.update_notice)))
        body.addView(ui.description(updateState.message(host)))
        ui.button(body, s(R.string.check_updates)) { updater.discover(::onUpdate) }
        toggle(body, R.string.automatic_updates, store.settings().automaticUpdates) { it.copy(automaticUpdates = !it.automaticUpdates) }
        when (val state = updateState) {
            is UpdateState.Available -> ui.button(body, s(R.string.download_update)) { updater.download(state.release, ::onUpdate) }
            is UpdateState.Ready -> ui.button(body, s(R.string.install_update)) { updater.install(state.update, ::onUpdate) }
            UpdateState.NeedsPermission -> ui.button(body, s(R.string.allow_installs)) { launchInstallIntent(updater.unknownAppsSettingsIntent()) }
            is UpdateState.ConfirmationRequired -> ui.button(body, s(R.string.install_update)) { launchInstallIntent(state.intent) }
            else -> Unit
        }
        ui.button(body, s(R.string.cancel)) { updater.cancel() }
    }

    private fun launchInstallIntent(intent: Intent) {
        try { host.startActivity(intent) } catch (_: RuntimeException) { host.message(R.string.update_failed) }
    }
    private fun onUpdate(state: UpdateState) {
        updateState = state
        if (state == UpdateState.Failed) DiagnosticJournal.record(host, DiagnosticJournal.Event.UPDATE_ERROR)
        if (host.route.page == "updates") host.refresh()
        host.message(state.message(host))
        // This preference cannot activate the publication gate or bypass Android confirmation.
        if (store.settings().automaticUpdates) when (state) {
            is UpdateState.Available -> updater.download(state.release, ::onUpdate)
            is UpdateState.Ready -> updater.install(state.update, ::onUpdate)
            else -> Unit
        }
    }
    fun onForeground() {
        val resumed = updater.resumeInstallation(::onUpdate)
        if (!resumed && !automaticCheckDone && store.settings().automaticUpdates) { automaticCheckDone = true; updater.discover(::onUpdate) }
    }
    fun onBackground() { preview?.stop(); pairing?.close(); pairing = null; weather.cancel(); updater.pause() }
    fun close() { onBackground(); updater.close(); weather.close() }

    fun startPairing() {
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
                body.addView(ImageView(host).apply { setImageBitmap(bitmap); contentDescription = s(R.string.pairing) }, LinearLayout.LayoutParams(ui.dp(240), ui.dp(240)))
                // Do not display the bearer URL as text or put it in clipboard/logs.
            }, { request ->
                awaitingConsent = true
                host.showDialog(AlertDialog.Builder(host).setMessage(host.getString(R.string.pairing_request, request.displayName))
                    .setPositiveButton(R.string.pairing_approve) { _, _ ->
                        val approved = coordinator.approve(request.id)
                        if (approved == null) host.message(R.string.pairing_expired) else {
                            val source = approved.source
                            host.mutate { store.addItems(listOf(com.openvista.prototype.library.LibraryItem(
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

    private fun toggle(body: LinearLayout, title: Int, value: Boolean, transform: (AppSettings) -> AppSettings) {
        ui.button(body, "${s(title)}: ${s(if (value) R.string.on else R.string.off)}", "setting_$title") { host.mutate { store.saveSettings(transform(store.settings())) } }
    }
    private fun <T> choice(body: LinearLayout, title: Int, values: List<T>, labels: List<String>, selected: T, action: (T) -> Unit) {
        val index = values.indexOf(selected)
        ui.button(body, s(title) + if (index >= 0) ": ${labels[index]}" else "", "setting_$title") {
            host.showDialog(AlertDialog.Builder(host).setTitle(title).setSingleChoiceItems(labels.toTypedArray(), index) { dialog, which ->
                dialog.dismiss(); host.mutate { action(values[which]) }
            }.setNegativeButton(R.string.cancel, null).create())
        }
    }
    private fun number(body: LinearLayout, title: Int, value: Int, range: IntRange, change: (AppSettings, Int) -> AppSettings) {
        ui.button(body, "${s(title)}: $value", "setting_$title") {
            val fields = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
            val input = ui.field(fields, "${s(title)} (${range.first}–${range.last})", value.toString(), 5, InputType.TYPE_CLASS_NUMBER)
            val error = ui.description(""); fields.addView(error)
            val dialog = AlertDialog.Builder(host).setTitle(title).setView(fields).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.save, null).create()
            host.showDialog(dialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val number = input.text.toString().toIntOrNull()
                if (number == null || number !in range) error.text = "${range.first}–${range.last}"
                else { dialog.dismiss(); host.mutate { store.saveSettings(change(store.settings(), number)) } }
            }
        }
    }
    private fun time(body: LinearLayout, title: Int, value: Int, change: (AppSettings, Int) -> AppSettings) {
        ui.button(body, "${s(title)}: %02d:%02d".format(value / 60, value % 60), "setting_$title") {
            val fields = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
            val input = ui.field(fields, s(title), "%02d:%02d".format(value / 60, value % 60), 5)
            val error = ui.description(""); fields.addView(error)
            val dialog = AlertDialog.Builder(host).setTitle(title).setView(fields).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.save, null).create()
            host.showDialog(dialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pieces = input.text.toString().split(':')
                val hour = pieces.getOrNull(0)?.toIntOrNull(); val minute = pieces.getOrNull(1)?.toIntOrNull()
                if (pieces.size != 2 || hour == null || minute == null || hour !in 0..23 || minute !in 0..59) error.text = s(R.string.invalid_time)
                else { dialog.dismiss(); host.mutate { store.saveSettings(change(store.settings(), hour * 60 + minute)) } }
            }
        }
    }
    private fun destination(body: LinearLayout, label: Int, page: String) { ui.button(body, s(label), "destination_$page") { host.open(LibraryRoute(page)) } }
    private fun temperature(value: Double?): String {
        if (value == null) return s(R.string.not_available)
        val fahrenheit = store.settings().temperatureUnit == "fahrenheit" ||
            (store.settings().temperatureUnit == "system" && host.applicationContext.resources.configuration.locales[0].country == "US")
        return "%.0f°%s".format(if (fahrenheit) value * 9 / 5 + 32 else value, if (fahrenheit) "F" else "C")
    }
    private fun metric(value: Double?) = value?.let { "%.1f".format(it) } ?: s(R.string.not_available)
}
