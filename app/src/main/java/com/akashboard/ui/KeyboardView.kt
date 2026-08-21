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
import kotlin.math.min

/**
 * Custom keyboard view rendered via Canvas for maximum performance.
 *
 *为什么不使用 XML layouts:
 *   - Canvas rendering gives us 60-120fps consistently
 *   - No XML inflation overhead
 *   - Full control over touch hitboxes
 *   - GPU-accelerated via HardwareLayer
 *   - No framework layout measurement costs
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── Paint objects (pre-allocated for performance) ─────────────────────

    private val keyBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val keyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * resources.displayMetrics.density
    }

    private val keyPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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

    // ── Key data ──────────────────────────────────────────────────────────

    data class Key(
        val label: String,
        val code: Int,
        val rect: RectF,
        val hitRect: RectF,       // Larger touch region
        val isWide: Boolean = false
    )

    private val keys = mutableListOf<Key>()
    private var pressedKey: Key? = null

    // ── Colors (from DesignTokens) ────────────────────────────────────────

    private var keyBgColor = 0xFF2B2E34.toInt()
    private var keyPressedColor = 0xFF363A42.toInt()
    private var keyBorderColor = 0x1AFFFFFF
    private var keyTextColor = 0xFFF2F3F5.toInt()
    private var accentColor = 0xFF6C63FF.toInt()

    // ── Dimensions ────────────────────────────────────────────────────────

    private val density = resources.displayMetrics.density
    private val cornerRadius = 8f * density
    private val keyGap = 6f * density
    private val keyPadding = 4f * density
    private val hitboxExpansion = 4f * density  // Extra touch area

    // ── Layout ────────────────────────────────────────────────────────────

    private val qwertyRows = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("⇧", "Z", "X", "C", "V", "B", "N", "M", "⌫"),
        listOf("?123", "🌐", "😊", "SPACE", ",", "⏎")
    )

    // ── Initialization ────────────────────────────────────────────────────

    init {
        // Enable hardware layer for GPU-accelerated rendering
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    // ── Layout calculation ────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val keyHeight = 46f * density
        val bottomRowHeight = 52f * density
        val suggestionBarHeight = 48f * density
        val totalHeight = suggestionBarHeight +
                (keyHeight * 3) + keyGap * 3 +
                bottomRowHeight + keyGap

        setMeasuredDimension(width, totalHeight.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateKeyPositions(w, h)
    }

    private fun calculateKeyPositions(width: Int, height: Int) {
        keys.clear()

        val suggestionBarHeight = 48f * density
        val keyHeight = 46f * density
        val bottomRowHeight = 52f * density
        val totalKeys = qwertyRows.maxOf { it.size }

        var yOffset = suggestionBarHeight

        for ((rowIndex, row) in qwertyRows.withIndex()) {
            val isBottomRow = rowIndex == qwertyRows.size - 1
            val rowHeight = if (isBottomRow) bottomRowHeight else keyHeight
            val rowCount = row.size

            // Calculate key width for this row
            val availableWidth = width.toFloat() - (keyGap * (rowCount + 1))
            val standardKeyWidth = availableWidth / totalKeys

            var xOffset = keyGap

            for (keyLabel in row) {
                val keyWidth = when (keyLabel) {
                    "⇧", "⌫" -> standardKeyWidth * 1.5f
                    "?123", "🌐", "😊", "," -> standardKeyWidth * 1.2f
                    "SPACE" -> standardKeyWidth * 4f
                    "⏎" -> standardKeyWidth * 1.8f
                    else -> standardKeyWidth
                }

                val rect = RectF(
                    xOffset,
                    yOffset,
                    xOffset + keyWidth,
                    yOffset + rowHeight
                )

                // Hitbox is larger than visual key
                val hitRect = RectF(
                    rect.left - hitboxExpansion,
                    rect.top - hitboxExpansion,
                    rect.right + hitboxExpansion,
                    rect.bottom + hitboxExpansion
                )

                val code = when (keyLabel) {
                    "⇧" -> KEYCODE_SHIFT
                    "⌫" -> KEYCODE_DELETE
                    "SPACE" -> KEYCODE_SPACE
                    "⏎" -> KEYCODE_ENTER
                    "?123" -> KEYCODE_SYMBOLS
                    "🌐" -> KEYCODE_LANGUAGE
                    "😊" -> KEYCODE_EMOJI
                    "," -> KEYCODE_COMMA
                    else -> keyLabel[0].code
                }

                keys.add(Key(
                    label = keyLabel,
                    code = code,
                    rect = rect,
                    hitRect = hitRect
                ))

                xOffset += keyWidth + keyGap
            }

            yOffset += rowHeight + keyGap
        }
    }

    // ── Drawing ───────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (key in keys) {
            drawKey(canvas, key)
        }
    }

    private fun drawKey(canvas: Canvas, key: Key) {
        val isPressed = key == pressedKey
        val rect = key.rect

        // Scale animation for pressed state
        if (isPressed) {
            val scale = 0.92f
            val cx = rect.centerX()
            val cy = rect.centerY()
            canvas.save()
            canvas.scale(scale, scale, cx, cy)
        }

        // Key background
        keyBackgroundPaint.color = if (isPressed) keyPressedColor else keyBgColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBackgroundPaint)

        // Key border
        keyBorderPaint.color = keyBorderColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBorderPaint)

        // Glow effect on press
        if (isPressed) {
            glowPaint.color = (accentColor and 0x00FFFFFF) or 0x40000000
            glowPaint.maskFilter = android.graphics.BlurMaskFilter(
                12f * density, android.graphics.BlurMaskFilter.Blur.NORMAL
            )
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)
        }

        // Key text
        keyTextPaint.color = keyTextColor
        keyTextPaint.textSize = when (key.label) {
            "SPACE" -> 14f * density
            "⇧", "⌫", "⏎", "?123", "🌐", "😊" -> 16f * density
            else -> 22f * density
        }

        val textY = rect.centerY() + (keyTextPaint.textSize / 3)
        canvas.drawText(key.label, rect.centerX(), textY, keyTextPaint)

        if (isPressed) {
            canvas.restore()
        }
    }

    // ── Touch handling ────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val key = findKeyAt(event.x, event.y)
                if (key != pressedKey) {
                    pressedKey = key
                    invalidate()
                    // Future: Haptic feedback
                }
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
                    // Future: Haptic feedback, sound
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

    private fun findKeyAt(x: Float, y: Float): Key? {
        // Check hit regions (larger than visual keys)
        return keys.find { it.hitRect.contains(x, y) }
    }

    // ── Listener ──────────────────────────────────────────────────────────

    interface OnKeyPressedListener {
        fun onKeyPressed(key: Key)
    }

    var onKeyPressedListener: OnKeyPressedListener? = null

    // ── Key codes ─────────────────────────────────────────────────────────

    companion object {
        const val KEYCODE_SHIFT = -100
        const val KEYCODE_DELETE = -101
        const val KEYCODE_SPACE = 32
        const val KEYCODE_ENTER = -102
        const val KEYCODE_SYMBOLS = -103
        const val KEYCODE_LANGUAGE = -104
        const val KEYCODE_EMOJI = -105
        const val KEYCODE_COMMA = 44
    }
}
