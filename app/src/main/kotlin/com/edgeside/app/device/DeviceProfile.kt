package com.edgeside.app.device

import android.os.Build

/**
 * 设备识别与厂商适配路由
 *
 * 适配目标：
 * - MIUI / HyperOS：自启动 + 电池优化 + 后台弹出
 * - EMUI / HarmonyOS：应用启动管理 + 电池优化
 * - ColorOS：自启动 + 耗电保护
 * - OriginOS：后台高耗电 + 自启动
 * - OneUI：电池优化（最简单）
 */
enum class Vendor {
    XIAOMI, HUAWEI, OPPO, VIVO, SAMSUNG, GOOGLE, OTHER
}

data class DeviceProfile(
    val vendor: Vendor,
    val model: String,
    val sdkInt: Int
) {
    /** 自启动入口的常见路径（部分厂商支持直接跳转） */
    val autostartIntentClass: String?
        get() = when (vendor) {
            Vendor.XIAOMI -> "com.miui.permcenter.autostart.AutoStartManagementActivity"
            Vendor.OPPO -> "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            Vendor.VIVO -> "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            else -> null
        }
}

object DeviceProfileFactory {
    fun detect(): DeviceProfile {
        val vendor = when {
            Build.MANUFACTURER.equals("Xiaomi", true) || Build.MANUFACTURER.contains("Redmi", true) -> Vendor.XIAOMI
            Build.MANUFACTURER.equals("HUAWEI", true) || Build.MANUFACTURER.equals("HONOR", true) -> Vendor.HUAWEI
            Build.MANUFACTURER.equals("OPPO", true) || Build.MANUFACTURER.equals("realme", true) -> Vendor.OPPO
            Build.MANUFACTURER.equals("vivo", true) -> Vendor.VIVO
            Build.MANUFACTURER.equals("samsung", true) -> Vendor.SAMSUNG
            Build.MANUFACTURER.equals("Google", true) -> Vendor.GOOGLE
            else -> Vendor.OTHER
        }
        return DeviceProfile(
            vendor = vendor,
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            sdkInt = Build.VERSION.SDK_INT
        )
    }
}
