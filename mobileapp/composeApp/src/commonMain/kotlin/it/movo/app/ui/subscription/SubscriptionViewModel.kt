package it.movo.app.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.SubscriptionPlan
import it.movo.app.data.model.UserSubscription
import it.movo.app.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val plans: List<SubscriptionPlan> = emptyList(),
    val activeSubscription: UserSubscription? = null,
    val isLoading: Boolean = false,
    val isSubscribing: Boolean = false,
    val errorMessage: String? = null,
    val subscribeSuccess: Boolean = false,
    val cancelSuccess: Boolean = false
)

class SubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadPlans()
        loadActiveSubscription()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            subscriptionRepository.getPlans()
                .onSuccess { plans ->
                    _uiState.update { it.copy(plans = plans, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun loadActiveSubscription() {
        viewModelScope.launch {
            subscriptionRepository.getActiveSubscription()
                .onSuccess { sub ->
                    _uiState.update { it.copy(activeSubscription = sub) }
                }
                .onFailure {
                    _uiState.update { it.copy(activeSubscription = null) }
                }
        }
    }

    fun subscribe(planId: String, autoRenew: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubscribing = true, errorMessage = null) }
            subscriptionRepository.subscribe(planId, autoRenew)
                .onSuccess { sub ->
                    _uiState.update {
                        it.copy(
                            activeSubscription = sub,
                            isSubscribing = false,
                            subscribeSuccess = true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubscribing = false, errorMessage = e.message) }
                }
        }
    }

    fun toggleAutoRenew() {
        val sub = _uiState.value.activeSubscription ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            subscriptionRepository.toggleAutoRenew(sub.id, !sub.autoRenew)
                .onSuccess { updated ->
                    _uiState.update { it.copy(activeSubscription = updated, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun cancelSubscription() {
        val sub = _uiState.value.activeSubscription ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            subscriptionRepository.cancelSubscription(sub.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            activeSubscription = null,
                            isLoading = false,
                            cancelSuccess = true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSubscribeSuccess() {
        _uiState.update { it.copy(subscribeSuccess = false) }
    }

    fun clearCancelSuccess() {
        _uiState.update { it.copy(cancelSuccess = false) }
    }
}
