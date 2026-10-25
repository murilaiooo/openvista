# OpenVista roadmap

All milestones are **proposed**, not scheduled. Product interview must happen before implementation.

## Phase 0: Product definition (NOW)
- [x] Create GitHub repository and record high-level product intent.
- [x] Select/install relevant Matt Pocock skills for Codex and Claude Code.
- [x] Document architecture hypotheses, reference sources, content-rights checks, glossary, workflow and open questions.
- [x] Record round 1 product decisions (8 confirmed choices).
- [x] Record round 2 user-experience decisions (10 confirmed choices).
- [x] Record round 3 visual and TV-interaction decisions (10 confirmed choices).
- [x] Record round 4 catalog, weather and wallpaper decisions (10 confirmed choices).
- [x] Record round 5 runtime, energy and performance decisions (10 confirmed choices).
- [x] Record round 6 player, playlist and local-file decisions (10 confirmed choices).
- [x] Record round 7 settings, controls and recovery decisions (10 confirmed choices).
- [ ] Complete round 8 first-release boundary decisions; resolve remaining product/architecture gates.
- [ ] Verify [first-release blockers and proofs](product/first-release-blockers.md); define a testable version-1 acceptance spec without weakening the 68 approved decisions.
- [ ] Choose technical baseline after comparing original Kotlin project vs license-compliant reuse/fork.

## Phase 1: Feasibility spikes
- [ ] Validate AndroidX Media3 local MP4 and permissioned live HLS on TV.
- [ ] Validate OS/document-picker URI permission persistence, including external storage.
- [ ] Test a basic DreamService and normal app-mode on representative Android TV, Google TV, Fire TV.
- [ ] Prototype supported idle intervals (1/3/5/10/15/30 min), OK/Back/arrows in DreamService, no boot autostart and constrained keep-awake/schedules.
- [ ] Prototype last-scene persistence, network-to-local fallback with non-disruptive reconnect probes and USB hot-unplug recovery.
- [ ] Verify weather service licensing, forecast accuracy and data caching policy.
- [ ] Document decoder limits, memory, CPU and bitrate findings, including automatic quality/effect degradation and paused background resource usage.

## Phase 2: Vertical MVP (scope subject to interview)
- [ ] Fullscreen ambient playback with source switching/failover.
- [ ] Local internal/USB folder selection with **recursive subfolder discovery**, MP4/MKV/WebM/MOV candidate recognition and supported-codec detection.
- [ ] Separate camera/video playlists; create, rename, reorder, duplicate and delete; batch add/remove/reorder playlist items. Camera playlists **shuffle by default** but allow sequence; automatically advance/repeat local videos.
- [ ] Fixed ±10-second local seeks; persistent last-video seek position on manual/Dream reopen; no local subtitle feature.
- [ ] Stable decode/fallback and bounded skip-on-corrupt/unsupported-media handling with discreet alerts and optional details.
- [ ] Live source management with legal attribution, verified availability, broad themed categories, remote catalog updates/local fallback, and reviewable community suggestions.
- [ ] TV-keyboard custom URL input and secure QR-assisted input, design validated before implementation.
- [ ] Opt-in all-camera-failed fallback to a user-selected local video playlist, with visible retry when unavailable.
- [ ] Clock, current weather and expanded seven-day forecast UI with supported temperature, rain, wind, humidity, sunrise and sunset; manual city search plus optional device detection.
- [ ] TV D-pad focus/navigation, onboarding, preferences, accessibility.
- [ ] Basic categorized Settings plus separate Advanced; Favorites sections for cameras/videos; local-only autosave without import/export or account sync.
- [ ] OK menu provides quick Cameras/My Videos mode switch; auto-hides after 5 seconds; optional technical diagnostics.
- [ ] On reconnect during local fallback, finish playing video then return to an available camera, unless user elects to stay local.

## Phase 3: Release quality
- [ ] Long-running soak tests, suspend/resume, network loss, malformed media, low-memory conditions.
- [ ] Multi-device compatibility matrix and clear unsupported configurations.
- [ ] Security/privacy review, licenses and trademark review.
- [ ] Release APK, changelog, installation/uninstall docs, reproducible builds.

## Future: ecosystem
- [ ] Evaluate legal, curated downloadable wallpaper library integration **after the first APK**.
- [ ] Explore additional community catalogs/providers and optional future preferences transfer only after explicit product approval; **no settings sync, import/export or backup in initial release**.
- [ ] More playback types/features only after performance evidence and user validation.

No app code, artifact or store release exists at this stage.
