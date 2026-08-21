/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * PopupPreviewManager.kt — Shows popup preview on long-press.
 *
 * When the user holds a key, a popup appears above showing:
 *   - The alternate character (e.g., "1" above "Q")
 *   - Or the key label enlarged
 *
 * The popup dismisses when the user:
 *   - Slides up to select the alternate
 *   - Lifts their finger (selects original key)
 */

package com.akashboard.core

/**
 * Popup preview state.
 *
 * Represents the current state of the long-press popup.
 */
data class PopupState(
    /** Whether the popup is visible */
    val visible: Boolean = false,

    /** The key being held */
    val key: KeyData? = null,

    /** The popup label (alternate character) */
    val label: String? = null,

    /** Popup position X (center of key) */
    val x: Float = 0f,

    /** Popup position Y (above the key) */
    val y: Float = 0f,

    /** Whether the user has slid up to select alternate */
    val alternateSelected: Boolean = false
)

/**
 * Manages popup preview for long-press keys.
 *
 * Shows a floating preview above the held key with the
 * alternate character. The user can slide up to select it.
 */
class PopupPreviewManager {

    /** Current popup state */
    var state: PopupState = PopupState()
        private set

    /** Callback when state changes */
    var onStateChanged: ((PopupState) -> Unit)? = null

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Show the popup for a key.
     *
     * @param key The key being held
     * @param density Device density for positioning
     */
    fun show(key: KeyData, density: Float) {
        val popupLabel = key.popupLabel ?: key.label

        state = PopupState(
            visible = true,
            key = key,
            label = popupLabel,
            x = key.rect.centerX(),
            y = key.rect.top - (POPUP_HEIGHT * density)
        )
        onStateChanged?.invoke(state)
    }

    /**
     * Update popup based on finger position.
     *
     * If the finger slides up past the threshold, select the alternate.
     *
     * @param touchY Current touch Y position
     * @param density Device density
     */
    fun update(touchY: Float, density: Float) {
        if (!state.visible || state.key == null) return

        val keyTop = state.key!!.rect.top
        val slideDistance = keyTop - touchY
        val threshold = SLIDE_THRESHOLD * density

        val alternateSelected = slideDistance > threshold

        if (alternateSelected != state.alternateSelected) {
            state = state.copy(alternateSelected = alternateSelected)
            onStateChanged?.invoke(state)
        }
    }

    /**
     * Dismiss the popup.
     *
     * @return The selected label (alternate if slid up, original otherwise)
     */
    fun dismiss(): String? {
        val selectedLabel = if (state.alternateSelected) {
            state.key?.popupLabel ?: state.key?.label
        } else {
            state.key?.label
        }

        state = PopupState()
        onStateChanged?.invoke(state)

        return selectedLabel
    }

    /**
     * Force dismiss without returning a label.
     */
    fun forceDismiss() {
        state = PopupState()
        onStateChanged?.invoke(state)
    }

    // ── Constants ─────────────────────────────────────────────────────────

    companion object {
        /** Height of the popup preview (dp) */
        const val POPUP_HEIGHT = 48f

        /** Slide distance threshold to select alternate (dp) */
        const val SLIDE_THRESHOLD = 30f

        /** Long-press delay before popup appears (ms) */
        const val LONG_PRESS_DELAY = 400L
    }
}
