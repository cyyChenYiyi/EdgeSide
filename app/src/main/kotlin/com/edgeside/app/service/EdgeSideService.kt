package com.edgeside.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.edgeside.app.CaptureActivity
import com.edgeside.app.EdgeSideApp
import com.edgeside.app.MainActivity
import com.edgeside.app.R
import com.edgeside.app.data.entity.BarConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Foreground service that hosts the floating ball overlay.
 *
 * Thread-safety:
 *  All edgeBar / edgePanel reads + addView / removeView must happen inside
 *  viewLock. We snapshot the BarConfig before entering viewLock so the
 *  synchronized blocks contain no suspend points.
 */
class EdgeSideService : Service() {

    private lateinit var windowManager: WindowManager
    private var edgeBar: EdgeBarView? = null
    private var edgePanel: EdgePanelView? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val viewLock = Any()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startInForeground()
        scope.launch {
            val cfg = (application as EdgeSideApp).container.configRepository.getBarConfig()
            showEdgeBar(cfg)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("EdgeSideService onStartCommand action=${intent?.action}")
        when (intent?.action) {
            ACTION_HIDE -> hideEdgeBar()
            ACTION_SHOW -> scope.launch {
                val cfg = (application as EdgeSideApp).container.configRepository.getBarConfig()
                ensureBarVisible(cfg)
            }
            else -> scope.launch {
                val cfg = (application as EdgeSideApp).container.configRepository.getBarConfig()
                ensureBarVisible(cfg)
            }
        }
        // START_REDELIVER_INTENT: when killed by the system, recreate with the
        // original intent. Keeps the floating ball alive across OOM kills.
        return START_REDELIVER_INTENT
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Timber.w("Task removed -- will be restarted by system")
    }

