# R01/R02 technical investigation — Smart Director and Sun Chaser

**Owner items:** R01 automatic scenes, R02 Sun Chaser. **Evidence:** E1 source and algorithm design, **no run**. **Decision:** investigate pure algorithms before UI; do not implement as a coupled monster mode. [Original R01](../dossiers/R01-smart-director.md) • [Original R02](../dossiers/R02-sun-chaser.md).

## 1. Exact current control path and the engineering risk

[PlaybackSession.play](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) re-resolves IDs through `LibraryStore`, filters family, shuffles initial camera queue if requested, then invokes `start(item)`. `advance(direction)` walks the existing queue and skips the session-local `failed` IDs. Successful first frame clears startup timeout and marks availability; [scheduleRotation](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) uses `SystemClock.elapsedRealtime()` and the 1–60 minute setting. `PlaybackSnapshot` restores IDs and fallback state after Activity ↔ Dream handoff.

**Nonnegotiable invariant:** The director must never call `player.setMediaItem` directly, reset retries, override manual Next, switch family without explicit selection, drop previously user-ordered playlist IDs or sample a remote camera to infer quality. It is a **queue selection policy**, not another player. The solar clock is geographic context, **not a weather or image classifier**.

## 2. Decouple ranking from player events: one small interface

Proposal for a throwaway pure Kotlin contract (not approved code):

```kotlin
data class SceneEvidence(
    val id: String,
    val category: String?,
    val locationKey: String?,
    val coordinates: Pair<Double, Double>?,
    val lastShownElapsedMs: Long?,
    val healthConfidence: Double?, // nullable = unknown
    val userPinnedRank: Int?
)
data class SceneContext(
    val instant: Instant,
    val elapsedRealtimeMs: Long,
    val requestedMode: Mode,
    val lastIds: List<String>,
    val explicitPlaylistOrder: List<String>?
)
data class SceneChoice(val id: String, val reasons: List<Reason>)
fun chooseNext(candidates: List<SceneEvidence>, context: SceneContext): SceneChoice?
```

`elapsedRealtimeMs` represents interval/backoff, never solar calculation; `Instant` represents position of the Sun, never retry timers. Explicit playlist selection outranks all rules. Require that `choice.id` belongs to current candidates, with no fallback beyond `PlaybackSession` existing logic. If selector throws or all eligible candidates lack metadata, use baseline sequential/shuffle and emit a **sanitized local** explanation.

## 3. Four selector strategies contrasted

| Alternative | Computational cost | UX explanation | Determinism | Failure mode | Assessment |
|---|---|---|---|---|---|
| One-pass shuffle (current) | O(n) once, O(1) per next | trivial | random seed uncontrolled | repeats cities and broken sources | keep baseline |
| Hard filtering + weighted ranking | O(n) per switch | good (“Daylight; not recently shown”) | yes with stable seed/ties | starvation/empty subset | **best first prototype** |
| Stateful bandit learner | O(n) plus persistent observation | poor, implicit personal profiling | difficult | reinforcement bias, data/privacy | reject for v1 |
| Cloud AI/vision scoring | network+GPU/cost | subjective | not reliably | unavailable internet, private URLs | reject |

Candidate bounded score **for experimentation only** (do not ship these weights): `S = 3.0*(daylightMatch) + 2.0*(categoryVariety) + 1.5*(recentlyHealthy) - 4.0*(repeatLast3) - 2.0*(sameRegionLast2)`. Unknown health/coordinates contributes zero, not penalty. If no daylight match, relax that soft constraint before abandoning the user. Strong constraints: source belongs to chosen family and currently allowed playlist; **no irreversible blacklist**.

**Fairness tests:** prove each available eligible item is visited within a bounded number of rotations when the user has not explicitly filtered. Need explore max-starvation for ties, missing geodata and favorited sources. A bounded recent-list (3–5 IDs) in memory is sufficient; avoid a new high-write persistence store at first.

## 4. Solar geometry: what can actually be inferred

For latitude φ, solar declination δ and hour angle H, approximate altitude `h` obeys
`sin(h) = sin(φ)sin(δ) + cos(φ)cos(δ)cos(H)`.
This relation by itself is **not a complete implementation**: accurate δ and solar time require dated ephemeris, equation-of-time, longitude, UTC conversion and optional refraction. Local `timeZone` is for display, not for physical solar geometry. Sunrise horizon definition depends on solar disc/refraction (common standard zenith ~90.833°), elevation and topology. “Golden hour” is **a photographic heuristic, not astronomic law**, e.g. Sun altitude around 0°–6°; do not promise the visible image contains the sun.

