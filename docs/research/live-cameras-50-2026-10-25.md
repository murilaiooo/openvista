# 50 live cameras around the world — research of 2026-10-25

**Status: research document. It is NOT the approved v1 catalog of OpenVista.**

Read this before using any link below as a source for the app:

- **None of these 50 cameras counts, yet, toward the v1 goal** (25 authorized, family-friendly cameras tested on the Hisense 58U6HF / Fire OS 7). The repository rule still applies: a camera only counts after (1) operator permission/attribution, (2) a direct, playable HLS/DASH stream, and (3) a real test on the device. See the evidence template in [webcam-source-evidence-template.md](webcam-source-evidence-template.md) and the v1 contract in [../product/v1-acceptance.md](../product/v1-acceptance.md).
- **The links below are official viewing pages, not direct streams.** No token, session manifest URL, or internal player link was extracted, copied, or published. Some operators use players with temporary tokens; extracting those links to embed in the app would bypass the operator's control, so it was not done here.
- **The videos, brands, and images belong to their respective operators.** This document only lists public links for viewing in a browser. It does not redistribute video, does not claim a license to use it in OpenVista, and does not change any operator's terms — before any integration into the app, read the operator's terms and, when required, ask for written permission.

## Authorization status — read carefully

**This document does NOT claim that all 50 cameras are authorized.** That claim would not be true today, and writing it in the repository would be worse than leaving the field blank: it would make the v1 gate look passed when it is not, and if the app shipped with that claim, an operator could demand removal or take action later — which is exactly the kind of block this project wants to avoid.

The honest status for every entry in this document is:

- **Authorization: NOT VERIFIED / PENDING** — no written permission from the operator has been recorded for any of the 50 viewing pages yet.
- **Publicly viewable is not the same as authorized for redistribution.** A page that anyone can open in a browser does not grant the right to embed, rebroadcast, or redistribute its video inside an app.
- The only exception to "pending" would be an operator that explicitly publishes an open/public feed with terms allowing reuse, or a written reply granting permission. In that case the evidence (operator, date, contact, and the exact terms) must be recorded in the evidence template, and only then may that single entry be marked "Authorized".

If you, the owner, already hold written permissions from any of these operators, send them and they will be recorded here immediately, entry by entry, with their evidence — that is the legitimate way to flip a status to "Authorized".

## How to unblock authorization without faking it

For each operator, in this order:

1. Identify the exact owner/operator of the camera (for aggregator pages, this is usually the aggregator and/or the original camera owner, not just the page host).
2. Send a short request (template below) and keep the reply.
3. Record the reply in the evidence template: operator, date, contact, what was granted (viewing only? embedding? redistribution?), and any attribution required.
4. Only then mark that entry "Authorized" and count it toward the v1 goal of 25.

Template request (adapt the bracketed parts):

> Subject: Permission request to embed your live camera in a free Android TV screensaver app
>
> Hello, I am developing OpenVista, a free, non-commercial Android TV screensaver/ambient-video app. I would like permission to embed your live camera "[camera name, location]" in the app, with attribution to you as the operator, linking back to your official page. The app will not record, rebroadcast, or claim ownership of your video. Please let me know if this is allowed, and if any attribution wording or conditions apply. Thank you.

## What the repository was before this document

OpenVista was created on 2026-10-24 (23:19 PDT; 2026-10-25 06:19 UTC) and, until this research, was in product discovery/definition: README, 78 confirmed decisions, implementation plan, and research documents — **no APK, no app code, and no verified cameras**. This document does not change that state: it adds a list of researched candidates, not a working app.

## Verification method — 2026-10-25, morning (America/Los_Angeles)

