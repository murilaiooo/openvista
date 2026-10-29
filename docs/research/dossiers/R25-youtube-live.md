# R25 — YouTube Live: official embeds do not meet ad-free unattended playback

**Research verdict:** **NO-GO** for the required automatic, ad-free ambient experience; **HOLD** for an independently launched, visibly official manual viewer. **E1 code/prototype documentation and policy references**, **no physical test**. [Research #48](https://github.com/murilaiooo/openvista/issues/48).

## User intent vs platform contract
YouTube hosts many high-quality continuous camera livestreams. The product owner explicitly requires that OpenVista never interrupt ambient viewing with advertisements. Official YouTube playback controls, advertising, branding, player configuration, content entitlements and embed errors are governed by YouTube. An app cannot guarantee zero ads using the sanctioned embedded player. Removing, replacing, proxying or blocking YouTube ads, extracting HLS/DASH with unofficial tools, or building a hidden background player would be neither an acceptable nor reliable alternative. Paid user subscription does **not** prove third-party embedded sessions are always ad-free.

## Existing project evidence, not hypothetical
The project already has [draft PR #35](https://github.com/murilaiooo/openvista/pull/35), “Experiment: official YouTube Live playback on Fire OS 7.” That PR specifies:
- Separate Settings experiment accepting only validated watch/live/share IDs, opening official embedded YouTube in Android `WebView`, preserving YouTube controls, branding and ads.
- HTTPS Referer/app identity requirement to avoid embed error 153; reject generic channel landing pages and unsupported playlists. Starts muted; destroy WebView on Back/background.
- Explicit exclusion from saved cameras, Media3 playlist, DreamService, auto-rotation, fallback and public releases.
- **No build, device playback or Gradle test was performed** as recorded in PR body. Risk of Fire OS WebView first-frame freeze is reported, not proved on the target.

Cross-reference [current STATUS](../../STATUS.md): experimental YouTube embeds excluded from stable v1 pending security/release evidence. Current [player/build.gradle.kts](../../../player/build.gradle.kts) includes Media3 HLS/DASH but no YouTube integration. Reimplementing PR #35 from scratch would duplicate untested work; first audit its code and test checklist.

## Remaining possible routes
1. **Official IFrame / WebView, manual-only:** possible isolated opt-in experiment, visible player UI, ads unchanged, may freeze or fail on Android 9 WebView. It does *not* fulfill ambient contract.
2. **Use the official YouTube Android TV app via system intent:** useful as a handoff for users, but **not** an OpenVista background source or seamless playlist member. No guarantees about ads, focus return or video.
3. **Provider-authorized alternative HLS/DASH feed from same camera owner:** highest-value path. Users directly add a sanctioned camera-provider URL; no dependency on YouTube playback. Investigate broadcaster's own published feed and terms.
4. **Server-side rehosting/transcoding/yt-dlp, streamlink, Invidious:** reject for this feature, as they cannot guarantee platform terms compliance, source rights, stability or advertising policy.

## Official policy verification work (not done in this GitHub-only session)
Read https://developers.google.com/youtube/terms/developer-policies, https://developers.google.com/youtube/terms/required-minimum-functionality, and https://developers.google.com/youtube/iframe_api_reference with an up-to-date Google source before any implementation decision. No scraping or bypass test is authorized. The policy result here draws from the existing project PR and documented requirements, not from a new Google-site fetch.

## Discriminating experiment
Only if owner later explicitly authorizes manual-viewer research: compile isolated PR #35 on its branch, test one permitted broadcaster's own public YouTube stream on independent Fire OS 7, reproduce/record error 101/150/153, visible controls, WebView focus, unexpected ads (any are compatible with official playback but disqualify ambient), first-frame freeze, background cleanup, player memory and app lifecycle. Compare a legal alternative direct feed for the same broadcaster if available. Do not treat a 10-minute session without ads as an ad-free guarantee.

## UI / stop rules
**No “YouTube source” type in the main cameras library**, no persistent settings entry, no route from automatic scene modes while ads can occur. If a manual official viewer passes technical/policy gates, it could live separately under Experimental, clearly labeled as outside the ambient experience. Stop at the first ad-policy conflict or WebView instability; keep #35 draft and independent.

## Product decision
The number of YouTube live cameras is attractive but **not actionable** under the zero-ad/zero-interruption constraint. Prioritize verified authorized HLS/DASH cameras and user-owned RTSP instead of claiming general YouTube support.