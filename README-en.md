# Akış Gesture

Akış Gesture is a personal, open-source navigation app designed to provide
natural edge gestures on Android, with particular attention to HyperOS devices.

The project started from the MIT-licensed
[OpenSwipe](https://github.com/ARCJ137442/OpenSwipe) codebase. The original
copyright and license notice is preserved in `LICENSE`. Akış Gesture has its own
Android package identity, `io.github.omeryol.akisgesture`, and is maintained as
an independent project.

**English** | [Türkçe](README.md)

## Version v1.3.0 Release Highlights

- **↔️ Bottom-edge L Gestures**: Pull inward and turn right or left to use `L-right` and `L-left` gestures.
- **🖼️ Real Wallpaper**: The phone map uses the device's current wallpaper.
- **🎨 Colorful Independent Settings**: Edge toggles, appearance cards, icon packs, and feedback settings work independently.
- **💾 Complete Backup**: Settings, rules, app profiles, paused apps, and service preferences are preserved in one JSON backup.
- **🔄 Update Check**: Check GitHub Releases for a newer version from `Settings > About`.
- **🌍 English and Turkish**: New gesture names and updated settings flows are localized in both languages.

## Language support

The full interface is available in English and Turkish. Choose System default,
Türkçe, or English under `Settings > About > Language`. Additional languages
can be added through Android resources: English is in
`res/values/strings.xml`, Turkish is in `res/values-tr/strings.xml`, and the
supported locale list is in `res/xml/locales_config.xml`. A unit test verifies
that locale resource keys stay aligned.

## Screenshots

Selected screenshots captured from the current device build:

<p align="center">
  <img src="docs/screenshots/akis-home.jpg" width="180" alt="Home screen">
  <img src="docs/screenshots/akis-gestures.jpg" width="180" alt="Gestures screen">
  <img src="docs/screenshots/akis-appearance.jpg" width="180" alt="Appearance settings">
</p>
<p align="center">
  <img src="docs/screenshots/akis-about.jpg" width="180" alt="About screen">
  <img src="docs/screenshots/akis-actions.jpg" width="180" alt="Action picker">
  <img src="docs/screenshots/akis-rule-detail.jpg" width="180" alt="Rule details">
</p>

## Open source and permissions

Akış Gesture is an open-source, personal hobby project. Its source code can be
reviewed in the [GitHub repository](https://github.com/omeryol/AkisGesture). The
app is provided as-is; ongoing development, device compatibility, and individual
technical support are not guaranteed.

The Android permissions are used for these purposes:

- **Accessibility service:** Required to detect edge gestures and perform system
  actions such as Back, Home, and Recents. This sensitive permission is explicitly
  enabled by the user in Android settings.
- **Vibration:** Used for haptic gesture feedback.
- **Camera:** Declared only so the optional flashlight action can control the
  device torch; the app does not take photos or record video.
- **Notifications and foreground service:** Used to show that the gesture service
  is active and keep its status visible under Android background restrictions.
- **Boot completion and battery optimization exemption:** Used, when enabled by
  the user, to keep the gesture service available after reboot and reduce the
  chance of vendor battery management stopping it.

Rules and app settings can be exported as a JSON file from `Settings > Backup`.
Keeping that backup safe is the user's responsibility; automatic cloud backup and
data recovery are not guaranteed.

## Before disabling system navigation

Disabling the system navigation buttons or bar separately can leave the device
temporarily unusable if Akış Gesture stops or its accessibility service is
disabled. Akış Gesture does not perform this system modification, and it is
outside the project's scope. Do not lock navigation until a recovery path is
ready.

Before testing:

- Export the rules and settings as a JSON backup.
- Keep ADB/USB debugging or the device's official recovery method available;
  test computer-and-cable access beforehand when possible.
- Keep system navigation enabled while testing a single gesture rule first.
- Confirm how to stop Akış Gesture, disable its accessibility service, and
  restore system navigation for the specific device.

If Akış Gesture stops, restore system navigation or disable the accessibility
service from Android Settings first. Then restart the app or restore the JSON
backup. Recovery commands vary between vendors and third-party root tools. The
user must accept the risk of being unable to recover the device before making
these system changes.

## Project and release model

Akış Gesture is developed as an independent open-source project rather than a
single large pull request to OpenSwipe. Its package identity, interface,
gesture engine, natural feedback modules, and optional root actions have
diverged substantially from the upstream base.

- This repository is the project `origin`; OpenSwipe remains the read-only
  historical `upstream`.
- Small and generally useful fixes may be proposed to OpenSwipe separately.
- OpenSwipe attribution is preserved, and new Akış Gesture contributions are
  released under the same MIT license.
- APK files are not committed to the source tree. Verified, signed packages are
  published through GitHub Releases with a version tag and SHA-256 checksum.

Source code is published at
[omeryol/AkisGesture](https://github.com/omeryol/AkisGesture).

## Support, contributions, and limitations

Akış Gesture is an independent hobby and community project. Update frequency,
device compatibility, fix times, and individual support are not guaranteed.
Reproducible bug reports, improvements, and contributions are welcome.

Core edge gestures use Android Accessibility Service and do not require root.
Root is optional and used only for clearly marked advanced or experimental
actions; behavior can vary by device, ROM, APatch/Magisk version, and vendor
restrictions.

Akış Gesture does not hide the system navigation bar. System modifications and
third-party root solutions needed for full-screen setups are outside the
project scope.

## Goals

- Low-latency detection on the left, right, and bottom edges
- Independent actions for quick swipe, swipe-and-hold, and L-shaped swipe
- Back, Home, Recents, app launching, media, and other user actions
- Per-app profiles and orientation-aware behavior
- A clear English and Turkish interface
- Safe recovery when HyperOS stops the service
- An isolated helper layer for optional root/APatch actions

The root force-stop action targets only the foreground personal-profile app and
protects critical system packages.

## Behavior in 1.1.54

- Gesture states progress from primary to hold and then up/down L-swipe. Pulling
  back reverses this sequence through the secondary action, primary action, and
  cancellation.
- L-swipe feedback stays anchored at the bend point; color and action icon grow
  with progress, and the first completed L direction is locked for that touch.
- Visual feedback includes 15 independent natural animation modules with
  separate quick, hold, and L colors.
- App-adaptive color blends the selected quick-swipe color with the dominant app
  icon color instead of replacing it.
- Haptic feedback can be disabled. Intensity is saved once when the slider is
  released and provides a preview pulse.
- Previous/next app actions share one reversible history session. Root force-stop
  verifies the target process and removes only that app's Recents cards.

## Architecture principles

1. The gesture engine has one source of truth.
2. Root is a fallback helper when normal Android APIs cannot provide an action.
3. The app is not converted into a system application.
4. Work profiles such as Island are never targeted automatically.
5. The app avoids continuous process killing, foreground launches, and frequent polling.
6. Every behavior change is accompanied by tests and a CHANGELOG entry.

## Current status

Left, right, and bottom gestures are working on a HyperOS/Android 15 device.
Quick swipe, swipe-and-hold, and L-swipe can run independent actions in the same
area. The phone-shaped rule map supports moving and resizing areas, grouped
action assignment, and live gesture rehearsal. Pause conditions cover the lock
screen, keyboard, landscape, full screen, permission screens, and selected apps.

### How L-swipe works

An L gesture starts by pulling inward from an edge and then bending the finger
up or down. Detection has two phases:

- **Phase 1 — Inward:** once `maxInwardPx ≥ threshold`, `inwardArmed` is locked
  and the bend origin (`bendStartY`) is recorded.
- **Phase 2 — Turn:** `turnDy = event.rawY − bendStartY`
  - `turnDy ≤ −35 px` → `SWIPE_UP_L`
  - `turnDy ≥ +35 px` → `SWIPE_DOWN_L`

Rule matching uses the initial edge contact coordinate rather than the finger's
final position. Hold is ready after 280 ms by default and fires on release. Any
gesture can be cancelled by moving the finger back toward the edge.

## Source layout

```text
app/src/main/java/io/github/omeryol/akisgesture/
├── action/       Modular navigation, system, media, hardware, and app actions
├── backup/       JSON settings and rule backup/restore
├── feedback/     Natural animation renderers, icons, haptics, and sound
├── gesture/      Gesture state machine, thresholds, detectors, and pause policy
├── model/        Actions, rules, triggers, and gesture types
├── navigation/   Internal navigation bridge
├── overlay/      Accessibility overlay windows and edge sensors
├── receiver/     External START, STOP, TOGGLE, and action broadcasts
├── root/         Optional APatch/Magisk command helper
├── rule/         Rule graph, profiles, presets, serialization, and validation
├── service/      Accessibility, keep-alive, boot, tile, and MacroDroid services
└── ui/           Jetpack Compose screens, components, theme, and view models
```

## Build

Requirements:

- JDK 21
- Android SDK 35

Windows:

```powershell
.\gradlew.bat assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

Install only for the personal profile:

```powershell
adb install --user 0 -r app\build\outputs\apk\debug\app-debug.apk
```

A general `adb install -r` can also register the app in an active Island work
profile and is therefore not recommended for this project.

## Automation intents

Use these package-scoped broadcasts to start, stop, or toggle Akış Gesture:

```text
io.github.omeryol.akisgesture.action.START
io.github.omeryol.akisgesture.action.STOP
io.github.omeryol.akisgesture.action.TOGGLE
```

They do not modify other accessibility services. MacroDroid can also use the
Akış Gesture plugin entries or its three exported activity targets. Rules and
settings can be backed up to and restored from one JSON file in Settings.

## Roadmap

- [x] Independent left, right, and bottom gesture areas
- [x] Quick, hold, and two-direction L-swipe state machine
- [x] Configurable rule engine and built-in presets
- [x] Per-edge sensitivity, thresholds, position, and size
- [x] Natural visual feedback, haptics, sound, and cancellation
- [x] Interactive phone map with live rehearsal
- [x] HyperOS service-health recovery
- [x] App launching, app switching, and protected root actions
- [x] Complete English and Turkish localization
- [ ] Extended real-device latency and false-trigger measurements
- [ ] Additional per-app profile workflows

## License

OpenSwipe-derived portions and new Akış Gesture contributions are licensed
under the MIT License. See `LICENSE` and `UPSTREAM.md` for attribution details.
