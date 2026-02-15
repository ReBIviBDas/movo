package it.movo.app.ui.issue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.IssueCategory
import it.movo.app.data.repository.IssueRepository
import it.movo.app.data.repository.RentalRepository
import it.movo.app.platform.ImagePicker
import it.movo.app.platform.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportIssueUiState(
    val vehicleId: String = "",
    val rentalId: String? = null,
    val selectedCategory: IssueCategory? = null,
    val description: String = "",
    val photos: List<ByteArray> = emptyList(),
    val isCategoryMenuExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null,
    val gpsLocation: String = ""
)

class ReportIssueViewModel(
    private val issueRepository: IssueRepository,
    private val rentalRepository: RentalRepository,
    private val locationProvider: LocationProvider,
    private val imagePicker: ImagePicker
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportIssueUiState())
    val uiState: StateFlow<ReportIssueUiState> = _uiState.asStateFlow()

    init {
        captureLocation()
    }

    fun captureLocation() {
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                _uiState.update {
                    it.copy(gpsLocation = "${location.latitude},${location.longitude}")
                }
            }
        }
    }

    fun pickAndAddPhoto() {
        viewModelScope.launch {
            val photoData = imagePicker.pickImage()
            if (photoData != null) {
                addPhoto(photoData)
            }
        }
    }

    fun setVehicleId(vehicleId: String) {
        _uiState.update { it.copy(vehicleId = vehicleId) }
    }

    fun setRentalId(rentalId: String?) {
        _uiState.update { it.copy(rentalId = rentalId) }

        if (rentalId != null && _uiState.value.vehicleId.isEmpty()) {
            viewModelScope.launch {
                rentalRepository.getRental(rentalId)
                    .onSuccess { rental ->
                        _uiState.update { it.copy(vehicleId = rental.vehicleId) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(errorMessage = e.message ?: "Failed to load rental details") }
                    }
            }
        }
    }

    fun onCategorySelected(category: IssueCategory) {
        _uiState.update { it.copy(selectedCategory = category, isCategoryMenuExpanded = false, errorMessage = null) }
    }

    fun toggleCategoryMenu() {
        _uiState.update { it.copy(isCategoryMenuExpanded = !it.isCategoryMenuExpanded) }
    }

    fun dismissCategoryMenu() {
        _uiState.update { it.copy(isCategoryMenuExpanded = false) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description, errorMessage = null) }
    }

    fun addPhoto(photo: ByteArray) {
        if (_uiState.value.photos.size >= 5) {
            _uiState.update { it.copy(errorMessage = "Maximum 5 photos allowed") }
            return
        }
        _uiState.update { it.copy(photos = it.photos + photo, errorMessage = null) }
    }

    fun removePhoto(index: Int) {
        _uiState.update {
            val updatedPhotos = it.photos.toMutableList().apply { removeAt(index) }
            it.copy(photos = updatedPhotos)
        }
    }

    fun submitReport() {
        val state = _uiState.value

        if (state.selectedCategory == null) {
            _uiState.update { it.copy(errorMessage = "Please select a category") }
            return
        }
        if (state.description.length < 10) {
            _uiState.update { it.copy(errorMessage = "Description must be at least 10 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            issueRepository.createIssue(
                vehicleId = state.vehicleId,
                rentalId = state.rentalId,
                category = state.selectedCategory,
                description = state.description,
                photos = state.photos,
                gpsLocation = state.gpsLocation.ifEmpty { null }
            )
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSubmitted = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}