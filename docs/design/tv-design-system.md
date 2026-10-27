# OpenVista TV design system

Implementation for [Issue #36](https://github.com/murilaiooo/openvista/issues/36), October 27, 2026. Native Android Views, minimum API 28. This is implementation documentation, not a claim of tested compatibility or stable-release readiness.

## Visual contract

Home uses three equal, full-height scenic destinations. Settings uses a dark decorative atlas, a left category rail and translucent groups. The ambient player gives the scene most of the display, with identity at upper left, source information at upper right, viewer information at lower left and forecast below. Shared typography, line icons, quiet surfaces and cyan remote focus connect these screens.

The original mockup PNGs are not available in the issue or tracked assets. The written issue is the implemented reference. Original files, before/after captures and pixel comparison remain pending; no images in this directory are presented as captured app output.

## Tokens

`player/src/main/java/com/openvista/prototype/ui/TvDesign.kt` is the central token source.

| Token | Value / use |
| --- | --- |
| Background | `#07121F`, midnight navy |
| Surface | `#16283A` at 85% alpha |
| Raised surface | `#203548` at 90% alpha |
| Primary text | `#F3F6F8` |
| Secondary text | `#B9C8D5` |
| Focus / accent | `#91DFEA` |
| Dividers | `#4F7891` at 20% alpha |
| Typography | Android `sans-serif`, `sans-serif-medium`, `sans-serif-light`; no bundled font |
| Page title / body / label | 32sp / 17sp / 13sp |
| Home destination title | 28–40sp adaptive; number/eyebrow 12sp |
| Ambient source clock | 44sp at overlay-size 20; viewer clock scaled to 82% |
| Spacing rhythm | 4, 8, 12, 16, 24, 32, 40dp |
| Standard safe padding | 40dp horizontally, 28dp vertically |
| Ambient safe area | At least 5% or 32dp horizontal / 22dp vertical, plus reserved movement inset |
| Row / setting row | Minimum 56dp / 64dp; multiline settings grow naturally |
| Panel radius / border | 8dp / 1dp |
| Focus stroke | 2dp cyan, no permanent bright outline on unfocused items |
| Navigation icons | Original 24-unit line drawings, rendered at 22dp, 1.5-unit stroke |
| Optional information fade | 200ms on inactivity hide / return |
| Position movement | Discrete change every 1–30 minutes; no continuous animation |

`TvDesign.control` defines disabled, pressed, focused and selected backgrounds. Selected Settings categories retain a quiet tinted panel; actual remote focus gets the strong cyan outline. Controls keep light text in every focus state. UI panels use precomputed gradients and native drawables; there is no realtime blur or video transition added by this redesign.

## Layout and remote behavior

The comparison target is 1920 × 1080 with 1280 × 720 as the second required review size. Layout code uses measured bounds and dp/sp rather than assuming a pixel density. Scenery uses center crops without aspect distortion. Views support larger resolutions through density-independent measurements.

Home allocates exactly one third of the display to each destination. Header and footer remain separate from destination text. Left/right changes destinations; Up reaches Settings; Down reaches Resume when saved media exists. OK opens the destination. The selected destination is retained in navigation state and the Activity state bundle. Returning through Back restores the previous route's focus; the explicit Home action clears history.

Settings has seven categories: Playback, Screensaver, Weather & Clock, My Library, Appearance, Advanced, About. The 200dp rail and content scroll independently. Right enters settings, Left returns to the active category, and vertical navigation remains explicit. Header actions and feedback use measured height. A row has one focus target, including its toggle/value. Disabled provider, pairing and update controls state why they are unavailable.

Ambient information never receives D-pad focus. Contextual player controls keep their existing five-second timeout and media-specific arrow actions. When controls open, viewer and forecast information clear their space; controls never move with display care. OK/Back and existing camera switching/local-video seeking remain owned by the player entry point.

Large optional overlay content is measured at natural height before fitting the complete group into its allocation. Text, icons and scrim scale together if needed, rather than clipping a forecast or provider-credit row. This fitting and Home/Settings font scaling still require visual accessibility review at both comparison sizes.

## Components and state ownership

| Component | Responsibility |
| --- | --- |
| `TvDesign` | Colors, fonts, spacing, surfaces, gradients, focus state drawables |
| `TvUi` | Buttons, field labels, scroll containers, setting rows, toggle rows, announcements |
| `TvIcon` | Shared original line icons for category navigation and Settings |
| `HomeScreen` | Scenic destinations, real counts, source-empty copy, focus memory, one background artwork decode |
| `SettingsShell` | Atlas backdrop, measured header/feedback, rail, content region and directional navigation |
| `SettingsScreens` | Real persisted settings, dialogs, Simple/Advanced overlay editor and 16:9 preview |
| `AmbientOverlay.LocationGroup` | A real location's clock/date/current weather/metrics and optional attribution |
| `AmbientOverlay.ForecastCell` | Provider day, condition icon, high/low and precipitation probability |
| `AmbientPresentation` | Locale/unit/timezone formatting and filtering actual forecast dates |
| `WeatherSymbol` | Original condition symbols, neutral dash when the condition is unknown |
| `PlayerScreen` / `OpenVistaDreamService` | Existing playback/input ownership plus actual Media3 state and menu visibility binding |

Library screens, onboarding, source editing, playlists and dialogs retain their existing routes/actions and use the shared controls. User sources, folder grants and media checkpoints are not replaced. The redesign adds no media catalog, playback engine, permission or dependency.

## Weather and source truth

Source city/country comes only from saved source metadata. An actual source title can be shown as a title; it is never interpreted as a city. No flag is fabricated from free-text country names. Source time uses a valid saved timezone or a valid timezone returned for those same coordinates by the official weather provider. If neither exists, the source clock/date is hidden. Viewer time uses the configured timezone, falling back to the TV timezone only in the viewer/device-time group. A clock-only preset can display explicitly labeled device time before a viewer location is configured.

Source and viewer current weather remain independent. Matching coordinate requests share a slot; changing a source title does not cancel an in-flight request for the same place. Refreshes normally use the existing 30-minute provider budget; failed requests may retry after five minutes. Requests stop with their owning screen. Forecasts filter actual provider dates and retain 3–7-day selection; the on-demand seven-day dialog remains available.

The forecast context preference selects source coordinates when available, otherwise configured viewer coordinates. Its title identifies the location only if the global Location switch allows that. This fallback never substitutes viewer weather or timezone into the source zone. Missing current conditions, coordinates, daily periods, provider approval or network data produce unavailable/loading/location guidance. Stale results are labeled. Missing fields remain dashes or disappear; no Positano/Tucson values or forecasts are embedded.

The NWS adapter keeps provider-supplied daily condition text and optional point timezone. Existing optional Open-Meteo code now retains daily condition codes, as described in its [official forecast API](https://open-meteo.com/en/docs). Its approval gate stays disabled. No new provider is enabled. Weather is forecast information, not a claim of station observation. Actual player state is read through the existing [Media3 event interface](https://developer.android.com/media/media3/exoplayer/listening-to-player-events); a camera URL or HLS format alone does not warrant the LIVE label.

## Display care and persistence

Movement and inactivity hiding remain separately switchable and persisted. Movement visits nine bounded offsets using a single timer, with up to 12dp of reserved travel in each direction. It moves the entire information composition, including logos, background gradients, dividers and symbols. Hiding uses the configured inactivity delay; any ordinary player interaction restores information. Neither changes the system screensaver, power configuration or media position.

The cinematic arrangement constrains advanced X/Y offsets to the reserved composition inset because its full-width forecast has no spare horizontal travel. Compact mode preserves percentage positioning within measured non-overlapping source/viewer lanes. Corner selection applies to the source anchor in compact mode; cinematic mode uses it to mirror the reference composition. These are explicit adaptations from the former single-box editor to four information zones.

New preferences use optional JSON fields. Existing four visibility switches, opacity/color/size, clock/units, movement, hiding and delay are read unchanged. Saved legacy None remains empty and makes no forecast requests. When the new layout and zone keys are absent in older saved data, enabled overlays adopt the approved Cinematic composition, source information and restrained brand; explicitly saved zone choices remain intact. New installations also default to Cinematic. Per-zone opacity is 20–100%; visibility switches turn zones off. Existing source libraries and explicitly saved user settings are never reset.

Dream sessions start muted even if ordinary app playback was previously unmuted. An explicit Dream sound action changes only that session; the app's saved sound choice is retained. Screensaver selection and device settings are not changed by this implementation. Display care reduces static exposure and cannot guarantee prevention of panel burn-in.

## Assets and licensing

- [Home scenery provenance and complete generation prompt](home-assets.md): one original, text-generated photographic triptych. It is decorative UI artwork, visibly labeled illustrative, with no available camera associated with it. One sampled bitmap is shared across three crop rectangles; no cropped copies or image-loading library. Existing vector art remains the decode fallback.
- `SettingsAtlasDrawable` contains original coarse continent and grid geometry. It is a static decorative schematic under this repository's source license, not a geographic dataset, tile service or interactive map.
- `TvIcon`, `WeatherSymbol`, focus outlines and toggles are original native vector/code artwork. No external icon font or icon package was added.
- Typography uses the installed Android system fonts. Existing application icon/banner assets are unchanged.
- No stream, playlist, video, personal location, API key or source URL was added as an application asset. Package-content review must still inspect the independently built artifact.

## Independent acceptance

See [the independent review runbook](issue-36-validation.md). Compilation, tests, lint, runtime screenshots, remote behavior, visual fidelity, CPU/GPU/RAM measurements, screensaver lifecycle and extended playback remain unverified for this branch. No build, test, lint, emulator, ADB, device operation or automated validation was executed during implementation.
