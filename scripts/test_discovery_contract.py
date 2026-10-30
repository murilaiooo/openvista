"""End-to-end regression checks of the documentation/source guard.

Copies a small repo snapshot to temporary storage; never mutates the checkout.
"""
from __future__ import annotations

from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[1]


def copy_fixture(destination: Path):
    for folder in ("docs", "scripts"):
        shutil.copytree(
            ROOT / folder, destination / folder,
            ignore=shutil.ignore_patterns("__pycache__", "*.pyc"),
        )
    for item in ("AGENTS.md", "README.md", "README.pt-BR.md", "CONTRIBUTING.md", "GLOSSARY.md", "LICENSE"):
        shutil.copy2(ROOT / item, destination / item)


def run_guard(destination: Path):
    return subprocess.run(
        [sys.executable, str(destination / "scripts/validate_discovery_docs.py")],
        capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=20,
    )


class DiscoveryContractTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        copy_fixture(self.root)

    def test_current_docs_pass(self):
        run = run_guard(self.root)
        self.assertEqual(run.returncode, 0, run.stdout + run.stderr)

    def test_rejects_hardcoded_hls_in_future_android_module(self):
        fake = self.root / "app/src/main/java/openvista/Bad.kt"
        fake.parent.mkdir(parents=True)
        fake.write_text('val source = "https://host.invalid/live.m3u8?token=secret"\n')
        run = run_guard(self.root)
        self.assertNotEqual(run.returncode, 0)
        self.assertIn("Hard-coded media endpoint in app source", run.stdout)

    def test_rejects_media_file_in_future_android_module(self):
        fake = self.root / "app/src/main/assets/scenery.mp4"
        fake.parent.mkdir(parents=True)
        fake.write_bytes(b"no media may be bundled")
        run = run_guard(self.root)
        self.assertNotEqual(run.returncode, 0)
        self.assertIn("Bundled media/playlist prohibited", run.stdout)

    def test_rejects_lost_c01_scope_marker(self):
        decision_file = self.root / "docs/product/decisions.md"
        decision_file.write_text(
            decision_file.read_text(encoding="utf-8").replace("C-01", "ZZ-00"),
            encoding="utf-8",
        )
        run = run_guard(self.root)
        self.assertNotEqual(run.returncode, 0)
        self.assertIn("C-01", run.stdout + run.stderr, f"stdout={run.stdout!r}; stderr={run.stderr!r}")


if __name__ == "__main__":
    unittest.main()
