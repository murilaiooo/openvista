# OpenVista architecture: hypotheses, not approved implementation

## Design constraints
- A normal fullscreen app must work even where the OS restricts third-party screensavers.
- Optional Android `DreamService` can share a media session/controller layer with the fullscreen app. Its availability depends on OS/OEM.
- Hardware-decoded video and power/memory limits matter on low-end TV SoCs. Codec capability is per device and source.
- TV remote focus/back handling, no touch assumptions. The owner approved OK to show Dream controls, Back to exit, and directional navigation where the OS delivers them. **No automatic app launch on TV boot.**

## Candidate seams
1. **Scene catalog and discovery**: broad licensed webcam themes (beaches, cities, mountains, wildlife, landmarks, ski resorts, ports), country/city/theme/favorites/verified-available filters and a TV-remote-accessible interactive map. Investigate a versioned remotely updated metadata feed with bundled last-resort catalog data; community suggestions must be manually reviewed before publication. Distinguish available/unavailable/unknown source status.
2. **Media source resolver and source details**: permitted custom and catalog camera URLs (TV keyboard or secure QR-assisted pairing), persisted local URIs for internal/USB folders, location/title/attribution/local time/status/actual resolution metadata and typed playback errors. QR pairing method, network boundary and authentication are research tasks.
3. **Playback and sequence controller**: AndroidX Media3/ExoPlayer candidate, ordered/shuffled playback, separate camera/video playlists, automatic rotation (1–60 min), persist last camera/video for Dream entry, immediately skip unavailable streams, optionally switch to the configured local fallback during network outage, **continue the local video while checking reconnection non-disruptively**, and offer an explicit retry state otherwise. Prioritize fluid playback via adaptive quality/effects. Precise video seek-position resume remains open.
4. **Weather/overlay controller**: compact clock/current weather and expanded 7-day high/low temperatures, rain, wind, humidity and sunrise/sunset when offered by an authorized weather provider. Support camera or viewer weather context, manual city geocoding and optional device detection; no forced location permissions. Preserve simple/advanced editor, presets and independent position-shift/inactivity-hide settings.
5. **Input/navigation**: image-backed Home cards for Cameras/My Videos/Favorites, 2D map clustered markers with synchronized side list, TV D-pad focus, camera-change arrows versus local-video seek behavior, a skippable first-run guide, app/DreamService entry points and Back behavior (exact key map still open).
6. **Dream lifecycle, energy and scheduling**: optional DreamService with supported **1/3/5/10/15/30-minute** system-owned idle intervals and user-facing limitation guidance; keep-awake settings only within OS/OEM policy; optional schedule; no boot autostart or invasive settings changes. On resume, attempt last camera/video. App mode remains independent.
7. **Settings/persistence and resources**: last-scene identity, user-owned preferences, optional schedules and keep-awake flags, playlist definitions, USB/internal URI permissions and removal notices, bounded/cache-based catalog/weather updates, instant/smooth transition preference with low-performance fallback, no API secrets in source, and PT-BR/English with TV-locale detection and manual overrides.

**Do not construct a large abstraction layer now.** Validate seams with independent spikes for HLS playback, folder selection and DreamService before finalizing structure.

## User content folder
Investigate `ACTION_OPEN_DOCUMENT_TREE` with persisted URI permissions and `DocumentFile`. Android 11+ disallows choosing certain roots and Download directories by tree picker; OEMs may omit a functioning DocumentsUI. Provide a user-tested fallback, without broad storage access by default.

## Streams and weather
A provider adapter should enforce permitted sources; no scraping/rehosting camera streams contrary to provider terms. Keep stream endpoints configurable and check liveness. Weather data must be licensed for intended distribution; use caching and explicit timezone attribution.

## Proposed tests
- Unit tests: separated camera/local playlists, shuffle/sequence, 1–60 min rotation, OS-supported idle-interval visibility, persisted last-scene choice, immediate failover, non-disruptive offline-to-local behavior with bounded reconnect probes, quality caps, schedule windows, timezone labels, forecast state and permissions boundaries.
- Emulator: focus graph, image-backed Home accessibility, map marker clustering/list navigation, camera-versus-local contextual remote input, simple/advanced overlay editor, reposition and inactivity-hide settings, skippable onboarding, switchable transition modes, source rotation, USB-picker boundaries, locale detection/overrides, lifecycle and process death.
- Real TV: internal/USB folder persistence and safe hot-unplug notice, fill/crop, adaptive playback and transitions, OS-specific Dream controls/timeouts, stay-awake/standby, no boot autostart, last-scene resume, offline playback without interruption on reconnect, background work suspension, HDR fallback, sleep/wake, 24-hour soak and CPU/RAM/network.
