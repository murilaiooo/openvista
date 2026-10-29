# OpenVista R&D v2 — Research decision matrix and sequencing

**Evidence level:** All rows E1 desk research. The word GO means **research go**, not product approval. See [evidence contract](00-research-standard-and-source-audit.md) and [original release gates](../../product/v1-acceptance.md). Owner has not approved new feature builds. Re-evaluate these judgments after E2/E3 evidence, provider terms and user approval.

## 1. What users would actually notice

The strongest user value isn't how many settings we add; it is **more minutes watching a good moving scene, fewer unsuccessful switches, quick content setup, predictable data usage, and effortless nighttime viewing**. That requires relatively little visible UI but serious safety work under the surface.

| Rank for research | Feature | Incremental user benefit vs current app | Smallest disproof experiment | Relative engineering scope | External dependencies | Research outcome |
|---|---|---|---|---|---|---|
| 1 | **R07 Camera Health** | Fewer dead/unstable selections over long sessions | 100-source failure simulation vs current first-frame/failed set | M | none for E2; TV for E3 | GO E2; HOLD release |
| 2 | **R05 Data Monitor** | Understand daily data spent in 8h/day sessions | UID counter on Fire OS, compare known payload | S/M | Android OS counters | GO E2; HOLD E3 |
| 3 | **R13 M3U subscriptions** | Keep sources current without losing favorites after URL changes | deterministic three-way merge on rotating signed URLs | L | list host and credentials | GO E2; HOLD network |
| 4 | **R01 Smart Director** | More varied relevant footage without manual Next | seeded repeated-city/failed-source benchmark | M/L | R07 useful, R02 optional | GO E2; HOLD product |
| 5 | **R02 Sun Chaser** | Find daylight/sunrise from own geolocated cams | ephemeris error tests incl polar cases | M | coordinates, tested solar algorithm | GO E2 |
| 6 | **R06 Live Preview** | Recognize visually appealing feed without repeated fullscreen starts | D-pad focus test with thumbnail vs no thumbnail; decode ownership | M/L | permissions to thumbnails; target hardware | GO design; HOLD decoder |
| 7 | **R27 Navigation IA** | Faster discovery of existing and future functions | route/focus Back state suite and task-depth trace | M (test), L if refactor | no provider | GO audit |
| 8 | **R22 Comfort** | Easy evening mode with fewer distracting overlays | screenshot contrast/32-phase movement test | S/M | visual hardware approval | GO E2 |
| 9 | **R12 Network Library** | Play personal 4K library without USB copying | WebDAV authenticated Range+seek with test server | L | NAS/server, library dependencies | GO isolated transport |
| 10 | **R11 Routines** | Context-specific scenes throughout day automatically | fake clock DST/no-wake rule suite | M/L | system Dream lifecycle | GO E2 |
| 11 | **R10 Soundscapes** | Optional relaxing audio across silent webcams | audio-focus + 50 scene switches without leaked audio | M/L | local audio, hardware audio focus | HOLD feature |
| 12 | **R18 Discover Place** | Learn about viewed geography on demand | ambiguity and permissioned provider match tests | M/L | data source terms/API | HOLD provider |
| 13 | **R21 Digital Window** | Optional architectural illusion | compare to fullscreen for 8h static pixel risk | M | visual/hardware reviewer | HOLD, likely reject elaborate bezel |
| 14 | **R19 Home Assistant** | Automation for advanced users | mock official auth/WebSocket events; independent HA | L | HA server/tester | HOLD |
| 15 | **R23 Multi-TV** | Control more than one screen | two-peer authenticated command-only simulation | L/XL | second TV, D-67 scope decision | HOLD |
| 16 | **R24 Google Photos** | Personal photos and videos without manual copying | official eligibility/usage-scope confirmation first | XL | Google Partner/OAuth; policy | HOLD |
| 17 | **R26 More protocols** | Real wider camera supply only if lawful HD sources exist | 20+ rights-confirmed HD sources across providers and TV decoder proof | RTSP M; WHEP XL | media rights, decoder | RTSP GO E2 only; WHEP HOLD |
| 18 | **R25 YouTube Live** | Many attractive camera broadcasts | cannot prove ad-free official auto-play | M manual; prohibited auto | YouTube player and ads | NO-GO ambient |

**Scope** is relative engineering risk, not a duration or guaranteed budget. A feature requiring provider approval can consume indefinite time even if code is small.

## 2. Prerequisites versus attractive extras

```text
Unchanged v1 release gates (focus, source security, 24h soak)
   |
   +-- Route/Focal UX coverage R27 ----+----> source previews R06
   |                                   +----> all future provider UIs
   +-- Existing camera first-frame/retry correctness
   |      +-- Reliable event observations R07 --+--> Smart Director R01
   |      +-- Independent solar algorithm R02 --+
   +-- Safe source identity & parser behavior ----> M3U subscription R13
   +-- Explicit media-source type / rights model
   |      +-- RTSP user camera proof R26
   |      +-- WebDAV/NAS R12 --> optional personal self-hosted photos
   |      +-- Google Photos R24 only after API eligibility
   +-- Visual profile model R22 ---> optional R21 Window and R11 routine preset
   +-- Network auth/security review ----> R19 HA, R23 multi-TV (owner cannot test)
   +-- Data Monitor R05 can be investigated independently
   +-- YouTube ad-free R25 = hard policy/product conflict; no build
```

## 3. Research epics as falsifiable gates

**Gate A: Local deterministic functionality (lowest cost).** Build fake-clock/solar/R07/R01/R05 state-machine test harnesses with no Android runtime, no network, no user media. Evidence should include before/after metrics, random seeds, failure injection and all edge cases. If R01 adds no measurable benefit over a better shuffle, delete the proposal.

**Gate B: Data integrity and privacy.** Build R13 three-way merge test with 500 entries and signed URL rotations. Treat “zero missing favorite IDs” as hard invariant. Use isolated library fixtures and migration/rollback; no production library mutation.

**Gate C: Real display lifecycle.** Exercise R06 preview and R22 comfort on actual Fire OS 7 while verifying exactly one video decoder, player frame advancement, memory and focus. A screenshot alone cannot prove no freeze.

**Gate D: Controlled network protocol.** Test R12 WebDAV seek/auth, R26 RTSP H264, with **synthetic permitted local server**. Every endpoint is test-owned. No list harvesting or external camera-catalog claims.

**Gate E: External contract.** Google eligibility/provider terms, HA and multi-TV independent devices, source broadcaster rights. These gates are outside code control and remain HOLD if no tester/provider exists.

## 4. Dependency and risk-based recommendations

- **Research first:** R27 route audit, R07 health, R05 counters and R13 stable ID semantics. They address concrete existing pain.
- **Then differentiation:** R02 pure astronomy + R01 transparent director. These become valuable when cameras have useful coordinates and a healthy saved library; their utility may be low for users with two cameras.
- **Then convenience:** R06 focused preview, R22 comfort, R12 WebDAV. None should block player reliability.
- **Only after data:** R10, R11, R18, R21 and expensive platform integrations. Complexity, novelty and 4K claims need evidence of actual repeat use.
- **Defer/reject:** full-library multi-TV sync conflicts with D-67; automatic YouTube ad-free is no-go; universal camera format support without a lawful source inventory is unjustified.

## 5. Precise sign-off definition

A decision package for each feature must show **one reproducible test command with output**, **one failure case**, **one rollback path**, security/rights evidence, and **target-hardware measurement** if it affects player/OS. No E1 dossier qualifies. No feature is “approved to ship” without explicit owner consent; never merge experimental Kotlin into main just because a research score is high.
