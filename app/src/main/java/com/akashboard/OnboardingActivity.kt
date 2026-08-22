/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * OnboardingActivity.kt — First-run onboarding.
 */

package com.akashboard

import android.content.Context
import android.content.Intent
import android.graphics.Color

import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var actionButton: MaterialButton
    private lateinit var stepIndicator: TextView
    private lateinit var instructionsText: TextView
    private var currentStep = 0
    private var allDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isKeyboardEnabled() && isKeyboardSelected()) {
            launchMainApp()
            return
        }

        setContentView(createLayout())
        updateStep()
    }

    override fun onResume() {
        super.onResume()
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

            // Spacer top
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0, 1f
                )
            })

            // App icon
            addView(TextView(context).apply {
                text = "⌨️"
                textSize = 64f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, (16 * density).toInt())
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

            // Description — per-step instructions, updated by updateStep()
            instructionsText = TextView(context).apply {
                text = ""
                textSize = 14f
                setTextColor(0xFF60656D.toInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, (32 * density).toInt())
            }
            addView(instructionsText)

            // Action button
            actionButton = MaterialButton(context).apply {
                text = "Enable Keyboard"
                textSize = 16f
                setTextColor(Color.WHITE)
                // Use backgroundTintList instead of setBackground for MaterialButton
                backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF6C63FF.toInt())
                cornerRadius = (12 * density).toInt()
                insetTop = 0
                insetBottom = 0
                layoutParams = LinearLayout.LayoutParams(
                    (280 * density).toInt(),
                    (52 * density).toInt()
                )
                setOnClickListener { handleAction() }
            }
            addView(actionButton)

            // Skip button
            addView(TextView(context).apply {
                text = "Skip for now"
                textSize = 14f
                setTextColor(0xFF60656D.toInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (16 * density).toInt()
                }
                setPadding(
                    (16 * density).toInt(),
                    (8 * density).toInt(),
                    (16 * density).toInt(),
                    (8 * density).toInt()
                )
                isClickable = true
                isFocusable = true
                setOnClickListener { launchMainApp() }
            })

            // Spacer bottom
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0, 1f
                )
            })
        }
    }

    private fun updateStep() {
        val enabled = isKeyboardEnabled()
        val selected = isKeyboardSelected()

        when {
            !enabled -> {
                currentStep = 0
                allDone = false
                stepIndicator.text = "Step 1 of 2"
                statusText.text = "Enable AkashBoard"
                actionButton.text = "Enable Keyboard"
                instructionsText.text =
                    "Tap the button below to open keyboard settings,\nthen toggle AkashBoard on."
            }
            !selected -> {
                currentStep = 1
                allDone = false
                stepIndicator.text = "Step 2 of 2"
                statusText.text = "Switch to AkashBoard"
                actionButton.text = "Switch Keyboard"
                instructionsText.text =
                    "Tap the button below, then pick AkashBoard\nfrom the input method picker."
            }
            else -> {
                allDone = true
                stepIndicator.text = "✓ Complete"
                statusText.text = "You're all set!"
                actionButton.text = "Start Typing →"
                instructionsText.text =
                    "AkashBoard is your active keyboard.\nOpen any text field and start typing."
            }
        }
    }

    private fun handleAction() {
        if (allDone) {
            launchMainApp()
            return
        }

        when (currentStep) {
            0 -> {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                startActivity(intent)
            }
            1 -> {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.any { it.packageName == packageName }
    }

    private fun isKeyboardSelected(): Boolean {
        val currentIME = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return currentIME?.contains(packageName) == true
    }

    private fun launchMainApp() {
        getSharedPreferences("akashboard_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_complete", true)
            .apply()

        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }
}
