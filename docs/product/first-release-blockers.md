# OpenVista: first-release blockers and approval gates

**Status:** Discovery triage as of 2026-10-25. This is a planning checklist, **not** an approved cut of existing requirements, implementation record or release promise.

## Owner-approved constraints that cannot be silently dropped

- Both manually launched application and automatic system screensaver **where supported**; no boot auto-launch, root or invasive OEM changes.
- First public distribution via GitHub APK, fully free, no advertising/account.
- Camera catalog and 2D map, authorized sources and user-added URLs (TV keyboard/QR input), reviewed community suggestions, metadata refresh with local fallback.
- Local media and USB selection, recursively scanned MP4/MKV/WebM/MOV where device codecs allow; camera/video playlists, local resume, ten-second seek, no local subtitle feature.
- Simple/advanced customizable weather/clock, 7-day details, PT-BR/English and anti-static controls.
- D-pad-first experience with OK menu, five-second player controls, categorized Basic/Advanced settings, local-only automatic persistence.
- Offline playback/fallback/return policy, device-aware fluid playback, power/idle behavior, and safety measures.

## Decisions still needed from the owner

Round 8 in [the decision log](decisions.md) asks about:
- Whether to release clearly labeled development **pre-releases** before all approved features are complete.
- Initial device test/compatibility expectations, minimum verified webcam count and intended protocols.
- QR-assisted pairing threat boundary, offline map expectations and APK update experience.
- Release verification evidence, camera catalog family-appropriateness and optional local diagnostics/privacy.

These options are not approved until the owner answers. Device compatibility must be based on actual test results, not broad guarantees.

## Objective research / prototype gates (engineering, not user polling)

| Priority | Unknown / proof needed | Required evidence before selecting implementation |
| --- | --- | --- |
| P0 | Screensaver support across OEM/OS | Reproducible manual Activity vs DreamService tests, timeout and input behavior, safe rollback |
| P0 | Legal and reliable camera sources | Primary provider terms, permitted embeds/streams, attribution, source availability and catalog update rights |
| P0 | Source and player decoding on TVs | Local MP4/MKV/WebM/MOV samples, supported codecs/HDR paths, live stream variations, CPU/RAM/frame drops |
| P0 | Scoped storage and USB | SAF document-tree grants, nested-file enumeration, permissions over reboot, USB removal behavior |
| P0 | Weather provider licensing and API | Seven-day fields and limits, location/geo handling, caching/attribution, no paid-secret dependency |
| P0 | TV remote and map usability | Focus map for three-card Home, 2D map clusters/side list, quick OK switching, five-second controls, accessibility |
| P1 | QR URL entry safety and UX | Network pairing proof, authorization, expiration, isolation from private URL exfiltration |
| P1 | Low-end long-running performance | 24-hour soak, failover to local, reconnect without interruption, pause-on-background, power management |
| P1 | Versioned catalog and verified availability | Valid schema, signed/trusted metadata updates, safe cached fallback, bounded health checks |
| P1 | Offline/local persistence integrity | Last file checkpoint, playlists/favorites, process death, no export/sync, user-friendly loss-of-data warning |

## Workflow to implement after scope approval

1. Finish Round 8 product decisions, then write a compact **v1 acceptance specification** distinguishing required user behavior from OEM limitations.
2. Use the existing Matt Pocock research/specification skills to validate external licensing/device claims before coding against them.
3. Create narrowly scoped GitHub issues and ADRs only for costly-to-reverse architecture choices with real tradeoffs.
4. Build short device-sensitive spikes and a runnable Kotlin/Media3 vertical slice before attempting the full screensaver/catalog/editor UX.
5. Test locally and on real TV devices; record measurable pass/fail outcomes. Public release only with explicit supported-device caveats.

**No OpenVista APK or working app has been built yet.** Future wallpaper downloads remain excluded from the first release.
