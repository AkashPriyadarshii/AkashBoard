# API.md — Internal API Reference

## AkashBoard v1.0

**Author:** Akash Priyadarshi
**Date:** August 21, 2026
**Status:** Draft

---

## 1. Overview

This document describes the internal APIs used across AkashBoard's layers:
- **Kotlin → Rust** (JNI bridge)
- **UI → Core** (input processing)
- **Core → Engine** (prediction requests)
- **Data Layer** (storage operations)

---

## 2. JNI Bridge API (Kotlin ↔ Rust)

### 2.1 PredictorBridge

**File:** `app/src/main/java/com/akashboard/engine/PredictorBridge.kt`

```kotlin
class PredictorBridge {
    companion object {
        init { System.loadLibrary("predictor") }
        
        // Initialize the prediction engine
        external fun nativeInit(configPath: String)
        
        // Destroy the prediction engine (free resources)
        external fun nativeDestroy()
        
        // Predict next words given context
        // Returns: Array of suggested words (max 5)
        external fun nativePredict(context: String, topK: Int): Array<String>
        
        // Get word completion for partial word
        // Returns: Array of completions
        external fun nativeComplete(partial: String, topK: Int): Array<String>
        
        // Auto-correct a misspelled word
        // Returns: Corrected word (or original if no correction)
        external fun nativeCorrect(word: String, context: String): String
        
        // Learn a new word/pattern
        // Returns: true if learned, false if rejected
        external fun nativeLearn(word: String, context: String, timestamp: Long): Boolean
        
        // Recognize a swipe gesture
        // path: [x1,y1, x2,y2, ...] flattened array
        // keyPositions: [key1_x, key1_y, key1_w, key1_h, ...] flattened array
        // Returns: Array of matching words (max 5)
        external fun nativeRecognizeSwipe(
            path: FloatArray,
            keyPositions: FloatArray,
            topK: Int
        ): Array<String>
        
        // Detect sentiment of text
        // Returns: Float from -1.0 (negative) to 1.0 (positive)
        external fun nativeDetectMood(text: String): Float
        
        // Save model to disk
        // Returns: true if saved successfully
        external fun nativeSaveModel(): Boolean
        
        // Load model from disk
        // Returns: true if loaded successfully
        external fun nativeLoadModel(): Boolean
        
        // Get current storage size in bytes
        external fun nativeGetStorageSize(): Long
        
        // Prune old patterns (call periodically)
        external fun nativePrune(maxAgeDays: Int)
        
        // Clear all learned data
        external fun nativeClearAll()
    }
}
```

### 2.2 Rust JNI Functions

**File:** `engine/src/lib.rs`

```rust
use jni::JNIEnv;
use jni::objects::{JClass, JString, JFloatArray};
use jni::sys::{jboolean, jfloat, jstring, jint, jlong};

// JNI-called functions follow naming convention:
// Java_com_akashboard_engine_PredictorBridge_<methodName>

#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeInit(
    mut env: JNIEnv,
    _class: JClass,
    config_path: JString,
) { ... }

#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativePredict(
    mut env: JNIEnv,
    _class: JClass,
    context: JString,
    top_k: jint,
) -> jstring { ... }

// ... (all other JNI functions)
```

---

## 3. UI Component APIs

### 3.1 KeyboardView

**File:** `app/src/main/java/com/akashboard/ui/KeyboardView.kt`

```kotlin
class KeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Load a keyboard layout
    fun loadLayout(layout: KeyboardLayout)
    
    // Set shift state
    fun setShiftState(state: ShiftState)
    
    // Get current layout
    fun getCurrentLayout(): KeyboardLayout
    
    // Enable/disable key sounds
    fun setSoundEnabled(enabled: Boolean)
    
    // Set sound pack
    fun setSoundPack(pack: SoundPack)
    
    // Set haptic feedback enabled
    fun setHapticEnabled(enabled: Boolean)
    
    // Listener for key events
    interface OnKeyPressedListener {
        fun onKeyPressed(key: KeyData)
        fun onKeyLongPressed(key: KeyData)
        fun onSwipeStarted(startX: Float, startY: Float)
        fun onSwipeMoved(currentX: Float, currentY: Float)
        fun onSwipeEnded(points: List<Point>)
    }
    
    var onKeyPressedListener: OnKeyPressedListener? = null
}
```

