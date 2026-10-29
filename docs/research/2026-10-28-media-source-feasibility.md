# OpenVista: personal media and worldwide camera protocol feasibility

**Status:** Primary-source reconnaissance, **not** a confirmation of live stream inventories, device compatibility, or provider permission. Parent: [#42](https://github.com/murilaiooo/openvista/issues/42), 2026-10-28.

## Core rule: source supply beats format-count marketing

The project does **not** bundle stream URLs, M3U lists or camera catalogs (decision C-01). Investigate *user-provided, permitted* source formats based on (1) actual number of legitimate real-world cameras available **to users**, (2) sustained 720p/1080p/4K quality and daylight/night usability, (3) stable direct link or sanctioned embed/API, (4) no mandatory advertisements, (5) account/fees/geo/token constraints, (6) Android 9/Fire OS 7 codec support, (7) implementability without cloud re-streaming. **No claim of 'thousands of compatible streams' without independent test samples, rights review and source provenance.** A web directory thumbnail, 5-minute snapshot timelapse or browser-only embed must not be advertised as continuous HD live video.

## Source and protocol triage matrix

| Candidate | Real supply hypothesis | Player approach | Rights/technical gate | Research priority |
| --- | --- | --- | --- | --- |
| **HLS** (existing) | Broad content delivery for webcams, stations and licensed OTT; many variants are protected/expiring | Media3 HLS/LL-HLS where supported | Validate direct links, variants, token expiry, format/codec, entitlement and ad inserts | **Baseline first** |
| **MPEG-DASH** (existing) | Common live platform/player packaging, but raw public camera manifests often restricted | Media3 DASH | CORS is irrelevant to native player but DRM, auth, no-license links and CDNs matter | **Baseline first** |
| **RTSP (R26A)** | Very common in user-owned IP cameras/NVRs; may not open a global public scenery catalog | `media3-exoplayer-rtsp`, single session, H264 and supported audio | UDP/TCP, NAT, Basic/Digest credentials, safe encrypted storage, private LAN vs public Internet risk | **P1 feasibility** |
| **MJPEG / multipart JPEG (R26B)** | Used by some older webcams, LAN and still-image cameras; unknown worldwide HD continuous supply | Bounded multipart parser or image refresh adapter, not assume Media3 HLS | Rate limiting, stale image, quality, JPEG re-encode CPU, source permissions, network costs, static-frame burn-in | **P1 inventory first** |
| **HTTP(S) image-refresh / JPEG snapshots (R26C)** | Numerous webcam directories supply periodically updated images (not 25fps video) | Explicit *snapshot slideshow* mode with timestamp and refresh interval | Distinct UI/type from LIVE VIDEO; honor remote server rate/rights, never mislabel freshness | **P1 separate experience** |
| **WebRTC/WHEP (R26D)** | Growing low-latency delivery, but actual worldwide public scenic camera supply unknown | Native WebRTC stack / WHEP signaling research, separate architecture from ExoPlayer | Higher dependency/codec/ICE complexity, security, server grants; WHEP still IETF Internet-Draft in Oct 2026 | **P2 after inventory** |
| **Progressive MP4/WebM/MKV/TS** | Common self-hosted recordings and looped scenes; weaker fit for live rotating cams | Existing Media3 progressive stack + MIME/codec probing | True live vs downloadable file, seek, range, buffering, container/codec support; URLs need user permission | **P1 local/video sources** |
| **SRT / RTMP ingest** | Mostly production ingestion workflows, not end-user camera viewing links | Requires a bridge/protocol library or re-stream server | Costs, security and absence of directly user-viewable legal public sources likely outweigh benefit | **P3 / likely no-go** |
| **Webpage/embed/iframe** | High count of webcam *pages* but not direct playback, sometimes ads/DRM/JS/consent | Site-specific official player only if authorized; avoid generic WebView | Terms, trackers, no ad-free guarantee, remote focus, Fire OS 7 WebView reliability | **HOLD** |
| **YouTube Live page URLs** | Large quantity of attractive quality livestreams but not direct HLS/DASH to repackage | Official visible YouTube player only (if acceptable) | Ads cannot be blocked; no uninterrupted guarantee, player policy, WebView/app identity | **NO-GO for ad-free ambient** |

**Important:** Media3 RTSP docs specify H264 SPS/PPS and RTP over UDP or interleaved RTSP/TCP; device codec support is not universal. The Media3 supported-format table includes LL-HLS and progressive containers. WHEP draft is not the same as WHIP (ingestion standard): distinguish viewer egress from publisher ingest. Sources:
- https://developer.android.com/media/media3/exoplayer/rtsp
- https://developer.android.com/media/media3/exoplayer/supported-formats
- https://datatracker.ietf.org/doc/draft-ietf-wish-whep/
- https://www.rfc-editor.org/rfc/rfc9725.html

## A practical camera-supply verification campaign (research task, not performed)

Start with a **licensed provider/source inventory** documented as a separate *non-runtime* private research artifact. Sample each format across countries and categories, daylight and nighttime, public vs private sources, and source hosts, with user-supplied explicit authorization where necessary. The project can assess a list without packaging/distributing its URLs.

For each candidate camera, record: source host/provider; official license and terms URL; whether the **viewer** has permission; country/coords provenance; category; true live video vs recent image/timelapse; direct HLS/DASH/RTSP/MJPEG/WHEP vs YouTube/HTML wrapper; required authentication/cookies/geo; stable link lifetime; expected availability; actual measured width/height/fps/codec/bitrate; test date; 10-minute/1-hour/24-hour failure behavior; daytime/nighttime sample; fallback; provider API fees. A publicly visible URL is **not** permission to scrape/re-stream/ship media.

Quality bar to consider broad support: at least **20 independently usable authorized examples across >=5 countries and >=3 providers** for a protocol, with a meaningful share of genuinely live HD content, before claiming it increases the global scenic catalog. This is a **research benchmark** to revisit, not an approved v1 requirement. A smaller count may justify RTSP as a user-owned camera feature but not a public worldwide-camera strategy.

### Specific market observations

- [Windy Webcams API](https://api.windy.com/webcams/docs) offers camera metadata, preview images and timelapses with expiring tokens; this is **not proof that all entries are HD real-time video feeds**. Its [pricing](https://api.windy.com/webcams/pricing) describes free link/embed and a substantially priced professional tier. Do not ship its data/API keys or claim free unrestricted access.
- A public [Live Environment Streams repository](https://github.com/william-ricchiuti/Live-Environment-Streams) claims a large directory including HLS, YouTube and HTML types. Treat claims as **unverified lead generation**, not trusted proof of freshness, stream rights, host support, quality or global coverage. Do not incorporate dataset into the public app or repository.
- [Media3 RTSP](https://developer.android.com/media/media3/exoplayer/rtsp) makes a native RTSP prototype relatively tractable, but public Internet cameras can have questionable authorization and privacy. Prioritize authorized personal/NVR streams on trusted networks.
- No credential harvesting, generic stream extraction, access control bypass or arbitrary site scraping.

## Google Photos: two completely different tracks

**A. Google Photos Ambient API:** Officially designed for ambient photos/videos on smart TVs and digital frames; the **Google Photos Partner Program is required for Ambient API access**. It supports TV/limited-input OAuth device flow and curated media sources. This is the **best conceptual fit**, but the project cannot treat the API as available until admitted and verified. Public launch requires Google's reviews. Account requirement would be limited to the optional personal-media integration, never the base app.

**B. Google Photos Picker API:** Public integration path for an explicitly selected set of photos/videos through OAuth and a picker session. The picker creates `pickerUri` for user selection, perhaps opened on a phone through QR, then the app retrieves chosen items. Picked media `baseUrl` expires (typically 60 minutes) and needs an auth header. It is **not** a persistent API for the user's entire library, auto-sync and full-album slideshow; assess local temporary caching, refresh, revoke/erase, OAuth verification, and user intent. Google Photos **Library API** since March 31, 2025 is restricted to app-created media, so do not design unrestricted import of a user's entire preexisting library from it.

**Policy gate:** [Google Photos developer policy](https://developers.google.com/photos/support/api-policy) prohibits substituting for Google Photos or using its APIs as a general purpose gallery and requires narrow permissions. A simple Google Photos clone/slideshow using Picker may conflict; legal/product review is necessary. Do not promise that any of these APIs will be authorized.

Primary sources:
- https://developers.google.com/photos/ambient/guides/about
- https://developers.google.com/photos/partner-program/overview
- https://developers.google.com/photos/ambient/guides/get-started
- https://developers.google.com/photos/picker/guides/get-started-picker
- https://developers.google.com/photos/picker/guides/sessions
- https://developers.google.com/photos/picker/guides/media-items
- https://developers.google.com/photos/support/updates
- https://developers.google.com/photos/support/api-policy

**Decision (research stage):** Ambient API = **HOLD (partner access)**; Picker = **HOLD (policy, OAuth/expiry UX, scope)**; Library API unrestricted photos = **NO-GO**. Continue with SAF/local/NAS personal media independent of Google.

## YouTube Live: commercial policies vs owner requirement

User's required product experience is **ad-free, uninterrupted ambient video**. YouTube's documented developer policies forbid blocking, replacing or modifying player advertisements; they also disallow non-official retrieval, player manipulation, and background/invisible playback. Official IFrame playback could be an **optional explicitly launched viewer only**, with visible YouTube branding, subject to rules and verified WebView playback. It **cannot satisfy the stated ambient screensaver contract**, so don't create a default YouTube adapter, scrape DASH/HLS video URLs, use proxies/invidious/yt-dlp to circumvent platform rules, or present YouTube Premium as a guaranteed embedded ad-free solution.

- https://developers.google.com/youtube/terms/developer-policies
- https://developers.google.com/youtube/terms/developer-policies-guide
- https://developers.google.com/youtube/iframe_api_reference

**Decision (research stage):** **NO-GO for background/screensaver/automatic playlist use** while ad-free uninterrupted playback remains a requirement. A distinct manual official YouTube viewer remains **HOLD** and is not approved.

## Candidate selection order

1. Fix reliability and input experience for existing HLS/DASH and permitted local video.
2. Inventory proven useful legal streams and source formats first, including quality/time of day.
3. Build an isolated RTSP user-camera feasibility spike without adding it to stable v1.
4. Only add MJPEG/snapshot mode if meaningful quality/availability exists; label snapshots honestly.
5. Evaluate WHEP only with validated licensed sample inventory and performance budgets.
6. Pursue optional Google Photos only through permitted scopes/partner eligibility.
7. Reject any approach that requires breaking ads/policies or sending personal source URLs to a third-party proxy.

## Acceptance evidence table

| Gate | Evidence needed | Current evidence |
| --- | --- | --- |
| Permission to play | Source host written terms and per-sample rights, not merely network reachability | Not collected |
| Broad source supply | Audit with country/provider/codec/quality/uptime sample | Not collected |
| Fire OS 7 playback | Native TV decoder, startup, memory, 24h stability | Not run for new formats |
| Google Photos | Partner approval or compatible Picker use-case/OAuth review | Not approved |
| YouTube ad-free | Provider-sanctioned proof of no interruption, no ad blocking | Conflicts with published policy |
| Security | Secrets, token lifecycle, credential encryption, network permissions, prompt injection/untrusted M3Us | Needs review |
| Packaging | Zero stream URLs/videos in release APK; no private research fixtures | Must run final artifact guard |

This is a set of **research decisions**, not evidence of an implemented feature.
