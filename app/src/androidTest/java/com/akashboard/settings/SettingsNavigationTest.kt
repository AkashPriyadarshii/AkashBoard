/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SettingsNavigationTest.kt — Tests for settings activity, fragments,
 * preference persistence, and theme switching.
 */

package com.akashboard.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsNavigationTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var settingsProvider: KeyboardSettingsProvider

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        prefs = context.getSharedPreferences("akashboard_prefs", Context.MODE_PRIVATE)
        settingsProvider = KeyboardSettingsProvider(context)
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 1: Settings Persistence
    // ════════════════════════════════════════════════════════════════

    @Test
    fun themeIndex_persists() {
        settingsProvider.themeIndex = 3
        val loaded = KeyboardSettingsProvider(context)
        assertEquals(3, loaded.themeIndex)
    }

    @Test
    fun vibrateEnabled_persists() {
        settingsProvider.vibrateEnabled = false
        val loaded = KeyboardSettingsProvider(context)
        assertFalse(loaded.vibrateEnabled)
    }

    @Test
    fun soundEnabled_persists() {
        settingsProvider.soundEnabled = true
        val loaded = KeyboardSettingsProvider(context)
        assertTrue(loaded.soundEnabled)
    }

    @Test
    fun autocorrectStrength_persists() {
        settingsProvider.autocorrectStrength = 0.75f
        val loaded = KeyboardSettingsProvider(context)
        assertEquals(0.75f, loaded.autocorrectStrength, 0.01f)
    }

    @Test
    fun keyRepeatDelay_persists() {
        settingsProvider.keyRepeatDelay = 500L
        val loaded = KeyboardSettingsProvider(context)
        assertEquals(500L, loaded.keyRepeatDelay)
    }

    @Test
    fun keyRepeatRate_persists() {
        settingsProvider.keyRepeatRate = 30L
        val loaded = KeyboardSettingsProvider(context)
        assertEquals(30L, loaded.keyRepeatRate)
    }

    @Test
    fun showEmojiKey_persists() {
        settingsProvider.showEmojiKey = false
        val loaded = KeyboardSettingsProvider(context)
        assertFalse(loaded.showEmojiKey)
    }

    @Test
    fun showVoiceKey_persists() {
        settingsProvider.showVoiceKey = false
        val loaded = KeyboardSettingsProvider(context)
        assertFalse(loaded.showVoiceKey)
    }

    @Test
    fun showClipboardKey_persists() {
        settingsProvider.showClipboardKey = false
        val loaded = KeyboardSettingsProvider(context)
        assertFalse(loaded.showClipboardKey)
    }

    @Test
    fun keyboardHeight_persists() {
        settingsProvider.keyboardHeight = 250
        val loaded = KeyboardSettingsProvider(context)
        assertEquals(250, loaded.keyboardHeight)
    }

    @Test
    fun keyCornerRadius_persists() {
        settingsProvider.keyCornerRadius = 16f
        val loaded = KeyboardSettingsProvider(context)
        assertEquals(16f, loaded.keyCornerRadius, 0.01f)
    }

    @Test
    fun keySpacing_persists() {
        settingsProvider.keySpacing = 8f
        val loaded = KeyboardSettingsProvider(context)
        assertEquals(8f, loaded.keySpacing, 0.01f)
    }

    @Test
    fun oneHandedMode_persists() {
        settingsProvider.oneHandedMode = true
        val loaded = KeyboardSettingsProvider(context)
        assertTrue(loaded.oneHandedMode)
    }

    @Test
    fun followSystemTheme_persists() {
        settingsProvider.followSystemTheme = false
        val loaded = KeyboardSettingsProvider(context)
        assertFalse(loaded.followSystemTheme)
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 2: Settings Defaults
    // ════════════════════════════════════════════════════════════════

    @Test
    fun defaultThemeIndex_isZero() {
        val fresh = KeyboardSettingsProvider(context)
        assertEquals(0, fresh.themeIndex)
    }

    @Test
    fun defaultVibrateEnabled_isTrue() {
        val fresh = KeyboardSettingsProvider(context)
        assertTrue(fresh.vibrateEnabled)
    }

    @Test
    fun defaultSoundEnabled_isFalse() {
        val fresh = KeyboardSettingsProvider(context)
        assertFalse(fresh.soundEnabled)
    }

    @Test
    fun defaultAutocorrectStrength_isHalf() {
        val fresh = KeyboardSettingsProvider(context)
        assertEquals(0.5f, fresh.autocorrectStrength, 0.01f)
    }

    @Test
    fun defaultKeyRepeatDelay_is300ms() {
        val fresh = KeyboardSettingsProvider(context)
        assertEquals(300L, fresh.keyRepeatDelay)
    }

    @Test
    fun defaultKeyRepeatRate_is50ms() {
        val fresh = KeyboardSettingsProvider(context)
        assertEquals(50L, fresh.keyRepeatRate)
    }

    @Test
    fun defaultShowEmojiKey_isTrue() {
        val fresh = KeyboardSettingsProvider(context)
        assertTrue(fresh.showEmojiKey)
    }

    @Test
    fun defaultShowVoiceKey_isTrue() {
        val fresh = KeyboardSettingsProvider(context)
        assertTrue(fresh.showVoiceKey)
    }

    @Test
    fun defaultShowClipboardKey_isTrue() {
        val fresh = KeyboardSettingsProvider(context)
        assertTrue(fresh.showClipboardKey)
    }

    @Test
    fun defaultFollowSystemTheme_isTrue() {
        val fresh = KeyboardSettingsProvider(context)
        assertTrue(fresh.followSystemTheme)
    }

    @Test
    fun defaultOneHandedMode_isFalse() {
        val fresh = KeyboardSettingsProvider(context)
        assertFalse(fresh.oneHandedMode)
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 3: Reset to Defaults
    // ════════════════════════════════════════════════════════════════

    @Test
    fun resetToDefaults_restoresAll() {
        // Change everything
        settingsProvider.themeIndex = 5
        settingsProvider.vibrateEnabled = false
        settingsProvider.soundEnabled = true
        settingsProvider.autocorrectStrength = 0.99f
        settingsProvider.keyRepeatDelay = 999L
        settingsProvider.keyRepeatRate = 1L
        settingsProvider.showEmojiKey = false
        settingsProvider.showVoiceKey = false
        settingsProvider.showClipboardKey = false
        settingsProvider.oneHandedMode = true
        settingsProvider.followSystemTheme = false

        // Reset
        settingsProvider.resetToDefaults()

        // Verify all defaults
        val fresh = KeyboardSettingsProvider(context)
        assertEquals(0, fresh.themeIndex)
        assertTrue(fresh.vibrateEnabled)
        assertFalse(fresh.soundEnabled)
        assertEquals(0.5f, fresh.autocorrectStrength, 0.01f)
        assertEquals(300L, fresh.keyRepeatDelay)
        assertEquals(50L, fresh.keyRepeatRate)
        assertTrue(fresh.showEmojiKey)
        assertTrue(fresh.showVoiceKey)
        assertTrue(fresh.showClipboardKey)
        assertFalse(fresh.oneHandedMode)
        assertTrue(fresh.followSystemTheme)
    }

    @Test
    fun resetToDefaults_doesNotWipeOtherPrefs() {
        // Write a custom pref (not from settings provider)
        prefs.edit().putString("custom_key", "custom_value").apply()

        // Reset settings
        settingsProvider.resetToDefaults()

        // Custom pref should survive (resetToDefaults only clears known keys)
        assertEquals("custom_value", prefs.getString("custom_key", null))
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 4: Theme Switching
    // ════════════════════════════════════════════════════════════════

    @Test
    fun themeIndex_rangeCheck() {
        // Valid indices: 0-4 (5 themes)
        settingsProvider.themeIndex = 0
        assertEquals(0, settingsProvider.themeIndex)
        settingsProvider.themeIndex = 4
        assertEquals(4, settingsProvider.themeIndex)
    }

    @Test
    fun followSystemTheme_toggling() {
        settingsProvider.followSystemTheme = true
        assertTrue(settingsProvider.followSystemTheme)
        settingsProvider.followSystemTheme = false
        assertFalse(settingsProvider.followSystemTheme)
        settingsProvider.followSystemTheme = true
        assertTrue(settingsProvider.followSystemTheme)
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 5: Settings Validation
    // ════════════════════════════════════════════════════════════════

    @Test
    fun autocorrectStrength_clamped() {
        settingsProvider.autocorrectStrength = 1.5f
        // Should be clamped to 0.0-1.0
        val loaded = KeyboardSettingsProvider(context)
        assertTrue("Strength should be <= 1.0", loaded.autocorrectStrength <= 1.0f)
    }

    @Test
    fun keyRepeatDelay_positive() {
        settingsProvider.keyRepeatDelay = -100L
        val loaded = KeyboardSettingsProvider(context)
        assertTrue("Delay should be >= 0", loaded.keyRepeatDelay >= 0L)
    }

    @Test
    fun keyboardHeight_positive() {
        settingsProvider.keyboardHeight = -50
        val loaded = KeyboardSettingsProvider(context)
        assertTrue("Height should be >= 0", loaded.keyboardHeight >= 0)
    }

    @Test
    fun keyCornerRadius_positive() {
        settingsProvider.keyCornerRadius = -5f
        val loaded = KeyboardSettingsProvider(context)
        assertTrue("Corner radius should be >= 0", loaded.keyCornerRadius >= 0f)
    }
}
