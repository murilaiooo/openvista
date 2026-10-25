# OpenVista: faster route to the first local APK

**Review date:** 2026-10-25. **Status:** Engineering sequencing adopted for prototyping; not a rewrite of owner-approved decisions D-01–D-78 or C-01. No real-device compatibility, new Android source, or built APK is claimed by this document.

## Decision table

| Suggestion | Disposition | Reason and acceptance boundary |
| --- | --- | --- |
| USB / local file import before phone-to-TV QR | **Adopt for first functional slice** | User-selected M3U import is simpler and requires no LAN listener. The TV's actual USB/DocumentsUI support remains unverified, so internal storage and direct TV-keyboard entry are independent fallback paths. |
| Demote issue #8 QR/LAN from P0 | **Move to P3, keep issue open** | D-32 and D-73 historically approve QR. Deferral removes it from the *first-APK critical path*, not from the signed-off stable v1 contract. Post-v1 deferral would require a separate explicit owner scope decision. |
| Read-only diagnostic APK as first source | **Adopt** | Build an original one-screen, separate-package diagnostic Activity. No root, system changes, auto-upload, boot receiver or automatic enumeration of private user files. |
| One Activity + Media3 + URL + file/folder picker | **Adopt shared P0 prototype** | A single thin local test application can collect evidence for #3, #5 and #9; three independent acceptance matrices remain. It does **not** establish DreamService support (#2). |
| Local sunrise/sunset calculation | **Adopt as a design/prototype path** | Derive solar position from documented astronomical formulae with test fixtures, rather than burning weather API requests solely for sun events; actual meteorological fields still need a provider. |
| Daylight/Golden Hour, overlay guardian | **Research as P2 opt-in product experiments** | Validate geotags, solar edge cases and low-resource rendering with users and device before calling them product requirements. |
| Three-action source-empty state | **Adopt in first app UX** | Give three D-pad-accessible ways forward instead of dead-end messaging; preserve the approved three-card Home layout. |
| Procedural demo loop | **Decision pending** | C-01 says OpenVista ships no videos or feeds, and G01 excludes bundled media. Do not add shaders, generated video, embedded demo assets or claims until the owner explicitly decides the boundary. |

## First executable: a safe self-check APK

**Purpose:** Collect reproducible capability evidence while the main player remains unbuilt, even if development ADB is disconnected. Deliver a **locally built, manually launched** `com.openvista.diagnostics` test APK first (exact identifier to confirm at implementation).

Single screen: **Device & playback readiness** with `Collect`, `View report`, `Save JSON` (system `ACTION_CREATE_DOCUMENT` picker), and `Close`. Do not read personal storage or transmit any report automatically.

Recommended JSON schema fields:

- `schemaVersion`, capture timestamp UTC, diagnostic app version, Android `Build.MANUFACTURER`, `MODEL`, API level, OS release and build display string (avoid serial/device identifiers).
- Display width, height, refresh rate when OS reports them; fallback `unavailable` where APIs or OEM restrictions block observation.
- Advertised decoder names, supported MIME types, hardware/software classification where the current API exposes it. **Declared capability is not sustained playback proof.**
- Total/available memory estimates and UI/remote-focus smoke checks. Keep per-app data and other apps' details out of the report.
- `documentsPickerAvailable`, `treePickerAvailable`; count of grants already issued **to this diagnostic package**, with no URI strings or paths. Empty grants are expected before explicit user selection.
- `checks` status values `pass`, `fail`, `unsupported`, or `not_tested`. A picker launch alone does not establish USB enumeration or retained grants.

Tests: open and exit via D-pad; handle missing picker without crash; save JSON only after user confirmation; compare JSON to schema and redact host identifiers; no `INTERNET`, location, broad filesystem permissions or user-content assets. Run on Hisense 58U6HF before labeling hardware verified. **Do not overwrite existing apps, change default launcher/screensaver or upload the APK to Releases.** Debug signing and package identity must be documented for rollback.

## Next executable: one shared proof Activity

Use one **local-only** Kotlin Android TV app with a fullscreen Activity, D-pad input, Media3 player, direct HLS/DASH URL field, user-selected playlist-file picker (`ACTION_OPEN_DOCUMENT`), and local video folder picker (`ACTION_OPEN_DOCUMENT_TREE`). Keep the source importer and local video tree scanner separate internally, even if both are accessible from one screen.

- **#3:** Empty library, keyboard entry, USB/imported M3U, URI revocation, malformed/untrusted entries and no bundled streams. Limit imported text size, entry count, URL length, recursion and network fetching; explicitly distinguish HLS `#EXT-X` manifests from user playlist `#EXTINF` records.
- **#5:** Lawful test HLS/DASH feeds and permitted local media, real decoder/bitrate/frames/buffering errors; never infer Fire OS behavior from PC `ffprobe`.
- **#9:** File picker actually available, USB media selection, persisting grants only where needed, recursive folder scan, unplug/reinsert and safe failure. `ACTION_OPEN_DOCUMENT` (single playlist file) is not a replacement for `ACTION_OPEN_DOCUMENT_TREE` (media folder).
- **#2:** Separately investigate interactive DreamService / OEM selection and idle input, after the manual Activity has a demonstrated input lifecycle. Activity key input cannot establish DreamService support.

Target small, reviewable classes and full negative tests, **not a promised 500-line ceiling**. Ensure source URLs remain user-entered and never embedded in shipped artifacts. The first signed distribution still requires owner approval.

## Revised critical path

```text
Toolchain readiness
  -> one-screen diagnostics APK -> on-device, redacted JSON evidence
  -> shared app-mode player + direct URL + M3U import + SAF folder picker
  -> #3, #5, #9 independent on-device acceptance evidence
  -> reliable v1 UX, map/weather and recovery
  -> separately prove optional DreamService and updater constraints
  -> stable-v1 24-hour soak + licensing/privacy/package checks
  -> owner-reviewed publication
```

In parallel: provider-terms research, source-list parser threat cases, a tested solar calculation and offline UX design. QR/LAN security research is P3 and **must complete before QR is enabled or a stable v1 containing QR is labeled finished**.

### Sources

- Android Storage Access Framework: https://developer.android.com/guide/topics/providers/document-provider
- Android document picker: https://developer.android.com/training/data-storage/shared/documents-files
- NOAA approximate sun event algorithms: https://gml.noaa.gov/grad/solcalc/calcdetails.html
- NOAA sunrise equation PDF: https://gml.noaa.gov/grad/solcalc/solareqns.PDF
- [Original decisions](../product/decisions.md), [current scope](../product/user-managed-content.md), [v1 gates](../product/v1-acceptance.md).
