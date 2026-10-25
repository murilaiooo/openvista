# OpenVista product decisions

**Status:** Discovery. Rounds 1–7 confirmed (68 decisions) on 2026-10-25. Round 8 (first-release boundaries) open.

Only owner-confirmed choices are requirements. Open questions, design recommendations and proposed technical solutions are **not** approved. Product agreement is not proof of compatibility or implementation.

## Product baseline

OpenVista is a free, ad-free, account-free ambient Android TV application and, where the OS allows, an automatic system screensaver. It shows public, permitted live cameras or looping user-owned videos. It includes configurable clock and weather overlays (current conditions and seven-day forecast), with weather for the camera location or the user's chosen location. First distribution is by GitHub APK; no APK exists yet.

## Round 1: confirmed (D-01 to D-08)

| ID | Requirement | Confirmed choice / boundary |
| --- | --- | --- |
| D-01 | Entry points | Both automatic screensaver and manually launched app. Automatic mode subject to device/OS permission. |
| D-02 | Live sources | Both curated/browsable cameras and user-entered permitted stream URLs. |
| D-03 | Local media | Select folder(s), play in sequence, shuffle and playlists. Details refined in D-13/D-14. |
| D-04 | Weather location | Option to switch between camera location and viewer location; no mandatory personal location permission. |
| D-05 | Overlays | Fully customizable. Presets/editor refined in D-15/D-24. |
| D-06 | Audio | Muted initially; allow the user to enable sound. |
| D-07 | Commercial model | 100% free, no advertisements or user account. Provider licenses must allow the app's use. |
| D-08 | Initial delivery | APK via GitHub. Store distribution is not committed. |

## Round 2: confirmed (D-09 to D-18)

| ID | Requirement | Confirmed choice / boundary |
| --- | --- | --- |
| D-09 | Home | Three large destinations: Cameras, My Videos and Favorites. |
| D-10 | Camera discovery | Both organized catalog and interactive map. |
| D-11 | Rotation | Automatic scene change at a configurable interval of 1–60 minutes. |
| D-12 | Camera failure | Skip immediately to the next camera; prevent an endless cycle when every camera is unavailable. |
| D-13 | Local storage | Internal TV storage and USB flash/external HDD; device/system picker support to validate. |
| D-14 | Playlists | Multiple playlists, with cameras and local videos in separate playlists. |
| D-15 | Overlay customization | Both presets and a full visual editor. |
| D-16 | Video fit | Fill the screen, crop excess rather than distort. |
| D-17 | Quality | Automatic playback quality, configurable maximum where supported by source variants and decoder. |
| D-18 | Localization | Brazilian Portuguese and English initially. |

## Round 3: confirmed (D-19 to D-28)

| ID | Requirement | Confirmed choice / boundary |
| --- | --- | --- |
| D-19 | Visual style | Scenic footage fills the screen; interface is nearly invisible during ambient playback. Avoid permanent chrome. |
| D-20 | Home appearance | Three **large image-backed cards** for Cameras, My Videos and Favorites. |
| D-21 | Remote controls | **Contextual by media type**: camera mode uses directional navigation for changing cameras; local video mode allows seeking backward/forward. Exact D-pad/OK/Back/key conflicts remain to specify. |
| D-22 | World map | **2D map with clustered markers and a lateral list**. Must be navigable by a TV remote; engine, dataset and rendering unchosen. |
| D-23 | Default clock/weather | **Discreet clock and current conditions**; detailed seven-day forecast opened on demand. All overridable using D-15/D-24. |
| D-24 | Widget editor | **Both simple and advanced editing**. What gestures, fields and positioning controls are available remains open. |
| D-25 | Static-content protection | **Both periodic overlay repositioning and inactivity hiding**, independently configurable. These reduce risk but cannot guarantee absence of burn-in. |
| D-26 | First-run experience | **Short optional tutorial** to choose cameras, video folder and weather context; users can skip it. |
| D-27 | Transitions | **Configurable instant or smooth scene changes**, choosing practical behavior based on hardware performance. Never promise seamless crossfades on every device. |
| D-28 | Defaults | **Detect TV language/region**; allow manual overrides for language, time display and temperature unit. |

## Round 3 behavior implications

