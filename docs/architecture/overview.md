# OpenVista architecture: hypotheses, not approved implementation

## Design constraints
- A normal fullscreen app must work even where the OS restricts third-party screensavers.
- Optional Android `DreamService` can share a media session/controller layer with the fullscreen app. Its availability depends on OS/OEM.
- Hardware-decoded video and power/memory limits matter on low-end TV SoCs. Codec capability is per device and source.
- TV remote focus/back handling, no touch assumptions.

## Candidate seams
1. **Scene catalog and discovery**: camera catalog metadata, geolocation, attribution, favorites, categories, country filtering and a TV-remote-accessible interactive world map. Evaluate marker clustering and a list fallback.
2. **Media source resolver**: permitted custom and catalog camera URLs or persisted local content URIs for internal/USB folders; return a playable source or typed failure.
3. **Playback and sequence controller**: AndroidX Media3/ExoPlayer candidate, ordered/shuffled playback, separate camera/video playlists, automatic rotation (1–60 min), immediate failover from offline cameras, loop detection when all sources fail, and live quality caps where renditions are available.
4. **Visual overlay editor**: compact default clock/current weather with 7-day forecast on demand; simple/advanced editing, presets, and independent settings for periodic widget position shift and inactivity hiding. Both display-care controls reduce risk but cannot guarantee no burn-in.
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
