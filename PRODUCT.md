# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

People using a television and its D-pad remote to watch ambient live cameras and local scenery videos, organize their own media, and occasionally adjust weather, clock, and playback behavior. First-time users may have an empty library. Returning users may have large personal camera lists and local folders.

## Product Purpose

OpenVista is a native TV media player, personal source organizer, and ambient viewing experience. Success means a user can add their own camera/playlist or local video, start playback, leave the scene running reliably, and find the next item with only a remote.

## Positioning

The user controls the sources. OpenVista does not supply a camera catalog, hosted playlists, downloadable media, an account, ads, or telemetry. It combines user-managed HLS/DASH streams and SAF-local videos with an optional ambient player, location-aware weather, independent source/viewer clocks, and display-care settings.

## Operating Context

- Native Kotlin/Android Views + Media3 on a television; minimum Android API 28.
- The documented physical test device is Hisense 58U6HF, Fire OS 7. Other TV models and stable release quality are unverified.
- Primary input is a D-pad, OK, and Back, viewed at typical couch distance. No mouse, touch, or account should be necessary.
- Playback often runs for hours. Readability on bright and dark video matters; overlays are secondary to the moving scenery.
- Screensaver/DreamService support depends on the OS. The application must not change device-wide screensaver, launcher, or sleep settings without the owner's consent.
- Users supply camera URLs/M3U lists and select local media/folders via Android's supported pickers. Locations, metadata, and provider data may be absent.

## Capabilities and Constraints

- Home destinations: Cameras, My Videos, Favorites.
- Single seven-category Settings navigation: Playback, Screensaver, Weather & Clock, My Library, Appearance, Advanced, About.
- Saved cameras, HLS/DASH/M3U import, favorites, playlists, local videos/SAF, playback recovery, preview, weather and configurable overlay/display care are implemented as development work, not certified across devices.
- Weather: NWS in its coverage area; Open-Meteo for supported international use with proper attribution. Source weather and viewer weather remain distinct. Never fabricate missing data or location.
- English and Brazilian Portuguese user-facing strings. Repository prose and issues are in English.
- Zero bundled streams/videos, no private URLs or credentials committed, no first-party camera catalog. C-01 overrides the historical catalog idea.
- Experimental pairing/updater/YouTube features remain gated. Ad-free, interruption-free YouTube live playback is not a validated screensaver source.
- Preserve the owner's approved visual references, data, OS configuration, and D-pad behaviors. Do not make unsupported compatibility, signing, safety, or performance claims.
- Public v1 requires actual signed-artifact checks, security/privacy/content review, navigation/accessibility QA, and a 24-hour physical-device soak.

## Brand Commitments

OpenVista uses its approved logo and a cinematic, restrained TV interface. The owner-approved Home/Settings/Live mockups are binding design references, but the original mockup PNGs are not tracked in this Git repository. The three committed README screenshots are captures of a working app, not those original mockups. Preserve the three-panel Home, atlas-style Settings, translucent overlays, and subtle D-pad focus language unless the owner requests redesign.

## Evidence on Hand

- [Current engineering status](docs/STATUS.md), [approved v1 acceptance](docs/product/v1-acceptance.md), [decisions D-01–D-78](docs/product/decisions.md), and [C-01 user-managed sources](docs/product/user-managed-content.md).
- [TV design system](docs/design/tv-design-system.md), [October 28 device integration QA](docs/testing/integrated-tv-snapshot-2026-10-28.md), and [October 30 real-device UX observations](https://github.com/murilaiooo/openvista/issues/54).
- Captured real app output: `assets/screenshots/home.webp`, `assets/screenshots/settings.webp`, `assets/screenshots/live-camera.webp`.
- Actual design tokens/components: `player/src/main/java/com/openvista/prototype/ui/TvDesign.kt`, `TvUi.kt`, `HomeScreen.kt`, `SettingsShell.kt`, `SettingsScreens.kt`, `LibraryScreens.kt`, and `AmbientOverlay.kt`.
- Gaps: original mockup source assets, reproducible large-font/720p comparisons, 32-phase physical display-care sweep, and broad device accessibility evidence.

## Product Principles

1. Put the scenery first; on-video UI remains secondary, legible, and optionally hideable.
2. A remote-only path from zero personal sources to first successful playback must be obvious and recoverable.
3. Preserve truthful states: no fabricated videos, availability, location, weather, or device compatibility.
4. Make settings reversible, focused, and predictable; advanced controls should not obscure essential tasks.
5. Favor reliability, privacy, low decoder cost, and user-owned media over decorative effects.

## Accessibility & Inclusion

All actionable controls must be navigable with a D-pad, have a conspicuous focused state, sensible reading order and TalkBack semantics, and accommodate EN/PT-BR text expansion. Validate text scaling, safe areas, 720p/1080p sizing, error states, and reduced-motion expectations on real TV hardware. Claims of compliance require measured evidence.
