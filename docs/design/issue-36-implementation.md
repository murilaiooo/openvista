# Issue #36 implementation handoff

The owner requested implementation on October 27, 2026 and explicitly prohibited builds, tests, lint, emulators, ADB, device operations and automated validation. These instructions override the issue's execution gates for this implementation session. Independent review must complete those gates before merge.

## Design input

[Issue #36](https://github.com/murilaiooo/openvista/issues/36) is the approved specification. Its three written references describe scenic three-column Home, four-zone ambient information, and midnight atlas Settings. The original PNGs are absent from the issue body and tracked repository assets. Do not fabricate reference screenshots or claim a pixel comparison. Add the original files to this directory when accessible.

## Architecture

Keep native Android Views, Activity route history, shared LibraryStore and Media3. TvDesign centralizes tokens; TvUi owns reusable focusable controls; Home and Settings use dedicated bounded layouts; AmbientOverlay owns informational presentation and lifecycle-bound weather requests. No Compose, image-loading dependency, player replacement, permissions, bundled media, new provider approval or system-setting change.

The isolated branch starts at 24249bc on codex/remaining-development to preserve the working checkout's existing development functionality. The review base is that development branch so this PR isolates the redesign. Mainline integration must retain the separate verified SAF/Dream changes.

## Implementation sequence

- [x] Read the issue, status, roadmap, architecture, content policy and existing screens.
- [x] Establish shared palette, type, spacing, panels and focus tokens.
- [x] Implement scenic Home and route/focus restoration.
- [x] Implement atlas Settings, grouped controls and saved states.
- [x] Implement separate source/viewer clocks, real weather and forecast, bounded movement and independent hiding.
- [x] Document assets, components, limitations and independent validation steps.
- [x] Read the final changes, commit, push and open [draft PR #37](https://github.com/murilaiooo/openvista/pull/37) without executing checks.

## Implementation rulings

- Retain the existing native architecture to avoid playback and storage churn.
- Use UI-only scenery with clear source-empty copy; Home images never identify an available camera.
- Never substitute viewer data into the source weather/clock zone. A missing source timezone hides its clock; an unnamed source stays unnamed.
- Respect existing global overlay switches and editor offsets; add optional settings with backward-compatible read defaults.
- Move the complete information composition inside a measured safe rectangle; focused controls remain outside it.
- No runtime screenshots or claims of working hardware behavior can be supplied under the owner's testing restriction.

## Source review record

Peer source reading covered Home/Activity standards, Settings/control requirements and ambient/weather state. Implementation fixes addressed large-font content regions, measured feedback/header height, duplicate same-coordinate weather requests, cancellation when moving between shared/separate locations, prior-source timezone reuse, legacy None migration and named forecast visibility. Whole information groups are measured before fitting to avoid partial forecast/credit rows. This was source reading only; it is not compilation, automated validation, runtime evidence or acceptance.

See [the design system](tv-design-system.md), [Home asset provenance](home-assets.md) and [independent validation instructions](issue-36-validation.md).
