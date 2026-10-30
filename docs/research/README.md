# OpenVista research

This directory keeps **public engineering references**, not live-camera playlists.

## Maintained references

- [Platform feasibility](platform-feasibility-2026-10-25.md): Android TV / Fire OS constraints and source-player decisions.
- [Curated upstream code research](curated-upstream-code-2026-10-25.md): compatible ideas, upstream licenses, and integration cautions.
- [Reference index](references.md): original upstream projects, platform APIs and licensing research.

## Private media policy

OpenVista is a player for user-supplied content. The application and its public repository do not provide a camera directory, playlists, webcam URLs, hosted video, or recorded wallpapers.

For development, keep source inventories and test media **outside Git**, for example under an ignored local working directory. Never add provider tokens, private lists, camera geolocation audits, or downloaded 4K video to a PR. The public project must build and run with an empty library.

The original dated stream-probing data was retired from the version-controlled tree before community publication. It remains recoverable through Git history by authorized maintainers, but should not be reintroduced to public branches. Git deletion **does not erase old commits or forks**; review history before changing the repository's visibility.
