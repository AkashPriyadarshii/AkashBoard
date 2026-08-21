/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SuggestionBar.kt — Prediction strip with animations.
 *
 * Week 6: Added slide-in/out animations for suggestions.
 *   - New suggestions slide in from right (200ms)
 *   - Staggered: each suggestion delays by 50ms
 *   - Tap to accept: word scales up, others fade
 */

package com.akashboard.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * Suggestion bar with animated suggestions.
 */
class SuggestionBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── Dimensions ────────────────────────────────────────────────────────

    private val density = resources.displayMetrics.density
    private val barHeight = 48f * density
    private val micButtonWidth = 48f * density

    // ── Paint objects ─────────────────────────────────────────────────────

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x0D000000
    }

    private val suggestionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB8B8CC.toInt()
        textSize = 16f * density
        textAlign = Paint.Align.CENTER
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x1AFFFFFF
        strokeWidth = 1f * density
    }

    private val micPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFA6ABB4.toInt()
        textSize = 20f * density
        textAlign = Paint.Align.CENTER
    }

    // ── State ─────────────────────────────────────────────────────────────

    private var suggestions = listOf<String>()
    private var suggestionRects = mutableListOf<RectF>()
    private var micRect = RectF()

    /** Animation offsets for each suggestion (0.0 = off-screen right, 1.0 = in place) */
    private val animOffsets = FloatArray(3) { 1f }

    /** Animator for slide-in effect */
    private var slideAnimator: ValueAnimator? = null

    // ── Initialization ────────────────────────────────────────────────────

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    // ── Layout ────────────────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, barHeight.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) calculateLayout(w.toFloat())
    }

    private fun calculateLayout(width: Float) {
        suggestionRects.clear()
        val availableWidth = width - micButtonWidth
        val suggestionWidth = availableWidth / 3f

        for (i in 0 until 3) {
            suggestionRects.add(RectF(i * suggestionWidth, 0f, (i + 1) * suggestionWidth, barHeight))
        }
        micRect = RectF(width - micButtonWidth, 0f, width, barHeight)
    }

    // ── Drawing ───────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), barHeight, bgPaint)

        for (i in suggestions.indices.take(3)) {
            val rect = suggestionRects.getOrNull(i) ?: continue

            // Apply slide animation offset
            val offset = animOffsets[i]
            val slideX = (1f - offset) * rect.width()

            canvas.save()
            canvas.translate(slideX, 0f)

            if (i > 0) {
                canvas.drawLine(rect.left, 8f * density, rect.left, barHeight - 8f * density, dividerPaint)
            }

            // Fade based on animation progress
            suggestionTextPaint.alpha = (offset * 255).toInt().coerceIn(0, 255)
            canvas.drawText(suggestions[i], rect.centerX(), rect.centerY() + suggestionTextPaint.textSize / 3, suggestionTextPaint)

            canvas.restore()
        }

        canvas.drawText("🎤", micRect.centerX(), micRect.centerY() + micPaint.textSize / 3, micPaint)
    }

    // ── Touch ─────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y

            if (micRect.contains(x, y)) {
                onMicClickListener?.onClick()
                return true
            }

            for (i in suggestions.indices.take(3)) {
                val rect = suggestionRects.getOrNull(i) ?: continue
                // Adjust for slide offset
                val adjustedRect = RectF(
                    rect.left + (1f - animOffsets[i]) * rect.width(),
                    rect.top,
                    rect.right + (1f - animOffsets[i]) * rect.width(),
                    rect.bottom
                )
                if (adjustedRect.contains(x, y)) {
                    onSuggestionClickListener?.onSuggestionClicked(i, suggestions[i])
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Update suggestions with slide-in animation.
     */
    fun setSuggestions(newSuggestions: List<String>) {
        suggestions = newSuggestions.take(3)

        // Reset offsets to off-screen
        for (i in animOffsets.indices) {
            animOffsets[i] = 0f
        }

        // Animate each suggestion in with stagger
        slideAnimator?.cancel()
        slideAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                for (i in animOffsets.indices) {
                    // Stagger: each suggestion delays by 50ms worth of progress
                    val staggerOffset = i * 0.15f
                    animOffsets[i] = ((value - staggerOffset) / (1f - staggerOffset)).coerceIn(0f, 1f)
                }
                invalidate()
            }
            start()
        }
    }

    fun clearSuggestions() {
        suggestions = emptyList()
        for (i in animOffsets.indices) animOffsets[i] = 1f
        invalidate()
    }

    // ── Listeners ─────────────────────────────────────────────────────────

    interface OnSuggestionClickListener {
        fun onSuggestionClicked(index: Int, word: String)
    }

    interface OnMicClickListener {
        fun onClick()
    }

    var onSuggestionClickListener: OnSuggestionClickListener? = null
    var onMicClickListener: OnMicClickListener? = null
}
