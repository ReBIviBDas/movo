package it.movo.app.platform

import it.movo.app.data.model.GeoPoint
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLLocationAccuracyBest

class IosLocationProvider : LocationProvider {
    private val locationManager = CLLocationManager()

    override suspend fun getCurrentLocation(): GeoPoint? {
        val status = CLLocationManager.authorizationStatus()
        val authorized = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways

        if (!authorized) {
            locationManager.requestWhenInUseAuthorization()
            return null
        }

        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        val location = locationManager.location ?: return null

        return GeoPoint(
            coordinates = listOf(
                location.coordinate.longitude,
                location.coordinate.latitude
            )
        )
    }
}
