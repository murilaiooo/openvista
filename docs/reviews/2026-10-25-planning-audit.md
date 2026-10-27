# OpenVista: comprehensive planning audit — 2026-10-25

**Historical planning audit:** the camera-catalog licensing blocker and 25-feed release threshold documented below have been **superseded by C-01**. The product is now a user-managed media player and does not distribute streams or media files. Original review findings remain for traceability.

**Scope:** repository `murilaiooo/openvista` at `5221c76` before this audit; eight-round owner decision log (D-01–D-78), 27 project skills, README, AGENTS, CONTRIBUTING, glossary, ADR-0001, research, architecture, P0–P3 plan, 10 v1 gates, GitHub issues #1–#8 and local validation. **No app source code/APK existed when reviewed. No TV state was changed.**

## Verdict

**Planning baseline: adequate for a P0 research sprint, NOT ready to promise the public v1 release, licensed webcam catalog, universal OEM screensaver compatibility or unattended APK installation.** 78 owner decisions are preserved. The audit improves traceability and source facts without marking hardware experiments complete.

## Findings by severity

| ID | Level | Finding and actual evidence | Required fix / status |
| --- | --- | --- | --- |
| A01 | **BLOCKER** | **25 lawful, reliable, family-safe native live cameras unproven.** [Windy free API](https://api.windy.com/webcams/pricing) describes image/link/timelapse use; [Windy terms](https://account.windy.com/agreements/windy-api-webcams-terms-of-use) regulate API keys and redistribution. [Skyline FAQ](https://www.skylinewebcams.com/en/support/faq.html) restricts live-feed embeds to webcam hosts. Available still images are not playable HLS/DASH. | Updated issue #3 and created a per-source evidence template (now archived outside the public source tree); the required 25 actual streams remain **unverified**. **OPEN** |
| A02 | **HIGH / OS** | D-75's fully automatic APK installation is not established for **Fire OS 7 / Android 9**. [`SessionParams.setRequireUserAction`](https://developer.android.com/reference/android/content/pm/PackageInstaller.SessionParams) only exists from **API 31**; Android may return [`STATUS_PENDING_USER_ACTION`](https://developer.android.com/reference/android/content/pm/PackageInstaller). | Updated issue #7 to require **API 28-compatible install evidence**, not API-31-only calls, with system confirmation fallback. Real test remains **OPEN** |
| A03 | **HIGH / INCONSISTENCY** | `AGENTS.md` and `CONTRIBUTING.md` still said product discovery. ADR-0001 demanded initial Google TV/Android TV test devices contrary to D-70's **Hisense-only first test**. | Correct steering and ADR without changing scope; completed in this audit. |
| A04 | **HIGH / LICENSING** | OpenVista's LICENSE is **MIT**. [AerialViews LICENSE](https://github.com/theothernt/AerialViews/blob/master/LICENSE) is **GPLv3**. A copied/forked codebase cannot simply be represented as MIT-only without satisfying GPL obligations. | Favor original implementation with permissible patterns/SDKs; revisit license strategy explicitly if copying upstream code. **OPEN before reuse** |
| A05 | **HIGH / TRACEABILITY** | Old validator checked 78 sequential IDs and relative links in **14 selected Markdown docs** but did **not** connect D-IDs to evidence/tests or all planning documents. The 10 high-level gates omit individual pass/fail coverage. | Added a D-01–D-78 **one-primary-gate trace matrix**; expand validator coverage. Individual runnable test cases are still **NOT TESTED**. |
| A06 | **HIGH / QUALITY** | 24h soak has no pre-declared numerical limits for dropped frames, buffer ratio, RAM growth, retries, thermal throttling, source uptime or acceptable degradation. | Created [24-hour soak protocol](../testing/hisense-24h-soak-protocol.md); the **numerical limits still require measured baseline** and must be frozen before the acceptance run. **PARTLY FIXED** |
| A07 | **HIGH / DEPENDENCY** | P0 proof paths are mostly prose, not a tracked blocking DAG; issue #2 combines DreamService, picker, USB and compatibility; **a standalone SAF/USB ticket was missing. | Created separate [SAF/USB issue #9](https://github.com/murilaiooo/openvista/issues/9); future P1 tasks must declare explicit P0 blockers. **PARTLY FIXED** |
| A08 | **MEDIUM / SCOPE** | All 78 requirements remain in public stable-v1 scope. The first engineering APK is local/incomplete, but explicit staged checkpoints and go/no-go criteria are thin. | Keep owner-approved v1 scope; use separate local prototype, functional P1 app, and stable v1 gates. Do not silently drop requested features. **OPEN for execution** |
| A09 | **MEDIUM / MAP** | OSM community tile servers [prohibit bulk offline tile downloading](https://operations.osmfoundation.org/policies/tiles/), require attribution/caching/user agent. Owner chose a **saved-camera list when offline**, not offline maps. | Confirm provider terms and distinguish locally cached metadata vs downloadable map tiles; issue #6. **OPEN** |
| A10 | **MEDIUM / WEATHER** | [Open-Meteo free terms](https://open-meteo.com/en/terms) require noncommercial use/attribution and cap request volume; free app status alone is not a quota guarantee. | Write license, geocoder and forecast variable cost/usage budget, data attribution & caching evidence before integration. **OPEN** |
| A11 | **MEDIUM / INPUT** | D-43 OK/Back/arrows, D-60 quick switching and D-65 five-second hide can conflict with focus and idle in Dream mode. [DreamService](https://developer.android.com/reference/android/service/dreams/DreamService) requires interactive mode for input; OEM may intercept keys. | Create real remote-input state chart, test focused menu and playback mode on Hisense, ensure accessibility/timeout resets. **OPEN** |
| A12 | **MEDIUM / PERSISTENCE** | Local-only settings checkpoint and recursive SAF folder scan lack transaction/file identity/index limits. USB or provider can revoke folders while decoder has the file open. | Plan versioned local schema, migration tests, bounded enumeration, permission revocation and absent-media recovery. **OPEN** |
| A13 | **MEDIUM / LAN SECURITY** | QR pairing is same LAN with TV approval but the HTTP/HTTPS origin, Wi-Fi client isolation, SSRF, token expiry and local listener shutdown are unverified. | Issue #8 threat model must pass *before* enabling exposed network listener in production. **OPEN** |
| A14 | **MEDIUM / TRIAGE** | GitHub #2/#3 were `needs-triage`; #4–#8 had no clear role labels or native blocking edges at review start. The formal `to-tickets` skill requires each deliverable independently testable. | Mark dependency/ownership and triage appropriately after audit, not claim P1 issues ready. **OPEN** |
| A15 | **LOW / SKILL USE** | 27 installed skills are not 27 mandatory jobs. `improve-codebase-architecture`, TDD, implement and code-review require executable code/change surfaces which do not exist. | Apply research, domain modeling, writing-for-agents, wayfinder, to-spec/tickets and review/retro *to the planning artifacts*; avoid claiming 27 full invocations. |
| A16 | **LOW / UX** | Camera playlist defaults to shuffle while the user can reorder entries, which might surprise users; exact behavior not demonstrated. | Design sequence/shuffle cue and Preview option; test no accidental reorder loss. **OPEN in P1 UX** |

## Skills actually applied and applicability

- **Research:** checked official Android APIs and upstream provider licenses, rather than treating a Google-search URL as permission.
- **Domain modeling:** preserved canonical "camera", "local video", "playlist", "Dream mode" and separated stream, still image, metadata, permitted integration and physical device verification.
- **Wayfinder / to-spec / to-tickets:** reviewed P0 graph and staged acceptance; dependency edges are tightened in the critical-path document. Ticket state remains evidence-based.
- **Writing for agents / codebase design / architecture:** reviewed and corrected AGENTS, CONTRIBUTING and ADR drift; preferred an app-mode/player test seam before speculative modules.
- **Code-review (standards vs spec), triage, retro:** compared docs against signed-off D-IDs and against repo conventions; documented reproducible lint gaps.
- **TDD / implement / prototype / PR / deep refactoring / deployment:** deliberately **not executed** because the owner asked for a planning audit and there is no OpenVista app implementation or tested feature yet. Product UI prototypes remain future work.

## Impact on sequence and architecture

**First go/no-go (P0):** Device baseline (#4), then isolated media and USB tests (#5 and SAF/USB work), DreamService feasibility (#2), authorized camera register (#3) and provider map/weather (#6). QR (#8) and installer (#7) are independent research tracks after device baseline. Do not allow a catalog UI proof to hide missing actual licensed playable video.

**Second (P1):** One end-to-end manually launched local APK with D-pad, one licensed/test stream, one local media source and cleanup; then source registry, playlists, map, weather and overlays. Keep Dream and updater behind proved system interfaces.

**Third (P2–P3):** Safety, performance and reliability; 25-source/24h gate; verify every D-ID, label every unsupported feature and ask owner before first public GitHub binary.

## Changes vs still-open work

**Corrected in this review:** stale instructions and initial-device ADR, primary-source feasibility notes, D-ID traceability map, stronger all-doc/traceability validator, focused updates to issues #2/#3/#6/#7 and a dedicated SAF/USB proof issue #9. **Not done:** downloading licensed webcam streams, instrumenting playback, actual device testing, auto-update code, UI prototypes, release approval.

**Review integrity:** findings are about plans and verifiable external facts; they do **not** claim the existing OpenVista code failed, because no app code was present. Treat A01/A02/A04 as design blockers and A05–A16 as engineering/data-quality gaps; do not merge them into one generic “project broken” statement.
