package com.edgeside.app.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.edgeside.app.R
import com.edgeside.app.data.DataRepository
import com.edgeside.app.ui.theme.IosBlue
import com.edgeside.app.ui.theme.IosGroupLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val pinned: Boolean
)

@Composable
fun PickAppsScreen(activity: ComponentActivity, onBack: () -> Unit) {
    var search by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf(emptyList<AppEntry>()) }
    var pinnedSet by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(Unit) {
        val pm = activity.packageManager
        val pinnedNames = mutableSetOf<String>()
        val pinnedJob = launch {
            DataRepository.pinnedAppsFlow.collect { list ->
                pinnedNames.clear()
                pinnedNames.addAll(list.map { it.package_name })
                pinnedSet = pinnedNames.toSet()
                apps = apps.map { it.copy(pinned = pinnedNames.contains(it.packageName)) }
            }
        }
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = withContext(Dispatchers.IO) {
            pm.queryIntentActivities(intent, 0)
        }
        val entries = resolved.mapNotNull { ri ->
            try {
                val appInfo = pm.getApplicationInfo(ri.activityInfo.packageName, 0)
                AppEntry(
                    packageName = ri.activityInfo.packageName,
                    label = ri.loadLabel(pm).toString(),
                    icon = ri.loadIcon(pm),
                    pinned = pinnedNames.contains(ri.activityInfo.packageName)
                )
            } catch (_: Throwable) { null }
        }.sortedBy { it.label.lowercase() }
        apps = entries
    }

    val filtered = apps.filter {
        search.isBlank() || it.label.contains(search, ignoreCase = true)
            || it.packageName.contains(search, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 44.dp)) {
            // Search bar
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text(activity.getString(R.string.pick_apps_search)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                text = activity.getString(R.string.pick_apps_selected, pinnedSet.size),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = IosGroupLabel,
                fontSize = 13.sp
            )
            LazyColumn {
                items(filtered, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        pinned = pinnedSet.contains(app.packageName),
                        onClick = { DataRepository.togglePinnedApp(app.packageName) }
                    )
                }
            }
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
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onBack() }
            )
        }
    }
}

@Composable
private fun AppRow(app: AppEntry, pinned: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bmp = remember(app.packageName) { app.icon.toBitmap(128, 128).asImageBitmap() }
        Image(
            bitmap = bmp,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = app.label, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(text = app.packageName, fontSize = 12.sp, color = IosGroupLabel)
        }
        Checkbox(
            checked = pinned,
            onCheckedChange = null,
            colors = androidx.compose.material.CheckboxDefaults.colors(checkedColor = IosBlue)
        )
    }
}
