# OpenVista: requirements-to-acceptance traceability

**Baseline mapped 2026-10-25; acceptance state reviewed 2026-10-26.** C-01 supersedes bundled first-party catalog assumptions with **user-managed sources and zero bundled feeds**. This map assigns all [78 decisions](decisions.md) a primary [stable-v1 acceptance gate](v1-acceptance.md); other gates may also apply. **PENDING V1 ACCEPTANCE** is not a claim that no tests ran: limited build and Hisense evidence exists in [current status](../STATUS.md), but the complete decision has not been independently signed off. Documentation checks alone never prove app behavior.

## Gate coverage and evaluation ownership

| Gate | Domain | P0 evidence / delivery relationship |
| --- | --- | --- |
| G01 | User-managed stream and playlist library | #3 source import + #5 HLS/DASH; empty-library UX and zero bundled feeds/media for public v1 |
| G02 | Manual app UX | #4 baseline; P1 UI and navigation tickets not yet created |
| G03 | DreamService, timing, power | #2 with #4 baseline; may be OEM unsupported |
| G04 | Local USB / playlist / decoder | #5 Media3, new SAF/USB spike issue needed |
| G05 | Failure recovery | #5 + P1 recovery tests; no runaway retry |
| G06 | Weather/map, UI overlays, localization | #6 and planned design/UX tickets |
| G07 | Performance/lifecycle and 24h stability | #4 metrics plus future soak test |
| G08 | Privacy/content/licensing | #3, #6, #8 plus rights/security review |
| G09 | Updater install constraints | #7; Android 9 silent installation unproven |
| G10 | Distribution/device matrix | #4 baseline and explicit release approval |

## Decision-level trace matrix

| Decision | Primary gate | Historical issue/research reference | Stable-v1 approval |
| --- | --- | --- | --- |
| D-01 | G03 | #2 + #4 | PENDING V1 ACCEPTANCE |
| D-02 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-03 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-04 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-05 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-06 | G08 | #3 + #6 + #8 | PENDING V1 ACCEPTANCE |
| D-07 | G08 | #3 + #6 + #8 | PENDING V1 ACCEPTANCE |
| D-08 | G08 | #3 + #6 + #8 | PENDING V1 ACCEPTANCE |
| D-09 | G08 | #3 + #6 + #8 | PENDING V1 ACCEPTANCE |
| D-10 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-11 | G07 | #4 + soak issue | PENDING V1 ACCEPTANCE |
| D-12 | G05 | #5 + recovery ticket | PENDING V1 ACCEPTANCE |
| D-13 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-14 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-15 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-16 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-17 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-18 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-19 | G02 | #4 + app-shell issue | PENDING V1 ACCEPTANCE |
| D-20 | G02 | #4 + app-shell issue | PENDING V1 ACCEPTANCE |
| D-21 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-22 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-23 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-24 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-25 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-26 | G02 | #4 + app-shell issue | PENDING V1 ACCEPTANCE |
| D-27 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-28 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-29 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-30 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-31 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-32 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-33 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-34 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-35 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-36 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-37 | G08 | #3 + #6 + #8 | PENDING V1 ACCEPTANCE |
| D-38 | G05 | #5 + recovery ticket | PENDING V1 ACCEPTANCE |
| D-39 | G03 | #2 + #4 | PENDING V1 ACCEPTANCE |
| D-40 | G03 | #2 + #4 | PENDING V1 ACCEPTANCE |
| D-41 | G03 | #2 + #4 | PENDING V1 ACCEPTANCE |
| D-42 | G05 | #5 + recovery ticket | PENDING V1 ACCEPTANCE |
| D-43 | G03 | #2 + #4 | PENDING V1 ACCEPTANCE |
| D-44 | G05 | #5 + recovery ticket | PENDING V1 ACCEPTANCE |
| D-45 | G07 | #4 + soak issue | PENDING V1 ACCEPTANCE |
| D-46 | G07 | #4 + soak issue | PENDING V1 ACCEPTANCE |
| D-47 | G05 | #5 + recovery ticket | PENDING V1 ACCEPTANCE |
| D-48 | G03 | #2 + #4 | PENDING V1 ACCEPTANCE |
| D-49 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-50 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-51 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-52 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-53 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-54 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-55 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-56 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-57 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-58 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-59 | G02 | #4 + app-shell issue | PENDING V1 ACCEPTANCE |
| D-60 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-61 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-62 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-63 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-64 | G04 | #5 + #9 | PENDING V1 ACCEPTANCE |
| D-65 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-66 | G06 | #6 + UX issues | PENDING V1 ACCEPTANCE |
| D-67 | G08 | #3 + #6 + #8 | PENDING V1 ACCEPTANCE |
| D-68 | G05 | #5 + recovery ticket | PENDING V1 ACCEPTANCE |
| D-69 | G10 | #4 | PENDING V1 ACCEPTANCE |
| D-70 | G10 | #4 | PENDING V1 ACCEPTANCE |
| D-71 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-72 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-73 | G08 | #3 + #6 + #8 | PENDING V1 ACCEPTANCE |
| D-74 | G08 | #3 + #6 + #8 | PENDING V1 ACCEPTANCE |
| D-75 | G09 | #7 | PENDING V1 ACCEPTANCE |
| D-76 | G07 | #4 + soak issue | PENDING V1 ACCEPTANCE |
| D-77 | G01 | #3 + #5 | PENDING V1 ACCEPTANCE |
| D-78 | G08 | #3 + #6 + #8 | PENDING V1 ACCEPTANCE |

## Remaining traceability work

1. Every approved requirement needs a reproducible automated or manual test-case ID, expected vs actual, target OS/device and evidence location. The map does NOT assert these tests exist.
2. Map secondary gates when creating P1 tickets: D-68 also affects G07; D-73 needs G08 and UX; D-75 is a platform/distribution issue.
3. Each verification needs date, device build, source rights/license, codec/profile, exact steps, observed output, measurements and redacted logs.
4. Distinguish PASS / FAIL / BLOCKED / NOT APPLICABLE. When an OEM blocks a system capability, record UNSUPPORTED ON THIS DEVICE plus manual-app fallback; never silently call it PASS.
5. Convert planned issue references to independently testable vertical-slice tickets as relevant P0 proofs finish. Do not prematurely mark unknowns ready-for-agent.
6. Retest changed requirements and updates to camera permissions/availability.

The 78 D-IDs are owner approvals. G01–G10 are implementation-team evidence gates, not completed tests.
