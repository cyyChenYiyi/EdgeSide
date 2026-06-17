package com.edgeside.app.notifications

import android.app.Application
import android.app.Notification
import android.content.pm.ApplicationInfo
import android.service.notification.StatusBarNotification
import com.edgeside.app.EdgeSideApp
import java.util.concurrent.ConcurrentHashMap

object NotificationCache {

    private const val MAX_SIZE = 20
    private val cache = ConcurrentHashMap<String, StatusBarNotification>()

    fun add(sbn: StatusBarNotification) {
        cache[sbn.key] = sbn
        trim()
    }

    fun remove(key: String) {
        cache.remove(key)
    }

    fun replaceAll(list: List<StatusBarNotification>) {
        cache.clear()
        list.take(MAX_SIZE).forEach { cache[it.key] = it }
    }

    private fun trim() {
        if (cache.size > MAX_SIZE) {
            val sorted = cache.values.sortedByDescending { it.postTime }
            val toRemove = sorted.drop(MAX_SIZE)
            toRemove.forEach { cache.remove(it.key) }
        }
    }

    fun getSummary(): String {
        val items = cache.values
            .filter { it.isClearable || it.notification.flags and Notification.FLAG_ONGOING_EVENT == 0 }
            .sortedByDescending { it.postTime }
            .take(3)

        if (items.isEmpty()) return ""

        val app: Application? = EdgeSideApp.instance
        val pm = app?.packageManager

        return items.joinToString("\n") { sbn ->
            val appName = pm?.let {
                try {
                    val info: ApplicationInfo? = it.getApplicationInfo(sbn.packageName, 0)
                    info?.let { inf -> it.getApplicationLabel(inf).toString() }
                } catch (_: Throwable) { null }
            } ?: sbn.packageName
            val title = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)
                ?.toString()?.take(40) ?: "(无标题)"
            "• $appName: $title"
        }
    }
}
