package com.openvista.prototype.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.media3.common.util.UnstableApi
import com.openvista.prototype.LibraryRoute
import com.openvista.prototype.OpenVistaActivity
import com.openvista.prototype.R
import com.openvista.prototype.ambient.SourceMapView
import com.openvista.prototype.library.Availability
import com.openvista.prototype.library.LibraryItem
import com.openvista.prototype.library.MediaFamily
import com.openvista.prototype.library.SavedPlaylist
import com.openvista.sources.StreamKind
import com.openvista.sources.UserSourceParser
import java.util.TimeZone
import java.util.UUID

@UnstableApi
class LibraryScreens(private val host: OpenVistaActivity) {
    private val ui get() = host.ui
    private val store get() = host.store
    private fun s(id: Int) = host.getString(id)
    private var search = ""
    private var country = ""
    private var city = ""
    private var category = ""
    private var favoriteOnly = false
    private var availableOnly = false

    fun saveState(state: Bundle) {
        state.putString("filterSearch", search); state.putString("filterCountry", country)
        state.putString("filterCity", city); state.putString("filterCategory", category)
        state.putBoolean("filterFavorite", favoriteOnly); state.putBoolean("filterAvailable", availableOnly)
    }

    fun restoreState(state: Bundle?) {
        if (state == null) return
        search = state.getString("filterSearch").orEmpty().take(100)
        country = state.getString("filterCountry").orEmpty().take(100)
        city = state.getString("filterCity").orEmpty().take(100)
        category = state.getString("filterCategory").orEmpty().take(100)
        favoriteOnly = state.getBoolean("filterFavorite"); availableOnly = state.getBoolean("filterAvailable")
    }

    fun onboarding(body: LinearLayout, revisit: Boolean) {
        body.addView(ui.heading(s(R.string.onboarding_title)))
        body.addView(ui.description(s(R.string.onboarding_body)))
        if (!revisit) {
            ui.button(body, s(R.string.get_started), "get_started") {
                host.mutate { store.saveSettings(store.settings().copy(onboardingComplete = true)) }
                host.open(LibraryRoute("setup"))
            }
            ui.button(body, s(R.string.skip), "skip") {
                host.mutate { store.saveSettings(store.settings().copy(onboardingComplete = true)) }
                host.open(LibraryRoute("home"))
            }
        } else {
            body.addView(ui.heading(s(R.string.setup_sources)))
            sourceActions(body)
            body.addView(ui.heading(s(R.string.setup_weather)))
            ui.button(body, s(R.string.weather_context)) { host.open(LibraryRoute("weather_settings")) }
            ui.button(body, s(R.string.setup_done)) { host.open(LibraryRoute("home")) }
        }
    }

