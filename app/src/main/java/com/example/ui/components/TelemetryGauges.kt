package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.obd.ConnectionStatus
import com.example.obd.TrailSensorState
import com.example.obd.VinInfo
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FaultCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OverlandAmber
import com.example.ui.theme.TrailSage
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun EcuStatusBanner(
    connectionStatus: ConnectionStatus,
    adapterName: String?,
    statusMessage: String,
    vinInfo: VinInfo?,
    milOn: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor by animateColorAsState(
        targetValue = when (connectionStatus) {
            ConnectionStatus.CONNECTED -> TrailSage
            ConnectionStatus.CONNECTING, ConnectionStatus.INITIALIZING -> CautionYellow
            ConnectionStatus.ERROR -> FaultCrimson
            ConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.outline
        },
        label = "ecu_status_dot"
    )

    val statusText = when (connectionStatus) {
        ConnectionStatus.CONNECTED -> stringResource(R.string.status_connected)
        ConnectionStatus.CONNECTING, ConnectionStatus.INITIALIZING -> stringResource(R.string.status_connecting)
        ConnectionStatus.ERROR -> "LINK ERROR"
        ConnectionStatus.DISCONNECTED -> stringResource(R.string.status_disconnected)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (connectionStatus == ConnectionStatus.CONNECTED) {
                TrailSage.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outline
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (milOn) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = "Check Engine Light active",
                                        tint = FaultCrimson,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "MIL ON",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FaultCrimson
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = when {
                            vinInfo != null && connectionStatus == ConnectionStatus.CONNECTED ->
                                "${vinInfo.modelYear} JK • ${vinInfo.vin}"
                            adapterName != null && connectionStatus == ConnectionStatus.CONNECTED ->
                                adapterName
                            else -> statusMessage
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (connectionStatus == ConnectionStatus.CONNECTED ||
                connectionStatus == ConnectionStatus.CONNECTING ||
                connectionStatus == ConnectionStatus.INITIALIZING
            ) {
                OutlinedButton(
                    onClick = onDisconnectClick,
                    modifier = Modifier.testTag("ecu_disconnect_button"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BluetoothConnected,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.action_disconnect))
                }
            } else {
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.testTag("ecu_connect_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (connectionStatus == ConnectionStatus.ERROR) {
                            Icons.Rounded.BluetoothDisabled
                        } else {
                            Icons.Rounded.Bluetooth
                        },
                        contentDescription = stringResource(R.string.cd_connect_bluetooth),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.action_connect_adapter))
                }
            }
        }
    }
}

