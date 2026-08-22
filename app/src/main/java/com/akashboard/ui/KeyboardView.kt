/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * KeyboardView.kt — Keyboard view with theme support.
 *
 * Week 7: Reads colors from ThemeManager instead of hardcoding.
 */

package com.akashboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import java.lang.ref.WeakReference
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
import com.akashboard.core.SwipeDictionary
import com.akashboard.core.SwipeTrail
import com.akashboard.theme.ThemeColors

/**
 * Keyboard view with theme support.
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val displayDensity = resources.displayMetrics.density
    private var cornerRadius = 8f * displayDensity

    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val keyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1f * displayDensity }
    private val keyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val popupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = 0xFF363A42.toInt() }
    private val popupTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER; textSize = 24f * displayDensity }

    private var keys = listOf<KeyData>()
    private var pressedKey: KeyData? = null
    private var currentLayoutType = KeyboardLayoutType.QWERTY
    private var shiftState = ShiftState.NONE
    private var totalHeight = 0f
    private var popupState: PopupState = PopupState()

    private val keyRepeatManager = KeyRepeatManager(onRepeat = { onRepeatTick() })
    private val popupPreviewManager = PopupPreviewManager()
    private val spacebarCursorManager = SpacebarCursorManager()
    private val swipeTrail = SwipeTrail(displayDensity)
    private var swipeDetector: SwipeDetector? = null

    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isSpacebarGesture = false
    private var longPressTriggered = false
    private var isSwipeGesture = false
    private var swipeThreshold = 30f * displayDensity

    /** Current theme colors — set via setThemeColors() */
    private var themeColors: ThemeColors? = null
    private var a11yProvider: KeyboardAccessibilityNodeProvider? = null

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        popupPreviewManager.onStateChanged = { popupState = it; invalidate() }
        spacebarCursorManager.onCursorMove = { delta -> onCursorMoveListener?.onCursorMove(delta) }
    }

    // ── Theme ─────────────────────────────────────────────────────────────

    fun setThemeColors(colors: ThemeColors) {
        themeColors = colors
        invalidate()
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
        for (key in keys) drawKey(canvas, key)
        if (swipeTrail.isVisible) swipeTrail.draw(canvas)
        if (popupState.visible) drawPopup(canvas)
    }

    private fun drawKey(canvas: Canvas, key: KeyData) {
        val colors = themeColors
        val isPressed = key == pressedKey
        val rect = key.rect

        if (isPressed) {
            canvas.save()
            canvas.scale(0.92f, 0.92f, rect.centerX(), rect.centerY())
        }

        keyBgPaint.color = colors?.keyBg ?: 0xFF2B2E34.toInt()
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBgPaint)

        keyBorderPaint.color = colors?.keyBorder ?: 0x1AFFFFFF
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, keyBorderPaint)

        if (isPressed) {
            glowPaint.color = ((colors?.accent ?: 0xFF6C63FF.toInt()) and 0x00FFFFFF) or 0x40000000
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)
        }

        keyTextPaint.color = colors?.keyText ?: 0xFFF2F3F5.toInt()
        keyTextPaint.textSize = when {
            key.type == KeyType.SPACE -> 14f * displayDensity
            key.type in listOf(KeyType.SHIFT, KeyType.DELETE, KeyType.ENTER,
                KeyType.SYMBOLS, KeyType.LANGUAGE, KeyType.EMOJI) -> 16f * displayDensity
            else -> 22f * displayDensity
        }

        val displayLabel = when {
            key.type == KeyType.SHIFT -> when (shiftState) {
                ShiftState.NONE -> "⇧"; ShiftState.ONE -> "⇧"; ShiftState.LOCKED -> "⇧🔒"
            }
            else -> key.label
        }

        canvas.drawText(displayLabel, rect.centerX(), rect.centerY() + (keyTextPaint.textSize / 3), keyTextPaint)
        if (isPressed) canvas.restore()
    }

    private fun drawPopup(canvas: Canvas) {
        val state = popupState
        if (!state.visible || state.label == null) return
        val colors = themeColors

        val popupWidth = 48f * displayDensity
        val popupHeight = 48f * displayDensity
        val popupRadius = 8f * displayDensity
        val popupRect = RectF(state.x - popupWidth / 2, state.y - popupHeight, state.x + popupWidth / 2, state.y)

        popupBgPaint.color = if (state.alternateSelected) (colors?.accent ?: 0xFF6C63FF.toInt()) else 0xFF363A42.toInt()
        canvas.drawRoundRect(popupRect, popupRadius, popupRadius, popupBgPaint)
        popupTextPaint.color = Color.WHITE
        canvas.drawText(state.label, state.x, state.y - popupHeight / 2 + popupTextPaint.textSize / 3, popupTextPaint)
    }

    // ── Touch ─────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> handleTouchDown(event)
            MotionEvent.ACTION_MOVE -> handleTouchMove(event)
            MotionEvent.ACTION_UP -> handleTouchUp(event)
            MotionEvent.ACTION_CANCEL -> handleTouchCancel()
        }
        return true
    }

    // ── Accessibility ─────────────────────────────────────────────────────

    /** Expose keys to screen readers via virtual node hierarchy. */
    override fun getAccessibilityNodeProvider(): AccessibilityNodeProvider {
        if (a11yProvider == null) a11yProvider = KeyboardAccessibilityNodeProvider(this)
        return a11yProvider!!
    }

    /** Announce the pressed key's label for TalkBack users. */
    private fun announceKey(key: KeyData) {
        contentDescription = key.accessibilityLabel
        announceForAccessibility(key.accessibilityLabel)
    }

    private class KeyboardAccessibilityNodeProvider(view: KeyboardView) :
        AccessibilityNodeProvider() {

        private val viewRef = WeakReference(view)

        override fun createAccessibilityNodeInfo(virtualViewId: Int): AccessibilityNodeInfo? {
            val view = viewRef.get() ?: return null
            if (virtualViewId == HOST_VIEW_ID) {
                return AccessibilityNodeInfo.obtain(view).apply {
                    className = view.javaClass.name
                    contentDescription = "Keyboard"
                    // ponytail: single flat node list; add grouping/rows only if TalkBack navigation demands it
                }
            }
            val key = view.keys.getOrNull(virtualViewId) ?: return null
            val node = AccessibilityNodeInfo.obtain(view, virtualViewId)
            node.className = "android.widget.Button"
            node.contentDescription = key.accessibilityLabel
            node.setBoundsInParent(
                android.graphics.Rect(
                    key.rect.left.toInt(), key.rect.top.toInt(),
                    key.rect.right.toInt(), key.rect.bottom.toInt()
                )
            )
            node.isClickable = true
            node.addAction(AccessibilityNodeInfo.ACTION_CLICK)
            return node
        }

        override fun performAction(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
            val view = viewRef.get() ?: return false
            if (action == AccessibilityNodeInfo.ACTION_CLICK) {
                val key = view.keys.getOrNull(virtualViewId) ?: return false
                view.onKeyPressedListener?.onKeyPressed(key)
                view.invalidate()
                return true
            }
            return false
        }
    }

    private fun handleTouchDown(event: MotionEvent) {
        val x = event.x; val y = event.y
        touchStartX = x; touchStartY = y; longPressTriggered = false; isSwipeGesture = false
        val key = findKeyAt(x, y); pressedKey = key; invalidate()
        if (key == null) return
        if (key.type == KeyType.LETTER && swipeTypingEnabled) { swipeDetector?.onTouchDown(x, y); swipeTrail.start(x, y) }
        if (longPressRepeatEnabled && KeyRepeatManager.supportsRepeat(key.type)) keyRepeatManager.start()
        if (key.type == KeyType.LETTER && swipeTypingEnabled) {
            postDelayed({
                if (pressedKey == key && !longPressTriggered && !isSwipeGesture) {
                    longPressTriggered = true; popupPreviewManager.show(key, displayDensity)
                }
            }, PopupPreviewManager.LONG_PRESS_DELAY)
        }
        if (key.type == KeyType.SPACE && spacebarCursorEnabled) { isSpacebarGesture = true; spacebarCursorManager.startTracking(x) }
    }

    private fun handleTouchMove(event: MotionEvent) {
        val x = event.x; val y = event.y
        if (isSpacebarGesture && spacebarCursorManager.isTracking) {
            if (Math.abs(y - touchStartY) > SpacebarCursorManager.MAX_VERTICAL_DRIFT * displayDensity) {
                spacebarCursorManager.cancelTracking(); isSpacebarGesture = false
            } else { spacebarCursorManager.update(x, displayDensity); return }
        }
        if (!isSpacebarGesture && !longPressTriggered) {
            val dx = x - touchStartX; val dy = y - touchStartY
            val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (!isSwipeGesture && distance > swipeThreshold) {
                isSwipeGesture = true; popupPreviewManager.forceDismiss()
            }
            if (isSwipeGesture) {
                swipeDetector?.onTouchMove(x, y); swipeTrail.addPoint(x, y); invalidate()
                val closestKey = findKeyAt(x, y)
                if (closestKey != pressedKey) { pressedKey = closestKey; invalidate() }
                return
            }
        }
        if (popupState.visible) { popupPreviewManager.update(y, displayDensity); return }
        val key = findKeyAt(x, y)
        if (key != pressedKey) {
            val prevType = pressedKey?.type
            pressedKey = key
            if (key == null || key.type != prevType) { keyRepeatManager.stop(); popupPreviewManager.forceDismiss() }
            invalidate()
        }
    }

    private fun handleTouchUp(event: MotionEvent) {
        if (isSwipeGesture) {
            val results = swipeDetector?.onTouchUp(dictionary = SwipeDictionary.getWords()) ?: emptyList()
            swipeTrail.end()
            if (results.isNotEmpty()) onSwipeListener?.onSwipeCompleted(results[0])
            isSwipeGesture = false; pressedKey = null; invalidate(); return
        }
        if (isSpacebarGesture) { spacebarCursorManager.endTracking(); isSpacebarGesture = false }
        if (popupState.visible) popupPreviewManager.dismiss()
        keyRepeatManager.stop()
        pressedKey?.let { key -> announceKey(key); onKeyPressedListener?.onKeyPressed(key) }
        pressedKey = null; longPressTriggered = false; invalidate()
    }

    private fun handleTouchCancel() {
        keyRepeatManager.stop(); popupPreviewManager.forceDismiss()
        spacebarCursorManager.cancelTracking(); swipeDetector?.cancel(); swipeTrail.cancel()
        isSpacebarGesture = false; isSwipeGesture = false; pressedKey = null; longPressTriggered = false; invalidate()
    }

    private fun findKeyAt(x: Float, y: Float): KeyData? = keys.find { it.hitRect.contains(x, y) }
    private fun onRepeatTick() { pressedKey?.let { key -> onKeyPressedListener?.onKeyPressed(key) } }

    // ── Public API ────────────────────────────────────────────────────────

    fun setLayout(type: KeyboardLayoutType) { if (type != currentLayoutType) { currentLayoutType = type; requestLayout(); invalidate() } }
    fun setShiftState(state: ShiftState) { if (state != shiftState) { shiftState = state; invalidate() } }
    fun getCurrentLayoutType(): KeyboardLayoutType = currentLayoutType
    fun updateRepeatTiming(delayMs: Long, rateMs: Long) { keyRepeatManager.updateTiming(delayMs, rateMs) }
    fun setCornerRadius(radiusDp: Int) { cornerRadius = radiusDp * displayDensity; invalidate() }
    fun setKeySpacing(spacingDp: Int) {
        LayoutCalculator.keyGapOverrideDp = spacingDp.toFloat()
        requestLayout(); invalidate()
    }
    fun setKeyboardHeight(heightPx: Int) {
        LayoutCalculator.keyHeightOverrideDp = heightPx / displayDensity
        requestLayout(); invalidate()
    }

    /**
     * One-handed mode: keyboard shrinks to [fraction] of screen width,
     * shifted toward the given side ("off", "left", "right").
     */
    fun setOneHandedMode(mode: String) {
        when (mode.lowercase()) {
            "left" -> {
                LayoutCalculator.keyboardWidthFractionOverride = ONE_HANDED_WIDTH_FRACTION
                LayoutCalculator.keyboardSideOffsetFraction = 0f
            }
            "right" -> {
                LayoutCalculator.keyboardWidthFractionOverride = ONE_HANDED_WIDTH_FRACTION
                LayoutCalculator.keyboardSideOffsetFraction = 1f
            }
            else -> {
                LayoutCalculator.keyboardWidthFractionOverride = null
                LayoutCalculator.keyboardSideOffsetFraction = 0f
            }
        }
        requestLayout(); invalidate()
    }

    private companion object {
        const val ONE_HANDED_WIDTH_FRACTION = 0.8f
    }

    /** Feature gates wired from settings */
    var swipeTypingEnabled = true
    var spacebarCursorEnabled = true
    var longPressRepeatEnabled = true

    fun destroy() { keyRepeatManager.destroy() }

    interface OnKeyPressedListener { fun onKeyPressed(key: KeyData) }
    interface OnCursorMoveListener { fun onCursorMove(deltaChars: Int) }
    interface OnSwipeListener { fun onSwipeCompleted(word: String) }

    var onKeyPressedListener: OnKeyPressedListener? = null
    var onCursorMoveListener: OnCursorMoveListener? = null
    var onSwipeListener: OnSwipeListener? = null
}
