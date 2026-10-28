# October 27: mainline-to-redesign integration and automated QA

## Context
The owner's newest installed OpenVista debug APK corresponds to the previous `5e40e4d` redesign/weather revision. Mainline `bd6b287` independently introduced documented SAF canonical identity, DreamService activation evidence and three real screenshots. To prepare a single coherent codebase for Claude, mainline was merged into the active redesign work while preserving the video/player fixes. **This branch's newly assembled APK was not installed on the TV.**

## Merge resolution
- Two paths conflicted: `player/src/main/java/com/openvista/prototype/SafVideoScanner.kt` and `player/src/test/java/com/openvista/prototype/SavedFolderAccessTest.kt`.
- `SafVideoScanner.scan` retains mainline's explicit contract: an already-cancelled scan returns `Unavailable` **without provider queries**, while cancellation raised during an active scan propagates. Its pre-cancel Robolectric regression test now asserts that specific current contract rather than the old throw-only expectation.
- Saved folder selection metadata remains when a provider/grant disappears, but grants are never assumed valid and explicit `forget()` removes the preference. The combined test retains both guarantees.
- Existing LibraryStore and canonical SAF additions merged without manual content resolution. UI and weather/player code were not altered by these conflict fixes.

## Reproducible local verification
With Windows JDK 21 and Android SDK `C:\Users\muril\AppData\Local\Android\Sdk` configured as `ANDROID_HOME`, from the integrated worktree:

```powershell
.\gradlew.bat :player:testDebugUnitTest :player:assembleDebug :player:lintDebug --offline --console=plain --no-daemon
```

**Result: PASS**, 35 unit tests, 0 failures; debug APK assembly completed; Android Lint completed. The initial attempt without an SDK environment failed at configuration (not an app defect). The first integrated attempt with the SDK detected a contradictory old test expectation; test aligned with the reviewed mainline API, then the full command passed. No physical device testing is claimed for this newly integrated source.

This proof is limited to unit/build/lint on a debug variant. The current TV APK hash comparison is for the *earlier* installed build and must not be represented as validation of the merged APK. Separate [physical capture and playback regression evidence](live-video-freeze-regression-2026-10-27.md), [weather proof](missing-today-high-weather-qa-2026-10-27.md), [SAF proof](saf-canonical-qa-2026-10-26.md) and [DreamService proof](hisense-dreamservice-activation-2026-10-26.md) preserve their original scopes.
