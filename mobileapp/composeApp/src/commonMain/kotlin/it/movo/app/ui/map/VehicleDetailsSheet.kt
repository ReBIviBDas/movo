package it.movo.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.movo.app.data.model.Vehicle
import it.movo.app.ui.theme.MovoOnSurface
import it.movo.app.ui.theme.MovoOnSurfaceVariant
import it.movo.app.ui.theme.MovoOutline
import it.movo.app.ui.theme.MovoSurface
import it.movo.app.ui.theme.MovoSurfaceVariant
import it.movo.app.ui.theme.MovoTeal
import it.movo.app.ui.theme.MovoTealContainer
import it.movo.app.ui.theme.MovoWhite
import it.movo.app.composeapp.generated.resources.Res
import it.movo.app.composeapp.generated.resources.vehicle_available
import it.movo.app.composeapp.generated.resources.vehicle_book_now
import it.movo.app.composeapp.generated.resources.vehicle_distance_label
import it.movo.app.composeapp.generated.resources.vehicle_range_label
import it.movo.app.composeapp.generated.resources.vehicle_reserve_later
import it.movo.app.composeapp.generated.resources.vehicle_seats_label
import it.movo.app.composeapp.generated.resources.vehicle_split_add
import it.movo.app.composeapp.generated.resources.vehicle_split_friends
import it.movo.app.composeapp.generated.resources.vehicle_split_you
import org.jetbrains.compose.resources.stringResource

@Composable
fun VehicleDetailsSheet(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onBookClick: () -> Unit,
    onReserveClick: () -> Unit
) {
    val pricePerMinute = vehicle.basePricePerMinute

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 0.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
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

            Spacer(modifier = Modifier.height(20.dp))

            // Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MovoOnSurfaceVariant
                    )
                }
            }

            // Car image with available badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Car image placeholder
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MovoTealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚗",
                        fontSize = 100.sp
                    )
                }

                // Available badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp)
                        .border(1.dp, MovoTeal, RoundedCornerShape(16.dp))
                        .background(MovoWhite, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.vehicle_available),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MovoTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title and price row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicle.model,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MovoOnSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val priceEuros = pricePerMinute.toInt()
                    val priceCents = ((pricePerMinute - priceEuros) * 100).toInt()
                    Text(
                        text = "€$priceEuros.${priceCents.toString().padStart(2, '0')}/min",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MovoTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // License plate and variant
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .border(1.dp, MovoOutline, RoundedCornerShape(4.dp))
                        .background(MovoSurfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = vehicle.licensePlate ?: "XXXXXX",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MovoOnSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "• ${vehicle.model}",
                    fontSize = 14.sp,
                    color = MovoOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info cards row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val estimatedRange = vehicle.rangeKm ?: (vehicle.batteryLevel * 3)

                InfoCard(
                    icon = Icons.Default.BatteryChargingFull,
                    label = stringResource(Res.string.vehicle_range_label),
                    value = "$estimatedRange km",
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    label = stringResource(Res.string.vehicle_distance_label),
                    value = "--",
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    icon = Icons.Default.Groups,
                    label = stringResource(Res.string.vehicle_seats_label),
                    value = "--",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Split with friends section
            Column {
                Text(
                    text = stringResource(Res.string.vehicle_split_friends),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MovoOnSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Your avatar
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MovoTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MovoWhite,
                                modifier = Modifier.size(24.dp)
                            )

                            // Checkmark badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MovoWhite)
                                    .border(2.dp, MovoTeal, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MovoTeal,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(Res.string.vehicle_split_you),
                            fontSize = 12.sp,
                            color = MovoOnSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Add button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, MovoOutline, CircleShape)
                                .background(MovoSurface)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Light,
                                color = MovoOnSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(Res.string.vehicle_split_add),
                            fontSize = 12.sp,
                            color = MovoOnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Book now button
            Button(
                onClick = onBookClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MovoTeal)
            ) {
                Text(
                    text = "${stringResource(Res.string.vehicle_book_now)} →",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reserve for later link
            Text(
                text = stringResource(Res.string.vehicle_reserve_later),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onReserveClick)
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MovoOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovoSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MovoTeal,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MovoOnSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = MovoOnSurfaceVariant
            )
        }
    }
}
