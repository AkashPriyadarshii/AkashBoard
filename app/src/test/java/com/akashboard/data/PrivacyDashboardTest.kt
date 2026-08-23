/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * PrivacyDashboardTest.kt — Unit tests for PrivacyDashboard.
 */

package com.akashboard.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.akashboard.analytics.TypingStats
import com.akashboard.settings.KeyboardSettingsProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrivacyDashboardTest {

    private lateinit var context: Context
    private lateinit var dashboard: PrivacyDashboard
    private lateinit var settings: KeyboardSettingsProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        settings = KeyboardSettingsProvider(context)
        settings.resetToDefaults()
        dashboard = PrivacyDashboard(context)
    }

    @Test
    fun testDataCategories() {
        val categories = dashboard.getDataCategories()
        assertTrue(categories.isNotEmpty())
        assertEquals(6, categories.size)

        val names = categories.map { it.name }
        assertTrue(names.contains("Typing Patterns"))
        assertTrue(names.contains("Personal Dictionary"))
        assertTrue(names.contains("Clipboard History"))
        assertTrue(names.contains("Theme Preferences"))
        assertTrue(names.contains("Typing Statistics"))
        assertTrue(names.contains("Typing DNA"))
    }

    @Test
    fun testNotCollectedList() {
        val notCollected = dashboard.getNotCollected()
        assertTrue(notCollected.isNotEmpty())
        assertTrue(notCollected.any { it.contains("No keystroke content") })
        assertTrue(notCollected.any { it.contains("No network requests") })
    }

    @Test
    fun testPrivacyControls() {
        val controls = dashboard.getPrivacyControls()
        assertEquals(3, controls.size)

        val incognito = controls.find { it.action == PrivacyDashboard.PrivacyAction.INCOGNITO }
        assertNotNull(incognito)
        assertFalse(incognito!!.isEnabled)
    }

    @Test
    fun testGenerateReport() {
        val report = dashboard.generateReport()
        assertNotNull(report)
        assertEquals(6, report.categories.size)
        assertEquals(10, report.notCollected.size)
        assertEquals(3, report.controls.size)
        assertFalse(report.isIncognito)
        assertTrue(report.totalSizeKB >= 0)
    }
}
