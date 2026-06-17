package com.edgeside.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pinned_apps",
    indices = [Index(value = ["package_name"], unique = true)]
)
data class PinnedApp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val package_name: String,
    val sort_order: Int,
    val added_at: Long
)
