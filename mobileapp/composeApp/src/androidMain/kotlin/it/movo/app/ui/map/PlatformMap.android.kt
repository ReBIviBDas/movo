package it.movo.app.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptor
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
            val markerIcon = remember(vehicle.id, vehicle.batteryLevel) {
                createVehicleBitmapDescriptor(vehicle.batteryLevel)
            }

            MarkerInfoWindowContent(
                state = rememberMarkerState(key = vehicle.id, position = position),
                title = vehicle.model,
                icon = markerIcon,
                onClick = {
                    onVehicleClick(vehicle)
                    false
                },
                onInfoWindowClick = { onVehicleClick(vehicle) }
            ) {
                VehicleInfoWindow(vehicle)
            }
        }
    }
}

private fun createVehicleBitmapDescriptor(batteryLevel: Int): BitmapDescriptor {
    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val batteryColor = when {
        batteryLevel >= 60 -> android.graphics.Color.rgb(76, 175, 80)
        batteryLevel >= 30 -> android.graphics.Color.rgb(255, 193, 7)
        else -> android.graphics.Color.rgb(244, 67, 54)
    }

    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = batteryColor
        style = Paint.Style.FILL
    }
    val cx = size / 2f
    val cy = size / 2f
    canvas.drawCircle(cx, cy, size / 2f, outerPaint)

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, size / 2f - 6f, innerPaint)

    val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    canvas.drawText("\uD83D\uDE97", cx, cy + 14f, emojiPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
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
