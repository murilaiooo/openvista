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
