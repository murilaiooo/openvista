# OpenVista implementation plan — research and technical evidence first

**Prepared:** 2026-10-25. **Baseline:** 78 owner-approved product decisions. **Current state:** documentation only; no app code, validated TV test, or APK yet.

## Working constraints

- First APK: **local tests only**. No binary GitHub release until owner explicitly approves publication.
- First tested device: **Hisense 58U6HF / Fire OS 7**. Other Android TV, Google TV, Fire TV versions remain **not verified**. This does not reduce the product's long-term platform intent.
- Stable v1 must pass [v1 acceptance](../product/v1-acceptance.md), including **at least 25 distinct lawful, working, family-appropriate webcams**, documented 24-hour soak, privacy/licenses and compatibility results.
- Supported public live protocols initially: **HLS and MPEG-DASH**, not RTSP. Device codecs and HDR are test-dependent.
- **Never** root the TV, use privileged hacks/ADB workarounds to bypass package installation, automatically open at boot, publish unauthorized streams, or substitute guesswork for evidence.
- Skills: use the installed repository skills for architecture, specification, testing and code review. Run validation locally on the Windows dev machine; don't require GitHub Actions.

## P0 — research, isolated spikes and feasibility gates

| Sequence | Priority task | Specific investigation and test | Required evidence / exit |
| --- | --- | --- | --- |
| 0.1 | Device & toolchain baseline | Read-only ADB: Hisense model/Fire OS/API/build/SoC, connected SDK/JDK/Gradle, display capabilities; identify safe rollback | Device baseline note with dated commands and no changes to installed TV apps |
| 0.2 | Screensaver permission & remote | Minimal standalone Activity plus DreamService; can OEM select it, request 1/3/5/10/15/30 min, send OK/Back/arrows, and respect sleep? | Real-device support/limitation matrix; manual Activity remains valid even if Dream unavailable |
| 0.3 | SAF and USB | Prototype system folder-picker, persisted URI grants, recursive subfolders, USB unplug/replug, picker absent and missing files | Which folders the actual Fire OS 7 grants, with repeatable steps and limitations |
| 0.4 | Media3 decoding | Isolated tests using authorized HLS + DASH and local MP4/MKV/WebM/MOV; codec/HDR limits, bitrate quality fallback, 4K vs 1080p, CPU/RAM/frame drops | Source-by-source, container/codec-by-codec compatibility matrix; never infer support from extension |
| 0.5 | Licensed webcam catalog | Research providers' official rights/embedding rules; collect attribution, URL, geography, family-safety and playback verification on Hisense | Register of **25+ authorized and demonstrably playable sources**; no scraped or guessed URLs |
| 0.6 | Weather and maps | Compare official weather/map terms, 7-day fields, quotas, cached/offline fallback; prototype remote-friendly 2D clustering and list | Approved license/architecture recommendation and test note, no premature provider lock-in |
| 0.7 | Automatic app updates | Read PackageInstaller/unknown-source rules; test two throwaway *locally signed* versions on Hisense, system user prompts, cert continuity, install ownership and failure cases | Written feasibility verdict for unattended install vs required system confirmation; no privilege bypass |
| 0.8 | Local QR pairing | Threat model LAN phone+TV, short-lived token, explicit on-TV confirmation, replay/CSRF and malicious URL protections | Secure local pairing proof, or documented blocker and safe design alternative |

**Dependencies:** P0.1 before device tests; P0.4 before declaring any catalog stream actually playable; P0.5 rights research can proceed in parallel; P0.7 before asserting unattended update availability. Never begin a dependent production feature before its P0 proof is recorded.

## P1 — small, verifiable vertical slices, locally installed APKs

1. **Native TV shell:** Kotlin project, three large image-backed Home cards, D-pad focus, PT-BR/EN, settings scaffold, manual Activity and first **local** debug APK. No unlicensed assets.
2. **Minimal Media3 player:** authorized live HLS/DASH and local media; default mute, fill/crop, performance-first decoder selection, buffering/errors and proper pause/destroy.
3. **Local media library:** SAF internal + USB picker, recursive scan, compatible containers, invalid-file skip, last position resume, 10-second seek, next-item progression and repeat.
4. **Playlists and preferences:** separate camera/video playlists, batch add/remove/reorder, camera shuffle default, 5-minute rotation configurable to 1–60, Favorites sections and local automatic persistence.
5. **Camera catalog & discovery:** validated 25-source catalog with owner/rights metadata, safe remote update with bundled fallback, available/unknown/offline filters, 2D map with clustered pins and lateral D-pad list.
6. **Weather and widgets:** authorized seven-day provider, camera/viewer city context, optional consented location, presets, basic/advanced overlay editor and moving/hiding static widgets.
7. **Player UX and resilience:** OK quick mode switching, controls hide at 5 seconds, Basic/Advanced settings, optional real metrics panel; all-camera offline/local fallback and finish-current-video before live recovery.

