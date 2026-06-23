package com.apptolast.menufrontend

import android.app.Application
import com.apptolast.menufrontend.di.initKoin
import org.apptolast.menuadmin.data.remote.firebase.FirebaseConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MenuFrontendApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Select the Firestore database by build type: debug builds use the `debug` database, release
        // builds the production `(default)`. Must run before initKoin (before any Firestore call).
        FirebaseConfig.databaseId = if (BuildConfig.DEBUG) "debug" else "(default)"
        initKoin {
            androidLogger()
            androidContext(this@MenuFrontendApplication)
        }
    }
}
