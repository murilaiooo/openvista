# R12 deep investigation — Network media (WebDAV and SMB)

**E1:** competitor source and OpenVista storage/player analysis. **Decision:** WebDAV isolated prototype first; SMB later only if dependencies, security and hardware results justify it. [Prior dossier](../dossiers/R12-network-library.md).

## 1. Real precedent with actual code, not generic product claims

[Aerial Views](https://github.com/theothernt/AerialViews) implements
- [SambaHelper.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/data/network/SambaHelper.kt), importing SMBJ `SMBClient`, `SMB2Dialect`, `AuthenticationContext`, not Android's SAF.
- [OkHttpWebDavClient.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/providers/webdav/OkHttpWebDavClient.kt), using PROPFIND `Depth: 1` with Basic auth.
- [WebDavDataSource.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/providers/webdav/WebDavDataSource.kt), implementing Media3 `BaseDataSource` and HTTP `Range` using `DataSpec.position`, parsing `Content-Range`.
- [WebDAV contract tests](https://github.com/theothernt/AerialViews/tree/master/app/src/test/java/com/neilturner/aerialviews/providers/webdav).

This proves that Android TV support is plausible **for that other app**, not that Fire OS 7 can decode a specific NAS 4K HEVC file or that the GPLv3 source can be copied into MIT OpenVista. Study the seam; write independently.

## 2. Existing OpenVista model and what would break

[LibraryStore.kt](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) stores `VideoAccessRoute`, SAF canonical identities and folder grants, all protected by a full-state AtomicFile and byte budget. A remote SMB path is not a `content://` SAF grant. A network record needs connection ID, opaque stable remote media ID, relative path, server ETag/version and credential reference. File rename or auth rotation should not irreversibly delete saved favorites/checkpoints. No plaintext password or credential-bearing URI in `openvista-library.json`.

[PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) currently constructs a `MediaItem` by URI; default DataSource cannot be assumed to interpret SMB. Proper Media3 `DataSource.Factory` and transport-specific cancellation are necessary. Playback already distinguishes local vs live recovery; network file is VIDEO family but may have network failure semantics different from removable USB. A lost NAS connection should not be mistaken for a bad video codec.

## 3. WebDAV first: robust seek contract

1. User explicitly enters HTTPS URL + scoped credentials, chooses only a folder; no arbitrary LAN crawl.
2. PROPFIND Depth:1 with secure XML parser (no external entities/DTD), size and entry limits, 207 multistatus handling.
3. GET with `Range: bytes=N-`; validate 206 start/end, total length and ETag before reusing checkpoint. If server returns 200 to a nonzero range, **do not discard gigabytes**: disable seek or reopen with a clear message.
4. 401/403 auth: request reauth without treating media as permanently dead. 404: missing file. 416: invalid seek/changed file. 429/503: capped retry.
5. Validate HTTP redirects at each hop and drop Authorization if origin changes. No silent TLS bypass; block unintended loopback/metadata endpoints as relevant. Local HTTP is a separately gated informed setting.
6. Close socket on Back, suspension, Dream handoff and player release; no lingering SMB connection waiting for network.
7. Bound file listing: 100–200 records/page, no eager recursive scan for thousands of videos, proper path encoding.

## 4. SMB adapter risk and contrast

SMB2/3 can offer native read/seek semantics; SMB1 must not silently negotiate. Audit SMBJ dependency and transitive licensing, Android API 28 compatibility, APK size, memory, signing, permission and timeouts. Guest access insecure; evaluate explicit user consent. TCP 445 often blocked on guest Wi-Fi or client-isolated networks. A separate home server (MediaMTX/go2rtc/Jellyfin) can proxy, but adds setup and is not OpenVista default. Windows share may be sleeping, unmounted or require domain; distinguish “Unavailable” vs “Wrong password”.

## 5. Credentials and local privacy

App has `android:allowBackup=false`; that's not encryption. If storing NAS/RTSP secrets, use Android Keystore-backed local credential store (behavior on API28 and device reset must be tested), with opaque IDs referenced by library items. Reject secret-bearing analytics, logging, screenshots, XML errors and user-visible debug. Revocation deletes local secrets/caches, never deletes server originals.

## 6. Falsifiable test table

| Test | Fixture | Required behavior |
|---|---|---|
| DAV-01 | valid Depth=1 PROPFIND 207 | bounded directory rows |
| DAV-02 | 206 with matching Content-Range | exact seek |
| DAV-03 | 200 despite nonzero Range | disable seek, no O(size) discard |
| DAV-04 | mismatched ETag after resume | detect changed content |
| DAV-05 | 416 and 404 | graceful recovery, not corruption |
| DAV-06 | 401 midstream | reauthorization path, redact secret |
| DAV-07 | redirect to attacker domain | no credential forwarding |
| DAV-08 | malicious XML entity/billion laughs | parser refuses before external fetch |
| DAV-09 | 4GiB file, seek at 3GiB | `Long` safe, bounded memory |
| DAV-10 | 50k files in share | pagination, stop recursion |
| SMB-01 | SMB1-only NAS | refused with explanation |
| SMB-02 | SMB2/SMB3/guest/domain | policy and auth matrix |
| SMB-03 | NAS reboot in 4K play | no blocked main thread |
| SMB-04 | file renamed/replaced | identity conflict visible |
| SMB-05 | user presses Back while TCP hang | prompt release, no ANR |
| TV-01 | 4K HEVC at 30/60 fps | actual decoder metrics, not assumption |

Record start-to-first-frame, sustained read speed, seeking accuracy, peak heap, decoder dropped frames, cancellation latency, APK delta and 24h stability. **Not yet tested.**

## 7. UI and kill conditions

My Videos → Add source → WebDAV or SMB → Test connection → Browse root → Select folder → Save. Settings → Library → Manage connections. Saving connection must not import everything in background. When offline, list displays last known metadata without falsely claiming playable.

**Stop/HOLD** if a transport requires TLS validation disabled, GPL code copying without compliance, secret storage cannot meet threat model, connection consumes too much CPU or user has a safer simpler local-file solution. R12 does not authorize a content-hosting backend.