Each slice closes only with reproducible PC tests **and** a feature demonstration on the target TV. Changes to installed TV apps require the user's authorized test workflow and an undo route.

## P2 — OS integrations and safety

8. **Optional DreamService:** integrate only after isolated proof; correctly handle interactive keys/automatic activation if available; don't pretend unsupported idle settings function.
9. **LAN QR URL entry:** implement only after safe pairing design approved; keep TV keyboard input as a reliable option.
10. **Release updater:** trust only eventual official signed update channel, verify package signature/version and asset integrity before downloads; request install using supported Android APIs. User confirmation is mandatory where OS requires it.
11. **Privacy and reliability:** user-triggered sanitized copy/share diagnostics, no background telemetry, no sync or settings export/import, bounded source retries and power-aware cancellation.

## P3 — stable v1 verification, then approval for public release

12. Run a documented **24-hour real-device soak** following [Hisense soak protocol](../testing/hisense-24h-soak-protocol.md): freeze numeric limits first, then measure playback/idle/network/USB, dropped frames, RAM/CPU/thermal trends and failures.
13. Audit >=25 rights-cleared, family-appropriate streams using [per-source proof records](../research/webcam-source-evidence-template.md) for attribution, actual on-TV HLS/DASH playback, region and failure handling. Audit weather/map contracts and QR/updater security.
14. Reconcile D-01–D-78 against the [acceptance matrix](../product/v1-acceptance.md); document all device-limited unsupported behavior and user-visible fallbacks.
15. Prepare signed reproducible APK, changelog, local test evidence and exact compatibility wording (Hisense tested only). **Ask owner before the first GitHub binary publication**.

## Backlog packaging — proposed small GitHub issues

| Group | Proposed ticket(s) | Exit test |
| --- | --- | --- |
| P0 | TV/device baseline; DreamService probe; SAF/USB; Media3 HLS+DASH/containers; camera rights register; forecast/map feasibility; installer test; QR threat model | Individual Markdown evidence + pass/fail table |
| P1 | Native shell; player; local library; playlists; camera catalog; 2D map; weather/widgets; remote/UX/recovery | Local debug APK, unit tests, Hisense screenshots/logs |
| P2 | Dream integration; secure LAN URL entry; update integrity/installer; performance/privacy | OEM-specific and security tests |
| P3 | 24h soak; licenses/privacy checks; compatibility matrix; controlled release | Checked acceptance contract + owner release approval |

## GitHub P0 issue tracker (created)

- [#2 — DreamService selection / idle / remote feasibility](https://github.com/murilaiooo/openvista/issues/2) (Hisense first, independently of USB).
- [#3 — Authorized global webcams and rights register](https://github.com/murilaiooo/openvista/issues/3) (existing; >=25-camera stable-v1 gate added).
- [#4 — Read-only Hisense and Windows toolchain baseline](https://github.com/murilaiooo/openvista/issues/4).
- [#5 — Media3 HLS/DASH and local-container device test](https://github.com/murilaiooo/openvista/issues/5).
- [#9 — Standalone SAF/USB scoped-tree and recursive folders probe](https://github.com/murilaiooo/openvista/issues/9) (blocked by #4).
- [#6 — Weather/map provider terms and offline behaviors](https://github.com/murilaiooo/openvista/issues/6).
- [#7 — Android Fire OS 7 updater and installer constraints](https://github.com/murilaiooo/openvista/issues/7).
- [#8 — Same-LAN QR pairing security prototype](https://github.com/murilaiooo/openvista/issues/8).

P1–P3 tickets should be created only as the P0 proof removes uncertainty. Each implementation issue must cite the corresponding tested artifact.

## Definition of done

- Link work to decision IDs and tests.
- Keep PRs small; no unrelated modifications; document risks and rollbacks.
- Run tests on the PC and TV, not just CI.
- Record actual license/OS/codec evidence before claiming compatibility.
- Preserve existing installed applications and saved settings on the TV until there is a specific test reason to change them.
- No GitHub APK release or claimed silent update until technical and owner gates pass.

See [v1 acceptance](../product/v1-acceptance.md), [78-D-ID gate traceability](../product/requirement-traceability.md), [critical path](critical-path.md), [independent planning audit](../reviews/2026-10-25-planning-audit.md), [official platform notes](../research/platform-feasibility-2026-10-25.md), [discovery decisions](../product/decisions.md).