- **Immersion:** app/ambient playback is full-bleed; interactions can reveal contextual controls without keeping menus permanently visible.
- **Distinct controls:** camera change is different from local video seek. Define exact mapping and discoverability in a later interaction spec; do not assume arrows will always mean the same action.
- **Editor:** both quick settings/presets and a full editor must be usable from a remote. On-screen movement, focus, safe margins and live preview must be validated on real hardware.
- **Display care:** reposition and idle-hide operate as independent, user-adjustable strategies; reserve overlays for useful information, avoid prolonged bright static elements. Never claim this eliminates burn-in.
- **Performance:** use direct cuts as a fallback for scenes where fades require extra decoders/surfaces; respect system power/sleep behavior.
- **Localization:** initial text PT-BR/EN, default from device region; explicit manual language, temperature unit and clock-format overrides.
- **Onboarding:** skip never blocks entry into manual Home; selecting optional sources/weather can happen later.

## Round 4: confirmed (D-29 to D-38)

| ID | Requirement | Confirmed choice / boundary |
| --- | --- | --- |
| D-29 | Camera categories | **Broad curated catalog**: beaches, cities, mountains, wildlife, landmarks, ski resorts, ports and additional themes. Exact taxonomy and source availability are to be researched; only include authorized sources. |
| D-30 | Catalog updates | **Automatically update catalog online**, with a bundled/local fallback list. Fallback metadata cannot make offline webcams play; the app must explain loss of connectivity. |
| D-31 | Catalog contributions | **Authorized camera providers and community suggestions subject to review**. Community submissions are not automatically trusted or published; review attribution, rights, safety and stream compatibility. |
| D-32 | Custom stream entry | **Both TV keyboard and QR code** for convenient entry. QR-assisted transfer must be designed for safe pairing with no account; access control, network exposure and token expiry remain engineering decisions. |
| D-33 | Optional camera details | **Name, country, local time, stream resolution, status, and source** available through an optional info panel; do not show unverified quality/online status as fact. |
| D-34 | Catalog filters | **Country, city, category, favorites, and only available cameras**. Online/available indicator must be evidence-based; unknown status should be distinct from offline. |
| D-35 | Seven-day forecast | **Temperatures, rain, wind, humidity, sunrise and sunset**, wherever the weather provider supplies reliable values. Preserve compact default overlay and expanded on-demand details. |
| D-36 | Viewer location setup | **Manual city search plus optional device-assisted detection** on supported hardware. Do not require location permission to use camera-local forecasts. |
| D-37 | Wallpaper library | **Deferred until a future release**. Initial focus is live cameras and user-provided local videos; no integrated wallpaper downloading in the first release. |
| D-38 | All cameras unavailable | **Optional user-configured fallback to an existing local playlist**. If no fallback is configured/accessible, show a clear notice with a retry action. No infinite skipping loop. |

### Derived constraints (not additional approvals)

- Maintain a versioned, validated catalog format and safe last-known/bundled metadata fallback. A public metadata feed does not imply a paid backend or guarantee connectivity.
- Do not publish community camera URLs until reviewing authorization, owner/attribution details, public safety and working playback. Provide a community submission/review process to research later.
- QR pairing must not expose private URLs over insecure public endpoints; the exact discovery/pairing method is still open and must be threat-modeled before implementation.
- Availability filtering depends on current, observed checks with explicit unknown/error states; successful directory entry alone does not imply a playing camera.
- Respect user choice: only switch to local-video playback as an all-camera-failed fallback when the user configured it, and never break the separation between saved camera/video playlists.
- Forecast details depend on weather-provider data and terms; avoid inventing unsupported values and don't silently collect precise location.

## Round 5: confirmed (D-39 to D-48)

