package it.movo.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.movo.app.data.model.GeoPoint
import it.movo.app.data.model.RentalSummary
import it.movo.app.data.model.VehicleMapItem
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.history_duration
import it.movo.app.composeapp.generated.resources.history_empty
import it.movo.app.composeapp.generated.resources.history_empty_subtitle
import it.movo.app.composeapp.generated.resources.history_title
import it.movo.app.composeapp.generated.resources.retry
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onRentalClick: (String) -> Unit,
    onReportIssue: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    HistoryContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRetry = { viewModel.loadHistory() },
        onRentalClick = onRentalClick,
        onReportIssue = onReportIssue
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryContent(
    uiState: HistoryUiState,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    onRentalClick: (String) -> Unit,
    onReportIssue: (String) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.history_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MovoSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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

                uiState.rentals.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.history_empty),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.history_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.rentals) { rental ->
                            RentalHistoryCard(
                                rental = rental,
                                onClick = { onRentalClick(rental.id) },
                                onReportIssue = { onReportIssue(rental.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RentalHistoryCard(
    rental: RentalSummary,
    onClick: () -> Unit = {},
    onReportIssue: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rental.vehicle?.model ?: "Vehicle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDate(rental.startedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rental.durationMinutes ?: 0} ${stringResource(Res.string.history_duration)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "€${formatCost(rental.finalCostCents)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MovoTeal
                    )
                )
            }

            OutlinedButton(
                onClick = onReportIssue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Report,
                    contentDescription = "Report issue"
                )
                Text("Report Issue")
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun formatDate(dateString: String): String {
    return try {
        val instant = kotlinx.datetime.Instant.parse(dateString)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.day}/${localDateTime.month}/${localDateTime.year}"
    } catch (e: Exception) {
        dateString
    }
}

private fun formatCost(cents: Int): String {
    val euros = cents / 100
    val remainingCents = cents % 100
    return "$euros.${remainingCents.toString().padStart(2, '0')}"
}

@Preview
@Composable
private fun HistoryScreenPreview() {
    val mockRentals = listOf(
        RentalSummary(
            id = "1",
            userId = "user1",
            vehicleId = "v1",
            vehicle = VehicleMapItem(
                id = "v1",
                model = "Fiat 500e",
                licensePlate = "AB123CD",
                location = GeoPoint(coordinates = listOf(12.5, 41.9)),
                batteryLevel = 85,
                basePricePerMinute = 0.25
            ),
            startedAt = "2026-02-10T14:30:00Z",
            durationMinutes = 45,
            finalCostCents = 1125
        ),
        RentalSummary(
            id = "2",
            userId = "user1",
            vehicleId = "v2",
            vehicle = VehicleMapItem(
                id = "v2",
                model = "Vespa Elettrica",
                licensePlate = "EF456GH",
                location = GeoPoint(coordinates = listOf(12.6, 41.8)),
                batteryLevel = 72,
                basePricePerMinute = 0.20
            ),
            startedAt = "2026-02-08T09:15:00Z",
            durationMinutes = 120,
            finalCostCents = 2400
        ),
        RentalSummary(
            id = "3",
            userId = "user1",
            vehicleId = "v3",
            vehicle = VehicleMapItem(
                id = "v3",
                model = "Renault Zoe",
                licensePlate = "IJ789KL",
                location = GeoPoint(coordinates = listOf(12.4, 42.0)),
                batteryLevel = 90,
                basePricePerMinute = 0.28
            ),
            startedAt = "2026-02-05T16:45:00Z",
            durationMinutes = 30,
            finalCostCents = 840
        )
    )

    MovoTheme {
        HistoryContent(
            uiState = HistoryUiState(
                rentals = mockRentals,
                isLoading = false,
                errorMessage = null
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onRentalClick = {},
            onReportIssue = {}
        )
    }
}
