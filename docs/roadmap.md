# OpenVista: final development and release roadmap

**Updated October 28, 2026.** Read [current engineering status](STATUS.md), [Claude's full handoff](implementation/claude-release-handoff-2026-10-27.md), and [owner decisions/acceptance](product/v1-acceptance.md). This roadmap replaces the older October 26 phase priorities; prior plans are historical.

## Completed as development implementation, not release acceptance
- [x] Owner decisions D-01..D-78 and later C-01 user-managed content policy.
- [x] Kotlin Android TV/Fire TV application, Media3 HLS/DASH playback, user-supplied M3U and local SAF video/folder import, playlists, favorites, screensaver entry point and English/pt-BR UI.
- [x] Physical Hisense scoped tests for basic playback, source lists, storage, clocks/weather and application workflows.
- [x] Initial unified 3-panel Home, atlas-style Settings and ambient overlays with measured group-level display-care movement.
- [x] NWS current/seven-day forecast plus international Open-Meteo camera weather and missing current-day viewer high completion.
- [x] Remove the unsafe PixelCopy frame-hold that froze a live-camera image; device motion verified in a short capture.
- [x] Merge the latest mainline SAF identity/Dream validation and 3 real README captures into the active redesign integration branch.
- [x] After resolving the SAF scanner cancel-contract test conflict, 35 Android player unit tests, debug APK assembly and Android Lint passed for the integrated source.

## Phase 1: precise visual and interaction finalization
- [ ] Use **all three original owner-provided design reference screenshots**, not the README's real current-state screenshots, to implement close Home / Settings / Live Camera Playback visual fidelity.
- [ ] Ensure there is **one Settings menu** with coherent subsections, no duplicated/disconnected configuration screens, persisted real controls and consistent shared dialog/focus states across all screens.
- [ ] Validate 1080p and 720p, typography/weight/spacing, background readability, large/long EN/PT-BR text, safe areas, focus/D-pad/Back and zero-source/no-weather screens.
- [ ] Verify the complete 32-position display-care cycle (four compositions with eight drift offsets), independent inactivity hiding, saved preferences and no overlap, clipping, frozen frames or distracting transitions on physical hardware.
- [ ] Address open issues #33 (camera-list visual preview), #34 (loading/error states), #36 (design) with real acceptance evidence.

## Phase 2: performance, reliability and regression proof
- [ ] Profile cold/warm startup, view inflation, CPU/RAM/GPU, decoder lifecycle, network fetch/cache and background transitions. Optimize only where there is a clear practical benefit.
- [ ] Stress rapid camera changes, stream failures/timeouts, pause/resume, reconnect, rolling queues, one-decoder fallback, Dream/app ownership, muted screensaver and screen wake.
- [ ] Verify U.S. and international camera/current-viewer weather, timezones, today high/low, units and locale, provider failures, credit, cache expiry and source invalidation.
- [ ] Validate media dedup/SAF identity after fresh and upgraded installs, real USB unplug/replug, persisted and revoked permissions, local playlists, video containers/codecs and fallback.
- [ ] Add regression tests where risk is high; rerun player/source-core/diagnostics tests, Android Lint, and content guard on produced packages.

## Phase 3: public repo and signed-release gates
- [ ] Audit current Git tree **and entire history** for private stream URLs, research lists, credentials, imagery/media licensing and personal data. Unmerged cleanup PR #32 alone is insufficient for public visibility.
- [ ] Confirm licenses, clean README/screenshots, contributor documentation, source package identity, data policies, privacy notices and owner-managed content rules.
- [ ] Test and record at least 24 hours of real Hisense playback/rotation, memory, thermal and network recovery; additional TV models remain unverified.
- [ ] Independently check the **actual signed candidate** for packaged unauthorized media, stream links, dependencies and signing/installer behavior; don't mistake debug tests for a production gate.
- [ ] Obtain owner approval before changing GitHub visibility, merging the final release candidate, publishing a GitHub Release or enabling updater/LAN pairing.

## Branch and issue policy

The consolidated development source is now on `main` following [PR #37](https://github.com/murilaiooo/openvista/pull/37) and [PR #40](https://github.com/murilaiooo/openvista/pull/40). [October 28 integration QA](testing/integrated-tv-snapshot-2026-10-28.md) documents the physical debug APK hash, passing local tests/build/lint, and remaining release gates. Keep experimental YouTube embedding and older overlapping draft PRs separate. GitHub remains private, and no signed release or public APK is authorized until the owner approves after privacy/history review.