# OpenVista roadmap

All milestones are **proposed**, not scheduled. Product interview must happen before implementation.

## Phase 0: Product definition (NOW)
- [x] Create GitHub repository and record high-level product intent.
- [x] Select/install relevant Matt Pocock skills for Codex and Claude Code.
- [x] Document architecture hypotheses, reference sources, content-rights checks, glossary, workflow and open questions.
- [x] Record round 1 product decisions (8 confirmed choices).
- [x] Record round 2 user-experience decisions (10 confirmed choices).
- [ ] Complete round 3 and remaining interview rounds; resolve follow-up ADRs.
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
- [ ] Live source management with legal attribution and health checks.
- [ ] Clock, current weather and seven-day forecast UI.
- [ ] TV D-pad focus/navigation, onboarding, preferences, accessibility.

## Phase 3: Release quality
- [ ] Long-running soak tests, suspend/resume, network loss, malformed media, low-memory conditions.
- [ ] Multi-device compatibility matrix and clear unsupported configurations.
- [ ] Security/privacy review, licenses and trademark review.
- [ ] Release APK, changelog, installation/uninstall docs, reproducible builds.

## Future: ecosystem
- [ ] Evaluate legal, curated media library integration.
- [ ] Optional collection sync / community catalogs / multiple providers.
- [ ] More playback types/features only after performance evidence and user validation.

No app code, artifact or store release exists at this stage.
