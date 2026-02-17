package it.movo.app.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import it.movo.app.data.model.VehicleMapItem
import it.movo.app.data.model.VehicleStatus
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSuccess
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoTheme
import it.movo.app.ui.theme.MovoWarning
import it.movo.app.ui.theme.MovoWhite
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.filter_apply
import it.movo.app.composeapp.generated.resources.filter_max_distance
import it.movo.app.composeapp.generated.resources.filter_max_price
import it.movo.app.composeapp.generated.resources.filter_min_battery
import it.movo.app.composeapp.generated.resources.filter_reset
import it.movo.app.composeapp.generated.resources.filter_title
import it.movo.app.composeapp.generated.resources.map_search_placeholder
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onBookVehicle: (String) -> Unit,
    onReserveVehicle: (String) -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    MapContent(
        uiState = uiState,
        onVehicleMarkerClick = viewModel::onVehicleMarkerClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToggleFilterSheet = viewModel::toggleFilterSheet,
        onVehiclePreviewDismiss = viewModel::onVehiclePreviewDismiss,
        onShowVehicleDetails = viewModel::onShowVehicleDetails,
        onVehicleDetailsDismiss = viewModel::onVehicleDetailsDismiss,
        onBookVehicle = onBookVehicle,
        onReserveVehicle = onReserveVehicle,
        onNavigateToVehicleDetail = onNavigateToVehicleDetail,
        onMinBatteryChange = viewModel::onMinBatteryChange,
        onMaxPriceChange = viewModel::onMaxPriceChange,
        onMaxDistanceChange = viewModel::onMaxDistanceChange,
        onApplyFilters = viewModel::applyFilters,
        onResetFilters = viewModel::resetFilters,
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapContent(
    uiState: MapUiState,
    onVehicleMarkerClick: (VehicleMapItem) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleFilterSheet: () -> Unit,
    onVehiclePreviewDismiss: () -> Unit,
    onShowVehicleDetails: (String) -> Unit,
    onVehicleDetailsDismiss: () -> Unit,
    onBookVehicle: (String) -> Unit,
    onReserveVehicle: (String) -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    onMinBatteryChange: (Int) -> Unit,
    onMaxPriceChange: (Double) -> Unit,
    onMaxDistanceChange: (Double) -> Unit,
    onApplyFilters: () -> Unit,
    onResetFilters: () -> Unit,
    onClearError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PlatformMap(
            vehicles = uiState.vehicles,
            parkingAreas = uiState.parkingAreas,
            onVehicleClick = onVehicleMarkerClick,
            modifier = Modifier.fillMaxSize()
        )

        // Search bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange,
            onFilterClick = onToggleFilterSheet,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        // My location FAB
        FloatingActionButton(
            onClick = { /* TODO: Get current location */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp),
            containerColor = MovoWhite,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My location",
                tint = MovoTeal
            )
        }

        // Vehicle preview sheet
        AnimatedVisibility(
            visible = uiState.selectedPreview != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            uiState.selectedPreview?.let { vehicle ->
                VehiclePreviewSheet(
                    vehicle = vehicle,
                    onDismiss = onVehiclePreviewDismiss,
                    onReserveClick = {
                        onShowVehicleDetails(vehicle.id)
                    },
                    onExpandClick = {
                        onNavigateToVehicleDetail(vehicle.id)
                    }
                )
            }
        }

        // Vehicle details sheet
        AnimatedVisibility(
            visible = uiState.showVehicleDetails,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            uiState.selectedVehicle?.let { vehicle ->
                VehicleDetailsSheet(
                    vehicle = vehicle,
                    onDismiss = onVehicleDetailsDismiss,
                    onBookClick = { onBookVehicle(vehicle.id) },
                    onReserveClick = { onReserveVehicle(vehicle.id) }
                )
            }
        }

        // Filter Sheet
        if (uiState.showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = onToggleFilterSheet,
                sheetState = sheetState,
                containerColor = MovoSurface
            ) {
                FilterSheetContent(
                    minBattery = uiState.minBattery,
                    maxPrice = uiState.maxPrice,
                    maxDistance = uiState.maxDistance,
                    onMinBatteryChange = onMinBatteryChange,
                    onMaxPriceChange = onMaxPriceChange,
                    onMaxDistanceChange = onMaxDistanceChange,
                    onApply = onApplyFilters,
                    onReset = onResetFilters
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FilterSheetContent(
    minBattery: Int,
    maxPrice: Double,
    maxDistance: Double,
    onMinBatteryChange: (Int) -> Unit,
    onMaxPriceChange: (Double) -> Unit,
    onMaxDistanceChange: (Double) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(Res.string.filter_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Battery Filter
        Text(
            text = stringResource(Res.string.filter_min_battery, minBattery),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = minBattery.toFloat(),
            onValueChange = { onMinBatteryChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = MovoTeal,
                activeTrackColor = MovoTeal,
                inactiveTrackColor = MovoOutline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Price Filter
        val displayPrice = if (maxPrice == Double.MAX_VALUE) 1.0 else maxPrice
        Text(
            text = if (maxPrice == Double.MAX_VALUE) "No price limit" else stringResource(
                Res.string.filter_max_price,
                maxPrice
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = displayPrice.toFloat(),
            onValueChange = { onMaxPriceChange(it.toDouble()) },
            valueRange = 0.1f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = MovoTeal,
                activeTrackColor = MovoTeal,
                inactiveTrackColor = MovoOutline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Distance Filter
        val displayDistance = if (maxDistance == Double.MAX_VALUE) 10.0 else maxDistance
        Text(
            text = if (maxDistance == Double.MAX_VALUE) "No distance limit" else stringResource(
                Res.string.filter_max_distance,
                maxDistance
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = displayDistance.toFloat(),
            onValueChange = { onMaxDistanceChange(it.toDouble()) },
            valueRange = 1f..10f,
            colors = SliderDefaults.colors(
                thumbColor = MovoTeal,
                activeTrackColor = MovoTeal,
                inactiveTrackColor = MovoOutline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MovoOutline.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(Res.string.filter_reset))
            }

            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MovoTeal,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(Res.string.filter_apply))
            }
        }
    }
}

@Composable
private fun MapPlaceholder(
    vehicles: List<VehicleMapItem>,
    parkingAreas: List<it.movo.app.data.model.ParkingArea>,
    onVehicleClick: (VehicleMapItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)) // Light green map background
    ) {
        // Grid lines to simulate map
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

        // Vehicle markers (positioned absolutely for demo)
        vehicles.take(5).forEachIndexed { index, vehicle ->
            val offsets = listOf(
                Pair(0.3f, 0.4f),
                Pair(0.6f, 0.3f),
                Pair(0.45f, 0.6f),
                Pair(0.25f, 0.7f),
                Pair(0.7f, 0.5f)
            )
            val (xOffset, yOffset) = offsets.getOrElse(index) { Pair(0.5f, 0.5f) }

            VehicleMarker(
                vehicle = vehicle,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = (xOffset * 300).dp,
                        top = (yOffset * 500).dp
                    ),
                onClick = { onVehicleClick(vehicle) }
            )
        }
    }
}

@Composable
private fun VehicleMarker(
    vehicle: VehicleMapItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val batteryColor = when {
        vehicle.batteryLevel >= 60 -> MovoSuccess
        vehicle.batteryLevel >= 30 -> MovoWarning
        else -> Color(0xFFDC2626) // Red
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        // Battery badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MovoWhite),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${vehicle.batteryLevel}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = batteryColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Car icon circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MovoWhite)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🚗",
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MovoOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(Res.string.map_search_placeholder)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MovoTeal,
                    unfocusedBorderColor = MovoSurface,
                    focusedContainerColor = MovoSurface,
                    unfocusedContainerColor = MovoSurface
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = MovoTeal,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onFilterClick() }
            )
        }
    }
}

