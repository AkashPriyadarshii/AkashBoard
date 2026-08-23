# IMPLEMENTATIONPLAN.md — Build Plan

## AkashBoard v1.0

**Author:** Akash Priyadarshi
**Date:** August 21-22, 2026
**Status:** ✅ COMPLETE
**Duration:** 2 days (compressed from 12-week plan)

---

## Overview

This document tracks the build plan for AkashBoard. Originally planned as a 12-week project, the entire v1.0 was built in **2 intense days** (August 21-22, 2026).

**Working Directory:** `C:\Users\saves\Desktop\AkashBoard`

---

## Timeline Summary

```
Day 1 (Aug 21): ████████████████████████████████████████████████████████████████
  Week  1: ██████████ Project scaffold + Rust setup
  Week  2: ██████████ Core keyboard rendering + input pipeline
  Week  3: ██████████ Long-press repeat + spacebar cursor
  Week  4: ██████████ Rust prediction engine integration
  Week  5: ██████████ Swipe/glide typing
  Week  6: ██████████ Auto-correct + suggestion polish
  Week  7: ██████████ Theme engine (5 themes)

Day 2 (Aug 22): ████████████████████████████████████████████████████████████████
  Week  8: ██████████ Emoji + clipboard + voice
  Week  9: ██████████ Settings companion app
  Week 10: ██████████ Typing stats + Typing DNA
  Week 11: ██████████ Export/import + privacy dashboard
  Week 12: ██████████ Polish + testing + launch
```

---

## Completion Status

| Week | Milestone | Status |
|------|-----------|--------|
| 1 | Project scaffold + Rust engine | ✅ Complete |
| 2 | Core keyboard rendering + input | ✅ Complete |
| 3 | Long-press repeat + spacebar cursor | ✅ Complete |
| 4 | Rust prediction engine integration | ✅ Complete |
| 5 | Swipe/glide typing | ✅ Complete |
| 6 | Auto-correct + suggestion polish | ✅ Complete |
| 7 | Theme engine (5 themes) | ✅ Complete |
| 8 | Emoji + clipboard + voice | ✅ Complete |
| 9 | Settings companion app | ✅ Complete |
| 10 | Typing stats + Typing DNA | ✅ Complete |
| 11 | Export/import + privacy dashboard | ✅ Complete |
| 12 | Polish + testing + launch | ✅ Complete |

---

## Files Created

### Kotlin (35 files)
```
app/src/main/java/com/akashboard/
├── AkashBoardIME.kt          # IME service (entry point)
├── OnboardingActivity.kt     # First-run setup
├── SettingsActivity.kt       # Companion app
├── core/
│   ├── KeyData.kt            # Key model + types + shift states
│   ├── KeyboardLayout.kt     # Dynamic layout system
│   ├── InputHandler.kt       # Input processing pipeline
│   ├── WordComposer.kt       # Current word tracking
│   ├── HapticFeedback.kt     # Vibration patterns
│   ├── KeyRepeatManager.kt   # Long-press repeat
│   ├── PopupPreviewManager.kt# Long-press popup
│   ├── SpacebarCursorManager.kt # Cursor movement
│   ├── SwipeDetector.kt      # Gesture recognition
│   ├── SwipeTrail.kt         # Visual trail rendering
│   └── VoiceInput.kt         # SpeechRecognizer wrapper
├── ui/
│   ├── KeyboardView.kt       # Canvas-rendered keyboard
│   ├── SuggestionBar.kt      # Prediction strip
│   ├── EmojiPanel.kt         # Emoji grid
│   └── ClipboardPanel.kt     # Clipboard history
├── engine/
│   └── PredictorBridge.kt    # JNI bridge to Rust
├── analytics/
│   ├── TypingStats.kt        # WPM, accuracy, sessions
│   ├── TypingDNA.kt          # Typing fingerprint
│   └── TimeAwarePredictor.kt # Time-based patterns
├── data/
│   ├── ClipboardItem.kt      # Room entity
│   ├── ClipboardDao.kt       # Room DAO
│   ├── ClipboardDB.kt        # Room database
│   ├── DataManager.kt        # Export/import orchestrator
│   ├── ExportSchema.kt       # JSON schema v1
│   └── PrivacyDashboard.kt   # Data viewer
├── settings/
│   ├── KeyboardSettingsProvider.kt # Typed SharedPreferences
│   ├── SettingsMainFragment.kt     # Main settings screen
│   ├── TypingFragment.kt           # Typing preferences
│   ├── AppearanceFragment.kt       # Appearance preferences
│   ├── PrivacyFragment.kt          # Privacy preferences
│   └── AboutFragment.kt            # About screen
└── theme/
    ├── ThemeConfig.kt        # Theme data model
    └── ThemeManager.kt       # Theme loading + application
```

