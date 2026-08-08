# Akış Gesture

[![Total Downloads](https://img.shields.io/github/downloads/omeryol/AkisGesture/total?color=blue&label=Total%20Downloads)](https://github.com/omeryol/AkisGesture/releases)
[![Latest Release](https://img.shields.io/github/v/release/omeryol/AkisGesture?color=green&label=Latest%20Release)](https://github.com/omeryol/AkisGesture/releases)

[Türkçe](README.md) | **English**

Akış Gesture is an open-source Android accessibility app that customizes left,
right, and bottom edge gestures, with particular attention to HyperOS devices.
It began from the MIT-licensed [OpenSwipe](https://github.com/ARCJ137442/OpenSwipe)
project; attribution and license notices are retained. Its application ID is
`io.github.omeryol.akisgesture`.

## v1.4.0 highlights

- Added a 3D phone map on the Home screen for visually managing edge gestures.
- Left and right trigger areas can be resized and repositioned vertically, with
  live measurements for both edges shown together.
- Refined translations, screen consistency, and visual feedback animations.

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

The MacroDroid integration and Quick Settings tile use the same safe control
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
