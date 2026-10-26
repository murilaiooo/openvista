# Hisense DreamService display-idle regression investigation

**Date:** 2026-10-26

## User-observed symptom

While the independent QA operator was updating and reopening the test APK, the Hisense display turned off and the TV returned to its launcher/Home. The QA procedure included an ADB wake key and an explicit `am start -S` of OpenVista Activity, so this incident alone **does not establish a DreamService crash**. An independent read-back confirmed the OpenVista screensaver was still enabled and selected, with both system inactivity timers at their original 180000 ms.

## Code-level defect and correction

The prior `OpenVistaDreamService` called `setScreenBright(false)` when attaching and removed the dream's `FLAG_KEEP_SCREEN_ON` during playback unless the general manual-player `keepAwake` option was enabled. The owner had `keepAwake=false`; the dream could therefore be dimmed or slept by OEM idle policy even while it was buffering or actively playing.

Use Android's supported DreamService API, `setScreenBright(boolean)`, to keep the display active while an attached, valid dream is playing or buffering a media item. Restore ordinary screen behavior when paused, stopped, released, or outside the dream. Do not bypass the remote Power key, user-invoked standby, OEM hard power limits or system permissions. This fix does not silently modify Android system settings, launcher selection or installed packages.

Android API documentation: https://developer.android.com/reference/android/service/dreams/DreamService

## Verification

- Focused `DreamScreenPolicyTest` covers active playing/buffering, pause, stop, failed initialization, unavailable session and detached dreams.
- Local Windows Gradle JVM/unit tests, debug APK assembly and Android Lint: **PASS** on the focused isolated branch.
- Previous debug signature was compared to the candidate; in-place package update: **PASS**. System screensaver component continued pointing to `com.openvista.prototype/.OpenVistaDreamService`.
- The non-disruptive post-install idle probe recorded four samples at 0, 37, 72 and 107 seconds: Fire OS remained `Asleep`, without an OpenVista process or active DreamService. The TV was already asleep when sampling began, so this **does not test active Dream playback or establish a crash**. Both temporarily shortened OS timers were restored to their original 180000 ms values and read back successfully. No ADB wake key or activity relaunch was sent after this installation.
- Device-level proof of preventing actual OEM screen-off during an active Dream, 24-hour continuous playback and recovery to Dream after waking: **PENDING**. A screen-off caused by a user Power command must remain possible.

## Next physical acceptance

With the TV awake on Home, let the three-minute idle timer start the OpenVista screensaver. Observe playback for at least ten minutes without remote input. Confirm a moving camera and both info panels remain visible instead of blacking out or returning to Home. Test an explicit Power/standby press separately; it must still stop the display, and Home after a later wake is platform-controlled. Record exact observed behavior, not an inferred pass.
