# OpenVista product decisions

**Historical owner decision record:** Eight product rounds (78 owner-confirmed decisions) were completed on 2026-10-25. The original decisions below are preserved verbatim as a historical contract, with C-01 overriding the bundled-catalog assumptions. A Kotlin TV development APK now exists; see [current engineering status](../STATUS.md) for implementation and validation, not this record. Reopen decisions only when technical evidence or explicit owner change requires it.

Only owner-confirmed choices are requirements. Open questions, design recommendations and proposed technical solutions are **not** approved. Product agreement is not proof of compatibility or implementation.

> **Current-scope clarification C-01 (2026-10-25):** OpenVista does **not** supply camera streams, source lists, or videos. Users bring their own. This **supersedes** the first-party curated-content assumptions in D-02, D-10, D-29–D-31, D-71, and D-77 while retaining library browsing, map, filtering, playback, and customization goals. The original decision rows remain unchanged for historical traceability. See [User-managed content](user-managed-content.md). No bundled 25-camera release gate remains.

## Product baseline

OpenVista is a free, ad-free, account-free ambient Android TV application and, where the OS allows, an automatic system screensaver. It shows public, permitted live cameras or looping user-owned videos. It includes configurable clock and weather overlays (current conditions and seven-day forecast), with weather for the camera location or the user's chosen location. First **test APK is local-only**, and GitHub is a later public distribution target (D-69 refines D-08). No APK exists yet.

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
| D-42 | Start/resume scene in automatic mode | **Continue the last used camera or video**, if it remains accessible. When unavailable use skip/fallback policy; D-58 confirms resuming a local video's last saved position. |
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
- On loss of network, keep the configured local fallback playing while performing **bounded, non-disruptive** connection checks. D-68 resolves restoration: finish the active local video before returning to a verified working live camera, unless user elects to remain local.
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
- Playlist operations must not conflate playlist **ordering** with ordering of **items inside a playlist**; both are approved (D-50 and D-64), and the exact UI can be designed during implementation.
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

## Round 8: confirmed (D-69 to D-78) — local prototype and v1 delivery

| ID | Topic | Owner's confirmed choice / boundary |
| --- | --- | --- |
| D-69 | First APK distribution | **First APK stays local for testing; publish via GitHub only later.** This refines D-08: GitHub is intended for eventual public distribution, not the first build. No automatic upload of test binaries to Releases. |
| D-70 | Initial device validation | **First test only Hisense 58U6HF / Fire OS 7**. Explicitly label all other models/versions **untested**, not supported, until real-device evidence exists. This is an initial test scope, not a permanent compatibility restriction. |
| D-71 | Camera catalog launch threshold | **At least 25 distinct verified, currently working and authorized public cameras** before releasing v1. Count only sources with recorded permission/terms, attribution, tested playback on target device, and reasonable reliability. Do not invent or aggregate inaccessible sources to meet quota. |
| D-72 | Streaming protocols | **HLS and MPEG-DASH first** for included/custom live sources, within supported Media3/device codecs; consider RTSP and other protocols after tests and a later scope decision. |
| D-73 | QR assisted input | **TV and phone on same local Wi-Fi/LAN, with explicit pairing confirmation**. No account or required cloud relay; prototype authenticated short-lived local pairing, deny unwanted access and do not leak private URLs. |
| D-74 | Offline map | If map/network content cannot load, show a **local list of saved/cached cameras** with a clear notice that online map tiles are unavailable; metadata availability does not imply stream playback offline. |
| D-75 | Application updating | **Automatically download and install application updates** as the desired feature. Treat update *discovery*, authenticated/verified *download*, and Android-controlled *installation* separately. Truly unattended install must be **proven legal/available on the target OS/install ownership/API**; when blocked, use the supported package-installer confirmation flow, explain it honestly, and never attempt root/ADB/privileged bypass. No production update distribution until the eventual GitHub release channel is approved. |
| D-76 | Stable v1 release gate | **Documented media playback, compatibility, 24-hour stability, privacy, and provider/content-license tests** required before labeling v1 stable. At minimum collect evidence for the Hisense/Fire OS 7 target; other devices remain unverified. |
| D-77 | First-party content suitability | Curated camera sources must be **authorized and suitable for family viewing**. Review each source before release, support removals/takedowns and do not assume publicly reachable implies redistributable. |
| D-78 | Error-report privacy | Keep diagnostic logs **locally** and let the user **copy or share a report deliberately on demand**; no automatic external telemetry. Redact personal URLs, local file paths and secrets by default. D-67 still governs settings/favorites/playlists: no automatic off-device backup/import/export. |

### Engineering interpretation (not additional approval)

- D-75 is a **desired outcome subject to Android package install rules**, not proof that background silent installation works. Supported fallback may involve mandatory user approval; document any OS restrictions on the UI. Verify signing continuity, release integrity, rollback and installer-of-record rules without privileged hacks.
- A local test APK can precede satisfying D-71/D-76 if labeled an engineering prototype and **not** v1; only the future stable GitHub publication requires the full 25-camera gate.
- Prototype on the Hisense first; do not claim supported status for untested Google/Android TV devices.
- Do not confuse **on-demand diagnostic report sharing** (approved) with **automatic settings export/backup** (explicitly excluded by D-67).

## Product discovery outcome

The owner answered **all eight rounds**. The next step is engineering research/prototypes plus a v1 acceptance matrix and prioritized issues. New product questions should be raised only for actual blockers, not continue repetitive wide-round interviews.

## Source-scope amendment C-01 (confirmed after round 8)

- The owner explicitly clarified: **the OpenVista project does not provide camera feeds, streaming lists, or video media to end users**. Each user supplies and manages their own.
- The earlier requirement to curate/distribute at least **25 first-party cameras** is superseded. Testing private/temporary research feeds is not a promise to ship those feeds.
- User-controlled source import and an empty-library experience are required. Import parser format details remain an engineering choice, to be validated before making support claims.
- All project documentation, code comments, issue discussions, pull requests, and public copy must be **English**, even when the owner's instructions are Portuguese. Original place names and URLs are retained.
- This amendment does not re-open unrelated playback, UX, screensaver, privacy, performance, or hardware compatibility decisions.

## Later engineering questions (not approved yet)

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
- **2026-10-25:** D-69–D-78 confirmed by owner (round 8): local-only first test APK, initial Hisense-only validation, at least 25 authorized/working cameras for stable v1, HLS/DASH, LAN QR pairing with user confirmation, offline local camera list, desired automatic update download/install subject to OS installer restrictions, documented playback/compatibility/24-hour/privacy/licenses gate, family-appropriate sources and user-shared local error reports. Eight-round product interview concluded; engineering validation and v1 issue planning begin.
