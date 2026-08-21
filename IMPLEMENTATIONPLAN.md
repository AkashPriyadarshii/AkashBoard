# IMPLEMENTATIONPLAN.md — Build Plan

## AkashBoard v1.0

**Author:** Akash Priyadarshi
**Date:** August 21, 2026
**Status:** Draft
**Duration:** 12 weeks (84 days)

---

## Overview

This document details the exact steps to build AkashBoard from zero to v1.0 release. Each week has clear deliverables, files to create, and acceptance criteria.

**Working Directory:** `C:\Users\saves\Desktop\AkashBoard`

---

## Prerequisites

### Required Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Android Studio | Ladybug (2024.2+) | IDE, SDK, emulator |
| JDK | 21 (Eclipse Temurin) | ✅ Already installed |
| Gradle | 8.12+ | ✅ Already installed at `C:\tools\gradle-8.12` |
| Rust | 1.96.0+ | ✅ Already installed |
| cargo-ndk | Latest | Rust→Android cross-compilation |
| Android NDK | 28.2.13676358 | ✅ Already installed at `C:\Android\ndk\28.2.13676358` |
| Android SDK | API 26-35 | ✅ Already installed at `C:\Android` |
| Git | 2.54+ | ✅ Already installed |

### Setup Commands

```bash
# Install cargo-ndk
cargo install cargo-ndk

# Add Android target
rustup target add aarch64-linux-android

# Verify NDK
ls $ANDROID_HOME/ndk/28.2.13676358/
```

---

## Week 1: Project Scaffold

### Goal: Empty Android project that builds and installs

### Day 1-2: Create Android Project

**Files to create:**

```
AkashBoard/
├── build.gradle.kts              # Root build (Kotlin DSL)
├── settings.gradle.kts           # Module settings
├── gradle.properties             # Build properties
├── local.properties              # SDK/NDK paths
├── gradlew                       # Gradle wrapper
├── gradlew.bat                   # Gradle wrapper (Windows)
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts          # App module build
    └── src/main/
        ├── AndroidManifest.xml   # IME service declaration
        ├── java/com/akashboard/
        │   └── AkashBoardIME.kt  # Minimal IME service
        └── res/
            ├── layout/
            │   └── keyboard_main.xml  # Placeholder layout
            ├── values/
            │   ├── strings.xml
            │   └── themes.xml
            └── xml/
                └── method.xml    # IME configuration
```

**Key file: `app/src/main/AndroidManifest.xml`**
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.akashboard">

    <!-- NO INTERNET PERMISSION — by design -->
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:supportsRtl="true"
        android:theme="@style/Theme.AkashBoard">

        <service
            android:name=".AkashBoardIME"
            android:label="@string/ime_name"
            android:permission="android.permission.BIND_INPUT_METHOD"
            android:exported="true">
            <intent-filter>
                <action android:name="android.view.InputMethod" />
            </intent-filter>
            <meta-data
                android:name="android.view.im"
                android:resource="@xml/method" />
        </service>

    </application>
</manifest>
```

**Key file: `app/src/main/java/com/akashboard/AkashBoardIME.kt`**
```kotlin
package com.akashboard

import android.inputmethodservice.InputMethodService
import android.view.View

class AkashBoardIME : InputMethodService() {

    override fun onCreateInputView(): View {
        // Week 2: Replace with KeyboardView
        return layoutInflater.inflate(R.layout.keyboard_main, null)
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Future: Reset word composer, update context
    }

    override fun onKey(code: Int, labels: IntArray?, x: Int, y: Int) {
        // Week 3: Implement key handling
    }
}
```

### Day 3-4: Build & Test on Device

```bash
# Build debug APK
cd AkashBoard
./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Enable in Settings → System → Languages → Keyboard
# Switch to AkashBoard in any text field
```

### Day 5-7: Setup Rust Engine Scaffold

**Files to create:**

```
AkashBoard/
├── engine/
│   ├── Cargo.toml
│   ├── build.rs
│   └── src/
│       └── lib.rs
└── app/src/main/jniLibs/
    └── arm64-v8a/          # Will contain compiled .so
```

**Key file: `engine/Cargo.toml`**
```toml
[package]
name = "akashboard-engine"
version = "0.1.0"
edition = "2021"

[lib]
crate-type = ["cdylib"]

[dependencies]
jni = "0.21"

