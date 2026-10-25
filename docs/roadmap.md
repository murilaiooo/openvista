# OpenVista roadmap

**Status 2026-10-25:** eight product rounds are **complete**, totaling **78 confirmed owner decisions**. No application code or APK has been built. Engineering experiments are next, **not another generic product interview**.

## Phase 0: product definition — COMPLETED

- [x] Create GitHub repository, choose Matt Pocock skills and document legal/security constraints.
- [x] Record product interview rounds 1–8: D-01 through D-78.
- [x] Confirm **local-only first test APK**, initial test solely on Hisense 58U6HF/Fire OS 7, later GitHub publication only after approval.
- [x] Confirm stable-v1 requirement of **25+ permitted, family-appropriate working webcams**, HLS/DASH, offline list, same-LAN approved QR pairing and local on-demand diagnostics.
- [x] Record automatic download/install preference **as OS-conditional**, with system confirmation when the installer requires it.
- [x] Document [v1 acceptance gates](product/v1-acceptance.md) and [prioritized engineering plan](implementation/priority-plan.md) (relative link from docs directory).
- [ ] Validate actual technical feasibility; do **not** mark research tasks completed based solely on documentation.

## Phase 1: P0 research and physical-device spikes

- [ ] Record read-only Hisense device/OS and Windows Android toolchain baseline.
- [ ] Prototype DreamService, app-mode, idle interval and OK/Back/D-pad input feasibility without invasive OS changes.
- [ ] Validate internal/USB folder picker, recursive scans and URI permission persistence.
- [ ] Measure Media3 HLS/DASH and local MP4/MKV/WebM/MOV decoding on Hisense at 1080p/4K, including HDR limitations.
- [ ] Research official permission/attribution for **25 or more actually playable cameras** and source health checks; release gate not met until evidence exists.
- [ ] Verify weather/map data licenses and provider rate/caching rules; prototype map tiles offline-to-list fallback.
- [ ] Investigate Fire OS 7 PackageInstaller: automatic update staging, system approval requirements, signature continuity; no claim of silent install before proof.
- [ ] Threat-model and prototype short-lived local Wi-Fi QR pairing with explicit on-TV confirmation.

## Phase 2: P1 local app vertical slices

- [ ] Kotlin native Android TV app shell with Home Cards, D-pad and PT-BR/EN, build **local-only APK** for tests.
- [ ] Stable Media3 playback of legally permitted HLS/DASH and device-compatible local media, mute-first and fill/crop.
- [ ] SAF/USB local library and playlists, recursive scanning, resume, fixed ±10 sec seeks, next/loop and corrupt-file skip.
- [ ] Camera catalog and filters, versioned remote metadata with local fallback; only rights-verified reviewed sources.
- [ ] 2D map with clusters and list, and clearly described local list if offline.
- [ ] Weather/current and detailed 7-day widgets, presets, simple/advanced editor, consented location and anti-static behavior.
- [ ] Favorites/playlist management and Basic/Advanced settings persisted automatically **on device only**.
- [ ] OK quick mode switching, 5-second controls, diagnostic panel and skippable onboarding.
- [ ] Network outage to configured local playlist and verified return after **current** video ends (unless user remains local).

## Phase 3: P2 OS integration, update safety and hardening

- [ ] Integrate DreamService only where experimentally supported; display honest limitations.
- [ ] Secure LAN QR custom camera entry only after threat-model check; TV keyboard as fallback.
- [ ] Build safe GitHub-release updater with **automatic download and install only where OS supports it**; otherwise use mandatory user install confirmation.
- [ ] Redacted local diagnostic logs with user-controlled copy/share, **no automated external telemetry**.
- [ ] Privacy, storage, perf, cancellation and reconnection policies validated.

## Phase 4: P3 stable-v1 release gate

- [ ] Document at least **24 hours** of real Hisense stability evidence, CPU/RAM/frame drops/network, suspend/wake and fault injection.
- [ ] Demonstrate **25+ authorized working family-safe webcams**, attribution and source-review/takedown process.
- [ ] Complete playback, licensing, privacy, accessibility and device-compatibility acceptance matrix.
- [ ] Mark untested Android TV/Google TV/Fire TV versions **not verified**, not “compatible.”
- [ ] Prepare signed APK and reproducible build docs; **ask owner before publishing any first GitHub release binary**.

## Future releases (deferred)

- Integrated downloadable video wallpapers, RTSP, compatibility claims for other TV models, cloud sync/settings import-export.
- Any wider scope requires a new explicit decision, not an implicit assumption.

**Execution guide:** [prioritized implementation plan](implementation/priority-plan.md) and [v1 acceptance](product/v1-acceptance.md). No actual tests/builds have yet been executed.
