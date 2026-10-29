# R22 — Comfort Mode: restrained long-session light, motion and overlay behavior

**Verdict:** GO for offline UI-model tests, HOLD production until device checks. **E1** source audit only. [Research #46](https://github.com/murilaiooo/openvista/issues/46).

## User job
During evening or eight-hour viewing, a bright white overlay, frequent change or loud audio may be distracting. The viewer should choose quieter information density and transitions, without digging through unrelated sliders. This is about **comfort**, not ophthalmic treatment, proven blue-light health benefits, saving electrical power, or preventing OLED burn-in.

## What exists
OpenVista already lets users select overlay opacity, color, size, position, hides after inactivity, moves informational groups in a 32-position cycle, and starts muted. [AppSettings](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt); [AmbientOverlay](../../../player/src/main/java/com/openvista/prototype/ambient/AmbientOverlay.kt); [AmbientPlacement](../../../player/src/main/java/com/openvista/prototype/ambient/AmbientPlacement.kt); [SettingsScreens](../../../player/src/main/java/com/openvista/prototype/ui/SettingsScreens.kt). **Do not relabel these as unimplemented features**. New value is a convenient **preset** and optional daypart choice affecting several existing knobs together.

## Options
1. **Comfort preset** (preferred): transitory overlay opacity/contrast reduction, reduce forecast information, hide optional status text sooner, respect audio mute; maintain overlays at accessible text size and preserve system brightness.
2. **Night schedule** using R11 routine profile when approved; standalone preset works even without routines, no clock or internet prerequisite.
3. **Full-video dim shader**: alters image and may require GPU draw pass; must test actual decoder surface and HDR/SDR effects. Dark pixels do not necessarily lower backlight power on LCD. Not default.
4. **Change display backlight via system settings**: reject; platform privilege/OEM constraints and side effects outside app.

Proposed state: Off / Comfort On / Auto-night (later) with “Temporary until next launch” versus “Always” explicit. Preserve personalized user settings when preset enabled; disabling should restore prior values, not flatten user customizations. Settings migration never writes old defaults into user choice.

## Quantitative checks
Simulated overlay screenshots on high-contrast snow and dark skyline, 32 display phases, 720p safe-area, long PT-BR text, visibility while controls open, unknown weather, no location, accessibility large-font. Assert no legend text disappears below user-selected contrast; fade and movement remain **independent**. Check image freeze regressions and no extra video decoder. Proposed metrics: zero new network traffic; one toggle from Player or Appearance; no player rebuild. Physical screen luminance/eye preference and power measurements not yet performed.

## Placement and blockers
Appearance → **Comfort** preset, Player OK → optional fast toggle; do not put another top-level icon on Home. A R11 scheduled routine can activate preset later after independent approval. Ensure no static matte/screen border and do not claim burn-in protection guarantees. GO only after no focus/navigation regression and hardware visual evidence against original screen references.