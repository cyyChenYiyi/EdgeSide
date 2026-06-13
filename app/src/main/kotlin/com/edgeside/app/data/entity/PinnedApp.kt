package com.edgeside.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 用户钉选的应用 */
@Entity(tableName = "pinned_apps")
data class PinnedApp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val sortOrder: Int,
    val addedAt: Long
)
