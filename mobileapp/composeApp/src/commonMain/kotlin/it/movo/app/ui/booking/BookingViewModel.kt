package it.movo.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.Booking
import it.movo.app.data.model.GeoPoint
import it.movo.app.data.repository.BookingRepository
import it.movo.app.data.repository.RentalRepository
import it.movo.app.platform.LocationProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

data class BookingUiState(
    val booking: Booking? = null,
    val remainingSeconds: Int = 899,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val isCancelling: Boolean = false,
    val isUnlocking: Boolean = false,
    val errorMessage: String? = null,
    val rentalStarted: Boolean = false,
    val bookingCancelled: Boolean = false,
    val bookingFailed: Boolean = false
) {
    val timerMinutes: Int get() = remainingSeconds / 60
    val timerSeconds: Int get() = remainingSeconds % 60
    val timerText: String
        get() = "${
            timerMinutes.toString().padStart(2, '0')
        }:${timerSeconds.toString().padStart(2, '0')}"
    val isExpired: Boolean get() = remainingSeconds <= 0
}

class BookingViewModel(
    private val bookingRepository: BookingRepository,
    private val rentalRepository: RentalRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    private var timerJob: Job? = null

    init {
        fetchActiveBooking()
    }

    private fun fetchActiveBooking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            bookingRepository.getActiveBooking()
                .onSuccess { booking ->
                    val remainingSeconds = calculateRemainingSeconds(booking.expiresAt)
                    _uiState.update {
                        it.copy(
                            booking = booking,
                            remainingSeconds = remainingSeconds,
                            isLoading = false
                        )
                    }
                    if (remainingSeconds > 0) {
                        startTimer()
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
        }
    }

    fun createBooking(vehicleId: String) {
        if (_uiState.value.isProcessing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isProcessing = true) }
            try {
                bookingRepository.createBooking(vehicleId)
                    .onSuccess { booking ->
                        val remainingSeconds = calculateRemainingSeconds(booking.expiresAt)
                        _uiState.update {
                            it.copy(
                                booking = booking,
                                remainingSeconds = remainingSeconds,
                                isLoading = false
                            )
                        }
                        startTimer()
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message,
                                bookingFailed = true
                            )
                        }
                    }
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    private fun calculateRemainingSeconds(expiresAt: String): Int {
        return try {
            val expiresInstant = Instant.parse(expiresAt)
            val now = Clock.System.now()
            val diff = expiresInstant - now
            maxOf(0, diff.inWholeSeconds.toInt())
        } catch (_: Exception) {
            900
        }
    }

    fun cancelBooking() {
        val bookingId = _uiState.value.booking?.id ?: return
        if (_uiState.value.isProcessing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, isProcessing = true) }
            try {
                bookingRepository.cancelBooking(bookingId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isCancelling = false,
                                bookingCancelled = true
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isCancelling = false,
                                errorMessage = e.message
                            )
                        }
                    }
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun unlockVehicle() {
        val booking = _uiState.value.booking ?: return
        val bookingId = booking.id
        if (_uiState.value.isProcessing) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUnlocking = true,
                    isProcessing = true,
                    errorMessage = null
                )
            }
            try {
                val userLocation = locationProvider.getCurrentLocation()
                if (userLocation == null) {
                    _uiState.update {
                        it.copy(
                            isUnlocking = false,
                            errorMessage = "Unable to get your location. Please enable GPS."
                        )
                    }
                    return@launch
                }
                val vehicleLocation = booking.vehicle?.location

                if (vehicleLocation != null) {
                    val distance = calculateDistance(userLocation, vehicleLocation)
                    if (distance > 30.0) {
                        _uiState.update {
                            it.copy(
                                isUnlocking = false,
                                errorMessage = "Avvicinati al veicolo per sbloccarlo"
                            )
                        }
                        return@launch
                    }
                }

                rentalRepository.unlockVehicle(bookingId, userLocation)
                    .onSuccess { rental ->
                        _uiState.update {
                            it.copy(
                                isUnlocking = false,
                                rentalStarted = true
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isUnlocking = false,
                                errorMessage = e.message
                            )
                        }
                    }
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    /**
     * Calculates the distance between two GeoPoints in meters using the Haversine formula.
     */
    private fun calculateDistance(p1: GeoPoint, p2: GeoPoint): Double {
        val r = 6371000.0 // Earth radius in meters
        val lat1 = p1.latitude * kotlin.math.PI / 180.0
        val lat2 = p2.latitude * kotlin.math.PI / 180.0
        val dLat = (p2.latitude - p1.latitude) * kotlin.math.PI / 180.0
        val dLon = (p2.longitude - p1.longitude) * kotlin.math.PI / 180.0

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return r * c
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
