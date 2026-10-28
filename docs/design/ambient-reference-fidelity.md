# Ambient viewer: reference composition and fidelity contract

Reference: owner-supplied OpenVista sunset mockup, October 27, 2026 (1672 × 941, 16:9). This document captures its layout as a production UI contract. The referenced image is present in the owner's conversation but is not represented as a shipped background or screenshot in this repository.

## Reference geometry (percent of the visible frame)

| Element | Left | Top | Width / distribution | Type |
| --- | --- | --- | --- | --- |
| openvista wordmark | 4.3% | 4.5% | Content-width only | lowercase, extremely light, quiet |
| Source header | ~75% | 4% | top-right cluster | green verified LIVE dot, city and country, optional verified flag |
| Source-local clock | ~75% | 8.2% | largest upper-group text | narrow thin sans, no bold |
| Source-local date | ~75% | 19.5% | aligned to clock left | secondary regular sans |
| Source weather | ~75% | 25% | weather symbol, condition, separated humidity and wind | compact |
| Viewer heading | 4.3% | 73% | left bottom cluster | small, low contrast |
| Viewer city | 4.3% | 76.5% | single line where possible | light sans |
| Viewer-local clock and date | 4.3% | 81% | thin large clock, date below | date on reference |
| Viewer current weather | ~25% | 80% | vertical separator, enlarged symbol, temperature, condition, high/low | not the forecast for the source |
| Forecast | ~47% | 78% | five even columns, thin vertical rules, icons, highs/lows and cyan rain markers | discreet bottom band |

## Non-negotiable appearance

- Live video stays edge-to-edge. Do not paste the mockup into the player, add a fixed scenic background, or paint opaque/rounded widget cards over the scene.
- Source and viewer are distinct compositions. Their clocks, temperatures and time zones are never swapped or inferred from a filename.
- Use Android's light/thin sans family, quiet secondary labels and measured safe margins. The source clock has 92sp baseline, viewer clock 70sp baseline; these values are scaled by existing overlay size and screen constraints. Preserve the text hierarchy rather than using uniform-sized clocks.
- Place 5 upcoming days after today in the default forecast, because the current day is represented by the viewer's current weather. Honor the existing 3–7-day selector with available real provider data.
- Forecast icons use clear sky gold, precipitation cyan and neutral cloud white. Rain probability has an original cyan droplet glyph; columns have subtle dividers. Missing weather is explicitly marked unavailable, never invented to resemble the reference. International locations use Open-Meteo with a discreet CC BY 4.0 provider credit; US locations favor NWS.
- The local user's stored city label, date preference and media items are separate from the app's released defaults. Source flags appear only for recognized country metadata.
- The soft, full-frame bottom gradient and text shadows are for readability; no solid black panels or expensive real-time blur.

## Burn-in behavior

At phase zero, match the reference composition. Display care then repositions the **complete four content blocks**, not individual glyphs, across four non-overlapping mirrored/top-bottom arrangements. Each block retains its internal typography and alignment. Motion is discrete with an adjustable interval (default five minutes), bounded by safe areas. The independent idle fade and user opt-out remain functional. This reduces prolonged static exposure but cannot guarantee against permanent panel damage.

## Validation

- Current implementation uses native Android Views and works with the already installed Media3 player; no new permissions, streaming sources or media have been bundled.
- 3840 × 2160, density override 480 on the owner's Hisense, has been used for visual captures, including video, separate clocks and actual Tucson forecast conditions. The provided screenshot is a **visual target**, not a capture from the current APK.
- Android debug build, focused placement tests and lint must pass before every installation. Four reposition phases must be checked on the hardware, including live-stream loading and missing-source-weather states.
- Exact pixel/font identity cannot be certified without the source design/font file and image-to-device comparison. Record observed deviations and do not label the implementation a 100% match without evidence.
