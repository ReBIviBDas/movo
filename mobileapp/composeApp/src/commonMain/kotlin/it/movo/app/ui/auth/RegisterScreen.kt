package it.movo.app.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoTealContainer
import it.movo.app.ui.theme.MovoTheme
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.back
import it.movo.app.composeapp.generated.resources.register_accept_cookies
import it.movo.app.composeapp.generated.resources.register_accept_privacy
import it.movo.app.composeapp.generated.resources.register_accept_terms
import it.movo.app.composeapp.generated.resources.register_address
import it.movo.app.composeapp.generated.resources.register_age_error
import it.movo.app.composeapp.generated.resources.register_completed
import it.movo.app.composeapp.generated.resources.register_confirm_password
import it.movo.app.composeapp.generated.resources.register_continue
import it.movo.app.composeapp.generated.resources.register_create_account
import it.movo.app.composeapp.generated.resources.register_date_format
import it.movo.app.composeapp.generated.resources.register_date_of_birth
import it.movo.app.composeapp.generated.resources.register_drivers_license
import it.movo.app.composeapp.generated.resources.register_email
import it.movo.app.composeapp.generated.resources.register_first_name
import it.movo.app.composeapp.generated.resources.register_fiscal_code
import it.movo.app.composeapp.generated.resources.register_get_started
import it.movo.app.composeapp.generated.resources.register_identity_document
import it.movo.app.composeapp.generated.resources.register_identity_front
import it.movo.app.composeapp.generated.resources.register_identity_hint
import it.movo.app.composeapp.generated.resources.register_identity_tap
import it.movo.app.composeapp.generated.resources.register_license_front
import it.movo.app.composeapp.generated.resources.register_license_hint
import it.movo.app.composeapp.generated.resources.register_license_tap
import it.movo.app.composeapp.generated.resources.register_password
import it.movo.app.composeapp.generated.resources.register_password_placeholder
import it.movo.app.composeapp.generated.resources.register_phone
import it.movo.app.composeapp.generated.resources.register_skip_license
import it.movo.app.composeapp.generated.resources.register_step1_subtitle
import it.movo.app.composeapp.generated.resources.register_step1_title
import it.movo.app.composeapp.generated.resources.register_step2_subtitle
import it.movo.app.composeapp.generated.resources.register_step2_title
import it.movo.app.composeapp.generated.resources.register_step3_subtitle
import it.movo.app.composeapp.generated.resources.register_step3_title
import it.movo.app.composeapp.generated.resources.register_step4_subtitle
import it.movo.app.composeapp.generated.resources.register_step4_title
import it.movo.app.composeapp.generated.resources.register_step_of
import it.movo.app.composeapp.generated.resources.register_surname
import it.movo.app.composeapp.generated.resources.register_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    RegisterContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onNavigateBack,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onFirstNameChange = viewModel::onFirstNameChange,
        onLastNameChange = viewModel::onLastNameChange,
        onDateOfBirthChange = viewModel::onDateOfBirthChange,
        onFiscalCodeChange = viewModel::onFiscalCodeChange,
        onPhoneChange = viewModel::onPhoneChange,
        onAddressChange = viewModel::onAddressChange,
        onIdentityDocumentClick = viewModel::onIdentityDocumentClick,
        onLicenseClick = viewModel::onDriverLicenseSelected,
        onSkipLicense = viewModel::nextStep,
        onAcceptTermsChange = viewModel::onAcceptTermsChange,
        onAcceptPrivacyChange = viewModel::onAcceptPrivacyChange,
        onAcceptCookiesChange = viewModel::onAcceptCookiesChange,
        onNextStep = viewModel::nextStep,
        onRegistrationSuccess = onRegistrationSuccess
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun RegisterScreenStep1Preview() {
    MovoTheme {
        RegisterContent(
            uiState = RegisterUiState(
                currentStep = 1,
                email = "mario.rossi@email.com",
                password = "Password123!",
                confirmPassword = "Password123!"
            ),
            onBackClick = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onFirstNameChange = {},
            onLastNameChange = {},
            onDateOfBirthChange = {},
            onFiscalCodeChange = {},
            onPhoneChange = {},
            onAddressChange = {},
            onIdentityDocumentClick = {},
            onLicenseClick = {},
            onSkipLicense = {},
            onAcceptTermsChange = {},
            onAcceptPrivacyChange = {},
            onAcceptCookiesChange = {},
            onNextStep = {},
            onRegistrationSuccess = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun RegisterScreenStep2Preview() {
    MovoTheme {
        RegisterContent(
            uiState = RegisterUiState(
                currentStep = 2,
                email = "mario.rossi@email.com",
                password = "Password123!",
                confirmPassword = "Password123!",
                firstName = "Mario",
                lastName = "Rossi",
                dateOfBirth = "15/03/1995",
                fiscalCode = "RSSMRA95C15H501Z",
                phone = "+39 333 1234567",
                address = "Via Roma 1, Milano"
            ),
            onBackClick = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onFirstNameChange = {},
            onLastNameChange = {},
            onDateOfBirthChange = {},
            onFiscalCodeChange = {},
            onPhoneChange = {},
            onAddressChange = {},
            onIdentityDocumentClick = {},
            onLicenseClick = {},
            onSkipLicense = {},
            onAcceptTermsChange = {},
            onAcceptPrivacyChange = {},
            onAcceptCookiesChange = {},
            onNextStep = {},
            onRegistrationSuccess = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBackClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onFiscalCodeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onIdentityDocumentClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onSkipLicense: () -> Unit,
    onAcceptTermsChange: (Boolean) -> Unit,
    onAcceptPrivacyChange: (Boolean) -> Unit,
    onAcceptCookiesChange: (Boolean) -> Unit,
    onNextStep: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = {
                    Text(
                        text = stringResource(Res.string.register_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        Res.string.register_step_of,
                        uiState.currentStep,
                        uiState.totalSteps
                    ),
                    color = MovoTeal,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        Res.string.register_completed,
                        (uiState.progress * 100).toInt()
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MovoTeal,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "StepContent"
            ) { step ->
                when (step) {
                    1 -> Step1AccountSetup(
                        email = uiState.email,
                        password = uiState.password,
                        confirmPassword = uiState.confirmPassword,
                        passwordVisible = uiState.passwordVisible,
                        onEmailChange = onEmailChange,
                        onPasswordChange = onPasswordChange,
                        onConfirmPasswordChange = onConfirmPasswordChange,
                        onTogglePasswordVisibility = onTogglePasswordVisibility
                    )

                    2 -> Step2PersonalDetails(
                        firstName = uiState.firstName,
                        lastName = uiState.lastName,
                        dateOfBirth = uiState.dateOfBirth,
                        fiscalCode = uiState.fiscalCode,
                        phone = uiState.phone,
                        address = uiState.address,
                        isUnder18 = uiState.isUnder18,
                        hasIdentityDocument = uiState.hasIdentityDocument,
                        onIdentityDocumentClick = onIdentityDocumentClick,
                        onFirstNameChange = onFirstNameChange,
                        onLastNameChange = onLastNameChange,
                        onDateOfBirthChange = onDateOfBirthChange,
                        onFiscalCodeChange = onFiscalCodeChange,
                        onPhoneChange = onPhoneChange,
                        onAddressChange = onAddressChange,
                        onLicenseClick = onLicenseClick,
                        onSkipLicense = onSkipLicense
                    )

                    3 -> Step3TermsAndPrivacy(
                        acceptTerms = uiState.acceptTerms,
                        acceptPrivacy = uiState.acceptPrivacy,
                        acceptCookies = uiState.acceptCookies,
                        onAcceptTermsChange = onAcceptTermsChange,
                        onAcceptPrivacyChange = onAcceptPrivacyChange,
                        onAcceptCookiesChange = onAcceptCookiesChange
                    )

                    4 -> Step4Success()
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.currentStep < 4) {
                Button(
                    onClick = onNextStep,
                    enabled = uiState.canProceed && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MovoTeal,
                        disabledContainerColor = MovoTeal.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        val buttonText = when (uiState.currentStep) {
                            3 -> stringResource(Res.string.register_create_account)
                            else -> stringResource(Res.string.register_continue)
                        }
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                Button(
                    onClick = onRegistrationSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.register_get_started),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Step1AccountSetup(
    email: String,
    password: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.register_step1_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.register_step1_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email field
        Text(
            text = stringResource(Res.string.register_email),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("name@example.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        Text(
            text = stringResource(Res.string.register_password),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(Res.string.register_password_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password field
        Text(
            text = stringResource(Res.string.register_confirm_password),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(Res.string.register_confirm_password)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2PersonalDetails(
    firstName: String,
    lastName: String,
    dateOfBirth: String,
    fiscalCode: String,
    phone: String,
    address: String,
    isUnder18: Boolean,
    hasIdentityDocument: Boolean,
    onIdentityDocumentClick: () -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onFiscalCodeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onLicenseClick: () -> Unit,
    onSkipLicense: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column {
        Text(
            text = stringResource(Res.string.register_step2_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.register_step2_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // First Name field
        Text(
            text = stringResource(Res.string.register_first_name),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Surname field
        Text(
            text = stringResource(Res.string.register_surname),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date of Birth field
        Text(
            text = stringResource(Res.string.register_date_of_birth),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            placeholder = { Text(stringResource(Res.string.register_date_format)) },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) {
                            showDatePicker = true
                        }
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val days = (millis / 86_400_000).toInt()
                                val date = kotlinx.datetime.LocalDate.fromEpochDays(days)
                                val formatted = "${date.dayOfMonth.toString().padStart(2, '0')}/${
                                    date.monthNumber.toString().padStart(2, '0')
                                }/${date.year}"
                                onDateOfBirthChange(formatted)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (isUnder18) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.register_age_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fiscal Code field
        Text(
            text = stringResource(Res.string.register_fiscal_code),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = fiscalCode,
            onValueChange = onFiscalCodeChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone field
        Text(
            text = stringResource(Res.string.register_phone),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Address field
        Text(
            text = stringResource(Res.string.register_address),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Driver's License section
        Text(
            text = stringResource(Res.string.register_drivers_license),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dashed border upload box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .drawBehind {
                    val stroke = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(12f, 8f),
                            0f
                        )
                    )
                    drawRoundRect(
                        color = MovoTeal,
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = stroke
                    )
                }
                .background(
                    color = MovoTealContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onLicenseClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Camera icon in teal circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MovoTealContainer,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = MovoTeal,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(Res.string.register_license_front),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.register_license_tap),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Helper text
        Text(
            text = stringResource(Res.string.register_license_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Identity Document section
        Text(
            text = stringResource(Res.string.register_identity_document),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .drawBehind {
                    val stroke = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(12f, 8f),
                            0f
                        )
                    )
                    drawRoundRect(
                        color = if (hasIdentityDocument) MovoTeal else MovoTeal.copy(alpha = 0.6f),
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = stroke
                    )
                }
                .background(
                    color = if (hasIdentityDocument) MovoTealContainer.copy(alpha = 0.5f) else MovoTealContainer.copy(
                        alpha = 0.3f
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onIdentityDocumentClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MovoTealContainer,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasIdentityDocument) Icons.Default.Check else Icons.Default.Badge,
                        contentDescription = null,
                        tint = MovoTeal,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(Res.string.register_identity_front),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.register_identity_tap),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.register_identity_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.register_skip_license),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MovoTeal,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSkipLicense() }
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun Step3TermsAndPrivacy(
    acceptTerms: Boolean,
    acceptPrivacy: Boolean,
    acceptCookies: Boolean,
    onAcceptTermsChange: (Boolean) -> Unit,
    onAcceptPrivacyChange: (Boolean) -> Unit,
    onAcceptCookiesChange: (Boolean) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.register_step3_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.register_step3_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Terms checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAcceptTermsChange(!acceptTerms) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = acceptTerms,
                onCheckedChange = onAcceptTermsChange
            )
            Text(
                text = stringResource(Res.string.register_accept_terms),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Privacy checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAcceptPrivacyChange(!acceptPrivacy) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = acceptPrivacy,
                onCheckedChange = onAcceptPrivacyChange
            )
            Text(
                text = stringResource(Res.string.register_accept_privacy),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cookies checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAcceptCookiesChange(!acceptCookies) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = acceptCookies,
                onCheckedChange = onAcceptCookiesChange
            )
            Text(
                text = stringResource(Res.string.register_accept_cookies),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun Step4Success() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Success icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MovoTealContainer,
                    shape = RoundedCornerShape(60.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MovoTeal,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(Res.string.register_step4_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.register_step4_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
