# R23 — Multi-TV: opt-in LAN scene and library state synchronization

**Verdict:** HOLD, research can proceed with simulated peers. **E1** architectural reasoning, not even proof the feature is valuable enough for the target audience. The owner cannot test multiple TVs. [Research #47](https://github.com/murilaiooo/openvista/issues/47).

## First answer: what should sync?
Distinguish **library metadata** (saved stream/playlist IDs), **scene intent** (play a given saved camera), **schedule/profile** (muted/night mode) and **frame-level playback** (same frame same millisecond). These are different products. The last is technically expensive, depends on network delay and independent live-stream buffers, and is **out of scope**. Do not advertise “perfect sync” or “no lag”.

## Baseline
[LibraryStore](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) saves library/settings on-device as `openvista-library.json`. [PlaybackSnapshot](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSnapshot.kt) stores queue, selected ID and recovery lineage within one device. [PairingCoordinator](../../../player/src/main/java/com/openvista/prototype/pairing/PairingCoordinator.kt) is **security-gated off**; no trusted peer registry. There is no account/cloud sync. Merging libraries requires stable IDs and conflict handling, not copying arbitrary local file URIs across TVs.

## Candidate minimal protocol
A deliberate QR/PIN device enrollment on same LAN with explicit confirmation **on both TVs**, unique per-pair secret and revoke UI. Allow sharing **camera/playlists metadata** only after scanning source URLs for credentials and obtaining explicit confirmation. **Local SAF content URIs and attached USB paths are not portable**, so never copy as playable links. Initial phase sync commands `playKnownCamera(id)` and `choosePlaylist(id)`; receiving TV says “source missing” if not imported. Schedule/visual preset may be copied with opt-in selection; secrets are excluded.

Alternatives: (A) hub-and-spoke single “controller” TV, simpler and recommended for first proof; (B) peer-to-peer election/multicast, complex and brittle across routers; (C) optional self-hosted broker, depends on extra infrastructure; (D) vendor cloud account, rejected. Never require LAN multicast or assume Wi-Fi client isolation is disabled.

## Safety and hard tests
Threats: hostile LAN peer, leaked camera credentials, revoked TV remaining authorized, source ID collision, conflicting edits while offline, replay, clock skew, TV software version mismatch, crash halfway through import, private LAN IP addresses in logs, unauthorized automatic playback. Use end-to-end encrypted authenticated envelope if LAN transport is untrusted; require manual consent for sharing URL/token-bearing content and never turn a local `content://` URI into remote reference.

Test with two isolated simulated state stores and a deliberately flaky transport: disconnect/reconnect; concurrent rename/delete; 100, 1000 messages; delayed/out-of-order replay; lost acknowledgement; coordinator power-off; app suspend; device factory reset; rotate secrets. Choose a **clear conflict policy**: request confirmation on conflicts or make only one controller authoritative for read-only target sessions. Measure eventual state/intent convergence, not frame sync. Then enlist an independent two-TV tester for E3; this evidence cannot be replaced by mocks.

## UX
Advanced → Integrations → Multi-TV → Pair / Connected TVs / Share options / Revoke; no Home card or mandatory setup. On guest TV, show transient prompt when remote command arrives; explicit “Do not disturb manual playback” setting. Offline TV continues own local playback.

## Stop conditions
Need for global account/server, permissions to broadcast private content implicitly, inability to verify identity/revocation, or resource cost that impairs the single-TV product. Treat as low priority until stable v1 and evidence of meaningful demand. No proof of compatibility or functionality claimed.