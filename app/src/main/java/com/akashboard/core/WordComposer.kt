/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * WordComposer.kt — Tracks the current word being composed.
 *
 * As the user types, WordComposer maintains:
 *   - The current word buffer
 *   - The cursor position within the word
 *   - The surrounding context (recent words)
 *   - Whether the word was auto-corrected
 *
 * When the user presses space, punctuation, or accepts a suggestion,
 * the word is "finished" and the context is updated.
 */

package com.akashboard.core

/**
 * Tracks the current word being typed.
 *
 * This is the bridge between raw key presses and meaningful text.
 * The InputHandler uses WordComposer to build words, and the
 * prediction engine uses WordComposer's context for suggestions.
 */
class WordComposer {

    /** Current word buffer */
    private val buffer = StringBuilder()

    /** Cursor position within the current word */
    var cursorPosition: Int = 0
        private set

    /** Whether shift is active */
    var shiftState: ShiftState = ShiftState.NONE
        private set

    /** Previous words for context (ring buffer of last 5) */
    private val contextWords = ArrayDeque<String>(5)

    /** Whether the current word was auto-corrected */
    var wasAutoCorrected: Boolean = false
        private set

    /** Total characters typed in this session */
    var totalCharactersTyped: Long = 0
        private set

    // ── Word Building ─────────────────────────────────────────────────────

    /**
     * Add a character to the current word.
     *
     * @param char The character to add
     * @return The character that was actually added (may differ due to shift)
     */
    fun addCharacter(char: Char): Char {
        val actualChar = when (shiftState) {
            ShiftState.NONE -> char.lowercaseChar()
            ShiftState.ONE, ShiftState.LOCKED -> char.uppercaseChar()
        }

        buffer.insert(cursorPosition, actualChar)
        cursorPosition++
        totalCharactersTyped++

        // Auto-advance shift state
        if (shiftState == ShiftState.ONE) {
            shiftState = ShiftState.NONE
        }

        return actualChar
    }

    /**
     * Delete the character before the cursor.
     *
     * @return true if a character was deleted, false if buffer was empty
     */
    fun deleteLast(): Boolean {
        if (cursorPosition == 0) return false

        buffer.deleteCharAt(cursorPosition - 1)
        cursorPosition--
        return true
    }

    /**
     * Delete the character after the cursor.
     *
     * @return true if a character was deleted
     */
    fun deleteForward(): Boolean {
        if (cursorPosition >= buffer.length) return false

        buffer.deleteCharAt(cursorPosition)
        return true
    }

    /**
     * Finish the current word (called on space, punctuation, etc.).
     *
     * Adds the word to context and clears the buffer.
     *
     * @return The completed word, or null if buffer was empty
     */
    fun finishWord(): String? {
        val word = buffer.toString()
        if (word.isBlank()) return null

        // Add to context (ring buffer)
        contextWords.addLast(word)
        if (contextWords.size > 5) {
            contextWords.removeFirst()
        }

        // Clear buffer
        buffer.clear()
        cursorPosition = 0
        wasAutoCorrected = false

        return word
    }

    /**
     * Accept a suggestion (replace current word).
     *
     * @param word The suggested word to accept
     */
    fun acceptSuggestion(word: String) {
        buffer.clear()
        buffer.append(word)
        cursorPosition = word.length
        wasAutoCorrected = true
    }

    // ── Shift Management ──────────────────────────────────────────────────

    /**
     * Toggle shift state.
     */
    fun toggleShift() {
        shiftState = when (shiftState) {
            ShiftState.NONE -> ShiftState.ONE
            ShiftState.ONE -> ShiftState.LOCKED
            ShiftState.LOCKED -> ShiftState.NONE
        }
    }

    /**
     * Set shift to ONE (next character only).
     */
    fun setShiftOne() {
        shiftState = ShiftState.ONE
    }

    /**
     * Disable shift.
     */
    fun clearShift() {
        shiftState = ShiftState.NONE
    }

    // ── Context ───────────────────────────────────────────────────────────

    /**
     * Get the current word being composed.
     */
    fun getCurrentWord(): String = buffer.toString()

    /**
     * Get the context string (recent words + current partial word).
     *
     * Example: "I am going to th" (where "th" is being typed)
     */
    fun getContext(): String {
        val context = contextWords.joinToString(" ")
        val current = buffer.toString()
        return if (context.isEmpty()) current else "$context $current"
    }

    /**
     * Get the last completed word (for context).
     */
    fun getLastWord(): String? = contextWords.lastOrNull()

    /**
     * Get all context words.
     */
    fun getContextWords(): List<String> = contextWords.toList()

    // ── State ─────────────────────────────────────────────────────────────

    /**
     * Whether the current word buffer is empty.
     */
    fun isEmpty(): Boolean = buffer.isEmpty()

    /**
     * Length of the current word buffer.
     */
    fun length(): Int = buffer.length

    /**
     * Reset all state (called when switching input fields).
     */
    fun reset() {
        buffer.clear()
        cursorPosition = 0
        shiftState = ShiftState.NONE
        contextWords.clear()
        wasAutoCorrected = false
    }

    /**
     * Get word count for this session.
     */
    fun getWordCount(): Int = contextWords.size

    override fun toString(): String {
        return "WordComposer(word='${buffer.toString()}', cursor=$cursorPosition, shift=$shiftState)"
    }
}
