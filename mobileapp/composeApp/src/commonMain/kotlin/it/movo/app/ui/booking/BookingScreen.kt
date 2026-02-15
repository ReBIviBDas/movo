package it.movo.app.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import it.movo.app.ui.theme.MovoTheme
import it.movo.app.data.model.VehicleMapItem
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoOutlineVariant
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoWhite
import movo.composeapp.generated.resources.Res
import movo.composeapp.generated.resources.booking_cancel
import movo.composeapp.generated.resources.booking_check_damages
import movo.composeapp.generated.resources.booking_plate
import movo.composeapp.generated.resources.booking_reservation_active
import movo.composeapp.generated.resources.booking_time_to_reach
import movo.composeapp.generated.resources.booking_unlock_app
import movo.composeapp.generated.resources.booking_unlock_vehicle
import movo.composeapp.generated.resources.booking_walk_time
import movo.composeapp.generated.resources.booking_walk_to
import movo.composeapp.generated.resources.vehicle_battery
import org.jetbrains.compose.resources.stringResource

@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    onNavigateBack: () -> Unit,
    onRentalStarted: (String) -> Unit,
    onBookingCancelled: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.rentalStarted) {
        if (uiState.rentalStarted) {
            val rentalId = uiState.booking?.id ?: return@LaunchedEffect
            onRentalStarted(rentalId)
        }
    }

    LaunchedEffect(uiState.bookingCancelled) {
        if (uiState.bookingCancelled) {
            onBookingCancelled()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    BookingContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUnlockVehicle = viewModel::unlockVehicle,
        onCancelBooking = viewModel::cancelBooking,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun BookingContent(
    uiState: BookingUiState,
    onNavigateBack: () -> Unit,
    onUnlockVehicle: () -> Unit,
    onCancelBooking: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.45f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MovoTeal.copy(alpha = 0.3f),
                                    MovoTeal.copy(alpha = 0.1f),
                                    MovoWhite
                                )
                            )
                        )
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .clip(CircleShape)
                            .background(MovoWhite)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MovoOnSurface
                        )
                    }

                    FloatingActionButton(
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(40.dp),
                        containerColor = MovoWhite,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Navigate",
                            tint = MovoTeal
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MovoTeal,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.55f),
                    color = MovoSurface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MovoOutline)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(Res.string.booking_reservation_active),
                            style = MaterialTheme.typography.labelMedium,
                            color = MovoOnSurfaceVariant,
                            letterSpacing = 2.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = uiState.timerText,
                            style = MaterialTheme.typography.displayLarge,
                            color = MovoTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 56.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = stringResource(Res.string.booking_time_to_reach),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MovoOnSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        VehicleInfoCard(
                            vehicle = uiState.booking?.vehicle,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        ChecklistSection(
                            address = "Vehicle location",
                            walkMinutes = 0,
                            walkMeters = 0,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onUnlockVehicle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isProcessing && !uiState.isExpired
                        ) {
                            Icon(
                                imageVector = if (uiState.isUnlocking) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = if (uiState.isUnlocking) "Unlocking vehicle" else "Unlock vehicle",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.booking_unlock_vehicle),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        TextButton(
                            onClick = onCancelBooking,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            enabled = !uiState.isProcessing
                        ) {
                            Text(
                                text = stringResource(Res.string.booking_cancel),
                                color = MovoOnSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleInfoCard(
    vehicle: VehicleMapItem?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MovoOutlineVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚗",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle?.model ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MovoOnSurface
                )

                Text(
                    text = stringResource(Res.string.booking_plate, vehicle?.licensePlate ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MovoOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MovoTeal.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryFull,
                            contentDescription = null,
                            tint = MovoTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = stringResource(Res.string.vehicle_battery, vehicle?.batteryLevel ?: 0),
                            style = MaterialTheme.typography.labelSmall,
                            color = MovoTeal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistSection(
    address: String,
    walkMinutes: Int,
    walkMeters: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ChecklistItem(
            isCompleted = true,
            isFirst = true,
            title = stringResource(Res.string.booking_walk_to, address),
            subtitle = if (walkMinutes > 0 || walkMeters > 0) stringResource(Res.string.booking_walk_time, walkMinutes, walkMeters) else null,
            showNavigateIcon = true
        )

        ChecklistItem(
            isCompleted = false,
            title = stringResource(Res.string.booking_check_damages),
            subtitle = null,
            showNavigateIcon = false
        )

        ChecklistItem(
            isCompleted = false,
            isLast = true,
            title = stringResource(Res.string.booking_unlock_app),
            subtitle = null,
            showNavigateIcon = false
        )
    }
}

@Composable
private fun ChecklistItem(
    isCompleted: Boolean,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    title: String,
    subtitle: String?,
    showNavigateIcon: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(if (isCompleted) MovoTeal else MovoOutline)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Circle,
                    contentDescription = null,
                    tint = MovoTeal,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = MovoOutline,
                    modifier = Modifier.size(12.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(MovoOutline)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCompleted) FontWeight.Medium else FontWeight.Normal,
                    color = if (isCompleted) MovoOnSurface else MovoOnSurfaceVariant
                )

                if (showNavigateIcon) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = "Navigate",
                        tint = MovoTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MovoOnSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun BookingScreenPreview() {
    MovoTheme {
        BookingContent(
            uiState = BookingUiState(
                remainingSeconds = 899
            ),
            onNavigateBack = {},
            onUnlockVehicle = {},
            onCancelBooking = {}
        )
    }
}
