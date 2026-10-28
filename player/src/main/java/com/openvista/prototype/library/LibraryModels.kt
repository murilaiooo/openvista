package com.openvista.prototype.library

enum class MediaFamily { CAMERAS, VIDEOS }
enum class Availability { UNKNOWN, AVAILABLE, UNAVAILABLE }

/** One picker-granted route to a document; a tree and a direct grant remain independent. */
data class VideoAccessRoute(
    val uri: String,
    val folderUri: String? = null,
    val availability: Availability = Availability.UNKNOWN,
    val observedAt: Long = 0,
)

data class LibraryItem(
    val id: String,
    val title: String,
    val uri: String,
    val family: MediaFamily,
    val streamKind: String? = null,
    val folderUri: String? = null,
    val category: String = "",
    val country: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timeZone: String = "",
    val attribution: String = "",
    val favorite: Boolean = false,
    val availability: Availability = Availability.UNKNOWN,
    val observedAt: Long = 0,
    val accessRoutes: List<VideoAccessRoute> = emptyList(),
    val formerIds: List<String> = emptyList(),
)

data class SavedPlaylist(
    val id: String,
    val name: String,
    val family: MediaFamily,
    val itemIds: List<String> = emptyList(),
    val shuffle: Boolean = family == MediaFamily.CAMERAS,
)

data class FolderGrant(val uri: String, val title: String, val persisted: Boolean)
data class PlaybackCheckpoint(val itemId: String, val positionMs: Long, val durationMs: Long)

data class AppSettings(
    val language: String = "system",
    val muted: Boolean = true,
    val rotationMinutes: Int = 5,
    val maxVideoHeight: Int = 0,
    val resizeMode: String = "crop",
    val smoothTransitions: Boolean = false,
    val keepAwake: Boolean = false,
    val fallbackPlaylistId: String? = null,
    val stayLocal: Boolean = false,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val showWeather: Boolean = true,
    val showLocation: Boolean = false,
    val clockFormat: String = "system",
    val temperatureUnit: String = "system",
    val overlayCorner: Int = 0,
    val overlaySize: Int = 20,
    val overlayOpacity: Int = 85,
    val overlayColor: Int = -1,
    val overlayX: Int = 0,
    val overlayY: Int = 0,
    val moveOverlay: Boolean = true,
    val hideOverlay: Boolean = false,
    val overlayHideSeconds: Int = 60,
    val weatherContext: String = "camera",
    val viewerCity: String = "",
    val viewerLatitude: Double? = null,
    val viewerLongitude: Double? = null,
    val viewerTimeZone: String = "",
    val scheduleEnabled: Boolean = false,
    val scheduleStartMinute: Int = 0,
    val scheduleEndMinute: Int = 0,
    val onboardingComplete: Boolean = false,
    val diagnosticsEnabled: Boolean = false,
    val automaticUpdates: Boolean = false,
    val overlayLayout: String = "cinematic",
    val showBrand: Boolean = true,
    val showSourceInfo: Boolean = true,
    val showViewerInfo: Boolean = true,
    val showForecast: Boolean = true,
    val viewerForecastDays: Int = 5,
    val sourceOpacity: Int = 100,
    val viewerOpacity: Int = 100,
    val forecastOpacity: Int = 100,
    val brandOpacity: Int = 70,
    val overlayMoveMinutes: Int = 5,
)
