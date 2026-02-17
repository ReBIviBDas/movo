package it.movo.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBookingRequest(
    @SerialName("vehicle_id") val vehicleId: String,
    @SerialName("duration_minutes") val durationMinutes: Int = 15
)

@Serializable
data class Booking(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("vehicle_id") val vehicleId: String,
    val vehicle: VehicleMapItem? = null,
    @SerialName("duration_minutes") val durationMinutes: Int,
    val status: BookingStatus = BookingStatus.ACTIVE,
    @SerialName("created_at") val createdAt: String,
    @SerialName("expires_at") val expiresAt: String
)

@Serializable
enum class BookingStatus {
    @SerialName("active")
    ACTIVE,
    @SerialName("expired")
    EXPIRED,
    @SerialName("converted_to_rental")
    CONVERTED_TO_RENTAL
}
