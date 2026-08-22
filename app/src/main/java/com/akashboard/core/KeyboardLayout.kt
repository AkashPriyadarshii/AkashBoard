/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyboardLayout.kt — Layout definitions for the keyboard.
 *
 * Defines the QWERTY layout and calculates key positions based on
 * screen width. Layout is dynamic — not hard-coded to one screen size.
 */

package com.akashboard.core

import android.graphics.RectF

/**
 * A row of keys in the keyboard.
 */
data class KeyRow(
    val keys: List<KeySpec>
)

/**
 * Key specification — defines a key's properties without position.
 * Position is calculated at render time based on screen width.
 */
data class KeySpec(
    val label: String,
    val code: Int,
    val type: KeyType,
    val width: Float = 1.0f,
    val popupLabel: String? = null,
    val popupCode: Int? = null
)

/**
 * Keyboard layout definition.
 *
 * Contains all rows and their key specifications.
 * Actual pixel positions are calculated by [calculateKeyPositions].
 */
data class KeyboardLayoutDef(
    val type: KeyboardLayoutType,
    val rows: List<KeyRow>
)

/**
 * Calculated key positions for a specific screen width.
 *
 * @param keys List of all keys with calculated positions
 * @param totalHeight Total keyboard height in pixels
 */
data class CalculatedLayout(
    val keys: List<KeyData>,
    val totalHeight: Float
)

/**
 * Layout calculator — converts layout definitions to pixel positions.
 *
 * Adapts to any screen width. Keys grow/shrink proportionally.
 */
object LayoutCalculator {

    private const val DEFAULT_KEY_GAP_DP = 6f
    private const val DEFAULT_KEY_HEIGHT_DP = 46f
    private const val BOTTOM_ROW_HEIGHT_DP = 52f
    private const val SUGGESTION_BAR_HEIGHT_DP = 48f
    private const val HITBOX_EXPANSION_DP = 4f

    /** Current overrides in dp; null uses defaults. */
    @Volatile var keyGapOverrideDp: Float? = null
    @Volatile var keyHeightOverrideDp: Float? = null

    /** One-handed mode: fraction of screen width the keyboard occupies, and side offset */
    @Volatile var keyboardWidthFractionOverride: Float? = null
    @Volatile var keyboardSideOffsetFraction: Float = 0f

    /**
     * Calculate key positions for a given screen width.
     *
     * @param layout The layout definition
     * @param screenWidth Available width in pixels
     * @param density Device density (dp → px multiplier)
     * @return CalculatedLayout with positioned keys
     */
    fun calculate(layout: KeyboardLayoutDef, screenWidth: Float, density: Float): CalculatedLayout {
        // One-handed mode: shrink usable width, shift toward thumb side
        val widthFraction = keyboardWidthFractionOverride ?: 1f
        val sideOffset = (screenWidth - screenWidth * widthFraction) * keyboardSideOffsetFraction
        val usableWidth = screenWidth * widthFraction

        val keyGap = (keyGapOverrideDp ?: DEFAULT_KEY_GAP_DP) * density
        val keyHeight = (keyHeightOverrideDp ?: DEFAULT_KEY_HEIGHT_DP) * density
        val bottomRowHeight = BOTTOM_ROW_HEIGHT_DP * density
        val suggestionBarHeight = SUGGESTION_BAR_HEIGHT_DP * density
        val hitboxExpansion = HITBOX_EXPANSION_DP * density

        val keys = mutableListOf<KeyData>()
        var yOffset = suggestionBarHeight

        for ((rowIndex, row) in layout.rows.withIndex()) {
            val isBottomRow = rowIndex == layout.rows.size - 1
            val rowHeight = if (isBottomRow) bottomRowHeight else keyHeight

            // Calculate this row's total weight independently
            val rowWeight = row.keys.sumOf { it.width.toDouble() }.toFloat()

            // Key width = available space / row weight
            // Available = usable width minus gaps (gap before first key + gap after each key)
            val availableWidth = usableWidth - keyGap * (row.keys.size + 1)
            val standardKeyWidth = availableWidth / rowWeight

            var xOffset = sideOffset + keyGap

            for (spec in row.keys) {
                val keyWidth = standardKeyWidth * spec.width

                val rect = RectF(
                    xOffset,
                    yOffset,
                    xOffset + keyWidth,
                    yOffset + rowHeight
                )

                // Hitbox is larger than visual key
                val hitRect = RectF(
                    rect.left - hitboxExpansion,
                    rect.top - hitboxExpansion,
                    rect.right + hitboxExpansion,
                    rect.bottom + hitboxExpansion
                )

                val id = "key_${spec.label.lowercase().replace(" ", "_")}"

                keys.add(
                    KeyData(
                        id = id,
                        label = spec.label,
                        code = spec.code,
                        rect = rect,
                        hitRect = hitRect,
                        type = spec.type,
                        popupLabel = spec.popupLabel,
                        popupCode = spec.popupCode,
                        width = spec.width,
                        accessibilityLabel = getAccessibilityLabel(spec)
                    )
                )

                xOffset += keyWidth + keyGap
            }

            yOffset += rowHeight + keyGap
        }

        return CalculatedLayout(keys = keys, totalHeight = yOffset)
    }

