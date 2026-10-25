"""Fail-closed inspection of proposed OpenVista APK/AAB/ZIP release artifacts.

This is a package-content check, not a substitute for inspecting source code,
licensing, device playback, or the Android build configuration.

Usage:
    python scripts/release_content_guard.py path/to/OpenVista.apk
"""
from __future__ import annotations

import io
from pathlib import Path, PurePosixPath
import re
import sys
import zipfile

BLOCK_SIZE = 64 * 1024
OVERLAP = 4096
MAX_UNCOMPRESSED_BYTES = 2 * 1024 * 1024 * 1024
MAX_NESTED_BYTES = 64 * 1024 * 1024
MAX_ENTRIES = 100_000
MAX_NESTING = 2
MEDIA_SUFFIXES = {".m3u", ".m3u8", ".mpd", ".mp4", ".mkv", ".webm", ".mov"}
NESTED_SUFFIXES = {".zip", ".jar", ".aar", ".apk", ".aab"}
# URLs may contain query parameters, but a literal stream endpoint must not
# appear anywhere in release bytes (classes.dex, XML, assets, native libraries).
STREAM_URL_PATTERN = rb"""https?://[^\x00-\x20"'<>\\]{1,2048}?\.(?:m3u8|mpd)(?:[?#][^\x00-\x20"'<>\\]*)?"""
STREAM_URL = re.compile(STREAM_URL_PATTERN, re.IGNORECASE)
STREAM_URL_TEXT = re.compile(STREAM_URL_PATTERN.decode("ascii"), re.IGNORECASE)


class ArtifactInspectionError(Exception):
    """The artifact cannot be inspected safely or completely."""


def _contains_stream_url(source) -> bool:
    previous = b""
    while True:
        chunk = source.read(BLOCK_SIZE)
        if not chunk:
            return False
        window = previous + chunk
        if STREAM_URL.search(window):
            return True
        # Android resources may encode static strings as UTF-16 instead of UTF-8.
        lower = window.lower()
        if b"h\x00t\x00t\x00p\x00" in lower:
            if any(STREAM_URL_TEXT.search(window[offset:].decode("utf-16le", errors="ignore")) for offset in (0, 1)):
                return True
        if b"\x00h\x00t\x00t\x00p" in lower:
            if any(STREAM_URL_TEXT.search(window[offset:].decode("utf-16be", errors="ignore")) for offset in (0, 1)):
                return True
        previous = window[-OVERLAP:]


def _inspect_zip(archive, label: str, depth: int, budget: list[int]) -> list[str]:
    issues: list[str] = []
    try:
        with zipfile.ZipFile(archive) as bundle:
            entries = bundle.infolist()
            if len(entries) > MAX_ENTRIES:
                raise ArtifactInspectionError(f"Too many archive entries in {label}")
            for item in entries:
                if item.is_dir():
                    continue
                entry_label = f"{label}:{item.filename}"
                suffix = PurePosixPath(item.filename.replace("\\", "/")).suffix.lower()
                if item.flag_bits & 1:
                    raise ArtifactInspectionError(f"Encrypted entry cannot be inspected: {entry_label}")
                budget[0] += item.file_size
                if budget[0] > MAX_UNCOMPRESSED_BYTES:
                    raise ArtifactInspectionError("Uncompressed inspection budget exceeded")
                if suffix in MEDIA_SUFFIXES:
                    issues.append(f"Bundled media or playlist file: {entry_label}")
                if suffix in NESTED_SUFFIXES and depth >= MAX_NESTING:
                    raise ArtifactInspectionError(f"Archive nesting exceeds inspection limit: {entry_label}")
                try:
                    with bundle.open(item) as entry:
                        if depth < MAX_NESTING and suffix in NESTED_SUFFIXES:
                            if item.file_size > MAX_NESTED_BYTES:
                                raise ArtifactInspectionError(
                                    f"Nested archive exceeds inspection limit: {entry_label}"
                                )
                            nested = entry.read(MAX_NESTED_BYTES + 1)
                            if len(nested) > MAX_NESTED_BYTES:
                                raise ArtifactInspectionError(
                                    f"Nested archive exceeds inspection limit: {entry_label}"
                                )
                            if not zipfile.is_zipfile(io.BytesIO(nested)):
                                raise ArtifactInspectionError(f"Malformed nested archive: {entry_label}")
                            issues.extend(_inspect_zip(io.BytesIO(nested), entry_label, depth + 1, budget))
                        elif _contains_stream_url(entry):
                            issues.append(f"Embedded HLS/DASH URL: {entry_label}")
                except (OSError, RuntimeError, EOFError, zipfile.BadZipFile) as exc:
                    raise ArtifactInspectionError(f"Cannot inspect {entry_label}: {exc}") from exc
    except (OSError, zipfile.BadZipFile, zipfile.LargeZipFile) as exc:
        raise ArtifactInspectionError(f"Invalid release artifact {label}: {exc}") from exc
    return issues


def inspect_artifact(path: Path) -> list[str]:
    if not path.is_file():
        raise ArtifactInspectionError(f"Release artifact does not exist: {path}")
    if path.suffix.lower() not in {".apk", ".aab", ".zip"}:
        raise ArtifactInspectionError("Expected an .apk, .aab, or .zip release artifact")
    return _inspect_zip(path, str(path), 0, [0])


def main(argv: list[str] | None = None) -> int:
    args = sys.argv[1:] if argv is None else argv
    if len(args) != 1:
        print("Usage: python scripts/release_content_guard.py path/to/OpenVista.apk")
        return 2
    try:
        findings = inspect_artifact(Path(args[0]))
    except ArtifactInspectionError as exc:
        print(f"FAIL: {exc}")
        return 2
    if findings:
        for finding in findings:
            print(f"FAIL: {finding}")
        print(f"FAIL: {len(findings)} bundled media/stream finding(s). Do not publish.")
        return 1
    print("PASS: No bundled media files or literal HLS/DASH stream URLs found in inspected release bytes.")
    print("NOTE: This does not prove runtime behavior, source provenance, or TV compatibility.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
