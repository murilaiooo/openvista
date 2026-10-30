Method: design review only (detector not applicable, native Android TV platform)

# OpenVista: Impeccable UX/UI critique and native quality audit

**Date:** October 30, 2026  
**Target:** the Kotlin Android TV UI on `main`, inspected at `30cfdeb46658afab0d9adad9ae286178ecfe01ca`  
**Mode:** Operate for Home, library, settings, editors, dialogs; Experience for full-screen ambient playback  
**Framework:** [pbakaus/impeccable, v4.8.0](https://github.com/pbakaus/impeccable) (`critique`, `audit.native`, `android`, `onboard`, `operate` playbooks)  
**Evidence class:** source inspection plus the real October 28 [Home](../../assets/screenshots/home.webp), [Settings](../../assets/screenshots/settings.webp), and [Live](../../assets/screenshots/live-camera.webp) captures. [Issue #54](https://github.com/murilaiooo/openvista/issues/54) separately documents an October 30 walkthrough on the owner's Hisense with the integration build.

> This run did not control the TV or record fresh hardware measurements. The Oct 28 captures may predate Oct 30 changes, so observations from them are visual evidence only for the captured revision. All scores are provisional heuristic judgments, not accessibility certification or proven regressions. The web DOM detector is not applicable to native Android Views and was intentionally not run.

## Design specificity

**Verdict: distinctive and coherent.** Three scenic Home columns, the atlas Settings composition, and a quiet four-zone Live overlay make OpenVista recognizable. It does not resemble a generic video app or a stock Material dashboard. The right next step is a *behavioral and legibility refinement*, not a replacement of the approved visual world.

**Automated detector:** not applicable (native platform); no generated browser overlay exists.

### What's working

1. **Appropriate information hierarchy.** The real Home gives each of Cameras, My Videos, Favorites an entire cinematic destination. The existing source counts and explanatory copy connect appearance to user-owned content ([HomeScreen.kt](../../player/src/main/java/com/openvista/prototype/ui/HomeScreen.kt)).
2. **Consistent focus vocabulary.** A shared cyan focus rim and quieter selected fills exist in `TvDesign.control` and `SettingsShell.RailItem`. Home preserves the focused panel when returning.
3. **Truthful, considered asynchronous states.** The camera preview owns one muted decoder and distinguishes idle/loading/offline/unavailable/unsupported. Shared loading is delayed to prevent flicker, and library/save operations use recoverable messages ([loading-state contract](../design/loading-states.md)).

## Design health score: 26/40 (provisional)

| # | Nielsen heuristic | /4 | Evidence / issue |
|---|---|---:|---|
| 1 | Visibility of system status | 3 | Shared delayed loading and preview retry exist; some details can still show unknown availability after preview. |
| 2 | Match with real-world language | 3 | Plain primary destinations, but camera details can show raw `yyyy-MM-dd HH:mm z` time. |
| 3 | User control and freedom | 3 | Back, Cancel, confirmation and visible settings are implemented; editing is a lengthy remote dialog. |
| 4 | Consistency and standards | 2 | Heart on Home versus star in details; mismatched Edit/Move/Remove icon metaphors; player menu differs from shared row language. |
| 5 | Error prevention | 3 | URL/location validation, duplicate-source checks and removal confirmation; save errors are inline but at the end of a long form. |
| 6 | Recognition rather than recall | 2 | Remote shortcuts and selection/transport state are not always visible; users must remember list page and player-arrow behavior. |
| 7 | Flexibility and efficiency | 2 | Bulk playlist actions and quick camera preview exist, but filters use four text fields and the library pages at 30 items. |
| 8 | Aesthetic and minimalist design | 3 | Visually disciplined; 20+ Appearance controls and a 12-action player menu undermine task-level simplicity. |
| 9 | Error recognition and recovery | 3 | Typed playback failures, retry/backoff and preview retry exist; verify source-specific status and USB/keyboard transitions. |
| 10 | Help and documentation | 2 | Skippable intro exists, but long editor terminology, remote shortcuts and advanced choices have limited contextual guidance. |
| **Total** | | **26/40** | **Good visual identity, uneven TV interaction.** |

### Cognitive load

- **Home:** low. Three big destinations have a clear choice and explicit labels.
- **Appearance Settings:** high for a single browsing session. One scroll contains preset/layout, a preview, four visibility toggles plus location, corner/size/offset/color, and five opacity controls ([SettingsScreens.kt](../../player/src/main/java/com/openvista/prototype/ui/SettingsScreens.kt)). Chunking exists, but essential/advanced progression does not.
- **Add/Edit Stream:** high. The form exposes name, URL, stream type, category, country, city, latitude, longitude, timezone, attribution, and location lookup at once ([LibraryScreens.kt](../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt)). Most users do not need coordinates to get their first video playing.
- **Player menu:** high at the decision point. Three rows of four buttons present 12 equally styled actions when the most common need is Pause/Play, Previous/Next, and sound.

Three material checklist failures: too many simultaneous choices on task-heavy pages; weak progressive disclosure; and short-term memory burden in controls whose current state is not reflected in their labels.

## Priority issues

### P1. Legibility collapses on high-key scenes

**Where:** Live overlay, [real captured frame](../../assets/screenshots/live-camera.webp); `AmbientOverlay.kt`.  
**Evidence:** The light logo at upper left fades into a bright sky. The lower-left label/date and source-side white metrics rely on footage luminance. Issue #54 independently observed the same on bright video.  
**Impact:** The information is precisely least readable when the window is most visually attractive. Users may increase opacity or disable the overlay rather than trust it.  
**Fix:** Add bounded local luminance-independent edge shading or soft text shadows under *each complete group*, respecting user opacity and movement. Compare bright sky, white buildings, snow, high-detail night scenes and dark ocean. Keep the video genuinely moving; no PixelCopy/still hold.  
**Suggested command:** `/impeccable polish` + `/impeccable audit`  
**Tracker:** [#54](https://github.com/murilaiooo/openvista/issues/54).

### P1. Camera creation/editing asks for specialist metadata before first value

**Where:** [LibraryScreens.kt, editSource](../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt#L215-L297), [TvUi.kt, field](../../player/src/main/java/com/openvista/prototype/ui/TvUi.kt#L240-L263).  
**Evidence:** Eleven-ish inputs and actions live in a vertically capped AlertDialog. A URL and a name would suffice for a basic camera; metadata is optional under C-01. The viewer must navigate and enter text with a TV keyboard. Validation writes the error line after the final metadata input, far from an offending field.  
**Impact:** High activation friction and avoidable abandoned form states.  
**Fix:** First screen: **Name**, **Stream URL**, **HLS/DASH**, **Save**, with optional **Location & details** expansion. Preserve all existing fields for editors. Place field-specific validation near the relevant input, keep Save/Cancel accessible while the TV IME is open, and confirm successful save. Let Find Location fill connected metadata after explicit user action. Do not add a compulsory account, unapproved remote pairing, or bundled demo stream.  
**Suggested command:** `/impeccable onboard`, `/impeccable distill`, `/impeccable clarify`.

### P1. Remote focus requires extra hops and hidden knowledge

**Where:** [SettingsShell.kt](../../player/src/main/java/com/openvista/prototype/ui/SettingsShell.kt#L81-L123), [PlayerScreen.kt](../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt#L57-L143), camera map.  
**Evidence:** After opening a Settings category, focus can remain on the rail; the user must press Right before editing. Player Up/Right both advance cameras, while Down/Left go backward (the same arrows seek video by 10 s). A menu hides 12 actions behind OK. Issue #54 saw extra taps and the map focus/marker disconnect on real hardware.  
**Impact:** Slow daily operation and repeated trial-and-error.  
**Fix:** On category confirmation, move focus to the first available setting or provide a clear, immediate category preview. Give the player a small contextual hint the first time each media family is used, with an unobtrusive dismiss/learn-again path. Map list focus should identify its matching pin. Test exact Back/focus restoration routes.  
**Suggested command:** `/impeccable audit`, `/impeccable clarify`.  
**Tracker:** [#54](https://github.com/murilaiooo/openvista/issues/54).

### P1. Readability and accessibility have untested scaling boundaries

**Where:** [TvDesign.kt](../../player/src/main/java/com/openvista/prototype/ui/TvDesign.kt#L36-L47), [TvUi.kt](../../player/src/main/java/com/openvista/prototype/ui/TvUi.kt#L31-L48), [AppLocale.kt](../../player/src/main/java/com/openvista/prototype/ui/AppLocale.kt).  
**Evidence:** Current typography tokens are 12sp body, 9sp summary, 7.5sp caps on a normalized 960×540dp canvas; many controls use explicit heights and one-line ellipsis. The repository does not yet contain a complete 720p, fontScale 1.3, TalkBack and long Portuguese-text comparison. This is a *verification gap*, not proof the font-scale feature is broken.  
**Impact:** Low-vision viewers and smaller TVs may lose information or see focus/labels clipped.  
**Fix:** Record 720p and 1080p hardware captures at system font scaling 1.0 and 1.3 (restore original settings after test), TalkBack reading order, overscan, color/opacity contrast, long pt-BR city names and strings. Measure failures and fix only proven issues without globally abandoning the approved layout.  
**Suggested command:** `/impeccable audit`, `/impeccable adapt`, `/impeccable typeset`.

### P2. Appearance needs simple and advanced paths

**Where:** [SettingsScreens.kt, appearance](../../player/src/main/java/com/openvista/prototype/ui/SettingsScreens.kt#L147-L225).  
**Evidence:** Layout and preset duplicate some of the same choices, followed by all visibility, position, scale, color and per-group opacity options.  
**Impact:** Normal users must parse a powerful visual editor when they only want a clock, weather or a quieter screen.  
**Fix:** Keep **Preset** and a real preview in the first section. Move per-zone switches, coordinates, and opacity behind a focusable **Advanced overlay settings** row within the existing Appearance category. Make preset consequences clear; do not create a parallel Settings menu.  
**Suggested command:** `/impeccable distill`, `/impeccable layout`.

### P2. Lists, filters and player actions need efficient hierarchy

**Where:** [LibraryScreens.kt](../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt#L72-L142), [filters](../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt#L299-L320), [PlayerScreen.kt](../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt#L57-L77).  
**Evidence:** Saved cameras use 30-item pages and a four-text-field filter; player controls present 12 similarly weighted items and static Pause/Play and Sound names.  
**Impact:** Too much typing and button hunting for a couch interface.  
**Fix:** Scroll a virtual/recycled camera list, preserve selection/preview and focus, expose quick category/country choices from saved sources, move rare player actions to More, and make Pause/Play and Sound labels reflect state. Verify low-powered device/decoder cost.  
**Suggested command:** `/impeccable distill`, `/impeccable clarify`, `/impeccable optimize`.  
**Tracker:** [#54](https://github.com/murilaiooo/openvista/issues/54).

### P2. Small but visible semantic drift

**Where:** `HomeScreen.kt` and `LibraryScreens.kt`.  
**Evidence:** Home uses a heart for Favorites; details and saved rows use a star. Edit uses a link icon, Move uses generic four-arrow Move, and Remove uses Report. Camera local time appears in a raw data format.  
**Impact:** Users cannot quickly transfer learned visual meanings across screens.  
**Fix:** One favorite symbol, dedicated pencil/up/down/trash icons, localized date/time matching selected 12/24-hour preference.  
**Suggested command:** `/impeccable polish`, `/impeccable clarify`.  
**Tracker:** [#54](https://github.com/murilaiooo/openvista/issues/54).

### P2. First-run journey should be tested as a zero-content product

**Where:** [LibraryScreens.kt, onboarding](../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt#L51-L70), zero-library branch, Home empty copy.  
**Evidence:** Onboarding is skippable, which is good, but `Get started` marks `onboardingComplete` immediately before entering Setup. The same source-actions row is reused in both a full library and a zero-library state. The original #13 described a richer first-run empty experience, but its closure is not acceptance proof.  
**Impact:** A user with no M3U files, local media, or stream URL may leave without seeing a meaningful next step.  
**Fix:** Test fresh install with zero sources. Place an explicit primary add/import action with concise guidance, make later setup re-entry easy, and distinguish skipping from successful setup completion. No fake sample content.  
**Suggested command:** `/impeccable onboard`, `/impeccable harden`.

## Native technical quality audit: 12/20 (provisional)

| Dimension | /4 | Finding |
|---|---:|---|
| Accessibility | 2 | Accessibility nodes/headings and focus semantics exist, but font-scale/contrast/TalkBack QA remains incomplete; compact labels need scrutiny. |
| Performance | 2 | One-decoder preview, delayed loading, reusable tokens, async Home imagery are positive. Long settings pages and camera list scale lack benchmarked performance evidence. |
| Appearance & theming | 3 | Coherent Inter/navy/teal/cyan token set; on-video text contrast and icon semantics still need work. |
| Android TV conformance | 3 | Native Android Views, D-pad paths, Back/OK handling and focus outlines. Complex dialogs and some action navigation remain cumbersome on TV. |
| Adaptivity | 2 | 960×540dp normalization and measured bounds exist, but 720p, overscan, large-font and multiple-device claims are not fully validated. |
| **Total** | **12/20** | **Acceptable foundation; key release QA incomplete.** |

**Platform verdict:** Native Android TV design, not a web-style port. No HTML/web detector was run. The Impeccable Android reference is general mobile/tablet guidance; TV-specific D-pad/focus and ten-foot readability take precedence over mobile touch-target or bottom-bar assumptions.

## Persona walkthroughs

- **Jordan, first-time TV owner:** Opens with no cameras, selects Get Started, then Add Stream. A long technical form demands timezone/latitude/attribution alongside URL. Risk: abandonment before first playback. Success test: a person who only has one working HLS link can add it and play within a few remote steps.
- **Sam, low-vision remote user:** Reads small gray caps and source/weather metrics against scenic photos, navigates settings at 130% font size. Risk: tiny labels, uncertain focus and clipping. Success test: full TalkBack and D-pad run at both target resolutions with long text.
- **Riley, stress tester:** Opens 114 saved cameras, filters by country, changes source rapidly and goes Back from preview. Risk: extra pagination, stale availability status, mismatched map marker, or lost focus. Success test: large library and network-loss matrix with focus and source identity preserved.

## Directed QA before implementation claims

1. **Visual capture matrix:** Home, all seven Settings categories, Cameras browser/empty state, first-run, source editor with IME visible, details, map, full-screen Live over sun/sky/snow/night, and playback menu. Capture at 1280×720 and 1920×1080, EN/PT-BR, fontScale 1.0/1.3.
2. **Navigation matrix:** record each D-pad/OK/Back transition and expected focus target; test selection retention after dialogs, editing, switching category, returning from full-screen, connection failure and SAF cancellation.
3. **Legibility:** test whole-group overlays at maximum/minimum selected opacity in bright/dark camera scenes; inspect moving position phases and long labels without cropping, and measure contrast against actual captured frames.
4. **Empty/slow/error states:** zero cameras/videos, unsupported URL, duplicate, offline preview, unavailable geocoding, expired SAF grant, USB unplug, unresponsive stream; verify inline feedback and persistent user data.
5. **Performance:** startup time, library scroll with 100+ sources, decoder transitions from preview to playback, memory, dropped frames, animations and 24-hour soak on Hisense. Never infer success from Kotlin tests alone.
6. **Review privacy:** source editor's `Find location` query currently falls back from blank city to the source **title** ([LibraryScreens.kt L244](../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt#L244)); verify the privacy copy accurately explains what can be transmitted and avoid sending private arbitrary titles without a clear user action.

## Recommended sequence

1. **P1 `/impeccable polish`**: improve overlay contrast with real moving-camera evidence.
2. **P1 `/impeccable onboard` + `distill`**: simplify first camera creation, test zero-content journey.
3. **P1 `/impeccable audit` + `adapt`**: TV focus, font scale, 720p and TalkBack matrix.
4. **P2 `/impeccable distill` + `clarify`**: Appearance hierarchy, player menu, filters and icons.
5. **`/impeccable polish` then rerun native `critique`/`audit`** after on-device fixes and compare the two provisional scores.

**Questions skipped:** the user requested immediate critique of the existing approved design; this review does not propose a new design direction or require a fresh owner decision before documenting defects.

## Sources and references

- [Impeccable](https://github.com/pbakaus/impeccable), [Android TV D-pad navigation](https://developer.android.com/training/tv/get-started/navigation), [Android TV focus system](https://developer.android.com/design/ui/tv/guides/styles/focus-system), [TV legibility](https://developer.android.com/design/ui/tv/guides/styles/typography), [TV accessibility](https://developer.android.com/training/tv/accessibility).
- [OpenVista engineering status](../STATUS.md), [approved decisions](../product/decisions.md), [user-managed content](../product/user-managed-content.md), [native design system](../design/tv-design-system.md), and [real-device review issue #54](https://github.com/murilaiooo/openvista/issues/54).
