# External research and candidate reuse

**Updated 2026-10-25.** Reference projects are for learning, not a blanket authorization to reuse code or video.

**Start with the [curated upstream code research and implementation guide](curated-upstream-code-2026-10-25.md)** for a ranked shortlist, verified source paths, license distinctions, acceptance tests and step-by-step adoption order. The entries below preserve the earlier feasibility reference index.

## Core references
1. [Aerial Views (GitHub)](https://github.com/theothernt/AerialViews): actively developed Android/Google/Fire TV screensaver supporting 4K/HDR where devices permit, local USB videos, multiple media feeds, clock overlays, weather, playlists, direct HLS/RTSP. A particularly relevant reference for OS integration and local-media handling. **Check its actual code license and media usage rights before copying anything.** Recent Google TV and Fire OS versions can restrict selection of external screensavers.
2. [Android TV Live Screensaver](https://github.com/hauxir/androidtv-live-screensaver): inspiration for live sources and DreamService; validate maintenance, licensing and functionality separately.
3. [Android DreamService official API](https://developer.android.com/reference/android/service/dreams/DreamService): screensaver component lifecycle, interaction and screen behavior.
4. [Android Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files): folder grants and Android 11+ picker restrictions.
5. [AndroidX Media3 supported formats](https://developer.android.com/media/media3/exoplayer/supported-formats): HLS/live support and device codec limitations.
6. [AndroidX Media3 supported devices](https://developer.android.com/media/media3/exoplayer/supported-devices): Android API minima and hardware testing caveats.
7. [Open-Meteo terms](https://open-meteo.com/en/terms): free non-commercial tier has limits and attribution requirements. **Do not assume it is allowed if the app later monetizes.**
8. [Matt Pocock engineering skills](https://github.com/mattpocock/skills): skills for interviewing, specs, TDD, reviews, architecture, workflow.

## Competitive/reuse assessment
- **Aerial Views:** extremely relevant Android TV baseline, but upstream [LICENSE](https://github.com/theothernt/AerialViews/blob/master/LICENSE) is **GPLv3** while OpenVista currently uses **MIT**. Research concepts without copying upstream source; a fork or copied derivative requires a conscious license/compliance decision, not MIT-only redistribution by default.
- **Weather provider:** evaluate legal, rate-limit and offline/cache policies before selection.
- **Webcams:** OpenVista does not distribute streams or source catalogs. Webcam research is kept in owner-managed local files, outside version control. Verify operator rights, geographic metadata and device playback before using any third-party source; public availability does not grant redistribution rights.
- **Live wallpaper library:** discovery task. No catalog source is approved; do not ingest videos or commit them to Git.

## Validation tasks
- Check actual LICENSE and build details of reuse candidates.
- Prototype app-mode and DreamService **initially only on Hisense 58U6HF / Fire OS 7**, recording all other devices as unverified. Expand test matrix in later, explicitly planned compatibility work.
- Test folder picker on devices with missing/limited document provider.
- Measure streams in 1080p and 4K on low-end TV hardware.
- **No third-party media endpoints are shipped** under owner clarification C-01. User-provided URLs and research-only fixtures are separate; focus public release testing on safe input, playback and package-content inspection.
- Read [official platform feasibility notes](platform-feasibility-2026-10-25.md), including Android's user-action restrictions for package updates and HLS/DASH support.
