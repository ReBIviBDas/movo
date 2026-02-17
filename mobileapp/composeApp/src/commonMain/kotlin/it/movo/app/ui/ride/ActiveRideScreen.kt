package it.movo.app.ui.ride

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.movo.app.ui.theme.MovoError
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSuccess
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoTheme
import it.movo.app.ui.theme.MovoWhite
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.back
import it.movo.app.composeapp.generated.resources.ride_active_safe
import it.movo.app.composeapp.generated.resources.ride_connected
import it.movo.app.composeapp.generated.resources.ride_current_cost
import it.movo.app.composeapp.generated.resources.ride_distance
import it.movo.app.composeapp.generated.resources.ride_duration
import it.movo.app.composeapp.generated.resources.ride_end_rental
import it.movo.app.composeapp.generated.resources.ride_lock_car
import it.movo.app.composeapp.generated.resources.ride_near_vehicle
import it.movo.app.composeapp.generated.resources.ride_passengers
import it.movo.app.composeapp.generated.resources.ride_report_issue
import it.movo.app.composeapp.generated.resources.ride_splitting_bill
import it.movo.app.composeapp.generated.resources.ride_swipe_unlock
import it.movo.app.composeapp.generated.resources.ride_total_cost
import it.movo.app.composeapp.generated.resources.ride_vehicle_locked
import it.movo.app.composeapp.generated.resources.ride_vehicle_unlocked
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActiveRideScreen(
    viewModel: ActiveRideViewModel,
    onNavigateBack: () -> Unit,
    onRideEnded: (String) -> Unit,
    onReportIssue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    if (uiState.rideEnded) {
        val rentalId = uiState.rental?.id ?: return
        onRideEnded(rentalId)
        return
    }

    ActiveRideContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onToggleLock = viewModel::toggleLock,
        onEndRental = viewModel::endRental,
        onReportIssue = onReportIssue,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun ActiveRideContent(
    uiState: ActiveRideUiState,
    onNavigateBack: () -> Unit,
    onToggleLock: () -> Unit,
    onEndRental: () -> Unit,
    onReportIssue: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MovoTeal)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Map area with top bar overlay
                    Box(modifier = Modifier.weight(1f)) {
                        MapPlaceholder()

                        // Top bar overlay
                        TopBarOverlay(
                            vehicleId = uiState.vehicleId,
                            vehicleName = uiState.vehicleName,
                            batteryLevel = uiState.batteryLevel,
                            onBackClick = onNavigateBack
                        )
                    }

                    // Content area
                    if (uiState.isVehicleLocked) {
                        LockedStateContent(
                            uiState = uiState,
                            onUnlockClick = onToggleLock,
                            onEndRentalClick = onEndRental,
                            onReportIssueClick = onReportIssue
                        )
                    } else {
                        UnlockedStateContent(
                            uiState = uiState,
                            onLockClick = onToggleLock,
                            onEndRentalClick = onEndRental,
                            onReportIssueClick = onReportIssue
                        )
                    }
                }

                if (uiState.isEnding) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MovoTeal)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarOverlay(
    vehicleId: String,
    vehicleName: String,
    batteryLevel: Int,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MovoTeal.copy(alpha = 0.8f),
                        MovoTeal.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(MovoWhite.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back),
                    tint = Color.Black
                )
            }

            // Center: Vehicle info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = vehicleId,
                    color = MovoWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = vehicleName,
                    color = MovoWhite.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }

            // Battery pill
            BatteryPill(batteryLevel = batteryLevel)
        }
    }
}

@Composable
private fun BatteryPill(batteryLevel: Int) {
    val batteryColor = when {
        batteryLevel >= 60 -> MovoSuccess
        batteryLevel >= 30 -> Color(0xFFEAB308)
        else -> MovoError
    }

    Row(
        modifier = Modifier
            .background(MovoWhite.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.BatteryFull,
            contentDescription = "Battery level",
            tint = batteryColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$batteryLevel%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
private fun MapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(10) { index ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .weight(1f)
                            .background(Color(0xFFC8E6C9))
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MovoTeal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚗",
                    fontSize = 32.sp
                )
            }
        }
    }
}

