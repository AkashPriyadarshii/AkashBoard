/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * TimeAwarePredictor.kt — Time-based prediction patterns.
 *
 * Learns what the user types at different times of day,
 * on different days, and in different contexts.
 * Improves predictions after 7+ days of use.
 */

package com.akashboard.analytics

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.Calendar

/**
 * Time-aware prediction engine.
 *
 * Tracks word frequency by:
 * - Hour of day (0-23)
 * - Day of week (0-6)
 * - App context (which app the user is typing in)
 *
 * Predictions improve over time as more patterns are learned.
 */
class TimeAwarePredictor(context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // ── Pattern Storage ───────────────────────────────────────────────────

    // Hour → word → frequency
    private val hourlyPatterns = mutableMapOf<Int, MutableMap<String, Int>>()

    // Day → word → frequency
    private val dailyPatterns = mutableMapOf<Int, MutableMap<String, Int>>()

    // App package → word → frequency
    private val appPatterns = mutableMapOf<String, MutableMap<String, Int>>()

    // Session count (for "after 7 days" checks)
    private var patternDaysActive = 0

    // ── Learning ──────────────────────────────────────────────────────────

    /**
     * Learn a word was used at a specific time.
     */
    fun learnWord(word: String, appPackage: String? = null) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val day = calendar.get(Calendar.DAY_OF_WEEK)

        // Hourly pattern
        hourlyPatterns.getOrPut(hour) { mutableMapOf() }
            .merge(word, 1, Int::plus)

        // Daily pattern
        dailyPatterns.getOrPut(day) { mutableMapOf() }
            .merge(word, 1, Int::plus)

        // App pattern
        if (appPackage != null) {
            appPatterns.getOrPut(appPackage) { mutableMapOf() }
                .merge(word, 1, Int::plus)
        }

        // Persist periodically
        patternDaysActive++
        if (patternDaysActive % 10 == 0) {
            persistPatterns()
        }
    }

    /**
     * Get time-aware predictions.
     *
     * @param partial Partial word being typed
     * @param appPackage Current app package
     * @param maxResults Maximum results to return
     * @return Ranked list of predictions
     */
    fun predict(
        partial: String,
        appPackage: String? = null,
        maxResults: Int = 3
    ): List<ScoredWord> {
        if (partial.isBlank()) return emptyList()

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val day = calendar.get(Calendar.DAY_OF_WEEK)

        val candidates = mutableMapOf<String, Float>()

        // Get hourly matches
        val hourlyWords = hourlyPatterns[hour] ?: emptyMap()
        for ((word, count) in hourlyWords) {
            if (word.startsWith(partial, ignoreCase = true)) {
                candidates[word] = (candidates[word] ?: 0f) + count * HOURLY_WEIGHT
            }
        }

        // Get daily matches
        val dailyWords = dailyPatterns[day] ?: emptyMap()
        for ((word, count) in dailyWords) {
            if (word.startsWith(partial, ignoreCase = true)) {
                candidates[word] = (candidates[word] ?: 0f) + count * DAILY_WEIGHT
            }
        }

        // Get app-specific matches
        if (appPackage != null) {
            val appWords = appPatterns[appPackage] ?: emptyMap()
            for ((word, count) in appWords) {
                if (word.startsWith(partial, ignoreCase = true)) {
                    candidates[word] = (candidates[word] ?: 0f) + count * APP_WEIGHT
                }
            }
        }

        // Sort by score and return top results
        return candidates.entries
            .sortedByDescending { it.value }
            .take(maxResults)
            .map { ScoredWord(word = it.key, score = it.value) }
    }

    /**
     * Get the most common words for the current time period.
     */
    fun getTopWordsForCurrentTime(limit: Int = 10): List<ScoredWord> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val hourlyWords = hourlyPatterns[hour] ?: emptyMap()

        return hourlyWords.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { ScoredWord(word = it.key, score = it.value.toFloat()) }
    }

    /**
     * Check if time-aware patterns are available.
     * Returns true after 7+ days of use.
     */
    fun hasEnoughData(): Boolean = patternDaysActive >= 7

    /**
     * Get pattern maturity level.
     */
    fun getMaturityLevel(): MaturityLevel {
        return when {
            patternDaysActive >= 30 -> MaturityLevel.EXPERT
            patternDaysActive >= 14 -> MaturityLevel.GOOD
            patternDaysActive >= 7 -> MaturityLevel.BASIC
            patternDaysActive >= 1 -> MaturityLevel.LEARNING
            else -> MaturityLevel.EMPTY
        }
    }

    // ── Persistence ───────────────────────────────────────────────────────

    private fun persistPatterns() {
        // Simple JSON-like persistence using SharedPreferences
        val editor = prefs.edit()

        // Save hourly patterns
        for ((hour, words) in hourlyPatterns) {
            val wordMap = words.entries.joinToString(",") { "${it.key}:${it.value}" }
            editor.putString("time_hourly_$hour", wordMap)
        }

        // Save daily patterns
        for ((day, words) in dailyPatterns) {
            val wordMap = words.entries.joinToString(",") { "${it.key}:${it.value}" }
            editor.putString("time_daily_$day", wordMap)
        }

        // Save app patterns
        for ((pkg, words) in appPatterns) {
            val wordMap = words.entries.joinToString(",") { "${it.key}:${it.value}" }
            editor.putString("time_app_$pkg", wordMap)
        }

        editor.putInt("time_days_active", patternDaysActive)
        editor.apply()
    }

    fun loadPatterns() {
        patternDaysActive = prefs.getInt("time_days_active", 0)

        // Load hourly patterns
        for (hour in 0..23) {
            val data = prefs.getString("time_hourly_$hour", null) ?: continue
            val words = parseWordMap(data)
            if (words.isNotEmpty()) hourlyPatterns[hour] = words
        }

        // Load daily patterns
        for (day in 1..7) {
            val data = prefs.getString("time_daily_$day", null) ?: continue
            val words = parseWordMap(data)
            if (words.isNotEmpty()) dailyPatterns[day] = words
        }
    }

    private fun parseWordMap(data: String): MutableMap<String, Int> {
        val map = mutableMapOf<String, Int>()
        if (data.isBlank()) return map
        data.split(",").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                map[parts[0]] = parts[1].toIntOrNull() ?: 0
            }
        }
        return map
    }

    // ── Reset ─────────────────────────────────────────────────────────────

    fun reset() {
        hourlyPatterns.clear()
        dailyPatterns.clear()
        appPatterns.clear()
        patternDaysActive = 0
        prefs.edit()
            .clear()
            .apply()
    }

    // ── Data Models ───────────────────────────────────────────────────────

    data class ScoredWord(
        val word: String,
        val score: Float
    )

    enum class MaturityLevel {
        EMPTY,      // No data yet
        LEARNING,   // 1-6 days
        BASIC,      // 7-13 days
        GOOD,       // 14-29 days
        EXPERT      // 30+ days
    }

    companion object {
        // Scoring weights
        private const val HOURLY_WEIGHT = 3.0f  // Most specific
        private const val DAILY_WEIGHT = 1.0f   // General pattern
        private const val APP_WEIGHT = 2.0f      // Context-specific
    }
}
