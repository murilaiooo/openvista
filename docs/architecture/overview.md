# OpenVista architecture: hypotheses, not approved implementation

## Design constraints
- A normal fullscreen app must work even where the OS restricts third-party screensavers.
- Optional Android `DreamService` can share a media session/controller layer with the fullscreen app. Its availability depends on OS/OEM.
- Hardware-decoded video and power/memory limits matter on low-end TV SoCs. Codec capability is per device and source.
- TV remote focus/back handling, no touch assumptions.

## Candidate seams
1. **Scene catalog and discovery**: broad licensed webcam themes (beaches, cities, mountains, wildlife, landmarks, ski resorts, ports), country/city/theme/favorites/verified-available filters and a TV-remote-accessible interactive map. Investigate a versioned remotely updated metadata feed with bundled last-resort catalog data; community suggestions must be manually reviewed before publication. Distinguish available/unavailable/unknown source status.
2. **Media source resolver and source details**: permitted custom and catalog camera URLs (TV keyboard or secure QR-assisted pairing), persisted local URIs for internal/USB folders, location/title/attribution/local time/status/actual resolution metadata and typed playback errors. QR pairing method, network boundary and authentication are research tasks.
3. **Playback and sequence controller**: AndroidX Media3/ExoPlayer candidate, ordered/shuffled playback, separate camera/video playlists, automatic rotation (1–60 min), immediate failover from offline cameras, bounded detection when all sources fail, optional fallback to a configured local video playlist (otherwise an explicit retry state), and live quality caps where renditions are available.
4. **Weather/overlay controller**: compact clock/current weather and expanded 7-day high/low temperatures, rain, wind, humidity and sunrise/sunset when offered by an authorized weather provider. Support camera or viewer weather context, manual city geocoding and optional device detection; no forced location permissions. Preserve simple/advanced editor, presets and independent position-shift/inactivity-hide settings.
5. **Input/navigation**: image-backed Home cards for Cameras/My Videos/Favorites, 2D map clustered markers with synchronized side list, TV D-pad focus, camera-change arrows versus local-video seek behavior, a skippable first-run guide, app/DreamService entry points and Back behavior (exact key map still open).
6. **Dream integration**: optional service + settings guidance, never invasive undocumented automatic system changes.
7. **Settings/persistence**: user-owned preferences, playlist definitions, URI permissions, catalog cache, instant/smooth transition preference with low-performance fallback, no API secrets in source, and PT-BR/English with TV-locale detection and manual overrides.

**Do not construct a large abstraction layer now.** Validate seams with independent spikes for HLS playback, folder selection and DreamService before finalizing structure.

## User content folder
Investigate `ACTION_OPEN_DOCUMENT_TREE` with persisted URI permissions and `DocumentFile`. Android 11+ disallows choosing certain roots and Download directories by tree picker; OEMs may omit a functioning DocumentsUI. Provide a user-tested fallback, without broad storage access by default.

## Streams and weather
A provider adapter should enforce permitted sources; no scraping/rehosting camera streams contrary to provider terms. Keep stream endpoints configurable and check liveness. Weather data must be licensed for intended distribution; use caching and explicit timezone attribution.

## Proposed tests
- Unit tests: separated camera/local playlists, shuffle/sequence, 1–60 min timers, immediate failover and all-offline fallback, quality caps, timezone labels, forecast state and permissions boundaries.
- Emulator: focus graph, image-backed Home accessibility, map marker clustering/list navigation, camera-versus-local contextual remote input, simple/advanced overlay editor, reposition and inactivity-hide settings, skippable onboarding, switchable transition modes, source rotation, USB-picker boundaries, locale detection/overrides, lifecycle and process death.
- Real TV: internal/USB folder persistence, fill/crop, automatic quality fallback, cold/warm start, bitrates/codecs, HDR fallback, sleep/wake, 24-hour soak and CPU/RAM/network.
