package com.edgeside.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.edgeside.app.data.entity.PanelEdge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "edgeside_prefs")

data class UserPreferences(
    val showNotificationsCard: Boolean = true,
    val showBatteryCard: Boolean = true,
    val showNetworkCard: Boolean = true,
    val showClipboardCard: Boolean = true,
    val barEdge: PanelEdge = PanelEdge.RIGHT,
    val barVerticalPosPx: Int = 0,
    val barHeightDp: Int = 100
)

class PreferencesStore(private val context: Context) {

    private object Keys {
        val SHOW_NOTIFICATIONS = booleanPreferencesKey("show_notifications")
        val SHOW_BATTERY = booleanPreferencesKey("show_battery")
        val SHOW_NETWORK = booleanPreferencesKey("show_network")
        val SHOW_CLIPBOARD = booleanPreferencesKey("show_clipboard")
        val BAR_EDGE = stringPreferencesKey("bar_edge")
        val BAR_VERTICAL_POS = intPreferencesKey("bar_vertical_pos")
        val BAR_HEIGHT_DP = intPreferencesKey("bar_height_dp")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            showNotificationsCard = prefs[Keys.SHOW_NOTIFICATIONS] ?: true,
            showBatteryCard = prefs[Keys.SHOW_BATTERY] ?: true,
            showNetworkCard = prefs[Keys.SHOW_NETWORK] ?: true,
            showClipboardCard = prefs[Keys.SHOW_CLIPBOARD] ?: true,
            barEdge = PanelEdge.fromKey(prefs[Keys.BAR_EDGE]),
            barVerticalPosPx = prefs[Keys.BAR_VERTICAL_POS] ?: 0,
            barHeightDp = prefs[Keys.BAR_HEIGHT_DP] ?: 100
        )
    }

    suspend fun updateCard(key: String, enabled: Boolean) {
        val prefKey = when (key) {
            "notifications" -> Keys.SHOW_NOTIFICATIONS
            "battery" -> Keys.SHOW_BATTERY
            "network" -> Keys.SHOW_NETWORK
            "clipboard" -> Keys.SHOW_CLIPBOARD
            else -> return
        }
        context.dataStore.edit { it[prefKey] = enabled }
    }

    suspend fun setBarEdge(edge: PanelEdge) {
        context.dataStore.edit { it[Keys.BAR_EDGE] = edge.key }
    }

    suspend fun setBarVerticalPos(pos: Int) {
        context.dataStore.edit { it[Keys.BAR_VERTICAL_POS] = pos }
    }

    suspend fun setBarHeightDp(height: Int) {
        context.dataStore.edit { it[Keys.BAR_HEIGHT_DP] = height.coerceIn(60, 180) }
    }
}
