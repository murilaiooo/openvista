# OpenVista local media and playlists

**Status:** Product decisions **D-49–D-58 confirmed 2026-10-25**. Specification of desired behavior, not evidence of an implemented player or hardware support.

## Media selection and discovery

- The user grants access to a folder on supported internal TV storage or USB flash/HDD (D-13). OpenVista must scan **all accessible nested subfolders automatically** (D-51); there is no recursive-scan toggle in the approved requirements.
- Only enumerate files within the user-granted document-tree boundary. The TV's Storage Access Framework provider may limit what can be selected/read; do not claim unrestricted access or request broad storage privileges to bypass restrictions.
- Proposed scanning safeguards: asynchronous discovery (do not freeze remote navigation), bounded traversal and loop/cycle defense, deduplication when the document provider exposes repeated IDs, graceful revocation/unplug response and sensible re-indexing on return.
- Recognize file candidates in **MP4, MKV, WebM, MOV** containers. Their video/audio codecs, bitrate, HDR and DRM conditions remain device- and content-dependent: extension recognition does **not** mean guaranteed playback (D-52).

## Playlist domain

- Keep **camera playlists** and **local-video playlists** separate (D-14).
- **Each camera playlist** supports sequence or shuffle (D-49), while camera switching uses the existing 1–60 minute scene rotation when enabled.
- Local-video playlists support sequence and shuffle (D-03), and the end of an individual video advances to the next item; when the playlist is exhausted, **repeat from the beginning** (D-53), including single-item lists.
- Users may **create, rename, reorganize/reorder, duplicate and delete** multiple playlists (D-50). Deleting a playlist must never delete source media files. The exact D-pad editing interface, confirmation screens and playlist-**item** reordering remain open.
- Saved playlist entries referencing disconnected USB files or revoked URIs must be handled as unavailable, not destructive cleanup without approval.

## Local playback controls

- The local video player uses **fixed 10-second seek increments** forward or backward from directional navigation (D-54), limited to valid bounds.
- During camera playback, directional navigation changes cameras instead (D-21); OS-owned Back/Home/OK handling applies in Dream mode.
- Play in immersive full screen, **fill/crop rather than stretch** (D-16). Keep sound muted by default unless the user enables it (D-06).
- **No local subtitle feature** (D-56): do not implement language selection, external subtitle loading, subtitle downloads or a subtitle UI in the initial product. Do not promise removal of subtitles permanently burned into user-provided video images.

## Reliability and resume

- **Stable playback first** (D-55, D-45). Prefer a compatible supported decode path and/or lower rendition if available; otherwise skip the unsupported media and explain. Do not promise software HDR-to-SDR conversion or transcoding for every TV.
- On a corrupt, unsupported or unreadable item: skip ahead to the next playable video, show an unobtrusive notification with optional diagnostics, and **stop safely if the entire finite playlist is unplayable** (D-57).
- Save the most recent **local-video playback position** and **resume from where the user stopped** the same accessible file (D-58). This applies to manual reopen and last-scene automatic Dream entry, consistent with D-42. A webcam is live and has **no seekable last timestamp**.
- Handle stale saved checkpoints: file missing/moved, changed duration, permission lost, deleted playlist and completed file. Exact checkpoint cadence and what "completed" means are implementation details; do not corrupt previously saved state.
- On USB unplug, inform the user and offer source selection (D-47). If all cameras fail, a fallback video playlist may be played **only when configured** (D-38). On return of the internet, **do not interrupt currently playing fallback video** (D-44).

## Validation targets (not executed)

1. Automatic recursive scan across nested folders with duplicates, empty folders, very large trees and inaccessible items.
2. MP4, MKV, WebM, MOV with supported and unsupported codecs/containers, HDR cases and absent audio tracks on real hardware.
3. Multiple camera/video playlists; camera sequence vs shuffle; create, rename, reorder, duplicate and delete without erasing source media.
4. Local video end-of-item advance, end-of-list repeat, one-item list and empty list states.
5. Exactly ±10-second seeks near time 0 / duration end; seekable vs nonseekable local sources.
6. Stable decoder fallback and bounded bad-file skipping with optional diagnostics; all-bad-source final state.
7. No subtitle settings/menu/auto-rendering for local media; no unsupported promise concerning burned-in subtitles.
8. Resume local file at last saved time after normal close, app relaunch, Dream reentry and process death as feasible.
9. Handle USB unplug, revoked URI, changed media duration and exhausted local fallback without freezes or infinite retries.
10. Record hardware model, Android/Fire OS version, container, video/audio codec, bitrate, HDR, observed result, CPU/RAM and dropped frames.

Read [decision log](decisions.md), [runtime requirements](runtime-and-recovery.md), [device matrix](../testing/compatibility.md), and [architecture hypotheses](../architecture/overview.md).
