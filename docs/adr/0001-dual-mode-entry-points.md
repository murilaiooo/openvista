# ADR 0001: App mode and optional Android screensaver mode

- **Status:** Proposed technical architecture; **product requirement approved** on 2026-10-24 (both modes).
- **Date:** 2026-10-24

## Context
OpenVista should work on a broad range of Android TV-family devices. The Android DreamService framework exists, but current Android TV / Google TV / Fire OS implementations may prevent users from selecting third-party screensavers normally. Some devices may require ADB steps or block selection entirely.

## Proposed decision
Implement two entry points sharing the same playback core:
1. **App mode**: ordinary lean-back fullscreen Activity, launched manually.
2. **Dream mode**: optional DreamService, enabled only on devices that allow it.

Never alter the device's default screensaver, ambient service or power policy without explicit opt-in, documented revert and compatibility check.

## Benefits and downsides
- Improves device coverage and usability without privileged access.
- Adds lifecycle/testing complexity and may need device-specific docs.

## Product approval and technical validation
The owner confirmed **both** automatic screensaver and manually launched app in round 1. This approves the product behavior, not universal OEM support or a specific technical implementation.

Before accepting the shared playback-core architecture, prototype DreamService/manual mode on Fire OS 7 plus at least one Google TV and another Android TV class. Test wake/remote behavior, no black screen and normal rollback. Clearly document devices where only manual mode is supported.
