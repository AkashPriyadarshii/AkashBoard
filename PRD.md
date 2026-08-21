# PRD.md — Product Requirements Document

## AkashBoard v1.0

**Author:** Akash Priyadarshi
**Date:** August 21, 2026
**Status:** Draft
**License:** GPLv3

---

## 1. Executive Summary

AkashBoard is a FOSS Android keyboard that genuinely evolves with the user. Unlike Gboard (cloud-based, privacy-invasive) or FOSS alternatives (incomplete, no personal learning), AkashBoard combines the best of both worlds: **real personal intelligence with zero privacy compromise**.

**Target User:** Privacy-conscious Android users who want a keyboard that gets smarter over time without sending data to the cloud.

**Success Metric:** Users who try AkashBoard for 30+ days should find it impossible to switch back to their previous keyboard.

---

## 2. Problem Statement

### The Keyboard Problem

Keyboards are the most-used app on any phone. Users interact with them **hundreds of times daily**. Yet:

1. **Gboard** (10B+ downloads) sends all keystrokes to Google servers. Users don't know this.
2. **SwiftKey** (1B+ downloads) requires Microsoft account, shows ads, and still depends on cloud.
3. **FOSS keyboards** (OpenBoard, HeliBoard, FlorisBoard) are privacy-respecting but lack:
   - Personal learning (they use generic dictionaries, not YOUR patterns)
   - Modern UI (they look like 2018)
   - Complete feature sets (no clipboard manager, no export/import, no analytics)
   - Swipe typing without proprietary blobs

### The Opportunity

There is no keyboard that is simultaneously:
- ✅ 100% private (no cloud)
- ✅ Genuinely personal (learns YOUR typing)
- ✅ Feature-complete (swipe, emoji, clipboard, voice, themes)
- ✅ Beautiful (modern 2026 UI)
- ✅ Open source (GPLv3)

**AkashBoard fills this gap.**

---

## 3. Target Audience

### Primary: Privacy-Conscious Android Users
- Age: 18-45
- Tech-savvy enough to install F-Droid or sideload APKs
- Care about data privacy
- Currently using Gboard but uncomfortable with data collection
- Willing to try new keyboards

### Secondary: FOSS Enthusiasts
- Already using HeliBoard, OpenBoard, or FlorisBoard
- Frustrated by missing features (no learning, no swipe, no themes)
- Want a keyboard that's actually competitive with closed-source options

### Tertiary: Power Users
- Want typing analytics and insights
- Want customizable themes and layouts
- Want export/import of their typing profile

---

## 4. Product Goals

### v1.0 Goals (12 weeks)
| Goal | Metric |
|------|--------|
| Core keyboard works flawlessly | Zero crashes in 1000 keystrokes |
| Predictions feel smart | User rates suggestions 4/5+ after 7 days |
| Swipe typing works | 80% accuracy on common English words |
| Themes look modern | Glassmorphism UI matches 2026 design standards |
| Privacy is real | Zero network requests (verified by network monitor) |
| APK is small | <5MB install size |

### v2.0 Goals (Post-launch)
| Goal | Metric |
|------|--------|
| Personal learning is noticeable | User feels keyboard "knows them" after 30 days |
| Typing DNA is unique | No two users have identical typing fingerprints |
| Community grows | 1000+ GitHub stars in 3 months |
| F-Droid featured | Listed in F-Droid "Featured" section |

---

## 5. Feature Requirements

### 5.1 Core Keyboard (P0 — Must Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-001 | QWERTY Layout | Standard QWERTY keyboard with number row toggle | Keys render correctly on all screen sizes |
| F-002 | Key Press | Handle tap events with haptic feedback | <5ms from tap to character output |
| F-003 | Shift/Caps | Toggle shift, caps lock, auto-capitalize | Works at sentence start, after period, manual toggle |
| F-004 | Backspace | Delete previous character, long-press repeat | Repeat starts after 300ms, repeats every 50ms |
| F-005 | Spacebar | Insert space, cursor movement via swipe | Space inserts space, swipe moves cursor |
| F-006 | Enter | Insert newline or send action (based on EditorInfo) | Respects imeOptions (actionSend, actionDone, etc.) |
| F-007 | Symbol Layout | Switch to symbols/numbers layout | Toggle between ABC and ?123 layouts |
| F-008 | Emoji Layout | Dedicated emoji panel with categories | Scrollable grid, category tabs, recently used |
| F-009 | Language Switch | Cycle through enabled languages | Long-press spacebar or globe key |

