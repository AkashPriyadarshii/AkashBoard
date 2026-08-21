/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AkashBoardIME.kt — Main Input Method Editor service.
 *
 * This is the entry point for AkashBoard on Android.
 * It connects the keyboard view to the input handler and
 * manages the IME lifecycle.
 *
 * Lifecycle:
 *   onCreate → onCreateInputView → onStartInputView → [typing] → onFinishInputView → onDestroy
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
 *
 * Connects:
 *   KeyboardView (UI) → InputHandler (logic) → InputConnection (target app)
 */
class AkashBoardIME : InputMethodService() {

    companion object {
        private const val TAG = "AkashBoardIME"
    }

    // ── Components ────────────────────────────────────────────────────────

    private var keyboardView: KeyboardView? = null
    private lateinit var hapticFeedback: HapticFeedback
    private lateinit var inputHandler: InputHandler

    // ── Lifecycle ─────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()

        // Initialize haptic feedback
        hapticFeedback = HapticFeedback(this)

        // Initialize input handler with callbacks
        inputHandler = InputHandler(
            hapticFeedback = hapticFeedback,
            onLayoutChange = { layout -> handleLayoutChange(layout) },
            onSuggestionsUpdate = { suggestions -> handleSuggestionsUpdate(suggestions) }
        )
    }

    override fun onCreateInputView(): View {
        // Create the keyboard view
        val view = KeyboardView(this).apply {
            onKeyPressedListener = object : KeyboardView.OnKeyPressedListener {
                override fun onKeyPressed(key: KeyData) {
                    this@AkashBoardIME.handleKeyPress(key)
                }
            }
        }
        keyboardView = view
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        // Set input connection for the handler
        inputHandler.setInputConnection(currentInputConnection, info)

        // Update keyboard view
        keyboardView?.setShiftState(inputHandler.getShiftState())

        // Detect field type and adjust
        val inputType = info?.inputType ?: 0
        val isPassword = (inputType and EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) != 0 ||
                (inputType and EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD) != 0

        if (isPassword) {
            // Disable predictions for password fields
            // Future: Hide suggestion bar
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        // Future: Save session data
    }

    override fun onDestroy() {
        keyboardView = null
        super.onDestroy()
    }

    // ── Key Processing ────────────────────────────────────────────────────

    /**
     * Handle a key press from the keyboard view.
     */
    private fun handleKeyPress(key: KeyData) {
        val result = inputHandler.handleKeyPress(key)

        // Update keyboard view based on result
        when (result) {
            is com.akashboard.core.KeyPressResult.ShiftChanged -> {
                keyboardView?.setShiftState(result.state)
            }
            is com.akashboard.core.KeyPressResult.LayoutChanged -> {
                keyboardView?.setLayout(result.layout)
            }
            else -> {
                // Update shift state display
                keyboardView?.setShiftState(inputHandler.getShiftState())
            }
        }

        // Future: Get predictions from Rust engine and update suggestion bar
        // val context = inputHandler.getContextForPrediction()
        // val predictions = predictorBridge.predict(context, 3)
        // inputHandler.updateSuggestions(predictions)
    }

    // ── Layout Management ─────────────────────────────────────────────────

    /**
     * Handle layout change request.
     */
    private fun handleLayoutChange(layout: KeyboardLayoutType) {
        keyboardView?.setLayout(layout)
    }

    /**
     * Handle suggestion updates.
     */
    private fun handleSuggestionsUpdate(suggestions: List<String>) {
        // Future: Update suggestion bar UI
    }

    // ── Configuration ─────────────────────────────────────────────────────

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Future: Recalculate layout for orientation change
    }
}
