# R&D v2 — Five-pass audit, factual corrections and unresolved gates

**Date:** 2026-10-28. **Scope:** GitHub-only read + documentation updates in [draft PR #50](https://github.com/murilaiooo/openvista/pull/50). This is a manual/static audit, **not** a build, runtime test, code review by independent engineers or formal legal/security audit.

## Pass 1 — User scope coverage

Checked all **14 selected owner IDs**: 1 Smart Director; 2 Sun Chaser; 5 Data Monitor; 6 Live Preview; 7 Camera Health; 10 Soundscapes; 11 Routines; 12 NAS; 13 M3U subscriptions; 18 Place Discovery; 19 Home Assistant; 21 Window; 22 Comfort; 23 Multi-TV. Plus 4 explicitly requested or necessary investigations: Google Photos, YouTube, new live formats/useful source supply, full app navigation. The [v2 index](README.md) maps each to a study and provisional decision. R01/R02 share one pure selection/solar seam study; R21/R22 share one overlay/preset study, while earlier [dossiers](../dossiers/README.md) still retain separate per-feature artifacts.

**Result:** All owner-selected topics have substantive source-grounded design plus specific falsification/test conditions. Coverage means research coverage, not confirmed feasibility.

## Pass 2 — Existing source alignment and architecture constraints

Re-read [PlaybackSession](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) events (single active player, first-frame status, live-edge resync, fallback, player settings), [LibraryStore](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) (single AtomicFile JSON, validation caps, exact-URI M3U import), [LibraryModels](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) (two families, optional geodata, display settings), [UserSourceParser](../../../source-core/src/main/kotlin/com/openvista/sources/UserSourceParser.kt) (HLS/DASH, 500 entries, 1 MiB), [PlayerScreen](../../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt) (D-pad actions, 5s controls), [OpenVistaActivity](../../../player/src/main/java/com/openvista/prototype/OpenVistaActivity.kt) (route stack and explicit `folders` dispatch), [PairingCoordinator](../../../player/src/main/java/com/openvista/prototype/pairing/PairingCoordinator.kt) (security gate disabled), and relevant overlay/settings sources.

**Correction carried forward:** The first research wrongly suggested `folders` was absent from the Activity dispatcher. Source explicitly includes it. Both initial IA document and issue #49 were corrected before v2. No `folders` bug is asserted.

**Critical constraint:** D-67 [v1 acceptance](../../product/v1-acceptance.md) defers library/settings backup and sync. Therefore full-library Multi-TV sync is a future scope-change proposal, not automatically compatible.

## Pass 3 — External primary GitHub source vs unsupported ecosystem claims

Confirmed **official AndroidX Media RTSP module** [README](https://github.com/androidx/media/blob/release/libraries/exoplayer_rtsp/README.md), official [sample Kotlin code](https://github.com/androidx/media/blob/release/docsamples/src/main/java/androidx/media3/docsamples/exoplayer/Rtsp.kt), and `DefaultMediaSourceFactory` RTSP factory reflection. Evidence supports an isolated Media3 RTSP **prototype** with matching version dependency; **does not** prove Hisense codecs or source rights.

Confirmed **official Home Assistant developer docs on GitHub** for [authenticated WebSocket](https://github.com/home-assistant/developers.home-assistant/blob/master/docs/api/websocket.md) and [Bearer-token REST](https://github.com/home-assistant/developers.home-assistant/blob/master/docs/api/rest.md), allowing an explicit protocol and threat model. No real HA instance connected.

Inspected [Aerial Views](https://github.com/theothernt/AerialViews) SMBJ, Media3 WebDAV Range DataSource, Immich API and message API files. These features already exist in other products, so some OpenVista ideas are **useful parity, not groundbreaking novelty**. Its GPLv3 code cannot simply be copied into MIT OpenVista without compliance review. [Immich Android TV](https://github.com/giejay/Immich-Android-TV) confirms specialized remote-photo product complexity, not Google Photos API eligibility.

Read [Live Environment Streams sources.json](https://github.com/william-ricchiuti/Live-Environment-Streams/blob/main/sources.json) and [validation script](https://github.com/william-ricchiuti/Live-Environment-Streams/blob/main/scripts/validate.py). Its 5,997/4,226 counts are maintainer claims; 53/67 families labeled “fair_use” does NOT confer a playback license. The public `probe_hls` only checks HTTP 2xx, not actual decoded HD/24h video. Broad country counts are not a lawful live-video catalog. These corrections are included in [R26](R26-protocols-supply-and-decoder-feasibility.md).

**Unverified sources:** Live Google Photos Partner, Picker, Library policies, scopes, quotas and legal use; YouTube current developer policy details; OSM/Wikidata provider rate/attribution; rights of any individual external camera. Only GitHub was used, so these remain explicit HOLD and must not be represented as new primary-provider checks.

## Pass 4 — Hypothesis falsification, risk and product decision consistency

Checked the research against C-01 (no bundled streams), D-67 (no cloud settings/playlist sync in v1), default muted/account-free/privacy-first design, real 32-phase display care and stable one-decoder playback. Competing design options, failure modes, and "kill criteria" are present in new deep studies. [Experiment protocol](92-experiment-protocols-test-data-and-measurement.md) defines E2 synthetic fixtures, actual metrics, target-TV E3 extension and unmeasured example output schema. [Threat ledger](93-security-privacy-media-rights-ledger.md) describes SSRF, auth redirect, camera token, LAN commands and personal photo privacy. Numeric bandwidth scenarios are *assumptions*, not the TV's actual usage.

**Provisional decisions:** R01/02/05/07/11/13 research GO for narrow deterministic tests; RTSP research GO for licensed synthetic source; R06/12/22 conditional research; HA/Multi-TV/Google Photos/WHEP/individual camera supply HOLD; automatic ad-free YouTube NO-GO; Digital Window ornate matte likely poor trade-off. No runtime feature adopted.

## Pass 5 — GitHub diff/links/secrets scope

GitHub `list_pr_changed_filenames` and `fetch_pr_patch` returned **46 changed files and all 46 Markdown under `docs/research/`** at the initial v2 security scan; **22 were in `docs/research/v2/`** then, with this audit file and later index edits adding docs. The added-patch text was ~319k characters at the time of that scan. A static pattern check found **zero GitHub classic/fine-grained PAT prefixes, zero Google API-key/AWS access-key prefixes, zero credential-bearing RTSP/HTTP URL formats and zero direct HLS/DASH URL patterns** in the added text. This is a **narrow heuristic scan**, not repository history, APK assets, provider license or professional secret scanning.

Fetched all then-current **22 v2 Markdown files**, resolved their relative Markdown links against the actual branch Git tree, and found **zero broken relative target paths**. This does not verify external website URLs or heading anchors. This new file and any later edits must be included in the subsequent final changed-file check.

## Honest final limitation

The depth increase is substantive **desk engineering research**, not a disguised E2/E3 implementation. No Gradle tests, fake HTTP servers, camera URLs played, 24h soak, physical Fire TV measurements, Google OAuth approval or HA/Multi-TV setups were run. GitHub-only constraints make those unavailable; withholding such evidence is necessary for correct product decisions. An E1 memo can justify a cheap falsifiable prototype; it cannot approve integration or production compatibility.
