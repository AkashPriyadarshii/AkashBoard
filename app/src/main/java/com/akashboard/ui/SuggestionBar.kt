/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SuggestionBar.kt — Prediction strip above the keyboard.
 *
 * Shows top 3 word suggestions from the Rust prediction engine.
 * Tap to accept, tap mic for voice input.
 *
 * Design (from DESIGN.md):
 *   - Frosted glass background (near-opaque for legibility)
 *   - Suggestions slide in with stagger (50ms delay each)
 *   - Tap to accept → word slides up, others fade
 *   - Rightmost: mic button for voice input
 */

package com.akashboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Suggestion bar rendered via Canvas.
 *
 * Shows up to 3 word suggestions plus a mic button.
 * Positioned above the keyboard in the IME layout.
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
        color = 0x0D000000  // Near-transparent dark
    }

    private val suggestionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB8B8CC.toInt()
        textSize = 16f * density
        textAlign = Paint.Align.CENTER
    }

    private val suggestionHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF6C63FF.toInt()
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
        if (w > 0) {
            calculateLayout(w.toFloat())
        }
    }

    private fun calculateLayout(width: Float) {
        suggestionRects.clear()

        val availableWidth = width - micButtonWidth
        val suggestionWidth = availableWidth / 3f

        for (i in 0 until 3) {
            val rect = RectF(
                i * suggestionWidth,
                0f,
                (i + 1) * suggestionWidth,
                barHeight
            )
            suggestionRects.add(rect)
        }

        micRect = RectF(
            width - micButtonWidth,
            0f,
            width,
            barHeight
        )
    }

    // ── Drawing ───────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Background
        canvas.drawRect(0f, 0f, width.toFloat(), barHeight, bgPaint)

        // Suggestions
        for (i in suggestions.indices.take(3)) {
            val rect = suggestionRects.getOrNull(i) ?: continue
            val text = suggestions[i]

            // Divider between suggestions
            if (i > 0) {
                canvas.drawLine(rect.left, 8f * density, rect.left, barHeight - 8f * density, dividerPaint)
            }

            // Text (centered in rect)
            canvas.drawText(text, rect.centerX(), rect.centerY() + suggestionTextPaint.textSize / 3, suggestionTextPaint)
        }

        // Mic button
        canvas.drawText("🎤", micRect.centerX(), micRect.centerY() + micPaint.textSize / 3, micPaint)
    }

    // ── Touch handling ────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y

            // Check mic button
            if (micRect.contains(x, y)) {
                onMicClickListener?.onClick()
                return true
            }

            // Check suggestions
            for (i in suggestions.indices.take(3)) {
                val rect = suggestionRects.getOrNull(i) ?: continue
                if (rect.contains(x, y)) {
                    onSuggestionClickListener?.onSuggestionClicked(i, suggestions[i])
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Update the displayed suggestions.
     */
    fun setSuggestions(newSuggestions: List<String>) {
        suggestions = newSuggestions.take(3)
        invalidate()
    }

    /**
     * Clear all suggestions.
     */
    fun clearSuggestions() {
        suggestions = emptyList()
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
