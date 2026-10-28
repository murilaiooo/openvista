# Camera switching and soft-glass QA

Date: 2026-10-27. Scope: regular player and DreamService.

## User-reported defects
- Brief black flash between live cameras.
- Unnecessary "Loading your media" banner at the top.
- Source country missing or truncated.
- Overlays need light glass-like contrast without hard rectangular cards.

## Implementation
- Retain Media3 player content on reset, use a transparent shutter, and remove the full-screen black transition in both playback entry points.
- Cache a single 640px in-memory still using PixelCopy after the first successfully rendered video frame. Hold it while the next source prepares, then fade out after its first rendered frame. The cache expires onscreen after 12 seconds. One decoder; no added stream, continuous readback, extra permission, or persistent video file.
- Hide normal buffering/play status; show failures only temporarily, centered rather than in the upper information band.
- Separate source status from city/country/flag, allow source place to wrap without ellipsis; read both from user library metadata, never from filenames.
- GPU-drawn feathered radial gradients behind source, viewer and forecast create soft glass-like contrast with no opaque rectangle. The gradient is not a true live-video Gaussian blur: SurfaceView backdrop blurring on Fire OS 7 would require more expensive periodic readback.

## Verification
- Offline Gradle debug unit tests, APK assembly and Android Lint passed.
- Updated the existing OpenVista APK on the owner's physical Hisense; existing library backup saved before installation, no device-wide setting changed.
- Real video screenshot confirms source header "Kraków, Poland", current source weather and no top loading notice.
- A 10-second hardware screen recording tested two right-arrow camera changes around 2s and 4s. At approximately four samples per second, there were no fully black frames during either change. The first screenrecord frame was black during capture startup; it is not treated as a camera-change failure.
- Weak-network failures, overnight rotation and longer international names still require QA.

One active Media3 decoder remains the hard playback limit. No video or stream is bundled.
