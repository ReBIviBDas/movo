package it.movo.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import it.movo.app.data.model.ParkingArea
import it.movo.app.data.model.VehicleMapItem

@Composable
actual fun PlatformMap(
    vehicles: List<VehicleMapItem>,
    parkingAreas: List<ParkingArea>,
    onVehicleClick: (VehicleMapItem) -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)),
        contentAlignment = Alignment.Center
    ) {
        Text("Map not available on iOS yet")
    }
}
