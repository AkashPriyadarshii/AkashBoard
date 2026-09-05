<div align="center">

# 🔥 AkashBoard — Free Open Source Android Keyboard

### The keyboard that becomes YOU.

**100% FOSS. Voice AI. Zero compromise.**
**Built on FlorisBoard. Rust prediction. Whisper AI dictation.**

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-7C3AED.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Platform](https://img.shields.io/badge/Platform-Android%206%2B-7C3AED.svg)](https://developer.android.com/about/versions/marshmallow)
[![Privacy](https://img.shields.io/badge/Privacy-On--device%20mode-7C3AED.svg)]()
[![GitHub](https://img.shields.io/github/last-commit/AkashPriyadarshii/AkashBoard?color=7C3AED&labelColor=1b1e2b)](https://github.com/AkashPriyadarshii/AkashBoard)

</div>

---

## What is AkashBoard?

AkashBoard is a **free, open-source Android keyboard** built on top of [FlorisBoard](https://github.com/florisboard/florisboard) with a full **Whisper AI voice dictation layer** on top. Speak instead of type — in any app. Or glide-type with word suggestions and autocorrect.

No cloud required. No account. No data collection. Ever.

**AkashBoard** is the best alternative to Gboard, SwiftKey, and Samsung Keyboard for privacy-conscious users — with swipe typing, autocorrect, emoji, GIFs, clipboard history, voice input, and on-device AI transcription — all running locally on your Android device.

---

## 🎤 Voice Dictation (Whisper AI)

Tap the mic, talk naturally, watch clean punctuated text appear in **any** app in real time.

| Provider | Mode |
|---|---|
| OpenAI Whisper | Cloud + on-device |
| Google Gemini, Groq, Deepgram | Cloud API |
| AssemblyAI, ElevenLabs, Soniox | Cloud API |
| Mistral, OpenRouter, Anthropic | AI reword |
| Ollama | Local reword |
| **On-device (offline)** | Whisper, Parakeet, Canary, GigaAM, SenseVoice |

No server needed — use your own API key or download a model and transcribe fully offline. No audio leaves the device in offline mode.

---

## ✅ Features

### 🧠 Intelligence
| Feature | Description |
|---|---|
| **Whisper AI Dictation** | Real-time voice-to-text in any app |
| **AI Rewording** | Translate, summarize, formalize with one tap |
| **Glide / Swipe Typing** | FlorisBoard gesture engine — per-language dict |
| **Smart Autocorrect** | Word suggestions + correction bar |
| **On-device transcription** | Fully offline, no API key needed |

### 🎨 Design
| Feature | Description |
|---|---|
| **Electric Violet UI** | `#7C3AED` accent — cyberpunk, brutalist |
| **5 Built-in Themes** | FlorisBoard theme engine |
| **Dynamic Layout** | Keys adapt to screen width |

### 🔒 Privacy
| Feature | Description |
|---|---|
| **On-device mode** | Zero network — audio never leaves phone |
| **No Account Required** | Install → use. No login. |
| **Open Source** | Apache 2.0. Audit the code yourself. |
| **Incognito Mode** | Stop learning for sensitive sessions |

---

## Architecture

```
AkashBoard
├── FlorisBoard core (Kotlin + Jetpack Compose)
│   ├── Keyboard layouts, theming, gesture handling
│   ├── Clipboard, emoji, GIF panel (KLIPY)
│   └── IME plumbing
├── lib/dictate-core — Whisper AI layer
│   ├── Voice recording + streaming
│   ├── Provider integrations (OpenAI, Gemini, Groq...)
│   ├── On-device ONNX inference (sherpa-onnx)
│   └── AI rewording / live prompt
└── app — Android InputMethodService & Settings UI
```

---

## Quick Start

```bash
git clone https://github.com/AkashPriyadarshii/AkashBoard.git
cd AkashBoard
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Comparison

| Feature | Gboard | SwiftKey | FlorisBoard | **AkashBoard** |
|---|---|---|---|---|
| Privacy | ❌ Cloud | ❌ Cloud | ✅ Local | ✅ **Local / Your key** |
| Voice Dictation | ✅ Cloud | ❌ | ❌ | ✅ **Cloud + Offline** |
| AI Reword | ❌ | ❌ | ❌ | ✅ **Any LLM** |
| Swipe Typing | ✅ | ✅ | ⚠️ Blob | ✅ **FOSS** |
| No Account | ✅ | ❌ | ✅ | ✅ |
| No Ads | ✅ | ❌ | ✅ | ✅ |
| Open Source | ❌ | ❌ | ✅ | ✅ |

---

## Using Your Own Server

AkashBoard speaks the plain OpenAI API — any compatible server works:

1. **Settings → AI providers → Add your own server**
2. Set **Base URL** (include trailing `/v1/`), e.g. `http://192.168.1.20:8000/v1/`
3. Set **API key** (leave empty if not required)
4. Pick as active provider for transcription, rewording, or both

`localhost` = the phone, not your PC — use LAN IP.

---

## Built on FlorisBoard

AkashBoard is a fork of [**FlorisBoard**](https://github.com/florisboard/florisboard) by
[Patrick Goldinger](https://github.com/patrickgold) and contributors — Apache 2.0.
See [`LICENSE`](LICENSE) and [`NOTICE`](NOTICE) for full attribution.

Speech recognition powered by [OpenAI Whisper](https://openai.com/index/whisper/).
On-device transcription via [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx).

---

## Contributing

[Open an issue](https://github.com/AkashPriyadarshii/AkashBoard/issues) with bugs, ideas, or feedback.

**Security issue?** Use [GitHub's private advisory form](https://github.com/AkashPriyadarshii/AkashBoard/security/advisories/new). See [`SECURITY.md`](SECURITY.md).

---

## License

Apache License 2.0. Fork of FlorisBoard — Copyright The FlorisBoard Contributors.

---

<div align="center">

**AkashBoard** by [Akash Priyadarshi](https://github.com/AkashPriyadarshii) · Patna, Bihar, India

[GitHub](https://github.com/AkashPriyadarshii) · [Portfolio](https://akashpriyadarshi.vercel.app) · [LinkedIn](https://linkedin.com/in/akash-priyadarshi-1aa51b37a) · [Resume](https://akashpriyadarshii.github.io/Resume/)

[X / Twitter](https://x.com/Akash__ydv001) · [Threads](https://www.threads.com/@free_dev2026) · [Instagram](https://www.instagram.com/akash.priyadarshii/) · [Reddit](https://reddit.com/user/DragonfruitWeak2801)

*Your keyboard. Your data. Your rules.*

</div>
