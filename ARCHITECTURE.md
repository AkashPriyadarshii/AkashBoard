# ARCHITECTURE.md — System Architecture

## AkashBoard v1.0

**Author:** Akash Priyadarshi
**Date:** August 21, 2026
**Status:** Draft

---

## 1. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        ANDROID SYSTEM                           │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Target App                             │  │
│  │  (WhatsApp, Chrome, Gmail, Notes, etc.)                  │  │
│  └───────────────────────┬───────────────────────────────────┘  │
│                          │ InputConnection                       │
│  ┌───────────────────────▼───────────────────────────────────┐  │
│  │              Android InputMethodManager                   │  │
│  │              (System Service)                             │  │
│  └───────────────────────┬───────────────────────────────────┘  │
│                          │                                       │
│  ┌───────────────────────▼───────────────────────────────────┐  │
│  │                  AkashBoardIME.kt                          │  │
│  │              (IME Service Entry Point)                     │  │
│  ├─────────┬──────────┬──────────┬──────────┬────────────────┤  │
│  │         │          │          │          │                 │  │
│  │  ┌──────▼──────┐ ┌─▼────────┐ ┌▼────────▼┐ ┌───────────┐ │  │
│  │  │ KeyboardView│ │Suggestion│ │  Emoji   │ │ Clipboard │ │  │
│  │  │  (Canvas)   │ │   Bar    │ │  Panel   │ │   Panel   │ │  │
│  │  └──────┬──────┘ └────┬────┘ └────┬─────┘ └─────┬─────┘ │  │
│  │         │             │           │              │        │  │
│  │  ┌──────▼─────────────▼───────────▼──────────────▼──────┐ │  │
│  │  │                  InputHandler.kt                      │ │  │
│  │  │          (Key Event Processing Engine)                │ │  │
│  │  └──────────────────────┬───────────────────────────────┘ │  │
│  │                         │                                  │  │
│  │  ┌──────────────────────▼───────────────────────────────┐ │  │
│  │  │              JNI Bridge (libpredictor.so)             │ │  │
│  │  └──────────────────────┬───────────────────────────────┘ │  │
│  └─────────────────────────┼─────────────────────────────────┘  │
│                            │ JNI Calls                           │
│  ┌─────────────────────────▼─────────────────────────────────┐  │
│  │                    RUST ENGINE (ARM64)                     │  │
│  │  ┌───────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐  │  │
│  │  │ Predictor │ │ Learner  │ │Corrector │ │   Mood     │  │  │
│  │  │ (N-gram)  │ │(Personal)│ │(Auto-fix)│ │ (Sentiment)│  │  │
│  │  └───────────┘ └──────────┘ └──────────┘ └────────────┘  │  │
│  │  ┌───────────┐ ┌──────────────────────────────────────┐  │  │
│  │  │  Swipe    │ │         Storage (Compressed)          │  │  │
│  │  │ (Path)    │ │     (Model + Learning Data)           │  │  │
│  │  └───────────┘ └──────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                   DATA LAYER                              │  │
│  │  ┌─────────────┐ ┌──────────────┐ ┌──────────────────┐  │  │
│  │  │  Room DB    │ │ SharedPrefs  │ │  File Storage    │  │  │
│  │  │ (Clipboard) │ │ (Settings)   │ │ (Themes, Export) │  │  │
│  │  └─────────────┘ └──────────────┘ └──────────────────┘  │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Layer Architecture

### 2.1 Presentation Layer (Kotlin)

Responsible for rendering the keyboard UI and handling user interaction.

