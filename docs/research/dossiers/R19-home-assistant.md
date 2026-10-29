# R19 — Home Assistant integration: safe optional LAN scene control

**Verdict:** HOLD. The owner cannot test Home Assistant; no physical HA server or TV integration was tested. E1 competitor code verified on GitHub. [Research #47](https://github.com/murilaiooo/openvista/issues/47).

## User benefit and boundaries
An advanced home-automation user wants to start a saved source, switch to a named playlist or change a visual preset through their local automation. Ordinary users should never need HA, a server or an account. Commands must not override personal TV controls without explicit opt-in. The OpenVista product **remains free, account-free and local-first**.

## Evidence
[Aerial Views README](https://github.com/theothernt/AerialViews) advertises a LAN message overlay interface usable with Home Assistant. [OverlaysMessageApiFragment.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/ui/settings/OverlaysMessageApiFragment.kt) exposes a configured local address/port; existence demonstrates integration precedent, **not** an acceptable security model for OpenVista. OpenVista's own [PairingCoordinator.kt](../../../player/src/main/java/com/openvista/prototype/pairing/PairingCoordinator.kt) has `PairingSecurity.REVIEWED = false` and should remain gated; do not repurpose its expiring pairing secret for an unrestricted control server.

## Integration alternatives
**A. Opt-in HA → TV HTTP commands:** clear commands, but TV listener may be reachable by hostile LAN peers; require pairing, per-device keys and revocation, origin/CSRF defenses and network scope. **B. TV → HA WebSocket client:** no public listener, but requires HA URL/token and a persistent socket; adds battery/lifecycle/auth complexity. **C. Deep linking through existing Android intents:** lower networking exposure but limited HA reachability. **D. MQTT broker:** not first phase, adds broker credentials/maintenance and trust surface.

Preferred **research experiment**: inbound local-only service after visible two-sided pairing, narrow allowlisted operations, HTTPS or authenticated signed requests with replay protection. Evaluate *local network not automatically trusted*; Android Fire OS policies and TV sleep may prevent permanent service. Avoid permanent background service if use-case works only while OpenVista foreground.

## Command policy
Allow read-only state queries and commands `playSavedCamera(id)`, `playSavedPlaylist(id)`, `setAmbientPreset(name)`, `pauseAmbient()` only for existing IDs, and only when owner-enabled. Never allow raw URL imports, filesystem read/write, OS settings changes, executable commands, source credentials, arbitrary browser navigation or remote screenshot. Rate-limit and log only redacted event type/time, optionally off entirely. Provide user confirmation for pairing and visible “Connected devices” with **Revoke all**.

## Threat and test matrix
Unauthenticated request, stolen token, replayed nonce, clock skew, denied network, compromised guest Wi-Fi, cross-origin web page on LAN, user switches Wi-Fi, stale app version, revoked device, command from unknown host, 100 requests/s, token leaked in debug log, source removed, TV goes to sleep. Use mock HA + fake HTTP client for E2; simulate malicious traffic, test no privileged action. **Independent HA deployment and physical TV** must later confirm E3; do not ask owner to test.

## Navigation
Advanced → Integrations → Home Assistant (disabled/default Off) → Pair/Status/Permissions/Revoke. Player would display only a transient action confirmation when an authorized automation changes the content, with an option to suppress remote commands during manual use.

## Non-goals
No user data in Home Assistant cloud by default, no uncontrolled API, no remote access outside LAN, no forced token export, no automatic trigger based on TV user's precise location. Compare relevant upstream software: [Home Assistant Core](https://github.com/home-assistant/core), [Aerial Views](https://github.com/theothernt/AerialViews), but external HA REST/WebSocket policy documents need separate current verification before implementation.