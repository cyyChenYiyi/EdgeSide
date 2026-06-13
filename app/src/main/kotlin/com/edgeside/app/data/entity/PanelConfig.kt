package com.edgeside.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 面板配置（单例 row）
 *
 * panelPosition: LEFT / RIGHT�������面板弹出方向
 * 卡片开关：4 个 boolean，控制信息卡是否展示
 */
@Entity(tableName = "panel_config")
data class PanelConfig(
    @PrimaryKey val id: Int = 1,
    val showNotifications: Boolean = true,
    val showBattery: Boolean = true,
    val showNetwork: Boolean = true,
    val showClipboard: Boolean = true,
    val panelPosition: String = "RIGHT"
)
