# R26 deep investigation — Protocol support only when lawful HD cameras actually exist

**Evidence:** E1 primary GitHub source + vendor code, **no camera playback, no rights clearance**. **Decision:** E2 RTSP pilot on authorized lab source; improve HLS/DASH baseline; MJPEG/snapshot and WHEP on HOLD pending legitimate supply. [Original dossier](../dossiers/R26-camera-protocols-and-supply.md).

## 1. Problem statement and correct denominator

Adding 10 transport types is worthless if the user can access 0 more **legal, stable HD scenic live cameras**. Measure:
- **Discovered entry:** URL or source family exists.
- **Accessible:** official provider allows end user viewing and link actually responds.
- **Playable:** actual media decoded as moving video, valid frame rate/resolution on target.
- **Reliable:** sustained duration and repeat success, no transient expired tokens or forbidden APIs.
- **Ambient-grade:** permitted unattended playback, no ads/interruption, image quality usable at daylight **and** night where relevant, camera coordinates reasonably accurate.
- **Internationally diverse:** countries/hosts not just 2000 US road-cameras.

The only honest headline count is **#AmbientGradeByProtocol**, after qualifying each preceding stage. No upstream readme can establish the denominator alone.

## 2. Audited OpenVista constraint

[UserSourceParser.kt](../../../source-core/src/main/kotlin/com/openvista/sources/UserSourceParser.kt) declares `enum StreamKind { HLS, DASH }` and detects URI extension; caps M3U import. [PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) forces `MediaItem.Builder.setMimeType` to DASH if streamKind says DASH, else HLS. [player/build.gradle.kts](../../../player/build.gradle.kts) pins **Media3 1.11.1**, with HLS and DASH modules, **no RTSP module**. An unknown `rtsp://` URI manually added as HLS will not magically play. The app must classify source *kind* and fail unsupported schemes before adding them, preserving old HLS/DASH data and ensuring remote lists cannot be used as a generic network scanner.

## 3. Official RTSP source from AndroidX (strong evidence)

