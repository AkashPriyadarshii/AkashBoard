/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * ClipboardPanel.kt — Clipboard history UI.
 *
 * Shows recent clippings in a scrollable list.
 */

package com.akashboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.akashboard.data.ClipboardItem

class ClipboardPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    private var items = listOf<ClipboardItem>()
    private var itemRects = mutableListOf<RectF>()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF1A1C20.toInt()
    }

    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF2F3F5.toInt()
        textSize = 16f * density
        isFakeBoldText = true
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF60656D.toInt()
        textSize = 14f * density
        textAlign = Paint.Align.CENTER
    }

    private val itemTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB8B8CC.toInt()
        textSize = 14f * density
    }

    private val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF6B60.toInt()
        textSize = 12f * density
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x1AFFFFFF
        strokeWidth = 1f * density
    }

    private val itemHeight = 56f * density
    private val headerHeight = 48f * density

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val desiredHeight = (headerHeight + items.size * itemHeight).toInt()
        val maxHeight = (400 * density).toInt()
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightConstraint = MeasureSpec.getSize(heightMeasureSpec)

        val finalHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightConstraint
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightConstraint)
            else -> desiredHeight.coerceAtMost(maxHeight)
        }

        setMeasuredDimension(width, finalHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) rebuildItemRects(w.toFloat())
    }

    private fun rebuildItemRects(width: Float) {
        itemRects.clear()
        for (i in items.indices) {
            val y = headerHeight + i * itemHeight
            itemRects.add(RectF(0f, y, width, y + itemHeight))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        if (items.isEmpty()) {
            canvas.drawText(
                "No clipboard items yet",
                width / 2f,
                headerHeight + 40f * density,
                emptyPaint
            )
            return
        }

        // Header
        canvas.drawText("📋 Clipboard", 16f * density, headerHeight - 16f * density, headerPaint)

        // Items
        for (i in items.indices) {
            val item = items[i]
            val y = headerHeight + i * itemHeight

            if (i > 0) {
                canvas.drawLine(16f * density, y, width - 16f * density, y, dividerPaint)
            }

            if (item.isPinned) {
                canvas.drawText("📌", 16f * density, y + 32f * density, pinPaint)
            }

            val maxChars = 50
            val displayText = if (item.text.length > maxChars) {
                item.text.substring(0, maxChars) + "..."
            } else {
                item.text
            }
            canvas.drawText(displayText, 40f * density, y + 32f * density, itemTextPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val index = ((event.y - headerHeight) / itemHeight).toInt()
            if (index in items.indices) {
                onItemClickListener?.onItemClicked(items[index])
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setItems(newItems: List<ClipboardItem>) {
        items = newItems
        rebuildItemRects(width.toFloat())
        requestLayout()
        invalidate()
    }

    interface OnItemClickListener {
        fun onItemClicked(item: ClipboardItem)
    }

    var onItemClickListener: OnItemClickListener? = null
}
