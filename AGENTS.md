# AGENTS.md — AkashBoard AI Agent Instructions

> This file tells AI coding assistants (Codebuff, Claude, Copilot, Cursor, etc.) how to work on AkashBoard.

---

## Project Identity

**AkashBoard** is a FOSS Android keyboard app (GPLv3) that runs entirely on-device. It uses Kotlin for Android UI/IME and Rust compiled to ARM64 native code for the prediction/learning engine via JNI.

**Author:** Akash Priyadarshi (AkashPriyadarshii)
**License:** GPLv3
**Target:** Android 8.0+ (API 26), ARMv8-A (64-bit ARM) only

---

## Golden Rules

1. **NEVER add network permissions or make HTTP requests.** The keyboard is 100% offline. No analytics, no telemetry, no cloud.
2. **NEVER add proprietary blobs.** All code must be FOSS-compatible under GPLv3.
3. **Performance is sacred.** The prediction engine must return results in <0.01ms. If your code adds latency, it's wrong.
4. **ARM64 only for v1.** Do not add x86, x86_64, or armeabi-v7a targets.
5. **Storage budget: 2MB hard cap.** The keyboard's local data must never exceed 2MB.
6. **No external runtime dependencies.** No React Native, no Flutter, no web views. Pure native Android.
7. **Privacy first.** Every feature must work locally. If it needs a server, it doesn't belong here.

---

## Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| IME Service | Kotlin | Modern, null-safe, Google-recommended for IME |
| UI Rendering | Custom View (Canvas) | 60fps animations, no framework overhead |
| Prediction Engine | Rust → JNI (ARM64) | 50x faster than Java for pattern matching |
| Swipe Engine | Kotlin (ported from AnySoftKeyboard) | Proven algorithm, no proprietary libs |
| Storage | Room DB + SharedPreferences | Room for structured data, SharedPrefs for settings |
| Voice | Android SpeechRecognizer | Built-in, works offline |
| Build | Gradle + cargo-ndk | Standard Android build + Rust cross-compilation |

---

## Project Structure

```
AkashBoard/
├── app/                              # Android application module
│   ├── src/main/
│   │   ├── java/com/akashboard/      # Kotlin source
│   │   │   ├── AkashBoardIME.kt      # IME service (ENTRY POINT)
│   │   │   ├── ui/                   # Keyboard UI components
│   │   │   ├── core/                 # Input handling, swipe, voice
│   │   │   ├── engine/               # JNI bridge to Rust
│   │   │   ├── theme/                # Theme engine
│   │   │   ├── data/                 # Storage, clipboard, export/import
│   │   │   └── analytics/            # Typing stats, DNA
│   │   ├── jniLibs/arm64-v8a/        # Compiled Rust .so files
│   │   ├── res/                      # Android resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── engine/                           # Rust prediction/learning engine
│   ├── src/
│   │   ├── lib.rs                    # JNI entry point
│   │   ├── predictor.rs              # N-gram prediction
│   │   ├── learner.rs                # Personal pattern learning
│   │   ├── corrector.rs              # Auto-correct
│   │   ├── swipe.rs                  # Swipe path matching
│   │   ├── mood.rs                   # Sentiment detection
│   │   └── storage.rs                # Compressed model storage
│   ├── Cargo.toml
│   └── build.rs                      # NDK cross-compilation config
│
├── docs/                             # Documentation
├── python/                           # Prototyping scripts (dev-time only)
├── themes/                           # Built-in theme JSONs
├── build.gradle.kts                  # Root build
├── settings.gradle.kts
├── LICENSE                           # GPLv3
├── README.md
├── AGENTS.md                         # THIS FILE
├── PRD.md                            # Product requirements
├── ARCHITECTURE.md                   # System architecture
├── DESIGN.md                         # UI/UX design system
├── IMPLEMENTATIONPLAN.md             # Build plan
├── API.md                            # Internal API docs
├── CONTRIBUTING.md                   # Contribution guidelines
├── SECURITY.md                       # Security policy
└── CHANGELOG.md                      # Version history
```

---

## Coding Conventions

### Kotlin

- Use **Kotlin idioms**: extension functions, sealed classes, coroutines
- **Null safety**: Never use `!!` operator. Use `?.` or `requireNotNull()`
- **Coroutines**: Use `Dispatchers.Default` for CPU work, `Dispatchers.IO` for storage, `Dispatchers.Main` for UI
- **Naming**: `camelCase` for functions/variables, `PascalCase` for classes, `SCREAMING_SNAKE_CASE` for constants
- **No Java-style getters/setters**. Use Kotlin properties.
- **Import ordering**: `android.*`, `androidx.*`, `com.*`, `java.*`, `kotlin.*` (alphabetical within groups)

### Rust

