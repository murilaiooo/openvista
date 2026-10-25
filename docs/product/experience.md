# OpenVista interaction and visual experience

**Status:** Product requirements confirmed through round 7 (2026-10-25). This is a UX brief, **not** a finished layout or tested app.

## Visual identity

The live scene or local scenic video occupies the entire TV display. During passive watching, OpenVista should appear almost invisible: no persistent navigation rail, large panel, or scrolling controls. Default information is a discreet clock and current weather. The full seven-day forecast is revealed on demand; widgets remain fully customizable.

## Manually launched Home

The manual entry point shows **three large image-backed cards**: **Cameras**, **My Videos**, **Favorites**. Artwork must not conceal labels or focus outlines. User can navigate with a standard TV D-pad. Final artwork, placement, typography and colors are not yet decided.

## Cameras

- Enter through the user-managed camera library or a **2D interactive map with clustered markers and a lateral list** showing only sources with user-provided location data. Preserve keyboard-free D-pad navigation; allow focus to move between map markers/list.
- The user can add permitted custom URLs using **either the TV keyboard or a QR-assisted second-device flow** (secure pairing design still TBD).
- New camera playlists start **shuffled** (user can change to sequence), with an initial **5-minute rotation** adjustable from 1–60 minutes.
- The user-managed library offers themed categories, geographic filters, favorites and observed availability when metadata exists; remote-list metadata refresh is opt-in, with locally saved source metadata available offline.
- An optional on-demand camera information panel gives name, country, camera-local time, verified availability, resolution where known and attribution.
- Fullscreen camera playback supports contextual directional scene changes and optional controls that appear during input. In supported DreamService mode, **OK shows controls and Back exits**; directional navigation is subject to OEM key handling.
- Automatically rotate the active camera after the configured 1–60 minute interval.
- An unavailable camera is **skipped immediately**. If all candidates fail, use the user's preconfigured **local-video playlist fallback** if accessible; otherwise show a clear notice and Retry, never loop forever.

## My Videos

- Open a selected internal-storage or USB folder as permitted by Android's scoped-storage picker; **scan accessible subfolders automatically**.
- Treat **MP4/MKV/WebM/MOV** as candidate containers subject to the decoder on the TV. Prioritize stable compatible playback; skip unreadable or unsupported items with a discreet message and optional explanation.
- Play local media in sequence or shuffle within a **local-video playlist** (camera playlists remain separate); automatically advance to the next video and **repeat the playlist** at its end.
- Camera playlists independently support sequential/shuffled play. Users may create, rename, reorder, duplicate and delete playlists **and batch-add, remove and freely reorder individual playlist items**.
- Fill/crop without stretching by default; omit subtitle settings, loading and display features for local videos.
- Unlike camera mode, directional controls seek **exactly 10 seconds** backward/forward (clamped to valid media positions). On reopening a local video, **resume the last saved playback position**, including automatic Dream reentry where feasible.

## Contextual controls and Favorites (Round 7)

- **OK reveals quick controls** including direct **Cameras / My Videos switching**; dismiss the transient controls after **5 seconds without interaction**, without altering the separately configured clock/weather visibility.
- Standard remote arrows navigate the focused menu while it is visible; otherwise preserve camera-change and local-video ±10-second seek mappings. In Dream mode, the OS may intercept keys.
- One **Favorites** destination includes separate sections for camera favorites and local video favorites.
- Basic settings use clear categories, while a separate Advanced area contains technical options. An optional diagnostics screen shows only measurements actually available from the decoder/source.
- Automatically store playlists/favorites/preferences on TV; no account, import/export or cloud backup in the first release. Clearing app data or uninstalling may erase these.

## Weather and overlays

- Default **compact clock + current conditions**.
- Seven-day forecast appears **only when requested** by default. Extended details include temperatures, rain, wind, humidity and sunrise/sunset when available.
- Weather source context is either current camera location or a user-chosen city, set through manual city search or optional consent-based device detection where supported.
- Provide quick presets **and** advanced editor. Detailed customization gestures/properties are not yet specified.
- Two distinct display-care settings: **periodically move static overlay widgets** and **automatically hide them after inactivity**. Each should be individually adjustable; this reduces but does not remove burn-in risk.

## First-run onboarding

Show a **short, skippable** walkthrough for camera choice, optional local folder and weather setup. Never require a user account, location permission, or payment to enter the manual Home. The user must be able to revisit setup from settings.

## Automatic screensaver runtime (Round 5)

- Support OS-permitted idle intervals of **1, 3, 5, 10, 15 and 30 minutes**, explaining when OEM settings cannot be changed.
- On automatic entry, attempt to continue the **last selected camera or video**; unavailable sources follow the configured fallback behavior.
- Run continuously when allowed, with an optional user schedule and device-constrained keep-awake options. Never override TV sleep/standby or auto-open at boot.
- During loss of internet, play a configured **local video fallback**. Check reconnection without pausing, restarting or replacing the playing local video. When an online camera is playable, **finish the active local file**, then return to cameras unless the user selects **remain on local videos**.
- Disconnecting USB presents an understandable notice and alternative source choice instead of freezing.
- Pause unnecessary off-screen playback and refresh data using provider-compatible caching. Prioritize smooth playback over forcing 4K.

## Scene transitions and device constraints

Allow selection between direct cuts and smooth transitions. Prefer reduced-cost/direct change where overlapping decoding would stress hardware. The implementation must not assert transition smoothness until measured on low-powered TVs.

## Localization

Start from TV language/region. Support **Portuguese (Brazil)** and **English** with manually adjustable language, 12/24-hour clock and °C/°F temperature. Exact fallback language and formatting defaults must be tested.

## Product acceptance ideas (pending tests)

- All Home cards and controls usable without touch/mouse.
- Cameras and local video respond **differently but predictably** to remote arrows.
- Focus remains visible within map list and advanced editor.
- Information overlays never mask core camera content unnecessarily.
- On-screen fade settings degrade cleanly on constrained hardware.
- User can skip onboarding and still use Home.
- Local USB disconnect and network loss never freeze the UI.
