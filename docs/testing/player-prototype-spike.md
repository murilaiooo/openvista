# Media3 player prototype: physical TV spike

**Scope:** the first working player Activity for issue #3 (user-managed input), issue #5 (Media3 playback) and issue #9 (scoped storage). Each issue retains its own acceptance criteria. This is local engineering-only work, not evidence of a complete stable-v1 player.

## Package and dependencies

- Target: Hisense 58U6HF, Fire OS 7 / Android 9 / API 28, `AFTHA001`.
- Test package: `com.openvista.prototype`, version `0.1.0-player-prototype`. It is **different** from `com.openvista.diagnostics`.
- Media3 ExoPlayer, HLS, DASH and UI: 1.11.1. `source-core` validates user-provided URL/list data.
- Explicit user selection only. The app ships with **no camera stream, stream URL, M3U list, video, sample media, user account, analytics or background collection**.
- The player requires Android's Internet permission for user-requested network playback; `ACCESS_NETWORK_STATE` and `WAKE_LOCK` are merged by Media3. HTTP cleartext is enabled for user-supplied legacy streams and must be reconsidered before public release. Never log credential-bearing URLs.

## First physical smoke test (2026-10-25)

| Check | Observed result |
| --- | --- |
| Debug APK build | PASS |
| Android Lint | PASS, no blocking errors; non-blocking warnings remain |
| Debug APK v2 signing | PASS |
| No bundled media or stream URLs | PASS (package content guard) |
| One-test-app policy | PASS: old `com.openvista.diagnostics` uninstalled **before** installing `com.openvista.prototype`; no other apps touched |
| APK installation | PASS |
| App launch / 4K UI rendering | PASS, actual TV screenshot visually inspected |
| D-pad Add/Import control | PASS, `Import a list` opened OEM `ACTION_OPEN_DOCUMENT` picker after focus checks |
| Physical remote-only picker import | NOT TESTED; do not assume the OEM picker is fully D-pad-accessible |
| `ACTION_OPEN_DOCUMENT_TREE` folder selection | IMPLEMENTED LOCALLY, **NOT PHYSICALLY TESTED** |
| HLS/DASH playback | NOT TESTED; no stream decoding/quality claims |
| Selected local video playback | IMPLEMENTED LOCALLY, **NOT PHYSICALLY TESTED** |
| Back/Stop lifecycle | NOT TESTED with active playback |
| DreamService | NOT TESTED and outside this Activity's scope |

### Guard against accidental unrelated app launches

The first test script sent D-pad input while an existing launcher, streaming app or screensaver held focus. That script was abandoned. Use `scripts/safe_player_adb_smoke.ps1` for future checks. It validates the TV model, confirms that exactly one OpenVista package is installed, and checks the actual foreground Activity **before and after each D-pad event**. If the Fire OS screensaver or an unrelated app holds focus, it exits without input. Once the system picker opens, **no further navigation keys are sent**.

Command (safe read-only status):

```powershell
.\scripts\safe_player_adb_smoke.ps1 -Action Inspect
```

Command for a controlled picker launch, only when the player actually has focus:

```powershell
.\scripts\safe_player_adb_smoke.ps1 -Action ImportPicker
```

The first physical focus-gated Import test succeeded. A subsequent synthetic HLS test attempt aborted before any input because the OEM `Sys2023:dream` screensaver was in the foreground. Re-launching the OpenVista Activity alone did not dismiss it. **Do not bypass the guard by sending D-pad, Back, Home or touch commands to the screensaver.**

### Synthetic playback fixture (not shipped)

A short, 720p/24fps H.264 HLS pattern was generated with local FFmpeg under `I:\codex\openvista-fixtures`. A temporary Python HTTP server bound to Windows loopback was confirmed HTTP 200, and an ephemeral ADB reverse tunnel was created for testing without changing router or firewall settings. The playback run **did not start** because the screensaver blocked the foreground focus gate. The HTTP server and reverse tunnel were then stopped/removed. No third-party streams were requested.

## Picker lifecycle refinement (2026-10-25)

The two system picker routes now use AndroidX `ActivityResultContracts.StartActivityForResult`, with callbacks registered unconditionally when the Activity is instantiated. This replaces the deprecated `startActivityForResult` / `onActivityResult` hooks and preserves the same explicit, owner-selected document/folder actions.

The app only uses the transient read grant needed for the current in-memory session. It does **not** retain folder grants or source lists across device restarts yet, so the owner must select a folder again after restarting. Grant persistence and revocation remain unverified work under issue #9. No broad storage permission was added.

The implementation compiled and passed local Android Lint and regression suites after this migration. An Android 9 (API 28) Robolectric harness with **5/5 local Activity tests and 2/2 SAF scanner tests** verifies that importing a list and selecting a folder require user interaction, request **read-only** document access, keep an empty library disabled for playback, and do not launch a picker for an invalid direct URL. Robolectric is a simulated Android environment; physical Fire OS picker completion and return behavior are still unverified on this upgraded code.

Commands to reproduce: `./gradlew :player:testDebugUnitTest :source-core:test :player:assembleDebug :player:lintDebug --no-daemon`. Test instrumentation remains local; no GitHub Actions were used.

## User-controlled sound and scoped folder regression tests

