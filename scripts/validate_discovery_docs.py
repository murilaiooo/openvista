"""Validate the OpenVista planning baseline. No external dependencies.

Usage on the development PC:
    python scripts/validate_discovery_docs.py

Scope: markdown and structural consistency only; never claim this runs Android tests.
"""
from pathlib import Path
import json
import re
import sys

from release_content_guard import STREAM_URL

ROOT = Path(__file__).resolve().parents[1]
DECISIONS = ROOT / "docs/product/decisions.md"
TRACE = ROOT / "docs/product/requirement-traceability.md"
errors = []

decisions = DECISIONS.read_text(encoding="utf-8")
decision_ids = [int(n) for n in re.findall(r"^\| D-(\d{2}) \|", decisions, re.M)]
expected = list(range(1, 79))
if decision_ids != expected:
    errors.append("Canonical decisions are not exactly D-01..D-78 in order (no duplicates allowed).")

if "C-01" not in decisions:
    errors.append("Current user-managed content clarification C-01 is missing.")

if not all(x in decisions for x in ["Round 8: confirmed", "Product discovery outcome"]):
    errors.append("Eight-round completion is not recorded in decisions.")

summary_contracts = [
    ("README.md", "docs/product/decisions.md"),
    ("docs/product/user-managed-content.md", "C-01"),
    ("docs/product/vision.md", "78 historical decisions"),
    ("docs/product/v1-acceptance.md", "C-01 user-managed content clarification"),
    ("docs/implementation/priority-plan.md", "78 owner-approved"),
]
for path, marker in summary_contracts:
    if marker not in (ROOT / path).read_text(encoding="utf-8"):
        errors.append(f"{path}: stale or missing baseline marker {marker}")

trace_text = TRACE.read_text(encoding="utf-8")
trace_rows = re.findall(r"^\| D-(\d{2}) \| (G\d{2}) \|[^\n]*\| NOT TESTED \|$", trace_text, re.M)
mapped_ids = [int(id_text) for id_text, gate in trace_rows]
allowed_gates = {f"G{n:02d}" for n in range(1, 11)}
if sorted(mapped_ids) != expected:
    errors.append("Traceability map must cover each of D-01..D-78 exactly once.")
if any(gate not in allowed_gates for _, gate in trace_rows):
    errors.append("Unknown gate ID in traceability map.")
if {gate for _, gate in trace_rows} != allowed_gates:
    errors.append("Every G01..G10 gate must own at least one D-ID.")

# All repo docs + root-facing Markdown; skills are separately owned external copies.
docs = sorted((ROOT / "docs").rglob("*.md"))
files = docs + [ROOT / n for n in ("README.md", "AGENTS.md", "CONTRIBUTING.md", "GLOSSARY.md")]
for file in files:
    body = file.read_text(encoding="utf-8")
    # Inline Markdown links only. Fenced source text and external links are not processed.
    for target in re.findall(r"\[[^\]]+\]\(([^)]+)\)", body):
        target = target.split("#", 1)[0].strip()
        if not target or target.startswith(("http:", "https:", "mailto:", "data:", "//")):
            continue
        if " " in target or target.startswith("<"):
            continue
        if not (file.parent / target).exists():
            errors.append(f"Missing relative link: {file.relative_to(ROOT)} -> {target}")

# Quarantined camera candidates are reference material, not a licensed catalog.
candidate_path = ROOT / "catalog/candidates/live-cameras-2026-10-25.json"
if candidate_path.exists():
    candidates = json.loads(candidate_path.read_text(encoding="utf-8"))
    cameras = candidates.get("cameras", [])
    feeds = [f for cam in cameras for f in cam.get("feeds", [])]
    if len(cameras) != 59 or len(feeds) != 60:
        errors.append("Staged camera catalog must contain 59 cameras / 60 feeds.")
    if candidates.get("approvedForPublicCatalog") != 0 or any(cam.get("publishEnabled") is not False for cam in cameras):
        errors.append("Camera candidates cannot be published before permission is verified.")
    if any(cam.get("onDevicePlayback") != "not_tested" for cam in cameras):
        errors.append("Device verification status changed without evidence.")
    if len({f.get("url") for f in feeds}) != len(feeds):
        errors.append("Duplicate staged feed URLs.")
    if not all(cam.get("rightsReview") and cam.get("sourceRows") for cam in cameras):
        errors.append("Provenance or rights-review status missing.")

# Guard the English-only repository language requirement without flagging real place names.
portuguese_prose = [
    "praias e litoral", "cidades e arquitetura", "lagos e marinas",
    "data do teste:", "fontes dos candidatos:", "além dos 52",
    "resolução medida", "teste seguinte:", "sem estabilidade garantida",
    "descoberta via", "stream servido pela", "não é órgão público",
]
for file in [*docs, *(ROOT / "scripts").glob("*.py")]:
    if file.name == "validate_discovery_docs.py" or file.name.endswith("_temp.py"):
        continue
    body = file.read_text(encoding="utf-8").casefold()
    for phrase in portuguese_prose:
        if phrase in body:
            errors.append(f"Non-English prose found in {file.relative_to(ROOT)}: {phrase}")

# Prevent future runtime tree from accidentally bundling research streams or media.
runtime_roots = [ROOT / name for name in ("app", "androidApp", "application")]
for runtime in runtime_roots:
    if not runtime.exists():
        continue
    for file in runtime.rglob("*"):
        if not file.is_file():
            continue
        if file.suffix.lower() in (".m3u", ".m3u8", ".mpd", ".mp4", ".mkv", ".webm", ".mov"):
            errors.append(f"Bundled media/playlist prohibited: {file.relative_to(ROOT)}")
        elif file.suffix.lower() in (".kt", ".java", ".xml", ".json", ".txt"):
            try:
                text_content = file.read_text(encoding="utf-8")
            except UnicodeDecodeError:
                continue
            if STREAM_URL.search(text_content.encode("utf-8")):
                errors.append(f"Hard-coded media endpoint in app source: {file.relative_to(ROOT)}")

# Structural sanity checks (not claims of runtime verification).
for path in ("AGENTS.md", "CONTRIBUTING.md"):
    if "in product discovery" in (ROOT / path).read_text(encoding="utf-8").lower():
        errors.append(f"{path} falsely states discovery is active.")
adr = (ROOT / "docs/adr/0001-dual-mode-entry-points.md").read_text(encoding="utf-8")
if "plus at least one Google TV" in adr:
    errors.append("ADR-0001 contradicts owner-approved initial Hisense-only test.")

if errors:
    for e in errors:
        print("FAIL:", e)
    sys.exit(1)

print("PASS: all 78 ordered, unique approved decisions; eight rounds complete")
print("PASS: 78/78 D-ID mappings across 10 acceptance gates (planned, NOT TESTED)")
print(f"PASS: local relative links valid across {len(files)} Markdown documents")
print("PASS: active steering and ADR avoid stale interview/device claims")
print("PASS: English-language prose checks and zero bundled runtime media (where runtime source exists)")
print("NOTE: no APK, TV tests, camera permissions or runtime evidence validated")
