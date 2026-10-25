# Official technical references and feasibility notes — 2026-10-25

These are **research findings**, not completed local/TV prototypes or an engineering claim that OpenVista is supported yet. Recheck platform documentation and exact OS build before implementing.

## Android screensaver / DreamService

- Official API: https://developer.android.com/reference/android/service/dreams/DreamService
- Interactive dreams require `DreamService.setInteractive(true)`; otherwise the first input event can dismiss a noninteractive dream. Real Fire OS/OEM screensaver selection and D-pad dispatch remain **device-dependent**.
- A regular manually launched full-screen Activity is the unconditional fallback where OS-driven auto-start of a third-party screensaver is unavailable.

## Media3 HLS / DASH and device decoding

- Formats: https://developer.android.com/media/media3/exoplayer/supported-formats
- Devices: https://developer.android.com/media/media3/exoplayer/supported-devices
- HLS: https://developer.android.com/media/media3/exoplayer/hls
- DASH: https://developer.android.com/media/media3/exoplayer/dash
- Media3/ExoPlayer supports HLS and MPEG-DASH within specified manifest/container/codecs. DASH media requires appropriate manifest separation and container support; hardware/OS may reject particular video/audio codecs and HDR. **HLS/DASH protocol support does not imply any real-world webcam URL is legally licensed or playable.**
- Prototype manual playback with permitted or locally generated streams before implementing large catalog adapters.

## User-selected internal/USB media

- Official SAF: https://developer.android.com/training/data-storage/shared/documents-files
- `ACTION_OPEN_DOCUMENT_TREE` can grant access to the selected directory and accessible subfolders, subject to picker/provider restrictions; Android 11+ restricts selected roots and Downloads. Performance may suffer for very large trees.
- On Fire OS 7 verify system picker availability and USB URI grants in practice; do not bypass with broad file permissions.

## Application update installation limits

- PackageInstaller SessionParams: https://developer.android.com/reference/android/content/pm/PackageInstaller.SessionParams
- Package manager install authorization: https://developer.android.com/reference/android/content/pm/PackageManager
- Sideload/unknown-apps policy: https://developer.android.com/studio/publish
- Android differentiates permission to **request** installation from user-action-free installation. `USER_ACTION_NOT_REQUIRED` is only eligible under strict conditions (API/target levels, installer/update ownership, required permissions), and the platform may return `STATUS_PENDING_USER_ACTION`.
- User-directed automatic download/install is approved, but the **unattended portion is technically unproven** on Fire OS 7/Android 9. A conventional updater may stage a verified APK and invoke the platform installer, asking the user for confirmation. Do not misrepresent that as silent installation.
- Verify provenance (official GitHub release tag, asset identity, APK signature, package name, hashes if available, monotonic version), rollback/signer continuity and no private API calls.

## Camera rights and forecast/map licenses

- An internet-accessible HLS feed is **not necessarily authorized** for third-party display/distribution. Keep a source-permission inventory (owner, terms, embedding permissions, attribution, family-safety review, uptime, testing timestamp).
- Do not promise 25 sources until the register contains 25 distinct, verified lawful streams. **Provider review is outstanding**.
- Investigate weather forecast provider terms, seven-day field support, attribution/caching quotas, map tile usage policy and offline map metadata. No weather or map service was approved automatically by the user.
- QR local pairing requires a separate security threat model before implementation.

## Feasibility status

| Workstream | Documentation found | Local/TV validation |
| --- | --- | --- |
| DreamService and interactive keys | Official Android API | Pending |
| HLS/DASH Media3 support | Official Android Media3 docs | Pending |
| SAF folder-tree and USB | Official Android storage docs | Pending |
| APK automatic installation | Official Android PackageInstaller docs | Pending (silent install not promised) |
| 25 webcam rights/reliability | Policy gate identified | Pending provider-specific verification |
| Weather/map licenses | Research scope identified | Pending provider selection |

Read [v1 acceptance contract](../product/v1-acceptance.md) and [implementation plan](../implementation/priority-plan.md) for next tests.
