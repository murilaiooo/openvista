# R13 deep investigation — M3U subscription identity, three-way merge and security

**E1:** direct OpenVista parser/storage inspection. **Decision:** local merge prototype **before** remote HTTP fetching. [Original dossier](../dossiers/R13-m3u-subscriptions.md).

## 1. Precise current gap

[UserSourceParser.kt](../../../source-core/src/main/kotlin/com/openvista/sources/UserSourceParser.kt) parses HLS/DASH only, limits M3U input to **1MiB, 500 entries, 10,000 lines**, and recognizes source metadata attributes. [LibraryStore.importCameraList](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) maps known cameras by **exact URL**, creates a new playlist ID on each import and imports items atomically. It **does not** track subscription origin, last accepted remote version, local overrides, HTTP ETag, or conflicts. Re-importing signed URL `/cam.m3u8?token=A` as `?token=B` can create a duplicate with a new ID. It is unsafe to call “download list periodically” a solved subscription feature.

## 2. User visible behavior specification

User enters a permitted list URL; OpenVista fetches only by explicit user action or approved refresh policy; shows changes **before** accepting; keeps favorites, personal names, order, manually added cameras and current playback; highlights missing/changed records. Removing subscription **keeps imported saved cameras by default**, unless user consciously chooses cleanup. An empty feed, 401, 500 or malformed response does not erase saved cameras.

## 3. Three-way data model

B = last accepted provider baseline, R = new provider response, L = current user edits:

```text
subscriptionId : UUID
providerStableKey : scoped tvg-id if trustworthy; else persisted mapping
savedItemId : canonical OpenVista library UUID
remoteFields : provider title, URI, category, coords, group, order
userOverrides : title, favorite, category, coordinates, order
Merge(B,R,L) => Added | Updated | Unchanged | RemovedUpstream | Conflict
Only CommitAcceptedDiff may write LibraryStore
```

Avoid generic URL canonicalization: `?camera=1` and `?camera=2` can be different feeds, while `?token=abc` may rotate for the same camera. Never discard every query parameter to “solve” deduplication; use stable provider IDs scoped to subscription and careful conflict checks. Duplicate `tvg-id` within one list is ambiguity, not proof of same camera.

## 4. Adversarial examples

- B has `tvg-id=waterfront`, URI token A; R token B; L has favorite=true/custom title: **only URI updates**; ID, favorite and custom title survive.
- B named “Beach”; R renames “Sunrise Beach”; L unchanged: new provider title adopted.
- Same B and R; L has local renamed “My Beach”: keep override.
- B/R same tvg-id shared by **two different camera URLs**: conflict with explicit user resolution.
- Provider removes 400/500 entries: hold removal by default, show suspicious mass delete.
- User removed a camera deliberately, R retains it: respect local tombstone, no resurrection by refresh.
- Provider changes city to a namesake abroad: do not overwrite confirmed coordinates.
- Remote list reordered, user manually organized library: preserve local order by default.

## 5. Security: importing untrusted network addresses

Untrusted M3U can contain URLs to loopback/metadata/control devices. Parsing must **never** trigger probing/fetching/thumbnail loads; importing is pure text processing. A later user-selected playback of a private LAN URL is a separately reviewed use case. Subscription fetcher needs URL and redirect policy, response/decompression limits, DNS rebinding awareness, HTTPS default, no credentials forwarded cross-origin, ETag, 304, 429/503 backoff and token revocation. `#EXTINF` fields are untrusted strings, not HTML to render. Logo URLs should not auto-download. Never build a GitHub-hosted first-party list.

## 6. Storage/migration and atomicity

Current library JSON has byte cap and `AtomicFile` transaction. Storing both full previous and current remote lists inside it might exceed cap. Compare (A) bounded metadata added to existing state, one atomic transaction, versus (B) separate subscription provenance store with coordinated recovery (harder). A crash simulation **between download and write** must prove existing favorites remain. Maintain old saved schema readable or use controlled schema version upgrade with fixture from previous version. Secret tokens/credentials are not persisted inside plaintext URI without explicit security design. Distinguish source URL containing credential from the source's identity; user-controlled list can still be remote credential-bearing.

## 7. Test vectors

| ID | Condition | Required outcome |
|---|---|---|
| SUB-01 | unchanged 500-entry list | exact idempotence |
| SUB-02 | URL query token changes | same saved ID |
| SUB-03 | duplicate tvg-id different content | conflict, no merge |
| SUB-04 | provider deletes a favorited camera | keep favorite/orphan status |
| SUB-05 | locally renamed item and provider rename | local override wins |
| SUB-06 | user deleted item then refresh | no silent resurrection |
| SUB-07 | empty provider list / 500 error | preserve prior state |
| SUB-08 | 501 entries or >1MiB | rejection with old state safe |
| SUB-09 | 304 Not Modified | no write, no changes |
| SUB-10 | redirect across origin with auth | credentials not forwarded |
| SUB-11 | 100 concurrent refresh callbacks | one transaction/lock, no duplicate playlist |
| SUB-12 | user edits during fetch | merge uses newest local revision |
| SUB-13 | network dies after headers | no partial commit |
| SUB-14 | two subscriptions claim same saved camera | explicit conflict/provenance |
| SUB-15 | malicious titles, newlines, XML | sanitized UI; never executed |
| SUB-16 | privacy clear/delete subscription | secrets removed, source persistence policy observed |

## 8. UI and decision

Cameras → Add → Subscribe M3U → Preview diff (“8 added; 3 changed; 2 missing; 1 conflict”) → Accept. Library → Subscriptions → refresh/pause/remove; show last success and errors, not silently update. E2 requires offline merge fixture success **before** any HTTP subscription. **HOLD production** until stable ID preservation, storage rollback and network security proven, and explicit owner approval. No test executed today.
