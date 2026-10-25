# OpenVista v1: acceptance contract and release stages

**Owner-confirmed scope:** decisions D-01–D-78, last confirmed 2026-10-25. This page separates a **local engineering APK** from future **stable public v1.0**. It is not a record of completed testing.

## Local engineering APK (first artifact)

- Build and install on the **Hisense 58U6HF running Fire OS 7** for owner-led tests. Never upload first test APK to GitHub Releases without a subsequent explicit release instruction.
- Prototype may be deliberately incomplete, but every missing requirement must be tracked, visibly labeled and must not be described as stable v1.
- **P0 acceptance of first app-mode playback slice:** opens by TV launcher, D-pad focus works, plays at least one *legally permitted test* HLS/DASH feed and one supported local file via a supported access path, cleanly exits and restores basic state, no boot auto-launch, no privileged system changes.
- If no licensed live feed can be secured for prototype: validate Media3 on local assets or provider-authorized testing streams first. Do not insert scraped/public-looking feeds to shortcut permission checks.
- Test package identity/signing, no destructive replacement of existing TV apps and an installation rollback route.

## Stable v1.0 public GitHub release criteria

This is **not** permission to publish a release automatically; the owner will choose publication when evidence is reviewed.

| Gate | Minimum evidence required | Pass condition |
| --- | --- | --- |
| G-01 Permitted cameras | Provider-permission and attribution register, country/category/location and tested HLS/DASH endpoints | **25+ distinct authorized, family-appropriate and working public cameras**, actual playback verified on Hisense; no stolen/geo-bypassed links |
| G-02 App mode | Test cases with logs/screenshots/video from Hisense / Fire OS 7 | Manual Home, catalog and map, favorites, playback, remote, overlays/clock/weather and settings meet approved behavior |
| G-03 Screensaver | DreamService state and system settings capability captured on target | Works if OS/OEM allows; otherwise honest unsupported guidance, with app mode fully usable |
| G-04 Local media | Internal & USB test media; deep/nested folders; MP4/MKV/WebM/MOV matrix | Accessible source paths and supported codecs play; unsupported files skip and explain; playlists/resume/±10 sec/loop function |
| G-05 Failure recovery | Network toggles, all-sources-down, USB removal, process death | Configured local fallback works; no frozen UI, repeated invalid source storm or premature switch when internet returns |
| G-06 TV UX | D-pad focus map, PT-BR and EN checks, simple/advanced editor and accessibility review | No unreachable menus; 5-second controls, five-minute camera default, map 2D + list and viewer weather function where data is available |
| G-07 24-hour soak | At least 24 hours of real-device continuous playback/rotation, sampled CPU/RAM, dropped frames, error rate, thermal/network behavior | No unrecovered crash, runaway memory/CPU, busy loops or unhandled background playback; quantitative tolerances decided from baseline before test |
| G-08 Privacy and licensing | Permission inventory, logs redaction review, offline behavior, upstream source/media/weather/map licenses | Account-/ad-/telemetry-free, reports shared only by explicit action, family-safe licensed sources and clearly attributed data |
| G-09 Updates | GitHub signed release/download integrity, PackageInstaller behavior on Fire OS 7 | Download verification works; automatic installation **only if OS actually permits**, otherwise clear required user confirmation; no bypass, no unstable silent update claim |
| G-10 Device scope | Named device model/build/OS/software versions, documented test result | **Only Hisense 58U6HF/Fire OS 7 is tested initially**. Every other TV family marked *not tested*. No universal compatibility claim |

## Streaming formats

- Public and user-entered **HLS** and **MPEG-DASH** are the intended first supported streaming formats; stream samples, DRM/encryption and hardware codecs are separate capability gates.
- **RTSP and other protocols are not in first-release scope** without a later decision. A URL is not automatically an executable feed.
- Prior saved local-video formats MP4/MKV/WebM/MOV are container *candidates*, only if the TV can decode them.

## Local QR input

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

No gates have been passed and no application APK exists at the date of writing. Each gate requires test runs with date, exact device build, test case, expected/actual outcome, evidence location and responsible reviewer. Later testing may trigger a documented scope change rather than silently waiving a requirement.
