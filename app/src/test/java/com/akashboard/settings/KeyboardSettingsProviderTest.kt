/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyboardSettingsProviderTest.kt — Unit tests for KeyboardSettingsProvider.
 */

package com.akashboard.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KeyboardSettingsProviderTest {

    private lateinit var context: Context
    private lateinit var provider: KeyboardSettingsProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        provider = KeyboardSettingsProvider(context)
        provider.resetToDefaults()
    }

    @Test
    fun testDefaults() {
        assertTrue(provider.autoCorrectEnabled)
        assertTrue(provider.predictiveTextEnabled)
        assertEquals("medium", provider.autoCorrectStrength)
        assertTrue(provider.swipeTypingEnabled)
        assertTrue(provider.spacebarCursorEnabled)
        assertTrue(provider.longPressRepeatEnabled)
        assertEquals(300, provider.keyRepeatDelay)
        assertEquals(50, provider.keyRepeatRate)
        assertTrue(provider.vibrateOnKeypress)
        assertTrue(provider.vibrateEnabled)
        assertFalse(provider.soundOnKeypress)
        assertFalse(provider.soundEnabled)
        assertEquals("akash_dark", provider.themeId)
        assertEquals(0, provider.themeIndex)
        assertTrue(provider.followSystemTheme)
        assertEquals(280, provider.keyboardHeight)
        assertEquals("off", provider.oneHandedMode)
        assertEquals(48, provider.suggestionBarHeight)
        assertEquals(8, provider.keyCornerRadius)
        assertEquals(6, provider.keySpacing)
        assertTrue(provider.showEmojiKey)
        assertTrue(provider.showVoiceKey)
        assertFalse(provider.showClipboardKey)
        assertFalse(provider.showNumberRow)
        assertFalse(provider.incognitoMode)
        assertTrue(provider.learningEnabled)
        assertTrue(provider.clipboardHistoryEnabled)
        assertEquals(50, provider.clipboardMaxItems)
        assertFalse(provider.clipboardAutoClear)
    }

    @Test
    fun testMutationsAndPersistence() {
        provider.autoCorrectEnabled = false
        provider.predictiveTextEnabled = false
        provider.autoCorrectStrength = "high"
        provider.swipeTypingEnabled = false
        provider.spacebarCursorEnabled = false
        provider.longPressRepeatEnabled = false
        provider.keyRepeatDelay = 400
        provider.keyRepeatRate = 75
        provider.vibrateOnKeypress = false
        provider.soundOnKeypress = true
        provider.themeId = "neon"
        provider.themeIndex = 3
        provider.followSystemTheme = false
        provider.keyboardHeight = 320
        provider.oneHandedMode = "right"
        provider.suggestionBarHeight = 56
        provider.keyCornerRadius = 12
        provider.keySpacing = 10
        provider.showEmojiKey = false
        provider.showVoiceKey = false
        provider.showClipboardKey = true
        provider.showNumberRow = true
        provider.incognitoMode = true
        provider.learningEnabled = false
        provider.clipboardHistoryEnabled = false
        provider.clipboardMaxItems = 100
        provider.clipboardAutoClear = true

        val fresh = KeyboardSettingsProvider(context)
        assertFalse(fresh.autoCorrectEnabled)
        assertFalse(fresh.predictiveTextEnabled)
        assertEquals("high", fresh.autoCorrectStrength)
        assertFalse(fresh.swipeTypingEnabled)
        assertFalse(fresh.spacebarCursorEnabled)
        assertFalse(fresh.longPressRepeatEnabled)
        assertEquals(400, fresh.keyRepeatDelay)
        assertEquals(75, fresh.keyRepeatRate)
        assertFalse(fresh.vibrateOnKeypress)
        assertFalse(fresh.vibrateEnabled)
        assertTrue(fresh.soundOnKeypress)
        assertTrue(fresh.soundEnabled)
        assertEquals("neon", fresh.themeId)
        assertEquals(3, fresh.themeIndex)
        assertFalse(fresh.followSystemTheme)
        assertEquals(320, fresh.keyboardHeight)
        assertEquals("right", fresh.oneHandedMode)
        assertEquals(56, fresh.suggestionBarHeight)
        assertEquals(12, fresh.keyCornerRadius)
        assertEquals(10, fresh.keySpacing)
        assertFalse(fresh.showEmojiKey)
        assertFalse(fresh.showVoiceKey)
        assertTrue(fresh.showClipboardKey)
        assertTrue(fresh.showNumberRow)
        assertTrue(fresh.incognitoMode)
        assertFalse(fresh.learningEnabled)
        assertFalse(fresh.clipboardHistoryEnabled)
        assertEquals(100, fresh.clipboardMaxItems)
        assertTrue(fresh.clipboardAutoClear)
    }

    @Test
    fun testResetToDefaults() {
        provider.autoCorrectEnabled = false
        provider.keyboardHeight = 350
        provider.themeIndex = 4
        provider.themeId = "cyberpunk"
        provider.suggestionBarHeight = 60
        provider.resetToDefaults()

        val fresh = KeyboardSettingsProvider(context)
        assertTrue(fresh.autoCorrectEnabled)
        assertEquals(280, fresh.keyboardHeight)
        assertEquals(0, fresh.themeIndex)
        assertEquals("akash_dark", fresh.themeId)
        assertEquals(48, fresh.suggestionBarHeight)
    }

    @Test
    fun testBoundsAndClamping() {
        provider.keyboardHeight = -50
        assertEquals(0, provider.keyboardHeight)

        provider.keyCornerRadius = -10
        assertEquals(0, provider.keyCornerRadius)

        provider.keySpacing = -5
        assertEquals(0, provider.keySpacing)

        provider.keyRepeatDelay = -100
        assertEquals(0, provider.keyRepeatDelay)

        provider.keyRepeatRate = -50
        assertEquals(0, provider.keyRepeatRate)

        provider.themeIndex = 10
        assertEquals(4, provider.themeIndex)

        provider.themeIndex = -2
        assertEquals(0, provider.themeIndex)

        provider.clipboardMaxItems = -10
        assertEquals(1, provider.clipboardMaxItems)
    }

    @Test
    fun testExportAndImportSettings() {
        provider.themeId = "midnight"
        provider.keyboardHeight = 310
        val exported = provider.exportSettings()
        assertTrue(exported.isNotEmpty())

        provider.resetToDefaults()
        assertEquals(280, provider.keyboardHeight)

        provider.importSettings(exported)
        assertEquals("midnight", provider.themeId)
        assertEquals(310, provider.keyboardHeight)
    }
}
