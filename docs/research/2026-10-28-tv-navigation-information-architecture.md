# OpenVista TV information architecture and navigation map

**Status:** Audited current routes + proposed future IA; **no UI changes approved**. Parent: [#42](https://github.com/murilaiooo/openvista/issues/42). Date: 2026-10-28.

## Grounding: actual current code, not assumed screenshots

- [OpenVistaActivity.kt](../../player/src/main/java/com/openvista/prototype/OpenVistaActivity.kt): `LibraryRoute(page, id, family, ...)`, `history`, `open()`, `handleOnBackPressed()`, `refresh()`, `renderPlayer()`; a single active route. Settings category-to-category navigation does not push history.
- [HomeScreen.kt](../../player/src/main/java/com/openvista/prototype/ui/HomeScreen.kt): exactly three primary image panels **Cameras / My Videos / Favorites**, Settings icon, last-playback resume.
- [LibraryScreens.kt](../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt): onboarding/setup, cameras/videos/favorites collections, details, playlists, playlist, folders, map, source and file actions.
- [SettingsShell.kt](../../player/src/main/java/com/openvista/prototype/ui/SettingsShell.kt): **seven** existing rail categories: Playback, Screensaver, Weather & Clock, Library, Appearance, Advanced, About.
- [SettingsScreens.kt](../../player/src/main/java/com/openvista/prototype/ui/SettingsScreens.kt): handles legacy aliases `settings`, `overlays`, `overlay_editor`, `schedule`, `privacy`, `updates` among others; actual main content maps to seven category pages.
- [PlayerScreen.kt](../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt): fullscreen context UI, remote keys and player ownership.
- [SourceMapView.kt](../../player/src/main/java/com/openvista/prototype/ambient/SourceMapView.kt): 2D user-location map with clusters and list; not a full geodata catalog.

### Current route graph (readable text)

```text
App start
  └── Onboarding (first run; optional)
        ├── Setup → add stream / import M3U / choose local file or folder / weather
        └── Skip → Home
Home: [Cameras] [My Videos] [Favorites]               Settings icon / Resume
  ├── Cameras → camera list → details → play → Fullscreen player
  │              ├── filters / user playlists / map → details
  │              └── add URL / import M3U
  ├── My Videos → video list → details → play → Fullscreen player
  │                ├── video playlists
  │                └── file picker / folder picker / manage folders
  ├── Favorites → cameras section / videos section → details / player
  └── Settings (7-section rail)
       ├── Playback
       ├── Screensaver
       ├── Weather & Clock
       ├── Library (add/import/manage sources)
       ├── Appearance (overlay editor/presets)
       ├── Advanced (diagnostics / privacy / updates* / pairing*)
       └── About
  Fullscreen player: OK → transient context controls; Back → hide controls, then previous screen
  * Currently disabled pending security/release approval
```

### Audit correction: `folders` route is implemented

A renewed read of the current [OpenVistaActivity.refresh()](../../player/src/main/java/com/openvista/prototype/OpenVistaActivity.kt) explicitly shows `"folders" -> libraryScreens.folders(body)`. The earlier suspicion that this route was missing was **wrong and retracted**; there is no evidence of a `folders` dispatch bug. Continue general route/focus tests, but **do not open an implementation bug on this disproven basis**. The detailed route matrix and states appear in [R27](dossiers/R27-tv-navigation-system.md).

Other audit questions: legacy `overlays` alias should not make the user enter duplicate Settings trees; verify actual D-pad focus return, canceled source editor, player-to-Settings, Home/resume state, and settings top-level Back behavior. Focus must be visually obvious and deterministic.

## Proposed interaction principle

**Keep the existing three-card Home unchanged.** Do not add 14 top-level tiles. Use **contextual discovery** in the relevant library/player and **progressive disclosure** in existing Settings. Most advanced features belong under a dedicated category *if and only if* they eventually pass research gates. People must be able to use the app with zero configuration, zero accounts and zero cameras.

### Proposed app map, not implementation plan

```text
HOME (3 hero cards + existing Settings / Resume)
  ├── CAMERAS
  │   ├── All cameras (default) → focused preview (R06)
  │   ├── Filters: location / favorites / available / daylight (R02) / weather
  │   ├── Playlists: manual + optional Smart Director rules (R01) + World Journey (separate research)
  │   ├── Map → nearby sources → camera detail
  │   ├── Camera detail → health / stream quality (R07), place info (R18), source edit
  │   └── Add camera → direct HLS/DASH / user M3U / subscribed user M3U (R13) / gated protocol adapters
  ├── MY VIDEOS
  │   ├── On-device / USB and permissioned folders
  │   ├── Network library (R12: SMB/WebDAV, optional)
  │   ├── My photos and videos (R24: Google Photos only if approved)
  │   └── Playlists / file details
  ├── FAVORITES
  │   ├── Cameras
  │   └── Local videos (future connected personal media, if accepted)
  ├── PLAYER (single fullscreen playback owner from any source)
  │   ├── OK → transient Play/Next/Previous/Info / switch source family
  │   ├── Info → metadata + place context (R18) + status
  │   ├── Contextual scene mode → Smart Director (R01) / Sun Chaser (R02)
  │   └── Audio menu → camera audio / local soundscapes (R10)
  └── SETTINGS (7 existing rail entries; avoid second settings application)
      ├── Playback → rotation, transition, quality, optional Smart Director (R01)
      ├── Screensaver → schedule, idle/display-care → advanced routines (R11)
      ├── Weather & Clock → location, context, forecast, local solar/daylight settings (R02)
      ├── Library → source imports, remote storage (R12), subscriptions (R13), Google Photos (R24)
      ├── Appearance → overlays / visual presets / Digital Window (R21) / Comfort Mode (R22)
      ├── Advanced → diagnostics / Data Monitor (R05) / health stats (R07)
      │              / pairing / integrations (HA R19 / multi-TV R23, both gated)
      └── About → version, licenses, disclosure
```

## Where each selected feature belongs

| Feature | First user-visible entry (proposed) | Secondary settings | Do not do |
| --- | --- | --- | --- |
| R01 Smart Director | Cameras → Scene mode / Player context | Playback → Automatic selection | Automatic silent takeover of manual playlist |
| R02 Sun Chaser | Cameras → Daylight filter / Scene mode | Weather & Clock → Solar preferences | Show fake sunny badge for missing coordinates |
| R05 Data Monitor | Advanced → Network & Diagnostics | Small optional status in player details | Persistent bandwidth ticker over scenery |
| R06 Live Preview | Cameras → focused row/card | Advanced → Preview behavior (only if needed) | Open 10 decoders simultaneously |
| R07 Camera Health | Camera detail → reliability | Advanced → Reset history | Confuse offline with permanently failed |
| R10 Soundscapes | Player → Audio | Playback → Soundscapes | Re-enable audio by default |
| R11 Routines | Screensaver → Routines | Playback profile choices | Wake TV outside OEM permission |
| R12 Network Library | My Videos → Add network folder | Library → Network connections | Expose credentials in list/log |
| R13 M3U subscriptions | Cameras → Import / Subscribe | Library → Subscription refresh | First-party curated catalog |
| R18 Place Info | Player → Info / Camera detail | Optional info-display preference | Permanent distracting overlays |
| R19 Home Assistant | Advanced → Integrations | Local permissions/commands screen | Open unauthenticated LAN command listener |
| R21 Digital Window | Appearance → Preset selector | Appearance → Frame geometry | Replace fullscreen default |
| R22 Comfort | Appearance → Comfort / Night preset | Screensaver → Scheduled visual behavior | Claims of real backlight adjustment |
| R23 Multi-TV | Advanced → Integrations (future) | Pair/revoke device screen | Forced cloud user account |
| R24 Google Photos | My Videos → Add personal library | Library → Account/connection state | Implicit Google login at first run |
| R25 YouTube | **No ambient entry; research blocked** | None | Ad bypass or scraping |
| R26 protocols | Cameras → Add source → typed source options | Advanced diagnostics only | Generic paste-any-webpage promise |

## Settings architecture guardrails

1. **One Settings rail**; existing seven categories remain authoritative until user approves a redesign. No duplicates or buried settings-only destinations.
2. Put frequent actions near the content: Add camera in Cameras, Add NAS in My Videos, Info in Player. Settings should control *how* a feature behaves, not be the only way to *use* it.
3. Primary actions must be accessible in at most **three D-pad navigation screens from Home** for normal use (research target, not measured).
4. Basic actions should avoid vocabulary such as RTSP handshake, UID, OAuth. Advanced diagnostics hold technical data.
5. Every proposed Settings row has a clear state: Off, On, Not configured, Pending review, Unsupported or Unavailable. Disabled rows explain why.
6. Preserve user focus on return from details, picker, editor, modal and full-screen player; Back first dismisses modal/controls, then returns to originating list, then Home.
7. Do not push a history entry when switching Settings rail categories; it should not take seven Back presses to exit.
8. Standalone screens must survive process recreation, localization and remote-only use. Permission failures never trap the user.

## State / routing review checklist

- Current route IDs and call sites match all UI actions (including `folders`).
- Source actions work from Library and Settings without divergent forms.
- Default Back behavior from Home, onboarding/setup, map, playlist, source editor, and player.
- Modal focus/back for long-press and remote key collisions; sources may be playback-seek or navigation context, never both at once.
- Zero cameras; zero videos; no USB; expired M3U; weather provider offline; LAN disabled; partner OAuth unavailable.
- 720p, 1080p, 4K safe areas; short and long Portuguese labels; accessible labels and focus rings.
- Exactly one active video decoder; previews suspend while player is active; no frame freezing hacks.
- Avoid 14 top-level cards or a settings page of 50 toggles; most novel modes enter via **Mode selector** with sensible defaults.

## Diagram policy

This is a **navigation map**, not a literal geographic map and not a finished wireframe. After approval, create per-screen TV-safe wireframes with focus order, state diagrams, empty/loading/error designs and back-stack test assertions. Visual references must be verified against the owner's original Home/Settings/Player examples before implementation.
