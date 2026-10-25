# Ambient discovery and display-care experiments

**Status:** Engineering/UX concepts proposed on 2026-10-25 for prototype evaluation; not new owner-confirmed D-IDs or stable-v1 acceptance gates. The latest C-01 user-managed, zero-bundled-source boundary still applies.

## Source-empty state: an intentional first-use experience

A clean installation has no cameras. The approved Home still has **Cameras**, **My Videos** and **Favorites**. Inside an empty Cameras library, show three large, remote-focusable actions:

1. **Add a stream**: TV keyboard entry of the user's HLS/DASH URL.
2. **Import a list**: select a user-owned M3U file from available internal storage or USB with the Android document picker; make the action visible but provide a useful unsupported-picker fallback when needed.
3. **Play my videos**: choose an accessible video folder through supported SAF UI.

Show one sentence of guidance rather than an empty catalog or sample feed that cannot play. Each card has visible focus and a safe Back path. Offer basic help about the difference between a camera URL, a playlist file and a local video. No login, account, bundled source, hidden download or automatic provider discovery. Test first launch with zero sources, denied picker, USB absent, malformed list, and import success.

## Local daylight and solar-time data

For a user-geotagged source, calculate sun position from latitude, longitude and UTC time, entirely on-device. Default interpretation: daylight when the **apparent** Sun is approximately above the horizon (sunrise/sunset threshold corrected for refraction and disk radius); handle civil twilight separately. Provide a clear `Unknown location` state when valid coordinates are absent. Do not guess a place from stream hostname, camera title or user IP.

Use a documented NOAA-style algorithm or an independently licensed tested astronomy implementation. Account for time/date rollover, high latitudes with polar day/night, leap years, timezone display only, and numeric errors. NOAA uses a **90.833° zenith** in approximate sunrise/sunset calculations, subject to atmospheric limitations. A 20-line formula is not a tested implementation.

**Daylight-only** (P2 experiment): filter geotagged sources by approximate current daylight. An unknown/untagged source is not labeled night and should be accessible under an explicit `Include unknown locations` option.

**Golden Hour** (P2 experiment): create a *dynamic view/rotation filter*, not an automatic shipped camera playlist. Use a configurable low solar elevation band such as **0° to +6°**, optionally near both sunrise and sunset, and label the results as an approximate solar condition, not a guarantee of warm light. A proposed `15° from sunset` rule is ambiguous and far from the usual low-altitude golden-hour range. Define whether the filter targets morning, evening or both; exclude/relabel high-latitude days with no event. Prefer cached local computation with no background wakeups.

Sunrise/sunset fields can be derived offline, but **temperatures, precipitation, humidity, wind and forecasts still require weather data/provider approval**. Computing solar times eliminates API requests for these event fields only. The map still requires a licensed tile provider where shown.

## Display-care guardian (P2 experiment)

Use product-facing language such as **Display care** instead of promising OLED protection. Preserve D-25's two independent controls: reposition static informational overlays and hide them after a user-selected inactivity period. D-65's five-second timer applies only to contextual player controls.

Prototype small, bounded movement of clock/weather labels within safe margins, fade after the chosen timeout and an optional low-light overlay preset. The suggested **continuous 1–3-pixel drift** and fixed **60-second fade** are hypotheses, not universal defaults. Measure TV resolution scaling, font legibility, D-pad focus, CPU/GPU frame drops, accessibility and bright-content persistence before setting values. Avoid changing hardware luminance, forcing TV-wide brightness or intercepting OEM power settings. No method here guarantees prevention of burn-in, particularly for bright static pixels in user videos/cameras.

## Procedural demo is not authorized

A generated shader or synthetic video may be authored by the project, but it would still alter the product's no-content launch contract and could conflict with the strict no-bundled-video G01 check. **No demo shader, synthetic asset, generated in-app video or demo-mode release promise is approved.** Record the choice separately if the owner explicitly opts in; document whether code-generated pixels are allowed and whether they may be shipped without any source media.

## Prototype acceptance

- Empty state is navigable using only D-pad, with focus retained when returning from picker/keyboard.
- Any daylight/Golden Hour label has coordinates/time provenance and an explicit unknown/polar fallback. Validate with independent reference values.
- Display care never moves actionable controls out of reach, never covers player-critical focus, and remains safe under screen sleep/resume.
- No hidden third-party network calls, bundled streams/videos or unexplained weather attribution.
- Distinguish **tested prototype** from **proposed visual language** in README, issues and release notes.

References: https://gml.noaa.gov/grad/solcalc/glossary.html and https://gml.noaa.gov/grad/solcalc/calcdetails.html.
