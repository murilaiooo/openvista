# OpenVista: agent instructions

OpenVista is in **product discovery**, not active feature implementation. Never claim an APK or tested device support until verified.

## Working agreements

- Treat `docs/product/vision.md` as confirmed scope; `docs/product/decisions.md` lists unresolved choices.
- Ask design decisions through the installed `grill-with-docs` / `grilling` skills. Update the glossary and ADRs when decisions are made.
- Never silently turn a proposal into a confirmed requirement.
- Work in small GitHub issues and branches. Keep `main` coherent; do not overwrite uncommitted work.
- Prefer original Android platform docs and upstream sources for technical assertions.
- For device changes, explicitly preserve the user's launcher, existing apps, and OS settings; seek consent before changing device-wide screensaver or ADB settings.
- Provide reliable fallback when live feeds fail, when the device lacks a decoder, or when the weather source is unavailable.
- Prioritize low CPU/memory, stable hardware decoding, no video burn-in hotspots, accessible D-pad focus, and minimal permissions.
- Do not ship sample live-camera URLs or video assets until usage/redistribution permission and uptime are verified.
- Do not hardcode API keys, user locations, or authentication material into version control.
- GitHub Actions is not a substitute for local reproducible tests and physical TV validation.

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
