# OpenVista product decisions

**Status:** Discovery. Round 1 and Round 2 confirmed on 2026-10-24; Round 3 open.

Only **Confirmed** items are requirements. **Open** questions and recommendations are not approval. A requested feature is not evidence of technical feasibility or universal device compatibility.

## Confirmed baseline

OpenVista is a free, ad-free, account-free Android TV ambient app and screensaver, intended for the broader Android TV ecosystem, including Google TV and Fire TV where compatible. It shows either permitted real-time webcams or locally stored looping scenic videos, with optional, customizable clock and weather overlays, including a seven-day forecast. A future wallpaper library is an exploration, not yet a committed first-release feature.

## Round 1: confirmed decisions

| ID | Decision | Owner's selection | Boundaries |
| --- | --- | --- | --- |
| D-01 | Entry points | Both automatic screensaver and manually opened app | Automatic mode only where the OS/OEM allows it; do not change system-wide settings without consent. |
| D-02 | Live camera input | Both a browsable catalog and manual URL entry | Only streams permitted for this use; validate protocol, ownership and accessibility. |
| D-03 | Local media | Select a folder with sequence, shuffle and playlists | Details refined in D-13 and D-14. |
| D-04 | Weather location | Switch between camera location and viewer location | Viewer location optional; request consent when necessary. |
| D-05 | Weather/clock overlay | Fully customizable | Presets plus editor confirmed in D-15. |
| D-06 | Audio | Muted by default with user option to enable | Do not assume every live camera carries audio. |
| D-07 | Business model | 100% free, no ads, no account | Third-party services must be compatible with this model. |
| D-08 | First distribution | APK through GitHub initially | Other stores deferred; no release APK exists yet. |

## Round 2: confirmed decisions

| ID | Decision | Owner's selection | Boundary / implementation implication |
| --- | --- | --- | --- |
| D-09 | Home screen | **Three main entries: Cameras, My Videos, Favorites** | Manual app opens at Home; whether screensaver resumes a particular scene remains open. |
| D-10 | Discover cameras | **Both an organized catalog and an interactive world map** | Include TV-remote-accessible marker navigation; map engine, grouping, map data licensing and performance still need evaluation. |
| D-11 | Automatic scene rotation | **Configurable interval from 1 to 60 minutes** | User-selected interval; exact stepping, enable/disable behavior and different defaults per mode remain open. |
| D-12 | Failed webcam | **Immediately skip to the next available camera** | No prolonged on-screen retry state. Avoid endless cycles if *all* cameras fail; detect source failures and provide fallback. |
| D-13 | Local video storage | **Internal storage and USB flash drives / external USB HDDs** | No network/NAS requirement yet. Subject to Android system picker support and scoped-storage restrictions. |
| D-14 | Playlists | **Multiple playlists, separating cameras from videos** | No mixing cameras and local video in the same playlist for now. Named playlists, persistence and order remain to be detailed. |
| D-15 | Overlay configuration | **Presets AND a full visual editor** | The editor must work with a TV remote; precise editable properties and rendering behavior remain open. |
| D-16 | Video fitting | **Fill the TV screen, cropping when necessary** | Cropping is the selected default behavior. Avoid distortion/stretch and clarify if alternative fit mode is offered later. |
| D-17 | Stream resolution | **Automatic quality with a configurable cap** | The maximum depends on original stream variants and TV codec support; do not promise dynamic selection where provider has only one rendition. |
| D-18 | Localization | **Brazilian Portuguese and English** | Default selection, forecast units, clock formats, region fallbacks and translation quality remain open. |

### Derived constraints (not extra product decisions)

- The Home layout must prominently expose three confirmed destinations, while a map may be reachable from Cameras and favorites may contain separate media types.
- Camera and local-video playlists are different domain concepts or types, even if they share a common sequencing interface.
- A camera-failure resolver should avoid repeated looping through offline entries, and must surface an unobtrusive fallback if the whole collection is unavailable.
- Internal/USB media should be accessed through user-granted Storage Access Framework mechanisms when supported; never assume arbitrary filesystem paths are available.
- Interactive world maps can be costly on TV hardware; prototype accessible D-pad focus, marker clustering and a list alternative before choosing a rendering library.
- Live ABR and resolution caps are conditional on the stream format and server offerings; local videos must respect decoder capabilities.
- No login, analytics advertising SDK or paid backend is required for the agreed baseline.

## Round 3: look and interaction (OPEN)

Recommendations in this section are **not approved**.

1. **Overall visual style:** cinematic dark/immersive, information-rich dashboard, or light/minimal? **Recommend:** cinematic dark, with overlays that do not obscure video.
2. **Main navigation:** left-side rail with three confirmed destinations, horizontal navigation tabs, or full-screen tile hub? **Recommend:** large three-destination tile Home with a lightweight side menu in secondary screens.
3. **TV remote controls during playback:** OK reveals controls; Left/Right change scene; Back hides controls/exits; or a less gesture-dependent menu? **Recommend:** OK for controls, Left/Right for scene changes, Back to exit; avoid conflicting with seek on local files.
4. **Interactive map presentation:** 2D map with clustered markers and list alternative, 3D globe, or map-only? **Recommend:** efficient 2D map with clustered markers plus remote-friendly synchronized list.
5. **Initial clock/weather appearance:** always visible compact widgets, automatically fading overlays, or no overlays until OK? **Recommend:** clean compact clock and current weather visible, forecast available on request; all overridable in editor.
6. **Visual editor UX:** on-TV drag-by-D-pad with safe placement grid, free cursor, or preset-only customization? **Recommend:** grid/zone-based D-pad movement, plus font size/color/opacity and live preview.
7. **Anti-burn-in handling:** move static overlays periodically, hide after inactivity, or both? **Recommend:** both configurable, accounting for LCD as well as OLED/QD-OLED TVs.
8. **Initial onboarding:** start playback immediately, short guided setup, or full multi-step wizard? **Recommend:** optional short guided setup for sources, storage and weather, with Skip button.
9. **Fullscreen scene transition:** instant cut, short crossfade, or user selectable? **Recommend:** simple quick fade only when smooth on lower-powered TVs, configurable/disable.
10. **Language and units defaults:** derive from TV locale (PT-BR/EN) with manual overrides, or force a welcome language choice? **Recommend:** use TV language initially, with independent settings for 12/24h and °C/°F.

## Later design branches

- Content curation and licensing, stream URL validation, regions/categories and safe-content requirements.
- Playlists, favorites and failure fallback when nothing is playable.
- Weather providers, forecasts, timezones, permissions and stale-data presentation.
- Source rotation behavior, scene schedule, user controls, sleep and app return state.
- Adjustable streaming cap and data usage; HDR/codec fallback, long-run memory and decoder testing.
- Local folder workflows and USB limitations, supported file types and video scanning.
- Screensaver selection restrictions by OEM and Android version; no privileged workarounds by default.
- Third-party wallpaper library, licensing, attribution, rights and local download cache policies.
- Visual polish, accessibility and more than 2 languages after the initial release.

## Decision history

- **2026-10-24:** Owner confirmed D-01 to D-08 in round 1.
- **2026-10-24:** Owner confirmed D-09 to D-18 in round 2. Choices override earlier **recommendations**, especially the interactive map, *immediate* camera failover, separate playlists, fullscreen crop and GitHub-first distribution.
- **2026-10-24:** Round 3 opened. All Round 3 recommendations remain open.
