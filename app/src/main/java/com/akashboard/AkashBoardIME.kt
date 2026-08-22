/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AkashBoardIME.kt — Main IME with comprehensive error handling.
 *
 * All initialization is wrapped in try-catch to prevent crashes.
 * The keyboard degrades gracefully if components fail.
 */

package com.akashboard

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.akashboard.analytics.TimeAwarePredictor
import com.akashboard.analytics.TypingDNA
import com.akashboard.analytics.TypingStats
import com.akashboard.core.InputHandler
import com.akashboard.core.HapticFeedback
import com.akashboard.core.KeyData
import com.akashboard.core.VoiceInput
import com.akashboard.data.ClipboardDB
import com.akashboard.data.ClipboardItem
import com.akashboard.engine.PredictorBridge
import com.akashboard.theme.ThemeManager
import com.akashboard.ui.ClipboardPanel
import com.akashboard.ui.EmojiPanel
import com.akashboard.ui.KeyboardView
import com.akashboard.ui.SuggestionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AkashBoardIME : InputMethodService() {

    companion object {
        private const val TAG = "AkashBoardIME"
    }

    private var keyboardView: KeyboardView? = null
    private var suggestionBar: SuggestionBar? = null
    private var emojiPanel: EmojiPanel? = null
    private var clipboardPanel: ClipboardPanel? = null
    private var voiceInput: VoiceInput? = null

    private var hapticFeedback: HapticFeedback? = null
    private var inputHandler: InputHandler? = null
    private var themeManager: ThemeManager? = null
    private var clipboardDB: ClipboardDB? = null
    private var typingStats: TypingStats? = null
    private var typingDNA: TypingDNA? = null
    private var timeAwarePredictor: TimeAwarePredictor? = null
    private var settingsProvider: com.akashboard.settings.KeyboardSettingsProvider? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private enum class PanelState { KEYBOARD, EMOJI, CLIPBOARD }
    private var currentPanel = PanelState.KEYBOARD

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        try {
            // Initialize Rust engine (optional — keyboard works without it)
            try {
                PredictorBridge.init(filesDir.absolutePath)
            } catch (e: Exception) {
                Log.w(TAG, "Rust engine init failed, predictions disabled", e)
            }

            // Initialize settings provider
            try {
                settingsProvider = com.akashboard.settings.KeyboardSettingsProvider(this)
            } catch (e: Exception) {
                Log.w(TAG, "Settings provider init failed", e)
            }

            // Initialize haptic feedback
            try {
                hapticFeedback = HapticFeedback(this)
                settingsProvider?.let { sp ->
                    hapticFeedback?.setEnabled(sp.vibrateOnKeypress)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Haptic feedback init failed", e)
            }

            // Initialize theme manager
            try {
                themeManager = ThemeManager(this)
                themeManager?.loadSavedTheme()
            } catch (e: Exception) {
                Log.w(TAG, "Theme manager init failed", e)
            }

            // Initialize clipboard database
            try {
                clipboardDB = ClipboardDB.getDatabase(this)
            } catch (e: Exception) {
                Log.w(TAG, "Clipboard DB init failed", e)
            }

            // Initialize analytics
            try {
                typingStats = TypingStats(this)
            } catch (e: Exception) {
                Log.w(TAG, "TypingStats init failed", e)
            }

            try {
                typingDNA = TypingDNA(this)
            } catch (e: Exception) {
                Log.w(TAG, "TypingDNA init failed", e)
            }

            try {
                timeAwarePredictor = TimeAwarePredictor(this)
                timeAwarePredictor?.loadPatterns()
            } catch (e: Exception) {
                Log.w(TAG, "TimeAwarePredictor init failed", e)
            }

            // Initialize input handler
            try {
                inputHandler = InputHandler(
                    hapticFeedback = hapticFeedback ?: HapticFeedback(this),
                    onLayoutChange = { layout -> keyboardView?.setLayout(layout) },
                    onSuggestionsUpdate = { suggestions -> suggestionBar?.setSuggestions(suggestions) }
                )
                // Wire typing settings
                settingsProvider?.let { sp ->
                    inputHandler?.setAutoCorrect(sp.autoCorrectEnabled)
                    inputHandler?.setIncognito(sp.incognitoMode)
                }
            } catch (e: Exception) {
                Log.e(TAG, "InputHandler init failed", e)
            }

            // Initialize voice input (optional)
            try {
                voiceInput = VoiceInput(this)
                voiceInput?.setListener(object : VoiceInput.OnVoiceInputListener {
                    override fun onPartialResult(text: String) {
                        suggestionBar?.setSuggestions(listOf(text))
                    }
                    override fun onResult(text: String) {
                        val connection = currentInputConnection ?: return
                        connection.commitText(text, 1)
                        hapticFeedback?.selection()
                        suggestionBar?.setSuggestions(emptyList())
                    }
                    override fun onError(error: String) {
                        Log.w(TAG, "Voice input error: $error")
                    }
                    override fun onStateChanged(state: VoiceInput.VoiceState) {
                        // Update UI state if needed
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG, "Voice input init failed (might need permissions)", e)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Critical init failure", e)
        }
    }

    override fun onCreateInputView(): View {
        Log.d(TAG, "onCreateInputView")

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(themeManager?.getColors()?.canvas ?: 0xFF111214.toInt())
        }

        // Suggestion bar
        try {
            val bar = SuggestionBar(this).apply {
                onSuggestionClickListener = object : SuggestionBar.OnSuggestionClickListener {
                    override fun onSuggestionClicked(index: Int, word: String) {
                        inputHandler?.acceptSuggestion(word)
                    }
                }
                onMicClickListener = object : SuggestionBar.OnMicClickListener {
                    override fun onClick() {
                        try {
                            voiceInput?.startListening()
                        } catch (e: Exception) {
                            Log.w(TAG, "Voice start failed", e)
                        }
                    }
                }
            }
            suggestionBar = bar
            root.addView(bar, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                48 * resources.displayMetrics.density.toInt()
            ))
        } catch (e: Exception) {
            Log.e(TAG, "SuggestionBar creation failed", e)
        }

        // Keyboard view
        try {
            val keyboard = KeyboardView(this).apply {
                themeManager?.getColors()?.let { setThemeColors(it) }
                onKeyPressedListener = object : KeyboardView.OnKeyPressedListener {
                    override fun onKeyPressed(key: KeyData) {
                        this@AkashBoardIME.handleKeyPress(key)
                    }
                }
                onCursorMoveListener = object : KeyboardView.OnCursorMoveListener {
                    override fun onCursorMove(deltaChars: Int) {
                        this@AkashBoardIME.handleCursorMove(deltaChars)
                    }
                }
                onSwipeListener = object : KeyboardView.OnSwipeListener {
                    override fun onSwipeCompleted(word: String) {
                        this@AkashBoardIME.handleSwipeWord(word)
                    }
                }
            }
            keyboardView = keyboard
            root.addView(keyboard, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        } catch (e: Exception) {
            Log.e(TAG, "KeyboardView creation failed", e)
        }

        // Emoji panel
        try {
            val emoji = EmojiPanel(this).apply {
                onEmojiClickListener = object : EmojiPanel.OnEmojiClickListener {
                    override fun onEmojiClicked(emoji: String) {
                        currentInputConnection?.commitText(emoji, 1)
                        hapticFeedback?.keyPress()
                    }
                }
            }
            emojiPanel = emoji
            root.addView(emoji, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300 * resources.displayMetrics.density.toInt()
            ))
        } catch (e: Exception) {
            Log.e(TAG, "EmojiPanel creation failed", e)
        }

        // Clipboard panel
        try {
            val clipboard = ClipboardPanel(this).apply {
                onItemClickListener = object : ClipboardPanel.OnItemClickListener {
                    override fun onItemClicked(item: ClipboardItem) {
                        currentInputConnection?.commitText(item.text, 1)
                        hapticFeedback?.selection()
                        showPanel(PanelState.KEYBOARD)
                    }
                }
            }
            clipboardPanel = clipboard
            root.addView(clipboard, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300 * resources.displayMetrics.density.toInt()
            ))
        } catch (e: Exception) {
            Log.e(TAG, "ClipboardPanel creation failed", e)
        }

        // Start with keyboard visible
        showPanel(PanelState.KEYBOARD)

        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        try {
            inputHandler?.setInputConnection(currentInputConnection, info)
            keyboardView?.setShiftState(inputHandler?.getShiftState() ?: com.akashboard.core.ShiftState.NONE)
            typingStats?.startSession()

            // Wire ALL settings to keyboard components
            settingsProvider?.let { sp ->
                inputHandler?.setAutoCorrect(sp.autoCorrectEnabled)
                inputHandler?.setPredictiveText(sp.predictiveTextEnabled)
                inputHandler?.setIncognito(sp.incognitoMode)
                inputHandler?.learningEnabled = sp.learningEnabled
                keyboardView?.updateRepeatTiming(sp.keyRepeatDelay.toLong(), sp.keyRepeatRate.toLong())
                keyboardView?.setCornerRadius(sp.keyCornerRadius)
                keyboardView?.setKeySpacing(sp.keySpacing)
                keyboardView?.setKeyboardHeight(sp.keyboardHeight)
                keyboardView?.swipeTypingEnabled = sp.swipeTypingEnabled
                keyboardView?.spacebarCursorEnabled = sp.spacebarCursorEnabled
                keyboardView?.longPressRepeatEnabled = sp.longPressRepeatEnabled
                hapticFeedback?.setEnabled(sp.vibrateOnKeypress)
            }

            // Load clipboard items (respects max-items + history toggle)
            val db = clipboardDB
            if (db != null) {
                scope.launch(Dispatchers.IO) {
                    try {
                        val max = settingsProvider?.clipboardMaxItems ?: 50
                        val items = db.clipboardDao().getItems(max)
                        scope.launch(Dispatchers.Main) {
                            clipboardPanel?.setItems(items)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load clipboard", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onStartInputView failed", e)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        try {
            typingStats?.endSession()
            voiceInput?.stopListening()
        } catch (e: Exception) {
            Log.w(TAG, "onFinishInputView cleanup failed", e)
        }
    }

    override fun onDestroy() {
        try {
            scope.cancel()
            voiceInput?.destroy()
            PredictorBridge.destroy()
            keyboardView?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "onDestroy cleanup failed", e)
        }
        keyboardView = null
        suggestionBar = null
        emojiPanel = null
        clipboardPanel = null
        voiceInput = null
        clipboardDB = null
        super.onDestroy()
    }

    private fun showPanel(panel: PanelState) {
        currentPanel = panel

        keyboardView?.visibility = if (panel == PanelState.KEYBOARD) View.VISIBLE else View.GONE
        suggestionBar?.visibility = if (panel == PanelState.KEYBOARD) View.VISIBLE else View.GONE
        emojiPanel?.visibility = if (panel == PanelState.EMOJI) View.VISIBLE else View.GONE
        clipboardPanel?.visibility = if (panel == PanelState.CLIPBOARD) View.VISIBLE else View.GONE

        if (panel == PanelState.KEYBOARD) {
            keyboardView?.setShiftState(inputHandler?.getShiftState() ?: com.akashboard.core.ShiftState.NONE)
        }
    }

    private fun handleKeyPress(key: KeyData) {
        try {
            if (currentPanel != PanelState.KEYBOARD) {
                showPanel(PanelState.KEYBOARD)
            }

            // Track analytics
            typingDNA?.onKeyPressed(key.label.firstOrNull() ?: ' ', System.currentTimeMillis())

            val handler = inputHandler ?: return
            val result = handler.handleKeyPress(key)

            when (result) {
                is com.akashboard.core.KeyPressResult.ShiftChanged -> keyboardView?.setShiftState(result.state)
                is com.akashboard.core.KeyPressResult.LayoutChanged -> keyboardView?.setLayout(result.layout)
                is com.akashboard.core.KeyPressResult.Emoji -> showPanel(PanelState.EMOJI)
                is com.akashboard.core.KeyPressResult.Clipboard -> {
                    val db = clipboardDB
                    if (db != null) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val max = settingsProvider?.clipboardMaxItems ?: 50
                                val items = db.clipboardDao().getItems(max)
                                scope.launch(Dispatchers.Main) {
                                    clipboardPanel?.setItems(items)
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to load clipboard for panel", e)
                            }
                        }
                    }
                    showPanel(PanelState.CLIPBOARD)
                }
                is com.akashboard.core.KeyPressResult.WordCompleted -> {
                    typingStats?.onWordCompleted(result.word)
                    timeAwarePredictor?.learnWord(result.word, packageName)
                }
                else -> keyboardView?.setShiftState(handler.getShiftState())
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleKeyPress failed", e)
        }
    }

    private fun handleSwipeWord(word: String) {
        try {
            val connection = currentInputConnection ?: return
            val handler = inputHandler ?: return
            val currentWord = handler.getCurrentWord()
            if (currentWord.isNotEmpty()) connection.deleteSurroundingText(currentWord.length, 0)
            connection.commitText("$word ", 1)
            hapticFeedback?.selection()
            val context = handler.getContextForPrediction()
            PredictorBridge.learn(word, context, System.currentTimeMillis())

            typingStats?.onWordCompleted(word)
            timeAwarePredictor?.learnWord(word, packageName)
        } catch (e: Exception) {
            Log.e(TAG, "handleSwipeWord failed", e)
        }
    }

    private fun handleCursorMove(deltaChars: Int) {
        try {
            val connection = currentInputConnection ?: return
            val text = connection.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0) ?: return
            val newStart = (text.selectionStart + deltaChars).coerceIn(0, text.text?.length ?: 0)
            val newEnd = (text.selectionEnd + deltaChars).coerceIn(0, text.text?.length ?: 0)
            connection.setSelection(newStart, newEnd)
        } catch (e: Exception) {
            Log.e(TAG, "handleCursorMove failed", e)
        }
    }
}
