"""Generate an English-language scenic HLS playlist for private feasibility testing.

The URLs are research fixtures and MUST NOT be bundled with the OpenVista app.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "docs/research"
PROBES = [
    BASE / "scenic-1080p-probe-results-2026-10-25.json",
    BASE / "scenic-city-1080p-probe-results-2026-10-25.json",
]
REPORT = BASE / "scenic-fullhd-test-links-2026-10-25.md"
PLAYLIST = BASE / "scenic-fullhd-test-only-2026-10-25.m3u"

data = [json.loads(p.read_text(encoding="utf-8")) for p in PROBES]
attempts = sum(x.get("attempted", 0) for x in data)
unique = {}
duplicates = 0
for document in data:
    for item in document["results"]:
        if item.get("status") != "verified_1080plus":
            continue
        if item["url"] in unique:
            duplicates += 1
        else:
            unique[item["url"]] = item

rows = list(unique.values())

def group(row):
    label = (row["label"] + " " + (row.get("name") or "")).lower()
    if any(word in label for word in ["beach", "praia do norte", "meia praia", "coast", "nazaré", "arrifana", "carcavelos", "caparica", "faro", "sopot", "matosinhos", "dębki", "jurata", "jastarnia", "hel", "władysławowo", "apúlia", "espinho", "leça", "orłowo", "kalamata"]):
        return "Beaches & Coast"
    if any(word in label for word in ["lake", "marina", "pier", "píer", "puck", "giżycko", "kietlice"]):
        return "Lakes & Marinas"
    if any(word in label for word in ["mountain", "tatras", "bukowina", "wintergreen", "bryce", "karpacz", "rzepiska"]):
        return "Mountains"
    return "Cities & Architecture"

order = {"Beaches & Coast": 0, "Lakes & Marinas": 1, "Cities & Architecture": 2, "Mountains": 3}
rows.sort(key=lambda r: (order[group(r)], r["label"].casefold()))
lines = [
    "# OpenVista — scenic, beach, and city HLS streams (private feasibility research)",
    "",
    "**Test date:** October 25, 2026. **Purpose:** investigate playback on Windows and later on the Hisense TV, **not** distribute camera feeds with OpenVista.",
    "",
    f"**Results:** {len(rows)} distinct URLs reported an actual video stream at **1920×1080 or higher** in {attempts} ffprobe attempts across two batches ({duplicates} overlapping successful URLs). The probe opened each reachable stream and read its codec and dimensions. **It was not an extended playback stability test or a Fire OS/Media3 test.**",
    "",
    "Candidate source: [Live-Environment-Streams](https://github.com/william-ricchiuti/Live-Environment-Streams). Actual resolutions were measured using **ffprobe on Windows**, rather than inferred from upstream metadata. No login bypass, token extraction, paywall circumvention, or lengthy video recording was performed.",
    "",
    "## Test playlist",
    "",
    "Open [scenic-fullhd-test-only-2026-10-25.m3u](scenic-fullhd-test-only-2026-10-25.m3u) with VLC or a compatible HLS player. This playlist is **a separate local research fixture**; it is not used by the public app or included in APK assets.",
    "",
    "## Links that passed the resolution probe",
    "",
    "| Scene type | Location / subject | Measured resolution | Codec | HLS manifest |",
    "| --- | --- | ---: | --- | --- |",
]
for item in rows:
    codec = item.get("codec") or "unknown"
    lines.append(f'| {group(item)} | {item["label"]} | {item["actualWidth"]}×{item["actualHeight"]} | {codec} | `{item["url"]}` |')

lines.extend([
    "",
    "## Additional verification: one decoded frame",
    "",
    "On top of the resolution probes, **FFmpeg decoded one real video frame on Windows** for four samples: **Nazaré (1920×1080)**, **Kalamata (2560×1440)**, **Sopot (2560×1440)**, and **Warsaw (1920×1080)**, without storing image or video files. See [the one-frame decode results](scenic-sample-frame-decode-2026-10-25.json). This proves individual sample decoding, **not** sustained playback or Media3 compatibility on the TV.",
    "",
    "## Technical notes",
    "",
    "- **Full HD** means at least 1920 pixels wide and 1080 pixels tall. A 5120×1440 ultra-wide panorama is not standard 3840×2160 UHD.",
    "- **No uptime guarantee:** sources may change their URLs, reject clients, go offline, appear dark at local night, or downgrade stream quality.",
    "- **Failures:** some Puerto Rico and Dominican Republic feeds did not respond in the probe; they are not in the Full HD shortlist.",
    "- **Next test:** play selected sources for several minutes on Media3/Hisense, measure frame drops, buffering, CPU, and RAM at 1080p, 1440p, and ultra-wide dimensions.",
    "- **Rights and scope:** these URLs are for private technical testing, not a promise to bundle them in any release. OpenVista now has a user-managed source model: the software ships no feeds, lists, or videos.",
    "",
    "To reproduce the research, obtain the upstream `streams.geojson` at `%TEMP%\\openvista-streams.geojson`, run `python scripts/probe_scenic_streams.py`, `python scripts/probe_scenic_city_streams.py`, and then `python scripts/build_scenic_test_playlist.py`. These tools use ffprobe and do not change TV settings.",
])
REPORT.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")
playlist = ["#EXTM3U", "# Private OpenVista feasibility playlist; never bundle into a release."]
for item in rows:
    playlist.append(f'#EXTINF:-1 group-title="{group(item)}",{item["label"]} ({item["actualWidth"]}x{item["actualHeight"]})')
    playlist.append(item["url"])
PLAYLIST.write_text("\n".join(playlist).rstrip() + "\n", encoding="utf-8")
print(f"Built English report and M3U: {len(rows)} sources, {attempts} probes, {duplicates} overlaps.")
