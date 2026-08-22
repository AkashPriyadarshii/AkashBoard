/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AppearanceFragment.kt — Appearance settings.
 *
 * Controls themes, keyboard height, one-handed mode, and layout.
 */

package com.akashboard.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.akashboard.R
import com.akashboard.theme.ThemeManager

class AppearanceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_appearance, rootKey)

        // Theme preview: render sample keys with the active theme's colors
        findPreference<androidx.preference.Preference>("theme_preview")?.setOnPreferenceClickListener {
            val colors = ThemeManager(this@AppearanceFragment.requireContext().applicationContext).apply {
                loadSavedTheme()
            }.getColors()
            showThemePreview(colors)
            true
        }
    }

    private fun showThemePreview(colors: com.akashboard.theme.ThemeColors) {
        val density = resources.displayMetrics.density
        val swatches = listOf(
            "Canvas" to colors.canvas,
            "Keys" to colors.keyBg,
            "Pressed" to colors.keyPressed,
            "Text" to colors.keyText,
            "Accent" to colors.accent,
            "Suggestions" to colors.suggestionBg
        )
        val label = buildString {
            append("Active theme colors:\n")
            swatches.forEach { (name, color) ->
                append("\n■ $name  #${Integer.toHexString(color and 0xFFFFFF).padStart(6, '0')}")
            }
        }
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Theme Preview")
            .setMessage(label)
            .setPositiveButton("OK", null)
            .show()
    }
}
