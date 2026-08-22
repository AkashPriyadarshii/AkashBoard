/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * AkashBoardIME.kt - Main IME rewritten for Jetpack Compose.
 */

package com.akashboard

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.akashboard.analytics.TimeAwarePredictor
import com.akashboard.analytics.TypingDNA
import com.akashboard.analytics.TypingStats
import com.akashboard.core.InputHandler
import com.akashboard.core.HapticFeedback
import com.akashboard.core.VoiceInput
import com.akashboard.data.ClipboardDB
import com.akashboard.engine.PredictorBridge
import com.akashboard.theme.ThemeManager
import com.akashboard.ui.compose.ComposeImeRootView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AkashBoardIME : InputMethodService() {

    companion object {
        private const val TAG = "AkashBoardIME"
    }

    var voiceInput: VoiceInput? = null
    private var hapticFeedback: HapticFeedback? = null
    var inputHandler: InputHandler? = null
    private var themeManager: ThemeManager? = null
    private var clipboardDB: ClipboardDB? = null
    private var typingStats: TypingStats? = null
    private var typingDNA: TypingDNA? = null
    private var timeAwarePredictor: TimeAwarePredictor? = null
    private var settingsProvider: com.akashboard.settings.KeyboardSettingsProvider? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        try {
            PredictorBridge.init(filesDir.absolutePath)
        } catch (e: Exception) {
            Log.w(TAG, "Rust engine init failed, predictions disabled", e)
        }

        try {
            settingsProvider = com.akashboard.settings.KeyboardSettingsProvider(this)
            themeManager = ThemeManager(this)
            typingStats = TypingStats(this)
            typingDNA = TypingDNA(this)
            timeAwarePredictor = TimeAwarePredictor(this)
            clipboardDB = ClipboardDB.getDatabase(this)
            
            val sp = settingsProvider
            hapticFeedback = HapticFeedback(this)
            hapticFeedback?.setEnabled(sp?.vibrateOnKeypress ?: false)
            inputHandler = InputHandler(
                hapticFeedback!!,
                onLayoutChange = {},
                onSuggestionsUpdate = { suggestions -> com.akashboard.ui.compose.SuggestionsGlobalState.value = suggestions }
            )
            voiceInput = VoiceInput(this)
            voiceInput?.setListener(object : VoiceInput.OnVoiceInputListener {
                override fun onPartialResult(text: String) {}
                override fun onResult(text: String) {
                    currentInputConnection?.commitText("$text ", 1)
                }
                override fun onError(error: String) {
                    Log.e(TAG, "Voice error: $error")
                }
                override fun onStateChanged(state: VoiceInput.VoiceState) {}
            })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize non-UI components", e)
        }
    }

    override fun onCreateInputView(): View {
        Log.d(TAG, "onCreateInputView (Returning ComposeImeRootView)")
        return ComposeImeRootView(this, this)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        try {
            inputHandler?.setInputConnection(currentInputConnection, info)
            typingStats?.startSession()

            settingsProvider?.let { sp ->
                inputHandler?.setAutoCorrect(sp.autoCorrectEnabled)
                inputHandler?.setPredictiveText(sp.predictiveTextEnabled)
                inputHandler?.setIncognito(sp.incognitoMode)
                inputHandler?.learningEnabled = sp.learningEnabled
                hapticFeedback?.setEnabled(sp.vibrateOnKeypress)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onStartInputView failed", e)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        try {
            typingStats?.endSession()
            voiceInput?.stopListening()
            PredictorBridge.saveModel()
        } catch (e: Exception) {
            Log.w(TAG, "onFinishInputView cleanup failed", e)
        }
    }

    override fun onDestroy() {
        try {
            scope.cancel()
            voiceInput?.destroy()
            PredictorBridge.saveModel()
            PredictorBridge.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "onDestroy cleanup failed", e)
        }
        super.onDestroy()
    }
}
