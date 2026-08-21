/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * EmojiPanel.kt — Emoji grid with categories.
 *
 * Shows emojis in a scrollable grid with category tabs.
 * Features:
 *   - Category tabs (Smileys, Animals, Food, etc.)
 *   - Recently used section
 *   - Grid layout (8 columns)
 *   - Tap to insert
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
 * Emoji grid panel.
 */
class EmojiPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    // ── Emoji data ────────────────────────────────────────────────────────

    private val categories = listOf(
        "😊" to smileys,
        "🐱" to animals,
        "🍕" to food,
        "⚽" to activities,
        "🚗" to travel,
        "💡" to objects,
        "❤️" to symbols,
        "🏁" to flags
    )

    private var selectedCategoryIndex = 0
    private var currentEmojis = smileys

    // ── Layout ────────────────────────────────────────────────────────────

    private val columns = 8
    private val tabHeight = 40f * density
    private val emojiSize = 32f * density
    private val emojiPadding = 4f * density

    private var tabRects = mutableListOf<RectF>()
    private var emojiRects = mutableListOf<RectF>()
    private var panelHeight = 0f

    // ── Paint ─────────────────────────────────────────────────────────────

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF1A1C20.toInt()
    }

    private val tabPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF2B2E34.toInt()
    }

    private val tabSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF6C63FF.toInt()
    }

    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f * density
        textAlign = Paint.Align.CENTER
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x1AFFFFFF
        strokeWidth = 1f * density
    }

    // ── Initialization ────────────────────────────────────────────────────

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    // ── Layout ────────────────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val rows = (currentEmojis.size + columns - 1) / columns
        val gridHeight = rows * (emojiSize + emojiPadding) + emojiPadding
        val desiredHeight = (tabHeight + gridHeight).toInt()
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightConstraint = MeasureSpec.getSize(heightMeasureSpec)

        val finalHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightConstraint
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightConstraint)
            else -> desiredHeight
        }
        panelHeight = finalHeight.toFloat()
        setMeasuredDimension(width, finalHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) calculateLayout(w.toFloat())
    }

    private fun calculateLayout(width: Float) {
        tabRects.clear()
        val tabWidth = width / categories.size

        for (i in categories.indices) {
            tabRects.add(RectF(i * tabWidth, 0f, (i + 1) * tabWidth, tabHeight))
        }

        emojiRects.clear()
        val emojiWidth = width / columns
        val startY = tabHeight

        for (i in currentEmojis.indices) {
            val row = i / columns
            val col = i % columns
            emojiRects.add(RectF(
                col * emojiWidth + emojiPadding,
                startY + row * (emojiSize + emojiPadding),
                (col + 1) * emojiWidth - emojiPadding,
                startY + row * (emojiSize + emojiPadding) + emojiSize
            ))
        }
    }

    // ── Drawing ───────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Background
        canvas.drawRect(0f, 0f, width.toFloat(), panelHeight, bgPaint)

        // Category tabs
        for (i in categories.indices) {
            val rect = tabRects.getOrNull(i) ?: continue
            val paint = if (i == selectedCategoryIndex) tabSelectedPaint else tabPaint
            canvas.drawRect(rect, paint)

            // Tab emoji
            emojiPaint.textSize = 20f * density
            canvas.drawText(categories[i].first, rect.centerX(), rect.centerY() + emojiPaint.textSize / 3, emojiPaint)
        }

        // Divider
        canvas.drawLine(0f, tabHeight, width.toFloat(), tabHeight, dividerPaint)

        // Emojis
        emojiPaint.textSize = emojiSize
        for (i in currentEmojis.indices) {
            val rect = emojiRects.getOrNull(i) ?: continue
            canvas.drawText(currentEmojis[i], rect.centerX(), rect.centerY() + emojiSize / 3, emojiPaint)
        }
    }

    // ── Touch ─────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y

            // Check tabs
            for (i in categories.indices) {
                val rect = tabRects.getOrNull(i) ?: continue
                if (rect.contains(x, y)) {
                    selectedCategoryIndex = i
                    currentEmojis = categories[i].second
                    requestLayout()
                    invalidate()
                    return true
                }
            }

            // Check emojis
            for (i in currentEmojis.indices) {
                val rect = emojiRects.getOrNull(i) ?: continue
                if (rect.contains(x, y)) {
                    onEmojiClickListener?.onEmojiClicked(currentEmojis[i])
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // ── Listener ──────────────────────────────────────────────────────────

    interface OnEmojiClickListener {
        fun onEmojiClicked(emoji: String)
    }

    var onEmojiClickListener: OnEmojiClickListener? = null

    companion object {
        val smileys = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
            "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩",
            "😘", "😗", "😚", "😙", "🥲", "😋", "😛", "😜",
            "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔", "🫡",
            "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
            "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕",
            "🤢", "🤮", "🥵", "🥶", "🥴", "😵", "🤯", "🤠",
            "🥳", "🥸", "😎", "🤓", "🧐", "😕", "😟", "🙁"
        )

        val animals = listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
            "🐻‍❄️", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵",
            "🙈", "🙉", "🙊", "🐒", "🐔", "🐧", "🐦", "🐤",
            "🐣", "🐥", "🦆", "🦅", "🦉", "🦇", "🐺", "🐗"
        )

        val food = listOf(
            "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓",
            "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝",
            "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌶️", "🫑",
            "🌽", "🥕", "🫒", "🧄", "🧅", "🥔", "🍠", "🍞"
        )

        val activities = listOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉",
            "🥏", "🎱", "🪀", "🏓", "🏸", "🏒", "🥅", "⛳",
            "🪁", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹",
            "🛼", "🛷", "⛸️", "🥌", "🎿", "🎯", "🪃", "🎮"
        )

        val travel = listOf(
            "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑",
            "🚒", "🚐", "🛻", "🚚", "🚛", "🚜", "🛵", "🏍️",
            "🛺", "🚲", "🛴", "🛹", "🛼", "🚁", "✈️", "🛩️",
            "🚀", "🛸", "🚢", "⛵", "🛶", "🚤", "🛥️", "🛳️"
        )

        val objects = listOf(
            "💡", "🔦", "🕯️", "📱", "💻", "⌨️", "🖥️", "🖨️",
            "🖱️", "💾", "💿", "📷", "📹", "🎥", "📺", "📻",
            "🎙️", "🎚️", "🎛️", "🧭", "⏱️", "⏰", "📡", "🔋",
            "💰", "💳", "📦", "📫", "📬", "📪", "📭", "📮"
        )

        val symbols = listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
            "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖",
            "💘", "💝", "⭐", "🌟", "💫", "✨", "🔥", "💯",
            "✅", "❌", "❓", "❗", "‼️", "⁉️", "⭕", "🔴"
        )

        val flags = listOf(
            "🏁", "🚩", "🎌", "🏴", "🏳️", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️",
            "🇺🇸", "🇬🇧", "🇫🇷", "🇩🇪", "🇮🇳", "🇯🇵", "🇰🇷", "🇧🇷",
            "🇨🇦", "🇦🇺", "🇮🇹", "🇪🇸", "🇲🇽", "🇷🇺", "🇨🇳", "🇿🇦",
            "🇸🇪", "🇳🇴", "🇫🇮", "🇩🇰", "🇳🇱", "🇧🇪", "🇨🇭", "🇦🇹"
        )
    }
}
