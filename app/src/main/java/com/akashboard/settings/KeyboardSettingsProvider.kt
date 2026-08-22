/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyboardSettingsProvider.kt — Central settings access.
 *
 * Provides typed access to all keyboard settings.
 * Uses SharedPreferences with default values.
 */

package com.akashboard.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Typed access to keyboard settings.
 *
 * Thread-safe for reads. Write operations should be called from main thread.
 */
class KeyboardSettingsProvider(context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // ── Typing ────────────────────────────────────────────────────────────

    var autoCorrectEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTOCORRECT, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTOCORRECT, value).apply()

    var predictiveTextEnabled: Boolean
        get() = prefs.getBoolean(KEY_PREDICTIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_PREDICTIVE, value).apply()

    var autoCorrectStrength: String
        get() = prefs.getString(KEY_AUTOCORRECT_STRENGTH, "medium") ?: "medium"
        set(value) = prefs.edit().putString(KEY_AUTOCORRECT_STRENGTH, value).apply()

    var swipeTypingEnabled: Boolean
        get() = prefs.getBoolean(KEY_SWIPE, true)
        set(value) = prefs.edit().putBoolean(KEY_SWIPE, value).apply()

    var spacebarCursorEnabled: Boolean
        get() = prefs.getBoolean(KEY_SPACEBAR_CURSOR, true)
        set(value) = prefs.edit().putBoolean(KEY_SPACEBAR_CURSOR, value).apply()

    var longPressRepeatEnabled: Boolean
        get() = prefs.getBoolean(KEY_LONG_PRESS_REPEAT, true)
        set(value) = prefs.edit().putBoolean(KEY_LONG_PRESS_REPEAT, value).apply()

    var keyRepeatDelay: Int
        get() = prefs.getString(KEY_REPEAT_DELAY, "300")?.toIntOrNull() ?: 300
        set(value) = prefs.edit().putString(KEY_REPEAT_DELAY, value.toString()).apply()

    var keyRepeatRate: Int
        get() = prefs.getString(KEY_REPEAT_RATE, "50")?.toIntOrNull() ?: 50
        set(value) = prefs.edit().putString(KEY_REPEAT_RATE, value.toString()).apply()

    var vibrateOnKeypress: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE, value).apply()

    var soundOnKeypress: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    // ── Appearance ────────────────────────────────────────────────────────

    var themeId: String
        get() = prefs.getString(KEY_THEME, "akash_dark") ?: "akash_dark"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var keyboardHeight: Int
        get() = prefs.getInt(KEY_HEIGHT, 280)
        set(value) = prefs.edit().putInt(KEY_HEIGHT, value).apply()

    var oneHandedMode: String
        get() = prefs.getString(KEY_ONE_HANDED, "off") ?: "off"
        set(value) = prefs.edit().putString(KEY_ONE_HANDED, value).apply()

    var keyCornerRadius: Int
        get() = prefs.getInt(KEY_CORNER_RADIUS, 8)
        set(value) = prefs.edit().putInt(KEY_CORNER_RADIUS, value).apply()

    var keySpacing: Int
        get() = prefs.getInt(KEY_SPACING, 6)
        set(value) = prefs.edit().putInt(KEY_SPACING, value).apply()

    var showEmojiKey: Boolean
        get() = prefs.getBoolean(KEY_SHOW_EMOJI, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_EMOJI, value).apply()

    var showVoiceKey: Boolean
        get() = prefs.getBoolean(KEY_SHOW_VOICE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_VOICE, value).apply()

    var showClipboardKey: Boolean
        get() = prefs.getBoolean(KEY_SHOW_CLIPBOARD, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_CLIPBOARD, value).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NUMBER_ROW, value).apply()

    // ── Privacy ───────────────────────────────────────────────────────────

    var incognitoMode: Boolean
        get() = prefs.getBoolean(KEY_INCOGNITO, false)
        set(value) = prefs.edit().putBoolean(KEY_INCOGNITO, value).apply()

    var learningEnabled: Boolean
        get() = prefs.getBoolean(KEY_LEARNING, true)
        set(value) = prefs.edit().putBoolean(KEY_LEARNING, value).apply()

    var clipboardHistoryEnabled: Boolean
        get() = prefs.getBoolean(KEY_CLIPBOARD_HISTORY, true)
        set(value) = prefs.edit().putBoolean(KEY_CLIPBOARD_HISTORY, value).apply()

    var clipboardMaxItems: Int
        get() = prefs.getString(KEY_CLIPBOARD_MAX, "50")?.toIntOrNull() ?: 50
        set(value) = prefs.edit().putString(KEY_CLIPBOARD_MAX, value.toString()).apply()

    var clipboardAutoClear: Boolean
        get() = prefs.getBoolean(KEY_CLIPBOARD_AUTO_CLEAR, false)
        set(value) = prefs.edit().putBoolean(KEY_CLIPBOARD_AUTO_CLEAR, value).apply()

    var networkAccess: Boolean
        get() = prefs.getBoolean(KEY_NETWORK, false)
        set(value) = prefs.edit().putBoolean(KEY_NETWORK, value).apply()

    // ── Bulk Operations ───────────────────────────────────────────────────

    fun resetToDefaults() {
        // Only clear keyboard-specific keys, not all SharedPreferences
        val editor = prefs.edit()
        editor.remove(KEY_AUTOCORRECT)
        editor.remove(KEY_PREDICTIVE)
        editor.remove(KEY_AUTOCORRECT_STRENGTH)
        editor.remove(KEY_SWIPE)
        editor.remove(KEY_SPACEBAR_CURSOR)
        editor.remove(KEY_LONG_PRESS_REPEAT)
        editor.remove(KEY_REPEAT_DELAY)
        editor.remove(KEY_REPEAT_RATE)
        editor.remove(KEY_VIBRATE)
        editor.remove(KEY_SOUND)
        editor.remove(KEY_THEME)
        editor.remove(KEY_HEIGHT)
        editor.remove(KEY_ONE_HANDED)
        editor.remove(KEY_CORNER_RADIUS)
        editor.remove(KEY_SPACING)
        editor.remove(KEY_SHOW_EMOJI)
        editor.remove(KEY_SHOW_VOICE)
        editor.remove(KEY_SHOW_CLIPBOARD)
        editor.remove(KEY_SHOW_NUMBER_ROW)
        editor.remove(KEY_INCOGNITO)
        editor.remove(KEY_LEARNING)
        editor.remove(KEY_CLIPBOARD_HISTORY)
        editor.remove(KEY_CLIPBOARD_MAX)
        editor.remove(KEY_CLIPBOARD_AUTO_CLEAR)
        editor.remove(KEY_NETWORK)
        editor.apply()
    }

    fun exportSettings(): Map<String, *> = prefs.all

    fun importSettings(data: Map<String, *>) {
        val editor = prefs.edit()
        data.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is String -> editor.putString(key, value)
            }
        }
        editor.apply()
    }

    // ── Listener ──────────────────────────────────────────────────────────

    fun addOnSettingsChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun removeOnSettingsChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        // Typing
        private const val KEY_AUTOCORRECT = "autocorrect_enabled"
        private const val KEY_PREDICTIVE = "predictive_text_enabled"
        private const val KEY_AUTOCORRECT_STRENGTH = "autocorrect_strength"
        private const val KEY_SWIPE = "swipe_typing_enabled"
        private const val KEY_SPACEBAR_CURSOR = "spacebar_cursor_enabled"
        private const val KEY_LONG_PRESS_REPEAT = "long_press_repeat_enabled"
        private const val KEY_REPEAT_DELAY = "key_repeat_delay"
        private const val KEY_REPEAT_RATE = "key_repeat_rate"
        private const val KEY_VIBRATE = "vibrate_on_keypress"
        private const val KEY_SOUND = "sound_on_keypress"

        // Appearance
        private const val KEY_THEME = "theme_id"
        private const val KEY_HEIGHT = "keyboard_height"
        private const val KEY_ONE_HANDED = "one_handed_mode"
        private const val KEY_CORNER_RADIUS = "key_corner_radius"
        private const val KEY_SPACING = "key_spacing"
        private const val KEY_SHOW_EMOJI = "show_emoji_key"
        private const val KEY_SHOW_VOICE = "show_voice_key"
        private const val KEY_SHOW_CLIPBOARD = "show_clipboard_key"
        private const val KEY_SHOW_NUMBER_ROW = "show_number_row"

        // Privacy
        private const val KEY_INCOGNITO = "incognito_mode"
        private const val KEY_LEARNING = "learning_enabled"
        private const val KEY_CLIPBOARD_HISTORY = "clipboard_history_enabled"
        private const val KEY_CLIPBOARD_MAX = "clipboard_max_items"
        private const val KEY_CLIPBOARD_AUTO_CLEAR = "clipboard_auto_clear"
        private const val KEY_NETWORK = "network_access"
    }
}
