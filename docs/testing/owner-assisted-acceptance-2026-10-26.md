# OpenVista owner-assisted Hisense acceptance checks

OpenVista is a **development preview**, not an approved public release. Test on the existing single OpenVista package. Do not enable the system screensaver or change other apps/settings. Keep a copy of your results with **PASS/FAIL + exact observed behavior**, omitting private stream URLs.

## Tests requiring a person at the actual TV

1. **Remote / localization:** Open the three Home cards and Settings with the Fire TV remote. Navigate to the last entry and back; inspect focus, long titles, dialogs and screen edges. Switch EN/PT-BR, restart the app and verify the language and focus.
2. **Local USB / SAF:** Insert your own USB drive with a supported MP4 and, if available, MKV/WebM/MOV. Add the MP4 as a single file, then select its containing folder. Confirm one visible record, correct title, favorite, playlist and resume position after restarting OpenVista. Unplug/replug the drive; verify the absence is explained, metadata is retained and playback recovers. Use **Forget folder** to revoke the selection and confirm the app does not read it afterward. Do not delete user files.
3. **Playback / recovery:** Try your own stable 1080p/4K HLS and DASH sources. Check crop/fit, initial mute, camera switching and configured rotation. Play local media and verify ±10-second seek, pause, checkpoint resume and looping. Disrupt only your **test source/server** (not the whole household network) and verify a bounded error or fallback, then restore it.
4. **Clocks / weather / map:** Verify Tucson weather and forecast in the selected UI language. Confirm the map falls back clearly to a source list when a provider backdrop is unavailable. Worldwide city search and map tiles are **not activated** in this candidate.
5. **Screensaver / updates / pairing:** Do not test automatic DreamService activation while the owner-disabled screensaver must remain off. LAN pairing and public updates are deliberately disabled pending separate security/provider/release approvals. Do not bypass Android installation consent.

## 24-hour physical endurance test

With OpenVista already playing your authorized playlist, leave the TV and PC operating normally. Run `scripts/hisense-soak-monitor.ps1 -Hours 24` in a Windows PowerShell where `adb` is on PATH, or use `-AdbPath` to specify the installed `adb.exe`. This tool **only observes**, samples each minute, and writes a private CSV and summary JSON under LocalAppData. It does not force the TV awake, change settings, capture private URLs or automatically approve a release.

Record start/end clock time, source category/resolution, any visible freeze, crash, black frame, recovery failure or thermal warning, and any required TV interaction. Return `summary.json` and `samples.csv` plus PASS/FAIL notes for independent acceptance. When an OS metric is unavailable, it is recorded as **NOT_MEASURABLE**, never as zero. Stable-v1 thresholds still require a reviewed baseline and a full uninterrupted 24-hour run. Failure or a gate-changing bug requires rerunning the full period.
