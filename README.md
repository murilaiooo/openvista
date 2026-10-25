# OpenVista

**Turn your Android TV into a window to the world.**

OpenVista is a planned, open-source Android TV screensaver and immersive ambient-video app. It will play **live public webcams** or **your own local looping videos**, with optional clock and weather information.

> **Status: discovery / product definition.** This repository currently contains research, project setup, and selected engineering agent skills. There is **no working OpenVista APK yet**. Features below are a roadmap, not a release claim.

## Product foundations

The following is already agreed:

- **TV-first:** designed for Android TV devices generally, not a single TV model. Include Google TV and Fire TV/Fire OS in the compatibility test plan.
- **Two content modes:** (1) live cameras around the world, and (2) user-selected downloaded videos stored in a folder on the TV or attached storage.
- **Two entry points:** a manually opened TV application plus automatic screensaver mode where the OS allows it.
- **Live sources:** both a browsable webcam catalog and user-provided permitted stream URLs.
- **Home:** Cameras, My Videos and Favorites.
- **Camera discovery:** a curated catalog, **interactive world map**, and permitted manual URLs.
- **Automatic rotation:** user-configurable intervals between 1 and 60 minutes; unavailable cameras are immediately skipped.
- **My videos:** internal-storage or USB folders, ordered playback, shuffle and multiple local-video playlists; camera playlists remain separate.
- **Video playback:** fill/crop the screen by default, plus automatic live quality selection with a configurable cap where the source supports it.
- **Information overlays:** fully customizable clock, weather conditions and 7-day forecast, switchable between camera location and viewer location; include presets and a visual editor.
- **Localization:** Brazilian Portuguese and English at launch.
- **Audio:** muted by default with an option to enable sound.
- **Free by design:** no advertisements and no user account required.
- **First release:** a downloadable APK via GitHub when implementation and compatibility tests pass.
- **Future library:** explore a discoverable library of curated live wallpapers and optional integrations. Do not assume media is freely redistributable.

**18 decisions have been confirmed across two product interview rounds.** Exact providers, player controls, overlay defaults, weather integration and physical-device compatibility are **still open**. See [Product decisions](docs/product/decisions.md).

## Proposed experience (not implemented)

1. Open OpenVista from the TV launcher; choose **Cameras**, **My Videos**, or **Favorites**.
2. Discover cameras through the catalog or an interactive map, or select permitted custom URLs; browse video folders on internal or USB storage.
3. Watch scenes in immersive full-screen playback with automatic 1–60-minute rotation, immediate camera skip on failure, and user-defined camera/video playlists.
4. Customize clock, local time, location and 7-day forecast using presets or a visual overlay editor.
5. Where supported by the TV OS, select OpenVista as the system screensaver. Devices that block third-party screensavers can still use the manual app mode.

## Compatibility is device-specific

A normal Android TV app can open manually on supported devices. **Automatic system-screensaver integration is not guaranteed**: Android/Google TV and Fire OS versions differ in DreamService policy, OEM UI, and permissions. Avoid privileged permissions, root requirements, or irreversible ADB modifications.

The first hardware target for validation is a **Hisense 58U6HF / Fire OS 7**, but it is a test device, **not the product's compatibility boundary**. Validate on Google TV and other Android TV devices before making broad claims.

## Technical direction to investigate

| Area | Candidate | Status |
| --- | --- | --- |
| App | Kotlin, native Android TV | Proposed |
| Media | AndroidX Media3 / ExoPlayer (MP4, HLS, device-supported codecs) | Proposed |
| Screensaver | Android DreamService + regular fullscreen Activity | Feasibility and OS compatibility to verify |
| Local files | Android Storage Access Framework, URI permission persistence | Research needed per device |
| Weather | Licensed forecast provider, caching, offline fallback | Provider and license undecided |
| Remote | D-pad-first navigation and accessible focus states | Product requirement |
| Catalog | Source metadata + verified permission to display streams | Provider contract undecided |

No Gradle project or application code has been generated yet. The architecture will be decided after the product interview and targeted prototypes.

## Roadmap and documentation

- [Vision and requirements](docs/product/vision.md)
- [Decisions and brainstorming questions](docs/product/decisions.md)
- [Proposed architecture](docs/architecture/overview.md)
- [Reference projects and official sources](docs/research/references.md)
- [Content rights and security](docs/SECURITY_AND_CONTENT.md)
- [Delivery milestones](docs/roadmap.md)
- [Glossary](GLOSSARY.md)
- [Agent guidance](AGENTS.md)
- [Engineering skills selected](docs/skill-selection.md)
- [Contributing](CONTRIBUTING.md)

## Development workflow

1. Resolve high-impact decisions with **grill-with-docs** and record ADRs.
2. Use **research**, **to-spec**, and **to-tickets** to turn approved decisions into implementation-sized GitHub issues.
3. Prototype device-sensitive playback, folder selection, and DreamService before committing to a large architecture.
4. Build vertical slices using **tdd** where feasible; test on emulator and physical hardware.
5. Run **code-review** and regression checks before merging small PRs. Never claim cross-device support without testing it.

### Matt Pocock skills

27 relevant skills from [mattpocock/skills](https://github.com/mattpocock/skills) are vendored for Codex under `.agents/skills/` and Claude Code under `.claude/skills/`, using the upstream `skills` installer. The install lock is kept in `skills-lock.json`.

From the repository root, to update the installed skills:

```sh
npx skills@latest update -p -y
```

See [skill selection](docs/skill-selection.md) for why these skills were chosen. Review upstream changes before committing updates.

## License

The repository's code is released under [MIT](LICENSE), unless otherwise indicated. Third-party videos, webcams, forecasts, imagery, and third-party code retain **their own licenses and permissions**; MIT does not grant redistribution rights to them.
