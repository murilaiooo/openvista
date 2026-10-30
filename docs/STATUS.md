# OpenVista: current engineering status

**Reviewed October 28, 2026; final source integration documented below.** This is the live status for the active Android TV development work. Read [the Claude release handoff](implementation/claude-release-handoff-2026-10-27.md) for the full history and [the owner-approved acceptance criteria](product/v1-acceptance.md). Historical PR bodies and dated plans can be stale.

## At a glance

| Question | Current answer |
| --- | --- |
| Is the Android TV application implemented? | **Yes, development preview.** Native Kotlin/Media3 with user-managed HLS/DASH/M3U, local videos/SAF, playlists, favorites, Home, a unified Settings design and ambient overlays. |
| Where is the integrated source? | The latest owner-TV-matched local overlay, PR #40, and the updated English/Portuguese READMEs are merged into `main` through PR #37. See [integration QA](testing/integrated-tv-snapshot-2026-10-28.md) and [PR #37](https://github.com/murilaiooo/openvista/pull/37). |
| Is this ready for a public release? | **No.** The source is integrated into `main`; historical stream URLs and other distribution gates still need review. |
| Does the TV match the integrated APK? | **Yes, byte for byte.** The installed October 27 debug APK and the current local build share the same SHA-256. This is not a 24-hour stability certification. See [QA record](testing/integrated-tv-snapshot-2026-10-28.md). |
| Is there a signed APK? | **Yes: v0.3.0-preview.1** is a GitHub **prerelease**, built and signed independently of the existing debug key. It is not stable and not a 24-hour soak certification. See [release notes](releases/v0.3.0-preview.1.md). |
| Does the APK contain a camera catalog, playlists or bundled videos? | **It must not.** Under owner decision C-01, all live URLs, M3Us and local videos are user-managed, not shipped. Run the actual final-artifact guard before any release. |
| Is weather global? | **Camera weather is now implemented outside the US** through Open-Meteo, while NWS remains preferred within its US coverage. A missing same-day high after sunset is supplemented only for that exact date. Location geocoding and all international failure/terms cases remain subject to review. |
| What is next? | Reference-accurate design audit; one coherent Settings menu; burn-in phase/device verification; QA/optimization; security/history/content audit; 24-hour hardware soak; explicit owner approval. |

## Observed and documented milestones

- Previous October 26 integration tests established HLS/DASH 720p playback, MP4 and user-selected folder flow, M3U import, navigation, scoped UI localization and Tucson NWS on the Hisense. See [integrated app QA](testing/production-pr22-validation-2026-10-26.md).
- Mainline [SAF canonical identity and migration](testing/saf-canonical-qa-2026-10-26.md) and [DreamService activation](testing/hisense-dreamservice-activation-2026-10-26.md) have separate evidence and are integrated into `main`. Do not infer USB grant and OEM behavior are universally validated.
- Owner-requested 3-panel Home, atlas/sidebar Settings, native styling, real source/viewer clocks and 3-7-day forecasts are now in `main`. References describe the intended visuals, **not a certified pixel match**.
- [Display care](design/ambient-display-care-2026-10-27.md): four bounded position arrangements for complete information groups, configurable movement default five minutes plus independent inactivity fading. Unit geometry/device spot-checks exist; timed cycles across varying layouts require further hardware checks. This reduces static exposure; it cannot guarantee burn-in prevention.
- [Worldwide weather](design/worldwide-weather-provider.md): NWS preferred where available; international Open-Meteo camera weather independently seen on physical hardware, subject to non-commercial and credit requirements.
- [Player freeze recovery](testing/live-video-freeze-regression-2026-10-27.md): a PixelCopy hold overlay caused stale/frozen moving footage and was removed by `d90c535`. Ten-second physical TV recording around a camera switch showed 21/21 changed frame pairs; short decoder gaps are still possible.
- [Missing current-day high](testing/missing-today-high-weather-qa-2026-10-27.md): after sunset, NWS high absence is supplemented using same-date Open-Meteo daily high while preserving original provider values and credit; focused tests and physical Tucson display passed.
- Three genuine 3840x2160 physical TV screenshots are in `assets/screenshots/` and linked from README. **These are not the three original owner design mockups**, which must be provided to the implementation agent separately.

## October 28 release-quality pass (branch `claude/project-thread-xcksji`)

Implemented in PR #40 and merged into `main` through PR #37. Evidence is labeled per item; nothing here is release acceptance.