[profile.release]
opt-level = "z"
lto = true
codegen-units = 1
strip = true
```

**Key file: `engine/src/lib.rs`**
```rust
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeInit(
    mut env: JNIEnv,
    _class: JClass,
    config_path: JString,
) {
    let _config: String = env.get_string(&config_path).unwrap().into();
    // Week 4: Initialize prediction engine
}

#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativePredict(
    mut env: JNIEnv,
    _class: JClass,
    context: JString,
    top_k: i32,
) -> jstring {
    let _context: String = env.get_string(&context).unwrap().into();
    let _top_k = top_k as usize;
    // Week 4: Implement prediction
    let result = env.new_string("hello").unwrap();
    result.into_raw()
}
```

**Build Rust engine:**
```bash
cd engine
cargo ndk -t arm64-v8a build --release
cp target/aarch64-linux-android/release/libakashboard_engine.so \
   ../app/src/main/jniLibs/arm64-v8a/libpredictor.so
```

### Week 1 Deliverables
- [ ] Android project builds without errors
- [ ] AkashBoardIME appears in keyboard picker
- [ ] Keyboard shows a placeholder view
- [ ] Rust engine compiles to ARM64 .so
- [ ] JNI bridge loads without crash

---

## Week 2: Core Keyboard Rendering

### Goal: QWERTY keyboard renders via Canvas, keys are tappable

### Files to create/modify:

```
app/src/main/java/com/akashboard/
├── ui/
│   ├── KeyboardView.kt          # Custom View, Canvas rendering
│   ├── KeyData.kt               # Key model (label, code, rect)
│   └── KeyboardLayout.kt        # Layout definitions (QWERTY, symbols)
├── core/
│   └── InputHandler.kt          # Key event → character output
└── AkashBoardIME.kt             # Updated to use KeyboardView
```

### Key Implementation: `KeyboardView.kt`

```kotlin
class KeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val keys = mutableListOf<KeyData>()
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Touch handling
    private var pressedKey: KeyData? = null
    
    fun loadLayout(layout: KeyboardLayout) {
        keys.clear()
        keys.addAll(layout.keys)
        requestLayout()
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (key in keys) {
            drawKey(canvas, key)
        }
    }
    
    private fun drawKey(canvas: Canvas, key: KeyData) {
        val isPressed = key == pressedKey
        val scale = if (isPressed) 0.92f else 1.0f
        
        canvas.save()
        canvas.scale(scale, scale, key.rect.centerX(), key.rect.centerY())
        
        // Background
        keyPaint.color = if (isPressed) pressedColor else bgColor
        canvas.drawRoundRect(key.rect, cornerRadius, cornerRadius, keyPaint)
        
        // Border
        keyPaint.color = borderColor
        canvas.drawRoundRect(key.rect, cornerRadius, cornerRadius, keyPaint)
        
        // Text
        canvas.drawText(
            key.label,
            key.rect.centerX(),
            key.rect.centerY() + textOffset,
            textPaint
        )
        
        canvas.restore()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedKey = findKeyAt(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                pressedKey?.let { key ->
                    onKeyPressedListener?.onKeyPressed(key)
                }
                pressedKey = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun findKeyAt(x: Float, y: Float): KeyData? {
        return keys.find { it.rect.contains(x, y) }
    }
    
    interface OnKeyPressedListener {
        fun onKeyPressed(key: KeyData)
    }
    
    var onKeyPressedListener: OnKeyPressedListener? = null
}
```

### Week 2 Deliverables
- [ ] QWERTY layout renders correctly
- [ ] Keys respond to touch with visual feedback
- [ ] Key press scale animation works (0.92x)
- [ ] Keyboard size adapts to screen width
- [ ] No crashes on rapid tapping

---

## Week 3: Key Handling & Input Pipeline

### Goal: Typing produces characters in the target app

### Files to create/modify:

```
app/src/main/java/com/akashboard/
├── core/
│   ├── InputHandler.kt          # Main input pipeline
│   ├── WordComposer.kt          # Tracks current word
│   └── HapticFeedback.kt        # Vibration patterns
└── AkashBoardIME.kt             # Connect KeyboardView to InputConnection
```

### Key Implementation: `InputHandler.kt`

```kotlin
class InputHandler(
    private val inputConnection: InputConnection,
    private val hapticFeedback: HapticFeedback
) {
    private val wordComposer = WordComposer()
    
    fun handleKeyPress(key: KeyData) {
        when {
            key.isLetter -> {
                wordComposer.addCharacter(key.code)
                inputConnection.commitText(key.label, 1)
                hapticFeedback.tap()
            }
            key.code == KEYCODE_SPACE -> {
                wordComposer.finishWord()
                inputConnection.commitText(" ", 1)
                hapticFeedback.tap()
            }
            key.code == KEYCODE_DELETE -> {
                wordComposer.deleteLast()
                inputConnection.deleteSurroundingText(1, 0)
                hapticFeedback.tap()
            }
            key.code == KEYCODE_ENTER -> {
                inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)
                hapticFeedback.tap()
            }
            key.code == KEYCODE_SHIFT -> {
                // Toggle shift state
                hapticFeedback.tap()
            }
        }
    }
}
```

### Week 3 Deliverables
- [ ] Typing produces characters in any text field
- [ ] Backspace deletes characters
- [ ] Spacebar inserts space
- [ ] Enter key sends action (send/done/next based on field)
- [ ] Shift toggles capitalization
- [ ] Long-press backspace repeats delete
- [ ] Haptic feedback on key press

---

## Week 4: Rust Prediction Engine

### Goal: Next-word predictions appear in suggestion bar

### Files to create/modify:

```
engine/src/
├── lib.rs                        # Updated JNI bridge
├── predictor.rs                  # N-gram prediction
└── tokenizer.rs                  # Text tokenization

