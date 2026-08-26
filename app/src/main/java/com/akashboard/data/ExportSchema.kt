/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * ExportSchema.kt — Data export/import schema.
 *
 * Defines the JSON format for exporting and importing
 * keyboard settings, dictionary, and learning data.
 */

package com.akashboard.data

import kotlinx.serialization.Serializable

/**
 * Export schema version 1.
 *
 * All data is serialized to JSON for portability.
 * The schema is versioned for forward compatibility.
 */
@Serializable
data class AkashBoardExport(
    val version: Int = 1,
    val exportDate: Long = System.currentTimeMillis(),
    val appName: String = "AkashBoard",
    val appVersion: String = "0.1.0",

    // Settings
    val settings: Map<String, ExportValue> = emptyMap(),

    // Personal dictionary
    val dictionary: List<DictionaryEntry> = emptyList(),

    // Learned words (from typing engine)
    val learnedWords: List<LearnedWord> = emptyList(),

    // Theme
    val activeTheme: String? = null,

    // Custom themes
    val customThemes: List<CustomTheme> = emptyList(),

    // Stats (optional)
    val stats: ExportStats? = null
)

@Serializable
sealed class ExportValue {
    @Serializable data class BoolVal(val value: Boolean) : ExportValue()
    @Serializable data class IntVal(val value: Int) : ExportValue()
    @Serializable data class FloatVal(val value: Float) : ExportValue()
    @Serializable data class StringVal(val value: String) : ExportValue()
}

@Serializable
data class DictionaryEntry(
    val word: String,
    val frequency: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class LearnedWord(
    val word: String,
    val context: String,
    val frequency: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class CustomTheme(
    val id: String,
    val name: String,
    val config: Map<String, String> // token → hex color
)

@Serializable
data class ExportStats(
    val totalSessions: Long = 0,
    val totalCharacters: Long = 0,
    val totalWords: Long = 0,
    val bestWPM: Int = 0,
    val averageWPM: Int = 0,
    val overallAccuracy: Float = 100f
)

/**
 * Schema validation.
 */
object ExportValidator {
    private const val MIN_VERSION = 1
    private const val MAX_VERSION = 1

    fun validate(export: AkashBoardExport): ValidationResult {
        val errors = mutableListOf<String>()

        if (export.version < MIN_VERSION || export.version > MAX_VERSION) {
            errors.add("Unsupported version: ${export.version}")
        }

        if (export.appName != "AkashBoard") {
            errors.add("Invalid app name: ${export.appName}")
        }

        // Validate dictionary entries
        export.dictionary.forEach { entry ->
            if (entry.word.isBlank()) {
                errors.add("Empty dictionary entry found")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errors: List<String>) : ValidationResult()
    }
}
