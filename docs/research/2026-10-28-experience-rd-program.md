# OpenVista: experience R&D program (research only)

**Created:** 2026-10-28  
**Coordinator:** [Research epic #42](https://github.com/murilaiooo/openvista/issues/42)  
**Status:** Proposals awaiting validation. This document is **not** a feature approval, implementation mandate, release commitment, or change to D-01–D-78/C-01.  
**Baseline:** [STATUS](../STATUS.md), [Roadmap](../roadmap.md), [User-managed content](../product/user-managed-content.md).

## Mission and hard limits

Investigate owner-selected proposals **1, 2, 5, 6, 7, 10, 11, 12, 13, 18, 19, 21, 22, 23**, plus Google Photos, YouTube and higher-coverage stream protocols. Distinguish user benefit from novelty, provide technical proof, and reject nonviable concepts. Keep the three approved Home destinations (Cameras / My Videos / Favorites), fullscreen scenic player, local source ownership, free/ad-free/no-required-account product, English/pt-BR localization, and stable one-decoder-first playback. New navigation and settings taxonomy are proposals, not adopted UI.

**Validation context:** First target is Hisense Fire OS 7 / Android 9 with Media3. The existing 24-hour soak, lifecycle, decoder and design gates take priority; no new feature should mask or displace them. No owner-driven testing is available for this research. Source-only tests, emulation, automated tests, synthetic test services and independent testing may provide preliminary evidence but must be labeled honestly. Hardware acceptance must remain pending without real target-device evidence.

## Decision gates

Every feature passes through **Discover → Evidence → Prototype in isolation (if lawful) → Automated QA → Independent device validation → Owner decision → implementation issue**. Evidence level is one of E0 (idea only), E1 (documented primary-source feasibility), E2 (prototype/tests), E3 (real compatible device validation), E4 (long-duration regression/production evidence). **Do not equate E1/E2 with approved or shipped.**

A GO requires (a) demonstrable user value, (b) technically viable on target hardware, (c) lawful/terms-compatible sources and APIs, (d) privacy-security review, (e) measurable performance without destabilizing playback, and (f) explicit owner approval. HOLD means missing evidence or partnership/device dependence. NO-GO means a hard conflict with product principles or provider policies. If a proposed feature needs mandatory accounts/ads/cloud subscriptions, document it as an optional or rejected alternative rather than silently changing the product.

## Research backlog and testable hypotheses

| ID | Feature | User problem / research hypothesis | Candidate implementation | Falsifiable evidence and stop conditions |
| --- | --- | --- | --- | --- |
| R01 | **Smart Director** (former #1) | Fixed shuffle ignores time, quality and preferences. Better scene sequencing may increase good uninterrupted viewing time. | Deterministic, explainable local selector using saved metadata, availability, favorites, solar context and explicit opt-in preferences. Integrate downstream of queue selection, not into decoder. | Compare 30+ synthetic camera profiles against baseline shuffle for repeats, dead sources, timezone bias, determinism and user overrides; reject if selection causes perceptible switching delay or opaque/inaccurate rankings. |
| R02 | **Sun Chaser** (#2) | Users want daylight, sunrise and sunset among their *own* geotagged cameras. | Offline solar-elevation/sunrise/sunset library with a daylight/twilight/golden-hour filter; unknown coordinates remain explicitly unknown and selectable. | Cross-check astronomical outputs across regions, DST, polar day/night and date boundaries; show solar *estimate*, never assert the image is sunny or pointed at sun. Related existing research: [ambient experiments](../product/ambient-experiments.md). |
| R05 | **Data Monitor** (#5) | Eight-hour/day viewing makes data usage invisible. | Android TrafficStats own-UID RX/TX sampled while active, monotonic snapshots persisted with boot-reset handling; Media3 network byte estimates for stream-specific optional details. | Check own UID support, boot rollover, VPN, network changes, HLS retries, cache, midnight boundaries and separate uploads; if unsupported label unavailable, not zero. Daily/monthly estimates must disclose incomplete coverage. |
| R06 | **Live Preview** (#6) | Camera text rows are hard to choose from; issue [#33](https://github.com/murilaiooo/openvista/issues/33) already requests a preview. | Single focused preview only: first frame or licensed provider-approved thumbnail; no simultaneous grid of video decoders. Clear stale/reloading status. | Validate focus navigation, image permission, switching, memory, playback priority, absence of DRM/screenshots and real device decoder ownership. |
| R07 | **Camera Health** (#7) | Successful recovery alone does not prevent repeated selection of unstable feeds. | Small TTL/decay score from observed first-frame time, stall, failure and recent success; per-camera evidence without background bulk probing. | Simulate offline network, 403/auth, expired tokens, local devices and outages; never call globally unavailable cameras permanently broken; explicit reset and opt-out. |
| R10 | **Ambient Soundscapes** (#10) | Many user streams have no useful audio; users want a consistent optional mood. | Separate permissioned local audio track/mixer; independent volume/mute/ducking; optional self-supplied sound loops. | One-decoder/CPU impact, clock drift, lifecycle, no automatic remote downloads; respect copyrighted sounds and default muted. Separate YouTube audio is not allowed. |
| R11 | **Ambient Routines** (#11) | Basic scheduling cannot change scene profiles by time. | Local schedule engine choosing playlist/overlay/audio preset, not forcibly waking device; manual override priority. | DST, sleep/wake, timezone travel, app stopped, exact handoff, no notification or battery wake storm; do not override OEM power policy. |
| R12 | **Network Library** (#12) | TV-only/USB files limit convenient 4K personal media use. | SMB read-only / WebDAV HTTPS / self-hosted NAS files; bounded browsing, credential vault, streaming with seek support and local fallback. | NAS lab matrix (SMB versions, TLS/WebDAV, disconnect, 4K codec, seeks, credentials) and dependency footprint; avoid legacy SMB1 and shared unrestricted credentials. |
| R13 | **Subscribed M3U** (#13) | User-managed URLs change and manual reimport loses organization. | Explicit opt-in HTTPS list subscription with ETag/Last-Modified, parser security and stable identity merge; scheduled refresh only while permitted. | Preserve favorites, custom names and ordering across updates; limits on fetch size, SSRF/private IP, redirects, credentials, malformed entries and stale-feed rollback; no app-operated catalog. |
| R18 | **Discover This Place** (#18) | Camera locations lack optional context. | On-demand user-invoked details for **verified known place** via licensed open data; local cache; no guessing from stream URL. | False match rate, data attribution, privacy and rate limits; no static fullscreen info while screensaver running. |
| R19 | **Home Assistant** (#19) | Home automation enthusiasts want to control scenes. | **Optional local authenticated integration** using Home Assistant events/API or intents, with least-privilege commands and explicit allowlist. | Mock HA server, threat model, replay/rate-limit/focus, locked screen and no trusted-LAN assumption. Owner cannot test: hold hardware acceptance and seek independent testers, not require owner test. |
| R21 | **Digital Window** (#21) | Some households want the TV to look architectural, not like a video player. | Optional minimal bezel/matte overlay preset, never replacing fullscreen default; static-element budget and image-safe margins. | Visual focus/readability comparison at 720p/1080p/4K; reject heavy GPU gradients, opaque masks or static high-contrast borders creating display wear. |
| R22 | **Comfort Mode** (#22) | Long sessions and nighttime brightness can be visually intrusive. | Time-based overlay intensity/preset reduction with explicit setting; no claim software darkening adjusts TV backlight or prevents burn-in. | Confirm no RGB clipping, battery/power wakeups, high-contrast accessibility conflicts, black screens or persistent bright overlay. Preserve existing 32-position display care. |
| R23 | **Multi-TV Sync** (#23) | Households may want shared playlists/profiles across TVs. | LAN discovery with explicit user pairing; first aim for *state sync* not frame-perfect A/V sync; do not ship cloud service. | Emulated multi-peer tests + independently provided hardware; device enrollment, revoke, conflicts, version skew, key rotation, recovery, no multicast assumptions; **hold** if no realistic multi-device acceptance path. |

## Additional research streams

- **R24 Google Photos:** Ambient API is directly aligned with the TV use case but is partner-gated; Picker API allows user-chosen media with OAuth and short-lived URLs. Investigate account requirement as an optional exception only, policy review, API access and fallback to local files. See [media feasibility](2026-10-28-media-source-feasibility.md).
- **R25 YouTube Live:** Official embed-only research; YouTube does not allow removing ads or altering player playback. User's no-ad-interruptions requirement makes it **NO-GO for unattended ambient rotation unless provider-approved rights and uninterrupted authorized delivery can be proven**. No scraping/stream extraction, ad blockers, unofficial proxy or hidden player.
- **R26 Camera protocols / quality of supply:** Prioritize actual *legal, playable, high-quality sources* before adding RTSP, refreshed JPEG/MJPEG, WHEP/WebRTC, or website embeddings. Classify provider permissions and direct vs tokenized links. No sources bundled.
- **R27 App IA:** Audit all current routes, folder/detail/player, remote D-pad, Back, empty states, Settings, proposed feature entry points, and progressive disclosure. See [navigation map](2026-10-28-tv-navigation-information-architecture.md).

## Cross-feature architecture

1. **SourceAdapter**: user media descriptors and source-specific authorization; strict URI/protocol validation, never assume every webpage is a stream.
2. **AvailabilityLedger**: local, expiring observed signal for health, previews and Smart Director; do not export personal URLs.
3. **ContextEngine**: deterministic current time, solar geometry, weather-derived metadata, user opt-in selections; no mandatory AI/cloud.
4. **ScenePolicy**: one current playback owner with fallback/rotation; no background second decoder from previews or soundscapes.
5. **RoutineScheduler**: foreground/lifecycle-aware; explicit manual overrides and next-run explanations.
6. **LibraryProvider**: local SAF and optional remote storage; clearly separate source credentials and user-level permissions.
7. **IntegrationBoundary**: OAuth/third-party accounts and LAN automation disabled by default, reviewed independently.

Avoid implementing these abstractions preemptively. First prove each actually reduces duplication and fits the existing Kotlin modules.

## Priorities (research order, not implementation order)

- **P0 research:** navigation map, Data Monitor, Camera Health, Live Preview, user-source protocol feasibility; protect existing stability.
- **P1 research:** Smart Director + Sun Chaser + routines + subscribed M3U; design integration only after P0.
- **P2 research:** soundscapes, NAS, place context, comfort/window modes, Google Photos partner options.
- **P3 or gated:** Home Assistant (no owner test), Multi-TV sync (no owner test), YouTube (advertising policy incompatibility).
- Keep no requirement to implement anything; outcomes may be GO, HOLD or NO-GO.

## Completion artifacts for each investigation

An evidence dossier must contain: baseline code touchpoints, user job/story, alternatives, official links and version/date, lawful source availability by format, architecture sketch, CPU/RAM/network/security considerations, automation tests and results, missing real-TV tests, estimated maintenance costs, rollback, outcome with confidence, and proposed UI placement. A proposal may become an implementation issue **only after explicit approval**.

## References

- [Android Media3 supported formats](https://developer.android.com/media/media3/exoplayer/supported-formats)
- [Android TrafficStats](https://developer.android.com/reference/android/net/TrafficStats)
- [Google Photos Ambient API](https://developers.google.com/photos/ambient/guides/about) and [Partner access](https://developers.google.com/photos/partner-program/overview)
- [Google Photos Picker API](https://developers.google.com/photos/picker/guides/get-started-picker)
- [YouTube developer policies](https://developers.google.com/youtube/terms/developer-policies)
- [WebRTC WHEP draft](https://datatracker.ietf.org/doc/draft-ietf-wish-whep/)
- [Windy Webcams API](https://api.windy.com/webcams/docs)
