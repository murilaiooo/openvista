# Content model: user-managed sources

**Status:** Owner clarification **C-01**, October 25, 2026. This updates the previously documented first-party camera catalog assumptions without changing the historical record of decisions D-01–D-78.

## Product boundary

OpenVista is a **media player, organizer, and ambient TV experience**. Users bring their own streams, playlists, and local video files. The OpenVista app and its public builds **do not bundle, curate, sell, host, fetch, or distribute camera feeds, streaming playlists, or downloadable video assets**.

Camera link research, source playlists, downloaded videos, and playback fixtures are kept in **owner-controlled, ignored local directories**, outside the public Git tree and all APK/release assets. They are not an OpenVista service or first-party catalog.

### Source entry and library

- A user adds a compatible live URL using the TV keyboard or a same-LAN, explicitly approved QR flow.
- Users manage their own camera collections and playlists, independently from the local-video playlists.
- **Source-list import:** support user-provided camera/stream lists. Engineering must decide and verify parsers for common formats such as M3U/M3U8, taking care not to promise an unsupported format yet. Imported lists must not become a platform-managed publicly hosted catalog.
- Users select local files/folders through a supported picker; recursive access remains within granted storage permissions.
- Users can name, categorize, favorite, and reorder their sources. Metadata such as country, city, camera time, status, and map coordinates is optional and never invented.
- An interactive map can show **only user-supplied sources with usable location data**. Other cameras remain accessible in a list; with no sources, the app offers an attractive **empty state** explaining how to add the first item.
- The offline experience provides saved metadata and accessible local video; metadata does not make remote streams playable without internet.

## Historical requirements affected

| Prior decision | Clarification for implementation |
| --- | --- |
| D-02, D-10, D-29, D-30, D-31, D-34 | Browsing, categories, filtering, and lists apply to **the user's source library**. Do not build a first-party curated directory, public catalog-update service, or community content-submission pipeline. |
| D-22, D-33, D-36, D-74 | Map pins and camera-dependent weather require **user-entered or source-provided verified location data**; no location should be guessed from an arbitrary URL. |
| D-71 | The old target of **25 project-provided authorized live cameras** is **superseded**. Public v1 must work with **zero bundled cameras**, including an understandable empty state and user-managed import. |
| D-77 | Family-suitability screening and takedown procedures apply only if the project ever decides to curate and publish content. OpenVista does not choose or publish third-party streams under the approved user-managed model. |
| D-37 | An integrated downloadable video library is still out of scope. |

The remaining interface, playback, weather, navigation, accessibility, performance, privacy, and TV compatibility goals remain unchanged, subject to proof.

## Tests and release gates

1. Start with zero cameras and zero local videos: no crashes, fake map markers, fake availability statuses, or dead-end navigation.
2. Add one user-entered authorized test HLS/DASH URL; play it, rotate between several added sources, save a favorite, and handle offline failure.
3. Import a local/user-controlled list after the format/parser has been experimentally specified: parse safely, reject malformed entries, cap size, deduplicate sensibly, and mask credentials in logs.
4. Map geotagged user sources; show unlocated sources in lists without hallucinated coordinates.
5. Read local/USB video folders with explicit user permissions; skip unreadable files and recover from disconnected devices.
6. **Ensure public APK/AAB and release bundles contain no live camera URLs, M3U files, streaming catalog fixtures, or user media**. Validate with automated release-artifact checks, not README assurances.
7. Keep research/test fixtures separated from runtime resources. Before any public release, test the package inventory and review permission/URL handling, security, playback, memory, and the 24-hour soak.
8. Provider permissions and third-party terms remain relevant to **individual usage and future content-sharing decisions**; do not imply that merely testing a normal public URL authorizes downloading or redistributing media.

## Documentation policy

Public-facing README and screenshots should emphasize the viewer experience; product boundaries and fixture isolation belong here, in security documentation, and in test/release checklists. **All repository prose, source comments, filenames when practical, issue/PR text, and documentation must be in English**, regardless of the owner's conversation language. Preserve proper nouns, original provider URLs, and other immutable identifiers.
