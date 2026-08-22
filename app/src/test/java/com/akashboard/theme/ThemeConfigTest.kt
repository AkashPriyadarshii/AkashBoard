/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * ThemeConfigTest.kt — Unit tests for theme configuration and parsing.
 */

package com.akashboard.theme

import android.graphics.Color
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ThemeConfigTest {

    // ── ThemeColors ───────────────────────────────────────────────────────

    @Test
    fun `ThemeColors from AKASH_DARK has correct key colors`() {
        val colors = ThemeColors.from(BuiltInThemes.AKASH_DARK)
        assertEquals(BuiltInThemes.AKASH_DARK.colors.key, colors.keyBg)
        assertEquals(BuiltInThemes.AKASH_DARK.colors.keyPressed, colors.keyPressed)
        assertEquals(BuiltInThemes.AKASH_DARK.colors.accent, colors.accent)
    }

    @Test
    fun `ThemeColors from AKASH_LIGHT has correct key colors`() {
        val colors = ThemeColors.from(BuiltInThemes.AKASH_LIGHT)
        assertEquals(BuiltInThemes.AKASH_LIGHT.colors.key, colors.keyBg)
        assertEquals(BuiltInThemes.AKASH_LIGHT.colors.keyPressed, colors.keyPressed)
    }

    @Test
    fun `ThemeColors from NEON_CYBER has correct accent`() {
        val colors = ThemeColors.from(BuiltInThemes.NEON_CYBER)
        assertEquals(BuiltInThemes.NEON_CYBER.colors.accent, colors.accent)
    }

    @Test
    fun `ThemeColors from MINIMAL has white text`() {
        val colors = ThemeColors.from(BuiltInThemes.MINIMAL)
        assertEquals(Color.WHITE, colors.keyText)
    }

    @Test
    fun `ThemeColors from SUNSET has orange accent`() {
        val colors = ThemeColors.from(BuiltInThemes.SUNSET)
        assertEquals(BuiltInThemes.SUNSET.colors.accent, colors.accent)
    }

    // ── BuiltInThemes ─────────────────────────────────────────────────────

    @Test
    fun `BuiltInThemes ALL has 5 themes`() {
        assertEquals(5, BuiltInThemes.ALL.size)
    }

    @Test
    fun `getByName returns correct theme`() {
        val theme = BuiltInThemes.getByName("Akash Dark")
        assertNotNull(theme)
        assertEquals("Akash Dark", theme!!.name)
    }

    @Test
    fun `getByName returns null for unknown theme`() {
        assertNull(BuiltInThemes.getByName("Unknown Theme"))
    }

    @Test
    fun `all themes have valid names`() {
        for (theme in BuiltInThemes.ALL) {
            assertTrue(theme.name.isNotEmpty())
        }
    }

    @Test
    fun `all themes have version 1`() {
        for (theme in BuiltInThemes.ALL) {
            assertEquals(1, theme.version)
        }
    }

    @Test
    fun `all themes have author`() {
        for (theme in BuiltInThemes.ALL) {
            assertTrue(theme.author.isNotEmpty())
        }
    }

    // ── ThemeConfig data ──────────────────────────────────────────────────

    @Test
    fun `ThemeConfig stores colors correctly`() {
        val config = BuiltInThemes.AKASH_DARK
        assertTrue(config.colors.canvas != 0)
        assertTrue(config.colors.surface != 0)
        assertTrue(config.colors.key != 0)
        assertTrue(config.colors.text != 0)
        assertTrue(config.colors.accent != 0)
    }

    @Test
    fun `DimensionConfig has default values`() {
        val dim = DimensionConfig()
        assertEquals(8f, dim.cornerRadius)
        assertEquals(1f, dim.keyElevation)
        assertEquals(4f, dim.keyPadding)
        assertEquals(48f, dim.suggestionBarHeight)
    }

    @Test
    fun `AnimationConfig has default values`() {
        val anim = AnimationConfig()
        assertEquals(0.92f, anim.pressScale)
        assertEquals(80L, anim.pressDuration)
        assertEquals(200L, anim.transitionDuration)
    }

    // ── Theme JSON Parsing ────────────────────────────────────────────────

    @Test
    fun `parseTheme parses valid JSON`() {
        val json = """
            {
                "name": "Test Theme",
                "version": 1,
                "colors": {
                    "canvas": "#111111",
                    "surface": "#222222",
                    "surface2": "#333333",
                    "key": "#444444",
                    "keyPressed": "#555555",
                    "text": "#FFFFFF",
                    "textSecondary": "#888888",
                    "accent": "#6C63FF",
                    "selection": "#6C63FF",
                    "cursor": "#6C63FF",
                    "destructive": "#FF0000"
                }
            }
        """.trimIndent()

        val theme = ThemeManager.parseTheme(json)
        assertNotNull(theme)
        assertEquals("Test Theme", theme!!.name)
        assertEquals(1, theme.version)
        assertEquals(Color.parseColor("#111111"), theme.colors.canvas)
        assertEquals(Color.parseColor("#FFFFFF"), theme.colors.text)
        assertEquals(Color.parseColor("#6C63FF"), theme.colors.accent)
    }

    @Test
    fun `parseTheme returns null for invalid JSON`() {
        assertNull(ThemeManager.parseTheme("not json"))
    }

    @Test
    fun `parseTheme returns null for empty string`() {
        assertNull(ThemeManager.parseTheme(""))
    }

    @Test
    fun `parseTheme handles optional dimensions`() {
        val json = """
            {
                "name": "Test",
                "version": 1,
                "colors": {
                    "canvas": "#000000",
                    "surface": "#111111",
                    "surface2": "#222222",
                    "key": "#333333",
                    "keyPressed": "#444444",
                    "text": "#FFFFFF",
                    "textSecondary": "#888888",
                    "accent": "#FF0000",
                    "destructive": "#FF0000"
                }
            }
        """.trimIndent()

        val theme = ThemeManager.parseTheme(json)
        assertNotNull(theme)
        // Default dimensions should be applied
        assertEquals(8f, theme!!.dimensions.cornerRadius)
    }

    @Test
    fun `parseTheme handles custom dimensions`() {
        val json = """
            {
                "name": "Custom",
                "version": 1,
                "colors": {
                    "canvas": "#000000",
                    "surface": "#111111",
                    "surface2": "#222222",
                    "key": "#333333",
                    "keyPressed": "#444444",
                    "text": "#FFFFFF",
                    "textSecondary": "#888888",
                    "accent": "#FF0000",
                    "destructive": "#FF0000"
                },
                "dimensions": {
                    "cornerRadius": 16.0,
                    "keyElevation": 2.0,
                    "keyPadding": 8.0,
                    "suggestionBarHeight": 56.0
                }
            }
        """.trimIndent()

        val theme = ThemeManager.parseTheme(json)
        assertNotNull(theme)
        assertEquals(16f, theme!!.dimensions.cornerRadius)
        assertEquals(2f, theme.dimensions.keyElevation)
    }
}
