# Loading, empty and error states (issue #34)

OpenVista uses one restrained vocabulary for work the user waits on. It applies to every TV surface
and is implemented by two shared pieces:

- `TvLoading` / `LoadingDrawable` (`player/.../ui/TvLoading.kt`): the single indeterminate mark, a thin
  cyan arc on a faint ring. It animates only while shown and attached, so hidden indicators cost nothing.
- `OpenVistaActivity.message(text, busy = true)`: the screen feedback line. Busy work shows the spinner
  only after **350 ms**, so near-instant saves never flicker. After **30 s** the spinner stops and the text
  changes to *Still working…*, so no indicator spins forever. Any later message replaces it.

Rules:

1. Show feedback only for work that is actually pending; never add artificial delays.
2. Prefer inline feedback (feedback line or the panel that is loading) over blocking overlays.
3. Keep focus where it was; loading never steals or traps focus. Repeated OK presses restart, never duplicate, the work.
4. Distinguish *loading data*, *buffering*, *offline*, *permission required*, *unsupported* and *no content*.
5. Never expose a stream URL or file path in a message; use short, actionable text in English and pt-BR.

## Audit

| Flow | Loading | Empty | Error / retry | Notes |
| --- | --- | --- | --- | --- |
| Saved-camera preview (#33) | Spinner + *Connecting to this camera…* in the 16:9 pane after a 450 ms focus debounce | *Highlight a camera…* | Distinct *did not respond*, *TV is offline* (reloads automatically when the network returns) and *cannot decode*, each with **Retry** | One muted decoder; old loads cancelled; 15 s timeout. |
| Full-screen camera / video playback | Scene veil + playback notices (`SceneView`, `PlaybackNotice`) | No playable items → Home with message | Typed quiet/brief/persistent notices, backoff retries 1–15 min, network wait | Unchanged; already distinguishes offline vs failing source. |
| M3U import | Busy *Reading your selected list…* | — | *Import failed*; result counts added/skipped/duplicates | Cancelled when leaving the screen. |
| Folder selection / rescan (SAF, USB) | Busy *Loading…* | *No folders* / *No videos* text | Permission, unavailable, session-only and scan-limit messages from `LibraryAccess` | Revocation reports *access denied* with re-select guidance. |
| Saving library or settings changes | Busy *Loading…* (shown only if the write exceeds 350 ms) | — | *Library changes could not be saved safely…* | Data is retained on failure. |
| Find location / city search | *Searching…* inline in the dialog | *No matches* | *City search unavailable* | Only city and country are sent. |
| Source map | *Loading map…* notice | *No cameras with coordinates* | Falls back to saved tiles (*Saved map*) | |
| Weather and forecast | Overlay groups hide until data exists | Honest placeholders / hidden group | Provider fallback (NWS → Open-Meteo), cached data, no fictional values | No spinner over the scene by design. |

## Device evidence

Checked on the Hisense 58U6HF (Fire OS 7) on 2026-10-30: camera preview loading → live video, rapid
25-step D-pad navigation with only the final camera loading, an unresponsive camera showing the failure
state with Retry, OK to full screen and Back to the same list row.
