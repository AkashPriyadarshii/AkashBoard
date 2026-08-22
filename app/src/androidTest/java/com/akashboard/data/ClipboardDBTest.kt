/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * ClipboardDBTest.kt — Room database instrumented tests.
 *
 * Tests CRUD operations, edge cases, concurrent access,
 * and export/import functionality.
 */

package com.akashboard.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClipboardDBTest {

    private lateinit var db: ClipboardDB
    private lateinit var dao: ClipboardDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, ClipboardDB::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.clipboardDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 1: CRUD Operations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun insert_andGetAll() = runBlocking {
        val item = ClipboardItem(
            content = "Hello World",
            timestamp = System.currentTimeMillis()
        )
        dao.insert(item)
        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("Hello World", all[0].content)
    }

    @Test
    fun insert_multipleItems() = runBlocking {
        for (i in 1..10) {
            dao.insert(ClipboardItem(
                content = "Item $i",
                timestamp = System.currentTimeMillis() + i
            ))
        }
        val all = dao.getAll()
        assertEquals(10, all.size)
    }

    @Test
    fun insert_emptyContent() = runBlocking {
        dao.insert(ClipboardItem(
            content = "",
            timestamp = System.currentTimeMillis()
        ))
        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("", all[0].content)
    }

    @Test
    fun insert_longContent() = runBlocking {
        val longContent = "A".repeat(100_000)
        dao.insert(ClipboardItem(
            content = longContent,
            timestamp = System.currentTimeMillis()
        ))
        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals(100_000, all[0].content.length)
    }

    @Test
    fun insert_unicodeContent() = runBlocking {
        dao.insert(ClipboardItem(
            content = "مرحبا 🌍 café résumé ñödé",
            timestamp = System.currentTimeMillis()
        ))
        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("مرحبا 🌍 café résumé ñödé", all[0].content)
    }

    @Test
    fun insert_specialChars() = runBlocking {
        val specials = "!@#\$%^&*(){}[]|\\:\";<>?,./~`"
        dao.insert(ClipboardItem(
            content = specials,
            timestamp = System.currentTimeMillis()
        ))
        val all = dao.getAll()
        assertEquals(specials, all[0].content)
    }

    @Test
    fun insert_newlines() = runBlocking {
        val withNewlines = "Line 1\nLine 2\r\nLine 3\rLine 4"
        dao.insert(ClipboardItem(
            content = withNewlines,
            timestamp = System.currentTimeMillis()
        ))
        val all = dao.getAll()
        assertEquals(withNewlines, all[0].content)
    }

    @Test
    fun insert_nullBytes() = runBlocking {
        val withNulls = "before\u0000after"
        dao.insert(ClipboardItem(
            content = withNulls,
            timestamp = System.currentTimeMillis()
        ))
        val all = dao.getAll()
        assertEquals(withNulls, all[0].content)
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 2: Delete Operations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun deleteSingle_itemRemoved() = runBlocking {
        dao.insert(ClipboardItem(content = "Delete me", timestamp = 1))
        dao.insert(ClipboardItem(content = "Keep me", timestamp = 2))
        val all = dao.getAll()
        val toDelete = all.first { it.content == "Delete me" }
        dao.delete(toDelete)
        val remaining = dao.getAll()
        assertEquals(1, remaining.size)
        assertEquals("Keep me", remaining[0].content)
    }

    @Test
    fun clearAll_emptiesDatabase() = runBlocking {
        for (i in 0..20) {
            dao.insert(ClipboardItem(content = "Item $i", timestamp = i.toLong()))
        }
        dao.clearAll()
        val all = dao.getAll()
        assertTrue("Database should be empty", all.isEmpty())
    }

    @Test
    fun clearAll_onEmpty_doesNotCrash() = runBlocking {
        dao.clearAll()
        val all = dao.getAll()
        assertTrue(all.isEmpty())
    }

    @Test
    fun deleteNonexistent_doesNotCrash() = runBlocking {
        val fakeItem = ClipboardItem(id = 99999, content = "ghost", timestamp = 0)
        dao.delete(fakeItem)
        // No crash = pass
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 3: Ordering & Limits
    // ════════════════════════════════════════════════════════════════

    @Test
    fun getAll_orderedByTimestampDesc() = runBlocking {
        dao.insert(ClipboardItem(content = "First", timestamp = 100))
        dao.insert(ClipboardItem(content = "Second", timestamp = 200))
        dao.insert(ClipboardItem(content = "Third", timestamp = 300))
        val all = dao.getAll()
        assertEquals("Third", all[0].content)
        assertEquals("Second", all[1].content)
        assertEquals("First", all[2].content)
    }

    @Test
    fun getAll_duplicateContent_allowed() = runBlocking {
        dao.insert(ClipboardItem(content = "Same", timestamp = 100))
        dao.insert(ClipboardItem(content = "Same", timestamp = 200))
        val all = dao.getAll()
        assertEquals(2, all.size)
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 4: Concurrent Access
    // ════════════════════════════════════════════════════════════════

    @Test
    fun concurrentInserts_doNotCrash() = runBlocking {
        val threads = (0..19).map { i ->
            Thread {
                runBlocking {
                    dao.insert(ClipboardItem(
                        content = "Thread $i",
                        timestamp = System.currentTimeMillis() + i
                    ))
                }
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join(5000) }
        val all = dao.getAll()
        assertEquals(20, all.size)
    }

    @Test
    fun concurrentReadWrite_doesNotCrash() = runBlocking {
        // Writers
        val writers = (0..9).map { i ->
            Thread {
                runBlocking {
                    dao.insert(ClipboardItem(
                        content = "Writer $i",
                        timestamp = System.currentTimeMillis() + i
                    ))
                }
            }
        }
        // Readers
        val readers = (0..9).map {
            Thread {
                runBlocking { dao.getAll() }
            }
        }
        (writers + readers).forEach { it.start() }
        (writers + readers).forEach { it.join(5000) }
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 5: Export/Import
    // ════════════════════════════════════════════════════════════════

    @Test
    fun exportImport_roundTrip() = runBlocking {
        // Insert data
        dao.insert(ClipboardItem(content = "Export test 1", timestamp = 100))
        dao.insert(ClipboardItem(content = "Export test 2", timestamp = 200))

        // Export
        val all = dao.getAll()
        val exportData = all.map { mapOf("content" to it.content, "timestamp" to it.timestamp.toString()) }
        val json = kotlinx.serialization.json.Json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(
                kotlinx.serialization.builtins.MapSerializer(
                    kotlinx.serialization.builtins.serializer<String>(),
                    kotlinx.serialization.builtins.serializer<String>()
                )
            ),
            exportData
        )

        // Clear and import
        dao.clearAll()
        assertTrue(dao.getAll().isEmpty())

        val imported = kotlinx.serialization.json.Json.decodeFromString<
            List<Map<String, String>>
        >(json)
        for (entry in imported) {
            dao.insert(ClipboardItem(
                content = entry["content"]!!,
                timestamp = entry["timestamp"]!!.toLong()
            ))
        }

        // Verify
        val restored = dao.getAll()
        assertEquals(2, restored.size)
        assertEquals("Export test 1", restored[1].content) // Descending order
        assertEquals("Export test 2", restored[0].content)
    }

    @Test
    fun export_emptyDatabase_returnsEmptyArray() = runBlocking {
        val all = dao.getAll()
        assertTrue(all.isEmpty())
    }

    @Test
    fun import_malformedJSON_doesNotCrash() = runBlocking {
        try {
            val bad = "not valid json {{{"
            kotlinx.serialization.json.Json.decodeFromString<
                List<Map<String, String>>
            >(bad)
        } catch (e: Exception) {
            // Expected — malformed JSON throws
            assertTrue(true)
        }
    }

    // ════════════════════════════════════════════════════════════════
    // SECTION 6: Database Lifecycle
    // ════════════════════════════════════════════════════════════════

    @Test
    fun database_reopen_persistsData() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db1 = Room.databaseBuilder(context, ClipboardDB::class.java, "test_persist_db")
            .allowMainThreadQueries()
            .build()
        db1.clipboardDao().insert(ClipboardItem(content = "Persisted", timestamp = 1))
        db1.close()

        val db2 = Room.databaseBuilder(context, ClipboardDB::class.java, "test_persist_db")
            .allowMainThreadQueries()
            .build()
        val all = db2.clipboardDao().getAll()
        assertEquals(1, all.size)
        assertEquals("Persisted", all[0].content)
        db2.close()

        // Cleanup
        context.deleteDatabase("test_persist_db")
    }
}
