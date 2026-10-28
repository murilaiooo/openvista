# Worldwide weather: non-commercial Open-Meteo integration

Verified: 2026-10-27. Applies to OpenVista's free, non-commercial, advertisement-free, no-account Android TV build. This approval does **not** extend to paid, monetized or commercial editions.

## Providers and behavior

- U.S. and supported territories: National Weather Service (NWS), with automatic Open-Meteo fallback if the NWS service fails.
- Everywhere else: Open-Meteo's HTTPS forecast endpoint, using saved coordinates of each user's camera and the separate viewer-location coordinates.
- Existing camera-specific latitude/longitude and timezone values determine the point forecast; names are display-only and not geocoded heuristically.
- Current conditions, humidity, wind, seven daily highs/lows, precipitation chances and WMO condition codes are parsed only from real provider responses.
- Refresh at most every 30 minutes per location, with a 24-hour bounded cache, cancellation on scene switch and provider-specific error states.
- The source and viewer conditions remain independent even if the forecast strip is configured for the viewer.

## Attribution and terms

[Open-Meteo free API terms](https://open-meteo.com/en/terms): non-commercial uses without ads/subscriptions are eligible, up to 10,000 daily, 5,000 hourly, 600 minutely calls. No API key for the free endpoint. Observe service rate limits and provider reliability constraints.

Data licensing: [Open-Meteo](https://open-meteo.com/), [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). The ambient source displays **Weather: Open-Meteo · CC BY 4.0** in small type only when that provider's data is used. Input data is transformed by selecting current/forecast fields and converting units to the user's locale. This document provides full URLs and the modification notice for compliance. The fullscreen credit is not interchangeable with the source camera credit.

For deployment with paid plans, advertising, third-party commercial integration, or exceeding rate limits, review the contract and API plan again before releasing.

## Verification

- Confirm on the physical Fire OS 7 Hisense that a Portugal or Poland camera receives an international forecast and that Tucson retains the NWS path.
- Verify that the source clock, source weather and viewer weather use independent timezones, and no unverified weather is fabricated.
- Keep the provider metadata checks and licensing notice in the repository before merging.
