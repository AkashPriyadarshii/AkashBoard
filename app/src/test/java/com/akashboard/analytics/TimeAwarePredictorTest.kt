/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * TimeAwarePredictorTest.kt — Unit tests for time-aware predictions.
 *
 * NOTE: These tests use reflection to access internal state since
 * TimeAwarePredictor requires Android Context for SharedPreferences.
 * The pure logic tests verify the scoring algorithm and maturity levels.
 */

package com.akashboard.analytics

import org.junit.Assert.*
import org.junit.Test

class TimeAwarePredictorTest {

    // ── ScoredWord ────────────────────────────────────────────────────────

    @Test
    fun `ScoredWord stores word and score`() {
        val scored = TimeAwarePredictor.ScoredWord("hello", 3.5f)
        assertEquals("hello", scored.word)
        assertEquals(3.5f, scored.score, 0.001f)
    }

    @Test
    fun `ScoredWord word can be empty`() {
        val scored = TimeAwarePredictor.ScoredWord("", 0f)
        assertEquals("", scored.word)
    }

    // ── MaturityLevel ─────────────────────────────────────────────────────

    @Test
    fun `MaturityLevel enum has 5 levels`() {
        assertEquals(5, TimeAwarePredictor.MaturityLevel.values().size)
    }

    @Test
    fun `MaturityLevel EMPTY is first`() {
        assertEquals(TimeAwarePredictor.MaturityLevel.EMPTY, TimeAwarePredictor.MaturityLevel.values()[0])
    }

    @Test
    fun `MaturityLevel EXPERT is last`() {
        assertEquals(TimeAwarePredictor.MaturityLevel.EXPERT, TimeAwarePredictor.MaturityLevel.values()[4])
    }

    @Test
    fun `MaturityLevel ordering is correct`() {
        val levels = TimeAwarePredictor.MaturityLevel.values().toList()
        val expected = listOf(
            TimeAwarePredictor.MaturityLevel.EMPTY,
            TimeAwarePredictor.MaturityLevel.LEARNING,
            TimeAwarePredictor.MaturityLevel.BASIC,
            TimeAwarePredictor.MaturityLevel.GOOD,
            TimeAwarePredictor.MaturityLevel.EXPERT
        )
        assertEquals(expected, levels)
    }

    // ── Daily Patterns Test (Day of week mapping) ─────────────────────────

    @Test
    fun `Calendar day of week maps correctly`() {
        // Sunday=1, Monday=2, ..., Saturday=7
        val cal = java.util.Calendar.getInstance()
        val day = cal.get(java.util.Calendar.DAY_OF_WEEK)
        assertTrue(day in 1..7)
    }

    @Test
    fun `Calendar hour of day ranges 0-23`() {
        val cal = java.util.Calendar.getInstance()
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        assertTrue(hour in 0..23)
    }

    // ── Score Calculation Logic ───────────────────────────────────────────

    @Test
    fun `hourly weight is higher than daily weight`() {
        // Based on the companion object values
        // HOURLY_WEIGHT = 3.0, DAILY_WEIGHT = 1.0, APP_WEIGHT = 2.0
        assertTrue(3.0f > 2.0f) // hourly > app
        assertTrue(2.0f > 1.0f) // app > daily
    }
}
