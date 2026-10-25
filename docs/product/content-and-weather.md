# User-managed live sources and weather

**Status:** Owner-confirmed product scope from round 4, 2026-10-24. Engineering details are still to be researched and specified.

## User-managed live source discovery

- Organize **user-added cameras and imported source lists** into optional themes (beaches, cities, mountains, wildlife, landmarks, ski resorts, ports and more) when the user supplies a category or metadata. **Do not prepopulate or distribute a first-party camera feed catalog.** With no sources, display a clear add-source empty state.
- Search/filter by **country, city, category, favorites and available-only**.
- Provide an interactive **2D map with clustered markers and a side list** compatible with TV D-pad navigation. If online tiles cannot load, show a **local list of saved/cached camera metadata and a clear online-map-unavailable notice**; list availability doesn't mean streams play offline.
- User-supplied remote list metadata may refresh only when configured and authorized by the user; always keep a locally saved copy of imported source metadata and show honest offline status. There is **no built-in catalog feed** or central OpenVista camera service.
- Source library belongs to the user. OpenVista never publishes a shared directory of submitted camera feeds; therefore no community-content publication/review pipeline is needed. Parse user-entered sources defensively and never publish, upload, or log private source URLs.
- Provide an **optional details panel** showing scene name and, only when actually supplied/verified, country, camera-local time, resolution, availability and source attribution. Do not infer latitude/longitude or country from URL host alone.
- Availability can be **available, unavailable, or unknown**. Do not infer continuous availability solely from a previously playable link.

## Adding your own camera

Support **typing with the TV on-screen keyboard** and **QR-assisted input** from a phone **on the same local Wi-Fi/LAN, with explicit pairing approval on TV**. HLS and MPEG-DASH are initially supported custom live protocols; other protocols such as RTSP are deferred.

Product approval covers two entry options, **not a particular pairing protocol**. Any local pairing design must evaluate LAN discovery, authentication/pairing confirmation, binding restrictions, QR token expiry, CSRF/hostile links, unauthorized local URL probing and user control before implementation. No account is required.

## Playing cameras and fallback

- User-configured scene rotation every 1–60 minutes.
- When a camera becomes unavailable, skip promptly to the next available scene rather than trapping users in a retry loop.
- When all candidate cameras are unavailable, **only if explicitly configured** use a selected existing local-video playlist as the fallback. Otherwise show an informative retry screen. Keep camera playlists and local-video playlists separate as requested.
- Error handling must distinguish a network outage from a single unavailable stream and a user-imported remote list that has become stale.

## Weather and geography

- Current weather remains discreet next to the clock by default.
- On request, display **seven-day detail** with forecast conditions, temperatures, precipitation/rain, wind, humidity and sunrise/sunset **when the provider makes the relevant fields available**.
- Weather can follow either the **current camera location** or the **viewer's chosen city**.
- Set viewer location by **manual city search** or **optional device-supported detection** with the required permission and clear consent. The app should function without granting viewer location access.
- Choose provider based on official terms, permitted use by a completely free public app, API rate limits, data reliability and attribution. Refresh/caching/units/time zones and missing-data states remain research tasks.

## Live wallpapers

A searchable/downloadable in-app **wallpaper library is out of initial-release scope** and reserved for a future release. The first release focuses on user-supplied live streams and the user's local videos from internal storage/USB. This does not prohibit source research, but do not implement an in-app video download catalog ahead of approval.

## Remaining engineering evidence to gather

1. Defensive user-source URL/list import tests, optional map metadata validation, and lawful test-stream playback results.
2. Versioned user-managed source schema, import validation, locally saved metadata, and optional user-configured remote-list refresh policy.
3. URL-scheme validation, bounded list parsing, duplicate handling, credential redaction, SSRF protections and safe empty-library behavior.
4. Secure, TV-friendly QR input feasibility and device/network constraints.
5. Provider-supported seven-day forecast fields and personal location privacy.
6. Availability status collection without excessive polling and background work.
7. Integration test plan for network failures, unknown statuses and all-cameras-down fallback.

This file does not claim working cameras, an existing backend, released downloads, or an APK.
