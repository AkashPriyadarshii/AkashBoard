# Changelog

All notable changes to AkashBoard will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.0.1] - 2026-08-23

### ?? Hotfixes & IME Stabilization
- **Compose Layout Bounds**: Fixed 0-height invisible keyboard bug by providing explicit FrameLayout.LayoutParams to the Compose abstract view inside the IME.
- **Onboarding UX**: Overhauled the Onboarding Step 3 to include an embedded Compose OutlinedTextField so users can test the keyboard directly inside the onboarding flow.
- **Event Wiring**: Fixed several InputHandler bypass bugs where tapped suggestions and swipe inputs were dumping raw text instead of interacting with the WordComposer lifecycle.
- **Punctuation Interception**: Fixed a bug where autocorrect failed to trigger before commas and periods.
- **Manifest Integrity**: Fixed a corrupted XML newline that could silently break IME registration.

## [1.0.0] - 2026-08-22

### ?? v1.0.0 — First Production Release

**Built in 2 days** (August 21-22, 2026). From empty repository to production-ready keyboard with 227 passing tests.

---

### UI & UX (Jetpack Compose V2)
- Rebuilt entirely in Jetpack Compose (removed legacy Canvas/XML views)
- Dynamic Material You / Monet system theming (Android 12+)
- High-performance `QwertyGrid` with fluid `KeysGlobalState` coordinate tracking
- Smooth swipe trails using `Canvas` overlay with quadratic Bezier curves
- Responsive floating suggestion bar
- Offline Voice Typing button integrated into main layout

### Input Pipeline
- Robust `InputHandler` enforcing predictable state transitions
- `WordComposer` for current word tracking with accurate shift state (`NONE` -> `ONE` -> `LOCKED`)
- HapticFeedback with 5 vibration patterns (keyPress, modifier, error, selection, modeSwitch)
- Password field detection

### Prediction Engine (Rust ? JNI)
- Embedded `fasttext = "0.8.0"` dependency for semantic spatial embeddings
- Fast `TrieNode` DFS implementation replacing O(N) linear unigram loop for Swipe gesture decoding
- N-gram prediction (unigram, bigram, trigram)
- Edit distance auto-correct (Levenshtein)
- 25+ built-in typo corrections
- Personal pattern learning (time-of-day, app context)
- Time-aware predictions (hour/day/app-based)
- JNI bridge with 14 functions
- 309KB ARM64 native library

### Settings App & Modules
- SettingsActivity with Compose Material 3 UI
- EmojiPanel (8 categories, grid rendering)
- ClipboardPanel (Room DB-backed, pinned items)
- TypingStats (WPM, accuracy, sessions, historical metrics)
- TypingDNA (typing fingerprint, dominant hand detection)
- ExportSchema (versioned JSON format)
- PrivacyDashboard (transparent data view with Nuclear delete option)
- Zero network requests by design

### Architectural Bug Fixes 
- Fixed Ghost Keyboard bug where Compose failed to render QwertyGrid
- Fixed Float Array Mismatch where Rust read 3 floats per key while Kotlin sent 4
- Fixed Engine Bypass bug where QwertyGrid circumvented autocorrect pipelines
- Fixed Dead Suggestion Pipeline where lambda dropped predictions
- Fixed IME View Hierarchy Hijack ensuring clean OEM lifecycle integration
- Fixed WordComposer shift-state synchrony in Jetpack Compose

---

### Technical Details
- **Target:** Android 8.0+ (API 26), ARM64 only
- **Architecture:** Kotlin (Compose IME) + Rust (ARM64 engine) via JNI
- **Storage:** Room DB + SharedPreferences, <2MB
- **Engine Size:** 309KB (ARM64, stripped, LTO)
- **License:** GPLv3
- **Build Time:** 2 days (August 21-22, 2026)

