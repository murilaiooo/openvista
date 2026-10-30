# R11 — Ambient Routines: predictable daypart scene profiles

**Verdict:** GO for isolated schedule logic tests, HOLD activation. E1 source audit, no OEM alarm behavior verified. [Research #43](https://github.com/murilaiooo/openvista/issues/43).

## Why it matters
A static start/end screensaver window currently cannot express “use morning coastline playlist and calm clock, then cities at lunch, then night skylines with a darker overlay”. The feature should change content selection **only while OpenVista is already eligible to run** and respect a manual override. It must not turn on the TV, bypass idle rules or fight the OS power manager.

## Baseline
[AppSettings](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) has `scheduleEnabled`, `scheduleStartMinute`, `scheduleEndMinute`; [SettingsScreens](../../../player/src/main/java/com/openvista/prototype/ui/SettingsScreens.kt) exposes schedule under Screensaver. [PlaybackSession](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) applies existing playlist and rotation rules. Do not replace or silently migrate the simple schedule with an incompatible recurring rule engine.

## Candidate model
A `Routine` is a named optional **local, recurring rule**: `timeWindow` plus optional `playlistId`, `mode`, `overlayPreset`, `soundscapeId`, `priority`, `enabled`. Five-minute minimum between automatic changes is a **research option**; define exceptions for manual user action and scheduled expiry. Time windows are in configured or device local zone and compare `Instant` to zone-aware rules. Cross-midnight and DST folds/gaps are explicit; choose deterministic “next valid occurrence” rules and document them. “No matching playlist/USB” means preserve current playback, not black screen. Stored IDs must survive renamed playlists and be removed gracefully if deleted.

Three scheduling strategies: (A) foreground tick/resume computation (recommended; no background system permission), (B) AlarmManager for closed-app activation (out of scope, requires OEM permission and adverse power impact), (C) external Home Assistant trigger (future gated R19, not a prerequisite). User manual selection overrides active routine until next scheduled boundary or selected duration; user can choose “Ignore routines today”.

## Mock matrix
Fake timezone clock and event log: 00:00 crossing, DST skipped hour and repeated 01:30, zone changed mid-session, suspend and reopen, app process killed, unplug USB, cameras all offline, 2 conflicting rules, repeat reboot, disabled routines, media family boundary. Assert no rapid switches, no duplicate event loop, no unsupported system wake and one clear winner when multiple rules apply. Proposal: <1 command per planned boundary and no additional periodic network work; not benchmarked.

## UX placement
Screensaver → **Routines** row with “Off / 2 routines”. A simple list editor: name → time range → playlist → optional mood preset. “Preview next transition” uses wall-clock calculations without starting player. Default existing schedule untouched. For accessibility, Back cancels draft, confirmation on deletion, restore focus to edited row. Keep regular Playlists discoverable under Cameras and My Videos.

## Blocking decisions
Do routine settings affect manual app playback, Dream playback or both? Recommended initial proposal: both while actively running, explicit on/off per entry point; none can control OS wake/sleep. Need real tests of Dream lifecycle and Android 9 scheduler quirks. Reject if routines become a new Settings hierarchy of dozens of toggles. Sources: local [existing screensaver decisions](../../product/decisions.md) and [Aerial Views](https://github.com/theothernt/AerialViews) scheduling precedent; no device experiment run.