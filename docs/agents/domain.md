# Domain documentation conventions

Layout: **single-context** repository.

- `GLOSSARY.md` is the shared product vocabulary. Read first when changing terminology, APIs, PRDs or domain entities. Check [audited requirement traceability](../product/requirement-traceability.md) before creating implementation issues.
- `docs/adr/` contains architectural decision records. Use status **Proposed** until user/product approval or verified implementation decision.
- `docs/product/vision.md` is a living record of confirmed scope.
- `docs/product/decisions.md` contains **78 approved decisions from eight completed rounds**; only evidence-based follow-up technical choices remain. Trace to `docs/product/requirement-traceability.md` and `docs/product/v1-acceptance.md`.
- Research source material belongs in `docs/research/`.
- Do not turn proposed architecture into an accepted ADR. Revisit ADRs when device research contradicts assumptions.
