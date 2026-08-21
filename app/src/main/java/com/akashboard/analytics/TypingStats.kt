/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * TypingStats.kt — Typing speed and accuracy tracking.
 *
 * Tracks WPM, accuracy, error patterns, and session history.
 * All data stays on device.
 */

package com.akashboard.analytics

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Tracks typing statistics for the user.
 *
 * All metrics are computed in real-time and persisted to SharedPreferences.
 * No network requests. No data leaves the device.
 */
class TypingStats(context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // ── Session State ─────────────────────────────────────────────────────

    private var sessionStartTime = 0L
    private var sessionCharCount = 0
    private var sessionWordCount = 0
    private var sessionCorrectChars = 0
    private var sessionErrorChars = 0
    private var lastKeyTimestamp = 0L
    private var isSessionActive = false

    // ── Public API ────────────────────────────────────────────────────────

    fun startSession() {
        sessionStartTime = System.currentTimeMillis()
        sessionCharCount = 0
        sessionWordCount = 0
        sessionCorrectChars = 0
        sessionErrorChars = 0
        lastKeyTimestamp = sessionStartTime
        isSessionActive = true
    }

    fun endSession() {
        if (!isSessionActive) return
        isSessionActive = false

        // Persist session data
        val totalTime = System.currentTimeMillis() - sessionStartTime
        val totalSessions = prefs.getLong(KEY_TOTAL_SESSIONS, 0) + 1
        val totalTimeTyping = prefs.getLong(KEY_TOTAL_TIME_MS, 0) + totalTime
        val totalChars = prefs.getLong(KEY_TOTAL_CHARS, 0) + sessionCharCount
        val totalWords = prefs.getLong(KEY_TOTAL_WORDS, 0) + sessionWordCount

        prefs.edit()
            .putLong(KEY_TOTAL_SESSIONS, totalSessions)
            .putLong(KEY_TOTAL_TIME_MS, totalTimeTyping)
            .putLong(KEY_TOTAL_CHARS, totalChars)
            .putLong(KEY_TOTAL_WORDS, totalWords)
            .apply()

        // Update best WPM if this session was better
        val currentWpm = calculateWPM()
        if (currentWpm > getBestWPM()) {
            prefs.edit().putInt(KEY_BEST_WPM, currentWpm).apply()
        }
    }

    fun onKeyReleased(isCorrect: Boolean) {
        if (!isSessionActive) return

        val now = System.currentTimeMillis()
        val timeDelta = now - lastKeyTimestamp
        lastKeyTimestamp = now

        sessionCharCount++
        if (isCorrect) {
            sessionCorrectChars++
        } else {
            sessionErrorChars++
        }

        // Detect word completion (space or punctuation)
        if (timeDelta > 300) {
            // Long pause = likely word boundary
            sessionWordCount++
        }
    }

    fun onWordCompleted(word: String) {
        if (!isSessionActive) return
        sessionWordCount++
    }

    fun onAutoCorrect(original: String, corrected: String) {
        // Track auto-corrections for accuracy
        val totalCorrections = prefs.getInt(KEY_TOTAL_CORRECTIONS, 0) + 1
        prefs.edit().putInt(KEY_TOTAL_CORRECTIONS, totalCorrections).apply()
    }

    // ── Current Session Metrics ───────────────────────────────────────────

    fun calculateWPM(): Int {
        if (!isSessionActive || sessionStartTime == 0L) return 0
        val elapsedMinutes = (System.currentTimeMillis() - sessionStartTime) / 60000.0
        if (elapsedMinutes < 0.05) return 0 // Need at least 3 seconds
        // WPM = (characters / 5) / minutes
        return ((sessionCharCount / 5.0) / elapsedMinutes).toInt().coerceAtLeast(0)
    }

    fun calculateAccuracy(): Float {
        val total = sessionCorrectChars + sessionErrorChars
        if (total == 0) return 100f
        return (sessionCorrectChars.toFloat() / total * 100f).coerceIn(0f, 100f)
    }

    fun getSessionDuration(): Long = if (isSessionActive) {
        System.currentTimeMillis() - sessionStartTime
    } else 0L

    fun getSessionCharCount(): Int = sessionCharCount
    fun getSessionWordCount(): Int = sessionWordCount

    // ── Historical Metrics ────────────────────────────────────────────────

    fun getBestWPM(): Int = prefs.getInt(KEY_BEST_WPM, 0)
    fun getTotalSessions(): Long = prefs.getLong(KEY_TOTAL_SESSIONS, 0)
    fun getTotalTimeTyping(): Long = prefs.getLong(KEY_TOTAL_TIME_MS, 0)
    fun getTotalCharacters(): Long = prefs.getLong(KEY_TOTAL_CHARS, 0)
    fun getTotalWords(): Long = prefs.getLong(KEY_TOTAL_WORDS, 0)
    fun getTotalAutoCorrections(): Int = prefs.getInt(KEY_TOTAL_CORRECTIONS, 0)

    fun getAverageWPM(): Int {
        val totalWords = getTotalWords()
        val totalTimeMinutes = getTotalTimeTyping() / 60000.0
        if (totalTimeMinutes < 1) return 0
        return (totalWords / totalTimeMinutes).toInt()
    }

    fun getOverallAccuracy(): Float {
        val totalChars = getTotalCharacters()
        if (totalChars == 0L) return 100f
        // Estimate: assume 95% base accuracy improved by corrections
        val corrections = getTotalAutoCorrections()
        val estimatedErrors = (totalChars * 0.05 - corrections).coerceAtLeast(0.0)
        val accuracy = ((totalChars.toDouble() - estimatedErrors) / totalChars.toDouble() * 100.0).coerceIn(0.0, 100.0)
        return accuracy.toFloat()
    }

    // ── Typing DNA ────────────────────────────────────────────────────────

    fun getTypingProfile(): TypingProfile {
        return TypingProfile(
            bestWPM = getBestWPM(),
            averageWPM = getAverageWPM(),
            overallAccuracy = getOverallAccuracy(),
            totalSessions = getTotalSessions(),
            totalTimeMs = getTotalTimeTyping(),
            totalChars = getTotalCharacters(),
            autoCorrectionRate = if (getTotalCharacters() > 0) {
                getTotalAutoCorrections().toFloat() / getTotalCharacters() * 1000 // per 1000 chars
            } else 0f
        )
    }

    // ── Reset ─────────────────────────────────────────────────────────────

    fun resetAllStats() {
        prefs.edit()
            .remove(KEY_BEST_WPM)
            .remove(KEY_TOTAL_SESSIONS)
            .remove(KEY_TOTAL_TIME_MS)
            .remove(KEY_TOTAL_CHARS)
            .remove(KEY_TOTAL_WORDS)
            .remove(KEY_TOTAL_CORRECTIONS)
            .apply()
    }

    // ── Data Model ────────────────────────────────────────────────────────

    data class TypingProfile(
        val bestWPM: Int,
        val averageWPM: Int,
        val overallAccuracy: Float,
        val totalSessions: Long,
        val totalTimeMs: Long,
        val totalChars: Long,
        val autoCorrectionRate: Float
    )

    companion object {
        private const val KEY_BEST_WPM = "stats_best_wpm"
        private const val KEY_TOTAL_SESSIONS = "stats_total_sessions"
        private const val KEY_TOTAL_TIME_MS = "stats_total_time_ms"
        private const val KEY_TOTAL_CHARS = "stats_total_chars"
        private const val KEY_TOTAL_WORDS = "stats_total_words"
        private const val KEY_TOTAL_CORRECTIONS = "stats_total_corrections"
    }
}
