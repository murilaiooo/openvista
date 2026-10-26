# Hisense Fire OS 7: OpenVista DreamService activation evidence

**Date:** 2026-10-26
**Device:** Hisense 58U6HF / AFTHA001, Fire OS 7 (Android API 28)
**App package:** `com.openvista.prototype`, version `0.2.0-implementation-preview`
**Scope:** Owner-authorized activation on this physical TV only, not stable-v1 certification.

## Owner authorization and reversible configuration

The owner explicitly authorized re-enabling the **OpenVista** system screensaver after a prior request to keep the existing screensaver disabled.

| Setting | Before | After |
| --- | --- | --- |
| `secure/screensaver_enabled` | `0` | `1` |
| `secure/screensaver_components` | `tv.dumbtv.wallpaperdream/.PhotoDream` | `com.openvista.prototype/.OpenVistaDreamService` |
| `secure/screensaver_default_component` | `tv.dumbtv.wallpaperdream/.PhotoDream` | `com.openvista.prototype/.OpenVistaDreamService` |
| `system/screen_off_timeout` | `180000` | `180000` |
| `secure/contextual_screen_off_timeout` | `180000` | `180000` |

Original values were saved separately in an untracked, private Windows backup at `I:\codex\openvista-qa-private-20261026\dream-activation-20261026\before.json`. No unrelated applications, launchers, packages, user playlists, media, or credentials were changed.

## Physical automatic-entry test

- Fire OS package manager resolved `com.openvista.prototype/.OpenVistaDreamService` for `android.service.dreams.DreamService` with the `BIND_DREAM_SERVICE` protection.
- For a bounded idle test, changed **only** the system and contextual inactivity timers to `20000` ms, then observed the device without remote input for 37 seconds.
- `dumpsys dreams` reported `mCurrentDreamName=ComponentInfo{com.openvista.prototype/com.openvista.prototype.OpenVistaDreamService}`, with a non-null dream token and `mCurrentDreamIsTest=false`. `dumpsys power` reported `mWakefulness=Dreaming`.
- A private 3840 × 2160 physical TV screenshot showed an actual user-provided camera scene fullscreen, plus the OpenVista clock/date and Tucson weather overlay. The screenshot stays **outside Git**.
- Both timers were restored to `180000` ms and verified by read-back. Selection and `screensaver_enabled=1` remained unchanged, and DreamManager still identified OpenVista as the running dream in a subsequent check.

**Result:** automatic DreamService selection and entry were **observed on this Hisense**. The three-minute default duration was restored, but an additional complete natural 180-second wait was not timed. Controls/Back behavior in dream mode, wake/sleep transitions, scheduled playback, USB removal, 24-hour endurance and other Fire OS/Android TV models remain unverified.

## Reversal

The previous selections were `tv.dumbtv.wallpaperdream/.PhotoDream` (both fields) with `screensaver_enabled=0`. An authorized rollback may restore those exact saved values through Android secure settings. Do **not** roll back unless the owner requests it or a reproducible safety issue requires restoring the previous state.
