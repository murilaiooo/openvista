# DreamService-only audio mute (Hisense QA)

**Date:** 2026-10-26
**Branch:** `feat/dual-location-forecast-20261026` / PR #30
**Scope:** The owner requested that the OpenVista system screensaver never play video/audio sounds, without muting or changing the ordinary app.

## Implementation

- A new `PlaybackSession(..., screensaverSilent = true)` option is passed **only** by `OpenVistaDreamService`. Ordinary `PlayerScreen` playback still uses the original default `false`.
- Inside a silent DreamService session, Media3 `ExoPlayer.volume` is **0.0** and `C.TRACK_TYPE_AUDIO` is disabled at the track selection layer. These values are applied before any media item is started and retained across camera/video rotation, fallback and playlist changes.
- Silent-mode `setMuted()` is a no-op, explicitly preventing changes to the user's persisted `AppSettings.muted`. The standard player retains its existing audio on/off control.
- Remove the DreamService sound toggle and audio-track selector so no UI action suggests audio can be switched back on in the screensaver.
- No changes to global Android audio streams, HDMI/Fire TV system volume, remote volume keys, home launcher, screensaver component, content URLs or stored source/playlist data.

## Evidence

- Offline Windows Gradle tests, debug APK build, and Android Lint: **PASS**.
- PlaybackAudioPolicy unit tests assert dream sessions stay at zero volume regardless of the user's regular-player preference, and the regular player still honors that preference.
- Full physical Hisense audio/noise test after install remains **pending**. Build/static checks cannot certify actual audio path or external HDMI/eARC receivers.
- Before/after APK and app data must be backed up; update in place with the same signing identity; verify the existing 43 sources and 2 playlists, and ensure the screensaver stays selected.

**Owner acceptance:** With the DreamService active and a normally audible source, the physical TV/speakers should remain silent. Exit DreamService and play an audible source in regular mode with Sound On; audio should work there.
