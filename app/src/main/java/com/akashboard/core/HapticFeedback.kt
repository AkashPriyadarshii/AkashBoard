/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * HapticFeedback.kt — Haptic feedback patterns for keyboard interactions.
 *
 * Different actions produce different vibration patterns:
 *   - Key press: short, light tap
 *   - Modifier (shift): medium tap
 *   - Error: double tap
 *   - Mode switch: distinct pattern
 *
 * Haptic intensity is configurable via settings.
 */

package com.akashboard.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Haptic feedback manager.
 *
 * Provides vibration patterns for keyboard interactions.
 * All patterns are short and subtle — the keyboard should feel
 * tactile without being annoying.
 */
class HapticFeedback(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var enabled = true
    private var intensity = 1.0f  // 0.0 to 1.0

    /**
     * Enable or disable haptic feedback.
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /**
     * Set haptic intensity (0.0 = off, 1.0 = max).
     */
    fun setIntensity(intensity: Float) {
        this.intensity = intensity.coerceIn(0f, 1f)
    }

    /**
     * Key press feedback — short, light tap.
     *
     * Duration: 10ms
     * Pattern: single tap
     */
    fun keyPress() {
        vibrate(10, 0.6f)
    }

    /**
     * Modifier feedback — slightly stronger tap.
     *
     * Used for shift, caps lock, symbols toggle.
     * Duration: 15ms
     */
    fun modifier() {
        vibrate(15, 0.8f)
    }

    /**
     * Error feedback — distinct double tap.
     *
     * Used for invalid actions, autocorrect warnings.
     * Duration: 10ms + 50ms gap + 10ms
     */
    fun error() {
        if (!enabled || vibrator?.hasVibrator() != true) return

        val effect1 = VibrationEffect.createOneShot(
            (10 * intensity).toLong(),
            (80 * intensity).toInt()
        )
        vibrator?.vibrate(effect1)

        // Schedule second tap
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val effect2 = VibrationEffect.createOneShot(
                (10 * intensity).toLong(),
                (80 * intensity).toInt()
            )
            vibrator?.vibrate(effect2)
        }, 60)
    }

    /**
     * Selection feedback — medium tap.
     *
     * Used for suggestion selection, clipboard paste.
     * Duration: 20ms
     */
    fun selection() {
        vibrate(20, 0.7f)
    }

    /**
     * Mode switch feedback — distinct pattern.
     *
     * Used for keyboard layout changes, language switches.
     * Duration: 25ms
     */
    fun modeSwitch() {
        vibrate(25, 0.9f)
    }

    /**
     * Perform a vibration with given duration and amplitude.
     */
    private fun vibrate(durationMs: Long, amplitude: Float) {
        if (!enabled || vibrator?.hasVibrator() != true) return

        val effectiveDuration = (durationMs * intensity).toLong()
        val effectiveAmplitude = (amplitude * 255 * intensity).toInt().coerceIn(1, 255)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(
                effectiveDuration,
                effectiveAmplitude
            )
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(effectiveDuration)
        }
    }
}
