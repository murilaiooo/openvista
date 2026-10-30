# R01 — Smart Director: explainable automatic scene selection

**Research verdict:** GO for isolated design/prototype research; HOLD product adoption. Evidence **E1: repository code and design analysis**, no experiment or field test. [Parent #42](https://github.com/murilaiooo/openvista/issues/42), [track #43](https://github.com/murilaiooo/openvista/issues/43).

## Real user problem and testable hypothesis
Five-minute shuffle is simple but can serve the same city twice, select a camera already known to fail, or show a dark waterfront while daylight alternatives exist. Hypothesis: an **opt-in, deterministic selector** improves the number of satisfactory uninterrupted scenes per session without higher network use, extra decoders or user effort. It must **never imply computer vision or AI footage analysis**.

**User stories:** (1) start a good rotation with one action; (2) preserve manual playlist order on request; (3) see a brief understandable reason for the chosen scene; (4) prevent repeat destinations; (5) override and skip without training a hidden profile; (6) continue playback with missing metadata; (7) disable all ranking and return to existing shuffle.

## Current implementation, exact seams
- [PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt): `play()` constructs one-family queue and shuffles once; `advance()` walks that queue skipping `failed`; `scheduleRotation()` uses `rotationMinutes`; fallback/recovery must remain the sole owner of the ExoPlayer. `PlaybackSnapshot` restores stable IDs.
- [LibraryModels.kt](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) stores name, city/country/timezone/coordinates, favorite and observed availability; no user satisfaction rating or ranking history.
- [LibraryStore.kt](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) owns durable user data; the director should not rewrite playlist order or sources.
- [NetworkMonitor.kt](../../../player/src/main/java/com/openvista/prototype/playback/NetworkMonitor.kt) distinguishes connectivity from source reachability.

**Design proposal, not a new approved interface:** pure `selectNext(SceneCandidates, CurrentContext, UserPolicy, RecentHistory): RankedSelection`, in front of `start(item)`/advance, returning a chosen saved ID plus explainable reasons. Keep the Media3 lifecycle and availability retry intact; manual Next has priority, and a failed selection falls back to normal queue traversal. Do not add a generic rule engine until two actual independent selector implementations justify a seam (per [codebase-design skill](../../../.agents/skills/codebase-design/SKILL.md)).

## Three candidate approaches
1. **Rule-based transparent rotation (preferred):** exclude recently failed; bounded recent-ID and location penalties; optional daylight preference; prioritize existing favorites if explicitly enabled; uniform coverage so never visited cameras are not starved.
2. **Weighted randomized:** seed + capped weights avoids deterministic repetition but makes reason messages less straightforward. Use only if A/B evidence favors it.
3. **On-device visual ML:** image classification could identify interesting scenes, but requires extra image processing, licenses, privacy and energy; **reject for the initial phase**.

Example score model for research only: `eligible && !recentlyFailed` gate, then capped scores for freshness, preferred category, approximate local solar phase, quality/health confidence and time since shown. Unknown metadata is neutral, **not negative**. Hard precedence: explicit manual selection > user-selected ordered playlist > opt-in director > existing shuffle > established offline fallback. Do not accidentally privilege USA/Europe due to richer timezone records.

## Concrete experiment
Build 50 synthetic records: 10 each of coastal, urban, mountain, wildlife, mixed; 20% with missing coordinates, 15% with recent outage, multiple same-city entries; simulate 12-hour rotations and seeded replays. Compare baseline shuffle against candidate on failed selection rate, repeated source within last 3 scenes, category diversity, per-source starvation, selection runtime and manual override rate. **Proposed targets, not measured:** selection CPU under 5ms at 500 saved sources on test JVM; zero extra network requests; zero changes to on-disk source metadata; avoid same camera within last 3 slots when alternatives exist. Test one, zero and all-failed cameras; empty categories; DST; restore; delete/edit mid-rotation; network outage.

## User interface and privacy
Cameras → Scene mode → **Shuffle** (default) / **Smart Director** (opt-in); Player → Mode / “Why this scene?”. Playback settings expose only a simple preference (prioritize light / favorites / variety), with advanced thresholds hidden. Do not show rationale as a permanent overlay; no cloud scoring, URLs in logs, implicit telemetry, or algorithmic uploads.

## Stop conditions, dependencies and next evidence
Depends on R07 health records and optionally R02 solar phase; must not depend on weather API availability. **HOLD** if repeated start/stop raises decoder crashes, ranking cannot be explained or the player loses its reliable fallback. Next: isolated unit-level comparison, then real Fire OS 7 switch latency and 24h soak with core stability gates. **No test execution occurred in this research pass.**