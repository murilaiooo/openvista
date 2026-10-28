# Missing current-day high temperature, Tucson (2026-10-27)

## Root cause
The official NWS forecast is a list of future day/night periods. Once Tucson's daytime forecast period has elapsed, the same-date night period may remain but the daytime maximum is not included. The ambient viewer correctly displayed the missing value as H: -- while still showing an NWS low. This was a data-coverage issue, not a font or positioning issue.

## Fix
The combined regional weather provider still prefers NWS data for US locations. Only when the official forecast has an entry for the user's **local current date** with a null maximum does the provider request Open-Meteo's daily weather forecast. It merges solely that day's forecast high, preserving the NWS low, current conditions and every other daily period. If the supplement fails, the existing official result remains unchanged; no high is guessed from current temperature or tomorrow's forecast.

A sourced high carries the visible Open-Meteo CC BY 4.0 attribution. The provider cache key was updated from v1 to v2 so previously cached incomplete nighttime weather does not persist after installing the fix. No change to playback, camera data, UI layout, permissions, external media or player decoder.

## Verification
- Added five local unit tests covering missing nighttime high, preservation of an already valid NWS high and low, avoiding tomorrow's high, invalid supplement and absent local date.
- Offline `:player:testDebugUnitTest :player:assembleDebug :player:lintDebug` passed.
- Updated the existing APK on the owner's Hisense without wiping library data; saved pre-change library JSON backup.
- Real hardware screenshot after launching a user camera showed **Tucson, AZ: H: 30° L: 14°**, with source stream playing in the background.
- This is a daily **forecast** maximum, not a station measurement of the observed historical high.
