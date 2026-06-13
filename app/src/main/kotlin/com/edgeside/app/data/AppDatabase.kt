package com.edgeside.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.edgeside.app.data.dao.ConfigDao
import com.edgeside.app.data.dao.PinnedAppDao
import com.edgeside.app.data.entity.BarConfig
import com.edgeside.app.data.entity.PanelConfig
import com.edgeside.app.data.entity.PinnedApp

@Database(
    entities = [PinnedApp::class, PanelConfig::class, BarConfig::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinnedAppDao(): PinnedAppDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "edgeside.db"
            )
                .fallbackToDestructiveMigration() // V1 阶段简化
                .build()
                .also { INSTANCE = it }
        }
    }
}
