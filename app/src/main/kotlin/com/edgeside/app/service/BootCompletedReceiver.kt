package com.edgeside.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.edgeside.app.permission.PermissionHelper
import timber.log.Timber

/**
 * 开机自启接收器
 *
 * 仅在用户已授权 SYSTEM_ALERT_WINDOW 时才启动悬浮服务，避免无授权情况下崩溃。
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!PermissionHelper.hasOverlay(context)) {
            Timber.w("Boot received but overlay not granted, skip")
            return
        }
        Timber.i("Boot completed — starting overlay service")
        EdgeSideService.start(context)
    }
}
