# OpenVista release-artifact content isolation

**Status (2026-10-26):** The offline guard has passed on the local diagnostic APK and subsequent debug builds of the integrated TV player, including the NWS integration. **No stable/public signed APK is approved**; the actual future release artifact must be scanned again after production signing and packaging. See [current status](../STATUS.md).

## Commands (Windows / Python 3.12)

From the repository root:

```powershell
python scripts/validate_discovery_docs.py
python -m unittest discover -s scripts -p "test_*.py" -v
python scripts/release_content_guard.py "PATH_TO_ACTUAL_SIGNED_OPENVISTA.apk"
```

Run the final command on the **actual package intended for distribution**, not a source ZIP or a mock file. The tool accepts APK, AAB and ZIP archives. A nonexistent, malformed, encrypted, oversized or uninspectable archive fails closed; a clean synthetic unit-test ZIP is **not** evidence that an app release passes.

## What is checked

- ZIP entries whose filenames end in M3U, M3U8, MPD, MP4, MKV, WebM or MOV, including entries nested inside supported compressed archives.
- Literal HTTP(S) HLS/DASH endpoints ending in `.m3u8` or `.mpd` in compiled resources, bytecode, native libraries and packaged data, including UTF-8, UTF-16LE and UTF-16BE strings and URLs with query strings.
- Suspicious contents are reported **by archive entry path only**; potential private stream addresses are never printed.

The documentation validator also checks these extensions and literal endpoints in **actual player and diagnostics Android runtime source directories**. Owner-managed camera research and media are excluded from the public repository. The validator separately rejects known historical public research catalogs and playlists.

## Limits and required human review

This is a defense-in-depth static scan. It cannot prove the absence of dynamically constructed, encrypted, obfuscated or fetched URLs, nor does it prove that a stream is playable or its use is authorized. Review Gradle packaging/resource configuration, runtime network behavior, source import handling and the final artifact inventory independently. Confirm the package ID, signer continuity and version, then execute the real-device, privacy, licensing and 24-hour stability gates in [v1 acceptance](../product/v1-acceptance.md).

**No first test APK should be uploaded to GitHub Releases.** The first test is local on Hisense 58U6HF / Fire OS 7, and public release still requires explicit owner authorization. There is no first-party camera catalog in the current C-01 scope.
