# OpenVista R&D v2 — Reproducible prototype and physical validation protocol

**Status:** **Test designs only. No tests were run.** This is an actionable manual/automated test contract for E2 and E3. Do not confuse checks described here with results. Current v1 [Hisense 24h soak protocol](../../testing/hisense-24h-soak-protocol.md) takes precedence over any experimental feature.

## A. Synthetic-first fixtures

Store **only synthetic, licensed, non-secret** inputs in isolated test repositories/worktrees. Never commit user M3U URLs or credential-bearing RTSP links. Tests should be deterministic and self-contained:
- `synthetic-sources-500.json` (test artifact to be created later) with 100 cities, 40% unknown coordinates, 25% simulated failures, 10% expired credentials, 50% nighttime; IDs are random opaque synthetic UUIDs.
- `timezones.csv`: UTC, Arizona (no DST), California/Europe (DST), Pacific dateline, high latitude, polar no-sunrise cases.
- `m3u-baseline-and-refresh`: test-owned 500-entry URL set without real hosts, signed-query rotation, duplicate provider ID and a 501-entry rejection fixture.
- `http-server-matrix`: mock WebDAV PROPFIND/206/416/TLS/redirect/401 endpoints and video byte-range fixtures.
- `rtsp-lab`: test-owned synthetic video/color-bar loop on local RTSP server, H264 baseline/main 720p/1080p with authenticated/unauthenticated variants.
- `tv-navigation-states`: route, focus tag, back-stack, page offset, D-pad event and expected destination.

Test fixture *names* here are plans; these files **do not yet exist**.

## B. Five experimental tracks and exact measurements

### B1. Deterministic Kotlin logic: R01/R02/R07/R11

Run plain JVM tests without Android or network. For random algorithm, store seed, attempt sequence and per-camera choice result. Report `failedStartRate = failedStarts/autoStarts`, `repeatWithin3 = repeatedWithinLast3/scenes`, `coverage = uniquePlayed/eligible`, `starvation95 = p95 rotations between visits`. Compare shuffle baseline to director ranking on exactly the same synthetic traces. Never select “better” based on aesthetic opinion alone.

Solar: compare astronomical event times with a separately vetted reference ephemeris; report minute deviation per latitude and season; daylight classification uncertainty required. Routines: use fake `Clock`/ZoneId with 23h/25h days; no alarm receiver or power changes. Health: count legitimate source-specific errors separately from TV-wide loss.

### B2. Byte accounting R05

Fake `TrafficStats` own UID counters, media TransferListener and separate weather fetch, with boot resets and gaps. Assertions: exact nonnegative arithmetic for stable epoch, no duplicate counting of Media3 bytes within UID totals, no false “0 data” on unsupported, bound file writes, source privacy. Artifacts: counter snapshots (synthetic), summary output and coverage label. **Physical TV** later verifies OS APIs and overhead.

### B3. Content integrity R13 and playback access R12

M3U three-way merge with baseline/remote/local, test idempotence and no favorite loss across malformed or empty feed. Inject crash at each state-commit stage; reopen database/library to prove old-or-new consistency. WebDAV controlled server asserts Range and auth redirect behavior. SMB library test must verify actual dependency license and API compatibility before compilation, not borrow GPLv3 source verbatim.

### B4. Decoder ownership R06/R10/R26

Preview focus storm, main video handoff, audio-only mixing/focus, RTSP 1080p playback; measure simultaneous MediaCodec instances, first-frame time, dropped frames, rebuffering, heap, CPU, sockets, app/Dream lifecycle, return-to-source focus. **Zero additional video decoder while main playing** is an invariant, not an optimization target. Run on test-owned/synthetic streams.

### B5. Provider/network integration R18/R19/R23/R24/R25

Home Assistant mock server with official authentication handshake; multiple mocked TV peers with authenticated command-only protocol; mock Photos OAuth/asset expiry without user credentials; no real Google API until policy/partner approval. YouTube ambient: **no experiment bypassing ads**; any official manual WebView test must remain isolated from Dream/rotation. Place metadata from licensed test fixtures with deliberate city ambiguities.

## C. Hardware validation protocol extension

Current first target: Hisense 58U6HF, Fire OS 7/Android 9 (firmware and package version must be recorded for every E3 run). Before experiment: back up **test** library only, ensure user private camera data is not touched, no second TV app installation without explicit permission, no system settings/launcher modification. Run E2 on separate branch, not main.

Sample real-TV scenario schedule **after** a feature is approved for independent validation:
- 0–10m: clean start, main player current camera, confirm stable frame advancement.
- 10–30m: 20 camera changes and D-pad Back/OK/Info; capture first-frame duration.
- 30–60m: deliberate network outage / reconnect with saved local fallback; no false Camera Health score.
- 60–90m: App→Dream→App transitions, no leaked player/audio/socket.
- 90–120m: 720p/1080p/4K source variants where TV codec supports; memory and drops.
- 24h soak: only after short pass, reuse established repo protocol, sample real resource metrics and source recovery. Independent tester needed; **not completed**.

For multi-TV require two real TVs on shared LAN; for HA require independently authorized server; for Photos require sanctioned provider approval and test account. The owner cannot validate HA/multi-TV and should not be treated as test resource.

## D. Proposed measurement schema (do not store secret URLs)

```json
{
  "experiment": "R26-rtsp-e2",
  "sourceType": "synthetic-h264-1080p",
  "evidenceLevel": "E2",
  "codeCommit": "<actual commit>",
  "platform": {"androidApi": 28, "device": "test device name"},
  "runs": 0,
  "firstFrameMs": {"median": null, "p95": null},
  "decoderInstancesMax": null,
  "heapMbPeak": null,
  "droppedFramesPerMin": null,
  "rebufferSecondsPerHour": null,
  "recoverySuccessRatio": null,
  "permissions": "test-owned source",
  "testCommand": "<exact command when run>",
  "result": "NOT_RUN"
}
```

No claim of pass if `runs=0` or measurement null. A “max 1 decoder” test records actual `decoderInstancesMax`; it is not inferred from one `ExoPlayer` object because device/system implementations may allocate different codecs.

## E. Report outputs and thresholds

For each feature: raw log sanitized, commit, exact Gradle/JUnit invocation, duration, fixture hash, failed cases, median/p95 first frame, dropped frames/CPU where relevant, and explicit rival baseline. **Thresholds in dossiers are hypothetical until benchmarked.** Never overfit to one 10-minute successful stream and claim all sources work. Failure counts, confidence interval and error classification are more useful than “passed 100%” on a tiny synthetic sample.

Mandatory stop conditions: private source data leaked, favorites lost, auto-playlist reordered without permission, second video decoder under foreground main playback, security gate bypass, provider policy violation, unbounded storage/work queue, OS power override, or app UI freezing. These trigger investigation rollback, not cosmetic “known issue” acceptance.

## F. Release relationship

The experimental program cannot replace owner-approved v1 acceptance G01–G10. A feature with E2 green simulated tests can still be **HOLD** because of E3 hardware, provider eligibility, licensing, UX, or 24h soak. Documentation merge is not an acceptance record.
