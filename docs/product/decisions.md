# OpenVista product decisions

**Status:** Discovery. Rounds 1–4 confirmed on 2026-10-24 (38 decisions). Round 5 open.

Only owner-confirmed choices are requirements. Open questions, design recommendations and proposed technical solutions are **not** approved. Product agreement is not proof of compatibility or implementation.

## Product baseline

OpenVista is a free, ad-free, account-free ambient Android TV application and, where the OS allows, an automatic system screensaver. It shows public, permitted live cameras or looping user-owned videos. It includes configurable clock and weather overlays (current conditions and seven-day forecast), with weather for the camera location or the user's chosen location. First distribution is by GitHub APK; no APK exists yet.

## Round 1: confirmed (D-01 to D-08)

| ID | Requirement | Confirmed choice / boundary |
| --- | --- | --- |
| D-01 | Entry points | Both automatic screensaver and manually launched app. Automatic mode subject to device/OS permission. |
| D-02 | Live sources | Both curated/browsable cameras and user-entered permitted stream URLs. |
| D-03 | Local media | Select folder(s), play in sequence, shuffle and playlists. Details refined in D-13/D-14. |
| D-04 | Weather location | Option to switch between camera location and viewer location; no mandatory personal location permission. |
| D-05 | Overlays | Fully customizable. Presets/editor refined in D-15/D-24. |
| D-06 | Audio | Muted initially; allow the user to enable sound. |
| D-07 | Commercial model | 100% free, no advertisements or user account. Provider licenses must allow the app's use. |
| D-08 | Initial delivery | APK via GitHub. Store distribution is not committed. |

## Round 2: confirmed (D-09 to D-18)

| ID | Requirement | Confirmed choice / boundary |
| --- | --- | --- |
| D-09 | Home | Three large destinations: Cameras, My Videos and Favorites. |
| D-10 | Camera discovery | Both organized catalog and interactive map. |
| D-11 | Rotation | Automatic scene change at a configurable interval of 1–60 minutes. |
| D-12 | Camera failure | Skip immediately to the next camera; prevent an endless cycle when every camera is unavailable. |
| D-13 | Local storage | Internal TV storage and USB flash/external HDD; device/system picker support to validate. |
| D-14 | Playlists | Multiple playlists, with cameras and local videos in separate playlists. |
| D-15 | Overlay customization | Both presets and a full visual editor. |
| D-16 | Video fit | Fill the screen, crop excess rather than distort. |
| D-17 | Quality | Automatic playback quality, configurable maximum where supported by source variants and decoder. |
| D-18 | Localization | Brazilian Portuguese and English initially. |

## Round 3: confirmed (D-19 to D-28)

| ID | Requirement | Confirmed choice / boundary |
| --- | --- | --- |
| D-19 | Visual style | Scenic footage fills the screen; interface is nearly invisible during ambient playback. Avoid permanent chrome. |
| D-20 | Home appearance | Three **large image-backed cards** for Cameras, My Videos and Favorites. |
| D-21 | Remote controls | **Contextual by media type**: camera mode uses directional navigation for changing cameras; local video mode allows seeking backward/forward. Exact D-pad/OK/Back/key conflicts remain to specify. |
| D-22 | World map | **2D map with clustered markers and a lateral list**. Must be navigable by a TV remote; engine, dataset and rendering unchosen. |
| D-23 | Default clock/weather | **Discreet clock and current conditions**; detailed seven-day forecast opened on demand. All overridable using D-15/D-24. |
| D-24 | Widget editor | **Both simple and advanced editing**. What gestures, fields and positioning controls are available remains open. |
| D-25 | Static-content protection | **Both periodic overlay repositioning and inactivity hiding**, independently configurable. These reduce risk but cannot guarantee absence of burn-in. |
| D-26 | First-run experience | **Short optional tutorial** to choose cameras, video folder and weather context; users can skip it. |
| D-27 | Transitions | **Configurable instant or smooth scene changes**, choosing practical behavior based on hardware performance. Never promise seamless crossfades on every device. |
| D-28 | Defaults | **Detect TV language/region**; allow manual overrides for language, time display and temperature unit. |

## Round 3 behavior implications

- **Immersion:** app/ambient playback is full-bleed; interactions can reveal contextual controls without keeping menus permanently visible.
- **Distinct controls:** camera change is different from local video seek. Define exact mapping and discoverability in a later interaction spec; do not assume arrows will always mean the same action.
- **Editor:** both quick settings/presets and a full editor must be usable from a remote. On-screen movement, focus, safe margins and live preview must be validated on real hardware.
- **Display care:** reposition and idle-hide operate as independent, user-adjustable strategies; reserve overlays for useful information, avoid prolonged bright static elements. Never claim this eliminates burn-in.
- **Performance:** use direct cuts as a fallback for scenes where fades require extra decoders/surfaces; respect system power/sleep behavior.
- **Localization:** initial text PT-BR/EN, default from device region; explicit manual language, temperature unit and clock-format overrides.
- **Onboarding:** skip never blocks entry into manual Home; selecting optional sources/weather can happen later.

## Round 4: confirmed (D-29 to D-38)

