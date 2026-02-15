package it.movo.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Language {
    @SerialName("it") IT,
    @SerialName("en") EN,
    @SerialName("de") DE
}

@Serializable
enum class NotificationChannelType {
    @SerialName("push") PUSH,
    @SerialName("email") EMAIL,
    @SerialName("sms") SMS
}

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("fiscal_code") val fiscalCode: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val language: Language = Language.IT,
    val role: UserRole = UserRole.USER,
    val status: UserStatus = UserStatus.ACTIVE,
    @SerialName("driving_enabled") val drivingEnabled: Boolean = false,
    @SerialName("driving_license_verified") val drivingLicenseVerified: Boolean = false,
    @SerialName("driving_license_expiry") val drivingLicenseExpiry: String? = null,
    @SerialName("identity_document_expiry") val identityDocumentExpiry: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class UserProfileUpdate(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val language: Language? = null
)

@Serializable
data class PasswordUpdate(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String
)

@Serializable
data class DeleteAccountRequest(
    val password: String,
    val confirmation: String = "ELIMINA IL MIO ACCOUNT"
)

@Serializable
data class NotificationPreferences(
    val promotional: NotificationChannel = NotificationChannel(),
    val transactional: NotificationChannel = NotificationChannel(),
    @SerialName("booking_reminders") val bookingReminders: Boolean = true,
    @SerialName("rental_alerts") val rentalAlerts: Boolean = true
)

@Serializable
data class NotificationChannel(
    val enabled: Boolean = true,
    val channels: List<NotificationChannelType> = listOf(NotificationChannelType.PUSH)
)

@Serializable
data class DataExportResponse(
    val user: UserProfile,
    val rentals: List<it.movo.app.data.model.Rental>,
    val payments: List<it.movo.app.data.model.Payment>,
    @SerialName("exported_at") val exportedAt: String
)
