# R07 engineering feasibility — event-sourced reliability without surveillance

**Outcome:** E1; investigate a bounded event model; never present a fake global uptime percentage. [Original dossier](../dossiers/R07-camera-health.md).

## Read the current source correctly

[PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) marks a stream AVAILABLE on `onRenderedFirstFrame`; it marks UNAVAILABLE on `failCurrent` unless the local network is offline. It treats `ERROR_CODE_BEHIND_LIVE_WINDOW` as a resynchronization opportunity up to a bounded count. It also uses a stable-playback callback and per-session failed set, fallback-to-local and exponential retry. [LibraryStore.updateAvailability](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) persists the last observed status/time into the main AtomicFile JSON. **Status is already implemented; new value is provenance, freshness and fairness**, not “add offline detection.”

## Fault taxonomy: wrong classification destroys product value

```text
Observation (time, sourceId, sessionGen, networkState, stage, reason):
  FirstFrame / StableForWindow / BufferingRecovered
  StartupTimeout / SourceHttpForbidden / SourceGone / ManifestInvalid
  DecoderNotSupported / LiveWindowRejoin / SegmentTimeout / Stall
  UserSkip / UserPause / LocalNetworkLost / TVSleep / AppBackground
```

Only a subset is valid evidence of camera-source reliability. On app-wide loss of internet, all cameras remain **unknown/last-known** not “dead.” A 403 on a tokenized feed is often a credential problem, not downtime. A codec mismatch belongs to local **device compatibility** and should not penalize source globally. A YouTube embed error (if manual viewer exists) is out of camera-health scope. A user skip is preference, not failure. `onRenderedFirstFrame` is **startup success**, not 24h stability. When decoder freezes post-first-frame, collect *recovery/stall* separately from startup.

## Reliability statistic design

Do not create an ungrounded scalar “95% reliable.” Proposed a local **Beta prior** only as experimental ranking score with transparent evidence:
- start `alpha=1,beta=1` → neutral with high uncertainty.
- count an observed healthy session as a **success** after stable-playback threshold, not every render event.
- count genuine source-specific failure as failure, distinguish auth/network/codec causes; at most one per unique attempt.
- apply time-based decay or trailing recent attempt window (e.g. 20 attempts / 14 days), not an unbounded permanent blacklist.
- compute posterior mean `alpha/(alpha+beta)` for internal R01 ranking only; high confidence requires more independent observations. Showing an internal score as real uptime is prohibited.

Examples **not measured**: one success gives alpha=2,beta=1 → 0.67 internal evidence score, but should show “Played once” rather than “67% uptime”; 10 successful sessions and 1 genuine source failure yields alpha=11,beta=2 → 0.846 with the chosen prior, not a published availability SLA. In practice use conservative thresholds, confidence bounds or labels.

## Storage/persistence and privacy

A single high-frequency event in [LibraryStore](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) would rewrite all playlists; do not persist stall ticks to the main user library. Prefer short in-memory session events and infrequent compact per-ID summary file (no URL/title/location). A source URI update invalidates or resets historical source failure stats while preserving user's saved camera ID; keep separate `sourceRevisionFingerprint` calculated locally but do not store credentials in the fingerprint or emit them to logs. Expire records for deleted sources and enforce retention (e.g. 30 days) independently. An app uninstall/private-data clear must remove observations.

## Counterfactual experiments

Compare **current plain Available/Unavailable** vs prior+decay policy on a **synthetic 14-day sequence** of 100 streams:
- 20 perfectly healthy, 15 intermittently stall, 10 require expired auth, 5 are codec incompatible on this TV, 20 have never played, 30 briefly offline during TV-wide network outage.
- Shuffle cadence 5min; retry 1,2,4,8,15 minutes; explicit manual reselect after cooldown.
- Count **play attempts ending before stable playback**, wall-clock scene time lost, times a recovered camera was unfairly skipped and how much history needed.
- Show ablation R07 without R01 (status only), R01 without R07 (category/solar only), combined R01+R07 (ranking), preserving old fallback.

Suggested guardrails: zero system-wide network events counted against all sources; all saved cameras remain manually selectable; no extra background requests; at least 20% reduction in unsuccessful auto-attempts in the synthetic benchmark *as a hypothesis*, followed by real device comparison. Anything more than a categorical status in user UI needs sampled evidence and confidence labels.

## Human-facing statuses

Never show “Offline forever.” Use:
- **Recently played** (timestamp local, only if saved)
- **Had trouble recently** (last event)
- **Not verified on this TV**
- **Needs source authorization** (if known, without revealing token)
- **Unsupported codec on this device** (if observed and clear)
- **Last checked [date]**, reset after 14d stale

Camera Details → Reliability & Retry. Advanced → Privacy/Data → Clear observed history. Don't overwhelm Library with colored health badges; if no observed attempts, no “healthy” badge.

## Dependencies and kill rule

R01 may *read* optional health, but health module must work when R01 disabled; R06 preview errors must not enter health; R13 token rotations must reset auth/URL-specific fault rather than losing favorites. E2 = deterministic model tests with event duplicate suppression and failure classification; E3 = 24h TV playback with captured legitimate source failures. **STOP** if it classifies provider auth as permanent camera downtime, creates incessant probe traffic, or turns unreleased source data into remote telemetry. 
