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
- **Scope updated after this research:** OpenVista does not ship first-party cameras. The historical 25-source quota is superseded by C-01; validate **user-provided stream/list imports** instead. Any media test fixture remains outside the APK.
- Investigate weather forecast provider terms, seven-day field support, attribution/caching quotas, map tile usage policy and offline map metadata. No weather or map service was approved automatically by the user.
- QR local pairing requires a separate security threat model before implementation.

## Planning-audit findings from official primary sources (2026-10-25)

### Webcam discovery is not the same as license-cleared native video

- **SkylineWebcams:** its own [FAQ](https://www.skylinewebcams.com/en/support/faq.html) says only camera hosts may embed its *live* feed; ordinary users can embed periodically updated images. **Do not count Skyline public webcams as native HLS/DASH streams**, without an independent owner license.
- **Windy Webcams API:** [official free-plan pricing](https://api.windy.com/webcams/pricing) describes linked images and timelapse player. Its [API terms](https://account.windy.com/agreements/windy-api-webcams-terms-of-use) regulate API keys, redistribution and source/operator rights. Discovering camera metadata or snapshot URLs **does not prove access to a live Media3-playable stream**.
- This raises the source gate to a true **G01 blocker**: [issue #3](https://github.com/murilaiooo/openvista/issues/3) must produce the rights-cleared stream inventory with independently verified HLS/DASH playback and expiry/reliability tests. A user-added URL is a separate permitted custom source, **not** a loophole for populating the shipped catalog.
- **AerialViews code:** the upstream [LICENSE](https://github.com/theothernt/AerialViews/blob/master/LICENSE) is **GPLv3** whereas OpenVista's root LICENSE is **MIT**. Study architecture and patterns freely; **do not copy/fork upstream source into an MIT-only codebase** without a conscious GPL compliance/licensing decision, proper attribution and license review.

### Android 9 installer API version distinction

- [PackageInstaller.SessionParams.setRequireUserAction](https://developer.android.com/reference/android/content/pm/PackageInstaller.SessionParams) is **added at API 31 (Android 12)**, not part of Fire OS 7's Android 9 API 28 surface. A Fire OS 7-specific proof must use APIs actually supported by that OS; status callbacks such as [STATUS_PENDING_USER_ACTION](https://developer.android.com/reference/android/content/pm/PackageInstaller) still make install consent relevant.
- Do not copy an Android 12+ `setRequireUserAction(USER_ACTION_NOT_REQUIRED)` implementation into the Android 9 variant. Unless an authorized supported OEM API is genuinely available and validated, expect an installer confirmation instead of a guaranteed hands-free upgrade.
- Automatic **download** may be implemented separately from platform-controlled **installation**; on-device checks, background download policy, signature continuity and rollback all need evidence.

### Map tiles, weather and provider terms

- [OpenStreetMap Foundation tile policy](https://operations.osmfoundation.org/policies/tiles/) requires attribution, identifiable app user agent and HTTP-aware caching, while **prohibiting bulk offline/prefetch tile downloading** from the community tile servers. The owner-selected **offline saved camera list** avoids requiring offline map tiles, but normal interactive tile loading still needs a chosen legal provider and caching strategy.
- [Open-Meteo terms](https://open-meteo.com/en/terms) currently offer **noncommercial** free use, with attribution (CC BY 4.0) and daily/hourly/minute quotas. Free/no-account product goals do **not** exempt OpenVista from provider usage limits or altered future terms. Product suitability needs explicit provider selection and usage accounting.
- [DreamService](https://developer.android.com/reference/android/service/dreams/DreamService) interactive input handling requires `setInteractive(true)`; **that API alone does not mean Fire OS exposes the service as a user-selectable default**. The Hisense P0 proof controls the compatibility statement.

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
