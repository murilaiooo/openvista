# R07 — Camera Health: fair, time-bounded reliability memory

**Verdict:** GO for unit-level evaluation; **HOLD** feature adoption. Evidence **E1**, grounded in OpenVista playback source; no telemetry or observed uptime data collected. [Research #43](https://github.com/murilaiooo/openvista/issues/43).

## User problem, not a monitoring service
Rotation currently avoids a camera that fails in the active pass, but an unreliable stream can repeatedly waste startup time on future sessions. A small, local memory should prioritize **observed recent good experiences**, not claim to monitor a global camera network. A legitimate stream returning 403 because its access token expired is **not** physically offline; loss of the TV's internet should not count against any stream.

## Current implementation and missed opportunity
[PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) maintains a failed-ID set and bounded retries, marks a source available after its first rendered frame and unavailable on specific failures, and skips failures until retry. [LibraryItem](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) stores `Availability` and `observedAt`, but no bounded historical signal; [NetworkMonitor.kt](../../../player/src/main/java/com/openvista/prototype/playback/NetworkMonitor.kt) observes TV connectivity. The recovery loop deliberately uses the **same decoder** to probe while local fallback plays; preserve this.

## Define reliability precisely
Separate `lastObservation = success | startupTimeout | stall | HTTP rejection | decoderUnsupported | userSkip | localOffline | notObserved` and **reason confidence**. Count only first-frame successes, sustained playback and specific per-source faults. User Next is not a network failure. Device-wide network loss, app crash, screen-off, user pause, OS background and unsupported display codec should **not** poison source reliability. Unknown is different from unhealthy. Never perform a mass network poll or nightly probe of all user-entered URLs.

Proposed bounded record (research schema, not adopted): `sourceStableId, lastObservedAt, recentSuccesses, recentSourceFailures, EWMAStartupMs, lastFailureReason, cooldownUntil`. Restrict storage to 1 item per saved stream, cap counters and record age (e.g. 14 days); decay history over time and clear on major source-URL revision/credential change. Privacy: stream URI, credentials, query tokens and location are **never** stored in health logs.

## Options and recommendation
A. Binary current `Availability` only: zero extra storage but insufficient memory. B. **Decay-weighted observed health** (recommended for E2 trial): fair rankings and self-healing after a recovery. C. Global remote health service: reject, conflicts with private user URLs and no account/backend model. For ordered playlists, report status but never silently re-sort unless user opts into R01.

## Experiment plan
Deterministic 72-hour simulated timeline of 100 cameras: intermittent WLAN, 10% expired credentials, 10% permanent bad codec, 10% flaky segments, some sources only available at daytime. Simulate play, skip, recover, rename, delete, URL replacement, restart and 30-day passage. Compare baseline against R07 cooldown and R01 scoring. Measure unnecessary startup attempts, fairness after recovery, loss of all good candidates, and size of persistent state. **Research targets:** zero false source faults during TV-wide loss; cooldown expiry always re-enables source; 100% manual override; no decoder/network requests solely for health. Tests must be run later and results recorded, not claimed here.

## User-facing design
Camera details → “Observed recently: Played successfully / Recently had trouble / Not checked”. Advanced → “Clear playback history” and optional health weighting. Avoid unverifiable “99% uptime” scores, green badges when data is stale, and alarming permanent red labels. A failed request should still have a Retry command.

## Gates
Independent real Fire OS weak-network tests and 24h soak required; verify source credentials and retention. Do not ship until reliability actually decreases interruptions without hiding recovering cameras. Compare [Aerial Views](https://github.com/theothernt/AerialViews) as alternate TV content handling, not proof of equivalent failure policies.