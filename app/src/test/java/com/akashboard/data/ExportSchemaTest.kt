/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * ExportSchemaTest.kt — Unit tests for data export/import schema.
 */

package com.akashboard.data

import org.junit.Assert.*
import org.junit.Test

class ExportSchemaTest {

    // ── ExportValidator ────────────────────────────────────────────────────

    @Test
    fun `validate returns Valid for correct export`() {
        val export = AkashBoardExport(
            version = 1,
            appName = "AkashBoard",
            settings = mapOf(
                "theme_id" to ExportValue.StringVal("dark")
            )
        )
        val result = ExportValidator.validate(export)
        assertTrue(result is ExportValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate rejects unsupported version`() {
        val export = AkashBoardExport(version = 99, appName = "AkashBoard")
        val result = ExportValidator.validate(export)
        assertTrue(result is ExportValidator.ValidationResult.Invalid)
        val errors = (result as ExportValidator.ValidationResult.Invalid).errors
        assertTrue(errors.any { it.contains("version") })
    }

    @Test
    fun `validate rejects version 0`() {
        val export = AkashBoardExport(version = 0, appName = "AkashBoard")
        val result = ExportValidator.validate(export)
        assertTrue(result is ExportValidator.ValidationResult.Invalid)
    }

    @Test
    fun `validate rejects wrong app name`() {
        val export = AkashBoardExport(version = 1, appName = "WrongApp")
        val result = ExportValidator.validate(export)
        assertTrue(result is ExportValidator.ValidationResult.Invalid)
        val errors = (result as ExportValidator.ValidationResult.Invalid).errors
        assertTrue(errors.any { it.contains("app name") })
    }

    @Test
    fun `validate rejects empty dictionary entry`() {
        val export = AkashBoardExport(
            version = 1,
            appName = "AkashBoard",
            dictionary = listOf(DictionaryEntry(word = "  "))
        )
        val result = ExportValidator.validate(export)
        assertTrue(result is ExportValidator.ValidationResult.Invalid)
        val errors = (result as ExportValidator.ValidationResult.Invalid).errors
        assertTrue(errors.any { it.contains("Empty dictionary") })
    }

    // ── AkashBoardExport ──────────────────────────────────────────────────

    @Test
    fun `default export has version 1`() {
        val export = AkashBoardExport()
        assertEquals(1, export.version)
    }

    @Test
    fun `default export has appName AkashBoard`() {
        val export = AkashBoardExport()
        assertEquals("AkashBoard", export.appName)
    }

    @Test
    fun `default export has empty settings`() {
        val export = AkashBoardExport()
        assertTrue(export.settings.isEmpty())
    }

    @Test
    fun `default export has empty dictionary`() {
        val export = AkashBoardExport()
        assertTrue(export.dictionary.isEmpty())
    }

    @Test
    fun `default export has no stats`() {
        val export = AkashBoardExport()
        assertNull(export.stats)
    }

    // ── ExportValue ────────────────────────────────────────────────────────

    @Test
    fun `BoolVal stores boolean`() {
        val v = ExportValue.BoolVal(true)
        assertTrue(v.value)
    }

    @Test
    fun `IntVal stores int`() {
        val v = ExportValue.IntVal(42)
        assertEquals(42, v.value)
    }

    @Test
    fun `FloatVal stores float`() {
        val v = ExportValue.FloatVal(3.14f)
        assertEquals(3.14f, v.value, 0.001f)
    }

    @Test
    fun `StringVal stores string`() {
        val v = ExportValue.StringVal("hello")
        assertEquals("hello", v.value)
    }

    // ── DictionaryEntry ───────────────────────────────────────────────────

    @Test
    fun `DictionaryEntry default frequency is 1`() {
        val entry = DictionaryEntry(word = "hello")
        assertEquals(1, entry.frequency)
    }

    @Test
    fun `DictionaryEntry stores word`() {
        val entry = DictionaryEntry(word = "hello", frequency = 5)
        assertEquals("hello", entry.word)
        assertEquals(5, entry.frequency)
    }

    // ── ExportStats ────────────────────────────────────────────────────────

    @Test
    fun `ExportStats default values are zero`() {
        val stats = ExportStats()
        assertEquals(0L, stats.totalSessions)
        assertEquals(0L, stats.totalCharacters)
        assertEquals(0L, stats.totalWords)
        assertEquals(0, stats.bestWPM)
        assertEquals(0, stats.averageWPM)
        assertEquals(100f, stats.overallAccuracy)
    }

    // ── LearnedWord ───────────────────────────────────────────────────────

    @Test
    fun `LearnedWord stores context`() {
        val word = LearnedWord(word = "hello", context = "world", frequency = 3)
        assertEquals("hello", word.word)
        assertEquals("world", word.context)
        assertEquals(3, word.frequency)
    }

    // ── CustomTheme ───────────────────────────────────────────────────────

    @Test
    fun `CustomTheme stores config map`() {
        val theme = CustomTheme(
            id = "my_theme",
            name = "My Theme",
            config = mapOf("accent" to "#FF0000")
        )
        assertEquals("my_theme", theme.id)
        assertEquals("#FF0000", theme.config["accent"])
    }
}
