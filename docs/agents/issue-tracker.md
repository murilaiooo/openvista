# Issue tracker

- Tracker: **GitHub Issues** in [murilaiooo/openvista](https://github.com/murilaiooo/openvista/issues).
- PRs are **not** part of the issue triage queue by default.
- Agent workflow: `grill-with-docs` -> `to-spec` -> `to-tickets` -> `implement` / `tdd` -> `code-review` -> `pr`.
- One bounded change per issue or PR. Use GitHub issue links for dependencies and acceptance criteria. Initial P0 focus: #4 baseline, #2 DreamService, #5 player, #9 scoped USB, #3 licensed streams, #6 weather/map, #7 updater and #8 QR threat model. See [dependency map](../implementation/critical-path.md).
- User approval is required for product choices and for actions that change a connected TV's system-wide settings.
- Use `gh issue` in an authorized checkout, or GitHub connector tools when CLI authorization is unavailable.
- Do not represent a planned feature as an implemented issue completion.
