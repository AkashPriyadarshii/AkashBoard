# CHANGELOG.md — Version History

All notable changes to AkashBoard will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Planned
- Typing DNA visualization
- Adaptive key sizing
- Mood-aware suggestions
- Typing sound packs (Mechanical, Retro, Sci-fi)
- Community theme sharing
- Wear OS support
- Tablet-optimized layout

---

## [1.0.0] - 2026-XX-XX (Planned)

### Added

#### Core Keyboard
- QWERTY keyboard layout with responsive sizing
- Key press handling with haptic feedback
- Shift/Caps Lock with auto-capitalization
- Backspace with long-press repeat
- Spacebar with cursor movement via swipe
- Enter key respecting EditorInfo actions
- Symbol/number layout toggle
- Emoji panel with category browsing

#### Prediction Engine
- N-gram prediction model (unigrams, bigrams, trigrams)
- Next-word prediction (top 3 suggestions)
- Word completion for partial words
- Auto-correct with learned error patterns
- Suggestion bar with tap-to-accept

#### Swipe Typing
- Glide typing for English
- Swipe trail rendering
- Swipe-to-delete (left from backspace)
- Swipe spacebar for cursor movement

#### Theme Engine
- 5 built-in themes (Dark, Light, Neon Cyber, Minimal, Sunset)
- Glassmorphism UI with frosted glass effects
- Key press scale animation (0.92x)
- Glow effects on key press
- Theme switcher with live preview
- Dark/light mode following system setting

#### Clipboard Manager
- Clipboard history (last 50 items)
- Pin/unpin items
- One-tap paste
- Search clipboard
- Auto-clear after 5 minutes

#### Voice Input
- Voice-to-text via Android SpeechRecognizer
- Offline support
- Dedicated mic key on keyboard

#### Text Shortcuts
- Text expansion engine
- 10+ built-in shortcuts (brw, omw, brb, etc.)
- Custom shortcut editor

#### Multi-Language
- English (QWERTY)
- Hindi (Devanagari)
- Spanish (QWERTY)
- French (AZERTY)
- Language switcher via globe key

#### Analytics
- Typing speed tracking (WPM)
- Typing accuracy tracking
- Weekly scorecard

#### Data Management
- Export all data as JSON
- Import with merge/replace options
- Privacy dashboard (view all stored data)
- Nuclear delete (one-tap wipe)
- Incognito mode (stop learning)

#### Privacy
- Zero network requests (no INTERNET permission)
- No analytics or telemetry
- No account required
- Open source (GPLv3)

#### Performance
- Rust prediction engine (ARM64 native)
- Predictions in <1ms
- Key press to character output <5ms
- APK size <5MB
- Memory usage <30MB
- Battery impact <2% daily

### Changed
- N/A (initial release)

### Deprecated
- N/A (initial release)

### Removed
- N/A (initial release)

### Fixed
- N/A (initial release)

### Security
- No INTERNET permission in manifest
- All JNI inputs validated
- Clipboard auto-clear enabled
- Password fields excluded from clipboard
- Storage budget enforced (2MB cap)
- Theme JSON validated before parsing

---

## [0.1.0] - 2026-XX-XX (Development)

### Added
- Project scaffold
- Basic IME service
- QWERTY layout rendering
- Key press handling
- Rust engine FFI bridge
- N-gram predictor (basic)
- Suggestion bar UI

---

## Version Numbering

- **Major** (X.0.0): Breaking changes or major feature additions
- **Minor** (0.X.0): New features, backward compatible
- **Patch** (0.0.X): Bug fixes, backward compatible

## Release Cadence

- **v1.0.0**: Initial release (12 weeks of development)
- **v1.1.0**: 4 weeks after v1.0.0 (typical feature release)
- **v1.x.x**: As needed for bug fixes and features

## Support

- **v1.0.x**: Supported for 12 months after v1.1.0 release
- **v0.x.x**: No longer supported after v1.0.0 release
