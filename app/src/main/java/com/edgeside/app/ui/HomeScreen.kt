package com.edgeside.app.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
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

@Composable
fun HomeScreen(
    activity: ComponentActivity,
    onNavigateToPickApps: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var hasOverlayPermission by remember { mutableStateOf(false) }
    var isServiceRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            hasOverlayPermission = Settings.canDrawOverlays(activity)
            isServiceRunning = activity.isServiceRunning(EdgeSideService::class.java.name)
            kotlinx.coroutines.delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activity.getString(R.string.app_name)) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = activity.getString(R.string.home_subtitle),
                fontSize = 16.sp,
                color = Color.Gray
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                backgroundColor = if (isServiceRunning) Color(0xFFE3F2FD) else Color(0xFFFFF3E0),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = activity.getString(R.string.home_status),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isServiceRunning) activity.getString(R.string.home_status_running)
                                else activity.getString(R.string.home_status_stopped),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isServiceRunning) Color(0xFF1565C0) else Color(0xFFE65100)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (!Settings.canDrawOverlays(activity)) {
                                    requestOverlayPermission(activity)
                                } else {
                                    EdgeSideService.start(activity)
                                }
                            },
                            modifier = Modifier.height(40.dp),
                            enabled = !isServiceRunning
                        ) {
                            Text(activity.getString(R.string.home_start))
                        }
                        OutlinedButton(
                            onClick = { EdgeSideService.stop(activity) },
                            modifier = Modifier.height(40.dp),
                            enabled = isServiceRunning
                        ) {
                            Text(activity.getString(R.string.home_stop))
                        }
                    }
                }
            }

            if (!hasOverlayPermission) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = Color(0xFFFFEBEE)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = activity.getString(R.string.perm_overlay),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = activity.getString(R.string.perm_overlay_desc),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { requestOverlayPermission(activity) },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("授权悬浮窗")
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "钉选应用 & 设置",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToPickApps,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(activity.getString(R.string.home_pick_apps))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(activity.getString(R.string.home_settings))
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Text(
                    text = "提示：点击屏幕边缘小条呼出快捷面板",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
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