@Composable
private fun LockedStateContent(
    uiState: ActiveRideUiState,
    onUnlockClick: () -> Unit,
    onEndRentalClick: () -> Unit,
    onReportIssueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MovoSurface)
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Connected badge
        ConnectedBadge()

        Spacer(modifier = Modifier.height(16.dp))

        // Vehicle Locked text
        Text(
            text = stringResource(Res.string.ride_vehicle_locked),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(Res.string.ride_near_vehicle),
            color = MovoOnSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Swipe to Unlock button
        Button(
            onClick = onUnlockClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = "Unlock vehicle",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.ride_swipe_unlock),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Swipe to unlock"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lock Car button
        OutlinedButton(
            onClick = { /* Already locked */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Vehicle locked",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.ride_lock_car))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Metric cards
        MetricCardsRow(
            duration = uiState.durationText,
            cost = uiState.costText,
            distance = uiState.distanceText,
            costLabel = stringResource(Res.string.ride_total_cost)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Report Issue button
        OutlinedButton(
            onClick = onReportIssueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Report,
                contentDescription = "Report issue",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.ride_report_issue))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // End Rental button
        OutlinedButton(
            onClick = onEndRentalClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MovoError
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MovoError)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "End rental",
                tint = MovoError
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(Res.string.ride_end_rental),
                color = MovoError
            )
        }
    }
}

@Composable
private fun UnlockedStateContent(
    uiState: ActiveRideUiState,
    onLockClick: () -> Unit,
    onEndRentalClick: () -> Unit,
    onReportIssueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MovoSurface)
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(MovoOutline, RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Vehicle Unlocked header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MovoSuccess, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.ride_vehicle_unlocked),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Row {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth connected",
                    tint = MovoTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location tracking",
                    tint = MovoTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = stringResource(Res.string.ride_active_safe),
            color = MovoOnSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Passengers section
        PassengersSection(passengers = uiState.passengers)

        Spacer(modifier = Modifier.height(20.dp))

        // Metric cards
        MetricCardsRow(
            duration = uiState.durationText,
            cost = uiState.costText,
            distance = uiState.distanceText,
            costLabel = stringResource(Res.string.ride_current_cost)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Report Issue button
        OutlinedButton(
            onClick = onReportIssueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Report,
                contentDescription = "Report issue",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.ride_report_issue))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // End Rental button (filled red)
        Button(
            onClick = onEndRentalClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MovoError)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "End rental"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.ride_end_rental))
        }
    }
}

@Composable
private fun ConnectedBadge() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = "Bluetooth connected",
            tint = MovoTeal,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(Res.string.ride_connected),
            color = MovoTeal,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PassengersSection(passengers: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.ride_passengers),
            style = MaterialTheme.typography.labelMedium,
            color = MovoOnSurfaceVariant
        )

        // Splitting Bill badge
        Row(
            modifier = Modifier
                .background(MovoTeal.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.ride_splitting_bill),
                color = MovoTeal,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Avatar row
    Row(verticalAlignment = Alignment.CenterVertically) {
        passengers.forEachIndexed { index, name ->
            PassengerAvatar(name = name, isFirst = index == 0)
            if (index < passengers.size - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Add button
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, MovoOutline, CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add passenger",
                tint = MovoOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun PassengerAvatar(name: String, isFirst: Boolean) {
    val bgColor = if (isFirst) MovoTeal else Color(0xFFE5E7EB)
    val textColor = if (isFirst) MovoWhite else Color(0xFF374151)

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MetricCardsRow(
    duration: String,
    cost: String,
    distance: String,
    costLabel: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Duration card
        MetricCard(
            label = stringResource(Res.string.ride_duration),
            value = duration,
            isHighlighted = false,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Cost card
        MetricCard(
            label = costLabel,
            value = cost,
            isHighlighted = true,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Distance card
        MetricCard(
            label = stringResource(Res.string.ride_distance),
            value = distance,
            isHighlighted = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isHighlighted) MovoTeal else MovoOutline
    val textColor = if (isHighlighted) MovoTeal else Color.Black

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MovoOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
        }
    }
}

@Preview
@Composable
private fun ActiveRideScreenPreview() {
    MovoTheme {
        ActiveRideContent(
            uiState = ActiveRideUiState(
                isVehicleLocked = false,
                vehicleName = "Fiat 500e",
                vehicleId = "MV-1234",
                batteryLevel = 72,
                durationSeconds = 1834,
                currentCostCents = 1250,
                distanceKm = 5.3,
                passengers = listOf("Mario", "Luigi")
            ),
            onNavigateBack = {},
            onToggleLock = {},
            onEndRental = {},
            onReportIssue = {}
        )
    }
}
