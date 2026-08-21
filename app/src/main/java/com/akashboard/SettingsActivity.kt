/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SettingsActivity.kt — Companion app settings.
 *
 * Uses AndroidX Preference fragments for standard settings UI.
 * Navigation: Main → Typing / Appearance / Privacy / About
 */

package com.akashboard

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.akashboard.settings.KeyboardSettingsProvider
import com.akashboard.settings.AboutFragment
import com.akashboard.settings.AppearanceFragment
import com.akashboard.settings.PrivacyFragment
import com.akashboard.settings.SettingsMainFragment
import com.akashboard.settings.TypingFragment

class SettingsActivity : AppCompatActivity() {

    lateinit var settingsProvider: KeyboardSettingsProvider
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsProvider = KeyboardSettingsProvider(this)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        if (savedInstanceState == null) {
            showFragment(com.akashboard.settings.SettingsMainFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
