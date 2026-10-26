# OpenVista roadmap

**Updated 2026-10-26.** Native app development and physical Hisense testing are underway. This is the **current work plan**; the older [October 25 implementation sequence](implementation/first-apk-sequencing-review-2026-10-25.md) is historical. See [current status and observed evidence](STATUS.md).

## Completed milestones (development only)

- [x] Original product decisions D-01–D-78, with C-01 superseding first-party camera-feed/catalog expectations.
- [x] Windows toolchain, Kotlin/Gradle Android source tree and independent diagnostic baseline.
- [x] User-managed HLS/DASH parser, M3U import, Home + Cameras/My Videos/Favorites, localized EN/PT-BR UI.
- [x] Media3 player architecture, local/video source entry, bounded SAF folder scanning, saved library/playlists, overlays and settings.
- [x] Physically reproduce HLS/DASH 720p, local MP4, user-selected M3U import, folder permission restoration, weather and basic TV navigation on Hisense.
- [x] Implement official U.S. National Weather Service current hourly forecast and seven-day view; physically test Tucson.
- [x] Validate debug APK build, Android tests, Python checks, Android Lint and no-bundled-stream/media guard.
- [x] Close old issues/PRs administratively at the owner's request while preserving commits and outstanding release requirements.

These are implementation and **scoped test milestones**, not public-stable acceptance.

**Unmerged QA candidate (October 26):** an isolated copy of Codex's additional storage/playback/provider/security/UI work passed local tests after an SAF cancellation fix, and migrated the Hisense's saved library with no broken legacy references in the sampled data set. The duplicate-document correction is still a review candidate, not stable USB/SAF acceptance. [Evidence and remaining gates](testing/independent-codex-qa-2026-10-26.md).

## Phase A: improve the existing development preview

- [x] Fix canonical direct-document/tree-document duplicates in the library (merged PR #25, local tests and sampled hardware migration passed; broader USB access matrix remains pending).
- [ ] Audit complete D-pad focus, Back, first-run, import/error prompts, settings, localized text and accessibility on physical Fire OS.
- [ ] Confirm the newly separated 1-minute camera and 5-minute local-video rotation intervals, resume, source switching, favorites, local playlist ordering/looping and recovery with reproducible physical evidence; settings/UI implementation is in [smart ambient QA](testing/smart-ambient-rotation-2026-10-26.md).
- [ ] Verify NWS caching, hourly-to-daily normalization, offline stale states and locale-friendly weather descriptions.
- [ ] Resolve existing TV picker and UI regressions without changes to unrelated apps or the user's screen-saver preference.

## Phase B: compatibility and provider gates

- [ ] Complete USB unplug/replug, persisted-grant revocation, nested folder traversal and video format matrix (MP4/MKV/WebM/MOV, supported codecs only).
- [x] Render controlled 1080p HLS/H.264 and 4K DASH/HEVC QA test patterns on the Hisense with captured frames and HTTP segment evidence (development only).
- [ ] Verify sustained 1080p/4K playback, real-world sources, HDR capability, decoder fallback, CPU/RAM/network/dropped-frame measurements.
- [x] Observe owner-authorized Hisense OpenVista DreamService selection and automatic idle entry, retaining reversible system settings ([evidence](testing/hisense-dreamservice-activation-2026-10-26.md)).
- [ ] Complete DreamService remote controls, sleep/wake, normal 3-minute idle and long-running lifecycle QA.
- [ ] Review/select geographically suitable, legally usable map tiles and city geocoding; keep current schematic/list fallback until then.
- [ ] Securely validate QR/LAN phone-to-TV input before enabling PairingSecurity.REVIEWED; no unreviewed HTTP exposure.
- [ ] Approve signing/update-channel policy and test Android PackageInstaller user confirmation without silent-install claims.
- [ ] Keep experimental Golden Hour/daylight filters and extra display-care variants outside v1 scope unless separately approved.

## Phase C: stable-v1 certification

- [ ] Complete all [G01–G10 acceptance gates](product/v1-acceptance.md) with repeatable evidence, not inferred coverage from closed GitHub issues.
- [ ] Execute at least 24 hours of documented physical Hisense playback/recovery/thermal/CPU/RAM/dropped-frame testing.
- [ ] Verify no packaged first-party streams, sample lists, third-party videos or credentials in the **signed release** artifact.
- [ ] Finalize privacy/security review, Android TV remote ergonomics, EN/PT-BR copy and honest non-Hisense compatibility labels.
- [ ] Have the owner approve actual signing, package identity, release notes and publication **before** creating a GitHub Release.

## Explicitly outside the first stable release

- Bundled or hosted camera streams, built-in M3U lists, video downloads, subscriptions/accounts, cloud sync and user tracking.
- RTSP and untested protocols; automatic TV boot launch; root/privileged TV modifications.
- Guaranteed 4K/HDR decoding, silent installation or compatibility with devices that have not been tested.

## Where development work lives

The current app source is on the development mainline. Earlier implementation PRs [#20](https://github.com/murilaiooo/openvista/pull/20), [#21](https://github.com/murilaiooo/openvista/pull/21), [#22](https://github.com/murilaiooo/openvista/pull/22) and [#23](https://github.com/murilaiooo/openvista/pull/23) were closed unmerged by administrative request; their commits/branches and acceptance notes remain for traceability. Historical issues were likewise closed, not necessarily delivered. New work should use focused branches and real validation evidence.

See the [implementation module guide](implementation/production-module-guide.md), [latest status](STATUS.md), [dated integrated-app QA](testing/production-pr22-validation-2026-10-26.md), and [NWS QA](testing/nws-weather-integration-2026-10-26.md).
