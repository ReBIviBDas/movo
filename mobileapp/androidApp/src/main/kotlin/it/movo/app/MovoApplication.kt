package it.movo.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import it.movo.app.di.appModule
import it.movo.app.di.platformModule

class MovoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MovoApplication)
            modules(appModule, platformModule)
        }
    }
}
