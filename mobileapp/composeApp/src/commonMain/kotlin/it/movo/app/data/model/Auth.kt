package it.movo.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class GoogleLoginRequest(val code: String, @SerialName("redirect_uri") val redirectUri: String)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val user: AuthUser
)

@Serializable
data class AuthUser(
    val id: String,
    val email: String,
    val role: UserRole = UserRole.USER,
    val status: UserStatus = UserStatus.ACTIVE
)

@Serializable
enum class UserRole {
    @SerialName("user") USER,
    @SerialName("operator") OPERATOR,
    @SerialName("admin") ADMIN
}

@Serializable
enum class UserStatus {
    @SerialName("active") ACTIVE,
    @SerialName("pending_approval") PENDING_APPROVAL,
    @SerialName("suspended") SUSPENDED,
    @SerialName("blocked") BLOCKED
}

@Serializable
data class RefreshTokenRequest(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class PasswordResetRequest(
    val email: String,
    @SerialName("captcha_token") val captchaToken: String? = null
)

@Serializable
data class PasswordResetConfirm(val token: String, @SerialName("new_password") val newPassword: String)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val fiscalCode: String,
    val phone: String,
    val address: String? = null,
    val acceptTerms: Boolean = true,
    val acceptPrivacy: Boolean = true,
    val acceptCookies: Boolean = false,
    val drivingLicense: ByteArray? = null,
    val identityDocument: ByteArray? = null
)

@Serializable
data class RegisterResponse(
    @SerialName("user_id") val userId: String,
    val email: String,
    val status: RegistrationStatus,
    val message: String
)

@Serializable
enum class RegistrationStatus {
    @SerialName("active") ACTIVE,
    @SerialName("pending_approval") PENDING_APPROVAL
}

@Serializable
data class DocumentUploadResponse(
    val status: RegistrationStatus,
    val message: String
)
