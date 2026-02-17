package it.movo.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.NotificationChannel
import it.movo.app.data.model.NotificationChannelType
import it.movo.app.data.model.NotificationPreferences
import it.movo.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val promotionalPush: Boolean = false,
    val promotionalEmail: Boolean = false,
    val promotionalSms: Boolean = false,
    val transactionalPush: Boolean = true,
    val transactionalEmail: Boolean = true,
    val transactionalSms: Boolean = false,
    val bookingReminders: Boolean = true,
    val rentalAlerts: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)

class NotificationSettingsViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getNotificationPreferences()
                .onSuccess { prefs ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            promotionalPush = prefs.promotional.channels.contains(
                                NotificationChannelType.PUSH
                            ),
                            promotionalEmail = prefs.promotional.channels.contains(
                                NotificationChannelType.EMAIL
                            ),
                            promotionalSms = prefs.promotional.channels.contains(
                                NotificationChannelType.SMS
                            ),
                            transactionalPush = prefs.transactional.channels.contains(
                                NotificationChannelType.PUSH
                            ),
                            transactionalEmail = prefs.transactional.channels.contains(
                                NotificationChannelType.EMAIL
                            ),
                            transactionalSms = prefs.transactional.channels.contains(
                                NotificationChannelType.SMS
                            ),
                            bookingReminders = prefs.bookingReminders,
                            rentalAlerts = prefs.rentalAlerts
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun onPromotionalPushToggle(enabled: Boolean) {
        _uiState.update { it.copy(promotionalPush = enabled, saved = false) }
    }

    fun onPromotionalEmailToggle(enabled: Boolean) {
        _uiState.update { it.copy(promotionalEmail = enabled, saved = false) }
    }

    fun onPromotionalSmsToggle(enabled: Boolean) {
        _uiState.update { it.copy(promotionalSms = enabled, saved = false) }
    }

    fun onTransactionalPushToggle(enabled: Boolean) {
        _uiState.update { it.copy(transactionalPush = enabled, saved = false) }
    }

    fun onTransactionalEmailToggle(enabled: Boolean) {
        _uiState.update { it.copy(transactionalEmail = enabled, saved = false) }
    }

    fun onTransactionalSmsToggle(enabled: Boolean) {
        _uiState.update { it.copy(transactionalSms = enabled, saved = false) }
    }

    fun onBookingRemindersToggle(enabled: Boolean) {
        _uiState.update { it.copy(bookingReminders = enabled, saved = false) }
    }

    fun onRentalAlertsToggle(enabled: Boolean) {
        _uiState.update { it.copy(rentalAlerts = enabled, saved = false) }
    }

    fun savePreferences() {
        val state = _uiState.value
        val promotionalChannels = buildList {
            if (state.promotionalPush) add(NotificationChannelType.PUSH)
            if (state.promotionalEmail) add(NotificationChannelType.EMAIL)
            if (state.promotionalSms) add(NotificationChannelType.SMS)
        }
        val transactionalChannels = buildList {
            if (state.transactionalPush) add(NotificationChannelType.PUSH)
            if (state.transactionalEmail) add(NotificationChannelType.EMAIL)
            if (state.transactionalSms) add(NotificationChannelType.SMS)
        }

        val prefs = NotificationPreferences(
            promotional = NotificationChannel(
                enabled = promotionalChannels.isNotEmpty(),
                channels = promotionalChannels
            ),
            transactional = NotificationChannel(
                enabled = transactionalChannels.isNotEmpty(),
                channels = transactionalChannels
            ),
            bookingReminders = state.bookingReminders,
            rentalAlerts = state.rentalAlerts
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            userRepository.updateNotificationPreferences(prefs)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, saved = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }
        }
    }
}
