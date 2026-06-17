package com.edgeside.app.system

import android.net.TrafficStats
import kotlin.math.max

object NetworkMonitor {

    private var lastDownBytes: Long = -1L
    private var lastUpBytes: Long = -1L
    private var lastTimestamp: Long = -1L

    @Synchronized
    fun sample(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val downBytes = max(0L, TrafficStats.getTotalRxBytes())
        val upBytes = max(0L, TrafficStats.getTotalTxBytes())

        if (lastDownBytes < 0 || lastTimestamp < 0) {
            lastDownBytes = downBytes
            lastUpBytes = upBytes
            lastTimestamp = now
            return Pair(0L, 0L)
        }
        val dtMs = (now - lastTimestamp).coerceAtLeast(1L)
        val dtSec = dtMs / 1000.0
        val downPerSec = ((downBytes - lastDownBytes) / dtSec).toLong().coerceAtLeast(0L)
        val upPerSec = ((upBytes - lastUpBytes) / dtSec).toLong().coerceAtLeast(0L)

        lastDownBytes = downBytes
        lastUpBytes = upBytes
        lastTimestamp = now

        return Pair(downPerSec, upPerSec)
    }
}
