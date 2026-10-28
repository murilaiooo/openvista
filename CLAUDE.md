# Claude Code: OpenVista current working contract

Read this file before changing code. This is a **development preview**, not a certified public release.

## Start here, in order
1. [Current engineering status](docs/STATUS.md) and [October 27 finalization handoff](docs/implementation/claude-release-handoff-2026-10-27.md).
2. [Owner-approved decisions](docs/product/decisions.md), [user-managed content (C-01)](docs/product/user-managed-content.md), and [v1 acceptance](docs/product/v1-acceptance.md).
3. [Agent instructions](AGENTS.md), [skills guidance](docs/skill-selection.md), [design spec](docs/design/tv-design-system.md), and [Issue #36 validation matrix](docs/design/issue-36-validation.md).
4. [Playback regression record](docs/testing/live-video-freeze-regression-2026-10-27.md), [display care](docs/design/ambient-display-care-2026-10-27.md), and [weather high correction](docs/testing/missing-today-high-weather-qa-2026-10-27.md).

## Source of truth and working branch
- Resume from the latest `main` commit. PR #37 merged the owner-TV-matched source after PR #40 and the latest bilingual README work; older `codex/ui-redesign-36`, stale worktrees and prior PRs are historical development branches.
- [PR #37](https://github.com/murilaiooo/openvista/pull/37) was merged into `main` on October 28. The repository stays private and **no signed/public release** has been authorized.
- The physical Hisense Fire OS 7 installed APK was SHA-256-identical to the integrated local debug build (18,758,735 bytes). Read [the October 28 verification](docs/testing/integrated-tv-snapshot-2026-10-28.md) for exact scope. This does not prove 24-hour stability or production signing.
- Three real TV captures in `assets/screenshots/` are README previews. They are **not** the owner's three original design reference images. The owner will provide Home, Settings, and Live Camera Playback references separately. Copy layout and design, never the images' sample text or values.

## Non-negotiable priorities
- Owner requests a thorough **implementation** and optimization review: fix proven issues, improve launch/navigation/playback/memory/24-hour stability, avoid speculative churn, and provide reproducible evidence.
- Make Home, Settings, playback and secondary screens one coherent TV design system. Keep **one unified Settings menu**. Follow the three provided reference images as closely as native Android TV/Fire OS constraints allow, including microspacing, typography, focus and subtle overlay contrast.
- **Display care is a product differentiator:** reposition complete logo, source, viewer/weather and forecast groups periodically and safely (default 5 min; configurable), retain separately configured inactivity hiding. Never leave a permanently static forecast/footer, overlap groups or freeze video to create a cosmetic transition. No system can guarantee zero burn-in.
- Prioritize actual live video motion and decoder continuity over an artificial no-flash screenshot. **Do not reintroduce PixelCopy/CameraFrameHold/fullscreen still-image overlays**: they caused the October 27 freeze regression, fixed in `d90c535`.
- Preserve user-managed HLS/DASH/M3U and local video/SAF library, playlist and favorites state; real source/viewer timezones and weather; NWS with Open-Meteo international fallback and same-day high completion; locale EN/pt-BR; remote focus; offline fallback and clear loading states.
- Every setting visible must be functional and persist. No hardcoded sample cities, forecast, usernames, media sources, fake camera previews or third-party reference images.
- Never ship streams, playlists, user media, research catalog URLs, private TV/library data or credentials. Keep private local research paths and scratch screenshots out of commits and public artifacts.
- Keep all repository text, code comments, commits and PR descriptions in English. Load and apply **relevant** repository skills from `.claude/skills` / `.agents/skills`; do not run irrelevant skills blindly.

## Before declaring ready
Run the Android unit tests, debug build and Android Lint locally; test relevant source-core and diagnostics modules, validate 720p/1080p UI and D-pad on the **real** Hisense, source switching, weather failures, all burn-in phases, settings migration, SAF grants, resume, 24-hour playback and performance metrics. Run the signed artifact content guard on the actual candidate, check public Git history/privacy/licensing and get explicit owner approval before publishing APK or changing repository visibility. Never equate a successful compile, current README screenshot or administratively closed issue with production acceptance.