    fun library(body: LinearLayout, route: LibraryRoute) {
        val favorites = route.page == "favorites"
        val family = if (route.page == "videos") MediaFamily.VIDEOS else MediaFamily.CAMERAS
        body.addView(ui.heading(s(if (favorites) R.string.favorites else if (family == MediaFamily.CAMERAS) R.string.cameras else R.string.my_videos)))
        if (favorites) {
            var hasNext = false
            val offset = route.offset
            listOf(MediaFamily.CAMERAS, MediaFamily.VIDEOS).forEach { section ->
                body.addView(ui.caps(s(if (section == MediaFamily.CAMERAS) R.string.cameras else R.string.my_videos)).apply {
                    setPadding(ui.dp(2), ui.dp(12), ui.dp(2), ui.dp(5))
                })
                val items = store.items().filter { it.family == section && it.favorite }
                if (items.isEmpty()) body.addView(ui.description(s(R.string.favorite_empty)))
                else items.drop(offset).take(PAGE_SIZE).forEach { item -> sourceRow(body, item) }
                hasNext = hasNext || items.size > offset + PAGE_SIZE
            }
            if (offset > 0) ui.button(body, s(R.string.previous_page)) { host.open(route.copy(offset = (offset - PAGE_SIZE).coerceAtLeast(0))) }
            if (hasNext) ui.button(body, s(R.string.next_page)) { host.open(route.copy(offset = offset + PAGE_SIZE)) }
            return
        }
        if (family == MediaFamily.CAMERAS) sourceActions(body) else {
            val actions = ui.row()
            ui.button(actions, s(R.string.choose_folder)) { host.pickFolder() }
            ui.button(actions, s(R.string.choose_video)) { host.pickVideo() }
            ui.button(actions, s(R.string.manage_folders)) { host.open(LibraryRoute("folders")) }
            body.addView(actions)
        }
        val all = store.items().filter { it.family == family }
        if (all.isEmpty()) {
            body.addView(ui.description(s(if (family == MediaFamily.CAMERAS) R.string.camera_empty else R.string.video_empty)))
            return
        }
        val controls = ui.row()
        ui.button(controls, s(R.string.play_all)) { host.play(all, shuffle = family == MediaFamily.CAMERAS) }
        ui.button(controls, s(R.string.playlists)) { host.open(LibraryRoute("playlists", family = family)) }
        if (family == MediaFamily.CAMERAS) {
            ui.button(controls, s(R.string.filter)) { filters() }
            ui.button(controls, s(R.string.map)) { host.open(LibraryRoute("map")) }
        }
        body.addView(controls)
        val filtered = if (family == MediaFamily.CAMERAS) all.filter { item ->
            val values = listOf(item.title, item.country, item.city, item.category).joinToString(" ")
            values.contains(search, true) && item.country.contains(country, true) && item.city.contains(city, true) &&
                item.category.contains(category, true) && (!favoriteOnly || item.favorite) &&
                (!availableOnly || availability(item) == Availability.AVAILABLE)
        } else all
        if (filtered.isEmpty()) body.addView(ui.description(s(R.string.no_matches)))
        val offset = route.offset.coerceAtMost(((filtered.size - 1).coerceAtLeast(0) / PAGE_SIZE) * PAGE_SIZE)
        filtered.drop(offset).take(PAGE_SIZE).forEach { item -> sourceRow(body, item) }
        val pages = ui.row()
        if (offset > 0) ui.button(pages, s(R.string.previous_page)) { host.open(route.copy(offset = offset - PAGE_SIZE)) }
        if (offset + PAGE_SIZE < filtered.size) ui.button(pages, s(R.string.next_page)) { host.open(route.copy(offset = offset + PAGE_SIZE)) }
        body.addView(pages)
    }

    /** One library entry in the shared row style; favorites carry the star icon instead of a text mark. */
    private fun sourceRow(body: LinearLayout, item: LibraryItem) {
        val place = listOf(item.city, item.country, item.category).filter(String::isNotBlank).joinToString(" · ")
        val icon = when {
            item.favorite -> TvIcon.Symbol.STAR
            item.family == MediaFamily.CAMERAS -> TvIcon.Symbol.CAMERA
            else -> TvIcon.Symbol.FILM
        }
        ui.settingRow(body, item.title, key = "source_${item.id}", summary = place.ifBlank { null }, icon = icon) {
            host.open(LibraryRoute("details", item.id, item.family))
        }
    }

    private fun sourceActions(body: LinearLayout) {
        val actions = ui.row()
        ui.button(actions, s(R.string.add_stream), "add_stream") { editSource() }
        ui.button(actions, s(R.string.import_list), "import_list") { host.pickList() }
        ui.button(actions, s(R.string.play_my_videos), "play_my_videos") { host.open(LibraryRoute("videos")) }
        body.addView(actions)
    }

