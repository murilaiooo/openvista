# NWS weather provider: integration and physical validation (2026-10-26)

## Decision and license review

OpenVista uses the public US National Weather Service (NWS/NOAA) forecast API at `https://api.weather.gov` for US coordinates, without a paid account or API key. The NWS API documentation describes the forecast data as freely usable open US-government data, subject to request limits and the terms described at:

- https://www.weather.gov/documentation/services-web-api
- https://www.weather.gov/disclaimer

Every request sends the API-required identification header `User-Agent: OpenVista/0.2 (https://github.com/murilaiooo/openvista)` and the GeoJSON Accept header. The user-facing clock overlay and forecast dialog intentionally omit a separate provider-credit line. Internal provider identification, license documentation and original NWS forecast wording are retained. No NWS marks, logos or suggestion of endorsement appear in the UI.

## Implementation

- `NwsForecastProvider` resolves `/points/{latitude},{longitude}`, then uses the official HTTPS gridpoint forecast and hourly URLs. It accepts URLs only under `api.weather.gov` with a valid gridpoint route.
- Bounded 1 MiB network response, 7-second connect/read timeouts, no redirects, no logging or transmission of private stream URLs. Missing or non-US coverage produces an unavailable state; no fallback service silently receives the coordinates.
- The seven-day forecast uses actual NWS high/low temperatures, rain *probability* in percent, forecast wind and available hourly humidity. A probability is never misrepresented as measured millimeters. Sunrise/sunset and measured precipitation are left unknown because these endpoints do not provide them.
- The ambient overlay displays an hourly temperature *forecast* and textual conditions, not a claimed physical weather-station observation.
- Cache: at least 30 minutes between successful refreshes per location; last successful forecast can be used up to 24 hours, explicitly marked stale on network failure.
- Imported camera streams without coordinates automatically use the viewer's selected city as the overlay weather target; the camera/viewer context selector is still honored where camera coordinates exist.
- The existing city-search control currently explains that manual city and coordinate entry is needed. No unapproved third-party geocoder or background location permission was added.

## Local tests

`gradlew.bat :player:testDebugUnitTest :source-core:test :diagnostics:testDebugUnitTest :player:assembleDebug :player:lintDebug --offline --no-daemon` — BUILD SUCCESSFUL (84 tasks).

The new `NwsForecastProviderTest` includes four passed Android API-28 Robolectric cases: F/C temperature conversion, exact rain probability versus rain millimeters, daily grouping and current hourly period; degraded hourly fetch; third-party endpoint rejection; and invalid coordinates rejected pre-network. Python tests: 23 passed. Docs validation: passed. No-bundled-media guard on the debug APK: passed.

## Physical Hisense Fire OS 7

- Real NWS Tucson coordinates: 32.2226, -110.9747, timezone America/Phoenix.
- Live API discovery returned NWS weather office TWC; physical APK fetched and displayed all seven dates, high/low, wind, humidity and chance of precipitation in Brazilian Portuguese UI.
- A second TV test played an externally served user-supplied HLS fixture and rendered the overlay with **Tucson / 27°C Sunny**, without any provider-credit text.
- The Fire TV was updated in place with `adb install -r`, leaving only package `com.openvista.prototype`. No unrelated apps were installed, uninstalled or changed.
- No weather data or live camera sources were embedded into the APK. The registered private user library was preserved.
- NWS textual conditions are currently in English when received from the API (e.g., `Sunny`), even in PT-BR mode; a later localization improvement can map standard conditions without altering NWS alerts or numerical values.

## Deferred and limitations

- Weather integration works for NWS-covered US locations, not worldwide camera locations.
- City search still requires a separately licensed/approved geocoder.
- Map tiles/license decision and the unrelated update/pairing gates remain unresolved.
- Offline-stale and rate-limit behavior were unit/architecture reviewed but not subjected to a full multi-hour real-device endurance test.
- No release certification or merge to main is implied by this QA result.

Verification evidence (local workstation): `I:\codex\openvista-qa\tucson-nws-forecast.png` and `I:\codex\openvista-qa\nws-overlay-tucson-hls.png`.
