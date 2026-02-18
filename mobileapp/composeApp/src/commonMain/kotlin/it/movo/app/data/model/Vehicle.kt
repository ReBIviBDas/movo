package it.movo.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoPoint(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList(),
    val lat: Double? = null,
    val lng: Double? = null
) {
    val longitude: Double get() = lng ?: coordinates.getOrElse(0) { 0.0 }
    val latitude: Double get() = lat ?: coordinates.getOrElse(1) { 0.0 }
}

@Serializable
data class GeoPolygon(
    val type: String = "Polygon",
    val coordinates: List<List<List<Double>>> = emptyList()
)

@Serializable
data class VehicleMapItem(
    val id: String,
    val model: String,
    @SerialName("plate") val licensePlate: String? = null,
    val location: GeoPoint,
    @SerialName("battery_level") val batteryLevel: Int = 0,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    @SerialName("price_per_minute") val basePricePerMinute: Double = 0.0
)

@Serializable
data class Vehicle(
    val id: String,
    val model: String,
    @SerialName("plate") val licensePlate: String? = null,
    val location: GeoPoint,
    @SerialName("battery_level") val batteryLevel: Int,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    @SerialName("price_per_minute") val basePricePerMinute: Double = 0.0,
    val year: Int? = null,
    val color: String? = null,
    val features: List<String> = emptyList(),
    @SerialName("range_km") val rangeKm: Int? = null,
    @SerialName("last_maintenance_at") val lastMaintenanceAt: String? = null
)

@Serializable
data class VehicleSearchResult(
    val id: String,
    val model: String,
    @SerialName("plate") val licensePlate: String? = null,
    val location: GeoPoint,
    @SerialName("battery_level") val batteryLevel: Int,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    @SerialName("price_per_minute") val basePricePerMinute: Double = 0.0,
    val year: Int? = null,
    val color: String? = null,
    val features: List<String> = emptyList(),
    @SerialName("range_km") val rangeKm: Int? = null,
    @SerialName("distance_meters") val distanceMeters: Int = 0
)

@Serializable
enum class VehicleStatus {
    @SerialName("available")
    AVAILABLE,
    @SerialName("booked")
    BOOKED,
    @SerialName("rented")
    RENTED,
    @SerialName("maintenance")
    MAINTENANCE,
    @SerialName("charging")
    CHARGING
}

@Serializable
data class VehicleSummary(
    val id: String? = null,
    val plate: String? = null,
    val model: String? = null,
    val type: String? = null,
    @SerialName("battery_level") val batteryLevel: Int? = null
)

@Serializable
data class VehiclesResponse(
    val vehicles: List<VehicleMapItem>,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class VehicleSearchResponse(
    val data: List<VehicleSearchResult>,
    val pagination: CursorPagination? = null
)
