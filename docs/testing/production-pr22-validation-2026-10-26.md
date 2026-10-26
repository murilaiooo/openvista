# OpenVista PR #22: Independent Validation, 2026-10-26

Scope: incoming head e9ce0ac16e7a364b9aecea902bde60f244f664c3 on codex/complete-tv-implementation. Partial QA, not release approval.

## Local validation

- The incoming head failed Kotlin compilation in TvUi.kt: unresolved accessibilityHeading property. Fixed using View.setAccessibilityHeading(true), available on API 28.
- The next Gradle run compiled, but Android Lint failed with three RestrictedApi errors on ComponentActivity.dispatchKeyEvent. Scoped the suppression to the intentional full-screen TV D-pad interception override in OpenVistaActivity.kt.
- Final local Gradle :player:testDebugUnitTest :source-core:test :diagnostics:testDebugUnitTest :player:assembleDebug :player:lintDebug --offline --no-daemon: BUILD SUCCESSFUL.
- Existing Python unit suite: 23 tests passed. Discovery/docs validator: PASS.
- Actual debug APK: zero-bundled-content guard PASS; apksigner verified Android signature v2 with one signer.
- Added five successful Android-9 Robolectric LibraryStoreContractTest scenarios: first install empty/muted, atomic duplicate camera-list import and restoration, mixed-family playlist rejection, unplugged folder metadata/favorite preservation, settings bounds and persistence.

## Physical Hisense evidence

- Hisense 58U6HF, Fire OS 7, Android 9/API 28, reported model AFTHA001.
- Exactly one OpenVista package, com.openvista.prototype; existing dumbtv screensaver selection unchanged.
- adb install -r updated only that package successfully, without installing another app or deleting data.
- am start -W launched com.openvista.prototype/.OpenVistaActivity with Status: ok, foreground OpenVista window, mWakefulness=Awake and live application PID.
- On-device UI hierarchy displayed the onboarding page with Get started focused and Skip setup available; no source played.
- Fire OS subsequently entered Sys2023:dream, mWakefulness=Dreaming. Relaunch did not displace the dream. The focus-guarded navigation helper aborted BEFORE emitting any D-pad key. No key was sent to an unrelated window.
- No device-wide settings, installed apps (other than the in-place OpenVista update), stored media or network configuration were changed.

## Outstanding validation

- Physical D-pad navigation, Back, Home cards, EN/PT-BR, keyboard, SAF/USB, M3U import, persistence and revocation.
- HLS/DASH/local media playback, codec/HDR, transitions, fallback and recovery, CPU/RAM metrics, 24-hour soak and platform DreamService behavior.
- Provider approval: weather, geocoding and licensed map backdrop unavailable.
- Security/release gates remain disabled: PairingSecurity.REVIEWED=false and UpdateChannel.APPROVED=false. No pairing listener or public update channel was activated.
- This is a debug-artifact package guard, not signed public release validation.

Decision: Keep PR #22 as draft. Local checks PASS only after narrow validation fixes; physical acceptance and external gates are incomplete. Do not merge, publish, or mark stable-v1 passed.