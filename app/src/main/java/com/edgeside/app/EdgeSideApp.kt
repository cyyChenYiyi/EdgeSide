package com.edgeside.app

import android.app.Application
import com.edgeside.app.data.DataRepository
import timber.log.Timber

class EdgeSideApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        Timber.plant(Timber.DebugTree())
        DataRepository.init(this)
        Timber.d("EdgeSideApp created")
    }

    companion object {
        lateinit var instance: EdgeSideApp
            private set
    }
}