```
presentation/
├── ui/
│   ├── KeyboardView.kt          # Main keyboard rendering (Canvas)
│   ├── SuggestionBar.kt         # Prediction strip
│   ├── EmojiPanel.kt            # Emoji grid
│   ├── ClipboardPanel.kt        # Clipboard history
│   ├── SwipeTrail.kt            # Gesture trail rendering
│   └── KeyRenderer.kt           # Individual key rendering
├── theme/
│   ├── ThemeManager.kt          # Theme loading and application
│   ├── ThemeEditor.kt           # Live theme preview
│   └── PerAppTint.kt            # Per-app color adaptation
└── animations/
    ├── KeyPressAnimator.kt      # Key press scale + ripple
    └── TransitionAnimator.kt    # Layout transition animations
```

**Key Design Decisions:**
- **Custom Canvas rendering** (not XML layouts) for 60fps performance
- **No Jetpack Compose** — too heavy for a keyboard (adds ~2MB overhead)
- **GPU-accelerated animations** via `HardwareLayer` for key press effects

### 2.2 Input Layer (Kotlin)

Processes raw key events and manages the input pipeline.

```
core/
├── InputHandler.kt              # Main input processing pipeline
├── WordComposer.kt              # Tracks current word being typed
├── SwipeDetector.kt             # Gesture recognition (ported from ASK)
├── VoiceInput.kt                # SpeechRecognizer wrapper
├── ShortcutsManager.kt          # Text expansion engine
├── HapticFeedback.kt            # Vibration patterns
└── InputConnectionWrapper.kt    # Android InputConnection abstraction
```

**Input Processing Pipeline:**

```
User Action → InputHandler → WordComposer → Engine Bridge → SuggestionBar
     │                                              │
     │              ┌───────────────────────────────┘
     │              │
     │    ┌─────────▼──────────┐
     │    │   Rust Engine      │
     │    │   predict()        │
     │    │   correct()        │
     │    │   learn()          │
     │    └─────────┬──────────┘
     │              │
     │    ┌─────────▼──────────┐
     │    │  Suggestions[]     │
     │    └────────────────────┘
     │
     └──→ InputConnection.commitText() → Target App
```

### 2.3 Engine Layer (Rust → JNI)

The brain of AkashBoard. Runs as native ARM64 code via JNI.

```
engine/src/
├── lib.rs                    # JNI entry points
├── predictor.rs              # N-gram prediction engine
├── learner.rs                # Personal pattern learning
├── corrector.rs              # Auto-correct engine
├── swipe.rs                  # Swipe path matching
├── mood.rs                   # Sentiment detection
├── tokenizer.rs              # Text tokenization
└── storage.rs                # Compressed model I/O
```

**Why Rust?**
| Factor | Java/Kotlin | Rust |
|--------|------------|------|
| Prediction speed | ~0.5ms | ~0.01ms |
| Memory safety | GC pauses | Zero-cost abstractions |
| Binary size | Large (JVM overhead) | Small (~35KB .so) |
| ARM64 native | Via JVM JIT | Direct native code |

### 2.4 Data Layer (Kotlin)

Manages persistent storage and data operations.

```
data/
├── ClipboardDB.kt             # Room database for clipboard history
├── SettingsStore.kt           # SharedPreferences wrapper
├── DataManager.kt             # Export/Import orchestrator
├── StorageBudget.kt           # Storage monitoring and enforcement
└── Crypto.kt                  # Optional encryption for sensitive data
```

---

## 3. JNI Bridge Design

### 3.1 Interface Contract

Kotlin calls Rust through a well-defined JNI interface:

```kotlin
// engine/PredictorBridge.kt
class PredictorBridge {
    companion object {
        init {
            System.loadLibrary("predictor")
        }
    }

    // Prediction
    external fun nativePredict(
        context: String,
        topK: Int
    ): Array<String>

    // Auto-correct
    external fun nativeCorrect(
        word: String,
        context: String
    ): String

    // Learning
    external fun nativeLearn(
        word: String,
        context: String,
        timestamp: Long
    ): Boolean

    // Swipe
    external fun nativeRecognizeSwipe(
        path: FloatArray,    // [x1,y1, x2,y2, ...]
        keyPositions: FloatArray, // [key1_x, key1_y, key1_w, key1_h, ...]
        topK: Int
    ): Array<String>

    // Mood
    external fun nativeDetectMood(
        text: String
    ): Float  // -1.0 (negative) to 1.0 (positive)

    // Storage
    external fun nativeSaveModel(): Boolean
    external fun nativeLoadModel(): Boolean
    external fun nativeGetStorageSize(): Long

    // Lifecycle
    external fun nativeInit(configPath: String)
    external fun nativeDestroy()
}
```

