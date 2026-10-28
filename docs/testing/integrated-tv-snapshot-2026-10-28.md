# October 28: integrated OpenVista TV snapshot

## Source integration
This record covers the final private development-code synchronization, **not** a signed public release. The integration joins the October 28 PR #40 work on `codex/ui-redesign-36`, the separately validated local `claude/project-thread-fc05y0` ambient implementation, two uncommitted local fixes captured as `7350a50`, and the latest `main` English/Portuguese README and brand assets. The work remained in its own branch until local checks passed.

- The only source conflict was `AmbientOverlay.kt`. We preserved the full, recently built local live-overlay implementation: its own `placeLine` resolves localized country names and flags, and it separates city/country so long city labels cannot hide the country. This retains the reference-inspired layout and the 32-position display-care cycle instead of replacing it with an older compact overlay.
- The camera header is explicitly measured at wrap width, avoiding clipping. The source editor's scroller has a bounded non-exact height so its Find location, Save and Cancel controls can remain reachable. Physical D-pad navigation after this fix still requires its own acceptance check.
- Remote PR #40's live recovery, network/backoff, M3U metadata preservation, country resolver, Open-Meteo city completion and unified Home/Settings remain included. Latest mainline README and brand assets were merged without conflicts.
- Experimental YouTube embeds, disabled LAN pairing/updater, old overlapping draft PRs, private owner camera lists, research artifacts and local backups were **not** automatically merged into runtime or pushed as public release content.

## Physical APK comparison
The installed package `com.openvista.prototype` on the owner's Hisense Fire OS 7 has `versionCode=2`, `versionName=0.2.0-implementation-preview` and showed a last update on October 27 at 23:14:59 local. The pulled installed APK was 18,758,735 bytes and its SHA-256 matched the integrated local debug build:

`4d8da518a16539516cad6da02bc11f5d5678f69a3b293fa712653495cf28deff`

This verifies byte-identical **debug APK artifacts**; it is not a 24-hour hardware test, signed release certification or proof of pixel-perfect UI.

## Repeatable local validation
Windows JDK 21, Android SDK, Gradle offline:

```powershell
.\gradlew.bat :source-core:test :diagnostics:testDebugUnitTest :player:testDebugUnitTest :player:assembleDebug :player:lintDebug --offline --console=plain --no-daemon
python scripts/validate_discovery_docs.py
python -m unittest discover -s scripts -p "test_*.py" -q
python scripts/release_content_guard.py player/build/outputs/apk/debug/player-debug.apk
```

**All passed after reconciling README links and fixture setup:** 51 player unit tests, 4 diagnostics tests, 2 source-core test suites, 23 Python tests, Gradle build and Android Lint, documentation/traceability validation and the static packaged-media/stream guard for the debug APK. The content guard does not inspect historical Git objects or prove playback behavior. No new APK was installed, and no device OS settings or personal media data were changed.

## Remaining public-release blockers
The repository must **remain private** until an audit or rewrite removes historical research stream URLs from *Git history*, and owner permission is obtained. Signing/release identity, artifact privacy/rights/licensing, the updater's unused `REQUEST_INSTALL_PACKAGES` permission, consent for city geocoding, 24-hour stability, performance measurements, long-running display-care verification and real-TV editor focus checks are still incomplete. Never assert stable release readiness from these local checks.
