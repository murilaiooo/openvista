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
| `ACTION_OPEN_DOCUMENT_TREE` folder selection | NOT TESTED |
| HLS/DASH playback | NOT TESTED; no stream decoding/quality claims |
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
- Complete picker M3U import with a user-selected local fixture and validate handling of missing/unsupported providers. Verify actual USB and folder traversal separately under #9.
- Add integration tests for user input and playback lifecycle before integrating the full player into `main`.
