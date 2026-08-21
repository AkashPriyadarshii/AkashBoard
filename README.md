<div align="center">

# 🔥 AkashBoard

### The keyboard that becomes YOU.

**100% FOSS. 100% Local. Zero compromise.**

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0+-brightgreen.svg)](https://developer.android.com/about/versions/oreo)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Architecture](https://img.shields.io/badge/Architecture-ARM64%20(ARMv8A)-orange.svg)]()
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Local-purple.svg)]()

<img src="docs/banner.png" width="800" alt="AkashBoard Banner"/>

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

## Features

### 🧠 Intelligence
| Feature | Description |
|---------|-------------|
| **Personal Typing DNA** | Builds a unique typing fingerprint that evolves daily |
| **Smart Autocorrect** | Learns YOUR error patterns, not generic ones |
| **Context-Aware Suggestions** | Formal for email, casual for chat, technical for code |
| **Time-Aware Predictions** | Knows you type "good morning" at 8 AM, not 11 PM |
| **Mood-Aware Mode** | Adjusts suggestion tone based on what you're typing |
| **Emoji Genius** | Predicts emoji combos, not just single emoji |

### ⚡ Performance
| Feature | Description |
|---------|-------------|
| **Rust Prediction Engine** | Native ARM64 code via JNI — predictions in <0.01ms |
| **Zero-Lag Input** | No framework overhead, pure Canvas rendering |
| **<5MB APK** | 600x smaller than Gboard |

### 🎨 Design
| Feature | Description |
|---------|-------------|
| **Glassmorphism UI** | Frosted glass keys, neon accents, 60fps animations |
| **5 Built-in Themes** | Dark, Light, Neon Cyber, Minimal, Sunset |
| **Custom Theme Editor** | Create and share themes via JSON |
| **Adaptive Key Sizing** | Keys grow/shrink based on your usage |
| **Typing Sound Packs** | Mechanical, retro, sci-fi keypress sounds |

### 🔧 Utility
| Feature | Description |
|---------|-------------|
| **Swipe/Glide Typing** | Works without proprietary blobs (ported from AnySoftKeyboard) |
| **Clipboard Manager** | Auto-save, pin, search clipboard history |
| **Text Shortcuts** | `brw` → "be right with you" |
| **Voice-to-Text** | Offline via Android SpeechRecognizer |
| **Multi-Language** | EN, HI, ES, FR with easy switching |
| **Per-App Tint** | Keyboard color changes based on current app |

### 🔒 Privacy
| Feature | Description |
|---------|-------------|
| **Zero Network Requests** | No analytics, no telemetry, no phone home |
| **No Account Required** | Install → use. No login. |
| **Open Source** | GPLv3. Audit the code yourself. |
| **Privacy Dashboard** | See exactly what's stored, one-tap nuke |
| **Incognito Mode** | Stop learning for sensitive sessions |
| **Export/Import** | Full data portability via JSON |

---

## Architecture

```
AkashBoard
├── Kotlin (Android IME Service + UI)
├── Rust → JNI (Prediction + Learning Engine, ARM64 native)
├── Room DB (Clipboard history)
├── SharedPreferences (Settings)
└── Custom Canvas rendering (60fps keyboard)
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
| Adaptive Keys | ❌ | ❌ | ❌ | ❌ | ✅ |
| Mood-Aware | ❌ | ⚠️ Cloud | ❌ | ❌ | ✅ **Local** |
| No Ads | ✅ | ❌ | ✅ | ✅ | ✅ |
| No Account | ✅ | ❌ | ✅ | ✅ | ✅ |
| APK Size | ~200MB | ~80MB | ~15MB | ~20MB | **~5MB** |

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