app/src/main/java/com/akashboard/
├── engine/
│   └── PredictorBridge.kt        # Kotlin JNI wrapper
├── ui/
│   └── SuggestionBar.kt          # Suggestion strip UI
└── core/
    └── InputHandler.kt           # Updated to use predictions
```

### Key Implementation: `engine/src/predictor.rs`

```rust
use std::collections::HashMap;

pub struct Predictor {
    unigrams: HashMap<String, u32>,
    bigrams: HashMap<(String, String), u32>,
    string_table: Vec<String>,
}

impl Predictor {
    pub fn new() -> Self {
        Self {
            unigrams: HashMap::new(),
            bigrams: HashMap::new(),
            string_table: Vec::new(),
        }
    }
    
    pub fn predict(&self, context: &str, top_k: usize) -> Vec<String> {
        let words: Vec<&str> = context.split_whitespace().collect();
        if words.is_empty() {
            return self.top_unigrams(top_k);
        }
        
        let last_word = words.last().unwrap().to_lowercase();
        
        // Find bigrams starting with last_word
        let mut candidates: Vec<(String, u32)> = self.bigrams.iter()
            .filter(|((prev, _), _)| prev.to_lowercase() == last_word)
            .map(|((_, next), freq)| (next.clone(), *freq))
            .collect();
        
        // Sort by frequency
        candidates.sort_by(|a, b| b.1.cmp(&a.1));
        
        candidates.into_iter()
            .take(top_k)
            .map(|(word, _)| word)
            .collect()
    }
    
    pub fn learn(&mut self, word: &str, context: &str) {
        let lower_word = word.to_lowercase();
        *self.unigrams.entry(lower_word.clone()).or_insert(0) += 1;
        
        if let Some(last) = context.split_whitespace().last() {
            let key = (last.to_lowercase(), lower_word);
            *self.bigrams.entry(key).or_insert(0) += 1;
        }
    }
    
    fn top_unigrams(&self, k: usize) -> Vec<String> {
        let mut words: Vec<_> = self.unigrams.iter().collect();
        words.sort_by(|a, b| b.1.cmp(a.1));
        words.into_iter()
            .take(k)
            .map(|(word, _)| word.clone())
            .collect()
    }
}
```

### Week 4 Deliverables
- [ ] Rust predictor returns suggestions in <1ms
- [ ] Suggestion bar shows top 3 predictions
- [ ] Tap suggestion inserts word
- [ ] Predictor learns from typed words
- [ ] JNI bridge works without crashes
- [ ] No memory leaks (test with LeakCanary)

---

## Week 5: Swipe Typing

### Goal: Glide typing works for common English words

### Files to create/modify:

```
app/src/main/java/com/akashboard/core/
├── SwipeDetector.kt             # Gesture recognition (ported from ASK)
└── SwipeRenderer.kt             # Trail drawing

