# OpenVista architecture: hypotheses, not approved implementation

## Design constraints
- A normal fullscreen app must work even where the OS restricts third-party screensavers.
- Optional Android `DreamService` can share a media session/controller layer with the fullscreen app. Its availability depends on OS/OEM.
- Hardware-decoded video and power/memory limits matter on low-end TV SoCs. Codec capability is per device and source.
- TV remote focus/back handling, no touch assumptions.

## Candidate seams
1. **Scene catalog**: metadata, geolocation, descriptions, attribution, categories, favorites, source health.
2. **Media source resolver**: permissioned network streams or persisted local content URIs; returns a playable source or typed failure.
3. **Playback controller**: AndroidX Media3/ExoPlayer; stream recovery, shuffle/loop, quality fallback, surface lifecycle.
4. **Overlay controller**: timezones, clock, weather caching, forecast summaries, visibility and burn-in movement.
5. **Input/navigation**: D-pad user flows, focus, accessibility, entry points and back behavior.
6. **Dream integration**: optional service + settings guidance, never invasive undocumented automatic system changes.
7. **Settings/persistence**: user-owned preferences, URI permissions, catalog cache, no API secrets in source.

**Do not construct a large abstraction layer now.** Validate seams with independent spikes for HLS playback, folder selection and DreamService before finalizing structure.

## User content folder
Investigate `ACTION_OPEN_DOCUMENT_TREE` with persisted URI permissions and `DocumentFile`. Android 11+ disallows choosing certain roots and Download directories by tree picker; OEMs may omit a functioning DocumentsUI. Provide a user-tested fallback, without broad storage access by default.

## Streams and weather
A provider adapter should enforce permitted sources; no scraping/rehosting camera streams contrary to provider terms. Keep stream endpoints configurable and check liveness. Weather data must be licensed for intended distribution; use caching and explicit timezone attribution.

## Proposed tests
- Unit tests: playlist order, failover, time zone labels, metadata parsing, forecast state and permissions boundaries.
- Emulator: focus graph, lifecycle, rotation, process death.
- Real TV: cold/warm start, bitrates/codecs, HDR fallback, sleep/wake, 24-hour soak and CPU/RAM/network.
