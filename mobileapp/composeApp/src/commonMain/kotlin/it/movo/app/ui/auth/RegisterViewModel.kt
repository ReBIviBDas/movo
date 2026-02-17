package it.movo.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.movo.app.data.model.RegisterRequest
import it.movo.app.data.remote.parseErrorMessage
import it.movo.app.data.repository.AuthRepository
import it.movo.app.platform.ImagePicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class RegisterUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 4,
    // Step 1: Credentials
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    // Step 2: Personal info
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val fiscalCode: String = "",
    val phone: String = "",
    val address: String = "",
    // Step 3: Documents
    val hasDriverLicense: Boolean = false,
    val drivingLicense: ByteArray? = null,
    val identityDocument: ByteArray? = null,
    // Step 4: Terms
    val acceptTerms: Boolean = false,
    val acceptPrivacy: Boolean = false,
    val acceptCookies: Boolean = false,
    // General
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationSuccess: Boolean = false
) {
    val progress: Float get() = currentStep.toFloat() / totalSteps.toFloat()
    val isUnder18: Boolean get() = dateOfBirth.isNotBlank() && !isAtLeast18(dateOfBirth)
    val hasIdentityDocument: Boolean get() = identityDocument != null
    val canProceed: Boolean
        get() = when (currentStep) {
            1 -> email.isNotBlank() && isValidEmail(email) && password.length >= 8 && password == confirmPassword
            2 -> firstName.isNotBlank() && lastName.isNotBlank() && dateOfBirth.isNotBlank() && !isUnder18 && fiscalCode.isNotBlank() && phone.isNotBlank() && isValidPhone(
                phone
            )

            3 -> true
            4 -> acceptTerms && acceptPrivacy
            else -> false
        }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val DOB_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

        fun isValidEmail(email: String): Boolean {
            return EMAIL_REGEX.matches(email)
        }

        fun isValidPhone(phone: String): Boolean {
            val digitsOnly = phone.filter { it.isDigit() }
            return digitsOnly.length in 7..15
        }

        fun isAtLeast18(dateOfBirth: String): Boolean {
            if (!DOB_REGEX.matches(dateOfBirth)) return false
            return try {
                val dob = LocalDate.parse(dateOfBirth)
                val now = Clock.System.now()
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val age = today.year - dob.year -
                        if (today.month < dob.month ||
                            (today.month == dob.month && today.day < dob.day)
                        ) 1 else 0
                age >= 18
            } catch (_: Exception) {
                false
            }
        }
    }
}

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val imagePicker: ImagePicker
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value) }
    }

    fun onDateOfBirthChange(value: String) {
        _uiState.update { it.copy(dateOfBirth = value) }
    }

    fun onFiscalCodeChange(value: String) {
        _uiState.update { it.copy(fiscalCode = value) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value) }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value) }
    }

    fun onDriverLicenseSelected() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = imagePicker.pickImage()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasDriverLicense = data != null,
                    drivingLicense = data
                )
            }
        }
    }

    fun onDriverLicenseData(data: ByteArray?) {
        _uiState.update { it.copy(hasDriverLicense = data != null, drivingLicense = data) }
    }

    fun onIdentityDocumentSelected(data: ByteArray?) {
        _uiState.update { it.copy(identityDocument = data) }
    }

    fun onIdentityDocumentClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = imagePicker.pickImage()
            _uiState.update { it.copy(isLoading = false, identityDocument = data) }
        }
    }

    fun onAcceptTermsChange(value: Boolean) {
        _uiState.update { it.copy(acceptTerms = value) }
    }

    fun onAcceptPrivacyChange(value: Boolean) {
        _uiState.update { it.copy(acceptPrivacy = value) }
    }

    fun onAcceptCookiesChange(value: Boolean) {
        _uiState.update { it.copy(acceptCookies = value) }
    }

    fun nextStep() {
        val state = _uiState.value
        if (!state.canProceed) return
        if (state.currentStep < state.totalSteps) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1, errorMessage = null) }
        } else {
            register()
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1, errorMessage = null) }
        }
    }

    private fun register() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val request = RegisterRequest(
                email = state.email,
                password = state.password,
                firstName = state.firstName,
                lastName = state.lastName,
                dateOfBirth = state.dateOfBirth,
                fiscalCode = state.fiscalCode,
                phone = state.phone,
                address = state.address.ifBlank { null },
                acceptTerms = state.acceptTerms,
                acceptPrivacy = state.acceptPrivacy,
                acceptCookies = state.acceptCookies,
                drivingLicense = state.drivingLicense,
                identityDocument = state.identityDocument
            )
            authRepository.register(request)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registrationSuccess = true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = parseErrorMessage(e as Exception)
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
