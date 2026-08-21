/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AkashBoardIME.kt — Main IME with swipe typing support.
 *
 * Week 5: Integrated swipe gesture recognition.
 */

package com.akashboard

import android.inputmethodservice.InputMethodService
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.akashboard.core.InputHandler
import com.akashboard.core.HapticFeedback
import com.akashboard.core.KeyData
import com.akashboard.core.KeyboardLayoutType
import com.akashboard.engine.PredictorBridge
import com.akashboard.ui.KeyboardView
import com.akashboard.ui.SuggestionBar

class AkashBoardIME : InputMethodService() {

    private var keyboardView: KeyboardView? = null
    private var suggestionBar: SuggestionBar? = null
    private lateinit var hapticFeedback: HapticFeedback
    private lateinit var inputHandler: InputHandler

    override fun onCreate() {
        super.onCreate()
        PredictorBridge.init(filesDir.absolutePath)

        hapticFeedback = HapticFeedback(this)
        inputHandler = InputHandler(
            hapticFeedback = hapticFeedback,
            onLayoutChange = { layout -> keyboardView?.setLayout(layout) },
            onSuggestionsUpdate = { suggestions -> suggestionBar?.setSuggestions(suggestions) }
        )
    }

    override fun onCreateInputView(): android.view.View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val bar = SuggestionBar(this).apply {
            onSuggestionClickListener = object : SuggestionBar.OnSuggestionClickListener {
                override fun onSuggestionClicked(index: Int, word: String) {
                    inputHandler.acceptSuggestion(word)
                }
            }
            onMicClickListener = object : SuggestionBar.OnMicClickListener {
                override fun onClick() { /* Future: voice */ }
            }
        }
        suggestionBar = bar
        root.addView(bar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            48 * resources.displayMetrics.density.toInt()
        ))

        val keyboard = KeyboardView(this).apply {
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

        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        inputHandler.setInputConnection(currentInputConnection, info)
        keyboardView?.setShiftState(inputHandler.getShiftState())
    }

    override fun onDestroy() {
        PredictorBridge.destroy()
        keyboardView?.destroy()
        keyboardView = null
        suggestionBar = null
        super.onDestroy()
    }

    // ── Key Processing ────────────────────────────────────────────────────

    private fun handleKeyPress(key: KeyData) {
        val result = inputHandler.handleKeyPress(key)
        when (result) {
            is com.akashboard.core.KeyPressResult.ShiftChanged -> {
                keyboardView?.setShiftState(result.state)
            }
            is com.akashboard.core.KeyPressResult.LayoutChanged -> {
                keyboardView?.setLayout(result.layout)
            }
            else -> {
                keyboardView?.setShiftState(inputHandler.getShiftState())
            }
        }
    }

    // ── Swipe Processing ──────────────────────────────────────────────────

    private fun handleSwipeWord(word: String) {
        val connection = currentInputConnection ?: return

        // Delete current partial word if any
        val currentWord = inputHandler.getCurrentWord()
        if (currentWord.isNotEmpty()) {
            connection.deleteSurroundingText(currentWord.length, 0)
        }

        // Commit the swiped word + space
        connection.commitText("$word ", 1)
        hapticFeedback.selection()

        // Learn the swiped word
        val context = inputHandler.getContextForPrediction()
        PredictorBridge.learn(word, context, System.currentTimeMillis())
    }

    // ── Cursor Movement ───────────────────────────────────────────────────

    private fun handleCursorMove(deltaChars: Int) {
        val connection = currentInputConnection ?: return
        val text = connection.getExtractedText(
            android.view.inputmethod.ExtractedTextRequest(), 0
        ) ?: return
        val newStart = (text.selectionStart + deltaChars).coerceIn(0, text.text?.length ?: 0)
        val newEnd = (text.selectionEnd + deltaChars).coerceIn(0, text.text?.length ?: 0)
        connection.setSelection(newStart, newEnd)
    }
}
