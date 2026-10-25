# OpenVista: first-release blockers and evidence checklist

**Status 2026-10-25:** all **78 owner decisions** approved over eight rounds. This is a research checklist, not an app implementation or release.

## Approved release boundaries

- **First APK:** local testing only on **Hisense 58U6HF / Fire OS 7**. Do not publish the first test binary to GitHub.
- **Stable v1.0 (later GitHub, only with owner approval):** functional **user-managed streaming sources and playlists**, clear zero-source experience, **no bundled feeds/media**, plus documented playback, compatibility, 24-hour stability, privacy and platform/license checks.
- **Video:** licensed HLS and MPEG-DASH sources and permitted local files; no first-release RTSP.
- **QR entry:** phone and TV on the **same Wi-Fi/LAN**, explicit pairing confirmation and expiring authorization.
- **Map offline:** local saved-camera list with notice; no guarantee of cached playable feeds.
- **Update preference:** automatic download and installation **where OS allows**. Do not overpromise unattended installation on Fire OS 7; fallback to platform approval dialogue when necessary.
- **Diagnostics:** local retention, intentional user copy/share of sanitized report, no automatic upload or settings/favorites backup.

## Evidence / remaining blockers

| Priority | Gate | Required verification |
| --- | --- | --- |
| P0 | Hisense OS baseline | Record actual Fire OS/Android API, codecs, model, current state, installation/rollback safety |
| P0 | DreamService feasibility | OEM activation menu, idle timings, input dispatch, no app boot auto-start |
| P0 | HLS/DASH / Media3 | Lawful permitted test sources, actual codec, bitrate, frame-drop and error measurements |
| P0 | Storage/USB | Permissioned picker, recursive scanning, USB unplug/replug and persistable grants |
| P0 | User-source imports | Safe source-list parsing, supported stream formats, zero-source UX, no media/test URLs bundled in the APK |
| P0 | Weather & 2D map services | Seven-day licensed data, rates, attribution, map tile rights, offline local-list UX |
| P0 | App update permissions | PackageInstaller, unknown-app source privileges, installer-of-record, user-approval feasibility |
| P0 | QR security | Local pairing consent, token expiry, origin/CSRF, malicious URL protections |
| P1 | App UX and resilience | D-pad focus, control menu, playlists, diagnostics, offline-to-local return, no busy loops |
| P3 | Public acceptance | Documented 24h physical-TV soak, compatibility, legal/privacy and owner release sign-off |

Research must be done before production implementation for its dependent feature. The previously discussed first-party 25-camera directory is superseded by [the user-managed content clarification](user-managed-content.md); release builds should contain zero feeds and videos. No signed binary, compatibility claim, source license or silent installation is proven yet.

Use [v1 acceptance](v1-acceptance.md), [engineering plan](../implementation/priority-plan.md) and [official platform notes](../research/platform-feasibility-2026-10-25.md).
