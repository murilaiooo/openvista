# OpenVista: product vision

**Status:** Product discovery, round 1 decisions confirmed on 2026-10-24.

## Confirmed by the project owner

- OpenVista is an immersive Android TV screensaver and ambient-video **app for the wider Android TV ecosystem**, not just a Hisense/Fire TV model. Evaluate Google TV and Fire TV/Fire OS variants but never promise untested support.
- **Two entry points:** normal app opened manually and automatic screensaver mode **where permitted by the device/OS**.
- **Live cameras:** browse a catalog of scenes around the world or enter permitted stream URLs manually.
- **My videos:** choose a folder of downloaded scenic videos on the TV and play in sequence, shuffle, or playlists. USB/NAS support details remain unresolved.
- **Weather:** clock, current conditions and seven-day forecast goals, with option to show the current camera location or viewer location. Viewer geolocation is never required by default.
- **Overlays:** fully customizable; their controls, styling, placement and default appearance are still open.
- **Audio:** mute by default, with optional sound.
- **Economics:** free, no advertisements, no account required.
- **First distribution:** APK through GitHub, not an initial store-release commitment.
- Investigate a future discoverable library of video wallpapers, respecting source licenses; this is not yet an MVP feature.
- Use the relevant Matt Pocock engineering skills and document owner decisions, architecture hypotheses and findings in GitHub.

## Proposed product principles (not approved requirements yet)

- Smooth, accessible D-pad navigation, without needing a keyboard/mouse for common use.
- Fullscreen video that can remain immersive with optional overlays.
- Hardware decode where possible, with degradation on lower-powered TV hardware.
- Manual app mode always available; DreamService where compatible.
- Graceful offline behavior, retry and failover for network cameras.
- Privacy by default, minimal permissions, no mandatory cloud backend or telemetry.

## Success metrics to specify later

- Long-running playback reliability (proposed: 24-hour soak test).
- Folder permissions across reboot, including removal/reinsertion of external storage where supported.
- Stable live video switching and invalid-source recovery.
- Clock and forecast accuracy for both weather contexts.
- CPU, memory, dropped frames and bandwidth on low-powered TVs.
- Representative device test matrix before declaring compatibility.

See [decisions](decisions.md), [architecture](../architecture/overview.md), and [roadmap](../roadmap.md).
