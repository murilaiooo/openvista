# R05 — Data Monitor: transparent download, upload and projected use

**Research verdict:** GO to Android API capability and accounting prototype; HOLD release until real Fire OS verification. Evidence **E1** from app code, with platform API behavior awaiting device test. [Track #44](https://github.com/murilaiooo/openvista/issues/44).

## Job and distinctions
A long-running TV ambient app may use substantial bandwidth. Users need **daily / weekly / monthly received and transmitted bytes** attributable to OpenVista, plus optional estimates of which camera or feature consumed data. The labels **all app traffic**, **stream-only estimate**, **Wi-Fi**, **network unknown**, **received** and **sent** must not be confused. Transport-layer bytes are not equal to video bitrate; caching, TLS, retries, headers and segmentation matter.

## Existing seams
[PlaybackSession](../../../player/src/main/java/com/openvista/prototype/playback/PlaybackSession.kt) exposes codec/buffer/resolution through diagnostics, not byte counters; [NetworkMonitor](../../../player/src/main/java/com/openvista/prototype/playback/NetworkMonitor.kt) observes connectivity. [AppSettings](../../../player/src/main/java/com/openvista/prototype/library/LibraryModels.kt) has no byte history. Do **not** attach per-sample writes to large serialized `LibraryStore` state at 1s cadence; use coarse aggregation into bounded storage with atomic writes, and never log raw URLs.

## Data acquisition options
1. **Android `TrafficStats.getUidRxBytes(Process.myUid())` / `getUidTxBytes`:** cumulative UID-level counters; returns UNSUPPORTED where unavailable; counters may reset on boot or UID state changes. This counts app traffic broadly, including API calls and embedded viewers, not reliably camera-only or a single network type.
2. **Media3 upstream `TransferListener` / `AnalyticsListener`:** label as tracked media-transfer estimates; some transfers, HTTP implementations or DRM/adaptive contexts may be counted differently. Separate this clearly from UID totals and de-duplicate lifecycle events.
3. **`NetworkStatsManager`:** finer network/history access is platform/permission-sensitive; do not assume a third-party Fire TV app can query per-UID historical data without special access. **Not a first-choice dependency**.
4. **Bitrate × duration:** approximation only, never present as measured bytes.

## Collector design
At start/resume sample monotonic RX/TX; every 30–60 seconds active sample again; compute only `max(0, current − previous)` for valid same-boot snapshot; detect negative/reset/unavailable, annotate data gap and reset baseline. Roll hourly buckets using UTC timestamps, display calendar day in user-chosen timezone; use 24 hourly + 30/90 daily summaries with explicit retention limit. Recovery from process death must mark gaps rather than extrapolate phantom traffic. A 24-hour screen time estimate is a *projection* using recent median active throughput, with a confidence range and “assuming same source quality” note.

## Acceptance tests
Deterministic fake counter inputs: 0/zero traffic, UNSUPPORTED (−1), boot reset, clock rollback, DST, midnight crossing, 2GB+ counters, paused player with weather refresh, uploader event, multi-process own UID, VPN/tethering, failed requests/retransmits and changing HLS variants. Test atomic persistence and safe rounding (MiB/GB correctly labelled; avoid confusing GiB and GB). Compare synthetic count against known downloaded test payload; require **measured vs estimate labels**. No user-space packet sniffing or VPN capture.

**Draft success metrics:** no more than 1 small persisted write/minute; no measurable decoder/frame impact; no unbounded counters; no permission prompt solely for monitoring the app's own UID. These targets are not measurements.

## UI / privacy
Advanced → **Data usage**: Today  • This week • This month; Received vs Sent; Approx. GB/month; Source breakdown if collection supports it; Clear local history. No persistent counter in player. If API unsupported show “Not available on this device”, not 0. No telemetry, off-device usage upload, external billing assumptions or device-wide monitoring.

## Gating
GO only after actual Fire OS 7 and at least one other Android TV OS confirm counter semantics, screen-off behavior, lifecycle, RAM footprint and battery. Tracking data is private, locally stored and deleted on user request. Platform reference: [Android TrafficStats docs](https://developer.android.com/reference/android/net/TrafficStats) (**URL noted; not read because this pass used only GitHub**).