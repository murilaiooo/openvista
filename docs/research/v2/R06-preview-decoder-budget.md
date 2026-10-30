# R06 engineering feasibility — visual previews without decoder contention

**Outcome:** E1; GO for single-thumbnail-vs-single-preview comparison, HOLD full-grid live preview. [Original dossier](../dossiers/R06-live-preview.md) • [Existing issue #33](https://github.com/murilaiooo/openvista/issues/33).

## Why preview is deceptively expensive

Current [PlayerScreen.kt](../../../player/src/main/java/com/openvista/prototype/ui/PlayerScreen.kt) uses `SceneView` and one `PlaybackSession`, while [PlaybackSession.kt](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) enforces an **activeSession singleton** and releases the prior session on new construction. A second preview `PlaybackSession` would **kill the actual fullscreen player**. Android 9 on a specific Fire OS device may have scarce hardware decoder instances. A preview plan needs an explicit **exclusive video-owner state machine**, not an ExoPlayer in every RecyclerView/card.

State:
```text
NO_PLAYBACK
  ├─ library focus stable → STILL_REQUEST or PREVIEW_VIDEO (one only)
  ├─ source selected → RELEASE_PREVIEW → ACQUIRE_MAIN → PLAY
  └─ app background → CANCEL_ALL
MAIN_PLAYBACK
  ├─ no background preview, even for favorites or Settings
  ├─ details opened → release main according to existing handoff
  └─ app→Dream→app → existing snapshot; previews stay disabled
```

## Four approaches and an evidence-based preference

| Approach | Primary benefit | Cost and failure mode | First choice |
|---|---|---|---|
| User/provider-authorized static thumbnail | instant rows, no decoder | missing thumbnails; rights and URL expiry | **Yes, if explicitly permitted** |
| Cached last-decoded preview from an earlier normal play | no separate preview fetch | storing copyrighted frame may not be permitted; stale; bitmap RAM | conditional, never fake live |
| One on-focus preview decoder | actual current image | main-player conflict, start latency, bandwidth, focus churn | prototype only |
| 6–12 simultaneous streaming tiles | visually rich | decoder overload, connections, jank, network bills | no-go on first target |

**Important:** A thumbnail endpoint may be a still refresh every 60 seconds; label “Preview at [time]”, not “Live” if freshness unknown. A snapshot of a previously played camera should not appear as a still-image freeze in the fullscreen player. The [freeze regression](../../testing/live-video-freeze-regression-2026-10-27.md) documents why a PixelCopy hold overlay was removed; visual previews cannot recreate that failure.

## Focus request state contract

```text
FocusChanged(itemId, generation)
  -> cancel pending request for previous item
  -> after e.g. 600ms stable focus, check that item still exists
  -> permitted preview asset? request bounded still
  -> else if opt-in interactive preview, acquire isolated decoder
  -> result includes itemId + generation; reject stale completion
  -> release on focus loss, list dispose, background, main play
```

A simple `PreviewResult` union should have: `Ready(image, source, observedAt)`, `Loading`, `Unavailable`, `NoPermission`, `Unsupported`, `Stale`. Don't report preview timeout as camera failure: it may just be a thumbnail CDN problem. Zero thumbnails should not prevent camera selection. Never fetch every list image on import; pages are currently 30 items ([LibraryScreens.kt](../../../player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt)), and a focused rail preview can avoid 30 network requests per page.

## Memory/bandwidth estimation to validate, not measured

A decoded 640×360 ARGB_8888 bitmap uses 640×360×4 = **921,600 bytes (~0.88 MiB)** before overhead; retaining 20 could use ~17.6 MiB plus caches and UI, which is too much for some TV circumstances. A 320×180 bitmap is ~0.22 MiB. These are arithmetic estimates, not measured heap usage. At an *illustrative* 2 Mbps preview stream for 5 seconds, one focus action uses ~1.25 MB payload before protocol overhead. Ten quick focus operations would already add ~12.5 MB if not debounced; this motivates still-first design.

**Proposed pilot budgets:** at most one active video preview, at most four cached 320×180 images, 600ms D-pad dwell, 2s still timeout and 5s preview-video timeout, cache eviction when player starts. They must be tuned from hardware, not marketed as proven.

## Adversarial test matrix

1. 100 rapid focus moves in 10 seconds; only latest camera ever updates pane.
2. Source deleted during async preview; never display another camera's image.
3. Timeout returns after navigating away; late result ignored.
4. URI token expires; image errors do not mark Camera Health unavailable.
5. Huge 8K JPEG with misleading Content-Length; cap compressed response and decode bounds.
6. Same URL reused with different user IDs; no shared cache leakage across users/profiles.
7. Enter fullscreen while preview decoder is loading; release completes before main playback.
8. Exit player repeatedly to list; no leak of MediaCodec, Surface, Player or callbacks.
9. TV background and Dream transition while focused; no camera stays connected silently.
10. 720p/1080p/4K layout; long titles, missing country, focus ring, accessibility.
11. Offline, 401, 404, malformed JPEG, no thumbnail license.
12. Rapid family switch Cameras↔Videos; no phantom stale preview.

## UI geometry and accessibility

Cameras list remains normal D-pad row list; preview is a **read-only right-hand pane** for highlighted camera, approximately 40% available width, with no focus trap. Show “Preview unavailable” gracefully. Never fill every card with fake previews. On Play from highlighted row, return focus to that row after Back and preserve filter/page. The user can disable previews to save data.

## Independent prototype decision

First implement a **headless cache/focus generation unit test** and static mock preview with synthetic licensed images, not streams. Only if navigation improvement is evident compare a **single decoder preview** behind an independent test harness and full resource telemetry. E3 must demonstrate no main playback regression in the exact Fire OS 7 build. No user-owned camera URLs are put in GitHub. 
