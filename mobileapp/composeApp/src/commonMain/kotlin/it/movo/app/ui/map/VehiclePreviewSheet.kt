package it.movo.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.movo.app.data.model.VehicleMapItem
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSuccess
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoTealContainer
import it.movo.app.ui.theme.MovoWarning
import it.movo.app.ui.theme.MovoWhite
import movo.composeapp.generated.resources.Res
import movo.composeapp.generated.resources.close
import movo.composeapp.generated.resources.vehicle_battery
import movo.composeapp.generated.resources.vehicle_range
import movo.composeapp.generated.resources.vehicle_reserve_free
import org.jetbrains.compose.resources.stringResource

@Composable
fun VehiclePreviewSheet(
    vehicle: VehicleMapItem,
    distance: String? = null,
    onDismiss: () -> Unit,
    onReserveClick: () -> Unit,
    onExpandClick: () -> Unit
) {
    val batteryColor = when {
        vehicle.batteryLevel >= 60 -> MovoSuccess
        vehicle.batteryLevel >= 30 -> MovoWarning
        else -> Color(0xFFDC2626) // Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onExpandClick),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MovoOutline)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main content row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Car thumbnail placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MovoTealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚗",
                        fontSize = 40.sp
                    )

                    // Distance badge
                    distance?.let {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MovoWhite)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = it,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MovoOnSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Vehicle info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vehicle.model,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MovoOnSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${vehicle.licensePlate} • 4 seats",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MovoOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Battery and range
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Battery
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(batteryColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${vehicle.batteryLevel}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MovoTeal
                            )
                        }

                        // Range (estimate)
                        Text(
                            text = "${(vehicle.batteryLevel * 3)}km range",
                            fontSize = 14.sp,
                            color = MovoOnSurfaceVariant
                        )
                    }
                }

                // Close button
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MovoOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reserve button
            Button(
                onClick = onReserveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MovoTeal)
            ) {
                Text(
                    text = "${stringResource(Res.string.vehicle_reserve_free)} (15 min)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
