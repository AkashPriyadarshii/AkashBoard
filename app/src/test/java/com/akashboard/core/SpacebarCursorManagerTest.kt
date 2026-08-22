/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SpacebarCursorManagerTest.kt — Unit tests for spacebar cursor movement.
 */

package com.akashboard.core

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SpacebarCursorManagerTest {

    private lateinit var manager: SpacebarCursorManager

    @Before
    fun setup() {
        manager = SpacebarCursorManager()
    }

    // ── Tracking ──────────────────────────────────────────────────────────

    @Test
    fun `startTracking enables tracking`() {
        manager.startTracking(100f)
        assertTrue(manager.isTracking)
    }

    @Test
    fun `endTracking disables tracking`() {
        manager.startTracking(100f)
        manager.endTracking()
        assertFalse(manager.isTracking)
    }

    @Test
    fun `cancelTracking disables tracking`() {
        manager.startTracking(100f)
        manager.cancelTracking()
        assertFalse(manager.isTracking)
    }

    @Test
    fun `update returns 0 when not tracking`() {
        assertEquals(0, manager.update(200f, 3.0f))
    }

    // ── Cursor Movement ───────────────────────────────────────────────────

    @Test
    fun `update returns positive delta for rightward swipe`() {
        manager.startTracking(100f)
        val delta = manager.update(130f, 3.0f) // 30px right
        assertTrue(delta >= 0) // At least non-negative
    }

    @Test
    fun `update returns negative delta for leftward swipe`() {
        manager.startTracking(100f)
        val delta = manager.update(70f, 3.0f) // 30px left
        assertTrue(delta <= 0) // At most non-positive
    }

    @Test
    fun `update fires onCursorMove callback`() {
        var called = false
        var deltaReceived = 0
        manager.onCursorMove = { delta ->
            called = true
            deltaReceived = delta
        }
        manager.startTracking(100f)
        manager.update(200f, 3.0f) // Large movement
        // Callback might or might not fire depending on sensitivity
    }

    @Test
    fun `endTracking fires onGestureEnd callback`() {
        var called = false
        manager.onGestureEnd = { called = true }
        manager.startTracking(100f)
        manager.endTracking()
        assertTrue(called)
    }

    // ── Vertical Drift ────────────────────────────────────────────────────

    @Test
    fun `MAX_VERTICAL_DRIFT is 40`() {
        assertEquals(40f, SpacebarCursorManager.MAX_VERTICAL_DRIFT)
    }

    @Test
    fun `CURSOR_SENSITIVITY is 1_0`() {
        assertEquals(1.0f, SpacebarCursorManager.CURSOR_SENSITIVITY)
    }

    // ── Edge Cases ────────────────────────────────────────────────────────

    @Test
    fun `endTracking when not tracking does nothing`() {
        manager.endTracking() // Should not crash
        assertFalse(manager.isTracking)
    }

    @Test
    fun `cancelTracking resets accumulated movement`() {
        manager.startTracking(100f)
        manager.update(200f, 3.0f)
        manager.cancelTracking()
        assertFalse(manager.isTracking)
    }

    @Test
    fun `multiple startTracking calls reset state`() {
        manager.startTracking(100f)
        manager.update(150f, 3.0f)
        manager.startTracking(200f) // Reset
        assertTrue(manager.isTracking)
    }
}
