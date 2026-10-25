# Hisense 58U6HF / Fire OS 7: hardware and Windows build baseline

**Captured:** 2026-10-25, local time America/Phoenix. **Issue:** #4. **Scope:** one physical TV and one Windows development workstation. ADB hardware interrogation was read-only; the owner later authorized installation/testing of **one isolated diagnostic app at a time**. No existing package, launcher, screensaver, device setting, or private media was modified.

## Device identity and resources

| Property | Observed value | Evidence |
| --- | --- | --- |
| Retail test device | Hisense 58U6HF | Owner's verified hardware |
| Android manufacturer / model | Amazon / AFTHA001 | ADB `getprop`, diagnostic app screen |
| Codename / hardware | `hailey` / `m7632` | ADB |
| Fire OS / Android / API | Fire OS 7.0 / Android 9 / API 28 | ADB |
| Security patch | 2026-03-01 | ADB |
| Android ABI | `armeabi-v7a`, `armeabi` (32-bit app ABI) | ADB, diagnostic app |
| CPU | 4 reported ARMv8 cores, CPU part 0xd05 (Cortex-A55) | ADB `/proc/cpuinfo`, app available processors |
| Total RAM | 1,695,116 kB in `/proc/meminfo`, about 1.62 GiB | ADB |
| Available RAM | 305,528 kB in first ADB sample; approx. 0.47 GiB in subsequent app sample | Varies with system workload |
| Swap | 409,596 kB total in first sample | ADB |
| Data/app partition | Approx. 10.97 GiB total / 7.25 GiB available in the app sample | ADB `df`, app `StatFs` |

Hardware names, firmware family and build API are included; no serial number, raw build fingerprint, Wi-Fi identity or network address was retained. The diagnostic app samples system-reported data on demand, not personal files.

## Display

- Physical output: 3840 x 2160; active diagnostic mode: **3840 x 2160 at 60 Hz**.
- Reported modes: **1920 x 1080 at 60 Hz**, **3840 x 2160 at 60 Hz**.
- Physical density from `wm`: 320 dpi; an override of 480 dpi was also reported. Do not assume a universal TV UI density.
- Advertised HDR modes from the installed diagnostic Activity: **HDR10, Dolby Vision, HLG**.
- These are display capability reports; no HDR video playback or tone-mapping quality test has been completed.

## Video decoder inventory (advertised, not playback-verified)

ADB inspection of the device's `/vendor/etc/media_codecs.xml` reported the following non-encoder entries, along with other secure variants:

| Codec entry | MIME | Advertised maximum size in vendor configuration |
| --- | --- | --- |
| `OMX.MS.AVC.Decoder` | `video/avc` | 4096 x 2304 |
| `OMX.MS.HEVC.Decoder` | `video/hevc` | 4096 x 2176 |
| `OMX.MS.VP9.Decoder` | `video/x-vnd.on2.vp9` | 4096 x 2176 |
| `OMX.MS.AV1.Decoder` | `video/av01` | 4096 x 2176 |
| `OMX.MS.VP8.Decoder` | `video/x-vnd.on2.vp8` | 1920 x 1088 |

The Android API also reports specific codec entries and video size/rate support. Vendor XML maxima are **not** confirmation of simultaneous decoders, actual stable decoding at 4K30/4K60, HDR playback, or hardware/software classification. Android 9 lacks the newer reliable per-codec hardware classification API. Test actual HLS/DASH streams and local video under issue #5 with Media3, dropped frames, memory, and buffer evidence.

## File pickers and input

- Device feature list advertises `android.hardware.usb.host`, Amazon USB storage and `android.software.leanback`.
- `com.android.documentsui` exists; the diagnostic app resolved `ACTION_OPEN_DOCUMENT`, `ACTION_OPEN_DOCUMENT_TREE`, and `ACTION_CREATE_DOCUMENT`.
- **Observed:** Export JSON opened the app's explicit consent dialog and the OS document picker (`com.android.documentsui.picker.PickActivity`). Back correctly returned to the app.
- **Not yet verified:** successful file save through the Fire OS document picker using automated D-pad input; an attempted save did not create the expected file. No report was falsely marked saved. USB folder traversal, actual persistence of URI grants and handling USB removal remain untested.
- Android feature list advertises D-pad/Leanback, gamepad and HDMI-CEC. ADB-injected D-pad Center activated **Run Device Check** and **Export JSON** after the app was foregrounded. The improved focused button received visible cyan highlight on the real 4K TV screen.
- An existing OEM DreamService window briefly intercepted the first remote input before the OpenVista app was foregrounded. This **does not** prove OpenVista DreamService compatibility; #2 remains independent.

## Windows development environment

| Tool | Verified state |
| --- | --- |
| Computer | DESKTOP-OEAGULM |
| Local project | `I:\codex\openvista` |
| Git baseline | `278ae55b1cb3ee9ae4e8a3c13eade5e78d7ef2d1` before this branch |
| Java | Temurin OpenJDK 21.0.12.1 |
| Android SDK | Installed under the user-local Android SDK directory |
| SDK platforms | Android 36 and 37.0 installed |
| Android build tools | 34, 35, 36 and 37 installed |
| Gradle Wrapper | 8.13, generated in this implementation |
| Android Gradle Plugin | 8.13.2 |
| Kotlin Gradle Plugin | 2.2.20 |
| Device debugging | ADB over LAN, successfully connected |
| Python | 3.12 for repository tooling |

The SDK existed but `ANDROID_HOME` was initially unset. Setting it to the installed local SDK directory resolved the first Gradle failure. Existing untracked VLC research scripts were left untouched. No GitHub Actions were invoked.

## Local APK and test evidence

- Isolated debug package: `com.openvista.diagnostics`, version `0.1.0-diagnostic`, min API 28.
- `./gradlew :diagnostics:assembleDebug :diagnostics:testDebugUnitTest :diagnostics:lintDebug` **passed** with local SDK environment set.
- Four Kotlin/JUnit tests passed: schema fields, privacy exclusions, unsupported capability handling, and sanitized labels.
- Android Lint passed with no blocking errors after adding a TV launcher banner, an icon, and data extraction rules. Non-blocking dependency/version and SDK toolchain warnings do not constitute hardware test results.
- Verified `apksigner` debug v2 signature; APK inspection showed **zero requested Android permissions** and **zero bundled media/playlist entries**.
- ADB installed and launched exactly the diagnostic application; it showed a read-only idle state before action and real capability results after the user's Run Device Check action.
- The package remains an owner-authorized local test installation, not a public OpenVista release. A sanitized on-device screenshot is available at [the device-check screenshot](hisense-device-check.png). Replacing it with a different OpenVista app requires removing this package first.

## Open validation gates

1. Confirm saving an actual JSON document via the OEM picker with the TV's physical remote. Verify saved content against `diagnostic-report-schema.json`.
2. Verify Back/Exit and Home return consistently after sleep/wake and after document-picker cancellation.
3. Measure actual Media3 playback, codec/bitrate behavior and 4K HDR decode under #5, without conflating advertised capabilities with playback.
4. Test M3U file import and USB/internal folder selection separately under #3 and #9.
5. Test DreamService OEM selection and lifecycle separately under #2.

Build/install/rollback steps: [diagnostic APK runbook](diagnostic-apk-runbook.md). This baseline does not establish stable v1 support or compatibility with other devices.
