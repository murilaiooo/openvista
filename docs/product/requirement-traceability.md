# OpenVista: requirements-to-acceptance traceability

**Audit date:** 2026-10-25. **Canonical scope:** [78 approved decisions](decisions.md). This map assigns **every** decision to one **primary** [v1 acceptance gate](v1-acceptance.md); a requirement may influence other gates. **NOT TESTED** means planned, not passed. Documentation checks do not prove app behavior.

## Gate coverage and evaluation ownership

| Gate | Domain | P0 evidence / delivery relationship |
| --- | --- | --- |
| G01 | Legal playable webcam streams & catalog | #3 rights + #5 HLS/DASH; 25 legitimate family-safe sources before public v1 |
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

| Decision | Primary gate | Research/issue owner | Actual verification |
| --- | --- | --- | --- |
| D-01 | G03 | #2 + #4 | NOT TESTED |
| D-02 | G01 | #3 + #5 | NOT TESTED |
| D-03 | G04 | #5 + #9 | NOT TESTED |
| D-04 | G06 | #6 + UX issues | NOT TESTED |
| D-05 | G06 | #6 + UX issues | NOT TESTED |
| D-06 | G08 | #3 + #6 + #8 | NOT TESTED |
| D-07 | G08 | #3 + #6 + #8 | NOT TESTED |
| D-08 | G08 | #3 + #6 + #8 | NOT TESTED |
| D-09 | G08 | #3 + #6 + #8 | NOT TESTED |
| D-10 | G01 | #3 + #5 | NOT TESTED |
| D-11 | G07 | #4 + soak issue | NOT TESTED |
| D-12 | G05 | #5 + recovery ticket | NOT TESTED |
| D-13 | G04 | #5 + #9 | NOT TESTED |
| D-14 | G04 | #5 + #9 | NOT TESTED |
| D-15 | G06 | #6 + UX issues | NOT TESTED |
| D-16 | G04 | #5 + #9 | NOT TESTED |
| D-17 | G01 | #3 + #5 | NOT TESTED |
| D-18 | G06 | #6 + UX issues | NOT TESTED |
| D-19 | G02 | #4 + app-shell issue | NOT TESTED |
| D-20 | G02 | #4 + app-shell issue | NOT TESTED |
| D-21 | G04 | #5 + #9 | NOT TESTED |
| D-22 | G06 | #6 + UX issues | NOT TESTED |
| D-23 | G06 | #6 + UX issues | NOT TESTED |
| D-24 | G06 | #6 + UX issues | NOT TESTED |
| D-25 | G06 | #6 + UX issues | NOT TESTED |
| D-26 | G02 | #4 + app-shell issue | NOT TESTED |
| D-27 | G06 | #6 + UX issues | NOT TESTED |
| D-28 | G06 | #6 + UX issues | NOT TESTED |
| D-29 | G01 | #3 + #5 | NOT TESTED |
| D-30 | G01 | #3 + #5 | NOT TESTED |
| D-31 | G01 | #3 + #5 | NOT TESTED |
| D-32 | G01 | #3 + #5 | NOT TESTED |
| D-33 | G01 | #3 + #5 | NOT TESTED |
| D-34 | G01 | #3 + #5 | NOT TESTED |
| D-35 | G06 | #6 + UX issues | NOT TESTED |
| D-36 | G06 | #6 + UX issues | NOT TESTED |
| D-37 | G08 | #3 + #6 + #8 | NOT TESTED |
| D-38 | G05 | #5 + recovery ticket | NOT TESTED |
| D-39 | G03 | #2 + #4 | NOT TESTED |
| D-40 | G03 | #2 + #4 | NOT TESTED |
| D-41 | G03 | #2 + #4 | NOT TESTED |
| D-42 | G05 | #5 + recovery ticket | NOT TESTED |
| D-43 | G03 | #2 + #4 | NOT TESTED |
| D-44 | G05 | #5 + recovery ticket | NOT TESTED |
| D-45 | G07 | #4 + soak issue | NOT TESTED |
| D-46 | G07 | #4 + soak issue | NOT TESTED |
| D-47 | G05 | #5 + recovery ticket | NOT TESTED |
| D-48 | G03 | #2 + #4 | NOT TESTED |
| D-49 | G01 | #3 + #5 | NOT TESTED |
| D-50 | G04 | #5 + #9 | NOT TESTED |
| D-51 | G04 | #5 + #9 | NOT TESTED |
| D-52 | G04 | #5 + #9 | NOT TESTED |
| D-53 | G04 | #5 + #9 | NOT TESTED |
| D-54 | G04 | #5 + #9 | NOT TESTED |
| D-55 | G04 | #5 + #9 | NOT TESTED |
| D-56 | G04 | #5 + #9 | NOT TESTED |
| D-57 | G04 | #5 + #9 | NOT TESTED |
| D-58 | G04 | #5 + #9 | NOT TESTED |
| D-59 | G02 | #4 + app-shell issue | NOT TESTED |
| D-60 | G06 | #6 + UX issues | NOT TESTED |
| D-61 | G01 | #3 + #5 | NOT TESTED |
| D-62 | G01 | #3 + #5 | NOT TESTED |
| D-63 | G06 | #6 + UX issues | NOT TESTED |
| D-64 | G04 | #5 + #9 | NOT TESTED |
| D-65 | G06 | #6 + UX issues | NOT TESTED |
| D-66 | G06 | #6 + UX issues | NOT TESTED |
| D-67 | G08 | #3 + #6 + #8 | NOT TESTED |
| D-68 | G05 | #5 + recovery ticket | NOT TESTED |
| D-69 | G10 | #4 | NOT TESTED |
| D-70 | G10 | #4 | NOT TESTED |
| D-71 | G01 | #3 + #5 | NOT TESTED |
| D-72 | G01 | #3 + #5 | NOT TESTED |
| D-73 | G08 | #3 + #6 + #8 | NOT TESTED |
| D-74 | G08 | #3 + #6 + #8 | NOT TESTED |
| D-75 | G09 | #7 | NOT TESTED |
| D-76 | G07 | #4 + soak issue | NOT TESTED |
| D-77 | G01 | #3 + #5 | NOT TESTED |
| D-78 | G08 | #3 + #6 + #8 | NOT TESTED |

## Remaining traceability work

1. Every approved requirement needs a reproducible automated or manual test-case ID, expected vs actual, target OS/device and evidence location. The map does NOT assert these tests exist.
2. Map secondary gates when creating P1 tickets: D-68 also affects G07; D-73 needs G08 and UX; D-75 is a platform/distribution issue.
3. Each verification needs date, device build, source rights/license, codec/profile, exact steps, observed output, measurements and redacted logs.
4. Distinguish PASS / FAIL / BLOCKED / NOT APPLICABLE. When an OEM blocks a system capability, record UNSUPPORTED ON THIS DEVICE plus manual-app fallback; never silently call it PASS.
5. Convert planned issue references to independently testable vertical-slice tickets as relevant P0 proofs finish. Do not prematurely mark unknowns ready-for-agent.
6. Retest changed requirements and updates to camera permissions/availability.

The 78 D-IDs are owner approvals. G01–G10 are implementation-team evidence gates, not completed tests.
