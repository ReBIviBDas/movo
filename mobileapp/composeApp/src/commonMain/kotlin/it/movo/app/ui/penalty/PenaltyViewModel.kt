package it.movo.app.ui.penalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.Penalty
import it.movo.app.data.repository.PenaltyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PenaltyUiState(
    val penalties: List<Penalty> = emptyList(),
    val selectedPenalty: Penalty? = null,
    val contestReason: String = "",
    val isLoading: Boolean = false,
    val isContesting: Boolean = false,
    val errorMessage: String? = null,
    val contestSuccess: Boolean = false
)

class PenaltyViewModel(
    private val penaltyRepository: PenaltyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PenaltyUiState())
    val uiState: StateFlow<PenaltyUiState> = _uiState.asStateFlow()

    init {
        loadPenalties()
    }

    fun loadPenalties(status: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            penaltyRepository.getPenalties(status)
                .onSuccess { penalties ->
                    _uiState.update { it.copy(penalties = penalties, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun loadPenalty(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            penaltyRepository.getPenalty(id)
                .onSuccess { penalty ->
                    _uiState.update { it.copy(selectedPenalty = penalty, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun onContestReasonChange(reason: String) {
        _uiState.update { it.copy(contestReason = reason, errorMessage = null) }
    }

    fun contestPenalty(penaltyId: String) {
        val reason = _uiState.value.contestReason.trim()
        if (reason.length < 20) {
            _uiState.update { it.copy(errorMessage = "Reason must be at least 20 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isContesting = true, errorMessage = null) }
            penaltyRepository.contestPenalty(penaltyId, reason)
                .onSuccess { penalty ->
                    _uiState.update {
                        it.copy(
                            selectedPenalty = penalty,
                            isContesting = false,
                            contestSuccess = true,
                            contestReason = ""
                        )
                    }
                    loadPenalties()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isContesting = false, errorMessage = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearContestSuccess() {
        _uiState.update { it.copy(contestSuccess = false) }
    }

    fun clearSelectedPenalty() {
        _uiState.update { it.copy(selectedPenalty = null, contestReason = "") }
    }
}
