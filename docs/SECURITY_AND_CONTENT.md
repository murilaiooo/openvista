# Security, privacy and media rights

## User media
- OpenVista must only read media authorized by the user, through scoped storage/folder selection where available.
- Do not request unrestricted filesystem access as a shortcut, and never upload user video files by default.
- Preserve tree URI grants securely; allow folder change/revoke, handle missing USB drives and inaccessible URIs.
- Recursive subfolder enumeration must stay inside the granted document tree, avoid infinite cycles and not assume direct filesystem paths. Do not expose file names/URIs in diagnostics by default.
- Saved local-video resume positions must remain local to the app and be resilient to removed, replaced or inaccessible files; never upload watch history automatically.
- Save settings, playlists, favorites and other preferences **locally and automatically only**. Initial scope has no cloud sync and no import/export. Clearing app data, uninstalling or factory reset may erase preferences; do not market local persistence as off-device backup.
- Optional playback diagnostics must mask local paths, private camera URLs, pairing tokens and location-sensitive details; show only device-reported metrics rather than invented codec/buffering values. Logs **remain local by default**; copy/share sanitized reports **only after a deliberate user action**. This is not a settings/playlist export feature.
- No user media should be copied to or committed in the GitHub repo.

## Camera streams and wallpaper assets
- A publicly viewable webcam does **not** mean its stream is freely embeddable, cached, mirrored, or redistributed.
- OpenVista **does not ship camera streams, stream playlists, or video files**. User-supplied sources are parsed and stored locally under user control. Verify that no research URLs or playlist fixtures are embedded in public APK assets; third-party media use remains subject to applicable source terms.
- Do not scrape tokenized video streams or bypass authentication, paywalls, geo restrictions or access controls.
- Accept custom user-provided media sources only where allowed by the applicable terms.
- Keep the user's source library separate from app code. Refresh a user-imported remote list only with explicit configuration, with validated schema and bounded network activity; keep locally saved source metadata available when disconnected. Never bundle external live-stream fixtures.
- OpenVista does not operate a community source-submission or publication pipeline. Any future first-party catalog or shared submission service requires a separate product decision and applicable authorization, attribution and takedown processes.
- Display available-only filters and live/quality metadata only when supported by actual observations. Treat unknown as unknown rather than falsely labeling sources online.
- QR-assisted URL entry is restricted to phone and TV on the same local network with **explicit TV pairing confirmation**; must not leak local endpoints or private tokens. Review LAN exposure, binding scope, pairing confirmation, short token expiry, replay/CSRF and untrusted URLs before implementation.

## Weather
- Provider must allow chosen distribution model; cache within terms, show stale-data state, and honor attribution.
- Exact location is optional and must require permission/clear consent. Provide manual city search; device-assisted location is opt-in and only available where the TV supports it. A camera's publicly known coordinates do not disclose the viewer's location.
- Show temperature, precipitation, wind, humidity and solar events only when the forecast provider offers applicable values and the data's freshness is known.
- API secrets stay out of the app repository and release artifacts when server-side use is required.

## Performance and system safety
- No root; no invasive TV settings changes without explicit approval, supported procedure and rollback.
- Limit reconnect loops, network transfer and decoder usage; expose a manual quality or data-saving setting if warranted.
- Protect OLED/QD-OLED against fixed overlays; user options to hide/move/dim them.
- Respect power/sleep, accessibility and normal uninstall semantics. Keep-awake preferences and optional schedules must not bypass device standby or manufacturer limitations.
- **No auto-launch at TV boot**, no privileged boot hacks or background services to force a screensaver to start. Use system DreamService eligibility only when supported.
- Suspend playback and unnecessary network activity while inactive or screen-off; cache catalog/weather updates within provider terms and retry network recovery at bounded intervals.
- On network loss, switching to a locally stored fallback is opt-in. Reconnection checks must not interrupt the active offline video. Do not silently switch away from the user's playing content.
- On USB removal or revoked folder grants, explain that the source is missing and allow a different selection; never crash or retry indefinitely.

## Reporting vulnerabilities
For now, contact the repository owner privately rather than publishing exploits, secrets or private stream URLs in issues.