### 3.2 SuggestionBar

**File:** `app/src/main/java/com/akashboard/ui/SuggestionBar.kt`

```kotlin
class SuggestionBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Set suggestions to display
    fun setSuggestions(suggestions: List<Suggestion>)
    
    // Clear all suggestions
    fun clearSuggestions()
    
    // Show/hide voice input button
    fun setVoiceButtonVisible(visible: Boolean)
    
    // Listener for suggestion events
    interface OnSuggestionListener {
        fun onSuggestionTapped(suggestion: Suggestion, index: Int)
        fun onSuggestionSwiped(suggestion: Suggestion, direction: SwipeDirection)
        fun onVoiceButtonTapped()
    }
    
    var onSuggestionListener: OnSuggestionListener? = null
}

data class Suggestion(
    val text: String,
    val confidence: Float,    // 0.0 to 1.0
    val source: SuggestionSource
)

enum class SuggestionSource {
    PREDICTION,    // From n-gram model
    COMPLETION,    // Word completion
    CORRECTION,    // Auto-correction
    SHORTCUT       // Text shortcut expansion
}
```

### 3.3 EmojiPanel

**File:** `app/src/main/java/com/akashboard/ui/EmojiPanel.kt`

```kotlin
class EmojiPanel @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Load emoji categories
    fun loadCategories(categories: List<EmojiCategory>)
    
    // Switch to category
    fun switchCategory(categoryId: String)
    
    // Show recently used emojis
    fun showRecent()
    
    // Search emojis
    fun search(query: String)
    
    // Listener
    interface OnEmojiListener {
        fun onEmojiTapped(emoji: EmojiData)
        fun onEmojiLongPressed(emoji: EmojiData)  // For skin tone variants
    }
    
    var onEmojiListener: OnEmojiListener? = null
}

data class EmojiData(
    val character: String,
    val name: String,
    val category: String,
    val keywords: List<String>
)
```

### 3.4 ClipboardPanel

**File:** `app/src/main/java/com/akashboard/ui/ClipboardPanel.kt`

```kotlin
class ClipboardPanel @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Load clipboard items
    fun setItems(items: List<ClipboardItemData>)
    
    // Add new item
    fun addItem(item: ClipboardItemData)
    
    // Remove item
    fun removeItem(itemId: Long)
    
    // Clear all
    fun clearAll()
    
    // Listener
    interface OnClipboardListener {
        fun onItemTapped(item: ClipboardItemData)
        fun onItemPinned(item: ClipboardItemData)
        fun onItemDeleted(item: ClipboardItemData)
    }
    
    var onClipboardListener: OnClipboardListener? = null
}

data class ClipboardItemData(
    val id: Long,
    val text: String,
    val timestamp: Long,
    val isPinned: Boolean,
    val sourceApp: String?
)
```

---

## 4. Core Layer APIs

### 4.1 InputHandler

**File:** `app/src/main/java/com/akashboard/core/InputHandler.kt`

```kotlin
class InputHandler(
    private val inputConnection: InputConnection,
    private val predictorBridge: PredictorBridge,
    private val hapticFeedback: HapticFeedback
) {
    private val wordComposer = WordComposer()
    
    // Process a key press
    fun handleKeyPress(key: KeyData)
    
    // Process a swipe gesture
    fun handleSwipe(points: List<Point>)
    
    // Get current word being composed
    fun getCurrentWord(): String
    
    // Get context (recent words)
    fun getContext(): String
    
    // Accept a suggestion
    fun acceptSuggestion(suggestion: Suggestion)
    
    // Toggle shift state
    fun toggleShift()
    
    // Reset state (called on new input field)
    fun reset()
    
    // Set autocorrect enabled/disabled
    fun setAutocorrectEnabled(enabled: Boolean)
    
    // Set incognito mode
    fun setIncognitoMode(enabled: Boolean)
}
```