- **Playback recovery** (`9490c31`): a live stream that falls behind its live window resyncs instead of being marked unavailable; a network outage no longer marks every camera unavailable (the session waits for connectivity, plays the fallback playlist when configured and resumes after the network settles); when all cameras fail while online, retries back off at 1, 2, 4, 8 and then 15 minutes. Notices are typed (quiet, brief, persistent) instead of string-matched. The stored *Scene transition: fade through black* setting is now implemented by a shared `SceneView` used by the player and the screensaver; persistent failures darken the surface so a frozen last frame is not left on screen. No PixelCopy or still-frame overlay was reintroduced. Library lookups are indexed. *Unit tests for the retry policy and notice mapping; device evidence is listed in PR notes.*
- **Home and unified Settings** (`58d90cb`, built and checked on the Hisense): a shared 960x540 dp design canvas, Inter (OFL-1.1, license in `licenses/`), one logo for Home, launcher icon and banner (owner decision 2026-10-28), one Settings menu that maps legacy routes.
- **Camera country, flag and location** (`521486f`, `5ef7a1e`): the root cause of missing countries was that M3U import dropped every `#EXTINF` attribute and cut titles at their last comma. The parser now keeps `group-title`/`#EXTGRP`, `tvg-country`, city, region time zone and paired coordinates. Countries resolve from ISO codes, names in common languages, aliases or the camera time zone, and are shown localized with their flag. Cameras that name a city but lack country, coordinates or time zone are completed from Open-Meteo geocoding (only the city and known country are sent, never stream URLs; unambiguous results only; blank fields only; rate-limited, bounded and remembered for a week). The source editor has *Find location*. The location label now defaults to on for new installs. *Parser, resolver and matching logic: 28 JVM tests passing. Android integration: see PR validation.*
- Cameras with no city at all (title and URL only, e.g. from the phone pairing page) still need *Find location* or a list that declares metadata; nothing is inferred from titles, because a wrong country or local time is worse than none.

## October 28 source synchronization and QA

The local live overlay was reconciled with PR #40 and the newest English/Portuguese README commits. The selected overlay has a 32-position display-care cycle (four compositions, eight drift offsets) with independent hiding; its earlier historical four-position description remains a baseline, not an exhaustive account of current behavior. The source editor height and camera header visibility fixes are included. All 32 physical timed phases still need real-device evidence.

The integrated debug source passed 51 player tests, 4 diagnostic tests, 2 source-core test suites, 23 Python tests, Android build and Lint, document checks and the static packaged-stream guard. The installed TV APK and the local integrated debug APK were SHA-256-identical. These facts do not establish public-release readiness. See [integration details](testing/integrated-tv-snapshot-2026-10-28.md).
## Release blockers and risks

1. Precisely compare Home, Settings and Live Camera Playback against all three original reference images. Reconcile typography/spacing/blur treatment, focus, correct labels/real data, all secondary screens and the **single** Settings navigation structure.
2. Prove every burn-in movement phase and persisted setting on the actual TV, over bright/dark scenes, long translations and missing data. Prevent clipping, overlap, static forecast footer, playback flicker and focus jumps.
3. Verify camera switching under weak networks, decoding stalls, app/Dream transitions, source and viewer forecast failures, daily high/low, library data preservation and lifecycle. Never mask live video with a static frame to conceal flashes.
4. Run end-to-end D-pad/focus/accessibility, user-library SAF duplicate identity, real USB unplug/replug, grants/revocation, media containers/codecs, formats, pause/resume and fallback test matrices.
5. Measure startup, memory/CPU, decoder errors, dropped frames and **at least 24-hour** unattended playback/recovery on physical hardware. No such acceptance has yet been certified.
6. Keep pairing, updater and experimental YouTube embed out of stable v1 until accepted. Official YouTube embeds cannot guarantee uninterrupted ad-free ambient playback.
7. Audit licenses/third-party source history/private URLs before changing GitHub visibility. Current-tree deletion **does not erase Git history**. As of October 30 the repository is **public**; [PR #51](https://github.com/murilaiooo/openvista/pull/51) (rebased #32) removes the historical stream research from the tree, but a history decision is still needed. Review it and the [content guard](testing/release-content-guard.md). Never commit the owner's untracked camera research and QA scratch artifacts.
8. Complete final signed artifact content/privacy/security checks and obtain owner approval for publishing, signing and release.

## Active issue/PR tracking

- [#36](https://github.com/murilaiooo/openvista/issues/36): unified TV design and precise mockup fidelity. Integration record [PR #37](https://github.com/murilaiooo/openvista/pull/37).
- [#34](https://github.com/murilaiooo/openvista/issues/34): consistent, nonintrusive loading/error states. Implemented in [PR #52](https://github.com/murilaiooo/openvista/pull/52); audit in [loading states](design/loading-states.md).
- [#33](https://github.com/murilaiooo/openvista/issues/33): visual saved-camera selection preview. Implemented in [PR #52](https://github.com/murilaiooo/openvista/pull/52) and checked on the Hisense on October 30.
- Other open draft PRs are independent proposals or historical overlaps. Do not merge them blindly or claim a closed issue proves its requirements passed.

## Engineering rules

- English-only repository code, docs, PRs and commit messages; EN/PT-BR app localization retained.
- The package remains `com.openvista.prototype`. The signed preview is version code 3, `0.3.0-preview.1`; this is **not** a final public product identity. Different source commits can produce the same version string, so compare artifacts by hash and Git state.
- Preserve user data, launcher and OS screensaver settings. Avoid bundled streams, fake location/media/weather data, unlicensed imagery, release-ready assumptions and unauthorized device modifications.
- Use applicable `.claude/skills` and `.agents/skills` instructions, local Gradle/unit/Lint plus independent physical hardware checks. No GitHub Actions required.
