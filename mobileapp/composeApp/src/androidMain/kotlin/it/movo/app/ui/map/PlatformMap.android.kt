package it.movo.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import it.movo.app.data.model.ParkingArea
import it.movo.app.data.model.VehicleMapItem

@Composable
actual fun PlatformMap(
    vehicles: List<VehicleMapItem>,
    parkingAreas: List<ParkingArea>,
    onVehicleClick: (VehicleMapItem) -> Unit,
    modifier: Modifier
) {
    val trentoLocation = LatLng(46.0748, 11.1217)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(trentoLocation, 14f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(zoomControlsEnabled = false)
    ) {
        vehicles.forEach { vehicle ->
            val position = LatLng(vehicle.location.latitude, vehicle.location.longitude)
            val markerHue = batteryToHue(vehicle.batteryLevel)

            MarkerInfoWindowContent(
                state = rememberMarkerState(key = vehicle.id, position = position),
                title = vehicle.model,
                icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                onInfoWindowClick = { onVehicleClick(vehicle) }
            ) {
                VehicleInfoWindow(vehicle)
            }
        }
    }
}

private fun batteryToHue(batteryLevel: Int): Float = when {
    batteryLevel >= 60 -> BitmapDescriptorFactory.HUE_GREEN
    batteryLevel >= 30 -> BitmapDescriptorFactory.HUE_YELLOW
    else -> BitmapDescriptorFactory.HUE_RED
}

@Composable
private fun VehicleInfoWindow(vehicle: VehicleMapItem) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = vehicle.model,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BatteryIndicator(vehicle.batteryLevel)
            Text(
                text = "${vehicle.batteryLevel}%",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
        if (vehicle.basePricePerMinute > 0.0) {
            val euros = vehicle.basePricePerMinute.toInt()
            val cents = ((vehicle.basePricePerMinute - euros) * 100).toInt()
            Text(
                text = "${euros}.${cents.toString().padStart(2, '0')} €/min",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            text = "Tap for details",
            fontSize = 10.sp,
            color = Color(0xFF00897B)
        )
    }
}

@Composable
private fun BatteryIndicator(level: Int) {
    val color = when {
        level >= 60 -> Color(0xFF4CAF50)
        level >= 30 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
        drawCircle(color = color)
    }
}
