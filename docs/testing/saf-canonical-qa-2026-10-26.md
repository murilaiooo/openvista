# SAF canonical media identity: independent QA, 2026-10-26

Base: `ab2ef28ce5b18fabf45b9cef7f8dc5c802e66a95`. This focused branch carries **storage and document-access changes only**, without the Codex candidate's security/provider/updater/playback changes. C-01 remains enforced.

## Independent checks

- Offline Windows Gradle: `:source-core:test :diagnostics:testDebugUnitTest :player:testDebugUnitTest :player:assembleDebug :player:lintDebug`: **PASS (84 tasks)**.
- Python tests: **23/23 PASS**. Discovery and requirement traceability: **PASS**.
- Actual debug APK no-bundled-media guard: **PASS**; no streams, playlist files, video or literal media endpoints in the package.
- A previous integrated QA candidate with this same SAF implementation migrated the physical Hisense Fire OS 7 library from schema 1 to 2. Its 44 records became 43 (40 cameras unchanged, four videos merged to three). Automated read-back verified no missing old IDs, favorites, playlist links, or checkpoint references for that saved data set.
- Robolectric: tree/direct access to one document collapses to one record with two routes, preserving favorites, playlists and saved positions. Cancellation before a scan returns unavailable without querying a provider; a revoked OS grant cannot be restored from stale preference metadata.
- **Not verified as part of this narrow branch:** actual USB unplug/replug, diverse SAF document providers, nested permission revocation and robust failure injection. Real-device migration evidence is **partial** and belongs to the earlier integrated candidate, not this independently rebuilt narrow artifact.

## Rollback and release constraints

Schema-2 private library writes are not automatically readable by an older schema-1 APK; before on-device deployment retain a full app-private backup. Original user-owned workspace and Git history were not altered. The Fire TV owner-disabled screensaver remains off. No public binary, release, unrelated package changes or GitHub Actions.

Do not infer stable-v1 acceptance or broad TV compatibility from this focused change.
