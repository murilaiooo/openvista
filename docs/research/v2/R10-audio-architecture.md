# R10 deep investigation — Independent ambient soundscapes

**Evidence:** E1. **Verdict:** bounded opt-in user-owned audio prototype is plausible; hardware/lifecycle acceptance remains HOLD. [Earlier dossier](../dossiers/R10-soundscapes.md).

## Actual baseline and user benefit

[PlayerScreen.kt](../../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt) already supports selecting an audio track *within* a current stream. [PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) uses one video-capable `ExoPlayer`; default `AppSettings.muted` is true and Dream uses a separate mute flag. None of this provides an **independent ambient audio track** through camera changes. Users need optional nature sounds during silent streams, independent from current camera audio. Existing silent-default behavior remains untouched.

## Competing implementations

| Strategy | User value | Cost/risk | Research decision |
|---|---|---|---|
| Camera's native audio | Already works, simplest | Depends on broadcaster, often street noise | Preserve |
| User-owned local audio-only player | Continuous independent ambiance | Second audio decoder, focus, lifecycle and mixing | Prototype |
| Software PCM mixer | Fine volume control | More code, underrun/drift, audio pipeline ownership | HOLD |
| Bundled tracks | Immediate content | Rights/licensing, APK size, no user content | HOLD/NO-GO |
| Cloud/YouTube music | Large selection | Ads, accounts, rights, background playback | NO-GO ambient |

A second **audio-only** player is not a second **video** decoder, but still consumes memory, audio focus and CPU; measure rather than assume. Source-family model has only CAMERA and VIDEO; audio-only content must not be silently imported as a video and played by the wrong queue.

## Explicit state machine / contract

```text
AudioMode: OFF | CAMERA | SOUNDSCAPE | MIX
OFF (default) → user chooses permitted local track → audio-only engine
Camera change → keep optional soundscape without restart
Video family selected → policy: pause soundscape unless user opts in
Audio-focus loss → pause/duck per Android platform
TV sleep / Dream exit / Home / Back / force stop → release audio
USB/SAF loss → stop and preserve explicit setting for reconnect
App mute → zero BOTH camera and soundscape levels immediately
```

Candidate small interface: `startAudio(uri, volume, loop)`, `setGain`, `stop`, `release`, composed by Player/Dream lifecycle. Avoid tying soundscape to `PlaybackSession.start`. Loop must not reopen a remote URL or restart video. Mixing requires audio-focus arbitration, no unconditional request for system master-volume change.

**Data cost example, not measurement:** 320 kb/s × 3,600s ÷ 8 = 144 MB/hour for a remote audio stream, ~1.15 GB over 8 hours; a locally repeating file uses no recurring internet payload. Prioritize local files and make Data Monitor count any future remote audio separately when possible.

## E2/ E3 test matrix

1. Default off on clean boot/Dream, including after network change.
2. 50 sequential camera switches while a 1-minute local loop plays; no audio restart.
3. Mute while buffering video; immediate silence.
4. Camera audio present + local audio present: exclusive vs Mix policy deterministic.
5. USB unplug and permission revocation, clip missing, corrupt or unsupported audio.
6. 2/10/30-minute AAC, MP3, Vorbis loops: measure gaps, drift and residual voices.
7. 3-hour idle screen/Dream switch, audio focus stolen by another TV app and restored.
8. Skip, Back, Home and force stop: verify zero residual background audio.
9. Resume after TV sleep: no surprise unmute, no stale audio session.
10. File name includes private path: not logged, screenshot text or remote report.
11. User sets volume 0/50/100: gain applies only to OpenVista.
12. No second video decoder or fallback interruption on a weak Fire OS device.

Success measurement: glitch count, audio engine heap/CPU, continuity through rotations, foreground focus correctness and decoder release. **None measured here.** Player → Audio → My Soundscape; Settings → Playback → audio defaults only after E3 and product approval. **STOP** if focus hijacks other TV apps, sound persists after leaving app, or mute semantics become unpredictable.
