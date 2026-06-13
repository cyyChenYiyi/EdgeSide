package com.edgeside.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.edgeside.app.data.entity.PinnedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedAppDao {
    @Query("SELECT * FROM pinned_apps ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<PinnedApp>>

    @Query("SELECT * FROM pinned_apps ORDER BY sortOrder ASC")
    suspend fun listAll(): List<PinnedApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: PinnedApp)

    @Query("DELETE FROM pinned_apps WHERE packageName = :pkg")
    suspend fun deleteByPackage(pkg: String)

    @Query("SELECT COUNT(*) FROM pinned_apps")
    suspend fun count(): Int
}
