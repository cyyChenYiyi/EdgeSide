package com.edgeside.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
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
import com.edgeside.app.data.DataRepository
import com.edgeside.app.data.entity.PanelEdge
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(activity: ComponentActivity, onBack: () -> Unit) {
    var showNotifications by remember { mutableStateOf(true) }
    var showBattery by remember { mutableStateOf(true) }
    var showNetwork by remember { mutableStateOf(true) }
    var showClipboard by remember { mutableStateOf(true) }
    var barEdge by remember { mutableStateOf(PanelEdge.RIGHT) }

    LaunchedEffect(Unit) {
        launch {
            DataRepository.preferencesFlow.collect { prefs ->
                showNotifications = prefs.showNotificationsCard
                showBattery = prefs.showBatteryCard
                showNetwork = prefs.showNetworkCard
                showClipboard = prefs.showClipboardCard
                barEdge = prefs.barEdge
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activity.getString(R.string.settings_title)) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                navigationIcon = {
                    Text("←", modifier = Modifier.padding(16.dp).clickable { onBack() }, fontSize = 20.sp)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("信息卡片", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

            CardSwitch(
                title = activity.getString(R.string.settings_card_notifications),
                checked = showNotifications,
                onCheckedChange = { showNotifications = it; DataRepository.setCard("notifications", it) }
            )
            CardSwitch(
                title = activity.getString(R.string.settings_card_battery),
                checked = showBattery,
                onCheckedChange = { showBattery = it; DataRepository.setCard("battery", it) }
            )
            CardSwitch(
                title = activity.getString(R.string.settings_card_network),
                checked = showNetwork,
                onCheckedChange = { showNetwork = it; DataRepository.setCard("network", it) }
            )
            CardSwitch(
                title = activity.getString(R.string.settings_card_clipboard),
                checked = showClipboard,
                onCheckedChange = { showClipboard = it; DataRepository.setCard("clipboard", it) }
            )

            Spacer(Modifier.size(12.dp))
            Text("小条位置", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RadioOption(
                    label = "左侧",
                    selected = barEdge == PanelEdge.LEFT,
                    onClick = { barEdge = PanelEdge.LEFT; DataRepository.setBarEdge(PanelEdge.LEFT) }
                )
                RadioOption(
                    label = "右侧",
                    selected = barEdge == PanelEdge.RIGHT,
                    onClick = { barEdge = PanelEdge.RIGHT; DataRepository.setBarEdge(PanelEdge.RIGHT) }
                )
            }

            Spacer(Modifier.size(24.dp))
            Text("提示：修改配置后服务会自动刷新小条", fontSize = 13.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun CardSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, modifier = Modifier.weight(1f), fontSize = 15.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun RadioOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, fontSize = 14.sp)
    }
}
