# Curated upstream code research for OpenVista

**Research date:** October 25, 2026  
**Status:** Source review and engineering recommendations only. No upstream project was compiled or run as part of this review, and no OpenVista feature or Fire OS compatibility is proven by this document.  
**Project:** [OpenVista](https://github.com/murilaiooo/openvista), an original Kotlin ambient video app for Android TV / Google TV / Fire TV.  
**Target for initial local prototypes:** Hisense 58U6HF, Fire OS 7 / Android 9.

## Executive decision

**Do not fork another app to build OpenVista.** Its scope combines features that no single examined repository implements with the same boundaries: user-owned live sources, user-selected local/USB videos, ambient overlays, TV-first input, optional system screensaver mode, and a map containing only user-geotagged sources.

Use **official Android / AndroidX code first**, MIT/Apache-licensed reference implementations second, and GPL projects for **architectural observation only**. Prefer a minimal original Kotlin app with Media3, a clearly owned playback controller, and a manual full-screen Activity before introducing optional `DreamService`.

This document complements [platform feasibility](platform-feasibility-2026-10-25.md), [architecture hypotheses](../architecture/overview.md), [priority plan](../implementation/priority-plan.md), [user-managed content contract](../product/user-managed-content.md), and the existing [reference index](references.md). If a historical requirement mentions shipping 25 cameras, the later **C-01 user-managed-content clarification** controls: OpenVista ships **zero** camera feeds, stream URLs, playlists, or user videos.

### Selection criteria

Repositories were selected for: (1) direct overlap with an OpenVista feature, (2) inspectable real source and implementation details, (3) relevant Android / Kotlin platform choices, (4) licensing clarity, and (5) a practical prototype that can be isolated and tested. Popularity alone was not the ranking criterion. A small repository can be a useful narrow example, but it should not be assumed production-proven.

## The curated shortlist

| Rank | Upstream | Why it matters to OpenVista | License / reuse posture | When |
| --- | --- | --- | --- | --- |
| 1 | [theothernt/AerialViews](https://github.com/theothernt/AerialViews) | Closest **product behavior**: ambient local/remote video, DreamService, playlists, overlays, on-TV controls | **GPL-3.0. Study behavior only; no direct source copy into MIT app.** | P0 architectural reading |
| 2 | [android/tv-samples](https://github.com/android/tv-samples) | Official Compose for TV UI, TV accessibility, focus/navigation and sample app structure | Apache-2.0; eligible for reuse with notices/compliance | P0 TV shell |
| 3 | [androidx/media](https://github.com/androidx/media) | Official Media3 / ExoPlayer player lifecycle, HLS, DASH, track/decoder behavior | Apache-2.0; primary playback reference | P0 playback |
| 4 | [hauxir/androidtv-live-screensaver](https://github.com/hauxir/androidtv-live-screensaver) | Compact real `DreamService` + live HLS sample | MIT; adapt *only after* independent review | P0 screensaver spike |
| 5 | [open-ott-play/ottplay-android](https://github.com/open-ott-play/ottplay-android) | Native Kotlin user-managed source entry, M3U parsing, URL/header policy and Media3 setup | MIT root license; review notices and vendored modules separately | P0 import/source spike |
| 6 | [android/storage-samples](https://github.com/android/storage-samples) | Official `ACTION_OPEN_DOCUMENT_TREE` and SAF examples | Apache-2.0; **archived**, so verify against current platform docs | P0 storage spike |
| 7 | [isnow-git/strix](https://github.com/isnow-git/strix) | Low-memory streaming M3U import, bounded player behavior, D-pad UI | Apache-2.0; promising but small/early project, not a proven benchmark | P1 importer/performance |
| 8 | [jellyfin/jellyfin-androidtv](https://github.com/jellyfin/jellyfin-androidtv) | Mature Android TV lifecycle, remote control, navigation and playback separation | GPL-2.0; architecture-only reference | P1 resiliency |
| 9 | [iFan6oy/tv-focus-navigation-kit](https://github.com/iFan6oy/tv-focus-navigation-kit) | Focus restoration, bring-into-view and navigation race conditions | Apache-2.0; narrow, small independent sample | P1 navigation polish |
| 10 | [maplibre/maplibre-native](https://github.com/maplibre/maplibre-native) | Maps for **user-geotagged** cameras; no third-party camera directory | BSD-2-Clause; check SDK size, tile rights and performance | P2 optional map |
| 11 | [ahXN00/OwnTV](https://github.com/ahXN00/OwnTV) | D-pad live-source switching, source management, player HUD concepts | GPL-3.0; architecture-only reference, avoid scope creep | P2 reference |
| 12 | [videolan/vlc-android](https://github.com/videolan/vlc-android) | Deep codec/stream-compatibility and Android TV playback case studies | GPL-2.0; reference-only, **not** default engine choice | Conditional only |

**Priority key:** P0 = read/prototype now; P1 = read when implementing that subsystem; P2 = defer until core feasibility; conditional = only if a measured blocker justifies it. These are OpenVista priorities, not claims about upstream quality.

## Code-reading guide: exactly where to look

### 1. Aerial Views: ambient experience and lifecycle

- [DreamActivity.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/ui/screensaver/DreamActivity.kt): DreamService lifecycle, interactive key handling, and activity handoff.
- [VideoPlayerHelper.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/ui/core/VideoPlayerHelper.kt): how video playback is connected to ambient presentation.
- [LocalMediaProvider.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/providers/LocalMediaProvider.kt): local media as a source type.
- [PlaylistCacheRepository.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/data/PlaylistCacheRepository.kt): playlist/cache separation.
- [ClockOverlay.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/ui/overlays/ClockOverlay.kt) and [WeatherForecastOverlay.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/ui/overlays/WeatherForecastOverlay.kt): overlay responsibilities, not copy-ready assets.
- [Overlay state directory](https://github.com/theothernt/AerialViews/tree/master/app/src/main/java/com/neilturner/aerialviews/ui/overlays/state): state/UI boundary.

**OpenVista extraction:** define separate playback and overlay state machines; keep clock/weather widgets independent of decoder surfaces; own one explicit enter/exit lifecycle for manual Activity and another for DreamService. Avoid treating an activity handoff or OEM-specific workaround as supported behavior without Hisense proof.

**Do not transplant:** GPL classes, bundled feed/provider lists, metadata catalog, artwork, weather integrations or embedded service-specific URLs. Aerial Views has analytics-related code paths; OpenVista must preserve its separate no-automatic-telemetry requirement.

### 2. Official Android TV samples: original TV shell

- [JetStreamCompose](https://github.com/android/tv-samples/tree/main/JetStreamCompose): media-app navigation, large TV cards, focus states and screens.
- [TvMaterialCatalog](https://github.com/android/tv-samples/tree/main/TvMaterialCatalog): Compose for TV component usage in isolation.
- [ReferenceAppKotlin](https://github.com/android/tv-samples/tree/main/ReferenceAppKotlin): modern TV application integration reference.
- [JetStream baseline profiles](https://github.com/android/tv-samples/blob/main/JetStreamCompose/baseline-profiles.md): potential startup/scroll performance work **after** correctness.

**OpenVista extraction:** build the three Home choices from the product contract; D-pad selectable controls must have visible focus and predictable Back. Validate navigation when collections are empty and when returning from fullscreen playback. Do not clone a content catalog or visual theme that clashes with ambient-first product decisions.

### 3. Official AndroidX Media3: player baseline

- [Media3 main ExoPlayer demo](https://github.com/androidx/media/tree/release/demos/main): canonical player setup and stream probing.
- [Media3 Compose demo](https://github.com/androidx/media/tree/release/demos/compose): useful experimental Compose integration; upstream labels it work in progress.
- [HLS documentation](https://developer.android.com/media/media3/exoplayer/hls) and [DASH documentation](https://developer.android.com/media/media3/exoplayer/dash): supported protocol behavior.
- [Supported formats](https://developer.android.com/media/media3/exoplayer/supported-formats) and [supported devices](https://developer.android.com/media/media3/exoplayer/supported-devices): decode limits are device-dependent.

**OpenVista extraction:** initially use one player instance, a stable video surface, a lifecycle-aware release strategy, typed failure categories and measured buffering. Explicitly distinguish HLS media *manifests* from M3U *lists*. Do not assume all `.m3u8` URLs are importable channel lists, or all extensionless URLs are progressive video. Test genuine sources on the TV.

**Avoid now:** multiple players, MPV/LibVLC dual-engine orchestration, universal transcoding, giant prefetch buffers, and claims that PC FFmpeg success proves Hisense playback.

### 4. Android TV Live Screensaver: smallest DreamService prototype

- [LiveScreensaverService.kt](https://github.com/hauxir/androidtv-live-screensaver/blob/main/app/src/main/java/com/livescreensaver/tv/LiveScreensaverService.kt)
- [AndroidManifest.xml](https://github.com/hauxir/androidtv-live-screensaver/blob/main/app/src/main/AndroidManifest.xml)
- [dream.xml](https://github.com/hauxir/androidtv-live-screensaver/blob/main/app/src/main/res/xml/dream.xml)

**Observed in code:** the service owns an ExoPlayer, uses a `SurfaceView` lifecycle, keeps audio muted, and has playback-error/retry/stall handling. Its source also declares a bundled default streaming URL and sets `isInteractive = false`. **Neither choice meets OpenVista's requirements.** Reimplement the minimal experiment with *no default URL*, manual Activity fallback, and a separately tested interactive dream. Do not copy its ADB instructions into a user flow or change device-wide settings without permission.

### 5. OTT-play Android: source import, stream security and player configuration

- [M3uParser.kt](https://github.com/open-ott-play/ottplay-android/blob/main/core/src/main/kotlin/play/ott/nativeapp/core/M3uParser.kt): distinguishes an HLS stream from a source list, parses directives and reports limits/failures.
- [ItemMediaSourceFactory.kt](https://github.com/open-ott-play/ottplay-android/blob/main/app/src/main/java/play/ott/nativeapp/playback/ItemMediaSourceFactory.kt): per-item immutable headers and request-origin checks.
- [SourceVault.kt](https://github.com/open-ott-play/ottplay-android/blob/main/app/src/main/java/play/ott/nativeapp/data/SourceVault.kt): source/credential storage concerns.
- [ChannelNavigationPlayer.kt](https://github.com/open-ott-play/ottplay-android/blob/main/app/src/main/java/play/ott/nativeapp/playback/ChannelNavigationPlayer.kt): safe rapid switching and late-response behavior.
- [M3uParser source tests](https://github.com/open-ott-play/ottplay-android/tree/main/core/src/test): parser boundary cases; [security design](https://github.com/open-ott-play/ottplay-android/blob/main/docs/security-design.md) is useful for threat modeling.

**Observed in code:** each player's HTTP factory snapshots a source's headers and removes non-public headers when the request moves to another origin. This is particularly relevant to user-supplied URLs containing credentials or private headers. Its parser delegates some work to a vendored core; **verify that component's license and provenance independently before reusing it**.

**OpenVista extraction:** separate `UserSource`, `ImportedPlaylist` and `PlayableMediaItem`; provide a deterministic import outcome (accepted, skipped, invalid, duplicate). Persist user sources without exposing credentials in logs, crash reports, screenshots or exported diagnostics. Define policies for redirects, source-specific headers and non-HTTPS URLs, then test rather than inheriting another app's defaults.

**Out of scope:** Xtream, Stalker, EPG, catch-up, IPTV subscriptions, provider accounts and DRM. They are not part of the approved first OpenVista playback slice.

### 6. Official Android Storage Samples: user-selected USB/internal files

- [ActionOpenDocumentTree](https://github.com/android/storage-samples/tree/main/ActionOpenDocumentTree): scoped folder selection.
- [SafDemos](https://github.com/android/storage-samples/tree/main/SafDemos): SAF provider/consumer patterns.
- [Official Android SAF guidance](https://developer.android.com/training/data-storage/shared/documents-files): authoritative platform restrictions.

**OpenVista extraction:** use a platform folder picker where supported; persist only the URI grants received, discover nested media on a background dispatcher, bound traversal/dedup, and tolerate unplugged drives or revoked access. **The samples repository is archived:** use it as a readable API example, not proof that the Hisense system picker supports the desired flow.

### 7. Strix: low-RAM import and playback design

- [M3uParser.kt](https://github.com/isnow-git/strix/blob/main/core/network/src/main/kotlin/dev/strix/core/network/playlist/M3uParser.kt): lazy line-by-line parsing.
- [PlaylistImporter.kt](https://github.com/isnow-git/strix/blob/main/core/data/src/main/kotlin/dev/strix/core/data/PlaylistImporter.kt): persistence boundary.
- [TvBufferConfig.kt](https://github.com/isnow-git/strix/blob/main/core/player/src/main/kotlin/dev/strix/core/player/config/TvBufferConfig.kt): buffering as an explicit configuration.
- [StreamPlaybackController.kt](https://github.com/isnow-git/strix/blob/main/core/player/src/main/kotlin/dev/strix/core/player/playback/StreamPlaybackController.kt): source switching.
- [ADR-0004](https://github.com/isnow-git/strix/blob/main/docs/adr/0004-media3-over-custom-player.md): rationale for Media3.

**Observed in code:** its parser yields one channel at a time instead of creating an in-memory channel list. This is a useful pattern for large imports, but the source itself should still be audited for malformed metadata, memory caps, and URL validation. Strix is early-stage and has limited independent adoption; **evaluate an idea, not a performance guarantee**.

**OpenVista extraction:** off-main-thread list parsing, bounded import sizes, batched persistence, a single active decoder, cancellation of stale source switches and metrics on the Hisense.

### 8. Jellyfin Android TV: mature structure, not a transplant candidate

- [TV app](https://github.com/jellyfin/jellyfin-androidtv/tree/master/app)
- [Media3 playback module](https://github.com/jellyfin/jellyfin-androidtv/tree/master/playback/media3)
- [Playback modules](https://github.com/jellyfin/jellyfin-androidtv/tree/master/playback)

**OpenVista extraction:** read how a long-lived TV client separates selection, playback engine, view state and controls. Use the architecture review to design error/resume state boundaries. Jellyfin is server-centered and much broader than OpenVista; never recreate its server dependency or copy GPL implementation.

### 9. TV Focus Navigation Kit: specialized D-pad failures

- [Bring into view](https://github.com/iFan6oy/tv-focus-navigation-kit/blob/main/docs/bring-into-view.md)
- [Cross-zone focus](https://github.com/iFan6oy/tv-focus-navigation-kit/blob/main/docs/cross-zone-focus.md)
- [Restore on Back](https://github.com/iFan6oy/tv-focus-navigation-kit/blob/main/docs/restore-on-back.md)
- [Hero scroll race](https://github.com/iFan6oy/tv-focus-navigation-kit/blob/main/docs/hero-scroll-race.md)

**OpenVista extraction:** ensure the remote can always reach each visible control, restore focus after leaving a scene, and avoid focus lost during lazy-list scrolling. This is a compact illustration, not a substitute for real-device TV navigation tests.

### 10. MapLibre Native: optional map only after source data exists

- [Android SDK](https://github.com/maplibre/maplibre-native/tree/main/platform/android)
- [Android license](https://github.com/maplibre/maplibre-native/blob/main/platform/android/LICENSE.md)
- [OpenStreetMap tile usage policy](https://operations.osmfoundation.org/policies/tiles/)

**OpenVista extraction:** display user-supplied cameras **only when valid location metadata exists**. Keep unlocated cameras visible in list view. Determine map SDK binary/memory cost on Fire OS, remote-controlled focus/pan/zoom semantics and legally usable tile hosting. BSD licensing of a renderer does **not** grant free or unlimited map tiles.

### 11. OwnTV: remote-first playback UX case study

- [FocusTrap.kt](https://github.com/ahXN00/OwnTV/blob/main/app/src/main/java/tv/own/owntv/ui/components/FocusTrap.kt)
- [ChannelListOverlay.kt](https://github.com/ahXN00/OwnTV/blob/main/app/src/main/java/tv/own/owntv/features/shell/components/ChannelListOverlay.kt)
- [PlayerHud.kt](https://github.com/ahXN00/OwnTV/blob/main/app/src/main/java/tv/own/owntv/player/PlayerHud.kt)
- [DirectTune.kt](https://github.com/ahXN00/OwnTV/blob/main/app/src/main/java/tv/own/owntv/player/DirectTune.kt)

**OpenVista extraction:** observe short remote workflows and immediate channel/scene switching. OpenVista should remain ambient-first and simpler. Avoid adopting multi-engine video playback, TV guide, subscriptions, sports, or deep IPTV administration. Do not directly reuse GPL code.

### 12. VLC Android: escalation-only compatibility reading

- [Official Android client](https://github.com/videolan/vlc-android)
- [libVLC integration overview](https://github.com/videolan/vlc-android/blob/master/README.md)
- [Official LibVLC sample project](https://code.videolan.org/videolan/libvlc-android-samples)

**OpenVista extraction:** consult only if Media3 fails on a *specific reproducible media format* after checking server-side source properties, codec support, container correctness, network behavior and player logs. A second native engine costs APK size, lifecycle complexity, memory and licensing review. Do not add one preemptively.

## Concrete OpenVista architecture to prototype

Keep the initial implementation minimal and original, with independently testable boundaries:

```text
ManualFullScreenActivity
  ├── TV navigation / focus / settings
  ├── AmbientSceneScreen
  │   ├── Stable video surface (Media3)
  │   └── Clock + weather overlays (independent UI state)
  └── SceneController
       ├── SourceRepository (user-added URLs and imported lists)
       ├── LocalMediaRepository (SAF URIs, not arbitrary disk paths)
       ├── PlaylistSequencer (shuffle, rotation, favorites)
       ├── PlayerController (one decoder, typed errors, retry, release)
       └── SettingsRepository (local only)

OptionalDreamService (after real-device P0 proof)
  └── uses equivalent scene/controller behavior, with its own OS lifecycle
```

Do not commit to a large multi-module implementation before small prototypes establish these seams. Keep overlays independent of Media3 so repositioning text does not reconstruct the video player.

### P0-A: TV shell and focus

**Reference:** official TV samples, then the focus kit.  
**Build:** local debug APK with three entry cards, explicit focused states, sensible empty states, Back and OK semantics.  
**Tests:** every visible action reachable with only the remote; focus restored after returning; rapid Back/OK does not strand navigation. Test on physical Hisense, not only Compose previews.

### P0-B: authorized HLS/DASH source playback

**Reference:** Media3 main demo, then OTT-play's per-source HTTP handling.  
**Build:** add one user-entered test URL (outside the shipped APK), detect supported media type, attempt playback, show typed failure and switch sources without retaining a stale player. Default mute.  
**Tests:** working and broken HLS, DASH, extensionless endpoints, bad redirects, malformed manifests, stream freeze, 1080p vs 4K, memory/CPU/network; distinguish an unsupported decoder from a failed request. Preserve source-specific authorization confidentiality.

### P0-C: USB/local video access

**Reference:** official storage samples and platform SAF guidance.  
**Build:** picker, recursive file listing in granted tree, one local playable entry, safe handling of unplugged storage.  
**Tests:** Android 9/Fire OS picker availability, URI grants across restart, nested folders, missing/deleted files, media metadata and codec errors. Do not require blanket storage permissions.

### P0-D: optional system screensaver

**Reference:** official `DreamService` API, hauxir prototype, then Aerial Views' architecture.  
**Build:** standalone test Activity and independent DreamService experiment with no bundled stream.  
**Tests:** Fire OS exposes/permits selection or does not; key delivery, exit/Back, screen sleep and return to launcher, media resource cleanup. **No system-wide settings/ADB change in this research PR.** If DreamService is unavailable, regular manual full-screen mode is the supported fallback.

### P1: imported lists, sequencing and resilience

**Reference:** OTT-play M3U parsing, Strix lazy parser, Jellyfin lifecycle concepts.  
**Build:** a source-list import contract and tests, playlist changes, default shuffled camera rotation, bounded failed-source skipping, local-video resume, and full offline fallback.  
**Tests:** enormous/malformed list protection, HLS-manifest-versus-M3U-list distinction, duplicates, secret-bearing URLs, cancel during import, invalid-all-sources termination, network drop/recovery, sleep/wake, 24-hour target-device soak when ready.

### P2: weather, overlays, map and optional onboarding

**Reference:** Aerial Views as design observation, MapLibre SDK for maps, official weather-provider contract for forecast fields.  
**Build:** locally stored weather preferences, 7-day display once a permitted provider is selected, overlay placement/anti-burn-in logic, user-geotag-only map. LAN QR entry must pass a separate security proof with explicit on-TV confirmation.  
**Tests:** no weather/location permissions by default, stale/offline weather behavior, font/focus legibility, GPU/memory overhead, map attribution/tile terms, no invented geographic coordinates.

## Import and playback security checklist

These are **acceptance questions for original OpenVista code**, not endorsements of every upstream implementation:

1. Can an M3U *channel playlist* be distinguished from an HLS *media manifest* without consuming arbitrary amounts of RAM?
2. Are list count/bytes/line lengths, imported metadata sizes, URI schemes, redirects and HTTP response sizes bounded?
3. Are user-specific headers scoped to the right source/origin and removed on unsafe cross-origin requests/redirects?
4. Can diagnostics report the failing stage while removing credentials, tokens, full private URLs, local paths and location identifiers?
5. Do failed sources use bounded retries, backoff and a clear terminal/fallback state rather than infinite loops?
6. Are source selection and asynchronous decode callbacks generation-scoped so stale responses cannot replace the currently selected scene?
7. Are persisted SAF grants handled without broad file permissions? Does unplugging storage end playback cleanly?
8. Does the final APK contain **zero** built-in streams, camera feeds, live source lists, media fixtures, test URLs or bundled user content?
9. Are network, decoder and UI resources stopped at the appropriate Activity/DreamService lifecycle boundary?
10. Is the implementation testable without a bundled content provider and without pre-configured network access?

## Licensing and reuse rules

OpenVista currently uses **MIT**. A repository's public visibility is not a license grant.

- **Apache-2.0** (Android TV samples, Media3, Strix, archived storage samples, focus kit): copying permitted under its conditions, including retaining notices and attribution where applicable. Prefer original code with documented attribution.
- **MIT** (hauxir, OTT-play root): generally compatible with MIT, but preserve notices and check individual submodules, bundled third-party dependencies, assets and generated/vendor code.
- **BSD-2-Clause** (MapLibre Native): generally compatible; retain required notices. Map tiles, styles, icons and geographic datasets can have **separate** terms.
- **GPL-2.0 / GPL-3.0** (Jellyfin Android TV, VLC Android, Aerial Views, OwnTV): study behavior, architecture and public documentation, but do **not** copy source or transplant GPL classes into a project distributed as MIT-only. Any decision to incorporate GPL code would require a separate product-wide license/compliance review.
- **Content is separately licensed:** even a permissively licensed app does not grant permission to redistribute the feeds, videos, preview images or artwork it displays.
- Before importing **any** upstream code, pin a reviewed commit SHA, record the copied paths, verify per-file notices and dependencies, and produce the corresponding license/NOTICE attribution.

This is engineering guidance, not a legal opinion.

## Recommended implementation order and handoff

| Step | Practical action | Upstream starting point | Deliverable |
| --- | --- | --- | --- |
| 1 | Establish Windows SDK/JDK and read-only Hisense capabilities | Official Android samples + Media3 | Reproducible local baseline for [issue #4](https://github.com/murilaiooo/openvista/issues/4) |
| 2 | Build original fullscreen TV shell | `android/tv-samples` | Debug Activity navigable by D-pad |
| 3 | Add one-source Media3 player | `androidx/media/demos/main` | HLS/DASH/codec/error evidence for [issue #5](https://github.com/murilaiooo/openvista/issues/5) |
| 4 | Validate folder picker + USB | `android/storage-samples` | SAF permission and disconnected-device proof for [issue #9](https://github.com/murilaiooo/openvista/issues/9) |
| 5 | Prove isolated DreamService viability | `hauxir`, Aerial Views concepts | Supported/unsupported Fire OS evidence for [issue #2](https://github.com/murilaiooo/openvista/issues/2) |
| 6 | Implement robust user-managed source import | `ottplay-android`, Strix | Tested source-list parser and zero-bundled-contents package scan for [issue #3](https://github.com/murilaiooo/openvista/issues/3) |
| 7 | Add playlists, overlays, preferences, local fallback | Aerial Views concepts, TV samples | Focus/playback/offline acceptance tests |
| 8 | Evaluate weather/maps only with provider rights and device measurements | MapLibre and licensed weather docs | Written decision for [issue #6](https://github.com/murilaiooo/openvista/issues/6) |

**Proposed first development task:** the local **manual Activity + Media3 single-HLS playback** vertical slice, after the read-only device/toolchain baseline. It gives immediate proof of Kotlin toolchain, hardware decoding, remote navigation and stream error handling without committing to a screensaver strategy or a catalog.

## Explicitly avoid

- Forking Aerial Views or OwnTV as the foundation: licensing and product-scope mismatch.
- Building an IPTV-service client, scraper, channel directory or provider marketplace.
- Shipping any external webcam URLs, sample media lists or downloaded scenic footage in the app.
- Copying hauxir's bundled default feed or its noninteractive DreamService behavior.
- Assuming silent APK installation, unrestricted screensaver selection, full USB access, all 4K codecs, or universal source compatibility.
- Adding LibVLC/MPV fallback without a measured Media3 limitation.
- Treating third-party README claims, PC-only decoding or unit tests as successful Hisense verification.

## Research limitations and revalidation

This report examined public repository metadata, trees, representative source files and maintainers' documentation on **October 25, 2026**. It is **not** a formal full-code security audit and none of these repositories was built/tested locally for this report. Repository heads, licenses, dependencies, links and SDK behavior may change. Before writing or copying code, pin upstream revisions, audit the exact files and compile/test the selected approach on the target device. Release decisions remain governed by OpenVista's [v1 acceptance contract](../product/v1-acceptance.md).
