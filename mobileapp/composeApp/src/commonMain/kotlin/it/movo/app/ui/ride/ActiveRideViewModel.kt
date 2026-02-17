package it.movo.app.ui.ride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.Rental
import it.movo.app.data.repository.RentalRepository
import it.movo.app.platform.LocationProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

data class ActiveRideUiState(
    val rental: Rental? = null,
    val rentalId: String? = null,
    val isVehicleLocked: Boolean = true,
    val isConnected: Boolean = true,
    val durationSeconds: Int = 0,
    val currentCostCents: Int = 0,
    val distanceKm: Double = 0.0,
    val batteryLevel: Int = 0,
    val vehicleName: String = "",
    val vehicleId: String = "",
    val isLoading: Boolean = false,
    val isEnding: Boolean = false,
    val rideEnded: Boolean = false,
    val errorMessage: String? = null,
    val passengers: List<String> = emptyList(),
    val estimatedRangeKm: Double = 0.0,
) {
    val durationText: String
        get() {
            val h = durationSeconds / 3600
            val m = (durationSeconds % 3600) / 60
            val s = durationSeconds % 60
            return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${
                s.toString().padStart(2, '0')
            }"
        }
    val costText: String
        get() {
            val euros = currentCostCents / 100
            val cents = currentCostCents % 100
            return "€${euros}.${cents.toString().padStart(2, '0')}"
        }
    val distanceText: String
        get() {
            val whole = distanceKm.toInt()
            val decimal = ((distanceKm - whole) * 10).toInt()
            return "${whole}.${decimal} km"
        }
    val rangeText: String
        get() {
            val whole = estimatedRangeKm.toInt()
            val decimal = ((estimatedRangeKm - whole) * 10).toInt()
            return "${whole}.${decimal} km"
        }
}

class ActiveRideViewModel(
    private val rentalRepository: RentalRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActiveRideUiState())
    val uiState: StateFlow<ActiveRideUiState> = _uiState.asStateFlow()
    private var timerJob: Job? = null
    private var refreshJob: Job? = null

    init {
        startTimer()
        startPeriodicRefresh()
    }

    private fun loadRentalData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            rentalRepository.getActiveRental()
                .onSuccess { rental ->
                    val duration = calculateDurationFromStart(rental.startedAt)
                    _uiState.update {
                        it.copy(
                            rental = rental,
                            rentalId = rental.id,
                            vehicleName = rental.vehicle?.model ?: "",
                            vehicleId = rental.vehicleId,
                            batteryLevel = rental.vehicle?.batteryLevel ?: 0,
                            estimatedRangeKm = (rental.vehicle?.batteryLevel
                                ?: 0) * RANGE_FACTOR_KM,
                            durationSeconds = duration,
                            currentCostCents = rental.currentCostCents,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun loadRental(rentalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, rentalId = rentalId) }
            rentalRepository.getRental(rentalId)
                .onSuccess { rental ->
                    val duration = calculateDurationFromStart(rental.startedAt)
                    _uiState.update {
                        it.copy(
                            vehicleName = rental.vehicle?.model ?: "",
                            vehicleId = rental.vehicleId,
                            batteryLevel = rental.vehicle?.batteryLevel ?: 0,
                            estimatedRangeKm = (rental.vehicle?.batteryLevel
                                ?: 0) * RANGE_FACTOR_KM,
                            durationSeconds = duration,
                            currentCostCents = rental.totalCostCents,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun calculateDurationFromStart(startedAt: String): Int {
        return try {
            val startInstant = Instant.parse(startedAt)
            val now = Clock.System.now()
            (now - startInstant).inWholeSeconds.toInt().coerceAtLeast(0)
        } catch (_: Exception) {
            0
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && !_uiState.value.rideEnded) {
                delay(1000)
                _uiState.update {
                    it.copy(durationSeconds = it.durationSeconds + 1)
                }
            }
        }
    }

    private fun startPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive && !_uiState.value.rideEnded) {
                delay(30_000)
                refreshRentalData()
            }
        }
    }

    private fun refreshRentalData() {
        viewModelScope.launch {
            rentalRepository.getActiveRental()
                .onSuccess { rental ->
                    _uiState.update {
                        it.copy(
                            currentCostCents = rental.currentCostCents,
                            batteryLevel = rental.vehicle?.batteryLevel ?: it.batteryLevel,
                            estimatedRangeKm = (rental.vehicle?.batteryLevel
                                ?: it.batteryLevel) * RANGE_FACTOR_KM
                        )
                    }
                }
        }
    }

    fun toggleLock() {
        _uiState.update { it.copy(isVehicleLocked = !it.isVehicleLocked) }
    }

    fun endRental() {
        viewModelScope.launch {
            _uiState.update { it.copy(isEnding = true, errorMessage = null) }
            val rentalId = _uiState.value.rental?.id ?: _uiState.value.rentalId
            if (rentalId == null) {
                _uiState.update {
                    it.copy(
                        isEnding = false,
                        errorMessage = "No active rental found"
                    )
                }
                return@launch
            }

            val endLocation = locationProvider.getCurrentLocation()
            if (endLocation == null) {
                _uiState.update {
                    it.copy(
                        isEnding = false,
                        errorMessage = "Unable to get your location. Please enable GPS."
                    )
                }
                return@launch
            }

            rentalRepository.endRental(rentalId, endLocation)
                .onSuccess { _uiState.update { it.copy(isEnding = false, rideEnded = true) } }
                .onFailure { e ->
                    val message =
                        if (e.message?.contains("403") == true || e.message?.contains("parking") == true) {
                            "You must be in an authorized parking zone to end your rental"
                        } else {
                            e.message ?: "Failed to end rental"
                        }
                    _uiState.update { it.copy(isEnding = false, errorMessage = message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        refreshJob?.cancel()
    }

    companion object {
        private const val RANGE_FACTOR_KM = 1.5
    }
}
