# Product decisions: OpenVista

**Status:** Discovery, round 1 completed on 2026-10-24. Round 2 is open.

Treat **Confirmed** as approved product requirements, **Open** as interview decisions, and **Proposed** as recommendations only. Do not silently implement recommendations.

## Confirmed baseline

The owner wants a general Android TV app named OpenVista, not a product tied to one Hisense device. Its two content families are real-time webcams around the world and looping videos downloaded by the user and stored on their television. The app targets clock/weather information including a seven-day forecast. A future video-wallpaper library is an exploration, not a confirmed MVP feature.

## Round 1: confirmed choices

| ID | Question | Owner's decision | Detail / boundary |
| --- | --- | --- | --- |
| D-01 | Launch behavior | **Both** automatic system screensaver and manually opened app | Auto activation is conditional on OS/OEM support; offer manual mode regardless. |
| D-02 | Live camera selection | **Both** a browsable catalog and manual URL entry | Only authorized/permitted sources. Catalog provider and protocol support remain open. |
| D-03 | Local videos | **Folder-based** playback with sequence, shuffle and playlists | Folder picker and persisted access; number of folders, USB and network storage are still open. |
| D-04 | Weather context | **Choose either** the displayed camera's location or the viewer's location | Viewer location must be opt-in, or manually entered; no silent location collection. |
| D-05 | Clock/weather overlays | **Fully customizable** | Which components, positioning, styling and whether seven-day forecast stays always visible are open. |
| D-06 | Sound | **Muted by default**, with the option to enable | Handle silent camera streams and user videos gracefully. |
| D-07 | Business model | **100% free, ad-free, no account** | Any providers must be usable under that distribution model; no user account required. |
| D-08 | First release distribution | **APK via GitHub** | Future store distribution is undecided. No release or APK exists yet. |

### Consequences for the architecture

- Design a fullscreen **app mode** independently from optional Android **DreamService mode**; a third-party screensaver cannot be promised on every TV.
- Separate content **catalog** and **custom source**, respecting stream permissions.
- Support a persistent user-selected folder and an independent **playlist/playback-order** model.
- Keep **weather context** switchable without requiring viewer geolocation.
- Overlay composition and controls must accommodate customization rather than a permanently fixed screen.
- Preserve audio mute on first use, and store explicit user choices.
- Avoid signup, advertising SDKs, and paid-service dependency in the baseline.
- Initial delivery and installation docs target signed APKs on GitHub Releases after testing.

## Round 2: user-experience decisions (OPEN)

Ask these in the next conversation round. Recommendations are not approved:

1. **Opening screen:** open a clear Home screen with two modes, or resume last scene immediately? **Recommend:** Home when manually launched; last selection when automatic screensaver starts.
2. **Camera discovery:** category/country/city browsing + search + favorites, or a simpler list? **Recommend:** categories, countries, search, and favorites; optional map later.
3. **Automatic switching:** manual only, or selectable 1/5/10/15/30/60-minute rotation? **Recommend:** user toggle and selectable intervals, default 10 minutes.
4. **Offline/failed camera:** show errors and wait, or retry briefly then advance to another allowed camera / fallback local file? **Recommend:** retry briefly, then advance with a visible unobtrusive notice.
5. **Folder support:** internal storage only, internal + USB, or internal + USB + SMB/NAS immediately? **Recommend:** internal and USB in MVP where the picker permits, consider NAS later.
6. **Playlist scope:** one active playlist, or many saved named playlists mixing local videos and online cameras? **Recommend:** several named collections, but decide whether media families can mix.
7. **Overlay editor depth:** presets + individual toggles/position/style, or only a few simple presets? **Recommend:** simple presets plus an Advanced screen for components, size, position, opacity, colors and forecast.
8. **Video fitting:** fit entire picture (black bars sometimes), fill/crop, or user-controlled? **Recommend:** user-controlled with Fit as default.
9. **Quality/bandwidth:** always request maximum available quality, manual quality selector, or Auto + configurable cap? **Recommend:** Auto with caps on supported providers/players.
10. **Language and units:** English only initially, Portuguese and English, or multilingual from first release? **Recommend:** English + Brazilian Portuguese, 12/24-hour clock and C/F units.

## Later rounds (do not assume yet)

- Visual design style, TV remote navigation map, favorites behavior and onboarding.
- Exact screensaver timeout/OS constraints, remote wake and power/energy policy.
- Search facets, live-camera source verification, catalog curation and attribution.
- Local folder changes, removable drive recovery, nested files, formats and file errors.
- Weather API, geocoding, data retention, forecast update frequency and stale data.
- Weather overlays: fixed text/burn-in avoidance, clock time zone and forecast readability.
- Playlist playback rules (camera + local media, repeat/fallback and per-scene timer).
- Online streams, codec/HDR support, buffering, data usage and 24-hour endurance.
- Third-party wallpaper library API, downloads, licenses, offline caches and storage budgets.
- Accessibility, translations, parental safe content filters and update policy.

## Decision log

- **2026-10-24:** Owner confirmed D-01 through D-08 via the first-round questionnaire. These supersede the former proposed answers, including **fully customizable** overlays and **GitHub APK as the only confirmed initial distribution**.
- **2026-10-24:** Round 2 opened. All suggestions remain nonbinding until explicitly answered.
