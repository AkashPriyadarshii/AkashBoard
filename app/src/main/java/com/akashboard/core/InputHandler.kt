/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * InputHandler.kt — Input processing with autocorrect.
 *
 * Week 6: Autocorrect flow:
 *   1. User types a word
 *   2. On space/punctuation, check if word needs correction
 *   3. If correction found, replace the word silently
 *   4. If user re-types the original, learn it as preference
 */

package com.akashboard.core

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.akashboard.engine.PredictorBridge

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
    var predictiveTextEnabled: Boolean = true
    var learningEnabled: Boolean = true

    private fun shouldLearn(): Boolean = !incognitoMode && learningEnabled

    // Track last autocorrect for undo
    private var lastAutoCorrectedWord: String? = null
    private var lastOriginalWord: String? = null

    // ── Connection ────────────────────────────────────────────────────────

    fun setInputConnection(connection: InputConnection?, info: EditorInfo?) {
        inputConnection = connection
        editorInfo = info
        wordComposer.reset()
        lastAutoCorrectedWord = null
        lastOriginalWord = null

        val inputType = info?.inputType ?: 0
        val isPassword = (inputType and EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) != 0 ||
                (inputType and EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD) != 0
        if (isPassword) onSuggestionsUpdate(emptyList())
    }

    // ── Key Processing ────────────────────────────────────────────────────

    fun handleKeyPress(key: KeyData): KeyPressResult {
        val connection = inputConnection ?: return KeyPressResult.None

        // Route special keys by CODE first — "ABC" shares KeyType.SYMBOLS with
        // "?123" but must switch layouts in the opposite direction.
        if (key.code == KeyCodes.QWERTY) {
            return handleLayoutSwitch(KeyboardLayoutType.QWERTY)
        }

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

        if (result is KeyPressResult.Character) {
            requestPredictions()
        }

        return result
    }

    private fun handleLetter(connection: InputConnection, key: KeyData): KeyPressResult {
        // Symbols layout keys are literal characters — never shift/lowercase them.
        if (currentLayout != KeyboardLayoutType.QWERTY) {
            val literal = key.label[0]
            connection.commitText(literal.toString(), 1)
            hapticFeedback.keyPress()
            return KeyPressResult.Character(literal.toString())
        }

        val char = key.label[0]
        val committed = wordComposer.addCharacter(char)
        connection.commitText(committed.toString(), 1)

        hapticFeedback.keyPress()
        return KeyPressResult.Character(committed.toString())
    }

    private fun handleSpace(connection: InputConnection): KeyPressResult {
        val word = wordComposer.finishWord()

        // Autocorrect before committing space
        if (autoCorrectEnabled && !word.isNullOrBlank() && !incognitoMode) {
            val corrected = PredictorBridge.correct(word, wordComposer.getContext())
            if (corrected != word) {
                // Replace the word
                connection.deleteSurroundingText(word.length, 0)
                connection.commitText("$corrected ", 1)

                // Track for potential undo
                lastAutoCorrectedWord = corrected
                lastOriginalWord = word

                // Learn the correction + the user's error pattern
                PredictorBridge.learn(corrected, wordComposer.getContext(), System.currentTimeMillis())
                if (shouldLearn()) PredictorBridge.learnError(word, corrected)

                hapticFeedback.selection()
                onSuggestionsUpdate(emptyList())
                return KeyPressResult.WordCompleted(corrected)
            }
        }

        // Learn the word
        if (shouldLearn() && !word.isNullOrBlank()) {
            val context = wordComposer.getContextWords().joinToString(" ")
            PredictorBridge.learn(word, context, System.currentTimeMillis())
        }

        connection.commitText(" ", 1)
        hapticFeedback.keyPress()
        onSuggestionsUpdate(emptyList())

        return if (word != null) KeyPressResult.WordCompleted(word) else KeyPressResult.None
    }

    private fun handleDelete(connection: InputConnection): KeyPressResult {
        // If last action was autocorrect, undo it
        if (lastAutoCorrectedWord != null && wordComposer.isEmpty()) {
            val corrected = lastAutoCorrectedWord!!
            val original = lastOriginalWord ?: ""

            connection.deleteSurroundingText(corrected.length + 1, 0) // +1 for space
            connection.commitText(original, 1)

            // Rebuild word composer with original
            for (char in original) {
                wordComposer.addCharacter(char)
            }

            lastAutoCorrectedWord = null
            lastOriginalWord = null

            hapticFeedback.keyPress()
            return KeyPressResult.Backspace
        }

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
        lastAutoCorrectedWord = null
        lastOriginalWord = null
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
        return when (layout) {
            KeyboardLayoutType.EMOJI -> KeyPressResult.Emoji
            else -> KeyPressResult.LayoutChanged(layout)
        }
    }

    private fun handleLanguageSwitch(): KeyPressResult {
        hapticFeedback.modeSwitch()
        return KeyPressResult.None
    }

    private fun handlePunctuation(connection: InputConnection, punct: String): KeyPressResult {
        val word = wordComposer.finishWord()

        // Autocorrect before punctuation
        if (autoCorrectEnabled && !word.isNullOrBlank() && !incognitoMode) {
            val corrected = PredictorBridge.correct(word, wordComposer.getContext())
            if (corrected != word) {
                connection.deleteSurroundingText(word.length, 0)
                connection.commitText("$corrected$punct", 1)
                lastAutoCorrectedWord = corrected
                lastOriginalWord = word
                hapticFeedback.selection()
                onSuggestionsUpdate(emptyList())
                return KeyPressResult.WordCompleted(corrected)
            }
        }

        connection.commitText(punct, 1)
        hapticFeedback.keyPress()
        onSuggestionsUpdate(emptyList())
        lastAutoCorrectedWord = null
        lastOriginalWord = null
        return if (word != null) KeyPressResult.WordCompleted(word) else KeyPressResult.None
    }

    // ── Predictions ───────────────────────────────────────────────────────

    private fun requestPredictions() {
        if (!predictiveTextEnabled) { onSuggestionsUpdate(emptyList()); return }
        val context = wordComposer.getContext()
        if (context.isBlank()) { onSuggestionsUpdate(emptyList()); return }
        val predictions = PredictorBridge.predict(context, 3)
        onSuggestionsUpdate(predictions)
    }

    fun acceptSuggestion(word: String) {
        val connection = inputConnection ?: return
        val currentWord = wordComposer.getCurrentWord()
        if (currentWord.isNotEmpty()) {
            connection.deleteSurroundingText(currentWord.length, 0)
        }
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
    fun setPredictiveText(enabled: Boolean) { predictiveTextEnabled = enabled }

    // ── Compose-facing public facades ────────────────────────────────────────
    // QwertyGrid calls these directly instead of going via KeyData/handleKeyPress.
    // Each one delegates to the existing private handler so autocorrect, learning,
    // TypingDNA, and suggestions all fire normally.

    /** Commit a single alphabetic or punctuation character. */
    fun handleCharacter(char: Char) {
        val connection = inputConnection ?: return
        // Replicate handleLetter logic without constructing a full KeyData.
        // wordComposer.addCharacter handles shift-state and returns the committed char.
        val committed = wordComposer.addCharacter(char)
        connection.commitText(committed.toString(), 1)
        hapticFeedback.keyPress()
        requestPredictions()
    }

    /** Delete the character before the cursor, with autocorrect-undo support. */
    fun handleBackspace() {
        val connection = inputConnection ?: return
        handleDelete(connection)
    }

    /** Commit a space, triggering autocorrect and word-learning. */
    fun handleSpace() {
        val connection = inputConnection ?: return
        handleSpace(connection)
    }

    /** Perform the editor action (Send / Search / Go / newline). */
    fun handleEnter() {
        val connection = inputConnection ?: return
        handleEnter(connection)
    }
}
