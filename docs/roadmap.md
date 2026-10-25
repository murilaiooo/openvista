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

## Phase 1: P0 diagnostic APK and shared media/USB feasibility

**Current content model:** user-supplied camera/list sources and local media. See [C-01](product/user-managed-content.md). Research-only test streams are never built into the app.

- [ ] **Build the first code as a local-only read-only self-check APK:** collect user-reviewed OS/model/API/display/decoder/picker capability evidence and export sanitized JSON on explicit user action. Record the Windows toolchain and safe rollback.
- [ ] Prototype a basic fullscreen Activity/D-pad first, then independently verify DreamService selection, idle intervals and key handling without invasive OS changes.
- [ ] In the **same minimal app-mode playback prototype** as Media3, validate user-selected M3U file import, internal/USB folder picker, recursive scanning and URI permission persistence. Track distinct evidence for #3 and #9.
- [ ] In the same minimal Activity, measure Media3 HLS/DASH and permitted local MP4/MKV/WebM/MOV on Hisense at 1080p/4K, including HDR limitations (#5).
- [x] Import **59 camera candidates / 60 direct HLS/DASH links** to quarantined machine-readable metadata, keeping publication disabled.
- [ ] Verify user-added HLS/DASH, list import, empty state and absence of third-party stream URLs/videos in the built APK. Research camera URLs remain external testing inputs only.
- [ ] Verify weather/map data licenses and provider rate/caching rules; prototype map tiles offline-to-list fallback.
- [ ] Investigate Fire OS 7 PackageInstaller: automatic update staging, system approval requirements, signature continuity; no claim of silent install before proof.
- [ ] **Defer QR/LAN to P3**. USB/import and keyboard are first-APK paths; D-32/D-73 remain a stable-v1 commitment until changed explicitly.

## Phase 2: P1 local app vertical slices

- [ ] Kotlin native Android TV app shell with Home Cards, D-pad and PT-BR/EN, following the separate **local-only diagnostic APK** and shared media/USB prototype.
- [ ] Stable Media3 playback of legally permitted HLS/DASH and device-compatible local media, mute-first and fill/crop.
- [ ] SAF/USB local library and playlists, recursive scanning, resume, fixed ±10 sec seeks, next/loop and corrupt-file skip.
- [ ] User-managed source library, direct HLS/DASH entry, **user-selected M3U from internal storage/USB**, local metadata fallback and an intentional camera-empty state with **Add a stream / Import a list / Play my videos**. **No preloaded streams.**
- [ ] 2D map with clusters and list, and clearly described local list if offline.
- [ ] Weather/current and detailed 7-day widgets, presets, simple/advanced editor, consented location and anti-static behavior.
- [ ] Favorites/playlist management and Basic/Advanced settings persisted automatically **on device only**.
- [ ] OK quick mode switching, 5-second controls, diagnostic panel and skippable onboarding.
- [ ] Network outage to configured local playlist and verified return after **current** video ends (unless user remains local).

## Phase 3: P2 OS integration, update safety and hardening

- [ ] Integrate DreamService only where experimentally supported; display honest limitations.
- [ ] Evaluate optional **Daylight/Golden Hour** views for geotagged user sources and configurable display-care overlays, without promising burn-in prevention or replacing weather forecasts.
- [ ] Build safe GitHub-release updater with **automatic download and install only where OS supports it**; otherwise use mandatory user install confirmation.
- [ ] Redacted local diagnostic logs with user-controlled copy/share, **no automated external telemetry**.
- [ ] Privacy, storage, perf, cancellation and reconnection policies validated.

## Phase 4: P3 security and stable-v1 release gate

- [ ] Finish the deferred **QR/LAN security gate (#8)** before releasing a stable v1 that claims D-32/D-73; post-v1 deferral requires explicit owner approval.
- [ ] Document at least **24 hours** of real Hisense stability evidence, CPU/RAM/frame drops/network, suspend/wake and fault injection.
- [ ] Demonstrate empty library, user-provided source-list import and camera playback, then prove **no third-party media or research URLs** are included in signed release bundles.
- [ ] Complete playback, licensing, privacy, accessibility and device-compatibility acceptance matrix.
- [ ] Mark untested Android TV/Google TV/Fire TV versions **not verified**, not “compatible.”
- [ ] Prepare signed APK and reproducible build docs; **ask owner before publishing any first GitHub release binary**.

## Future releases (deferred)

- Integrated downloadable video wallpapers, RTSP, compatibility claims for other TV models, cloud sync/settings import-export.
- Any wider scope requires a new explicit decision, not an implicit assumption.

**Sequencing update:** [first-APK review](implementation/first-apk-sequencing-review-2026-10-25.md), [ambient UX experiments](product/ambient-experiments.md), and [quality review](quality/writing-and-code-review.md).

**Execution guide:** [prioritized implementation plan](implementation/priority-plan.md), [P0 blocking graph](implementation/critical-path.md), [v1 acceptance](product/v1-acceptance.md), [78-ID traceability](product/requirement-traceability.md), and [planning audit](reviews/2026-10-25-planning-audit.md). No actual tests/builds have yet been executed.
