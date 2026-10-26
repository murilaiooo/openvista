# Codex implementation handoff

**Historical implementation handoff recorded before independent QA, retained for provenance.** The code has since compiled, passed local tests and received partial Hisense TV verification, and the NWS adapter was added separately. The former draft PRs were closed administratively; see [current status](../STATUS.md), [TV QA](../testing/production-pr22-validation-2026-10-26.md) and [NWS QA](../testing/nws-weather-integration-2026-10-26.md). The original untested-handoff details below describe what was true **at that time**, not today.

## Implemented source and requirement coverage

| Area | Source behavior | Requirements / related issues |
| --- | --- | --- |
| TV application | Three illustrated Home cards, skippable setup, Cameras' three entry paths, source details/editor, metadata filters, separate Favorites sections, navigation history/focus restoration, pagination, localized EN/PT-BR Basic/Advanced settings | C-01; D-09–D-10, D-18–D-21, D-26, D-28, D-33–D-34, D-59, D-63; #13, #3 |
| User library and local media | Versioned atomic TV-only persistence, typed camera/video playlists, source and playlist CRUD/order/duplicate/batch item selection, atomic M3U-to-library-and-playlist import, multi-folder/direct-video SAF selection, persistent or explicitly session-only grants, legacy grant migration, recursive bounded/cycle-safe scan, revocation and unavailable records | C-01; D-02–D-03, D-13–D-14, D-29–D-31 as amended, D-47, D-50–D-52, D-64, D-67, D-74; #3, #9 |
| Playback | Fullscreen crop/fit, initial mute and audio-track selection, quality cap/decoder fallback, five-minute rotation configurable 1–60, camera shuffle default or sequence, local advance/loop, fixed ±10-second seeking, resume checkpoints, bounded skip/exhaustion/retry, configured local fallback, deferred live recovery, Stay local, five-second contextual controls and Cameras/Videos switching | D-06, D-11–D-12, D-16–D-17, D-21, D-27, D-38, D-42, D-44–D-46, D-49, D-53–D-58, D-60–D-62, D-65, D-68, D-72; #5 |
| Ambient presentation | Clock/date/place/weather visibility, presets and visual preview/editor, position/size/opacity/color/time/unit settings, independent opt-in reposition/hide; current/seven-day weather and geocoder seams, attribution/cache/offline/missing states; clustered user-coordinate grid and sidebar with licensed-map adapter/cache seam | D-04–D-05, D-15, D-22–D-25, D-28, D-35–D-36; #6 (provider-dependent portions remain blocked) |
| System integration and privacy | Optional shared-player DreamService, last scene, contextual remote input, daily/overnight schedule, app-local keep-awake request, honest OS-owned interval guidance, no boot launch, sanitized local error journal and preview/copy/share | D-01, D-39–D-43, D-46, D-48, D-66–D-67, D-78; #2 |
| Updater and phone entry | Production discovery/download/hash/package/version/signer/installer path; optional foreground automation with Android confirmation; short-lived subnet-bound pairing protocol/phone page/QR/TV approval; both activation gates disabled | D-32, D-73, D-75; #7, #8 |

D-07/D-08 and D-69–D-71/D-76–D-77 remain distribution/acceptance constraints, with C-01 superseding supplied-camera assumptions. D-37 remains deferred. No Golden Hour/daylight experiment, wallpaper library, procedural demo, content catalog or video fixture was added. None of the rows above is a test result.

## Working branch and dependencies