### 4.2 WordComposer

**File:** `app/src/main/java/com/akashboard/core/WordComposer.kt`

```kotlin
class WordComposer {
    // Current word being typed
    val currentWord: String
    
    // Position of cursor within current word
    val cursorPosition: Int
    
    // Whether shift is active
    val isShifted: Boolean
    
    // Add a character to current word
    fun addCharacter(char: Char)
    
    // Delete last character
    fun deleteLast()
    
    // Finish current word (space, punctuation, etc.)
    fun finishWord()
    
    // Accept a suggestion (replace current word)
    fun acceptSuggestion(word: String)
    
    // Get the full context (recent words)
    fun getContext(): String
    
    // Reset state
    fun reset()
}
```

### 4.3 ThemeManager

**File:** `app/src/main/java/com/akashboard/theme/ThemeManager.kt`

```kotlin
class ThemeManager(private val context: Context) {
    
    // Get current theme
    fun getCurrentTheme(): ThemeConfig
    
    // Apply a theme by name
    fun applyTheme(themeName: String)
    
    // Get all available themes
    fun getAvailableThemes(): List<ThemeConfig>
    
    // Get built-in themes
    fun getBuiltinThemes(): List<ThemeConfig>
    
    // Get custom themes (user-created)
    fun getCustomThemes(): List<ThemeConfig>
    
    // Save a custom theme
    fun saveCustomTheme(theme: ThemeConfig): Boolean
    
    // Delete a custom theme
    fun deleteCustomTheme(themeName: String): Boolean
    
    // Export theme as JSON string
    fun exportTheme(theme: ThemeConfig): String
    
    // Import theme from JSON string
    fun importTheme(json: String): ThemeConfig?
    
    // Apply per-app tint
    fun applyPerAppTint(packageName: String)
    
    // Reset per-app tint
    fun resetPerAppTint()
    
    // Listener
    interface OnThemeChangedListener {
        fun onThemeChanged(newTheme: ThemeConfig)
    }
    
    fun addThemeChangedListener(listener: OnThemeChangedListener)
    fun removeThemeChangedListener(listener: OnThemeChangedListener)
}
```

### 4.4 ClipboardManager

**File:** `app/src/main/java/com/akashboard/data/ClipboardManager.kt`

```kotlin
class ClipboardManager(private val context: Context) {
    
    // Get all clipboard items
    fun getItems(): Flow<List<ClipboardItemData>>
    
    // Get pinned items only
    fun getPinnedItems(): Flow<List<ClipboardItemData>>
    
    // Add item to clipboard
    suspend fun addItem(text: String, sourceApp: String?)
    
    // Pin/unpin item
    suspend fun togglePin(itemId: Long)
    
    // Delete item
    suspend fun deleteItem(itemId: Long)
    
    // Clear all unpinned items
    suspend fun clearAll()
    
    // Nuclear delete (everything)
    suspend fun nukeAll()
    
    // Search clipboard
    suspend fun search(query: String): List<ClipboardItemData>
    
    // Get storage size in bytes
    suspend fun getStorageSize(): Long
    
    // Max items limit
    companion object {
        const val MAX_ITEMS = 50
        const val MAX_STORAGE_BYTES = 300 * 1024  // 300KB
    }
}
```

### 4.5 DataManager

**File:** `app/src/main/java/com/akashboard/data/DataManager.kt`

