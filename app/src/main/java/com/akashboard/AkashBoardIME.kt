/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AkashBoardIME.kt — Main Input Method Editor service.
 *
 * Week 3 additions:
 *   - Cursor movement via spacebar swipe
 *   - Long-press repeat (backspace)
 *   - Popup preview (alternate characters)
 */

package com.akashboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.akashboard.core.InputHandler
import com.akashboard.core.HapticFeedback
import com.akashboard.core.KeyData
import com.akashboard.core.KeyboardLayoutType
import com.akashboard.core.ShiftState
import com.akashboard.ui.KeyboardView

/**
 * AkashBoard's IME service.
 */
class AkashBoardIME : InputMethodService() {

    private var keyboardView: KeyboardView? = null
    private lateinit var hapticFeedback: HapticFeedback
    private lateinit var inputHandler: InputHandler

    override fun onCreate() {
        super.onCreate()

        hapticFeedback = HapticFeedback(this)

        inputHandler = InputHandler(
            hapticFeedback = hapticFeedback,
            onLayoutChange = { layout -> handleLayoutChange(layout) },
            onSuggestionsUpdate = { suggestions -> handleSuggestionsUpdate(suggestions) }
        )
    }

    override fun onCreateInputView(): View {
        val view = KeyboardView(this).apply {
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
        keyboardView = view
        return view
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
        keyboardView?.destroy()
        keyboardView = null
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

        if (deltaChars > 0) {
            // Move cursor right
            connection.setSelection(
                connection.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0)?.selectionStart?.plus(deltaChars)
                    ?: return,
                connection.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0)?.selectionEnd?.plus(deltaChars)
                    ?: return
            )
        } else if (deltaChars < 0) {
            // Move cursor left
            connection.setSelection(
                connection.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0)?.selectionStart?.plus(deltaChars)
                    ?: return,
                connection.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0)?.selectionEnd?.plus(deltaChars)
                    ?: return
            )
        }
    }

    // ── Layout Management ─────────────────────────────────────────────────

    private fun handleLayoutChange(layout: KeyboardLayoutType) {
        keyboardView?.setLayout(layout)
    }

    private fun handleSuggestionsUpdate(suggestions: List<String>) {
        // Future: Update suggestion bar UI
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
