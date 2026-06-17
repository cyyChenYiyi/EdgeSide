package com.edgeside.app.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import timber.log.Timber

class EdgeNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.d("Notification listener connected")
        try {
            val active = activeNotifications
            NotificationCache.replaceAll(active.toList())
        } catch (e: Throwable) {
            Timber.e(e, "Failed to read active notifications")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { NotificationCache.add(it) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let { NotificationCache.remove(it.key) }
    }
}
