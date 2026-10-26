# Hisense Full HD / 4K independent playback probe: 2026-10-26

**Device:** Hisense 58U6HF, Fire OS 7, API 28, Android ADB model AFTHA001. **Target:** same-package local development APK, with owner-selected media and settings preserved by off-repository backup.

## Reproducible private test media

- Generated a **48-second** H.264 1920x1080, 24 fps HLS test pattern with FFmpeg 9.0.2.
- Generated a **32-second** HEVC Main 3840x2160, 24 fps MPEG-DASH test pattern with FFmpeg 9.0.2.
- Both playlists/segments were served temporarily from the Windows PC's HTTP server on the same LAN. No links, manifests, M3U files, clips or live-camera sources were added to APK/repository assets. Two local-only test records were staged in the backed-up app library and are removed when testing finishes.

## Observed

1. The Hisense requested the 1080p HLS manifest and segments with HTTP 200, and the intended saturated test pattern visibly appeared in a 3840x2160 on-device screenshot. OpenVista retained foreground focus and remained alive.
2. The original FFmpeg-generated 4K DASH manifest specified an empty `codecs` attribute. The TV requested its MPD but not its HEVC initialization/segments, and showed a black frame. This was **an invalid QA fixture, not a demonstrated OpenVista decoder failure**.
3. The HEVC codec string was reconstructed from the fragment's `hvcC` metadata (`hev1.1.6.L150.90`) and put in the **private test MPD**. On replay, the Hisense requested the 4K initialization and segment files (HTTP 200), and the intended pattern appeared in an actual on-device screenshot.
4. After the finite 4K camera test reached its normal end, the Codex candidate falsely marked its record `UNAVAILABLE`. Source review found `PlaybackSession.onPlaybackStateChanged(STATE_ENDED)` calling `failCurrent()` for a normally completed camera clip. The QA branch now advances to the next source without marking a completed, frame-rendering clip as failed.
5. After the fix, Gradle module tests, debug assembly and Android Lint passed. The identical finite 4K fixture was replayed on the Hisense; the saved DASH test record remained `AVAILABLE` and the last-item reference moved to another source after the test. This is bounded end-of-stream recovery evidence.
6. `screensaver_enabled=0` stayed unchanged; no other apps/launchers were installed or modified. A memory snapshot exposed approximately 136,075 KiB of total PSS, but this single observation is **not** a memory growth/performance pass.

## Still not measured or accepted

No 24-hour soak, sustained 4K throughput, HDR, representative hardware frame-drop statistics, decoder CPU efficiency, thermal trend, rapid switching or full USB/permission-revocation matrix was established by this test. No assumption of support on other TVs. Screenshots and app-private test data are kept outside Git.
