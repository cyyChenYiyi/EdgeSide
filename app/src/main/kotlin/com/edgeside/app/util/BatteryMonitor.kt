package com.edgeside.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 电池信息（电量百分比 + 充电状态）
 *
 * 通过 sticky broadcast `BATTERY_CHANGED` 获取初始状态，再注册普通 receiver 监听变化。
 * 由于面板只在展开时读取，不需要高频轮询。
 */
class BatteryMonitor(private val context: Context) {

    data class Snapshot(val percent: Int, val charging: Boolean, val plugged: Int)

    private val _state = MutableStateFlow(read())
    val state: StateFlow<Snapshot> = _state

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            intent ?: return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
            _state.value = Snapshot(
                percent = pct,
                charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL,
                plugged = plugged
            )
        }
    }

    fun start() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = context.registerReceiver(receiver, filter)
        sticky?.let { receiver.onReceive(context, it) }
    }

    fun stop() {
        runCatching { context.unregisterReceiver(receiver) }
    }

    private fun read(): Snapshot {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        intent ?: return Snapshot(0, false, -1)
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        return Snapshot(
            percent = pct,
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL,
            plugged = plugged
        )
    }
}