### 5.2 Prediction Engine (P0 — Must Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-010 | Next-Word Prediction | Suggest top 3 next words based on context | Uses n-gram model, returns in <1ms |
| F-011 | Auto-Correct | Correct misspelled words as you type | Corrects common errors, respects user dictionary |
| F-012 | Word Completion | Suggest completions for partial words | Shows completions after 3+ characters typed |
| F-013 | Suggestion Bar | Strip above keyboard showing suggestions | Tap to accept, swipe to dismiss, 3 suggestions max |

### 5.3 Swipe Typing (P0 — Must Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-014 | Glide Typing | Swipe finger across keys to type words | Path matching with fuzzy tolerance |
| F-015 | Swipe Delete | Swipe left from backspace to delete words | Deletes word-by-word on swipe |
| F-016 | Swipe Spacebar | Swipe spacebar to move cursor | Smooth cursor movement following finger |

### 5.4 Theme Engine (P1 — Should Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-017 | 5 Built-in Themes | Dark, Light, Neon, Minimal, Sunset | Each theme has unique color palette and style |
| F-018 | Glassmorphism UI | Frosted glass keys with blur effects | Smooth animations, no jank |
| F-019 | Key Press Animation | Scale + ripple effect on key press | 60fps, GPU-accelerated |
| F-020 | Custom Theme Picker | Select and preview themes | Live preview before applying |
| F-021 | Per-App Tint | Keyboard color changes per app | Detects current app, applies matching tint |

### 5.5 Clipboard Manager (P1 — Should Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-022 | Clipboard History | Auto-save last 50 clippings | Accessible from keyboard toolbar |
| F-023 | Pin Clippings | Pin important items permanently | Pinned items survive history pruning |
| F-024 | Search Clipboard | Search through clipboard history | Instant search, filter by text |
| F-025 | One-Tap Paste | Tap to paste any clipping | Inserts at cursor position |

### 5.6 Voice Input (P2 — Nice to Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-026 | Voice-to-Text | Dictate text via microphone | Uses Android SpeechRecognizer, works offline |
| F-027 | Voice Button | Dedicated mic key on keyboard | One-tap to start/stop recording |

### 5.7 Text Shortcuts (P2 — Nice to Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-028 | Text Expansion | Expand abbreviations to full text | `brw` → "be right with you" |
| F-029 | Shortcut Editor | Add/edit/delete shortcuts in settings | Simple form UI |
| F-030 | Built-in Shortcuts | 10+ common shortcuts pre-loaded | `omw`, `brb`, `ty`, `np`, etc. |

### 5.8 Personal Learning (P2 — Nice to Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-031 | Personal N-gram Model | Learn user's word patterns locally | Model updates after each session |
| F-032 | Error Pattern Learning | Learn user's common typos | Auto-corrects user-specific errors |
| F-033 | Time-Aware Learning | Track typing patterns by time of day | Different suggestions morning vs night |
| F-034 | Context Profiles | Formal/casual/technical per app | Detects app, adjusts suggestion tone |
| F-035 | Decay System | Old patterns lose weight over time | Patterns unused for 30 days decay by 10% |

### 5.9 Analytics & Insights (P2 — Nice to Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-036 | Typing Speed | Track WPM over time | Shows current speed, daily average |
| F-037 | Typing Accuracy | Track correction rate | Shows accuracy percentage |
| F-038 | Typing DNA | Visual typing fingerprint | Unique animated pattern per user |
| F-039 | Weekly Scorecard | Summary of typing stats | Delivered as notification or in-app card |

### 5.10 Data Management (P2 — Nice to Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-040 | Export All Data | Export typing model + themes + shortcuts + clipboard | Single JSON file, versioned schema |
| F-041 | Import Data | Import previously exported data | Validates schema, merges or replaces |
| F-042 | Privacy Dashboard | View all stored data | Categorized view with sizes |
| F-043 | Nuclear Delete | One-tap wipe all learned data | Confirmation dialog, irreversible |
| F-044 | Incognito Mode | Stop learning for current session | Toggle in suggestion bar |

### 5.11 Adaptive UI (P2 — Nice to Have)

| ID | Feature | Description | Acceptance Criteria |
|----|---------|-------------|-------------------|
| F-045 | Adaptive Key Sizing | Keys grow/shrink based on usage | Frequently used keys grow by up to 15% |
| F-046 | Typing Sounds | Custom keypress sound packs | Mechanical, retro, sci-fi options |

---

## 6. Non-Functional Requirements

### 6.1 Performance

| Metric | Target |
|--------|--------|
| Key press to character output | <5ms |
| Prediction response time | <1ms (Rust engine) |
| Swipe word recognition | <10ms |
| Keyboard show/hide animation | 60fps |
| Memory usage | <30MB RAM |
| Battery impact | <2% daily |
| APK size | <5MB |

