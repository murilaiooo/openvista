# OpenVista future features: research validation, scorecards and stop/go rules

**Created:** 2026-10-28. Research only. Parent [#42](https://github.com/murilaiooo/openvista/issues/42).  
**Important:** The owner cannot personally validate Home Assistant or multi-TV, and is not available for a manual end-to-end test program. No research document can replace missing physical-TV or provider partner acceptance.

## Research evidence levels

- **E0** hypothesis with no independent external evidence.
- **E1** official API/provider documents, accurate current code audit, repeatable sample inventory with provenance.
- **E2** isolated prototype and repeatable automated behavior/unit/integration results on test infrastructure. Include commands, source commits, logs and known limits.
- **E3** independent actual compatible hardware/device tests, recorded firmware/Android API, latency, codec, RAM, CPU, navigation and failure behavior. Must be performed by available authorized tester or lab; never call emulator E3.
- **E4** prolonged unattended real-device stability, media/permission/security/compliance, regression, release-quality evidence.

Research may conclude E1/HOLD even if no device can be made available. Only new implementation work requires explicit owner approval. Current preview's existing 24-hour soak gate still stands.

## Test tracks that do not require the owner

| Track | Independent test approach | Evidence to store | Remaining acceptance |
| --- | --- | --- | --- |
| Smart Director / Camera Health | Deterministic JVM fixtures: 100+ virtual sources, gaps, network outage, metadata errors; compare with baseline random/fixed rotation | Ranking fairness, failure-decay, no aggressive probing, repeatability | On-device scene-switch latency and long-session behavior |
| Sun Chaser | Offline astronomy reference tests at equator, polar regions, DST and international date line; no live feed required | Expected solar events, uncertainty, unknown-coordinate fallback | Visual UX of daylight mode |
| Data Monitor | Fake monotonic TrafficStats provider + boot resets, API unsupported, RX/TX, storage growth; compare own-app counters where allowed | Bytes accuracy within documented coverage; honest unsupported/estimated labels | Real Fire OS counter availability |
| Live Preview | Synthetic thumbnails; one-session-at-a-time cache policy; automated focus / screen-state tests | Memory bounds, canceled fetch, stale detection | Native decoder behavior on target TV |
| Soundscapes | Local licensed sample clips, simulated player lifecycle and volume ducking | Loop boundaries, long-duration drift/CPU on available test device | Target codec/audio behavior |
| Routines | Fake clock and device lifecycle for DST, sleep, reset, time-zone change | Exactly-once transition intent, no wake loop | OEM scheduling/permissions |
| NAS/WebDAV | Containerized read-only test services and synthetic 4K files with size/hash known | TLS, credentials, range/seek, disconnect, auth | LAN performance on real TV |
| M3U subscription | Controlled HTTPS fixtures, ETag, 304, redirect, expired auth, malformed/cyclic entries, large list | Preservation of custom names/favorites/playlist IDs | Real remote picker/refresh UX |
| Place context | Licensed mocked place results and offline cache; invalid/ambiguous locations | Correct attribution and zero guessed place facts | Live provider/UX approval |
| Comfort / Digital Window | Screenshot diff, layout test on multiple resolutions, focus/readability | No permanent hot bright border, retained overlays and safe areas | Display-care 32-phase physical TV review |
| Home Assistant | Fake HA server, protocol tests, authenticated commands and replay attacks | Threat model, no unauthenticated LAN listener | **Independent external HA integration tester**; owner cannot test |
| Multi-TV sync | Multi-process simulators and multiple emulators, network partition/conflict tests | State convergence, authentication, revoke, version skew | **At least two physical independent TVs**; owner cannot test |
| Google Photos | Official docs + eligibility questionnaire + mock session/token expired/OAuth revocation | Partner eligibility proof / accepted scopes | Google review and independent real sign-in |
| YouTube | Policy analysis and official player experiments only when useful | No-ads constraint cannot be met by blocking ads | **NO-GO** for unattended ad-free ambient playback |
| Additional protocols | Rights-backed test sources, validator, ffprobe where licensed, 10m/1h repeated media measurements | HD/bitrate/uptime, provider/category/country spread, auth and user permissions | Fire OS real decoder, terms and reliability |
| Navigation | Route graph assertions, generated accessibility/focus model, automated remote D-pad key tests | Screens reachable, Back invariant, initial/return focus | Physical remote UX check when independently possible |

## Product scorecard

For each topic record 1–5, **unknown** if not measured:

- **User value**: a task made easier or recurring annoyance removed.
- **Evidence confidence**: provider terms + code tests + device reality, not fabricated scores.
- **Supply fit**: especially for camera protocols, proven number of lawful HD available sources.
- **Performance risk**: codec ownership, CPU/GPU/RAM/network impact, stability risk.
- **Maintenance burden**: tokens, vendor API, server costs, partner dependency, external service reliability.
- **Privacy/legal risk**: permissions, OAuth/data retention, broadcaster rights, ads.
- **Interaction simplicity**: actions on TV remote, minimal primary screens, discoverable defaults.
- **No-user-test feasibility**: ability to validate independently or explicit deferred hardware gate.

The scorecard cannot overrule a hard legal/safety/policy blocker. Prefer simpler changes with proven everyday usefulness over flashy research with no source supply.

## Acceptance outcomes (examples, provisional, not approvals)

| Proposal | Initial research stance | Reason |
| --- | --- | --- |
| Navigation map / route audit | **GO to deeper audit only** | Grounded code discrepancy and strong immediate usability impact |
| Data Monitor | **GO to E2 feasibility only** | Standard platform counters, but target OS support unverified |
| Camera Health / Sun Chaser / Smart Director | **GO to isolated E2 research only** | Local algorithms and fixture-testable logic |
| Live Preview | **GO to design+memory prototype only** | Real value, single-decoder budget unverified |
| Soundscapes / Routines / M3U / NAS | **HOLD feature approval** | Can mock/test, security and physical performance outstanding |
| Place details / Digital Window / Comfort | **HOLD feature approval** | Requires interaction and visual reference review |
| Home Assistant / Multi-TV | **HOLD** | External independently authorized hardware testers missing |
| Google Photos Ambient | **HOLD** | Partner Program approval needed |
| Google Photos Picker | **HOLD** | User-use-case/policy/OAuth UX validation needed |
| Google Photos Library (all personal photos) | **NO-GO** | API restriction after 2025 |
| YouTube ad-free background / automatic rotation | **NO-GO** | Advertisements cannot be blocked under official policy |
| RTSP | **GO to licensed-source inventory and E2 only** | Media3 supports subset, real catalog improvement unknown |
| MJPEG/snapshot / WHEP | **HOLD** | Demonstrate live HD camera inventory and hardware viability first |
| RTMP/SRT production ingest | **LOW priority / likely NO-GO** | Weak practical supply for ambient user cameras |

## Engineering safety controls

- No bundled camera URLs, private source fixtures, M3U lists or test videos in repository release assets; guard final APK and git history. Never publish user tokens or LAN addresses.
- No change to installed TV apps, launcher, disabled system screensaver, APK signing, releases, or mainline product code during research.
- Google Photos optional account flow must not become a mandatory OpenVista account; respect revocation, no cloud export, verification, expiring URLs.
- No extra always-on background decoder, passive blanket network scan, hidden proxy/transcoding/DRM bypass, automatic server uploads, ad blocking, or unapproved trackers.
- Credentials for NAS/RTSP/API integrations are encrypted and scoped. Untrusted M3U addresses must not drive device-local SSRF/probing or leak URL secrets through logs.
- Preserve Home's three-card layout, D-pad safe navigation, muted default, camera/local playlist boundaries, EN/PT-BR and user-accessible clear/reset controls.
- If emulator/KVM unavailable, document blocker, use local JVM/unit validation and seek trusted independent physical tester for E3. **Do not ask owner to test what they cannot test**.

## Reporting template for future R&D outcomes

```markdown
# Rxx - Research verdict (date)
Status: E0 / E1 / E2 / E3 / E4
Outcome: GO / HOLD / NO-GO (for further development only)
User job:
Known code/design baseline:
Primary-source documentation (URLs + consulted dates):
Inventory of rights-confirmed source samples (if protocol):
Alternatives rejected with reasons:
Prototype branch/commit and exact test commands:
Measured codec / FPS / RX-TX / RAM / CPU / focus / startup:
Privacy/licensing/provider terms:
Risks and rollback:
Evidence gaps, including unavailable owner/hardware tests:
Recommended UI placement and settings location:
Explicit product approval: NOT GRANTED / GRANTED WITH LINK
```

## Sequence

1. Audit route graph and create an acceptance checklist per existing screen (especially `folders` dispatch), no app changes.
2. Complete licensed/source-format inventory, YouTube policy and Google Photos partnership decision.
3. Design isolated test harnesses for data counters, solar math, health tracking, and preview lifecycle.
4. Write physical-TV acceptance gates; do not pretend unavailable hardware was tested.
5. Collect structured per-proposal verdicts; ask for targeted approvals only after proof, creating implementation tickets only for approved GO items.
