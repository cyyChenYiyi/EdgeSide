package com.edgeside.app

import android.app.Application
import com.edgeside.app.data.DataRepository
import timber.log.Timber

class EdgeSideApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        DataRepository.init(this)
        Timber.d("EdgeSideApp created")
    }
}
