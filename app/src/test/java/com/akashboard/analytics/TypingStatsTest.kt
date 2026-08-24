/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * TypingStatsTest.kt — Unit tests for TypingStats.
 */

package com.akashboard.analytics

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TypingStatsTest {

    private lateinit var context: Context
    private lateinit var typingStats: TypingStats

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        typingStats = TypingStats(context)
        typingStats.resetAllStats()
    }

    @Test
    fun testInitialStats() {
        assertEquals(0, typingStats.getBestWPM())
        assertEquals(0L, typingStats.getTotalSessions())
        assertEquals(0L, typingStats.getTotalTimeTyping())
        assertEquals(0L, typingStats.getTotalCharacters())
        assertEquals(0L, typingStats.getTotalWords())
        assertEquals(0, typingStats.getTotalAutoCorrections())
        assertEquals(0, typingStats.getAverageWPM())
        assertEquals(100f, typingStats.getOverallAccuracy(), 0.01f)
    }

    @Test
    fun testSessionFlowAndAccuracy() {
        typingStats.startSession()

        // 8 correct chars, 2 error chars
        for (i in 1..8) {
            typingStats.onKeyReleased(isCorrect = true)
        }
        for (i in 1..2) {
            typingStats.onKeyReleased(isCorrect = false)
        }

        assertEquals(10, typingStats.getSessionCharCount())
        assertEquals(80f, typingStats.calculateAccuracy(), 0.01f)

        typingStats.onWordCompleted("test")
        assertEquals(1, typingStats.getSessionWordCount())

        typingStats.onAutoCorrect("tset", "test")
        assertEquals(1, typingStats.getTotalAutoCorrections())

        typingStats.endSession()

        assertEquals(1L, typingStats.getTotalSessions())
        assertEquals(10L, typingStats.getTotalCharacters())
    }

    @Test
    fun testResetAllStats() {
        typingStats.startSession()
        typingStats.onKeyReleased(true)
        typingStats.onAutoCorrect("teh", "the")
        typingStats.endSession()

        assertTrue(typingStats.getTotalSessions() > 0)
        assertTrue(typingStats.getTotalCharacters() > 0)

        typingStats.resetAllStats()

        assertEquals(0, typingStats.getBestWPM())
        assertEquals(0L, typingStats.getTotalSessions())
        assertEquals(0L, typingStats.getTotalCharacters())
        assertEquals(0L, typingStats.getTotalWords())
        assertEquals(0, typingStats.getTotalAutoCorrections())
        assertEquals(0, typingStats.getSessionCharCount())
    }

    @Test
    fun testTypingProfile() {
        val profile = typingStats.getTypingProfile()
        assertEquals(0, profile.bestWPM)
        assertEquals(0, profile.averageWPM)
        assertEquals(100f, profile.overallAccuracy, 0.01f)
        assertEquals(0L, profile.totalSessions)
        assertEquals(0f, profile.autoCorrectionRate, 0.01f)
    }
}
