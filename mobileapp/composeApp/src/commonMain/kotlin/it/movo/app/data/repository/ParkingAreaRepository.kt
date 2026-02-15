package it.movo.app.data.repository

import it.movo.app.data.model.ParkingArea
import it.movo.app.data.remote.api.ParkingAreaApi

class ParkingAreaRepository(private val parkingAreaApi: ParkingAreaApi) {
    suspend fun getParkingAreas(bounds: String? = null): Result<List<ParkingArea>> = runCatching {
        parkingAreaApi.getParkingAreas(bounds).parkingAreas
    }

    suspend fun getParkingArea(id: String): Result<ParkingArea> = runCatching {
        parkingAreaApi.getParkingArea(id)
    }
}
