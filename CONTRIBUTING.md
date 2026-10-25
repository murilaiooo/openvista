# Contributing to OpenVista

Thanks for your interest in OpenVista — an open-source ambient video experience built for the big screen.

OpenVista is currently in **engineering research and early development**. There is no public APK yet. Useful contributions include technical research, accessible TV interfaces, storage and playback prototypes, performance measurements, translations, documentation, and reproducible tests.

## Find something to work on

Start with the [project roadmap](docs/roadmap.md), [open issues](https://github.com/murilaiooo/openvista/issues), and [engineering plan](docs/implementation/priority-plan.md). If an issue is already assigned or has active discussion, coordinate there before beginning a large change.

Small, focused contributions with a clear test plan are especially valuable.

## Before proposing a change

- Read the [product vision](docs/product/vision.md) and [architecture hypotheses](docs/architecture/overview.md).
- Respect the [user-managed content model](docs/product/user-managed-content.md): the app does **not** bundle camera streams, streaming playlists or video media.
- Prefer supported Android APIs, practical TV D-pad navigation, and well-defined fallbacks.
- Use real evidence for device compatibility. A successful desktop playback test does not prove a stream works on Fire OS.
- Do not commit private stream URLs, account tokens, secrets, personal files, copyrighted footage, or test-fixture playlists into app runtime assets.

## Issues and bug reports

A useful report includes the expected result, observed behavior, steps to reproduce, and any relevant device details. For playback issues, include the OS version, source protocol (for example HLS/DASH), codec, resolution, and approximate bitrate where available.

Remove secrets, locations, and private URLs from logs before sharing them.

## Pull requests

1. Fork or branch from the current `main`.
2. Keep the change scoped to one issue or independently testable feature.
3. Describe the user-visible behavior and link the relevant issue or [decision record](docs/product/decisions.md).
4. Run applicable local tests and document the commands and results.
5. Explain device-specific assumptions, known limitations, and the rollback path for any OS integration change.

Changes to Android TV navigation, player lifecycle, USB access, or screensaver integration need physical-device verification before compatibility is claimed.

## Repository language

**English is mandatory** for all documentation, source-code comments, identifiers/descriptions, commit messages, issues, and pull requests — regardless of the language in which a request was originally made. Proper nouns, real place names, URLs, and externally defined identifiers remain unchanged.

The app itself is intended to support **English and Brazilian Portuguese** at launch. Translatable user-interface resources are the only intentional exception to the English-only repository prose rule.

## Licensing and conduct

OpenVista is released under the [MIT License](LICENSE). Only submit content and code you are permitted to contribute under compatible terms. Be respectful, constructive, and precise in reviews and discussions.

For sensitive security issues, avoid disclosing exploit details or secrets in a public issue; contact the repository maintainer privately first.
