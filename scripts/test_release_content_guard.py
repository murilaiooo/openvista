"""Fast offline regression tests for release-artifact isolation."""
from __future__ import annotations

import io
from pathlib import Path
import tempfile
import unittest
from zipfile import ZipFile

from release_content_guard import ArtifactInspectionError, inspect_artifact, main


class ArtifactGuardTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.path = Path(self.temp.name) / "openvista.apk"

    def make_zip(self, files: dict[str, bytes]):
        with ZipFile(self.path, "w") as archive:
            for name, contents in files.items():
                archive.writestr(name, contents)
        return self.path

    def test_clean_apk_passes(self):
        self.make_zip({
            "AndroidManifest.xml": b"OpenVista",
            "classes.dex": b"https://developer.android.com/media/media3/exoplayer/hls",
            "res/drawable/logo.png": b"image-data",
        })
        self.assertEqual(inspect_artifact(self.path), [])
        self.assertEqual(main([str(self.path)]), 0)

    def test_rejects_playlist_file(self):
        self.make_zip({"assets/cameras.m3u": b"#EXTM3U"})
        self.assertIn("Bundled media", inspect_artifact(self.path)[0])

    def test_rejects_uppercase_video_file(self):
        self.make_zip({"assets/samples/BEACH.MP4": b"video"})
        self.assertIn("Bundled media", inspect_artifact(self.path)[0])

    def test_rejects_hls_endpoint_in_dex(self):
        self.make_zip({"classes.dex": b"\x00https://cams.example.org/live/sea.m3u8\x00"})
        self.assertIn("Embedded HLS/DASH", inspect_artifact(self.path)[0])

    def test_rejects_utf16_little_endian_endpoint(self):
        self.make_zip({"resources.arsc": "https://cam.example/live.m3u8".encode("utf-16le")})
        self.assertIn("Embedded HLS/DASH", inspect_artifact(self.path)[0])

    def test_rejects_utf16_big_endian_endpoint(self):
        self.make_zip({"resources.arsc": "https://cam.example/live.mpd".encode("utf-16be")})
        self.assertIn("Embedded HLS/DASH", inspect_artifact(self.path)[0])

    def test_rejects_dash_endpoint_with_query(self):
        self.make_zip({"assets/config.json": b'{"url":"https://cams.example.com/live.mpd?quality=high"}'})
        self.assertIn("Embedded HLS/DASH", inspect_artifact(self.path)[0])

    def test_crosses_chunk_boundaries(self):
        self.make_zip({"classes.dex": b"x" * (65536 - 13) + b"https://cam.example/live.m3u8"})
        self.assertIn("Embedded HLS/DASH", inspect_artifact(self.path)[0])

    def test_rejects_nested_archive_stream(self):
        output = io.BytesIO()
        with ZipFile(output, "w") as nested:
            nested.writestr("sources.json", b"https://example.org/live.m3u8")
        self.make_zip({"assets/staged.zip": output.getvalue()})
        self.assertIn("Embedded HLS/DASH", inspect_artifact(self.path)[0])

    def test_rejects_nested_playlist(self):
        output = io.BytesIO()
        with ZipFile(output, "w") as nested:
            nested.writestr("sources.m3u8", b"#EXTM3U")
        self.make_zip({"assets/staged.zip": output.getvalue()})
        self.assertIn("Bundled media", inspect_artifact(self.path)[0])

    def test_missing_release_fails_closed(self):
        with self.assertRaises(ArtifactInspectionError):
            inspect_artifact(self.path)
        self.assertEqual(main([str(self.path)]), 2)

    def test_corrupt_release_fails_closed(self):
        self.path.write_bytes(b"not a zip")
        with self.assertRaises(ArtifactInspectionError):
            inspect_artifact(self.path)

    def test_does_not_reject_nonstream_urls(self):
        self.make_zip({"classes.dex": b"https://example.org/healthy?name=beach"})
        self.assertEqual(inspect_artifact(self.path), [])


if __name__ == "__main__":
    unittest.main()
