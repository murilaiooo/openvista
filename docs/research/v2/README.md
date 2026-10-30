# OpenVista R&D v2 — Evidence-driven feature research and navigation architecture

**Created:** 2026-10-28, revised after owner feedback that the first research draft was too shallow.  
**Branch:** `research/ambient-product-architecture-2026-10-28`; [draft PR #50](https://github.com/murilaiooo/openvista/pull/50) | [research epic #42](https://github.com/murilaiooo/openvista/issues/42).

This is a **new layer of engineering research**, not a cosmetic rewrite. It is grounded in direct OpenVista Kotlin source, external primary GitHub projects (AndroidX Media, Home Assistant developers, Aerial Views, go2rtc, MediaMTX), actual stream-catalog verification scripts, clear cross-feature dependencies and **testable contracts**. It does **not** claim any implementation, Android/Fire TV test, Google Photos partner approval, source broadcasting right, official non-GitHub policy revalidation or executed experiment. All E2/E3 work remains unperformed.

## Start here

1. [Research quality standard, evidence grading and hard product constraints](00-research-standard-and-source-audit.md)
2. [Decision matrix: prioritize, defer or reject based on evidence](90-feature-decision-matrix-and-dependencies.md)
3. [Actual open-source competitor capabilities and differentiation](91-comparison-with-real-world-open-source-apps.md)
4. [Synthetic and real-device experimental test protocol](92-experiment-protocols-test-data-and-measurement.md)
5. [Privacy, source-permissions, credential and LAN threat ledger](93-security-privacy-media-rights-ledger.md)
6. [Five-pass factual/source/link/static-risk audit and remaining blockers](94-five-pass-audit-and-evidence-limitations.md)
7. [Existing screen and app information architecture, full route/focus test matrix](R27-information-architecture-acceptance.md)

## All original owner-selected 14 items

| Owner number | Research subject and deep-dive | Central technical discovery | Status |
|---|---|---|---|
| 1 | [Smart Director / R01](R01-R02-smart-direction-and-solar-engine.md) | Pure explainable selection policy layered over existing `PlaybackSession`, not parallel player | GO E2 research; HOLD |
| 2 | [Sun Chaser / R02](R01-R02-smart-direction-and-solar-engine.md) | UTC solar elevation, pole/dayline edge cases, missing camera heading, no false sunny claim | GO E2 astronomy; HOLD |
| 5 | [Data Monitor / R05](R05-bandwidth-accounting.md) | UID RX/TX vs Media3 transfer counters, reset and missing coverage; no double counting | GO API capability; HOLD Fire OS |
| 6 | [Live Preview / R06](R06-preview-decoder-budget.md) | `PlaybackSession` single-owner must not be interrupted by second preview decoder | GO still-first; HOLD video preview |
| 7 | [Camera Health / R07](R07-camera-health-statistics.md) | First frame ≠ sustained reliability; do not penalize internet/auth/codec failures globally | GO local model; HOLD |
| 10 | [Soundscapes / R10](R10-audio-architecture.md) | Audio-only engine separate from one video decoder and Dream lifecycle | HOLD audio/hardware |
| 11 | [Routines / R11](R11-scheduling-policy.md) | Existing OS/screensaver schedule ≠ recurring content profiles; no forced TV wake | GO pure clock; HOLD |
| 12 | [Network Library / R12](R12-network-media-transport.md) | Real SMBJ + WebDAV Range `DataSource` precedent, no GPLv3 copying, secure secrets | GO WebDAV E2; HOLD |
| 13 | [M3U Subscriptions / R13](R13-subscription-identity-migration.md) | Current exact-URI import is not refresh; signed tokens require three-way merge | GO merge E2; HOLD |
| 18 | [Place Discovery / R18](R18-geography-and-knowledge-provenance.md) | City coordinates cannot prove landmarks or camera viewing direction | HOLD provider/accuracy |
| 19 | [Home Assistant / R19](R19-home-assistant-protocol-threat-model.md) | Official GitHub WebSocket auth contract and locked OpenVista LAN pairing gate | HOLD independent E3 |
| 21 | [Digital Window / R21](R21-R22-visual-display-research.md) | Fixed frame risks long-session static pixels and picture crop | HOLD, reject ornate bezel |
| 22 | [Comfort Mode / R22](R21-R22-visual-display-research.md) | Build preset over existing opacities; no false LCD brightness/burn-in claims | GO pure preset; HOLD E3 |
| 23 | [Multi-TV / R23](R23-multi-tv-distributed-protocol.md) | Separate scene intent from library sync and frame sync; D-67 scope conflict | HOLD external TVs and owner scope |

## Additional owner-requested subjects and architecture

| Research | Deep-dive | Critical result |
|---|---|---|
| Personal Google Photos | [R24](R24-google-photos-auth-and-media-semantics.md) | Ambient partner access, Picker limitations and Library API changes need direct Google policy/eligibility evidence before integration; account strictly optional |
| YouTube Live | [R25](R25-youtube-live-contract-review.md) | Existing OpenVista draft #35 is manual official embed experiment, not built/tested; YouTube cannot meet guaranteed ad-free ambient requirement |
| New video protocols **and actually useful cameras** | [R26](R26-protocols-supply-and-decoder-feasibility.md) | Primary [AndroidX RTSP module](https://github.com/androidx/media/blob/release/libraries/exoplayer_rtsp/README.md) supports default ExoPlayer integration once dependency added; broad source/rights evidence still absent |
| App map and fluid Settings | [R27](R27-information-architecture-acceptance.md) | Explicit current routes, focus/back transitions, 3-panel Home/7 Settings categories, avoid adding a new screen for every feature |

The original [18 individual E1 dossiers](../dossiers/README.md) and [initial planning files](../2026-10-28-experience-rd-program.md) remain available as historical research. V2 is the **preferred decision-making reference**. Some second-pass files intentionally combine interdependent R01/R02 and R21/R22 at their technical seam while keeping the original per-item dossiers. They each have separate verdicts and acceptance criteria.

## Where real “10× deeper” evidence would still require more than GitHub reading

Paper research can expose flaws and design meaningful experiments but cannot establish **actual performance**: real TV decoder limits, 24-hour HLS stability, provider Google partnership, token lifetimes, cloud policy revisions, broadcast rights and legal video source supply. The owner explicitly restricted tools to GitHub and cannot test HA/multi-TV. Therefore these gaps are **first-class HOLD gates**, not silently called complete.

No new runtime Kotlin, source URLs, M3U lists, APKs, credentials or third-party asset media are committed in this PR. No merge to `main`. Every selected feature remains an optional proposal requiring explicit approval after evidence.

## Suggested issue ownership

- [#43](https://github.com/murilaiooo/openvista/issues/43): R01/R02/R07/R11 algorithms and local rules.
- [#44](https://github.com/murilaiooo/openvista/issues/44): R05 usage and R06 preview.
- [#45](https://github.com/murilaiooo/openvista/issues/45): R10 audio, R12 NAS/WebDAV, R13 M3U.
- [#46](https://github.com/murilaiooo/openvista/issues/46): R18 places, R21 window, R22 comfort.
- [#47](https://github.com/murilaiooo/openvista/issues/47): R19 HA, R23 multi-TV, R24 Google Photos.
- [#48](https://github.com/murilaiooo/openvista/issues/48): R25 YouTube and R26 actual source protocol evidence.
- [#49](https://github.com/murilaiooo/openvista/issues/49): R27 complete route and focus design.

**Research completion definition:** E1 documentation may be complete; an implementation-ready decision requires E2 reproduced tests, E3 hardware and/or provider eligibility as relevant, security terms review, and explicit owner product approval. No test-result numbers are asserted in these v2 materials.
