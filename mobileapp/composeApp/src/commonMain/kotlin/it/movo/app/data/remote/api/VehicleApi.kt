package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import it.movo.app.data.model.Vehicle
import it.movo.app.data.model.VehicleSearchResponse
import it.movo.app.data.model.VehiclesResponse

class VehicleApi(private val client: HttpClient) {
    suspend fun getVehiclesOnMap(
        bounds: String? = null,
        minBattery: Int? = null
    ): VehiclesResponse =
        client.get("vehicles") {
            bounds?.let { parameter("bounds", it) }
            minBattery?.let { parameter("min_battery", it) }
        }.body()

    suspend fun getVehicle(id: String): Vehicle =
        client.get("vehicles/$id").body()

    suspend fun searchVehicles(
        location: String? = null,
        maxDistance: Int? = null,
        minBattery: Int? = null,
        model: String? = null,
        maxPricePerMinute: Int? = null,
        cursor: String? = null,
        limit: Int? = null
    ): VehicleSearchResponse =
        client.get("vehicles/search") {
            location?.let { parameter("location", it) }
            maxDistance?.let { parameter("max_distance", it) }
            minBattery?.let { parameter("min_battery", it) }
            model?.let { parameter("model", it) }
            maxPricePerMinute?.let { parameter("max_price_per_minute", it) }
            cursor?.let { parameter("cursor", it) }
            limit?.let { parameter("limit", it) }
        }.body()
}