    /**
     * Get accessibility label for a key.
     */
    private fun getAccessibilityLabel(spec: KeySpec): String {
        return when (spec.type) {
            KeyType.SHIFT -> "Shift"
            KeyType.DELETE -> "Delete"
            KeyType.SPACE -> "Space"
            KeyType.ENTER -> "Enter"
            KeyType.SYMBOLS -> "Symbol keyboard"
            KeyType.EMOJI -> "Emoji keyboard"
            KeyType.VOICE -> "Voice input"
            KeyType.LANGUAGE -> "Switch language"
            KeyType.COMMA -> "Comma"
            KeyType.PERIOD -> "Period"
            KeyType.LETTER -> spec.label
        }
    }
}

/**
 * Pre-defined keyboard layouts.
 */
object KeyboardLayouts {

    /**
     * Standard QWERTY layout for English.
     */
    val QWERTY = KeyboardLayoutDef(
        type = KeyboardLayoutType.QWERTY,
        rows = listOf(
            // Row 1: Q W E R T Y U I O P
            KeyRow(
                keys = listOf(
                    KeySpec("Q", 'Q'.code, KeyType.LETTER, 1.0f),
                    KeySpec("W", 'W'.code, KeyType.LETTER, 1.0f),
                    KeySpec("E", 'E'.code, KeyType.LETTER, 1.0f),
                    KeySpec("R", 'R'.code, KeyType.LETTER, 1.0f),
                    KeySpec("T", 'T'.code, KeyType.LETTER, 1.0f),
                    KeySpec("Y", 'Y'.code, KeyType.LETTER, 1.0f),
                    KeySpec("U", 'U'.code, KeyType.LETTER, 1.0f),
                    KeySpec("I", 'I'.code, KeyType.LETTER, 1.0f),
                    KeySpec("O", 'O'.code, KeyType.LETTER, 1.0f),
                    KeySpec("P", 'P'.code, KeyType.LETTER, 1.0f)
                )
            ),
            // Row 2: A S D F G H J K L
            KeyRow(
                keys = listOf(
                    KeySpec("A", 'A'.code, KeyType.LETTER, 1.0f),
                    KeySpec("S", 'S'.code, KeyType.LETTER, 1.0f),
                    KeySpec("D", 'D'.code, KeyType.LETTER, 1.0f),
                    KeySpec("F", 'F'.code, KeyType.LETTER, 1.0f),
                    KeySpec("G", 'G'.code, KeyType.LETTER, 1.0f),
                    KeySpec("H", 'H'.code, KeyType.LETTER, 1.0f),
                    KeySpec("J", 'J'.code, KeyType.LETTER, 1.0f),
                    KeySpec("K", 'K'.code, KeyType.LETTER, 1.0f),
                    KeySpec("L", 'L'.code, KeyType.LETTER, 1.0f)
                )
            ),
            // Row 3: ⇧ Z X C V B N M ⌫
            KeyRow(
                keys = listOf(
                    KeySpec("⇧", KeyCodes.SHIFT, KeyType.SHIFT, 1.5f),
                    KeySpec("Z", 'Z'.code, KeyType.LETTER, 1.0f),
                    KeySpec("X", 'X'.code, KeyType.LETTER, 1.0f),
                    KeySpec("C", 'C'.code, KeyType.LETTER, 1.0f),
                    KeySpec("V", 'V'.code, KeyType.LETTER, 1.0f),
                    KeySpec("B", 'B'.code, KeyType.LETTER, 1.0f),
                    KeySpec("N", 'N'.code, KeyType.LETTER, 1.0f),
                    KeySpec("M", 'M'.code, KeyType.LETTER, 1.0f),
                    KeySpec("⌫", KeyCodes.DELETE, KeyType.DELETE, 1.5f)
                )
            ),
            // Row 4: ?123 🌐 😊 [SPACE] , ⏎
            KeyRow(
                keys = listOf(
                    KeySpec("?123", KeyCodes.SYMBOLS, KeyType.SYMBOLS, 1.3f),
                    KeySpec("🌐", KeyCodes.LANGUAGE, KeyType.LANGUAGE, 1.2f),
                    KeySpec("😊", KeyCodes.EMOJI, KeyType.EMOJI, 1.2f),
                    KeySpec(" ", KeyCodes.SPACE, KeyType.SPACE, 4.0f),
                    KeySpec(",", ','.code, KeyType.COMMA, 1.0f),
                    KeySpec("⏎", KeyCodes.ENTER, KeyType.ENTER, 1.8f)
                )
            )
        )
    )

