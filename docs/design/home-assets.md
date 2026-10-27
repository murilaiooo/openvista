# Home scenery provenance

Issue #36 uses one original, generated photographic triptych as decorative Home artwork. It is not a camera preview, user media, bundled stream, video, playlist or downloadable wallpaper. The visible Home note identifies the scenery as illustrative. Camera, video and favorite counts come from the saved user library, including zero counts.

## Asset

- Repository path: `player/src/main/res/drawable-nodpi/home_scenery.png`.
- Generated on October 27, 2026 with the built-in OpenAI `image_gen.imagegen` tool. CLI/API fallback was not used.
- Original generator output: `C:\Users\muril\.codex\generated_images\01a17e2a-b173-7250-a31f-9de747e4f83b\exec-ceb005cd-9b70-43f7-a3fd-cd2d1b9b346c.png`.
- Input: text only. No personal files, stock download or third-party reference photograph was used.
- Composition: three equal portrait regions, depicting a mountain valley, ocean coast and alpine lake. The locations are illustrative and have no source metadata or named camera association.
- The original PNG was copied into the repository without external image processing. The original output remains in its generated-images directory.

The original Issue #36 reference PNGs are unavailable in the issue body and tracked repository. This asset follows the written scenic direction; it is not a reproduced reference or pixel-match claim.

## Generation prompt

> Use case: photorealistic-natural. Asset type: a single wide photographic triptych used as three equal full-height portrait panels in a native television app Home screen. Generate an original horizontal 3:2 image with three exactly equal vertical columns, edge to edge, NO gutters, NO borders, NO labels, NO UI. Left third: a serene evergreen forest valley opening toward layered blue mountain ridges and a soft dawn sky. Middle third: a tranquil rocky ocean coast with dark blue water and soft mist in late afternoon, subtle pale light on the horizon. Right third: a remote still alpine lake reflecting a warm amber sunset, mountains in silhouette. Each column is a distinct original natural landscape, photo-realistic with subtle analog film grain, premium editorial landscape photography, rich midnight blue and forest tones, natural atmospheric depth, restrained highlights. Compose each landscape for a narrow portrait crop with its horizon in the upper half, large calm low-detail dark lower third suitable for a native white text overlay, and a calm top area for white branding. No people, animals, houses, cameras, text, trademarks, watermarks, video-play UI or frame. These must look like evocative scenery, not specific live-camera thumbnails; no recognizable landmarks. No gradients, no text rendered into the image, no artificial illustration.

## Native presentation

`HomeScreen` decodes one shared bitmap on its artwork worker. A bounds pass selects a power-of-two sample that caps decoded dimensions at 1920 × 1080; RGB 565 is requested to reduce memory. Actual decode configuration and GPU memory use remain unmeasured. Each panel draws a source rectangle from that bitmap with native `Canvas`, `Paint` and `Rect`; it creates no cropped bitmap copies. Decode failure retains the existing abstract vector scenery for that panel.

Text, scrims, focus outlines and controls are native Android Views. Home does not download artwork, animate scenery, start video playback or request storage/network access for these images. The activity closes the artwork worker on destruction.

## Review still required

The generated image was visually inspected before inclusion. No Android build, test, lint, emulator, ADB, device operation or automated validation was run for this change, as the owner prohibited those actions in this session. Independent review must cover package contents, text clipping in English and Brazilian Portuguese, font scaling, overscan, D-pad navigation, focus restoration and measured TV memory before merge or distribution. This asset does not change any release gate.
