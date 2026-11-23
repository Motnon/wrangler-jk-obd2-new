package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Explore
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.obd.TrailSensorState
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.FaultCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import com.example.ui.theme.OverlandAmber
import com.example.ui.theme.TrailSage
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TrailInclinometerCard(
    sensorState: TrailSensorState,
    pitchTareDeg: Float,
    rollTareDeg: Float,
    onTareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.trailInclinometer,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (sensorState.isSensorAvailable) {
                            strings.liveGravityCompass
                        } else {
                            strings.accelUnavailable
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
                // Roll Dial (Realistic Jeep Wrangler JK Front View)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WranglerJkInclinometerDial(
                        imageRes = R.drawable.img_jk_inclinometer_front,
                        imageContentDescription = "Jeep Wrangler JK front roll angle",
                        angleDegrees = roll,
                        rotationZDegrees = -animatedRoll,
                        imageFraction = 0.74f,
                        horizonYFraction = 0.76f
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${roll.roundToInt()}° ${strings.rollSuffix}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (abs(roll) >= 25f) FaultCrimson else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Pitch Dial (Realistic Jeep Wrangler JK Side Profile)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WranglerJkInclinometerDial(
                        imageRes = R.drawable.img_jk_inclinometer_side,
                        imageContentDescription = "Jeep Wrangler JK side pitch angle",
                        angleDegrees = pitch,
                        rotationZDegrees = animatedPitch,
                        imageFraction = 0.80f,
                        horizonYFraction = 0.70f
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${pitch.roundToInt()}° ${strings.pitchSuffix}",
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
                                contentDescription = strings.trailHeading,
                                tint = TrailSage,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.trailHeading,
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

@Composable
private fun WranglerJkInclinometerDial(
    @DrawableRes imageRes: Int,
    imageContentDescription: String,
    angleDegrees: Float,
    rotationZDegrees: Float,
    imageFraction: Float,
    horizonYFraction: Float,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    val isHighAngle = abs(angleDegrees) >= 25f
    val accentColor = if (isHighAngle) FaultCrimson else OverlandAmber
    val wellBrush = if (palette.isNightMode) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF2B1014),
                Color(0xFF170709),
                Color(0xFF0A0304)
            )
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF2E3B4E),
                Color(0xFF192230),
                Color(0xFF0E141D)
            )
        )
    }
    val vehicleColorFilter = if (palette.isNightMode) {
        ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    1.05f, 0f, 0f, 0f, 12f,
                    0f, 0.35f, 0f, 0f, 0f,
                    0f, 0f, 0.35f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
    } else {
        null
    }

    Box(
        modifier = modifier
            .size(124.dp)
            .clip(CircleShape)
            .background(wellBrush),
        contentAlignment = Alignment.Center
    ) {
        // Dial perimeter bezel, degree scale ticks, danger arcs, and horizon baseline
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 3.dp.toPx()

            if (isHighAngle) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(FaultCrimson.copy(alpha = 0.24f), Color.Transparent)
                    ),
                    radius = radius,
                    center = center
                )
            }

            // Brushed outer metallic ring & inner accent bezel
            drawCircle(
                color = if (isHighAngle) FaultCrimson.copy(alpha = 0.85f) else Color(0xFF4B5C74),
                radius = radius,
                center = center,
                style = Stroke(width = 2.2.dp.toPx())
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.25f),
                radius = radius - 3.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Horizon terrain reference line beneath tires
            val horizonY = size.height * horizonYFraction
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = 0.8f),
                        accentColor,
                        accentColor.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                ),
                start = Offset(size.width * 0.10f, horizonY),
                end = Offset(size.width * 0.90f, horizonY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Side degree perimeter ticks (-45° to +45° on left and right sides)
            val tickOuter = radius - 2.dp.toPx()
            for (degOffset in -45..45 step 15) {
                val isMajor = degOffset % 30 == 0 || degOffset == 0
                val isDanger = abs(degOffset) >= 30
                val tickLen = if (isMajor) 7.dp.toPx() else 4.dp.toPx()
                val tickColor = when {
                    degOffset == 0 -> accentColor
                    isDanger -> FaultCrimson.copy(alpha = 0.8f)
                    else -> Color(0xFF8A9BB3)
                }

                listOf(degOffset.toFloat(), 180f - degOffset.toFloat()).forEach { baseAngle ->
                    val rad = Math.toRadians(baseAngle.toDouble())
                    val p1 = Offset(
                        center.x + ((tickOuter - tickLen) * cos(rad)).toFloat(),
                        center.y + ((tickOuter - tickLen) * sin(rad)).toFloat()
                    )
                    val p2 = Offset(
                        center.x + (tickOuter * cos(rad)).toFloat(),
                        center.y + (tickOuter * sin(rad)).toFloat()
                    )
                    drawLine(
                        color = tickColor,
                        start = p1,
                        end = p2,
                        strokeWidth = if (isMajor) 1.8.dp.toPx() else 1.1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Photorealistic Jeep Wrangler JK render rotating smoothly with tilt
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = imageContentDescription,
            contentScale = ContentScale.Fit,
            colorFilter = vehicleColorFilter,
            modifier = Modifier
                .fillMaxSize(imageFraction)
                .graphicsLayer {
                    rotationZ = rotationZDegrees
                }
        )
    }
}

private fun cardinalDirection(deg: Float): String {
    val dirs = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val idx = (((deg + 22.5f) % 360f) / 45f).toInt().coerceIn(0, 7)
    return dirs[idx]
}