### 6.2 Compatibility

| Requirement | Target |
|-------------|--------|
| Android version | 8.0+ (API 26) |
| Architecture | ARM64 (ARMv8-A) only for v1 |
| Screen sizes | 4.5" to 7" phones |
| Orientations | Portrait + Landscape |
| Dark mode | Follow system setting |

### 6.3 Privacy

| Requirement | Implementation |
|-------------|---------------|
| No network requests | No INTERNET permission in manifest |
| No analytics | No analytics SDKs or tracking |
| No cloud sync | All data stored locally |
| No account required | App works immediately after install |
| Data encryption | Encrypt user dictionary and clipboard at rest |
| Privacy dashboard | User can view all stored data |

### 6.4 Reliability

| Metric | Target |
|--------|--------|
| Crash rate | <0.1% sessions |
| ANR rate | <0.01% sessions |
| Data loss | Zero (backup before writes) |
| Uptime | 99.9% (no server dependency) |

---

## 7. Competitive Analysis

### Why Users Should Switch

| Pain Point | Current Solution | AkashBoard Solution |
|-----------|-----------------|-------------------|
| "Gboard sends my data to Google" | Switch to FOSS keyboard | AkashBoard: 100% local, zero network |
| "FOSS keyboards don't learn" | Accept generic suggestions | AkashBoard: Personal learning engine |
| "My keyboard looks outdated" | Use Gboard's basic themes | AkashBoard: Glassmorphism, 5 themes, per-app tint |
| "I can't export my typing profile" | Lose everything on phone switch | AkashBoard: One-tap export/import |
| "Autocorrect changes 'gonna' to 'going'" | Disable autocorrect entirely | AkashBoard: Learns YOUR vocabulary |
| "No clipboard manager" | Use a separate clipboard app | AkashBoard: Built-in clipboard history |
| "Swipe typing requires Google's blob" | Give up on FOSS swipe | AkashBoard: FOSS swipe algorithm (AnySoftKeyboard port) |

---

## 8. Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Rust-JNI integration complexity | High | Medium | Start with simple FFI, test early, fallback to pure Kotlin |
| Android IME API quirks | High | High | Study AOSP LatinIME source, test on multiple devices |
| Storage budget overflow | Medium | Medium | Hard cap + LRU eviction + warning system |
| Users don't switch from Gboard | High | High | Focus on privacy angle + "Day 30 lock-in" strategy |
| Swipe typing accuracy | Medium | Medium | Port proven AnySoftKeyboard algorithm, tune parameters |
| Battery drain from learning | Medium | Low | Batch writes, use WorkManager, respect Doze mode |
| Google Play rejection | Low | Low | F-Droid first, sideload APK as backup |

---

## 9. Success Criteria

### v1.0 Launch Criteria
- [ ] Keyboard renders correctly on 5+ device sizes
- [ ] Key press to character output <5ms
- [ ] Prediction engine returns results <1ms
- [ ] Swipe typing works for 90% of common English words
- [ ] Zero network requests (verified by network monitor)
- [ ] APK size <5MB
- [ ] Zero crashes in 1000-keystroke test
- [ ] 5 themes working with glassmorphism effects
- [ ] Clipboard history functional
- [ ] Export/Import produces valid JSON

### Post-Launch Success Metrics (3 months)
- [ ] 1000+ GitHub stars
- [ ] 500+ F-Droid installs
- [ ] 50+ community themes shared
- [ ] 10+ community contributions
- [ ] 4+ star rating on F-Droid

---

## 10. Future Vision

### v2.0 (6 months post-launch)
- Typing DNA visualization
- Adaptive key sizing
- Mood-aware suggestions
- Typing sound packs
- Community theme marketplace
- Multi-language expansion (20+ languages)

### v3.0 (12 months post-launch)
- Wear OS support
- Tablet-optimized layout
- Split keyboard for tablets
- AI-powered text composition (local model)
- Cross-device sync via QR code (optional, encrypted)

---

## Appendix A: Glossary

| Term | Definition |
|------|-----------|
| **IME** | Input Method Editor — Android's keyboard API |
| **N-gram** | A sequence of N words used for prediction |
| **Trie** | A tree data structure for efficient word lookup |
| **JNI** | Java Native Interface — bridge between Kotlin and Rust |
| **ARM64** | 64-bit ARM architecture (ARMv8-A) |
| **Glassmorphism** | UI design style with frosted glass effects |
| **LRU** | Least Recently Used — cache eviction strategy |
| **Snygg** | FlorisBoard's CSS-like theming system (inspiration) |
| **GPLv3** | GNU General Public License v3.0 |
| **FOSS** | Free and Open Source Software |
