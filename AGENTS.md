# OpenVista: agent instructions

OpenVista completed **eight product interview rounds (D-01–D-78)**. It is now in **engineering research / feasibility validation**; there is **no OpenVista app code or APK** yet. Never claim tested device support, approved providers, working streams or fully automatic installation without reproducible evidence.

## Working agreements

- Treat `docs/product/decisions.md` as the **canonical 78 owner-approved decisions** and `docs/product/v1-acceptance.md` as release gates. `docs/product/vision.md` summarizes requirements; research findings and proposed architectures are not approvals.
- Discovery interviews are complete. Research facts first; ask the owner only about proven blockers or changes in scope, using relevant `grilling` / `domain-modeling` skills. Do not reopen settled choices without an evidence-backed reason.
- Never silently turn a proposal into a confirmed requirement.
- Work in small, dependency-aware GitHub issues and branches. Prefer research proofs and vertical slices to architecture speculation. Keep `main` coherent; do not overwrite uncommitted work.
- Prefer original Android platform docs and upstream sources for technical assertions.
- For device changes, explicitly preserve the user's launcher, existing apps, and OS settings; seek consent before changing device-wide screensaver or ADB settings.
- For planning/spec changes, consult [planning audit](docs/reviews/2026-10-25-planning-audit.md), [critical path](docs/implementation/critical-path.md), [78-ID traceability](docs/product/requirement-traceability.md) and [P0 research issues](docs/implementation/priority-plan.md). Do not call documentation checks Android tests.
- Provide reliable fallback when live feeds fail, when the device lacks a decoder, or when the weather source is unavailable.
- Prioritize low CPU/memory, stable hardware decoding, no video burn-in hotspots, accessible D-pad focus, and minimal permissions.
- Do not ship sample live-camera URLs or video assets until usage/redistribution permission and uptime are verified.
- Do not hardcode API keys, user locations, or authentication material into version control.
- GitHub Actions is not a substitute for local reproducible tests and physical TV validation.
- **First APK stays local for tests on Hisense 58U6HF / Fire OS 7**. Other TVs are unverified; first GitHub binary release requires explicit owner approval. Do not install on or change a TV in a planning/documentation audit.
- v1 requires >=25 **authorized and actually playable HLS/DASH webcam streams**, documented 24h soak, privacy/license review; never mistake static webcam images or restricted embeds for native live streams.
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

## Quality gates (once app code exists)

1. Document target OS/API support and unsupported device policies.
2. Verify on-device D-pad navigation, folder-grant persistence, lifecycle and screen-off transitions.
3. Test rapid source switching, long-run playback, network loss and recovery, corrupt local media, unavailable weather, and codecs.
4. Record actual device model, OS, codec, bitrate, resolution, performance and validation result; do not generalize from one device.
5. Preserve the ability to uninstall without side effects and revert any optional system-settings changes.
