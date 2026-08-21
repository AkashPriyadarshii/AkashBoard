/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * ThemeManager.kt — Theme loading and application.
 *
 * Manages built-in themes and custom themes.
 * Themes are stored as JSON in assets/ and internal storage.
 */

package com.akashboard.theme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import org.json.JSONObject

/**
 * Theme manager.
 *
 * Loads, caches, and applies themes.
 */
class ThemeManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "akashboard_themes", Context.MODE_PRIVATE
    )

    /** Currently active theme */
    private var activeTheme: ThemeConfig = BuiltInThemes.AKASH_DARK

    /** Cached resolved colors */
    private var cachedColors: ThemeColors = ThemeColors.from(activeTheme)

    /** Theme change listeners */
    private val listeners = mutableListOf<OnThemeChangedListener>()

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Get the currently active theme.
     */
    fun getActiveTheme(): ThemeConfig = activeTheme

    /**
     * Get resolved colors for the active theme.
     */
    fun getColors(): ThemeColors = cachedColors

    /**
     * Apply a theme by name.
     */
    fun applyTheme(name: String) {
        val theme = BuiltInThemes.getByName(name)
        if (theme != null) {
            activeTheme = theme
            cachedColors = ThemeColors.from(theme)
            prefs.edit().putString("active_theme", name).apply()
            notifyListeners()
        }
    }

    /**
     * Apply a theme config directly.
     */
    fun applyTheme(theme: ThemeConfig) {
        activeTheme = theme
        cachedColors = ThemeColors.from(theme)
        notifyListeners()
    }

    /**
     * Get all available themes.
     */
    fun getAvailableThemes(): List<ThemeConfig> = BuiltInThemes.ALL

    /**
     * Get the active theme name.
     */
    fun getActiveThemeName(): String = activeTheme.name

    /**
     * Load saved theme preference.
     */
    fun loadSavedTheme() {
        val savedName = prefs.getString("active_theme", "Akash Dark")
        val theme = BuiltInThemes.getByName(savedName ?: "Akash Dark")
        if (theme != null) {
            activeTheme = theme
            cachedColors = ThemeColors.from(theme)
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────────

    fun addListener(listener: OnThemeChangedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: OnThemeChangedListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.onThemeChanged(activeTheme, cachedColors)
        }
    }

    // ── JSON Parsing ──────────────────────────────────────────────────────

    companion object {
        /**
         * Parse a theme from JSON string.
         */
        fun parseTheme(json: String): ThemeConfig? {
            return try {
                val obj = JSONObject(json)

                val colorsObj = obj.getJSONObject("colors")
                val colors = ColorConfig(
                    canvas = Color.parseColor(colorsObj.getString("canvas")),
                    surface = Color.parseColor(colorsObj.getString("surface")),
                    surface2 = Color.parseColor(colorsObj.getString("surface2")),
                    key = Color.parseColor(colorsObj.getString("key")),
                    keyPressed = Color.parseColor(colorsObj.getString("keyPressed")),
                    text = Color.parseColor(colorsObj.getString("text")),
                    textSecondary = Color.parseColor(colorsObj.getString("textSecondary")),
                    accent = Color.parseColor(colorsObj.getString("accent")),
                    selection = Color.parseColor(colorsObj.optString("selection", "#6C63FF")),
                    cursor = Color.parseColor(colorsObj.optString("cursor", "#6C63FF")),
                    destructive = Color.parseColor(colorsObj.getString("destructive"))
                )

                val dimObj = obj.optJSONObject("dimensions")
                val dimensions = DimensionConfig(
                    cornerRadius = dimObj?.optDouble("cornerRadius", 8.0)?.toFloat() ?: 8f,
                    keyElevation = dimObj?.optDouble("keyElevation", 1.0)?.toFloat() ?: 1f,
                    keyPadding = dimObj?.optDouble("keyPadding", 4.0)?.toFloat() ?: 4f,
                    suggestionBarHeight = dimObj?.optDouble("suggestionBarHeight", 48.0)?.toFloat() ?: 48f
                )

                val animObj = obj.optJSONObject("animation")
                val animation = AnimationConfig(
                    pressScale = animObj?.optDouble("pressScale", 0.92)?.toFloat() ?: 0.92f,
                    pressDuration = animObj?.optLong("pressDuration", 80) ?: 80,
                    transitionDuration = animObj?.optLong("transitionDuration", 200) ?: 200
                )

                ThemeConfig(
                    name = obj.getString("name"),
                    version = obj.optInt("version", 1),
                    author = obj.optString("author", ""),
                    colors = colors,
                    dimensions = dimensions,
                    animation = animation
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * Theme change listener interface.
 */
interface OnThemeChangedListener {
    fun onThemeChanged(theme: ThemeConfig, colors: ThemeColors)
}

/**
 * Built-in themes.
 */
object BuiltInThemes {

    val AKASH_DARK = ThemeConfig(
        name = "Akash Dark",
        version = 1,
        author = "Akash Priyadarshi",
        colors = ColorConfig(
            canvas = Color.parseColor("#111214"),
            surface = Color.parseColor("#1A1C20"),
            surface2 = Color.parseColor("#24272C"),
            key = Color.parseColor("#2B2E34"),
            keyPressed = Color.parseColor("#363A42"),
            text = Color.parseColor("#F2F3F5"),
            textSecondary = Color.parseColor("#A6ABB4"),
            accent = Color.parseColor("#6C63FF"),
            selection = Color.parseColor("#6C63FF"),
            cursor = Color.parseColor("#6C63FF"),
            destructive = Color.parseColor("#FF6B60")
        )
    )

    val AKASH_LIGHT = ThemeConfig(
        name = "Akash Light",
        version = 1,
        author = "Akash Priyadarshi",
        colors = ColorConfig(
            canvas = Color.parseColor("#F5F6F8"),
            surface = Color.parseColor("#FFFFFF"),
            surface2 = Color.parseColor("#ECEEF2"),
            key = Color.parseColor("#E7E9ED"),
            keyPressed = Color.parseColor("#D9DCE2"),
            text = Color.parseColor("#15171A"),
            textSecondary = Color.parseColor("#60656D"),
            accent = Color.parseColor("#4A90D9"),
            selection = Color.parseColor("#4A90D9"),
            cursor = Color.parseColor("#4A90D9"),
            destructive = Color.parseColor("#B3261E")
        )
    )

    val NEON_CYBER = ThemeConfig(
        name = "Neon Cyber",
        version = 1,
        author = "Akash Priyadarshi",
        colors = ColorConfig(
            canvas = Color.parseColor("#0A0A0A"),
            surface = Color.parseColor("#111111"),
            surface2 = Color.parseColor("#1A1A1A"),
            key = Color.parseColor("#1E1E1E"),
            keyPressed = Color.parseColor("#2A2A2A"),
            text = Color.parseColor("#E0E0E0"),
            textSecondary = Color.parseColor("#888888"),
            accent = Color.parseColor("#00FF88"),
            selection = Color.parseColor("#00FF88"),
            cursor = Color.parseColor("#00FF88"),
            destructive = Color.parseColor("#FF4444")
        )
    )

    val MINIMAL = ThemeConfig(
        name = "Minimal",
        version = 1,
        author = "Akash Priyadarshi",
        colors = ColorConfig(
            canvas = Color.parseColor("#000000"),
            surface = Color.parseColor("#0A0A0A"),
            surface2 = Color.parseColor("#141414"),
            key = Color.parseColor("#1A1A1A"),
            keyPressed = Color.parseColor("#252525"),
            text = Color.parseColor("#FFFFFF"),
            textSecondary = Color.parseColor("#888888"),
            accent = Color.parseColor("#FFFFFF"),
            selection = Color.parseColor("#FFFFFF"),
            cursor = Color.parseColor("#FFFFFF"),
            destructive = Color.parseColor("#FF0000")
        )
    )

    val SUNSET = ThemeConfig(
        name = "Sunset",
        version = 1,
        author = "Akash Priyadarshi",
        colors = ColorConfig(
            canvas = Color.parseColor("#1A0A1E"),
            surface = Color.parseColor("#221028"),
            surface2 = Color.parseColor("#2A1830"),
            key = Color.parseColor("#322040"),
            keyPressed = Color.parseColor("#3E2850"),
            text = Color.parseColor("#F5E6FF"),
            textSecondary = Color.parseColor("#B89AC7"),
            accent = Color.parseColor("#FF6B35"),
            selection = Color.parseColor("#FF6B35"),
            cursor = Color.parseColor("#FF6B35"),
            destructive = Color.parseColor("#FF4444")
        )
    )

    val ALL = listOf(AKASH_DARK, AKASH_LIGHT, NEON_CYBER, MINIMAL, SUNSET)

    fun getByName(name: String): ThemeConfig? = ALL.find { it.name == name }
}
