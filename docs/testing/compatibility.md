# TV compatibility and acceptance matrix

**Status:** Planned, no devices have been tested for OpenVista yet.

| Device family | App mode | DreamService auto idle | Local storage picker | Live HLS | Weather | Status |
| --- | --- | --- | --- | --- | --- | --- |
| Hisense 58U6HF, Fire OS 7 | Unverified | Unverified | Unverified | Unverified | Unverified | First planned test device |
| Google TV 12+ | Unverified | OEM policy varies | Unverified | Unverified | Unverified | Test representative model |
| Older Android TV | Unverified | Unverified | Unverified | Unverified | Unverified | Test representative model |
| Other Fire OS versions | Unverified | OEM policy varies | Unverified | Unverified | Unverified | Test representative model |

**Recording rule:** no 'supported' flag without reproducible results on that device/OS build. Test manual app mode separately from system screensaver integration.

## Acceptance test scenarios
1. Cold boot / launch and input focus; Back exits cleanly.
2. Folder picker, USB disconnect/reconnect, permission restoration after reboot.
3. Local clips with supported codecs, unsupported codec, corrupt file and missing file.
4. Public licensed live HLS, mid-stream disconnect, long-running recovery and no busy retry loops.
5. Current weather + 7-day forecast; provider offline, stale cache, mixed timezones.
6. Power / idle / DreamService wake and screen-off behavior.
7. 24-hour soak metrics: dropped frames, memory, CPU, temperature (if available), data transfer.
8. Accessibility: focus visibility, navigation with D-pad, high-contrast/large text.
9. Image-backed three-card Home and full-bleed/near-invisible playback interface; focus must remain visible on interaction.
10. Camera change via directional input vs local video seek; validate no remote conflicts and return behavior.
11. 2D map clustered-marker + sidebar navigation with remote; no inaccessible clusters on low-memory hardware.
12. On-demand seven-day weather versus discreet clock/current condition defaults; simple/advanced editor with safe focus bounds.
13. Independently adjust widget reposition interval and hide-after-inactivity; verify interactions, noting no anti-burn-in guarantee.
14. Skip first-run guide without granting optional locations/folder access; revisit later from settings.
15. Instant and smooth transition options: monitor dropped frames, decoder usage and degradation on constrained TVs.
16. Locale defaults from device and manual PT-BR/English, clock 12/24h and °C/°F overrides.
17. Remote catalog metadata refresh, version validation, offline bundled/last-known metadata and degraded stream behavior without network.
18. Broad categories, country/city/favorites/available-only filters with available/unavailable/unknown states, map/list consistency.
19. Reviewed community-submitted camera metadata: licensing, source attribution, safety and untrusted URL checks.
20. Custom URL entry via TV keyboard and secure QR-assisted pairing (confirm approval, expiry and local-network isolation); no account requirement.
21. Optional scene metadata panel: source, scene name, country, local time, actual resolution and verified status.
22. Seven-day weather detail: rain, wind, humidity, sunrise/sunset plus unavailable-value labels; manual city input and opt-in device location denial path.
23. All cameras unavailable: configured playable local playlist fallback, missing-USB fallback, and explicit retry when unconfigured; no infinite skip loop.
24. Verify first APK has **no integrated wallpaper download library**, which is deferred to later releases.