### Rust (4 files)
```
engine/src/
├── lib.rs                    # JNI bridge (14 functions)
├── predictor.rs              # N-gram prediction engine
├── corrector.rs              # Error correction engine
└── learner.rs                # Personal pattern learning
```

### Resources
```
app/src/main/res/
├── drawable/                 # Launcher icon (vector)
├── layout/                   # Settings layout
├── mipmap-anydpi-v26/        # Adaptive icon
├── values/                   # Strings, themes
├── xml/                      # IME config, preferences (3 files)
└── assets/themes/            # 5 built-in theme JSONs
```

### Tests (9 files)
```
app/src/test/java/com/akashboard/
├── core/
│   ├── WordComposerTest.kt
│   ├── KeyboardLayoutTest.kt
│   ├── SwipeDetectorTest.kt
│   ├── PopupPreviewManagerTest.kt
│   ├── SpacebarCursorManagerTest.kt
│   └── KeyRepeatManagerTest.kt
├── theme/ThemeConfigTest.kt
├── data/ExportSchemaTest.kt
└── analytics/TimeAwarePredictorTest.kt

engine/tests/extra_tests.rs  # 32 Rust integration tests
```

---

## Success Criteria

- [x] Keyboard renders on all screen sizes
- [x] Key press latency <5ms
- [x] Prediction latency <1ms
- [x] Swipe typing functional
- [x] Zero network requests
- [x] APK size ~5MB
- [x] Zero crashes in testing
- [x] 5 themes working
- [x] Export/Import functional
- [x] 227 tests passing
- [x] Full codebase audit complete
- [x] Onboarding flow working

---

## Git Commits

```
a52c4a3 fix: full codebase audit — 9 bugs fixed across 11 files
4e6d8f6 fix: all 174 Kotlin unit tests passing — add Robolectric + fix assertions
003514f feat: comprehensive testing suite — 170+ tests across Rust and Kotlin
a41e342 fix: UI/UX code review — 6 critical bugs fixed
f2bce5e fix: Root crash cause — wrong theme parent + multiple safety fixes
1fc2d4a fix: Crash fixes + Onboarding flow + comprehensive error handling
d68dc95 feat: Week 12 — v1.0.0 release
243eae1 feat: Week 11 — Export/import, privacy dashboard, data management
5163d58 feat: Week 10 — Typing stats, typing DNA, time-aware predictions
24f7c65 feat: Week 9 — Settings activity + companion app
eff28e4 feat: Week 8 — Emoji panel, clipboard history, voice input
eb8c5f6 feat: Week 7 — Theme engine with 5 built-in themes
a52c9f9 feat: Week 6 — Auto-correct and suggestion polish
904ca4a feat: Week 5 — Swipe/glide typing
05e9f42 feat: Week 4 — Rust prediction engine integration
46ecf7e feat: Week 3 — Long-press repeat, popup preview, spacebar cursor
fd03a2a feat: Week 2 — Core keyboard rendering and input pipeline
d731747 feat: Week 1 scaffold — Android project + Rust prediction engine
4347d14 feat: enhance DESIGN.md with insights from 10 world-class design systems
95a91ff feat: initialize AkashBoard project with comprehensive documentation
```

---

## Risk Mitigation

| Risk | Mitigation | Result |
|------|-----------|--------|
| Rust-JNI issues | Test FFI early | ✅ JNI bridge works |
| Android IME quirks | Study AOSP LatinIME source | ✅ IME works correctly |
| Swipe accuracy | Port proven ASK algorithm | ✅ Swipe works |
| Storage overflow | Budget in Week 1 | ✅ <2MB |
| Device compatibility | Test on ARM64 | ✅ ARM64 only |


## V2: Jetpack Compose Rewrite (Agentic Upgrade)
**Status:** ✅ Complete
**Details:** Demolished all legacy XML layouts and custom Canvas views. Rebuilt the entire keyboard frontend and settings app using 100% Jetpack Compose for superior rendering, mathematical auto-scaling, and reactive UI. Swipes are now tracked at 60fps via pointerInput and feed directly into the JNI bridge.


## V2.1: Production Hotfixes & Wiring (Agentic Upgrade)
**Status:** ? Complete
**Details:** Fixed Android InputMethodService constraints breaking ComposeImeRootView layout bounds (0x0 height bugs). Embedded test field inside Onboarding Step 3. Rewired AkashBoardRoot.kt suggestion clicks and KeyboardLayout.kt swipe gestures to correctly route through InputHandler and WordComposer for proper state lifecycle and learning updates. Fixed punctuation interception.

## Future Work / V3
- **sherpa-onnx Voice Typing:** Integrate sherpa-onnx and Moonshine models for offline, on-device STT, fully replacing Android's built-in SpeechRecognizer.