    override fun onDestroy() {
        super.onDestroy()
        synchronized(viewLock) {
            edgeBar?.let { runCatching { windowManager.removeView(it) } }
            edgePanel?.let { runCatching { windowManager.removeView(it) } }
            edgeBar = null
            edgePanel = null
        }
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startInForeground() {
        val channelId = CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.svc_running_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.svc_running_text)
                setShowBadge(false)
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(getString(R.string.svc_running_title))
            .setContentText(getString(R.string.svc_running_text))
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notif)
        }
    }

    private fun showEdgeBar(config: BarConfig) {
        ensureBarVisible(config)
    }

    private fun ensureBarVisible(config: BarConfig) {
        synchronized(viewLock) {
            val current = edgeBar
            when {
                current == null -> attachBarLocked(config)
                !current.isAttachedToWindow -> {
                    val params = current.layoutParams as? WindowManager.LayoutParams
                    if (params != null) {
                        runCatching { windowManager.addView(current, params) }
                    }
                }
            }
        }
    }

    /** Caller MUST hold [viewLock]. */
    private fun attachBarLocked(config: BarConfig) {
        if (edgeBar != null) return
        val container = (application as EdgeSideApp).container
        val ball = EdgeBarView(this, container).apply {
            onPanelToggle = { show -> if (show) showPanel() else hidePanel() }
            // Long-press 3s on the floating ball = full service shutdown.
            // Lets the user fully quit EdgeSide even if "hide from recents" is on.
            onLongPressStop = { stopEntireService() }
        }
        val params = ball.buildLayoutParams(config.edge)
        if (config.verticalPos > 0) params.y = config.verticalPos
        runCatching { windowManager.addView(ball, params) }
            .onFailure { Timber.e(it, "addView(EdgeBar) failed") }
        edgeBar = ball
    }

    private fun hideEdgeBar() {
        synchronized(viewLock) {
            val bar = edgeBar ?: return
            if (bar.isAttachedToWindow) {
                runCatching { windowManager.removeView(bar) }
            }
            edgeBar = null
        }
    }

    private fun showPanel() {
        synchronized(viewLock) {
            if (edgePanel != null) return
            val container = (application as EdgeSideApp).container
            val ball = edgeBar ?: return
            val ballParams = (ball.layoutParams as WindowManager.LayoutParams)
            val panel = EdgePanelView(this, container).apply {
                onDismiss = { hidePanel() }
                onAppLaunch = { pkg -> launchApp(pkg) }
                onQuickAction = { action -> handleQuickAction(action) }
            }
            val panelParams = panel.buildLayoutParams(attachedEdge = ballParams.gravity)
            runCatching { windowManager.addView(panel, panelParams) }
                .onFailure { Timber.e(it, "addView(EdgePanel) failed") }
            edgePanel = panel
            ball.setPanelShowing(true)
        }
    }

    private fun hidePanel() {
        synchronized(viewLock) {
            val panel = edgePanel ?: return
            runCatching { windowManager.removeView(panel) }
            edgePanel = null
            edgeBar?.setPanelShowing(false)
        }
    }

    private fun launchApp(pkg: String) {
        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            hidePanel()
        } else {
            Timber.w("No launch intent for $pkg")
        }
    }

    // -------- Quick action handlers --------

    private fun handleQuickAction(action: String) {
        Timber.i("quick action: $action")
        when (action) {
            "flashlight" -> toggleFlashlight()
            "wifi" -> toggleWifi()
            "bluetooth" -> openBluetoothSettings()
            "silent" -> toggleSilent()
            "calc" -> launchCalculator()
            "settings" -> launchAndroidSettings()
            "screenshot" -> captureScreen()
            else -> Timber.w("unknown quick action: $action")
        }
    }

    private fun toggleFlashlight() {
        runCatching {
            val cm = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cm.cameraIdList.firstOrNull { id ->
                runCatching {
                    cm.getCameraCharacteristics(id)
                        .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }.getOrDefault(false)
            } ?: return@runCatching
            try {
                cm.setTorchMode(cameraId, true)
            } catch (_: Exception) {
                try { cm.setTorchMode(cameraId, false) } catch (_: Exception) {}
            }
        }.onFailure { Timber.w(it, "toggleFlashlight failed") }
    }

    private fun toggleWifi() {
        runCatching {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            wm.isWifiEnabled = !wm.isWifiEnabled
        }.onFailure { Timber.w(it, "toggleWifi failed") }
    }

    private fun openBluetoothSettings() {
        runCatching {
            val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            hidePanel()
        }.onFailure { Timber.w(it, "openBluetoothSettings failed") }
    }

    private fun toggleSilent() {
        runCatching {
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.ringerMode = if (am.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                AudioManager.RINGER_MODE_NORMAL
            } else {
                AudioManager.RINGER_MODE_SILENT
            }
        }.onFailure { Timber.w(it, "toggleSilent failed") }
    }

    private fun launchCalculator() {
        runCatching {
            val candidates = listOf(
                "com.android.calculator2",
                "com.google.android.calculator",
                "com.miui.calculator",
                "com.huawei.calculator"
            )
            for (pkg in candidates) {
                val intent = packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    hidePanel()
                    return@runCatching
                }
            }
            launchAndroidSettings()
        }.onFailure { Timber.w(it, "launchCalculator failed") }
    }

    private fun launchAndroidSettings() {
        runCatching {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            hidePanel()
        }.onFailure { Timber.w(it, "launchAndroidSettings failed") }
    }

    private fun captureScreen() {
        runCatching {
            val intent = Intent(this, CaptureActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            hidePanel()
        }.onFailure { Timber.w(it, "captureScreen failed") }
    }

    // -------- Long-press stop --------

    private fun stopEntireService() {
        Timber.i("stopEntireService via long-press")
        hideEdgeBar()
        hidePanel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "edgeside_running"

        const val ACTION_SHOW = "com.edgeside.app.action.SHOW_BAR"
        const val ACTION_HIDE = "com.edgeside.app.action.HIDE_BAR"

        fun start(context: Context) {
            val intent = Intent(context, EdgeSideService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, EdgeSideService::class.java))
        }
    }
}