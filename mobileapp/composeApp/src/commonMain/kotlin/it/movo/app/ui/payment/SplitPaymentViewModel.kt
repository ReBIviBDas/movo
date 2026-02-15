package it.movo.app.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.SplitParticipant
import it.movo.app.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParticipantEntry(
    val userId: String = "",
    val percentage: String = ""
)

data class SplitPaymentUiState(
    val rentalId: String = "",
    val participants: List<ParticipantEntry> = listOf(ParticipantEntry()),
    val isLoading: Boolean = false,
    val requestSent: Boolean = false,
    val errorMessage: String? = null
)

class SplitPaymentViewModel(private val paymentRepository: PaymentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SplitPaymentUiState())
    val uiState: StateFlow<SplitPaymentUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.update { SplitPaymentUiState() }
    }

    fun setRentalId(rentalId: String) {
        _uiState.update { SplitPaymentUiState(rentalId = rentalId) }
    }

    fun addParticipant() {
        val current = _uiState.value.participants
        if (current.size >= 4) return
        _uiState.update { it.copy(participants = current + ParticipantEntry()) }
    }

    fun removeParticipant(index: Int) {
        if (_uiState.value.participants.size <= 1) return
        val updated = _uiState.value.participants.filterIndexed { i, _ -> i != index }
        _uiState.update { it.copy(participants = updated) }
    }

    fun updateParticipantUserId(index: Int, userId: String) {
        val current = _uiState.value.participants.toMutableList()
        if (index >= current.size) return
        current[index] = current[index].copy(userId = userId)
        _uiState.update { it.copy(participants = current, errorMessage = null) }
    }

    fun updateParticipantPercentage(index: Int, percentage: String) {
        val current = _uiState.value.participants.toMutableList()
        if (index >= current.size) return
        current[index] = current[index].copy(percentage = percentage)
        _uiState.update { it.copy(participants = current, errorMessage = null) }
    }

    fun splitEqually() {
        val current = _uiState.value.participants
        if (current.isEmpty()) return
        val equalShare = (100.0 / (current.size + 1)).toInt().toString()
        val updated = current.map { it.copy(percentage = equalShare) }
        _uiState.update { it.copy(participants = updated) }
    }

    fun sendSplitRequest() {
        val state = _uiState.value

        if (state.participants.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "At least one participant is required") }
            return
        }

        val participants = state.participants.mapNotNull { entry ->
            val userId = entry.userId.trim()
            val pct = entry.percentage.toDoubleOrNull()
            if (userId.isBlank() || pct == null || pct <= 0) return@mapNotNull null
            SplitParticipant(userId = userId, percentage = pct)
        }

        if (participants.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Add at least one valid participant with a positive percentage") }
            return
        }

        val totalPercentage = participants.sumOf { it.percentage }
        if (totalPercentage > 100.0) {
            _uiState.update { it.copy(errorMessage = "Total percentage cannot exceed 100% (current: $totalPercentage%)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            paymentRepository.createSplitRequest(state.rentalId, participants)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, requestSent = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}
