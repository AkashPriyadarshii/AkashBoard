<div align="center">

# 🔥 AkashBoard

### The keyboard that becomes YOU.

**100% FOSS. 100% Local. Zero compromise.**

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0+-brightgreen.svg)](https://developer.android.com/about/versions/oreo)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Architecture](https://img.shields.io/badge/Architecture-ARM64%20(ARMv8A)-orange.svg)]()
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Local-purple.svg)]()

</div>

---

## What is AkashBoard?

AkashBoard is an **open-source Android keyboard** that genuinely learns how YOU type — not just generic word frequencies, but your timing, your context, your patterns, your personality. It runs entirely on your device. No cloud. No accounts. No data collection. Ever.

### Why AkashBoard Exists

Every keyboard today has the same problem: **they serve the company, not the user.**

- **Gboard** sends your keystrokes to Google servers
- **SwiftKey** requires a Microsoft account and has ads
- **Samsung Keyboard** is locked to Samsung devices
- **FOSS keyboards** (OpenBoard, HeliBoard, FlorisBoard) are either incomplete, have no personal learning, or require proprietary blobs for basic features

AkashBoard fixes this: **a FOSS keyboard that's actually BETTER than the closed-source ones.**

---

## ✅ v1.0 Features (Weeks 1-12 Complete)

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
| **7.5MB debug APK** | 30x smaller than Gboard |

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

## Project Status

| Week | Milestone | Status |
|------|-----------|--------|
| 1 | Project scaffold + Rust engine | ✅ |
| 2 | Core keyboard rendering + input | ✅ |
| 3 | Long-press repeat + spacebar cursor | ✅ |
| 4 | Rust prediction engine integration | ✅ |
| 5 | Swipe/glide typing | ✅ |
| 6 | Auto-correct + suggestion polish | ✅ |
| 7 | Theme engine (5 themes) | ✅ |
| 8 | Emoji + clipboard + voice | ✅ |
| 9 | Settings companion app | ✅ |
| 10 | Typing stats + Typing DNA | ✅ |
| 11 | Export/import + privacy dashboard | ✅ |
| 12 | Polish + testing + launch | ✅ |

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
| Kotlin TimeAwarePredictor | 6 | ✅ All passing |
| **Total** | **170+** | **✅ All passing** |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

AkashBoard is licensed under the [GNU General Public License v3.0](LICENSE).

## Author

**Akash Priyadarshi** — [GitHub](https://github.com/AkashPriyadarshii) | [Twitter](https://twitter.com/Akash__ydv001) | [Website](https://akashpriyadarshi.vercel.app)

---

<div align="center">

*Your keyboard. Your data. Your rules.*

</div>
