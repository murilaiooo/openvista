# Dual-location ambient forecast and safe-area QA — 2026-10-26

**Scope:** owner-requested OpenVista private preview, with no bundled user media, no new app package and no public release. Git branch: `feat/dual-location-forecast-20261026`.

## Rendering

Two independent, **background-free** groups retain the approved minimalist white typography and restrained text shadow.

- **Camera group:** resolved camera city **plus country** (for example `Jurata, Poland`), that city's clock and its temperature. This group never displays the raw video filename or stream title. If the city remains unresolved, the group is hidden instead of inventing a place.
- **Viewer group:** the owner's configured viewer location (currently Tucson, America/Phoenix), its current local time and temperature, plus **3–7 daily forecast rows**, default **5**. Each row shows abbreviated localized date, forecast maximum/minimum temperatures, and precipitation probability only when supplied by the weather provider.
- No GPS permission, automatic device-IP geolocation, private URI disclosure or assumption about the user's actual street address. The location is read from the existing user-saved settings, changeable from Settings > Weather.
- NWS-first forecast for U.S. coordinates and Open-Meteo worldwide support use separate request/cancellation lifecycles for camera and viewer. Daily results filter past dates against the viewer's own IANA time zone. The day-count preference persists across restarts, clamps to 3–7 and loads as 5 in older library versions.
- Provider-required attribution remains as understated small text. Offline or incomplete responses display only verified fields; missing weather values are not invented.

## TV-safe margins and screen care

- On every layout pass, the safe-area geometry uses at least **5%** of both physical viewport axes and a density-aware minimum of **48 dp horizontally / 40 dp vertically**.
- Both groups occupy **opposite diagonal corners**, with one-minute randomized or ordered corner movement and a subtle 900 ms transition. No opaque backgrounds or fixed-position clock/forecast.
- `AmbientLayoutPolicy` verifies all four positions fit within the visible rectangle, rejecting geometry that clips content or overlaps the two groups.
- When a narrow viewport or enlarged text makes both groups impossible to place, the viewer forecast wins; the camera information is temporarily suppressed rather than drawn over the forecast. If the full viewer group cannot fit the configured viewport, the overlay hides rather than clips or paints under the TV's overscan edge.
- Static source watermarks embedded in the video and system UI are outside OpenVista's overlay control. Moving UI cannot guarantee burn-in prevention.

## Evidence and remaining acceptance

- Locally executed Windows offline Gradle module tests, debug APK build and Android Lint: **PASS**.
- Unit tests for safe areas and non-overlap at **1920×1080 and 3840×2160** with seven-day forecast geometry: **PASS**.
- Unit tests for camera-country extraction, 3/5/7-day selection, local-day filtering, invalid/missing temperatures, precipitation formatting, and persisted bounded settings: **PASS**.
- Python tests (23/23), no-bundled-video/stream APK guard, docs/ADR references: **PASS**.
- The owner-saved viewer location is Tucson with timezone `America/Phoenix`; no new location permission was requested.
- **Still required:** physical TV screenshot of 3 and 7-day layouts, multi-minute movement, both forecasts together, source switching, online/offline transitions and long-duration 4K/24-hour playback.

The OTA updater, LAN pairing, OSM tiles and commercial/public-provider release gates remain unchanged.
