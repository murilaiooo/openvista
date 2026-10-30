# OpenVista R&D v2 — Research standards, evidence and falsification

**Purpose:** This is a second, substantive research pass, not a restatement of the E1 idea inventory. It remains confined to GitHub public-source and first-party repository evidence. **No code execution, app changes, live stream access, TV inspection, provider authentication or non-GitHub web access.** The owner's requirement is decision-grade engineering research, not simply a high page count.

## Research quality contract

Each feature study must answer seven questions separately:

1. **Which exact behavior exists now?** Pin to actual source modules, methods, defaults, current formats and persistence invariants. Cite GitHub source links, not prose summaries. Distinguish current implementation from a plausible proposal.
2. **What observable user problem changes?** Define task-specific success, competing approaches, and negative cases where the feature is actively harmful.
3. **What is the smallest possible vertical prototype?** Define input, output, state transitions, failure semantics, and rollback. No generic service layer without two concrete adapters.
4. **What evidence would falsify feasibility?** Specify fixture values, test procedure, measurement artifact, budget and explicit stop condition. A paper-only answer is at most E1.
5. **How does it interact with the rest of the application?** Model source identity, player lifecycle, fallback, app/Dream mode, D-pad focus and Android 9 hardware.
6. **Are permissions and distribution rights viable?** Provider terms, source authorization, scopes, licensing, credential storage, third-party server, and abuse surfaces. Never infer permission from a public URL or “fair use” curator label.
7. **What is the decision?** Distinguish discovery feasibility from approval to build, cost estimate hypotheses from measured results, and production evidence from tests still blocked.

## Hard existing product constraints (not optional design preferences)

- [C-01](../../product/user-managed-content.md): zero built-in live streams, camera catalogs or shipped third-party playlists. Users bring permitted streams and videos.
- [v1 acceptance](../../product/v1-acceptance.md): no mandatory account/ads/telemetry, 24-hour physical device soak, Fire OS 7 as first targeted platform, approval before publication; D-67 explicitly defers library/settings backup and cloud sync. A multi-TV shared-library proposal is therefore a **product scope change**, not automatically compatible.
- [LibraryModels.kt](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt): CAMERA/VIDEO families, string `streamKind`, fixed settings, optional coordinates and an observed availability flag.
- [LibraryStore.kt](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt): one `AtomicFile` JSON store, bounded schema/item count/bytes, serialized changes via worker, validation and durable flush; not a high-frequency metrics database or encrypted credential vault.
- [PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt): main-thread single-owner `ExoPlayer`, source-family separation, first-frame availability, restore/fallback, network-aware failure and timed recovery. New live adapters must not start a second background player.
- [UserSourceParser.kt](../../../source-core/src/main/kotlin/com/openvista/sources/UserSourceParser.kt): HLS/DASH-only source kinds, 1MiB M3U input, 500 entries and strict URI parsing. Source identity changes when signed query parameters rotate unless explicitly addressed.
- [PairingCoordinator.kt](../../../player/src/main/java/com/openvista/prototype/pairing/PairingCoordinator.kt): `PairingSecurity.REVIEWED=false`; local network controls require a separate security gate, not merely a new Settings toggle.
- [SettingsShell.kt](../../../player/src/main/java/com/openvista/prototype/ui/SettingsShell.kt): existing seven groups and D-pad navigation; do not add 18 Home cards.

## Evidence grading and immutable result fields

E0: speculative concept; E1: read audited source/spec and traceable external upstream code; E2: reproducible isolated test output with commit, command, hardware and artifacts; E3: genuine target TV/HA/NAS behavior with exact firmware and captured metrics; E4: production/long-duration validation, privacy/rights and rollback. **All v2 studies remain E1 until experimental output is attached.**

Each study reports: `code_truth`, `alternative_decision`, `prototype_contract`, `test_vectors`, `measured_results = NOT MEASURED`, `device_validation = PENDING`, `legal_gate`, `stop_rule`, `recommended_research_next_step`. Targets are hypotheses, never retrospective passes.

## Cost and benefit normalization

Do not give invented USD engineering costs or pretend 10/10 value scores are measurements. Report **relative** likely effort (XS/S/M/L/XL), scope of ongoing external dependencies, maintenance failure surface, decoder/GPU/network cost to test, and user time saved. Scoring inputs: P(benefit) confidence, user task frequency, estimated engineering footprint, permission burden and **consequence of failure**. “Very innovative” is not a proxy for user retention.

Competing concepts must be compared against **do nothing / improve existing UI**. A feature is rejected if the user can already achieve ≥80% of its value via an existing simple action with less risk; this 80% figure is a decision heuristic, not empirically derived.

## Risk ledger and research blocker classes

**B1:** External provider policy/partnership/terms; **B2:** Android 9/Fire OS physical codec/GPU/system API availability; **B3:** private-source credentials/SSRF/LAN authentication; **B4:** source supply and license; **B5:** persistence migration and data loss; **B6:** focus/navigation/visual fidelity; **B7:** overnight playback/recovery/energy; **B8:** owner decision overriding D-67/C-01 or v1 scope.

For every B-class: identify who can obtain evidence (GitHub source analyst, independent tester, provider, owner) and do not manufacture unavailable tests. If no owner testing is available, use test doubles E2 then **HOLD E3**.

## Required prototype report (future work; not execution)

```text
Experiment ID / branch / commit / exact dependencies / target API:
Hypothesis and single rival baseline:
Input fixture and provenance (never secret or nonlicensed camera URL):
Procedure / commands / duration:
Observations (raw metrics, n, variance):
Failures and error taxonomy:
Compare to baseline / alternate:
Device exact model/build/OS when applicable:
Terms/license evidence and date verified:
Data safety / rollback:
Reproduce or falsify:
Evidence grade / GO research, HOLD, NO-GO:
```

## Independently checked upstream exemplars (GitHub source only)

- [AndroidX Media](https://github.com/androidx/media) for native player module design, including optional RTSP module; prefer docs/spec instead of assumptions about codec support.
- [Aerial Views](https://github.com/theothernt/AerialViews) includes SMB2/3 through SMBJ and WebDAV through a Media3 `BaseDataSource` and tested HTTP/Range behavior. It is GPLv3; study concepts but **do not copy code** into the MIT OpenVista repository.
- [Immich Android TV](https://github.com/giejay/Immich-Android-TV) for bounded photo/video selection and login UX, not proof of Google Photos access.
- [go2rtc](https://github.com/AlexxIT/go2rtc) and [MediaMTX](https://github.com/bluenviron/mediamtx) for real camera protocol interoperability; a self-hosted gateway is **not** the same as native decoder support.
- [Live Environment Streams](https://github.com/william-ricchiuti/Live-Environment-Streams) GitHub README/catalog: source labels and HTTP probes are **not legal access and HD decoding verification**. The project's public HLS validator returns success on HTTP 2xx, not decode.

## Documentation discipline

All new files live in `docs/research/v2/`. Existing Rxx E1 dossiers remain historical context. New v2 investigations refer back to these files but must add experiments, algorithms, migration/failure details and explicit alternatives. No implementation issue is labeled approved. Use the existing PR #50 as research review, not a feature PR. The v2 final audit must check markdown paths and diff scope plus misleading measured claims.

**Unverified claims limitation:** External Google/YouTube policies, pricing, quotas and current partnership conditions require primary-provider verification. GitHub-only access cannot establish them; all such points are explicitly provisional. The research will not manufacture API eligibility or live-camera measurements.
