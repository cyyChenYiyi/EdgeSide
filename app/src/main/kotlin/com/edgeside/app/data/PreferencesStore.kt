package com.edgeside.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "edgeside_prefs")

/** 简单 KV 配置（开关/主题色等） */
class PreferencesStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme") // "system" / "light" / "dark"
        val SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
    }

    val theme: Flow<String> = context.dataStore.data.map { it[Keys.THEME] ?: "system" }
    val serviceEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.SERVICE_ENABLED] ?: false }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { it[Keys.THEME] = value }
    }

    suspend fun setServiceEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.SERVICE_ENABLED] = value }
    }
}
