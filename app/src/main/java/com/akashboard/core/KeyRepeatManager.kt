/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyRepeatManager.kt — Handles long-press repeat for keys.
 *
 * When the user holds a key (e.g., backspace), this manager:
 *   1. Waits for the initial delay (300ms)
 *   2. Repeats at the configured rate (50ms)
 *   3. Accelerates over time (faster deletion)
 *   4. Stops when the user lifts their finger
 *
 * The repeat is interruptible — lifting the finger stops immediately.
 */

package com.akashboard.core

import android.os.Handler
import android.os.Looper

/**
 * Key repeat manager.
 *
 * Manages long-press repeat behavior for keys like backspace.
 * Uses a Handler for timing — no coroutines needed.
 *
 * @param onRepeat Callback invoked on each repeat tick
 */
class KeyRepeatManager(
    private val onRepeat: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())

    /** Whether repeat is currently active */
    var isRepeating = false
        private set

    /** Current repeat delay (decreases over time for acceleration) */
    private var currentRepeatRate = REPEAT_RATE_INITIAL

    /** The repeat runnable */
    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (!isRepeating) return

            onRepeat()

            // Accelerate: reduce interval over time
            currentRepeatRate = (currentRepeatRate * ACCELERATION_FACTOR).toLong()
                .coerceAtLeast(REPEAT_RATE_MIN)

            handler.postDelayed(this, currentRepeatRate)
        }
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Start the repeat cycle.
     *
     * Call this when the user first presses a repeatable key.
     * The initial delay prevents accidental repeats.
     */
    fun start() {
        if (isRepeating) return

        isRepeating = true
        currentRepeatRate = REPEAT_RATE_INITIAL

        // Initial delay before repeat starts
        handler.postDelayed(repeatRunnable, INITIAL_DELAY)
    }

    /**
     * Stop the repeat cycle.
     *
     * Call this when the user lifts their finger.
     */
    fun stop() {
        isRepeating = false
        handler.removeCallbacks(repeatRunnable)
        currentRepeatRate = REPEAT_RATE_INITIAL
    }

    /**
     * Check if a key type supports repeat.
     */
    companion object {
        /** Initial delay before repeat starts (ms) */
        const val INITIAL_DELAY = 300L

        /** Initial repeat rate (ms between repeats) */
        const val REPEAT_RATE_INITIAL = 50L

        /** Minimum repeat rate (fastest) */
        const val REPEAT_RATE_MIN = 20L

        /** Acceleration factor (each repeat reduces interval) */
        const val ACCELERATION_FACTOR = 0.92

        /**
         * Check if a key type supports long-press repeat.
         */
        fun supportsRepeat(type: KeyType): Boolean {
            return type == KeyType.DELETE
        }
    }

    /**
     * Clean up resources.
     */
    fun destroy() {
        stop()
        handler.removeCallbacksAndMessages(null)
    }
}
