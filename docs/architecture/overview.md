# OpenVista architecture

**Current as of 2026-10-26:** this describes implemented Kotlin development-preview modules, not a proposal to build a new app. Evidence, limitations and hardware-specific acceptance live in [STATUS](../STATUS.md), the [module guide](../implementation/production-module-guide.md) and [device matrix](../testing/compatibility.md).

## System boundaries

OpenVista is an **offline-first, account-free native Android TV application**. It uses Media3 playback for compatible **user-selected** live HLS/MPEG-DASH and local videos. No bundled streams, sample media, hosted source directory, cloud account or TV boot launch are present.

| Code | Responsibility | Key boundary |
| --- | --- | --- |
| player / OpenVistaActivity | TV launcher, navigation history, dialogs, Activity Result picker integration, foreground lifecycle | One visible app package: com.openvista.prototype |
| player / ui | Home, source library, settings, remote focus, playback controls and PT-BR/EN resources | D-pad first; no touch requirement |
| player / library | Process-shared LibraryStore and SAF access management, bounded atomic JSON state, favorites, playlists and resume checkpoints | Never bypass Android user-granted storage URIs |
| player / playback | Media3 ExoPlayer owner, source selection, rotation, retries/fallback, resumable local media | Shared player ownership; no concurrent active decoders assumed |
| player / ambient | Clock/date/place overlay, NWS forecast provider/cache, user-coordinate schematic map/list | U.S. NWS only; no external geocoder or licensed base map |
| player / OpenVistaDreamService | Optional OS-selected screensaver entry with shared state | Must be validated per Fire OS device; no forced system setting writes |
| player / update | Version/channel/download/hash/signer/PackageInstaller pathways | Feature intentionally disabled pending distribution approval |
| player / pairing | Short-lived phone entry, QR and TV confirmation boundary | Feature intentionally disabled pending security approval |
| player / diagnostics | Bounded sanitized local diagnostic journal | No telemetry; deliberate report preview/copy/share only |
| source-core | Validates user-supplied direct URLs, playlists and file rules | Limits size, count, invalid schemes, duplicate records |
| diagnostics | Independent read-only device capability probe | Separate diagnostic package; not the TV player |

## App lifecycle

- The player launches from the TV Home. It does not start automatically on boot.
- A media source may be entered through the TV keyboard, user-selected M3U with ACTION_OPEN_DOCUMENT, a chosen video file, or an authorized folder tree with ACTION_OPEN_DOCUMENT_TREE.
- URI grants are handled through Android scoped-storage APIs; unavailable/revoked folders preserve metadata without inventing files.
- Persisted library and last-scene state stay in app-private storage. Settings/favorites/playlists are not uploaded or automatically backed up off-device.
- Display and decoder lifecycle are bounded: no promise that all sources or 4K/HDR codecs work on every TV.
- The optional DreamService is separate from normal app mode and remains under system/OEM control.
- Paired phone input and public updater are disabled by security/release gates even if their source is present.

## Weather and map data

An official National Weather Service adapter resolves U.S. coordinates to NWS gridpoint daily and hourly forecasts using HTTPS and an identifying User-Agent. Cache refresh is rate-limited; weather is optional and some forecast fields may be absent. Forecast probability (%) is distinct from rainfall volume (mm). The provider is identified in documentation and request metadata without an on-video attribution label.

NWS is not a global service. Imported cameras lacking coordinates may show the viewer-selected city weather instead. City geocoding is not enabled. The existing map presents saved locations in a schematic/list, not an approved online tile basemap.

See [NWS validation](../testing/nws-weather-integration-2026-10-26.md), [privacy](../SECURITY_AND_CONTENT.md) and [current blockers](../STATUS.md).

## Verification boundaries

Only the Hisense 58U6HF / Fire OS 7 / Android 9 (API 28) has device test evidence. The working Android SDK is configured for compile/target 36, minimum 28. A local debug APK passing tests and playing a fixture is **not** a public signed release, stable-v1 proof or generalized compatibility claim.

| Gate | Remaining work |
| --- | --- |
| Media | Real USB failure, high-resolution/codec/thermal and long-run tests, source recovery |
| UI | Full D-pad/accessibility and localization test matrix |
| Dream | OEM service selection, idle timers and remote |
| Weather/map | NWS stale/offline longevity; worldwide data/licensed map/geocoding choice |
| Security | Phone pairing confidentiality and threat-model approval |
| Updates | Signing/channel approval and actual Android installer behavior |
| Stable v1 | G01–G10 review, 24-hour soak and owner authorization |

The original architecture hypotheses and planning audit are retained in the dated research/history files, not as a reason to rebuild a working codebase.