    /**
     * Symbol/number layout.
     */
    val SYMBOLS = KeyboardLayoutDef(
        type = KeyboardLayoutType.SYMBOLS,
        rows = listOf(
            KeyRow(
                keys = listOf(
                    KeySpec("1", '1'.code, KeyType.LETTER, 1.0f),
                    KeySpec("2", '2'.code, KeyType.LETTER, 1.0f),
                    KeySpec("3", '3'.code, KeyType.LETTER, 1.0f),
                    KeySpec("4", '4'.code, KeyType.LETTER, 1.0f),
                    KeySpec("5", '5'.code, KeyType.LETTER, 1.0f),
                    KeySpec("6", '6'.code, KeyType.LETTER, 1.0f),
                    KeySpec("7", '7'.code, KeyType.LETTER, 1.0f),
                    KeySpec("8", '8'.code, KeyType.LETTER, 1.0f),
                    KeySpec("9", '9'.code, KeyType.LETTER, 1.0f),
                    KeySpec("0", '0'.code, KeyType.LETTER, 1.0f)
                )
            ),
            KeyRow(
                keys = listOf(
                    KeySpec("!", '!'.code, KeyType.LETTER, 1.0f),
                    KeySpec("@", '@'.code, KeyType.LETTER, 1.0f),
                    KeySpec("#", '#'.code, KeyType.LETTER, 1.0f),
                    KeySpec("$", '$'.code, KeyType.LETTER, 1.0f),
                    KeySpec("%", '%'.code, KeyType.LETTER, 1.0f),
                    KeySpec("&", '&'.code, KeyType.LETTER, 1.0f),
                    KeySpec("*", '*'.code, KeyType.LETTER, 1.0f),
                    KeySpec("(", '('.code, KeyType.LETTER, 1.0f),
                    KeySpec(")", ')'.code, KeyType.LETTER, 1.0f)
                )
            ),
            KeyRow(
                keys = listOf(
                    KeySpec("⇧", KeyCodes.SHIFT, KeyType.SHIFT, 1.5f),
                    KeySpec("-", '-'.code, KeyType.LETTER, 1.0f),
                    KeySpec("_", '_'.code, KeyType.LETTER, 1.0f),
                    KeySpec("=", '='.code, KeyType.LETTER, 1.0f),
                    KeySpec("+", '+'.code, KeyType.LETTER, 1.0f),
                    KeySpec("[", '['.code, KeyType.LETTER, 1.0f),
                    KeySpec("]", ']'.code, KeyType.LETTER, 1.0f),
                    KeySpec("{", '{'.code, KeyType.LETTER, 1.0f),
                    KeySpec("⌫", KeyCodes.DELETE, KeyType.DELETE, 1.5f)
                )
            ),
            KeyRow(
                keys = listOf(
                    KeySpec("ABC", KeyCodes.QWERTY, KeyType.SYMBOLS, 1.3f),
                    KeySpec("/", '/'.code, KeyType.LETTER, 1.0f),
                    KeySpec("\\", '\\'.code, KeyType.LETTER, 1.0f),
                    KeySpec("|", '|'.code, KeyType.LETTER, 1.0f),
                    KeySpec(" ", KeyCodes.SPACE, KeyType.SPACE, 3.0f),
                    KeySpec(".", '.'.code, KeyType.PERIOD, 1.0f),
                    KeySpec("⏎", KeyCodes.ENTER, KeyType.ENTER, 1.8f)
                )
            )
        )
    )
}

/**
 * Custom key codes (negative values to avoid collision with Android key codes).
 */
object KeyCodes {
    const val SHIFT = -100
    const val DELETE = -101
    const val SPACE = 32
    const val ENTER = -102
    const val SYMBOLS = -103
    const val LANGUAGE = -104
    const val EMOJI = -105
    const val QWERTY = -106
    const val COMMA = 44
    const val PERIOD = 46
}
