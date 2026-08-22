# Changelog

All notable changes to AkashBoard will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.0.0] - 2026-08-22

### 🚀 v1.0.0 — First Production Release

**Built in 2 days** (August 21-22, 2026). From empty repository to production-ready keyboard with 227 passing tests.

---

### Core Keyboard
- Custom Canvas-rendered keyboard (60-120 FPS)
- QWERTY layout with dynamic sizing
- Symbols layout with full key definitions
- Touch hitbox model (larger than visual keys)
- Key press animation (scale 0.92x)
- Hardware-accelerated rendering
- Dynamic layout calculator (adapts to any screen width)

### Input Pipeline
- InputHandler with full key processing
- WordComposer for current word tracking
- HapticFeedback with 5 vibration patterns (keyPress, modifier, error, selection, modeSwitch)
- Password field detection
- Shift states (None → One → Locked)

### Gestures
- Long-press key repeat (backspace: 300ms → 50ms → 20ms)
- Popup preview for long-press alternate characters
- Spacebar cursor movement via swipe (1:1 ratio)
- Swipe/glide typing with Bezier trail rendering

### Prediction Engine (Rust → JNI)
- N-gram prediction (unigram, bigram, trigram)
- Edit distance auto-correct (Levenshtein)
- 25+ built-in typo corrections
- Personal pattern learning (time-of-day, app context)
- Time-aware predictions (hour/day/app-based)
- JNI bridge with 14 functions
- 309KB ARM64 native library

### UI Components
- SuggestionBar with slide animations (staggered 50ms)
- EmojiPanel (8 categories, grid rendering)
- ClipboardPanel (Room DB-backed, pinned items)
- VoiceInput (Android SpeechRecognizer wrapper)

### Themes
- 5 built-in themes: Akash Dark, Akash Light, Neon Cyber, Minimal, Sunset
- JSON theme format (schema v1)
- ThemeManager with listener pattern
- Live theme switching

### Settings App
- SettingsActivity with fragment navigation
- TypingFragment (autocorrect, predictions, gestures)
- AppearanceFragment (themes, height, one-handed, corner radius, spacing)
- PrivacyFragment (incognito, clipboard, export/import, reset)
- AboutFragment (version, licenses, GitHub)
- KeyboardSettingsProvider (typed SharedPreferences)

### Analytics
- TypingStats (WPM, accuracy, sessions, historical metrics)
- TypingDNA (typing fingerprint, dominant hand detection)
- TimeAwarePredictor (hour/day/app word frequency patterns)

### Privacy & Data
- ExportSchema (versioned JSON format)
- DataManager (export/import orchestrator)
- PrivacyDashboard (transparent data view)
- Nuclear delete (complete data wipe)
- Zero network requests by design

### Onboarding
- OnboardingActivity with 2-step setup flow
- Enable keyboard → Switch keyboard → Start typing
- Auto-skips on return visit if already configured

### Code Quality
- Full codebase audit (every file reviewed)
- 9 critical bugs found and fixed
- ProGuard rules for Room, coroutines, serialization
- MaterialComponents theme compatibility
- Unused imports cleaned up

---

### Testing (227 Total)

#### Rust Engine Tests (53)
- 21 unit tests (predictor, corrector, learner, lib)
- 32 integration tests (extra_tests.rs)

#### Kotlin Unit Tests (174)
- WordComposerTest — 25 tests
- KeyboardLayoutTest — 20 tests
- ThemeConfigTest — 18 tests
- ExportSchemaTest — 14 tests
- PopupPreviewManagerTest — 12 tests
- SwipeDetectorTest — 12 tests
- SpacebarCursorManagerTest — 10 tests
- KeyRepeatManagerTest — 10 tests
- TimeAwarePredictorTest — 9 tests

All tests passing: `cargo test` ✅ `./gradlew testDebugUnitTest` ✅

---

### Bug Fixes
- Fixed crash: Theme.AppCompat/MaterialComponents required for AppCompatActivity
- Fixed crash: runBlocking import path in DataManager and PrivacyDashboard
- Fixed crash: OnboardingActivity Button styling with MaterialComponents theme
- Fixed crash: SettingsMainFragment programmatic view thread safety
- Fixed bug: ClipboardPanel.itemRects accumulating in onDraw (memory leak)
- Fixed bug: SuggestionBar touch detection during animation
- Fixed bug: OnboardingActivity "Start Typing" button logic
- Fixed bug: EmojiPanel onMeasure ignoring height constraint
- Fixed bug: SettingsActivity deprecated onBackPressed
- Fixed bug: TimeAwarePredictor.reset() clearing ALL SharedPreferences
- Fixed bug: KeyboardSettingsProvider.resetToDefaults() clearing ALL SharedPreferences
- Fixed bug: DataManager.nuclearDelete() blocking main thread with runBlocking
- Fixed bug: EmojiPanel phantom touches outside view bounds
- Fixed bug: AboutFragment version string saying "Week 9"
- Fixed bug: Hindi isAsciiCapable incorrectly set to true

### Cleanup
- Removed unused imports across codebase
- Removed dead code (unused GradientDrawable variable)

---

### Technical Details
- **Target:** Android 8.0+ (API 26), ARM64 only
- **Architecture:** Kotlin (IME) + Rust (ARM64 engine) via JNI
- **Storage:** Room DB + SharedPreferences, <2MB
- **Engine Size:** 309KB (ARM64, stripped, LTO)
- **License:** GPLv3
- **Build Time:** 2 days (August 21-22, 2026)
