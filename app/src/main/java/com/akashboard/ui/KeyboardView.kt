/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyboardView.kt — Custom keyboard view with swipe typing.
 *
 * Week 5 additions:
 *   - Swipe gesture detection
 *   - Visual trail rendering
 *   - Swipe-to-word matching
 */

package com.akashboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.akashboard.core.KeyData
import com.akashboard.core.KeyType
import com.akashboard.core.KeyRepeatManager
import com.akashboard.core.KeyboardLayoutType
import com.akashboard.core.KeyboardLayouts
import com.akashboard.core.LayoutCalculator
import com.akashboard.core.PopupPreviewManager
import com.akashboard.core.PopupState
import com.akashboard.core.ShiftState
import com.akashboard.core.SpacebarCursorManager
import com.akashboard.core.SwipeDetector
import com.akashboard.core.SwipeTrail

/**
 * Custom keyboard view with swipe typing support.
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── Dimensions ────────────────────────────────────────────────────────

    private val displayDensity = resources.displayMetrics.density
    private val cornerRadius = 8f * displayDensity

    // ── Paint objects ─────────────────────────────────────────────────────

    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val keyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * displayDensity
    }
    private val keyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val popupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF363A42.toInt()
    }
    private val popupTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 24f * displayDensity
    }

    // ── State ─────────────────────────────────────────────────────────────

    private var keys = listOf<KeyData>()
    private var pressedKey: KeyData? = null
    private var currentLayoutType = KeyboardLayoutType.QWERTY
    private var shiftState = ShiftState.NONE
    private var totalHeight = 0f
    private var popupState: PopupState = PopupState()

    // ── Managers ──────────────────────────────────────────────────────────

    private val keyRepeatManager = KeyRepeatManager { onRepeatTick() }
    private val popupPreviewManager = PopupPreviewManager()
    private val spacebarCursorManager = SpacebarCursorManager()
    private val swipeTrail = SwipeTrail(displayDensity)
    private var swipeDetector: SwipeDetector? = null

    // ── Touch tracking ────────────────────────────────────────────────────

    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isSpacebarGesture = false
    private var longPressTriggered = false
    private var isSwipeGesture = false
    private var swipeThreshold = 30f * displayDensity

    // ── Colors ────────────────────────────────────────────────────────────

    private var keyBgColor = 0xFF2B2E34.toInt()
    private var keyPressedColor = 0xFF363A42.toInt()
    private var keyBorderColor = 0x1AFFFFFF
    private var keyTextColor = 0xFFF2F3F5.toInt()
    private var accentColor = 0xFF6C63FF.toInt()

    // ── Initialization ────────────────────────────────────────────────────

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)

        popupPreviewManager.onStateChanged = { state ->
            popupState = state
            invalidate()
        }

        spacebarCursorManager.onCursorMove = { delta ->
            onCursorMoveListener?.onCursorMove(delta)
        }
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
            initSwipeDetector()
        }
    }

    private fun initSwipeDetector() {
        val keyMap = mutableMapOf<Char, PointF>()
        for (key in keys) {
            if (key.type == KeyType.LETTER && key.label.length == 1) {
                keyMap[key.label[0].lowercaseChar()] = PointF(key.rect.centerX(), key.rect.centerY())
            }
        }
        swipeDetector = SwipeDetector(keyMap)
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

        // Draw keys
        for (key in keys) {
            drawKey(canvas, key)
        }

        // Draw swipe trail
        if (swipeTrail.isVisible) {
            swipeTrail.draw(canvas)
        }

        // Draw popup preview
        if (popupState.visible) {
            drawPopup(canvas)
        }
    }

    private fun drawKey(canvas: Canvas, key: KeyData) {
        val isPressed = key == pressedKey
        val rect = key.rect

        if (isPressed) {
            canvas.save()
            canvas.scale(0.92f, 0.92f, rect.centerX(), rect.centerY())
        }

        keyBgPaint.color = if (isPressed) keyPressedColor else keyBgColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBgPaint)

        keyBorderPaint.color = keyBorderColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBorderPaint)

        if (isPressed) {
            glowPaint.color = (accentColor and 0x00FFFFFF) or 0x40000000
            glowPaint.maskFilter = android.graphics.BlurMaskFilter(
                12f * displayDensity, android.graphics.BlurMaskFilter.Blur.NORMAL
            )
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)
        }

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

    private fun drawPopup(canvas: Canvas) {
        val state = popupState
        if (!state.visible || state.label == null) return

        val popupWidth = 48f * displayDensity
        val popupHeight = 48f * displayDensity
        val popupRadius = 8f * displayDensity

        val popupRect = RectF(
            state.x - popupWidth / 2,
            state.y - popupHeight,
            state.x + popupWidth / 2,
            state.y
        )

        popupBgPaint.color = if (state.alternateSelected) accentColor else 0xFF363A42.toInt()
        canvas.drawRoundRect(popupRect, popupRadius, popupRadius, popupBgPaint)

        popupTextPaint.color = Color.WHITE
        canvas.drawText(state.label, state.x, state.y - popupHeight / 2 + popupTextPaint.textSize / 3, popupTextPaint)
    }

    // ── Touch handling ────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> handleTouchDown(event)
            MotionEvent.ACTION_MOVE -> handleTouchMove(event)
            MotionEvent.ACTION_UP -> handleTouchUp(event)
            MotionEvent.ACTION_CANCEL -> handleTouchCancel()
        }
        return true
    }

    private fun handleTouchDown(event: MotionEvent) {
        val x = event.x
        val y = event.y
        touchStartX = x
        touchStartY = y
        longPressTriggered = false
        isSwipeGesture = false

        val key = findKeyAt(x, y)
        pressedKey = key
        invalidate()

        if (key == null) return

        // Start swipe detection for letter keys
        if (key.type == KeyType.LETTER) {
            swipeDetector?.onTouchDown(x, y)
            swipeTrail.start(x, y)
        }

        // Start long-press repeat for backspace
        if (KeyRepeatManager.supportsRepeat(key.type)) {
            keyRepeatManager.start()
        }

        // Start long-press popup for letter keys
        if (key.type == KeyType.LETTER) {
            postDelayed({
                if (pressedKey == key && !longPressTriggered && !isSwipeGesture) {
                    longPressTriggered = true
                    popupPreviewManager.show(key, displayDensity)
                }
            }, PopupPreviewManager.LONG_PRESS_DELAY)
        }

        // Start spacebar cursor tracking
        if (key.type == KeyType.SPACE) {
            isSpacebarGesture = true
            spacebarCursorManager.startTracking(x)
        }
    }

    private fun handleTouchMove(event: MotionEvent) {
        val x = event.x
        val y = event.y

        // Handle spacebar cursor movement
        if (isSpacebarGesture && spacebarCursorManager.isTracking) {
            val verticalDrift = Math.abs(y - touchStartY)
            if (verticalDrift > SpacebarCursorManager.MAX_VERTICAL_DRIFT * displayDensity) {
                spacebarCursorManager.cancelTracking()
                isSpacebarGesture = false
            } else {
                spacebarCursorManager.update(x, displayDensity)
                return
            }
        }

        // Handle swipe gesture
        if (!isSpacebarGesture && !longPressTriggered) {
            val dx = x - touchStartX
            val dy = y - touchStartY
            val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            if (!isSwipeGesture && distance > swipeThreshold) {
                // Movement exceeded threshold — start swipe
                isSwipeGesture = true
                popupPreviewManager.forceDismiss()
            }

            if (isSwipeGesture) {
                swipeDetector?.onTouchMove(x, y)
                swipeTrail.addPoint(x, y)
                invalidate()

                // Highlight closest key
                val closestKey = findKeyAt(x, y)
                if (closestKey != pressedKey) {
                    pressedKey = closestKey
                    invalidate()
                }
                return
            }
        }

        // Handle popup preview slide selection
        if (popupState.visible) {
            popupPreviewManager.update(y, displayDensity)
            return
        }

        // Normal key tracking
        val key = findKeyAt(x, y)
        if (key != pressedKey) {
            pressedKey = key
            if (key == null || key.type != pressedKey?.type) {
                keyRepeatManager.stop()
                popupPreviewManager.forceDismiss()
            }
            invalidate()
        }
    }

    private fun handleTouchUp(event: MotionEvent) {
        // Handle swipe gesture completion
        if (isSwipeGesture) {
            val results = swipeDetector?.onTouchUp() ?: emptyList()
            swipeTrail.end()

            if (results.isNotEmpty()) {
                onSwipeListener?.onSwipeCompleted(results[0])
            }

            isSwipeGesture = false
            pressedKey = null
            invalidate()
            return
        }

        // End spacebar cursor gesture
        if (isSpacebarGesture) {
            spacebarCursorManager.endTracking()
            isSpacebarGesture = false
        }

        // Dismiss popup
        if (popupState.visible) {
            popupPreviewManager.dismiss()
        }

        // Stop key repeat
        keyRepeatManager.stop()

        // Fire key press event
        pressedKey?.let { key ->
            onKeyPressedListener?.onKeyPressed(key)
        }

        pressedKey = null
        longPressTriggered = false
        invalidate()
    }

    private fun handleTouchCancel() {
        keyRepeatManager.stop()
        popupPreviewManager.forceDismiss()
        spacebarCursorManager.cancelTracking()
        swipeDetector?.cancel()
        swipeTrail.cancel()
        isSpacebarGesture = false
        isSwipeGesture = false
        pressedKey = null
        longPressTriggered = false
        invalidate()
    }

    private fun findKeyAt(x: Float, y: Float): KeyData? {
        return keys.find { it.hitRect.contains(x, y) }
    }

    private fun onRepeatTick() {
        pressedKey?.let { key ->
            onKeyPressedListener?.onKeyPressed(key)
        }
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

    fun destroy() {
        keyRepeatManager.destroy()
    }

    // ── Listeners ─────────────────────────────────────────────────────────

    interface OnKeyPressedListener {
        fun onKeyPressed(key: KeyData)
    }

    interface OnCursorMoveListener {
        fun onCursorMove(deltaChars: Int)
    }

    interface OnSwipeListener {
        fun onSwipeCompleted(word: String)
    }

    var onKeyPressedListener: OnKeyPressedListener? = null
    var onCursorMoveListener: OnCursorMoveListener? = null
    var onSwipeListener: OnSwipeListener? = null
}
