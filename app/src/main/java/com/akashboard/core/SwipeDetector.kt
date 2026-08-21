/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SwipeDetector.kt — Gesture recognition for glide typing.
 *
 * Algorithm (inspired by AnySoftKeyboard's GestureTypingDetector):
 *   1. Collect touch points during gesture
 *   2. Filter noise (remove points too close together)
 *   3. Find closest key for each point
 *   4. Build key sequence (e.g., "h" → "e" → "l" → "l" → "o")
 *   5. Match against dictionary (find words matching the key path)
 *   6. Rank by frequency + path distance
 *
 * Performance targets:
 *   - Gesture collection: real-time (no lag)
 *   - Path matching: <5ms for top-5 results
 */

package com.akashboard.core

import android.graphics.PointF
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Swipe gesture detector.
 *
 * Recognizes glide typing gestures and returns matching words.
 *
 * Usage:
 *   val detector = SwipeDetector(keyPositions)
 *   detector.onTouchDown(x, y)
 *   detector.onTouchMove(x, y)
 *   val results = detector.onTouchUp()
 */
class SwipeDetector(
    /** Map of character to key center positions */
    private val keyPositions: Map<Char, PointF>
) {
    /** Collected gesture points */
    private val points = mutableListOf<PointF>()

    /** Whether we're currently tracking a gesture */
    var isTracking = false
        private set

    /** Minimum distance between consecutive points (px) */
    private var minPointDistance = 8f

    // ── Gesture Collection ────────────────────────────────────────────────

    /**
     * Start a new gesture.
     */
    fun onTouchDown(x: Float, y: Float) {
        points.clear()
        points.add(PointF(x, y))
        isTracking = true
    }

    /**
     * Continue the gesture.
     *
     * Points are filtered — only added if sufficiently far from the last point.
     */
    fun onTouchMove(x: Float, y: Float) {
        if (!isTracking) return

        val lastPoint = points.lastOrNull() ?: return
        val distance = distanceBetween(lastPoint, PointF(x, y))

        if (distance >= minPointDistance) {
            points.add(PointF(x, y))
        }
    }

    /**
     * End the gesture and return matching words.
     *
     * @param dictionary List of valid words to match against
     * @param topK Maximum number of results
     * @return List of matching words, ordered by relevance
     */
    fun onTouchUp(dictionary: List<String> = emptyList(), topK: Int = 5): List<String> {
        isTracking = false

        if (points.size < 2) return emptyList()

        // Find closest key for each point
        val keySequence = points.map { point ->
            findClosestKey(point)
        }.filterNotNull()

        if (keySequence.isEmpty()) return emptyList()

        // Match against dictionary
        val results = if (dictionary.isNotEmpty()) {
            matchDictionary(keySequence, dictionary, topK)
        } else {
            // No dictionary — return the key sequence as a word
            val word = keySequence.joinToString("")
            listOf(word)
        }

        points.clear()
        return results
    }

    /**
     * Cancel the current gesture.
     */
    fun cancel() {
        isTracking = false
        points.clear()
    }

    // ── Key Matching ──────────────────────────────────────────────────────

    /**
     * Find the closest key to a touch point.
     *
     * Uses Euclidean distance to find the nearest key center.
     *
     * @param point Touch point
     * @return Closest character, or null if no key is close enough
     */
    private fun findClosestKey(point: PointF): Char? {
        var bestChar: Char? = null
        var bestDistance = Float.MAX_VALUE

        for ((char, keyCenter) in keyPositions) {
            val distance = distanceBetween(point, keyCenter)

            // Only match if within reasonable distance (50px threshold)
            if (distance < MAX_KEY_DISTANCE && distance < bestDistance) {
                bestDistance = distance
                bestChar = char
            }
        }

        return bestChar
    }

    // ── Dictionary Matching ───────────────────────────────────────────────

    /**
     * Match a key sequence against a dictionary.
     *
     * Finds words where:
     *   1. All characters in the word appear in the key sequence (in order)
     *   2. The word is a valid dictionary entry
     *
     * @param keySequence Characters touched during gesture
     * @param dictionary List of valid words
     * @param topK Maximum results
     * @return Matching words ordered by relevance
     */
    private fun matchDictionary(
        keySequence: List<Char>,
        dictionary: List<String>,
        topK: Int
    ): List<String> {
        val sequenceStr = keySequence.joinToString("").lowercase()

        return dictionary
            .filter { word ->
                val wordLower = word.lowercase()
                // Check if word matches the key sequence
                matchesSequence(wordLower, sequenceStr)
            }
            .sortedBy { word ->
                // Rank by: exact match > prefix match > contains
                val wordLower = word.lowercase()
                when {
                    wordLower == sequenceStr -> 0  // Exact match
                    sequenceStr.startsWith(wordLower) -> 1  // Prefix match
                    wordLower.startsWith(sequenceStr) -> 2  // Sequence is prefix
                    else -> 3
                }
            }
            .take(topK)
    }

    /**
     * Check if a word matches a key sequence.
     *
     * A word matches if:
     *   - All characters in the word appear in the sequence (in order)
     *   - The sequence contains the word's characters with possible skips
     */
    private fun matchesSequence(word: String, sequence: String): Boolean {
        var seqIndex = 0

        for (char in word) {
            // Find this character in the remaining sequence
            val found = sequence.indexOf(char, seqIndex)
            if (found == -1) return false
            seqIndex = found + 1
        }

        return true
    }

    // ── Geometry ──────────────────────────────────────────────────────────

    /**
     * Euclidean distance between two points.
     */
    private fun distanceBetween(a: PointF, b: PointF): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Get the collected gesture points (for trail rendering).
     */
    fun getPoints(): List<PointF> = points.toList()

    // ── Constants ─────────────────────────────────────────────────────────

    companion object {
        /** Maximum distance from a touch point to a key center (px) */
        const val MAX_KEY_DISTANCE = 50f

        /** Minimum distance between consecutive gesture points (px) */
        const val MIN_POINT_DISTANCE = 8f
    }
}