    fun details(body: LinearLayout, id: String) {
        val item = store.item(id) ?: run { body.addView(ui.description(s(R.string.no_playable_items))); return }
        body.addView(ui.heading(item.title))
        body.addView(ui.description(listOf(item.city, item.country, item.category).filter(String::isNotBlank).joinToString(" · ")))
        val observed = availability(item)
        body.addView(ui.description(host.getString(R.string.source_status, s(when (observed) {
            Availability.AVAILABLE -> if (item.family == MediaFamily.VIDEOS) R.string.access_observed else R.string.available
            Availability.UNAVAILABLE -> R.string.unavailable; else -> R.string.unknown
        }))))
        if (item.attribution.isNotBlank()) body.addView(ui.description(item.attribution))
        if (item.timeZone.isNotBlank()) {
            val time = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm z", host.resources.configuration.locales[0])
                .apply { timeZone = TimeZone.getTimeZone(item.timeZone) }.format(java.util.Date())
            body.addView(ui.description(host.getString(R.string.camera_local_time, time)))
        }
        val actions = ui.row()
        ui.button(actions, s(R.string.play), "play_${item.id}") {
            host.play(store.items().filter { it.family == item.family }, item.id, item.family == MediaFamily.CAMERAS)
        }
        ui.button(actions, s(if (item.favorite) R.string.favorite_remove else R.string.favorite_add)) {
            host.mutate { store.upsert(item.copy(favorite = !item.favorite)) }
        }
        ui.button(actions, s(R.string.edit)) { editSource(item) }
        body.addView(actions)
        ui.button(body, s(R.string.playlists)) { choosePlaylist(item) }
        val organize = ui.row()
        ui.button(organize, s(R.string.move_up)) { host.mutate { store.moveItem(item.id, -1) } }
        ui.button(organize, s(R.string.move_down)) { host.mutate { store.moveItem(item.id, 1) } }
        body.addView(organize)
        if (item.family == MediaFamily.CAMERAS) ui.button(body, s(R.string.forecast)) { host.openForecast(item) }
        ui.button(body, s(R.string.remove)) {
            host.confirm(s(R.string.confirm_remove)) { host.removeItem(item.id) }
        }
    }

