package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import kotlin.math.roundToInt

/**
 * Colorful motorsport circular arc dial for Engine RPM, Manifold MAP, or Engine Load.
 */
@Composable
fun RaceCircularArcPod(
    title: String,
    valueText: String,
    unit: String,
    value: Float?,
    minValue: Float,
    maxValue: Float,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    val targetFraction = if (value != null && maxValue > minValue) {
        ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessLow),
        label = "race_pod_$title"
    )
    val accent = gradientColors.lastOrNull() ?: palette.zone1

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.bezelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val minSide = minOf(maxWidth.value, maxHeight.value)
                val valueFontSp = (minSide * 0.21f).coerceIn(13f, 24f).sp
                val unitFontSp = (minSide * 0.105f).coerceIn(8f, 12f).sp

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = (size.minDimension - 6.dp.toPx()).coerceAtLeast(36f)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = diameter / 2f
                    val strokeW = (radius * 0.20f).coerceIn(5.dp.toPx(), 12.dp.toPx())
                    val arcR = radius - strokeW / 2f
                    val topLeft = Offset(center.x - arcR, center.y - arcR)
                    val arcSize = Size(arcR * 2f, arcR * 2f)

                    drawCircle(
                        color = palette.boardBg,
                        radius = radius,
                        center = center
                    )

                    drawArc(
                        color = palette.podSurface,
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    if (animatedFraction > 0.005f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = if (gradientColors.size >= 2) gradientColors else listOf(accent, accent),
                                center = center
                            ),
                            startAngle = 140f,
                            sweepAngle = 260f * animatedFraction,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = valueText,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = valueFontSp,
                        color = if (value == null) palette.textSecondary else palette.textPrimary
                    )
                    Text(
                        text = unit,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = unitFontSp,
                        color = accent
                    )
                }
            }
        }
    }
}

/**
 * Full-width vivid horizontal racing Throttle Position (TPS) bar.
 */
@Composable
fun RaceThrottleBarCard(
    throttlePercent: Float?,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    val targetFraction = throttlePercent?.let { (it / 100f).coerceIn(0f, 1f) } ?: 0f
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
        label = "race_throttle_bar"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.bezelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "THROTTLE (TPS)",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.9.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = throttlePercent?.let { "${it.roundToInt()}%" } ?: "--%",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = when {
                        throttlePercent == null -> palette.textSecondary
                        throttlePercent >= 80f -> palette.zone5
                        throttlePercent >= 50f -> palette.zone3
                        else -> palette.zone1
                    }
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val segments = 20
                val gap = 3.dp.toPx()
                val segWidth = ((size.width - gap * (segments - 1)) / segments).coerceAtLeast(3f)
                val litCount = (animatedFraction * segments).roundToInt()

                for (i in 0 until segments) {
                    val segColor = when {
                        i < 6 -> palette.zone1
                        i < 12 -> palette.zone2
                        i < 16 -> palette.zone3
                        i < 18 -> palette.zone4
                        else -> palette.zone5
                    }
                    val lit = throttlePercent != null && i < litCount
                    drawRoundRect(
                        color = segColor.copy(alpha = if (lit) 0.95f else 0.16f),
                        topLeft = Offset(i * (segWidth + gap), 0f),
                        size = Size(segWidth, size.height.coerceAtLeast(6f)),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Pit-wall telemetry strip with Peak RPM / Peak Speed tracking & Reset button.
 */
@Composable
fun RaceSessionPeaksCard(
    peakRpm: Int?,
    peakSpeedDisplay: Int?,
    speedUnit: String,
    batteryVoltsText: String,
    isLowBattery: Boolean,
    intakeTempText: String,
    fuelTrimText: String,
    onResetPeaks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.bezelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PIT-WALL & SESSION PEAKS",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = palette.accentViolet,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onResetPeaks,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("race_reset_peaks_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reset session peak telemetry",
                        tint = palette.zone1
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RaceMetricTile(
                    label = "PEAK RPM",
                    value = peakRpm?.toString() ?: "--",
                    accentColor = palette.zone4,
                    modifier = Modifier.weight(1f)
                )
                RaceMetricTile(
                    label = "TOP $speedUnit",
                    value = peakSpeedDisplay?.toString() ?: "--",
                    accentColor = palette.zone1,
                    modifier = Modifier.weight(1f)
                )
                RaceMetricTile(
                    label = "BATTERY",
                    value = batteryVoltsText,
                    accentColor = if (isLowBattery) palette.zone5 else palette.zone2,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RaceMetricTile(
                    label = "INTAKE AIR",
                    value = intakeTempText,
                    accentColor = palette.accentBlue,
                    modifier = Modifier.weight(1f)
                )
                RaceMetricTile(
                    label = "STFT B1",
                    value = fuelTrimText,
                    accentColor = palette.zone3,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RaceMetricTile(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = palette.podSurface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .width(3.dp)
                    .height(22.dp),
                shape = RoundedCornerShape(2.dp),
                color = accentColor
            ) {}
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = palette.textSecondary
                )
                Text(
                    text = value,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    color = palette.textPrimary
                )
            }
        }
    }
}
