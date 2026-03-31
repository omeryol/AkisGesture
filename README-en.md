# <img src="docs/icon.svg" width="48" align="center"/> OpenSwipe

**Open-source Android gesture navigation** — Custom gesture navigation powered by AccessibilityService. No root required.

No ads · No tracking · Fully open-source · Free forever

**English** | [中文](README.md)

---

### Features

| Category | Feature | Status |
|----------|---------|--------|
| **Rule Engine** | Bipartite-graph gesture rule engine (Trigger → Action configurable mapping) | ✅ |
| **Rule Control** | Enable/disable each rule independently | ✅ |
| **Trigger Mode** | Per-rule trigger mode (Tap / Swipe with click pass-through) | ✅ |
| **Persistence** | Rule persistence via DataStore | ✅ |
| **Presets** | Built-in presets (iOS-style / Android Classic / Media Control) | ✅ |
| **Bottom Segments** | Bottom edge split into 3 zones (Left 1/3, Center 1/3, Right 1/3) with independent actions | ✅ |
| **System Actions** | 18 system actions available | ✅ |
| **Edge Config** | Adjustable edge trigger width & height | ✅ |
| **Rule Editor** | Rule detail editing page (Save + Delete) | ✅ |
| **Modern UI** | Jetpack Compose Material3 | ✅ |
| **Branding** | Custom vector icon | ✅ |

### Privacy Promise

- ❌ **Zero ads** — No AdMob, AppLovin, or Facebook Ads
- ❌ **Zero tracking** — No Firebase Analytics or Crashlytics
- ❌ **Zero network** — The app never connects to the internet
- ✅ **Fully open-source** — Every line of code is auditable

### Installation

1. Download APK from [Releases](https://github.com/ARCJ137442/OpenSwipe/releases)
2. Install on your device
3. Open the app → Follow the permission guide to enable AccessibilityService
4. Swipe from screen edges and enjoy!

### Architecture

```
com.openswipe/
├── service/     → AccessibilityService + keep-alive + auto-start on boot
├── overlay/     → TYPE_ACCESSIBILITY_OVERLAY window management
├── gesture/     → State-machine gesture detection engine
├── rule/        → Bipartite-graph rule engine (Trigger ↔ Action mapping)
├── action/      → Sealed class action dispatch (18 system actions)
├── feedback/    → Bézier curve stretch + haptic feedback
└── ui/          → Jetpack Compose Material3
```

**Tech Stack**: Kotlin 2.1 · Jetpack Compose · Material3 · DataStore · Coroutines

**Core Principle**:
```kotlin
// System-level actions via AccessibilityService, no Root needed
performGlobalAction(GLOBAL_ACTION_BACK)     // Back
performGlobalAction(GLOBAL_ACTION_HOME)     // Home
performGlobalAction(GLOBAL_ACTION_RECENTS)  // Recents
```

### Build

```bash
git clone https://github.com/ARCJ137442/OpenSwipe.git
cd OpenSwipe
./gradlew assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```

Requirements: Android Studio 2024.2+ · JDK 21 · Android SDK 35

### Roadmap

- [x] MVP: Left / Right / Bottom gesture navigation
- [x] Independent toggle + real-time effect
- [x] Adjustable bottom height + Tap/Swipe mode
- [x] Bipartite-graph rule engine with configurable mapping
- [x] Rule persistence + per-rule enable/disable
- [x] Preset schemes (iOS-style / Android Classic / Media Control)
- [x] Bottom 3-segment zones with independent actions
- [x] 18 system actions
- [x] Rule detail editor (Save + Delete)
- [x] Brand vector icon
- [ ] Bézier curve visual feedback ([#3](https://github.com/ARCJ137442/OpenSwipe/issues/3))
- [ ] Per-app configuration
- [ ] F-Droid listing

### License

[MIT](LICENSE)

---

> Last updated: 2026-03-31

> ✨ *DIY your gesture navigation — open source saves the world* 🚀
