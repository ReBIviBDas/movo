package it.movo.app.di

import it.movo.app.platform.IosImagePicker
import it.movo.app.platform.IosLocationProvider
import it.movo.app.platform.ImagePicker
import it.movo.app.platform.LocationProvider
import org.koin.dsl.module

actual val platformModule = module {
    single<LocationProvider> { IosLocationProvider() }
    single<ImagePicker> { IosImagePicker() }
}
