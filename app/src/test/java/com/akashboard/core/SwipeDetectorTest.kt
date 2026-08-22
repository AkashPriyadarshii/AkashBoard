/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SwipeDetectorTest.kt — Unit tests for swipe gesture recognition.
 */

package com.akashboard.core

import android.graphics.PointF
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SwipeDetectorTest {

    private val keyPositions = mapOf(
        'h' to PointF(100f, 200f),
        'e' to PointF(80f, 150f),
        'l' to PointF(60f, 250f),
        'o' to PointF(90f, 130f),
        'w' to PointF(200f, 180f),
        'r' to PointF(180f, 200f),
        'd' to PointF(160f, 220f),
        'a' to PointF(120f, 200f),
        't' to PointF(140f, 180f),
        'n' to PointF(170f, 210f),
        's' to PointF(110f, 200f)
    )

    private lateinit var detector: SwipeDetector

    @Before
    fun setup() {
        detector = SwipeDetector(keyPositions)
    }

    // ── Basic Gesture ─────────────────────────────────────────────────────

    @Test
    fun `onTouchDown starts tracking`() {
        detector.onTouchDown(100f, 200f)
        assertTrue(detector.isTracking)
    }

    @Test
    fun `onTouchUp stops tracking`() {
        detector.onTouchDown(100f, 200f)
        detector.onTouchMove(200f, 300f)
        detector.onTouchUp()
        assertFalse(detector.isTracking)
    }

    @Test
    fun `cancel stops tracking and clears points`() {
        detector.onTouchDown(100f, 200f)
        detector.onTouchMove(200f, 300f)
        detector.cancel()
        assertFalse(detector.isTracking)
        assertEquals(0, detector.getPoints().size)
    }

    @Test
    fun `onTouchUp with too few points returns empty`() {
        detector.onTouchDown(100f, 200f)
        val results = detector.onTouchUp()
        assertTrue(results.isEmpty())
    }

    // ── Key Sequence Matching ─────────────────────────────────────────────

    @Test
    fun `simple swipe h-e-l-l-o matches hello in dictionary`() {
        detector.onTouchDown(100f, 200f)  // h
        detector.onTouchMove(80f, 150f)   // e
        detector.onTouchMove(60f, 250f)   // l
        detector.onTouchMove(60f, 260f)   // l (close, might not add)
        detector.onTouchMove(90f, 130f)   // o

        val results = detector.onTouchUp(dictionary = listOf("hello", "world", "help", "he"))
        assertTrue(results.contains("hello"))
    }

    @Test
    fun `swipe returns closest match when no exact match`() {
        // Swipe h-e-l-l-o (with duplicate l point near same position)
        detector.onTouchDown(100f, 200f)  // h
        detector.onTouchMove(80f, 150f)   // e
        detector.onTouchMove(60f, 250f)   // l
        detector.onTouchMove(65f, 245f)   // l (close to previous l, may be filtered)
        detector.onTouchMove(90f, 130f)   // o

        val results = detector.onTouchUp(dictionary = listOf("hello", "world", "heel", "helo"))
        // The key sequence is h-e-l-o (4 chars), so "helo" should match as a subsequence
        assertTrue(results.isNotEmpty())
    }

    // ── Noise Filtering ───────────────────────────────────────────────────

    @Test
    fun `points too close together are filtered`() {
        detector.onTouchDown(100f, 200f)
        detector.onTouchMove(101f, 200f)  // 1px away, filtered
        detector.onTouchMove(102f, 200f)  // 2px away, filtered

        val points = detector.getPoints()
        assertEquals(1, points.size) // Only initial point
    }

    @Test
    fun `points far enough apart are kept`() {
        detector.onTouchDown(100f, 200f)
        detector.onTouchMove(120f, 200f)  // 20px away
        detector.onTouchMove(140f, 200f)  // 20px away

        val points = detector.getPoints()
        assertEquals(3, points.size)
    }

    // ── No Dictionary ─────────────────────────────────────────────────────

    @Test
    fun `without dictionary returns key sequence as word`() {
        detector.onTouchDown(100f, 200f)  // h
        detector.onTouchMove(80f, 150f)   // e
        detector.onTouchMove(60f, 250f)   // l

        val results = detector.onTouchUp()
        assertEquals(1, results.size)
        assertEquals("hel", results[0])
    }

    // ── Constants ─────────────────────────────────────────────────────────

    @Test
    fun `MIN_POINT_DISTANCE is 8`() {
        assertEquals(8f, SwipeDetector.MIN_POINT_DISTANCE)
    }

    @Test
    fun `points far from all keys are dropped`() {
        // Key centers ~100px apart; a point 500px away exceeds maxKeyDistance
        val detector = SwipeDetector(mapOf('a' to PointF(0f, 0f), 'b' to PointF(100f, 0f)))
        detector.onTouchDown(500f, 500f)
        detector.onTouchMove(510f, 500f)
        val results = detector.onTouchUp()
        assertTrue("Gesture nowhere near any key should produce no word", results.isEmpty())
    }

    // ── Edge Cases ────────────────────────────────────────────────────────

    @Test
    fun `onTouchMove when not tracking does nothing`() {
        detector.onTouchMove(100f, 200f)
        assertEquals(0, detector.getPoints().size)
    }

    @Test
    fun `getPoints returns copy not reference`() {
        detector.onTouchDown(100f, 200f)
        val points1 = detector.getPoints()
        detector.onTouchMove(200f, 300f)
        val points2 = detector.getPoints()
        assertEquals(1, points1.size) // Original not modified
        assertEquals(2, points2.size)
    }

    @Test
    fun `multiple gestures can be performed sequentially`() {
        // First gesture near key positions
        detector.onTouchDown(100f, 200f)
        detector.onTouchMove(80f, 150f)
        detector.onTouchUp()

        // Second gesture near key positions
        detector.onTouchDown(100f, 200f)
        detector.onTouchMove(80f, 150f)
        detector.onTouchMove(60f, 250f)
        val results = detector.onTouchUp(dictionary = listOf("hello", "help"))
        // At minimum the gesture was tracked and completed without error
        assertTrue(detector.getPoints().isEmpty()) // Points cleared after onTouchUp
    }
}
