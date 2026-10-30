# R23 deep investigation — Multi-TV sync, conflicting product scope and distributed-system behavior

**Evidence:** E1 analysis; no two physical TVs or LAN paired. **Decision:** HOLD. Multi-TV metadata backup/sync conflicts with current v1 D-67 deferral, so needs an explicit *later product decision* even if technically possible. [Prior dossier](../dossiers/R23-multi-tv-sync.md).

## 1. Define the feature honestly: four distinct products

1. **Scene-command mirroring:** TV A chooses saved camera ID, sends “play ID” to TV B, which must already have source. Low complexity, but needs auth/network and local control.
2. **Playlist sync:** TV A shares source identifiers and order, TV B merges. High **privacy and data-loss** risk because URLs can include camera credentials.
3. **Visual preset sync:** share opacity, clock layout, theme; lower content privacy but conflicting app versions and device geometry.
4. **Frame-perfect synchronized video walls:** requires timestamp-aligned media, NTP drift, live-edge consistency, synchronized decoding/display refresh and latency control. It is a **different architecture**; reject for now.

[Approved v1 acceptance](../../product/v1-acceptance.md) states D-67 defers backup/export/import of settings and playlists. R23's versions 2–3 **cannot be presumed approved**. Only optional command mirroring can be investigated as research, not a shipped product.

## 2. Actual OpenVista state model

[LibraryStore.kt](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) persists user source IDs, URLs, playlists and settings in single local JSON; [PlaybackSnapshot.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSnapshot.kt) persists queue, recovery attempts and current ID for **one device**. A `content://` SAF URI on TV A cannot be opened on TV B; nor can a USB path or local decoder capability. Do not copy checkpoint “positionMs” to a different file solely because title matches. Each TV remains independently playable and has its own fallback and active Dream lifecycle.

## 3. Architecture choices

**A. Controller/follower with scene intent only (recommended research):** paired device IDs, signed per-message command to play only preexisting saved source IDs. B responds MissingSource, Denied, Busy, OK. No camera URL transfer. Easier conflict semantics; no automatic library copying.
**B. Peer-to-peer shared library:** needs offline merge/CRDT or authoritative owner and privacy consent; large scope. HOLD.
**C. One self-hosted local relay:** avoids TV-to-TV port routing, but adds a server to a no-server product.
**D. Public cloud account:** contradicts default free/no-account stance; reject unless owner explicitly revisits it.

No LAN transport is automatically trusted. [PairingSecurity.REVIEWED=false](../../../player/src/main/java/com/openvista/prototype/pairing/PairingCoordinator.kt) already blocks the user-source pairing listener; a persistent multi-TV listener needs independent security review and user-confirmed enrollment on **both** devices.

## 4. A minimal protocol proposal

```json
{
  "protocol": 1,
  "peerId": "opaque-peer-id",
  "session": "rotating-session-id",
  "messageId": 7,
  "sentAtMs": 123,
  "intent": {"type": "PLAY_EXISTING", "savedItemId": "opaque-id"},
  "auth": "authenticated-envelope"
}
```

This is a **proposed shape**, not a working implementation. Do not treat `sentAtMs` as a globally synchronized clock or rely solely on timestamp to prevent replay. A receiver needs a bounded replay window, idempotent message ID/ack, peer authorization, pairing confirmation, current manual-override policy and revocation. A command should be rejected if the source is unknown; do not fetch a remote URL contained in the payload. If user manually changes TV B, locally chosen content should take precedence until they approve remote control again.

## 5. Partition and attacker tests

| Situation | Expected |
|---|---|
| one TV loses Wi-Fi | each continues local playback |
| sender sends same command 10 times | idempotent effect, no 10 restarts |
| message arrives 15 min late | stale command rejected or user-approved |
| two controllers race | simple single-controller rule, not flicker |
| receiver sees unknown source ID | MissingSource; no auto-import |
| revoked TV attempts command | rejected; no stale pairing |
| attacker on guest LAN guesses peer ID | fails authentication |
| TV reboots while command queued | no implicit replay of old request |
| two devices on different app versions | explicit protocol downgrade or unsupported |
| USB media on sender | never send unusable local URI |
| favorite source URL includes token | never transmit under command-only model |
| TV starts Dream while another scene playing | single ExoPlayer preserved |
| 1000 commands/min hostile peer | rate limit and bounded queue |
| clock drift 2 min | no need for synchronized frames in command-only mode |

E2 can use two simulated Kotlin instances and a mock authenticated message transport with drop/duplicate/reorder/replay. E3 **requires two actual TVs and an independent authorized tester**; the user cannot test this and should not be asked. A device-control service on Fire OS may be suspended in background; test actual lifecycle before claiming remote control while screen off.

## 6. UX and hard blockers

Advanced → Integrations → Multi-TV → Pair via separate short-lived code + explicit approval → Connected devices → Who controls this TV? → Revoke. No Home card, no mandatory startup. Default Off. Add no library sync, cloud sync or remote-control listener without distinct approvals. **HOLD** if two physical devices unavailable, D-67 unresolved, secrets/URLs might cross network, app would require background wakelock, or video sync is misrepresented as frame-perfect.
