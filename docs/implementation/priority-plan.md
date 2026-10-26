# OpenVista implementation plan: ship small, prove on hardware

**Historical plan recorded 2026-10-25.** It preserves the original engineering order and should not be read as the current completion status. The native TV preview, device smoke tests and NWS adapter now exist; use [the updated roadmap](../roadmap.md) and [canonical status](../STATUS.md). The 78 owner-approved decisions and C-01 remain binding. See [the detailed sequencing review](first-apk-sequencing-review-2026-10-25.md) and [critical path](critical-path.md) for historical engineering rationale.

## Invariants

- The first diagnostic and player APKs are **local test artifacts** for **Hisense 58U6HF / Fire OS 7 only**, not GitHub Releases. No other TV is verified.
- Ship zero first-party cameras, stream URLs, videos, imported research lists or cloud accounts. User-controlled HLS/DASH, playlists and local media remain core.
- Native Kotlin, Android-supported APIs, Media3, fast D-pad focus, limited RAM/CPU, explicit recoveries. No root, boot auto-start, privileged device workarounds or unapproved TV changes.
- Use small focused PRs, reproducible PC checks **and physical device observations**. No GitHub Actions required.
- QR/LAN is deferred from P0, **not cancelled**: D-32/D-73 still apply to stable v1 until explicitly changed. Never open a pairing listener without threat-model review.
- The technical criteria below are experiments or implementation sequencing, not automatic additions to D-01–D-78.

## P0: first diagnostic and shared feasibility prototype

| Order | Issue(s) | Deliverable | Evidence required |
| --- | --- | --- | --- |
| P0-A | [#4](https://github.com/murilaiooo/openvista/issues/4) | **First code:** local, one-screen, read-only diagnostics APK (separate package) after PC toolchain setup | On-device sanitized JSON with OS/API, model, display, advertised decoders, memory and picker capability. Verify no private URIs or automatic upload. ADB may collect extra read-only evidence if connected. |
| P0-B | [#3](https://github.com/murilaiooo/openvista/issues/3) | Define direct URL and **user-selected M3U from internal storage or USB**; parser threat cases and source-empty screen | Size/count/URL bounds, HLS manifest vs M3U list distinction, duplicate handling, safe persistence, no bundled feeds. TV keyboard always available. |
| P0-C | [#5](https://github.com/murilaiooo/openvista/issues/5) + [#9](https://github.com/murilaiooo/openvista/issues/9) + #3 | **One small fullscreen Activity** with Media3, direct URL, single playlist picker (`ACTION_OPEN_DOCUMENT`), video folder picker (`ACTION_OPEN_DOCUMENT_TREE`) | Distinct checklists for HLS/DASH, decoder/container/bitrate/4K, URI persistence, nested USB folder scan, unplug/replug, bad files and failure recovery. Device proof, not PC-only probe. |
| P0-D | [#2](https://github.com/murilaiooo/openvista/issues/2) | Isolated DreamService experiment reusing player after app-mode smoke test | OEM selection availability, idle intervals, OK/Back/arrows, sleep/wake and rollback; unsupported is acceptable evidence but not an automatic stable-v1 requirement waiver. |
| P0-E | [#6](https://github.com/murilaiooo/openvista/issues/6) | Weather/map terms and a tested local sun-position prototype | Seven-day provider attribution/quota/legal comparison **plus separate** offline sunrise/sunset reference cases and geolocation boundaries. |
| P0-F | [#7](https://github.com/murilaiooo/openvista/issues/7) | Fire OS 7 PackageInstaller feasibility | Document update download verification and whether Android requires confirmation; no unsupported silent install claims. |

**Not P0:** [#8 QR/LAN](https://github.com/murilaiooo/openvista/issues/8) moves to P3 because USB import and TV keyboard cover first-prototype input. This is a schedule deferral only; it still requires security proof before QR ships. Separate deliverables can be researched in parallel without blocking the first code.

## P1: locally testable application slices

1. Build the original user-facing Kotlin Android TV shell: Home cards **Cameras**, **My Videos**, **Favorites**, focus/restoration, English/PT-BR and short skippable tutorial.
2. **First-use Cameras empty state:** three large actions: **Add a stream**, **Import a list**, **Play my videos**. Picker not available or USB missing must never trap the user; no bundled sample feed.
3. Use the proven shared player/import/picker code in the app; separate source records from local video tree entries, with safe retries and privacy-preserving storage.
4. Add separate camera/local playlists, favorites, defaults (camera shuffle, five-minute rotation, adjustable 1–60), local resume, 10-second seeking, USB failure recovery, and instant/smooth transitions only within decoder capability.
5. Add map for user-geotagged sources, local list when offline, licensed current/7-day weather, compact overlays and a D-pad-usable simple/advanced editor.
6. Keep settings local only, technical diagnostics optional, controls hide after **five seconds** of inactivity, local fallback user-configured and restoration deferred until current local video ends.

Each slice needs reproducible PC tests and on-device behavior evidence. Small code size is desirable, but never replace correctness with an arbitrary 500-line limit.

## P2: optional experience and device integration

- Investigate **Daylight-only** and approximate **Golden Hour** filters for sources with user-provided coordinates. Solar elevation is derived locally and displayed as an estimate, never as real-time camera image analysis. Unknown location and polar day/night have honest states.
- Prototype optional **Display care** for static clock/weather widgets: bounded repositioning, individually configured hide/fade and optional dimmed overlay appearance. Do not promise burn-in prevention or change system brightness automatically.
- Integrate DreamService only where the OS supports the user-selected service; provide manual app mode on unsupported devices.
- Add verified-release updater with Android-controlled install confirmation where required; local diagnostics only, optional deliberate report share, no automatic telemetry.

The UX concepts live in [ambient experiments](../product/ambient-experiments.md). Product experiments are not automatically mandatory v1 requirements.

## P3: security and stable-v1 release gate

- Implement and validate **QR/LAN pairing** only after threat-model review (#8), with explicit TV approval, short-lived pairing, local-only transport and rejection of untrusted requests. D-32/D-73 remain owner-approved. **Deferring QR beyond stable v1 needs a new owner decision; this plan does not grant one.**
- Run [24-hour Hisense soak](../testing/hisense-24h-soak-protocol.md), measure CPU/RAM/dropped frames/recovery and compare against numeric predeclared thresholds.
- Verify USB and source failure paths, privacy, user-provided content, licensed weather/map, supported accessibility, and zero bundled sources in the signed release bundle.
- Record tested hardware exactly, complete [G01–G10 acceptance](../product/v1-acceptance.md), then request owner approval before a GitHub APK release.

**Excluded unless separately approved:** procedural built-in demo/shader/video, RTSP, account/cloud sync, wallpaper download library and compatibility claims about devices not tested.

## Review checklist

Link issue -> code/PR -> tests -> platform/OS result -> release gate. Check code for regressions, reviewable architecture and readable documentation with [quality guidance](../quality/writing-and-code-review.md). Preserve uncommitted local work and avoid unrelated edits. Some tasks overlap at the code level without losing separate acceptance evidence.
