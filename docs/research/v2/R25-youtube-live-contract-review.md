# R25 deep investigation — YouTube Live vs uninterrupted ambient contract

**Evidence:** E1 first-party OpenVista draft implementation and source status, **not a new YouTube-policy fetch**. **Verdict:** NO-GO as an automatic ambient source under the owner's mandatory **no advertising/no interruptions**. Manual viewer: HOLD separate, not an implied feature approval. [Earlier dossier](../dossiers/R25-youtube-live.md).

## 1. Existing experiment matters

[OpenVista draft PR #35](https://github.com/murilaiooo/openvista/pull/35) already explores a **manual official YouTube embedded player**, with typed URL validation and a WebView:
- limited to YouTube watch/live/share URLs containing a concrete video ID;
- requires app identity/HTTPS Referer context for embedding; recognizes errors 101/150/153 as possible provider results;
- shows **official YouTube controls, branding and advertisements** unchanged;
- explicitly excludes DreamService, saved camera library, Media3 HLS/DASH queues, rotation, local fallback and releases;
- **not compiled or physically tested** per PR author notes.

[OpenVista status](../../STATUS.md) also explicitly excludes experimental YouTube embed from release. This research must not duplicate or merge that draft by accident. The product question is **not** whether a video can open once; it's whether an unattended 8h TV screensaver can be guaranteed ad-free, seamless and provider-compliant. Those are different.

## 2. Four integration models and why they diverge

| Model | Feasible user action | Policy/technical constraint | Decision |
|---|---|---|---|
| Official visible YouTube embed | manually open a livestream, possibly ads | YouTube player may show ads, be unavailable or stop; WebView overhead | HOLD optional manual |
| System intent to YouTube TV app | open content in official app | not integrated into OpenVista playlist/screensaver; focus/lifecycle change | possible **handoff** only |
| Authorized broadcaster publishes independent HLS/DASH feed | user enters direct sanctioned URL | source-specific license/auth; not a YouTube integration | **preferred research path** |
| Extract YouTube media / strip ads via proxy/unofficial API | scrape stream URL | unreliable, platform terms/ad bypass and rights conflicts | NO-GO |

YouTube video ID or channel page is not a stable MPEG-DASH/HLS media URL, and treating it as `StreamKind.HLS` is incorrect. A 10-minute official-embed test with zero encountered ads is **not proof that zero ads will appear during 8 hours**. YouTube Premium membership does not make arbitrary third-party iframe sessions an unconditional ad-free API.

## 3. Why “higher quality YouTube cameras” is not enough

A live camera can be labeled 4K on the YouTube website but an embedded browser/Fire OS WebView may negotiate a lower variant, lack codecs, use unsupported video rendering, freeze on first frame or block embeds entirely. Some broadcasters rotate live video IDs. Verified playback requires independent **measured negotiated resolution, codec, startup, dropped frames, ad interruptions, resume after error and operator permission**. These cannot be inferred from thumbnail/YouTube title.

## 4. Product tests and separate hypotheses

**Technical hypothesis (manual-only):** Fire OS WebView can render a sanctioned broadcaster stream with controls, Back, audio focus and no frozen first frame.
**Policy hypothesis (ambient):** an official permitted player can guarantee zero ads/interruption without manipulating advertising. The existing program cannot establish this; current owner rule makes it a **hard rejection**, not a performance experiment to bypass policy.

If future owner permits manual viewer, the protocol test matrix includes:
1. Valid 11-char concrete ID vs channel live landing page.
2. Embed disabled (101/150), identity error (153), region restricted.
3. Invalid/deleted video, source changes ID, age/consent wall.
4. 4K label but actual negotiated quality unsupported.
5. First frame frozen while audio advances (Fire OS concern, unverified).
6. Remote OK/Back focus, visible YouTube branding, normal controls.
7. Midroll/preroll advertisement, **must not attempt bypass**.
8. App Home/sleep: WebView/media/audio released, no hidden background.
9. 8h uninterrupted playback claim must fail if any advertising can occur.
10. Live reconnect/fallback handling cannot use Media3 camera recovery as is.

## 5. Official provider verification blocker

The API's current official restrictions should be confirmed from Google-owned documentation at https://developers.google.com/youtube/terms/developer-policies, https://developers.google.com/youtube/terms/required-minimum-functionality, https://developers.google.com/youtube/iframe_api_reference before any external distribution. The current GitHub-only mandate does not permit independent fetch of those developer pages. Therefore no new definitive policy claim is based on a 2026 Google page read here.

## 6. Final decision

No YouTube URLs in Camera Add, Favorites, rotation or DreamService. Keep existing #35 experiment separate and not implemented in main. Focus camera supply work on original broadcaster's permitted direct HLS/DASH streams, RTSP user-controlled sources and licensed media. **Stop** immediately if a suggested integration depends on ad blocking, unofficial media extraction, hidden players or aggressive token refresh of websites.
