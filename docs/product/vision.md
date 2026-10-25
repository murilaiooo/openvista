# OpenVista: product vision

**Status:** Product discovery, rounds 1 and 2 confirmed on 2026-10-24.

## Confirmed product requirements

- **Platforms:** Android TV broadly, with compatibility research for Google TV and Fire TV/Fire OS. No single-device or universal compatibility claims.
- **Two entry points:** manually launched app and automatic screensaver where the OS/OEM supports a third-party service.
- **Home:** prominent **Cameras**, **My Videos**, and **Favorites** destinations.
- **Cameras:** browse via both an organized catalog and an **interactive world map**, or enter custom, permitted live-source URLs.
- **Live rotation:** the scene changes automatically at a user-configurable interval between **1 and 60 minutes**.
- **Unavailable webcam:** immediately advance to the next available webcam, avoiding endless cycling if all streams are unavailable.
- **Local videos:** browse a chosen folder on **internal storage or USB flash drive/external USB HDD**. Playback supports sequence, shuffle and **multiple playlists for videos**.
- **Separate playlists:** users can maintain multiple camera playlists and multiple local-video playlists; do not combine those two media types within one playlist.
- **Video display:** fill the screen, cropping excess image rather than stretching the image.
- **Live stream quality:** automatic selection with a user-configurable maximum, wherever the source and decoder make quality selection possible.
- **Clock and weather:** current conditions and **7-day forecast**; weather can be based on either the scene/camera location or the user's chosen location.
- **Customizable overlays:** presets and a **complete visual editor** to place and customize clock/weather widgets.
- **Sound:** muted by default with an option to turn on audio.
- **Localization:** Brazilian Portuguese and English initially.
- **Cost and access:** completely free, no advertisements and no user account.
- **First distribution:** APK on GitHub, after development and validation.
- **Potential future feature:** curated library of videos/live wallpapers, under compatible content rights and with user-approved scope.

## Open questions

- Exact screens and remote navigation patterns; map input and performance.
- Clock/weather widget defaults and editor controls, weather service/data licensing.
- Scene rotation defaults and error behavior if every camera fails.
- Playlist naming, ordering and favorites management.
- Media formats, USB picker support, HDR, streaming quality ceilings, and low-end TV performance.
- Thematic library, provider licensing and user-reported cameras.
- DreamService behavior on devices that disallow third-party system screensavers.
- Safety, power use, privacy and accessibility decisions.

## Design principles under investigation

Remote-first, immersive viewing, user-controlled appearance, no required account, minimal permissions, playback resilience and low-overhead operation. These are not claims that the app already exists or works.

## Validation goals

Prototype camera map interactions, dynamic stream switching, Android Storage Access Framework, Media3 playback and DreamService compatibility before choosing the final architecture. Use long-running playback tests on representative Fire TV, Android TV and Google TV hardware; measure dropped frames, memory, CPU and bandwidth.

See [recorded decisions](decisions.md), [architecture](../architecture/overview.md), [testing](../testing/compatibility.md) and [roadmap](../roadmap.md).