1. Starting point: the SkylineWebcams most-popular cameras page on 2026-10-25 (179 cameras listed that day) + EarthCam homepage featured pages the same day + 3 standalone official pages (Abbey Road Studios, NPS Yellowstone, and Explore.org).
2. Selection criteria: diversity of country and category (city, beach, landmark, nature, wildlife, volcano, port), preference for long-running operators with their own stable page, and at most one repeated location (Times Square appears twice, with two different operators and angles: SkylineWebcams and EarthCam 4K).
3. Test applied to each of the 50 links, by HTTP request with redirects followed: **result 50/50 with HTTP 200 and a live marker on the page** (title containing "LIVE"/"Live Cam" or a player/video on the page). The test checks the viewing page, it does not decode the video frame by frame, and it was not done on the Hisense TV.
4. Honest limit of the test: it was done from this virtual machine (datacenter IP). An operator may respond differently on a residential network, in another country, or with geographic blocking. A future failure of a link does not prove the camera died — and today's success does not prove it will stay online.

## Summary of the list

- **Total: 50 cameras, 28 countries.**
- **By operator/source:** SkylineWebcams 38, EarthCam 9, Abbey Road Studios 1, National Park Service / Yellowstone Forever 1, Explore.org 1.
- **By category (grouped):** city 16, beach 14, landmark 9, nature / wildlife / volcano / port / island 11.
- **Quality:** the pages are presented by the operators as HD, and EarthCam highlights Times Square in 4K. The effective resolution in a browser/TV depends on the source, the connection, and the device — bitrate was not measured here, and no camera is promised as "guaranteed 4K" in this document.

## The 50 cameras

