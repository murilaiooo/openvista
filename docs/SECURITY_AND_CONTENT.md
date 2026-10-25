# Security, privacy and media rights

## User media
- OpenVista must only read media authorized by the user, through scoped storage/folder selection where available.
- Do not request unrestricted filesystem access as a shortcut, and never upload user video files by default.
- Preserve tree URI grants securely; allow folder change/revoke, handle missing USB drives and inaccessible URIs.
- No user media should be copied to or committed in the GitHub repo.

## Camera streams and wallpaper assets
- A publicly viewable webcam does **not** mean its stream is freely embeddable, cached, mirrored, or redistributed.
- For each shipped provider: record title/owner, official permission or API terms, commercial policy, attribution, link, and fallback behavior.
- Do not scrape tokenized video streams or bypass authentication, paywalls, geo restrictions or access controls.
- Accept custom user-provided media sources only where allowed by the applicable terms.
- Keep the catalog separate from the code so invalid sources can be disabled without distributing a broken APK. Online metadata updates must validate integrity and schema; cache a verified copy and ship bundled fallback metadata.
- Community camera suggestions require human moderation/review, permission and attribution evidence, compatibility checks and a removal/takedown path; do not publish suggestions automatically.
- Display available-only filters and live/quality metadata only when supported by actual observations. Treat unknown as unknown rather than falsely labeling sources online.
- A QR-assisted URL entry flow must not leak local endpoints or private tokens. Review local-network exposure, binding scope, pairing confirmation, session expiry and untrusted URLs before choosing an implementation.

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
