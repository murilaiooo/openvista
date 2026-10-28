# Ambient display care and layout correction (October 27, 2026)

This correction follows the supplied ambient-player reference and Issue #36. It does not change playback, media sources, operating-system power settings, location permissions or weather providers.

## Verified defect

The earlier Cinematic implementation advanced a nine-point position phase but restricted the entire composition to a maximum 12dp movement inset. Brand, source, viewer and full-width forecast were nearly fixed in the same screen regions even with movement enabled. New installations also had display-care movement disabled by default.

## Implementation

- Four discrete compositions rotate source/brand and viewer/forecast between opposite corners and upper/lower safe rows. Each group moves as a whole: clocks, city, status, weather icons, dividers/attribution and backgrounds. No continuous animation loop.
- A 62%-width forecast with an independent viewer block replaces the always-full-width footer. Five- to seven-day weather remains selectable. Source and viewer locations stay independent, with no filename substituted for a city.
- Reserve the existing 5% screen-safe margin, plus an additional 8dp on the full-size player. Each information group is measured and fitted before being positioned. The upper and lower rows cannot overlap given their capped heights (42% and 38% of the available area).
- When the controls are open, source information stays in the upper band while the viewer/forecast are hidden by the existing controller. Controls do not move.
- The movement interval still accepts 1–30 minutes and defaults to 5. Movement now defaults to on for **new settings**; an existing explicitly saved off choice remains off. Hide/fade is still an independent optional control.
- The Cinematic preset now includes date lines, and light translucent gradients replace the opaque-looking ambient scrims. Older explicitly saved date preferences are preserved.

## Validation and limitations

- Local Gradle command: `gradlew :player:testDebugUnitTest --tests com.openvista.prototype.ambient.AmbientPlacementTest :player:assembleDebug --offline --no-daemon --console plain` (Android SDK environment configured).
- **Passed**: targeted four-phase geometry tests, all corner/phase safe-area and collision tests, contextual-control placement, new-installation default, and debug APK compilation.
- The updated debug APK was installed on the existing Hisense Fire TV device without adding a second app; the pre-change library JSON was copied to the app's private files directory before installation.
- **Still required**: recorded screenshots after each timed shift on the actual 4K player; 720p text/forecast visual check; continuous 24-hour playback and performance observation. Geometry tests alone do not prove real-world burn-in prevention.
