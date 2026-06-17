package com.edgeside.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgeside.app.R
import com.edgeside.app.data.DataRepository
import com.edgeside.app.data.entity.PanelEdge
import com.edgeside.app.ui.theme.IosBlue
import com.edgeside.app.ui.theme.IosGroupLabel
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // iOS-style group header
            Text(
                "信息卡片",
                fontSize = 13.sp,
                color = IosGroupLabel,
                letterSpacing = 0.06.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            // iOS-style grouped card containing switches
            Card(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    CardSwitch(
                        title = activity.getString(R.string.settings_card_notifications),
                        checked = showNotifications,
                        onCheckedChange = { showNotifications = it; DataRepository.setCard("notifications", it) }
                    )
                    SettingsDivider()
                    CardSwitch(
                        title = activity.getString(R.string.settings_card_battery),
                        checked = showBattery,
                        onCheckedChange = { showBattery = it; DataRepository.setCard("battery", it) }
                    )
                    SettingsDivider()
                    CardSwitch(
                        title = activity.getString(R.string.settings_card_network),
                        checked = showNetwork,
                        onCheckedChange = { showNetwork = it; DataRepository.setCard("network", it) }
                    )
                    SettingsDivider()
                    CardSwitch(
                        title = activity.getString(R.string.settings_card_clipboard),
                        checked = showClipboard,
                        onCheckedChange = { showClipboard = it; DataRepository.setCard("clipboard", it) }
                    )
                }
            }

            // Bar position
            Text(
                "小条位置",
                fontSize = 13.sp,
                color = IosGroupLabel,
                letterSpacing = 0.06.sp
            )
            Card(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
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
            }

            Spacer(Modifier.size(16.dp))
            Text(
                "提示：修改配置后服务会自动刷新小条",
                fontSize = 13.sp,
                color = IosGroupLabel
            )
            Spacer(Modifier.size(32.dp))
        }

        // iOS-style top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "‹ 返回",
                color = IosBlue,
                fontSize = 17.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                modifier = Modifier.clickable { onBack() }
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    // iOS inset-grouped style: full-width inset separator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(0.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(IosGroupLabel.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun CardSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, modifier = Modifier.weight(1f), fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.surface, checkedTrackColor = IosBlue)
        )
    }
}

@Composable
private fun RadioOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = androidx.compose.material.RadioButtonDefaults.colors(selectedColor = IosBlue)
        )
        Text(text = label, fontSize = 16.sp)
    }
}
