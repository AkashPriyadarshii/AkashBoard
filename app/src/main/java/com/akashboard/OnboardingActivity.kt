/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * OnboardingActivity.kt — First-run onboarding.
 *
 * Guides the user through:
 * 1. Enable AkashBoard in system settings
 * 2. Switch to AkashBoard as default keyboard
 * 3. Welcome and ready to type
 */

package com.akashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class OnboardingActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var actionButton: Button
    private lateinit var stepIndicator: TextView
    private var currentStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if keyboard is already enabled — skip onboarding
        if (isKeyboardEnabled() && isKeyboardSelected()) {
            launchMainApp()
            return
        }

        setContentView(createLayout())

        updateStep()
    }

    override fun onResume() {
        super.onResume()
        // Re-check when coming back from settings
        updateStep()
    }

    private fun createLayout(): View {
        val density = resources.displayMetrics.density

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(
                (32 * density).toInt(),
                (48 * density).toInt(),
                (32 * density).toInt(),
                (32 * density).toInt()
            )
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(0xFF111214.toInt())

            // App icon
            addView(TextView(context).apply {
                text = "⌨️"
                textSize = 64f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, (24 * density).toInt(), 0, (16 * density).toInt())
            })

            // Title
            addView(TextView(context).apply {
                text = "AkashBoard"
                textSize = 32f
                setTextColor(0xFFF2F3F5.toInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            })

            // Subtitle
            addView(TextView(context).apply {
                text = "The keyboard that becomes YOU."
                textSize = 16f
                setTextColor(0xFFA6ABB4.toInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, (8 * density).toInt(), 0, (48 * density).toInt())
            })

            // Step indicator
            stepIndicator = TextView(context).apply {
                text = "Step 1 of 2"
                textSize = 14f
                setTextColor(0xFF6C63FF.toInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, (16 * density).toInt())
            }
            addView(stepIndicator)

            // Status text
            statusText = TextView(context).apply {
                text = "Enable AkashBoard in your keyboard settings"
                textSize = 18f
                setTextColor(0xFFF2F3F5.toInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, (8 * density).toInt())
            }
            addView(statusText)

            // Description
            addView(TextView(context).apply {
                text = "Go to Settings → System → Languages & Input →\nOn-screen keyboard → Manage keyboards\nThen enable AkashBoard."
                textSize = 14f
                setTextColor(0xFF60656D.toInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, (32 * density).toInt())
            })

            // Action button
            actionButton = Button(context).apply {
                text = "Enable Keyboard"
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xFF6C63FF.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    (280 * density).toInt(),
                    (52 * density).toInt()
                )
                setOnClickListener { handleAction() }
            }
            addView(actionButton)

            // Skip button
            addView(Button(context).apply {
                text = "Skip for now"
                textSize = 14f
                setTextColor(0xFF60656D.toInt())
                setBackgroundColor(0x00000000)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (16 * density).toInt()
                }
                setOnClickListener { launchMainApp() }
            })
        }
    }

    private fun updateStep() {
        val enabled = isKeyboardEnabled()
        val selected = isKeyboardSelected()

        when {
            !enabled -> {
                currentStep = 0
                stepIndicator.text = "Step 1 of 2"
                statusText.text = "Enable AkashBoard"
                actionButton.text = "Enable Keyboard"
            }
            !selected -> {
                currentStep = 1
                stepIndicator.text = "Step 2 of 2"
                statusText.text = "Switch to AkashBoard"
                actionButton.text = "Switch Keyboard"
            }
            else -> {
                // Both done!
                statusText.text = "You're all set!"
                actionButton.text = "Start Typing"
                stepIndicator.text = "✓ Complete"
            }
        }
    }

    private fun handleAction() {
        when (currentStep) {
            0 -> {
                // Open keyboard settings
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            1 -> {
                // Show keyboard picker
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledIMEs = imm.enabledInputMethodList
        return enabledIMEs.any { it.packageName == packageName }
    }

    private fun isKeyboardSelected(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentIME = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return currentIME?.contains(packageName) == true
    }

    private fun launchMainApp() {
        // Mark onboarding as complete
        getSharedPreferences("akashboard_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_complete", true)
            .apply()

        // Launch settings
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }
}
