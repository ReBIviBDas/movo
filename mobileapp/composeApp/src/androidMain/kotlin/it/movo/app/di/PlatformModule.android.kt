package it.movo.app.di

import it.movo.app.platform.AndroidImagePicker
import it.movo.app.platform.AndroidLocationProvider
import it.movo.app.platform.ImagePicker
import it.movo.app.platform.LocationProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<LocationProvider> { AndroidLocationProvider(androidContext()) }
    single<ImagePicker> { AndroidImagePicker(androidContext()) }
}
