# R19 deep investigation — Home Assistant integration with protocol-level evidence

**Evidence:** E1 official Home Assistant developer documentation *hosted in its own GitHub repository*, and OpenVista pairing source. **Verdict:** HOLD until permission review and independent HA tester. [Original dossier](../dossiers/R19-home-assistant.md).

## 1. Official protocol evidence (not marketing)

- [Home Assistant WebSocket API](https://github.com/home-assistant/developers.home-assistant/blob/master/docs/api/websocket.md) documents endpoint `/api/websocket`, initial `auth_required`, client `auth` with `access_token`, successful `auth_ok`, followed by numbered JSON requests/results. This is a **TV-as-client** path to a Home Assistant server, not a server automatically controlling the TV.
- [Home Assistant REST API](https://github.com/home-assistant/developers.home-assistant/blob/master/docs/api/rest.md) uses JSON, `Authorization: Bearer TOKEN` on API calls. Tokens are sensitive; logging or putting them in URL query parameters is unacceptable.
- [Aerial Views message overlay](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/ui/settings/OverlaysMessageApiFragment.kt) demonstrates a TV LAN HTTP endpoint pattern, but its existence is not authentication/security evidence.
- OpenVista [PairingCoordinator.kt](../../../player/src/main/java/com/openvista/prototype/pairing/PairingCoordinator.kt) has `PairingSecurity.REVIEWED=false`, a 120-second pairing session and a dedicated short-lived local listener. That gate **must not** be reused to open a permanent unauthenticated HA endpoint.

## 2. Three architectures (pick one, not all)

| Option | Direction | Secrets where? | Persistent resource | Problem |
|---|---|---|---|---|
| TV as WebSocket client to HA | TV → HA | scoped HA token stored on TV | active socket | HA must know/emit events; TV could be asleep |
| HA as HTTP client to TV | HA → TV | per-device shared secret | TV listener | LAN attack surface, OS background policy |
| Explicit deep link / launcher intent | Android app → TV | possibly none | none while idle | limited commands/permissions/focus |

**Recommended study:** start with TV-initiated, **foreground-only WebSocket client** to a user-configured HA instance. This avoids a TV HTTP server but requires the owner to supply an HA token, verify TLS and consent to event subscriptions. Narrow permitted commands: play known saved camera, select known playlist, switch visual preset, pause. No raw URL import, filesystem access, camera credentials, arbitrary shell, OS power or rooting.

## 3. WebSocket message lifecycle (research contract)

```text
Disabled by default
User chooses Integrations → Home Assistant → Connect
TV opens configured secure connection only while app active
HA sends auth_required
TV sends auth(token), verify auth_ok (else revoke/reauthorize)
Subscribe only to allowlisted event/command channel
Receive event(id, command, savedSourceId, nonce)
Validate auth/session/replay, rate limits, sourceId ownership
Queue command on main thread if Player active and not manually overridden
On app background/Dream suspend → close or use explicitly approved lifecycle
On revoke → close socket + delete HA token + clear command queue
```

REST endpoints and event semantics must be chosen from **real HA developer docs**, not invented `playSavedCamera` support as if built-in HA understands it. That name is a proposed OpenVista-internal command; a real HA integration likely needs an HA automation/action mapping and device entity definition. First E2 proof should only connect/authenticate and receive a harmless mock event with correct request IDs.

## 4. Threat model and attack table

1. A guest Wi-Fi client sends forged commands → must fail; “LAN is trusted” is invalid.
2. Lost HA token appears in diagnostic file → forbidden; logs sanitize before persistence.
3. Valid but replayed command after 24h → nonce/session or monotonically increasing sequence rejects.
4. Command for deleted private camera ID → reject, no fallback to arbitrary URL.
5. User manually presses Next while HA event arrives → defined manual priority, no flicker loop.
6. HA disconnects/restarts → backoff and visible disconnected state, no busy loop.
7. TLS certificate failure → no “disable SSL checks” automatic fallback.
8. Automation storm 1000 events/min → bounded queue, per-command throttle, never ANR.
9. Device revoked → immediate token deletion/connection teardown.
10. TV sleeps → no undocumented background privileges or wakelock required.
11. Source entry includes credentials → never expose to HA by default.
12. Foreign site triggers local browser request → no unauthenticated TV HTTP endpoint.
13. HA token grants broad actions → request/advise least possible scope; current HA token granularity must be verified.
14. App update changes protocol version → capability negotiation and explicit unsupported result.

## 5. Independent validation without user's HA equipment

Build a **mock HA WebSocket** implementing exactly `auth_required` / `auth_ok` / numbered responses using synthetic saved IDs; test authentication, permission denials, disconnect and command mapping. This is E2 only. Then recruit independent HA instance + Fire OS 7 device for E3; record Home Assistant version, TLS, time, network partition, CPU/RAM and video continuity. Owner cannot test HA and must **not be asked to confirm capability**.

## 6. UI and acceptance

Advanced → Integrations → HA, disabled default. Connect includes clear server address, local disclosure, state, revoke. Never add an HA Home tile or allow automations to turn on TV. User can switch to “Ignore remote requests during manual viewing”. **NO-GO if integration requires a permanently exposed unauthenticated LAN listener, secrets in library JSON, or disrupting offline playback.**
