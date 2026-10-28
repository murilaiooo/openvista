# Live video freeze regression and recovery (2026-10-27)

## Root cause and rollback
The camera-transition change in commit `3f66009` added an ImageView on top of the live decoder and a PixelCopy snapshot called `CameraFrameHold`. The image was meant to hide a short black flash while a new stream prepared. On the Hisense Fire TV, the screen could instead appear frozen because a previous source frame remained visible over the real video; retaining Media3 content on player reset could also hold the stale surface image. These visual-transition changes were unsafe for normal playback.

The recovery **removes CameraFrameHold and its full-frame ImageView entirely** in both `PlayerScreen` and `OpenVistaDreamService`. PlayerView now uses `setKeepContentOnPlayerReset(false)` again. No secondary or still-video layer can cover the player during source transitions.

This intentionally prioritizes live playback continuity over masking every decoder transition. A short gap/flicker might remain until a safe alternative is validated on the hardware. No double decoder, frame readback or streamed content is bundled.

## Preserved improvements
- Source city/country and live camera weather
- Quiet status banner (no normal loading text on top)
- Feathered translucent contrast behind the information zones
- Independent viewer weather, forecast, positions and burn-in controls
- Single-decoder source rotation and error handling

## Verification
- Android unit tests, debug APK compilation and Lint: passed.
- Installed over the existing APK on the physical Hisense (same package, no other applications).
- Real TV screen recording: 10-second, 1280x720 display capture with a camera change.
- A 600x390 center region outside informational overlays was checked over 22 sampled video frames. **21/21 consecutive sample pairs differed** (mean absolute differences from roughly 1.1 to 94.6), including visible motion following the camera change. This establishes that live frames were updating during the recording, unlike a static ImageView cover.
- This is not a 24-hour soak or a guarantee that all camera sources will be continuously responsive. If the stream stalls, the existing timeout/recovery logic remains responsible for cycling sources.

Never reintroduce a fullscreen captured last-frame overlay without an independently tested state machine and an automatic fail-safe that cannot obscure ongoing video.
