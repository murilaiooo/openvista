# Minimal online city / weather overlay QA, 2026-10-26

This feature is an **owner-requested private development preview**. It is not a public-release authorization, and provider terms still require a separate distribution review.

## Visual changes

- **No video titles** in the ambient overlay, including live-camera title and filename.
- No black boxes, cards, gradients, or tinted backgrounds. The new minimalist composition floats over the video with clean white typography and a light text shadow: **city**, **local time**, **current temperature**. A small service credit is shown when provider data is used.
- The full composition shifts among screen corners every minute; the existing random versus ordered positioning setting still works.
- When the current source is a local video or location metadata is missing, the app may use the owner's configured viewer city, clearly labeled **YOUR LOCATION / LOCAL**. It never misrepresents Tucson weather as remote-camera conditions.

## Internet location resolution

- Prefer the user-saved city, coordinates and IANA time zone.
- Otherwise, extract **only a location-like prefix** from the source title, removing scene suffixes and video resolution. Never upload raw media URLs, stream tokens, file paths or arbitrary full titles.
- Use Open-Meteo's HTTPS geocoding endpoint to find matching city coordinates and its IANA time zone. Request up to two safe candidates serially, with a bounded response and no background bulk lookup. Cache successful resolutions for seven days and a negative result for twelve hours in private local app preferences.
- Fetch location-specific weather through the existing **NWS-first / Open-Meteo worldwide** adapters (30-minute refresh and weather caching). Only expose a city name returned by the geocoder or explicitly configured by the user.
- Avoid guessing a city when the lookup fails. The API may rank similarly named cities differently; ambiguous title inference needs user confirmation and explicit per-source metadata for accuracy.

## Provider terms, privacy and release gating

- Open-Meteo's keyless hosted API is for **noncommercial use** and requires visible attribution. The overlay credits `open-meteo.com` and GeoNames. Before any public/commercial release, review provider licensing, rate limits and geocoder lookups with counsel/vendor and deploy an appropriate account or self-hosted service.
- GeoNames supplies the location dataset through Open-Meteo.
- OSM tiles, LAN pairing and external APK-update gates remain disabled.
- Provider references: https://open-meteo.com/en/docs/geocoding-api , https://open-meteo.com/en/terms , https://open-meteo.com/en/docs .

## Verification

- Source labels seen on the owner's physical test device include Jurata, Karpacz, Costa da Caparica, Florence, Faro and Gdynia Orłowo, alongside some unresolvable QA test streams. The public geocoding API successfully returned plausible IANA zones for Jurata, Karpacz, Costa da Caparica, Florence and Faro during small-scale manual checks.
- Unit tests verify title sanitization, no URL/filename lookup, scene suffix stripping, display-name extraction and explicit-location precedence.
- Full Gradle/Android Lint and on-device behavior: **must be recorded separately before merge to production main**.
- No 24-hour endurance or broad Fire OS certification is claimed.
