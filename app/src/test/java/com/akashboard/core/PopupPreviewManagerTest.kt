/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * PopupPreviewManagerTest.kt — Unit tests for popup preview behavior.
 */

package com.akashboard.core

import android.graphics.RectF
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PopupPreviewManagerTest {

    private lateinit var manager: PopupPreviewManager

    private fun createKey(label: String, popupLabel: String? = null): KeyData {
        return KeyData(
            id = "key_${label.lowercase()}",
            label = label,
            code = label[0].code,
            rect = RectF(50f, 200f, 150f, 250f),
            hitRect = RectF(46f, 196f, 154f, 254f),
            type = KeyType.LETTER,
            popupLabel = popupLabel
        )
    }

    @Before
    fun setup() {
        manager = PopupPreviewManager()
    }

    // ── Show ──────────────────────────────────────────────────────────────

    @Test
    fun `show makes popup visible`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)
        assertTrue(manager.state.visible)
    }

    @Test
    fun `show stores the key`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)
        assertEquals(key, manager.state.key)
    }

    @Test
    fun `show uses popupLabel when available`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)
        assertEquals("1", manager.state.label)
    }

    @Test
    fun `show falls back to label when no popupLabel`() {
        val key = createKey("A")
        manager.show(key, 3.0f)
        assertEquals("A", manager.state.label)
    }

    @Test
    fun `show positions popup above the key`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)
        assertTrue(manager.state.y < key.rect.top)
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Test
    fun `update sets alternateSelected when sliding up past threshold`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)

        // Slide up past threshold (30dp * density = 90px)
        manager.update(100f, 3.0f) // Touch Y = 100, well above key top (200)

        assertTrue(manager.state.alternateSelected)
    }

    @Test
    fun `update does not set alternateSelected when below threshold`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)

        // Touch Y is still below key top
        manager.update(190f, 3.0f)

        assertFalse(manager.state.alternateSelected)
    }

    @Test
    fun `update fires callback on state change`() {
        var callbackFired = false
        manager.onStateChanged = { callbackFired = true }

        val key = createKey("Q", "1")
        manager.show(key, 3.0f)
        callbackFired = false // Reset after show

        manager.update(100f, 3.0f)
        assertTrue(callbackFired)
    }

    @Test
    fun `update does nothing when popup not visible`() {
        // No show() call, so popup not visible
        manager.update(100f, 3.0f)
        assertFalse(manager.state.alternateSelected)
    }

    // ── Dismiss ───────────────────────────────────────────────────────────

    @Test
    fun `dismiss returns original label when not slid up`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)
        val label = manager.dismiss()
        assertEquals("Q", label)
    }

    @Test
    fun `dismiss returns popupLabel when slid up`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)
        manager.update(100f, 3.0f) // Slide up
        val label = manager.dismiss()
        assertEquals("1", label)
    }

    @Test
    fun `dismiss resets state to invisible`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)
        manager.dismiss()
        assertFalse(manager.state.visible)
        assertNull(manager.state.key)
    }

    @Test
    fun `forceDismiss resets state without returning label`() {
        val key = createKey("Q", "1")
        manager.show(key, 3.0f)
        manager.forceDismiss()
        assertFalse(manager.state.visible)
        assertNull(manager.state.key)
    }

    // ── Callback ──────────────────────────────────────────────────────────

    @Test
    fun `onStateChanged fires on show`() {
        var called = false
        manager.onStateChanged = { called = true }
        manager.show(createKey("Q"), 3.0f)
        assertTrue(called)
    }

    @Test
    fun `onStateChanged fires on dismiss`() {
        var called = false
        manager.show(createKey("Q"), 3.0f)
        manager.onStateChanged = { called = true }
        manager.dismiss()
        assertTrue(called)
    }

    // ── Constants ─────────────────────────────────────────────────────────

    @Test
    fun `POPUP_HEIGHT is 48`() {
        assertEquals(48f, PopupPreviewManager.POPUP_HEIGHT)
    }

    @Test
    fun `SLIDE_THRESHOLD is 30`() {
        assertEquals(30f, PopupPreviewManager.SLIDE_THRESHOLD)
    }

    @Test
    fun `LONG_PRESS_DELAY is 400`() {
        assertEquals(400L, PopupPreviewManager.LONG_PRESS_DELAY)
    }
}
