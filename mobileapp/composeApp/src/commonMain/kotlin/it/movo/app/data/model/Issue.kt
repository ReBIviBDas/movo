package it.movo.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Issue(
    val id: String,
    @SerialName("vehicle_id") val vehicleId: String,
    @SerialName("rental_id") val rentalId: String? = null,
    val category: IssueCategory,
    val description: String,
    val photos: List<String> = emptyList(),
    val status: IssueStatus = IssueStatus.OPEN,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("resolution_notes") val resolutionNotes: String? = null
)

@Serializable
enum class IssueCategory {
    @SerialName("vehicle_damage")
    VEHICLE_DAMAGE,
    @SerialName("vehicle_malfunction")
    VEHICLE_MALFUNCTION,
    @SerialName("accident")
    ACCIDENT,
    @SerialName("parking_issue")
    PARKING_ISSUE,
    @SerialName("app_issue")
    APP_ISSUE,
    @SerialName("payment_issue")
    PAYMENT_ISSUE,
    @SerialName("other")
    OTHER
}

@Serializable
enum class IssueStatus {
    @SerialName("open")
    OPEN,
    @SerialName("in_progress")
    IN_PROGRESS,
    @SerialName("resolved")
    RESOLVED,
    @SerialName("closed")
    CLOSED
}

@Serializable
data class IssuesResponse(
    val data: List<Issue>,
    val pagination: CursorPagination? = null
)

data class CreateIssueRequest(
    val vehicleId: String,
    val rentalId: String? = null,
    val category: IssueCategory,
    val description: String,
    val photos: List<ByteArray> = emptyList(),
    val gpsLocation: String? = null
)
