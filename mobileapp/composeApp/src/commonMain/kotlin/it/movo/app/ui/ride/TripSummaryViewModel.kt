package it.movo.app.ui.ride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.RentalStatus
import it.movo.app.data.repository.RentalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TripSummaryUiState(
    val rentalId: String = "",
    val vehicleName: String = "",
    val totalCostCents: Int = 0,
    val discountAppliedCents: Int = 0,
    val finalCostCents: Int = 0,
    val durationMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val paymentMethod: String = "",
    val paymentLabel: String = "",
    val isPaid: Boolean = false,
    val splitBillEnabled: Boolean = false,
    val co2SavedGrams: Int = 0,
    val rating: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val totalCostText: String get() {
        val euros = finalCostCents / 100
        val cents = finalCostCents % 100
        return "€${euros}.${cents.toString().padStart(2, '0')}"
    }
    val durationText: String get() {
        return "$durationMinutes min"
    }
    val distanceText: String get() {
        val whole = distanceKm.toInt()
        val decimal = ((distanceKm - whole) * 10).toInt()
        return "${whole}.${decimal} km"
    }
}

class TripSummaryViewModel(private val rentalRepository: RentalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TripSummaryUiState())
    val uiState: StateFlow<TripSummaryUiState> = _uiState.asStateFlow()
    private var currentRentalId: String? = null

    fun loadTripSummary(rentalId: String) {
        currentRentalId = rentalId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            rentalRepository.getRental(rentalId)
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            rentalId = summary.id,
                            vehicleName = summary.vehicle?.model ?: "",
                            totalCostCents = summary.totalCostCents,
                            discountAppliedCents = summary.discountAppliedCents,
                            finalCostCents = summary.finalCostCents,
                            durationMinutes = summary.durationMinutes ?: 0,
                            distanceKm = summary.distanceKm ?: 0.0,
                            isPaid = summary.status == RentalStatus.COMPLETED,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun onSplitBillToggle(enabled: Boolean) {
        _uiState.update { it.copy(splitBillEnabled = enabled) }
    }

    fun onRatingChange(rating: Int) {
        _uiState.update { it.copy(rating = rating) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun retry() {
        currentRentalId?.let { loadTripSummary(it) }
    }
}
