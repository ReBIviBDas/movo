package it.movo.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
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
    val defaultLocation = LatLng(41.9028, 12.4964)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 13f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(zoomControlsEnabled = false)
    ) {
        vehicles.forEach { vehicle ->
            val position = LatLng(vehicle.location.latitude, vehicle.location.longitude)
            Marker(
                state = rememberMarkerState(position = position),
                title = vehicle.model,
                snippet = "Battery: ${vehicle.batteryLevel}%",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                onClick = {
                    onVehicleClick(vehicle)
                    true
                }
            )
        }
    }
}
