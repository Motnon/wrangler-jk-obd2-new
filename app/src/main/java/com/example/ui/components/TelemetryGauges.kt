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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.obd.ConnectionStatus
import com.example.obd.DetectedVehicle
import com.example.obd.VinInfo
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import kotlin.math.cos
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
    modifier: Modifier = Modifier,
    detectedVehicle: DetectedVehicle? = null
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    val dotColor by animateColorAsState(
        targetValue = when (connectionStatus) {
            ConnectionStatus.CONNECTED -> palette.zone2
            ConnectionStatus.CONNECTING, ConnectionStatus.INITIALIZING -> palette.zone3
            ConnectionStatus.ERROR -> palette.zone5
            ConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.outline
        },
        label = "ecu_status_dot"
    )

    val statusText = when (connectionStatus) {
        ConnectionStatus.CONNECTED -> stringResource(R.string.status_connected)
        ConnectionStatus.CONNECTING, ConnectionStatus.INITIALIZING -> stringResource(R.string.status_connecting)
        ConnectionStatus.ERROR -> strings.linkError
        ConnectionStatus.DISCONNECTED -> stringResource(R.string.status_disconnected)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (connectionStatus == ConnectionStatus.CONNECTED) {
                palette.zone2.copy(alpha = 0.65f)
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
                        if (detectedVehicle != null && (connectionStatus == ConnectionStatus.CONNECTED || connectionStatus == ConnectionStatus.INITIALIZING)) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = detectedVehicle.detectionSource.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
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
                                        tint = palette.zone5,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.milOn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.zone5
                                    )
                                }
                            }
                        }
                    }
                    val displaySubStatus = when {
                        detectedVehicle != null && (connectionStatus == ConnectionStatus.CONNECTED || connectionStatus == ConnectionStatus.INITIALIZING) -> {
                            val tail = detectedVehicle.vin ?: detectedVehicle.protocolName ?: adapterName.orEmpty()
                            if (tail.isNotBlank()) "${detectedVehicle.displayTitle} • $tail" else detectedVehicle.displayTitle
                        }
                        vinInfo != null && connectionStatus == ConnectionStatus.CONNECTED ->
                            "${vinInfo.modelYear} ${vinInfo.detectedModel?.code ?: "Vehicle"} • ${vinInfo.vin}"
                        adapterName != null && connectionStatus == ConnectionStatus.CONNECTED ->
                            adapterName
                        statusMessage == "Ready to pair ELM327 adapter" -> strings.readyToPairStatus
                        else -> statusMessage
                    }
                    Text(
                        text = displaySubStatus,
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
    val palette = LocalRacePalette.current
    val isWarning = value != null && value >= warningThreshold
    val activeArcColor = if (isWarning) palette.zone5 else accentColor
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
    val warnFrac = ((warningThreshold - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isWarning) palette.zone5.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
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
                        color = palette.zone5.copy(alpha = 0.42f),
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
                            color = if (step / 10f >= warnFrac) palette.zone5.copy(alpha = 0.8f) else tickColor,
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
                            palette.zone5
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

