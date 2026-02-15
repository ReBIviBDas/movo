package it.movo.app.ui.promotion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.Promotion
import it.movo.app.data.repository.PromotionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PromotionUiState(
    val promotions: List<Promotion> = emptyList(),
    val promoCode: String = "",
    val isLoading: Boolean = false,
    val isApplying: Boolean = false,
    val errorMessage: String? = null,
    val appliedPromotion: Promotion? = null
)

class PromotionViewModel(
    private val promotionRepository: PromotionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PromotionUiState())
    val uiState: StateFlow<PromotionUiState> = _uiState.asStateFlow()

    init {
        loadPromotions()
    }

    fun loadPromotions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            promotionRepository.getPromotions()
                .onSuccess { promotions ->
                    _uiState.update { it.copy(promotions = promotions, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun onPromoCodeChange(code: String) {
        _uiState.update { it.copy(promoCode = code, errorMessage = null) }
    }

    fun applyPromoCode() {
        val code = _uiState.value.promoCode.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a promo code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true, errorMessage = null) }
            promotionRepository.applyPromoCode(code)
                .onSuccess { promotion ->
                    _uiState.update {
                        it.copy(
                            isApplying = false,
                            appliedPromotion = promotion,
                            promoCode = ""
                        )
                    }
                    loadPromotions()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isApplying = false, errorMessage = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearAppliedPromotion() {
        _uiState.update { it.copy(appliedPromotion = null) }
    }

}
