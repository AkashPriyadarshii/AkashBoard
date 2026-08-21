/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * InputHandler.kt — Main input processing pipeline.
 *
 * Week 4: Integrated with Rust prediction engine.
 *   - Requests predictions after each key press
 *   - Accepts suggestions from prediction bar
 *   - Learns new words from user typing
 */

package com.akashboard.core

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.akashboard.engine.PredictorBridge

/**
 * Main input processing handler.
 *
 * Connects keyboard view → prediction engine → target app.
 */
class InputHandler(
    private val hapticFeedback: HapticFeedback,
    private val onLayoutChange: (KeyboardLayoutType) -> Unit,
    private val onSuggestionsUpdate: (List<String>) -> Unit
) {
    private var inputConnection: InputConnection? = null
    private var editorInfo: EditorInfo? = null

    val wordComposer = WordComposer()
    var currentLayout: KeyboardLayoutType = KeyboardLayoutType.QWERTY
        private set
    var autoCorrectEnabled: Boolean = true
    var incognitoMode: Boolean = false

    // ── Connection ────────────────────────────────────────────────────────

    fun setInputConnection(connection: InputConnection?, info: EditorInfo?) {
        inputConnection = connection
        editorInfo = info
        wordComposer.reset()

        val inputType = info?.inputType ?: 0
        val isPassword = (inputType and EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) != 0 ||
                (inputType and EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD) != 0
        if (isPassword) onSuggestionsUpdate(emptyList())
    }

    // ── Key Processing ────────────────────────────────────────────────────

    fun handleKeyPress(key: KeyData): KeyPressResult {
        val connection = inputConnection ?: return KeyPressResult.None

        val result = when (key.type) {
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
            KeyType.VOICE -> KeyPressResult.None
        }

        // Request predictions after any key press
        if (result is KeyPressResult.Character) {
            requestPredictions()
        }

        return result
    }

    private fun handleLetter(connection: InputConnection, key: KeyData): KeyPressResult {
        val char = key.label[0]
        val committed = wordComposer.addCharacter(char)
        connection.commitText(committed.toString(), 1)

        if (wordComposer.shiftState == ShiftState.ONE) {
            wordComposer.clearShift()
        }

        hapticFeedback.keyPress()
        return KeyPressResult.Character(committed.toString())
    }

    private fun handleSpace(connection: InputConnection): KeyPressResult {
        val word = wordComposer.finishWord()

        // Learn the completed word
        if (!incognitoMode && !word.isNullOrBlank()) {
            val context = wordComposer.getContextWords().joinToString(" ")
            PredictorBridge.learn(word, context, System.currentTimeMillis())
        }

        connection.commitText(" ", 1)
        hapticFeedback.keyPress()
        onSuggestionsUpdate(emptyList())

        return if (word != null) KeyPressResult.WordCompleted(word) else KeyPressResult.None
    }

    private fun handleDelete(connection: InputConnection): KeyPressResult {
        if (wordComposer.deleteLast()) {
            connection.deleteSurroundingText(1, 0)
            hapticFeedback.keyPress()
            requestPredictions()
            return KeyPressResult.Backspace
        }
        connection.deleteSurroundingText(1, 0)
        hapticFeedback.keyPress()
        return KeyPressResult.Backspace
    }

    private fun handleEnter(connection: InputConnection): KeyPressResult {
        val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_UNSPECIFIED
        wordComposer.finishWord()
        connection.performEditorAction(action)
        hapticFeedback.modifier()
        return KeyPressResult.Enter(action)
    }

    private fun handleShift(): KeyPressResult {
        wordComposer.toggleShift()
        hapticFeedback.modifier()
        return KeyPressResult.ShiftChanged(wordComposer.shiftState)
    }

    private fun handleLayoutSwitch(layout: KeyboardLayoutType): KeyPressResult {
        currentLayout = layout
        onLayoutChange(layout)
        hapticFeedback.modeSwitch()
        return KeyPressResult.LayoutChanged(layout)
    }

    private fun handleLanguageSwitch(): KeyPressResult {
        hapticFeedback.modeSwitch()
        return KeyPressResult.None
    }

    private fun handlePunctuation(connection: InputConnection, punct: String): KeyPressResult {
        val word = wordComposer.finishWord()
        connection.commitText(punct, 1)
        hapticFeedback.keyPress()
        onSuggestionsUpdate(emptyList())
        return if (word != null) KeyPressResult.WordCompleted(word) else KeyPressResult.None
    }

    // ── Predictions ───────────────────────────────────────────────────────

    private fun requestPredictions() {
        val context = wordComposer.getContext()
        if (context.isBlank()) {
            onSuggestionsUpdate(emptyList())
            return
        }

        val predictions = PredictorBridge.predict(context, 3)
        onSuggestionsUpdate(predictions)
    }

    /**
     * Accept a suggestion from the suggestion bar.
     */
    fun acceptSuggestion(word: String) {
        val connection = inputConnection ?: return

        // Delete current partial word
        val currentWord = wordComposer.getCurrentWord()
        if (currentWord.isNotEmpty()) {
            connection.deleteSurroundingText(currentWord.length, 0)
        }

        // Commit the suggested word + space
        connection.commitText("$word ", 1)
        wordComposer.finishWord()
        wordComposer.clearShift()

        hapticFeedback.selection()
        onSuggestionsUpdate(emptyList())
    }

    // ── State ─────────────────────────────────────────────────────────────

    fun getCurrentWord(): String = wordComposer.getCurrentWord()
    fun getShiftState(): ShiftState = wordComposer.shiftState
    fun getContextForPrediction(): String = wordComposer.getContext()
    fun reset() { wordComposer.reset(); currentLayout = KeyboardLayoutType.QWERTY }
    fun setAutoCorrect(enabled: Boolean) { autoCorrectEnabled = enabled }
    fun setIncognito(enabled: Boolean) { incognitoMode = enabled }
}
