package com.edgeside.app.system

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.edgeside.app.permissions.PermissionHelper
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Provides the list of recently-used apps via [UsageStatsManager].
 * Requires the user to grant "Usage access" (PACKAGE_USAGE_STATS).
 *
 * Note: this returns apps sorted by last-time-used so the user can quickly
 * re-open them. It does NOT switch to an existing background task instance;
 * the caller re-launches via the package's launch intent.
 */
object RecentAppsProvider {

    private const val LIMIT = 8

    data class RecentApp(
        val packageName: String,
        val label: String,
        val lastUsed: Long
    )

    /**
     * @return up to [LIMIT] recently-used launchable apps, newest first.
     *         Empty list if no permission or no data. The current app and the
     *         launcher are filtered out.
     */
    fun query(context: Context): List<RecentApp> {
        if (!PermissionHelper.hasUsageStatsPermission(context)) return emptyList()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) return emptyList()
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                now - TimeUnit.HOURS.toMillis(24),
                now
            ) ?: return emptyList()

            val pm = context.packageManager
            val ownPkg = context.packageName
            val launcherPkg = getLauncherPackage(pm)

            // De-dup by package, keep the highest lastTimeUsed.
            val latest = LinkedHashMap<String, Long>()
            for (s in stats) {
                val pkg = s.packageName ?: continue
                if (pkg == ownPkg || pkg == launcherPkg) continue
                if (s.lastTimeUsed <= 0) continue
                val prev = latest[pkg] ?: 0L
                if (s.lastTimeUsed > prev) latest[pkg] = s.lastTimeUsed
            }

            latest.entries
                .sortedByDescending { it.value }
                .take(LIMIT)
                .mapNotNull { (pkg, used) ->
                    val label = resolveLabel(pm, pkg) ?: return@mapNotNull null
                    // only launchable apps
                    if (pm.getLaunchIntentForPackage(pkg) == null) return@mapNotNull null
                    RecentApp(pkg, label, used)
                }
        } catch (e: Throwable) {
            Timber.e(e, "RecentAppsProvider.query failed")
            emptyList()
        }
    }

    private fun resolveLabel(pm: PackageManager, pkg: String): String? {
        return try {
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info)?.toString()
        } catch (_: Throwable) {
            null
        }
    }

    private fun getLauncherPackage(pm: PackageManager): String? {
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
                .addCategory(android.content.Intent.CATEGORY_HOME)
            val res = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            res?.activityInfo?.packageName
        } catch (_: Throwable) {
            null
        }
    }
}
