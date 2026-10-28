<div align="center">

<img src="assets/branding/openvista-cover.png" alt="OpenVista: a serene mountain lake beneath a warm horizon" width="100%" />

# OpenVista

**Your screen. Your scenery. Your moment.**

An open-source ambient video player built in Kotlin for Android TV, Fire TV, and potentially Google TV.

Play your own live cameras and local videos with a minimal, background-free city/time/temperature overlay, configurable source rotation, and one-minute display-care movement.

[![Status](https://img.shields.io/badge/status-development%20preview-4DDBC4?style=flat-square)](docs/STATUS.md)
[![Platform](https://img.shields.io/badge/tested-Hisense%20Fire%20OS%207-84C7FF?style=flat-square)](docs/testing/compatibility.md)
[![License](https://img.shields.io/badge/license-MIT-9EC5C1?style=flat-square)](LICENSE)

[Status](docs/STATUS.md) · [Get started](#run-it-locally) · [Roadmap](docs/roadmap.md) · [Contributing](CONTRIBUTING.md) · [Product decisions](docs/product/decisions.md)

</div>

---

## A window to somewhere you choose

OpenVista is for the moments when a television should feel like a window, not a wall of menus. Pick a live beach camera, a city skyline, or a video from your own collection. Let it play with only the information you want to see.

**You bring the content.** The app ships with **no camera streams, M3U playlists, videos, or hosted catalog**. Add a compatible HLS/MPEG-DASH URL, import your own M3U list, or select media from internal/USB storage. Research-only streams in this repository are **not** part of the APK.

## What's in the development build?

| Area | Implemented behavior | Evidence / limits |
| --- | --- | --- |
| TV experience | Three-card Home, Cameras, My Videos, Favorites, onboarding, remote-first menus, EN/PT-BR | Home, library and basic D-pad/picker flows observed on Hisense; full accessibility pass pending |
| Live sources | Manual HLS/DASH entry, user-selected M3U import, playlists, details, favorites | Original HLS/DASH 720p and M3U import observed; an independent unmerged QA candidate also rendered controlled HLS 1080p and DASH/HEVC 4K test patterns on Hisense. Sustained decoding remains unverified |
| Local videos | Scoped Android file/folder picker, recursive scan, persistence, playlists, player resume | MP4 and selected-folder restoration observed; physical USB removal/revocation and full codec matrix pending |
| Ambient playback | Fullscreen Media3; independent 1-minute live and 5-minute local-video rotation; text-only online city/local-time/temperature overlay with one-minute movement | The minimalist overlay is in a separate QA branch. Real-TV visual approval, exact rotation timing and endurance are pending |
| Weather | NWS-first U.S. forecasts; owner-requested noncommercial preview of Open-Meteo global geocoding/weather with caching and visible GeoNames/Open-Meteo credit | Tucson NWS tested; global online camera auto-location requires physical validation and provider/license review before public release |
| Maps | Local metadata coordinates, schematic map and source list | A licensed map-tile provider is not active |
| System screensaver | Owner-selected OpenVista DreamService on Hisense | Automatic non-test Dream entry and on-screen video/clock/weather observed on Fire OS 7; broader lifecycle tests pending |
| Pairing and updates | Security-gated phone input and verified-update implementation | **Disabled** pending security, release channel and Android installer validation |

See [the dated verification ledger](docs/STATUS.md), [the smart-ambient QA record](docs/testing/smart-ambient-rotation-2026-10-26.md), [the independent Codex QA candidate report](docs/testing/independent-codex-qa-2026-10-26.md), and [Hisense device matrix](docs/testing/compatibility.md). The additional QA candidate is not merged or release-certified. A feature being coded does **not** mean it has passed stable-release acceptance.

## Run it locally

This repository currently contains Android app source and tests, **not a public release APK**.

Requirements: JDK 17, Android SDK (API 36), a compatible Android/Fire TV test device, and a supported Gradle environment. The Gradle wrapper is included.

On Windows, from the repository root:

    gradlew.bat :player:assembleDebug

To run the existing local validation:

    gradlew.bat :player:testDebugUnitTest :source-core:test :diagnostics:testDebugUnitTest :player:lintDebug
    python -m unittest discover -s scripts -p test_*.py
    python scripts/validate_discovery_docs.py
    python scripts/release_content_guard.py player/build/outputs/apk/debug/player-debug.apk

Linux/macOS: use the Gradle wrapper script instead of gradlew.bat. The app module is **player**; the development APK is written to the Android Gradle output directory. Build locally and review the risks before installing; this is not a release distribution or compatibility promise.

Do not install a separate diagnostic app alongside OpenVista on the owner's test Fire TV. The existing debug package is **com.openvista.prototype**, minimum Android API 28. Preserve device launcher, user media grants and system settings.

## Add your scenery

1. Open **Cameras** to add an HLS/DASH URL or import your own M3U playlist with Android's file picker.
2. Open **My Videos** to select an accessible video or folder from the device or attached storage.
3. Choose a source, set Favorites or playlists, and play it fullscreen.
4. Configure separate live-camera and local-video rotation intervals in **Settings > Playback** (defaults 1 and 5 minutes), and configure random or ordered one-minute panel movement in **Settings > Clock and overlays**.
5. The preview can resolve a likely city from a safe prefix of a camera title using the internet. For accuracy, edit a saved source to specify its actual city, country, time zone and coordinates. The media filename/title never appears in the overlay. An unresolved source may show a clearly labeled viewer location or hide the overlay.

The owner-requested private preview uses Open-Meteo/GeoNames for worldwide name lookup and worldwide weather; NWS remains preferred for supported U.S. locations. Provider attribution is visible in small type without a background. The keyless Open-Meteo endpoint is noncommercial and must not be enabled in a public/commercial APK without a separate license, privacy and request-budget review. [Implementation and limitations](docs/testing/minimal-online-overlay-2026-10-26.md). No location permission is required.

## Compatibility and release policy

**Physical test target:** Hisense 58U6HF, Fire OS 7 / Android 9 (API 28). That confirms only the specific observed workflows on that device. Other Fire OS, Android TV and Google TV builds have **not** been verified.

There is no public stable release, no owner-approved signing/update channel and no guarantee of automatic system screensaver or silent APK installation. Publication requires review of the [v1 acceptance gates](docs/product/v1-acceptance.md), 24-hour hardware endurance, security/privacy and license checks, and explicit owner approval.

## Architecture and project map

| Area | Read |
| --- | --- |
| What exists today, proof and blockers | [Current status](docs/STATUS.md) |
| Milestones and what is still outstanding | [Roadmap](docs/roadmap.md) |
| Kotlin modules and ownership | [Architecture](docs/architecture/overview.md) and [module guide](docs/implementation/production-module-guide.md) |
| Original owner decisions (D-01–D-78 and C-01) | [Decisions](docs/product/decisions.md) |
| Weather implementation and Fire TV evidence | [NWS validation](docs/testing/nws-weather-integration-2026-10-26.md) |
| Hardware/software acceptance | [Compatibility](docs/testing/compatibility.md) and [v1 gates](docs/product/v1-acceptance.md) |
| Privacy, safe content handling | [Security and media rights](docs/SECURITY_AND_CONTENT.md) |
| How to help | [Contributing](CONTRIBUTING.md) |

The research and October 25 planning notes are retained as **dated engineering history**, not current status. The canonical summary is [docs/STATUS.md](docs/STATUS.md).

## Contribute

OpenVista is MIT licensed and welcomes small, reviewable improvements to TV navigation, playback reliability, source handling, localization, privacy, weather, and testing.

[Contribution guide](CONTRIBUTING.md) · [GitHub issues](https://github.com/murilaiooo/openvista/issues) · [Source repository](https://github.com/murilaiooo/openvista)

All repository prose, engineering documentation and contributions use **English**. The app intentionally includes **Brazilian Portuguese** UI resources.

---

<div align="center">

**A window to everywhere.**

[License](LICENSE) · [Status](docs/STATUS.md) · [Roadmap](docs/roadmap.md)

</div>