engine/src/
└── swipe.rs                     # Path matching algorithm
```

### Port from AnySoftKeyboard

**Key algorithm to port:**
```kotlin
// SwipeDetector.kt — Port of AnySoftKeyboard's GestureTypingDetector
class SwipeDetector(private val keyPositions: Map<Char, RectF>) {
    
    data class Point(val x: Float, val y: Float, val time: Long)
    
    private val points = mutableListOf<Point>()
    private var isTracking = false
    
    fun onTouchDown(x: Float, y: Float) {
        points.clear()
        points.add(Point(x, y, System.currentTimeMillis()))
        isTracking = true
    }
    
    fun onTouchMove(x: Float, y: Float) {
        if (!isTracking) return
        points.add(Point(x, y, System.currentTimeMillis()))
    }
    
    fun onTouchUp(): List<String> {
        isTracking = false
        return recognize(points, keyPositions)
    }
    
    private fun recognize(
        gesturePoints: List<Point>,
        keys: Map<Char, RectF>
    ): List<String> {
        // 1. Filter points (remove noise)
        val filtered = filterPoints(gesturePoints)
        
        // 2. Find closest key for each point
        val keySequence = filtered.map { point ->
            findClosestKey(point, keys)
        }.filterNotNull()
        
        // 3. Match against dictionary
        return matchDictionary(keySequence, keys)
    }
    
    private fun findClosestKey(point: Point, keys: Map<Char, RectF>): Char? {
        return keys.minByOrNull { (_, rect) ->
            val centerX = rect.centerX()
            val centerY = rect.centerY()
            val dx = point.x - centerX
            val dy = point.y - centerY
            dx * dx + dy * dy
        }?.key
    }
}
```

### Week 5 Deliverables
- [ ] Swipe trail renders on screen
- [ ] Basic path-to-word matching works
- [ ] 80% accuracy on top 1000 English words
- [ ] Swipe delete (left from backspace) works
- [ ] Swipe spacebar cursor movement works

---

## Week 6: Auto-Correct & Suggestion Bar Polish

### Goal: Autocorrect feels smart, suggestion bar is polished

### Files to create/modify:

```
engine/src/
└── corrector.rs                  # Error correction engine

app/src/main/java/com/akashboard/
├── ui/
│   └── SuggestionBar.kt          # Updated animations
└── core/
    └── InputHandler.kt           # Updated with autocorrect
```

### Week 6 Deliverables
- [ ] Auto-correct fixes common typos
- [ ] Auto-correct learns user's error patterns
- [ ] Suggestion bar animations are smooth
- [ ] Tap to accept suggestion works
- [ ] Swipe to dismiss suggestion works

---

## Week 7: Theme Engine

### Goal: 5 beautiful themes with glassmorphism effects

### Files to create:

```
app/src/main/java/com/akashboard/theme/
├── ThemeManager.kt              # Theme loading/application
├── ThemeConfig.kt               # Theme data model
├── ThemeParser.kt               # JSON parser
└── PerAppTint.kt                # Per-app color adaptation

themes/
├── akash-dark.json
├── akash-light.json
├── neon-cyber.json
├── minimal.json
└── sunset.json
```

### Week 7 Deliverables
- [ ] 5 themes render correctly
- [ ] Theme switching works instantly
- [ ] Glassmorphism blur effects work
- [ ] Key press glow animation works
- [ ] Dark/light mode follows system setting

---

## Week 8: Emoji, Clipboard, Voice

### Goal: Emoji panel, clipboard history, voice input all functional

### Files to create:

```
app/src/main/java/com/akashboard/ui/
├── EmojiPanel.kt                # Emoji grid
└── ClipboardPanel.kt            # Clipboard history

app/src/main/java/com/akashboard/data/
└── ClipboardDB.kt               # Room database

app/src/main/java/com/akashboard/core/
└── VoiceInput.kt                # SpeechRecognizer wrapper
```

### Week 8 Deliverables
- [ ] Emoji panel with categories works
- [ ] Clipboard history saves items
- [ ] Clipboard history shows in panel
- [ ] One-tap paste from clipboard
- [ ] Voice-to-text works (offline)

---

## Week 9: Multi-Language & Shortcuts

### Goal: Hindi, Spanish, French support + text shortcuts

### Files to create:

```
app/src/main/java/com/akashboard/core/
├── LanguageManager.kt           # Language switching
└── ShortcutsManager.kt          # Text expansion

