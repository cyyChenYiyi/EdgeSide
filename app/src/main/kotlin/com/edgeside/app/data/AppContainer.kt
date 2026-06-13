package com.edgeside.app.data

import android.content.Context
import com.edgeside.app.data.entity.BarConfig
import com.edgeside.app.data.entity.PanelConfig
import com.edgeside.app.data.entity.PinnedApp
import kotlinx.coroutines.flow.Flow

/**
 * 简易 DI 容器，承载 Repository、Database、DataStore。
 *
 * V1 规模下手动持有引用，避免 Hilt 的注解编译开销。
 */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.get(context)
    val pinnedAppRepository = PinnedAppRepository(database.pinnedAppDao())
    val configRepository = ConfigRepository(database.configDao())
    val preferencesStore = PreferencesStore(context)
}

class PinnedAppRepository(private val dao: com.edgeside.app.data.dao.PinnedAppDao) {
    fun observeAll(): Flow<List<PinnedApp>> = dao.observeAll()
    suspend fun listAll(): List<PinnedApp> = dao.listAll()
    suspend fun pin(packageName: String) {
        val count = dao.count()
        dao.insert(
            PinnedApp(
                packageName = packageName,
                sortOrder = count,
                addedAt = System.currentTimeMillis()
            )
        )
    }
    suspend fun unpin(packageName: String) = dao.deleteByPackage(packageName)
}

class ConfigRepository(private val dao: com.edgeside.app.data.dao.ConfigDao) {
    fun observePanelConfig(): Flow<PanelConfig?> = dao.observePanelConfig()
    suspend fun getPanelConfig(): PanelConfig = dao.getPanelConfig() ?: PanelConfig().also { dao.upsertPanelConfig(it) }
    suspend fun upsertPanelConfig(config: PanelConfig) = dao.upsertPanelConfig(config)

    fun observeBarConfig(): Flow<BarConfig?> = dao.observeBarConfig()
    suspend fun getBarConfig(): BarConfig = dao.getBarConfig() ?: BarConfig().also { dao.upsertBarConfig(it) }
    suspend fun upsertBarConfig(config: BarConfig) = dao.upsertBarConfig(config)
}