@Preview
@Composable
private fun MapScreenPreview() {
    MovoTheme {
        MapContent(
            uiState = MapUiState(
                vehicles = listOf(
                    VehicleMapItem(
                        id = "1",
                        model = "Tesla Model 3",
                        licensePlate = "AB123CD",
                        location = it.movo.app.data.model.GeoPoint(
                            type = "Point",
                            coordinates = listOf(12.5, 41.9)
                        ),
                        batteryLevel = 85,
                        status = VehicleStatus.AVAILABLE,
                        basePricePerMinute = 0.25
                    ),
                    VehicleMapItem(
                        id = "2",
                        model = "Fiat 500e",
                        licensePlate = "XY789ZW",
                        location = it.movo.app.data.model.GeoPoint(
                            type = "Point",
                            coordinates = listOf(12.6, 41.95)
                        ),
                        batteryLevel = 42,
                        status = VehicleStatus.AVAILABLE,
                        basePricePerMinute = 0.20
                    ),
                    VehicleMapItem(
                        id = "3",
                        model = "Renault Zoe",
                        licensePlate = "EF456GH",
                        location = it.movo.app.data.model.GeoPoint(
                            type = "Point",
                            coordinates = listOf(12.4, 41.85)
                        ),
                        batteryLevel = 28,
                        status = VehicleStatus.AVAILABLE,
                        basePricePerMinute = 0.18
                    )
                ),
                parkingAreas = emptyList(),
                searchQuery = "",
                minBattery = 0,
                maxPrice = Double.MAX_VALUE,
                maxDistance = Double.MAX_VALUE,
                showFilterSheet = false,
                isLoading = false,
                showVehicleDetails = false,
                errorMessage = null
            ),
            onVehicleMarkerClick = {},
            onSearchQueryChange = {},
            onToggleFilterSheet = {},
            onVehiclePreviewDismiss = {},
            onShowVehicleDetails = {},
            onVehicleDetailsDismiss = {},
            onBookVehicle = {},
            onReserveVehicle = {},
            onNavigateToVehicleDetail = {},
            onMinBatteryChange = {},
            onMaxPriceChange = {},
            onMaxDistanceChange = {},
            onApplyFilters = {},
            onResetFilters = {},
            onClearError = {}
        )
    }
}
