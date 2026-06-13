package com.edgeside.app.util

import android.net.TrafficStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 网速监控（基于 TrafficStats）
 *
 * 每 2 秒采样一次，计算差值得到 B/s。
 * 0 = 没流量，避免展示噪声。
 */
class NetworkMonitor {

    data class Snapshot(val rxBps: Long, val txBps: Long)

    private val _state = MutableStateFlow(Snapshot(0, 0))
    val state: StateFlow<Snapshot> = _state

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    private var lastRx = TrafficStats.getTotalRxBytes()
    private var lastTx = TrafficStats.getTotalTxBytes()
    private var lastSample = System.currentTimeMillis()

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            while (true) {
                delay(SAMPLE_INTERVAL_MS)
                sample()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun sample() {
        val nowRx = TrafficStats.getTotalRxBytes()
        val nowTx = TrafficStats.getTotalTxBytes()
        val nowMs = System.currentTimeMillis()
        val dtMs = (nowMs - lastSample).coerceAtLeast(1L)
        val rxDelta = (nowRx - lastRx).coerceAtLeast(0L)
        val txDelta = (nowTx - lastTx).coerceAtLeast(0L)
        lastRx = nowRx
        lastTx = nowTx
        lastSample = nowMs
        val rxBps = rxDelta * 1000L / dtMs
        val txBps = txDelta * 1000L / dtMs
        _state.value = Snapshot(rxBps = rxBps, txBps = txBps)
        Timber.v("Net sample: rx=$rxBps tx=$txBps")
    }

    companion object {
        private const val SAMPLE_INTERVAL_MS = 2000L
    }
}
