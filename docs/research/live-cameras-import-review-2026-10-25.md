# OpenVista — review of 60 live stream links (2026-10-25)

**Original user research:** [60 direct HLS/DASH addresses](live-cameras-50-2026-10-25.md). The filename contains "50" historically; the table actually has **60 endpoints for 59 distinct cameras**. Do not rename the original file merely to match the count: existing links would break.

**Staged, machine-readable candidates:** [catalog/candidates/live-cameras-2026-10-25.json](../../catalog/candidates/live-cameras-2026-10-25.json), generated with [import_live_camera_candidates.py](../../scripts/import_live_camera_candidates.py). **No feed has been approved for automatic inclusion in public builds**; all staged records set `publishEnabled:false`.

## What has actually been checked

- This import validated table structure, 1–60 sequential row IDs, distinct HTTPS links, HLS/DASH suffixes, 60 unique feed URLs and 59 distinct camera identities. Two Ö3 StudioCam links (HLS and DASH) were merged into **one** camera with two variants.
- The original document **reports** successful same-day manifest and `ffprobe` checks, with frame checks for Thailand. The import did **not** repeat those internet checks, test source-rights contracts, or play anything on the Hisense 58U6HF / Fire OS 7.
- **Technical stream syntax, earlier PC-side `ffprobe` success, rights permission, reliability and compatibility are separate states.** A status of "not yet approved" is **not** a claim that a stream is unusable.
- All 60 links are non-tokenized HTTPS URLs as *reported in the file*, but URL paths may still represent hosted resources requiring operator authorization. No target livestream URLs were automatically polled during this import.

## Inventory snapshot

| Publisher / service | Feed addresses | Rights/availability situation |
| --- | ---: | --- |
| Caltrans (California DOT) | **31** | **Review operator ownership and allowed reuse.** [Caltrans Conditions of Use](https://dot.ca.gov/conditions-of-use) generally treat agency-owned website information as public domain, but warn that some photographs or other third-party materials require permission. The DOT's stream playback permissions and service load limits still need confirmation. |
| Thailand Department of Highways | **11** | Official [traffic-camera portal](https://www.highwaytraffic.go.th/DOHMobile/Modules/CCTV.aspx) is available; no independently documented reuse permission for third-party Android TV apps found here. |
| Iowa DOT | **3** | **Prior express written permission required for republishing/displaying or reusing site content**, per [Iowa DOT Terms of Use](https://iowadot.gov/policies-statements/terms-use). Official [2026 camera URL migration advisory](https://iowadot.gov/news/2026-05-28/media-advisory-iowa-dot-updates-traffic-camera-video-system-data-feed-usersdevelopers) documents changing video URL hosts. Do **not** auto-publish without authorization. |
| MoDOT | **3** | Live public camera viewing confirmed by [official agency page](https://www.modot.org/closed-circuit-cameras); public access alone does not prove broad redistribution rights. |
| Arlington County | **2** | Published government-related streams, but third-party redistribution authorization still needs evidence. |
| Maryland SHA (CHART) | **2** | Official [CHART feeds](https://chart.maryland.gov/DataFeeds/GetDataFeeds) publish public camera view URLs; this does not establish independent reuse/distribution permission. |
| IPCamLive / owners | **2** | [IPCamLive FAQ](https://www.ipcamlive.com/faqs) describes hosted camera pages and integration features; individual camera owners and hosting terms must allow OpenVista re-use. |
| VDOT | **1** | Operator-specific authorization and live feed stability unverified. |
| Gesell | **1** | Beach camera: origin `gesell.com.ar`; owner's integration permission not yet documented. |
| 3Cat | **1** | Rotating scenic **TV feed**, not a single fixed camera. Broadcaster streaming availability does not imply third-party republishing license. |
| ORF | **2** | **One Ö3 StudioCam with HLS and DASH alternatives.** Broadcaster rights need review. |
| Radio Radicale | **1** | Parliament interior, not landscape; inclusion and content/operator rights need review. |
| **Total** | **60** | **59 unique cameras; 0 approved for the public catalog** |

## Catalog category fit

- **53/60 stream addresses** are road/traffic/highway cameras. The default catalog will look like a traffic-monitoring system unless we prioritize the scenic/urban picks and search for actual beaches, mountains, monuments, parks, ports and wildlife as approved in D-29.
- Three links are beaches, one is a broadcaster's rotating scenic feed, two are one radio studio and one is a parliamentary interior. This corresponds to **59 camera identities** after the Ö3 duplicate.
- Many road cameras in the file are 320×240 or similar. Even a formally permitted and functioning stream may be **visually unsuitable for a 4K ambient TV screensaver**. Do not equate `ffprobe` success with content quality.
- Prioritize native live quality over arbitrary catalog size. Stable v1 still requires **25 licensed, family-appropriate, actually playable sources**, not necessarily 25 HD ones; nevertheless record a separate picture-quality threshold and category-diversity goals without silently modifying D-71.

## Safe inclusion pipeline

1. **Stage (completed):** keep original research document unchanged and generate 59 camera candidates / 60 streams in a clearly quarantined JSON file.
2. **Rights (pending):** attach primary operator license/permission evidence to each camera ID, including any channel/session conditions. Reject providers that only allow their website/player or private use.
3. **Family/content suitability (pending):** watch an authorized sample, check category, image quality, location/timezone and any privacy concerns.
4. **Device playback (pending):** use Media3 on the actual Hisense Fire OS 7, record codec, frame drops, buffering/bitrate, 10-minute play and outage recovery under source rules. Never flood agency servers with automated 24h per-stream checks.
5. **Promote only after all gates:** change catalog publication status by an explicitly reviewed metadata update/approval workflow. The current candidate importer always sets `publishEnabled: false`; it does not implicitly ship URLs to runtime or unlock playback.
6. **Keep availability fresh:** selected licensed sources should use provider-supported URLs/data feeds, health checks with appropriate TTL/backoff, and honest offline states. Do not cache live video unless provider terms allow it.

### Triage recommendation

Prioritize the **31 Caltrans** links for *ownership/rights confirmation*, because they are the largest first-party group with comparatively permissive general terms; prioritize **beach/scenic feeds separately** to align with OpenVista's actual ambient-visual goal. Explicitly quarantine Iowa links until written authorization. Do not promise that 25 Caltrans cameras satisfy release criteria until camera ownership and Hisense playback are independently proven.

## Evidence and scope

Source file: [owner list](live-cameras-50-2026-10-25.md). Requirements: [D-71/D-72/D-77](../product/decisions.md); [v1 gate G01](../product/v1-acceptance.md); [source evidence checklist](webcam-source-evidence-template.md); [issue #3](https://github.com/murilaiooo/openvista/issues/3) and [issue #5](https://github.com/murilaiooo/openvista/issues/5).

**As of this review:** 59 candidate cameras / 60 links, **0 public approvals, 0 OpenVista Hisense playback tests**; any earlier `ffprobe` verification is attributed to the supplied list, not re-performed here.
