package it.movo.app.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import it.movo.app.data.model.AuthResponse
import it.movo.app.data.model.GoogleLoginRequest
import it.movo.app.data.model.LoginRequest
import it.movo.app.data.model.MessageResponse
import it.movo.app.data.model.PasswordResetConfirm
import it.movo.app.data.model.PasswordResetRequest
import it.movo.app.data.model.RefreshTokenRequest
import it.movo.app.data.model.RegisterRequest
import it.movo.app.data.model.RegisterResponse

class AuthApi(private val client: HttpClient) {
    suspend fun login(request: LoginRequest): AuthResponse =
        client.post("auth/login") { setBody(request) }.body()

    suspend fun loginWithGoogle(request: GoogleLoginRequest): AuthResponse =
        client.post("auth/login/google") { setBody(request) }.body()

    suspend fun logout(): Unit =
        client.post("auth/logout").body()

    suspend fun logoutAll(): Unit =
        client.post("auth/logout-all").body()

    suspend fun refreshToken(request: RefreshTokenRequest): AuthResponse =
        client.post("auth/refresh") { setBody(request) }.body()

    suspend fun requestPasswordReset(request: PasswordResetRequest): MessageResponse =
        client.post("auth/password-reset/request") { setBody(request) }.body()

    suspend fun confirmPasswordReset(request: PasswordResetConfirm): MessageResponse =
        client.post("auth/password-reset/confirm") { setBody(request) }.body()

    suspend fun register(request: RegisterRequest): RegisterResponse =
        client.submitFormWithBinaryData(
            url = "auth/register",
            formData = formData {
                append("email", request.email)
                append("password", request.password)
                append("first_name", request.firstName)
                append("last_name", request.lastName)
                append("date_of_birth", request.dateOfBirth)
                append("fiscal_code", request.fiscalCode)
                append("phone", request.phone)
                request.address?.let { append("address", it) }
                append("accept_terms", request.acceptTerms.toString())
                append("accept_privacy", request.acceptPrivacy.toString())
                append("accept_cookies", request.acceptCookies.toString())
                request.drivingLicense?.let {
                    append("driving_license", it, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"driving_license.jpg\"")
                    })
                }
                request.identityDocument?.let {
                    append("identity_document", it, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"identity_document.jpg\"")
                    })
                }
            }
        ).body()
}
