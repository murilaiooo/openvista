# OpenVista: agent instructions

OpenVista completed **eight product interview rounds (D-01–D-78)**, with C-01 overriding first-party catalog assumptions. The Kotlin TV application, tests and official U.S. NWS adapter now live in the development codebase and have partial physical Hisense evidence. **This is a local development preview, not a public/stable release.** Start at [the canonical status](docs/STATUS.md) and [live roadmap](docs/roadmap.md); dated research plans are historical. Never claim other-device compatibility, full media matrix coverage, Fire OS DreamService selection or unattended installation without reproducible evidence.

## Working agreements

- **English-only repository:** Write all README/documentation, code comments, UI source strings, configuration descriptions, GitHub issues, pull requests, and commit messages in **English**, regardless of the language used by the owner in conversation. Preserve official proper nouns, URLs, and external identifiers.

- Treat `docs/product/decisions.md` as the **historical record of 78 owner-approved decisions**, with the later **C-01 user-managed content clarification** overriding first-party catalog assumptions. See `docs/product/user-managed-content.md` and `docs/product/v1-acceptance.md` before implementing source handling. `docs/product/vision.md` summarizes requirements; research findings and proposed architectures are not approvals.
- Discovery interviews are complete. Research facts first; ask the owner only about proven blockers or changes in scope, using relevant `grilling` / `domain-modeling` skills. Do not reopen settled choices without an evidence-backed reason.
- Never silently turn a proposal into a confirmed requirement.
- Work in small, dependency-aware GitHub issues and branches. Prefer research proofs and vertical slices to architecture speculation. Keep `main` coherent; do not overwrite uncommitted work.
- Prefer original Android platform docs and upstream sources for technical assertions.
- For device changes, explicitly preserve the user's launcher, existing apps, and OS settings; seek consent before changing device-wide screensaver or ADB settings.
- For planning/spec changes, consult [planning audit](docs/reviews/2026-10-25-planning-audit.md), [critical path](docs/implementation/critical-path.md), [78-ID traceability](docs/product/requirement-traceability.md) and [P0 research issues](docs/implementation/priority-plan.md). Do not call documentation checks Android tests.
- Provide reliable fallback when live feeds fail, when the device lacks a decoder, or when the weather source is unavailable.
- Prioritize low CPU/memory, stable hardware decoding, no video burn-in hotspots, accessible D-pad focus, and minimal permissions.
- **Do not ship any live-camera URLs, streaming playlists, hosted catalogs, research feed fixtures, or videos in the app.** Users supply their own sources and local media. Keep research fixtures out of all public APK/release assets. Before any distribution, run [the package-content guard](docs/testing/release-content-guard.md) on the actual signed artifact and complete source/build reviews.
- Do not hardcode API keys, user locations, or authentication material into version control.
- GitHub Actions is not a substitute for local reproducible tests and physical TV validation.
- **The development APK stays local for tests on Hisense 58U6HF / Fire OS 7.** Other TVs are unverified; first GitHub binary release requires explicit owner approval. Do not change TV settings/apps without the owner's specific authorization; the owner currently requires the existing system screensaver to remain **disabled** until they say otherwise.
- **Do not restart or redo the diagnostic/first-player milestones.** The TV application, Media3, SAF/M3U, libraries, weather and optional Dream source already exist. Use the [current status](docs/STATUS.md) for remaining tests, the [module guide](docs/implementation/production-module-guide.md) for ownership and the older [first-APK plan](docs/implementation/first-apk-sequencing-review-2026-10-25.md) as historical rationale. QR/LAN pairing and the public updater remain **disabled** pending security/release approval.
- Treat Daylight/Golden Hour and Display care as opt-in research concepts, not approved shipped features. Never include a procedural demo shader or video without explicit owner approval.
- Review public copy with [No AI Slop / code quality guidelines](docs/quality/writing-and-code-review.md); use project-local `no-ai-slop` for human-readable prose and optional telemetry-disabled `aislop` for Python, **not Kotlin**.
- Public v1 must work with **zero bundled streams**, user-managed source URL/list import, and meaningful empty states; verify no research stream URLs or playlist assets end up in the APK. Retain the documented 24-hour stability, privacy, and real-device playback gates.
- Target Fire OS 7 / Android 9 **cannot use API-31-only `SessionParams.setRequireUserAction`**; never promise unattended APK self-installation. Use supported system install confirmation when required.

## Agent skills

### Issue tracker

GitHub Issues in `murilaiooo/openvista`. See `docs/agents/issue-tracker.md`.

### Triage labels

Use `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, and `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one root `GLOSSARY.md`, decisions under `docs/adr/`. See `docs/agents/domain.md`.

### Installed skills

Look for `.agents/skills/<name>/SKILL.md` (Codex) and `.claude/skills/<name>/SKILL.md` (Claude Code). Use the **applicable** skills, not every one on every task. Start with `grill-with-docs` for discovery, `research` for external facts, `to-spec` and `to-tickets` when decisions are approved, `tdd` for logic, and `code-review` before merge. See `docs/skill-selection.md`.

## Remaining quality gates

1. Document target OS/API support and unsupported device policies.
2. Verify on-device D-pad navigation, folder-grant persistence, lifecycle and screen-off transitions.
3. Test rapid source switching, long-run playback, network loss and recovery, corrupt local media, unavailable weather, and codecs.
4. Record actual device model, OS, codec, bitrate, resolution, performance and validation result; do not generalize from one device.
5. Preserve the ability to uninstall without side effects and revert any optional system-settings changes.