```kotlin
class DataManager(private val context: Context) {
    
    // Export all data to JSON
    suspend fun exportAll(): ExportData
    
    // Import data from JSON
    suspend fun importAll(data: ExportData, mode: ImportMode)
    
    // Get storage breakdown
    suspend fun getStorageBreakdown(): StorageBreakdown
    
    // Get total storage size
    suspend fun getTotalStorageSize(): Long
    
    // Backup to file
    suspend fun backupToFile(file: Uri)
    
    // Restore from file
    suspend fun restoreFromFile(file: Uri, mode: ImportMode)
}

data class ExportData(
    val schemaVersion: Int,
    val exportDate: String,
    val appVersion: String,
    val typingProfile: TypingProfile,
    val predictions: PredictionData,
    val shortcuts: Map<String, String>,
    val themes: ThemeData,
    val preferences: Map<String, Any>,
    val clipboard: List<ClipboardItemData>
)

enum class ImportMode {
    MERGE,      // Add to existing data
    REPLACE     // Replace all data
}

data class StorageBreakdown(
    val predictions: Long,
    val clipboard: Long,
    val themes: Long,
    val settings: Long,
    val total: Long
)
```

### 4.6 VoiceInput

**File:** `app/src/main/java/com/akashboard/core/VoiceInput.kt`

```kotlin
class VoiceInput(private val context: Context) {
    
    // Start voice recognition
    fun startListening()
    
    // Stop voice recognition
    fun stopListening()
    
    // Check if voice input is available
    fun isAvailable(): Boolean
    
    // Listener
    interface OnVoiceInputListener {
        fun onPartialResult(text: String)
        fun onResult(text: String)
        fun onError(error: String)
        fun onStateChanged(state: VoiceState)
    }
    
    var onVoiceInputListener: OnVoiceInputListener? = null
}

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    ERROR
}
```

---

## 5. Data Model APIs

### 5.1 KeyData

```kotlin
data class KeyData(
    val id: String,              // Unique key identifier
    val label: String,           // Display label (e.g., "Q")
    val code: Int,               // Key code (e.g., KEYCODE_Q)
    val rect: RectF,             // Bounding rectangle
    val type: KeyType,           // Letter, shift, delete, space, etc.
    val popupLabel: String?,     // Long-press popup (e.g., "1" for "Q")
    val popupCode: Int?,         // Long-press key code
    val width: Int,              // Width in dp (standard = 1)
    val weight: Float = 1.0f     // Usage weight (for adaptive sizing)
)

enum class KeyType {
    LETTER,
    SHIFT,
    DELETE,
    SPACE,
    ENTER,
    SYMBOL切换,
    EMOJI,
    VOICE,
    GLOBE,          // Language switch
    COMMA,
    PERIOD
}
```

### 5.2 KeyboardLayout

```kotlin
data class KeyboardLayout(
    val id: String,
    val name: String,
    val language: String,
    val rows: List<KeyRow>
)

data class KeyRow(
    val keys: List<KeyData>
)
```

### 5.3 ThemeConfig

```kotlin
data class ThemeConfig(
    val name: String,
    val version: Int = 1,
    val author: String = "",
    val colors: ColorConfig,
    val dimensions: DimensionConfig,
    val animation: AnimationConfig
)

data class ColorConfig(
    val background: Long,
    val keyBackground: Long,
    val keyPressed: Long,
    val keyBorder: Long,
    val keyText: Long,
    val suggestionBar: Long,
    val suggestionText: Long,
    val suggestionHighlight: Long,
    val accent: Long
)

data class DimensionConfig(
    val cornerRadius: Float = 8f,
    val keyElevation: Float = 2f,
    val keyPadding: Float = 4f,
    val suggestionBarHeight: Float = 48f
)

data class AnimationConfig(
    val pressScale: Float = 0.92f,
    val pressDuration: Long = 80,
    val rippleColor: Long,
    val transitionDuration: Long = 200
)
```

---

## 6. Error Handling

### 6.1 JNI Error Protocol

All JNI functions follow this error protocol:

```
Success → Return result (non-null)
Failure → Return empty/null (never throw across JNI boundary)
```

```kotlin
// Kotlin side
val suggestions = predictorBridge.nativePredict("hello world", 3)
if (suggestions.isEmpty()) {
    // Engine not initialized or error occurred
    // Fall back to empty suggestions
}
```

