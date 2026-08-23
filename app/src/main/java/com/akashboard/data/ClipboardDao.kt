/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * ClipboardDao.kt — Room DAO for clipboard operations.
 */

package com.akashboard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_history ORDER BY is_pinned DESC, timestamp DESC LIMIT :limit")
    suspend fun getItems(limit: Int = 50): List<ClipboardItem>

    @Query("SELECT * FROM clipboard_history WHERE is_pinned = 1 ORDER BY timestamp DESC")
    suspend fun getPinnedItems(): List<ClipboardItem>

    @Query("SELECT * FROM clipboard_history WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun search(query: String): List<ClipboardItem>

    @Insert
    suspend fun insert(item: ClipboardItem): Long

    @Update
    suspend fun update(item: ClipboardItem)

    @Delete
    suspend fun delete(item: ClipboardItem)

    @Query("DELETE FROM clipboard_history WHERE is_pinned = 0")
    suspend fun clearUnpinned()

    @Query("DELETE FROM clipboard_history")
    suspend fun clearAll()

    @Query("SELECT * FROM clipboard_history ORDER BY timestamp DESC")
    suspend fun getAll(): List<ClipboardItem>

    @Query("SELECT COUNT(*) FROM clipboard_history")
    suspend fun count(): Int

    @Query("DELETE FROM clipboard_history WHERE is_pinned = 0 AND timestamp < :olderThanTimestamp")
    suspend fun deleteOlderThan(olderThanTimestamp: Long)

    @Query("DELETE FROM clipboard_history WHERE id NOT IN (SELECT id FROM clipboard_history ORDER BY is_pinned DESC, timestamp DESC LIMIT :keepCount)")
    suspend fun trimToMax(keepCount: Int)
}
