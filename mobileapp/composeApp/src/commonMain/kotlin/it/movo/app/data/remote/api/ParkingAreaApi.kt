package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import it.movo.app.data.model.ParkingArea
import it.movo.app.data.model.ParkingAreasResponse

class ParkingAreaApi(private val client: HttpClient) {
    suspend fun getParkingAreas(bounds: String? = null): ParkingAreasResponse =
        client.get("parking-areas") {
            bounds?.let { parameter("bounds", it) }
        }.body()

    suspend fun getParkingArea(id: String): ParkingArea =
        client.get("parking-areas/$id").body()
}
