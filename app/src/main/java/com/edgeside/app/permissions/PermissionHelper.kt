package com.edgeside.app.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import timber.log.Timber

enum class DeviceVendor(val displayName: String) {
    XIAOMI("小米/Redmi/POCO"),
    HUAWEI("华为/Honor"),
    OPPO("OPPO/一加/realme"),
    VIVO("vivo"),
    SAMSUNG("三星"),
    GOOGLE("Google"),
    OTHER("其他")
}

object DeviceProfile {

    fun detect(): DeviceVendor {
        val brand = Build.BRAND?.lowercase() ?: ""
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        val model = Build.MODEL?.lowercase() ?: ""
        val fingerprint = Build.FINGERPRINT?.lowercase() ?: ""

        return when {
            brand.contains("xiaomi") || manufacturer.contains("xiaomi") ||
                brand.contains("redmi") || brand.contains("poco") -> DeviceVendor.XIAOMI
            brand.contains("huawei") || manufacturer.contains("huawei") ||
                brand.contains("honor") -> DeviceVendor.HUAWEI
            brand.contains("oppo") || manufacturer.contains("oppo") ||
                brand.contains("oneplus") || brand.contains("realme") -> DeviceVendor.OPPO
            brand.contains("vivo") || manufacturer.contains("vivo") -> DeviceVendor.VIVO
            brand.contains("samsung") || manufacturer.contains("samsung") -> DeviceVendor.SAMSUNG
            brand.contains("google") || fingerprint.contains("google") -> DeviceVendor.GOOGLE
            else -> DeviceVendor.OTHER
        }
    }

    fun getAutostartIntent(vendor: DeviceVendor = detect()): Intent? {
        return when (vendor) {
            DeviceVendor.XIAOMI -> {
                val intent = Intent()
                intent.component = android.content.ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent
            }
            DeviceVendor.HUAWEI -> {
                val intent = Intent()
                intent.component = android.content.ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent
            }
            DeviceVendor.OPPO -> {
                val intent = Intent()
                intent.component = android.content.ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent
            }
            DeviceVendor.VIVO -> {
                val intent = Intent()
                intent.component = android.content.ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent
            }
            else -> null
        }
    }

    fun canResolveAutostart(context: Context): Boolean {
        val intent = getAutostartIntent() ?: return false
        val list = context.packageManager.queryIntentActivities(intent, 0)
        return list.isNotEmpty()
    }
}

object PermissionHelper {

    fun hasOverlayPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermission(context: Context) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to launch overlay permission screen")
        }
    }

    fun requestNotificationListener(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to launch notification listener settings")
        }
    }

    fun openAutostart(context: Context) {
        val intent = DeviceProfile.getAutostartIntent() ?: return
        try {
            context.startActivity(intent)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to launch autostart screen")
        }
    }

    /**
     * Whether the user has granted "Usage access" (PACKAGE_USAGE_STATS).
     * Required to read the list of recently-used apps via UsageStatsManager.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageStatsPermission(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to launch usage access settings")
        }
    }
}
