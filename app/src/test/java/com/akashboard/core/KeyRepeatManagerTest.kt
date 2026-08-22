/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyRepeatManagerTest.kt — Unit tests for key repeat behavior.
 */

package com.akashboard.core

import org.junit.Assert.*
import org.junit.Test

class KeyRepeatManagerTest {

    @Test
    fun `supportsRepeat returns true for DELETE`() {
        assertTrue(KeyRepeatManager.supportsRepeat(KeyType.DELETE))
    }

    @Test
    fun `supportsRepeat returns false for LETTER`() {
        assertFalse(KeyRepeatManager.supportsRepeat(KeyType.LETTER))
    }

    @Test
    fun `supportsRepeat returns false for SPACE`() {
        assertFalse(KeyRepeatManager.supportsRepeat(KeyType.SPACE))
    }

    @Test
    fun `supportsRepeat returns false for SHIFT`() {
        assertFalse(KeyRepeatManager.supportsRepeat(KeyType.SHIFT))
    }

    @Test
    fun `supportsRepeat returns false for ENTER`() {
        assertFalse(KeyRepeatManager.supportsRepeat(KeyType.ENTER))
    }

    @Test
    fun `supportsRepeat returns false for SYMBOLS`() {
        assertFalse(KeyRepeatManager.supportsRepeat(KeyType.SYMBOLS))
    }

    @Test
    fun `supportsRepeat returns false for EMOJI`() {
        assertFalse(KeyRepeatManager.supportsRepeat(KeyType.EMOJI))
    }

    @Test
    fun `INITIAL_DELAY is 300ms`() {
        assertEquals(300L, KeyRepeatManager.INITIAL_DELAY)
    }

    @Test
    fun `REPEAT_RATE_INITIAL is 50ms`() {
        assertEquals(50L, KeyRepeatManager.REPEAT_RATE_INITIAL)
    }

    @Test
    fun `REPEAT_RATE_MIN is 20ms`() {
        assertEquals(20L, KeyRepeatManager.REPEAT_RATE_MIN)
    }

    @Test
    fun `ACCELERATION_FACTOR is 0_92`() {
        assertEquals(0.92, KeyRepeatManager.ACCELERATION_FACTOR, 0.001)
    }
}
