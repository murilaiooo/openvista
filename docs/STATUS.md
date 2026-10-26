# OpenVista: current engineering status

**Last reviewed: 2026-10-26.** This document is the single canonical status page for the repository. Other documents may preserve dated planning and implementation history.

## At a glance

| Question | Answer |
| --- | --- |
| Is there Android app source? | **Yes.** Native Kotlin player, UI, user library, SAF access, Media3, ambient overlays and tests are in the mainline development source. |
| Can it run on a TV? | **Yes, as a development preview.** Individual app flows were exercised on the owner's Hisense 58U6HF / Fire OS 7 (Android 9 / API 28). |
| Is there a public APK or stable release? | **No.** Only locally built debug/test APKs; no signed public release or owner authorization to publish. |
| Does OpenVista bundle live-camera links, M3U lists or videos? | **No.** Users import their own. Research-only media/playlist fixtures are excluded from runtime artifacts. |
| Does weather work? | **Yes, for NWS-covered U.S. coordinates.** Tucson hourly conditions and seven-day forecast were exercised physically. Worldwide geocoding is not implemented. |
| Are all original v1 requirements finished? | **No.** Hisense automatic DreamService entry was verified, but full dream controls/lifecycle, physical USB, 24-hour soak, maps, pairing security, updater and final release gates remain. |
| Are there open GitHub issues/PRs? | **Yes.** The QA development candidate and later focused changes are tracked separately; administrative closure of older records did not certify v1. |

## Evidence actually collected

| Area | Observed result | Evidence |
| --- | --- | --- |
| Diagnostics and device | Hisense 58U6HF, Fire OS 7, Android API 28; local diagnostic APK and device baseline | [Device baseline](testing/hisense-baseline.md) and [diagnostic runbook](testing/diagnostic-apk-runbook.md) |
| App shell | Android TV Home, basic remote focus and navigation, zero-source workflow, manual source entry | [Integrated-app QA](testing/production-pr22-validation-2026-10-26.md) and owner-authorized 2026-10-26 test session |
| Media import | User-selected M3U playlist imported; later 38 user-selected live-camera entries added with 0 rejected/duplicates; app contains no built-in source URLs | [User-managed contract](product/user-managed-content.md), [APK guard](testing/release-content-guard.md) |
| Streaming | HLS 720p and DASH 720p played on Hisense using independently served test media | [Media spike](testing/player-prototype-spike.md) and owner-authorized 2026-10-26 test session |
| Local media | An individually selected MP4 played; an authorized folder was scanned with two videos and was present after process restart | [SAF work](testing/saf-usb-spike.md) and owner-authorized 2026-10-26 test session |
| Weather | Official NWS seven-day Tucson forecast displayed; overlay displayed Tucson temperature + condition while HLS fixture played | [NWS integration](testing/nws-weather-integration-2026-10-26.md) |
| Localization | English source resources and Brazilian Portuguese TV interface, including settings and forecast, exercised | [Integrated-app QA](testing/production-pr22-validation-2026-10-26.md) |
| Local verification | Android unit tests, source-core tests, diagnostics tests, build and Android Lint passed; 23 Python tests; no-bundled-content guard passed on debug APK | [Integrated QA](testing/production-pr22-validation-2026-10-26.md), [NWS QA](testing/nws-weather-integration-2026-10-26.md) |

These are **partial results**, not comprehensive acceptance for entire feature families. A local file or research-list import does not establish rights to redistribute a third-party stream. A declared 1080p/4K resolution is not a measured decoder result.

### Independent unmerged QA candidate (2026-10-26)