### 3.2 Data Flow Across JNI Boundary

```
┌──────────────┐                    ┌──────────────┐
│   Kotlin     │                    │    Rust      │
│              │                    │              │
│  predict()   │─── JNI Call ──────→│  predict()   │
│              │                    │              │
│  String      │                    │  &str        │
│  context     │                    │  context     │
│              │                    │              │
│  Array<String>│←── JNI Return ────│  Vec<String> │
│  suggestions │                    │  suggestions │
└──────────────┘                    └──────────────┘
```

**Key Rules:**
- Rust never holds references to Java objects
- All data crosses the boundary as primitives or owned types
- Kotlin allocates result arrays, Rust fills them
- Errors are returned as empty arrays (no exceptions across JNI)

---

## 4. Prediction Engine Architecture

### 4.1 N-gram Model

```
User types: "I am going to the"

Model stores:
  unigrams: { "I": 100, "am": 80, "going": 60, "to": 90, "the": 110 }
  bigrams:  { ("I", "am"): 80, ("am", "going"): 50, ("going", "to"): 45, ("to", "the"): 70 }
  trigrams: { ("I", "am", "going"): 30, ("am", "going", "to"): 25 }

Next prediction: "store" (if user frequently types "going to the store")
```

### 4.2 Learning Pipeline

```
User types word
    │
    ▼
┌─────────────┐
│ Tokenize    │ → Split into words
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Update      │ → Increment unigram count
│ Unigrams    │ → Update time-of-day pattern
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Update      │ → Increment bigram count
│ Bigrams     │ → Update context profile
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Check       │ → If word is new, add to model
│ Novelty     │ → If error pattern, update corrections
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Decay       │ → Apply time-based decay to old patterns
│ (Background)│ → Prune low-confidence entries
└─────────────┘
```

### 4.3 Storage Format

```rust
// Compressed binary format for the prediction model
struct ModelHeader {
    version: u32,           // Schema version
    unigram_count: u32,     // Number of unigrams
    bigram_count: u32,      // Number of bigrams
    trigram_count: u32,     // Number of trigrams
    created_at: u64,        // Unix timestamp
    last_updated: u64,      // Unix timestamp
    checksum: u32,          // CRC32 for integrity
}

// Unigram entry
struct UnigramEntry {
    word_id: u32,           // Reference to string table
    frequency: u32,         // Usage count
    last_used: u64,         // Unix timestamp
    time_pattern: [u32; 24],// Hourly usage counts
}

// Bigram entry
struct BigramEntry {
    prev_word_id: u32,      // Previous word reference
    curr_word_id: u32,      // Current word reference
    frequency: u32,         // Usage count
    context: u8,            // Context profile (0=neutral, 1=formal, 2=casual)
}
```

---

## 5. Theme System Architecture

### 5.1 Theme Schema (JSON)

```json
{
  "name": "Akash Dark",
  "version": 1,
  "author": "Akash Priyadarshi",
  
  "colors": {
    "background": "#0D0D1A",
    "keyBackground": "rgba(255, 255, 255, 0.08)",
    "keyPressed": "rgba(108, 99, 255, 0.4)",
    "keyBorder": "rgba(255, 255, 255, 0.12)",
    "keyText": "#FFFFFF",
    "suggestionBar": "rgba(255, 255, 255, 0.05)",
    "suggestionText": "#B8B8CC",
    "suggestionHighlight": "#6C63FF",
    "accent": "#6C63FF"
  },
  
  "dimensions": {
    "cornerRadius": 8,
    "keyElevation": 2,
    "keyPadding": 4,
    "suggestionBarHeight": 48
  },
  
  "animation": {
    "pressScale": 0.92,
    "pressDuration": 80,
    "rippleColor": "rgba(108, 99, 255, 0.3)",
    "transitionDuration": 200
  }
}
```

