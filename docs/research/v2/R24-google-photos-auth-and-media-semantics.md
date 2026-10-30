# R24 deep investigation — Google Photos integration feasibility and user-consent design

**Evidence:** E1 existing product/source constraints and GitHub available exemplars; Google's **live platform policy, partner access, quota, prices and exact OAuth flows were NOT fetched** because this pass was GitHub-only. **Verdict:** HOLD; not enough evidence to promise direct Google Photos support. [Earlier dossier](../dossiers/R24-google-photos.md).

## 1. Exact user job and three distinct levels of access

The user wants **personal photos AND videos** (not generic Drive videos), ideally selectable on the television and periodically refreshed. Different levels have different permissions:
- **One-time selected media:** user chooses photos/videos; app may display those selections during a session if permitted. No assumption of full-library read.
- **Persistent albums/automatic memories:** slideshow that follows changes to albums over weeks; requires sustained authorization, provider-supported listing/session and provider policies.
- **Unlimited full-library mirror:** scan everything, background sync and cache forever; very different privacy/terms/cost and explicitly not safe to infer from a picker.

Do not conflate the three when marketing “Google Photos integration.”

## 2. Product and code incompatibilities

OpenVista currently stores **two** library families, CAMERAS and VIDEOS, with `LibraryItem.uri` and Media3 playback. A **photo** (static JPEG/HEIF) cannot be treated as an HLS live camera or ordinary video without carefully defined slide duration, transition, zoom/crop and decoder. A picked cloud video has a potentially expiring authenticated media URL: saving that URL to `LibraryStore` as a permanent playable URI would cause broken items and leak credentials. Introduce media-reference semantics `LocalFile | AuthenticatedRemoteVideo | PhotoAssetReference` only after true API viability; do not change the current user-library schema speculatively.

D-67 [v1 acceptance](../../product/v1-acceptance.md) explicitly defers exporting/importing settings and playlists. Google Photos image **display** isn't automatically backup/sync, but any cloud mirroring of OpenVista app data would require a new decision. Base OpenVista must work without Google account; Google authorization is optional only.

## 3. Candidate provider routes, honest feasibility gates

**Ambient API:** Designed conceptually for digital frames/TV ambient media; partner program access may be required. A partnership-dependent API is a *business eligibility blocker*, not solved by Kotlin code. Need documented approved project identity, supported devices, allowed displays, source selection, offline media, quotas, allowed caching, OAuth consent, photo+video bitrate/resolution and review process. **No approval was obtained**.

**Picker API:** User-selected photos/media may be reachable through official OAuth/session, but is **not equivalent to Google Photos automatic album sync**. Need confirm exact media reference expiry, size limits, picker on TV vs second-screen, selected album vs individual media, refresh/revocation and developer-policy scope. Do not assert previously reported TTL as a current verified contract without official docs.

**Library API:** Historic broad scopes have changed; unrestricted reading of preexisting user libraries cannot be assumed. Need direct official permission-eligibility confirmation. Until then this path remains NO-GO for full-library personal slideshow.

**Self-hosted alternative:** User exports their chosen Google Takeout media to their **own** NAS/WebDAV/SMB folder; OpenVista reads files without Google API. This is not true Google Photos live sync but is genuinely achievable as a user-controlled option if R12 passes. [Immich Android TV](https://github.com/giejay/Immich-Android-TV) implements personal photo/video slideshow and album API with authentication; [Aerial Views ImmichApi.kt](https://github.com/theothernt/AerialViews/blob/master/app/src/main/java/com/neilturner/aerialviews/providers/immich/ImmichApi.kt) reads album/asset APIs. These validate a *different* possible optional provider, not Google permissions.

## 4. TV-auth flow options and where risk lives

```text
My Videos → Add personal collection → Google Photos (only if eligibility verified)
Explain data use and no required OpenVista account
TV displays short code/QR or sanctioned official OAuth device flow
User authenticates on official Google surface
TV receives scoped approval, never user's password
User selects allowed albums/media (as provider permits)
OpenVista stores opaque asset reference + provider credentialRef
Media displayed when account/session available; offline if rights/caching permit
User unlinks: revoke/forget token; clear cached bytes and provider metadata
```

A QR code by itself is **not OAuth authorization**; it merely links a browser flow. Local pairing server is disabled in [PairingCoordinator.kt](../../../player/src/main/java/com/openvista/prototype/pairing/PairingCoordinator.kt). Use sanctioned OAuth flow, not a homemade password screen, cookie-sharing or browser scraping. Do not assume an app installed from GitHub Releases meets Google's verification requirements or can embed secrets in an APK.

## 5. Personal data and media handling

- User's personal photos can include faces, children, precise GPS EXIF and sensitive subjects. Keep processing local on TV; no analytics or uploading.
- Thumbnails and full-resolution video may differ in available size/format. HEIC/HEIF, animated photos, portrait orientation, 4K HDR and live-motion assets need individual decoder/image pipeline tests.
- Avoid prefetching entire albums and exhausting bandwidth; support data saver and bounded cache/expiry.
- Token refresh and 401/revoked media should not mark every user asset as deleted. Provide clear account status, remove references on unlink and avoid cloud content lists in diagnostic reports.
- The app must not persist a short-lived playback URL as permanent identity. Stable provider asset IDs and renewed authorized media fetch are needed if provider permits.
- Display copyright/licensing and provider-specific terms in relevant UI; not a permanent brand overlay on personal photos unless required.

## 6. Test vector collection

| ID | Case | Expected |
|---|---|---|
| GP-01 | no Google account | OpenVista fully functional |
| GP-02 | deny scopes | return to My Videos, no residual token |
| GP-03 | 1 photo selected | correct aspect and duration |
| GP-04 | 1 long video selected | Media3 starts only if auth/codec supported |
| GP-05 | 500 mixed items, 4K images | bounded memory/lazy loading |
| GP-06 | expired media URL mid-video | renew only via authorized provider path |
| GP-07 | revoke access while slideshow running | exit account media, local fallback |
| GP-08 | album deleted or unshared | explain missing, do not delete local video |
| GP-09 | OAuth flow interrupted/TV restarts | no partial account state |
| GP-10 | network offline | only rights-cleared cached files play |
| GP-11 | unlink/delete saved account | credentials and photos cache gone |
| GP-12 | unsupported HEIC/HDR/codec | skip safely, no black-screen loop |
| GP-13 | duplicate media across albums | stable identity, dedup optional |
| GP-14 | personal photo geotags present | never send EXIF to unrelated provider |
| GP-15 | simultaneous Weather/Photos requests | bandwidth accounting correct |

## 7. Decision and missing evidence

**Direct Ambient/Picker integration is HOLD**, because this GitHub-only pass cannot establish partner approval, live Google permission terms or media longevity. An independent provider account and official policy check are prerequisites before a prototype; **do not request user's Google account credentials**. Real Android9 TV authorization and video playback require E3. Media import from personal local/NAS storage remains the practical no-provider fallback. No integration code was added.