| ID | Requirement | Owner's confirmed choice / boundary |
| --- | --- | --- |
| D-39 | Automatic idle activation | Offer **1, 3, 5, 10, 15, or 30 minutes**, **where the device/OS allows** setting those intervals and selecting the OpenVista screensaver. Unsupported options must not appear functional or change private system settings silently. |
| D-40 | Energy / screen-on controls | Offer user-facing **keep-awake / continuous playback options** where technically allowed, **respecting device power policies and platform limits**. Never promise to bypass standby, screen-off or OEM restrictions. |
| D-41 | Scheduled operation | Normally **eligible to run continuously**, with an **optional user-configured schedule**; timezone, daily windows and actions outside the schedule are still open. |
| D-42 | Start/resume scene in automatic mode | **Continue the last used camera or video**, if it remains accessible. When it is unavailable, follow existing skip/fallback policy; exact timestamp behavior for videos is open. |
| D-43 | Dream-mode remote | **OK reveals contextual controls; Back exits; directional keys navigate** (cameras change, local videos seek per D-21), **where OS/OEM delivers these keys to the screensaver**. Never hijack reserved Home/system inputs; verify per device. |
| D-44 | Network outage | When configured, **play the local fallback playlist** and **check for reconnection without interrupting the currently playing local video**. No retry storms, no premature foreground switch; behavior for restoring live feeds later is open. |
| D-45 | Performance priority | **Fluency/stable playback first**; adapt stream quality and optional transitions/effects to actual hardware, honoring configured caps. Do not force 4K where it stalls. |
| D-46 | Background work | **Suspend unneeded playback/background tasks** when inactive; retrieve/catalog/weather updates using appropriate **caching** and only when needed. Respect screen-off, lifecycle and foreground-service rules. |
| D-47 | USB unplug | **Inform user that the USB drive/media became unavailable and allow choosing another source**. No infinite retry or application freeze. |
| D-48 | TV boot | **Never auto-launch on TV boot**. Launch only by deliberate user action or eligible OS screensaver entry. Do not install boot receivers to open the app automatically. |

### Derived constraints (not additional owner approvals)

- Distinguish user-requested timeout selections from OS-owned timeout settings; gracefully explain when 1/3/5/10/15/30-minute choices are not available, without root, privileged APIs or unsupported device modifications.
- Separate app-fullscreen awake requests from system DreamService and device standby policy. Schedules and energy-saving features must never silently override the TV.
- Persist last selected camera/video and its local-video playback position (approved in D-58); handle missing USB, offline cameras, permissions revocation and files whose content changed since the checkpoint.
- On loss of network, keep the configured local fallback playing while performing **bounded, non-disruptive** connection checks. Do not silently switch away from local playback when connectivity reappears until the restoration rule is agreed.
- Treat a wake/resume event separately from boot and from intentional activity launch. Hardware/OS may consume D-pad or Back before the app sees them.
- Validate smooth playback across representative devices under 4K, decoder limits, thermal conditions, transitions and background/screen-off lifecycle.

## Round 6: confirmed (D-49 to D-58)

| ID | Requirement | Owner's confirmed choice / boundary |
| --- | --- | --- |
| D-49 | Camera playlist order | Each **camera playlist supports sequential or shuffled** playback. Rotation interval remains user-configurable (D-11). |
| D-50 | Playlist management | **Create, rename, reorder, duplicate and delete** multiple playlists. Camera and local-video playlists stay separate (D-14). Item reordering, safeguards and exact editing UI need implementation design. |
| D-51 | Subfolder discovery | **Automatically and recursively include videos from all subfolders** of a user-selected accessible folder. Respect scoped storage, permission boundaries and removable-media failures. |
| D-52 | Local formats | Try **MP4, MKV, WebM and MOV**, only where the TV's demuxers/codecs support the specific streams. Container extension alone does not guarantee playback. |
| D-53 | End-of-video behavior | **Advance to the next video and loop the playlist** when its end is reached. Support single-item and empty-list edge cases safely. |
| D-54 | Local video seeking | Directional seek step is **10 seconds**, backward/forward (D-21), clamped to media bounds; no alternate seek intervals are part of the confirmed scope. |
| D-55 | HDR / decoder fallback | **Prioritize playback stability** and use a **compatible fallback when technically possible**. Do not claim an unsupported HDR stream can be converted to SDR automatically without a verified path. |
| D-56 | Subtitles | **No subtitle feature for local videos**: the player focuses on ambient/scenic content. Do not add subtitle selection, external subtitle loading or forced subtitle overlays to the initial UI. |
| D-57 | Invalid media | **Skip corrupt/unsupported video to the next playable item**, with an unobtrusive notice and optional error details. Cap attempts to prevent an infinite loop if everything fails. |
| D-58 | Local resume | **Resume from the last saved playback position** when a previously viewed local video is reopened, including its use as the last scene in automatic screensaver mode where accessible. Exact checkpoint persistence intervals are an engineering detail. Live webcams have no seekable resume position. |

### Derived constraints (not extra approvals)

