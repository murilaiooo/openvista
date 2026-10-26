# Production module guide

**Current source guide, 2026-10-26.** The full development-preview source has partial independent Windows/Hisense validation. Not every feature is accepted; see [current status](../STATUS.md), the original [Codex handoff](codex-handoff.md) and [v1 release gates](../product/v1-acceptance.md).

| Module | Ownership and integration |
| --- | --- |
| `OpenVistaActivity`, `ui/` | Manual Home, skippable setup, focus/navigation history, source editing and list import, Favorites, typed playlists, Basic/Advanced settings, system picker results, fullscreen controls and English/PT-BR strings. |
| `library/` | One shared app-private state owner; bounded versioned atomic storage, transaction validation, library/playlist ordering, favorites, settings, per-video checkpoints, multiple selected SAF trees and direct documents, grant migration/revocation, session-only access and disconnected records. |
| `playback/` | Media3 HLS/DASH/local media, decoder fallback, quality cap, muted default, text-track disabling, audio selection integration, finite failed-source passes, camera rotation, local loop/resume, foreground snapshots and optional local fallback recovery. |
| `OpenVistaDreamService` | Optional platform-selected Dream entry using the same state/player; last scene, controls, overlay, optional local daily schedule and bounded lifecycle ownership. No boot receiver or settings writes. |
| `ambient/` | Clock/date/place overlays, presets/editor settings, independent reposition/hide, official U.S. NWS forecast adapter and cache, unconfigured city geocoder, and schematic user-coordinate map/list with unconfigured licensed map backdrop. |
| `diagnostics/` | A fixed-vocabulary, local, bounded seven-day error journal (64 entries). Reports omit titles, URLs, paths and coordinates and require preview plus explicit copy/share. Media3 exception logging is disabled to avoid recording user URIs. |
| `update/` | Gated release discovery, bounded private HTTPS download, SHA-256/package/version/current-signer checks, PackageInstaller staging, confirmation and lifecycle recovery. |
| `pairing/` | Gated two-minute, same-subnet, one-submission phone input with origin/host/token/body/rate checks and explicit TV approval. No source probing or autoplay. |

## Platform and content constraints

The player retains package `com.openvista.prototype` to preserve the existing local test-app identity. Its source version is `0.2.0-implementation-preview` / code 2, minimum API 28, target/compile API 36. This is not a new public package or release, and signing continuity is still ChatGPT's validation responsibility. The separate diagnostic application stays separate.

Only the new Home activity is launcher-visible. The earlier engineering activity remains internal for continuity. There is no boot launch, privileged permission, broad storage permission, bundled stream, source list, map dataset or media. Static vector Home artwork is interface artwork, not demo playback content. HTTP remains available for explicitly supplied sources; the updater requires HTTPS. A user who plays an HTTP source accepts that transport's confidentiality limits.

`SafVideoScanner` walks recursively without a depth cutoff, detects repeated document IDs, and limits a scan to 20,000 inspected entries and 2,000 video candidates. The library supports at most 2,000 source records, 100 playlists, 32 folders and an 8 MiB private state file; capacity failures preserve the preceding transaction. These are implementation bounds, not measured hardware budgets. Incomplete scans have an explicit notice. Missing media and revoked access preserve saved metadata rather than deleting user files.

Camera startup/stall attempts time out after 15 seconds. An all-failed camera pass can enter only an explicitly configured video playlist. Recovery attempts occur only after a local video ends, use one decoder and require a rendered camera frame. Attempts back off to five minutes, with six attempts per finite pass and a five-minute cooldown before another pass; a persistent camera cursor prevents starvation in larger libraries. There is no retry storm or parallel camera probe by design; timing and failure behavior remain unvalidated.

## Provider and release boundaries

`ForecastProvider`, `CitySearchProvider` and `MapBackdropProvider` require adapters reviewed for licensing, privacy, quota and attribution. Their repositories already handle asynchronous cancellation, bounded cache data, missing fields, stale/offline data and UI results. The app now installs the official U.S. NWS adapter for supported viewer/camera coordinates and sends weather requests only when needed. It does **not** configure a city geocoder or licensed map tiles. The map displays an explicitly labeled coordinate schematic and saved-source sidebar without claiming a real licensed basemap. See [NWS validation](../testing/nws-weather-integration-2026-10-26.md).

Forecast and map data are not demo fixtures. The forecast cache retains one location, and the map cache retains one explicitly requested viewport, never a bulk tile set. Cache age and permitted persistence follow the eventual adapter policy. Map images are bounded to 1024 × 768. A map adapter must render the supplied equirectangular viewport so geotagged markers align with it.

`UpdateChannel.APPROVED` and `PairingSecurity.REVIEWED` remain false. Preferences and incoming requests cannot bypass these gates. The future update asset/tag convention is a proposed channel contract in [update-channel.md](update-channel.md); it is not permission to publish. Pairing's [threat model](../security/lan-pairing-threat-model.md) explicitly identifies plain-HTTP confidentiality as unresolved. Both implementations need review before activation.

## Implementation references

- [Android scoped document access and persistable grants](https://developer.android.com/training/data-storage/shared/documents-files).
- [Android TV navigation](https://developer.android.com/training/tv/get-started/navigation).
- [Media3 track selection](https://developer.android.com/media/media3/exoplayer/track-selection) and [logging controls](https://developer.android.com/reference/androidx/media3/common/util/Log).
- [DreamService](https://developer.android.com/reference/android/service/dreams/DreamService).
- [ZXing 3.5.3 source and release](https://github.com/zxing/zxing/releases/tag/zxing-3.5.3), used only to encode local pairing QR images. It is an Apache-2.0 dependency and performs no network operation. Its artifact notices must remain in any eventual packaged distribution.

**Historical handoff note:** The initial Codex implementation mission did not run tests, compilation, Android Lint, APK packaging, validation scripts, CI or device operations. Subsequent independent QA **did** run these checks and physically test the app. See [current status](../STATUS.md), [TV QA](../testing/production-pr22-validation-2026-10-26.md) and [NWS QA](../testing/nws-weather-integration-2026-10-26.md).
