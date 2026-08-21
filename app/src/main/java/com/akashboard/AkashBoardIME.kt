/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AkashBoardIME.kt — Main IME service with prediction integration.
 *
 * Week 4: Connected to Rust prediction engine.
 *   - Initializes PredictorBridge on create
 *   - Updates suggestions after each key press
 *   - Accepts suggestions from SuggestionBar
 *   - Learns words on space/punctuation
 */

package com.akashboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.akashboard.core.InputHandler
import com.akashboard.core.HapticFeedback
import com.akashboard.core.KeyData
import com.akashboard.core.KeyboardLayoutType
import com.akashboard.core.ShiftState
import com.akashboard.engine.PredictorBridge
import com.akashboard.ui.KeyboardView
import com.akashboard.ui.SuggestionBar

/**
 * AkashBoard's IME service.
 *
 * Connects:
 *   KeyboardView → InputHandler → PredictorBridge → SuggestionBar
 *                                  ↓
 *                             InputConnection → Target App
 */
class AkashBoardIME : InputMethodService() {

    private var keyboardView: KeyboardView? = null
    private var suggestionBar: SuggestionBar? = null
    private lateinit var hapticFeedback: HapticFeedback
    private lateinit var inputHandler: InputHandler

    override fun onCreate() {
        super.onCreate()

        // Initialize Rust prediction engine
        PredictorBridge.init(filesDir.absolutePath)

        hapticFeedback = HapticFeedback(this)

        inputHandler = InputHandler(
            hapticFeedback = hapticFeedback,
            onLayoutChange = { layout -> handleLayoutChange(layout) },
            onSuggestionsUpdate = { suggestions -> handleSuggestionsUpdate(suggestions) }
        )
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
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
                    // Future: voice input
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

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
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

    // ── Layout & Suggestions ──────────────────────────────────────────────

    private fun handleLayoutChange(layout: KeyboardLayoutType) {
        keyboardView?.setLayout(layout)
    }

    private fun handleSuggestionsUpdate(suggestions: List<String>) {
        suggestionBar?.setSuggestions(suggestions)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
