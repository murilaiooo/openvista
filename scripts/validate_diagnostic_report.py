"""Validate an owner-exported OpenVista report without printing its contents."""

from __future__ import annotations

import argparse
import ipaddress
import json
from pathlib import Path
import re
import sys

DEFAULT_SCHEMA = Path(__file__).resolve().parents[1] / "docs/testing/diagnostic-report-schema.json"
PRIVATE_KEYS = {"serial", "fingerprint", "androidid", "ipaddress", "macaddress",
                "ssid", "password", "account", "accountname", "filepath", "uri", "url"}
PRIVATE_FRAGMENTS = ("content://", "file://", "rtsp://", "http://", "https://")
IPV4 = re.compile(r"(?<!\d)(?:\d{1,3}\.){3}\d{1,3}(?!\d)")


def validate(report_path: Path, schema_path: Path) -> int:
    try:
        from jsonschema import Draft202012Validator
    except ImportError:
        print("ERROR: install Python jsonschema==4.25.1 to validate the full schema.", file=sys.stderr)
        return 2

    try:
        report = json.loads(report_path.read_text(encoding="utf-8"))
        schema = json.loads(schema_path.read_text(encoding="utf-8"))
        Draft202012Validator.check_schema(schema)
        Draft202012Validator(schema).validate(report)
        if report["checks"]["physicalPlayback"] != "not_tested":
            raise ValueError("The diagnostics-only app cannot verify media playback")
        if report["pickers"]["usbEnumerationTested"] is not False:
            raise ValueError("Picker resolution does not prove USB access")

        def examine(value: object) -> None:
            if isinstance(value, dict):
                for key, member in value.items():
                    if key.casefold() in PRIVATE_KEYS:
                        raise ValueError("Unexpected sensitive field")
                    examine(member)
            elif isinstance(value, list):
                for member in value:
                    examine(member)
            elif isinstance(value, str):
                if any(fragment in value.lower() for fragment in PRIVATE_FRAGMENTS):
                    raise ValueError("Report includes a resource URL or file URI")
                for match in IPV4.finditer(value):
                    try:
                        ipaddress.IPv4Address(match.group())
                    except ipaddress.AddressValueError:
                        continue
                    raise ValueError("Report includes an IP address")

        examine(report)
    except (OSError, UnicodeError, ValueError, KeyError, TypeError) as error:
        print(f"FAIL: exported diagnostic report did not pass validation ({type(error).__name__}).", file=sys.stderr)
        return 1
    except Exception as error:
        print(f"FAIL: exported diagnostic report did not pass validation ({type(error).__name__}).", file=sys.stderr)
        return 1
    print("PASS: JSON Schema Draft 2020-12 and private-data checks.")
    return 0


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report", type=Path, help="Path to locally exported JSON (never commit it)")
    parser.add_argument("--schema", type=Path, default=DEFAULT_SCHEMA)
    args = parser.parse_args()
    sys.exit(validate(args.report, args.schema))