Solar output candidate:

```text
SolarEstimate =
  Known(instant, elevationDeg, azimuthDeg, phase, approach, uncertainty)
  | PolarDay
  | PolarNight
  | NoEventInWindow
  | UnknownCoordinates
  | InvalidCoordinates
```

For moving-camera azimuth unknown, no reliable “camera faces sunrise” claim is possible. Clouds/haze/weather/weather station location can differ from camera image; keep overlays “estimated daylight at location” and use solar relevance only for scene **candidate selection**. Existing [OpenMeteoForecastProvider.kt](../../../player/src/main/java/com/openvista/prototype/ambient/OpenMeteoForecastProvider.kt) already reads provider `sunrise/sunset` in forecast data, but its cache/rate and outages make it an optional reference, not mandatory runtime selector. Check offline astronomical implementation against published ephemeris before implementation; external NOAA pages not fetched due to GitHub-only constraint.

## 5. Adversarial test dataset to create and preserve in code (future prototype)

| Fixture | Specific input | Expected behavior |
|---|---|---|
| AST-01 | 0°N, 0°E at March equinox UTC noon | roughly high Sun; no timezone dependence |
| AST-02 | 70°N near June solstice | no fabricated sunset on polar day |
| AST-03 | 70°N near December solstice | no fabricated sunrise on polar night |
| AST-04 | 33°N, −111°W, summer and winter | sunrise/sunset change smoothly, never reuse yesterday |
| AST-05 | 180° longitude boundary | global UTC instant yields consistent phase |
| AST-06 | latitude 91° / longitude NaN | invalid, not crash |
| AST-07 | valid coords / missing zone | solar still works |
| AST-08 | same coords, different display timezones | identical solar elevation/azimuth |
| SEL-01 | 1 healthy saved camera | select without cycling/restart storm |
| SEL-02 | 10 cameras, all coordinates unknown | gracefully degrade to baseline |
| SEL-03 | 50 entries, 20 same city, 5 countries | penalize repetitive city without geography bias |
| SEL-04 | 20 dead, 5 reliable, network offline | no remote probe; existing fallback |
| SEL-05 | user manually chooses source while automatic mode active | preserve manual intent until defined boundary |
| SEL-06 | DST spring/fall, 23/25-hour local day | no duplicate/omitted rotation event |
| SEL-07 | app → Dream → app transition | restore selected ID and no extra decoder |
| SEL-08 | source removed mid-candidate calculation | revalidate ID before playback, use baseline if gone |
| SEL-09 | all valid sources failed | no busy retry; defer to existing backoff |
| SEL-10 | user-selected ordered playlist | never reorder silently |

**Suggested, unmeasured gates:** pure select 500 sources in <10ms on a modest JVM emulator; no extra network request per selection; repeat rate less than baseline in 100 seeded 8h simulations; zero source unavailable solely because geolocation absent; 100% restoration of explicit user playlist order. Device test then measures first-frame delay, player restarts, UX focus and 24h soak.

## 6. State machine and timing

```text
MANUAL_PLAY > MANUAL_PLAYLIST > (SMART_MODE with explicit opt-in) > BASELINE_ROTATION
Connected and playable? → candidate policy → PlaybackSession.start → first frame
No first frame? → current PlaybackSession timeout/retry; record R07 observation
Network-wide offline? → existing local fallback; DO NOT blame sources
User Next? → current behavior; optional scoring on next transition only
App background? → no background algorithm; release/snapshot existing player
```

Use separate **wall clock** for solar and routines and **monotonic elapsed clock** for timer correctness. Don't persist season-sensitive solar phase in source metadata. Every 5 minutes, a player transition may select a *new* camera; solar phase is computed on demand. No location changes should secretly re-order a manual playlist.

## 7. UX proposal and negative acceptance

Home remains three cards. Cameras → Mode (Shuffle / Smart / Daylight / Follow sunrise / Follow sunset); initially show only Shuffle and one optional Smart mode; advanced solar choices nested, not nine buttons. Player OK → transient “Mode” menu, does not permanently show score or reason. No screen-wide “AI recommendations” claim. If no daylight cameras, show “No daylight matches; play all instead?” and preserve last scene until user choice; do not turn screen black. Clear local history independently of user favorites.

**Recommendation:** R02 astronomy as standalone pure tests; R07 observed health independent; combine into R01 only if both prove value. E2 and real hardware still absent. Approximate effort: R02 M, R01 M/L; primary risks B2/B5/B7. Rejection: user-control loss, false “sunny” labels, more decoder stalls, biased rotation or excessively complex Settings.
