# Writing and code-quality review

OpenVista's repository files, code comments, PRs, issues and commits must be in **English**, even when instructions are in Portuguese. PT-BR is appropriate inside localized app strings.

## Writing and UX: No AI Slop

Installed project-locally for Codex and Claude at `.agents/skills/no-ai-slop/SKILL.md` and `.claude/skills/no-ai-slop/SKILL.md`, from [petergyang/no-ai-slop](https://github.com/petergyang/no-ai-slop) (MIT, copyright 2026 Peter Yang).

Use this editorial skill for README, UI labels, onboarding, contributor guidance and release notes. Preserve meaningful caveats, original product voice, technical precision and context. Cut repetitive copy, AI-sounding filler, dramatic contrasts, unsupported marketing claims and gratuitous headers. It is an **editing guide, not an authorship detector**, and does not create new product approvals.

## Code: aislop

[scanaislop/aislop](https://github.com/scanaislop/aislop) is a MIT-licensed **optional local** deterministic scanner (copyright 2025 heavykenny). Pin the version, disable all telemetry and inspect individual findings rather than blindly accepting a score:

```powershell
$env:AISLOP_NO_TELEMETRY = "1"
npx --yes aislop@0.18.1 scan --json
```

`.aislop/config.yml` also sets `telemetry.enabled: false`. No GitHub Actions, hosted badge, background monitor, agent hook or automatic source-code rewriting is required.

**Coverage boundary:** aislop covers existing Python scripts but does **not support Kotlin**. Do not advertise a Python-only score as Android app quality. Missing optional Ruff also causes formatting/lint engines to skip. Kotlin quality requires Android lint, compilation, tests, manual D-pad review and evidence on the Hisense TV. Keep [release artifact content checks](../testing/release-content-guard.md) independent from style scans.

License/copyright notices for the copied No AI Slop skill are retained alongside its installed files.
