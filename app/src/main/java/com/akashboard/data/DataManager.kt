/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * DataManager.kt — Data export/import orchestrator.
 *
 * Handles exporting settings, dictionary, and learning data to JSON,
 * and importing them back. All operations are synchronous for simplicity.
 */

package com.akashboard.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceManager
import com.akashboard.analytics.TimeAwarePredictor
import com.akashboard.analytics.TypingStats
import com.akashboard.engine.PredictorBridge
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Manages data export and import for AkashBoard.
 *
 * Export produces a single JSON file containing:
 * - All keyboard settings
 * - Personal dictionary
 * - Learned words
 * - Active theme
 * - Custom themes
 * - Optional stats
 */
class DataManager(
    private val context: Context,
    private val clipboardDB: ClipboardDB,
    private val timeAwarePredictor: TimeAwarePredictor,
    private val typingStats: TypingStats
) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ── Export ────────────────────────────────────────────────────────────

    /**
     * Export all keyboard data to JSON.
     *
     * @param includeStats Whether to include typing statistics
     * @return The export data object
     */
    fun exportData(includeStats: Boolean = false): AkashBoardExport {
        // Export settings
        val settings = mutableMapOf<String, ExportValue>()
        prefs.all.forEach { (key, value) ->
            when (value) {
                is Boolean -> settings[key] = ExportValue.BoolVal(value)
                is Int -> settings[key] = ExportValue.IntVal(value)
                is Float -> settings[key] = ExportValue.FloatVal(value)
                is String -> settings[key] = ExportValue.StringVal(value)
            }
        }

        // Export active theme
        val activeTheme = prefs.getString("theme_id", null)

        // Export stats if requested
        val stats = if (includeStats) {
            ExportStats(
                totalSessions = typingStats.getTotalSessions(),
                totalCharacters = typingStats.getTotalCharacters(),
                totalWords = typingStats.getTotalWords(),
                bestWPM = typingStats.getBestWPM(),
                averageWPM = typingStats.getAverageWPM(),
                overallAccuracy = typingStats.getOverallAccuracy()
            )
        } else null

        return AkashBoardExport(
            settings = settings,
            activeTheme = activeTheme,
            stats = stats
        )
    }

    /**
     * Export to JSON string.
     */
    fun exportToJson(includeStats: Boolean = false): String {
        return json.encodeToString(AkashBoardExport.serializer(), exportData(includeStats))
    }

    /**
     * Export to file.
     *
     * @param file Target file
     * @param includeStats Whether to include typing statistics
     * @return true if successful
     */
    fun exportToFile(file: File, includeStats: Boolean = false): Boolean {
        return try {
            val jsonString = exportToJson(includeStats)
            file.writeText(jsonString)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get default export file location.
     */
    fun getExportFile(): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        return File(dir, "akashboard_export_${System.currentTimeMillis()}.json")
    }

    // ── Import ────────────────────────────────────────────────────────────

    /**
     * Import from JSON string.
     *
     * @param jsonString The JSON to import
     * @return Import result
     */
    fun importFromJson(jsonString: String): ImportResult {
        return try {
            val export = json.decodeFromString(AkashBoardExport.serializer(), jsonString)

            // Validate
            when (val validation = ExportValidator.validate(export)) {
                is ExportValidator.ValidationResult.Valid -> {
                    importData(export)
                    ImportResult.Success
                }
                is ExportValidator.ValidationResult.Invalid -> {
                    ImportResult.ValidationError(validation.errors)
                }
            }
        } catch (e: Exception) {
            ImportResult.ParseError(e.message ?: "Unknown error")
        }
    }

    /**
     * Import from file.
     */
    fun importFromFile(file: File): ImportResult {
        return try {
            val jsonString = file.readText()
            importFromJson(jsonString)
        } catch (e: Exception) {
            ImportResult.FileError(e.message ?: "Failed to read file")
        }
    }

    /**
     * Apply imported data.
     */
    private fun importData(export: AkashBoardExport) {
        val editor = prefs.edit()

        // Import settings
        export.settings.forEach { (key, value) ->
            when (value) {
                is ExportValue.BoolVal -> editor.putBoolean(key, value.value)
                is ExportValue.IntVal -> editor.putInt(key, value.value)
                is ExportValue.FloatVal -> editor.putFloat(key, value.value)
                is ExportValue.StringVal -> editor.putString(key, value.value)
            }
        }

        // Import active theme
        export.activeTheme?.let { editor.putString("theme_id", it) }

        editor.apply()
    }

    // ── Nuclear Delete ────────────────────────────────────────────────────

    /**
     * Delete ALL keyboard data.
     *
     * This is irreversible. Shows confirmation dialog before calling.
     */
    fun nuclearDelete(): Boolean {
        return try {
            // Clear all preferences
            prefs.edit().clear().apply()

            // Clear clipboard database
            // Note: Room doesn't support clear() directly, so we delete all rows
            android.database.sqlite.SQLiteDatabase.openDatabase(
                context.getDatabasePath("akashboard_clipboard").absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
            ).apply {
                execSQL("DELETE FROM clipboard_history")
                close()
            }

            // Clear analytics
            timeAwarePredictor.reset()
            typingStats.resetAllStats()

            true
        } catch (e: Exception) {
            false
        }
    }

    // ── Data Viewer ───────────────────────────────────────────────────────

    /**
     * Get a summary of all stored data.
     */
    fun getDataSummary(): DataSummary {
        val settingsCount = prefs.all.size
        val totalChars = typingStats.getTotalCharacters()
        val totalWords = typingStats.getTotalWords()
        val totalSessions = typingStats.getTotalSessions()

        return DataSummary(
            settingsCount = settingsCount,
            totalCharacters = totalChars,
            totalWords = totalWords,
            totalSessions = totalSessions,
            estimatedSizeKB = estimateDataSize()
        )
    }

    private fun estimateDataSize(): Long {
        // Rough estimate of data size in KB
        val prefsSize = prefs.all.toString().length.toLong()
        val dbSize = try {
            context.getDatabasePath("akashboard_clipboard").length()
        } catch (e: Exception) { 0L }
        return (prefsSize + dbSize) / 1024
    }

    // ── Data Models ───────────────────────────────────────────────────────

    sealed class ImportResult {
        object Success : ImportResult()
        data class ValidationError(val errors: List<String>) : ImportResult()
        data class ParseError(val message: String) : ImportResult()
        data class FileError(val message: String) : ImportResult()
    }

    data class DataSummary(
        val settingsCount: Int,
        val totalCharacters: Long,
        val totalWords: Long,
        val totalSessions: Long,
        val estimatedSizeKB: Long
    )
}
