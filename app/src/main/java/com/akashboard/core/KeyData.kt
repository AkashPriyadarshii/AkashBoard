/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyData.kt — Key data model for the keyboard.
 *
 * Every key on the keyboard is represented by a KeyData instance.
 * The model separates visual representation (rect) from touch input (hitRect)
 * to optimize both rendering and hit accuracy.
 */

package com.akashboard.core

import android.graphics.RectF

/**
 * Represents a single key on the keyboard.
 *
 * @param id Unique identifier (e.g., "key_q", "key_shift")
 * @param label Display label (e.g., "Q", "⇧", "⌫")
 * @param code Android key code (positive for characters, negative for special keys)
 * @param rect Visual bounding rectangle (what the user sees)
 * @param hitRect Touch hit rectangle (larger than rect for better accuracy)
 * @param type Key type classification
 * @param popupLabel Long-press popup label (e.g., "1" for "Q")
 * @param popupCode Long-press key code
 * @param width Weight multiplier (1.0 = standard, 1.5 = wide, etc.)
 * @param accessibilityLabel Screen reader label
 */
data class KeyData(
    val id: String,
    val label: String,
    val code: Int,
    val rect: RectF,
    val hitRect: RectF,
    val type: KeyType,
    val popupLabel: String? = null,
    val popupCode: Int? = null,
    val width: Float = 1.0f,
    val accessibilityLabel: String = label
)

/**
 * Key type classification.
 *
 * Used by InputHandler to determine behavior and by KeyboardView
 * to determine rendering style.
 */
enum class KeyType {
    /** Regular letter key (A-Z) */
    LETTER,

    /** Shift modifier (toggle capitalization) */
    SHIFT,

    /** Backspace / delete key */
    DELETE,

    /** Spacebar */
    SPACE,

    /** Enter / send / done key */
    ENTER,

    /** Symbol/number layout toggle */
    SYMBOLS,

    /** Emoji panel toggle */
    EMOJI,

    /** Voice input toggle */
    VOICE,

    /** Language switch (globe key) */
    LANGUAGE,

    /** Comma key */
    COMMA,

    /** Period key */
    PERIOD
}

/**
 * Shift state for the keyboard.
 */
enum class ShiftState {
    /** Lowercase */
    NONE,

    /** Single uppercase (next key only) */
    ONE,
    
    /** Caps lock (all uppercase until toggled) */
    LOCKED
}

/**
 * Key press result — what happened when a key was pressed.
 */
sealed class KeyPressResult {
    /** A character was committed to the text field */
    data class Character(val char: String) : KeyPressResult()

    /** A word was completed (space, punctuation) */
    data class WordCompleted(val word: String) : KeyPressResult()

    /** A suggestion was accepted */
    data class SuggestionAccepted(val word: String) : KeyPressResult()

    /** Backspace was pressed */
    object Backspace : KeyPressResult()

    /** Shift state changed */
    data class ShiftChanged(val state: ShiftState) : KeyPressResult()

    /** Layout changed (e.g., ABC → ?123) */
    data class LayoutChanged(val layout: KeyboardLayoutType) : KeyPressResult()

    /** Enter action performed */
    data class Enter(val action: Int) : KeyPressResult()

    /** No action (key was consumed but no output) */
    object None : KeyPressResult()
}

/**
 * Keyboard layout type.
 */
enum class KeyboardLayoutType {
    QWERTY,
    SYMBOLS,
    EMOJI,
    NUMBERS
}
