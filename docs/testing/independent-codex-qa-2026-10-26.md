# Independent QA: Codex remaining-development candidate

**Date:** 2026-10-26
**Base main:** `ab2ef28ce5b18fabf45b9cef7f8dc5c802e66a95`
**Status:** DEVELOPMENT REVIEW ONLY. Not stable-v1 acceptance or authorization to publish.

## Scope and preservation

The QA candidate was copied from the uncommitted `codex/remaining-development` workspace into an isolated detached worktree. The owner's original working tree and untracked research fixtures were not modified. No GitHub Actions were run. The candidate includes storage identity/migration, playback, Dream lifecycle, weather/map adapter source, LAN pairing and updater hardening, and UI changes. Security/provider publication gates remain disabled.

## Local validation

| Test | Result |
| --- | --- |
| Clean `main`: `:source-core:test :diagnostics:testDebugUnitTest :player:testDebugUnitTest :player:assembleDebug :player:lintDebug` | PASS, 84 Gradle tasks |
| Clean `main`: Python unittest discovery | PASS, 23 tests |
| Clean `main`: `validate_discovery_docs.py` | PASS, D-01–D-78 and links |
| Initial uncommitted Codex snapshot | FAIL, 2 of 22 Android unit tests |
| Repaired QA snapshot: full Gradle tests, debug APK and Lint | PASS, 84 Gradle tasks |
| Added canonical document-route deduplication regression test | PASS; Android unit-test suite now 23 tests |
| Debug APK content guard | PASS: no bundled media, M3U/DASH playlists or literal HLS/DASH endpoints |
| APK signature | PASS: Android debug v2, single signer; certificate matches the previously installed test APK |
| Manifest | INTERNET, NETWORK_STATE, WAKE_LOCK and REQUEST_INSTALL_PACKAGES; no broad storage or location permission |
| `git diff --check` | PASS |

## Findings and corrections

1. **SAF cancellation regression (reproduced):** an already-cancelled scan raised `OperationCanceledException` before its guarded scan block, contrary to its existing test. The isolated QA copy now returns `Unavailable` before any provider query in that case. In-flight cancellation still propagates to the lifecycle owner.
2. **Revoked grant test versus intentional new behavior:** `SavedFolderAccess.restore()` correctly refused access when the OS grant was absent but intentionally kept the user's selected-folder reference until explicit Forget. Revised the test to assert both the lack of read authorization and the explicit clean-up path. This was a stale test expectation, not proof of unauthorized file access.
3. **Duplicate SAF documents:** added a regression test for the same document imported via tree and direct URIs. It verifies one canonical item, favorite preservation, distinct access routes, stable playlist references and resume checkpoints across persistence/reload.

The source changes above are review candidates and have not been merged into `main`.

## Hisense 58U6HF / Fire OS 7 evidence

- Device: `AFTHA001`, Android API 28, one installed `com.openvista.prototype` application. The existing APK was replaced in-place, using its matching debug signing certificate. No other apps or launcher packages were installed or modified.
- Original app-private data and installed APK were backed up separately **outside the repository** before the schema-2 test. Backup library SHA-256 was compared with the live device's hash.
- Physical library schema migration: 44 -> 43 items, 40 -> 40 cameras, 4 -> 3 videos. No legacy item aliases, favorites, playlist references or video checkpoint references were lost in the verified local data set.
- OpenVista `OpenVistaActivity` launched successfully with app-owned foreground focus. PT-BR Home, Cameras, My Videos, Favorites, Settings and Resume controls were visible in a private 3840 x 2160 screenshot and UI hierarchy. Four D-pad direction events maintained OpenVista focus while the TV was awake. A later focus-guarded device run opened Settings with D-pad/OK, observed 12 focusable settings controls, and returned to Home with Back while retaining OpenVista focus. This is limited remote navigation evidence, not an accessibility certification.
- `screensaver_enabled=0` was observed before and after installation/navigation. The owner-disabled screensaver was **not** re-enabled. Fire OS later reported `mWakefulness=Asleep`, causing focus-guarded navigation/playback attempts to abort safely; this is **not** evidence that playback, wake scheduling or DreamService passed.
- UI dumps and screenshots contain potentially private library metadata and remain local; they are not committed or distributed.

## Explicitly unverified

- Real USB removal/reinsert, provider/grant revocation, malformed large files, complex picker flows, nested-directory edge cases on hardware.
- EN translation, full accessibility traversal, remote focus on every dialog, playlist CRUD, repeated playback switching, camera-source fallback and reconnect storms.
- Full HD HLS/H.264 and 4K DASH/HEVC test patterns visibly rendered on Hisense with HTTP segment retrieval, after correcting a missing HEVC codec field in the private MPD. A false-unavailable status at normal camera end-of-stream was reproduced, corrected in this QA branch and physically retested. Sustained 4K/HDR, dropped-frame counts, network throughput, CPU/RAM/thermal trends and 24-hour endurance remain unverified; see [the limited playback probe](hisense-fhd-4k-playback-2026-10-26.md).
- Full weather/offline behavior and worldwide city search. Worldwide Open-Meteo and OSM adapters exist in source but remain disabled pending independent legal/provider review; NWS remains the deployed U.S. adapter.
- OEM DreamService selection/idle, QR/LAN confidentiality and consent gate, Android PackageInstaller/release-channel compatibility and any non-Hisense device.
- Signed *public release* content and signing policy, privacy review and all G-01–G-10 stable-v1 gates.

## Merge/release ruling

**DO NOT MERGE OR RELEASE THIS CANDIDATE.** Tests and the measured storage migration are encouraging but not enough to verify all high-risk feature families. Complete the remaining physical and security/release gates, or split smaller independently accepted changes before promoting to `main`.
