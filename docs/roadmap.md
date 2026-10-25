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
- [ ] Complete round 5 and remaining interviews; resolve follow-up ADRs.
- [ ] Choose technical baseline after comparing original Kotlin project vs license-compliant reuse/fork.

## Phase 1: Feasibility spikes
- [ ] Validate AndroidX Media3 local MP4 and permissioned live HLS on TV.
- [ ] Validate OS/document-picker URI permission persistence, including external storage.
- [ ] Test a basic DreamService and normal app-mode on representative Android TV, Google TV, Fire TV.
- [ ] Verify weather service licensing, forecast accuracy and data caching policy.
- [ ] Document decoder limits, memory, CPU and bitrate findings.

## Phase 2: Vertical MVP (scope subject to interview)
- [ ] Fullscreen ambient playback with source switching/failover.
- [ ] Local folder selection, video catalog, playback order, repeat.
- [ ] Live source management with legal attribution, verified availability, broad themed categories, remote catalog updates/local fallback, and reviewable community suggestions.
- [ ] TV-keyboard custom URL input and secure QR-assisted input, design validated before implementation.
- [ ] Opt-in all-camera-failed fallback to a user-selected local video playlist, with visible retry when unavailable.
- [ ] Clock, current weather and expanded seven-day forecast UI with supported temperature, rain, wind, humidity, sunrise and sunset; manual city search plus optional device detection.
- [ ] TV D-pad focus/navigation, onboarding, preferences, accessibility.

## Phase 3: Release quality
- [ ] Long-running soak tests, suspend/resume, network loss, malformed media, low-memory conditions.
- [ ] Multi-device compatibility matrix and clear unsupported configurations.
- [ ] Security/privacy review, licenses and trademark review.
- [ ] Release APK, changelog, installation/uninstall docs, reproducible builds.

## Future: ecosystem
- [ ] Evaluate legal, curated downloadable wallpaper library integration **after the first APK**.
- [ ] Optional collection sync / community catalogs / multiple providers.
- [ ] More playback types/features only after performance evidence and user validation.

No app code, artifact or store release exists at this stage.
