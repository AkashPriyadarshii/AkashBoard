/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * TypingDNA.kt — Typing pattern fingerprint.
 *
 * Generates a unique typing signature based on timing patterns,
 * key preferences, and error tendencies. Used for device verification
 * and personalized predictions.
 */

package com.akashboard.analytics

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.security.MessageDigest
import kotlin.math.sqrt

/**
 * TypingDNA — generates a unique typing fingerprint.
 *
 * The fingerprint is computed locally and never leaves the device.
 * It can be used for:
 * - Device verification (biometric-like)
 * - Personalized prediction tuning
 * - User identification (optional)
 */
class TypingDNA(context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // ── Timing Data ───────────────────────────────────────────────────────

    // Inter-key timing for common digraphs (e.g., "th", "he", "in")
    private val digraphTimings = mutableMapOf<String, MutableList<Long>>()

    // Key hold durations
    private val keyHoldTimes = mutableMapOf<Char, MutableList<Long>>()

    // Flight times (time between releasing one key and pressing the next)
    private val flightTimes = mutableListOf<Long>()

    // Recent timing buffer (last N events for real-time fingerprinting)
    private val recentTimings = mutableListOf<TimingEvent>()

    private var lastKeyReleaseTime = 0L
    private var lastKeyPressedTime = 0L
    private var lastKeyChar = '\u0000'

    // ── Public API ────────────────────────────────────────────────────────

    fun onKeyPressed(char: Char, timestamp: Long) {
        lastKeyPressedTime = timestamp
    }

    fun onKeyReleased(char: Char, timestamp: Long) {
        // Hold time = release - press
        val holdTime = timestamp - lastKeyPressedTime
        keyHoldTimes.getOrPut(char) { mutableListOf() }.add(holdTime)

        // Flight time from previous key
        if (lastKeyReleaseTime > 0) {
            val flightTime = lastKeyPressedTime - lastKeyReleaseTime
            flightTimes.add(flightTime)

            // Digraph timing
            if (lastKeyChar != '\u0000') {
                val digraph = "$lastKeyChar$char"
                digraphTimings.getOrPut(digraph) { mutableListOf() }.add(flightTime)
            }
        }

        lastKeyReleaseTime = timestamp
        lastKeyChar = char

        // Buffer for real-time fingerprinting
        recentTimings.add(TimingEvent(char, holdTime, timestamp))
        if (recentTimings.size > MAX_TIMING_BUFFER) {
            recentTimings.removeAt(0)
        }
    }

    // ── Fingerprint Generation ────────────────────────────────────────────

    /**
     * Generate a typing fingerprint hash.
     *
     * The fingerprint is a SHA-256 hash of timing statistics.
     * It's deterministic for the same typing patterns but unique
     * to each user's typing style.
     */
    fun generateFingerprint(): String {
        val stats = computeStats()
        val raw = buildString {
            append("hold_mean=${stats.meanHoldTime}")
            append(":hold_std=${stats.stdHoldTime}")
            append(":flight_mean=${stats.meanFlightTime}")
            append(":flight_std=${stats.stdFlightTime}")
            append(":error_rate=${stats.errorRate}")
            append(":digraph_var=${stats.digraphVariance}")
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(raw.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Compute typing statistics from collected data.
     */
    fun computeStats(): TypingStats {
        val holdTimes = keyHoldTimes.values.flatten()
        val meanHold = if (holdTimes.isNotEmpty()) holdTimes.average() else 0.0
        val stdHold = if (holdTimes.size > 1) {
            sqrt(holdTimes.map { (it - meanHold) * (it - meanHold) }.average())
        } else 0.0

        val meanFlight = if (flightTimes.isNotEmpty()) flightTimes.average() else 0.0
        val stdFlight = if (flightTimes.size > 1) {
            sqrt(flightTimes.map { (it - meanFlight) * (it - meanFlight) }.average())
        } else 0.0

        // Digraph variance — how consistent are common letter pairs
        val digraphVars = digraphTimings.values.map { timings ->
            if (timings.size > 1) {
                val mean = timings.average()
                sqrt(timings.map { (it - mean) * (it - mean) }.average())
            } else 0.0
        }
        val digraphVariance = if (digraphVars.isNotEmpty()) digraphVars.average() else 0.0

        return TypingStats(
            meanHoldTime = meanHold,
            stdHoldTime = stdHold,
            meanFlightTime = meanFlight,
            stdFlightTime = stdFlight,
            errorRate = 0f, // Will be updated by InputHandler
            digraphVariance = digraphVariance
        )
    }

    /**
     * Check if current typing pattern matches the stored fingerprint.
     *
     * @param threshold Similarity threshold (0.0 - 1.0)
     * @return true if current typing matches the stored fingerprint
     */
    fun verifyIdentity(threshold: Float = 0.8f): Boolean {
        val storedFingerprint = prefs.getString(KEY_FINGERPRINT, null) ?: return false
        val currentFingerprint = generateFingerprint()
        return storedFingerprint == currentFingerprint // Exact match for now
    }

    /**
     * Save the current fingerprint as the reference.
     */
    fun saveFingerprint() {
        val fingerprint = generateFingerprint()
        prefs.edit().putString(KEY_FINGERPRINT, fingerprint).apply()
    }

    /**
     * Get the dominant hand timing pattern.
     *
     * Useful for one-handed mode optimization.
     * Returns true if left-side keys are faster (right-handed typist).
     */
    fun getDominantHand(): DominantHand {
        val leftChars = "qwertasdfgzxcvb"
        val rightChars = "yuiophjklnm"

        val leftTimings = keyHoldTimes.filter { it.key in leftChars }.values.flatten()
        val rightTimings = keyHoldTimes.filter { it.key in rightChars }.values.flatten()

        if (leftTimings.isEmpty() || rightTimings.isEmpty()) return DominantHand.UNKNOWN

        val leftAvg = leftTimings.average()
        val rightAvg = rightTimings.average()

        // Faster response = dominant hand
        return if (leftAvg < rightAvg) DominantHand.RIGHT else DominantHand.LEFT
    }

    /**
     * Get recommended keyboard position based on typing patterns.
     */
    fun getRecommendedPosition(): KeyboardPosition {
        val hand = getDominantHand()
        return when (hand) {
            DominantHand.LEFT -> KeyboardPosition.RIGHT // Right hand is dominant, shift left for reach
            DominantHand.RIGHT -> KeyboardPosition.LEFT // Left hand is dominant, shift right for reach
            DominantHand.UNKNOWN -> KeyboardPosition.CENTER
        }
    }

    // ── Reset ─────────────────────────────────────────────────────────────

    fun reset() {
        digraphTimings.clear()
        keyHoldTimes.clear()
        flightTimes.clear()
        recentTimings.clear()
        lastKeyReleaseTime = 0L
        lastKeyPressedTime = 0L
        lastKeyChar = '\u0000'
        prefs.edit().remove(KEY_FINGERPRINT).apply()
    }

    // ── Data Models ───────────────────────────────────────────────────────

    data class TypingStats(
        val meanHoldTime: Double,
        val stdHoldTime: Double,
        val meanFlightTime: Double,
        val stdFlightTime: Double,
        val errorRate: Float,
        val digraphVariance: Double
    )

    data class TimingEvent(
        val char: Char,
        val holdTime: Long,
        val timestamp: Long
    )

    enum class DominantHand { LEFT, RIGHT, UNKNOWN }
    enum class KeyboardPosition { LEFT, CENTER, RIGHT }

    companion object {
        private const val KEY_FINGERPRINT = "typingdna_fingerprint"
        private const val MAX_TIMING_BUFFER = 100
    }
}
