package com.edgeside.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.edgeside.app.R
import com.edgeside.app.data.DataRepository
import com.edgeside.app.data.entity.PanelEdge
import com.edgeside.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class EdgeSideService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var prefJob: Job? = null

    private lateinit var windowManager: WindowManager
    private var edgeBar: EdgeBarView? = null
    private var edgePanel: EdgePanelView? = null
    private var currentEdge: PanelEdge = PanelEdge.RIGHT
    private var barHeightDp: Int = 100
    private var barVerticalPos: Int = 0

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "edgeside_service"
        private const val ACTION_STOP = "com.edgeside.app.ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, EdgeSideService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, EdgeSideService::class.java)
            intent.action = ACTION_STOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, buildNotification())
        observePreferences()
        showEdgeBar()
        Timber.d("EdgeSideService started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun observePreferences() {
        prefJob?.cancel()
        prefJob = serviceScope.launch {
            DataRepository.preferencesFlow.collect { prefs ->
                val edgeChanged = currentEdge != prefs.barEdge
                val heightChanged = barHeightDp != prefs.barHeightDp
                currentEdge = prefs.barEdge
                barHeightDp = prefs.barHeightDp
                if (prefs.barVerticalPosPx != 0) {
                    barVerticalPos = prefs.barVerticalPosPx
                }
                if (edgeChanged || heightChanged) {
                    refreshBar()
                }
            }
        }
    }

    private fun refreshBar() {
        edgeBar?.let { bar ->
            try {
                windowManager.removeView(bar)
            } catch (_: Throwable) {}
        }
        edgeBar = null
        if (edgePanel != null) {
            hidePanel()
        }
        showEdgeBar()
    }

    private fun showEdgeBar() {
        if (edgeBar != null) return
        val bar = EdgeBarView(this, currentEdge, barHeightDp).apply {
            onPanelToggle = { show ->
                if (show) showPanel() else hidePanel()
            }
            onPositionChanged = { posPx ->
                barVerticalPos = posPx
                DataRepository.setBarVerticalPos(posPx)
            }
            onEdgeChanged = { newEdge ->
                currentEdge = newEdge
                DataRepository.setBarEdge(newEdge)
                refreshBar()
            }
        }
        val params = buildBarParams(currentEdge, barVerticalPos)
        try {
            windowManager.addView(bar, params)
            edgeBar = bar
        } catch (e: Throwable) {
            Timber.e(e, "Failed to add EdgeBarView")
        }
    }

    private fun buildBarParams(edge: PanelEdge, yPos: Int): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val widthPx = (6 * resources.displayMetrics.density).toInt().coerceAtLeast(12)
        val heightPx = (barHeightDp * resources.displayMetrics.density).toInt()
        val metrics = resources.displayMetrics
        val screenHeight = metrics.heightPixels
        val finalY = if (yPos <= 0) (screenHeight / 2 - heightPx / 2) else yPos
        val gravity = if (edge == PanelEdge.LEFT) Gravity.LEFT or Gravity.TOP else Gravity.RIGHT or Gravity.TOP
        return WindowManager.LayoutParams(
            widthPx, heightPx,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            x = 0
            y = finalY.coerceIn(0, (screenHeight - heightPx).coerceAtLeast(0))
        }
    }

    private fun showPanel() {
        if (edgePanel != null) return
        val panel = EdgePanelView(this).apply {
            onDismiss = { hidePanel() }
            onAppLaunch = { pkg -> launchApp(pkg) }
        }
        val metrics = resources.displayMetrics
        val widthPx = (280 * metrics.density).toInt()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val gravity = if (currentEdge == PanelEdge.LEFT) {
            Gravity.LEFT or Gravity.TOP
        } else {
            Gravity.RIGHT or Gravity.TOP
        }
        val params = WindowManager.LayoutParams(
            widthPx,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            x = 0
            y = 0
        }
        try {
            windowManager.addView(panel, params)
            edgePanel = panel
            panel.bind()
            edgeBar?.setPanelShown(true)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to add EdgePanelView")
        }
    }

    private fun hidePanel() {
        edgePanel?.let { p ->
            try {
                p.release()
                windowManager.removeView(p)
            } catch (_: Throwable) {}
        }
        edgePanel = null
        edgeBar?.setPanelShown(false)
    }

    private fun launchApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        } catch (e: Throwable) {
            Timber.e(e, "Failed to launch app: $packageName")
        }
        hidePanel()
    }

    override fun onDestroy() {
        prefJob?.cancel()
        hidePanel()
        edgeBar?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Throwable) {}
        }
        edgeBar = null
        Timber.d("EdgeSideService destroyed")
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.service_running_desc)
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        val stopIntent = Intent(this, EdgeSideService::class.java).let {
            it.action = ACTION_STOP
            PendingIntent.getService(this, 1, it, PendingIntent.FLAG_IMMUTABLE)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_running))
            .setContentText(getString(R.string.service_running_desc))
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.action_stop),
                stopIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
