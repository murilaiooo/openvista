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
25. Idle activation options 1/3/5/10/15/30 minutes are usable **only where supported**; unsupported OEM cases get explicit guidance and manual mode still works.
26. Optional stay-awake and scheduling controls respect TV sleep/standby and do not force unsupported power states.
27. Automatic Dream entry resumes last camera/video when accessible; unavailable USB, offline sources and permission denial recover gracefully.
28. OK reveals contextual controls, Back exits and arrows navigate **if dispatched by OS**; record device/OS exceptions and reserved keys.
29. Internet disconnected: configured local playlist plays; bounded reconnect probes continue without playback interruption or automatically switching to an online feed.
30. Smooth playback priority: adapt quality and transitions under low CPU/RAM, decoder restrictions, 1080p/4K load and thermal stress.
31. No unnecessary video/network activity while screen is off/inactive; catalog and forecast cached and refreshed only when appropriate.
32. USB drive removed mid-video: clear notice and alternate source selection, no crash or infinite retry.
33. Reboot the TV: OpenVista **does not auto-launch**. Normal user launch and OS Dream entry still work where supported.
34. Automatic recursive scan includes nested folders (not just top-level), handles deep/large trees, unreadable entries and repeated document IDs within scoped permissions.
35. Open MP4/MKV/WebM/MOV with representative codecs on diverse TVs; unsupported codec/container/DRM combinations skip gracefully, not presumed compatible by extension.
36. Verify separate camera/video playlists, per-camera-playlist sequence/shuffle; create, rename, reorder, duplicate, delete without deleting underlying camera sources or video files.
37. At local-video end, automatically advance; repeat at playlist end; handle one-item, empty and all-invalid lists without spinning forever.
38. Directional local-video seek is **±10 seconds** exactly; clamp near start/end and handle nonseekable media safely.
39. Stable playback/HDR fallbacks only where supported by actual device; no unverified HDR-to-SDR conversion claims.
40. No local subtitle features (menus, sidecar loading or subtitle downloads); acknowledge that subtitles permanently embedded in video pixels cannot be removed.
41. On corrupt/unsupported media, automatically skip and show a discreet error with optional diagnostics, bounded across a finite playlist.
42. Last local video's saved timestamp resumes after manual reopen, app lifecycle and Dream entry where available; handle changed/missing file, invalid saved timestamp, removed USB and revoked grants.
