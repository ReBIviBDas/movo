package it.movo.app.data.repository

import it.movo.app.data.auth.TokenManager
import it.movo.app.data.model.AuthResponse
import it.movo.app.data.model.GoogleLoginRequest
import it.movo.app.data.model.LoginRequest
import it.movo.app.data.model.PasswordResetConfirm
import it.movo.app.data.model.PasswordResetRequest
import it.movo.app.data.model.RefreshTokenRequest
import it.movo.app.data.model.RegisterRequest
import it.movo.app.data.model.RegisterResponse
import it.movo.app.data.remote.api.AuthApi
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn

    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        val response = authApi.login(LoginRequest(email, password))
        response.refreshToken?.let { tokenManager.saveTokens(response.accessToken, it) }
        response
    }

    suspend fun loginWithGoogle(code: String, redirectUri: String): Result<AuthResponse> = runCatching {
        val response = authApi.loginWithGoogle(GoogleLoginRequest(code, redirectUri))
        response.refreshToken?.let { tokenManager.saveTokens(response.accessToken, it) }
        response
    }

    suspend fun logout(): Result<Unit> = runCatching {
        authApi.logout()
        tokenManager.clearTokens()
    }

    suspend fun logoutAll(): Result<Unit> = runCatching {
        authApi.logoutAll()
        tokenManager.clearTokens()
    }

    suspend fun refreshTokens(): Result<AuthResponse> = runCatching {
        val token = tokenManager.refreshToken ?: throw IllegalStateException("No refresh token available")
        val response = authApi.refreshToken(RefreshTokenRequest(token))
        response.refreshToken?.let { tokenManager.saveTokens(response.accessToken, it) }
        response
    }

    suspend fun requestPasswordReset(email: String): Result<Unit> = runCatching {
        authApi.requestPasswordReset(PasswordResetRequest(email))
    }

    suspend fun confirmPasswordReset(token: String, newPassword: String): Result<Unit> = runCatching {
        authApi.confirmPasswordReset(PasswordResetConfirm(token, newPassword))
    }

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> = runCatching {
        authApi.register(request)
    }
}
