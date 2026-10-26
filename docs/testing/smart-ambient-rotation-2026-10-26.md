# Smart ambient panels and per-mode rotation: QA record

**Date:** 2026-10-26
**Branch:** `feat/smart-ambient-rotation-20261026` based on independently reviewed QA candidate (PR #24).
**Target device:** Hisense 58U6HF / Fire OS 7 / API 28.
**Release status:** development-only, no public binary or stable-v1 certification.

## Requested behavior implemented

- Default live camera rotation: **one minute**, independent setting named *Live camera rotation (minutes)*.
- Default local-video rotation: **five minutes**, independent setting named *Local video rotation (minutes)*. Only rotates a multi-item queue while playing; naturally ended short clips still advance; paused or single-item playback does not create unnecessary reopen loops.
- Information in **two separate, rounded, translucent panels**: camera/video identity and camera-local clock on one card, weather/temperature and clearly attributed viewer fallback on a different card.
- Camera-local time uses an explicit valid IANA time zone on that camera. When absent, it is shown as unavailable rather than displaying a guessed time.
- Temperature uses camera coordinates when available and selected. Otherwise, when configured, it identifies **viewer** weather explicitly; it does not mislabel Tucson weather as coming from an unknown camera.
- **One-minute panel movement** enforced in screensaver and playback, with a separate *Randomize panel positions* control. Moving each panel to a different quadrant on every minute leaves the two cards in opposite corners. Deterministic cycling remains available when randomization is off.
- Preferences are exposed in Playback / Clock and overlays; the camera editor tells owners how to add city, coordinates and a time zone.
- Legacy saved settings lacking new fields adopt the new 1-minute live, 5-minute local-video, and moving/random positioning defaults. Existing saved sources, user stream URLs, folders, playlists and video checkpoints are not changed.

## Data availability on the Hisense

The private, read-only metadata count before this update showed **40 camera records, zero explicit IANA time zones, zero geographic coordinates, and zero saved city values**. This is a source-data limitation, not a weather-provider lookup failure. To show the actual **camera's** local time and temperature, edit a source and supply its metadata. The app does not infer geography from unknown stream URLs or bundle geographic metadata for users.

## Quality and burn-in scope

OpenVista's information cards are moved once per minute, including when randomization is off. This **reduces**, but does not guarantee elimination of panel burn-in or image retention. Text and channel watermarks embedded in user-supplied video, system UI and transient playback controls are outside this overlay positioning system. OLED manufacturers' own pixel-shift and panel-care controls remain relevant. See https://www.rtings.com/tv/tests/picture-quality/temporary-image-retention .

## Validation / limits

- Local Kotlin/JVM and Robolectric tests (including motion policy and separate preference persistence), Gradle debug assembly and Android Lint: passed locally in the reviewed development worktree. Final release and 24-hour gates remain separate.
- Device visual evidence: an initial screenshot showed a clipped scene panel; explicit panel sizing/measurement was corrected, rebuilt, installed in place and rechecked on the actual 3840x2160 Hisense. Both rounded panels rendered legibly without overlap. OpenVista DreamService automatically entered the OS's non-test Dreaming state, and system inactivity timers were restored to 180000 ms.
- Per-minute random shifts: screenshots 87 seconds apart (23:08:30 and 23:09:57 UTC) showed the scene panel moving bottom-right to bottom-left, and weather top-left to top-right. The live user-managed camera changed between those frames (Jurata to Karpacz), consistent with the new one-minute camera interval. This samples one transition, not full-duration or statistical random coverage.
- Exact 60-second repeated camera cadence, local-video 5-minute rotation, 24-hour endurance, EN/PT-BR accessibility, rapid switching and true camera-specific weather are **not certified** until longer hardware evidence and camera source metadata are available.
- Preserve C-01: no bundled videos, stream URLs or M3U lists. No changes to system screensaver selection, unrelated apps or installer security gates.
