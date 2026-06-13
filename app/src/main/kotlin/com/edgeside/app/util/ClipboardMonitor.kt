package com.edgeside.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 剪贴板监听
 *
 * 保留最近 5 条文本。
 * 注意：Android 10+ 后台应用读不到剪贴板——本类只在面板展开时主动拉一次最新内容，
 * 由 EdgePanelView 在 showPanel 时调用 `readLatest()`。
 */
class ClipboardMonitor(private val context: Context) {

    data class Snapshot(val items: List<String>)

    private val cm: ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val _state = MutableStateFlow(Snapshot(emptyList()))
    val state: StateFlow<Snapshot> = _state

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        readLatest()
    }

    fun start() {
        cm.addPrimaryClipChangedListener(listener)
        readLatest()
    }

    fun stop() {
        runCatching { cm.removePrimaryClipChangedListener(listener) }
    }

    /** 由面板展开时调用，主动拉一次最新。 */
    fun readLatest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 10+：仅在应用进入前台时能读取（我们的应用已在前台或正展开面板）
            // 此处不强制校验焦点，由调用方负责
        }
        val clip = cm.primaryClip ?: return
        val text = clipText(clip) ?: return
        val list = (_state.value.items + text).distinct().take(MAX)
        _state.value = Snapshot(list)
    }

    private fun clipText(clip: ClipData): String? {
        if (clip.itemCount == 0) return null
        val item = clip.getItemAt(0)
        return item.coerceToText(context)?.toString()?.take(200)
    }

    companion object {
        private const val MAX = 5
    }
}
