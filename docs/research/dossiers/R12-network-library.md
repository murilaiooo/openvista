# R12 — Network Library: permissioned personal media over SMB and WebDAV

**Verdict:** GO for separate protocol/decoder proof, HOLD inclusion. Evidence **E1 verified competitor implementation** on GitHub, not yet an OpenVista experiment. [Research #45](https://github.com/murilaiooo/openvista/issues/45).

## User jobs and realistic scope
A TV with limited storage should play personal videos from a computer/NAS without copying to USB. Add a read-only network folder, browse, select video playlists, stream with range/seek, resume locally, preserve favorite IDs when server is temporarily offline, and disconnect credentials. **Never auto-discover every LAN share, crawl folders without consent or upload source addresses.**

## Baseline and competitor proof
OpenVista [SafVideoScanner](../../../player/src/main/java/com/openvista/prototype/SafVideoScanner.kt) / [LibraryStore](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) support user-granted local SAF files and access routes, but not network connectors. [PlaybackSession](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) can stream URIs through Media3 once supported data sources are provided.

**Real GitHub reference:** [theothernt/AerialViews](https://github.com/theothernt/AerialViews) README explicitly lists Samba and WebDAV. Relevant source files: [SambaHelper.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/data/network/SambaHelper.kt), [OkHttpWebDavClient.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/providers/webdav/OkHttpWebDavClient.kt), [WebDavDataSource.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/providers/webdav/WebDavDataSource.kt), [WebDavMediaProviderHttpTest.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/test/java/com/neilturner/aerialviews/providers/webdav/WebDavMediaProviderHttpTest.kt). Their existence verifies feasibility for some Android TVs, not performance compatibility or permission to copy their implementation (GPLv3 vs OpenVista MIT).

## Architecture options
- **HTTPS WebDAV first:** authenticated PROPFIND directory enumeration (minimal XML namespace parser; reject XML external entities), HTTP GET with byte Range and If-Range, streaming/retry/timeouts, TLS CA validation and user-approved hostname, redirects constrained to original trusted origin. Avoid disabling certificate checks.
- **SMB2/SMB3:** Java/Android SMB library evaluated for APK size/licensing/Android 9 compatibility, guest disabled by default, local path canonicalization, bounded recursive directory listing, optional direct TCP 445 with DNS rebinding precautions. Exclude SMB1.
- **HTTP directory or basic file URL:** little configuration but often lacks seek/metadata and access control; not replacement for WebDAV.
- **Media bridge (e.g. Jellyfin/Immich):** another ecosystem/integration, not generic NAS requirement.

Favor a narrow `NetworkVideoProvider.list(folder, cursor)` and `openVideo(item, byteRange)` seam behind existing library UI, with explicit credential/session lifetime. The stream adapter must handle seeks, media sniffing and failure classification. Persist source provider ID + stable remote file identifier; do not serialize passwords into `LibraryStore` JSON or expose credential-bearing URIs. Hardware-decoder capability remains a device concern regardless of transport.

## Independent verification
Controlled local test server with 200 items, 2GB+ MP4/MKV, 4K HEVC/H.264, tiny ranges, 206/200/416 responses, no Accept-Ranges, chunked transfer, 401/403 and digest challenge, host change, slow read, Wi-Fi flaps, file rename, revoked rights, duplicate files and server restart. Measure seek correctness, first frame, memory, throughput, dropped frames and leak-free cancellation. For SMB also test file lock, unusual Unicode paths and mixed SMB dialects. Privacy: user must initiate LAN connection, no default discovery or device port scanning.

**Important licensing risk:** Aerial Views GPLv3 source can be studied but should not be copied into an MIT distributed codebase without compatible compliance review. Use independently authored adapters or compatible licensed libraries. No remote test or physical TV test executed.

## UX and gate
My Videos → **Add source** → Storage/USB | Network folder → WebDAV / SMB. Saved folder list shows Connected / Unavailable / Credentials needed; no blank library on network loss. Settings → Library → Manage connections and remove secret. GO only if a real TV independently proves smooth 1080p/4K and persistent identifier after refresh without exposing LAN credentials.