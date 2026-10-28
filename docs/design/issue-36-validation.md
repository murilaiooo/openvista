# Independent review of Issue #36

The implementation session executed **no tests, builds, Gradle tasks, lint, emulators, ADB, device operations or automated validation**. Source was read by the implementers and peer reviewers. This document is a handoff for an independent reviewer; the commands below were not run for this change.

## Branch and inputs

Review `codex/ui-redesign-36` against `codex/remaining-development` at `24249bc`. This base preserves the development checkout used for the task, including its existing provider and player work. It is a stacked development PR; mainline integration must also retain the separate verified SAF and DreamService fixes already on `main`. Do not merge this branch without independent review.

Read [the approved issue](https://github.com/murilaiooo/openvista/issues/36), [design system](tv-design-system.md), [implementation handoff](issue-36-implementation.md), [asset provenance](home-assets.md), [canonical status](../STATUS.md), and [release gates](../product/v1-acceptance.md). Recover the owner's original three PNGs and put them in `docs/design/` before claiming visual comparison. They were not attached to the issue and were unavailable during this session.

## Desktop checks for the independent reviewer

Use the repository's documented Windows toolchain. Preserve the existing library and the original checkout's untracked VLC scripts. In a separate authorized review session, compile the branch and run the appropriate checks, for example:

```powershell
.\gradlew.bat :player:testDebugUnitTest :source-core:test :diagnostics:testDebugUnitTest :player:assembleDebug :player:lintDebug --offline --no-daemon
```

If dependencies are not cached, follow the existing toolchain guidance rather than treating an offline dependency failure as an app result. Complete the [package-content guard](../testing/release-content-guard.md) on the actual produced APK. The UI triptych is decorative artwork, never playback media. Public APK publication still requires owner approval.

Inspect optional JSON preference round trips: saved legacy None/minimal/weather presets, absent new keys, independent movement/hide flags, per-zone opacity, 3–7 days, custom layout and old X/Y values. Confirm no source/folder/checkpoint loss and no enabled pairing/public-update gate. Add focused logic/UI coverage where useful during the independent review; this implementation adds no claim of executed regression coverage.

## Screenshot and remote matrix

Record 1920 × 1080 and 1280 × 720 captures, with exact density/font scale, English/PT-BR, long names, light/dark scenes and overscan margins. Compare each actual capture with the corresponding original mockup. Capture focused/unfocused Home, Settings rail/row/disabled/loading/error states, cinematic and compact information, and all-empty/no-weather states. Do not use the generated scenery asset as a screenshot of the running app.

| Area | Independent behavior to inspect |
| --- | --- |
| Home | Equal full-height panels; zero and populated counts; no fake live feed; readable copy; outline follows actual focus; Settings and Resume reachable; panel restored after Back and Activity recreation |
| Settings | Seven rail categories; Left/Right and Up/Down without traps; measured header/feedback at larger fonts; scrolling; dialogs; actual saved values; error announcements; language recreation; unavailable provider/pairing/update explanations |
| Libraries | URL/M3U import, SAF folder/video actions, rescanning, favorites, source availability/details, playlists/order, last-item resume remain reachable |
| Clocks | Different saved source/viewer timezones, midnight/date boundaries, 12/24-hour preference, daylight-saving boundary, missing/invalid source zone, explicit device-time preset |
| Current weather | Supported NWS coordinates, out-of-coverage coordinates, missing current data, stale cache, no location, network failure, location switch during request |
| Forecast | 3, 4, 5, 6, 7 days; seven-day dialog; real high/low/precipitation; missing values; expired periods; unknown condition icon; named title hidden by Location switch |
| Request identity | Same coordinates in both zones; rapid source switches at the same coordinates; transition between shared and different coordinates; title/timezone edits; leave/re-enter screen during fetch |
| Display care | Movement on/hide off; movement off/hide on; both off/on; 1-minute interval; all nine offsets; each corner; X/Y extremes; opacity extremes; return after inactivity; no offscreen forecast, clipping or moving focus |
| Controls/playback | OK opens controls; five-second hide; Back dismiss/exit; D-pad camera switch vs local seek; pause/resume; true Media3 LIVE vs VOD; no geometry change to PlayerView; crop/fit unchanged |
| Audio | App unmuted preference survives; each new Dream session begins muted; Dream sound action is session-local; audio track selection and source switching still work |
| Lifecycle/performance | Background/resume, screen off, failed/corrupt media, missing USB, decoder fallback, failed camera recovery, long playback, bounded artwork memory, no retained workers or weather callbacks after teardown |

Physical Hisense 58U6HF / Fire OS 7 / Android API 28 validation remains necessary. Obtain the owner's specific authorization before installing, using ADB or changing any TV-wide setting. The existing system screensaver must stay disabled until the owner changes that instruction. No launcher, unrelated apps or OS preference should change as a side effect. Preserve the documented 24-hour stability gate and do not infer compatibility with other TVs.

## Evidence and remaining limitations

- No new runtime before/after screenshots, compilation result, passing test result or hardware measurement exists from this session.
- Original mockup PNGs are missing; visual implementation follows the written specification. Exact photographic/layout matching is unverified.
- Global city search/weather and licensed map-provider gates remain as before. Unavailable data is not replaced with invented weather. The Settings atlas is decorative geometry.
- Cinematic editor offsets are intentionally bounded to a small full-composition inset; Compact uses safe lanes for percentage positioning. Review this adaptation with the original mockups.
- Large ambient text is fitted as a complete optional group. Judge readability and provider attribution at the largest supported user/system sizes before approval.
- Source/build/package review, screenshots at both sizes, physical remote/lifecycle tests and extended playback must be recorded independently before acceptance. Issue #36 remains open until those gates are satisfied.
