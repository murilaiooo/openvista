# TV compatibility and acceptance matrix

**Status:** Planned, no devices have been tested for OpenVista yet.

| Device family | App mode | DreamService auto idle | Local storage picker | Live HLS | Weather | Status |
| --- | --- | --- | --- | --- | --- | --- |
| Hisense 58U6HF, Fire OS 7 | Unverified | Unverified | Unverified | Unverified | Unverified | **Only initial test target** |
| Google TV 12+ | Unverified | OEM policy varies | Unverified | Unverified | Unverified | Test representative model |
| Older Android TV | Unverified | Unverified | Unverified | Unverified | Unverified | Test representative model |
| Other Fire OS versions | Unverified | OEM policy varies | Unverified | Unverified | Unverified | Test representative model |

**Recording rule:** no 'supported' flag without reproducible results on that device/OS build. **Only Hisense 58U6HF / Fire OS 7 is scheduled for initial tests; all other devices are untested.** Test manual app mode separately from system screensaver integration.

**Stable-v1 quality gate:** before public publication, verify >=25 distinct authorized, family-appropriate actually working webcams on Hisense, full playback/compatibility/licensing/privacy checklist, and at least **24 hours** of documented device playback/soak. The first development APK is **local only**.

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
43. Basic settings categories remain easy to navigate; separate Advanced area houses technical options without exposing them in normal player.
44. OK reveals player controls with fast Cameras/My Videos switching; arrows navigate focused menu items without accidental seek/camera changes, respecting DreamService key restrictions.
45. New camera playlists default to shuffle, can change to sequence; initial camera rotation is 5 minutes and configurable 1–60 minutes.
46. Single Favorites hub separates camera entries from local-video favorites, with reliable add/remove persistence.
47. Batch-add several playlist items, remove items and reorder items independently of reordering whole playlists; no cross-type mixing.
48. Player controls disappear after exactly **five seconds** of user inactivity, without hiding/altering separate clock/weather widgets unless independently configured.
49. Optional diagnostic panel shows actual available resolution, codec, buffering and error status; unsupported values display unknown/unavailable, private source tokens and file paths masked.
50. User settings, favorites and playlists persist **automatically on device** across app relaunch; initial release has no import/export, remote sync or user account. Clearing app data removes app-owned state as expected.
51. On network restoration during local fallback, the current video finishes before switching to a **verified playable** live camera; a user who selects remain local is not switched away.
52. Network flaps or unavailable cameras never cause premature video interruption or endless retry loops.
53. HLS and MPEG-DASH permitted test streams work within actual codec support; unsupported RTSP is not advertised as v1 functionality.
54. At least 25 distinct authorized, family-appropriate, correctly attributed cameras individually tested as working for stable v1; track source rights and dates.
55. QR entry uses same LAN/Wi-Fi with explicit on-TV confirmation, expiring token, resistance to unapproved entry and correct cancellation.
56. On loss of map tiles/network, saved/cached camera list appears with a notice that online map is unavailable; unavailable video streams are not implied to work.
57. App-update pipeline validates trusted release and APK signer/version, supports intended automatic download; records when Android PackageInstaller **requires explicit user action** and cannot silently install.
58. Local diagnostic report can be inspected/copied/shared by user action only; scrub paths, URLs, tokens and sensitive data; no passive telemetry uploads.
59. At least 24 hours uninterrupted real Hisense soak with recorded CPU, RAM, dropped frames, network, error count and recovery.
60. First APK is not published to GitHub Releases; all non-Hisense device families are labeled *untested* until real evidence exists.