```rust
// Rust side
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativePredict(
    mut env: JNIEnv,
    _class: JClass,
    context: JString,
    top_k: jint,
) -> jstring {
    // Never panic! Return empty string on error
    let context = match env.get_string(&context) {
        Ok(s) => s.into(),
        Err(_) => return env.new_string("").unwrap().into_raw(),
    };
    
    let predictions = predictor.predict(&context, top_k as usize);
    let result = predictions.join(",");
    
    match env.new_string(&result) {
        Ok(s) => s.into_raw(),
        Err(_) => env.new_string("").unwrap().into_raw(),
    }
}
```

### 6.2 Kotlin Error Handling

```kotlin
// Use sealed classes for error handling
sealed class InputResult {
    data class Success(val text: String) : InputResult()
    data class Error(val message: String, val cause: Throwable?) : InputResult()
}

// Use Result type for suspend functions
suspend fun exportData(): Result<ExportData> = withContext(Dispatchers.IO) {
    try {
        val data = DataManager.exportAll()
        Result.success(data)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 7. Event System

### 7.1 Keyboard Events

```kotlin
// Sealed class for all keyboard events
sealed class KeyboardEvent {
    data class KeyPressed(val key: KeyData) : KeyboardEvent()
    data class KeyLongPressed(val key: KeyData) : KeyboardEvent()
    data class SwipeStarted(val x: Float, val y: Float) : KeyboardEvent()
    data class SwipeMoved(val x: Float, val y: Float) : KeyboardEvent()
    data class SwipeEnded(val points: List<Point>) : KeyboardEvent()
    data class SuggestionAccepted(val suggestion: Suggestion) : KeyboardEvent()
    data class SuggestionDismissed(val suggestion: Suggestion) : KeyboardEvent()
    data class LayoutChanged(val newLayout: KeyboardLayout) : KeyboardEvent()
    data class ThemeChanged(val newTheme: ThemeConfig) : KeyboardEvent()
    data class LanguageChanged(val newLanguage: String) : KeyboardEvent()
}
```

### 7.2 Event Flow

```
User Action → KeyboardView → InputHandler → Engine → SuggestionBar
     │              │              │           │          │
     │     TouchEvent      KeyPressed    predict()  setSuggestions()
     │              │              │           │          │
     └──────────────┴──────────────┴───────────┴──────────┘
                           EventBus (optional)
```

---

## 8. Configuration

### 8.1 Settings Keys

```kotlin
object SettingsKeys {
    // Input
    const val AUTO_CORRECT = "auto_correct"
    const val NEXT_WORD_PREDICTION = "next_word_prediction"
    const val SWIPE_TYPING = "swipe_typing"
    const val HAPTIC_FEEDBACK = "haptic_feedback"
    const val SOUND_ENABLED = "sound_enabled"
    const val SOUND_PACK = "sound_pack"
    
    // Appearance
    const val ACTIVE_THEME = "active_theme"
    const val KEY_SIZE = "key_size"
    const val SHOW_NUMBER_ROW = "show_number_row"
    const val PER_APP_TINT = "per_app_tint"
    
    // Privacy
    const val INCOGNITO_MODE = "incognito_mode"
    const val LEARNING_ENABLED = "learning_enabled"
    
    // Advanced
    const val VIBRATION_DURATION = "vibration_duration"
    const val KEY_REPEAT_DELAY = "key_repeat_delay"
    const val KEY_REPEAT_RATE = "key_repeat_rate"
}
```

### 8.2 Default Values

```kotlin
object Defaults {
    const val AUTO_CORRECT = true
    const val NEXT_WORD_PREDICTION = true
    const val SWIPE_TYPING = true
    const val HAPTIC_FEEDBACK = true
    const val SOUND_ENABLED = false
    const val SOUND_PACK = "mechanical"
    const val ACTIVE_THEME = "akash-dark"
    const val KEY_SIZE = 1.0f
    const val SHOW_NUMBER_ROW = false
    const val PER_APP_TINT = true
    const val INCOGNITO_MODE = false
    const val LEARNING_ENABLED = true
    const val VIBRATION_DURATION = 15L
    const val KEY_REPEAT_DELAY = 300L
    const val KEY_REPEAT_RATE = 50L
}
```
