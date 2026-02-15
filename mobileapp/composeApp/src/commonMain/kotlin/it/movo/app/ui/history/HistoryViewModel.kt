package it.movo.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.RentalSummary
import it.movo.app.data.repository.RentalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val rentals: List<RentalSummary> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HistoryViewModel(private val rentalRepository: RentalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = rentalRepository.getRentalHistory(
                from = null,
                to = null,
                cursor = null,
                limit = 50
            )
            
            result.fold(
                onSuccess = { rentals ->
                    _uiState.update { 
                        it.copy(
                            rentals = rentals,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load history"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
