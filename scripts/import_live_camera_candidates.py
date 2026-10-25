"""Import the owner's live-camera research into a quarantined staging catalog.

Never publish or autoplay these feeds based on this output alone.
Every candidate requires independent rights review and real Fire OS playback testing.

Usage:
    python scripts/import_live_camera_candidates.py
    python scripts/import_live_camera_candidates.py --check
"""
from __future__ import annotations

import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/research/live-cameras-50-2026-10-25.md"
OUTPUT = ROOT / "catalog/candidates/live-cameras-2026-10-25.json"

def organization(source: str) -> str:
    text = source.lower()
    if "caltrans" in text: return "Caltrans"
    if "iowa dot" in text: return "Iowa DOT"
    if "maryland sha" in text: return "Maryland SHA"
    if "vdot" in text: return "VDOT"
    if "arlington county" in text: return "Arlington County"
    if "modot" in text: return "MoDOT"
    if "ipcamlive" in text: return "IPCamLive / camera owner"
    if "gesell.com.ar" in text: return "Gesell"
    if "3cat" in text: return "3Cat"
    if "thailand department" in text: return "Thailand Department of Highways"
    if "orf" in text: return "ORF"
    if "radio radicale" in text: return "Radio Radicale"
    raise ValueError(f"Unknown source organization, do not silently assign: {source}")

def rights_status(org: str) -> str:
    if org == "Iowa DOT":
        return "prior_written_consent_required"
    if org == "Caltrans":
        return "confirm_camera_ownership_and_reuse_terms"
    if org == "IPCamLive / camera owner":
        return "camera_owner_and_host_permission_required"
    return "rights_not_yet_documented"

def build() -> dict:
    content = SOURCE.read_text(encoding="utf-8")
    by_key: dict[tuple[str,str,str], dict] = {}
    rows: list[int] = []
    urls: set[str] = set()
    for line in content.splitlines():
        if not re.match(r"^\|\s*\d+\s*\|", line):
            continue
        cells = [v.strip() for v in line.strip().strip("|").split("|")]
        if len(cells) != 8:
            raise ValueError(f"Expected 8 table columns, got {len(cells)}")
        row_text, name, location, category, protocol, reported_video, source, raw_url = cells
        n = int(row_text)
        rows.append(n)
        url = raw_url.strip(" `")
        if not url.startswith("https://") or not re.fullmatch(r"https://[^\s`]+",url):
            raise ValueError(f"Non-HTTPS/invalid URL in row {n}")
        if "?" in url or "@" in url:
            raise ValueError(f"URL in row {n} has a query string or credentials: manual review required")
        if url in urls:
            raise ValueError(f"Repeated URL in row {n}")
        urls.add(url)
        if protocol not in ("HLS", "DASH"):
            raise ValueError(f"Unsupported protocol {protocol} in row {n}")
        if not url.endswith(".m3u8" if protocol == "HLS" else ".mpd"):
            raise ValueError(f"URL extension does not match {protocol} at row {n}")
        org = organization(source)
        key = (name, location, org)
        if key not in by_key:
            by_key[key] = {
                "id": f"candidate-{n:03d}",
                "name": name,
                "locationLabel": location,
                "categoryAsReported": category,
                "organization": org,
                "sourceCredit": source,
                "sourceRows": [],
                "feeds": [],
                "candidateState": "research_only",
                "publishEnabled": False,
                "rightsReview": rights_status(org),
                "licenseEvidence": None,
                "familySafetyReview": "pending",
                "onDevicePlayback": "not_tested",
                "coordinates": None,
            }
        obj = by_key[key]
        obj["sourceRows"].append(n)
        obj["feeds"].append({
            "protocol": protocol,
            "url": url,
            "resolutionAndCodecAsReported": reported_video,
            "sourceDocumentProbe": "reported_2026-10-25_manifest_and_ffprobe",
            "onDeviceVerified": False,
        })
    if rows != list(range(1, 61)):
        raise ValueError(f"Expected 60 ordered addresses in source, got {rows}")
    if len(urls) != 60 or len(by_key) != 59:
        raise ValueError(f"Expected 60 URLs / 59 cameras, got {len(urls)} / {len(by_key)}")
    return {
        "schemaVersion": 1,
        "status": "QUARANTINED_RESEARCH_ONLY_NOT_A_PUBLIC_CATALOG",
        "sourceDocument": "docs/research/live-cameras-50-2026-10-25.md",
        "sourceReportDate": "2026-10-25",
        "notes": [
            "Original researcher reports manifest + ffprobe checks; not repeated by this importer.",
            "HTTP technical reachability does NOT grant license rights or imply Android TV playback.",
            "Every feed is disabled pending source-specific rights, family-safety and target-device checks.",
            "Do not auto-include in app catalog or upload media/video payloads."
        ],
        "uniqueCameras": len(by_key),
        "feedUrls": len(urls),
        "approvedForPublicCatalog": 0,
        "cameras": list(by_key.values()),
    }

def main() -> None:
    encoded = (json.dumps(build(), ensure_ascii=False, indent=2) + "\n").encode("utf-8")
    if len(sys.argv) == 2 and sys.argv[1] == "--check":
        if not OUTPUT.exists() or OUTPUT.read_bytes() != encoded:
            raise SystemExit("FAIL: candidates JSON is stale. Regenerate with this script.")
        print("PASS: 59 distinct candidate cameras, 60 unique feeds, 0 approved; staged JSON matches the owner's list.")
        return
    if len(sys.argv) > 1:
        raise SystemExit("Usage: python scripts/import_live_camera_candidates.py [--check]")
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_bytes(encoded)
    print("WROTE:", OUTPUT.relative_to(ROOT), "- 59 cameras, 60 feeds; all disabled pending rights and device tests")

if __name__ == "__main__":
    main()
