/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * PredictorBridge.kt — JNI bridge to the Rust prediction engine.
 *
 * Provides a clean Kotlin API for calling Rust functions.
 * All JNI complexity is hidden behind simple Kotlin methods.
 */

package com.akashboard.engine

/**
 * JNI bridge to the Rust prediction engine.
 *
 * Usage:
 *   PredictorBridge.init(filesDir)
 *   val suggestions = PredictorBridge.predict("hello world", 3)
 *   PredictorBridge.learn("store", "going to the", System.currentTimeMillis())
 */
object PredictorBridge {

    private var isLoaded = false
    private var isInitialized = false

    init {
        try {
            System.loadLibrary("predictor")
            isLoaded = true
        } catch (e: UnsatisfiedLinkError) {
            isLoaded = false
        }
    }

    /** Initialize the engine */
    fun init(configPath: String) {
        if (!isLoaded || isInitialized) return
        nativeInit(configPath)
        isInitialized = true
    }

    /** Destroy the engine */
    fun destroy() {
        if (!isLoaded || !isInitialized) return
        nativeDestroy()
        isInitialized = false
    }

    /** Predict next words */
    fun predict(context: String, topK: Int = 3): List<String> {
        if (!isLoaded || !isInitialized) return emptyList()
        if (context.isBlank()) return emptyList()

        val k = topK.coerceIn(1, 5)
        val result = nativePredict(context, k)
        return if (result.isNullOrBlank()) emptyList()
        else result.split(",").filter { it.isNotBlank() }
    }

    /** Auto-correct a word */
    fun correct(word: String, context: String = ""): String {
        if (!isLoaded || !isInitialized) return word
        if (word.isBlank()) return word
        return nativeCorrect(word, context) ?: word
    }

    /** Learn a new word/pattern */
    fun learn(word: String, context: String, timestamp: Long): Boolean {
        if (!isLoaded || !isInitialized) return false
        if (word.isBlank()) return false
        return nativeLearn(word, context, timestamp)
    }

    /** Detect sentiment of text */
    fun detectMood(text: String): Float {
        if (!isLoaded || !isInitialized) return 0f
        return nativeDetectMood(text)
    }

    /** Save model to disk */
    fun saveModel(): Boolean {
        if (!isLoaded || !isInitialized) return false
        return nativeSaveModel()
    }

    /** Load model from disk */
    fun loadModel(): Boolean {
        if (!isLoaded || !isInitialized) return false
        return nativeLoadModel()
    }

    /** Get storage size in bytes */
    fun getStorageSize(): Long {
        if (!isLoaded || !isInitialized) return 0
        return nativeGetStorageSize()
    }

    /** Prune old patterns */
    fun prune(maxAgeDays: Int = 30) {
        if (!isLoaded || !isInitialized) return
        nativePrune(maxAgeDays)
    }

    /** Clear all learned data */
    fun clearAll() {
        if (!isLoaded || !isInitialized) return
        nativeClearAll()
    }

    /** Whether engine is available */
    fun isAvailable(): Boolean = isLoaded && isInitialized

    // ── Native Methods ────────────────────────────────────────────────────

    private external fun nativeInit(configPath: String)
    private external fun nativeDestroy()
    private external fun nativePredict(context: String, topK: Int): String?
    private external fun nativeCorrect(word: String, context: String): String?
    private external fun nativeLearn(word: String, context: String, timestamp: Long): Boolean
    private external fun nativeDetectMood(text: String): Float
    private external fun nativeSaveModel(): Boolean
    private external fun nativeLoadModel(): Boolean
    private external fun nativeGetStorageSize(): Long
    private external fun nativePrune(maxAgeDays: Int)
    private external fun nativeClearAll()
}
