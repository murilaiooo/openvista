# Remaining development implementation plan

Baseline: `ab2ef28ce5b18fabf45b9cef7f8dc5c802e66a95`, October 26, 2026.

Authority: the owner's complete-development request, D-01–D-78 as amended by C-01, the stable-v1 acceptance contract, current status and roadmap. The owner delegated implementation choices and reserved **all testing and acceptance to ChatGPT**. No build, compilation, lint, automated-test creation/edit/execution, validation script, CI, device access, installation, merge or release is authorized in this work.

## Approach and constraints

Extend the existing Kotlin modules, shared local store and single-decoder playback owner. Preserve package identity, user data, NWS support, existing repository work and disabled security/distribution gates. Do not introduce bundled content, simulated weather/geography, background tracking or device-settings writes. A new provider must have documented primary-source terms, attribution, quotas and privacy behavior; restrictions that require owner authority remain explicit gates.

Use focused source commits followed by draft PRs. Implementation agents own disjoint files; the lead owns integration and commits. The project's subagent-driven-development and two-axis code-review skills call for delegated implementation/review. Static source review is permitted; execution-based validation is deferred without implying a pass.

## Tasks

- [ ] Storage: canonical SAF document identity across direct and tree grants; migrate existing duplicates without losing favorites, playlist references or checkpoints; preserve multiple access routes, disconnected records and bounded scans.
- [ ] Playback and Dream: inspect failure/recovery, codec/quality/HDR, resume, switching and single-owner lifecycle paths; complete feasible source fixes and safe OEM fallback behavior.
- [ ] Security integrations: finish feasible pairing and update hardening, authenticated confidential source transfer, expiration/TV approval, integrity/signing/version/installer state handling. Keep `PairingSecurity.REVIEWED` and `UpdateChannel.APPROVED` false.
- [ ] Weather/maps: preserve NWS; add an actual worldwide forecast adapter, city search and bounded interactive map rendering with primary-source provider/license documentation, attribution, cache/offline and localization support. Gate any unapproved provider use according to its terms.
- [ ] TV workflow/polish: inspect focus/navigation, persistence, source and playlist workflows, accessibility, diagnostics/privacy and EN/PT-BR text; integrate module changes.
- [ ] Delivery: update canonical status, module guide, README and `codex-handoff.md` with changed files, remaining external gates and concrete independent validation scenarios. Perform source-only standards/spec reviews; commit/push and open draft PRs without triggering CI.

## Review focus

Duplicate migration must preserve user intent and distinct providers; an absent USB must preserve records; one failed media pass must terminate; callbacks must not revive inactive playback or stale screens; provider data must retain honest missing/stale states. Security activation and release authority are separate from source implementation.

## Progress and rulings

- Initial tracked workspace is clean; pre-existing untracked `openvista-fixtures/` and VLC helper scripts belong to the owner and are excluded from changes.
- The requested baseline matches HEAD. There is no `.github/workflows/` directory at baseline; do not add CI.
- Work proceeds on a feature branch in the explicitly requested workspace. No worktree or setup/build/test command is needed.
- All implementation and review results in this plan are **untested**; historical QA does not validate new changes.
