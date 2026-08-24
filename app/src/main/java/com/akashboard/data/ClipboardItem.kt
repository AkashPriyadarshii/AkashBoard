/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * ClipboardItem.kt — Clipboard history data model.
 */

package com.akashboard.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.Ignore

/**
 * A single clipboard entry.
 */
@Entity(tableName = "clipboard_history")
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "source_app")
    val sourceApp: String? = null,

    @ColumnInfo(name = "char_count")
    val charCount: Int = text.length
) {
    @Ignore
    val content: String = text

    constructor(
        content: String,
        timestamp: Long,
        isPinned: Boolean = false,
        sourceApp: String? = null,
        id: Long = 0
    ) : this(
        id = id,
        text = content,
        timestamp = timestamp,
        isPinned = isPinned,
        sourceApp = sourceApp,
        charCount = content.length
    )
}
