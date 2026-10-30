# Using Impeccable for OpenVista's native TV UI

OpenVista uses the [Impeccable](https://github.com/pbakaus/impeccable) design vocabulary. The product context is at [PRODUCT.md](../../PRODUCT.md), the incumbent design contract at [DESIGN.md](../../DESIGN.md), and the [October 30 critique](../reviews/2026-10-30-impeccable-native-tv-critique.md) records a baseline.

## Local installation (project-specific; not required to read the critique)

From the root of the **authorized OpenVista checkout**, with Node/npm available:

```powershell
npx impeccable install --providers=codex,claude --scope=project
```

Review installer prompts and any hooks before enabling them. Reopen the coding tool so its skills appear. Do not install the entire upstream repository or bundle its screenshots, scripts or binary into the APK.

Suggested subsequent commands (narrow targets, one at a time):
- `$impeccable critique player/src/main/java/com/openvista/prototype/ui/LibraryScreens.kt`
- `$impeccable audit player/src/main/java/com/openvista/prototype/ui/SettingsScreens.kt`
- `$impeccable onboard source editor and first-run empty Cameras library`
- `$impeccable distill Appearance settings without changing the seven-category navigation`
- `$impeccable polish AmbientOverlay legibility on high-key video`

## Native-platform limits

- `impeccable detect`, browser overlays, and web live mode are for HTML/DOM surfaces. OpenVista's Kotlin Android Views must be evaluated with native code inspection, real APK captures, D-pad traces and device logs. An empty web detector report is **not** a passing Android audit.
- Treat Impeccable as a reviewer and playbook, not an autonomous agent that has access to the device. The source of truth remains `AGENTS.md`, current `docs/STATUS.md`, `docs/product/decisions.md`, C-01, and owner-approved references.
- Start from the current implemented visual system. The owner requested critical review, **not replacement** of the three-panel Home, atlas Settings or four-zone overlay.
- Read [Issue #54](https://github.com/murilaiooo/openvista/issues/54) before opening duplicate visual/interaction issues. Save reproducible defects with source path, current commit, screenshots, exact D-pad sequence and P0-P3 impact.
- Do not change TV device settings, GitHub releases, media sources, launcher or screensaver without the owner's authority. Do not expose user camera URLs or location data.
- Validate EN/PT-BR, source-empty, weather-unavailable, hardware decoder and 24-hour playback states before making stable-release claims.

The current audit applied the Impeccable 4.8.0 playbooks directly from upstream without executing its installer or web-only detector. This file documents how to make those playbooks available in an authorized local tool session later.
