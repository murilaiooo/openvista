# R21/R22 deep investigation — Architectural window and long-session comfort

**Evidence:** E1 source and display engineering; no measured luminance/power or visual acceptance. **Verdict:** investigate one preset engine; R21 Digital Window HOLD as high aesthetic/regression risk; R22 Comfort preset GO for pure configuration experiment, HOLD device deployment. [Original R21](../dossiers/R21-digital-window.md) • [Original R22](../dossiers/R22-comfort-mode.md).

## 1. Current display-care baseline

[AmbientPlacement.kt](../../../player/src/main/java/com/openvista/prototype/ambient/AmbientPlacement.kt) defines `CYCLE_LENGTH=32`: 4 layout arrangements × drift changes. [AmbientOverlay.kt](../../../player/src/main/java/com/openvista/prototype/ambient/AmbientOverlay.kt) positions source/viewer/weather and forecast groups and supports inactivity visibility, opacity and panel content. [AppSettings.kt is LibraryModels.kt](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) already holds `overlayOpacity`, per-group opacities, `hideOverlay`, `overlayMoveMinutes`, brand and forecast toggles. [SceneView.kt](../../../player/src/main/java/com/openvista/prototype/ui/SceneView.kt) composes video with display surface. The user already approved a clean full-bleed presentation. These are **not new**; R21/22 only add an optional *composition preset* and a coherent “comfort” shortcut.

## 2. Physics: what software dimming does and does not do

A semi-transparent dark overlay reduces RGB pixel values, **not necessarily LCD panel backlight power**, because backlight luminance is typically separately controlled. It can alter perceived brightness while reducing visual detail and crushing near-black video. OLED/miniLED behavior differs. A frame/matte creates a **fixed luminance boundary** on long sessions; moving text in 32 phases does not move a permanent window border. Software “burn-in protection”, “blue light therapy”, reduced eyesight strain or lifetime claims need actual evidence; don't make them.

HDR is especially risky: overlay color in SDR graphics over HDR video may be tone-mapped inconsistently depending on Surface composition. A shader can break hardware protected surfaces or force GPU composition on weak device. **Preferred first experiment:** no custom GLSL or video re-encoding; adjust only existing overlay group opacity/visibility, respecting original scene untouched. Physical display care must be validated independently.

## 3. Competing visual concepts

| Version | Changes | Benefit | Risk | Recommendation |
|---|---|---|---|---|
| Current Fullscreen | None | maximum view, approved layout | overlays already moved | default |
| Comfort preset | clock/weather group opacity, density, auto-hide | quick quieter ambience | unreadable text/accessibility | prototype |
| Subtle Edge Hint | tiny low-luminance translucent edge | design distinction | persistent border/static hotspot | HOLD |
| Window matte/inset frame | video reduced with bezel | architectural effect | letterboxing, fixed bright/dark border | likely NO-GO for daily 8h |
| Full-scene dim shader | darken every pixel | lower perceived glare | HDR distortion, decoder/GPU | last resort |
| TV-system backlight control | change hardware brightness | actual backlight effect | OEM privileges, global side-effects | NO-GO without scope change |

## 4. Preset engine ownership

A `VisualProfile` should **derive effective UI properties** while preserving exact user customizations. Do not overwrite original `AppSettings` sliders with preset values, because disabling Comfort must recover user's prior opacity/layout/size. Design:
```text
EffectiveVisuals = applyTemporaryPreset(UserSavedVisuals, ComfortOrWindowPreset)
UserSavedVisuals remains untouched
On preset Off → immediate restoration
On app restart → explicit persisted or temporary preference choice
Screensaver routines R11 may choose profile without mutating base user settings
```

If a user manually adjusts individual opacity while Comfort is active, determine whether it edits base values or the preset; prefer transparent “Customize this preset” and avoid hidden overwrites. A preset should not rebuild the Media3 player or call `player.setMediaItem`.

## 5. Test scenes and pixel checks

Test source set **synthetic/licensed**: midday snow (bright), night skyline (dark), water shimmer (motion/high frequency), sky gradient (banding), sunset HDR10 (when hardware supports), news-like static banner (burn-in risk). At 720p/1080p/4K compare:
1. Exactly 32 placement phases, all four overlay groups visible/in bounds where configured.
2. Disappearing forecast or no camera location must not strand “blank” static placeholder.
3. Contrast with white/blurry and dark scenery; preserve legibility and text outline against all colors.
4. D-pad focus/Settings modal visible with Comfort enabled, never dim focused actions.
5. Long PT-BR labels and number/date localizations without clipping.
6. Video orientation/crop: frame does not obscure focal edges or hide source content.
7. Blur/Glass effect maintains original owner visual style with no obvious rectangle.
8. Fade and position transitions never call Player restart (avoid prior frozen-frame regression).
9. Simulated 24h temporal histogram of static pixels **as a risk heuristic**, not proof of panel safety.
10. Actual TV 24-hour bright/dark inspection needed for display life claims; no such test completed.

Quantitative design limits to test: no new streaming requests, no new video decoder, stable 60fps UI target under normal load where device supports, no unexpected changes to saved Settings, and a one-action toggle. No measured fps or luminance data exists.

## 6. User tasks and navigation

Player OK → More → **Comfort**; Appearance → **Scene presentation** with Fullscreen (default), Comfort and optional Window experiment. Existing Settings rail should be reused, not expanded. A Window preview must use synthetic static content and not intercept remote focus. The image should look like a window by restraint, not bright 3D wood/mullions. Night schedule is an independent R11 integration, not a hidden dependency of the Comfort toggle.

## 7. Decision gates

**R22:** go to offline profile derivation tests first; only release if high contrast, user overrides, HDR and original overlay movement pass on TV. **R21:** hold until owner visual comparisons show benefit large enough to justify static-frame risk; if frame is always brighter/harder-edged than current full-screen, reject. Both are UX/graphics tasks, **not** a video-format expansion. 