### 5.2 Theme Application Flow

```
ThemeManager.loadTheme("akash-dark")
    │
    ▼
┌─────────────────┐
│ Parse JSON      │ → Deserialize to ThemeConfig
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Validate        │ → Check all required fields
│                 │ → Apply defaults for missing fields
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Cache           │ → Store in memory for fast access
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Notify          │ → ThemeChanged event to all views
│ Observers       │ → KeyboardView re-renders
└─────────────────┘
```

---

## 6. Clipboard Architecture

### 6.1 Room Database Schema

```kotlin
@Entity(tableName = "clipboard_history")
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "source_app") val sourceApp: String? = null,
    @ColumnInfo(name = "char_count") val charCount: Int = text.length
)
```

### 6.2 Clipboard Lifecycle

```
User copies text
    │
    ▼
┌─────────────────┐
│ ClipboardManager │ → Detects clipboard change
│ (Listener)       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Create          │ → Build ClipboardItem
│ ClipboardItem   │ → Set timestamp, source app
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Check Budget    │ → Max 50 items
│                 │ → Prune oldest unpinned if over budget
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Insert to Room  │ → Async database write
│ DB              │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Notify UI       │ → Update ClipboardPanel if visible
└─────────────────┘
```

---

## 7. Swipe Engine Architecture

### 7.1 Algorithm (Ported from AnySoftKeyboard)

```
User swipe gesture
    │
    ▼
┌─────────────────┐
│ Collect Points  │ → Touch move events: [(x,y,t), ...]
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Filter Noise    │ → Remove points too close together
│                 │ → Apply curvature threshold
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Match Against   │ → For each word in dictionary:
│ Dictionary      │   - Generate expected path
│                 │   - Calculate distance to actual path
│                 │   - Apply direction penalty
│                 │   - Apply proximity penalty
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Rank Results    │ → Sort by distance score
│                 │ → Apply frequency boost
│                 │ → Return top-K
└─────────────────┘
```

### 7.2 Key Position Mapping

```
QWERTY Layout Key Centers:
  Q(80,50)  W(160,50)  E(240,50)  R(320,50)  T(400,50)
  A(120,120) S(200,120) D(280,120) F(360,120) G(440,120)
  Z(160,190) X(240,190) C(320,190) V(400,190) B(480,190)

Swipe path for "hello":
  H(400,120) → E(240,50) → L(480,190) → L(480,190) → O(520,50)
  Path: [(400,120), (240,50), (480,190), (480,190), (520,50)]
```

---

## 8. Export/Import Architecture

### 8.1 Export Format (JSON)

```json
{
  "schemaVersion": 1,
  "exportDate": "2026-08-21T12:00:00Z",
  "appVersion": "1.0.0",
  "device": "Pixel 8 Pro",
  
  "typingProfile": {
    "typingDNA": "a1b2c3d4...",
    "averageSpeed": 62,
    "totalKeystrokes": 142857,
    "commonErrors": { "teh": "the" },
    "topWords": ["the", "and", "you"],
    "timePatterns": {},
    "contextProfiles": {}
  },
  
  "predictions": {
    "unigrams": { "the": 4521 },
    "bigrams": { "going to": 891 },
    "customWords": ["nexType"]
  },
  
  "shortcuts": {
    "brw": "be right with you"
  },
  
  "themes": {
    "active": "akash-dark",
    "custom": []
  },
  
  "preferences": {
    "vibration": true,
    "sound": "mechanical"
  },
  
  "clipboard": []
}
```