The [AndroidX Media RTSP module README](https://github.com/androidx/media/blob/release/libraries/exoplayer_rtsp/README.md) explicitly documents dependency `androidx.media3:media3-exoplayer-rtsp:1.X.X` **matching all Media3 module versions**; with default player config, the `DefaultMediaSourceFactory` detects the module and maps RTSP MediaItems to `RtspMediaSource`. The [official Kotlin sample](https://github.com/androidx/media/blob/release/docsamples/src/main/java/androidx/media3/docsamples/exoplayer/Rtsp.kt) shows player `setMediaItem(MediaItem.fromUri(rtspUri))` or explicit `RtspMediaSource.Factory().createMediaSource(...)`. [DefaultMediaSourceFactory.java](https://github.com/androidx/media/blob/release/libraries/exoplayer/src/main/java/androidx/media3/exoplayer/source/DefaultMediaSourceFactory.java) includes a case for `C.CONTENT_TYPE_RTSP` and reflective loading of `androidx.media3.exoplayer.rtsp.RtspMediaSource$Factory`.

**Conclusion from source:** a narrow H.264 RTSP path using existing ExoPlayer is a significantly smaller **prototype** than shipping a complete WebRTC native stack. **Not proven:** which RTP transport, RTSP authentication modes, H.265, codec hardware support, stream timing or real-camera compatibility on the Hisense. Read specific module tests and official developer guide before promising. No credentials in saved URL/query/logs.

## 4. Real-world upstream stream claims audited and challenged

[Live Environment Streams](https://github.com/william-ricchiuti/Live-Environment-Streams) claims 5,997 entries, 98 countries, 67 provider families and 4,226 active. [sources.json](https://github.com/william-ricchiuti/Live-Environment-Streams/blob/main/sources.json) sums to those maintainer counts. License-type labels by family: **53 `fair_use`**, 11 public domain, 1 open-data, 2 unknown; these are *curator labels*, not evidence of source permission or viewer licensing. The maintainer's public [scripts/validate.py](https://github.com/william-ricchiuti/Live-Environment-Streams/blob/main/scripts/validate.py) treats HLS HTTP status 2xx as active, HTTP images 2xx/3xx, and YouTube as page-markers; this script **does not decode content or measure quality/uptime**. The README separately claims ffprobe-based production monitoring, which we did not independently audit. It describes ~1,750 Skyline entries as HTML pages with expiring HLS tokens, ~475 YouTube extraction cases, 3,614 city-level coordinate approximations, 1,771 unverified.

**Critical categorization:** 1,168 VDOT + 404 Maryland SHA + 261 Delaware DOT + 252 NRW autobahn = **2,085 traffic-camera entries** from just four families (sum of third-party metadata), before any other traffic family. This is neither a scenic HD catalog nor internationally balanced. Counts are not proof of “Live.” Never copy streams or mislead user with totals.

## 5. Protocol-by-protocol decision table

| Protocol | Distinct use case and realistic supply | Native effort | Severe blocker | Preliminary |
|---|---|---|---|---|
| HLS | permitted direct CDN scenic live feeds; master variant playlists | already built | segment gaps, token expiry, rights, live window | improve |
| DASH | professional live feeds where direct manifest available | already built | DRM/entitlements/auth, codec | maintain |
| RTSP | **owner's own IP cameras/NVRs**, some public infrastructure | small-to-medium module integration | camera auth, codecs, UDP/TCP, NAT/security | prototype |
| RTSP over TLS / SRTP | private cameras where client/server support | protocol-specific | auth/cert transport support not yet shown | HOLD |
| MJPEG multipart | some legacy webcams; often low-quality/high bandwidth | dedicated parser/renderer | CPU, no adaptive bitrate, low HD supply | HOLD |
| Timed JPEG snapshot | weather/traffic snapshots; not 25fps moving live | slide refresh adapter | low update frequency, false “live” marketing | possible **separate** mode |
| WHEP / WebRTC | authorized low-latency publisher egress | L/XL WebRTC stack | ICE/DTLS/TURN, native codec and dependency size, lawful sample supply | HOLD |
| Progressive MP4/WebM | user-hosted videos / loops | already within video family depending format | not a live camera | enhance videos |
| RTMP/SRT ingest | broadcaster publish workflows, fewer viewer links | high | needs gateway/backend, rights and transport complexity | low priority |
| Plain HTML webcam page | high number of directory pages | WebView/special integration | ads, cookies, token-extraction terms and freezes | NO-GO generic |
| YouTube watch/live URL | many attractive HD/4K broadcasts | official embed independent | ads and interruption guarantee incompatible | NO-GO ambient |

[go2rtc](https://github.com/AlexxIT/go2rtc) and [MediaMTX](https://github.com/bluenviron/mediamtx) prove that a **self-hosted conversion gateway** could translate certain protocols to HLS, but OpenVista should not require all users to run a PC/server. [MediaMTX WebRTC read guide](https://github.com/bluenviron/mediamtx/blob/main/docs/5-read/03-webrtc.md) shows WHEP endpoints, not automatic Android Media3 support. [WHEP working draft](https://github.com/wish-wg/webrtc-http-egress-protocol) is a separate viewer-signaling protocol from WHIP ingest; confirm its normative status before design commitment.

## 6. Quantitative source scorecard and experimental selection

For each **authorized source sample**, collect `providerId, originalRightsDoc, country, category, protocol, directOrEmbed, auth, tokenTtl, claimsLive, measuredMovingVideo, width, height, FPS, codec, bitrate, measuredFirstFrameMs, 10mSuccess, 1hSuccess, 24hSuccess, nightlyDaylightRating, exactVsCityCoordinates, rebufferTime, independentHost`.

Compute **source usability pass/fail** first: permission ✓, no advertisement interruption ✓, valid actual moving frames ✓, decoder compatibility ✓, HD 720p or more ✓, duration ✓. Rank passing sources by repeat uptime/HD/novel country/scene; do **not** merge failed rights with an average score that looks acceptable.

An **invented sample for explanation**:
- Source A: HLS 1920×1080/30fps authorized, 10m good, 1h unknown, 24h unknown ⇒ “candidate, not 24h verified.”
- Source B: HTML page with 4K thumbnail, 5-minute token and copyright unknown ⇒ **reject** despite marketing quality.
- Source C: HTTP image 1920×1080 updated every 60s ⇒ “snapshot”, not smooth live 1080p.
- Source D: user's own RTSP H264 camera 1920×1080/25fps ⇒ permitted if owned, but not evidence that worldwide scenic source supply expanded.

Prototype benchmark of 20 independently authorized **HD moving-video** sources across ≥5 countries and ≥3 providers is a **research heuristic**, not a license to ship content or an owner-approved release requirement. For RTSP own-camera use, even one controlled sample can prove transport but not public-world-source breadth.

## 7. RTSP E2 experiment exact plan

- Separate worktree/branch; add matching `media3-exoplayer-rtsp:1.11.1`, new parser kind and explicit source input type **in disposable research prototype only**.
- Use synthetic or user-owned authorized RTSP server (e.g. local MediaMTX fixture) with test H264 720p/30fps and 1080p/30fps; document server config and licensing, no live camera URL to repository.
- Try RTSP/TCP; RTSP/UDP only where supported; authenticated stream via secure credential store; connect, 10m play, seek not expected for live, disconnect/timeout/reconnect, app/Dream release.
- Check first frame, video size/frame rate, frame drops, network bytes, Time-to-First-Frame, repeated source switching, codec exceptions, memory, QR pairing gate, auth failure classification.
- Compare current HLS 720p/1080p **on same device**, with same encoding when possible. Stop if additional decoder or stuck socket remains.
- Device-specific H.265, 4K and RTSP auth modes remain HOLD until actual measured proof.

## 8. Cost and user value decision

A new protocol is *valuable* if it demonstrably adds authorized high-quality feeds users can actually supply **without** mandatory servers/ads. RTSP is arguably valuable for private webcams, but user expectation “worldwide scenic cameras” should prioritize **better real HLS/DASH sources and metadata**, not marketing 10 incompatible types. Avoid a first-party catalog under C-01. Before release require sample rights ledger and real target TV playback results. No E2/E3 testing occurred. **Suggested research priority:** HLS quality/availability → RTSP → snapshots as separate experience → WHEP only if permissions and real HD supply justify L/XL cost.
