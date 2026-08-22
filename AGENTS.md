# AGENTS.md — AkashBoard AI Agent Instructions

> This file tells AI coding assistants (Codebuff, Claude, Copilot, Cursor, etc.) how to work on AkashBoard.

---

## Project Identity

**AkashBoard** is a FOSS Android keyboard app (GPLv3) that runs entirely on-device. It uses Kotlin for Android UI/IME and Rust compiled to ARM64 native code for the prediction/learning engine via JNI.

**Author:** Akash Priyadarshi (AkashPriyadarshii)
**License:** GPLv3
**Target:** Android 8.0+ (API 26), ARMv8-A (64-bit ARM) only
**Status:** v1.0.0 — Production Ready (227 tests passing)

---

## Multi-agent coordination (read this FIRST)

This repo runs multiple AI agents in parallel on separate branches:

| Agent | Branch | Territory |
|-------|--------|-----------|
| Claude engine agent | `claude-rs` | `engine/src/**`, `engine/tests/**`, jniLibs `.so` rebuilds |
| Claude Android agent | `claude-android` | `app/src/main/java/com/akashboard/**` |
| Codebuff | own branch off `main` (name it) | ANYTHING — unrestricted |
| Other agents (Copilot, Cursor, you) | feature branch off `main` | assigned per task |

Rules for every agent:

1. **Read `HANDOFF.md` at repo root before starting** and **append an entry
   before ending your session** (did / in-flight / don't-touch).
2. **Commit to your branch; never commit work directly to `main`.**
3. Territories are reserved: touching another agent's files invites merge
   conflicts — check `HANDOFF.md` first and reconcile instead of fighting.
4. `.so` rebuilds land ONLY on `claude-rs`.
5. **Worktrees, not one working dir.** The `claude-rs` agent works in
   `C:\Users\saves\Desktop\AkashBoard-rs` (`git worktree add ../AkashBoard-rs
   claude-rs`). The `claude-android` agent works in the main dir
   `C:\Users\saves\Desktop\AkashBoard`. NEVER run `git add -A` / checkout
   branches in a directory another agent is actively using — that ate a commit
   once already. Commit only files from your territory.

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
│   │   │   ├── ui/                   # KeyboardView, SuggestionBar, EmojiPanel
│   │   │   ├── engine/               # JNI bridge to Rust
│   │   │   ├── theme/                # Theme engine (5 built-in themes)
│   │   │   ├── data/                 # Storage, clipboard, export/import
│   │   │   ├── analytics/            # Typing stats, DNA, time-aware
│   │   │   └── settings/             # Settings fragments + preferences
│   │   ├── jniLibs/arm64-v8a/        # Compiled Rust .so files
│   │   ├── res/                      # Android resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── engine/                           # Rust prediction/learning engine
│   ├── src/
│   │   ├── lib.rs                    # JNI entry point (14 functions)
│   │   ├── predictor.rs              # N-gram prediction (unigram/bigram/trigram)
│   │   ├── learner.rs                # Personal pattern learning + decay
│   │   └── corrector.rs              # Error correction (25+ built-in typos)
│   ├── tests/
│   │   └── extra_tests.rs            # 32 integration tests
│   └── Cargo.toml
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

## What Was Built (v1.0.0 — All Complete)

### Phase 1: Core ✅
1. `AkashBoardIME.kt` — Android IME service with basic keyboard
2. `KeyboardView.kt` — QWERTY layout rendering via Canvas
3. `InputHandler.kt` — Key press processing
4. `lib.rs` + `predictor.rs` — Rust prediction engine
5. JNI bridge between Kotlin and Rust

### Phase 2: Smart ✅
6. Swipe typing (port from AnySoftKeyboard's algorithm)
7. Auto-correct via Rust engine
8. Suggestion bar UI
9. Emoji panel
10. Theme engine with 5 built-in themes

### Phase 3: Utility ✅
11. Clipboard history (Room DB)
12. Voice-to-text (SpeechRecognizer)
13. Settings companion app

### Phase 4: Intelligence ✅
14. Personal learning engine (Rust)
15. Time-aware predictions
16. Context profiles (per-app)
17. Typing analytics (WPM, accuracy, sessions)
18. Typing DNA (fingerprint, dominant hand)
19. Export/Import system
20. Privacy dashboard

---

## Testing (227 Tests Passing)

- **Rust unit tests**: 21 tests (predictor, corrector, learner, lib)
- **Rust integration tests**: 32 tests (extra_tests.rs)
- **Kotlin unit tests**: 174 tests (9 test files, Robolectric for Android classes)
- **No UI tests for v1** (too fragile, slow to write)
- **Manual testing**: Install APK on real ARM64 device

### Test Commands

```bash
# Rust engine tests (53 total)
cd engine && cargo test

# Kotlin unit tests (174 total)
./gradlew testDebugUnitTest

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
| `OnboardingActivity.kt` | First-run setup (enable keyboard, switch keyboard) |
| `SettingsActivity.kt` | Companion app with fragment navigation |
| `KeyboardView.kt` | Renders keyboard layout via Canvas, handles touch events |
| `SuggestionBar.kt` | Renders prediction strip, handles suggestion taps |
| `InputHandler.kt` | Processes key events, manages word composition, autocorrect |
| `WordComposer.kt` | Current word tracking, shift states, context |
| `SwipeDetector.kt` | Gesture recognition, path-to-word matching |
| `SwipeTrail.kt` | Bezier trail rendering for swipe gestures |
| `KeyRepeatManager.kt` | Long-press repeat (backspace acceleration) |
| `PopupPreviewManager.kt` | Long-press popup preview |
| `SpacebarCursorManager.kt` | Spacebar swipe cursor movement |
| `HapticFeedback.kt` | 5 vibration patterns |
| `VoiceInput.kt` | SpeechRecognizer wrapper |
| `EmojiPanel.kt` | Emoji grid with 8 categories |
| `ClipboardPanel.kt` | Clipboard history UI |
| `lib.rs` | JNI bridge — 14 functions callable from Kotlin |
| `predictor.rs` | N-gram prediction (unigram/bigram/trigram) |
| `learner.rs` | Personal pattern learning, decay, context profiles |
| `corrector.rs` | Error correction (25+ built-in typos, edit distance) |
| `PredictorBridge.kt` | Kotlin JNI wrapper for Rust engine |
| `ThemeManager.kt` | Loads/applies themes, JSON parsing |
| `ThemeConfig.kt` | Theme data model (colors, dimensions, animation) |
| `ClipboardDB.kt` | Room database for clipboard history |
| `ClipboardDao.kt` | Room DAO for clipboard operations |
| `ClipboardItem.kt` | Room entity for clipboard entries |
| `DataManager.kt` | Export/Import all data as JSON |
| `ExportSchema.kt` | JSON schema v1 + validation |
| `PrivacyDashboard.kt` | Transparent data view |
| `KeyboardSettingsProvider.kt` | Typed SharedPreferences access |
| `TypingStats.kt` | Speed, accuracy, session tracking |
| `TypingDNA.kt` | Unique typing fingerprint generation |
| `TimeAwarePredictor.kt` | Time-of-day/app word frequency patterns |

---

## When in Doubt

- **Read the PRD.md** for product requirements
- **Read the ARCHITECTURE.md** for system design
- **Read the DESIGN.md** for UI/UX guidelines
- **Read the API.md** for interface contracts
- **Read the IMPLEMENTATIONPLAN.md** for build order

**If you still don't know, ask. Don't guess.**
