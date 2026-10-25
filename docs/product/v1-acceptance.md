# OpenVista v1: acceptance contract and release stages

**Owner-confirmed scope:** the 78 historical decisions, as modified by the later **C-01 user-managed content clarification**. The app ships no camera feeds, source lists, or videos. This page separates a **local engineering APK** from future **stable public v1.0**. It is not a record of completed testing.

## Local engineering APK (first artifact)

The **first experimental build may be an isolated, read-only device self-check** without Media3. It must be clearly labeled a diagnostic APK, not a playback prototype. The subsequent app-mode playback proof below still has its own separate acceptance test. See [first-APK sequencing](../implementation/first-apk-sequencing-review-2026-10-25.md).

- Build and install on the **Hisense 58U6HF running Fire OS 7** for owner-led tests. Never upload first test APK to GitHub Releases without a subsequent explicit release instruction.
- Prototype may be deliberately incomplete, but every missing requirement must be tracked, visibly labeled and must not be described as stable v1.
- **P0 acceptance of first app-mode playback slice:** opens by TV launcher, D-pad focus works, plays at least one *legally permitted test* HLS/DASH feed and one supported local file via a supported access path, cleanly exits and restores basic state, no boot auto-launch, no privileged system changes.
- Test HLS/DASH playback with ordinary user-selected test sources and local media files; keep temporary research playlists outside the application, and do not bundle or publish any media with the APK. Never circumvent authentication or access controls.
- Test package identity/signing, no destructive replacement of existing TV apps and an installation rollback route.

## Stable v1.0 public GitHub release criteria

This is **not** permission to publish a release automatically; the owner will choose publication when evidence is reviewed.

| Gate | Minimum evidence required | Pass condition |
| --- | --- | --- |
| G-01 User-managed sources | Source entry, saved list import, empty-library behavior, source metadata and security tests on the Hisense, plus the [signed package-content scan](../testing/release-content-guard.md) | **No built-in camera streams, playlists, or videos in public builds**; user-added HLS/DASH and compatible source lists work reliably, without leaked private URLs |
| G-02 App mode | Test cases with logs/screenshots/video from Hisense / Fire OS 7 | Manual Home, user-managed source library and map, favorites, playback, remote, overlays/clock/weather and settings meet approved behavior |
| G-03 Screensaver | DreamService state and system settings capability captured on target | Works if OS/OEM allows; otherwise honest unsupported guidance, with app mode fully usable |
| G-04 Local media | Internal & USB test media; deep/nested folders; MP4/MKV/WebM/MOV matrix | Accessible source paths and supported codecs play; unsupported files skip and explain; playlists/resume/±10 sec/loop function |
| G-05 Failure recovery | Network toggles, all-sources-down, USB removal, process death | Configured local fallback works; no frozen UI, repeated invalid source storm or premature switch when internet returns |
| G-06 TV UX | D-pad focus map, PT-BR and EN checks, simple/advanced editor and accessibility review | No unreachable menus; 5-second controls, five-minute camera default, map 2D + list and viewer weather function where data is available |
| G-07 24-hour soak | At least 24 hours of real-device playback/rotation, sampled CPU/RAM, dropped frames, error rate, thermal/network behavior, using [the Hisense test protocol](../testing/hisense-24h-soak-protocol.md) | No unrecovered crash, runaway memory/CPU, busy loops or unhandled background playback; quantitative tolerances decided from baseline **before** test |
| G-08 Privacy and licensing | Permission inventory, logs redaction review, offline behavior, upstream source/media/weather/map licenses | Account-/ad-/telemetry-free, reports shared only by explicit action, safe handling of user sources, tested fixture permissions and clearly attributed third-party weather/map data |
| G-09 Updates | GitHub signed release/download integrity, PackageInstaller behavior on Fire OS 7 | Download verification works; automatic installation **only if OS actually permits**, otherwise clear required user confirmation; no bypass, no unstable silent update claim |
| G-10 Device scope | Named device model/build/OS/software versions, documented test result | **Only Hisense 58U6HF/Fire OS 7 is tested initially**. Every other TV family marked *not tested*. No universal compatibility claim |

