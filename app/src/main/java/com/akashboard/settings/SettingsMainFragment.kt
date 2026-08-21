/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SettingsMainFragment.kt — Main settings screen.
 *
 * Shows keyboard status and category navigation.
 */

package com.akashboard.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.akashboard.R
import com.akashboard.SettingsActivity

class SettingsMainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createMainLayout()
    }

    private fun createMainLayout(): LinearLayout {
        val density = resources.displayMetrics.density
        val context = requireContext()

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(0, (16 * density).toInt(), 0, 0)

            // Keyboard status card
            addView(createStatusCard(density))

            // Category items
            addView(createCategoryItem(
                density,
                "⌨️",
                "Typing",
                "Auto-correction, predictions, gestures",
                TypingFragment()
            ))

            addView(createCategoryItem(
                density,
                "🎨",
                "Appearance",
                "Themes, height, one-handed mode",
                AppearanceFragment()
            ))

            addView(createCategoryItem(
                density,
                "🔒",
                "Privacy",
                "Incognito, clipboard, data export",
                PrivacyFragment()
            ))

            addView(createCategoryItem(
                density,
                "ℹ️",
                "About",
                "Version, licenses, GitHub",
                AboutFragment()
            ))
        }
    }

    private fun createStatusCard(density: Float): LinearLayout {
        val context = requireContext()

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(
                (20 * density).toInt(),
                (16 * density).toInt(),
                (20 * density).toInt(),
                (16 * density).toInt()
            )
            setBackgroundColor(0xFF6C63FF.toInt())

            addView(TextView(context).apply {
                text = "AkashBoard"
                textSize = 22f
                setTextColor(0xFFFFFFFF.toInt())
            })

            addView(TextView(context).apply {
                text = "Tap to enable or switch keyboard"
                textSize = 14f
                setTextColor(0xCCFFFFFF.toInt())
                setPadding(0, (4 * density).toInt(), 0, 0)
            })

            addView(TextView(context).apply {
                text = "Version 1.0.0 • GPLv3"
                textSize = 12f
                setTextColor(0x99FFFFFF.toInt())
                setPadding(0, (8 * density).toInt(), 0, 0)
            })
        }
    }

    private fun createCategoryItem(
        density: Float,
        icon: String,
        title: String,
        subtitle: String,
        fragment: Fragment
    ): LinearLayout {
        val context = requireContext()

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (72 * density).toInt()
            )
            setPadding(
                (20 * density).toInt(),
                (12 * density).toInt(),
                (20 * density).toInt(),
                (12 * density).toInt()
            )
            gravity = android.view.Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            foreground = android.util.TypedValue().let {
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, it, true)
                context.getDrawable(it.resourceId)
            }

            setOnClickListener {
                (activity as? SettingsActivity)?.navigateTo(fragment)
            }

            // Icon
            addView(TextView(context).apply {
                text = icon
                textSize = 24f
                layoutParams = LinearLayout.LayoutParams(
                    (48 * density).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            })

            // Text container
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )

                addView(TextView(context).apply {
                    text = title
                    textSize = 16f
                    setTextColor(0xFFF2F3F5.toInt())
                })

                addView(TextView(context).apply {
                    text = subtitle
                    textSize = 13f
                    setTextColor(0xFFA6ABB4.toInt())
                    setPadding(0, (2 * density).toInt(), 0, 0)
                })
            })

            // Arrow
            addView(TextView(context).apply {
                text = "›"
                textSize = 20f
                setTextColor(0xFF60656D.toInt())
            })
        }
    }
}
