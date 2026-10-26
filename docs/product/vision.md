# OpenVista: product vision

**Product scope:** Eight completed interview rounds (78 historical decisions), followed by owner clarification **C-01: user-supplied content only**. The Kotlin app, Media3 player, SAF library and U.S. NWS weather provider now exist as a **development preview with partial Hisense proof**. This document describes intended product behavior, not completed release acceptance. See [current status](../STATUS.md).

## What the owner approved

- **Platform:** a broadly compatible Android TV app, not a project exclusively for a Hisense/Fire TV. Google TV and Fire OS support must be validated on real devices.
- **Two entry points:** manually opened TV app and optional automatic system screensaver where OEM/OS permissions allow; **no automatic launch at TV boot**. Offer idle options **1/3/5/10/15/30 minutes where supported**.
- **Manual Home:** **three large image-backed cards**, Cameras, My Videos and Favorites. Favorites is one hub with visually separate camera and local-video sections.
- **Immersive viewing:** scenic video in fullscreen with an almost invisible UI and no permanent navigation chrome.
- **Live cameras:** a **user-managed library** of compatible HLS/DASH sources and imported lists, browsable by categories and available metadata (beaches, cities, mountains, etc.). Filter favorites, country, city and availability where metadata exists. The **2D world map with marker clusters and a side list** visualizes sources with usable location information. Offline, show locally saved sources with an honest no-network notice. OpenVista does not ship or host camera feeds or playlists.
- **Custom camera URLs and lists:** user-provided stream URLs and source playlists, entered using the TV keyboard or the implemented user-selected M3U import; optional QR-assisted input remains disabled pending a security review; **phone and TV must use the same local Wi-Fi/LAN with explicit pairing confirmation**, with implementation security still to be validated. HLS and MPEG-DASH are the initially approved stream protocols.
- **Camera details:** optional panel for name, country, local time, resolution, verified status and source.
- **Camera flow:** automatic scene rotation **5 minutes by default**, adjustable 1–60 minutes; new camera playlists **shuffle by default**, with sequence as an option. When a camera fails, advance immediately. If every camera fails or the internet drops, use a local video playlist **only when configured**; check restored connectivity without interrupting a playing local video. Once a camera is available, **finish the active local video and then return to live cameras**, unless user chooses to remain local. Otherwise show a clear retry notice.
- **My Videos:** automatically scan all accessible subfolders of a selected internal/USB folder; try **MP4, MKV, WebM and MOV** where codecs permit. Maintain separate camera/video playlists with creation, renaming, reordering, duplication, deletion and **batch add, remove and free reordering of items**.
- **Local video progression:** advance to next playable file at end, repeat the playlist, **skip corrupt/unsupported media** with unobtrusive notice and optional diagnostics; never loop forever over all-bad files.
- **Local video controls:** fixed directional **±10-second** seek, with **resume from previous playback position** when the same video reopens. No local-video subtitle feature.
- **Remote:** context-specific controls; **OK reveals a contextual menu with Cameras/My Videos switching** that **auto-hides after 5 seconds of inactivity**; in supported Dream mode Back exits and directional keys navigate, subject to OEM dispatch. Cameras change with arrows; local videos seek ±10 seconds.
- **Video presentation:** fill/crop image rather than stretch it; streaming quality automatic with selectable cap where the source supports quality levels. Prefer stable compatible decoder/HDR handling instead of forcing unsupported quality; no universal transcoding or HDR fallback guarantee.
- **Overlay information:** compact clock and current weather visible by default; seven-day detail on demand including temperatures, rain, wind, humidity, sunrise and sunset where provided. Weather can follow scene location or viewer city selected by manual search or optional device-supported location.
- **Overlay customization:** quick presets and a complete visual editor with simple and advanced configuration; specific UI details to determine.
- **Display-care options:** independent user-configurable settings to shift fixed overlays periodically and hide overlays after inactivity. They reduce but do not eliminate burn-in risk.
- **First run:** brief, optional/skippable tutorial to choose a camera, folder and weather context.
- **Transitions and performance:** choose instant or smooth scene changes; prioritize **fluid playback** and adapt available video quality/effects to the decoder and thermal/resource limits of each TV.
- **Energy and continuity:** continuous availability with an optional schedule and keep-awake options **subject to device power policy**; automatic Dream entry attempts the **last viewed camera/video**. Pause unnecessary background playback and cache updates. If USB is removed, explain and offer another source.
- **Settings and data:** categorized Basic settings and separate Advanced section; optional technical playback diagnostics and **explicit user-controlled copy/share of sanitized local error reports**, with no auto-upload. **Automatically persist settings, favorites and playlists on-device only**. No configuration export/import or cloud sync in initial scope.
- **Audio:** muted initially, user can enable it where media provides audio.
- **Localization and preferences:** Portuguese (Brazil) and English; detect the TV locale/region initially, with manual overrides for language, time and temperature.
- **Business and delivery:** 100% free, no ads, no account. **First development APK stays local for Hisense 58U6HF / Fire OS 7 testing**; GitHub publication comes later after owner approval. Other TV versions remain untested. **Automatic update download and installation desired**, but actual unattended installation depends on OS installer permissions, with system confirmation if required.
- **Future release:** an integrated download/library of licensed video wallpapers is explicitly deferred; initial scope covers camera streams and local videos.

## Engineering evidence still required

The eight decision rounds are complete; C-01 updates content ownership. Local Hisense tests now cover basic Home/navigation, M3U import, selected HLS/DASH and MP4 playback, folder persistence and a Tucson NWS forecast. Full source/playback recovery, 1080p/4K codec matrix, device-specific DreamService/input, USB unplug/replug, map/geocoding rights, secure same-LAN pairing, update installer behavior and the 24-hour stability/privacy gate remain. No bundled source inventory is required. See [evidence and blockers](../STATUS.md).

## Proposed validation

Prove DreamService and standard app-mode separately. Test map navigation on TV D-pad and hardware limitations of smooth transitions. Validate internal/USB media access, long-running playback, stream changes, quality caps, fallback state, overlay updates, display-care settings, PT-BR/English translations and 12/24-hour/°C/°F preferences on representative Android TV / Google TV / Fire TV hardware. The locally tested Hisense preview is **not** a stable APK or proof of other-device compatibility.

See [decisions](decisions.md), [v1 acceptance](v1-acceptance.md), [engineering plan](../implementation/priority-plan.md), [UX brief](experience.md), [content and weather](content-and-weather.md), [runtime/recovery](runtime-and-recovery.md), [local media and playlists](local-media-and-playlists.md), [settings and controls](settings-and-player.md), [architecture](../architecture/overview.md), [tests](../testing/compatibility.md) and [roadmap](../roadmap.md).
