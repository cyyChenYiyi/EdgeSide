package com.edgeside.app

import android.app.Application
import com.edgeside.app.data.AppContainer
import timber.log.Timber

class EdgeSideApp : Application() {

    /** 手动实现的简易 DI 容器。V1 规模够用，V2 再考虑 Hilt。 */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        container = AppContainer(this)
        Timber.i("EdgeSide app created")
    }
}
