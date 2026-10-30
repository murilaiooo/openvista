# R11 deep investigation — Routines without waking the television

**Evidence:** E1 source audit. **Verdict:** pure schedule-math experiment is viable; no system alarm/background service authorized. [Earlier dossier](../dossiers/R11-ambient-routines.md).

## What is new vs already shipped

[AppSettings](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) already has `scheduleEnabled`, `scheduleStartMinute` and `scheduleEndMinute`. The existing schedule constrains *when* screensaver behavior is eligible. R11 is an opt-in **what-to-play and how-to-display** recurring rule applied only while OpenVista is active. It cannot bypass TV idle/power policy, change platform backlight or wake the TV.

## Precedence layers

1. OEM app/Dream eligibility and lifecycle (hard gate).
2. Existing screensaver schedule (unchanged).
3. User's manual selected source or ordered playlist.
4. If explicitly enabled, the effective time-of-day routine.
5. Normal current shuffle/rotation/fallback.

A manual override must persist until a defined boundary and never be immediately overwritten by a timer. A missing NAS folder cannot cause fallback storms or black screen. Routine selection must operate through the existing single-player interface, not create a second decoder.

## Candidate interface

```kotlin
data class RoutineRule(val id: String, val zone: ZoneId,
  val weekdays: Set<DayOfWeek>, val startLocal: LocalTime,
  val endLocal: LocalTime, val playlistId: String?,
  val presetId: String?, val enabled: Boolean)
data class RoutineDecision(val activeId: String?,
  val nextTransitionUtc: Instant?, val explanation: String)
fun evaluate(now: Instant, rules: List<RoutineRule>,
  override: UserOverride?, runtimeEligible: Boolean): RoutineDecision
```

Use UTC instants for comparisons and zone-aware calendar rules; monotonic elapsed time only for timers. Daylight savings: repeated 01:30 must not cause two abrupt switches unless product explicitly chooses double occurrence; missing 02:30 advances to first real instant. Cross-midnight 22:00–06:00 belongs to its start date. On resume after sleeping for hours, evaluate **current rule**, don't replay missed changes.

## Two possible runtime designs

**A. While-running scheduler:** evaluate on app start/resume and at next boundary; no alarm service. Preferred.
**B. AlarmManager/boot receiver to launch app:** not part of approved product, conflicts with OEM settings and power expectations, reject without explicit scope change.
**C. Home Assistant schedule triggers:** separate optional integration, not required for basic routines.

Store bounded rules separately or with a carefully scoped AtomicFile schema migration; [LibraryStore](../../../player/src/main/java/com/openvista/prototype/library/LibraryStore.kt) has strict total byte bounds and durability. Never corrupt current schedule or delete user playlists when removing routines.

## Concrete tests to run later

| ID | Fixture | Expected |
|---|---|---|
| 01 | zero routines | identical current behavior |
| 02 | daypart active 07:00–09:00 | applies once |
| 03 | overlapping same-priority routines | deterministic tie-break |
| 04 | 02:30 in DST forward gap | no infinite retry |
| 05 | 01:30 in DST backward overlap | policy defined and tested |
| 06 | cross-midnight Sunday/Monday | start-day semantics |
| 07 | clock jumps back 3 hours | no rapid switch |
| 08 | app sleeps for 8 hours | evaluate present, not missed events |
| 09 | user manual source chosen 1 minute before boundary | explicit priority |
| 10 | USB unplug mid-routine | existing local fallback preserved |
| 11 | deleted playlist ID | mark “Needs attention” |
| 12 | Dream starts after Activity finishes | no duplicate alarm or decoder |
| 13 | system schedule disallows playback | routine cannot start TV |
| 14 | EN/PT-BR + 720p long titles | no remote-focus trap |

Propose a Screensaver → Routines editor with “Next change” preview, a Pause routines today action in player, and no new Home card. **STOP** if rules require a permanent wakelock, re-enter after user control, or duplicate the current simple schedule without demonstrated user benefit. No E2 or device timing measurements occurred.