- File enumeration is **recursive by default** within the folder tree actually granted by the TV picker; avoid cycles, unbounded work and traversal outside permitted URIs. Handle detached USB volumes, large trees, temporary permission loss and rescans.
- Treat filename/container and codec support separately. Skipping an unplayable MKV/MOV or HDR source is compliant if compatible decoding is impossible; never claim transcoding as a guaranteed fallback.
- Playlist operations must not conflate playlist **ordering** with ordering of **items inside a playlist**; item-edit interactions are open for follow-up specification.
- Playback loop and bad-file skipping need a finite exhaustion outcome. Resume checkpoints must be validated against the current URI and duration, including completed or deleted files.
- Preserve the media-type-specific remote controls: camera D-pad changes scene, local-video D-pad seeks ±10 seconds. OK and Back remain subject to operating-system input handling.

## Round 7: confirmed (D-59 to D-68)

| ID | Requirement | Owner's confirmed selection / acceptance boundary |
| --- | --- | --- |
| D-59 | Settings organization | **Basic settings plus a separate Advanced section, organized by categories**. Keep ordinary settings understandable and technical settings optional; precise taxonomy needs UX definition. |
| D-60 | Fullscreen mode switching | **Quick Cameras / My Videos switch through the menu opened with OK**, without returning to Home. Respect existing context-sensitive directional controls and OS-owned DreamService key handling. |
| D-61 | Default camera-playlist order | **Shuffle by default** for newly created camera playlists; the per-playlist sequence/shuffle option approved in D-49 remains available. This overrides the previous sequential-default recommendation. |
| D-62 | Default automatic camera rotation | **5 minutes** initially, user-adjustable to **1–60 minutes** per D-11; the default is not a fixed requirement for all playlists. |
| D-63 | Favorites presentation | **One Favorites screen with separate sections for cameras and local videos**. Keep media families visually distinguished. |
| D-64 | Playlist item management | **Batch-add multiple items, remove and freely reorder items within a playlist**. This complements playlist create/rename/reorder/duplicate/delete in D-50; maintain separate camera/video playlists. |
| D-65 | Player controls timeout | **Automatically hide contextual controls after 5 seconds of inactivity**. Do not conflate this controls timer with the separate, configurable weather/clock overlay hiding from D-25. |
| D-66 | Playback diagnostics | **Optional technical diagnostics panel**, with codec/resolution/buffering/errors as available, while keeping the standard player UI simple. Unsupported metrics are labeled unavailable, never invented. |
| D-67 | Preferences storage | **Automatically save settings, favorites and playlists on the TV only**. **No export/import or cloud synchronization** is part of the approved initial scope. Local persistence is not an external backup and may be lost on uninstall/factory reset. |
| D-68 | Internet restored during local fallback | **Finish the current local video, then return to live cameras**, with an accessible option to **remain on local videos**. Do not interrupt a local video during connection checks; only resume live after connectivity and at least one playable camera are verified. |

### Derived consequences (not additional approvals)

- User-facing Settings has concise categories and an explicit Advanced area. Preserve D-pad focus order; control-mode switch through OK must not create conflicts with pressing OK to reveal controls.
- **Default** camera playlist behavior is shuffled (D-61); **capability** remains sequential or shuffled (D-49). The first interval shown to a new user is 5 minutes (D-62), and users may change it across the 1–60 minute range (D-11).
- Five seconds (D-65) applies to the temporary **player control bar**, not to user-configured information-widget visibility and burn-in safeguards (D-25).
- Local-only automatic storage means crash-consistent persistence of metadata/checkpoints is needed; this does **not** imply an uninstall-proof backup or syncing through an account.
- On network restoration, finish **exactly the currently playing local item**, then seek a verified available camera unless the user elects to stay local. If no camera can play, stay in a safe local/retry state without skip storms.

## Round 8: first-release boundaries and engineering blockers (OPEN)

Questions are recommendations, **not approved scope**. These are intended to close the final high-impact product ambiguities before technical implementation plans.

