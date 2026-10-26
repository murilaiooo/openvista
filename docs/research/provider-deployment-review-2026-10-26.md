# Provider deployment review: 2026-10-26

This review records upstream service terms, not owner approval to activate services. The development preview continues using the existing U.S. NWS forecast adapter. `ProviderPolicy.WORLDWIDE_WEATHER_APPROVED=false` and `ProviderPolicy.OSM_MAP_APPROVED=false`.

## Open-Meteo worldwide weather and city search

Primary provider sources:

- https://open-meteo.com/en/terms
- https://open-meteo.com/
- https://github.com/open-meteo/open-meteo

The free hosted API is for **noncommercial use**, currently limited to fewer than **10,000 calls/day**, **5,000/hour**, and **600/minute** per documented terms. Open-Meteo forecast data is CC BY 4.0 and **requires appropriate attribution with a link** where used. OpenVista's adapter already carries the provider credit and sets `requiresVisibleAttribution=true`; the overlay displays it when applicable. Automatic worldwide activation still requires owner approval of that visible attribution and noncommercial deployment scope, an independent end-to-end offline/quotas test, and a privacy review of sending coordinates to an external provider. The U.S. NWS forecast path is separate.

## OpenStreetMap raster tiles

Primary provider policy:

- https://operations.osmfoundation.org/policies/tiles/
- https://www.openstreetmap.org/copyright

The public `tile.openstreetmap.org` server is a donation-supported, **best-effort** service, not a guaranteed free production tile backend. It requires on-map `© OpenStreetMap contributors` attribution, stable identifying User-Agent, HTTPS, honoring cache headers (or at least seven days), and no bulk download, offline region fetch or unnecessary prefetching. OpenVista's candidate implementation bounds the viewport and tile count, identifies the application and draws attribution over the map. Deployment at unpredictable public scale, precise caching details and full TV D-pad rendering still require testing; a self-hosted or alternative licensed tile provider may be more appropriate for a public release.

## Gate decision

**Do not silently activate either provider or hide legally required map/weather credits.** Owner acceptance of provider conditions and full TV behavior checks are still required. This is not a restriction on NWS's current working U.S. weather flow.