### 8.2 Import Validation

```
Import JSON file
    │
    ▼
┌─────────────────┐
│ Parse JSON      │ → Validate JSON syntax
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Check Schema    │ → Validate schemaVersion
│ Version         │ → Migrate if older version
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Validate Data   │ → Check all required fields
│                 │ → Check data types
│                 │ → Check value ranges
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Merge or        │ → User chooses: merge with existing
│ Replace         │   or replace all data
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Write to        │ → Batch write to Room DB
│ Storage         │ → Update SharedPreferences
│                 │ → Reload prediction model
└─────────────────┘
```

---

## 9. Security Architecture

### 9.1 Threat Model

| Threat | Mitigation |
|--------|-----------|
| Network exfiltration | No INTERNET permission, no network code |
| Data theft (physical) | Optional encryption for sensitive data |
| Memory scraping | Rust memory safety, no raw pointers to Kotlin |
| JNI buffer overflow | Bounds checking on all JNI calls |
| Malicious theme JSON | Schema validation, sandboxed parsing |

### 9.2 Data Sensitivity Classification

| Data | Sensitivity | Storage | Encryption |
|------|------------|---------|------------|
| Typed text (real-time) | HIGH | Memory only | N/A (ephemeral) |
| User dictionary | HIGH | Room DB | Optional AES-256 |
| Clipboard history | HIGH | Room DB | Optional AES-256 |
| Prediction model | MEDIUM | File | Checksum only |
| Themes | LOW | File | None |
| Settings | LOW | SharedPrefs | None |
| Typing stats | LOW | Room DB | None |

---

## 10. Build Architecture

### 10.1 Build Pipeline

```
┌─────────────┐    ┌──────────────┐    ┌──────────────┐
│   Rust      │    │   Kotlin     │    │   Android    │
│   Engine    │    │   App        │    │   APK        │
│             │    │              │    │              │
│ cargo build │───→│ Gradle build │───→│ Final APK    │
│ --release   │    │              │    │              │
│ -t arm64    │    │ Bundles .so  │    │ <5MB         │
└─────────────┘    └──────────────┘    └──────────────┘
```

### 10.2 Rust Cross-Compilation

```toml
# engine/Cargo.toml
[package]
name = "akashboard-engine"
version = "0.1.0"
edition = "2021"

[lib]
crate-type = ["cdylib"]

[dependencies]
jni = "0.21"
serde = { version = "1", features = ["derive"] }
serde_json = "1"

[profile.release]
opt-level = "z"      # Optimize for size
lto = true           # Link-time optimization
codegen-units = 1    # Single codegen unit for max optimization
strip = true         # Strip debug symbols
```

```bash
# Build for ARM64
cargo ndk -t arm64-v8a build --release

# Output: engine/target/aarch64-linux-android/release/libpredictor.so (~35KB)
```

---

## Appendix A: Technology Decisions

| Decision | Choice | Alternative | Why |
|----------|--------|-------------|-----|
| IME language | Kotlin | Java | Null safety, coroutines, modern |
| UI rendering | Custom Canvas | XML Layouts | 60fps, full control |
| UI framework | None (raw) | Jetpack Compose | Compose adds ~2MB, too heavy |
| Prediction engine | Rust | Kotlin | 50x faster, smaller binary |
| JNI | Manual | Caffeine/Swiss | More control, no extra deps |
| Storage (structured) | Room | SQLite directly | Type safety, migration support |
| Storage (simple) | SharedPrefs | DataStore | Simpler, sufficient for settings |
| Swipe algorithm | AnySoftKeyboard port | Custom | Proven, no proprietary deps |
| Theming | Snygg-inspired JSON | XML styles | More flexible, shareable |
| Build system | Gradle + cargo-ndk | Bazel | Standard Android, simpler |
| Testing | JUnit + cargo test | Espresso | Fast, no device needed for unit tests |
