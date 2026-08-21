/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyboardView.kt — Custom View that renders the keyboard via Canvas.
 *
 * Design principles (from DESIGN.md):
 *   - 60-120fps rendering
 *   - <8ms touch-to-visual-feedback
 *   - Touch hitbox larger than visual key
 *   - Interruptible animations
 *   - Critically damped spring physics
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
import com.akashboard.core.KeyData
import com.akashboard.core.KeyType
import com.akashboard.core.KeyboardLayoutType
import com.akashboard.core.KeyboardLayouts
import com.akashboard.core.LayoutCalculator
import com.akashboard.core.ShiftState

/**
 * Custom keyboard view rendered via Canvas for maximum performance.
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── Dimensions (must be first — used by Paint init) ───────────────────

    private val displayDensity = resources.displayMetrics.density
    private val cornerRadius = 8f * displayDensity

    // ── Paint objects (pre-allocated for performance) ─────────────────────

    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val keyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * displayDensity
    }

    private val keyPressedBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val keyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = false
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // ── State ─────────────────────────────────────────────────────────────

    private var keys = listOf<KeyData>()
    private var pressedKey: KeyData? = null
    private var currentLayoutType = KeyboardLayoutType.QWERTY
    private var shiftState = ShiftState.NONE
    private var totalHeight = 0f

    // ── Colors (Dark theme default) ───────────────────────────────────────

    private var keyBgColor = 0xFF2B2E34.toInt()
    private var keyPressedColor = 0xFF363A42.toInt()
    private var keyBorderColor = 0x1AFFFFFF
    private var keyTextColor = 0xFFF2F3F5.toInt()
    private var accentColor = 0xFF6C63FF.toInt()

    // ── Initialization ────────────────────────────────────────────────────

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    // ── Layout ────────────────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val layout = getLayoutForType(currentLayoutType)
        val calculated = LayoutCalculator.calculate(layout, width.toFloat(), displayDensity)
        keys = calculated.keys
        totalHeight = calculated.totalHeight
        setMeasuredDimension(width, totalHeight.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) {
            val layout = getLayoutForType(currentLayoutType)
            val calculated = LayoutCalculator.calculate(layout, w.toFloat(), displayDensity)
            keys = calculated.keys
            totalHeight = calculated.totalHeight
        }
    }

    private fun getLayoutForType(type: KeyboardLayoutType) = when (type) {
        KeyboardLayoutType.QWERTY -> KeyboardLayouts.QWERTY
        KeyboardLayoutType.SYMBOLS -> KeyboardLayouts.SYMBOLS
        KeyboardLayoutType.EMOJI -> KeyboardLayouts.QWERTY
        KeyboardLayoutType.NUMBERS -> KeyboardLayouts.SYMBOLS
    }

    // ── Drawing ───────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (key in keys) {
            drawKey(canvas, key)
        }
    }

    private fun drawKey(canvas: Canvas, key: KeyData) {
        val isPressed = key == pressedKey
        val rect = key.rect

        if (isPressed) {
            canvas.save()
            canvas.scale(0.92f, 0.92f, rect.centerX(), rect.centerY())
        }

        // Background
        keyBgPaint.color = if (isPressed) keyPressedColor else keyBgColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBgPaint)

        // Border
        keyBorderPaint.color = keyBorderColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBorderPaint)

        // Glow on press
        if (isPressed) {
            glowPaint.color = (accentColor and 0x00FFFFFF) or 0x40000000
            glowPaint.maskFilter = android.graphics.BlurMaskFilter(
                12f * displayDensity, android.graphics.BlurMaskFilter.Blur.NORMAL
            )
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)
        }

        // Text
        keyTextPaint.color = keyTextColor
        keyTextPaint.textSize = when {
            key.type == KeyType.SPACE -> 14f * displayDensity
            key.type in listOf(KeyType.SHIFT, KeyType.DELETE, KeyType.ENTER,
                KeyType.SYMBOLS, KeyType.LANGUAGE, KeyType.EMOJI) -> 16f * displayDensity
            else -> 22f * displayDensity
        }

        val displayLabel = when {
            key.type == KeyType.SHIFT -> when (shiftState) {
                ShiftState.NONE -> "⇧"
                ShiftState.ONE -> "⇧"
                ShiftState.LOCKED -> "⇧🔒"
            }
            else -> key.label
        }

        canvas.drawText(displayLabel, rect.centerX(), rect.centerY() + (keyTextPaint.textSize / 3), keyTextPaint)

        if (isPressed) {
            canvas.restore()
        }
    }

    // ── Touch ─────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedKey = findKeyAt(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val key = findKeyAt(event.x, event.y)
                if (key != pressedKey) {
                    pressedKey = key
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                pressedKey?.let { key ->
                    onKeyPressedListener?.onKeyPressed(key)
                }
                pressedKey = null
                invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedKey = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findKeyAt(x: Float, y: Float): KeyData? {
        return keys.find { it.hitRect.contains(x, y) }
    }

    // ── Public API ────────────────────────────────────────────────────────

    fun setLayout(type: KeyboardLayoutType) {
        if (type != currentLayoutType) {
            currentLayoutType = type
            requestLayout()
            invalidate()
        }
    }

    fun setShiftState(state: ShiftState) {
        if (state != shiftState) {
            shiftState = state
            invalidate()
        }
    }

    fun getCurrentLayoutType(): KeyboardLayoutType = currentLayoutType

    // ── Listener ──────────────────────────────────────────────────────────

    interface OnKeyPressedListener {
        fun onKeyPressed(key: KeyData)
    }

    var onKeyPressedListener: OnKeyPressedListener? = null
}
