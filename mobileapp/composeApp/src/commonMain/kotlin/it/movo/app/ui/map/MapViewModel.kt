package it.movo.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.ParkingArea
import it.movo.app.data.model.Vehicle
import it.movo.app.data.model.VehicleMapItem
import it.movo.app.data.repository.ParkingAreaRepository
import it.movo.app.data.repository.VehicleRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MapUiState(
    val vehicles: List<VehicleMapItem> = emptyList(),
    val parkingAreas: List<ParkingArea> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val selectedPreview: VehicleMapItem? = null,
    val searchQuery: String = "",
    val minBattery: Int = 0,
    val maxPrice: Double = Double.MAX_VALUE,
    val maxDistance: Double = Double.MAX_VALUE,
    val showFilterSheet: Boolean = false,
    val isLoading: Boolean = false,
    val showVehicleDetails: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class)
class MapViewModel(
    private val vehicleRepository: VehicleRepository,
    private val parkingAreaRepository: ParkingAreaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    private val _searchQueryFlow = MutableStateFlow("")

    init {
        loadVehicles()
        loadParkingAreas()
        observeSearchQuery()
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(30_000L)
                loadVehicles()
            }
        }
    }

    fun loadVehicles() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true) }
            vehicleRepository.getVehiclesOnMap(
                minBattery = if (state.minBattery > 0) state.minBattery else null
            )
                .onSuccess { vehicles ->
                    _uiState.update {
                        it.copy(
                            vehicles = vehicles,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQueryFlow
                .debounce(300)
                .collect { query ->
                    if (query.isNotBlank()) {
                        performSearch(query)
                    } else {
                        loadVehicles()
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQueryFlow.value = query
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true) }
            vehicleRepository.searchVehicles(
                model = query,
                minBattery = if (state.minBattery > 0) state.minBattery else null,
                maxPricePerMinute = if (state.maxPrice < Double.MAX_VALUE) (state.maxPrice * 100).toInt() else null,
                maxDistance = if (state.maxDistance < Double.MAX_VALUE) state.maxDistance.toInt() else null
            )
                .onSuccess { results ->
                    val vehicles = results.map { result ->
                        VehicleMapItem(
                            id = result.id,
                            model = result.model,
                            licensePlate = result.licensePlate,
                            location = result.location,
                            batteryLevel = result.batteryLevel,
                            status = result.status
                        )
                    }
                    _uiState.update { it.copy(vehicles = vehicles, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun toggleFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = !it.showFilterSheet) }
    }

    fun onMinBatteryChange(value: Int) {
        _uiState.update { it.copy(minBattery = value) }
    }

    fun onMaxPriceChange(value: Double) {
        _uiState.update { it.copy(maxPrice = value) }
    }

    fun onMaxDistanceChange(value: Double) {
        _uiState.update { it.copy(maxDistance = value) }
    }

    fun applyFilters() {
        _uiState.update { it.copy(showFilterSheet = false) }
        if (_uiState.value.searchQuery.isNotBlank()) {
            performSearch(_uiState.value.searchQuery)
        } else {
            loadVehicles()
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                minBattery = 0,
                maxPrice = Double.MAX_VALUE,
                maxDistance = Double.MAX_VALUE,
                showFilterSheet = false
            )
        }
        if (_uiState.value.searchQuery.isNotBlank()) {
            performSearch(_uiState.value.searchQuery)
        } else {
            loadVehicles()
        }
    }

    fun onVehicleMarkerClick(vehicle: VehicleMapItem) {
        _uiState.update { it.copy(selectedPreview = vehicle) }
    }

    fun onVehiclePreviewDismiss() {
        _uiState.update { it.copy(selectedPreview = null) }
    }

    fun onShowVehicleDetails(vehicleId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            vehicleRepository.getVehicle(vehicleId)
                .onSuccess { vehicle ->
                    _uiState.update {
                        it.copy(
                            selectedVehicle = vehicle,
                            showVehicleDetails = true,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    fun onVehicleDetailsDismiss() {
        _uiState.update { it.copy(showVehicleDetails = false, selectedVehicle = null) }
    }

    fun loadParkingAreas(bounds: String? = null) {
        viewModelScope.launch {
            parkingAreaRepository.getParkingAreas(bounds)
                .onSuccess { areas -> _uiState.update { it.copy(parkingAreas = areas) } }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
