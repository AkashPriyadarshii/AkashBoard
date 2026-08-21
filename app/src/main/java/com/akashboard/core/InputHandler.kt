/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * InputHandler.kt — Main input processing pipeline.
 *
 * Processes key events and produces text output via Android's InputConnection.
 * This is the brain of the keyboard — it decides what happens when
 * each key is pressed.
 *
 * Processing pipeline:
 *   KeyData → InputHandler → WordComposer → InputConnection → Target App
 *                                    ↓
 *                             (Future: PredictorBridge)
 *                                    ↓
 *                             SuggestionBar
 */

package com.akashboard.core

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * Main input processing handler.
 *
 * Connects the keyboard view to the target app's text field.
 * Manages word composition, shift state, and layout switching.
 *
 * @param inputConnection Android's connection to the target text field
 * @param hapticFeedback Haptic feedback manager
 * @param onLayoutChange Callback when layout needs to change
 * @param onSuggestionsUpdate Callback when suggestions need updating
 */
class InputHandler(
    private val hapticFeedback: HapticFeedback,
    private val onLayoutChange: (KeyboardLayoutType) -> Unit,
    private val onSuggestionsUpdate: (List<String>) -> Unit
) {
    /** Current input connection (set per input field) */
    private var inputConnection: InputConnection? = null

    /** Current editor info (set per input field) */
    private var editorInfo: EditorInfo? = null

    /** Word composer — tracks current word */
    val wordComposer = WordComposer()

    /** Current keyboard layout */
    var currentLayout: KeyboardLayoutType = KeyboardLayoutType.QWERTY
        private set

    /** Whether auto-correct is enabled */
    var autoCorrectEnabled: Boolean = true

    /** Whether incognito mode is active */
    var incognitoMode: Boolean = false

    // ── Connection Management ─────────────────────────────────────────────

    /**
     * Set the input connection (called when input field gains focus).
     */
    fun setInputConnection(connection: InputConnection?, info: EditorInfo?) {
        inputConnection = connection
        editorInfo = info
        wordComposer.reset()

        // Disable predictions for password fields
        val inputType = info?.inputType ?: 0
        val isPassword = (inputType and EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) != 0 ||
                (inputType and EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD) != 0

        if (isPassword) {
            onSuggestionsUpdate(emptyList())
        }
    }

    // ── Key Processing ────────────────────────────────────────────────────

    /**
     * Process a key press.
     *
     * This is the main entry point for key events from KeyboardView.
     *
     * @param key The key that was pressed
     * @return The result of processing the key
     */
    fun handleKeyPress(key: KeyData): KeyPressResult {
        val connection = inputConnection ?: return KeyPressResult.None

        return when (key.type) {
            KeyType.LETTER -> handleLetter(connection, key)
            KeyType.SPACE -> handleSpace(connection)
            KeyType.DELETE -> handleDelete(connection)
            KeyType.ENTER -> handleEnter(connection)
            KeyType.SHIFT -> handleShift()
            KeyType.SYMBOLS -> handleLayoutSwitch(KeyboardLayoutType.SYMBOLS)
            KeyType.EMOJI -> handleLayoutSwitch(KeyboardLayoutType.EMOJI)
            KeyType.LANGUAGE -> handleLanguageSwitch()
            KeyType.COMMA -> handlePunctuation(connection, ",")
            KeyType.PERIOD -> handlePunctuation(connection, ".")
            KeyType.VOICE -> KeyPressResult.None  // Future: voice input
        }
    }

    /**
     * Handle a letter key press.
     */
    private fun handleLetter(connection: InputConnection, key: KeyData): KeyPressResult {
        val char = key.label[0]
        val committed = wordComposer.addCharacter(char)

        // Commit the character to the text field
        connection.commitText(committed.toString(), 1)

        // Update shift state after typing
        if (wordComposer.shiftState == ShiftState.ONE) {
            wordComposer.clearShift()
        }

        hapticFeedback.keyPress()

        return KeyPressResult.Character(committed.toString())
    }

    /**
     * Handle spacebar press.
     *
     * Finishes the current word and inserts a space.
     */
    private fun handleSpace(connection: InputConnection): KeyPressResult {
        val word = wordComposer.finishWord()

        // Insert space
        connection.commitText(" ", 1)

        hapticFeedback.keyPress()

        return if (word != null) {
            KeyPressResult.WordCompleted(word)
        } else {
            KeyPressResult.None
        }
    }

    /**
     * Handle backspace press.
     *
     * Deletes the character before the cursor.
     */
    private fun handleDelete(connection: InputConnection): KeyPressResult {
        // First try to delete from current word buffer
        if (wordComposer.deleteLast()) {
            connection.deleteSurroundingText(1, 0)
            hapticFeedback.keyPress()
            return KeyPressResult.Backspace
        }

        // If buffer is empty, delete from the text field
        connection.deleteSurroundingText(1, 0)
        hapticFeedback.keyPress()
        return KeyPressResult.Backspace
    }

    /**
     * Handle enter key press.
     *
     * Performs the editor action (send, done, next, etc.).
     */
    private fun handleEnter(connection: InputConnection): KeyPressResult {
        val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_UNSPECIFIED

        // Finish current word first
        wordComposer.finishWord()

        // Perform editor action
        connection.performEditorAction(action)

        hapticFeedback.modifier()

        return KeyPressResult.Enter(action)
    }

    /**
     * Handle shift key press.
     *
     * Toggles shift state: None → One → Locked → None
     */
    private fun handleShift(): KeyPressResult {
        wordComposer.toggleShift()
        hapticFeedback.modifier()
        return KeyPressResult.ShiftChanged(wordComposer.shiftState)
    }

    /**
     * Handle layout switch (symbols, emoji).
     */
    private fun handleLayoutSwitch(layout: KeyboardLayoutType): KeyPressResult {
        currentLayout = layout
        onLayoutChange(layout)
        hapticFeedback.modeSwitch()
        return KeyPressResult.LayoutChanged(layout)
    }

    /**
     * Handle language switch.
     */
    private fun handleLanguageSwitch(): KeyPressResult {
        // Future: Cycle through enabled languages
        hapticFeedback.modeSwitch()
        return KeyPressResult.None
    }

    /**
     * Handle punctuation (comma, period).
     */
    private fun handlePunctuation(connection: InputConnection, punct: String): KeyPressResult {
        // Finish current word
        val word = wordComposer.finishWord()

        // Insert punctuation
        connection.commitText(punct, 1)

        hapticFeedback.keyPress()

        return if (word != null) {
            KeyPressResult.WordCompleted(word)
        } else {
            KeyPressResult.None
        }
    }

    // ── Suggestions ───────────────────────────────────────────────────────

    /**
     * Get current context for prediction engine.
     */
    fun getContextForPrediction(): String {
        return wordComposer.getContext()
    }

    /**
     * Update suggestions from prediction engine.
     */
    fun updateSuggestions(suggestions: List<String>) {
        onSuggestionsUpdate(suggestions)
    }

    // ── State ─────────────────────────────────────────────────────────────

    /**
     * Get the current word being composed.
     */
    fun getCurrentWord(): String = wordComposer.getCurrentWord()

    /**
     * Get the current shift state.
     */
    fun getShiftState(): ShiftState = wordComposer.shiftState

    /**
     * Reset state (called when input field changes).
     */
    fun reset() {
        wordComposer.reset()
        currentLayout = KeyboardLayoutType.QWERTY
    }

    /**
     * Set auto-correct enabled/disabled.
     */
    fun setAutoCorrect(enabled: Boolean) {
        autoCorrectEnabled = enabled
    }

    /**
     * Set incognito mode.
     */
    fun setIncognito(enabled: Boolean) {
        incognitoMode = enabled
    }
}
