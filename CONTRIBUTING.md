# Contributing to OpenVista

Owner product discovery is complete (78 confirmed decisions). We are validating feasibility and preparing the first **local Hisense-only** engineering APK. Review [canonical decisions](docs/product/decisions.md), [v1 acceptance](docs/product/v1-acceptance.md), [P0 implementation plan](docs/implementation/priority-plan.md), [risk audit](docs/reviews/2026-10-25-planning-audit.md) and [glossary](GLOSSARY.md) before proposing code.

## Issues
- Report one problem or enhancement per issue. Include expected vs observed behavior and reproduction when applicable.
- For playback bugs: TV manufacturer/model, Android TV/Google TV/Fire OS version, codec, resolution, source type, approximate bitrate, steps, logs with private tokens removed.
- For camera providers: include official terms allowing integration and attribution. **Do not post private endpoints or tokens.**
- For feature proposals: link accepted decisions, success conditions and test strategy.

## Pull requests
- Branch from current main, keep changes small and single-purpose.
- Link issue/spec, include local test commands and physical device evidence when relevant.
- Document compatibility regressions and rollback, especially DreamService and permission changes.
- Do not add copyrighted media or third-party code without license compatibility review.
- Tests must be reproducible locally. GitHub Actions is optional, not proof by itself.

## Development environment
Gradle and Android source files have not been scaffolded yet. Architecture in `docs/architecture/overview.md` and ADR-0001 remains **proposed** until tested. Follow recorded technical evidence rather than treating proposals as facts. Use only relevant project-local skills in `.agents/skills` / `.claude/skills` and instructions in `AGENTS.md`.

## Conduct
Be respectful, specific, and pragmatic in technical discussions. Credit the original authors of reused compatible work.
