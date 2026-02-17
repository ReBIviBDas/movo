package it.movo.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnlockRequest(
    @SerialName("booking_id") val bookingId: String,
    @SerialName("user_location") val userLocation: GeoPoint? = null
)

@Serializable
data class Rental(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("vehicle_id") val vehicleId: String,
    val vehicle: VehicleMapItem? = null,
    val status: RentalStatus = RentalStatus.ACTIVE,
    @SerialName("started_at") val startedAt: String,
    @SerialName("start_location") val startLocation: GeoPoint? = null,
    @SerialName("current_cost_cents") val currentCostCents: Int = 0,
    @SerialName("paused_at") val pausedAt: String? = null
)

@Serializable
data class RentalSummary(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("vehicle_id") val vehicleId: String,
    val vehicle: VehicleMapItem? = null,
    val status: RentalStatus = RentalStatus.COMPLETED,
    @SerialName("started_at") val startedAt: String,
    @SerialName("start_location") val startLocation: GeoPoint? = null,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("end_location") val endLocation: GeoPoint? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("distance_km") val distanceKm: Double? = null,
    @SerialName("total_cost_cents") val totalCostCents: Int = 0,
    @SerialName("discount_applied_cents") val discountAppliedCents: Int = 0,
    @SerialName("final_cost_cents") val finalCostCents: Int = 0
)

@Serializable
enum class RentalStatus {
    @SerialName("active")
    ACTIVE,
    @SerialName("paused")
    PAUSED,
    @SerialName("completed")
    COMPLETED
}

@Serializable
data class EndRentalRequest(
    @SerialName("end_location") val endLocation: GeoPoint? = null,
    val photos: List<String> = emptyList()
)

@Serializable
data class RentalHistoryResponse(
    val data: List<RentalSummary>,
    val pagination: CursorPagination? = null
)