## Streaming formats

- User-entered **HLS** and **MPEG-DASH** are the intended first supported streaming formats, with source-list import evaluated independently. DRM/encryption and hardware codecs are separate capability gates.
- **RTSP and other protocols are not in first-release scope** without a later decision. A URL is not automatically an executable feed.
- Prior saved local-video formats MP4/MKV/WebM/MOV are container *candidates*, only if the TV can decode them.

## Local QR input

**Engineering schedule:** #8 moves from first-prototype P0 to P3 before a stable v1 that claims owner-approved D-32/D-73. USB/user-selected M3U files and keyboard entry are prioritized earlier. This deferral does not waive the stable-v1 QR requirement or authorize any LAN listener before security review.

- Show a QR code for a **short-lived locally scoped pairing session** when TV and phone are on the same LAN/Wi-Fi; require an explicit TV confirmation before accepting a submitted URL.
- Research/authentication and threat-model before coding: device discovery, token entropy, expiry, trusted local bind, replay/CSRF, origin checks and denial-of-service.
- Do not introduce mandatory accounts or internet relay to satisfy the pairing experience.

## Offline cameras/map behavior

- When map tiles are unavailable, show **local saved/cached camera metadata as a list**, with a visible notice that the online world map cannot load.
- This is browsing fallback, **not video cache** and does not imply streams will work without internet.

## Automatic application updates — conditional

Owner-approved outcome: **download and install updates automatically**. Android imposes installer permissions/ownership and possible user-consent requirements.

- **Detect:** look up the correct signed update only from the project-controlled eventual distribution channel; do not follow arbitrary redirects/untrusted binary links.
- **Download:** verify release signature/certificate continuity, package name, version monotonicity, HTTPS source/integrity and size before staging; never accept downgrade by default.
- **Install:** use only supported Android package installer interfaces. On the current **Fire OS 7 / Android 9** test target, do not claim fully unattended app self-installation. PackageInstaller installation user-approval behavior and unknown-apps entitlement must be measured on-device.
- **Fallback:** if system requires user action, surface the system install approval UI, not a misleading "installed" status. This is a **technical limitation of D-75**, not owner consent to root, ADB privilege elevation, device-owner modes, or sideload exploits.
- **Failure handling:** keep the running version and user data intact; no forced launch on boot; don't download public updates before a release channel actually exists.

See Google's [PackageInstaller.SessionParams](https://developer.android.com/reference/android/content/pm/PackageInstaller.SessionParams), [Installing unknown apps](https://developer.android.com/studio/publish), and [PackageManager.canRequestPackageInstalls](https://developer.android.com/reference/android/content/pm/PackageManager).

## Diagnostic privacy

- Persist logs locally with bounded retention and secret/URI/precise-location redaction. An explicit menu action may **copy/share an error report**, with preview/confirmation before sharing.
- D-67 remains binding: **no settings/favorites/playlists export/import, backup or cloud sync** in v1. Sharing a purpose-built sanitized bug report does not contradict it.

## Excluded or deferred

- Downloadable video-wallpaper library; RTSP; subscriptions/ads/accounts; cloud settings sync; exporting/importing settings/playlists; auto-run on TV boot; rooting/modifying TV to bypass the OS.
- Support claims for devices other than tested Hisense; forced unattended install when the OS rejects it.

## Decision trace and acceptance evidence

Each approved decision is assigned a primary G01–G10 owner in [requirement traceability](requirement-traceability.md), with every D-ID currently marked **NOT TESTED**. Multiple gates may be relevant to individual requirements; tests will add concrete evidence links when produced. The [planning audit](../reviews/2026-10-25-planning-audit.md) records blocking risks.

No gates have been passed and no application APK exists at the date of writing. Each gate requires test runs with date, exact device build, test case, expected/actual outcome, evidence location and responsible reviewer. Later testing may trigger a documented scope change rather than silently waiving a requirement.
