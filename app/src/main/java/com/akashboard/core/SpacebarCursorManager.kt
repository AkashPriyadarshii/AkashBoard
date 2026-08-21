/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SpacebarCursorManager.kt — Cursor movement via spacebar swipe.
 *
 * Swiping left/right on the spacebar moves the text cursor.
 * The cursor tracks the finger continuously for precise positioning.
 *
 * Interaction model:
 *   - Touch down on spacebar: start tracking
 *   - Horizontal movement: move cursor proportionally
 *   - Release: commit cursor position
 *
 * The cursor moves at 1:1 ratio with finger movement (pixels to characters).
 * On high-density screens, this feels natural and precise.
 */

package com.akashboard.core

/**
 * Spacebar cursor movement manager.
 *
 * Tracks horizontal swipe on the spacebar to move the text cursor.
 * Provides continuous feedback for precise cursor positioning.
 */
class SpacebarCursorManager {

    /** Whether we're currently tracking a cursor gesture */
    var isTracking = false
        private set

    /** Starting X position of the gesture */
    private var startX = 0f

    /** Accumulated cursor movement (in dp) */
    private var accumulatedMovement = 0f

    /** Callback for cursor movement */
    var onCursorMove: ((deltaChars: Int) -> Unit)? = null

    /** Callback when gesture ends */
    var onGestureEnd: (() -> Unit)? = null

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Start tracking a cursor gesture.
     *
     * @param x Starting X position
     */
    fun startTracking(x: Float) {
        isTracking = true
        startX = x
        accumulatedMovement = 0f
    }

    /**
     * Update cursor position based on finger movement.
     *
     * @param currentX Current X position
     * @param density Device density (dp → px)
     * @return Number of characters to move (positive = right, negative = left)
     */
    fun update(currentX: Float, density: Float): Int {
        if (!isTracking) return 0

        val deltaX = currentX - startX
        accumulatedMovement = deltaX / density  // Convert to dp

        // Calculate character delta (1dp ≈ 1 character at default key width)
        // Adjust sensitivity based on density
        val sensitivity = CURSOR_SENSITIVITY / density
        val charDelta = (accumulatedMovement * sensitivity).toInt()

        if (charDelta != 0) {
            // Adjust start position to prevent accumulation
            startX = currentX - (charDelta / sensitivity * density)
            onCursorMove?.invoke(charDelta)
        }

        return charDelta
    }

    /**
     * End the cursor gesture.
     */
    fun endTracking() {
        if (!isTracking) return

        isTracking = false
        accumulatedMovement = 0f
        onGestureEnd?.invoke()
    }

    /**
     * Cancel the cursor gesture (e.g., if finger moves too far vertically).
     */
    fun cancelTracking() {
        isTracking = false
        accumulatedMovement = 0f
        startX = 0f
    }

    // ── Constants ─────────────────────────────────────────────────────────

    companion object {
        /**
         * Cursor sensitivity.
         *
         * Lower values = more sensitive (less movement = more characters).
         * Higher values = less sensitive (more movement = fewer characters).
         *
         * Default: 1.0 (1dp of movement ≈ 1 character)
         */
        const val CURSOR_SENSITIVITY = 1.0f

        /** Maximum vertical movement before canceling (dp) */
        const val MAX_VERTICAL_DRIFT = 40f
    }
}
