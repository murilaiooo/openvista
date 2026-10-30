# R06 — Live Camera Preview: useful visual choice without decoder overload

**Research verdict:** GO for UX/thumbnail feasibility; HOLD any multi-live-grid design. **E1** source analysis. [Research #44](https://github.com/murilaiooo/openvista/issues/44); existing [visual preview issue #33](https://github.com/murilaiooo/openvista/issues/33) remains open.

## Problem
A text-only list of many user-owned streams makes it impossible to identify a good viewpoint, whether a camera faces sunset, or whether the image is a dark, static night feed. A visual preview would dramatically reduce repeated full-screen switching **if** it does not impair primary playback.

## Current implementation
[LibraryScreens](../../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt) creates camera rows, details and map; [HomeScreen](../../../player/src/main/java/com/openvista/prototype/ui/HomeScreen.kt) renders three non-live scenic backgrounds. [PlayerScreen](../../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt) creates a `PlaybackSession`; [PlaybackSession](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) owns one active player and recovery. A former PixelCopy last-frame hold caused frozen footage and was removed: [regression report](../../testing/live-video-freeze-regression-2026-10-27.md). **Never revive PixelCopy hold or promise smooth simultaneous decodes**.

## Compare four approaches
1. **Provider-supplied permitted thumbnails:** low cost and often best; require explicit user-provided or licensed thumbnail URL, crop-safe display, host rate limiting, signed-URL expiry and consent. Never derive arbitrary thumbnails from unverified web pages.
2. **Live focused on-demand preview:** one preview player shown *only while library is foreground and main player is released/suspended*; cancel on focus movement; only start after stable focus dwell (e.g. 400–800ms), never for all rows.
3. **Session image snapshot after first decoded frame:** can retain a short-lived in-memory bitmap *for library card only* when platform permits, with strictly bounded size, copyright policy and cancellation; never overlay the live player with it.
4. **Multiple simultaneous video players:** NO-GO for target TV until independent decoder/memory capacity verified; not justified by UX benefit.

**Suggestion:** default text/metadata row with a single adjacent preview pane; gray “Preview unavailable” state; optional cached verified still. Preview is read-only, muted, isolated from rotation, and cannot mark a camera unavailable just because a thumbnail endpoint failed.

## Lifecycle and data design
Library focus → debounce → request id / cancel token → verify saved record still exists → fetch limited image or initialize preview session → report **loading / ready / stale / unsupported / failed**. On Back, full-screen play, changing media family, screen-off or process background, cancel pending requests and release preview resources. A failed preview does not trigger playback fallback. Cache by source ID + authorization-safe revision (not raw URL in disk filename) with TTL and total byte cap; optional no-cache for private streams.

## Test cases and acceptance
Simulate 50 rapid D-pad focus changes and assert no stale frame appears for wrong row; network 403/404/timeout; provider image contains unexpected dimensions; 4k image decode OOM; no image permission; screen sleep mid-fetch; Android Activity recreation; source deleted while preview loading. Measure at 720p/1080p/4K, both EN/PT-BR. **Target, not measured:** no more than one active preview acquisition; limit cached thumbnail to e.g. 640×360; main player receives decoder priority and playback starts without a preview resource leak. Performance and actual hardware limits require E3.

## Ethical/rights constraints
Source pages may disallow capturing or retaining webcam imagery; respect permission and disclosure, never enable implicit screenshot harvesting or distribute captures. No background scanning all user URLs to populate a giant preview grid. Compare [Aerial Views](https://github.com/theothernt/AerialViews) as UX/remote-playback precedent, not proof of shared decoder performance on this Hisense.

## UI / outcome
Cameras → currently highlighted camera preview on the side, with country, live status if observed, last verified resolution and title. No new Home card and no technical settings until user testing establishes a need. Keep issue #33 independent of this research dossier. **No implementation or on-device test run.**