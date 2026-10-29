# R27 deep investigation — TV navigation model with route correctness and D-pad invariants

**Evidence:** E1 source walkthrough, no D-pad instrumentation/TV user test. **Decision:** route/focus model experiment before introducing new screens; continue three-card Home and seven Settings categories. [Original detailed audit](../dossiers/R27-tv-navigation-system.md).

## 1. Current authoritative route graph, verified in code

[OpenVistaActivity.kt](../../../player/src/main/java/com/openvista/prototype/OpenVistaActivity.kt) owns `LibraryRoute(page,id,family,offset,focus)`, `history`, `open`, `refresh`, and Back handling. `open` saves focus, leaves prior screen, conditionally records history; **Settings rail-to-rail does not push history**. `refresh` explicitly handles `"folders" -> libraryScreens.folders(body)` (older suspicion missing route **was incorrect and retracted**). It also handles:
```text
onboarding/setup → setup screens
home             → HomeScreen
cameras/videos/favorites → LibraryScreens.library
details          → LibraryScreens.details
playlists        → LibraryScreens.playlists
playlist         → LibraryScreens.playlist
folders          → LibraryScreens.folders
map              → LibraryScreens.map
settings family  → SettingsScreens + SettingsShell
player           → PlayerScreen + SceneView
```
[SettingsShell.kt](../../../player/src/main/java/com/openvista/prototype/ui/SettingsShell.kt) has seven rails: Playback, Screensaver, Weather & Clock, Library, Appearance, Advanced, About. [LibraryScreens.kt](../../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt) uses **page size 30**, stores filters, renders details and creates source-edit dialog. [PlayerScreen.kt](../../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt) responds to remote keys: OK reveals controls, D-pad left/down = previous camera or −10s video seek, right/up = next camera or +10s, controls auto-hide after 5s. Back hides controls before navigation. These are **source facts, not device UX passes**.

## 2. Navigation principles that create measurable value

- Keep the Home three hero cards: **Cameras / My Videos / Favorites**, Settings and Resume. Do not add cards for each optional experiment.
- “Use” actions belong near their media. “Configure” actions belong in Settings. Never force a user to navigate Advanced for Add Camera or Play Audio.
- A novice should find Add Source, My Local Videos, Favorites and Player Controls in ≤3 screen transitions from Home (**proposed target**, not measured).
- A robust Back function always has an origin: **save focus + scroll offset**, and after mutation return to nearest surviving item rather than top/first page.
- Every Settings row has actionable state: `Available`, `Off`, `Not configured`, `Pending review`, `Unsupported`, `Unavailable`. A disabled control must explain why, never be a dead button.
- Remote interactions must prioritize focus and never confuse D-pad seek with navigating an open menu.
- Design for 720p/1080p/4K with long Portuguese text, no touch or keyboard required for normal use after initial source import.

## 3. Complete proposed feature map with level control

```text
HOME (three panels, no change)
  Cameras
    All / Favorites / Playlists / Map / Add Source
    Camera rows → Details (status R07, place R18) → Play
    Filter/Mode (R01 director, R02 solar, when approved)
    Focus preview R06 (no extra screen)
    Add → paste URL / import M3U / subscribe R13 / RTSP R26
  My Videos
    Local and USB folders → File → Play
    Network folder R12 → chosen NAS source (only if configured)
    Personal Photos R24 → optional approved provider (auth-gated)
    Playlists
  Favorites
    Saved cameras and videos, never a duplicate source database
  PLAYER (single video owner)
    OK → transport, source family, audio, info, settings
    Contextual info: R18 About Place; audio R10; optional Comfort R22
    Back once: hide controls; Back twice: caller with focus preserved
  SETTINGS (existing seven rails)
    Playback → Quality, transition, mute, fallback, optional R01/R10
    Screensaver → idle/schedule, display-care, optional R11
    Weather & Clock → source/viewer location, solar options R02
    Library → source management, M3U R13, connections R12/R24
    Appearance → existing overlays, optional R21/R22 presets
    Advanced → diagnostics, Data usage R05, reliability R07, integrations R19/R23
    About → version, privacy/license/attribution
```

