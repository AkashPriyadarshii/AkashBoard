/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AboutFragment.kt — About screen.
 *
 * Shows version, licenses, and GitHub link.
 */

package com.akashboard.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.view.View.TEXT_ALIGNMENT_CENTER

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val density = resources.displayMetrics.density
        val context = requireContext()

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(
                (20 * density).toInt(),
                (16 * density).toInt(),
                (20 * density).toInt(),
                (16 * density).toInt()
            )

            // App icon / name
            addView(TextView(context).apply {
                text = "⌨️"
                textSize = 48f
                textAlignment = TEXT_ALIGNMENT_CENTER
            })

            addView(TextView(context).apply {
                text = "AkashBoard"
                textSize = 24f
                setTextColor(0xFFF2F3F5.toInt())
                textAlignment = TEXT_ALIGNMENT_CENTER
                setPadding(0, (8 * density).toInt(), 0, 0)
            })

            addView(TextView(context).apply {
                text = "The keyboard that becomes YOU."
                textSize = 14f
                setTextColor(0xFFA6ABB4.toInt())
                textAlignment = TEXT_ALIGNMENT_CENTER
                setPadding(0, (4 * density).toInt(), 0, (24 * density).toInt())
            })

            // Version
            addView(createInfoRow(density, "Version", "1.0.0"))

            // License
            addView(createInfoRow(density, "License", "GNU General Public License v3.0"))

            // Source code
            addView(createClickableRow(density, "Source Code", "GitHub") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AkashPriyadarshii/AkashBoard"))
                startActivity(intent)
            })

            // Report issue
            addView(createClickableRow(density, "Report Issue", "GitHub Issues") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AkashPriyadarshii/AkashBoard/issues"))
                startActivity(intent)
            })

            // Credits
            addView(createInfoRow(density, "Created by", "Akash Priyadarshi"))

            // Spacer
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0, 1f
                )
            })

            // Footer
            addView(TextView(context).apply {
                text = "Built with Kotlin + Rust • No tracking • No ads • FOSS"
                textSize = 12f
                setTextColor(0xFF60656D.toInt())
                textAlignment = TEXT_ALIGNMENT_CENTER
            })
        }
    }

    private fun createInfoRow(density: Float, label: String, value: String): LinearLayout {
        val context = requireContext()

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (48 * density).toInt()
            )
            gravity = android.view.Gravity.CENTER_VERTICAL

            addView(TextView(context).apply {
                text = label
                textSize = 14f
                setTextColor(0xFFA6ABB4.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })

            addView(TextView(context).apply {
                text = value
                textSize = 14f
                setTextColor(0xFFF2F3F5.toInt())
            })
        }
    }

    private fun createClickableRow(density: Float, label: String, linkText: String, onClick: () -> Unit): LinearLayout {
        val context = requireContext()

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (48 * density).toInt()
            )
            gravity = android.view.Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }

            addView(TextView(context).apply {
                text = label
                textSize = 14f
                setTextColor(0xFFA6ABB4.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })

            addView(TextView(context).apply {
                text = "$linkText →"
                textSize = 14f
                setTextColor(0xFF6C63FF.toInt())
            })
        }
    }
}
