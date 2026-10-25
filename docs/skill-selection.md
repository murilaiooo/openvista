# Matt Pocock skills selected for OpenVista

Source: [mattpocock/skills](https://github.com/mattpocock/skills). Installed on 2026-10-24 using `npx skills@latest add`, as **project-local copied skills** for Codex (`.agents/skills`) and Claude Code (`.claude/skills`). `skills-lock.json` records sources. Install does not itself execute a skill.

## Engineering (20)
| Skills | When to use |
| --- | --- |
| `ask-matt`, `setup-matt-pocock-skills` | Choose workflows and configure issue tracking/domain documentation. |
| `grill-with-docs`, `domain-modeling`, `wayfinder` | Product interviews, vocabulary, hard design decisions. |
| `research`, `prototype` | Primary-source investigation and throwaway feasibility experiments. |
| `to-spec`, `to-tickets`, `triage` | Approved requirements, dependency-aware tickets and status labels. |
| `codebase-design`, `improve-codebase-architecture` | Module seams, complexity and periodic audits once app code exists. |
| `tdd`, `implement`, `implement-spec` | Bounded development in proven, reviewable increments. |
| `diagnosing-bugs`, `code-review`, `pr`, `retro` | Debugging, review evidence, PR quality and continuous improvement. |
| `wizard` | Optional instructions for steps a human must perform. |

## Productivity (5)
- `grilling`: core design interview loop.
- `handoff`: preserve decisions across chat sessions.
- `to-questionnaire`: document questions requiring external stakeholder input.
- `writing-for-agents`: concise, maintainable agent instructions.
- `wait-what`: explain confusing choices using project vocabulary.

## General (2)
- `git-guardrails-claude-code`: safe Git mutations in agent workflows.
- `setup-pre-commit`: establish local pre-commit feedback once code exists.

**Total: 27 selected Matt Pocock skills.** Excluded in-progress/TS-only teaching/content-writing/migration skills unrelated to this native Android TV project. Use relevant skills for each task, not all 27 simultaneously. The [planning audit](reviews/2026-10-25-planning-audit.md) records which skill families were applicable to reviewing the documentation.

**Additional skill (2026-10-25):** `no-ai-slop` from [petergyang/no-ai-slop](https://github.com/petergyang/no-ai-slop), installed in both project-local agent skill directories (MIT attribution retained). Use for editing documentation, README, UI microcopy and release notes. Optional Python code scanner [scanaislop/aislop](https://github.com/scanaislop/aislop) is **not a skill** and does not support Kotlin; use [quality instructions](quality/writing-and-code-review.md).

Use only skills applicable to the current task. For **planning audit**: research (primary sources), domain-modeling (vocabulary), codebase-design (future module seams), wayfinder (dependency frontier), to-spec/to-tickets (acceptance and small tasks), triage (issue clarity), writing-for-agents (instructions), and retro (mechanical audit checks). Implement/TDD/code-review/prototype apply when executable code or relevant changes exist. Keep copies synced intentionally and review upstream edits before adoption; don't claim all 27 executed when some require an app, a PR or a running prototype.
