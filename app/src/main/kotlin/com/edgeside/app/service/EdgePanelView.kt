package com.edgeside.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edgeside.app.R
import com.edgeside.app.data.AppContainer
import com.edgeside.app.data.entity.PinnedApp
import com.edgeside.app.util.BatteryMonitor
import com.edgeside.app.util.ClipboardMonitor
import com.edgeside.app.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Floating quick panel.
 *
 * Two key behaviors:
 *  1. NO SCRIM. The panel is wrap_content sized. Touches outside the panel
 *     pass through to the underlying app via FLAG_NOT_TOUCH_MODAL. You can
 *     keep using other apps while the panel is open.
 *  2. ANCHORED TO THE EDGE BAR. buildLayoutParams(attachedEdge) places the
 *     panel on the same side as the edge bar (left edge bar -> panel opens
 *     on the left, right edge bar -> panel on the right). attachedEdge is
 *     the bar's WindowManager.LayoutParams.gravity int.
 *
 * Layout (top to bottom):
 *  - Title row: app name + close button
 *  - Quick toggles: \u624b\u7535 / WiFi / \u84dd\u7259 / \u9759\u97f3 / \u8ba1\u7b97 / \u8bbe\u7f6e (2 rows x 3 cols)
 *  - Pinned apps grid (4 cols)
 *  - Info cards: \u901a\u77e5 / \u7535\u91cf / \u7f51\u7edc / \u526a\u8d34\u677f
 */
