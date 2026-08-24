/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * TypingDNATest.kt — Unit tests for TypingDNA.
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
class TypingDNATest {

    private lateinit var context: Context
    private lateinit var typingDNA: TypingDNA

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        typingDNA = TypingDNA(context)
        typingDNA.reset()
    }

    @Test
    fun testGenerateFingerprintDeterministic() {
        val fp1 = typingDNA.generateFingerprint()
        val fp2 = typingDNA.generateFingerprint()
        assertEquals(fp1, fp2)
        assertEquals(64, fp1.length) // SHA-256 hex length
    }

    @Test
    fun testKeyPressAndReleaseUpdatesStats() {
        var t = 1000L
        // Type 'a' (hold 50ms)
        typingDNA.onKeyPressed('a', t)
        t += 50
        typingDNA.onKeyReleased('a', t)

        // Pause 100ms
        t += 100

        // Type 'b' (hold 60ms)
        typingDNA.onKeyPressed('b', t)
        t += 60
        typingDNA.onKeyReleased('b', t)

        val stats = typingDNA.computeStats()
        assertTrue(stats.meanHoldTime > 0)
        assertTrue(stats.meanFlightTime > 0)

        val fp = typingDNA.generateFingerprint()
        assertNotNull(fp)
    }

    @Test
    fun testSaveAndVerifyIdentity() {
        assertFalse(typingDNA.verifyIdentity())

        typingDNA.saveFingerprint()
        assertTrue(typingDNA.verifyIdentity())
    }

    @Test
    fun testDominantHandAndPosition() {
        assertEquals(TypingDNA.DominantHand.UNKNOWN, typingDNA.getDominantHand())
        assertEquals(TypingDNA.KeyboardPosition.CENTER, typingDNA.getRecommendedPosition())

        // Right side keys faster
        var t = 100L
        typingDNA.onKeyPressed('q', t)
        t += 100
        typingDNA.onKeyReleased('q', t)

        t += 50
        typingDNA.onKeyPressed('p', t)
        t += 40
        typingDNA.onKeyReleased('p', t)

        val hand = typingDNA.getDominantHand()
        assertNotEquals(TypingDNA.DominantHand.UNKNOWN, hand)
    }

    @Test
    fun testReset() {
        typingDNA.onKeyPressed('x', 100L)
        typingDNA.onKeyReleased('x', 150L)
        typingDNA.saveFingerprint()
        assertTrue(typingDNA.verifyIdentity())

        typingDNA.reset()
        assertFalse(typingDNA.verifyIdentity())
    }
}
