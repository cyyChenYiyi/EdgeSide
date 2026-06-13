package com.edgeside.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 小条配置（单例 row） */
@Entity(tableName = "bar_config")
data class BarConfig(
    @PrimaryKey val id: Int = 1,
    /** 距离屏幕顶部的 px，由 View 写入 */
    val verticalPos: Int = -1,
    /** LEFT / RIGHT */
    val edge: String = "RIGHT",
    /** 小条高度 dp */
    val heightDp: Int = 96
)
