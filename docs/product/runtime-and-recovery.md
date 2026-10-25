# OpenVista runtime, power and recovery behavior

**Status:** Product discovery. Round 5 choices D-39–D-48 confirmed on **2026-10-25**. This document specifies intended behavior, not verified OS compatibility or a working implementation.

## Entering automatic screensaver mode

- Offer activation choices of **1, 3, 5, 10, 15 and 30 minutes** **only when the TV OS/OEM allows** OpenVista to be selected and configured as the idle screensaver.
- Never assume a third-party app can enforce those system settings. Unsupported intervals or third-party DreamService selection must be explained in the UI without presenting dead controls.
- App mode (manually opened) remains independent and accessible on compatible devices.
- **Do not auto-launch at TV boot**. No boot-to-OpenVista receivers; launch through user action or OS-triggered screensaver where enabled.

## Playback and resume state

- Automatic entry should **continue the last selected camera or video** where it is still accessible.
- If the last camera is offline, apply the existing immediate skip and opt-in local-playlist fallback policy.
- If the last video is missing (USB removed, permission revoked), inform the user and offer source selection.
- **Video seek-position restoration is confirmed in D-58:** on reopening an accessible local video, resume at its saved playback position, including last-video automatic screensaver entry. Exact checkpoint frequency, end-of-file behavior and stale-file handling are implementation details. Camera streams have no seekable prior position.

## Remote in automatic mode

- **OK:** display contextual controls.
- **Back:** exit the screensaver.
- **Directional input:** navigate, with behavior contextual to the current source type (camera change vs local video seek).
- Respect platform-reserved keys, wake behavior and OEM input dispatch. These mappings require physical testing and must not be promised on devices that consume the keys before the app.

## Power and schedules

- Offer optional **stay-awake / keep-display-on preferences**, but **respect OS/OEM power savings, sleep, screen-off and device restrictions**. Never attempt unauthorized bypass of power policy.
- The experience can remain available continuously by default when running; provide **optional schedules**. Schedule windows, timezone, override and paused-state behavior remain open.
- Keep dream lifecycle and manual app fullscreen lifecycle distinct: platform may suspend a DreamService even if app mode can request an awake screen.

## Offline and resource management

- On internet outage, switch to the **existing user-configured local playlist**, where available. If no configured playable fallback exists, show a clear retry state.
- After switching to local playback, check reconnection periodically **without interrupting the playing video**. On verified connectivity and playable camera availability, **finish the active local video and then return to live cameras**, unless the user chooses **remain on local videos** (D-68). Avoid auto-switching immediately on a network-up signal; the exact user-setting UI remains an implementation detail.
- Use **bounded, low-frequency retries** with backoff; no busy loops, repeated stream probes or unnecessary background decoding.
- Prioritize **smooth playback** over forcing 4K; adapt quality within the stream's supported variants and reduce costly transition effects on weak devices.
- When playback UI is inactive or screen-off, **pause/unload unnecessary playback and network tasks**. Use appropriately cached user-imported list metadata and weather results; refresh only when needed and within provider/API limits.

## USB removal

- On unexpected removal, promptly show a **clear missing-drive notice** with an action to choose an alternate accessible source.
- Do not freeze, loop endlessly, request blanket filesystem permissions or crash when URI grants become inaccessible.
- If a chosen fallback source is itself on the disconnected drive, treat it as unavailable and present another source selection path.

## Critical acceptance checks (not yet performed)

1. On supported OS builds, show only working idle-time selections; preserve rollback instructions and original system screensaver settings.
2. No boot autostart; no privileged settings edits, root or undocumented ADB changes.
3. Distinguish OK/Back/D-pad in app mode and Dream mode; verify wake/exit.
4. Confirm last scene persisted through process death/TV sleep; missing scene falls back safely.
5. Optional schedules and keep-awake options never override system standby policy.
6. Airplane/network disconnection switches to configured local playlist; reconnection probes must not interrupt frames or audio.
7. No traffic/decoding storm while screen is off; cached forecast/user-source metadata stay bounded and labeled.
8. 1080p/4K and smooth/instant transition soak tests on low-end and higher-end Android TV devices; capture frame drops, CPU/RAM and energy effects.
9. USB disconnect mid-playback gives clear notice and selectable next source.
10. Distinguish requested versus *verified* device support in compatibility matrix.

See [decisions](decisions.md), [UX](experience.md), [architecture](../architecture/overview.md) and [device testing](../testing/compatibility.md).
