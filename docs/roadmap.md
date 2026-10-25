# OpenVista roadmap

**Status 2026-10-25:** eight product rounds are **complete**, totaling **78 confirmed owner decisions**. No application code or APK has been built. Engineering experiments are next, **not another generic product interview**.

## Phase 0: product definition — COMPLETED

- [x] Create GitHub repository, choose Matt Pocock skills and document legal/security constraints.
- [x] Record product interview rounds 1–8: D-01 through D-78.
- [x] Confirm **local-only first test APK**, initial test solely on Hisense 58U6HF/Fire OS 7, later GitHub publication only after approval.
- [x] Record eight rounds of decisions and the later **C-01 clarification:** users supply streams/playlists/videos, and OpenVista ships **no media sources**. HLS/DASH, offline list, same-LAN QR pairing and on-demand diagnostics remain planned.
- [x] Record automatic download/install preference **as OS-conditional**, with system confirmation when the installer requires it.
- [x] Document [v1 acceptance gates](product/v1-acceptance.md) and [prioritized engineering plan](implementation/priority-plan.md) (relative link from docs directory).
- [ ] Validate actual technical feasibility; do **not** mark research tasks completed based solely on documentation.

## Phase 1: P0 research and physical-device spikes

**Current content model:** user-supplied camera/list sources and local media. See [C-01](product/user-managed-content.md). Research-only test streams are never built into the app.

- [ ] Record read-only Hisense device/OS and Windows Android toolchain baseline.
- [ ] Prototype DreamService, app-mode, idle interval and OK/Back/D-pad input feasibility without invasive OS changes.
- [ ] Validate internal/USB folder picker, recursive scans and URI permission persistence.
- [ ] Measure Media3 HLS/DASH and local MP4/MKV/WebM/MOV decoding on Hisense at 1080p/4K, including HDR limitations.
- [x] Import **59 camera candidates / 60 direct HLS/DASH links** to quarantined machine-readable metadata, keeping publication disabled.
- [ ] Verify user-added HLS/DASH, list import, empty state and absence of third-party stream URLs/videos in the built APK. Research camera URLs remain external testing inputs only.
- [ ] Verify weather/map data licenses and provider rate/caching rules; prototype map tiles offline-to-list fallback.
- [ ] Investigate Fire OS 7 PackageInstaller: automatic update staging, system approval requirements, signature continuity; no claim of silent install before proof.
- [ ] Threat-model and prototype short-lived local Wi-Fi QR pairing with explicit on-TV confirmation.

## Phase 2: P1 local app vertical slices

- [ ] Kotlin native Android TV app shell with Home Cards, D-pad and PT-BR/EN, build **local-only APK** for tests.
- [ ] Stable Media3 playback of legally permitted HLS/DASH and device-compatible local media, mute-first and fill/crop.
- [ ] SAF/USB local library and playlists, recursive scanning, resume, fixed ±10 sec seeks, next/loop and corrupt-file skip.
- [ ] User-managed source library, direct HLS/DASH entry, optional playlist-list imports, local metadata fallback, remote-control-friendly filters and empty state. **No preloaded streams.**
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
- [ ] Demonstrate empty library, user-provided source-list import and camera playback, then prove **no third-party media or research URLs** are included in signed release bundles.
- [ ] Complete playback, licensing, privacy, accessibility and device-compatibility acceptance matrix.
- [ ] Mark untested Android TV/Google TV/Fire TV versions **not verified**, not “compatible.”
- [ ] Prepare signed APK and reproducible build docs; **ask owner before publishing any first GitHub release binary**.

## Future releases (deferred)

- Integrated downloadable video wallpapers, RTSP, compatibility claims for other TV models, cloud sync/settings import-export.
- Any wider scope requires a new explicit decision, not an implicit assumption.

**Execution guide:** [prioritized implementation plan](implementation/priority-plan.md), [P0 blocking graph](implementation/critical-path.md), [v1 acceptance](product/v1-acceptance.md), [78-ID traceability](product/requirement-traceability.md), and [planning audit](reviews/2026-10-25-planning-audit.md). No actual tests/builds have yet been executed.
