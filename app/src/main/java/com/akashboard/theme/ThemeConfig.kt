/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * ThemeConfig.kt — Theme data model.
 *
 * Defines the structure of a theme. Themes are JSON files
 * that can be imported/exported/shared.
 *
 * Minimum tokens (from DESIGN.md):
 *   canvas, surface, surface-2, key, keyPressed,
 *   text, secondaryText, accent, selection, cursor, destructive
 */

package com.akashboard.theme

import android.graphics.Color

/**
 * Complete theme configuration.
 *
 * @param name Display name (e.g., "Akash Dark")
 * @param version Schema version (currently 1)
 * @param author Theme author
 * @param colors Color tokens
 * @param dimensions Dimension tokens
 * @param animation Animation tokens
 */
data class ThemeConfig(
    val name: String,
    val version: Int = 1,
    val author: String = "",
    val colors: ColorConfig,
    val dimensions: DimensionConfig = DimensionConfig(),
    val animation: AnimationConfig = AnimationConfig()
)

/**
 * Color tokens for the theme.
 *
 * All colors are stored as ARGB integers.
 * Use Color.parseColor() or .toArgb() to create them.
 */
data class ColorConfig(
    val canvas: Int,
    val surface: Int,
    val surface2: Int,
    val key: Int,
    val keyPressed: Int,
    val text: Int,
    val textSecondary: Int,
    val accent: Int,
    val selection: Int,
    val cursor: Int,
    val destructive: Int
)

/**
 * Dimension tokens.
 */
data class DimensionConfig(
    val cornerRadius: Float = 8f,
    val keyElevation: Float = 1f,
    val keyPadding: Float = 4f,
    val suggestionBarHeight: Float = 48f
)

/**
 * Animation tokens.
 */
data class AnimationConfig(
    val pressScale: Float = 0.92f,
    val pressDuration: Long = 80,
    val transitionDuration: Long = 200
)

/**
 * Theme color resolver.
 *
 * Extracts colors from ThemeConfig into a flat map
 * for easy access by KeyboardView and other components.
 */
data class ThemeColors(
    val keyBg: Int,
    val keyPressed: Int,
    val keyBorder: Int,
    val keyText: Int,
    val accent: Int,
    val suggestionBg: Int,
    val suggestionText: Int,
    val canvas: Int
) {
    companion object {
        /**
         * Create ThemeColors from a ThemeConfig.
         */
        fun from(config: ThemeConfig): ThemeColors {
            val c = config.colors
            return ThemeColors(
                keyBg = c.key,
                keyPressed = c.keyPressed,
                keyBorder = withAlpha(c.surface2, 0x1A),  // 10% opacity
                keyText = c.text,
                accent = c.accent,
                suggestionBg = withAlpha(c.surface, 0x0D),  // 5% opacity
                suggestionText = c.textSecondary,
                canvas = c.canvas
            )
        }

        private fun withAlpha(color: Int, alpha: Int): Int {
            return (color and 0x00FFFFFF) or (alpha shl 24)
        }
    }
}