    fun editSource(item: LibraryItem? = null) {
        val fields = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
        val title = ui.field(fields, s(R.string.name), item?.title.orEmpty())
        val camera = item?.family != MediaFamily.VIDEOS
        val url = if (camera) ui.field(fields, s(R.string.stream_url), item?.uri.orEmpty(), 4096, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI) else null
        if (camera) fields.addView(ui.description(s(R.string.sensitive_url_notice)))
        var kind = if (item?.streamKind == "DASH") StreamKind.DASH else StreamKind.HLS
        if (camera) {
            val selectedKind = ui.description(kind.name)
            fields.addView(selectedKind)
            val row = ui.row()
            ui.button(row, "HLS") { kind = StreamKind.HLS; selectedKind.text = kind.name }
            ui.button(row, "DASH") { kind = StreamKind.DASH; selectedKind.text = kind.name }
            fields.addView(row)
        }
        val category = ui.field(fields, s(R.string.category), item?.category.orEmpty())
        val country = ui.field(fields, s(R.string.country), item?.country.orEmpty())
        val city = ui.field(fields, s(R.string.city), item?.city.orEmpty())
        val latitude = ui.field(fields, s(R.string.latitude), item?.latitude?.toString().orEmpty(), 16)
        val longitude = ui.field(fields, s(R.string.longitude), item?.longitude?.toString().orEmpty(), 16)
        val zone = ui.field(fields, s(R.string.time_zone), item?.timeZone.orEmpty())
        val credit = ui.field(fields, s(R.string.attribution), item?.attribution.orEmpty(), 500)
        val error = ui.description(""); fields.addView(error)
        val dialog = AlertDialog.Builder(host).setTitle(if (item == null) R.string.add_stream else R.string.edit)
            .setView(ui.scroll(fields)).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.save, null).create()
        host.showDialog(dialog)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = title.text.toString().trim()
            val source = if (camera) UserSourceParser().manualUrl(url!!.text.toString(), kind, name) else null
            if (name.isBlank()) { error.text = s(R.string.invalid_name); return@setOnClickListener }
            if (camera && source == null) { error.text = s(R.string.invalid_source); return@setOnClickListener }
            val latText = latitude.text.toString().trim(); val lonText = longitude.text.toString().trim()
            val lat = latText.toDoubleOrNull(); val lon = lonText.toDoubleOrNull()
            val tz = zone.text.toString().trim()
            if (((latText.isNotEmpty() || lonText.isNotEmpty()) &&
                    (lat == null || lon == null || !lat.isFinite() || !lon.isFinite() || lat !in -90.0..90.0 || lon !in -180.0..180.0)) ||
                (tz.isNotBlank() && tz !in TimeZone.getAvailableIDs())) {
                error.text = s(R.string.invalid_location); return@setOnClickListener
            }
            val existing = store.items().firstOrNull { camera && it.family == MediaFamily.CAMERAS && it.uri == source!!.url }
            if (existing != null && existing.id != item?.id) {
                error.text = s(R.string.source_already_saved); return@setOnClickListener
            }
            val base = item ?: existing ?: LibraryItem(UUID.randomUUID().toString(), name, source!!.url, MediaFamily.CAMERAS, source.kind.name)
            try {
                store.upsert(base.copy(title = name, uri = source?.url ?: base.uri, streamKind = source?.kind?.name ?: base.streamKind,
                    category = category.text.toString(), country = country.text.toString(), city = city.text.toString(),
                    latitude = lat, longitude = lon, timeZone = tz, attribution = credit.text.toString(),
                    availability = if (source != null && source.url != base.uri) Availability.UNKNOWN else base.availability,
                    observedAt = if (source != null && source.url != base.uri) 0 else base.observedAt))
                dialog.dismiss(); host.refresh(); host.persisted(s(R.string.source_saved))
            } catch (_: Exception) { error.text = s(R.string.operation_failed) }
        }
    }

    private fun filters() {
        val fields = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
        val name = ui.field(fields, s(R.string.search), search)
        val countryField = ui.field(fields, s(R.string.country), country)
        val cityField = ui.field(fields, s(R.string.city), city)
        val categoryField = ui.field(fields, s(R.string.category), category)
        var favorites = favoriteOnly; var recent = availableOnly
        lateinit var favorite: android.widget.Button
        favorite = ui.button(fields, "${s(R.string.favorites_only)}: ${s(if (favorites) R.string.on else R.string.off)}") {
            favorites = !favorites; favorite.text = "${s(R.string.favorites_only)}: ${s(if (favorites) R.string.on else R.string.off)}"
        }
        lateinit var available: android.widget.Button
        available = ui.button(fields, "${s(R.string.available_only)}: ${s(if (recent) R.string.on else R.string.off)}") {
            recent = !recent; available.text = "${s(R.string.available_only)}: ${s(if (recent) R.string.on else R.string.off)}"
        }
        host.showDialog(AlertDialog.Builder(host).setTitle(R.string.filter).setView(ui.scroll(fields))
            .setNeutralButton(R.string.clear_filter) { _, _ -> search = ""; country = ""; city = ""; category = ""; favoriteOnly = false; availableOnly = false; host.refresh() }
            .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.save) { _, _ ->
                search = name.text.toString(); country = countryField.text.toString(); city = cityField.text.toString()
                category = categoryField.text.toString(); favoriteOnly = favorites; availableOnly = recent; host.refresh()
            }.create())
    }

    fun playlists(body: LinearLayout, family: MediaFamily) {
        body.addView(ui.heading(s(R.string.playlists)))
        ui.button(body, s(R.string.new_playlist)) { nameDialog(s(R.string.new_playlist)) { name ->
            host.mutate { store.savePlaylist(SavedPlaylist(UUID.randomUUID().toString(), name, family)) }
        } }
        store.playlists().filter { it.family == family }.forEach { playlist ->
            ui.button(body, playlist.name, "playlist_${playlist.id}") { host.open(LibraryRoute("playlist", playlist.id, family)) }
        }
    }

    fun playlist(body: LinearLayout, id: String) {
        val playlist = store.playlists().firstOrNull { it.id == id } ?: run { body.addView(ui.description(s(R.string.empty_playlist))); return }
        body.addView(ui.heading(playlist.name))
        val items = playlist.itemIds.mapNotNull(store::item)
        val actions = ui.row()
        ui.button(actions, s(R.string.play)) { host.play(items, shuffle = playlist.shuffle) }
        ui.button(actions, s(R.string.rename)) { nameDialog(s(R.string.rename), playlist.name) { name -> host.mutate { store.savePlaylist(playlist.copy(name = name)) } } }
        ui.button(actions, s(R.string.duplicate)) { host.mutate { store.savePlaylist(playlist.copy(id = UUID.randomUUID().toString(), name = host.getString(R.string.playlist_copy, playlist.name).take(100))) } }
        body.addView(actions)
        val organize = ui.row()
        ui.button(organize, s(R.string.move_up)) { host.mutate { store.movePlaylist(id, -1) } }
        ui.button(organize, s(R.string.move_down)) { host.mutate { store.movePlaylist(id, 1) } }
        ui.button(organize, s(if (playlist.shuffle) R.string.shuffle else R.string.sequence)) { host.mutate { store.savePlaylist(playlist.copy(shuffle = !playlist.shuffle)) } }
        body.addView(organize)
        ui.button(body, s(R.string.batch_add)) { batchAdd(playlist) }
        if (playlist.family == MediaFamily.VIDEOS) ui.button(body, s(R.string.fallback_set)) {
            host.mutate { store.saveSettings(store.settings().copy(fallbackPlaylistId = id)) }
        }
        ui.button(body, s(R.string.delete)) { host.confirm(s(R.string.confirm_delete_playlist)) {
            host.mutate { store.deletePlaylist(id) }; host.open(LibraryRoute("playlists", family = playlist.family))
        } }
        body.addView(ui.heading(s(R.string.playlist_items)))
        if (items.isEmpty()) body.addView(ui.description(s(R.string.empty_playlist)))
        val offset = host.route.offset.coerceAtMost(((items.size - 1).coerceAtLeast(0) / PAGE_SIZE) * PAGE_SIZE)
        items.drop(offset).take(PAGE_SIZE).forEach { item -> ui.button(body, item.title, "playlist_item_${item.id}") { manageItem(playlist, item.id) } }
        if (offset > 0) ui.button(body, s(R.string.previous_page)) { host.open(host.route.copy(offset = offset - PAGE_SIZE)) }
        if (offset + PAGE_SIZE < items.size) ui.button(body, s(R.string.next_page)) { host.open(host.route.copy(offset = offset + PAGE_SIZE)) }
    }

    private fun batchAdd(playlist: SavedPlaylist) {
        val candidates = store.items().filter { it.family == playlist.family && it.id !in playlist.itemIds }
        if (candidates.isEmpty()) { host.message(R.string.no_matches); return }
        val checked = BooleanArray(candidates.size)
        host.showDialog(AlertDialog.Builder(host).setTitle(R.string.batch_add)
            .setMultiChoiceItems(candidates.map { it.title }.toTypedArray(), checked) { _, which, selected -> checked[which] = selected }
            .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.save) { _, _ -> host.mutate {
                store.savePlaylist(playlist.copy(itemIds = (playlist.itemIds + candidates.filterIndexed { index, _ -> checked[index] }.map { it.id }).distinct()))
            } }.create())
    }

    private fun manageItem(playlist: SavedPlaylist, id: String) {
        host.showDialog(AlertDialog.Builder(host).setTitle(store.item(id)?.title)
            .setItems(arrayOf(s(R.string.play), s(R.string.move_up), s(R.string.move_down), s(R.string.remove))) { _, which ->
                if (which == 0) host.play(playlist.itemIds.mapNotNull(store::item), id, playlist.shuffle)
                else host.mutate {
                    val ids = playlist.itemIds.toMutableList(); val index = ids.indexOf(id)
                    if (which == 3) ids.remove(id) else {
                        val target = index + if (which == 1) -1 else 1
                        if (index >= 0 && target in ids.indices) { ids.removeAt(index); ids.add(target, id) }
                    }
                    store.savePlaylist(playlist.copy(itemIds = ids))
                }
            }.setNegativeButton(R.string.cancel, null).create())
    }

    private fun choosePlaylist(item: LibraryItem) {
        val playlists = store.playlists().filter { it.family == item.family }
        host.showDialog(AlertDialog.Builder(host).setTitle(R.string.playlists)
            .setItems((playlists.map { it.name } + s(R.string.new_playlist)).toTypedArray()) { _, index ->
                if (index == playlists.size) nameDialog(s(R.string.new_playlist)) { name -> host.mutate {
                    store.savePlaylist(SavedPlaylist(UUID.randomUUID().toString(), name, item.family, listOf(item.id)))
                } } else host.mutate { store.savePlaylist(playlists[index].copy(itemIds = (playlists[index].itemIds + item.id).distinct())) }
            }.setNegativeButton(R.string.cancel, null).create())
    }

    private fun nameDialog(title: String, initial: String = "", save: (String) -> Unit) {
        val content = ui.column().apply { setPadding(ui.dp(20), ui.dp(10), ui.dp(20), ui.dp(10)) }
        val input = ui.field(content, s(R.string.name), initial)
        val error = ui.description(""); content.addView(error)
        val dialog = AlertDialog.Builder(host).setTitle(title).setView(content).setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null).create()
        host.showDialog(dialog)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val value = input.text.toString().trim()
            if (value.isEmpty()) error.text = s(R.string.invalid_name) else { dialog.dismiss(); save(value) }
        }
    }

    fun folders(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.manage_folders)))
        ui.button(body, s(R.string.choose_folder)) { host.pickFolder() }
        ui.button(body, s(R.string.choose_video)) { host.pickVideo() }
        ui.button(body, s(R.string.rescan)) { host.rescan() }
        val folders = store.folders()
        if (folders.isEmpty()) body.addView(ui.description(s(R.string.no_folders)))
        folders.forEach { folder -> ui.button(body, "${folder.title} · ${s(R.string.folder_forget)}", "folder_${folder.uri.hashCode()}") {
            host.confirm(s(R.string.folder_forget_notice)) { host.forgetFolder(folder.uri) }
        } }
    }

    fun map(body: LinearLayout) {
        body.addView(ui.heading(s(R.string.map)))
        val notice = ui.description(s(R.string.map_notice)); body.addView(notice)
        val items = store.items().filter { it.family == MediaFamily.CAMERAS }
        val located = items.filter { it.latitude != null && it.longitude != null }
        if (located.isEmpty()) body.addView(ui.description(s(R.string.map_empty)))
        val map = SourceMapView(host, located, onStatus = { state ->
            notice.text = s(when (state) {
                com.openvista.prototype.ambient.MapBackdropState.Loading -> R.string.map_loading
                is com.openvista.prototype.ambient.MapBackdropState.Ready -> if (state.stale) R.string.map_saved else R.string.map_ready
                else -> R.string.map_notice
            })
        }) { item -> host.open(LibraryRoute("details", item.id)) }
        val controls = ui.row()
        ui.button(controls, s(R.string.zoom_in)) { map.zoom(1) }
        ui.button(controls, s(R.string.zoom_out)) { map.zoom(-1) }
        ui.button(controls, s(R.string.reset_view)) { map.reset() }
        body.addView(controls)
        body.addView(ui.description(s(R.string.map_pan_help)))
        val pan = ui.row()
        ui.button(pan, s(R.string.map_west)) { map.pan(-1, 0) }
        ui.button(pan, s(R.string.map_north)) { map.pan(0, 1) }
        ui.button(pan, s(R.string.map_south)) { map.pan(0, -1) }
        ui.button(pan, s(R.string.map_east)) { map.pan(1, 0) }
        body.addView(pan)
        val panels = ui.row()
        panels.addView(map, LinearLayout.LayoutParams(0, ui.dp(300), 1.4f))
        val list = ui.column()
        val offset = host.route.offset.coerceAtMost(((items.size - 1).coerceAtLeast(0) / PAGE_SIZE) * PAGE_SIZE)
        items.drop(offset).take(PAGE_SIZE).forEach { sourceRow(list, it) }
        panels.addView(ui.scroll(list), LinearLayout.LayoutParams(0, ui.dp(300), 1f))
        body.addView(panels)
        if (offset > 0) ui.button(body, s(R.string.previous_page)) { host.open(host.route.copy(offset = offset - PAGE_SIZE)) }
        if (offset + PAGE_SIZE < items.size) ui.button(body, s(R.string.next_page)) { host.open(host.route.copy(offset = offset + PAGE_SIZE)) }
    }

    private fun availability(item: LibraryItem): Availability = if (System.currentTimeMillis() - item.observedAt in 0..900_000)
        item.availability else Availability.UNKNOWN

    companion object { private const val PAGE_SIZE = 30 }
}
