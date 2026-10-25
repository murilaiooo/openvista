# OpenVista

**Turn your Android TV into a window to the world.**

OpenVista is a planned, open-source Android TV screensaver and immersive ambient-video app. It will play **live public webcams** or **your own local looping videos**, with optional clock and weather information.

> **Status: discovery / product definition.** This repository currently contains research, project setup, and selected engineering agent skills. There is **no working OpenVista APK yet**. Features below are a roadmap, not a release claim.

## Product foundations

The following is already agreed:

- **TV-first:** designed for Android TV devices generally, not a single TV model. Include Google TV and Fire TV/Fire OS in the compatibility test plan.
- **Two content modes:** (1) live cameras around the world, and (2) user-selected downloaded videos stored in a folder on the TV or attached storage.
- **Two entry points:** a manually opened TV application plus automatic screensaver mode where the OS allows it. The app must **never auto-launch on TV boot**.
- **Live sources:** both a browsable webcam catalog and user-provided permitted stream URLs.
- **Home:** Cameras, My Videos and Favorites as three large image-backed cards; Favorites contains separate camera and video sections.
- **Camera discovery:** broad curated categories (beaches, cities, mountains, wildlife, landmarks, ski resorts and ports), **2D world map with clustered markers and side list**, and permitted manual URLs.
- **Live catalog:** online metadata updates with local fallback, reviewed community suggestions, country/city/category/favorites/availability filters, plus an optional panel for name, country, local time, resolution, source and verified status.
- **Custom URLs:** add a camera with the TV keyboard or a planned secure QR-assisted phone flow.
- **Automatic rotation:** **5 minutes by default**, configurable from 1 to 60 minutes; unavailable cameras are immediately skipped.
- **My videos:** internal/USB folders **with automatic recursive subfolder scanning**, multiple local-video playlists (separate from camera playlists), sequence/shuffle, and automatic next-video playback with playlist repeat.
- **Playlist management:** create, rename, reorder, duplicate and delete playlists; add many items at once, remove and freely reorder items. Camera playlists support sequence or shuffle, with **shuffle as the initial default**.
- **Local media compatibility:** attempt **MP4, MKV, WebM and MOV** when supported by each TV's decoders, favor stable compatible playback, skip corrupt/unsupported files with unobtrusive notices and optional diagnostics.
- **Local player:** fixed **10-second** forward/back seeks; resume each local video **where playback stopped**; **no subtitles** for local videos.
- **Player UI:** pressing **OK** opens an automatically hiding **5-second contextual controls menu**, including quick Cameras/My Videos switching; technical playback diagnostics are optional.
- **Settings:** simple categorized Basic settings with a separate Advanced section. Favorites, playlists and settings are **automatically stored on the TV only**; no cloud sync or import/export in the initial product.
- **Video playback:** fill/crop the screen by default, plus automatic live quality selection with a configurable cap where the source supports it.
- **Immersive design:** full-screen scenery with an almost invisible interface; contextual camera controls differ from local-video seek controls.
- **Information overlays:** compact clock and current weather by default, detailed 7-day forecasts (temperature, precipitation, wind, humidity, sunrise/sunset when supplied) on demand. Choose manual city search or optional device location; switch between camera and viewer weather contexts, with presets and a simple/advanced visual editor.
- **Display care:** separately configurable widget repositioning and hiding after inactivity (risk reduction, not burn-in prevention guarantee).
- **Setup and transitions:** short skippable setup, plus a user choice of instant or smooth scene switches according to device performance.
- **Idle and power:** optional **1/3/5/10/15/30-minute screensaver activation** where supported, optional schedule and stay-awake options subject to TV power policies. Automatic playback **resumes the last camera or video**; OK reveals controls, Back exits, arrows navigate where OS dispatch permits.
- **Resource priority:** prioritize stable, fluid playback through adaptive quality/effects; pause unnecessary background playback and use cached updates. On USB removal, explain the missing drive and allow another source.
- **Localization:** Brazilian Portuguese and English at launch; auto-detect TV region with manual language, clock-format and temperature overrides.
- **Audio:** muted by default with an option to enable sound.
- **Free by design:** no advertisements and no user account required.
- **First release:** a downloadable APK via GitHub when implementation and compatibility tests pass.
- **Fallback and connectivity:** if all webcams fail or the network drops, switch to the user-configured local playlist when available; check reconnection **without interrupting its current video**. When an available camera returns, **finish the local video and return to live cameras**, unless the user chooses to stay local. Otherwise show a clear retry notice.
- **Future wallpaper library:** deferred beyond the first APK. Local folders and live cameras come first. Do not assume media is freely redistributable.

**68 product decisions have been confirmed across seven interview rounds.** Source/provider rights, weather API, secure QR pairing, actual decoder compatibility, cross-device DreamService behavior and first-release acceptance gates remain **under research**. See [Product decisions](docs/product/decisions.md), [UX brief](docs/product/experience.md), [player and settings](docs/product/settings-and-player.md) and [runtime policy](docs/product/runtime-and-recovery.md).

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
- [TV UX and interaction brief](docs/product/experience.md)
- [Camera catalog and weather requirements](docs/product/content-and-weather.md)
- [Screensaver runtime, power and recovery](docs/product/runtime-and-recovery.md)
- [Local files, playlists and player behavior](docs/product/local-media-and-playlists.md)
- [Settings, player controls and favorites](docs/product/settings-and-player.md)
- [First-release blockers and validation plan](docs/product/first-release-blockers.md)
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
