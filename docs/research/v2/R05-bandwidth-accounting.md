# R05 engineering feasibility — truthful network usage on Fire OS

**Outcome:** E1 research, GO to an isolated counter capability spike, **not** GO to deployment. This memo replaces vague “count GB” assurances with an accounting specification. [Existing dossier](../dossiers/R05-data-monitor.md).

## What is actually counted?

Android has multiple fundamentally different traffic measurement layers. To be credible, OpenVista must label them and NEVER add overlapping measurements as if they were disjoint:

| Measurement | Candidate signal | Coverage | What it cannot prove |
|---|---|---|---|
| Process UID bytes received/sent | `TrafficStats.getUidRxBytes(Process.myUid())` and `getUidTxBytes` | Bytes attributed by OS to app UID, if supported | Exact camera bytes, Wi-Fi vs cellular, requests made by external apps |
| Video media transfer | Media3 DataSource/TransferListener `onBytesTransferred` | Transfers through instrumented Media3 upstream; source attribution possible | All OS/IP/TLS overhead, requests made outside Media3, all clients (unless separately instrumented) |
| Weather/geocoding HTTP | Existing [ProviderHttp.kt](../../../player/src/main/java/com/openvista/prototype/ambient/ProviderHttp.kt) | App's own provider fetch bytes if wrapped | What NIC actually transmitted; DNS/TLS header overhead |
| Source nominal bitrate | Video format bitrate or media estimates | Rough forecast, perhaps missing/bad | Historical measured usage |
| Android NetworkStatsManager | Special platform permission/usage access | Potential transport/history details | Availability to nonprivileged Fire OS app |

**Correct UX:** “App network data” from UID, “Live media transferred” from instrumented data (when supported), “Other app requests” only if their counters are actually measured. The second is a *subset*, not something to add to the first. Network counters can represent OS accounting, not ISP billing quantities.

## Mathematical counter model

At monotonic sample t with UID counters R(t), T(t), accept a delta only when both values are supported, boot instance is stable, and `R(t) >= R(t-1)` and `T(t)>=T(t-1)`. If reset/unknown, start new epoch and record **missing interval**, never treat wrap as a large transfer. 64-bit signed `Long` and saturated arithmetic are required for overflow safety.

```text
Sample(state, uidRx, uidTx, elapsedMs, wallTime, bootHint)
  unsupported?      -> Unavailable, keep prior closed buckets
  counter decreased? -> EpochReset, gap; new baseline
  implausible epoch? -> Discontinuity, don't sum
  valid?             -> deltaRx = currentRx - previousRx
                        deltaTx = currentTx - previousTx
                        distribute across time window if evidence permits
```

Hourly/day buckets can be allocated at observation timestamp; if an interval straddles midnight without intermediate sample, either divide with an **explicit estimate** or mark the two day totals uncertain. Do not pretend all bytes transferred precisely at sample time. Device timezone change and DST can give a local day of 23/25 hours; store **UTC hour start** then render local aggregation.

Example: counter changes from Rx 2,000,000,000 to 2,075,000,000, Tx 80,000,000 to 84,000,000. Valid measured delta is 75,000,000 received + 4,000,000 sent **(decimal bytes)**. Display 75 MB and 4 MB decimal, or ~71.5 MiB/~3.8 MiB binary; state which unit. **These are illustrative fixtures, not device observations.**

## Existing storage hazard: avoid writing metrics into LibraryStore

[LibraryStore.kt](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) performs full-state `AtomicFile` JSON writes with validation and a bounded byte estimate. Frequent data counter writes would rewrite all sources, increase flash IO and tie a cosmetic feature to critical favorites/playlist durability. Use separate **small** atomic aggregate file, versioned schema, capped buckets and corruption recovery that doesn't affect playback. No WorkManager/boot receiver until foreground sampling proves it useful. If app is not running, counters may change due to a shared UID/background OS work; never pretend gaps are exact video use.

## Choices for performance and privacy

- Sampling only during active playback or diagnostic view, interval ~60 seconds **as a test parameter**, not guaranteed accuracy.
- Separate `getUid` and Media3 sample engines behind independently testable interfaces; avoid cross-process or UID migration assumptions.
- Retain daily totals at most 90 days, optionally 24-hour recent buckets and “Clear history”; deletion should remove only the counter file.
- Avoid camera-level usage by default because a library ID may identify a private household camera. For advanced source breakdown, use opaque local ID with user-controlled history clear, never URL/title/IP in logs or external reports.
- No `PACKAGE_USAGE_STATS`, VPN Service or packet capture for app-own data.
- Real usage spikes from switching sources, re-fetching playlists, retries or weather must be visible rather than automatically blamed on “streaming”. Some advanced download/transmit diagnostics may remain unavailable on OS.

## Falsifiable test fixtures

| Test | Input | Required result |
|---|---|---|
| M-01 | UID Rx=UNSUPPORTED/TX=UNSUPPORTED | “Unavailable”; no bogus zero |
| M-02 | two valid 64-bit counters | exact decimal deltas |
| M-03 | UID Rx decreases after restart | reset/gap, no negative consumption |
| M-04 | successful media transfer counted in both UID+Media3 | **no double counting** |
| M-05 | app paused but weather request occurs | UID increases, player subset stays unchanged |
| M-06 | 23-hour DST day / timezone change | UTC ledger stable; local display correct |
| M-07 | 2+GB single interval | no 32-bit overflow |
| M-08 | app force-stopped, reopened in 24h | mark missing/unknown period |
| M-09 | storage full | telemetry file fails safely; library unaffected |
| M-10 | 4K bitrate changes midstream | measured bytes reflect changes; forecast adapts only if observed |
| M-11 | network switches Wi-Fi to Ethernet | UID total unaffected; avoid unsupported per-link claims |
| M-12 | user clears data | no cached residual IDs in reports |

Future E2 commands must be documented with fake counter provider and known fixture payload; E3 on Fire OS 7 must compare actual OS UID results with known app transfer and measure overhead/edge cases. **No sample results exist today.**

## UI decisions and kill rule

Advanced → Diagnostics → Data usage: segmented Today/7 days/30 days; Received, Sent, Total, Measured coverage, Projection (explicit “estimate”). A 30-day projected estimate needs **sample hours and confidence interval**; no arbitrary extrapolation from single camera. Prefer static display when no traffic rather than polling while Settings stays open.

**Kill/hold:** if UID counters unsupported and source-level measurement excludes most media, show partial media-only estimator and do not market “total OpenVista usage”. If counter subsystem threatens playback/favorites durability, do not ship. Complexity S/M for app-only UID, M/L with robust per-source bytes. Open source proof: [AndroidX Media](https://github.com/androidx/media) has DataSource transfer listeners; Android SDK references need platform verification later. 
