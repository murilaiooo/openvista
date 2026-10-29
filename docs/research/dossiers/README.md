# OpenVista R&D dossiers — master index

**As of 2026-10-28.** Parent issue: [Research program #42](https://github.com/murilaiooo/openvista/issues/42). Documentation-only [draft PR #50](https://github.com/murilaiooo/openvista/pull/50). **Nothing below is a shipped or owner-approved new feature.** This is a GitHub-only desk research pass: existing repo source, published GitHub project code/readmes, past owner decisions and preexisting project notes. No general web browsing, laboratory test, device test, independent streaming probe, OAuth or API partner communication was performed.

## Selected owner proposals: 14 dossiers

| ID | Feature | Verdict on **next research step** (not feature approval) | Dossier | Track |
| --- | --- | --- | --- | --- |
| R01 | Smart Director | GO to isolated selector tests; HOLD adoption | [R01](R01-smart-director.md) | [#43](https://github.com/murilaiooo/openvista/issues/43) |
| R02 | Sun Chaser | GO to offline astronomical tests; HOLD adoption | [R02](R02-sun-chaser.md) | #43 |
| R05 | Data Monitor | GO to own-UID counter proof; HOLD adoption | [R05](R05-data-monitor.md) | [#44](https://github.com/murilaiooo/openvista/issues/44) |
| R06 | Live Preview | GO to focused low-memory design; HOLD adoption | [R06](R06-live-preview.md) | #44 / existing [#33](https://github.com/murilaiooo/openvista/issues/33) |
| R07 | Camera Health | GO to local history simulation; HOLD adoption | [R07](R07-camera-health.md) | #43 |
| R10 | Ambient Soundscapes | HOLD audio lifecycle and rights | [R10](R10-soundscapes.md) | [#45](https://github.com/murilaiooo/openvista/issues/45) |
| R11 | Ambient Routines | GO to clock/schedule tests; HOLD adoption | [R11](R11-ambient-routines.md) | #43 |
| R12 | Network Library (SMB/WebDAV) | GO to isolated transport proof; HOLD adoption | [R12](R12-network-library.md) | #45 |
| R13 | Subscribed M3U | GO to safe import/merge proof; HOLD adoption | [R13](R13-m3u-subscriptions.md) | #45 |
| R18 | Discover This Place | HOLD provider match, terms and UX | [R18](R18-place-discovery.md) | [#46](https://github.com/murilaiooo/openvista/issues/46) |
| R19 | Home Assistant | HOLD: independent server/device testers needed | [R19](R19-home-assistant.md) | [#47](https://github.com/murilaiooo/openvista/issues/47) |
| R21 | Digital Window | HOLD visual/displays care review | [R21](R21-digital-window.md) | #46 |
| R22 | Comfort Mode | GO to preset tests; HOLD adoption | [R22](R22-comfort-mode.md) | #46 |
| R23 | Multi-TV | HOLD: two independent physical TVs needed | [R23](R23-multi-tv-sync.md) | #47 |

## Additional owner-requested investigations

| ID | Area | Current preliminary stance | Dossier |
| --- | --- | --- | --- |
| R24 | Google Photos | Ambient Partner HOLD, Picker HOLD, unrestricted legacy Library NO-GO | [R24](R24-google-photos.md) |
| R25 | YouTube Live | NO-GO for guaranteed ad-free unattended ambient; manual official embed HOLD | [R25](R25-youtube-live.md) |
| R26 | More protocols and **actual source supply** | Existing HLS/DASH first; RTSP user-owned spike; MJPEG/WHEP HOLD | [R26](R26-camera-protocols-and-supply.md) |
| R27 | Full TV route graph / Settings IA | GO for route tests only; **folders route already exists** | [R27](R27-tv-navigation-system.md) |

## Cross-cutting research documents

- [Owner proposal scope and evidence gates](../2026-10-28-experience-rd-program.md)
- [Current and proposed navigation map](../2026-10-28-tv-navigation-information-architecture.md)
- [Media source initial feasibility](../2026-10-28-media-source-feasibility.md)
- [Independent test and verdict policy](../2026-10-28-research-validation-and-decisions.md)
- [Cross-feature seams, dependency graph and five-pass desk review](../2026-10-28-cross-feature-architecture-and-review.md)

## Reading guidance

**Research verdict is not a product decision.** “GO” in this index always means “a bounded next research or prototype step has sufficient preliminary rationale.” It never means approved, built, measured or releasable. All dossiers are E1 at most. Google/YouTube external developer policy details are described as candidate official references, not newly verified in this GitHub-only session. Source inventory figures copied from GitHub project READMEs are explicitly labeled third-party claims, not independently verified live streams or rights.

## Research order

1. Core experience and local algorithms: R01 → R02 → R05 → R06 → R07.
2. Media usability and inputs: R10 → R11 → R12 → R13.
3. Enrichment, integrations and displays: R18 → R19 → R21 → R22 → R23.
4. Major provider/format constraints: R24 → R25 → R26.
5. Full navigation architecture R27 and cross-feature regression review.
6. **STOP before implementation**: decisions remain unapproved, and physical TV / partner / source licensing gates are outstanding.

The order above is the documentation/research review sequence, **not** a dependency order for future development. No user participation is required to read these plans or perform planned fake-clock/HTTP/unit tests; unsupported physical acceptance remains blocked until authorized independent hardware evidence is available.

## GitHub research-only skill workflow

Applied guidance from project-local [research](../../../.agents/skills/research/SKILL.md), [codebase-design](../../../.agents/skills/codebase-design/SKILL.md), [domain-modeling](../../../.agents/skills/domain-modeling/SKILL.md), [wayfinder](../../../.agents/skills/wayfinder/SKILL.md), [to-spec](../../../.agents/skills/to-spec/SKILL.md) (as tentative hypotheses, not approved specs), [to-tickets](../../../.agents/skills/to-tickets/SKILL.md) (issue maps, not build tickets), [writing-for-agents](../../../.agents/skills/writing-for-agents/SKILL.md), [no-ai-slop](../../../.agents/skills/no-ai-slop/SKILL.md), [triage](../../../.agents/skills/triage/SKILL.md), and [code-review](../../../.agents/skills/code-review/SKILL.md) (standards/spec review, no executable test). **Not every installed skill is appropriate**: implementation/TDD/prototype/retro/human interviews require different work or permissions and are not claimed to have run. We followed the repository's [skill-selection policy](../../skill-selection.md) rather than invoking all 27 indiscriminately.

## Product constraints

No bundled cameras, M3Us, video files, private tokens, remote URL scrapers, ads or forced accounts. All new integrations default Off. Keep native Kotlin/Media3, existing sources, user data, Home's three panels, consistent Settings rail, normal D-pad operation, single video decoder where possible, and target hardware release gates. Any accepted work later gets its **own** small scoped implementation issue only after evidence and owner authorization.
