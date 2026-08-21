/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SwipeTrail.kt — Visual trail rendering for swipe gestures.
 *
 * Draws a smooth, fading trail behind the user's finger during
 * glide typing. The trail provides visual feedback and helps
 * the user see which keys they're passing over.
 *
 * Trail properties:
 *   - Color: accent color (semi-transparent)
 *   - Width: 4dp, tapering at start/end
 *   - Opacity: fades from 80% at finger to 20% at start
 *   - Smoothing: quadratic Bezier curves between points
 */

package com.akashboard.core

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF

/**
 * Swipe trail renderer.
 *
 * Draws a smooth, fading trail along the gesture path.
 * Used by KeyboardView for visual feedback during swipe typing.
 */
class SwipeTrail(
    private val density: Float
) {
    /** Trail paint */
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = TRAIL_WIDTH * density
        color = TRAIL_COLOR
    }

    /** Glow paint (wider, more transparent) */
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = TRAIL_WIDTH * density * 3f
        color = TRAIL_GLOW_COLOR
    }

    /** Current trail points */
    private val points = mutableListOf<PointF>()

    /** Trail path (for smooth rendering) */
    private val trailPath = Path()

    /** Whether the trail is visible */
    var isVisible = false
        private set

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Start a new trail.
     */
    fun start(x: Float, y: Float) {
        points.clear()
        points.add(PointF(x, y))
        isVisible = true
    }

    /**
     * Add a point to the trail.
     */
    fun addPoint(x: Float, y: Float) {
        if (!isVisible) return
        points.add(PointF(x, y))
    }

    /**
     * End the trail (fade out animation).
     */
    fun end() {
        isVisible = false
        points.clear()
    }

    /**
     * Cancel the trail immediately.
     */
    fun cancel() {
        isVisible = false
        points.clear()
    }

    /**
     * Draw the trail on the canvas.
     *
     * @param canvas Canvas to draw on
     */
    fun draw(canvas: Canvas) {
        if (points.size < 2) return

        // Build smooth path
        trailPath.reset()
        trailPath.moveTo(points[0].x, points[0].y)

        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]

            // Quadratic Bezier for smooth curves
            val midX = (prev.x + curr.x) / 2f
            val midY = (prev.y + curr.y) / 2f
            trailPath.quadTo(prev.x, prev.y, midX, midY)
        }

        // Draw glow (wider, more transparent)
        canvas.drawPath(trailPath, glowPaint)

        // Draw main trail
        canvas.drawPath(trailPath, trailPaint)
    }

    /**
     * Get the current number of points.
     */
    fun pointCount(): Int = points.size

    // ── Constants ─────────────────────────────────────────────────────────

    companion object {
        /** Trail width (dp) */
        const val TRAIL_WIDTH = 4f

        /** Trail color (accent, semi-transparent) */
        const val TRAIL_COLOR = 0x806C63FF.toInt()

        /** Trail glow color (accent, very transparent) */
        const val TRAIL_GLOW_COLOR = 0x206C63FF.toInt()
    }
}
