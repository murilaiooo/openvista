<div align="center">

<img src="assets/branding/openvista-cover.png" alt="OpenVista ambient video app showing a live coastal camera, clock, local weather and five-day forecast on a television" width="100%" />

# OpenVista

**A window to everywhere.**

Bring live cameras, beautiful scenery and your own videos to the biggest screen in your home.

[![Development preview](https://img.shields.io/badge/status-development%20preview-293D52?style=flat-square)](docs/STATUS.md)
[![MIT License](https://img.shields.io/badge/license-MIT-168C94?style=flat-square)](LICENSE)

**English** · [Português (Brasil)](README.pt-BR.md) · [Project status](docs/STATUS.md) · [Roadmap](docs/roadmap.md) · [Contribute](CONTRIBUTING.md)

</div>

## Take a look

These screenshots show the app itself. The camera footage is an example of a user-added source, not content included with OpenVista.

### Home

<img src="assets/screenshots/home.webp" alt="OpenVista home screen with large cards for Cameras, My Videos and Favorites" width="100%" />

Find your cameras, local videos and favorites in one place.

### Live camera player

<img src="assets/screenshots/live-camera.webp" alt="OpenVista live camera player showing a beach, camera location, clocks, weather and forecast" width="100%" />

Enjoy the view with an optional clock, location details and weather.

### Settings

<img src="assets/screenshots/settings.webp" alt="OpenVista settings screen with options for playback, camera rotation, transitions and on-screen information" width="100%" />

Choose how your screen looks and behaves.

---

## What is OpenVista?

**OpenVista is an ambient video player for televisions.** It turns an Android-based TV into a window to places you love, using live camera streams and videos from your own collection. Watch a quiet beach, a busy city, a mountain landscape or a favorite travel recording, full screen and without a cluttered interface.

You choose what plays. OpenVista handles the viewing experience: moving between scenes, organizing your collection and showing useful information without covering the picture. Keep the screen clean or add a clock, location and weather details.

OpenVista is built for **Android TV and Fire TV**, with Google TV compatibility among the areas being evaluated. You can open it like a regular TV app. On devices that allow third-party screensavers, it can also be selected for the system's idle display; availability depends on the TV.

## What can you do with it?

### Watch live cameras from places around the world

Add your own compatible **HLS (M3U8)** or **MPEG-DASH (MPD)** stream links. Import a supported **M3U playlist** instead of entering each camera separately. Give sources meaningful names and add their city, country, category, time zone and coordinates when you know them.

Your cameras can show a beach at sunrise, a city after dark, a harbor, wildlife or any other scene available through a stream you are allowed to use. OpenVista doesn't decide which destinations you see.

### Turn local videos into an ambient display

Pick a video or a folder from your TV's storage or a connected USB drive, where the device supports it. Play your own landscape footage, travel clips, nature videos and other recordings without a streaming subscription. Video format and resolution support depend on the TV's codecs and hardware.

Use your videos even without an internet connection. They can also serve as a fallback when a saved live stream is unavailable, once you configure a local playlist.

### Build playlists and save favorites

Keep cameras and local videos in separate collections. Create playlists for different moods or places, choose shuffled or sequential playback, rearrange items and mark the sources you return to most often.

You can let live cameras change automatically instead of leaving the same scene on screen all day. The rotation interval is adjustable, with five minutes as the initial setting.

### Keep the scenery front and center

The fullscreen player is made for watching, not navigating menus. Use your TV remote to change cameras, control video playback and bring up controls when you need them. Temporary playback controls can disappear after a short period of inactivity.

Choose whether videos fill the screen or fit within it. Select an instant switch or a softer scene transition. Audio can stay off for a quiet background experience.

### Add a clock and weather, or show nothing at all

The information overlay is optional. Display the time and date, a camera's local time when its time zone is supplied, your chosen location and available weather conditions. A forecast view includes upcoming days and weather details when supported data is available.

Choose a clock-only layout, clock with weather, or no overlay. Adjust text size, color, transparency and position so the scenery remains easy to see. Overlay movement and inactivity hiding can reduce the time information stays fixed in one place, but they do not guarantee protection against screen burn-in.

**Weather availability:** the current forecast integration uses the U.S. National Weather Service for supported U.S. coordinates. Worldwide weather lookup and automatic city search are not available yet. Weather is optional, and camera playback doesn't depend on it.

### Explore the locations in your camera collection

A map view helps you explore saved cameras by location when you add coordinates to their details. You can also find cameras using names and filters such as country, city, category and favorites.

The map is based on your own saved locations. A complete worldwide map background and place-search service are still being developed.

## The app at a glance

| Screen | What you'll find |
| --- | --- |
| **Home** | Fast access to Cameras, My Videos and Favorites, with a way back to your last scene. |
| **Cameras** | Your live stream collection, source details, search, filters, playlists and location view. |
| **My Videos** | Locally selected files and folders, video playlists and fullscreen playback. |
| **Favorites** | A shortcut to the cameras and videos you want to keep close. |
| **Live player** | Fullscreen scenery, remote controls and optional clock, location and weather overlays. |
| **Settings** | Playback, sound, visual appearance, weather, language and screensaver preferences. |

## Make the experience yours

You don't need to configure everything before watching. Start with one camera or video, then change the details whenever you like.

| Setting | What you can adjust |
| --- | --- |
| **Playback** | Camera rotation interval, playback order, scene transitions, screen fit and video quality limit. |
| **Sound** | Enable or mute audio and use available audio tracks. |
| **Clock and weather** | Clock display, date, location, time format, temperature units and forecast information when available. |
| **Appearance** | Overlay presets, corner and position, text size, color, opacity and movement. |
| **Screen behavior** | Overlay hiding, optional playback scheduling and screensaver-related controls where the TV permits them. |
| **Language** | English or Brazilian Portuguese. |

OpenVista is designed around a TV remote, with no account required. Your sources, favorites, playlists and preferences are stored on the TV itself, not synchronized to an online account. Removing the app or clearing its data may erase those saved settings.

## Getting started

1. **Add a camera.** Open Cameras and enter a direct HLS/DASH stream URL, or import an M3U list you are authorized to use.
2. **Or add your videos.** Open My Videos and select a video or folder from accessible device or USB storage.
3. **Pick a view.** Play a source, add favorites or arrange a playlist. Let OpenVista rotate through cameras automatically if you prefer.
4. **Personalize the screen.** Open Settings to choose your clock, overlays, sound, transitions and optional weather location.

**You bring the scenery.** OpenVista doesn't ship with live camera feeds, an M3U catalog or bundled videos. This gives you control over what appears on your screen. Make sure you have permission to use any third-party streams you add.

## Frequently asked questions

**Is OpenVista a TV screensaver?**

It works as a fullscreen ambient video app. It also includes integration for the system screensaver on compatible devices, but some TVs do not allow third-party screensavers or control when they start.

**Does it include live webcams or video channels?**

No. You add your own supported stream URLs and playlists. The destinations shown in screenshots are examples, not a built-in camera service.

**Can I use OpenVista without the internet?**

Yes, for local videos that your TV can access. Live cameras and online weather need a connection.

**Can I paste a link from any video website?**

No. Live camera playback requires a compatible direct HLS or MPEG-DASH stream. A regular video webpage URL is not necessarily a playable stream.

**Does OpenVista show ads or require a subscription?**

OpenVista does not include an advertising system, user accounts or a subscription. It cannot control ads that a third-party stream provider might put into its own video feed.

**Is OpenVista available to download?**

OpenVista is currently a **development preview**, not a public stable release. There is no official public APK to install yet. You can follow progress in the [project status](docs/STATUS.md) and [roadmap](docs/roadmap.md). Device compatibility and individual features are still being tested.

---

<div align="center">

### OpenVista. A window to everywhere.

**Bring your own scenery. Make the screen yours.**

[Product decisions](docs/product/decisions.md) · [Project status](docs/STATUS.md) · [Roadmap](docs/roadmap.md) · [Contribute](CONTRIBUTING.md) · [MIT License](LICENSE)

</div>
