# OpenVista R&D v2 — Security/privacy/provider-rights review

**Status:** Proposed threat model, E1 desk review. Not a penetration test, legal opinion or source-license clearance. Must be evaluated against repo current privacy and release gates before product work.

## A. Data types and ownership

| Data | Current owner | Sensitivity | Future action |
|---|---|---|---|
| Source URI (may embed credentials/query tokens) | user LibraryStore | high | never place in reports/remote telemetry, redact logs |
| Saved camera metadata (city/coords) | user / M3U provider | privacy-sensitive | do not send to unrelated enrichment service without consent |
| Playback history and Camera Health | future app-local feature | behavioral | bounded retention, clear control, no upload |
| Network byte ledger | future app-local feature | usage analytics | no device-wide monitoring permissions, no cloud telemetry |
| SMB/WebDAV password | user | secret | Android Keystore-backed separate store, not plaintext library JSON |
| Home Assistant bearer token | user | high-power credential | least privilege, encrypted, revoke |
| Multi-TV shared pairing secret | two devices | authentication | scoped, rotating/revocable; never auto-share private camera URL |
| Google Photos OAuth/media | user/Google | extremely sensitive | only sanctioned scopes, cached media rights, unlink and purge |
| External source licensing | broadcaster | legal/contract | confirm actual user playback rights, not directory metadata |

## B. Specific attack chains requiring automated regression

**Source import as internal network probe:** A user subscription may include `http://127.0.0.1/admin` or link-local metadata endpoints. Import must be *pure parsing*, not eager preview/network fetch. If later allowed private LAN camera user selection, make source network policy explicit. Prevent DNS rebinding and dangerous redirects in any automatic downloader.

**Credential-bearing redirect:** A WebDAV or M3U fetch includes `Authorization` header; 302 to another host must not forward secret. Scheme downgrade HTTPS→HTTP requires denial unless separately approved. Test after retries and error handling.

**Phantom source deletion:** Provider returns empty 200 because of outage; auto-sync deletes all 500 cameras and their favorites. This is data integrity/security, not only UX. Three-way merge preview and crash-safe atomic write mandatory.

**Corrupt source/fault history:** Inability to play during user Wi-Fi outage marks every camera “bad,” causing Smart Director to skip everything. Health must separate source failures from environment failures; limited decay and manual override required.

**Personal photo exfiltration:** An optional cloud provider sends EXIF/face metadata into a public geocoding API or a diagnostics report. No automatic cross-provider data flow. Public build must not embed OAuth client secrets or use borrowed users' authentication cookies.

**LAN command hijack:** Unknown neighbor sends `play arbitrary URL` or deletes local media via an unprotected Home Assistant/multi-TV port. Only opaque **existing IDs** and authenticated, allowlisted commands. Temporary QR pairing for adding sources is still disabled and does not authorize permanent device control.

**Browser and YouTube shortcut:** A website embedded WebView with arbitrary JavaScript or downloaded stream links can execute untrusted navigation and violate platform terms. Official embeds must keep branding/ads; they cannot join unattended ad-free rotation. No extraction, proxy or hidden ad stripping.

**Public tile service abuse:** Bulk offline prefetch from OSM public tile service is not an appropriate unlicensed solution to map availability. The existing [ProviderPolicy.kt](../../../player/src/main/java/com/openvista/prototype/ambient/ProviderPolicy.kt) gates OSM map behavior; respect existing disabled provider and use local list fallback.

## C. Legal/provider evidence statuses (as of GitHub-only read)

- **Aerial Views:** project README displays GPLv3. Its SMB/WebDAV source cannot be casually copied into an MIT repo while ignoring license obligations. Architecture inspiration is distinct from code reuse.
- **Google Photos:** first-party Ambient, Picker, Library policies and Partner Program access need direct official verification and possible approval. GitHub-only does not prove eligibility; **HOLD**.
- **YouTube:** app's own PR #35 explicitly forbids ad bypass in its experiment. Current zero-ad ambient requirement means official embedded viewer cannot guarantee the product contract; **NO-GO** for background.
- **Live Environment Streams:** curator's `sources.json` labels 53/67 families “fair_use”. Fair use is **not a permission grant**; its `validate.py` checks HTTP response status for HLS and does not establish moving 1080p or rights. Every candidate source must be permission-cleared.
- **Windy/other webcam directories:** thumbnails, timelapses, geographic metadata and direct live stream rights are independent assets. “A site has 1,000 webcams” does not mean 1,000 user-permitted HLS 1080p streams.
- **Home Assistant:** official GitHub developer docs show Bearer-token REST and authenticated WebSocket; these are credentials not to log or share.

## D. Proposed data flow permission map

```text
User enters camera URI → Local parser → Local library → Media3 playback
                                    └→ Optional observed health (opaque ID only)
User opens Place Info → confirmed coordinates only → permitted place API → attributed display
User connects NAS → Android Keystore credentialRef → scoped LAN request → video-only reader
User connects HA → encrypted token + allowlisted events → only saved item IDs
User connects Google (if approved) → OAuth scoped account → user-selected assets → local optional display
User enables M3U refresh → approved URL → capped fetch / three-way merge → explicit review
App default → NO account, telemetry, bundled catalog, remote credential export or cloud sync
```

## E. Required security artifacts for later E2/E3

1. Repository manifest / permission diff; compare baseline internet and access-network-state permissions.
2. Secret scan of changed Kotlin and APK assets (full tool, not pattern heuristics).
3. URL redaction before DiagnosticJournal and any user-shared reports.
4. Dynamic hostile HTTP server tests for redirect loops, malicious HTML/XML, decompression bombs, oversized preview JPEG, token expiry and path traversal.
5. Pairing crypto protocol analysis with replay/key expiry/revocation, independent of current disabled security flag.
6. Credential storage migration/unlink/delete, factory reset, clock jumps and app restore.
7. Source provider terms/evidence per **individual sample**; copyright of video, webcam images, thumbnails, venue data, descriptions and audio loops separately.
8. Rate limiter / timeout / memory ceiling / backoff for every new network adapter.
9. Third-party dependency SBOM and license compatibility, especially SMB libraries and WebRTC implementations.
10. Manual permission/security review before public release; owner sign-off explicit.

## F. Hard stop policy

Any feature violating C-01 bundled-content boundary, D-67 without owner override, platform advertising terms, user credential secrecy, one-decoder reliability, or unauthorized always-on OS behavior gets **NO-GO or HOLD**, regardless of novelty or competitor prevalence. All data flow paths default Off and fail closed. 
