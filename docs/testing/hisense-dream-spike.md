# Hisense DreamService probe — issue #2

**Local date:** 2026-10-25 (America/Phoenix).
**Target:** Hisense 58U6HF, Fire OS 7 / Android 9 (API 28), model AFTHA001.
**Scope:** optional system screensaver registration and input handling. This is an engineering proof and is **not** automatic camera/video playback in screensaver mode.

## Official Android integration

- `player/src/main/AndroidManifest.xml` registers `OpenVistaDreamService` for `android.service.dreams.DreamService` with `android.permission.BIND_DREAM_SERVICE`. The service has the Android dream metadata resource `@xml/openvista_dream`. No boot receiver, alarm, privileged permission, launcher replacement or system settings write is added.
- `OpenVistaDreamService` uses `setInteractive(true)` and `setFullscreen(true)` during `onAttachedToWindow`, with `setScreenBright(false)` to avoid forcing the panel bright. It displays an explicit *engineering-probe* text-only view; **no built-in video, camera, URL or playlist** is provided.
- Direction/OK key-down events update a probe status. Back/wake and other events are delegated to Android's `DreamService.dispatchKeyEvent` behavior. The service does not launch other apps. Existing manual `PlayerPrototypeActivity` remains independent.
- Reference: [Android DreamService documentation](https://developer.android.com/reference/android/service/dreams/DreamService) and Android 9 framework API.

## Read-only Hisense observations

The following commands were used to **read** device state only. No setting was changed, no APK installed, and no navigation key was sent in this step:

```text
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell settings get secure screensaver_enabled
adb shell settings get secure screensaver_components
adb shell settings get secure screensaver_default_component
adb shell settings get secure screensaver_activate_on_sleep
adb shell settings get secure screensaver_activate_on_dock
adb shell dumpsys window windows
```

| Read-only field | Device result |
| --- | --- |
| Model / OS version | `AFTHA001` / Android `9` |
| `secure/screensaver_enabled` | `1` |
| `secure/screensaver_components` | `tv.dumbtv.wallpaperdream/.PhotoDream` |
| `secure/screensaver_default_component` | `tv.dumbtv.wallpaperdream/.PhotoDream` |
| Activate on sleep / dock | `1` / `1` |
| Foreground window | `Sys2023:dream` |

The above confirms the **existing OEM selection only**; it does **not** prove that the new OpenVista DreamService is eligible for selection, can launch on idle, receives remote events from Fire OS, or keeps playing across wake/sleep.

## Automated evidence

- Android 9 Robolectric package-manager test verifies the system dream intent resolves to OpenVista's declared service, requires `BIND_DREAM_SERVICE`, is exported for system discovery and contains the dream metadata resource.
- Android 9 Robolectric service test sends Left, Right and OK key-down events through `dispatchKeyEvent`; the probe consumes them. This does not prove how the Fire OS OEM routes physical D-pad input.
- Local Gradle tests, Android Lint, APK signature and zero-bundled-content guard must pass before code review/merge.

## Remaining physical evidence — not run

An owner-approved, isolated follow-up on the Hisense must test whether the OEM screensaver picker offers the new OpenVista entry; record allowed idle intervals (1/3/5/10/15/30 minutes where supported), D-pad/OK/Back interception, standby/wake behavior and whether the system actually switches from the current `PhotoDream`. Back up and restore each changed setting. **Do not modify the current wallpaperdream, launcher, or device settings without explicit owner consent.** A manual fullscreen app remains the supported fallback if the OEM disallows third-party dreams.

No universal Android TV/Google TV/Fire TV compatibility or stable-v1 G-03 PASS is claimed.
