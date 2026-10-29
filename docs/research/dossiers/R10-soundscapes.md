# R10 — Ambient Soundscapes: optional independent audio for silent scenes

**Verdict:** HOLD. Explore an E2 experiment with **user-owned or properly licensed local audio**, no bundled copyrighted recordings. Evidence E1 for app design; no audio-mixing benchmarks. [Research #45](https://github.com/murilaiooo/openvista/issues/45).

## Problem, user journeys
Many live camera feeds lack meaningful audio, and viewers want quiet atmosphere or their own ocean/rain/forest ambience. The scene should remain silent by default; switching camera must not restart a calm audio loop abruptly. Stories: select my own sound file, set independent volume, mute instantly, keep soundscape through camera rotation, pause when TV sleeps, choose silent fallback, and remove permission safely.

## Existing playback
[PlayerScreen.kt](../../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt) offers existing audio track selection on the one `ExoPlayer`. [AppSettings](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) has `muted=true`, while [PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) owns the main player and Dream lifecycle. No independent long-running soundscape session exists. This is **not** equivalent to selecting an audio track of the source.

## Approaches, trade-offs
1. **User-selected local AAC/MP3/OGG track played in a dedicated audio-only engine:** practical, but an extra audio decoder/session adds RAM, audio-focus, synchronization and lifecycle complexity. Must not allocate a second *video* decoder or steal background focus.
2. **Platform-level PCM mixer**: fine gain controls but unnecessary complexity, high maintenance and possible underruns. Avoid before E2 data.
3. **Bundled loop package or online sound service:** raises licensing, download and update costs; **NO-GO for MVP research scope**.
4. **Native audio tracks from camera**: already exist; not a substitute for independent ambience.

Proposed audio policy: source audio and ambience each have mute/volume, with 0 by default; optional exclusive vs mix mode; no surprise sound after app relaunch; explicit 0–100 gain interpreted as app-relative, not TV master volume. On incoming calls/notifications and system audio focus, pause/duck according to Android policies; never override OS. Clock drifts and gap-free looping need actual codec tests.

## Research experiment
Use permissioned local 2-, 10-, and 30-minute loops in MP3, AAC, and Vorbis; synthetic camera HLS with/without audio; 500 source changes, screen sleep/resume, rapid mute, Android focus loss/gain, USB removal. Record audio glitch count, memory/CPU while 1080p HLS decodes, audio residual after stop, silence/mute correctness and network use (should be zero for local tracks). Repeat in Dream if OS delivers lifecycle. No duration claim until tested on physical device.

## IA
Player OK → Audio → Camera sound / My Soundscape; Settings → Playback → Sound defaults. Under My Videos, user may select **audio-only** file explicitly without turning normal video playlists into mixed-source queues. Default: Off. Avoid a permanent sound indicator on ambient screen.

## Legal, privacy, stop
User must hold usage rights; local file path and playback history stay on TV. Never separate audio from unofficial YouTube videos or stream unofficial sound libraries. Reject if player stop leaves audio running, if audio-focus bugs affect other TV apps, or if hardware playback stability declines. Reference: [AndroidX Media repository](https://github.com/androidx/media); official platform audio-focus docs need direct confirmation beyond GitHub research.