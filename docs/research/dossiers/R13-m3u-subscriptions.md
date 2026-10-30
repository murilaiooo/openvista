# R13 — User-managed M3U subscriptions with safe updates

**Verdict:** GO for isolated parser/merge and authenticated fetch tests; **HOLD** production approval. Evidence E1 from OpenVista code. [Research #45](https://github.com/murilaiooo/openvista/issues/45).

## Concrete user value
People who maintain large permitted M3U lists should not have to reimport whenever the source owner rotates stream tokens or replaces dead URLs. An explicit saved **subscription** can refresh list metadata with consent and preserve user-customized names, favorites and playlist membership. It is not a catalog provided, curated or hosted by OpenVista.

## Current baseline and schema risk
[UserSourceParser.kt](../../../source-core/src/main/kotlin/com/openvista/sources/UserSourceParser.kt) recognizes **only HLS and DASH** source kinds, caps M3U input to 1 MiB, max 500 entries, max 10,000 lines and validates URLs. [LibraryStore.importCameraList](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) currently imports user-provided items into a playlist and matches on normalized URL; that is not sufficient stable identity for rotating signed URL tokens. [LibraryItem](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) stores URI, title, attributes and family, not subscription ownership/revision. A subscription must not silently overwrite manual user edits.

## Data model research
`Subscription { id, title, sanitizedHttpsLocationRef, credentialRef?, etag?, lastModified?, refreshPolicy, lastSuccess, error, enabled }`; `SubscriptionEntry { stableSourceId, subscriptionId, providerKey?, currentValidatedURI, originalMetadata, userOverrides, active/tombstoned, discoveredAt }`. Stable keys may come from a trustworthy `tvg-id` scoped to subscription + source host, with collision handling; fallback key from normalized URL path excluding ephemeral tokens requires caution (different cameras can share path). Unknown identity: prompt or create new, never blindly replace a favorite. Protect changes with an all-or-nothing diff preview and “Keep removed sources?” decision.

## Fetch threats and mitigation
Arbitrary user URL input poses SSRF, local loopback and metadata-service exposure if a list can embed remote addresses. Validate both subscription URL and each media source, restrict schemes to explicitly supported player types, sanitize redirects (same-origin or user approval), restrict response length, timeouts and decompression ratio, forbid recursive list loading, and never forward stored auth headers across origins. HTTPS required for remote subscription by default; local HTTP only via explicit informed LAN setting and Android cleartext review. Support ETag + Last-Modified, conditional 304, network/offline stale cache, capped retries with jitter and refresh only while UI/app is active unless user opts into allowed scheduled updates.

## Failure and merge experiments
Fixtures: 500 entries, 501, oversized file, UTF-8 BOM/invalid UTF-8, M3U EXTINF comma/title metadata, repeated URLs, multiple credentials, two titles same tvg-id, ID reused maliciously, source removal and return, reordering, 30% tokens changed, abort mid-write, power loss, 304/412/429/500 and CDN redirect. Assert all user overrides and favorite IDs remain stable or surface explicit conflict; never drop all library records because remote file became temporarily empty. Exactly one active import transaction and reversible diff, no source URL in diagnostics.

## UI
Cameras → Add → Import M3U file | **Subscribe to M3U URL**. Manage subscription: Last checked, Changes pending, Compare, Refresh now, Pause, Remove subscription (keep cameras by default). Settings → Library → Connection behavior only; the main import action belongs in Cameras.

## Rejection criteria
Reject unbounded periodic background fetching, implicit credential storage in JSON, cross-origin redirects leaking keys, duplicating favorite cameras, 10k feeds that overwhelm D-pad navigation, claims the feature adds cameras automatically or violates source rights. Real connectivity tests and security review needed before GO. Existing user-owned-source policy: [C-01](../../product/user-managed-content.md).