- Use `rustfmt` defaults
- **No `unwrap()` in production code**. Use `Result` or `expect()` with clear messages
- **No `panic!()` in JNI-called functions**. Return errors to Kotlin.
- **Naming**: `snake_case` for functions/variables, `PascalCase` for types, `SCREAMING_SNAKE_CASE` for constants
- **JNI naming**: Functions called from Kotlin use `Java_com_akashboard_engine_*` naming

### General

- **No magic numbers**. Use named constants.
- **Every public function has a doc comment** (Kotlin: `/** */`, Rust: `///`)
- **No TODO comments in committed code**. Either do it or create an issue.
- **One class per file** (except small helper/data classes)
- **Max function length: 50 lines**. If longer, break it up.

---

## What to Build (Priority Order)

### Phase 1: Core (Must Have)
1. `AkashBoardIME.kt` — Android IME service with basic keyboard
2. `KeyboardView.kt` — QWERTY layout rendering via Canvas
3. `InputHandler.kt` — Key press processing
4. `lib.rs` + `predictor.rs` — Rust prediction engine
5. JNI bridge between Kotlin and Rust

### Phase 2: Smart (Should Have)
6. Swipe typing (port from AnySoftKeyboard's algorithm)
7. Auto-correct via Rust engine
8. Suggestion bar UI
9. Emoji panel
10. Theme engine with 5 built-in themes

### Phase 3: Utility (Nice to Have)
11. Clipboard history
12. Text shortcuts
13. Voice-to-text
14. Multi-language support

### Phase 4: Intelligence (Advanced)
15. Personal learning engine (Rust)
16. Time-aware predictions
17. Context profiles (per-app tone)
18. Typing analytics dashboard
19. Export/Import system
20. Adaptive key sizing

---

## Testing

- **Unit tests** for Rust engine: `cargo test` in `engine/`
- **Unit tests** for Kotlin logic: JUnit in `app/src/test/`
- **Integration tests**: JNI bridge correctness
- **No UI tests for v1** (too fragile, slow to write)
- **Manual testing**: Install APK on real ARM64 device

### Test Commands

```bash
# Rust engine tests
cd engine && cargo test

# Kotlin unit tests
./gradlew test

# Build release APK
./gradlew assembleRelease

# Install on device
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## Security

- **No internet permission** in AndroidManifest.xml
- **No hardcoded secrets** or API keys
- **Encrypt sensitive data** at rest (user dictionary, clipboard)
- **Input validation** on all JNI boundary functions
- **Fuzz testing** for Rust parser functions
- See [SECURITY.md](SECURITY.md) for full policy

---

## Common Pitfalls

1. **Don't use `Log.d()` in release builds**. Use a debug flag.
2. **Don't allocate in the prediction hot path**. Pre-allocate buffers.
3. **Don't block the main thread**. All prediction/learning runs on background threads.
4. **Don't forget to call `System.loadLibrary()` before JNI calls**.
5. **Don't store more than 2MB**. Check storage budget before writes.
6. **Don't add `android.permission.INTERNET`**. Ever. For any reason.
7. **Don't use `WebView`**. It pulls in Chromium and bloats the APK.

---

## File Responsibilities

| File | Responsibility |
|------|---------------|
| `AkashBoardIME.kt` | IME lifecycle, input connection, view management |
| `KeyboardView.kt` | Renders keyboard layout via Canvas, handles touch events |
| `SuggestionBar.kt` | Renders prediction strip, handles suggestion taps |
| `InputHandler.kt` | Processes key events, manages word composition |
| `SwipeDetector.kt` | Gesture recognition, path-to-word matching |
| `VoiceInput.kt` | SpeechRecognizer wrapper |
| `lib.rs` | JNI bridge — receives calls from Kotlin, delegates to Rust modules |
| `predictor.rs` | N-gram model, trie lookup, suggestion ranking |
| `learner.rs` | Personal pattern learning, decay, context profiles |
| `corrector.rs` | Error detection, correction suggestions |
| `mood.rs` | Sentiment analysis on typed text |
| `ThemeManager.kt` | Loads/applies themes, supports Snygg-style stylesheets |
| `ClipboardManager.kt` | Clipboard history with Room DB |
| `DataManager.kt` | Export/Import all data as JSON |
| `TypingStats.kt` | Speed, accuracy, pattern tracking |
| `TypingDNA.kt` | Unique typing fingerprint generation |

---

## When in Doubt

- **Read the PRD.md** for product requirements
- **Read the ARCHITECTURE.md** for system design
- **Read the DESIGN.md** for UI/UX guidelines
- **Read the API.md** for interface contracts
- **Read the IMPLEMENTATIONPLAN.md** for build order

**If you still don't know, ask. Don't guess.**
