/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * DataManagerTest.kt — Unit tests for DataManager.
 */

package com.akashboard.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.akashboard.settings.KeyboardSettingsProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class DataManagerTest {

    private lateinit var context: Context
    private lateinit var dataManager: DataManager
    private lateinit var settings: KeyboardSettingsProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        settings = KeyboardSettingsProvider(context)
        settings.resetToDefaults()
        dataManager = DataManager(context)
    }

    @Test
    fun testExportData() {
        settings.themeId = "neon"
        val export = dataManager.exportData(includeStats = true)
        assertEquals(1, export.version)
        assertEquals("AkashBoard", export.appName)
        assertEquals("neon", export.activeTheme)
        assertNotNull(export.stats)
    }

    @Test
    fun testExportAndImportJson() {
        settings.themeId = "cyberpunk"
        settings.keyboardHeight = 330
        val jsonString = dataManager.exportToJson(includeStats = false)
        assertTrue(jsonString.isNotBlank())

        settings.resetToDefaults()
        assertEquals("akash_dark", settings.themeId)
        assertEquals(280, settings.keyboardHeight)

        val result = dataManager.importFromJson(jsonString)
        assertTrue(result is DataManager.ImportResult.Success)

        val freshSettings = KeyboardSettingsProvider(context)
        assertEquals("cyberpunk", freshSettings.themeId)
        assertEquals(330, freshSettings.keyboardHeight)
    }

    @Test
    fun testExportAndImportFile() {
        val tempFile = File(context.cacheDir, "test_export.json")
        try {
            settings.themeId = "matrix"
            val exportSuccess = dataManager.exportToFile(tempFile, includeStats = false)
            assertTrue(exportSuccess)
            assertTrue(tempFile.exists())

            settings.resetToDefaults()
            val importResult = dataManager.importFromFile(tempFile)
            assertTrue(importResult is DataManager.ImportResult.Success)

            val fresh = KeyboardSettingsProvider(context)
            assertEquals("matrix", fresh.themeId)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testImportInvalidJson() {
        val badJson = "{ invalid json"
        val result = dataManager.importFromJson(badJson)
        assertTrue(result is DataManager.ImportResult.ParseError)
    }

    @Test
    fun testDataSummary() {
        val summary = dataManager.getDataSummary()
        assertNotNull(summary)
        assertTrue(summary.settingsCount >= 0)
        assertTrue(summary.estimatedSizeKB >= 0)
    }
}
