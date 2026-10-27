# Contributing to OpenVista

OpenVista is a Kotlin/Media3 ambient TV application under the [MIT License](LICENSE). The current repository contains a **development-preview app** and reproducible tests; it is not a published or stable APK.

Please read [current engineering status](docs/STATUS.md), the [live roadmap](docs/roadmap.md) and the [architecture](docs/architecture/overview.md) before opening work. The former issue and PR backlog was closed at the owner's request; **closed does not mean the release requirements were accepted**. New isolated, evidence-backed work is welcome.

## Areas where contributions help

- Reliable Media3 stream switching, codecs and low-resource Fire TV playback.
- TV D-pad focus, accessibility, Back, input and EN/PT-BR localization.
- Scoped USB/SAF recovery, bounded folder walking and duplicate local-media identity.
- Clock and weather presentation, NWS cache/offline handling, user-entered geography.
- Legal map/geocoding provider research and TV-first map interaction.
- Secure optional QR/LAN pairing and platform-compliant update delivery.
- Device-labeled QA, 24-hour stability metrics and security/privacy review.

## Start a change

1. Check [roadmap and known blockers](docs/STATUS.md). Search closed issue/PR history for related code before duplicating a solution.
2. Make a small branch from current main, one coherent change at a time.
3. Follow the [product decisions](docs/product/decisions.md) and [user-managed content policy](docs/product/user-managed-content.md).
4. Use official Android/Media3 APIs, avoid privileged TV changes and support TV remote focus.
5. Provide a local test plan and, when suitable, automated tests. For Fire OS behavior, distinguish physical test evidence from assumptions.
6. Submit a focused PR with behavior, exact tests, supported target, known limitations and rollback. Do not mark tasks accepted without evidence.

## Local checks

Windows workstation with JDK 17, Android SDK API 36 and Python 3:

    gradlew.bat :player:testDebugUnitTest :source-core:test :diagnostics:testDebugUnitTest :player:assembleDebug :player:lintDebug
    python -m unittest discover -s scripts -p test_*.py
    python scripts/validate_discovery_docs.py
    python scripts/release_content_guard.py player/build/outputs/apk/debug/player-debug.apk

Run the Android check on the exact code under review. Use a **local** workstation, not GitHub Actions as a substitute for TV hardware proof. A debug APK guard is not a signed release approval.

## Non-negotiable product boundaries

- **No bundled or hosted live streams, M3U source lists or video wallpapers.** Users provide their own content.
- No passwords, API tokens, user locations, private stream URLs, live-feed research tables, M3U playlists, local media files or personally identifying logs in source control.
- Avoid broad storage permissions and external uploads; stick to user-authorized Android SAF paths.
- No root, boot auto-launch, silent system setting changes, forced screensaver selection or unauthorized app deletion.
- Do not enable LAN pairing or the public updater while their security/distribution gates remain disabled.
- No compatibility claim for an untested device, source codec or 4K/HDR stream.
- Do not publish any APK or release without explicit project-owner authorization.

## Reports and privacy

For a bug report, provide exact model, Fire OS/Android version, protocol, media codec/resolution where known, expected/actual behavior, and safe steps to reproduce. Remove tokens, personal URLs, local paths and sensitive data. For security issues, contact the maintainer privately instead of publishing an exploit.

## Language and licensing

**English is mandatory for source comments, documentation, branch/commit/PR/issue prose and contributor communication in the repository.** App user interface resources may be translated into Brazilian Portuguese and other explicitly supported locales.

Contribute only code, assets and sample evidence you are licensed to share. Research playlist files are not permission to redistribute someone else's live streams.

See [project skills](docs/skill-selection.md), [quality guidance](docs/quality/writing-and-code-review.md), [compatibility gates](docs/testing/compatibility.md) and [v1 acceptance](docs/product/v1-acceptance.md).
