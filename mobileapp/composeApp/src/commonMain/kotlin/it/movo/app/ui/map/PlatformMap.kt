package it.movo.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.movo.app.data.model.ParkingArea
import it.movo.app.data.model.VehicleMapItem

@Composable
expect fun PlatformMap(
    vehicles: List<VehicleMapItem>,
    parkingAreas: List<ParkingArea>,
    onVehicleClick: (VehicleMapItem) -> Unit,
    modifier: Modifier = Modifier
)