1. **First milestone:** release an explicitly labeled development preview as soon as a reliable manual app/player exists, or only publish an APK once the approved end-to-end features are available? **Recommend:** iterative GitHub pre-release builds for testers, stable v1 only after all agreed release requirements pass.
2. **Initial device compatibility contract:** initially test Android 9/Fire OS 7 plus a modern Google TV, or promise all Android TV releases before any release? **Recommend:** publish device-by-device tested support; start with available Fire OS 7 and an Android TV/Google TV test device; do not label untested devices supported.
3. **Camera catalog launch threshold:** start with 10, 25, or 50+ *verified authorized and working* public webcams? **Recommend:** quality first, target 25 if rights and reliable sources can actually be confirmed; release must not invent sources to meet a number.
4. **Manual stream protocols:** accept only common HTTP-based HLS/DASH at launch or also RTSP where supported? **Recommend:** HLS/DASH first, evaluate RTSP after device testing; never accept arbitrary URLs as executable content.
5. **QR pairing context:** require phone and TV on the same local network with explicit pairing confirmation, or use a hosted bridge? **Recommend:** local-network pairing with short-lived token, no accounts/third-party cloud; validate safety before implementation.
6. **Offline map behavior:** show a simple cached/bundled list when offline, or require an offline full-world map? **Recommend:** camera list and basic cached metadata offline; network map tiles are not guaranteed offline.
7. **Application update notification:** check GitHub Releases when users open Settings and provide a link, or background update checks? **Recommend:** unobtrusive manual check in About, no silent APK installs.
8. **Version-1 quality gate:** fixed checklist with tested manual mode + feasible Dream mode, storage, playback, 24-hour soak, and rights/license checks, or ship based on visual inspection alone? **Recommend:** enforce checklist and label unsupported OEM features.
9. **Curated-content safety:** restrict first-party catalog to generally family-appropriate public scenes or include any publicly accessible camera? **Recommend:** reviewed, family-appropriate licensed content; do not conflate publicly viewable with permissible embedding.
10. **Crash reporting / privacy:** fully offline device-local diagnostics, optional user-exported reports, or automatic external telemetry? **Recommend:** local diagnostic logs that users can inspect/share intentionally, no automatic analytics/uploading.

## Later-round topics (not approved yet)

- Default rotation interval, repeat vs sequential, per-playlist rules, favorite persistence, sleep/wake/resume.
- Exact D-pad button map for camera/player, accessibility and one-handed remote usability.
- Media codec format/HDR capability, local scanning and USB disconnect recovery.
- Provider choice, licenses, source terms, camera quality thresholds, weather refresh/privacy and API quotas.
- Offline cache and curated media licensing/attribution; no implicit right to mirror content.
- Screensaver auto-selection restrictions and power behavior per OS/OEM.
- Testing matrix and measurable performance/reliability budgets.

## Decision history

- **2026-10-24:** D-01–D-08 confirmed by owner (round 1).
- **2026-10-24:** D-09–D-18 confirmed by owner (round 2), including map, immediate camera skip and USB.
- **2026-10-24:** D-19–D-28 confirmed by owner (round 3), overriding previous recommendations in favor of almost invisible UI, contextual remote behavior, 2D map with sidebar and both anti-static-overlay strategies.
- **2026-10-24:** D-29–D-38 confirmed by owner (round 4): broad categories, online catalog with local fallback, reviewed community suggestions, keyboard/QR camera entry, optional info panel, filters, detailed seven-day weather, manual/optional auto location, deferred wallpaper library, and configurable local fallback.
- **2026-10-25:** D-39–D-48 confirmed by owner (round 5): supported idle intervals, constrained keep-awake options, optional schedules, last-scene resume, OK/Back/directional Dream controls, non-disruptive offline fallback, fluency priority, minimal background work, USB removal notice and no boot auto-launch.
- **2026-10-25:** D-49–D-58 confirmed by owner (round 6): camera playlist order/shuffle, full playlist CRUD including reorder/duplicate, automatic recursive video scanning, MP4/MKV/WebM/MOV as device-supported, advance-and-loop, fixed ±10-second seeking, stable decoder/HDR fallback, no subtitles, skip defective files and resume last local-video position.
- **2026-10-25:** D-59–D-68 confirmed by owner (round 7): basic/advanced categorized Settings, quick OK mode switch, shuffle default, five-minute default rotation, one Favorites hub with separate sections, batch add/reorder playlist items, five-second control timeout, optional technical diagnostics, TV-only automatic persistence and post-video reconnection return with stay-local option.
- **2026-10-25:** Round 8 opened to define first-release boundaries, device test contract, allowed protocols/providers, secure QR design and quality gates. Recommendations remain nonbinding.
