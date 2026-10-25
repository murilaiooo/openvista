# OpenVista glossary

Use these terms consistently in product text, code, and issues.

| Term | Definition |
| --- | --- |
| Ambient playback | Full-screen, low-interaction playback optimized for unattended TV viewing. |
| Live camera | A camera broadcasting a real-world place through a permitted stream or provider integration. It is **not** a wallpaper file. |
| Live wallpaper | A looping scenic video file or licensed downloadable visual, generally not a real-time camera. |
| My videos | Media files the user owns or is permitted to play from a folder selected on their TV or attached storage. |
| Scene | The currently visible camera feed or video, plus optional location and weather metadata. |
| Collection | A user's group of scenes organized for browsing by place, theme or playlist. |
| Playlist | A user-defined, named sequence of scenes; camera playlists and local-video playlists are separate media types. |
| Custom source | A live-camera stream URL added by a user rather than supplied by OpenVista. |
| Weather context | The location whose forecast is displayed: the viewed camera location or the viewer's chosen location. |
| Camera map | An interactive world map for selecting available live cameras by geographic location. |
| Scene rotation | Automatic switching to another available scene after a user-configured interval from 1 to 60 minutes. |
| Quality cap | The viewer-selected maximum stream quality, applied where the live provider exposes compatible alternative renditions. |
| Overlay preset | A saved arrangement of on-screen information components that can be further adjusted in a visual editor. |
| Contextual remote controls | Directional TV remote behavior that changes with the current media mode: camera change for webcam browsing and seeking for locally stored video. |
| Advanced overlay editor | Interactive TV-focused customization of overlay elements beyond quick presets; exact controls remain open. |
| Scene transition | Visual change between successive cameras or local videos, user-selectable as instant or smooth, subject to available device resources. |
| Display-care controls | Independent settings to reposition static informational overlays and hide them after inactivity, reducing but not eliminating image-retention risk. |
| Idle activation interval | Time of TV inactivity before its OS starts a configured screensaver; OpenVista offers only intervals supported by that device/OS. |
| Keep-awake preference | User-requested playback/display continuity that cannot override TV sleep, screen-off or OEM restrictions. |
| Last scene | The most recently selected camera or local video, used as the preferred source when automatic screensaver playback starts again. |
| Connection recovery check | Bounded verification that network access has returned while a configured offline fallback continues playing undisturbed. |
| Optional runtime schedule | User-configured windows in which automatic screensaver behavior is eligible; exact schedule interaction rules remain open. |
| Recursive media discovery | Scanning an accessible user-selected media folder and all of its accessible nested subfolders for candidate video files. |
| Local-video resume position | The most recently saved timestamp used to restart a previously viewed local video where it was left. |
| Seek step | Fixed ten-second forward or backward movement within a seekable local video. |
| Playlist repeat | Playback advances through videos and starts again from the first eligible item when the playlist ends. |
| Unplayable media | A video item the current TV cannot load or decode, or whose bytes are invalid/inaccessible. |
| Basic settings | Categorized, plain-language controls for common viewing preferences. |
| Advanced settings | A separate area for optional technical controls and diagnostics. |
| Player controls | Temporary contextual controls shown over fullscreen playback, automatically hidden five seconds after user inactivity. |
| Playlist item | An individual camera or local video stored as an ordered entry in its matching playlist type. |
| Favorites hub | One screen with separate groups for favorited cameras and local videos. |
| Local fallback return | After internet recovers, a configured local fallback finishes its active video before live cameras resume, unless the user chooses to stay local. |
| Local engineering APK | Development build installed locally for technical tests; not a public GitHub release or stable v1. |
| Public v1 acceptance gate | Evidence checklist required before stable release, including user-source handling, zero bundled media, 24-hour stability, privacy and compatibility tests. |
| APK updater | Component that checks, downloads and requests install of authentic application updates; unattended installation is conditional on Android installer permissions and ownership. |
| Local QR pairing | Short-lived, explicitly approved transfer from phone to TV on the same LAN/Wi-Fi for a permitted stream URL. |
| Sanitized report | On-device diagnostic summary with sensitive identifiers removed, copied/shared only by user request. |
| Overlay | Nonessential on-screen data above video, such as time, place, weather, or forecast. |
| Playback mode | User-selected content type: live cameras or local videos. |
| Dream mode | Optional Android system screensaver service, where permitted by the OEM/OS. |
| App mode | Regular launcher-opened TV activity, available independently of system screensaver permission. |
| D-pad | Directional remote navigation with arrows, Select/OK, Back, and Home. |
| Source health | Whether a configured stream is valid, reachable, and decodable; must distinguish available, unavailable, and not-yet-verified. |
| Offline source list | Locally saved user-source metadata available without a network connection; it does not make remote camera streams playable offline. |
| Source-list import | A user-directed operation that validates and stores supplied camera/stream entries locally; OpenVista does not publish a community directory. |
| QR-assisted source entry | Optional workflow for entering a custom URL via another device, with pairing/security method still to be decided. |
| Local video fallback | An optional user-selected video playlist played when all candidate webcams are unavailable; otherwise show a retry notice. |
| Provider | Third party supplying live video, forecast data, or media metadata under its own terms. |
| Source library | The user's locally managed camera sources and imported lists, with optional category and location metadata; the app provides no built-in camera catalog. |
| Decision status | Confirmed, Proposed, Open, Rejected, or Deferred. |
