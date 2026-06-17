package com.edgeside.app.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgeside.app.R
import com.edgeside.app.overlay.EdgeSideService
import com.edgeside.app.permissions.PermissionHelper
import com.edgeside.app.ui.theme.IosBlue
import com.edgeside.app.ui.theme.IosCardBg
import com.edgeside.app.ui.theme.IosGroupLabel
import com.edgeside.app.ui.theme.IosGreen
import com.edgeside.app.ui.theme.IosOrange
import com.edgeside.app.ui.theme.IosRed

@Composable
fun HomeScreen(
    activity: ComponentActivity,
    onNavigateToPickApps: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var hasOverlayPermission by remember { mutableStateOf(false) }
    var hasUsageStatsPermission by remember { mutableStateOf(false) }
    var isServiceRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            hasOverlayPermission = Settings.canDrawOverlays(activity)
            hasUsageStatsPermission = PermissionHelper.hasUsageStatsPermission(activity)
            isServiceRunning = activity.isServiceRunning(EdgeSideService::class.java.name)
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // iOS-style large title
            Text(
                text = activity.getString(R.string.app_name),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
            )
            Text(
                text = activity.getString(R.string.home_subtitle),
                fontSize = 15.sp,
                color = IosGroupLabel
            )

            // Status card
            val (statusBg, statusColor, statusText) = when {
                !hasOverlayPermission -> Triple(IosRed.copy(alpha = 0.12f), IosRed, "需要悬浮窗权限")
                isServiceRunning -> Triple(IosGreen.copy(alpha = 0.12f), IosGreen, activity.getString(R.string.home_status_running))
                else -> Triple(IosOrange.copy(alpha = 0.12f), IosOrange, activity.getString(R.string.home_status_stopped))
            }
            IosCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = activity.getString(R.string.home_status),
                        fontSize = 13.sp,
                        color = IosGroupLabel
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = statusText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (!Settings.canDrawOverlays(activity)) {
                                    requestOverlayPermission(activity)
                                } else {
                                    EdgeSideService.start(activity)
                                }
                            },
                            modifier = Modifier.height(44.dp),
                            enabled = !isServiceRunning && hasOverlayPermission,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = IosBlue)
                        ) {
                            Text(activity.getString(R.string.home_start), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { EdgeSideService.stop(activity) },
                            modifier = Modifier.height(44.dp),
                            enabled = isServiceRunning,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(activity.getString(R.string.home_stop), color = IosBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Overlay permission card
            if (!hasOverlayPermission) {
                PermissionCard(
                    icon = "ⓘ",
                    tint = IosRed,
                    title = activity.getString(R.string.perm_overlay),
                    desc = activity.getString(R.string.perm_overlay_desc),
                    buttonText = "授权悬浮窗",
                    onClick = { requestOverlayPermission(activity) }
                )
            }

            // Usage stats permission card (for Recent Apps feature)
            if (!hasUsageStatsPermission) {
                PermissionCard(
                    icon = "◷",
                    tint = IosOrange,
                    title = "使用情况访问",
                    desc = "用于在面板中显示“最近应用”，仅读取应用使用时间，不上传任何数据。",
                    buttonText = "去授权",
                    onClick = { PermissionHelper.requestUsageStatsPermission(activity) }
                )
            }

            // Quick links
            IosCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "钉选应用 & 设置",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colors.onBackground
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onNavigateToPickApps,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = IosBlue)
                    ) {
                        Text(activity.getString(R.string.home_pick_apps), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(activity.getString(R.string.home_settings), color = IosBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Text(
            text = "提示：点击屏幕边缘小条呼出快捷面板",
            fontSize = 12.sp,
            color = IosGroupLabel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun IosCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun PermissionCard(
    icon: String,
    tint: Color,
    title: String,
    desc: String,
    buttonText: String,
    onClick: () -> Unit
) {
    IosCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(tint.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(10.dp))
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground, fontSize = 16.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(desc, fontSize = 13.sp, color = IosGroupLabel)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = tint)
            ) {
                Text(buttonText, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun requestOverlayPermission(activity: Activity) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${activity.packageName}")
    )
    activity.startActivity(intent)
}

private fun Context.isServiceRunning(serviceClassName: String): Boolean {
    val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    @Suppress("DEPRECATION")
    return am.getRunningServices(Int.MAX_VALUE).any { it.service.className == serviceClassName }
}
