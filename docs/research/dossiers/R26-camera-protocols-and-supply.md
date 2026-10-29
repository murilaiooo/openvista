# R26 — Camera formats and actual useful global source supply

**Research verdict:** Existing HLS/DASH remain the priority; RTSP **GO to isolated user-owned camera proof**, snapshot JPEG/MJPEG **HOLD pending quality/usefulness evidence**, WHEP/WebRTC **HOLD pending supply+dependency evidence**, YouTube automatic **NO-GO**. Research **E1: source/document audit, no live-stream playback or licensing audit**. [Track #48](https://github.com/murilaiooo/openvista/issues/48).

## Selection question
“Add more formats” is not valuable if formats yield zero reliable scenic full-HD live cameras. Count **permissioned, real-time, independently playable sources**, not directory entries, URLs on GitHub, embeddable HTML pages, signed links requiring scraping, YouTube streams or static JPEGs. Every proposed addition must justify installation footprint, transport/codec support and Fire OS 7 resource cost.

## Existing code facts
[UserSourceParser.kt](../../../source-core/src/main/kotlin/com/openvista/sources/UserSourceParser.kt): `enum StreamKind { HLS, DASH }`; `detectKind` only recognizes compatible URL extensions; M3U max 500 entries, 1 MiB/10,000 lines. [PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) marks cameras as DASH else HLS media items, one Media3 ExoPlayer owner. [player/build.gradle.kts](../../../player/build.gradle.kts) references `media3-exoplayer`, `media3-exoplayer-hls`, `media3-exoplayer-dash` (v1.11.1) and `media3-ui`, **not** `media3-exoplayer-rtsp`. New URL schemes will not work just by changing extension heuristics or adding `<option>`.

## Actual GitHub market evidence, with honest limits

- [Live Environment Streams README](https://github.com/william-ricchiuti/Live-Environment-Streams): maintainer **claims** 5,997 records, 98 countries, 67 upstream families, 4,226 active and 4,229 directly usable; its “~2,929” filtered subset includes **HLS OR YouTube**, which is **not** a count of OpenVista-compatible legal streams. Its documentation admits ~1,750 SkylineWebcams entries are HTML pages with short-lived HLS tokens requiring page extraction, ~475 YouTube extraction cases are brittle, 3,614 location points are city estimates, and 1,771 have never been probed. **These are upstream claims, not independently validated statistics or permissions.** Data counts might change without notice.
- Same README source breakdown reports 1,684 SkylineWebcams, 1,168 US VDOT, 746 OpenCCTV, 404 Maryland SHA, 261 Delaware DOT and 252 NRW Autobahn entries. This indicates a *large traffic-camera skew* and many website-only records, not thousands of scenic high-quality streams. Its claimed country ranking includes US 2,210, Italy 828, Thailand 462, Germany 290, Korea 241, Spain 226, Japan 129. Most countries have far fewer candidate entries. **Do not import this dataset to production or distribute their URLs.**
- [iptv-org/iptv](https://github.com/iptv-org/iptv) aggregates public television channels, not a validated outdoor webcam catalog. Publicness is not rights clearance, and TV channels should not be counted as worldwide camera supply.
- [go2rtc](https://github.com/AlexxIT/go2rtc) documents RTSP camera ingest, HLS, MP4, WebRTC and many vendor protocols; demonstrates possible **gateway** interoperability, not a zero-cost protocol decoder on Android TV.
- [MediaMTX](https://github.com/bluenviron/mediamtx) documents streaming source conversion; its [WebRTC read notes](https://github.com/bluenviron/mediamtx/blob/main/docs/5-read/03-webrtc.md) explain WHEP endpoint semantics. Gateway use needs a self-hosted server and must remain optional, no backend hosted by OpenVista.
- [WHEP draft](https://github.com/wish-wg/webrtc-http-egress-protocol/blob/main/draft-ietf-wish-whep.md) specifies HTTP signaling for viewer WebRTC sessions; note that it is **egress/viewing**, not WHIP ingestion. A WHEP URL does not automatically work in Media3 HLS/DASH.

## Protocol-by-protocol implementation and supply assessment

| Type | Likely real useful source class | Current player readiness | Critical hidden dependency | Research outcome |
| --- | --- | --- | --- | --- |
| HLS (`.m3u8`) | Permitted provider live feeds, scenic cameras, user lists | **Existing**; measure rendition ladder, codec, age, auth | Token expiry, provider rights, discontinuities, variant stream bandwidth | Improve quality/recovery first |
| DASH (`.mpd`) | CDN-based live delivery / professional feeds | **Existing** | DRM, entitlement, location restrictions, fewer public direct webcam manifests | Maintain |
| RTSP / RTSPS | **Owner-controlled IP cameras/NVRs**, common in surveillance ecosystem | **Dependency missing**, possible Media3 module | Codec (often H264/H265), RTP transports, HTTP/RTSP auth, NAT, secure credentials | Best *functional* next candidate; not proof of global scenic supply |
| HTTP multipart MJPEG | Legacy webcams/private devices; unclear HD scenic supply | Not in existing ExoPlayer camera path | Custom parser, CPU JPEG decode/repaint, stale frame, high bitrate | Hold until 20 lawful examples |
| HTTP refreshed JPEG | Traffic cams, weather stations, image-based webcams; often not smooth | New **snapshot mode**, not video | Publisher refresh limits and timestamps; visible static frame | Different product type, not “live video” |
| WHEP / WebRTC | Low-latency broadcaster/server endpoints | Major native stack addition, not Media3 manifest | ICE/DTLS, NAT/TURN, codec negotiation, Android permissions, signal lifecycle | Hold until legal HD scene inventory proven |
| Progressive MP4/MKV/WebM | User-hosted/local landscape clips; mostly **not live cameras** | Video family may play per decoder/format | Range/seek and media-specific codec | Improve My Videos instead of claiming new live format |
| MPEG-TS over HTTP / raw UDP | Various transport/security feeds, not broad public webcams | Needs separate proof on device | Network requirements and parser support | Low priority |
| RTMP / SRT | Production **ingest** paths, rarely authorized viewer-facing cameras | Gateway/complex implementation | protocol libraries, public endpoint access, transcoding | Likely no-go for native ambient v1 |
| HTML page, iframe, YouTube | Many “cams” advertised only as pages | Not a direct source, WebView restrictions | Ads, consent, DRM, private APIs, changing tokens | No generic webpage playback |

## Supply proof method: reproducible without violating C-01

1. Enumerate **provider categories** first: municipal traffic cameras, national parks and visitor tourism sites, airport/harbor boards, ocean observatories, wildlife nonprofits, private NVR/ONVIF installations, public webcam directory listings.
2. For each, record **official broadcaster/provider terms** and allowed viewer access. **Public listing ≠ license**. Do not use extraction or anti-bot bypass.
3. Store *metadata only* in private research: provider name, country, scene class, protocol, stream type, auth/token, title/coords provenance, accessible HD evidence, live latency estimate, codec, average Mbps, night performance, first-frame time, tested date/time and license URL. **Do not commit camera URLs, passwords, playlist files, screenshots without rights or research fixtures to this public repo**.
4. A candidate qualifies for “useful OpenVista live camera” only if it is (a) legally playable via approved method, (b) actual moving video rather than a periodically updated still, (c) reliable for at least a 10-minute observation and longer repeat probe, (d) decoder-compatible 720p+ if advertised as HD, and (e) stable enough without stream extraction. Nighttime and location accuracy counted separately.
5. Report 3 counts per protocol: **discovered entries / legally testable / playback verified**, plus country/provider distribution. No extrapolation from single country to worldwide availability.
6. Test HLS/DASH first, then RTSP on user-owned lab camera, then MJPEG/snapshot **only if inventory demonstrates real user value**, finally WHEP after identifying sanctioned viewers/providers. Maintain country distribution as an acceptance metric, not just total list size.

**Research benchmark, not owner-approved v1 gate:** 20 genuinely permitted HD moving-video examples across 5 countries and 3 independent broadcasters to assert a new protocol **expands global scenic coverage**. For *personal RTSP cameras* a single authorized sample may justify narrow support, but that has a different value proposition.

## Decoder and security proof steps
Build isolated transport adapters behind explicit `StreamKind` support, never auto-interpret any unknown `http` URI as HLS. RTSP test on H264 720p/1080p with TCP/UDP transport where supported, Basic/Digest credential handling, IP changes, 10-minute startup/stall, ExoPlayer lifecycle, 24h soak, audio off. MJPEG cap dimensions/frame refresh and CPU; WHEP measure heap/startup/network reconnection and ICE failure. Verify credential-bearing URLs never reach overlay, logs, screenshots, backup or provider geocoding; no device-wide port scans. Respect `android:usesCleartextTraffic` and possible future tightening.

## Priority and decision table
1. **P0:** Audit current camera quality, availability, metadata and token expiry; make HLS/DASH experience stable. No additional format justified by count yet.
2. **P1:** RTSP personal-camera capability spike using official [AndroidX Media3 source](https://github.com/androidx/media), if its module supports exact chosen codec/transports.
3. **P1/P2:** Snapshot/MJPEG only if a clear separate, honest slideshow-type experience is desired and supply evidence exists.
4. **P2:** WHEP protocol + native WebRTC decoder/hardware budget only for sanctioned verified cameras.
5. **NO-GO:** ad-free guaranteed YouTube through official embeds; generic stream ripping, rehosting, bypassing ads, site scraping and unverifiable HD camera catalogs.

## What is **not** established by this research
No live URL was played, no country-by-country legal inventory was completed, none of the upstream README “active” counters were independently reproduced, no provider license was cleared, and no Fire OS 7 codec test was run. Those are explicit gates, not implied successes. Source provenance: GitHub README snapshots accessed Oct 28, 2026 via connector, not a permanent availability guarantee.

## Additional independent GitHub dataset audit: license labels and validity probes

An extra read of [sources.json](https://github.com/william-ricchiuti/Live-Environment-Streams/blob/main/sources.json) shows **67 declared source families** totaling **5,997 entries / 4,226 “active” entries** in the maintainer's static counts. Source-family self-reported `license_type` labels are: **53 `fair_use`**, **11 `public_domain`**, **1 `open_data`**, **2 `unknown`**. This is a **classification of families**, not a legal determination per stream. **`fair_use` is emphatically not a license grant to OpenVista or its users**, and a `public_domain` claim by a curator is not proof of a specific provider's authorization. No source or URL was adopted.

A source-code audit of [scripts/validate.py](https://github.com/william-ricchiuti/Live-Environment-Streams/blob/main/scripts/validate.py) finds that its `probe_hls` checks **`curl` HTTP response beginning with 2**, `probe_http` checks 2xx/3xx, and `probe_youtube` searches HTML for live markers. HTML page types are explicitly returned as unverified. Thus this **repository validator** does *not* prove the media decodes, contains moving video, has 720p+, has a valid live window, stays up for hours, or is legally licensed. The README separately claims more extensive ffprobe-based production validation; that private/other pipeline was **not independently inspected here**. Do not collapse these different evidence strengths into one “active” count.

**Practical consequence:** no format can pass OpenVista's source-supply gate from this dataset alone, and the 4,226/2,929/5,997 figures must not appear in user-facing camera counts or marketing. For every candidate require **original broadcaster terms, actual decoder evidence, resolution/FPS and reliability measurement**, not just HTTP status or a GitHub README. This stricter conclusion supersedes any optimistic reading of the first-pass numbers.
