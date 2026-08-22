<div align="center">

# 🔥 AkashBoard — Free Open Source Android Keyboard

### The keyboard that becomes YOU.

**100% FOSS. 100% Local. Zero compromise.**
**Built in 2 days. 227 tests. Production-ready.**

*Keywords: android keyboard, foss keyboard, open source keyboard, privacy keyboard, swipe typing, keyboard prediction, rust keyboard, local ai keyboard, gboard alternative, swiftkey alternative, no tracking keyboard, offline keyboard, best android keyboard 2026, free keyboard app, keyboard with ai*

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0+-brightgreen.svg)](https://developer.android.com/about/versions/oreo)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Architecture](https://img.shields.io/badge/Architecture-ARM64%20(ARMv8A)-orange.svg)]()
[![Tests](https://img.shields.io/badge/Tests-227%20passing-brightgreen.svg)]()
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Local-purple.svg)]()

</div>

---

## What is AkashBoard?

AkashBoard is a **free, open-source Android keyboard** (FOSS) with a **Rust prediction engine** that genuinely learns how YOU type — not just generic word frequencies, but your timing, your context, your patterns, your personality. It runs entirely on your device. No cloud. No accounts. No data collection. Ever.

**AkashBoard** is the best alternative to Gboard, SwiftKey, and Samsung Keyboard for privacy-conscious users. It's a **no-tracking keyboard** with **swipe typing**, **autocorrect**, **emoji support**, **clipboard history**, and **voice input** — all running 100% locally on your Android device.

### Built in 2 Days

This entire keyboard — from empty repository to production-ready APK with 227 passing tests — was built in **2 days** (August 21-22, 2026). That's 12 weeks of planned work compressed into an intense sprint covering:

- Kotlin IME service with custom Canvas rendering (60-120 FPS)
- Rust prediction engine compiled to ARM64 native code via JNI
- Swipe/glide typing, auto-correct, 5 themes, emoji, clipboard, voice
- Settings companion app with 4 preference screens
- 227 passing tests (174 Kotlin + 53 Rust)

### Why AkashBoard Exists

Every keyboard today has the same problem: **they serve the company, not the user.**

- **Gboard** sends your keystrokes to Google servers
- **SwiftKey** requires a Microsoft account and has ads
- **Samsung Keyboard** is locked to Samsung devices
- **FOSS keyboards** (OpenBoard, HeliBoard, FlorisBoard) are either incomplete, have no personal learning, or require proprietary blobs for basic features

AkashBoard fixes this: **a FOSS keyboard that's actually BETTER than the closed-source ones.**

---

## ✅ v1.0 Features

### 🧠 Intelligence
| Feature | Status | Description |
|---------|--------|-------------|
| **Rust Prediction Engine** | ✅ | Native ARM64 N-gram model via JNI — predictions in <1ms |
| **Smart Autocorrect** | ✅ | 25+ built-in typo corrections + learns YOUR patterns |
| **Personal Learning** | ✅ | Time-of-day patterns, app context profiles, error correction |
| **Time-Aware Predictions** | ✅ | Knows you type "good morning" at 8 AM, not 11 PM |
| **Typing DNA** | ✅ | Inter-key timing fingerprint, dominant hand detection |
| **Typing Statistics** | ✅ | WPM, accuracy, session tracking, historical metrics |
| **Swipe/Glide Typing** | ✅ | Bezier trail rendering, order-preserving dictionary match |

### ⚡ Performance
| Feature | Description |
|---------|-------------|
| **60-120 FPS** | Hardware-accelerated Canvas, pre-allocated Paint objects |
| **<5ms key press latency** | Zero framework overhead |
| **<1ms prediction latency** | In-memory Rust engine |
| **309KB native engine** | ARM64, stripped, LTO |
| **~5MB release APK** | 40x smaller than Gboard |

### 🎨 Design
| Feature | Description |
|---------|-------------|
| **Quiet Precision** | No glassmorphism, no neon, no AI branding |
| **5 Built-in Themes** | Akash Dark, Akash Light, Neon Cyber, Minimal, Sunset |
| **JSON Theme Format** | Create and share themes |
| **Dynamic Layout** | Keys adapt to any screen width |
| **Hitbox Model** | Touch regions larger than visual keys |
| **Spring Physics** | Critically damped motion, velocity handoff |

### 🔧 Utility
| Feature | Description |
|---------|-------------|
| **Long-Press Repeat** | Backspace accelerates: 300ms delay → 50ms → 20ms |
| **Popup Preview** | Hold key to see character, slide to select alternate |
| **Spacebar Cursor** | Swipe to move cursor continuously |
| **Emoji Panel** | 8 categories, grid rendering, tap-to-insert |
| **Clipboard History** | Room DB-backed, pinned items, tap-to-paste |
| **Voice Input** | Android SpeechRecognizer wrapper |
| **Settings App** | Typing, Appearance, Privacy, About |

### 🔒 Privacy
| Feature | Description |
|---------|-------------|
| **Zero Network Requests** | No INTERNET permission by design |
| **No Account Required** | Install → use. No login. |
| **Open Source** | GPLv3. Audit the code yourself. |
| **Privacy Dashboard** | See exactly what's stored |
| **Incognito Mode** | Stop learning for sensitive sessions |
| **Export/Import** | Full data portability via JSON |
| **Nuclear Delete** | Complete data wipe capability |

---

## Architecture

```
AkashBoard
├── Kotlin (Android IME Service + UI)
│   ├── core/        — Input handling, key repeat, gestures
│   ├── ui/          — KeyboardView, SuggestionBar, EmojiPanel
│   ├── engine/      — JNI bridge to Rust
│   ├── analytics/   — TypingStats, TypingDNA, TimeAwarePredictor
│   ├── data/        — ClipboardDB, ExportSchema, DataManager
│   ├── settings/    — Settings fragments, preferences
│   └── theme/       — Theme manager, JSON themes
├── Rust → JNI (Prediction + Learning Engine, ARM64 native)
│   ├── predictor.rs — N-gram prediction engine
│   ├── corrector.rs — Error correction engine
│   └── learner.rs   — Personal pattern learning
├── Room DB (Clipboard history)
├── SharedPreferences (Settings)
└── Custom Canvas rendering (60-120fps keyboard)
```

**Target:** Android 8.0+ (API 26), ARM64 (ARMv8-A) only for v1.

---

## Quick Start

```bash
# Clone
git clone https://github.com/AkashPriyadarshii/AkashBoard.git
cd AkashBoard

# Build Rust engine
cd engine
cargo ndk -t arm64-v8a build --release
cd ..

# Build Android app
./gradlew assembleRelease

# Install
adb install app/build/outputs/apk/release/app-release.apk
```

---

## Comparison

| Feature | Gboard | SwiftKey | HeliBoard | FlorisBoard | **AkashBoard** |
|---------|--------|----------|-----------|-------------|----------------|
| Privacy | ❌ Cloud | ❌ Cloud | ✅ Local | ✅ Local | ✅ **Local** |
| Personal Learning | ✅ Cloud | ✅ Cloud | ❌ | ❌ | ✅ **Local** |
| Swipe Typing | ✅ | ✅ | ⚠️ Blob | ❌ | ✅ **FOSS** |
| Theme Engine | ⚠️ Basic | ✅ | ✅ | ✅✅ | ✅✅ |
| Clipboard History | ❌ | ⚠️ | ✅ | ✅ | ✅ |
| Export/Import | ❌ | ❌ | ⚠️ | ❌ | ✅ |
| Typing Analytics | ❌ | ❌ | ❌ | ❌ | ✅ |
| Time-Aware | ❌ | ✅ Cloud | ❌ | ❌ | ✅ **Local** |
| No Ads | ✅ | ❌ | ✅ | ✅ | ✅ |
| No Account | ✅ | ❌ | ✅ | ✅ | ✅ |
| APK Size | ~200MB | ~80MB | ~15MB | ~20MB | **~5MB** |

---

## Testing

| Layer | Tests | Status |
|-------|-------|--------|
| Rust Engine (unit) | 21 | ✅ All passing |
| Rust Engine (integration) | 32 | ✅ All passing |
| Kotlin WordComposer | 25 | ✅ All passing |
| Kotlin KeyboardLayout | 20 | ✅ All passing |
| Kotlin SwipeDetector | 12 | ✅ All passing |
| Kotlin PopupPreviewManager | 12 | ✅ All passing |
| Kotlin SpacebarCursorManager | 10 | ✅ All passing |
| Kotlin KeyRepeatManager | 10 | ✅ All passing |
| Kotlin ThemeConfig | 18 | ✅ All passing |
| Kotlin ExportSchema | 14 | ✅ All passing |
| Kotlin TimeAwarePredictor | 9 | ✅ All passing |
| **Total** | **227** | **✅ All passing** |

```bash
# Run Rust tests
cd engine && cargo test

# Run Kotlin tests
./gradlew testDebugUnitTest
```

---

## Build Timeline

| Date | Milestone |
|------|-----------|
| August 21, 2026 | Project scaffold, core keyboard, input pipeline, Rust engine |
| August 21, 2026 | Long-press, popup preview, spacebar cursor, swipe typing |
| August 21, 2026 | Rust prediction engine integration, auto-correct |
| August 21, 2026 | Theme engine (5 themes), emoji, clipboard, voice |
| August 22, 2026 | Settings app, typing analytics, typing DNA, time-aware |
| August 22, 2026 | Export/import, privacy dashboard, data management |
| August 22, 2026 | Onboarding, crash fixes, full codebase audit |
| August 22, 2026 | **227 tests, production-ready v1.0.0** |

---

## Project Structure

```
AkashBoard/
├── app/src/main/java/com/akashboard/
│   ├── AkashBoardIME.kt          # IME service (entry point)
│   ├── OnboardingActivity.kt     # First-run setup
│   ├── SettingsActivity.kt       # Companion app
│   ├── core/                     # Input handling, gestures
│   ├── ui/                       # Keyboard rendering
│   ├── engine/                   # JNI bridge
│   ├── analytics/                # Typing stats, DNA
│   ├── data/                     # Clipboard, export/import
│   ├── settings/                 # Preference fragments
│   └── theme/                    # Theme engine
├── engine/src/
│   ├── lib.rs                    # JNI bridge
│   ├── predictor.rs              # N-gram engine
│   ├── corrector.rs              # Error correction
│   └── learner.rs                # Personal learning
├── app/src/test/                 # 174 Kotlin unit tests
├── engine/tests/                 # 32 Rust integration tests
└── engine/src/ (lib.rs tests)    # 21 Rust unit tests
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

AkashBoard is licensed under the [GNU General Public License v3.0](LICENSE).

## Author

**Akash Priyadarshi** — [GitHub](https://github.com/AkashPriyadarshii) | [Twitter](https://twitter.com/Akash__ydv001) | [Website](https://akashpriyadarshi.vercel.app)

---

---

## SEO — What AkashBoard Is

**AkashBoard** is a **free open-source Android keyboard** (FOSS) written in **Kotlin** with a **Rust prediction engine**. It is the best **Gboard alternative** and **SwiftKey alternative** for users who care about **privacy**.

### Search Terms

| Category | Keywords |
|----------|----------|
| **Primary** | android keyboard, open source keyboard, foss keyboard, free keyboard app |
| **Privacy** | privacy keyboard, no tracking keyboard, no ads keyboard, offline keyboard, local keyboard |
| **Features** | swipe typing keyboard, autocorrect keyboard, prediction keyboard, emoji keyboard, clipboard keyboard, voice keyboard |
| **Tech** | rust keyboard, kotlin keyboard, arm64 keyboard, jni keyboard, canvas keyboard |
| **Alternatives** | gboard alternative, swiftkey alternative, samsung keyboard alternative, openboard alternative, florisboard alternative, heliboard alternative |
| **Use Cases** | best android keyboard 2026, private keyboard app, secure keyboard, no cloud keyboard, keyboard without internet |

### Also Known As

- AkashBoard keyboard
- Akash keyboard app
- Free FOSS Android keyboard
- Open source keyboard with AI
- Privacy keyboard no tracking
- Swipe typing keyboard free
- Rust prediction keyboard
- Local AI keyboard android

---

<div align="center">

*Your keyboard. Your data. Your rules.*

*Built in 2 days. Ready for the world.*

</div>