@Composable
fun CircularArcGauge(
    title: String,
    valueText: String,
    unit: String,
    value: Float?,
    minValue: Float,
    maxValue: Float,
    warningThreshold: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val fractionTarget = if (value != null && maxValue > minValue) {
        ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedFraction by animateFloatAsState(
        targetValue = fractionTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "gauge_fraction"
    )
    val isWarning = value != null && value >= warningThreshold
    val activeArcColor = if (isWarning) FaultCrimson else accentColor
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val warnFrac = ((warningThreshold - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isWarning) FaultCrimson.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.size(144.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val pad = strokeWidth / 2f + 6.dp.toPx()
                    val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
                    val topLeft = Offset(pad, pad)
                    val startAngle = 145f
                    val totalSweep = 250f

                    // Base track
                    drawArc(
                        color = trackColor,
                        startAngle = startAngle,
                        sweepAngle = totalSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Warning zone marker arc
                    drawArc(
                        color = FaultCrimson.copy(alpha = 0.32f),
                        startAngle = startAngle + totalSweep * warnFrac,
                        sweepAngle = totalSweep * (1f - warnFrac),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth * 0.5f, cap = StrokeCap.Round)
                    )

                    // Active value arc
                    if (animatedFraction > 0.005f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(accentColor, activeArcColor)
                            ),
                            startAngle = startAngle,
                            sweepAngle = totalSweep * animatedFraction,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Perimeter scale ticks
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = arcSize.width / 2f + 2.dp.toPx()
                    val innerRadius = outerRadius - 6.dp.toPx()
                    for (step in 0..10) {
                        val deg = startAngle + (totalSweep / 10f) * step
                        val rad = Math.toRadians(deg.toDouble())
                        val p1 = Offset(
                            center.x + (innerRadius * cos(rad)).toFloat(),
                            center.y + (innerRadius * sin(rad)).toFloat()
                        )
                        val p2 = Offset(
                            center.x + (outerRadius * cos(rad)).toFloat(),
                            center.y + (outerRadius * sin(rad)).toFloat()
                        )
                        drawLine(
                            color = if (step / 10f >= warnFrac) FaultCrimson.copy(alpha = 0.7f) else tickColor,
                            start = p1,
                            end = p2,
                            strokeWidth = if (step % 2 == 0) 2.dp.toPx() else 1.dp.toPx()
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = valueText,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (value == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else if (isWarning) {
                            FaultCrimson
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VitalTelemetryTile(
    label: String,
    valueText: String,
    unit: String,
    icon: ImageVector,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = valueText,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TrailInclinometerCard(
    sensorState: TrailSensorState,
    pitchTareDeg: Float,
    rollTareDeg: Float,
    onTareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pitch = (sensorState.pitchDegrees - pitchTareDeg).coerceIn(-60f, 60f)
    val roll = (sensorState.rollDegrees - rollTareDeg).coerceIn(-60f, 60f)
    val animatedPitch by animateFloatAsState(
        targetValue = pitch,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pitch_anim"
    )
    val animatedRoll by animateFloatAsState(
        targetValue = roll,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "roll_anim"
    )
    val isHighAngle = abs(pitch) >= 25f || abs(roll) >= 25f
    val horizonColor = if (isHighAngle) FaultCrimson else OverlandAmber
    val frameColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isHighAngle) FaultCrimson.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRAIL INCLINOMETER",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (sensorState.isSensorAvailable) {
                            "Live device gravity & compass sensors"
                        } else {
                            "Accelerometer unavailable on hardware"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onTareClick,
                    modifier = Modifier.testTag("zero_inclinometer_button"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CenterFocusStrong,
                        contentDescription = stringResource(R.string.action_zero_inclinometer),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.action_zero_inclinometer))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Roll Dial (Rear/Front JK Profile)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(118.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.minDimension / 2f - 8.dp.toPx()

                            drawCircle(
                                color = gridColor,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            // Horizon baseline
                            drawLine(
                                color = gridColor,
                                start = Offset(center.x - radius, center.y),
                                end = Offset(center.x + radius, center.y),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Rotating Jeep JK Rear Axle / Grille Silhouette
                            rotate(degrees = -animatedRoll, pivot = center) {
                                // Axle & tires
                                drawLine(
                                    color = horizonColor,
                                    start = Offset(center.x - radius * 0.72f, center.y + 10.dp.toPx()),
                                    end = Offset(center.x + radius * 0.72f, center.y + 10.dp.toPx()),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // Left & Right Tires
                                drawLine(
                                    color = frameColor,
                                    start = Offset(center.x - radius * 0.62f, center.y + 2.dp.toPx()),
                                    end = Offset(center.x - radius * 0.62f, center.y + 20.dp.toPx()),
                                    strokeWidth = 8.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = frameColor,
                                    start = Offset(center.x + radius * 0.62f, center.y + 2.dp.toPx()),
                                    end = Offset(center.x + radius * 0.62f, center.y + 20.dp.toPx()),
                                    strokeWidth = 8.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // JK Hardtop / Rollbar Silhouette
                                drawRect(
                                    color = frameColor.copy(alpha = 0.85f),
                                    topLeft = Offset(center.x - 24.dp.toPx(), center.y - 16.dp.toPx()),
                                    size = Size(48.dp.toPx(), 22.dp.toPx()),
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                                drawRect(
                                    color = horizonColor,
                                    topLeft = Offset(center.x - 18.dp.toPx(), center.y - 26.dp.toPx()),
                                    size = Size(36.dp.toPx(), 10.dp.toPx()),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }
                    Text(
                        text = "${roll.roundToInt()}° ROLL",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (abs(roll) >= 25f) FaultCrimson else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Pitch Dial (Side JK Profile)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(118.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.minDimension / 2f - 8.dp.toPx()

                            drawCircle(
                                color = gridColor,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(center.x - radius, center.y),
                                end = Offset(center.x + radius, center.y),
                                strokeWidth = 1.dp.toPx()
                            )

                            rotate(degrees = animatedPitch, pivot = center) {
                                // Chassis rail
                                drawLine(
                                    color = horizonColor,
                                    start = Offset(center.x - radius * 0.72f, center.y + 8.dp.toPx()),
                                    end = Offset(center.x + radius * 0.72f, center.y + 8.dp.toPx()),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // Front & Rear Wheels
                                drawCircle(
                                    color = frameColor,
                                    radius = 9.dp.toPx(),
                                    center = Offset(center.x - 22.dp.toPx(), center.y + 12.dp.toPx()),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                drawCircle(
                                    color = frameColor,
                                    radius = 9.dp.toPx(),
                                    center = Offset(center.x + 22.dp.toPx(), center.y + 12.dp.toPx()),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                // Side JK tub + hood
                                drawLine(
                                    color = frameColor,
                                    start = Offset(center.x - 32.dp.toPx(), center.y + 4.dp.toPx()),
                                    end = Offset(center.x - 32.dp.toPx(), center.y - 18.dp.toPx()),
                                    strokeWidth = 2.5.dp.toPx()
                                )
                                drawLine(
                                    color = frameColor,
                                    start = Offset(center.x - 32.dp.toPx(), center.y - 18.dp.toPx()),
                                    end = Offset(center.x + 6.dp.toPx(), center.y - 18.dp.toPx()),
                                    strokeWidth = 2.5.dp.toPx()
                                )
                                drawLine(
                                    color = frameColor,
                                    start = Offset(center.x + 6.dp.toPx(), center.y - 18.dp.toPx()),
                                    end = Offset(center.x + 14.dp.toPx(), center.y - 4.dp.toPx()),
                                    strokeWidth = 2.5.dp.toPx()
                                )
                                drawLine(
                                    color = frameColor,
                                    start = Offset(center.x + 14.dp.toPx(), center.y - 4.dp.toPx()),
                                    end = Offset(center.x + 34.dp.toPx(), center.y - 4.dp.toPx()),
                                    strokeWidth = 2.5.dp.toPx()
                                )
                            }
                        }
                    }
                    Text(
                        text = "${pitch.roundToInt()}° PITCH",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (abs(pitch) >= 25f) FaultCrimson else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (sensorState.hasCompassSensor) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Explore,
                                contentDescription = "Compass heading",
                                tint = TrailSage,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRAIL HEADING",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${sensorState.azimuthDegrees.roundToInt()}° ${cardinalDirection(sensorState.azimuthDegrees)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = TrailSage
                        )
                    }
                }
            }
        }
    }
}

private fun cardinalDirection(deg: Float): String {
    val dirs = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val idx = (((deg + 22.5f) % 360f) / 45f).toInt().coerceIn(0, 7)
    return dirs[idx]
}