Decision **D-06** requires playback to begin muted while allowing viewers to enable audio. The prototype now has an explicit TV-focusable **Sound: Off / Sound: On** action. The initial session volume remains zero, enabling sound sets ExoPlayer volume to 1, and switching it off sets volume back to zero without connecting to a new source. A Robolectric user-interaction regression test first failed with the missing control and then passed after implementation.

The Android 9 JVM harness also exercises the SAF folder scanner against a synthetic `ContentProvider`: a user-selected tree contains two recognized videos, an excluded image, and a nested folder. The scan returns only the two videos and the URI authority from the granted fixture tree. A cancelled scan returns without reading content. **These are simulated provider tests, not evidence of physical Fire OS USB compatibility or successful media decoding.**

## Physical HLS and DASH playback (2026-10-25, Arizona time)

The owner explicitly opened the player and later authorized **temporarily disabling the TV screensaver** to test playback. The following is real evidence on **Hisense 58U6HF / Fire OS 7 / Android 9 / AFTHA001**; it is not general device compatibility or long-run certification.

| User-provided test | Physical result | Evidence |
| --- | --- | --- |
| H.264 HLS, synthetic 720p / 24 fps / 10 seconds | **PASS — Media3 READY** | OpenVista displayed `Playing selected source (muted)`; the local HTTP server returned 200 for the manifest and all five `.ts` segments |
| H.264 MPEG-DASH, synthetic 720p / 24 fps / 10 seconds | **PASS — playback ended at 00:10 / 00:10** | OpenVista showed visible color test frames and `Playback ended`; localhost server returned HTTP 200 for the `.mpd`, fMP4 init segment and five media segments |
| Physical USB folder/video playback, user-selected M3U, stream interruption/recovery, HDR/4K and 24-hour soak | **NOT TESTED** | These remain independent acceptance tasks under #3, #5 and #9 |

All stream assets were created using local FFmpeg outside the repository in `I:\codex\openvista-fixtures` and served only during testing from a loopback-bound Python HTTP server via an ephemeral ADB reverse tunnel. No streams, sample media, playlists, or test media URLs were packaged in the app. The test is not proof of arbitrary HLS/DASH streams, all codecs, or live network resilience.

**Physical-test pitfalls and safe recovery:** Fire OS may turn the **display OFF / `mWakefulness=Asleep`** even after `secure/screensaver_enabled=0`; the Activity may still report foreground focus while screenshots are fully black and `uiautomator dump` fails. Confirm display/wakefulness separately before drawing UI conclusions. On this owner-authorized run a `KEYCODE_WAKEUP` restored the panel while OpenVista was foreground. The OEM virtual keyboard also intercepted Play/format button touches; it introduced a trailing comma into the URL. With OpenVista foreground and the input method visibly open, one Delete followed by Back dismissed the IME; only then were DASH and Play buttons tapped. Guard foreground before/after every input and verify `mInputShown=false` before tapping player controls. Do not navigate OEM launchers, unrelated apps, or dialogs.

**Restoration verified:** Original `secure/screensaver_enabled=1` was backed up, set to `0` temporarily, and restored to `1` after playback. The existing `tv.dumbtv.wallpaperdream/.PhotoDream` configuration was preserved. Only the OpenVista playback was stopped; the temporary local HTTP listener was stopped, its ADB reverse rule removed, and the sole installed test package remained `com.openvista.prototype`. No new APK was installed as part of this physical test.

## Owner-reported additional acceptance (2026-10-25)

The owner subsequently confirmed in chat that the remaining practical checks for **local video / USB, extended playback endurance, and failure recovery** were **tested and approved**. This is recorded as **owner-reported acceptance**, distinct from the tool-observed, reproducible 10-second HLS/DASH tests documented above.

**Evidence limit:** no duration, codec/container-by-container results, USB provider identifiers, sampled CPU/RAM measurements, dropped-frame totals, failure-injection cases, or recordings were supplied with this confirmation. Do not infer the full **24-hour G-07** benchmark, persisted SAF URI grants across reboot, USB removal/reinsertion behavior, M3U picker completion or public v1 release criteria from this owner report alone. The separate GitHub #3/#5/#9 technical tasks can keep their detailed evidence requirements while this engineering prototype is integrated.

## Testing and rollback

```powershell
cd I:\codex\openvista
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :player:assembleDebug :player:lintDebug :source-core:test --no-daemon
```

Local APK: `player/build/outputs/apk/debug/player-debug.apk`. Only the owner-authorized one-app installation was performed. For rollback, uninstall **only** `com.openvista.prototype`. Do not uninstall or interact with the launcher, existing streaming apps, or OEM screensaver. Do not publish APK binaries to GitHub Releases.

## Remaining acceptance

- Verify playback with owner-generated HLS and DASH assets while the OpenVista Activity has foreground focus, inspect decoding/quality/errors, and test resume/Back.
- Verify keyboard entry and error states without accidentally interacting with system dialogs or other apps.
- Verify user-selected SAF folders on the physical TV, including the bounded 100-video / 500-document / depth-3 scanner and Play first video action. Media decoding and URI permissions are not established by Kotlin compilation.
- Complete picker M3U import with a user-selected local fixture and validate handling of missing/unsupported providers. Verify actual USB and folder traversal separately under #9.
- Add integration tests for user input and playback lifecycle before integrating the full player into `main`.
