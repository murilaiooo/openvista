# OpenVista product decisions

**Status:** Discovery. Rounds 1, 2 and 3 confirmed on 2026-10-24. Round 4 open.

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

## Round 4: sources, catalog, weather and wallpapers (OPEN)

The owner has not answered these. Recommendations are not approvals.

1. **Catalog categories:** prioritize nature/cities/beaches only, or broad themed categories including wildlife, landmarks, ski resorts, marinas and weather? **Recommend broad curated categories** with reliable, lawful footage.
2. **Catalog maintenance:** ship a small hardcoded list, or a remotely updateable public feed requiring no account? **Recommend public static metadata feed with bundled offline fallback**, reviewed/hosted without paid backend.
3. **Source eligibility:** only officially permitted integrations, or user-added URLs with separate validation? **Recommend both**, but never embed unauthorized streams or scrape protected video.
4. **Custom URL entry:** TV keyboard only, QR-assisted pairing from a phone on the same network, or both? **Recommend both**, with no account/cloud relay and explicit network/security safeguards.
5. **Camera information:** show country/city, scene title, stream status, resolution and attribution on an optional info panel, or just the scene title? **Recommend info panel on demand**, minimal default chrome.
6. **Discovery filters:** region/theme + live status and favorites, or region/theme only? **Recommend region/theme + available-only + favorites**, with only verifiable status claims.
7. **Forecast details:** 7-day temperature/conditions only, or extended daily info (rain chance, highs/lows, wind, sunrise/sunset) when data supports it? **Recommend extended detail on demand** without crowding video.
8. **Viewer location setup:** manual city search only, automatic geolocation opt-in only, or both? **Recommend manual city first plus opt-in coarse/device location where actually supported**.
9. **Wallpaper library release scope:** include a curated integrated download library in the first APK, or defer to a future release? **Recommend defer**, fully support user-selected local videos first.
10. **When all live cameras fail:** show a clear retry screen, switch to user's existing local video playlist, or provide choice? **Recommend user's opt-in fallback to local videos; otherwise an informative screen**, no endless spin.

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
- **2026-10-24:** Round 4 opened; source/provider/weather/library options await owner decisions.
