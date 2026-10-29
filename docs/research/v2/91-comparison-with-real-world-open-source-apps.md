# Competitive architecture and novelty review: what OpenVista should *not* duplicate

**Evidence:** project-owned GitHub READMEs/source files inspected 2026-10-28; **no app installed or usability benchmark run**. Provider feature claims are the project's own claims, and license assessment requires formal review before any copying.

## Aerial Views: an excellent baseline, not a blank field

[Aerial Views](https://github.com/theothernt/AerialViews) is GPLv3, states API23+ and Fire TV support, and advertises 4K/Dolby Vision where device supports it, USB, SMB, WebDAV, Immich, Nextcloud Memories, custom feeds, playlist options, time/date/location overlays and overlay movement. Its real repo contains [WebDavDataSource](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/providers/webdav/WebDavDataSource.kt), [SambaHelper](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/data/network/SambaHelper.kt), [ImmichApi](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/providers/immich/ImmichApi.kt) and [HA-related message overlay settings](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/ui/settings/OverlaysMessageApiFragment.kt).

**Implication:** R12 Network Library, R19 local HA display messages, R21 cinematic ambiance and personal photo albums are **not world-first ideas**. Implementing these only for parity may distract from OpenVista's distinct **user-supplied world cameras + real camera location/time/weather** emphasis. Do not copy GPLv3 implementation into MIT code without compatibility/license strategy; study interfaces and verify behavior independently.

## Immich Android TV: photos and videos are a specialized product

[giejay/Immich-Android-TV](https://github.com/giejay/Immich-Android-TV) advertises phone-based login and API key, album loading, person/recent/seasonal selection and slideshow. Actual [ApiService.kt](https://github.com/giejay/Immich-Android-TV/blob/main/app/src/main/java/nl/giejay/android/tv/immich/api/service/ApiService.kt) lists album/asset/memory endpoints. This demonstrates the product complexity of remote personal libraries: authentication, server versions, paging, media types and revocation. It is **not** a Google Photos API shortcut. Its README says compatible with Immich server v3+, which is a changing upstream claim and should not become hard-coded into OpenVista planning without revalidation.

## go2rtc and MediaMTX: gateways are different from native viewers

[go2rtc](https://github.com/AlexxIT/go2rtc) supports many ingest/playback protocols and optional FFmpeg transcoding. [MediaMTX](https://github.com/bluenviron/mediamtx) supports RTSP, RTMP, HLS, WebRTC/WHEP and other protocols on a separate media server. That is evidence that users with their **own** server can expose a TV-friendly HLS URL, not proof that OpenVista itself should ship a giant Go server, background transcoder or expose unauthenticated LAN endpoints.

## Feature novelty table

| OpenVista feature | Competitor precedent | Distinct OpenVista opportunity | Design trap |
|---|---|---|---|
| Weather, clock, overlays | Aerial Views | Separate source/viewer forecasts, worldwide saved geotags | inaccurate location or redundant widgets |
| NAS/remote video | Aerial Views, Immich Android TV | simple personal-media import under one TV experience | bloated provider settings |
| Home Assistant | Aerial Views message API | limited authenticated scene commands for own cameras | exposed HTTP server |
| Google Photos | No confirmed Google Photos partner-access proof in this source review | optional personal media if partner-approved | promise total account sync from Picker |
| Smart Director | Existing shuffle/playlist engines commonplace | explainable rotation by *camera geodata and quality* | user loses control |
| Sun Chaser | Solar/time context is known broadly | find real daylight in *user's own global camera collection* | “sunrise” label is false when camera faces wrong way |
| Data Monitor | OS network settings exist | app-specific usage and camera estimate understandable on TV | inaccurate network accounting |
| Live Preview | Many apps show thumbnails | one focused preview without harming full-screen decoder | 12 live decoders |
| Digital Window | Atmoph-like aesthetic not unique | restrained optional styling with weather/source info | static bezel, heavy GPU |
| Multi-TV | synchronization common elsewhere | local command-only co-viewing without cloud | conflicts with approved no-sync v1 |

## Differentiation hypothesis to test, not marketing copy

OpenVista's best possible point of difference is an **automated, explainable, permission-respecting virtual window into the user's own worldwide live cameras**, using local solar context and reliable playback while preserving attractive source/viewer information. This depends critically on actual high-quality authorized sources and coordinates. If user collection only contains 2 cameras or 80% have no location, Smart Director and Sun Chaser may yield little value; a good import/preview/health flow may be more useful than “innovative” ranking. Measure that before promoting a product narrative.
