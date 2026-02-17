package it.movo.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.NotificationChannel
import it.movo.app.data.model.NotificationChannelType
import it.movo.app.data.model.NotificationPreferences
import it.movo.app.data.model.UserProfileUpdate
import it.movo.app.data.repository.AuthRepository
import it.movo.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val memberSince: String = "",
    val licenseVerified: Boolean = false,
    val licenseExpiry: String = "",
    val pushNotificationsEnabled: Boolean = false,
    val emailUpdatesEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val logoutSuccess: Boolean = false,
    val profileSaved: Boolean = false,
    val address: String = "",
    val dateOfBirth: String = "",
    val fiscalCode: String = "",
    val isExporting: Boolean = false
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val profileResult = userRepository.getProfile()
            val prefsResult = userRepository.getNotificationPreferences()

            profileResult
                .onSuccess { profile ->
                    val fullName = "${profile.firstName} ${profile.lastName}"
                    val memberSince = profile.createdAt.take(4)
                    _uiState.update { state ->
                        state.copy(
                            fullName = fullName,
                            email = profile.email,
                            phone = profile.phone ?: "",
                            memberSince = memberSince,
                            licenseVerified = profile.drivingLicenseVerified,
                            licenseExpiry = profile.drivingLicenseExpiry ?: "",
                            isLoading = false,
                            address = profile.address ?: "",
                            dateOfBirth = profile.dateOfBirth ?: "",
                            fiscalCode = profile.fiscalCode ?: ""
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }

            prefsResult
                .onSuccess { prefs ->
                    _uiState.update { state ->
                        state.copy(
                            pushNotificationsEnabled = prefs.transactional.channels.contains(
                                NotificationChannelType.PUSH
                            ),
                            emailUpdatesEnabled = prefs.promotional.channels.contains(
                                NotificationChannelType.EMAIL
                            )
                        )
                    }
                }
        }
    }

    fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onPushNotificationsToggle(enabled: Boolean) {
        _uiState.update { it.copy(pushNotificationsEnabled = enabled) }
    }

    fun onEmailUpdatesToggle(enabled: Boolean) {
        _uiState.update { it.copy(emailUpdatesEnabled = enabled) }
    }

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val nameParts = _uiState.value.fullName.split(" ", limit = 2)
            val firstName = nameParts.getOrNull(0) ?: ""
            val lastName = nameParts.getOrNull(1) ?: ""

            val profileUpdate = UserProfileUpdate(
                firstName = firstName,
                lastName = lastName,
                phone = _uiState.value.phone,
                address = _uiState.value.address.ifBlank { null }
            )

            val profileResult = userRepository.updateProfile(profileUpdate)

            val notificationPrefs = NotificationPreferences(
                transactional = NotificationChannel(
                    enabled = _uiState.value.pushNotificationsEnabled,
                    channels = if (_uiState.value.pushNotificationsEnabled) listOf(
                        NotificationChannelType.PUSH
                    ) else emptyList()
                ),
                promotional = NotificationChannel(
                    enabled = _uiState.value.emailUpdatesEnabled,
                    channels = if (_uiState.value.emailUpdatesEnabled) listOf(
                        NotificationChannelType.EMAIL
                    ) else emptyList()
                )
            )

            val prefsResult = userRepository.updateNotificationPreferences(notificationPrefs)

            if (profileResult.isSuccess && prefsResult.isSuccess) {
                _uiState.update { it.copy(isSaving = false, profileSaved = true) }
            } else {
                val error = profileResult.exceptionOrNull()?.message
                    ?: prefsResult.exceptionOrNull()?.message
                _uiState.update { it.copy(isSaving = false, errorMessage = error) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess { _uiState.update { it.copy(logoutSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun logoutAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.logoutAll()
                .onSuccess { _uiState.update { it.copy(isLoading = false, logoutSuccess = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            userRepository.exportData()
                .onSuccess { _uiState.update { it.copy(isExporting = false, profileSaved = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            userRepository.updatePassword(currentPassword, newPassword)
                .onSuccess { _uiState.update { it.copy(isSaving = false, profileSaved = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.deleteAccount(password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, logoutSuccess = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}
