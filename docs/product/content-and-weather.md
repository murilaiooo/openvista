# Live camera catalog, custom sources and weather

**Status:** Owner-confirmed product scope from round 4, 2026-10-24. Engineering details are still to be researched and specified.

## Live camera discovery and catalog

- Curate a **broad themed directory** covering beaches, cities, mountains, wildlife, landmarks/monuments, ski resorts, ports and other useful categories.
- Search/filter by **country, city, category, favorites and available-only**.
- Provide an interactive **2D map with clustered markers and a side list** compatible with TV D-pad navigation, independently of the browse catalog.
- Catalog **metadata updates automatically over the internet**, but ships with a **local fallback catalog** (built-in or last-known verified metadata). A metadata fallback does **not** make video feeds reachable while offline.
- Source rights: include licensed/authorized camera integrations only. Community members may submit suggestions, but a **review process** must approve stream URL, rights, attribution, safe content and playback before publishing. Specific provider list and review workflow remain research work.
- Provide an **optional details panel** showing scene name, country, camera-location local time, actual/known resolution, verified availability status and source attribution.
- Availability can be **available, unavailable, or unknown**. Do not infer continuous availability solely from a previously playable link.

## Adding your own camera

Support **typing with the TV on-screen keyboard** and **QR-assisted input** from a second device.

Product approval covers two entry options, **not a particular pairing protocol**. Any local pairing design must evaluate LAN discovery, authentication/pairing confirmation, binding restrictions, QR token expiry, CSRF/hostile links, unauthorized local URL probing and user control before implementation. No account is required.

## Playing cameras and fallback

- User-configured scene rotation every 1–60 minutes.
- When a camera becomes unavailable, skip promptly to the next available scene rather than trapping users in a retry loop.
- When all candidate cameras are unavailable, **only if explicitly configured** use a selected existing local-video playlist as the fallback. Otherwise show an informative retry screen. Keep camera playlists and local-video playlists separate as requested.
- Error handling must distinguish a network outage from a single unavailable stream and a catalog that is old.

## Weather and geography

- Current weather remains discreet next to the clock by default.
- On request, display **seven-day detail** with forecast conditions, temperatures, precipitation/rain, wind, humidity and sunrise/sunset **when the provider makes the relevant fields available**.
- Weather can follow either the **current camera location** or the **viewer's chosen city**.
- Set viewer location by **manual city search** or **optional device-supported detection** with the required permission and clear consent. The app should function without granting viewer location access.
- Choose provider based on official terms, permitted use by a completely free public app, API rate limits, data reliability and attribution. Refresh/caching/units/time zones and missing-data states remain research tasks.

## Live wallpapers

A searchable/downloadable in-app **wallpaper library is out of initial-release scope** and reserved for a future release. The first release focuses on permitted world cameras and the user's local videos from internal storage/USB. This does not prohibit source research, but do not implement an in-app video download catalog ahead of approval.

## Remaining engineering evidence to gather

1. Source/provider license matrix with upstream primary-source links, map metadata and stream test results.
2. Versioned catalog schema, authenticity/checks, safe fallback and update frequency.
3. Safe reviewed community submission/takedown workflow without paid services.
4. Secure, TV-friendly QR input feasibility and device/network constraints.
5. Provider-supported seven-day forecast fields and personal location privacy.
6. Availability status collection without excessive polling and background work.
7. Integration test plan for network failures, unknown statuses and all-cameras-down fallback.

This file does not claim working cameras, an existing backend, released downloads, or an APK.