| # | Camera | Country | Category | Source | Link |
| --- | --- | --- | --- | --- | --- |
| 1 | Times Square | United States | City | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/united-states/new-york/new-york/times-square.html](https://www.skylinewebcams.com/en/webcam/united-states/new-york/new-york/times-square.html) |
| 2 | Empire State Building | United States | City | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/united-states/new-york/new-york/empire-state-building.html](https://www.skylinewebcams.com/en/webcam/united-states/new-york/new-york/empire-state-building.html) |
| 3 | Las Vegas Strip | United States | City | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/united-states/nevada/las-vegas/las-vegas.html](https://www.skylinewebcams.com/en/webcam/united-states/nevada/las-vegas/las-vegas.html) |
| 4 | Sydney | Australia | City/port | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/australia/new-south-wales/sydney/sydney.html](https://www.skylinewebcams.com/en/webcam/australia/new-south-wales/sydney/sydney.html) |
| 5 | Shanghai | China | City | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/china/shanghai/shanghai/skyline-of-shanghai.html](https://www.skylinewebcams.com/en/webcam/china/shanghai/shanghai/skyline-of-shanghai.html) |
| 6 | Eiffel Tower, Paris | France | Landmark | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/france/ile-de-france/paris/paris-tour-eiffel.html](https://www.skylinewebcams.com/en/webcam/france/ile-de-france/paris/paris-tour-eiffel.html) |
| 7 | Trevi Fountain, Rome | Italy | Landmark | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/lazio/roma/fontana-di-trevi.html](https://www.skylinewebcams.com/en/webcam/italia/lazio/roma/fontana-di-trevi.html) |
| 8 | Colosseum, Rome | Italy | Landmark | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/lazio/roma/roma-colosseo.html](https://www.skylinewebcams.com/en/webcam/italia/lazio/roma/roma-colosseo.html) |
| 9 | St. Mark's Square, Venice | Italy | Landmark | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/veneto/venezia/piazza-san-marco.html](https://www.skylinewebcams.com/en/webcam/italia/veneto/venezia/piazza-san-marco.html) |
| 10 | Milan Cathedral (Duomo) | Italy | Landmark | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/lombardia/milano/duomo-milano.html](https://www.skylinewebcams.com/en/webcam/italia/lombardia/milano/duomo-milano.html) |
| 11 | Ponte Vecchio, Florence | Italy | Landmark | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/toscana/firenze/ponte-vecchio.html](https://www.skylinewebcams.com/en/webcam/italia/toscana/firenze/ponte-vecchio.html) |
| 12 | Stromboli Volcano | Italy | Volcano | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/italia/sicilia/messina/stromboli.html](https://www.skylinewebcams.com/en/webcam/italia/sicilia/messina/stromboli.html) |
| 13 | Prague | Czech Republic | City | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/czech-republic/prague/prague/prague.html](https://www.skylinewebcams.com/en/webcam/czech-republic/prague/prague/prague.html) |
| 14 | Plaza del Callao, Madrid | Spain | City | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/espana/comunidad-de-madrid/madrid/madrid-plaza-del-callao.html](https://www.skylinewebcams.com/en/webcam/espana/comunidad-de-madrid/madrid/madrid-plaza-del-callao.html) |
| 15 | Los Cristianos, Tenerife | Spain | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/espana/canarias/santa-cruz-de-tenerife/playa-los-cristianos.html](https://www.skylinewebcams.com/en/webcam/espana/canarias/santa-cruz-de-tenerife/playa-los-cristianos.html) |
| 16 | Las Vistas, Tenerife | Spain | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/espana/canarias/santa-cruz-de-tenerife/playa-las-vistas.html](https://www.skylinewebcams.com/en/webcam/espana/canarias/santa-cruz-de-tenerife/playa-las-vistas.html) |
| 17 | Playa del Sardinero, Santander | Spain | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/espana/cantabria/santander/playa-del-sardinero.html](https://www.skylinewebcams.com/en/webcam/espana/cantabria/santander/playa-del-sardinero.html) |
| 18 | Porto Seguro, Bahia | Brazil | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/brasil/bahia/porto-seguro/praia-de-taperapuan.html](https://www.skylinewebcams.com/en/webcam/brasil/bahia/porto-seguro/praia-de-taperapuan.html) |
| 19 | Copacabana, Rio de Janeiro | Brazil | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/brasil/rio-de-janeiro/rio-de-janeiro/copacabana-beach.html](https://www.skylinewebcams.com/en/webcam/brasil/rio-de-janeiro/rio-de-janeiro/copacabana-beach.html) |
| 20 | Western Wall, Jerusalem | Israel | Landmark | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/israel/jerusalem-district/jerusalem/western-wall.html](https://www.skylinewebcams.com/en/webcam/israel/jerusalem-district/jerusalem/western-wall.html) |
| 21 | Petra, Tesouro | Jordan | Landmark | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/jordan/maan/amman/petra-the-treasury.html](https://www.skylinewebcams.com/en/webcam/jordan/maan/amman/petra-the-treasury.html) |
| 22 | Hellenic Parliament, Athens | Greece | City | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/ellada/atiki/athina/hellenic-parliament.html](https://www.skylinewebcams.com/en/webcam/ellada/atiki/athina/hellenic-parliament.html) |
| 23 | Santorini, Firostefani | Greece | Island | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/ellada/naigaio/kyklades/santorini-firostefani.html](https://www.skylinewebcams.com/en/webcam/ellada/naigaio/kyklades/santorini-firostefani.html) |
| 24 | Jökulsárlón | Iceland | Nature/glacier | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/iceland/austurland/hofn/jokulsarlon.html](https://www.skylinewebcams.com/en/webcam/iceland/austurland/hofn/jokulsarlon.html) |
| 25 | Henningsvær, Ilhas Lofoten | Norway | Nature/aurora | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/norge/nordland/lofoten/henningsvaer.html](https://www.skylinewebcams.com/en/webcam/norge/nordland/lofoten/henningsvaer.html) |
| 26 | Tsavo East | Kenya | Wildlife | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/kenya/taita-taveta-county/voi/tsavo-east-national-park.html](https://www.skylinewebcams.com/en/webcam/kenya/taita-taveta-county/voi/tsavo-east-national-park.html) |
| 27 | Lower Zambezi | Zambia | Wildlife | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/zambia/lusaka-province/mafuta/zambezi-national-park.html](https://www.skylinewebcams.com/en/webcam/zambia/lusaka-province/mafuta/zambezi-national-park.html) |
| 28 | Willard Beach, Ballito | South Africa | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/south-africa/kwazulu-natal/kwadukuza/willard-beach.html](https://www.skylinewebcams.com/en/webcam/south-africa/kwazulu-natal/kwadukuza/willard-beach.html) |
| 29 | Kuredu | Maldives | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/maldives/lhaviyani-atoll/kurendhoo/maldives-kuredu.html](https://www.skylinewebcams.com/en/webcam/maldives/lhaviyani-atoll/kurendhoo/maldives-kuredu.html) |
| 30 | Anse Parnel, Takamaka | Seychelles | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/seychelles/mahe/takamaka/seychelles-takamaka.html](https://www.skylinewebcams.com/en/webcam/seychelles/mahe/takamaka/seychelles-takamaka.html) |
| 31 | Nungwi, Zanzibar | Tanzania | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/zanzibar/zanzibar-north/nungwi/zanzibar-nungwi.html](https://www.skylinewebcams.com/en/webcam/zanzibar/zanzibar-north/nungwi/zanzibar-nungwi.html) |
| 32 | Cabo San Lucas | Mexico | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/mexico/baja-california-sur/cabo-san-lucas/cabo-san-lucas.html](https://www.skylinewebcams.com/en/webcam/mexico/baja-california-sur/cabo-san-lucas/cabo-san-lucas.html) |
| 33 | Ushuaia | Argentina | Port | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/argentina/tierra-del-fuego/ushuaia/ushuaia.html](https://www.skylinewebcams.com/en/webcam/argentina/tierra-del-fuego/ushuaia/ushuaia.html) |
| 34 | Plaza Mayor, Cusco | Peru | City | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/peru/cusco/cusco/plaza-mayor.html](https://www.skylinewebcams.com/en/webcam/peru/cusco/cusco/plaza-mayor.html) |
| 35 | Blackpool | United Kingdom | City/pier | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/united-kingdom/england/blackpool/blackpool.html](https://www.skylinewebcams.com/en/webcam/united-kingdom/england/blackpool/blackpool.html) |
| 36 | Lagos | Portugal | Beach | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/portugal/algarve/lagos/lagos-portugal.html](https://www.skylinewebcams.com/en/webcam/portugal/algarve/lagos/lagos-portugal.html) |
| 37 | Shibuya, Tóquio | Japan | City | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/japan/kanto/tokyo/tokyo-shibuya.html](https://www.skylinewebcams.com/en/webcam/japan/kanto/tokyo/tokyo-shibuya.html) |
| 38 | Torres del Paine, Patagônia | Chile | Nature | SkylineWebcams | [https://www.skylinewebcams.com/en/webcam/chile/magallanes-region/torres-del-paine/patagonia.html](https://www.skylinewebcams.com/en/webcam/chile/magallanes-region/torres-del-paine/patagonia.html) |
| 39 | Times Square 4K | United States | City | EarthCam | [https://www.earthcam.com/usa/newyork/timessquare/?cam=tsnorth4k](https://www.earthcam.com/usa/newyork/timessquare/?cam=tsnorth4k) |
| 40 | Waikoloa Village, Hawaii | United States | Beach | EarthCam | [https://www.earthcam.com/usa/hawaii/bigisland/waikoloa/](https://www.earthcam.com/usa/hawaii/bigisland/waikoloa/) |
| 41 | CN Tower, Toronto | Canada | City | EarthCam | [https://www.earthcam.com/world/canada/toronto/cntower/?cam=cntower1](https://www.earthcam.com/world/canada/toronto/cntower/?cam=cntower1) |
| 42 | Maho Beach, Sint Maarten | Sint Maarten | Beach/airport | EarthCam | [https://www.earthcam.com/world/sintmaarten/](https://www.earthcam.com/world/sintmaarten/) |
| 43 | Skydeck, Chicago | United States | City | EarthCam | [https://www.earthcam.com/usa/illinois/chicago/skydeck/?cam=chicagoskydeck](https://www.earthcam.com/usa/illinois/chicago/skydeck/?cam=chicagoskydeck) |
| 44 | Pittsburgh | United States | City | EarthCam | [https://www.earthcam.com/usa/pennsylvania/pittsburgh/](https://www.earthcam.com/usa/pennsylvania/pittsburgh/) |
| 45 | Tamariu | Spain | Beach | EarthCam | [https://www.earthcam.com/world/spain/tamariu/](https://www.earthcam.com/world/spain/tamariu/) |
| 46 | Port of Los Angeles | United States | Port | EarthCam | [https://www.earthcam.com/usa/california/losangeles/port/?cam=portofla](https://www.earthcam.com/usa/california/losangeles/port/?cam=portofla) |
| 47 | Abbey Road Crossing, London | United Kingdom | Landmark | Abbey Road Studios (official) | [https://www.abbeyroad.com/crossing](https://www.abbeyroad.com/crossing) |
| 48 | Old Faithful, Yellowstone | United States | Nature/geyser | NPS / Yellowstone Forever | [https://www.nps.gov/yell/learn/photosmultimedia/webcams.htm](https://www.nps.gov/yell/learn/photosmultimedia/webcams.htm) |
| 49 | African River Wildlife Cam (Mpala) | Kenya | Wildlife | Explore.org | [https://explore.org/livecams/african-wildlife/african-river-wildlife-cam](https://explore.org/livecams/african-wildlife/african-river-wildlife-cam) |
| 50 | Nashville | United States | City | EarthCam | [https://www.earthcam.com/usa/tennessee/nashville/](https://www.earthcam.com/usa/tennessee/nashville/) |

## Notes by source

- **SkylineWebcams (38):** a long-running aggregator, with its own page per camera. It is the most homogeneous source in the list — good for browsing, but concentrating a future integration on a single aggregator is a risk: if its terms change, many cameras fall together. Treating each camera as a separate operator is wrong; in most cases the permission contact is SkylineWebcams and/or the original camera owner.
- **EarthCam (9):** its own network since 1996, with several cameras in 4K (Times Square is the flagship). Pages featured on the homepage on 2026-10-25.
- **Abbey Road Crossing (1):** official Abbey Road Studios page, with the famous London zebra crossing.
- **Old Faithful, Yellowstone (1):** official National Park Service page that gathers the park's webcams, including the Old Faithful livestream. The other park webcams mentioned on the page are static images — do not confuse them with the livestream.
- **African River Wildlife Cam (1):** Explore.org. Wildlife is, by nature, unpredictable: the camera may be live and still show no animal for long periods. That is normal, not a defect.

## What is still missing before any of these becomes a v1 source

1. Identify the exact owner/operator of each camera and its permission clause (or ask for written authorization).
2. Confirm that a public, stable HLS (`.m3u8`) or DASH (`.mpd`) stream exists, without a session token, released for the intended use.
3. Test it on the Hisense 58U6HF / Fire OS 7 with Media3: first frame, resolution, codec, 5–10 minutes without stalling, behavior when the camera goes down.
4. Record everything in the evidence template, one record per camera, with date, decision (APPROVED / REJECTED / PENDING), and reason.
5. Only then count it toward the v1 goal of 25 — and even then, keep this research list separate from the approved catalog, so "a link that opened in the browser" is never mixed up with "an approved source for the app".

## How to revalidate this list later

Repeat the HTTP 200 + live-marker test on the 50 URLs and note the new date. Links that move from 200 to 404, redirect to a generic page, or lose their player must leave the list or move to a "down as of <date>" section, never stay listed as if they were working. Cameras that are dark at night in their local timezone must be rechecked in local daytime before declaring a failure.

---

*Verification: 2026-10-25, morning, America/Los_Angeles. Method: HTTP request with redirects followed, 50/50 with HTTP 200 and a live marker. There was no playback test on the TV, no extraction of an internal stream, and this document makes no claim of a license to use the cameras in OpenVista. Authorization status for all entries: NOT VERIFIED / PENDING, unless a written permission is recorded as evidence.*