| ID | Requirement | Confirmed choice / boundary |
| --- | --- | --- |
| D-29 | Camera categories | **Broad curated catalog**: beaches, cities, mountains, wildlife, landmarks, ski resorts, ports and additional themes. Exact taxonomy and source availability are to be researched; only include authorized sources. |
| D-30 | Catalog updates | **Automatically update catalog online**, with a bundled/local fallback list. Fallback metadata cannot make offline webcams play; the app must explain loss of connectivity. |
| D-31 | Catalog contributions | **Authorized camera providers and community suggestions subject to review**. Community submissions are not automatically trusted or published; review attribution, rights, safety and stream compatibility. |
| D-32 | Custom stream entry | **Both TV keyboard and QR code** for convenient entry. QR-assisted transfer must be designed for safe pairing with no account; access control, network exposure and token expiry remain engineering decisions. |
| D-33 | Optional camera details | **Name, country, local time, stream resolution, status, and source** available through an optional info panel; do not show unverified quality/online status as fact. |
| D-34 | Catalog filters | **Country, city, category, favorites, and only available cameras**. Online/available indicator must be evidence-based; unknown status should be distinct from offline. |
| D-35 | Seven-day forecast | **Temperatures, rain, wind, humidity, sunrise and sunset**, wherever the weather provider supplies reliable values. Preserve compact default overlay and expanded on-demand details. |
| D-36 | Viewer location setup | **Manual city search plus optional device-assisted detection** on supported hardware. Do not require location permission to use camera-local forecasts. |
| D-37 | Wallpaper library | **Deferred until a future release**. Initial focus is live cameras and user-provided local videos; no integrated wallpaper downloading in the first release. |
| D-38 | All cameras unavailable | **Optional user-configured fallback to an existing local playlist**. If no fallback is configured/accessible, show a clear notice with a retry action. No infinite skipping loop. |

### Derived constraints (not additional approvals)

- Maintain a versioned, validated catalog format and safe last-known/bundled metadata fallback. A public metadata feed does not imply a paid backend or guarantee connectivity.
- Do not publish community camera URLs until reviewing authorization, owner/attribution details, public safety and working playback. Provide a community submission/review process to research later.
- QR pairing must not expose private URLs over insecure public endpoints; the exact discovery/pairing method is still open and must be threat-modeled before implementation.
- Availability filtering depends on current, observed checks with explicit unknown/error states; successful directory entry alone does not imply a playing camera.
- Respect user choice: only switch to local-video playback as an all-camera-failed fallback when the user configured it, and never break the separation between saved camera/video playlists.
- Forecast details depend on weather-provider data and terms; avoid inventing unsupported values and don't silently collect precise location.

## Round 5: background screensaver behavior, power and performance (OPEN)

Recommendations here are *not* confirmed requirements:

1. **Idle activation interval:** let the user choose from supported system intervals (for example 3/5/10/15 minutes), or fixed app timer? **Recommend:** guide the user to supported OS settings, with a clear note when the OEM disallows custom screensavers.
2. **Screen-off/power:** respect TV energy-saving and sleep/standby, or try to keep display awake at all times? **Recommend:** respect TV sleep and user energy preferences; no always-on promise across platforms.
3. **Hours/schedule:** always eligible when idle, or optional quiet-hour schedule / exclude overnight? **Recommend:** automatic mode eligible normally, plus optional schedule and time windows.
4. **When playback starts automatically:** replay last viewed camera, favorites/playlist, or choose? **Recommend:** user setting with last scene or camera playlist; local video playlist optional.
5. **When user presses Back/Select in Dream mode:** exit instantly to previous TV screen, show overlay, or custom behavior? **Recommend:** buttons reveal controls or exit predictably without hijacking system Home.
6. **Connection loss:** immediate failover among cameras; when network is down, local playlist fallback if configured, otherwise a clear reconnect UI? **Recommend:** finite connection checks, offline local fallback and no retry storm.
7. **App resources:** prioritize low-end Android TV CPU/RAM with automatic resolution/transitions adjustment, or maximize 4K even if stuttering? **Recommend:** smooth playback and hardware decoder stability first.
8. **Background downloads/network:** fetch catalog/weather and monitor streams only when needed, or refresh continuously? **Recommend:** infrequent cached updates, pause unnecessary work when screen is off or app inactive.
9. **USB removed during playback:** pause/retry same file indefinitely, return to library, or switch to next playable source and explain? **Recommend:** clearly show missing drive and allow selecting another source, no freeze.
10. **TV restart/relaunch behavior:** automatically start OpenVista on every boot, or let Android TV handle launch/screen-saver state? **Recommend:** do not auto-launch on boot by default; restore saved state on normal launch and Dream entry.

## Later-round topics (not approved yet)

- Default rotation interval, repeat vs sequential, per-playlist rules, favorite persistence, sleep/wake/resume.
- Exact D-pad button map for camera/player, accessibility and one-handed remote usability.
- Media codec format/HDR capability, local scanning and USB disconnect recovery.
- Provider choice, licenses, source terms, camera quality thresholds, weather refresh/privacy and API quotas.
- Offline cache and curated media licensing/attribution; no implicit right to mirror content.
- Screensaver auto-selection restrictions and power behavior per OS/OEM.
- Testing matrix and measurable performance/reliability budgets.

## Decision history

- **2026-10-24:** D-01–D-08 confirmed by owner (round 1).
- **2026-10-24:** D-09–D-18 confirmed by owner (round 2), including map, immediate camera skip and USB.
- **2026-10-24:** D-19–D-28 confirmed by owner (round 3), overriding previous recommendations in favor of almost invisible UI, contextual remote behavior, 2D map with sidebar and both anti-static-overlay strategies.
- **2026-10-24:** D-29–D-38 confirmed by owner (round 4): broad categories, online catalog with local fallback, reviewed community suggestions, keyboard/QR camera entry, optional info panel, filters, detailed seven-day weather, manual/optional auto location, deferred wallpaper library, and configurable local fallback.
- **2026-10-24:** Round 5 opened to decide idle activation, power, scheduling, background use and performance.
