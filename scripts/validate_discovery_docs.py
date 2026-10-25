"""Validate product-discovery decisions and local Markdown links without dependencies.

Usage: python scripts/validate_discovery_docs.py
"""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
decisions = (ROOT / "docs/product/decisions.md").read_text(encoding="utf-8")
ids = [int(n) for n in re.findall(r"^\| D-(\d{2}) \|", decisions, re.M)]
errors = []
expected = list(range(1, 69))
if sorted(ids) != expected:
    errors.append(f"Expected decision IDs D-01..D-68 once each; found {ids!r}")
if "Round 8" not in decisions or "Round 7: confirmed" not in decisions:
    errors.append("Round 7 confirmation / Round 8 pending status is missing")
if "68 product decisions" not in (ROOT / "README.md").read_text(encoding="utf-8"):
    errors.append("README decision count is stale")
if "68 decisions" not in (ROOT / "docs/product/vision.md").read_text(encoding="utf-8"):
    errors.append("Vision decision count is stale")

files = [
    ROOT / "README.md",
    ROOT / "docs/product/decisions.md",
    ROOT / "docs/product/vision.md",
    ROOT / "docs/product/experience.md",
    ROOT / "docs/product/runtime-and-recovery.md",
    ROOT / "docs/product/local-media-and-playlists.md",
    ROOT / "docs/product/settings-and-player.md",
    ROOT / "docs/product/first-release-blockers.md",
    ROOT / "docs/architecture/overview.md",
    ROOT / "docs/roadmap.md",
    ROOT / "docs/testing/compatibility.md",
]
for file in files:
    body = file.read_text(encoding="utf-8")
    for target in re.findall(r"\[[^\]]*\]\(([^)]+)\)", body):
        if target.startswith(("http:", "https:", "#", "mailto:")):
            continue
        target = target.split("#", 1)[0].strip()
        if not target or " " in target or target.startswith("<"):
            continue
        if not (file.parent / target).exists():
            errors.append(f"Broken link in {file.relative_to(ROOT)}: {target}")

if errors:
    print("FAIL: " + "\nFAIL: ".join(errors))
    sys.exit(1)

print(f"PASS: {len(ids)} sequential confirmed decisions, Round 8 open")
print(f"PASS: all relative links valid in {len(files)} project documents")
