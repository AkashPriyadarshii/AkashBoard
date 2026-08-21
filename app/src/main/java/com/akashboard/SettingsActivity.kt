/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SettingsActivity.kt — Companion configuration app entry point.
 * This is the launcher activity that guides users to enable AkashBoard.
 */

package com.akashboard

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.graphics.Color
import android.util.TypedValue

class SettingsActivity : android.app.Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(32), dp(32), dp(32), dp(32))
            setBackgroundColor(Color.parseColor("#111214"))
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Title
        val title = TextView(this).apply {
            text = "AkashBoard"
            setTextColor(Color.parseColor("#F2F3F5"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            gravity = Gravity.CENTER
            setPadding(0, dp(48), 0, dp(16))
        }
        layout.addView(title, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        // Subtitle
        val subtitle = TextView(this).apply {
            text = "The keyboard that becomes YOU."
            setTextColor(Color.parseColor("#A6ABB4"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(48))
        }
        layout.addView(subtitle, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        // Enable button
        val enableButton = Button(this).apply {
            text = "Enable AkashBoard"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#6C63FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setPadding(dp(24), dp(16), dp(24), dp(16))
            setOnClickListener {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
        layout.addView(enableButton, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            setMargins(0, 0, 0, dp(16))
        })

        // Switch button
        val switchButton = Button(this).apply {
            text = "Switch to AkashBoard"
            setTextColor(Color.parseColor("#F2F3F5"))
            setBackgroundColor(Color.parseColor("#2B2E34"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setPadding(dp(24), dp(16), dp(24), dp(16))
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
        layout.addView(switchButton, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        setContentView(layout)
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
