package it.movo.app.data.auth

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenManager(private val settings: Settings) {
    private val _isLoggedIn = MutableStateFlow(settings.getStringOrNull(KEY_ACCESS_TOKEN) != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val accessToken: String? get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
    val refreshToken: String? get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)

    fun saveTokens(accessToken: String, refreshToken: String?) {
        settings[KEY_ACCESS_TOKEN] = accessToken
        if (refreshToken != null) {
            settings[KEY_REFRESH_TOKEN] = refreshToken
        }
        _isLoggedIn.value = true
    }

    fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        _isLoggedIn.value = false
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
