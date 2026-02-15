package it.movo.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.movo.app.ui.theme.MovoError
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoOutlineVariant
import it.movo.app.ui.theme.MovoSuccess
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoWhite
import movo.composeapp.generated.resources.Res
import movo.composeapp.generated.resources.cancel
import movo.composeapp.generated.resources.common_email
import movo.composeapp.generated.resources.common_error
import movo.composeapp.generated.resources.common_loading
import movo.composeapp.generated.resources.delete
import movo.composeapp.generated.resources.profile_change_password
import movo.composeapp.generated.resources.profile_current_password
import movo.composeapp.generated.resources.profile_enter_password
import movo.composeapp.generated.resources.profile_new_password
import movo.composeapp.generated.resources.profile_notification_settings
import movo.composeapp.generated.resources.profile_delete_account
import movo.composeapp.generated.resources.profile_delete_confirm
import movo.composeapp.generated.resources.profile_drivers_license
import movo.composeapp.generated.resources.profile_email_subtitle
import movo.composeapp.generated.resources.profile_email_updates
import movo.composeapp.generated.resources.profile_full_name
import movo.composeapp.generated.resources.profile_license_valid_until
import movo.composeapp.generated.resources.profile_license_verified
import movo.composeapp.generated.resources.profile_logout
import movo.composeapp.generated.resources.profile_member_since
import movo.composeapp.generated.resources.profile_notifications
import movo.composeapp.generated.resources.profile_personal_details
import movo.composeapp.generated.resources.profile_phone_number
import movo.composeapp.generated.resources.profile_push_notifications
import movo.composeapp.generated.resources.profile_push_subtitle
import movo.composeapp.generated.resources.profile_save_changes
import movo.composeapp.generated.resources.profile_title
import movo.composeapp.generated.resources.profile_subscriptions
import movo.composeapp.generated.resources.profile_promotions
import movo.composeapp.generated.resources.profile_penalties
import movo.composeapp.generated.resources.profile_movo_points
import movo.composeapp.generated.resources.profile_address
import movo.composeapp.generated.resources.profile_date_of_birth
import movo.composeapp.generated.resources.profile_fiscal_code
import movo.composeapp.generated.resources.profile_logout_all
import movo.composeapp.generated.resources.profile_export_data
import movo.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNotificationSettings: () -> Unit,
    onSubscription: () -> Unit,
    onPromotion: () -> Unit,
    onPenalty: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            onLogout()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.profile_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MovoSurface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = MovoTeal)
                    Text(
                        text = stringResource(Res.string.common_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MovoOnSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ProfileHeader(
                    fullName = uiState.fullName,
                    memberSince = uiState.memberSince
                )

                Spacer(modifier = Modifier.height(16.dp))

                // MovoPoints Placeholder (RF44-47)
                MovoPointsCard()

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle(text = stringResource(Res.string.profile_personal_details))

                Spacer(modifier = Modifier.height(12.dp))

                PersonalDetailsCard(
                    fullName = uiState.fullName,
                    email = uiState.email,
                    phone = uiState.phone,
                    address = uiState.address,
                    dateOfBirth = uiState.dateOfBirth,
                    fiscalCode = uiState.fiscalCode,
                    onFullNameChange = { viewModel.onFullNameChange(it) },
                    onPhoneChange = { viewModel.onPhoneChange(it) },
                    onAddressChange = { viewModel.onAddressChange(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LicenseCard(
                    verified = uiState.licenseVerified,
                    expiryDate = uiState.licenseExpiry
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle(text = stringResource(Res.string.profile_notifications))

                Spacer(modifier = Modifier.height(12.dp))

                NotificationCard(
                    pushEnabled = uiState.pushNotificationsEnabled,
                    emailEnabled = uiState.emailUpdatesEnabled,
                    onPushToggle = { viewModel.onPushNotificationsToggle(it) },
                    onEmailToggle = { viewModel.onEmailUpdatesToggle(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                NotificationSettingsRow(onClick = onNotificationSettings)

                Spacer(modifier = Modifier.height(16.dp))

                ChangePasswordRow(
                    onClick = { showPasswordDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                NavigationRow(
                    label = stringResource(Res.string.profile_subscriptions),
                    onClick = onSubscription
                )

                Spacer(modifier = Modifier.height(12.dp))

                NavigationRow(
                    label = stringResource(Res.string.profile_promotions),
                    onClick = onPromotion
                )

                Spacer(modifier = Modifier.height(12.dp))

                NavigationRow(
                    label = stringResource(Res.string.profile_penalties),
                    onClick = onPenalty
                )

                Spacer(modifier = Modifier.height(12.dp))

                NavigationRow(
                    label = stringResource(Res.string.profile_export_data),
                    onClick = { viewModel.exportData() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MovoOnSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Log out",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.profile_logout),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.logoutAll() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MovoOnSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Log out from all devices",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.profile_logout_all),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.profile_delete_account),
                        color = MovoError,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = {
                            Text(
                                text = stringResource(Res.string.profile_delete_account),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.profile_delete_confirm),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MovoError
                                )
                                OutlinedTextField(
                                    value = deletePassword,
                                    onValueChange = { deletePassword = it },
                                    label = { Text(stringResource(Res.string.profile_enter_password)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MovoTeal,
                                        focusedLabelColor = MovoTeal
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteAccount(deletePassword)
                                    showDeleteDialog = false
                                    deletePassword = ""
                                }
                            ) {
                                Text(
                                    text = stringResource(Res.string.delete),
                                    color = MovoError
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDeleteDialog = false }
                            ) {
                                Text(stringResource(Res.string.cancel))
                            }
                        }
                    )
                }

                if (showPasswordDialog) {
                    AlertDialog(
                        onDismissRequest = { showPasswordDialog = false },
                        title = {
                            Text(
                                text = stringResource(Res.string.profile_change_password),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentPassword,
                                    onValueChange = { currentPassword = it },
                                    label = { Text(stringResource(Res.string.profile_current_password)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MovoTeal,
                                        focusedLabelColor = MovoTeal
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text(stringResource(Res.string.profile_new_password)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MovoTeal,
                                        focusedLabelColor = MovoTeal
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.updatePassword(currentPassword, newPassword)
                                    showPasswordDialog = false
                                    currentPassword = ""
                                    newPassword = ""
                                }
                            ) {
                                Text(stringResource(Res.string.save))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showPasswordDialog = false }
                            ) {
                                Text(stringResource(Res.string.cancel))
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.saveChanges() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = MovoWhite,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.profile_save_changes),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    fullName: String,
    memberSince: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MovoTeal.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MovoTeal,
                    modifier = Modifier.size(48.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MovoTeal)
                    .border(2.dp, MovoWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit profile picture",
                    tint = MovoWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MovoOnSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (memberSince.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MovoTeal.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.profile_member_since, memberSince),
                    style = MaterialTheme.typography.labelMedium,
                    color = MovoTeal
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MovoOnSurfaceVariant
    )
}

@Composable
private fun PersonalDetailsCard(
    fullName: String,
    email: String,
    phone: String,
    address: String,
    dateOfBirth: String,
    fiscalCode: String,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text(stringResource(Res.string.profile_full_name)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MovoOnSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MovoTeal,
                    focusedLabelColor = MovoTeal
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { },
                label = { Text(stringResource(Res.string.common_email)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MovoOnSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MovoOutline,
                    disabledTextColor = MovoOnSurfaceVariant,
                    disabledLabelColor = MovoOnSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text(stringResource(Res.string.profile_phone_number)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MovoOnSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MovoTeal,
                    focusedLabelColor = MovoTeal
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text(stringResource(Res.string.profile_address)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MovoOnSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MovoTeal,
                    focusedLabelColor = MovoTeal
                ),
                shape = RoundedCornerShape(8.dp)
            )

            if (dateOfBirth.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { },
                    label = { Text(stringResource(Res.string.profile_date_of_birth)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MovoOnSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MovoOutline,
                        disabledTextColor = MovoOnSurfaceVariant,
                        disabledLabelColor = MovoOnSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            if (fiscalCode.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fiscalCode,
                    onValueChange = { },
                    label = { Text(stringResource(Res.string.profile_fiscal_code)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MovoOnSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MovoOutline,
                        disabledTextColor = MovoOnSurfaceVariant,
                        disabledLabelColor = MovoOnSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
private fun LicenseCard(
    verified: Boolean,
    expiryDate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MovoOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(Res.string.profile_drivers_license),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MovoOnSurface
                )

                if (expiryDate.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.profile_license_valid_until, expiryDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MovoOnSurfaceVariant
                    )
                }
            }

            if (verified) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MovoSuccess.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.profile_license_verified),
                        style = MaterialTheme.typography.labelSmall,
                        color = MovoSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    pushEnabled: Boolean,
    emailEnabled: Boolean,
    onPushToggle: (Boolean) -> Unit,
    onEmailToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MovoOnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.profile_push_notifications),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MovoOnSurface
                    )

                    Text(
                        text = stringResource(Res.string.profile_push_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MovoOnSurfaceVariant
                    )
                }

                Switch(
                    checked = pushEnabled,
                    onCheckedChange = onPushToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MovoWhite,
                        checkedTrackColor = MovoTeal,
                        uncheckedThumbColor = MovoWhite,
                        uncheckedTrackColor = MovoOutlineVariant
                    )
                )
            }

            HorizontalDivider(color = MovoOutline)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MovoOnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.profile_email_updates),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MovoOnSurface
                    )

                    Text(
                        text = stringResource(Res.string.profile_email_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MovoOnSurfaceVariant
                    )
                }

                Switch(
                    checked = emailEnabled,
                    onCheckedChange = onEmailToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MovoWhite,
                        checkedTrackColor = MovoTeal,
                        uncheckedThumbColor = MovoWhite,
                        uncheckedTrackColor = MovoOutlineVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun NotificationSettingsRow(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MovoOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(Res.string.profile_notification_settings),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MovoOnSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MovoOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ChangePasswordRow(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MovoOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(Res.string.profile_change_password),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MovoOnSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MovoOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun NavigationRow(label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MovoOnSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MovoOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun MovoPointsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoTeal.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovoTeal.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MovoTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        color = MovoWhite,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.profile_movo_points),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MovoOnSurface
                )
            }
            Text(
                text = "0 pts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MovoTeal
            )
        }
    }
}



