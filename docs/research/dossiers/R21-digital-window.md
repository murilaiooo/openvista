# R21 — Digital Window: optional architectural presentation without static damage

**Verdict:** HOLD pending screen references and display-care validation. E1 design/code analysis; no user comparison or image test. [Research #46](https://github.com/murilaiooo/openvista/issues/46).

## Proposed value
Some users want their TV to feel like a framed view, not just a screen. A highly restrained frame can convey architectural depth on a wall, but a dark bezel around a reduced video might waste screen area, create a static hotspot or look artificial. Visual ambition must **not** override the user's explicitly approved minimal fullscreen design.

## Existing constraints
[HomeScreen.kt](../../../player/src/main/java/com/openvista/prototype/ui/HomeScreen.kt) uses exactly three panels; [SceneView.kt](../../../player/src/main/java/com/openvista/prototype/ui/SceneView.kt) composes fullscreen video + overlays; [AmbientOverlay.kt](../../../player/src/main/java/com/openvista/prototype/ambient/AmbientOverlay.kt) uses weather/source/viewer groups and subtle contrast; [AmbientPlacement.kt](../../../player/src/main/java/com/openvista/prototype/ambient/AmbientPlacement.kt) defines a 32-phase display-care cycle. User's Home/Settings/Player references remain the visual source of truth, not decorative mockups.

## Design options
- **Frameless (current, default):** full-bleed, no frame.
- **Edge hint:** 1–2% subtle non-rectangular edge illumination or micro-shadow that never hides critical picture information; must respect color depth and test on LCD and OLED.
- **Architectural inset:** modest video inset with matte around it; risks permanent high-contrast frame and screen real estate loss; **likely reject** for long-running sessions.
- **Physical-world parallax from camera:** unavailable without camera viewpoint/depth; do not fake motion tracking or require a device camera.

**Recommended experiment:** two simple presets, Frameless and Edge Hint, with slider off by default; no wood, window mullions, fixed decorative labels or heavy blur. If any look requires a second player/shader on Fire OS 7, reject.

## Objective evaluation
Produce screenshots at 16:9 720p, 1080p, 4K; scene samples: bright snow, black night city, busy high-frequency water. Verify no crop of captions, user overlay, focal point, platform safe margins, contrast or D-pad focus. Inspect frame luminance over a simulated 24h static exposure; simulate 32 overlay movement phases with both presets. **Software cannot promise panel safety**. Compare legibility and subjective preference with independent reviewers when possible; no scores fabricated.

## Navigation
Appearance → **Scene presentation**: Fullscreen / Subtle window. Preview in the existing overlay editor; Back restores previous value; saved preset has one configuration instead of multiple nested pages. Changing settings must not restart/rebuffer current HLS stream.

## Rejection conditions
Static bright border, persistent scene crop that hides useful details, video rebuffering when toggled, inaccessible controls or evident “fake window” aesthetic. The design is a small optional surface, not a new Home tile, product identity or additional decoding mode. Related requirement: [display-care spec](../../design/ambient-display-care-2026-10-27.md).