@SuppressLint("ViewConstructor")
class EdgePanelView(
    context: Context,
    private val container: AppContainer
) : FrameLayout(context) {

    var onDismiss: (() -> Unit)? = null
    var onAppLaunch: ((String) -> Unit)? = null
    var onQuickAction: ((String) -> Unit)? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val battery = BatteryMonitor(context)
    private val network = NetworkMonitor()
    private val clipboard = ClipboardMonitor(context)

    private val rvApps: RecyclerView
    private val tvEmpty: TextView
    private val infoAdapter = InfoCardAdapter()

    private var pinnedJob: Job? = null
    private var infoJob: Job? = null

    init {
        val density = resources.displayMetrics.density

        // Outer card.
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = 16f * density
                setColor(Color.parseColor("#E61F1F1F"))
                setStroke((1 * density).toInt(), Color.parseColor("#333333"))
            }
            val pad = (14 * density).toInt()
            setPadding(pad, pad, pad, pad)
            elevation = 8f * density
        }
        val cardWidth = (320 * density).toInt()
        addView(card, LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_VERTICAL
            rightMargin = (8 * density).toInt()
            leftMargin = (8 * density).toInt()
        })

        // Title row.
        val title = TextView(context).apply {
            text = context.getString(R.string.app_name)
            setTextColor(Color.parseColor("#90CAF9"))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }
        val btnClose = TextView(context).apply {
            text = "\u00D7"  // multiplication X
            setTextColor(Color.parseColor("#BDBDBD"))
            textSize = 22f
            setPadding((12 * density).toInt(), 0, (4 * density).toInt(), 0)
            isClickable = true
            isFocusable = true
            setOnClickListener { onDismiss?.invoke() }
        }
        val titleRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(title, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(btnClose, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        card.addView(titleRow)

        // Quick toggles section.
        card.addView(makeSectionLabel("\u5FEB\u6377", density))  // \u5feb\u6377
        card.addView(buildQuickToggles(density))
        card.addView(makeDivider(density))

        // Pinned apps section.
        card.addView(makeSectionLabel("\u5E38\u7528", density))  // \u5e38\u7528
        tvEmpty = TextView(context).apply {
            text = "\u5728\u4E3B app \u4E2D\u9489\u9009"  // \u5728\u4e3b app \u4e2d\u9489\u9009
            setTextColor(Color.parseColor("#757575"))
            textSize = 11f
            gravity = Gravity.CENTER
            visibility = View.GONE
        }
        card.addView(tvEmpty, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = (8 * density).toInt()
            bottomMargin = (8 * density).toInt()
        })
        rvApps = RecyclerView(context).apply {
            layoutManager = GridLayoutManager(context, 4)
        }
        card.addView(rvApps, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        card.addView(makeDivider(density))

        // Info cards section.
        card.addView(makeSectionLabel("\u4FE1\u606F", density))  // \u4fe1\u606f
        val rvCards = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = infoAdapter
        }
        card.addView(rvCards, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = (4 * density).toInt()
            bottomMargin = (4 * density).toInt()
        })

        battery.start()
        network.start()
        clipboard.start()
        clipboard.readLatest()

        startObserve()
    }

    private fun makeSectionLabel(text: String, density: Float): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor("#9E9E9E"))
            textSize = 11f
            setPadding(0, (10 * density).toInt(), 0, (6 * density).toInt())
        }
    }

    private fun makeDivider(density: Float): View {
        return View(context).apply {
            setBackgroundColor(Color.parseColor("#2A2A2A"))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (1 * density).toInt()).apply {
                topMargin = (10 * density).toInt()
            }
        }
    }

    private fun buildQuickToggles(density: Float): LinearLayout {
        val container = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val items = listOf(
            QuickItem("flashlight", "\u624B\u7535"),  // \u624b\u7535
            QuickItem("wifi", "WiFi"),
            QuickItem("bluetooth", "\u84DD\u7259"),  // \u84dd\u7259
            QuickItem("silent", "\u9759\u97F3"),  // \u9759\u97f3
            QuickItem("calc", "\u8BA1\u7B97"),  // \u8ba1\u7b97
            QuickItem("settings", "\u8BBE\u7F6E"),  // \u8bbe\u7f6e
            QuickItem("screenshot", "\u622A\u56FE"),  // \u622a\u56fe
        )
        items.chunked(4).forEach { rowItems ->
            val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
            rowItems.forEach { item -> row.addView(buildQuickButton(item, density)) }
            container.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (4 * density).toInt()
                bottomMargin = (4 * density).toInt()
            })
        }
        return container
    }

    private fun buildQuickButton(item: QuickItem, density: Float): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                cornerRadius = 10f * density
                setColor(Color.parseColor("#2A2A2A"))
            }
            val pad = (12 * density).toInt()
            setPadding(pad, pad, pad, pad)
            setOnClickListener { onQuickAction?.invoke(item.action) }
        }
        val label = TextView(context).apply {
            text = item.label
            setTextColor(Color.parseColor("#E6E6E6"))
            textSize = 12f
        }
        container.addView(label)
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            val margin = (4 * density).toInt()
            setPadding(margin, 0, margin, 0)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            addView(container, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }

    /**
     * Anchors the panel to the same edge as the bar.
     * @param attachedEdge WindowManager.LayoutParams.gravity from the edge bar view
     *                     (typically Gravity.START or Gravity.END combined with CENTER_VERTICAL).
     */
    fun buildLayoutParams(attachedEdge: Int): WindowManager.LayoutParams {
        val gravity = if (attachedEdge and Gravity.START != 0) {
            Gravity.START or Gravity.CENTER_VERTICAL
        } else {
            Gravity.END or Gravity.CENTER_VERTICAL
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
        }
    }

    private fun startObserve() {
        pinnedJob?.cancel()
        pinnedJob = scope.launch {
            container.pinnedAppRepository.observeAll().collect { apps ->
                if (apps.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvApps.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvApps.visibility = View.VISIBLE
                    rvApps.adapter = AppShortcutAdapter(apps) { pkg -> onAppLaunch?.invoke(pkg) }
                }
            }
        }
        infoJob?.cancel()
        infoJob = scope.launch {
            combine(
                com.edgeside.app.notification.EdgeNotificationListener.summary,
                battery.state,
                network.state,
                clipboard.state
            ) { notifs, bat, net, clip -> buildSnapshot(notifs, bat, net, clip) }
                .collect { items -> infoAdapter.submit(items) }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        battery.stop()
        network.stop()
        clipboard.stop()
        scope.coroutineContext[Job]?.cancel()
    }

    private fun buildSnapshot(
        notifs: List<com.edgeside.app.notification.EdgeNotificationListener.NotificationItem>,
        bat: BatteryMonitor.Snapshot,
        net: NetworkMonitor.Snapshot,
        clip: ClipboardMonitor.Snapshot
    ): List<InfoCardAdapter.Item> {
        val items = mutableListOf<InfoCardAdapter.Item>()
        val firstNotif = notifs.firstOrNull()
        val notifText = if (firstNotif == null) "\u6682\u65E0\u901A\u77E5" else firstNotif.title  // \u6682\u65e0\u901a\u77e5
        items += InfoCardAdapter.Item.Notif("\u901A\u77E5", notifText.take(40))  // \u901a\u77e5
        val batText = if (bat.percent == 0) "\u672A\u77E5"  // \u672a\u77e5
            else "${bat.percent}%${if (bat.charging) " \u26A1" else ""}"
        items += InfoCardAdapter.Item.Battery("\u7535\u91CF", batText)  // \u7535\u91cf
        val netText = "\u2193 ${formatBytes(net.rxBps)}/s \u2191 ${formatBytes(net.txBps)}/s"
        items += InfoCardAdapter.Item.Network("\u7F51\u7EDC", netText)  // \u7f51\u7edc
        val clipText = clip.items.firstOrNull() ?: "\u7A7A"  // \u7a7a
        items += InfoCardAdapter.Item.Clipboard("\u526A\u8D34\u677F", clipText.take(40))  // \u526a\u8d34\u677f
        return items
    }

    private fun formatBytes(bps: Long): String = when {
        bps >= 1024 * 1024 -> "%.1f MB".format(bps / 1024.0 / 1024.0)
        bps >= 1024 -> "%.1f KB".format(bps / 1024.0)
        bps > 0 -> "$bps B"
        else -> "0"
    }

    private data class QuickItem(val action: String, val label: String)
}

