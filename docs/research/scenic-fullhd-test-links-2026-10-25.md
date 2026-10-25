# OpenVista — scenic, beach, and city HLS streams (private feasibility research)

**Test date:** October 25, 2026. **Purpose:** investigate playback on Windows and later on the Hisense TV, **not** distribute camera feeds with OpenVista.

**Results:** 38 distinct URLs reported an actual video stream at **1920×1080 or higher** in 52 ffprobe attempts across two batches (2 overlapping successful URLs). The probe opened each reachable stream and read its codec and dimensions. **It was not an extended playback stability test or a Fire OS/Media3 test.**

Candidate source: [Live-Environment-Streams](https://github.com/william-ricchiuti/Live-Environment-Streams). Actual resolutions were measured using **ffprobe on Windows**, rather than inferred from upstream metadata. No login bypass, token extraction, paywall circumvention, or lengthy video recording was performed.

## Test playlist

Open [scenic-fullhd-test-only-2026-10-25.m3u](scenic-fullhd-test-only-2026-10-25.m3u) with VLC or a compatible HLS player. This playlist is **a separate local research fixture**; it is not used by the public app or included in APK assets.

## Links that passed the resolution probe

| Scene type | Location / subject | Measured resolution | Codec | HLS manifest |
| --- | --- | ---: | --- | --- |
| Beaches & Coast | Apúlia | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/bcapulia/playlist.m3u8` |
| Beaches & Coast | Arrifana | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/arrifana/playlist.m3u8` |
| Beaches & Coast | Carcavelos | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/carcavelos/playlist.m3u8` |
| Beaches & Coast | Costa da Caparica | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/costacaparicacds/playlist.m3u8` |
| Beaches & Coast | Costa da Caparica — Riviera | 2560×1440 | h264 | `https://video-auth1.iol.pt/beachcam/riviera/playlist.m3u8` |
| Beaches & Coast | Dębki — Beach | 2688×1520 | h264 | `https://ls.tkchopin.pl:9043/live/debki_plaza_static.stream/playlist.m3u8` |
| Beaches & Coast | Espinho | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/espinho/playlist.m3u8` |
| Beaches & Coast | Faro — Beach | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/bcfaro/playlist.m3u8` |
| Beaches & Coast | Gdynia Orłowo — Beach | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/gdynia_orlowo_plaza_obrotowa.stream/playlist.m3u8` |
| Beaches & Coast | Hel — boulevard | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/hel_bulwar_nadmorski_obrotowa.stream/playlist.m3u8` |
| Beaches & Coast | Jastarnia — Beach | 2560×1440 | h264 | `https://ls.tkchopin.pl:9043/live/jastarnia_plaza_dom_zdrojowy_obrotowa.stream/playlist.m3u8` |
| Beaches & Coast | Jastarnia — Pier | 2560×1440 | h264 | `https://ls.tkchopin.pl:9043/live/jastarnia_molo_static.stream/playlist.m3u8` |
| Beaches & Coast | Jurata — Pier | 2560×1440 | h264 | `https://ls.tkchopin.pl:9043/live/jurata_molo_static.stream/playlist.m3u8` |
| Beaches & Coast | Kalamata — Beach (Greece) | 2560×1440 | h264 | `https://livecameras.gr:30443/live/kala3/playlist.m3u8` |
| Beaches & Coast | Leça da Palmeira | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/lecadapalmeira/playlist.m3u8` |
| Beaches & Coast | Matosinhos | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/matosinhos/playlist.m3u8` |
| Beaches & Coast | Meia Praia | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/meiapraia/playlist.m3u8` |
| Beaches & Coast | Nazaré — Praia do Norte | 1920×1080 | h264 | `https://video-auth1.iol.pt/beachcam/nazareparadonorte/playlist.m3u8` |
| Beaches & Coast | Sopot — Beach | 2560×1440 | h264 | `https://ls.tkchopin.pl:9043/live/sopot_plaza_male_molo_obrotowa.stream/playlist.m3u8` |
| Beaches & Coast | Sopot — Pier | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/sopot_molo_skwer_kuracyjny_obrotowa.stream/playlist.m3u8` |
| Beaches & Coast | Władysławowo — Beach | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/wladyslawowo_plaza_obrotowa.stream/playlist.m3u8` |
| Lakes & Marinas | Gdynia — Marina | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/gdynia_marina_obrotowa.stream/playlist.m3u8` |
| Lakes & Marinas | Giżycko — Lake Marina | 2688×1520 | h264 | `https://stream.mazury24.eu/m_lester2/index.m3u8` |
| Lakes & Marinas | Kietlice — Lake | 1920×1080 | h264 | `https://stream.mazury24.eu/m_kietlice/index.m3u8` |
| Lakes & Marinas | Puck — Pier | 1920×1080 | h264 | `https://ls.tkchopin.pl:9043/live/puck_molo_static.stream/playlist.m3u8` |
| Cities & Architecture | Arachova — Town (Greece) | 1920×1080 | h264 | `https://livecameras.gr:30443/live/arach1/playlist.m3u8` |
| Cities & Architecture | Florence — Historic Center | 1920×1080 | h264 | `https://5e0add8153fcd.streamlock.net:1936/vedetta/via-della-scala.stream/chunklist_w1359048502.m3u8` |
| Cities & Architecture | Kraków — Main Square | 1920×1080 | h264 | `https://hoktastream2.webcamera.pl/krakow_cam_702b61/krakow_cam_702b61.stream/playlist.m3u8` |
| Cities & Architecture | Malbork — Castle | 1920×1080 | h264 | `https://hoktastream2.webcamera.pl/malbork_cam_0ffc99/malbork_cam_0ffc99.stream/playlist.m3u8` |
| Cities & Architecture | Warsaw — Castle Square | 1920×1080 | h264 | `https://hoktastream2.webcamera.pl/warszawa_cam_5f3b1a/warszawa_cam_5f3b1a.stream/chunks.m3u8` |
| Cities & Architecture | Wrocław — Town Square | 1920×1080 | h264 | `https://hoktastream1.webcamera.pl/wroclaw_przed_cam_16c68e/wroclaw_przed_cam_16c68e.stream/playlist.m3u8` |
| Cities & Architecture | Zakopane — Central Street | 1920×1080 | h264 | `https://hoktastream5.webcamera.pl/krupowki2_cam_62169a/krupowki2_cam_62169a.stream/playlist.m3u8` |
| Mountains | Bryce Resort — Mountains | 1920×1080 | h264 | `https://stream.resortcams.com/live/bryceresort.stream/playlist.m3u8` |
| Mountains | Bukowina — hotel | 1920×1080 | h264 | `https://hoktastream1.webcamera.pl/bukovina_cam_7aa04f/bukovina_cam_7aa04f.stream/playlist.m3u8` |
| Mountains | Bukowina — panorama | 5120×1440 | h264 | `https://hoktastream1.webcamera.pl/skywind_cam_3c5be6/skywind_cam_3c5be6.stream/playlist.m3u8` |
| Mountains | Karpacz — panorama | 1920×1080 | h264 | `https://hoktastream2.webcamera.pl/karpacz_cam_ab594f/karpacz_cam_ab594f.stream/playlist.m3u8` |
| Mountains | Rzepiska — Tatra Panorama | 1920×1080 | h264 | `https://hoktastream1.webcamera.pl/airnet_cam_d60f3c/airnet_cam_d60f3c.stream/playlist.m3u8` |
| Mountains | Wintergreen — Mountains | 1920×1080 | h264 | `https://stream.resortcams.com/live/wintergreen2.stream/playlist.m3u8` |

## Additional verification: one decoded frame

On top of the resolution probes, **FFmpeg decoded one real video frame on Windows** for four samples: **Nazaré (1920×1080)**, **Kalamata (2560×1440)**, **Sopot (2560×1440)**, and **Warsaw (1920×1080)**, without storing image or video files. See [the one-frame decode results](scenic-sample-frame-decode-2026-10-25.json). This proves individual sample decoding, **not** sustained playback or Media3 compatibility on the TV.

## Technical notes

- **Full HD** means at least 1920 pixels wide and 1080 pixels tall. A 5120×1440 ultra-wide panorama is not standard 3840×2160 UHD.
- **No uptime guarantee:** sources may change their URLs, reject clients, go offline, appear dark at local night, or downgrade stream quality.
- **Failures:** some Puerto Rico and Dominican Republic feeds did not respond in the probe; they are not in the Full HD shortlist.
- **Next test:** play selected sources for several minutes on Media3/Hisense, measure frame drops, buffering, CPU, and RAM at 1080p, 1440p, and ultra-wide dimensions.
- **Rights and scope:** these URLs are for private technical testing, not a promise to bundle them in any release. OpenVista now has a user-managed source model: the software ships no feeds, lists, or videos.

To reproduce the research, obtain the upstream `streams.geojson` at `%TEMP%\openvista-streams.geojson`, run `python scripts/probe_scenic_streams.py`, `python scripts/probe_scenic_city_streams.py`, and then `python scripts/build_scenic_test_playlist.py`. These tools use ffprobe and do not change TV settings.
