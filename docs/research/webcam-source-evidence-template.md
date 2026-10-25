# Camera source evidence template (G01)

**Status:** empty template; **zero authorized HLS/DASH cameras verified** so far. Use this only after reviewing upstream terms; a public web page or screenshot is not authorization.

## One evidence record per candidate

| Field | Required proof |
| --- | --- |
| Stable camera ID + unique operator | No double-counting mirrored players or thumbnails |
| Original camera owner and publisher | Link to official source and date verified |
| Embedding/native stream permission | Exact official permission clause or explicit written authorization for OpenVista; avoid third-party scraped tokens |
| Stream protocol and format | HLS (.m3u8) / MPEG-DASH (.mpd), real-time media rather than still image/periodic photo/timelapse player |
| URL delivery/security | Manifest acquisition, expiry/token rules, TLS/redirect and permitted client behavior; **never commit private secret URLs** |
| Family suitability | Actual publicly viewable scene, content risks, takedown/report route |
| Geo and metadata | Country, location, timezone, category, attribution requirements, language |
| Playback test | Hisense 58U6HF / Fire OS 7 build, Media3 version, video/audio codec, resolution, bitrate, time-to-first-frame, playback 5–10 min with no illegal scraping |
| Availability | Check time in UTC, repeat at different times, failure/expiry behavior; label source health UNKNOWN outside fresh proof |
| Reviewer decision | APPROVED / REJECTED / PENDING + reason, evidence URL/commit and verification date |

**Never** count an item toward D-71 stable-v1 target of **25** until license/attribution, family suitability and actual on-device HLS/DASH playback are all proven. Outdated or changed terms require re-review.

Research examples: [Windy Webcam API terms](https://account.windy.com/agreements/windy-api-webcams-terms-of-use) and [Skyline FAQ](https://www.skylinewebcams.com/en/support/faq.html) impose restrictions; they are **not** an endorsement of these as eligible native stream providers.

Source status here remains **0 verified** because no operator has been licensed/tested yet.
