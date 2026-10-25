# Direct HLS/DASH live camera streams — verified 2026-10-25

**60 stream addresses, 59 cameras** (Ö3 StudioCam appears twice: once as HLS, once as DASH), all verified working on 2026-10-25, morning, America/Los_Angeles.

## How each stream was verified

Every address below passed two checks on 2026-10-25: the manifest returns a valid HLS playlist (`#EXTM3U`) or DASH manifest (`.mpd`), and `ffprobe` confirms a real video stream (codec and resolution shown in the table). No address in this list carries a session token in its query string. The Thailand streams were additionally frame-checked (one captured frame inspected per camera) on the same morning; the first two were re-confirmed in the final retest.

## Technical notes

- **Traffic cameras dominate this list.** Clean, token-free direct streams are published mainly by departments of transportation (Caltrans, Iowa DOT, MoDOT, Maryland SHA, VDOT, Arlington County, Thailand Department of Highways). Scenic beach/mountain cameras usually sit behind tokenized players, so they are not here.
- **Streams can go down without warning.** DOT cameras in particular are taken offline, moved, or renamed (on this same morning, all sampled Malibu D7 Caltrans streams returned 404, while D5 streams passed). Re-run the manifest + `ffprobe` check before relying on any address.
- **3Cat beauties (row 22) is a rotating channel**, not a fixed camera: it cycles through scenic cameras of the Catalan public broadcaster.
- **IPCamLive rows (19–20) are not a public agency**; they are a commercial camera-hosting service.
- **The last 3 rows are fixed cameras, but not scenic**: a radio studio (Ö3) and a parliament interior (Camera dei Deputati). They are listed separately at the end because they passed the same technical test.
- **TV channels were not included**, even when their streams worked, because they are not cameras.
- A camera that is dark at night in its local timezone is not necessarily down; recheck in local daytime.

## Streams

| # | Camera | Location | Category | Protocol | Video | Source | Stream |
|---|--------|----------|----------|----------|-------|--------|--------|

