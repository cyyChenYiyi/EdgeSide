package com.edgeside.app.ui.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgeside.app.permission.PermissionHelper

@Composable
fun PermissionGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "permissions",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "// 按顺序完成以下授权即可启用 EdgeSide",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionItem(
            title = "1. overlay",
            desc = "悬浮窗权限，用于显示边缘小条和面板",
            granted = PermissionHelper.hasOverlay(context),
            onGrant = { PermissionHelper.requestOverlay(context) }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionItem(
                title = "2. post_notifications",
                desc = "前台服务通知（Android 13+）",
                granted = PermissionHelper.hasPostNotifications(context),
                onGrant = { PermissionHelper.requestPostNotifications(context) }
            )
        }

        PermissionItem(
            title = "3. notification_listener",
            desc = "在系统设置中授权后，EdgeSide 即可读取通知",
            granted = PermissionHelper.hasNotificationListener(context),
            onGrant = { PermissionHelper.openNotificationListenerSettings(context) }
        )

        PermissionItem(
            title = "4. battery_optimization",
            desc = "将 EdgeSide 设为「不优化」，避免被系统杀后台",
            granted = PermissionHelper.isBatteryOptimizationIgnored(context),
            onGrant = { PermissionHelper.requestIgnoreBatteryOptimization(context) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "// 厂商自启动白名单（按需进入）",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Button(
            onClick = {
                // 跳转通用电池设置；各厂商通常会在这里附带自启动入口
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(intent)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "open battery settings")
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    desc: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            Text(text = desc, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Button(
                onClick = onGrant,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (granted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(text = if (granted) "granted" else "grant")
            }
        }
    }
}
