package com.edgeside.app.notification

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * 系统通知监听
 *
 * 仅在内存中保留最近 20 条摘要，不持久化、不上传。
 * 授权后由系统自动 bind 到本服务。
 */
class EdgeNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.i("NotificationListener connected")
        _connected.value = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.w("NotificationListener disconnected")
        _connected.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val n = sbn.notification
        val title = n.extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val pkg = sbn.packageName

        synchronized(items) {
            items.add(
                0,
                NotificationItem(
                    packageName = pkg,
                    title = title,
                    text = text,
                    timestamp = sbn.postTime
                )
            )
            if (items.size > MAX_ITEMS) items.subList(MAX_ITEMS, items.size).clear()
        }
        _summary.value = items.toList()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 简单实现：不主动从列表移除（用户已经看过）
    }

    data class NotificationItem(
        val packageName: String,
        val title: String,
        val text: String,
        val timestamp: Long
    )

    companion object {
        const val MAX_ITEMS = 20
        private val items = mutableListOf<NotificationItem>()
        private val _summary = MutableStateFlow<List<NotificationItem>>(emptyList())
        val summary: StateFlow<List<NotificationItem>> = _summary

        private val _connected = MutableStateFlow(false)
        val connected: StateFlow<Boolean> = _connected

        @Suppress("unused")
        fun clear() {
            synchronized(items) { items.clear() }
            _summary.value = emptyList()
        }
    }
}
