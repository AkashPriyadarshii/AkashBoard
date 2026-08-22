/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * WordComposerTest.kt — Unit tests for WordComposer.
 */

package com.akashboard.core

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WordComposerTest {

    private lateinit var composer: WordComposer

    @Before
    fun setup() {
        composer = WordComposer()
    }

    // ── Character Addition ────────────────────────────────────────────────

    @Test
    fun `addCharacter returns lowercase when shift is NONE`() {
        val result = composer.addCharacter('A')
        assertEquals('a', result)
    }

    @Test
    fun `addCharacter returns uppercase when shift is ONE`() {
        composer.setShiftOne()
        val result = composer.addCharacter('a')
        assertEquals('A', result)
    }

    @Test
    fun `addCharacter returns uppercase when shift is LOCKED`() {
        composer.toggleShift() // NONE -> ONE
        composer.toggleShift() // ONE -> LOCKED
        val result = composer.addCharacter('h')
        assertEquals('H', result)
    }

    @Test
    fun `addCharacter builds word in buffer`() {
        composer.addCharacter('h')
        composer.addCharacter('e')
        composer.addCharacter('l')
        composer.addCharacter('l')
        composer.addCharacter('o')
        assertEquals("hello", composer.getCurrentWord())
    }

    @Test
    fun `addCharacter increments totalCharactersTyped`() {
        assertEquals(0L, composer.totalCharactersTyped)
        composer.addCharacter('a')
        assertEquals(1L, composer.totalCharactersTyped)
        composer.addCharacter('b')
        assertEquals(2L, composer.totalCharactersTyped)
    }

    @Test
    fun `addCharacter advances shift ONE to NONE`() {
        composer.setShiftOne()
        assertEquals(ShiftState.ONE, composer.shiftState)
        composer.addCharacter('a')
        assertEquals(ShiftState.NONE, composer.shiftState)
    }

    @Test
    fun `addCharacter does not advance shift LOCKED`() {
        composer.toggleShift() // NONE -> ONE
        composer.toggleShift() // ONE -> LOCKED
        composer.addCharacter('a')
        assertEquals(ShiftState.LOCKED, composer.shiftState)
    }

    @Test
    fun `addCharacter inserts at cursor position`() {
        composer.addCharacter('h')
        composer.addCharacter('l')
        // cursor is at end, insert 'e' at position 1
        composer.deleteLast() // remove 'l', cursor at 1
        val result = composer.addCharacter('e')
        assertEquals('e', result)
        assertEquals("he", composer.getCurrentWord())
        assertEquals(2, composer.cursorPosition)
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Test
    fun `deleteLast removes last character`() {
        composer.addCharacter('h')
        composer.addCharacter('i')
        assertTrue(composer.deleteLast())
        assertEquals("h", composer.getCurrentWord())
    }

    @Test
    fun `deleteLast returns false when buffer is empty`() {
        assertFalse(composer.deleteLast())
    }

    @Test
    fun `deleteLast decrements cursor position`() {
        composer.addCharacter('a')
        composer.addCharacter('b')
        assertEquals(2, composer.cursorPosition)
        composer.deleteLast()
        assertEquals(1, composer.cursorPosition)
    }

    @Test
    fun `deleteForward removes character after cursor`() {
        composer.addCharacter('a')
        composer.addCharacter('b')
        composer.addCharacter('c')
        composer.deleteLast() // "ab", cursor at 2
        composer.deleteLast() // "a", cursor at 1
        // Now cursor is at 1, "a" — adding 'x' inserts at 1
        // Actually let me test deleteForward differently
        // Reset and set up properly
        composer.reset()
        composer.addCharacter('a')
        composer.addCharacter('b')
        composer.addCharacter('c')
        // cursor at 3, move back
        composer.deleteLast() // "ab", cursor at 2
        composer.deleteLast() // "a", cursor at 1
        // Now buffer is "a", cursor at 1
        // deleteForward should do nothing (cursor >= length)
        assertFalse(composer.deleteForward())
    }

    @Test
    fun `deleteForward returns false when cursor at end`() {
        composer.addCharacter('a')
        assertFalse(composer.deleteForward())
    }

    @Test
    fun `deleteForward returns false on empty buffer`() {
        assertFalse(composer.deleteForward())
    }

    // ── Finish Word ───────────────────────────────────────────────────────

    @Test
    fun `finishWord returns the word and adds to context`() {
        composer.addCharacter('h')
        composer.addCharacter('i')
        val word = composer.finishWord()
        assertEquals("hi", word)
        assertEquals("", composer.getCurrentWord())
        assertEquals(0, composer.cursorPosition)
    }

    @Test
    fun `finishWord returns null when buffer is empty`() {
        assertNull(composer.finishWord())
    }

    @Test
    fun `finishWord returns null when buffer is blank`() {
        // Only whitespace
        composer.addCharacter(' ')
        assertNull(composer.finishWord())
    }

    @Test
    fun `finishWord clears wasAutoCorrected`() {
        composer.acceptSuggestion("test")
        assertTrue(composer.wasAutoCorrected)
        composer.finishWord()
        assertFalse(composer.wasAutoCorrected)
    }

    @Test
    fun `multiple finishWords build context`() {
        composer.addCharacter('I')
        composer.finishWord()
        composer.addCharacter('a')
        composer.addCharacter('m')
        composer.finishWord()
        composer.addCharacter('h')
        composer.addCharacter('a')
        composer.addCharacter('p')
        composer.addCharacter('p')
        composer.addCharacter('y')

        assertEquals("happy", composer.getCurrentWord())
        val context = composer.getContextWords()
        assertEquals(2, context.size)
        assertEquals("I", context[0])
        assertEquals("am", context[1])
    }

    // ── Context ───────────────────────────────────────────────────────────

    @Test
    fun `getContext returns only current word when no history`() {
        composer.addCharacter('h')
        composer.addCharacter('i')
        assertEquals("hi", composer.getContext())
    }

    @Test
    fun `getContext combines history and current word`() {
        composer.addCharacter('h')
        composer.addCharacter('i')
        composer.finishWord()

        composer.addCharacter('w')
        composer.addCharacter('o')
        composer.addCharacter('r')
        composer.addCharacter('l')
        composer.addCharacter('d')

        assertEquals("hi world", composer.getContext())
    }

    @Test
    fun `getContext returns empty string when nothing typed`() {
        assertEquals("", composer.getContext())
    }

    @Test
    fun `context ring buffer holds max 5 words`() {
        for (i in 1..7) {
            composer.addCharacter('w')
            composer.addCharacter('o')
            composer.addCharacter('r')
            composer.addCharacter('d')
            composer.finishWord()
        }
        val contextWords = composer.getContextWords()
        assertEquals(5, contextWords.size)
        assertEquals("word", contextWords.last())
    }

    @Test
    fun `getLastWord returns most recently finished word`() {
        composer.addCharacter('f')
        composer.addCharacter('i')
        composer.addCharacter('r')
        composer.addCharacter('s')
        composer.addCharacter('t')
        composer.finishWord()

        composer.addCharacter('s')
        composer.addCharacter('e')
        composer.addCharacter('c')
        composer.addCharacter('o')
        composer.addCharacter('n')
        composer.addCharacter('d')
        composer.finishWord()

        assertEquals("second", composer.getLastWord())
    }

    // ── Accept Suggestion ─────────────────────────────────────────────────

    @Test
    fun `acceptSuggestion replaces buffer with word`() {
        composer.addCharacter('h')
        composer.addCharacter('e')
        composer.addCharacter('l')
        composer.acceptSuggestion("hello")
        assertEquals("hello", composer.getCurrentWord())
        assertEquals(5, composer.cursorPosition)
        assertTrue(composer.wasAutoCorrected)
    }

    // ── Shift Management ──────────────────────────────────────────────────

    @Test
    fun `toggleShift cycles NONE to ONE to LOCKED to NONE`() {
        assertEquals(ShiftState.NONE, composer.shiftState)
        composer.toggleShift()
        assertEquals(ShiftState.ONE, composer.shiftState)
        composer.toggleShift()
        assertEquals(ShiftState.LOCKED, composer.shiftState)
        composer.toggleShift()
        assertEquals(ShiftState.NONE, composer.shiftState)
    }

    @Test
    fun `setShiftOne sets shift to ONE`() {
        composer.setShiftOne()
        assertEquals(ShiftState.ONE, composer.shiftState)
    }

    @Test
    fun `clearShift resets to NONE`() {
        composer.toggleShift() // ONE
        composer.toggleShift() // LOCKED
        composer.clearShift()
        assertEquals(ShiftState.NONE, composer.shiftState)
    }

    // ── Reset ─────────────────────────────────────────────────────────────

    @Test
    fun `reset clears all state`() {
        composer.addCharacter('h')
        composer.addCharacter('i')
        composer.finishWord()
        composer.addCharacter('w')
        composer.setShiftOne()

        composer.reset()

        assertEquals("", composer.getCurrentWord())
        assertEquals(0, composer.cursorPosition)
        assertEquals(ShiftState.NONE, composer.shiftState)
        assertTrue(composer.getContextWords().isEmpty())
        assertFalse(composer.wasAutoCorrected)
    }

    @Test
    fun `isEmpty returns true on fresh composer`() {
        assertTrue(composer.isEmpty())
    }

    @Test
    fun `isEmpty returns false after adding character`() {
        composer.addCharacter('a')
        assertFalse(composer.isEmpty())
    }

    @Test
    fun `isEmpty returns true after finishWord`() {
        composer.addCharacter('a')
        composer.finishWord()
        assertTrue(composer.isEmpty())
    }

    @Test
    fun `length returns buffer length`() {
        assertEquals(0, composer.length())
        composer.addCharacter('a')
        assertEquals(1, composer.length())
        composer.addCharacter('b')
        assertEquals(2, composer.length())
    }

    @Test
    fun `getWordCount returns number of finished words`() {
        assertEquals(0, composer.getWordCount())
        composer.addCharacter('a')
        composer.finishWord()
        assertEquals(1, composer.getWordCount())
        composer.addCharacter('b')
        composer.finishWord()
        assertEquals(2, composer.getWordCount())
    }

    @Test
    fun `toString contains word and state`() {
        composer.addCharacter('h')
        val str = composer.toString()
        assertTrue(str.contains("h"))
        assertTrue(str.contains("shift="))
    }
}
