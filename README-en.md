# Akış Gesture

[![Total Downloads](https://img.shields.io/github/downloads/omeryol/AkisGesture/total?color=blue&label=Total%20Downloads)](https://github.com/omeryol/AkisGesture/releases)
[![Latest Release](https://img.shields.io/github/v/release/omeryol/AkisGesture?color=green&label=Latest%20Release)](https://github.com/omeryol/AkisGesture/releases)

[Türkçe](README.md) | **English**

Akış Gesture is an open-source Android accessibility app that customizes left,
right, and bottom edge gestures, with particular attention to HyperOS devices.
It began from the MIT-licensed [OpenSwipe](https://github.com/ARCJ137442/OpenSwipe)
project; attribution and license notices are retained. Its application ID is
`io.github.omeryol.akisgesture`.

## v1.9.0 highlights

- 🎨 **Organic Edge Animations:** Rebuilt 14 edge animation modules with fluid mechanics and natural luminosity while preserving Aurora Flow.
- 📊 **Live Home and Smart Diagnostic Hub:** Added a pulse-beacon Hero status card, dynamic edge indicators, and a 1-tap diagnostic panel with a HyperOS optimization guide.
- 📐 **19.5:9 Modern Phone Previews:** Updated gesture previews to modern smartphone ratios and added a vector neon trigger-zone visual.
- 🎯 **Bottom Edge and Section Ordering:** Enforced deterministic coordinate-based ordering with synchronized Left/Middle/Right position labels.
- 🎨 **Edge-Themed Card Framing:** Section cards and gesture settings now use the live theme color of their designated edge.

Signed APK: [Akış Gesture v1.9.0](https://github.com/omeryol/AkisGesture/releases/download/v1.9.0/app-release.apk)
SHA-256: `379DF5B76B8555AADB09933CE7B06CE45A2A82DEFBF3AD0EDF7081F277F96706`

## v1.8.1 highlights

- 🌍 **8 New Languages:** Added Russian (`ru`), Polish (`pl`), German (`de`), French (`fr`), Italian (`it`), Vietnamese (`vi`), Traditional Chinese (`zh-rTW`), and Persian (`fa`).
- 🌐 **22 Supported Languages:** Integrated a 22-language scrollable selector in Settings and updated Android 13+ Per-App Language Preferences.
- 📱 **Complete Key Parity:** 100% parity across all 607 string keys with native Android and HyperOS terminology.

Signed APK: [Akış Gesture v1.8.1](https://github.com/omeryol/AkisGesture/releases/download/v1.8.1/app-release.apk)
SHA-256: `33AA782A574E36298F2536643679026C582B89CFA522B74632723C34E45602B5`

## v1.8.0 highlights

- 📱 **Recent Apps Dock Menu:** Quick task switching by swiping and holding from the screen edge; smart mutual exclusion with Ring Menu.
- 🎛️ **Fully Customizable Dock Settings:** Independent sliders for recent apps count (2-6), inset distance, icon size, spacing, and arc curvature.
- ⏱️ **Configurable Reveal Hold Delay:** Customize hold duration before menu reveal with millisecond precision (150 ms – 1000 ms).
- ⚡ **120 FPS Ultra-Smooth Animations & Zero Lag:** Cached system calls and bitmap rendering during gesture moves to eliminate UI lag; instant launch via Android 14 BAL optimization.
- 🎯 **Strict Hit Detection:** Tightened target hit zones ensure selection only occurs directly over target bubble.

Signed APK: [Akış Gesture v1.8.0](https://github.com/omeryol/AkisGesture/releases/download/v1.8.0/app-release.apk)

SHA-256: `DCA0D61A36700F73A7702ED5899138196276F51E3412031DD9120D2750ABF37C`

## v1.7.3 highlights

- 🛡️ **Watchdog and Accessibility Protection:** Configurable from 5 seconds to 120 minutes, with immediate screen-on health checks and improved recovery after a manual stop.
- ⚡ **Quick Settings:** More reliable tile state synchronization with delayed verification and immediate watchdog signaling.
- 🔔 **Foreground Notification:** Added a notification-visibility option in Protection and a warning when the notification is disabled.
- 📚 **Dynamic Version History:** The About screen updates from GitHub Releases, with cached and bundled offline fallbacks.

Signed APK: [Akış Gesture v1.7.3](https://github.com/omeryol/AkisGesture/releases/download/v1.7.3/app-release.apk)

SHA-256: `A140FD22051873EBC56AE9EC062FF7C519D54DBBBE315868A1B625A1BFD107B`

## v1.7.2 highlights

- 🧭 **Dynamic Edge Sections:** Multiple gestures on one edge are automatically divided without overlap.
- 📱 **Clearer Edge Map:** Section labels, trigger areas, and assigned actions stay synchronized.
- 🛡️ **Root and Protection:** Root information has its own tab with direct permission actions.
- 🌍 **New Languages:** Amharic, Bengali, Spanish, Japanese, Korean, Portuguese, Quechua, and Swahili.
- 🎨 **UI Refinements:** Cards, buttons, icons, and the gesture editing flow were simplified.

## v1.7.0 highlights

- 🎨 **5 New Icon Families & Color Modes:** Fluent, Pixelart, Ionicons, Lucide, Radix families with Cyber Neon and Accent color modes.
- 🌊 **3D Physics & Fluid Animations:** Blinn-Phong lighting overhaul for surface tension, water bubbles, dynamic vortex, and natural flame.
- 📱 **Unified Interface:** Appearance tab reorganized into 3 expandable sections with anchored edge map geometry.
- ⚙️ **Feedback Flexibility:** Added option to disable animations (`FeedbackAnimation.NONE`) and stabilized L-gestures.

## v1.6.1 highlights

- General user-experience and interface refinements.
- Per-edge ring toggles and refreshed ring action cards.
- Improved blacklist and whitelist status guidance.
- Clearer colors and controls for the Home service-status card.

## v1.6.0 highlights

- Three configurable action rings per edge with action icons.
- Live tuning for ring size, spacing, inset, and half-arc curvature.
- Real device overlay preview while dragging ring settings.
- Home permission guidance and diagnostic ring tracing.

## v1.5.1 highlights

- Edge order is now consistent across Home, the phone map, Settings, and
  Gestures: Left → Bottom → Right.
- Automation apps cannot manage the service without explicit user consent;
  when allowed, they can only Start, Stop, or Toggle it.
- Root scope is clearer: core gestures do not require root and the app does not
  manage the system navigation bar.
- Telegram group and channel links were added, and the app icon was refreshed.

## Features

- Quick swipe, hold, and two-direction L-swipe gestures
- App-, orientation-, and system-aware pause conditions
- Adjustable edge area, thresholds, and sensitivity
- 15 distinct visual feedback styles, haptics, and optional sound
- App launching, navigation, media, system, and protected root actions
- JSON backup and restore for rules and settings
- Complete Turkish and English interface localization

## Permissions and safety

- **Accessibility service:** detects gestures and performs selected actions.
- **Vibration:** provides gesture feedback.
- **Camera:** used only for the flashlight action; the app does not capture
  photos or video.
- **Notifications and foreground service:** make service state visible and
  resilient.
- **Boot and battery-optimization exemption:** when enabled by the user, helps
  the service continue after a restart.

Disabling system navigation is outside the app's scope and can temporarily make
a device difficult to use. Export a JSON backup, confirm a recovery path, and
test one gesture first before making such system changes.

## Build and install

Requirements: JDK 21 and Android SDK 35.

```powershell
.\gradlew.bat assembleDebug
adb install --user 0 -r app\build\outputs\apk\debug\app-debug.apk
```

Signed release APKs are published only through
[GitHub Releases](https://github.com/omeryol/AkisGesture/releases), with a
version tag and SHA-256 checksum.

## Automation

Use these broadcast intents to control the Akış service from other apps:

```text
io.github.omeryol.akisgesture.action.START
io.github.omeryol.akisgesture.action.STOP
io.github.omeryol.akisgesture.action.TOGGLE
```

The automation-app integration and Quick Settings tile use the same safe control
path. These commands affect only the Akış Gesture service.

## Status and roadmap

Core left, right, and bottom gestures have been validated on HyperOS/Android
15. The next focus areas are real-device latency/false-trigger measurements
and more complete per-app profile workflows.

## Contributions and license

Akış Gesture is an independent hobby project and does not guarantee device
compatibility or individual support. Reproducible bug reports and contributions
are welcome. OpenSwipe-derived code and Akış Gesture contributions are licensed
under MIT; see [LICENSE](LICENSE) for details.

## Community

- [Telegram Group](https://t.me/+ZRMewoFvaIdhM2I0) — support, feedback, and device experiences
- [Telegram Channel](https://t.me/+ZTbxUGG-ynowOWE0) — release announcements and development news
