package com.posopensrc

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PosApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PosApp
            private set
    }
}
