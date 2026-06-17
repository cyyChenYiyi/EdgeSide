package com.edgeside.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.edgeside.app.data.entity.PinnedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedAppDao {

    @Query("SELECT * FROM pinned_apps ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<PinnedApp>>

    @Query("SELECT * FROM pinned_apps ORDER BY sort_order ASC")
    suspend fun getAll(): List<PinnedApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: PinnedApp)

    @Query("DELETE FROM pinned_apps WHERE package_name = :packageName")
    suspend fun deleteByPackage(packageName: String)

    @Query("DELETE FROM pinned_apps")
    suspend fun clearAll()

    @Update
    suspend fun update(apps: List<PinnedApp>)

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM pinned_apps")
    suspend fun getMaxSortOrder(): Int
}
