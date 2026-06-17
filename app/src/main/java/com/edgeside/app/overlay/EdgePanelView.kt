package com.edgeside.app.overlay

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Outline
import android.os.BatteryManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edgeside.app.R
import com.edgeside.app.data.DataRepository
import com.edgeside.app.data.entity.PinnedApp
import com.edgeside.app.system.RecentAppsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EdgePanelView(ctx: Context) : LinearLayout(ctx) {

    var onDismiss: (() -> Unit)? = null
    var onAppLaunch: ((String) -> Unit)? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var appsJob: Job? = null
    private var prefsJob: Job? = null
    private var tickerJob: Job? = null
    private var lastClipText: String = ""

    private lateinit var recentAppsList: RecyclerView
    private lateinit var appsGrid: RecyclerView
    private lateinit var cardsList: RecyclerView
    private lateinit var recentAdapter: RecentAppsAdapter
    private lateinit var appsAdapter: PinnedAppsAdapter
    private lateinit var cardsAdapter: InfoCardsAdapter
    private var currentPrefs: com.edgeside.app.data.UserPreferences? = null

    private data class InfoCard(val title: String, val content: String)

    init {
        LayoutInflater.from(ctx).inflate(R.layout.view_edge_panel, this, true)
        orientation = VERTICAL
        // The background drawable already has rounded corners; clip children to it.
        background = ctx.getDrawable(R.drawable.panel_bg)
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val r = (24 * resources.displayMetrics.density)
                outline.setRoundRect(0, 0, view.width, view.height, r)
            }
        }

        recentAppsList = findViewById(R.id.recentAppsList)
        appsGrid = findViewById(R.id.appsGrid)
        cardsList = findViewById(R.id.cardsList)

        recentAppsList.layoutManager =
            LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
        recentAdapter = RecentAppsAdapter(ctx) { pkg -> onAppLaunch?.invoke(pkg) }
        recentAppsList.adapter = recentAdapter
        recentAppsList.isNestedScrollingEnabled = false

        appsGrid.layoutManager = GridLayoutManager(ctx, 4)
        appsAdapter = PinnedAppsAdapter(ctx) { pkg -> onAppLaunch?.invoke(pkg) }
        appsGrid.adapter = appsAdapter

        cardsList.layoutManager = LinearLayoutManager(ctx)
        cardsAdapter = InfoCardsAdapter()
        cardsList.adapter = cardsAdapter
    }

    fun setPanelEdge(edge: com.edgeside.app.data.entity.PanelEdge) {
        // reserved for future UI changes
    }

    fun bind() {
        appsJob?.cancel()
        appsJob = scope.launch {
            DataRepository.pinnedAppsFlow.collect { list ->
                appsAdapter.submitList(list)
            }
        }
        prefsJob?.cancel()
        prefsJob = scope.launch {
            DataRepository.preferencesFlow.collect { prefs ->
                currentPrefs = prefs
                refreshCards()
            }
        }
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                refreshRecentApps()
                refreshCards()
                delay(2000)
            }
        }
        // Immediate first refresh
        refreshRecentApps()
    }

    fun release() {
        appsJob?.cancel()
        prefsJob?.cancel()
        tickerJob?.cancel()
    }

    private fun refreshRecentApps() {
        try {
            val list = RecentAppsProvider.query(context)
            recentAdapter.submitList(list)
        } catch (e: Throwable) {
            Timber.e(e, "refreshRecentApps failed")
        }
    }

    private fun refreshCards() {
        val prefs = currentPrefs ?: return
        val cards = mutableListOf<InfoCard>()
        val res = context.resources
        if (prefs.showNotificationsCard) {
            val summary = com.edgeside.app.notifications.NotificationCache.getSummary()
            cards.add(
                InfoCard(
                    res.getString(R.string.panel_notifications),
                    summary.ifBlank { "暂无新通知" }
                )
            )
        }
        if (prefs.showBatteryCard) {
            cards.add(InfoCard(res.getString(R.string.panel_battery), readBattery()))
        }
        if (prefs.showNetworkCard) {
            cards.add(InfoCard(res.getString(R.string.panel_network), readNetwork()))
        }
        if (prefs.showClipboardCard) {
            cards.add(InfoCard(res.getString(R.string.panel_clipboard), readClipboard()))
        }
        cardsAdapter.submitList(cards)
    }

    private fun readBattery(): String {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            val state = if (charging) "充电中" else "使用中"
            "$pct% · $state"
        } catch (e: Throwable) {
            Timber.e(e, "readBattery failed")
            "未知"
        }
    }

    private fun readNetwork(): String {
        return try {
            val (down, up) = com.edgeside.app.system.NetworkMonitor.sample()
            "↓ ${formatSpeed(down)}  ↑ ${formatSpeed(up)}"
        } catch (e: Throwable) {
            Timber.e(e, "readNetwork failed")
            "网速不可用"
        }
    }

    private fun readClipboard(): String {
        return try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = cm.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()?.trim() ?: ""
                if (text.isNotBlank()) {
                    lastClipText = text
                    return text.take(200)
                }
            }
            if (lastClipText.isNotBlank()) "最近: ${lastClipText.take(80)}" else "剪贴板为空"
        } catch (e: Throwable) {
            Timber.e(e, "readClipboard failed")
            "剪贴板不可用"
        }
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec < 1024 -> "${bytesPerSec} B/s"
            bytesPerSec < 1024 * 1024 -> "${bytesPerSec / 1024} KB/s"
            else -> "${String.format(Locale.US, "%.1f", bytesPerSec / (1024.0 * 1024.0))} MB/s"
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Consume touch inside panel to prevent leaking to underlying apps
        return true
    }

    // ---- Recent Apps Adapter (horizontal) ----
    private class RecentAppsAdapter(
        private val ctx: Context,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<RecentAppViewHolder>() {

        private val items = mutableListOf<RecentAppsProvider.RecentApp>()
        private val pm = ctx.packageManager

        fun submitList(list: List<RecentAppsProvider.RecentApp>) {
            // Only notify when content actually changed to avoid flicker
            if (items.map { it.packageName } == list.map { it.packageName }) return
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentAppViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_app, parent, false)
            return RecentAppViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecentAppViewHolder, position: Int) {
            val app = items[position]
            val info = try {
                pm.getApplicationInfo(app.packageName, 0)
            } catch (_: Throwable) { null }
            holder.icon.setImageDrawable(
                info?.loadIcon(pm) ?: ctx.getDrawable(android.R.drawable.sym_def_app_icon)
            )
            holder.label.text = app.label
            holder.itemView.setOnClickListener { onClick(app.packageName) }
        }

        override fun getItemCount(): Int = items.size
    }

    private class RecentAppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
    }

    // ---- Pinned Apps Adapter ----
    private class PinnedAppsAdapter(
        private val ctx: Context,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<PinnedAppsViewHolder>() {

        private val items = mutableListOf<PinnedApp>()
        private val pm = ctx.packageManager

        fun submitList(list: List<PinnedApp>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinnedAppsViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pinned_app, parent, false)
            return PinnedAppsViewHolder(view)
        }

        override fun onBindViewHolder(holder: PinnedAppsViewHolder, position: Int) {
            val app = items[position]
            val info = try {
                pm.getApplicationInfo(app.package_name, 0)
            } catch (_: Throwable) { null }
            holder.icon.setImageDrawable(
                info?.loadIcon(pm) ?: ctx.getDrawable(android.R.drawable.sym_def_app_icon)
            )
            holder.label.text = info?.loadLabel(pm)?.toString() ?: app.package_name
            holder.itemView.setOnClickListener { onClick(app.package_name) }
        }

        override fun getItemCount(): Int = items.size
    }

    private class PinnedAppsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
    }

    // ---- Info Cards Adapter ----
    private class InfoCardsAdapter : RecyclerView.Adapter<InfoCardViewHolder>() {
        private val items = mutableListOf<InfoCard>()

        fun submitList(list: List<InfoCard>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoCardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_info_card, parent, false)
            return InfoCardViewHolder(view)
        }

        override fun onBindViewHolder(holder: InfoCardViewHolder, position: Int) {
            val card = items[position]
            holder.title.text = card.title
            holder.content.text = card.content
        }

        override fun getItemCount(): Int = items.size
    }

    private class InfoCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.cardTitle)
        val content: TextView = view.findViewById(R.id.cardContent)
    }
}