app/src/main/res/xml/
├── keyboard_qwerty_en.xml
├── keyboard_qwerty_hi.xml
├── keyboard_qwerty_es.xml
└── keyboard_qwerty_fr.xml
```

### Week 9 Deliverables
- [ ] Language switcher works (long-press space)
- [ ] Hindi layout renders correctly
- [ ] Spanish layout renders correctly
- [ ] French layout renders correctly
- [ ] Text shortcuts expand (brw → "be right with you")

---

## Week 10: Analytics & Intelligence

### Goal: Typing stats, typing DNA, time-aware predictions

### Files to create:

```
app/src/main/java/com/akashboard/analytics/
├── TypingStats.kt               # Speed, accuracy tracking
├── TypingDNA.kt                 # Unique fingerprint generation
└── TimeAwarePredictor.kt        # Time-based pattern matching

engine/src/
└── learner.rs                   # Updated with time patterns
```

### Week 10 Deliverables
- [ ] Typing speed tracked and displayed
- [ ] Typing accuracy tracked
- [ ] Typing DNA visualization works
- [ ] Time-aware predictions improve after 7 days

---

## Week 11: Export/Import & Privacy

### Goal: Full data portability, privacy dashboard, incognito mode

### Files to create:

```
app/src/main/java/com/akashboard/data/
├── DataManager.kt               # Export/Import orchestrator
├── ExportSchema.kt               # JSON schema
└── PrivacyDashboard.kt           # Data viewer UI
```

### Week 11 Deliverables
- [ ] Export produces valid JSON
- [ ] Import validates and loads data
- [ ] Privacy dashboard shows all stored data
- [ ] Nuclear delete wipes all data
- [ ] Incognito mode stops learning

---

## Week 12: Polish, Testing, Launch

### Goal: Bug fixes, performance optimization, release APK

### Tasks:

1. **Performance audit**
   - [ ] Key press latency <5ms
   - [ ] Prediction latency <1ms
   - [ ] Memory usage <30MB
   - [ ] No ANRs in 1000-keystroke test

2. **Bug fixes**
   - [ ] Fix all crashes from device testing
   - [ ] Fix edge cases (empty input, long words, rapid typing)

3. **Build release APK**
   ```bash
   ./gradlew assembleRelease
   # Sign with release key
   # Verify APK size <5MB
   ```

4. **Documentation**
   - [ ] Update README.md with screenshots
   - [ ] Add build instructions
   - [ ] Add contribution guidelines

5. **Release**
   - [ ] Create GitHub release with APK
   - [ ] Submit to F-Droid
   - [ ] Announce on Reddit, XDA, Hacker News

### Week 12 Deliverables
- [ ] Release APK builds successfully
- [ ] APK size <5MB
- [ ] Zero crashes in testing
- [ ] README has screenshots
- [ ] GitHub release published

---

## Timeline Summary

```
Week  1: ██████████ Project scaffold + Rust setup
Week  2: ██████████ Core keyboard rendering
Week  3: ██████████ Key handling + input pipeline
Week  4: ██████████ Rust prediction engine
Week  5: ██████████ Swipe typing
Week  6: ██████████ Auto-correct + suggestion polish
Week  7: ██████████ Theme engine + 5 themes
Week  8: ██████████ Emoji + clipboard + voice
Week  9: ██████████ Multi-language + shortcuts
Week 10: ██████████ Analytics + intelligence
Week 11: ██████████ Export/import + privacy
Week 12: ██████████ Polish + testing + launch
```

---

## Risk Mitigation

| Risk | Mitigation | Fallback |
|------|-----------|----------|
| Rust-JNI issues | Test FFI early (Week 1) | Use pure Kotlin predictor |
| Android IME quirks | Study AOSP LatinIME source | Fork OpenBoard as base |
| Swipe accuracy | Port proven ASK algorithm | Use basic path matching |
| Storage overflow | Implement budget in Week 1 | Aggressive pruning |
| Device compatibility | Test on 3+ devices | Lower min SDK to 24 |

---

## Success Criteria Checklist

- [ ] Keyboard renders on all screen sizes
- [ ] Key press latency <5ms
- [ ] Prediction latency <1ms
- [ ] Swipe typing 80%+ accuracy
- [ ] Zero network requests
- [ ] APK size <5MB
- [ ] Zero crashes in 1000 keystrokes
- [ ] 5 themes working
- [ ] Export/Import functional
- [ ] F-Droid submission ready
