package com.edgeside.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.edgeside.app.data.entity.BarConfig
import com.edgeside.app.data.entity.PanelConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {
    // PanelConfig
    @Query("SELECT * FROM panel_config WHERE id = 1")
    fun observePanelConfig(): Flow<PanelConfig?>

    @Query("SELECT * FROM panel_config WHERE id = 1")
    suspend fun getPanelConfig(): PanelConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPanelConfig(config: PanelConfig)

    // BarConfig
    @Query("SELECT * FROM bar_config WHERE id = 1")
    fun observeBarConfig(): Flow<BarConfig?>

    @Query("SELECT * FROM bar_config WHERE id = 1")
    suspend fun getBarConfig(): BarConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBarConfig(config: BarConfig)
}
