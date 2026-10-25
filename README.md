<div align="center">

<img src="assets/branding/openvista-cover.png" alt="OpenVista — A window to everywhere. An atmospheric illustration of mountains, water, and a glowing horizon." width="100%" />

<br />

**Your screen. Your scenery. Your moment.**

An open-source ambient video experience designed for **Android TV, Google TV, and Fire TV**.

Turn your TV into a calm, immersive window with live streams and your own local videos — enhanced by an elegant clock, weather, and distraction-free controls.

<br />

[![Project status](https://img.shields.io/badge/status-in%20development-4DDBC4?style=for-the-badge&labelColor=11283F)](docs/roadmap.md)
[![License: MIT](https://img.shields.io/badge/license-MIT-84C7FF?style=for-the-badge&labelColor=11283F)](LICENSE)
[![Android TV](https://img.shields.io/badge/platform-Android%20TV-9EC5C1?style=for-the-badge&labelColor=11283F)](docs/testing/compatibility.md)

[**Explore the vision**](docs/product/vision.md) · [**Project roadmap**](docs/roadmap.md) · [**Contribute**](CONTRIBUTING.md) · [**Issues**](https://github.com/murilaiooo/openvista/issues)

</div>

---

## A beautiful reason to leave the TV on

OpenVista is being built around a simple idea: a television can become something more than a screen full of menus.

Imagine a quiet beach at sunrise, a city skyline after dark, or a favorite landscape playing in the background — with just the information you choose, arranged exactly where you want it.

The experience is designed for the couch, not a phone: **immersive visuals, effortless remote navigation, and as little interface as possible.**

## What OpenVista is designed to do

<table>
<tr>
<td width="50%" valign="top">

### 🌎 Live scenes

Play compatible **HLS and MPEG-DASH** streams you add to your collection. Organize cameras into playlists, switch scenes with the remote, and explore location-based entries through a planned interactive map.

</td>
<td width="50%" valign="top">

### 🎞️ Your videos

Select video folders on internal storage or USB drives, including nested folders. Build playlists, shuffle or loop your favorites, and resume a video where you left off.

</td>
</tr>
<tr>
<td valign="top">

### ☀️ Time & weather

Add a clean clock, current conditions, or a detailed seven-day forecast. Choose a city, customize the visual style, and keep the scenery front and center.

</td>
<td valign="top">

### ✨ Made for the big screen

Full-screen playback, remote-first controls, quick source switching, customizable transitions, and thoughtful performance handling for TV hardware.

</td>
</tr>
</table>

### Thoughtful by design

- **Personalize the experience:** flexible playlists, favorites, clock and weather layouts, plus simple and advanced settings.
- **Keep things effortless:** default five-minute scene rotation, with adjustable timing and automatic recovery from unavailable sources.
- **Stay in control:** audio muted by default, local settings storage, no required account, no ads, and no automatic analytics.
- **Choose how it runs:** open it like a regular TV app; use system screensaver integration where your device allows it.
- **Use the remote naturally:** press OK for a compact control menu, switch between cameras and videos, then return to an unobstructed view.

## How it will work

1. **Choose your source.** Add a compatible live stream or select videos stored on your TV or an attached drive.
2. **Make it yours.** Arrange sources into playlists, save favorites, and set the scene rotation you prefer.
3. **Enjoy the view.** Enter full-screen ambient mode, optionally add weather and time, and let your TV fade into the background.

> [!IMPORTANT]
> **OpenVista is in active development.** The app is not downloadable yet, and the features above describe its planned experience rather than completed functionality. Device support and system screensaver integration will be confirmed through real hardware testing.

## Development status

| Area | Status |
| :-- | :-- |
| Product direction and interaction requirements | Defined |
| Media playback, file browsing, and TV navigation | Research and prototype planning |
| Automatic screensaver integration | Device-dependent; not yet verified |
| Android TV / Google TV / Fire TV compatibility | Not yet verified |
| First development APK | Not yet available |
| Public release | Not scheduled |

The initial engineering test device is a **Hisense 58U6HF running Fire OS 7**. This does **not** imply support for other devices yet.

OpenVista will be developed as an **original Kotlin Android TV application**, with **Media3 / ExoPlayer** and Android's supported storage and system APIs evaluated through real prototypes.

## Project documentation

| Start here | What you'll find |
| :-- | :-- |
| [Product vision](docs/product/vision.md) | What we're building and why |
| [TV experience](docs/product/experience.md) | Remote navigation, playback, overlays, and customization |
| [Roadmap](docs/roadmap.md) | What's next and how work is prioritized |
| [Implementation plan](docs/implementation/priority-plan.md) | Engineering stages, dependencies, and acceptance goals |
| [Technical research](docs/research/platform-feasibility-2026-10-25.md) | Platform capabilities and open questions |
| [Architecture](docs/architecture/overview.md) | Candidate components and boundaries |
| [Compatibility](docs/testing/compatibility.md) | Test scope and device-specific limitations |
| [Contributing](CONTRIBUTING.md) | How to propose improvements and collaborate |

The [decision log](docs/product/decisions.md) preserves the approved requirements and design history. Research-only test assets are kept separate from runtime content.

## Build with us

OpenVista is an open-source project, and thoughtful contributions are welcome — especially around Android TV navigation, media playback, accessible interface design, storage, weather, performance, and device testing.

Browse the [open issues](https://github.com/murilaiooo/openvista/issues), read the [contribution guide](CONTRIBUTING.md), and keep proposed changes focused and reproducible.

**Repository language:** English for documentation, code comments, issues, and pull requests.

---

<div align="center">

**A window to everywhere.**

Made for quiet moments, curious minds, and bigger screens.

[MIT License](LICENSE) · [Roadmap](docs/roadmap.md) · [Contributing](CONTRIBUTING.md)

</div>
