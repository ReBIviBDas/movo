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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSuccess
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoTheme
import it.movo.app.ui.theme.MovoWhite
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.retry
import it.movo.app.composeapp.generated.resources.trip_co2_saved
import it.movo.app.composeapp.generated.resources.trip_distance
import it.movo.app.composeapp.generated.resources.trip_done
import it.movo.app.composeapp.generated.resources.trip_duration
import it.movo.app.composeapp.generated.resources.trip_paid
import it.movo.app.composeapp.generated.resources.trip_rate
import it.movo.app.composeapp.generated.resources.trip_split_bill
import it.movo.app.composeapp.generated.resources.trip_summary_title
import it.movo.app.composeapp.generated.resources.trip_view_breakdown
import org.jetbrains.compose.resources.stringResource

@Composable
fun TripSummaryScreen(
    viewModel: TripSummaryViewModel,
    onDone: () -> Unit,
    onSplitPayment: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    TripSummaryContent(
        uiState = uiState,
        onDone = onDone,
        onSplitPayment = onSplitPayment,
        onSplitBillToggle = viewModel::onSplitBillToggle,
        onRatingChange = viewModel::onRatingChange,
        onRetry = viewModel::retry,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun TripSummaryContent(
    uiState: TripSummaryUiState,
    onDone: () -> Unit,
    onSplitPayment: () -> Unit,
    onSplitBillToggle: (Boolean) -> Unit,
    onRatingChange: (Int) -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = { TopBar(onClose = onDone) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MovoTeal)
            }
            return@Scaffold
        }

        if (uiState.errorMessage != null && uiState.vehicleName.isEmpty()) {
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
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedButton(
                        onClick = onRetry
                    ) {
                        Text(stringResource(Res.string.retry))
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MovoSurface)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle name
            if (uiState.vehicleName.isNotBlank()) {
                Text(
                    text = uiState.vehicleName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Route map placeholder
            RouteMapPlaceholder()

            Spacer(modifier = Modifier.height(24.dp))

            // Total cost
            Text(
                text = uiState.totalCostText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // View breakdown link
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.trip_view_breakdown),
                    color = MovoTeal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = MovoTeal
                )
            }

            // Cost breakdown
            if (uiState.discountAppliedCents > 0 && uiState.discountAppliedCents <= uiState.totalCostCents) {
                Spacer(modifier = Modifier.height(8.dp))
                val totalEuros = uiState.totalCostCents / 100
                val totalCents = uiState.totalCostCents % 100
                val discountEuros = uiState.discountAppliedCents / 100
                val discountCents = uiState.discountAppliedCents % 100
                Column(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Subtotale: €${totalEuros}.${
                            totalCents.toString().padStart(2, '0')
                        }",
                        fontSize = 14.sp,
                        color = MovoOnSurfaceVariant
                    )
                    Text(
                        text = "Sconto: -€${discountEuros}.${
                            discountCents.toString().padStart(2, '0')
                        }",
                        fontSize = 14.sp,
                        color = MovoSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats cards
            StatsCardsRow(uiState = uiState)

            Spacer(modifier = Modifier.height(24.dp))

            // Payment info card
            PaymentInfoCard(
                paymentMethod = uiState.paymentMethod,
                paymentLabel = uiState.paymentLabel,
                isPaid = uiState.isPaid,
                splitBillEnabled = uiState.splitBillEnabled,
                onSplitBillToggle = onSplitBillToggle,
                onSplitPaymentClick = onSplitPayment
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Rating section
            RatingSection(
                rating = uiState.rating,
                onRatingChange = onRatingChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Done button
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MovoTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.trip_done),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopBar(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Black
            )
        }

        Text(
            text = stringResource(Res.string.trip_summary_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun RouteMapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8F5E9))
    ) {
        // Grid lines
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(6) { index ->
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

        // Route line (simulated)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .background(MovoTeal, RoundedCornerShape(2.dp))
            )
        }

        // Start and end markers
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 48.dp)
                .size(16.dp)
                .background(MovoTeal, CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 48.dp)
                .size(16.dp)
                .background(MovoSuccess, CircleShape)
        )
    }
}

@Composable
private fun StatsCardsRow(uiState: TripSummaryUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Duration card
        StatCard(
            icon = Icons.Default.AccessTime,
            label = stringResource(Res.string.trip_duration),
            value = "${uiState.durationMinutes} min",
            isHighlighted = false,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Distance card
        StatCard(
            icon = Icons.Default.LocationOn,
            label = stringResource(Res.string.trip_distance),
            value = "${uiState.distanceKm} km",
            isHighlighted = false,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // CO2 card (highlighted)
        StatCard(
            icon = Icons.Default.Eco,
            label = stringResource(Res.string.trip_co2_saved),
            value = "${uiState.co2SavedGrams}g",
            isHighlighted = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isHighlighted) Color(0xFFB2EEEC) else MovoSurface
    val borderColor = if (isHighlighted) MovoTeal else MovoOutline

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon in circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, MovoOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isHighlighted) MovoSuccess else MovoOnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MovoOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun PaymentInfoCard(
    paymentMethod: String,
    paymentLabel: String,
    isPaid: Boolean,
    splitBillEnabled: Boolean,
    onSplitBillToggle: (Boolean) -> Unit,
    onSplitPaymentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovoOutline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row: Visa info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Visa icon circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF3F4F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            tint = Color(0xFF1A1F71),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = paymentMethod,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            text = paymentLabel,
                            color = MovoOnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // PAID badge
                if (isPaid) {
                    Box(
                        modifier = Modifier
                            .background(MovoSuccess, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.trip_paid),
                            color = MovoWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MovoOutline)

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: Split bill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSplitPaymentClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MovoTeal,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = stringResource(Res.string.trip_split_bill),
                        fontSize = 14.sp
                    )
                }

                Switch(
                    checked = splitBillEnabled,
                    onCheckedChange = onSplitBillToggle
                )
            }
        }
    }
}

@Composable
private fun RatingSection(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.trip_rate),
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Star rating
        Row {
            repeat(5) { index ->
                val starNumber = index + 1
                val isFilled = starNumber <= rating

                Icon(
                    imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star $starNumber",
                    tint = if (isFilled) MovoTeal else MovoOutline,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onRatingChange(starNumber) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun TripSummaryScreenPreview() {
    MovoTheme {
        TripSummaryContent(
            uiState = TripSummaryUiState(
                vehicleName = "Fiat 500e",
                totalCostCents = 1450,
                finalCostCents = 1450,
                durationMinutes = 32,
                distanceKm = 8.5,
                co2SavedGrams = 420,
                paymentMethod = "Visa",
                paymentLabel = "****4242",
                isPaid = true,
                splitBillEnabled = false,
                rating = 4
            ),
            onDone = {},
            onSplitPayment = {},
            onSplitBillToggle = {},
            onRatingChange = {},
            onRetry = {}
        )
    }
}