/** app quick shortcut grid 4 cols */
class AppShortcutAdapter(
    private val apps: List<PinnedApp>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<AppShortcutAdapter.VH>() {

    class VH(val root: android.widget.LinearLayout) : RecyclerView.ViewHolder(root) {
        val icon: ImageView = root.getChildAt(0) as ImageView
        val label: TextView = root.getChildAt(1) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val density = ctx.resources.displayMetrics.density
        val icon = ImageView(ctx)
        val label = TextView(ctx).apply {
            textSize = 10f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setTextColor(Color.parseColor("#E6E6E6"))
            setPadding(0, (4 * density).toInt(), 0, 0)
        }
        val root = android.widget.LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                (8 * density).toInt(),
                (8 * density).toInt(),
                (8 * density).toInt(),
                (8 * density).toInt()
            )
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(icon, android.widget.LinearLayout.LayoutParams(
                (40 * density).toInt(),
                (40 * density).toInt()
            ))
            addView(label, android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
        return VH(root)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = apps[position]
        val ctx = holder.icon.context
        val pm = ctx.packageManager
        val label = runCatching { pm.getApplicationLabel(pm.getApplicationInfo(app.packageName, 0)).toString() }
            .getOrDefault(app.packageName)
        val icon = runCatching { pm.getApplicationIcon(pm.getApplicationInfo(app.packageName, 0)) }
            .getOrNull()
        holder.label.text = label
        holder.icon.setImageDrawable(icon)
        holder.itemView.setOnClickListener { onClick(app.packageName) }
    }

    override fun getItemCount() = apps.size
}

/** info card list */
class InfoCardAdapter : RecyclerView.Adapter<InfoCardAdapter.VH>() {

    sealed class Item {
        data class Notif(val label: String, val value: String) : Item()
        data class Battery(val label: String, val value: String) : Item()
        data class Network(val label: String, val value: String) : Item()
        data class Clipboard(val label: String, val value: String) : Item()
    }

    class VH(val view: TextView) : RecyclerView.ViewHolder(view)

    private var items: List<Item> = emptyList()

    fun submit(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            setTextColor(Color.parseColor("#E6E6E6"))
            textSize = 12f
            val d = resources.displayMetrics.density
            setPadding(
                (12 * d).toInt(),
                (8 * d).toInt(),
                (12 * d).toInt(),
                (8 * d).toInt()
            )
            background = GradientDrawable().apply {
                cornerRadius = 6f * d
                setColor(Color.parseColor("#262626"))
            }
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tv = holder.view
        tv.text = when (val it = items[position]) {
            is Item.Notif -> "${it.label}: ${it.value}"
            is Item.Battery -> "${it.label}: ${it.value}"
            is Item.Network -> "${it.label}: ${it.value}"
            is Item.Clipboard -> "${it.label}: ${it.value}"
        }
    }

    override fun getItemCount() = items.size
}