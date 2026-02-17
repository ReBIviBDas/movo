package it.movo.app.data.repository

import it.movo.app.data.model.Vehicle
import it.movo.app.data.model.VehicleMapItem
import it.movo.app.data.model.VehicleSearchResult
import it.movo.app.data.remote.api.VehicleApi

class VehicleRepository(private val vehicleApi: VehicleApi) {
    suspend fun getVehiclesOnMap(
        bounds: String? = null,
        minBattery: Int? = null
    ): Result<List<VehicleMapItem>> = runCatching {
        vehicleApi.getVehiclesOnMap(bounds, minBattery).vehicles
    }

    suspend fun getVehicle(id: String): Result<Vehicle> = runCatching {
        vehicleApi.getVehicle(id)
    }

    suspend fun searchVehicles(
        location: String? = null,
        maxDistance: Int? = null,
        minBattery: Int? = null,
        model: String? = null,
        maxPricePerMinute: Int? = null,
        cursor: String? = null,
        limit: Int? = null
    ): Result<List<VehicleSearchResult>> = runCatching {
        vehicleApi.searchVehicles(
            location,
            maxDistance,
            minBattery,
            model,
            maxPricePerMinute,
            cursor,
            limit
        ).data
    }
}
