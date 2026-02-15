package it.movo.app.platform

import it.movo.app.data.model.GeoPoint

interface LocationProvider {
    suspend fun getCurrentLocation(): GeoPoint?
}
