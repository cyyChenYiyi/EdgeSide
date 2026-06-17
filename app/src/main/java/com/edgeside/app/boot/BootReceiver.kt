package com.edgeside.app.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.edgeside.app.overlay.EdgeSideService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            EdgeSideService.start(context)
        }
    }
}
