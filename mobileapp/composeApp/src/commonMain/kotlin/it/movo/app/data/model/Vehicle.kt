package it.movo.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoPoint(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList()
) {
    val longitude: Double get() = coordinates.getOrElse(0) { 0.0 }
    val latitude: Double get() = coordinates.getOrElse(1) { 0.0 }
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
    @SerialName("license_plate") val licensePlate: String,
    val location: GeoPoint,
    @SerialName("battery_level") val batteryLevel: Int,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    @SerialName("base_price_per_minute") val basePricePerMinute: Int = 0
)

@Serializable
data class Vehicle(
    val id: String,
    val model: String,
    @SerialName("license_plate") val licensePlate: String,
    val location: GeoPoint,
    @SerialName("battery_level") val batteryLevel: Int,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    @SerialName("base_price_per_minute") val basePricePerMinute: Int = 0,
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
    @SerialName("license_plate") val licensePlate: String,
    val location: GeoPoint,
    @SerialName("battery_level") val batteryLevel: Int,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    @SerialName("base_price_per_minute") val basePricePerMinute: Int = 0,
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
    @SerialName("reserved")
    RESERVED,
    @SerialName("in_use")
    IN_USE
}

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
