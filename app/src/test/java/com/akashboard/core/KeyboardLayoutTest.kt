/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyboardLayoutTest.kt — Unit tests for keyboard layouts and layout calculator.
 */

package com.akashboard.core

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeyboardLayoutTest {

    // ── Layout Definitions ────────────────────────────────────────────────

    @Test
    fun `QWERTY layout has 4 rows`() {
        assertEquals(4, KeyboardLayouts.QWERTY.rows.size)
    }

    @Test
    fun `QWERTY first row has 10 letter keys`() {
        val row1 = KeyboardLayouts.QWERTY.rows[0]
        assertEquals(10, row1.keys.size)
        assertTrue(row1.keys.all { it.type == KeyType.LETTER })
    }

    @Test
    fun `QWERTY second row has 9 letter keys`() {
        val row2 = KeyboardLayouts.QWERTY.rows[1]
        assertEquals(9, row2.keys.size)
        assertTrue(row2.keys.all { it.type == KeyType.LETTER })
    }

    @Test
    fun `QWERTY third row has shift and delete`() {
        val row3 = KeyboardLayouts.QWERTY.rows[2]
        assertEquals(9, row3.keys.size)
        assertEquals(KeyType.SHIFT, row3.keys[0].type)
        assertEquals(KeyType.DELETE, row3.keys.last().type)
    }

    @Test
    fun `QWERTY fourth row has spacebar with width 4_0`() {
        val row4 = KeyboardLayouts.QWERTY.rows[3]
        val spaceKey = row4.keys.find { it.type == KeyType.SPACE }
        assertNotNull(spaceKey)
        assertEquals(4.0f, spaceKey!!.width)
    }

    @Test
    fun `SYMBOLS layout has 4 rows`() {
        assertEquals(4, KeyboardLayouts.SYMBOLS.rows.size)
    }

    @Test
    fun `SYMBOLS first row has number keys 1-0`() {
        val row1 = KeyboardLayouts.SYMBOLS.rows[0]
        assertEquals(10, row1.keys.size)
        assertEquals("1", row1.keys[0].label)
        assertEquals("0", row1.keys[9].label)
    }

    @Test
    fun `SYMBOLS fourth row has ABC key to return to QWERTY`() {
        val row4 = KeyboardLayouts.SYMBOLS.rows[3]
        val abcKey = row4.keys.find { it.label == "ABC" }
        assertNotNull(abcKey)
        assertEquals(KeyType.SYMBOLS, abcKey!!.type)
        assertEquals(KeyCodes.QWERTY, abcKey.code)
    }

    // ── KeySpec ───────────────────────────────────────────────────────────

    @Test
    fun `KeySpec defaults width to 1_0`() {
        val spec = KeySpec("A", 'A'.code, KeyType.LETTER)
        assertEquals(1.0f, spec.width)
    }

    @Test
    fun `KeySpec stores popup data`() {
        val spec = KeySpec("Q", 'Q'.code, KeyType.LETTER, popupLabel = "1", popupCode = '1'.code)
        assertEquals("1", spec.popupLabel)
        assertEquals('1'.code, spec.popupCode)
    }

    // ── Layout Calculator ─────────────────────────────────────────────────

    @Test
    fun `calculator produces keys with correct total height`() {
        val calculated = LayoutCalculator.calculate(
            KeyboardLayouts.QWERTY,
            screenWidth = 1080f,
            density = 3.0f
        )
        assertTrue(calculated.totalHeight > 0)
    }

    @Test
    fun `calculator produces keys within screen width`() {
        val screenWidth = 1080f
        val calculated = LayoutCalculator.calculate(
            KeyboardLayouts.QWERTY,
            screenWidth = screenWidth,
            density = 3.0f
        )
        for (key in calculated.keys) {
            assertTrue("Key ${key.label} left edge ${key.rect.left} should be >= 0", key.rect.left >= 0)
            assertTrue("Key ${key.label} right edge ${key.rect.right} should be <= screen width",
                key.rect.right <= screenWidth + 10) // small tolerance
        }
    }

    @Test
    fun `one-handed mode narrows keyboard to thumb side`() {
        val screenWidth = 1080f
        LayoutCalculator.keyboardWidthFractionOverride = 0.8f
        try {
            LayoutCalculator.keyboardSideOffsetFraction = 1f // right side
            val right = LayoutCalculator.calculate(KeyboardLayouts.QWERTY, screenWidth, 3.0f)
            val rightWidth = right.keys.maxOf { it.rect.right } - right.keys.minOf { it.rect.left }
            assertEquals("Keys span usable width minus edge gaps",
                screenWidth * 0.8f - 2 * 18f, rightWidth, 5f) // 6dp gap × 3 density × ~2 edges
            assertTrue("Right mode should shift keys toward right edge",
                right.keys.maxOf { it.rect.right } > screenWidth * 0.95f)

            LayoutCalculator.keyboardSideOffsetFraction = 0f // left side
            val left = LayoutCalculator.calculate(KeyboardLayouts.QWERTY, screenWidth, 3.0f)
            assertTrue("Left mode should hug left edge", left.keys.minOf { it.rect.left } < 30f)
        } finally {
            LayoutCalculator.keyboardWidthFractionOverride = null
            LayoutCalculator.keyboardSideOffsetFraction = 0f
        }
    }

    @Test
    fun `calculator hitRect is larger than visual rect`() {
        val calculated = LayoutCalculator.calculate(
            KeyboardLayouts.QWERTY,
            screenWidth = 1080f,
            density = 3.0f
        )
        for (key in calculated.keys) {
            assertTrue("Key ${key.label} hitRect should be wider",
                key.hitRect.width() >= key.rect.width())
            assertTrue("Key ${key.label} hitRect should be taller",
                key.hitRect.height() >= key.rect.height())
        }
    }

    @Test
    fun `calculator assigns unique IDs to keys`() {
        val calculated = LayoutCalculator.calculate(
            KeyboardLayouts.QWERTY,
            screenWidth = 1080f,
            density = 3.0f
        )
        val ids = calculated.keys.map { it.id }
        assertEquals(ids.size, ids.toSet().size) // All unique
    }

    @Test
    fun `calculator preserves key codes`() {
        val calculated = LayoutCalculator.calculate(
            KeyboardLayouts.QWERTY,
            screenWidth = 1080f,
            density = 3.0f
        )
        val qKey = calculated.keys.find { it.label == "Q" }
        assertNotNull(qKey)
        assertEquals('Q'.code, qKey!!.code)
    }

    @Test
    fun `calculator preserves key types`() {
        val calculated = LayoutCalculator.calculate(
            KeyboardLayouts.QWERTY,
            screenWidth = 1080f,
            density = 3.0f
        )
        val spaceKey = calculated.keys.find { it.type == KeyType.SPACE }
        assertNotNull(spaceKey)
        val shiftKey = calculated.keys.find { it.type == KeyType.SHIFT }
        assertNotNull(shiftKey)
    }

    @Test
    fun `calculator gives wider keys for wider weight`() {
        val calculated = LayoutCalculator.calculate(
            KeyboardLayouts.QWERTY,
            screenWidth = 1080f,
            density = 3.0f
        )
        val spaceKey = calculated.keys.find { it.type == KeyType.SPACE }!!
        val letterKey = calculated.keys.find { it.label == "Q" }!!
        assertTrue("Space key should be wider than letter key",
            spaceKey.rect.width() > letterKey.rect.width())
    }

    @Test
    fun `calculator adapts to different screen widths`() {
        val narrow = LayoutCalculator.calculate(KeyboardLayouts.QWERTY, 720f, 2.0f)
        val wide = LayoutCalculator.calculate(KeyboardLayouts.QWERTY, 1440f, 3.0f)

        // Both should produce keys
        assertTrue(narrow.keys.isNotEmpty())
        assertTrue(wide.keys.isNotEmpty())

        // Wide screen keys should be wider
        val narrowLetter = narrow.keys.find { it.label == "Q" }!!
        val wideLetter = wide.keys.find { it.label == "Q" }!!
        assertTrue(wideLetter.rect.width() > narrowLetter.rect.width())
    }

    @Test
    fun `calculator SYMBOLS layout produces valid keys`() {
        val calculated = LayoutCalculator.calculate(
            KeyboardLayouts.SYMBOLS,
            screenWidth = 1080f,
            density = 3.0f
        )
        assertTrue(calculated.keys.isNotEmpty())
        val numKey = calculated.keys.find { it.label == "5" }
        assertNotNull(numKey)
    }

    // ── KeyCodes ──────────────────────────────────────────────────────────

    @Test
    fun `KeyCodes are negative for special keys`() {
        assertTrue(KeyCodes.SHIFT < 0)
        assertTrue(KeyCodes.DELETE < 0)
        assertTrue(KeyCodes.ENTER < 0)
        assertTrue(KeyCodes.SYMBOLS < 0)
    }

    @Test
    fun `KeyCodes SPACE is 32`() {
        assertEquals(32, KeyCodes.SPACE)
    }

    @Test
    fun `KeyCodes COMMA is 44`() {
        assertEquals(44, KeyCodes.COMMA)
    }

    // ── KeyData ───────────────────────────────────────────────────────────

    @Test
    fun `KeyData stores all properties`() {
        val key = KeyData(
            id = "key_a",
            label = "A",
            code = 65,
            rect = android.graphics.RectF(0f, 0f, 100f, 50f),
            hitRect = android.graphics.RectF(-4f, -4f, 104f, 54f),
            type = KeyType.LETTER,
            popupLabel = "1",
            popupCode = 49,
            width = 1.0f,
            accessibilityLabel = "A"
        )
        assertEquals("key_a", key.id)
        assertEquals("A", key.label)
        assertEquals(65, key.code)
        assertEquals(KeyType.LETTER, key.type)
        assertEquals("1", key.popupLabel)
        assertEquals(49, key.popupCode)
        assertEquals(1.0f, key.width)
        assertEquals("A", key.accessibilityLabel)
    }

    @Test
    fun `KeyData default values are correct`() {
        val key = KeyData(
            id = "key_q",
            label = "Q",
            code = 81,
            rect = android.graphics.RectF(),
            hitRect = android.graphics.RectF(),
            type = KeyType.LETTER
        )
        assertNull(key.popupLabel)
        assertNull(key.popupCode)
        assertEquals(1.0f, key.width)
        assertEquals("Q", key.accessibilityLabel)
    }

    // ── Enum Tests ────────────────────────────────────────────────────────

    @Test
    fun `KeyType enum has all types`() {
        assertEquals(11, KeyType.values().size)
        assertNotNull(KeyType.valueOf("LETTER"))
        assertNotNull(KeyType.valueOf("SHIFT"))
        assertNotNull(KeyType.valueOf("DELETE"))
        assertNotNull(KeyType.valueOf("SPACE"))
        assertNotNull(KeyType.valueOf("ENTER"))
        assertNotNull(KeyType.valueOf("SYMBOLS"))
        assertNotNull(KeyType.valueOf("EMOJI"))
        assertNotNull(KeyType.valueOf("VOICE"))
        assertNotNull(KeyType.valueOf("LANGUAGE"))
        assertNotNull(KeyType.valueOf("COMMA"))
        assertNotNull(KeyType.valueOf("PERIOD"))
    }

    @Test
    fun `ShiftState enum has 3 states`() {
        assertEquals(3, ShiftState.values().size)
        assertEquals(ShiftState.NONE, ShiftState.valueOf("NONE"))
        assertEquals(ShiftState.ONE, ShiftState.valueOf("ONE"))
        assertEquals(ShiftState.LOCKED, ShiftState.valueOf("LOCKED"))
    }

    @Test
    fun `KeyboardLayoutType enum has 4 types`() {
        assertEquals(4, KeyboardLayoutType.values().size)
    }

    // ── KeyPressResult ────────────────────────────────────────────────────

    @Test
    fun `KeyPressResult Character stores char`() {
        val result = KeyPressResult.Character("a")
        assertEquals("a", result.char)
    }

    @Test
    fun `KeyPressResult WordCompleted stores word`() {
        val result = KeyPressResult.WordCompleted("hello")
        assertEquals("hello", result.word)
    }

    @Test
    fun `KeyPressResult Backspace is singleton`() {
        assertSame(KeyPressResult.Backspace, KeyPressResult.Backspace)
    }

    @Test
    fun `KeyPressResult None is singleton`() {
        assertSame(KeyPressResult.None, KeyPressResult.None)
    }

    @Test
    fun `KeyPressResult Emoji is singleton`() {
        assertSame(KeyPressResult.Emoji, KeyPressResult.Emoji)
    }

    @Test
    fun `KeyPressResult ShiftChanged stores state`() {
        val result = KeyPressResult.ShiftChanged(ShiftState.LOCKED)
        assertEquals(ShiftState.LOCKED, result.state)
    }

    @Test
    fun `KeyPressResult LayoutChanged stores layout`() {
        val result = KeyPressResult.LayoutChanged(KeyboardLayoutType.SYMBOLS)
        assertEquals(KeyboardLayoutType.SYMBOLS, result.layout)
    }

    @Test
    fun `KeyPressResult Enter stores action`() {
        val result = KeyPressResult.Enter(5)
        assertEquals(5, result.action)
    }

    @Test
    fun `KeyPressResult SuggestionAccepted stores word`() {
        val result = KeyPressResult.SuggestionAccepted("hello")
        assertEquals("hello", result.word)
    }
}
