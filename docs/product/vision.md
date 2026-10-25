# OpenVista: product vision

**Status: discovery. Updated 2026-10-24.**

## Confirmed by the project owner
- OpenVista is an ambient / screensaver app for **Android TV generally**, not a device-specific Hisense app. Consider Google TV and Fire TV variants, but do not promise untested compatibility.
- Offer **live cameras from around the world** and **looping downloaded videos** placed in a user-selected folder on the TV or external storage.
- Display a clock, weather conditions, and a **7-day forecast**. Exact overlays and locations need decisions.
- Investigate a possible library of video wallpapers, including permissioned integrations with existing libraries.
- Conduct a thorough product interview before implementation. Keep decisions, research, specs and tickets in GitHub.

## Proposed principles (not yet approved)
- Smooth D-pad navigation and accessible UI.
- Full-screen immersive video; overlays can disappear.
- Hardware decoding where possible and graceful degradation on old TVs.
- Manual-launch mode always available; optional DreamService on compatible OS versions.
- Local videos remain offline-friendly; network streams fail over gracefully.
- Privacy by default and minimal permissions; licensed/permissioned media sources.

## Success metrics to specify later
- Playback resilience for long sessions (24-hour soak tests).
- Input navigation, refresh/fallback quality, correct weather location/timezone.
- Memory, thermal and network behavior on low-power Android TV devices.
- Folder selection and persistence across reboot; user controls playback order.
- Representative hardware matrix across Android TV, Google TV, and Fire TV.

See [open decisions](decisions.md), [architecture](../architecture/overview.md), [roadmap](../roadmap.md).
