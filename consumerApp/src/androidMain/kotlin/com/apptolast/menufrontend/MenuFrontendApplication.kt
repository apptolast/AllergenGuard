package com.apptolast.menufrontend

import android.app.Application
import com.apptolast.menufrontend.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MenuFrontendApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@MenuFrontendApplication)
        }
    }
}
