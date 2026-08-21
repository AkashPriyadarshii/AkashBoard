/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AkashBoardIME.kt — Main IME with emoji, clipboard, and voice.
 *
 * Week 8: Integrates EmojiPanel, ClipboardPanel, VoiceInput.
 */

package com.akashboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
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

    private var keyboardView: KeyboardView? = null
    private var suggestionBar: SuggestionBar? = null
    private var emojiPanel: EmojiPanel? = null
    private var clipboardPanel: ClipboardPanel? = null
    private var voiceInput: VoiceInput? = null

    private lateinit var hapticFeedback: HapticFeedback
    private lateinit var inputHandler: InputHandler
    private lateinit var themeManager: ThemeManager
    private lateinit var clipboardDB: ClipboardDB
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private enum class PanelState { KEYBOARD, EMOJI, CLIPBOARD }
    private var currentPanel = PanelState.KEYBOARD

    override fun onCreate() {
        super.onCreate()
        PredictorBridge.init(filesDir.absolutePath)

        hapticFeedback = HapticFeedback(this)
        themeManager = ThemeManager(this)
        themeManager.loadSavedTheme()
        clipboardDB = ClipboardDB.getDatabase(this)

        inputHandler = InputHandler(
            hapticFeedback = hapticFeedback,
            onLayoutChange = { layout -> keyboardView?.setLayout(layout) },
            onSuggestionsUpdate = { suggestions -> suggestionBar?.setSuggestions(suggestions) }
        )

        voiceInput = VoiceInput(this)
        voiceInput?.setListener(object : VoiceInput.OnVoiceInputListener {
            override fun onPartialResult(text: String) {
                // Show partial results in suggestion bar
                suggestionBar?.setSuggestions(listOf(text))
            }

            override fun onResult(text: String) {
                val connection = currentInputConnection ?: return
                connection.commitText(text, 1)
                hapticFeedback.selection()
                suggestionBar?.setSuggestions(emptyList())
            }

            override fun onError(error: String) {
                // Could show a toast or status
            }

            override fun onStateChanged(state: VoiceInput.VoiceState) {
                // Update UI state if needed
            }
        })
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(themeManager.getColors().canvas)
        }

        // Suggestion bar
        val bar = SuggestionBar(this).apply {
            onSuggestionClickListener = object : SuggestionBar.OnSuggestionClickListener {
                override fun onSuggestionClicked(index: Int, word: String) {
                    inputHandler.acceptSuggestion(word)
                }
            }
            onMicClickListener = object : SuggestionBar.OnMicClickListener {
                override fun onClick() {
                    voiceInput?.startListening()
                }
            }
        }
        suggestionBar = bar
        root.addView(bar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            48 * resources.displayMetrics.density.toInt()
        ))

        // Keyboard view
        val keyboard = KeyboardView(this).apply {
            setThemeColors(themeManager.getColors())
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

        // Emoji panel
        val emoji = EmojiPanel(this).apply {
            onEmojiClickListener = object : EmojiPanel.OnEmojiClickListener {
                override fun onEmojiClicked(emoji: String) {
                    currentInputConnection?.commitText(emoji, 1)
                    hapticFeedback.keyPress()
                }
            }
        }
        emojiPanel = emoji
        root.addView(emoji, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            300 * resources.displayMetrics.density.toInt()
        ))

        // Clipboard panel
        val clipboard = ClipboardPanel(this).apply {
            onItemClickListener = object : ClipboardPanel.OnItemClickListener {
                override fun onItemClicked(item: ClipboardItem) {
                    currentInputConnection?.commitText(item.text, 1)
                    hapticFeedback.selection()
                    showPanel(PanelState.KEYBOARD)
                }
            }
        }
        clipboardPanel = clipboard
        root.addView(clipboard, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            300 * resources.displayMetrics.density.toInt()
        ))

        // Start with keyboard visible
        showPanel(PanelState.KEYBOARD)

        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        inputHandler.setInputConnection(currentInputConnection, info)
        keyboardView?.setShiftState(inputHandler.getShiftState())

        // Load clipboard items
        scope.launch(Dispatchers.IO) {
            val items = clipboardDB.clipboardDao().getItems(20)
            scope.launch(Dispatchers.Main) {
                clipboardPanel?.setItems(items)
            }
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        voiceInput?.stopListening()
    }

    override fun onDestroy() {
        scope.cancel()
        PredictorBridge.destroy()
        keyboardView?.destroy()
        keyboardView = null
        suggestionBar = null
        emojiPanel = null
        clipboardPanel = null
        voiceInput = null
        super.onDestroy()
    }

    private fun showPanel(panel: PanelState) {
        currentPanel = panel

        keyboardView?.visibility = if (panel == PanelState.KEYBOARD) View.VISIBLE else View.GONE
        suggestionBar?.visibility = if (panel == PanelState.KEYBOARD) View.VISIBLE else View.GONE
        emojiPanel?.visibility = if (panel == PanelState.EMOJI) View.VISIBLE else View.GONE
        clipboardPanel?.visibility = if (panel == PanelState.CLIPBOARD) View.VISIBLE else View.GONE

        if (panel == PanelState.KEYBOARD) {
            keyboardView?.setShiftState(inputHandler.getShiftState())
        }
    }

    private fun handleKeyPress(key: KeyData) {
        if (currentPanel != PanelState.KEYBOARD) {
            showPanel(PanelState.KEYBOARD)
        }

        val result = inputHandler.handleKeyPress(key)
        when (result) {
            is com.akashboard.core.KeyPressResult.ShiftChanged -> keyboardView?.setShiftState(result.state)
            is com.akashboard.core.KeyPressResult.LayoutChanged -> keyboardView?.setLayout(result.layout)
            is com.akashboard.core.KeyPressResult.Emoji -> showPanel(PanelState.EMOJI)
            is com.akashboard.core.KeyPressResult.Clipboard -> {
                scope.launch(Dispatchers.IO) {
                    val items = clipboardDB.clipboardDao().getItems(20)
                    scope.launch(Dispatchers.Main) {
                        clipboardPanel?.setItems(items)
                    }
                }
                showPanel(PanelState.CLIPBOARD)
            }
            else -> keyboardView?.setShiftState(inputHandler.getShiftState())
        }
    }

    private fun handleSwipeWord(word: String) {
        val connection = currentInputConnection ?: return
        val currentWord = inputHandler.getCurrentWord()
        if (currentWord.isNotEmpty()) connection.deleteSurroundingText(currentWord.length, 0)
        connection.commitText("$word ", 1)
        hapticFeedback.selection()
        val context = inputHandler.getContextForPrediction()
        PredictorBridge.learn(word, context, System.currentTimeMillis())
    }

    private fun handleCursorMove(deltaChars: Int) {
        val connection = currentInputConnection ?: return
        val text = connection.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0) ?: return
        val newStart = (text.selectionStart + deltaChars).coerceIn(0, text.text?.length ?: 0)
        val newEnd = (text.selectionEnd + deltaChars).coerceIn(0, text.text?.length ?: 0)
        connection.setSelection(newStart, newEnd)
    }
}
