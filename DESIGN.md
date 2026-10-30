---
name: OpenVista
description: Cinematic, remote-first Android TV interface for user-managed ambient video
colors:
  midnight-background: "#061421"
  panel-surface: "#0B1D2CB8"
  panel-raised: "#132A3C9E"
  glass-surface: "#0A1A28A6"
  text-primary: "#F3F5F7"
  text-muted: "#AAB8C3"
  text-faint: "#7E8F9C"
  focus-cyan: "#7FDDE9"
  focus-cyan-strong: "#5CCBDB"
  live-status: "#4CD368"
  home-focus: "#C4DCCB"
typography:
  display:
    fontFamily: "Inter, sans-serif"
    fontWeight: 700
  headline:
    fontFamily: "Inter, sans-serif"
    fontWeight: 300
  body:
    fontFamily: "Inter, sans-serif"
    fontWeight: 400
  label:
    fontFamily: "Inter, sans-serif"
    fontWeight: 600
---

# Design System: OpenVista

## Overview

**Creative North Star: "The Cinematic Window" (descriptive shorthand for the existing implementation, not a new brand decision).**

OpenVista frames the user's scenery and source material, rather than competing with it. The Home makes three destinations feel like views through a window. Settings uses an atlas as quiet context for source-aware controls. Playback information is laid directly over the moving image with restrained contrast treatment.

**Key Characteristics:**
- Three immersive, edge-to-edge Home destinations.
- Dark navy/teal settings surfaces with a quiet world-map motif.
- Light Inter typography, selective stronger headings, and cyan focus.
- Four independently configured ambient information groups with no permanent solid panels.
- Original line icons, bounded movement, and minimal transitions.

The original owner-approved mockups remain the visual authority. Tracked app screenshots are current-state evidence, not replacements for those assets. Keep the existing look while improving readability, focus, and task completion.

## Colors

The primary accent is `focus-cyan`. It identifies actual remote focus and interactive state, not miscellaneous decoration. `midnight-background` anchors Settings and secondary screens. `panel-surface`, `panel-raised`, and `glass-surface` provide subdued layering. Text uses primary, muted, and faint roles. Live status may use its distinct green.

**The Focus Is a State Rule.** The strong cyan rim belongs to the currently focused control; selected Settings categories can retain a quieter background without imitating active focus.

**The Source Stays Visible Rule.** Video overlays may use subtle adaptive gradients or shadows for legibility; never replace the moving stream with a static screenshot to fake smooth playback.

## Typography

Bundled **Inter** is the primary typeface; Android sans-serif is the fallback. Implementation is in `TvDesign.install`. Source numbers use tabular figures where appropriate.

Current 960×540-dp design canvas tokens in `TvDesign.kt` (not accessibility-approved minimums):
- Page title: 28sp; general heading: 22sp; body: 12sp.
- Summary: 9sp; label: 10sp; tracked caps: 7.5sp.
- Home panel title: around 36sp in `HomeScreen.kt`.
- Ambient clocks use a separate overlay size scale.

These sizes describe the incumbent code. Low-vision usability, system font scaling, and the 720p appearance remain verification work. Avoid shrinking long translated labels as the only response; wrap or restructure controls.

## Layout

The `AppLocale.wrap` context normalizes the display to a **960×540 dp** canvas; the layout scales from that basis. Shared safe-area tokens are 28dp horizontal and 24dp vertical, while Home and Settings use their own measured geometry.

- **Home:** three equal, full-height scenic panels. Left/right chooses a panel, Up reaches Settings, Down reaches Resume when available, OK opens it.
- **Settings:** seven-category left rail, independently scrollable content panel, visible selected category, Left back to rail and Right into content. A section has one focus stop per setting.
- **Playback:** uninterrupted video with identity at the upper left, source time/weather at the upper right, viewer clock/weather at the lower left, and optional forecast along the bottom.
- **Secondary pages:** one quiet glass panel with familiar Home and Settings navigation and scrollable content.

**The Remote-First Rule.** Every critical path must be operable with D-pad/OK/Back without touch, hover, or a hardware keyboard. Do not let a control, editor, or dialog become unreachable when the TV keyboard opens.

## Elevation & Depth

Tonal layering and subtle outlines create depth. The Home uses scenery plus gradients, Settings uses a static atlas and translucent panels, and ambient overlays use localized haze and edge shading. No expensive real-time video blur is required on Fire OS 7; any new effect must be profiled before adoption.

## Shapes

Quiet, lightly rounded controls: central radius 6dp, panel outlines around 1dp, focus stroke 2dp; smaller row variants can use about 4.6dp. Avoid phone-style oversized pills that waste TV navigation space. Home focus is a whole-panel indicator.

## Components

- **Buttons and setting rows:** `TvUi.button`, `settingRow`, `toggleRow` use `TvDesign.control`; distinguish default, focused, selected, pressed and disabled states. One row corresponds to one remote action.
- **Category rail:** `SettingsShell.RailItem`, with icon plus text, selected fill and cyan focused rim.
- **Home scenic panel:** `HomeScreen.HomePanel`, with visible destination, short explanation, and action, using genuine library counts and honest empty copy.
- **Input and dialogs:** `TvUi.field` and native Android dialogs, with focus, scroll caps and Cancel/Save behavior. Source editing currently needs progressive disclosure and remote-text-entry refinement.
- **Preview and loading:** `CameraPreviewPane` has a single muted decoder and explicit loading/offline/retry states; `TvLoading` uses a delayed restrained spinner.
- **Ambient:** `AmbientOverlay` shows actual source/viewer data and configurable movement/hiding. No static location/weather fixtures are allowed.
- **Motion:** shared motion token 140ms; overlay hide/show about 200ms; display care is discrete movement, not continuous decorative animation.

## Do's and Don'ts

### Do
- **Do** preserve the approved Home, Settings, Live composition and existing localized content.
- **Do** retain contrast against bright, dark, and high-detail camera footage.
- **Do** ensure focused and selected states are distinguishable from across the room.
- **Do** treat zero sources, missing weather, offline media and unsupported codecs as ordinary interface states.
- **Do** verify 720p, 1080p, EN/PT-BR expansion, font scaling, and physical D-pad travel before declaring the UI finished.

### Don't
- **Don't** bundle sample camera URLs or present illustrative Home imagery as playable sources.
- **Don't** invent stream metadata, weather or successful connectivity.
- **Don't** introduce an extra Settings system or a second competing visual vocabulary.
- **Don't** apply web-only detector findings to Kotlin native Views as if they were deterministic.
- **Don't** treat display care as a guarantee against burn-in or a reason to change system screensaver settings.
