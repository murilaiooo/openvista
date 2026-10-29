# R02 — Sun Chaser: solar-aware selection without guessing image content

**Research verdict:** GO to pure local astronomical prototype; HOLD UI/product adoption. **E1** from current source and earlier hypothesis only. [Track #43](https://github.com/murilaiooo/openvista/issues/43).

## Why users care
A camera is more enjoyable when its location is experiencing the kind of light the viewer wants. “Show sunrise somewhere”, “Follow the sunset”, or “Keep me in daylight” is substantially more useful than sorting alphabetically. This does not guarantee a sunny image: cloudy skies, indoor cameras, pointing direction, stale feeds and local obstruction can invalidate what the astronomy predicts.

## Starting point
[LibraryItem](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) already has optional latitude/longitude/timeZone; [AmbientOverlay](../../../player/src/main/java/com/openvista/prototype/ambient/AmbientOverlay.kt) displays camera and viewer clock/weather; [OpenMeteoForecastProvider](../../../player/src/main/java/com/openvista/prototype/ambient/OpenMeteoForecastProvider.kt) reads provider sunrise/sunset for forecast display, but no general independent scene filter. The previous [ambient-experiments note](../2026-10-28-experience-rd-program.md) and [original exploratory document](../../product/ambient-experiments.md) already describe the idea: **do not claim this is entirely novel project research**.

## Definition and algorithm to investigate
- Inputs: valid WGS84 coordinates, current UTC `Instant`, user mode, local clock only for labels; compute topocentric/approximate Sun elevation, azimuth, sunrise/sunset boundaries.
- Proposed modes: **Daylight** (upper Sun limb near or above horizon with refraction approximation), **Civil twilight** (Sun approximately 0° to −6°), **Golden hour** (approx. elevation 0° to +6°, separately rising/setting), **Sunset soon** (next boundary within configurable 45m). These are **heuristic thresholds**, not photographic promises or NOAA accuracy guarantees.
- **Missing coordinates:** UNKNOWN and selectable through “Include unknown locations”; do not infer from filenames, stream hostnames or IP. Coordinates corrected by user take precedence. Dates must use an astronomical epoch, not “same local day” assumptions.
- Polar day/night, high latitudes, date line, leap year, rare no-sunrise days and approximation bounds must return explicit result classes; never `NaN` or fictitious sunrise.
- A source's current live edge may be delayed: optional stream observed delay is not enough to prove camera image timestamp; label approximate solar condition only.

## Implementation candidates
1. **Pure Kotlin astronomy function** with a documented algorithm, sufficient accuracy and deterministic tests. No network, permissions, hourly API or background polling.
2. **Forecast-provider sunrise/sunset:** useful supplementary data, but provider availability and cache semantics make it unsuitable as the only selector.
3. **Live image analysis:** reject at first pass because expensive and visually unreliable on the TV.

Design seam: `solarPhase(position, instant): SolarEstimate` with confidence/availability; secondary `eligibleBySun(cameras, desiredPhase)`. Do not change `LibraryItem` to store ephemeral solar data. Recompute only on scene/filter refresh and a bounded timer while active.

## Verification matrix (not run)
Reference known solar events for Tucson, Reykjavik, Tromsø, Ushuaia, Sydney, Tokyo, and equatorial cities across June/December solstice, equinox, daylight saving and UTC date boundaries. Check azimuth trend around solar noon and end of civil twilight, compare to an independently maintained astronomy implementation and a published ephemeris, and record maximum minute deviation. Proposed acceptance: event-time deviation under 5 minutes for ordinary latitudes with reference coordinates and stated uncertainty elsewhere; deterministic UTC/zone behavior; zero mandatory API calls. Polar scenarios should say “No sunset today” rather than fabricate a time.

## Interaction
Cameras → Filter → Daylight / Golden Hour; optional Smart Director mode. In preview list label “Estimated daylight at camera location” only when coordinates exist. If no matching locations, offer all cameras or a different period, **never blank the screen or silently play a night camera as daylight**. Default remains existing shuffle.

## Risks / gates
User-supplied geolocation may be wrong; providers can move a camera without changing metadata. Respect International Date Line, local astronomical date and daylight-saving changes. Reject costly continuous video inspection, data collection or server computation. Needs a functioning filter UI and on-device validation of focus/performance before GO for implementation. Sources to check independently before coding: [NOAA solar calculation guidance](https://gml.noaa.gov/grad/solcalc/), a vetted open-source ephemeris implementation and its license; **NOAA external docs were not retrieved through this GitHub-only research pass**.