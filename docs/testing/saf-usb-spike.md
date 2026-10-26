# Scoped Storage / USB technical evidence — issue #9

Date: 2026-10-25 (America/Phoenix). Target device: Hisense 58U6HF, Fire OS 7 / Android 9 (API 28), model `AFTHA001`.

## Scope and evidence levels

The OpenVista `:player` prototype requests a folder using `ACTION_OPEN_DOCUMENT_TREE`. It passes the returned tree URI to `SafVideoScanner` and enumerates documents using `DocumentsContract` and a cancellation signal. It has no broad external-storage permission. All media and document-provider fixtures used in automated tests are created for the test; no streams, playlists, or videos are bundled with the APK.

The owner has reported testing and approval of local/USB playback, as recorded in [player-prototype-spike.md](player-prototype-spike.md). That report does **not** provide detailed USB provider identifiers, reboot/permission-persistence logs, hot-unplug traces, or exact container/codec metrics. The automated checks below are **Android 9 Robolectric simulations**, not fresh physical USB proof.

## Scanner properties verified locally

| Contract / regression | Evidence | Status |
| --- | --- | --- |
| Only video candidates from a user-selected SAF tree | Synthetic DocumentsProvider returns videos in root and child directory plus an image; returned list contains only two video candidates under the selected authority | PASS (Robolectric) |
| Pre-cancelled scanner | CancellationSignal is cancelled before traversal | PASS (Robolectric) |
| Cyclic directory links | Synthetic provider lists the root as its own child. The regression first failed with duplicate results; a visited-directory set now ensures the root is queried once | PASS (Robolectric) |
| Duplicate file document IDs | Synthetic provider returns the same video ID twice; scanner emits it only once | PASS (Robolectric) |
| Bounded enumeration | Scanner uses `MAX_ENTRIES = 500`, `MAX_VIDEOS = 100`, and `MAX_DIRECTORY_DEPTH = 3`. The cycle/dedup checks do **not** prove performance at these limits | IMPLEMENTED; exhaustive stress validation pending |
| Persisted URI grants, provider revocation, reboot | No persistent permission storage/reopen implementation proved by current tests | NOT VERIFIED |
| Real USB discovery, removal/reinsert, codec/container decoding | Owner-reported acceptance exists, but reproducible device evidence is incomplete | NOT VERIFIED in this regression |

Both visited-ID sets are local to one scan, do not escape the selected URI authority, and do not retain or transmit document identities. Deduplication is based on the document provider's `COLUMN_DOCUMENT_ID`; it does not attempt content hashing. Recognized file extensions indicate candidate media only, **not** decoder compatibility.

## Reproduce

From the repository root on Windows with the Android SDK installed:

```powershell
.\gradlew.bat :player:testDebugUnitTest :source-core:test :diagnostics:testDebugUnitTest :player:assembleDebug :player:lintDebug --no-daemon
python scripts/validate_discovery_docs.py
python scripts/release_content_guard.py player/build/outputs/apk/debug/player-debug.apk
```

No GitHub Actions were used. Do not claim physical USB or durable grants as validated from this suite. No Fire OS settings, installed apps or ADB reverse rules were changed for the cycle/dedup regression.

## Persisted folder grant and explicit removal prototype (2026-10-26)

A scoped, reviewable follow-up branch adds `SavedFolderAccess` for **only the user-selected** `ACTION_OPEN_DOCUMENT_TREE` folder. After the picker succeeds, the Activity attempts Android's read-only `takePersistableUriPermission`, saves the tree URI in app-private preferences, and automatically rescans only when the OS still reports a persisted read grant. A missing or revoked grant is discarded, without opening unrelated storage. The new **Forget folder** control cancels pending scans, stops playback, releases the saved permission when still present, and clears this local reference. If the picker/provider does not support persistence, the selected folder remains session-only and the UI says it must be selected again after restart. An unsuccessful new selection cannot silently restore an older saved folder.

The new Android 9 Robolectric tests cover the persistable-read picker request, empty first install, simulated save/restore/forget, and stale/revoked grant handling. **Robolectric simulates persisted URI authorization and does not prove the real Fire OS picker supplies a persistable grant.** Existing player/diagnostics/source-core tests, Android Lint and Python release-content guard were rerun locally and passed. The shipped prototype continues to include no stream URLs or bundled media.

### Limited Fire TV check (2026-10-26)

The Hisense was rediscovered on the owner's LAN using the existing ADB listener and confirmed as `AFTHA001`. The old IP was no longer current; IP details are not recorded in repository evidence. Exactly one OpenVista app (`com.openvista.prototype`) was installed. An `adb install -r` **updated that same package** and succeeded. The process launched and appeared in ActivityManager, and no matching OpenVista fatal error appeared in the filtered AndroidRuntime log. However, `am start -W` reported a timeout while `Sys2023:dream` held window focus and `mWakefulness=Dreaming`. Thus **on-screen focus, real folder picker completion, durable grant across reboot, and reselect/forget behavior were NOT verified** in this run. No D-pad/Back/Home events were sent to the unrelated dream/launcher; the existing screensaver selection and OS settings were not modified.

The feature remains in a separate **draft review branch** until a controlled real-picker selection and reboot/revocation sequence can verify permissions on the Fire TV. This check does not close issue #9 or satisfy stable-v1 acceptance.
