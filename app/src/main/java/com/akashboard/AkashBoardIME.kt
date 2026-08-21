/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AkashBoardIME.kt — Main Input Method Editor service.
 * This is the entry point for AkashBoard on Android.
 */

package com.akashboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.akashboard.ui.KeyboardView

/**
 * AkashBoard's IME service.
 *
 * Lifecycle:
 *   onCreate → onCreateInputView → onStartInputView → [typing] → onFinishInputView → onDestroy
 *
 * The keyboard view is created once and reused across input sessions.
 * Word composition and prediction state resets on each new input field.
 */
class AkashBoardIME : InputMethodService() {

    companion object {
        private const val TAG = "AkashBoardIME"
    }

    private var keyboardView: KeyboardView? = null

    // ── Lifecycle ─────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        // Future: Initialize Rust engine, load preferences, register broadcast receivers
    }

    override fun onCreateInputView(): View {
        // Create the keyboard view (reused across sessions)
        val view = KeyboardView(this).apply {
            // Future: Load layout, apply theme, set listeners
        }
        keyboardView = view
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Future: Reset word composer, update context based on EditorInfo
        //   - Detect password field → disable predictions
        //   - Detect URL field → adjust suggestion rail
        //   - Detect email field → adjust suggestion rail
        keyboardView?.requestLayout()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        // Future: Save session data, update learning model
    }

    override fun onDestroy() {
        keyboardView = null
        super.onDestroy()
        // Future: Destroy Rust engine, save state
    }

    // ── Configuration ─────────────────────────────────────────────────────

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Future: Update keyboard layout for orientation change
    }
}
