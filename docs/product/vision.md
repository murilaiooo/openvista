# OpenVista: product vision

**Status:** Product discovery. The owner confirmed 28 decisions across rounds 1–3 on 2026-10-24; round 4 remains open.

## What the owner approved

- **Platform:** a broadly compatible Android TV app, not a project exclusively for a Hisense/Fire TV. Google TV and Fire OS support must be validated on real devices.
- **Two entry points:** manually opened TV app and optional automatic system screensaver where OEM/OS permissions allow.
- **Manual Home:** **three large image-backed cards**, Cameras, My Videos and Favorites.
- **Immersive viewing:** scenic video in fullscreen with an almost invisible UI and no permanent navigation chrome.
- **Live cameras:** catalog and custom permitted URL input; organized country/category browsing and **2D interactive world map with grouped markers and a side list**.
- **Camera flow:** automatic scene rotation with configurable 1–60-minute interval; when a camera fails, advance immediately to the next, with a finite fallback if all are inaccessible.
- **My Videos:** video files in internal or USB storage, sequence/shuffle and **multiple separate camera/video playlists**.
- **Remote:** controls adapted by media type. Change cameras with directional navigation; in local videos, directional playback controls support forward/backward seeking. Exact button mapping is still open.
- **Video presentation:** fill/crop image rather than stretch it; streaming quality automatic with selectable cap where the source supports quality levels.
- **Overlay information:** compact clock and current weather visible by default; full seven-day forecast on demand. Weather can refer to scene location or the user's chosen location.
- **Overlay customization:** quick presets and a complete visual editor with simple and advanced configuration; specific UI details to determine.
- **Display-care options:** independent user-configurable settings to shift fixed overlays periodically and hide overlays after inactivity. They reduce but do not eliminate burn-in risk.
- **First run:** brief, optional/skippable tutorial to choose a camera, folder and weather context.
- **Transitions:** choice between instant and smooth scene change, with actual effects limited by device playback performance.
- **Audio:** muted initially, user can enable it where media provides audio.
- **Localization and preferences:** Portuguese (Brazil) and English; detect the TV locale/region initially, with manual overrides for language, time and temperature.
- **Business and delivery:** 100% free, no ads, no account; first release through GitHub APK.
- **Future possibility:** curated licensed video-wallpaper library, scope unconfirmed.

## Remaining decisions

Providers and legal rights, source category coverage, weather service, custom URL entry, playlist details, user location setup, exact remote mapping, wallpaper-library scope, all-camera-failed behavior, advanced overlay fields, decoder performance requirements, OS/OEM screensaver compatibility and app update strategy are not yet finalized.

## Proposed validation

Prove DreamService and standard app-mode separately. Test map navigation on TV D-pad and hardware limitations of smooth transitions. Validate internal/USB media access, long-running playback, stream changes, quality caps, fallback state, overlay updates, display-care settings, PT-BR/English translations and 12/24-hour/°C/°F preferences on representative Android TV / Google TV / Fire TV hardware. No APK or validated OpenVista app exists yet.

See [decisions](decisions.md), [UX brief](experience.md), [architecture](../architecture/overview.md), [tests](../testing/compatibility.md) and [roadmap](../roadmap.md).
