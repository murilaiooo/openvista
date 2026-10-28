# OpenVista: current engineering status

**Reviewed October 27, 2026.** This is the live status for the active Android TV development work. Read [the Claude release handoff](implementation/claude-release-handoff-2026-10-27.md) for the full history and [the owner-approved acceptance criteria](product/v1-acceptance.md). Historical PR bodies and dated plans can be stale.

## At a glance

| Question | Current answer |
| --- | --- |
| Is the Android TV application implemented? | **Yes, development preview.** Native Kotlin/Media3 with user-managed HLS/DASH/M3U, local videos/SAF, playlists, favorites, Home, a unified Settings design and ambient overlays. |
| What branch contains the newest integrated code? | **`codex/ui-redesign-36`**: UI/redesign/weather/playback recovery changes plus the latest `main` SAF, DreamService evidence and README screenshot commits. The review remains [draft PR #37](https://github.com/murilaiooo/openvista/pull/37), not a certified release. |
| Is `main` itself the latest app? | **No.** It has separately merged verified SAF and Dream documentation updates and real screenshot assets, but does not yet contain the entire UI redesign. |
| Is the TV on the latest tested APK? | **Yes, relative to the previous October 27 debug APK.** On the owner's Hisense 58U6HF / Fire OS 7 / API 28, the installed APK and the earlier local build were pulled and SHA-256 matched byte for byte. **The newer integrated Git source has not thereby been installed or tested on-device.** |
| Is this a public, signed or stable release? | **No.** GitHub repository remains private; no approved production APK release or supported cross-device matrix. |
| Does the APK contain a camera catalog, playlists or bundled videos? | **It must not.** Under owner decision C-01, all live URLs, M3Us and local videos are user-managed, not shipped. Run the actual final-artifact guard before any release. |
| Is weather global? | **Camera weather is now implemented outside the US** through Open-Meteo, while NWS remains preferred within its US coverage. A missing same-day high after sunset is supplemented only for that exact date. Location geocoding and all international failure/terms cases remain subject to review. |
| What is next? | Reference-accurate design audit; one coherent Settings menu; burn-in phase/device verification; QA/optimization; security/history/content audit; 24-hour hardware soak; explicit owner approval. |

## Observed and documented milestones

- Previous October 26 integration tests established HLS/DASH 720p playback, MP4 and user-selected folder flow, M3U import, navigation, scoped UI localization and Tucson NWS on the Hisense. See [integrated app QA](testing/production-pr22-validation-2026-10-26.md).
- Mainline [SAF canonical identity and migration](testing/saf-canonical-qa-2026-10-26.md) and [DreamService activation](testing/hisense-dreamservice-activation-2026-10-26.md) have separate evidence and are integrated into the new review branch. Do not infer USB grant and OEM behavior are universally validated.
- Owner-requested 3-panel Home, atlas/sidebar Settings, native styling, real source/viewer clocks and 3-7-day forecasts exist on the active redesign branch. References describe the intended visuals, **not a certified pixel match**.
- [Display care](design/ambient-display-care-2026-10-27.md): four bounded position arrangements for complete information groups, configurable movement default five minutes plus independent inactivity fading. Unit geometry/device spot-checks exist; timed cycles across varying layouts require further hardware checks. This reduces static exposure; it cannot guarantee burn-in prevention.
- [Worldwide weather](design/worldwide-weather-provider.md): NWS preferred where available; international Open-Meteo camera weather independently seen on physical hardware, subject to non-commercial and credit requirements.
- [Player freeze recovery](testing/live-video-freeze-regression-2026-10-27.md): a PixelCopy hold overlay caused stale/frozen moving footage and was removed by `d90c535`. Ten-second physical TV recording around a camera switch showed 21/21 changed frame pairs; short decoder gaps are still possible.
- [Missing current-day high](testing/missing-today-high-weather-qa-2026-10-27.md): after sunset, NWS high absence is supplemented using same-date Open-Meteo daily high while preserving original provider values and credit; focused tests and physical Tucson display passed.
- Three genuine 3840x2160 physical TV screenshots are in `assets/screenshots/` and linked from README. **These are not the three original owner design mockups**, which must be provided to the implementation agent separately.

## Release blockers and risks

1. Precisely compare Home, Settings and Live Camera Playback against all three original reference images. Reconcile typography/spacing/blur treatment, focus, correct labels/real data, all secondary screens and the **single** Settings navigation structure.
2. Prove every burn-in movement phase and persisted setting on the actual TV, over bright/dark scenes, long translations and missing data. Prevent clipping, overlap, static forecast footer, playback flicker and focus jumps.
3. Verify camera switching under weak networks, decoding stalls, app/Dream transitions, source and viewer forecast failures, daily high/low, library data preservation and lifecycle. Never mask live video with a static frame to conceal flashes.
4. Run end-to-end D-pad/focus/accessibility, user-library SAF duplicate identity, real USB unplug/replug, grants/revocation, media containers/codecs, formats, pause/resume and fallback test matrices.
5. Measure startup, memory/CPU, decoder errors, dropped frames and **at least 24-hour** unattended playback/recovery on physical hardware. No such acceptance has yet been certified.
6. Keep pairing, updater and experimental YouTube embed out of stable v1 until accepted. Official YouTube embeds cannot guarantee uninterrupted ad-free ambient playback.
7. Audit licenses/third-party source history/private URLs before changing GitHub visibility. Current-tree deletion **does not erase Git history**. Review the unmerged repository-cleanup draft [#32](https://github.com/murilaiooo/openvista/pull/32) and the [content guard](testing/release-content-guard.md). Never commit the owner's untracked camera research and QA scratch artifacts.
8. Complete final signed artifact content/privacy/security checks and obtain owner approval for publishing, signing and release.

## Active issue/PR tracking

- [#36](https://github.com/murilaiooo/openvista/issues/36): unified TV design and precise mockup fidelity. Active draft [PR #37](https://github.com/murilaiooo/openvista/pull/37).
- [#34](https://github.com/murilaiooo/openvista/issues/34): consistent, nonintrusive loading/error states.
- [#33](https://github.com/murilaiooo/openvista/issues/33): visual saved-camera selection preview.
- Other open draft PRs are independent proposals or historical overlaps. Do not merge them blindly or claim a closed issue proves its requirements passed.

## Engineering rules

- English-only repository code, docs, PRs and commit messages; EN/PT-BR app localization retained.
- The package is still `com.openvista.prototype`, version code 2, `0.2.0-implementation-preview`; this is **not** a final public identity. Different source commits can produce the same version string, so compare artifacts by hash and Git state.
- Preserve user data, launcher and OS screensaver settings. Avoid bundled streams, fake location/media/weather data, unlicensed imagery, release-ready assumptions and unauthorized device modifications.
- Use applicable `.claude/skills` and `.agents/skills` instructions, local Gradle/unit/Lint plus independent physical hardware checks. No GitHub Actions required.
