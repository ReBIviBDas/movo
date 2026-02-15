package it.movo.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.PaymentMethod
import it.movo.app.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WalletUiState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddCardDialog: Boolean = false
)

class WalletViewModel(private val paymentRepository: PaymentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadPaymentMethods()
    }

    fun loadPaymentMethods() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            paymentRepository.getPaymentMethods()
                .onSuccess { methods ->
                    _uiState.update { it.copy(paymentMethods = methods, isLoading = false, errorMessage = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun setDefaultMethod(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            paymentRepository.setDefaultPaymentMethod(id)
                .onSuccess {
                    loadPaymentMethods()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun deleteMethod(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            paymentRepository.deletePaymentMethod(id)
                .onSuccess {
                    loadPaymentMethods()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun showAddCardDialog() {
        _uiState.update { it.copy(showAddCardDialog = true) }
    }

    fun dismissAddCardDialog() {
        _uiState.update { it.copy(showAddCardDialog = false) }
    }

    fun addCard(cardNumber: String, expiry: String, cvv: String) {
        if (cardNumber.isBlank() || expiry.isBlank() || cvv.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val stripeToken = createStripeToken(cardNumber, expiry, cvv)

            paymentRepository.addPaymentMethod(stripeToken, setAsDefault = false)
                .onSuccess { _ ->
                    loadPaymentMethods()
                    dismissAddCardDialog()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to add card") }
                }
        }
    }

    private fun createStripeToken(cardNumber: String, expiry: String, cvv: String): String {
        return "tok_${cardNumber.takeLast(4)}_${expiry.replace("/", "")}_${cvv.hashCode()}"
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
