# Changelog

All notable changes to AkashBoard will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.0.0] - 2026-08-21

### Added

#### Core Keyboard
- Custom Canvas-rendered keyboard (60-120 FPS)
- QWERTY layout with dynamic sizing
- Symbols layout with full key definitions
- Touch hitbox model (larger than visual keys)
- Key press animation (scale 0.92x)
- Hardware-accelerated rendering

#### Input Pipeline
- InputHandler with full key processing
- WordComposer for current word tracking
- HapticFeedback with 5 vibration patterns
- Password field detection
- Shift states (None → One → Locked)

#### Gestures
- Long-press key repeat (backspace: 300ms → 50ms → 20ms)
- Popup preview for long-press alternate characters
- Spacebar cursor movement via swipe
- Swipe/glide typing with Bezier trail rendering

#### Prediction Engine (Rust)
- N-gram prediction (unigram, bigram, trigram)
- Edit distance auto-correct (Levenshtein)
- 25+ built-in typo corrections
- Personal pattern learning (time-of-day, app context)
- Time-aware predictions (hour/day/app-based)
- JNI bridge with 14 functions

#### UI Components
- SuggestionBar with slide animations
- EmojiPanel (8 categories, grid rendering)
- ClipboardPanel (Room DB-backed, pinned items)
- VoiceInput (Android SpeechRecognizer wrapper)

#### Themes
- 5 built-in themes (Akash Dark, Light, Neon Cyber, Minimal, Sunset)
- JSON theme format (schema v1)
- ThemeManager with listener pattern
- Live theme switching

#### Settings App
- SettingsActivity with fragment navigation
- TypingFragment (autocorrect, predictions, gestures)
- AppearanceFragment (themes, height, one-handed)
- PrivacyFragment (incognito, clipboard, data)
- AboutFragment (version, licenses, GitHub)
- KeyboardSettingsProvider (typed SharedPreferences)

#### Analytics
- TypingStats (WPM, accuracy, sessions, historical metrics)
- TypingDNA (typing fingerprint, dominant hand detection)
- TimeAwarePredictor (hour/day/app word frequency patterns)

#### Privacy & Data
- ExportSchema (versioned JSON format)
- DataManager (export/import orchestrator)
- PrivacyDashboard (transparent data view)
- Nuclear delete (complete data wipe)
- Zero network requests by design

### Technical Details
- **Target:** Android 8.0+ (API 26), ARM64 only
- **Architecture:** Kotlin (IME) + Rust (ARM64 engine) via JNI
- **Storage:** Room DB + SharedPreferences, <2MB
- **Engine Size:** 309KB (ARM64, stripped, LTO)
- **License:** GPLv3

---

## [0.9.0] - 2026-08-21

### Added
- Initial project scaffold
- Android project with Gradle
- Rust prediction engine
- Basic keyboard rendering
- Core input pipeline

---

## [0.1.0] - 2026-08-21

### Added
- Project documentation
- Architecture design
- Implementation plan
- License (GPLv3)
