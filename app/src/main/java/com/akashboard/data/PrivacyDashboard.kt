/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * PrivacyDashboard.kt — Privacy status viewer.
 *
 * Shows exactly what data is collected, stored, and exported.
 * Designed for transparency — the user should always know
 * what the keyboard knows about them.
 */

package com.akashboard.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.akashboard.analytics.TypingStats
import com.akashboard.settings.KeyboardSettingsProvider

/**
 * Privacy dashboard — transparent view of all collected data.
 *
 * Shows:
 * - What data is collected
 * - Where it's stored
 * - How much is stored
 * - What's NOT collected
 * - Privacy controls
 */
class PrivacyDashboard(
    private val context: Context,
    private val settingsProvider: KeyboardSettingsProvider,
    private val typingStats: TypingStats
) {

    // ── Data Inventory ────────────────────────────────────────────────────

    /**
     * Get all data categories and their status.
     */
    fun getDataCategories(): List<DataCategory> {
        return listOf(
            DataCategory(
                name = "Typing Patterns",
                description = "Words you type, timing patterns, error corrections",
                isCollected = settingsProvider.learningEnabled,
                storage = "On-device (SharedPreferences + Room)",
                sizeKB = estimateTypingDataSize(),
                canDelete = true,
                dataType = DataType.LEARNING
            ),
            DataCategory(
                name = "Personal Dictionary",
                description = "Words you add manually for correction",
                isCollected = true,
                storage = "On-device (SharedPreferences)",
                sizeKB = estimateDictionarySize(),
                canDelete = true,
                dataType = DataType.DICTIONARY
            ),
            DataCategory(
                name = "Clipboard History",
                description = "Recently copied text items",
                isCollected = settingsProvider.clipboardHistoryEnabled,
                storage = "On-device (Room database)",
                sizeKB = estimateClipboardSize(),
                canDelete = true,
                dataType = DataType.CLIPBOARD
            ),
            DataCategory(
                name = "Theme Preferences",
                description = "Your keyboard appearance settings",
                isCollected = true,
                storage = "On-device (SharedPreferences)",
                sizeKB = 1,
                canDelete = true,
                dataType = DataType.SETTINGS
            ),
            DataCategory(
                name = "Typing Statistics",
                description = "WPM, accuracy, session count",
                isCollected = settingsProvider.learningEnabled,
                storage = "On-device (SharedPreferences)",
                sizeKB = 1,
                canDelete = true,
                dataType = DataType.STATS
            ),
            DataCategory(
                name = "Typing DNA",
                description = "Fingerprint of your typing rhythm (optional)",
                isCollected = false, // Not yet enabled
                storage = "On-device (SharedPreferences)",
                sizeKB = 1,
                canDelete = true,
                dataType = DataType.FINGERPRINT
            )
        )
    }

    /**
     * Get data that is explicitly NOT collected.
     */
    fun getNotCollected(): List<String> {
        return listOf(
            "No keystroke content sent to any server",
            "No browsing history",
            "No contacts or call logs",
            "No location data",
            "No app usage statistics",
            "No advertising identifiers",
            "No crash reports (unless you enable them)",
            "No network requests for core functionality",
            "No third-party analytics",
            "No data sold to third parties"
        )
    }

    /**
     * Get privacy controls.
     */
    fun getPrivacyControls(): List<PrivacyControl> {
        return listOf(
            PrivacyControl(
                name = "Incognito Mode",
                description = "Stop learning from what you type",
                isEnabled = settingsProvider.incognitoMode,
                action = PrivacyAction.INCOGNITO
            ),
            PrivacyControl(
                name = "Learning",
                description = "Allow keyboard to learn your typing patterns",
                isEnabled = settingsProvider.learningEnabled,
                action = PrivacyAction.LEARNING
            ),
            PrivacyControl(
                name = "Clipboard History",
                description = "Save copied text for quick access",
                isEnabled = settingsProvider.clipboardHistoryEnabled,
                action = PrivacyAction.CLIPBOARD
            ),
            PrivacyControl(
                name = "Network Access",
                description = "Allow network for voice input (optional)",
                isEnabled = settingsProvider.networkAccess,
                action = PrivacyAction.NETWORK
            )
        )
    }

    /**
     * Get total data size.
     */
    fun getTotalDataSizeKB(): Long {
        return getDataCategories().sumOf { it.sizeKB }
    }

    /**
     * Check if keyboard has network permission.
     */
    fun hasNetworkPermission(): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val permissions = packageInfo.requestedPermissions ?: emptyArray()
            permissions.any { it == "android.permission.INTERNET" }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate privacy report.
     */
    fun generateReport(): PrivacyReport {
        return PrivacyReport(
            categories = getDataCategories(),
            notCollected = getNotCollected(),
            controls = getPrivacyControls(),
            totalSizeKB = getTotalDataSizeKB(),
            hasNetworkPermission = hasNetworkPermission(),
            isIncognito = settingsProvider.incognitoMode
        )
    }

    // ── Size Estimation ───────────────────────────────────────────────────

    private fun estimateTypingDataSize(): Long {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        var size = 0L
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("time_") || key.startsWith("stats_")) {
                size += (key.length + (value?.toString()?.length ?: 0))
            }
        }
        return (size / 1024).coerceAtLeast(1)
    }

    private fun estimateDictionarySize(): Long {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        var size = 0L
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("dict_")) {
                size += (key.length + (value?.toString()?.length ?: 0))
            }
        }
        return (size / 1024).coerceAtLeast(1)
    }

    private fun estimateClipboardSize(): Long {
        // Return estimated size based on DB file existence
        return try {
            val dbFile = context.getDatabasePath("akashboard_clipboard")
            if (dbFile.exists()) {
                dbFile.length() / 1024
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    // ── Data Models ───────────────────────────────────────────────────────

    data class DataCategory(
        val name: String,
        val description: String,
        val isCollected: Boolean,
        val storage: String,
        val sizeKB: Long,
        val canDelete: Boolean,
        val dataType: DataType
    )

    enum class DataType {
        LEARNING,
        DICTIONARY,
        CLIPBOARD,
        SETTINGS,
        STATS,
        FINGERPRINT
    }

    data class PrivacyControl(
        val name: String,
        val description: String,
        val isEnabled: Boolean,
        val action: PrivacyAction
    )

    enum class PrivacyAction {
        INCOGNITO,
        LEARNING,
        CLIPBOARD,
        NETWORK
    }

    data class PrivacyReport(
        val categories: List<DataCategory>,
        val notCollected: List<String>,
        val controls: List<PrivacyControl>,
        val totalSizeKB: Long,
        val hasNetworkPermission: Boolean,
        val isIncognito: Boolean
    )
}
