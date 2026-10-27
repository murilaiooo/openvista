# YouTube Live: isolated Fire TV compatibility experiment

**Status:** Experimental source code only. Not built or exercised on physical hardware as of October 26, 2026. Not approved for the screensaver or public release.

## Purpose

Check whether the official YouTube embedded player can render and sustain a user-selected public livestream on the Hisense 58U6HF / Fire OS 7. The owner's non-negotiable ambient requirement is **no advertising and no interruption**. This experiment **cannot guarantee** that requirement; YouTube controls ads, playback restrictions and branding.

No streaming URLs, links, playlist files, account credentials, downloader or extraction tools ship with OpenVista. The user supplies a test video ID through a YouTube watch/live/share link. All sources remain user-managed.

## Implementation boundary

- Navigate to **Settings > YouTube Live (experimental)**.
- Paste an HTTPS `youtube.com/watch?v=...`, `youtube.com/live/...`, `youtube.com/embed/...`, or `youtu.be/...` link that contains a concrete 11-character video ID. Arbitrary domains, channel live landing pages and playlists are rejected.
- Select **Test live playback**. A single Android system `WebView` opens the official `youtube.com/embed/...` player. Playback begins muted; standard YouTube UI and ads are untouched.
- Android package identity is supplied to YouTube using the HTTPS `Referer` request header, following the official WebView integration guidance to avoid embed error 153.
- The YouTube WebView has no native overlay, ad blocker, proxy, stream URL extractor, custom JavaScript bridge, or downloaded media. A full-screen player is provided without obstructing native YouTube controls.
- Press **Back** to stop and destroy the WebView, returning to the input screen. Leaving the app stops playback and releases the WebView.
- This page is deliberately **not registered** as a saved camera, Media3 playback source, scheduled rotation item, DreamService source, or unattended fallback. No background YouTube player is allowed.

## Physical QA steps (not yet performed)

1. On the Hisense TV, launch the experimental build of the **existing OpenVista app**. Do not install a second app, change the default launcher, or change the system screensaver.
2. In Settings > YouTube Live (experimental), paste a user-selected public livestream link. Confirm invalid URLs and channel landing pages are rejected.
3. Start playback, verify video **and** audio track behavior (muted by default), focus and D-pad operation. Watch for YouTube embed errors 101, 150 and 153.
4. Verify whether there are preroll, midroll or other ad interruptions. **Any ad or interruption fails the owner's ambient acceptance gate.** Do not attempt to remove ads from the official player.
5. Watch for the reported Fire OS WebView issue: the video freezes on its first frame while audio/time/captions advance. If reproduced, record OS build, WebView version, playback duration and relevant logcat lines.
6. Verify full-screen controls and Back, reopen the experiment repeatedly, then press Home and relaunch OpenVista. Confirm no YouTube audio or video continues in the background.
7. With network disconnected, confirm the experiment fails safely and the normal HLS/DASH camera and local-video paths remain unaffected.
8. Test for several hours only **after** basic video, focus, resource release and network recovery checks pass. Observe frame stalls, restart behavior and unexpected ads. Do not claim 24-hour stability without 24-hour evidence.

## Acceptance decision

**Experimental pass:** A valid user link can start and stop in the official embedded WebView without crashes, illegal stream extraction, or changes to other OpenVista playback paths.

**Owner's ambient pass:** Ad-free, interruption-free unattended playback in the real Fire TV environment. The official embedded player does **not** provide an enforceable ad-free guarantee, so this gate is *not met* by implementing an embed alone. Do not enable YouTube in screensaver/automatic rotation without owner approval and a policy-compliant solution that actually meets this gate.

**Known upstream risk:** Amazon Developer Community discussion, October 14–21, 2026, describes Fire OS 7 WebView rendering stalls during YouTube iframe playback. This is a report from other devices, not yet evidence from the owner's Hisense.

## Sources

- YouTube API Services: Required Minimum Functionality (client identity, WebView and overlay requirements): https://developers.google.com/youtube/terms/required-minimum-functionality
- YouTube IFrame API: https://developers.google.com/youtube/iframe_api_reference
- YouTube API Services Developer Policies: https://developers.google.com/youtube/terms/developer-policies
- Amazon Developer Community WebView issue: https://community.amazondeveloper.com/t/html5-video-shows-black-white-frosted-in-webview-based-apps-on-fire-tv-since-oct-11-same-page-plays-in-silk/29748
