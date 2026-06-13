package com.edgeside.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgeside.app.EdgeSideApp
import com.edgeside.app.data.entity.BarConfig
import com.edgeside.app.data.entity.PanelConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as EdgeSideApp
    val container = app.container
    val scope = rememberCoroutineScope()

    val panelFlow = remember { MutableStateFlow(PanelConfig()) }
    val barFlow = remember { MutableStateFlow(BarConfig()) }

    LaunchedEffect(Unit) {
        panelFlow.value = container.configRepository.getPanelConfig()
        barFlow.value = container.configRepository.getBarConfig()
    }

    val panel by panelFlow.collectAsState()
    val bar by barFlow.collectAsState()

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
                text = "settings",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        SectionHeader("// 面板信息卡")
        ToggleRow(
            label = "notifications",
            checked = panel.showNotifications,
            onChange = { v ->
                val u = panel.copy(showNotifications = v)
                panelFlow.value = u
                scope.launch { container.configRepository.upsertPanelConfig(u) }
            }
        )
        ToggleRow(
            label = "battery",
            checked = panel.showBattery,
            onChange = { v ->
                val u = panel.copy(showBattery = v)
                panelFlow.value = u
                scope.launch { container.configRepository.upsertPanelConfig(u) }
            }
        )
        ToggleRow(
            label = "network",
            checked = panel.showNetwork,
            onChange = { v ->
                val u = panel.copy(showNetwork = v)
                panelFlow.value = u
                scope.launch { container.configRepository.upsertPanelConfig(u) }
            }
        )
        ToggleRow(
            label = "clipboard",
            checked = panel.showClipboard,
            onChange = { v ->
                val u = panel.copy(showClipboard = v)
                panelFlow.value = u
                scope.launch { container.configRepository.upsertPanelConfig(u) }
            }
        )

        Spacer(modifier = Modifier.padding(top = 8.dp))

        SectionHeader("// 小条")
        EdgePicker(
            current = bar.edge,
            onChange = { e ->
                val u = bar.copy(edge = e)
                barFlow.value = u
                scope.launch { container.configRepository.upsertBarConfig(u) }
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun EdgePicker(current: String, onChange: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "edge",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            listOf("LEFT", "RIGHT").forEach { e ->
                val selected = current == e
                val bg = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
                val fg = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
                Text(
                    text = e,
                    color = fg,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .background(bg, RoundedCornerShape(4.dp))
                        .clickable { onChange(e) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
