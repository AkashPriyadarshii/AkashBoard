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

class AppearanceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_appearance, rootKey)

        // Theme preview listener
        findPreference<androidx.preference.Preference>("theme_preview")?.setOnPreferenceClickListener {
            // Could open a theme preview dialog
            true
        }
    }
}