A separate review snapshot of the Codex remaining-development work passed local Kotlin/Android tests (23 cases), Gradle build, Lint and debug APK content scanning after one SAF cancellation correction and an updated grant-retention test. Its additional deduplication test also passed. On the physical Hisense, schema 1 -> 2 consolidated 44 stored sources into 43 (four videos into three), retaining all 40 cameras, favorites, playlist links and checkpoint references. PT-BR Home and four D-pad directions received partial checks. Controlled HLS/H.264 1080p and DASH/HEVC 4K fixtures later rendered on the Hisense; a falsely unavailable status after finite camera completion was corrected and retested. The independently verified SAF/storage subset was merged as [PR #25](https://github.com/murilaiooo/openvista/pull/25); **the remainder of the QA candidate is still unmerged and uncertified**. See [the independent QA evidence](testing/independent-codex-qa-2026-10-26.md), [the playback probe](testing/hisense-fhd-4k-playback-2026-10-26.md) and [owner-assisted hardware checks](testing/owner-assisted-acceptance-2026-10-26.md). USB, sustained 4K/HDR, full focus traversal, 24-hour soak, pairing, updates and DreamService remain gated.

### Smart ambient preview under review (2026-10-26)

The focused development branch adds separate camera and local-video rotation intervals (defaults one and five minutes) and independent Smart TV-style camera/time and weather panels. Each panel moves to another corner each minute by default; random placement is configurable. Hardware image proof and persistence checks are recorded in [smart ambient QA](testing/smart-ambient-rotation-2026-10-26.md). The owner's existing 40 camera entries have no saved city, time-zone or coordinate metadata. Accurate camera-local time and temperature require supplying those fields; Tucson viewer weather is labeled separately. These features are development QA, **not stable-v1 certification**.

## Known issues and unverified paths

1. **Local video deduplication:** the canonical tree/direct SAF identity correction was merged as PR #25 after local verification and a physical schema migration of the existing library. The real USB/provider revocation matrix is still pending.
2. **Physical USB:** file/folder selection and persistence work for tested internal media. Unplug/replug, revoked grants, real USB drive behavior and all supported containers/codecs have not passed the required matrix.
3. **High-resolution and long-duration playback:** single controlled HLS/H.264 1080p and DASH/HEVC 4K clips visibly rendered on the target TV. Sustained playback, HDR, decoder dropped-frame statistics, thermal/CPU/RAM trends and 24-hour uninterrupted rotation/soak are not signed off.
4. **DreamService:** the owner authorized enabling OpenVista on the Hisense; physical, non-test automatic entry and video/clock/weather were verified, with the three-minute system timeout preserved. Remote control inside the dream, wake/recovery and 24-hour stability still require acceptance. See [Hisense DreamService evidence](testing/hisense-dreamservice-activation-2026-10-26.md).
5. **Map/geocoding:** an app-local schematic and saved-coordinate list exist. Licensed map tiles and city-name geocoding are not integrated.
6. **Weather scope:** NWS forecasts cover supported U.S. coordinates only. Some provider-supplied condition text remains English in PT-BR. Stale/offline behavior was not subjected to extended on-device failure injection.
7. **LAN pairing:** code exists, but the security gate remains off. It must not expose a listener before confidentiality and local authorization review.
8. **Updates:** discovery/download/installer code exists, but the public update channel is disabled. Android 9 installation may require explicit system UI confirmation. Signing, release channel and installer hardware proof remain.
9. **Full app QA:** complex focus, accessibility, network recovery, crop/quality settings, settings persistence, playback switching, source failures and background/sleep behavior require additional validation.
10. **Distribution:** there is no signed user-facing release, no 24-hour acceptance and no compatibility certification for TVs other than the test Hisense.

## Open source and content policy

- The repository is currently **private**; the MIT license applies to its source, but its presence does not imply a published or downloadable application.
- No bundled stream URLs, playlists, wallpapers or videos in runtime packages (C-01).
- The user chooses authorized live URLs and local media; research assets are never a first-party media catalog.
- The official NWS API uses the required identifying HTTP User-Agent, and the app does not add an on-video provider-credit label. Weather is optional and viewer-coordinates-based, without background geolocation permission.
- The prototype retains application identity **com.openvista.prototype**, app version **0.2.0-implementation-preview** (version code 2), Android minimum API 28. This is **not** a stable distribution identity.
- Do not activate pairing/update gates or publish APKs without security/release approval.

## Repository history and closed work

PRs [#20](https://github.com/murilaiooo/openvista/pull/20), [#21](https://github.com/murilaiooo/openvista/pull/21), [#22](https://github.com/murilaiooo/openvista/pull/22), and [#23](https://github.com/murilaiooo/openvista/pull/23) were closed unmerged at the owner's request. The code was subsequently consolidated into the development mainline (not an owner-approved release). Their branches and dated comments remain for provenance, without implying each closed PR passed release QA.

Issues #2, #5–#8 and #13–#15 were also closed, in several cases as **not planned/administrative closure** rather than implemented and tested. Closed issue count is not a v1 quality metric.

## Next release-gate sequence

Follow [the live roadmap](roadmap.md), [device compatibility](testing/compatibility.md) and [the stable-v1 acceptance contract](product/v1-acceptance.md). Prioritize duplicate media identity, physical media/remote tests, security and upstream provider approval, fault recovery, measured 24-hour soak, and signing/channel/owner approval. **Do not turn an incomplete gate into a passing gate merely by closing its GitHub issue.**
