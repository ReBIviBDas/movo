package it.movo.app

import androidx.compose.ui.window.ComposeUIViewController
import it.movo.app.di.appModule
import it.movo.app.di.platformModule
import org.koin.core.context.startKoin

fun initKoinIos() {
    startKoin {
        modules(appModule, platformModule)
    }
}

fun MainViewController() = ComposeUIViewController { MovoApp() }
