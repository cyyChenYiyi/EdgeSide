package com.edgeside.app.data

import android.content.Context
import com.edgeside.app.data.entity.PinnedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object DataRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var db: AppDatabase
    private lateinit var prefs: PreferencesStore
    private lateinit var appContext: Context

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            appContext = context.applicationContext
            db = AppDatabase.getInstance(appContext)
            prefs = PreferencesStore(appContext)
            initialized = true
        }
    }

    private fun ensureInit() {
        check(initialized) { "DataRepository.init(context) must be called first" }
    }

    val pinnedAppsFlow: Flow<List<PinnedApp>>
        get() {
            ensureInit()
            return db.pinnedAppDao().observeAll()
                .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    val preferencesFlow: Flow<UserPreferences>
        get() {
            ensureInit()
            return prefs.preferencesFlow
        }

    fun togglePinnedApp(packageName: String) {
        ensureInit()
        scope.launch {
            val dao = db.pinnedAppDao()
            val existing = dao.getAll().find { it.package_name == packageName }
            if (existing != null) {
                dao.deleteByPackage(packageName)
            } else {
                val order = dao.getMaxSortOrder() + 1
                dao.insert(
                    PinnedApp(
                        package_name = packageName,
                        sort_order = order,
                        added_at = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun pinApp(packageName: String) {
        ensureInit()
        scope.launch {
            val dao = db.pinnedAppDao()
            val existing = dao.getAll().find { it.package_name == packageName }
            if (existing == null) {
                val order = dao.getMaxSortOrder() + 1
                dao.insert(
                    PinnedApp(
                        package_name = packageName,
                        sort_order = order,
                        added_at = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun unpinApp(packageName: String) {
        ensureInit()
        scope.launch {
            db.pinnedAppDao().deleteByPackage(packageName)
        }
    }

    fun setCard(key: String, enabled: Boolean) {
        ensureInit()
        scope.launch { prefs.updateCard(key, enabled) }
    }

    fun setBarEdge(edge: com.edgeside.app.data.entity.PanelEdge) {
        ensureInit()
        scope.launch { prefs.setBarEdge(edge) }
    }

    fun setBarVerticalPos(pos: Int) {
        ensureInit()
        scope.launch { prefs.setBarVerticalPos(pos) }
    }

    fun setBarHeightDp(height: Int) {
        ensureInit()
        scope.launch { prefs.setBarHeightDp(height) }
    }

    fun isPinned(packageName: String, callback: (Boolean) -> Unit) {
        ensureInit()
        scope.launch {
            val result = db.pinnedAppDao().getAll().any { it.package_name == packageName }
            withContextMain { callback(result) }
        }
    }

    private fun withContextMain(block: () -> Unit) {
        appContext.mainExecutor.execute(block)
    }
}
