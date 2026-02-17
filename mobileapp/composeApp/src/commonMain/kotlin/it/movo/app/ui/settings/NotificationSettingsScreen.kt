package it.movo.app.ui.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoOutlineVariant
import it.movo.app.ui.theme.MovoSuccess
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoWhite
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.back
import it.movo.app.composeapp.generated.resources.notification_alerts
import it.movo.app.composeapp.generated.resources.notifications_booking_reminders
import it.movo.app.composeapp.generated.resources.notifications_email
import it.movo.app.composeapp.generated.resources.notifications_promotional
import it.movo.app.composeapp.generated.resources.notifications_push
import it.movo.app.composeapp.generated.resources.notifications_rental_alerts
import it.movo.app.composeapp.generated.resources.notifications_saved
import it.movo.app.composeapp.generated.resources.notifications_sms
import it.movo.app.composeapp.generated.resources.notifications_title
import it.movo.app.composeapp.generated.resources.notifications_transactional
import it.movo.app.composeapp.generated.resources.profile_save_changes
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.notifications_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
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
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MovoTeal)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SectionTitle(stringResource(Res.string.notifications_promotional))

                Spacer(modifier = Modifier.height(8.dp))

                NotificationCard {
                    ToggleRow(
                        icon = Icons.Default.Notifications,
                        label = stringResource(Res.string.notifications_push),
                        checked = uiState.promotionalPush,
                        onCheckedChange = { viewModel.onPromotionalPushToggle(it) }
                    )
                    HorizontalDivider(color = MovoOutline)
                    ToggleRow(
                        icon = Icons.Default.Email,
                        label = stringResource(Res.string.notifications_email),
                        checked = uiState.promotionalEmail,
                        onCheckedChange = { viewModel.onPromotionalEmailToggle(it) }
                    )
                    HorizontalDivider(color = MovoOutline)
                    ToggleRow(
                        icon = Icons.Default.Sms,
                        label = stringResource(Res.string.notifications_sms),
                        checked = uiState.promotionalSms,
                        onCheckedChange = { viewModel.onPromotionalSmsToggle(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle(stringResource(Res.string.notifications_transactional))

                Spacer(modifier = Modifier.height(8.dp))

                NotificationCard {
                    ToggleRow(
                        icon = Icons.Default.Notifications,
                        label = stringResource(Res.string.notifications_push),
                        checked = uiState.transactionalPush,
                        onCheckedChange = { viewModel.onTransactionalPushToggle(it) }
                    )
                    HorizontalDivider(color = MovoOutline)
                    ToggleRow(
                        icon = Icons.Default.Email,
                        label = stringResource(Res.string.notifications_email),
                        checked = uiState.transactionalEmail,
                        onCheckedChange = { viewModel.onTransactionalEmailToggle(it) }
                    )
                    HorizontalDivider(color = MovoOutline)
                    ToggleRow(
                        icon = Icons.Default.Sms,
                        label = stringResource(Res.string.notifications_sms),
                        checked = uiState.transactionalSms,
                        onCheckedChange = { viewModel.onTransactionalSmsToggle(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle(stringResource(Res.string.notification_alerts))

                Spacer(modifier = Modifier.height(8.dp))

                NotificationCard {
                    ToggleRow(
                        icon = Icons.Default.Notifications,
                        label = stringResource(Res.string.notifications_booking_reminders),
                        checked = uiState.bookingReminders,
                        onCheckedChange = { viewModel.onBookingRemindersToggle(it) }
                    )
                    HorizontalDivider(color = MovoOutline)
                    ToggleRow(
                        icon = Icons.Default.Notifications,
                        label = stringResource(Res.string.notifications_rental_alerts),
                        checked = uiState.rentalAlerts,
                        onCheckedChange = { viewModel.onRentalAlertsToggle(it) }
                    )
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (uiState.saved) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(Res.string.notifications_saved),
                        color = MovoSuccess,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.savePreferences() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MovoWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.profile_save_changes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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
private fun NotificationCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface),
        border = BorderStroke(1.dp, MovoOutline)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MovoOnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MovoOnSurface,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MovoWhite,
                checkedTrackColor = MovoTeal,
                uncheckedThumbColor = MovoWhite,
                uncheckedTrackColor = MovoOutlineVariant
            )
        )
    }
}
