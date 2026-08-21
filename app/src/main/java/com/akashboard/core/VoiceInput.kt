/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * VoiceInput.kt — Speech recognition wrapper.
 *
 * Uses Android's built-in SpeechRecognizer for voice-to-text.
 * Works offline on most devices.
 */

package com.akashboard.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Voice input manager.
 *
 * Wraps Android's SpeechRecognizer for keyboard voice input.
 */
class VoiceInput(context: Context) {

    private val speechRecognizer: SpeechRecognizer? = if (SpeechRecognizer.isRecognitionAvailable(context)) {
        SpeechRecognizer.createSpeechRecognizer(context)
    } else null

    private var listener: OnVoiceInputListener? = null
    private var isListening = false

    /** Voice state */
    enum class VoiceState {
        IDLE,
        LISTENING,
        PROCESSING,
        ERROR,
        UNAVAILABLE
    }

    private var currentState = if (speechRecognizer != null) VoiceState.IDLE else VoiceState.UNAVAILABLE

    init {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                currentState = VoiceState.LISTENING
                listener?.onStateChanged(VoiceState.LISTENING)
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                currentState = VoiceState.PROCESSING
                listener?.onStateChanged(VoiceState.PROCESSING)
            }

            override fun onError(error: Int) {
                currentState = VoiceState.ERROR
                isListening = false
                listener?.onError(getErrorMessage(error))
                listener?.onStateChanged(VoiceState.ERROR)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                isListening = false
                currentState = VoiceState.IDLE
                listener?.onResult(text)
                listener?.onStateChanged(VoiceState.IDLE)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                listener?.onPartialResult(text)
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // ── Public API ────────────────────────────────────────────────────────

    fun startListening() {
        if (speechRecognizer == null || isListening) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        isListening = true
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        if (!isListening) return
        speechRecognizer?.stopListening()
        isListening = false
        currentState = VoiceState.IDLE
    }

    fun isAvailable(): Boolean = speechRecognizer != null

    fun getState(): VoiceState = currentState

    fun setListener(listener: OnVoiceInputListener) {
        this.listener = listener
    }

    fun destroy() {
        speechRecognizer?.destroy()
        listener = null
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }

    // ── Listener ──────────────────────────────────────────────────────────

    interface OnVoiceInputListener {
        fun onPartialResult(text: String)
        fun onResult(text: String)
        fun onError(error: String)
        fun onStateChanged(state: VoiceState)
    }
}