- `codex/complete-tv-implementation`, initially based on PR #21 commit `af4c3ce`.
- Integration draft PR: [#22](https://github.com/murilaiooo/openvista/pull/22), targeting `main` and intentionally awaiting independent validation.
- Production commits: `8664bed` (library/SAF), `5c1ac95` (gated integrations), `ad6496e` (shared playback/ambient), `fca2352` (TV shell and production integration).
- PR #20: https://github.com/murilaiooo/openvista/pull/20 (DreamService registration).
- PR #21: https://github.com/murilaiooo/openvista/pull/21 (single persisted SAF grant).

The integration branch includes #21's existing commit and extends its storage behavior. It reuses #20's documented registration/entry approach in the completed shared-player Dream implementation without changing #20's branch. The integration PR targets `main`; reviewers must reconcile overlapping #20/#21 code before any eventual merge and must not independently merge overlapping changes blindly. Earlier test/device results from those PRs do not validate this head.

No issue state was changed. GitHub #3 and #9 were already closed when inspected; their historical issue state is not evidence for this implementation. The separate #3/#5/#9 acceptance records and #20/#21 physical gates still need independent review. Any tests visible in the integration diff against main were inherited from `af4c3ce`; this mission did not create, edit or run test suites.

## Modified modules and integration points

See [production-module-guide.md](production-module-guide.md) for ownership and [ADR 0002](../adr/0002-shared-local-state-and-decoder-ownership.md) for shared state/decoder decisions. The launcher is `OpenVistaActivity`; the package remains `com.openvista.prototype`, min SDK 28, compile/target SDK 36, version code 2 / `0.2.0-implementation-preview`. `LibraryStore.get(context)` must remain process-shared. `PlaybackSession` and bounded `PlaybackSnapshot` own queue/checkpoint/fallback restoration. App mode and Dream mode cannot retain separate active decoder owners.

The manifest adds only supported optional Dream registration and `REQUEST_INSTALL_PACKAGES`/a non-exported installer receiver to the existing Internet permission. No broad storage/location/boot/privileged permission was added. ZXing Core 3.5.3 is the only new production dependency, for local QR rendering. The diagnostic module and existing source parser remain separate.

## Validation restrictions

No tests, test edits, Gradle, compilation, lint, APK packaging, verification scripts, CI runs, emulator, ADB, TV interaction, releases, or merges were performed for this mission. Only manual source inspection, official API references and Git housekeeping informed implementation. Git hooks were disabled for commits/pushes to prevent accidental validation. Main, existing PR branches, research fixtures, untracked VLC scripts and user media were left intact.

## External decisions and acceptance gates

- **#6 provider approval:** no weather/geocoding/map adapter or endpoint is selected. Approve terms, quota, privacy, attribution and cache policy, then implement the actual selected adapters through the existing interfaces. Until then, current weather/forecast/city search/device-assisted location and a licensed map background remain unavailable; user coordinates and the explicit schematic/list remain usable as source code. The interfaces/cache/UI are implemented, external data delivery is not.
- **#8 security review:** `PairingSecurity.REVIEWED=false`; no listener can start. Resolve plain-HTTP confidentiality and record independent threat-model review before enabling this transport. Do not treat the implemented token/origin/subnet protections as security acceptance. See [the threat model](../security/lan-pairing-threat-model.md).
- **#7 channel approval:** `UpdateChannel.APPROVED=false`; update discovery cannot fetch a release. Approve the proposed asset/tag/package/signing contract before enabling it. Signer rotation is rejected until a policy is reviewed. System installation confirmation/entitlement/OEM behavior still needs physical proof. See [update-channel.md](update-channel.md).
- **Physical and release gates:** #2/#5/#9 acceptance remains separate. Stable-v1, signed-content guard, source/build reviews, privacy and 24-hour stability gates remain outstanding. Other devices are unverified. No public binary distribution is authorized.

These are the remaining implementation-dependent external gates. No independent production handler was deliberately left empty; no fake provider, media fixture or success state replaces these gates.

## Required ChatGPT validation and compatibility risks

1. Compile the exact head first, then adapt/add tests independently. Inspect API-28 and API-36 behavior, Media3 interfaces/resources, manifest, dependency notices and existing prototype test assumptions. Compilation has never been attempted for this head.
2. Validate cold first launch, skipped/revisited setup, TV keyboard, all Back/focus paths, page boundaries, EN/PT-BR long text, language recreation, filter/status freshness, duplicate edit/import, full library/playlist transaction limits, Favorites and arbitrary item/playlist reordering.
3. Independently validate #3 source/parser/import and signed-APK zero-content inventory; retain distinct #5 playback and #9 SAF evidence. Exercise interrupted M3U reads, absent picker, process death while a picker result is pending, legacy grants, multiple folders/direct documents, temporary grants, revoked access, unplug/replug and capped/inaccessible nested scans.
4. Validate HLS/DASH and MP4/MKV/WebM/MOV on the actual Hisense build, audio selection/mute, disabled subtitle rendering, quality caps, crop/fit, ±10 seconds, checkpoints, one-item/empty/all-bad playlists, decoder/HDR failures, rapid switching and camera stall timeouts.
5. Exercise configured-only fallback, long outage/recovery over more than six cameras, connection flapping, Stay local, camera first-frame acceptance at completed-video boundaries, failed probes and background/process recreation while local fallback or a recovery attempt is active. One-decoder recovery intentionally permits a connection gap; it does not promise seamless transitions.
6. Validate app/Dream ownership handoff, screen off, pause/resume keep-awake flags, OS Back/Home/wake behavior, Dream registration/selection, optional daily/overnight schedules and unavailable OEM screensaver settings. Preserve the current launcher/apps/settings; any device-wide change needs its separate consent/rollback.
7. Exercise overlay bounds, opacity/contrast/overscan, independent motion/hide and five-second controls; test diagnostics disappearance, dialog dismissal on stop and report redaction. Journal retention is 64 events/seven days. Do not infer a burn-in guarantee.
8. After provider approval, implement/review adapters and validate missing/invalid forecast fields, location consent, units/time zones, seven-day detail, current conditions, attribution, refresh budgets, stale/expired caches and cancellation. Map cache is one bounded viewport, never bulk offline tiles.
9. Independently review pairing framing, origin/Host/token/replay, input/time/connection bounds, interface ambiguity, expiry/approval races and background shutdown before any gate change. Review updater redirects/hash/signature/version/package/cancellation/storage limits, unknown-app entitlement, confirmation denial, interrupted install and process recreation before channel activation.
10. Run the documented real-device soak/privacy/release-content gates and record quantitative budgets before acceptance. No CPU, RAM, thermal, codec, frame-drop or other-TV support claim is established by this source implementation.
