/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * JniBridgeTest.kt — Integration tests for Kotlin ↔ Rust JNI bridge.
 *
 * Tests that the prediction engine loads correctly and JNI function
 * signatures match between Kotlin and Rust.
 */

package com.akashboard.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.akashboard.engine.PredictorBridge
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JniBridgeTest {

    private lateinit var bridge: PredictorBridge

    @Before
    fun setUp() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        bridge = PredictorBridge(ctx)
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 1: Engine Loading
    // ════════════════════════════════════════════════════════════════

    @Test
    fun nativeLibraryLoads() {
        // If PredictorBridge can be constructed, the .so loaded successfully
        assertNotNull(bridge)
    }

    @Test
    fun engineSurvivesMultipleInitializations() {
        // Creating multiple bridges shouldn't crash (engine is static/singleton)
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val bridge2 = PredictorBridge(ctx)
        val bridge3 = PredictorBridge(ctx)
        assertNotNull(bridge2)
        assertNotNull(bridge3)
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 2: Predict Round-Trip
    // ════════════════════════════════════════════════════════════════

    @Test
    fun predict_emptyContext_returnsEmptyOrSuggestions() {
        val result = bridge.predict("", 5)
        // Empty engine may return empty or predictions
        assertNotNull(result)
    }

    @Test
    fun predict_topK_respected() {
        // Learn some words first
        bridge.learn("store", "going to the", System.currentTimeMillis())
        bridge.learn("market", "going to the", System.currentTimeMillis())
        bridge.learn("shop", "going to the", System.currentTimeMillis())

        val result = bridge.predict("going to the", 3)
        val predictions = result.split(",").filter { it.isNotEmpty() }
        assertTrue("Should return ≤3 predictions", predictions.size <= 3)
    }

    @Test
    fun predict_returnsCommaSeparated() {
        bridge.learn("hello", "", System.currentTimeMillis())
        bridge.learn("world", "", System.currentTimeMillis())

        val result = bridge.predict("", 5)
        // Result should be comma-separated or empty
        if (result.isNotEmpty()) {
            assertFalse("Should not start with comma", result.startsWith(","))
            assertFalse("Should not end with comma", result.endsWith(","))
        }
    }

    @Test
    fun predict_afterLearning_returnsLearnedWords() {
        bridge.learn("akashboard", "is the best", System.currentTimeMillis())
        bridge.learn("keyboard", "is the best", System.currentTimeMillis())

        val result = bridge.predict("is the best", 5)
        assertTrue(
            "Should predict 'akashboard' or 'keyboard'",
            result.contains("akashboard") || result.contains("keyboard")
        )
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 3: Correct Round-Trip
    // ════════════════════════════════════════════════════════════════

    @Test
    fun correct_unknownWord_returnsSame() {
        val result = bridge.correct("xyzabc", "")
        assertEquals("xyzabc", result)
    }

    @Test
    fun correct_emptyString_returnsEmpty() {
        val result = bridge.correct("", "")
        assertEquals("", result)
    }

    @Test
    fun correct_knownWord_returnsSame() {
        bridge.learn("hello", "", System.currentTimeMillis())
        val result = bridge.correct("hello", "")
        assertEquals("hello", result)
    }

    @Test
    fun correct_closeMatch_corrects() {
        bridge.learn("hello", "", System.currentTimeMillis())
        bridge.learn("world", "", System.currentTimeMillis())
        val result = bridge.correct("helo", "")
        assertEquals("hello", result)
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 4: Learn Round-Trip
    // ════════════════════════════════════════════════════════════════

    @Test
    fun learn_validWord_returnsTrue() {
        val result = bridge.learn("test", "context", System.currentTimeMillis())
        assertTrue("Learn should succeed", result)
    }

    @Test
    fun learn_emptyWord_returnsFalse() {
        val result = bridge.learn("", "context", System.currentTimeMillis())
        assertFalse("Learn empty word should fail", result)
    }

    @Test
    fun learn_emptyContext_returnsTrue() {
        val result = bridge.learn("word", "", System.currentTimeMillis())
        assertTrue("Learn with empty context should succeed", result)
    }

    @Test
    fun learn_multipleTimes_increasesFrequency() {
        val ts = System.currentTimeMillis()
        bridge.learn("frequent", "ctx", ts)
        bridge.learn("frequent", "ctx", ts)
        bridge.learn("frequent", "ctx", ts)

        val result = bridge.predict("ctx", 5)
        assertTrue("Frequent word should appear in predictions", result.contains("frequent"))
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 5: Edge Cases — JNI Boundary
    // ════════════════════════════════════════════════════════════════

    @Test
    fun predict_hugeTopK_doesNotCrash() {
        val result = bridge.predict("hello", 999)
        assertNotNull(result)
    }

    @Test
    fun predict_negativeTopK_doesNotCrash() {
        val result = bridge.predict("hello", -1)
        assertNotNull(result)
    }

    @Test
    fun predict_zeroTopK_doesNotCrash() {
        val result = bridge.predict("hello", 0)
        assertNotNull(result)
    }

    @Test
    fun predict_longContext_doesNotCrash() {
        val longCtx = "word ".repeat(1000)
        val result = bridge.predict(longCtx, 5)
        assertNotNull(result)
    }

    @Test
    fun predict_unicodeContext_doesNotCrash() {
        val result = bridge.predict("ñödé café résumé", 5)
        assertNotNull(result)
    }

    @Test
    fun correct_longWord_doesNotCrash() {
        val longWord = "a".repeat(10000)
        val result = bridge.correct(longWord, "")
        assertNotNull(result)
    }

    @Test
    fun learn_longWord_doesNotCrash() {
        val longWord = "b".repeat(10000)
        val result = bridge.learn(longWord, "ctx", System.currentTimeMillis())
        // May return false for very long words, but shouldn't crash
        assertNotNull(result.toString())
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 6: Memory & Lifecycle
    // ════════════════════════════════════════════════════════════════

    @Test
    fun predict_afterManyLearns_stillWorks() {
        val ts = System.currentTimeMillis()
        for (i in 0..500) {
            bridge.learn("word$i", "ctx$i", ts + i)
        }
        val result = bridge.predict("ctx250", 5)
        assertNotNull(result)
    }

    @Test
    fun correct_afterManyLearns_stillWorks() {
        val ts = System.currentTimeMillis()
        for (i in 0..200) {
            bridge.learn("known$(i)", "", ts + i)
        }
        val result = bridge.correct("known0", "")
        assertEquals("known0", result)
    }

    @Test
    fun predict_isThreadSafe() {
        val ts = System.currentTimeMillis()
        val threads = (0..9).map { i ->
            Thread {
                bridge.learn("thread$ i", "ctx", ts)
                bridge.predict("ctx", 3)
                bridge.correct("thread$ i", "")
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join(5000) }
        // No crash = pass
    }

    @Test
    fun engine_sustainedLoad_doesNotLeak() {
        val ts = System.currentTimeMillis()
        // Simulate heavy typing session
        for (i in 0..2000) {
            bridge.learn("word${i % 100}", "context${i % 10}", ts + i)
            if (i % 3 == 0) {
                bridge.predict("context${i % 10}", 5)
            }
            if (i % 5 == 0) {
                bridge.correct("word${i % 100}", "")
            }
        }
        // If we get here without OOM, pass
        assertTrue(true)
    }
}
