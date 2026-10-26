# User-provided source input: first Kotlin parser contract

**Scope:** preliminary source-validation module for issue #3. This is a reusable, tested parser, **not** a completed TV import screen, playback proof or public-release feature.

## Input routes and user experience

- The user may enter an HLS or MPEG-DASH URL using the TV keyboard and explicitly select its protocol. Signed/opaque endpoints without a filename extension require explicit protocol selection.
- The user may choose a local M3U list file from internal storage or USB using Android `ACTION_OPEN_DOCUMENT`, where the OEM provider exposes it. The TV application must request permission through the system picker and supply the opened stream to `UserSourceParser.parseM3u(InputStream)`.
- The video folder path is a separate `ACTION_OPEN_DOCUMENT_TREE` operation under #9. Choosing an M3U file is **not** permission to traverse adjacent files or folders.
- A clean Cameras library presents **Add a stream**, **Import a list**, and **Play my videos**. Always permit manual URL entry even if DocumentsUI/USB is unavailable. Nothing is prepopulated.
- A location, city, country, coordinates, favorites and categories are optional **user data**, not inferred from source hostnames. No availability or playable status is invented.
- In offline/error states, show saved user metadata and a retry path; play local fallback media only when the owner enabled it. No automatic catalog fetch, server, account or telemetry.

## Parser semantics (first tested implementation)

| Behavior | Bound or rule |
| --- | --- |
| Direct URL | HTTP(S) absolute URI with hostname, no whitespace, control characters or embedded `user:password@` credentials |
| URL maximum | 4,096 characters |
| Format on manual entry | Explicit HLS or DASH; URL extension is not required |
| Format in M3U list | `.m3u8` maps to HLS, `.mpd` maps to DASH; a signed URL may contain query parameters |
| List bytes | Up to 1,048,576 bytes of strict UTF-8, optionally prefixed with BOM |
| List header | `#EXTM3U` with optional metadata after one space; reject missing/invalid header |
| HLS media manifest | Any `#EXT-X-` tag marks a manifest, rejected as `HLS_MANIFEST`, not imported as an IPTV list |
| Entry count | At most 500 non-comment URL lines |
| Line count | At most 10,000 lines |
| Title length | Up to 100 characters, control characters removed |
| Titles | Optional label after the last comma in `#EXTINF`, otherwise generic **Stream N** |
| Duplicates | Discard exact repeated URLs; preserve the first entry |
| Invalid entries | Skip unsupported schemes, invalid/ambiguous URLs; return aggregate counts, not private URLs |
| No playable entries | Return a generic failure status, never an empty successful import |
| Read failures | Close the supplied stream and return a generic error, without leaking paths or provider exception messages |

This first slice accepts **only user-provided HLS/DASH stream entries**, not RTSP, embedded HLS master-playlist variants, arbitrary MP4 downloads, remotely fetched M3U lists or IPTV protocols unrelated to the approved HLS/DASH target.

The parser itself performs **no networking, disk access, permission grants, persistence, logging, or device operations**. It only validates an in-memory URL or byte array, or consumes a caller-supplied, bounded input stream. The caller will manage URI grants and saved sources in the later Android Activity. Credentials in query strings must remain private, and imported URLs must never be logged verbatim.

## Verification and remaining proof

```powershell
cd I:\codex\openvista
.\gradlew.bat :source-core:test --offline --no-daemon
```

The JUnit suite covers signed URLs, protocol detection, M3U metadata, duplicate handling, invalid schemes, size limits, strict UTF-8, BOM, HLS manifest rejection, unknown endpoints and bounded stream reads/errors. This is a JVM test seam, **not** a physical Fire TV network or USB demonstration.

Still needed before closing #3: a real fullscreen TV Activity that shows the empty state, user-directed keyboard entry, `ACTION_OPEN_DOCUMENT` file selection, mapping error states to UI, safe local persistence, and separate Media3 playback verification under #5. The Fire OS picker accessibility and USB grants remain #9 work. No test M3U or stream URL is bundled in any release asset, per C-01.
