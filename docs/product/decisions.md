# Product decisions: OpenVista

No unapproved feature is a commitment. Record answers, alternatives and rationale here, then promote durable choices to an ADR.

## Confirmed
Name: OpenVista. Target: general Android TV app. Core sources: world live cams and a folder of user-provided looping scenic video. Desired: clock, weather, seven-day forecast, possible future wallpaper library.

## Interview: Round 1 (independent questions)

1. **Entry points:** automatic system screensaver, manually launched app, or both? **Recommended: both**, supporting manual launch if OS restrictions block the automatic DreamService.
2. **Camera catalog:** curated searchable places, user-provided stream URL, or both? **Recommended: both**, only for sources with permission and compatible streams.
3. **Local video:** choose a folder on internal/USB storage and autoplay shuffle/loop, or manage individual files? **Recommended: folder picker + repeat/shuffle**.
4. **Weather location:** place visible on screen, viewer's home location, or choice? **Recommended: location of current live camera, switchable to home after consent**.
5. **Overlay display:** always show all seven days, or minimal clock/current conditions with 7-day forecast on command? **Recommended: minimal overlay + extended forecast on remote action**.
6. **Audio policy:** silent by default or ambient sound where provided? **Recommended: muted by default, user override**.
7. **Commercial policy:** no ads/no accounts/free/open source vs another plan? **Recommended: free and no ads in first release**.
8. **Distribution:** sideload APK/GitHub Releases first vs immediate app-store publication? **Recommended: GitHub APK first, stores after OS testing**.

## Later interview rounds (depend on answers above)
- Source providers, their licenses and whether rehosting is prohibited; streamer reliability and uptime; favorites and curated categories.
- UI/navigation, app onboarding, visual themes, multilingual/localization and advanced control mapping.
- Rotation every N minutes, playlists, night schedule, resolution/data usage caps and fallback scenes.
- Forecast API, geocoding, permissions, units, clock timezones, stale-data labeling.
- Folder access limitations by OEM, photo support, SMB/NAS access, USB, metadata and file size.
- Third-party wallpaper library API, caching, downloads, storage limits, redistribution and attribution.
- DreamService OS matrix, OEM overrides, uninstallation behavior and energy/burn-in protections.

## Decision log
*Empty. Awaiting product owner answers.*
