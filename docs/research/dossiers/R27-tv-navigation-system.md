# R27 — TV navigation and Settings: complete route, focus and information architecture

**Research verdict:** GO for a route/focus test specification; HOLD all visual/navigation changes until independent UX and device evidence. **E1 code walkthrough**, not executed navigation tests. [Research #49](https://github.com/murilaiooo/openvista/issues/49). This expands the existing [IA research map](../2026-10-28-tv-navigation-information-architecture.md).

## Current code, reconciled on Oct 28

The app has one `LibraryRoute(page, id, family, offset, focus)` ([OpenVistaActivity.kt](../../../player/src/main/java/com/openvista/prototype/OpenVistaActivity.kt)); `open()` saves current focus, leaves screen, conditionally pushes history and refreshes; `handleOnBackPressed()` first hides visible player controls, then pops history, else returns Home. **Settings-to-Settings switches do not push extra history**, preserving one-Back exit intent. `refresh()` renders settings via `SettingsScreens.createShell()`, Home via `HomeScreen.create()`, Player via `PlayerScreen`, and the remaining routes via `LibraryScreens`.

**Verified route dispatch** in current `OpenVistaActivity.refresh()`:
- `home`: three large image-backed Cards (Cameras / My Videos / Favorites), Settings icon, Resume previous content.
- `onboarding`, `setup`: first-run/setup, skip and add/import flow.
- `cameras`, `videos`, `favorites`: lists and actions; favorites contains separately grouped source families.
- `details`: source metadata, Play/Favorite/Edit/Playlist/Reorder/Forecast/Remove.
- `playlists`, `playlist`: playlist collection and item editor.
- `folders`: **EXPLICITLY HANDLED** by `libraryScreens.folders(body)`; previous preliminary research claimed this was potentially missing. **That was incorrect and must not be carried into tickets**.
- `map`: saved camera pins based on known coordinates, with list.
- `player`: fullscreen SceneView and contextual controls; activity handles lifecycle snapshot.
- `settings` and legacy Settings route IDs: SettingsShell category aliases resolve to seven canonical rail items.

### Current Settings rail and boundaries
[SettingsShell.kt](../../../player/src/main/java/com/openvista/prototype/ui/SettingsShell.kt) defines seven categories: **Playback / Screensaver / Weather & Clock / Library / Appearance / Advanced / About**. [SettingsScreens.kt](../../../player/src/main/java/com/openvista/prototype/ui/SettingsScreens.kt) creates their contents and maps old route aliases. Available controls include rotation, resizing, quality cap, muted sound, fallback, screensaver schedule/display care, clock/forecast/location, import/local folders, appearance presets and technical diagnostics. Security-gated LAN pairing and update options are **not live features**.

## Reconstructed navigation and controls (observed code, not a tested UX trace)

```mermaid
flowchart TD
    START[Launch] --> O{First-run?}
    O -- Yes --> ON[Onboarding]
    O -- No --> H[Home: Cameras | My Videos | Favorites]
    ON --> SET[Setup] --> H
    ON -- Skip --> H
    H --> C[Cameras Library]
    H --> V[My Videos Library]
    H --> F[Favorites: Cameras and Videos]
    H --> S[Settings: 7 rail sections]
    H --> RES[Resume Player]
    C --> D[Camera Details]
    C --> MAP[User Sources Map]
    C --> PC[Camera Playlists]
    C --> M3U[Import M3U / Add URL]
    V --> VD[Video Details]
    V --> PF[Manage Folders]
    V --> PV[Video Playlists]
    V --> FILE[Choose File / Folder SAF]
    F --> D
    F --> VD
    D --> P[Fullscreen Player]
    VD --> P
    PC --> P
    PV --> P
    RES --> P
    P -- OK --> CTRL[Transient controls]
    CTRL --> P
    CTRL -- Library --> C
    CTRL -- Settings --> S
    P -- Back --> H
```

**Player keys:** [PlayerScreen.kt](../../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt): OK/Enter opens contextual panel (when absent); D-pad left/down goes previous camera or seeks −10 seconds for videos; right/up goes next camera or seeks +10 seconds; when menu visible D-pad must navigate menu focus rather than underlying media. Back hides panel first; a subsequent Back is activity navigation. Panel auto-hides after 5 seconds, unless a modal is open. Physical remote behavior, accessibility focus and OEM DreamService key dispatch still need E3.

## Research proposal: clean integration, **no fourth Home card**

Principle: **content actions where content is; preferences in Settings; rare technical integrations under Advanced**. Avoid duplicating source-add flows in four places; reuse one action/interface across buttons.

| Activity | Primary location | Contextual route | Settings location | Dependence |
| --- | --- | --- | --- | --- |
| R01 Smart Director | Cameras → Scene Modes | Player → Mode | Playback (opt-in selection policy) | R07, optional R02 |
| R02 Sun Chaser | Cameras → Filter/Mode | Player → Mode | Weather & Clock / Solar preference | source coords |
| R05 Data Monitor | none on Home | Player Info (optional usage) | Advanced → Data usage | own UID |
| R06 Live Preview | Camera list focus sidepane | no new route | optional preview toggle if demanded | one decoder |
| R07 Camera Health | Camera Details | status/Retry | Advanced → Health history/reset | observed events |
| R10 Soundscapes | Player → Audio | audio file picker | Playback → audio defaults | user local audio |
| R11 Routines | Screensaver → Routines | preview next transition | Screensaver | local clock |
| R12 NAS/WebDAV | My Videos → Add source | Network Folder picker | Library → Connections | credentials |
| R13 M3U subscription | Cameras → Import / Subscribe | Compare incoming changes | Library → Subscriptions | secure fetching |
| R18 Place Discovery | Camera Details / Player Info | Info → About this place | none by default | verified coords |
| R19 Home Assistant | no Home | authorized remote command only | Advanced → Integrations | HA server; optional |
| R21 Digital Window | Player visual preset | Appearance preview | Appearance → Scene presentation | single renderer |
| R22 Comfort | Player quick toggle | Appearance preview | Appearance → Comfort | existing overlays |
| R23 Multi-TV | no Home | authorized prompt | Advanced → Integrations | multiple devices; optional |
| R24 Google Photos | My Videos → Personal sources | Media selection | Library → Connections | partner/OAuth |
| R25 YouTube Live | **none** for ambient | optional isolated manual viewer only if approved | none for release | ads incompatibility |
| R26 New protocols | Cameras → Add supported source | source details + capability test | Advanced technical diagnostics | permitted source supply |

## Suggested TV navigation rules (hypotheses to test)
1. Keep Home three panels and only an unobtrusive Settings control. Do not use icon-only controls without accessible focus/description.
2. A core action should take ≤3 **screens** from Home (a proposed usability target; not a measured guarantee).
3. Advanced controls remain advanced: default Playback should not require configuring scoring weights, developer servers or HTTP headers.
4. Focus state is stable: entering a child remembers last focused source; Back returns to the same list row and scroll offset where possible; list changes gracefully fall back to closest surviving row.
5. Restore focus after activity recreation, picker cancel, permissions denial, edit dialogs and auto-hidden player menus. No accidental scene Next when an on-screen control has focus.
6. A secondary full-screen route always has a clear Back destination. Unknown route shows safe not-found/Back instead of a generic Settings page.
7. Source details should describe authentication errors without exposing secrets, and appearance previews should not require live network/video.
8. EN/PT-BR fit, 720p/1080p display safe margins, D-pad and accessibility semantics must pass independent tests.

## Typed route design (only if current string routes become costly)
Current `LibraryRoute.page` strings simplify the UI but allow accidental orphan aliases and implicit unknown-route fallbacks. **Do not refactor prematurely.** If route additions produce collisions, compare:
- (A) current string routes with centralized registry and exhaustive **routing tests** (preferred low-risk first step);
- (B) sealed route model with typed metadata/family/return state, a wider mechanical migration and risk to saved-state restore;
- (C) navigation library with Compose/Navigation replatforming (NO-GO solely for research; app is currently View-based).

Architecture target is one small `RouteRegistry.resolve(route): DestinationOrError` tested at the activity seam, not one new ViewModel per page. Beware route ID migration across saved instances and editing screen focus tags.

## Full matrix for independent navigation test generation

| From | Action | Expected destination | Back result | Blocker/empty case |
| --- | --- | --- | --- | --- |
| Cold install | Skip tutorial | Home | Home | No cameras/videos must not trap |
| Home | Cameras | Camera library | Home | Show Add URL/Import M3U actions |
| Home | My Videos | Video library | Home | Show choose file/folder |
| Home | Favorites | Favorite sections | Home | Two labeled empty sections |
| Home | Settings | Seven-section Settings | Home | First rail control focused |
| Cameras | Add/Edit URL | Dialog | Cameras/details | Cancel loses no data |
| Cameras | Map | Saved-source map/list | Cameras | No geotag: explain unknown coords |
| Cameras | Playlists | Camera playlists | Cameras | No playlists: create action |
| Videos | Manage folders | Folder management | Videos | SAF permission denied/revoked |
| Details | Play | Fullscreen player | Details | Invalid stream → retry/fallback |
| Player | OK → Controls | Menu over footage | Player | Auto-hide without losing focus |
| Player with controls | Back | Hide controls | Still Player | Do not exit on first Back |
| Player without controls | Back | Prior context | Prior list focus | Recovery in progress |
| Settings Playback | Rail to Appearance | Appearance contents | Home/previous | No separate history entry |
| My Videos | Add network folder (future) | Connection wizard | My Videos | Auth/SMB unavailable |
| Camera library | Daylight filter (future) | Filtered camera list | Camera library | Unknown coords remain discoverable |
| Advanced | HA pairing (future) | Consent/Permissions | Advanced | Unsupported / no server |
| My Videos | Photos (future) | Optional official picker | My Videos | No partner eligibility → disabled explanation |

## Settings density budget and proposed screen content

**Playback**: video quality, rotation, transitions, audio, fallback; optionally Smart Director and soundscape default only after approval.  
**Screensaver**: system integration, inactivity, overlay movement, basic schedule, optional Routines.  
**Weather & Clock**: source/viewer location, format, forecasts, optional daylight filter options.  
**Library**: source actions, M3U subscription management, local folders, optional NAS/Photos links (deep action remains in library).  
**Appearance**: live overlay preview, presets, positions, opacity, conditional Window/Comfort presets.  
**Advanced**: diagnostics and optional Data Monitor, source reliability history, integrations/pairing, privacy.  
**About**: version, content ownership/privacy, licenses, support.  

If a category exceeds one TV screen, use meaningful grouped rows with a short title and collapsed advanced subpages, rather than an endless dense toggle scroll. No duplicate Settings rail or fourth Home panel.

## Deep checks beyond route graph
- **Rebuild/jank**: `refresh()` removes all root views and recreates most screen contents; potential focus restoration and performance issue on large lists. Profile before refactoring; do not assert observed jank.
- **Pagination**: `LibraryScreens.library` uses page size 30 and client-side filters; test how focus+offset behave when a source is removed/renamed or when a new filtered subset is shorter.
- **Private source addresses**: avoid showing credential-bearing URLs on TV screenshots; ensure edit dialog and preview state masking.
- **Screenshot/demo parity**: the existing README screenshots are real app state, not original design references. Preserve owner-approved typography and visual layout.
- **Disabled settings**: `PairingSecurity.REVIEWED=false`; user sees disabled, not a fake working toggle.
- **One-player**: separate `SceneView` and Player lifecycle; all new source previews/audio must respect decoder ownership.
- **Physical map dependency**: OSM map provider has an approval gate, so the grid/list fallback must remain usable.

## Acceptance evidence still missing
A runnable route coverage matrix, Back/focus tests, 720p/1080p screenshot comparisons, EN/PT-BR overflow checks, Fire OS remote verification, 32-phase overlay positions and screenshot contrast. **No code/tests/screenshots were generated or executed by this GitHub-only research**. Any implementation tickets follow explicit owner approval and precede the release-quality 24h hardware soak.

**Correction notice:** Earlier IA planning described the `folders` route as potentially absent. A fresh read of current `OpenVistaActivity.refresh()` **explicitly contains** `"folders" -> libraryScreens.folders(body)`. Treat the earlier finding as retracted, not an open bug.