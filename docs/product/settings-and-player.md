# OpenVista Settings, player controls and favorites

**Status:** Owner decisions D-59–D-68 confirmed on 2026-10-25. This is a product interaction brief, not a built UI or approved widget-level visual design.

## Settings structure

- Present **simple, categorized Basic settings** with a separate **Advanced** area. Everyday controls (source selection, rotation, clock/weather and appearance, files, audio) should be discoverable without technical vocabulary.
- The Advanced area may surface compatibility limitations, actual playback statistics and error details without flooding the ordinary player interface.
- The exact information architecture, screen layout and D-pad focus map remain implementation tasks; no account or subscription settings are needed.

## The fullscreen controls menu

- Press **OK** to reveal a contextual player-controls menu. It includes an accessible **switch between Cameras and My Videos** without going back through Home (D-60).
- Automatically hide this transient **control menu after exactly five seconds of user inactivity** (D-65). This is separate from user-defined informational weather/clock overlay visibility and periodic anti-static repositioning (D-25).
- In camera mode, directional navigation changes scenes; local-video directions seek in **fixed ±10-second steps**. Handle focus on a revealed menu so arrows navigate menu items instead of accidentally changing scene/seeking.
- In DreamService the OS/OEM may consume OK, Back and other keys; document device-dependent behavior rather than changing system key mapping.

## Playlist and Favorites experience

- Newly created **camera playlists default to shuffle** (D-61). Each one still permits changing to sequential order as agreed in D-49.
- Default automatic camera rotation interval is **five minutes** (D-62); the user may configure **1–60 minutes** (D-11).
- The single **Favorites** hub contains clearly **separated Cameras and Local Videos sections** (D-63).
- For playlist items: **batch select/add many cameras or files, remove items, freely reorder items** (D-64). This is distinct from the earlier ability to reorder/duplicate whole playlists (D-50); camera/video playlist types remain separate (D-14).

## Diagnostics

- Normal UI is minimal and comprehensible. An **optional technical diagnostics panel** may expose resolution, codec, buffering and error details where actual device/player metrics provide them (D-66).
- Unsupported/unknown metrics are not fabricated. Log or diagnostics views must protect private user file paths and personal camera URLs.
- Diagnostics are local only unless a future, separately approved feature allows intentional user sharing; no implicit telemetry.

## Local-only automatic persistence

- Automatically save **settings, playlists and favorites on the television itself** (D-67), along with local-video resume checkpoints approved in D-58.
- **Do not implement settings import/export, cloud backup, account-based syncing, or automatic upload** for the initial release.
- Clearly explain that persistence on the TV is **not a separate backup**: uninstalling the app, clearing app data or factory resetting may remove these preferences. Revoke grants when the user removes a folder appropriately.
- Validate durability across normal app restarts, process death and TV reboot where storage is retained; do not promise persistence after uninstall.

## Connectivity restored while local fallback plays

- If network loss triggered a configured local playlist, **continue the currently playing local video without interruption** while bounded checks verify connectivity (D-44).
- Once an online camera is again actually playable, **finish the active local video first**, then return to live cameras automatically (D-68).
- The user has an **option to remain on local videos** rather than return. The exact button/UX entry point, persistence of the choice and which live camera resumes require screen-level specification.
- If no live camera is playable, remain in safe local/retry state; never cycle endless broken URLs. Respect disconnected USB storage and absent fallback.

## Acceptance test ideas (not executed)

1. From manual app and supported Dream mode, OK opens contextual controls, switches Cameras/My Videos without Home, and auto-hides controls at 5 seconds when idle.
2. Directional keys interact with focused controls predictably and do not accidentally seek/change the underlying source.
3. New camera playlist defaults to shuffle, can change to sequential; default scene interval is 5 minutes and can change from 1–60.
4. Favorite camera and local video appear in separate sections of the single Favorites screen.
5. Batch add/remove/reorder items; reorder whole playlists independently, no cross-media item mixing.
6. Basic/Advanced settings categories remain D-pad accessible without technical terminology in Basic.
7. Unknown/unavailable diagnostics metrics are labeled honestly and never display secrets in a normal screen.
8. Settings/favorites/playlists saved locally across restart, but no backup/export, login, remote telemetry or sync.
9. Network restoration during local fallback completes current local video then returns to an available camera, except when user elects to remain local.
10. Broken streams, connection flapping and USB removal do not create infinite retries or unexpected player takeover.

See [recorded decisions](decisions.md), [UX brief](experience.md), [local media spec](local-media-and-playlists.md), and [runtime policy](runtime-and-recovery.md).
