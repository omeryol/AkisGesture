# Akış Gesture

[![Total Downloads](https://img.shields.io/github/downloads/omeryol/AkisGesture/total?color=blue&label=Total%20Downloads)](https://github.com/omeryol/AkisGesture/releases)
[![Latest Release](https://img.shields.io/github/v/release/omeryol/AkisGesture?color=green&label=Latest%20Release)](https://github.com/omeryol/AkisGesture/releases)
[![Telegram Group](https://img.shields.io/badge/Telegram-Support%20Group-2CA5E0?logo=telegram)](https://t.me/+ZRMewoFvaIdhM2I0)
[![Telegram Channel](https://img.shields.io/badge/Telegram-Channel-2CA5E0?logo=telegram)](https://t.me/+ZTbxUGG-ynowOWE0)

[Türkçe](README.md) | **English**

Akış Gesture is an open-source Android accessibility app that customizes left,
right, and bottom edge gestures, with particular attention to HyperOS devices.
It began from the MIT-licensed [OpenSwipe](https://github.com/ARCJ137442/OpenSwipe)
project; attribution and license notices are retained. Its application ID is
`io.github.omeryol.akisgesture`.

> [!TIP]
> **Recommendation:** This release fixes the bottom-edge horizontal app-switch gesture, makes the accessibility service's self-repair far more reliable, and closes several rule/profile data-integrity bugs, so updating to **v1.9.3** (latest release) is recommended.

## v1.9.3 highlights

- 🤏 **Bottom-edge horizontal app switching fixed:** Flat/horizontal swipes are now recognized and dispatched correctly; they used to be silently cancelled.
- 🛡️ **More reliable accessibility service:** The health check now verifies that the edge sensor windows are actually attached, not just that the service process is alive; automatic repair now genuinely backs off after repeated failures instead of retrying forever at full speed.
- 🧩 **Rule and profile integrity:** Per-app profile rules can no longer be silently overwritten by an unrelated settings change; re-adding a rule for the same zone now correctly updates it instead of leaving a dead duplicate; app-shortcut and key-code labels now survive restarts and backup restores.
- 🔁 **More robust automation commands:** Back-to-back on/off triggers from automation apps no longer race each other.
- 🧹 **Memory leak and ANR risk fixed:** The Shizuku status listener is now cleaned up properly, and the Quick Settings tile no longer blocks the main thread.

Signed APK: [Akış Gesture v1.9.3](https://github.com/omeryol/AkisGesture/releases/download/v1.9.3/app-release.apk)
SHA-256: `06AF0D8F565AC557071EA6EEB90F5C5A9CE535420D4357A760B96CB07E0ADB00`

## v1.9.2 highlights

- 🌀 **Menus now fan out in a curve:** The ring menu and recent apps are arranged along a smooth arc instead of a straight row. The middle bubble sits furthest forward and the outer ones step back, so the layout looks natural and balanced.
- 🩹 **Bubbles no longer overlap:** However large you make the bubbles or however small you make the spacing, they never touch. There is always at least 1 dp of room between them, and the spacing adjusts itself as the menu grows.
- ⚖️ **Symmetry and edge distance:** The menu centres on your finger and slides as a whole instead of hitting the screen edge, and it never sits too close to the trigger edge.
- 🔐 **The automation permission really switches off:** While it is off, apps such as MacroDroid and Tasker cannot see or start Akış Gesture. While it is on, commands work reliably.
- 🎨 **New app icon:** A mark that brings the three edge gestures together in the middle; if your phone uses themed icons, the app matches them too.

Signed APK: [Akış Gesture v1.9.2](https://github.com/omeryol/AkisGesture/releases/download/v1.9.2/app-release.apk)
SHA-256: `6C72BE7BD324FCA26D3C28C0E3855D2758B26BF6703475098D4C8F25FC655808`

## v1.9.1 highlights

- 🎯 **Preserve Active Edge Tab:** Prevented resetting to the right edge when adding or editing gestures; the currently edited edge tab remains active.
- 🛡️ **Streamlined Protection Tab:** Refactored permissions card with compact status switches and prominent contextual alert banners for missing permissions.
- ⚡ **Tiered Root & Recovery Panel:** Structured root tools into "Tier 1: Automatic Repair" and "Tier 2: Periodic Watchdog" with expandable guidance notes.
- 🔒 **In-Device Component Isolation:** Secured exported receivers and activities against unauthorized inter-app intervention and intent injection; hardened data backup and automation rules.
- 🚀 **Background Resilience:** Reinforced reactive accessibility service healing, hang detection, and watchdog stability on HyperOS and aggressive battery managers.

Signed APK: [Akış Gesture v1.9.1](https://github.com/omeryol/AkisGesture/releases/download/v1.9.1/app-release.apk)
SHA-256: `01F73171350BD9CB40E13B99509B4430609429B5D3A13F4AAFA82B0688516151`

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
- Ring menu and recent-apps strip are laid out on a natural arc with symmetric,
  non-overlapping spacing that adapts automatically when the icons are enlarged
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

Use these intents to control the Akış service from other apps:

```text
io.github.omeryol.akisgesture.action.START
io.github.omeryol.akisgesture.action.STOP
io.github.omeryol.akisgesture.action.TOGGLE
```

The same names resolve both as broadcasts and as activities, so the target type
in your automation tool's "Send Intent" step may be Broadcast or Activity. For
the most reliable result, set the package name (`io.github.omeryol.akisgesture`)
explicitly: Android can drop implicit broadcasts while the app is in the
background.

MacroDroid and Tasker list Akış Gesture as a Locale/Tasker plugin (action: Start
gestures / Stop gestures / Toggle state). Re-editing the plugin preselects the
currently configured command.

The automation-app integration, the plugin, and the Quick Settings tile all use
the same safe control path. These commands affect only the Akış Gesture service.

Security switch: while **Allow automation apps** is off in Settings, every
externally reachable entry point (plugin, broadcast receiver, and the
start/stop/toggle activities) is disabled at the system level. Automation apps
then no longer see the plugin and the intents cannot be resolved. If the value
cannot be read, the safe default is "off".

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