The **third screen** must not turn Settings into a second copy of the Camera library. Each optional provider has one canonical management destination. No new navigation framework or Compose migration just to add screens; existing View-based UI is capable if route state is tested.

## 4. State-transition and focus correctness matrix

| Start → Action | Destination | Back / focus invariant | Error/empty |
|---|---|---|---|
| Home → Cameras | camera list | Back → camera hero card | 0 cameras: Add action |
| Cameras → item at page 4 | Details | Back → same item/page 4 | item deleted → nearest row |
| Cameras → Map | saved source map/list | Back → filter state | no coords: list + explanation |
| Cameras → Add/edit | modal | Cancel → no mutation | invalid URL stays editable |
| My Videos → Folders | folder manager | Back → My Videos | no SAF permission: accessible action |
| My Videos → Network | connection wizard (future) | Back cancels safely | auth failed: never lose local list |
| Favorites → Camera detail | detail | Back → Favorites section | unfavorited during detail → clean return |
| Details → Play | fullscreen player | Back twice → details | network down uses existing fallback |
| Player → OK | controls menu | Back hides menu; underlying video keeps playing | no offscreen focus |
| Player → audio options | selector (future) | Back → controls, no volume change | no soundscape → Off |
| Settings → Appearance | replaces Settings content | one Back → previous outside Settings | not seven Back presses |
| Settings → optional provider | scoped dialog/wizard | Back returns to owning rail | unavailable → reason |
| Setup → cancel picker | setup | no ghost grant | storage denied remains usable |
| Media family switch | appropriate list or playback | no cross-family queue corruption | zero videos/cameras explicit |

## 5. Navigation safety details

**Source editor:** [LibraryScreens](../../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt) holds a dialog with HLS/DASH buttons and location fields, requiring real keyboard. Future protocol selector should be typed, not a raw mega-drop-down. **Settings aliases:** old `overlays`/legacy routes resolve into current rail and shouldn't push duplicate Back entries. **Filter state:** LibraryScreens search/category/country/offset must survive a details trip; if new results are fewer than page, clamp to last valid page and focus nearest row. **Screensaver:** Android DreamService remote Back behavior requires distinct hardware test. **Player:** when menu visible, D-pad must navigate controls instead of changing camera; test back press twice.

**No universal promise:** an app action “Start screensaver automatically” may be unsupported by OEM; leave explanatory settings. Phone control/HA/multi-TV pairing currently gated off. No disabled toggle should appear to work.

## 6. Test implementation strategy

Write a pure **route registry + origin/focus transition specification** without replatforming navigation. Automatable fake event test:
```text
route=Home, focus=cameraHero
open(Cameras), focus=source_42, pageOffset=90
open(Details[42]), open(Player[42])
Back (menu visible) → hide menu
Back (menu closed) → Details[42]
Back → Cameras offset=90, focus=source_42
Back → Home, focus=cameraHero
```
For each current route and legacy alias, test that a render dispatch exists and that Back cannot loop. Vary item deleted, wrong family, empty library, permission denied, 50 rapid focus movements, 720p/1080p safe-area. Make a **screen capture video on real TV** later with a remote-key event log and count interactions/time-to-task; no screenshots generated here.

## 7. Exact release/UX gates

- No inaccessible Settings row under 720p and PT-BR locale.
- No focus lost after player exit or modal cancel.
- No previously tested player freeze, loading-banner regression or extra decoder.
- No player-family mixing or ambiguous action labels.
- No presentation of blocked experimental features as ready.
- Task depth target assessed with **actual route tests**, not diagram alone.

**Status:** The above is a **proposed routing/interaction spec** tied to current source, not a completed usability study. Future E2 route tests and E3 physical remote evidence are mandatory. Existing three-card visual composition remains approved baseline; no new UI was implemented.
