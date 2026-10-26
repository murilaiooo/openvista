# Hisense 58U6HF / Fire OS 7 — 24-hour soak protocol

**Status:** Draft test method approved only as a *planning artifact*, not an executed test or owner-approved numerical thresholds. Stable-v1 gate G07 and D-76 require a **documented 24-hour run**. Hardware/software build must be recorded first.

The read-only Windows sampler in [`scripts/hisense-soak-monitor.ps1`](../../scripts/hisense-soak-monitor.ps1) can collect UTC, process liveness, foreground state, wakefulness, PSS and process CPU ticks while a person operates the TV. Its two-sample smoke test passed; **no 24-hour run has been executed**. Decoder dropped frames and thermals remain NOT_MEASURABLE unless a verified platform mechanism is added.

## Before testing: freeze acceptance criteria

1. Record exact firmware/API, OpenVista version, display mode, network conditions, chosen playlists/cameras, storage type, decoding capabilities and power/schedule state.
2. Capture a **30-minute representative baseline**: steady-state CPU/memory, decoder dropped frames, buffering/seek rates and network throughput in local HD, online HD and 4K where supported. Do not arbitrarily force unsupported 4K.
3. **Write the pass limits before the 24-hour test**: allowed crashes (target **zero**), unrecoverable black-screen episodes (target **zero**), no infinite retry loop, agreed dropped-frame/rebuffering thresholds per codec/resolution, RAM slope and steady-state CPU limits relative to baseline. Owner may approve achievable constraints after observing device metrics.
4. Define safety: prevent screen damage with approved overlay shifts and observe OEM sleep/power policies; don't disable unrelated TV energy protections.

## Run matrix

| Time / segment | Exercise | Evidence |
| --- | --- | --- |
| Warmup | Manual Activity, source/picker setup; record startup time/focus | App & device logs, initial measurements |
| Continuous | Camera playlist rotation (default 5 min, configured shuffled) and/or authorized local looping media | Time-stamped frames-decoded/dropped, audio mute, rebuffer events, CPU/RAM/temp where available |
| Fault injection | Individual camera failure, complete network loss, restore network while local video plays | No busy loop; current fallback video finishes then live camera returns unless user elects stay local |
| Storage changes | USB removal/reinsert during playback, expired URI/file changed | User explanation and alternate-source selection, no hang |
| Power/lifecycle | Idle/Dream activation when OEM supports it, suspend/wake/background | No unexpected boot autostart or hidden decoder work |
| Final | Resume last scene/local file timestamp, settings retained | Confirm state and sanitized final report |

Collect samples at an explicit fixed interval (propose every **60 seconds**, adjusted to battery/power and observation overhead) and event markers for transitions/errors. At 5-minute rotation, 24 hours implies ~288 expected scene changes if no deliberate pauses; record actual counts rather than infer success.

## Stop / fail / rerun rules

- Crash, persistent black video, missing app responsiveness, all-broken-source infinite loop, excessive CPU/RAM/thermal growth or unauthorized installation automatically fails relevant gate and requires root-cause evidence before rerun.
- A metric unavailable on the actual Fire OS build is recorded **NOT MEASURABLE**, not zero.
- Repair and restart a full 24-hour run after a gate-changing bug fix; do not concatenate disjoint test periods into a fake soak.
- Verify user-provided source handling and network availability separately: player stability does not establish any third-party redistribution rights, and release builds must contain no media/test feeds.

## Evidence artifact contract

Write one dated `docs/testing/hisense-soak-YYYY-MM-DD.md` with predeclared numeric limits, command provenance, sanitized raw-output locations, start/end timestamps, firmware/app build, observed values, incident timeline, pass/fail result and reviewer. Do **not** check in large proprietary videos, private streams or unredacted device logs.