| 1 | I-80 MM 62, Cass County | Iowa, USA | Traffic | HLS | 480x270, h264 | Iowa DOT (511ia.org; descoberta via OpenCCTV) | `https://video3.iowadot.gov:8888/ankeny/80tv062lb/playlist.m3u8` |
| 2 | US-30, Ames | Ames, Iowa, USA | Traffic | HLS | 480x270, h264 | Iowa DOT (descoberta via OpenCCTV) | `https://video4.iowadot.gov:8888/ames/iwz3718lb/playlist.m3u8` |
| 3 | US-69, Ankeny | Ankeny, Iowa, USA | Traffic | HLS | 480x270, h264 | Iowa DOT (descoberta via OpenCCTV) | `https://video4.iowadot.gov:8888/ankeny/iwz5788lb/playlist.m3u8` |
| 4 | US-101 Ventura Blvd | Los Angeles, California, USA | Traffic/cidade | HLS | 1280x720, h264 | Caltrans — https://cwwp2.dot.ca.gov/vm/streamlist.htm | `https://wzmedia.dot.ca.gov/D7/CCTV-101.stream/playlist.m3u8` |
| 5 | US-101 Eureka, 14th St | Eureka, California, USA | Traffic/cidade | HLS | 320x240, h264 | Caltrans — https://cwwp2.dot.ca.gov/vm/loc/d1/us101eureka14thlookingnorthc039.htm | `https://wzmedia.dot.ca.gov/D1/eureka_14th_320x240.stream/playlist.m3u8` |
| 6 | I-5 Oceanside Blvd | Oceanside, California, USA | Traffic | HLS | 640x480, h264 | Caltrans — https://cwwp2.dot.ca.gov/vm/loc/d11/c102i5oceansideboulevard.htm | `https://wzmedia.dot.ca.gov/D11/C102_SB_5_at_Oceanside_Blvd.stream/playlist.m3u8` |
| 7 | US-101 Santa Barbara Rd | Santa Barbara, California, USA | Traffic | HLS | 896x504, h264 | Caltrans — https://cwwp2.dot.ca.gov/vm/loc/d5/us101santabarbarard.htm | `https://wzmedia.dot.ca.gov/D5/101atSantaBarbaraRd.stream/playlist.m3u8` |
| 8 | US-101 San Luis Bay Dr | San Luis Obispo, California, USA | Traffic | HLS | 896x504, h264 | Caltrans — https://cwwp2.dot.ca.gov/vm/loc/d5/us101sanluisbaydrive.htm | `https://wzmedia.dot.ca.gov/D5/101atSanLuisBayDr.stream/playlist.m3u8` |
| 9 | I-280 Monterey Blvd | San Francisco, California, USA | Traffic/cidade | HLS | 352x240, h264 | Caltrans — https://cwwp2.dot.ca.gov/vm/loc/d4/tv320i280onmontereybl.htm | `https://wzmedia.dot.ca.gov/D4/S280_on_Monterey_Bl.stream/playlist.m3u8` |
| 10 | I-405 Laguna Canyon | Laguna Beach, California, USA | Traffic | HLS | 1280x720, h264 | Caltrans — https://cwwp2.dot.ca.gov/vm/loc/d12/i405496lagunacanyon.htm | `https://wzmedia.dot.ca.gov/D12/SB405LagunaCanyon.stream/playlist.m3u8` |
| 11 | US-50 at I-97 | Annapolis, Maryland, USA | Traffic | HLS | 640x480, h264 | Maryland SHA (descoberta via OpenCCTV) | `https://strmr5.sha.maryland.gov/rtplive/8301f15800f700d700437a45351f0214/playlist.m3u8` |
| 12 | US-50 EB Oceanic Dr | Annapolis, Maryland, USA | Traffic | HLS | 640x480, h264 | Maryland SHA (descoberta via OpenCCTV) | `https://strmr5.sha.maryland.gov/rtplive/a30072ac00a600250064d336c4235c0a/playlist.m3u8` |
| 13 | 12th St S at South Clark St | Arlington, Virginia, USA | Traffic/cidade | HLS | 320x240, h264 | VDOT (descoberta via OpenCCTV) | `https://media-sfs2.vdotcameras.com/rtplive/38iggm3xn7t5tr9grypj6mmtuxjp165p/playlist_sfm4s.m3u8` |
| 14 | 10th St N at Highland | Arlington, Virginia, USA | Traffic/cidade | HLS | 352x240, h264 | Arlington County, VA (descoberta via OpenCCTV) | `https://itsvideo.arlingtonva.us:8011/live/cam74.stream/playlist.m3u8` |
| 15 | 12th St S at Clark (cam122) | Arlington, Virginia, USA | Traffic/cidade | HLS | 1280x720, h264 | Arlington County, VA (descoberta via OpenCCTV) | `https://itsvideo.arlingtonva.us:8012/live/cam122.stream/playlist.m3u8` |
| 16 | Route 141 at Clayton | Clayton, Missouri, USA | Traffic | HLS | 320x240, h264 | MoDOT (descoberta via OpenCCTV) | `https://sfs02-traveler.modot.mo.gov/rtplive/MODOT_CAM_200/playlist.m3u8` |
| 17 | Route 141 Corporate Woods | Bridgeton, Missouri, USA | Traffic | HLS | 1920x1080, h264 | MoDOT (descoberta via OpenCCTV) | `https://sfs01-traveler.modot.mo.gov/rtplive/MODOT_CAM_265/playlist.m3u8` |
| 18 | US-60 / US-25, Dexter | Dexter, Missouri, USA | Traffic | HLS | 320x240, h264 | MoDOT (descoberta via OpenCCTV) | `https://sfs03-traveler.modot.mo.gov/rtplive/MODOT_CAM_360/playlist.m3u8` |
| 19 | Ocean City MD Webcam | Ocean City, Maryland, USA | Beach | HLS | 1280x720, h264 | Descoberta via OpenCCTV; stream servido pela IPCamLive (não é órgão público) | `https://s67.ipcamlive.com/streams/43u2s3hnfxgkqt5zo/stream.m3u8` |
| 20 | 5th Avenue Beach | Manistee, Michigan, USA | Beach | HLS | 1024x576, h264 | Descoberta via OpenCCTV; stream servido pela IPCamLive (não é órgão público) | `https://s165.ipcamlive.com/streams/a5h5sd7bu4t0ymvic/stream.m3u8` |
| 21 | 104 y Playa | Villa Gesell, Buenos Aires, Argentina | Beach | HLS | 1920x1080, h264 | gesell.com.ar (descoberta via OpenCCTV) | `https://cam104.gesell.com.ar/playlist.m3u8` |
| 22 | 3Cat Càmeres del temps (beauties) | Catalonia, Spain | Rotating scenic cameras | HLS | up to 1920x1080 | 3Cat | `https://directes-tv-int.3catdirectes.cat/live-content/beauties-hls/master.m3u8` |
| 23 | HW32 Nakhon Luang | Nakhon Luang, Ayutthaya, Thailand | Traffic/highway | HLS | 1280x720, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-4/PER_4_011_IN.stream/playlist.m3u8` |
| 24 | HW2 Ban Phai | Ban Phai, Khon Kaen, Thailand | Traffic/highway | HLS | 1920x1080, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-6/PER_6_023.stream/playlist.m3u8` |
| 25 | HW4 Chumphon | Sawi, Chumphon, Thailand | Traffic/highway | HLS | 1280x720, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-5/PER_5_016.stream/playlist.m3u8` |
| 26 | HW3 Laem Chabang | Bang Lamung, Chon Buri, Thailand | Traffic/highway | HLS | 1920x1080, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-5/PER_5_002_IN.stream/playlist.m3u8` |
| 27 | HW41 Chaiya | Chaiya, Surat Thani, Thailand | Traffic/highway | HLS | 512x288, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-5/PER_5_017.stream/playlist.m3u8` |
| 28 | HW41 Wiang Sa (Thachi) | Wiang Sa, Surat Thani, Thailand | Traffic/highway | HLS | 1280x720, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-5/PER_5_018.stream/playlist.m3u8` |
| 29 | HW1 Banphot Phisai | Banphot Phisai, Nakhon Sawan, Thailand | Traffic/highway | HLS | 768x432, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-10/PER_10_013.stream/playlist.m3u8` |
| 30 | Saeng Sanit Road | Maha Sarakham, Thailand | Traffic/highway | HLS | 768x432, h264 | Thailand Department of Highways | `https://streaming3.highwaytraffic.go.th/PER-11/PER_11_004.stream/playlist.m3u8` |
| 31 | HW333 Dan Chang | Dan Chang, Thailand | Traffic/highway | HLS | 1920x1080, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-9/PER-9-021.stream/playlist.m3u8` |
| 32 | HW4 Kra Buri | Kra Buri, Thailand | Traffic/highway | HLS | 768x432, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-8/PER_8_023.stream/playlist.m3u8` |
| 33 | HW213 Phu Phan | Phu Phan, Thailand | Traffic/highway | HLS | 1920x1080, h264 | Thailand Department of Highways | `https://streaming1.highwaytraffic.go.th/PER-9/PER_9_008.stream/playlist.m3u8` |
| 34 | SR-1 Ocean Ave | Lompoc, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atOceanAve.stream/playlist.m3u8` |
| 35 | SR-1 Hearst Castle | San Simeon, CA, USA | Traffic | HLS | 1920x1080 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atHearstCastle.stream/playlist.m3u8` |
| 36 | SR-1 Carmel River CMS | Carmel, CA, USA | Traffic | HLS | 896x504 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atCarmelRiverCMS.stream/playlist.m3u8` |
| 37 | SR-1 Moss Landing Rd | Moss Landing, CA, USA | Traffic | HLS | 896x504 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atMossLandingRd.stream/playlist.m3u8` |
| 38 | SR-1 Rio Del Mar Blvd | Aptos, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atRiodelMarBlvd.stream/playlist.m3u8` |
| 39 | SR-1 South of SR-46 West | Cayucos, CA, USA | Traffic | HLS | 896x504 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atRoute46WestCMS.stream/playlist.m3u8` |
| 40 | SR-1 Central Ave | Lompoc, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atCentralAve.stream/playlist.m3u8` |
| 41 | SR-1 Foothill Blvd | San Luis Obispo, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atFoothillBlvd.stream/playlist.m3u8` |
| 42 | SR-17 Fishhook (Pasatiempo Dr) | Santa Cruz, CA, USA | Traffic | HLS | 1280x720 | Caltrans | `https://wzmedia.dot.ca.gov/D5/17atPasatiempoDr.stream/playlist.m3u8` |
| 43 | SR-17 Laurel Curves | Scotts Valley, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/17atLaurelRd.stream/playlist.m3u8` |
| 44 | SR-154 Calle Real / State St | Santa Barbara, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/154atCalleReal-StateSt.stream/playlist.m3u8` |
| 45 | SR-1 Manhattan Beach Blvd | Manhattan Beach, CA, USA | Traffic | HLS | 1280x720 | Caltrans | `https://wzmedia.dot.ca.gov/D7/CCTV-226.stream/playlist.m3u8` |
| 46 | SR-1 Bay Ave / Porter St | Soquel, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atBayAve-PorterSt.stream/playlist.m3u8` |
| 47 | SR-1 Buena Vista Dr | Watsonville, CA, USA | Traffic | HLS | 640x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atBuenaVistaDr.stream/playlist.m3u8` |
| 48 | SR-1 East of Morrissey Blvd | Santa Cruz, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atEastofMorriseyBlvd.stream/playlist.m3u8` |
| 49 | SR-1 Green Valley Rd (Harkins Slough Rd) | Watsonville, CA, USA | Traffic | HLS | 1280x720 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atHarkinsSloughRd.stream/playlist.m3u8` |
| 50 | SR-1 Larkin Valley Rd | Watsonville, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atLarkinValleyRd.stream/playlist.m3u8` |
| 51 | SR-1 North of Del Monte Blvd (Del Monte Ave) | Monterey, CA, USA | Traffic | HLS | 896x504 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atDelMonteAve.stream/playlist.m3u8` |
| 52 | SR-1 Soquel Ave | Santa Cruz, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atSoquelAve.stream/playlist.m3u8` |
| 53 | SR-1 South of Mar Monte Ave | Watsonville, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atMarMonteAve.stream/playlist.m3u8` |
| 54 | SR-1 SR-68 West | Carmel, CA, USA | Traffic | HLS | 896x504 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atHwy68West.stream/playlist.m3u8` |
| 55 | SR-1 West of Morrissey Blvd | Santa Cruz, CA, USA | Traffic | HLS | 1280x720 | Caltrans | `https://wzmedia.dot.ca.gov/D5/1atWofMorriseyBlvd.stream/playlist.m3u8` |
| 56 | SR-17 South of Summit Road | Los Gatos, CA, USA | Traffic | HLS | 704x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/17SouthoftheSummit.stream/playlist.m3u8` |
| 57 | SR-17 Old Santa Cruz Highway | Los Gatos, CA, USA | Traffic | HLS | 720x480 | Caltrans | `https://wzmedia.dot.ca.gov/D5/17atOldSantaCruzHwy.stream/playlist.m3u8` |
| 58 | Ö3 StudioCam | Vienna, Austria | Studio (radio camera) | HLS | 1280x720 | ORF | `https://studiocam-oe3.mdn.ors.at/orf/studiocam_oe3/q6a/master.m3u8` |
| 59 | Ö3 StudioCam | Vienna, Austria | Studio (radio camera) | DASH | 1280x720 | ORF | `https://studiocam-oe3.mdn.ors.at/orf/studiocam_oe3/q6a/manifest.mpd` |
| 60 | Camera dei Deputati — Camera 2 | Rome, Italy | Parliament interior | HLS | 640x360 | Radio Radicale | `https://video-ar.radioradicale.it/diretta/camera2/playlist.m3u8` |

---

*Verified: 2026-10-25, morning, America/Los_Angeles. Method: manifest + ffprobe video stream, per address. No tokenized URLs are included.*