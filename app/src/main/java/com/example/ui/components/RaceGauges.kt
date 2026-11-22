package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 14-LED progressive motorsport shift-light strip driven by live engine RPM.
 */
@Composable
fun RaceShiftLightBar(
    rpm: Int?,
    rpmRedline: Float,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    val startRpm = (rpmRedline * 0.25f).coerceAtLeast(1000f)
    val shiftTriggerRpm = rpmRedline * 0.90f
    val isShiftAlert = rpm != null && rpm.toFloat() >= shiftTriggerRpm

    val infiniteTransition = rememberInfiniteTransition(label = "shift_flash")
    val flashPhase by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 160, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift_flash_alpha"
    )

    val fillRatio = if (rpm != null && rpmRedline > startRpm) {
        ((rpm.toFloat() - startRpm) / (rpmRedline - startRpm)).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedRatio by animateFloatAsState(
        targetValue = fillRatio,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
        label = "shift_bar_ratio"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isShiftAlert) palette.zone5 else palette.bezelBorder,
        label = "shift_bar_border"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
        border = BorderStroke(1.2.dp, borderColor)
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
                    text = "SHIFT LIGHTS",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.1.sp,
                    color = palette.textSecondary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isShiftAlert -> palette.zone5.copy(alpha = 0.25f)
                        rpm != null && rpm > 0 -> palette.zone2.copy(alpha = 0.16f)
                        else -> palette.podSurface
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isShiftAlert -> palette.zone5
                            rpm != null && rpm > 0 -> palette.zone2.copy(alpha = 0.6f)
                            else -> palette.bezelBorder
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isShiftAlert -> "SHIFT NOW"
                            rpm != null -> "TRACK ACTIVE"
                            else -> "STANDBY"
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = when {
                            isShiftAlert -> palette.zone5
                            rpm != null -> palette.zone2
                            else -> palette.textSecondary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val ledCount = 14
                val gap = 4.dp.toPx()
                val ledWidth = ((size.width - gap * (ledCount - 1)) / ledCount).coerceAtLeast(4f)
                val ledHeight = size.height.coerceAtLeast(8f)
                val activeLeds = (animatedRatio * ledCount).roundToInt()

                for (i in 0 until ledCount) {
                    val baseColor = when {
                        i < 5 -> palette.zone2
                        i < 9 -> palette.zone3
                        i < 12 -> palette.zone4
                        else -> palette.zone5
                    }
                    val lit = isShiftAlert || i < activeLeds
                    val alpha = when {
                        isShiftAlert -> flashPhase
                        lit -> 0.96f
                        else -> 0.16f
                    }
                    val x = i * (ledWidth + gap)
                    if (lit) {
                        drawRoundRect(
                            color = baseColor.copy(alpha = alpha * 0.30f),
                            topLeft = Offset(x - 1.dp.toPx(), -1.dp.toPx()),
                            size = Size(ledWidth + 2.dp.toPx(), ledHeight + 2.dp.toPx()),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                        )
                    }
                    drawRoundRect(
                        color = baseColor.copy(alpha = alpha),
                        topLeft = Offset(x, 0f),
                        size = Size(ledWidth, ledHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Hero Motorsport Speedometer & Digital RPM Core rendered via Compose Canvas.
 * Analog dial, colored speed powerband zones, and needle display Vehicle Speed (KPH);
 * center digital readout displays live Engine RPM.
 */
@Composable
fun RaceSpeedometerDial(
    speedValue: Float?,
    speedDisplayText: String,
    speedUnit: String,
    maxSpeedScale: Float,
    speedWarningThreshold: Float,
    majorTickCount: Int,
    rpmText: String,
    isRedline: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    val fractionTarget = if (speedValue != null && maxSpeedScale > 0f) {
        (speedValue / maxSpeedScale).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedFraction by animateFloatAsState(
        targetValue = fractionTarget,
        animationSpec = spring(dampingRatio = 0.74f, stiffness = Spring.StiffnessMediumLow),
        label = "race_speed_needle"
    )
    val isSpeedAlert = speedValue != null && speedValue >= speedWarningThreshold
    val warnFraction = if (maxSpeedScale > 0f) {
        (speedWarningThreshold / maxSpeedScale).coerceIn(0.6f, 0.98f)
    } else {
        0.82f
    }

    val activeZoneColor = when {
        animatedFraction >= warnFraction -> palette.zone5
        animatedFraction >= warnFraction * 0.80f -> palette.zone4
        animatedFraction >= 0.58f -> palette.zone3
        animatedFraction >= 0.30f -> palette.zone2
        else -> palette.zone1
    }

    val textMeasurer = rememberTextMeasurer()

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        color = palette.surface,
        border = BorderStroke(
            width = 1.4.dp,
            color = if (isSpeedAlert || isRedline) palette.zone5 else palette.bezelBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPEED $speedUnit",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.9.sp,
                    color = palette.zone1
                )
                Text(
                    text = "$speedDisplayText $speedUnit",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isSpeedAlert) palette.zone5 else palette.zone3
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val minSideDp = minOf(maxWidth.value, maxHeight.value)
                val tickFontSp = (minSideDp * 0.056f).coerceIn(8f, 13f).sp
                val rpmFontSp = (minSideDp * 0.115f).coerceIn(13f, 26f).sp
                val unitFontSp = (minSideDp * 0.048f).coerceIn(8f, 11f).sp

                val scaleTextStyle = TextStyle(
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = tickFontSp,
                    color = palette.textPrimary
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = size.minDimension.coerceAtLeast(48f)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = diameter / 2f - 2.dp.toPx()

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(palette.podSurface, palette.boardBg),
                            center = center,
                            radius = outerRadius
                        ),
                        radius = outerRadius,
                        center = center
                    )

                    drawCircle(
                        brush = Brush.sweepGradient(
                            0.0f to palette.zone1.copy(alpha = 0.55f),
                            0.35f to palette.bezelBorder,
                            0.65f to palette.zone5.copy(alpha = 0.65f),
                            1.0f to palette.zone1.copy(alpha = 0.55f),
                            center = center
                        ),
                        radius = outerRadius,
                        center = center,
                        style = Stroke(width = 2.4.dp.toPx())
                    )

                    val startAngle = 135f
                    val sweepTotal = 270f
                    val arcStroke = (outerRadius * 0.11f).coerceIn(5.dp.toPx(), 15.dp.toPx())
                    val arcRadius = outerRadius - arcStroke / 2f - 5.dp.toPx()
                    val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)
                    val arcSize = Size(arcRadius * 2f, arcRadius * 2f)

                    val zones = listOf(
                        0.0f to 0.30f to palette.zone1,
                        0.30f to 0.58f to palette.zone2,
                        0.58f to (warnFraction * 0.80f) to palette.zone3,
                        (warnFraction * 0.80f) to warnFraction to palette.zone4,
                        warnFraction to 1.0f to palette.zone5
                    )

                    for ((bounds, zoneColor) in zones) {
                        val zStart = bounds.first.coerceIn(0f, 1f)
                        val zEnd = bounds.second.coerceIn(zStart, 1f)
                        if (zEnd > zStart) {
                            drawArc(
                                color = zoneColor.copy(alpha = if (zStart >= warnFraction) 0.34f else 0.18f),
                                startAngle = startAngle + sweepTotal * zStart,
                                sweepAngle = sweepTotal * (zEnd - zStart),
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = Stroke(width = arcStroke, cap = StrokeCap.Butt)
                            )
                        }
                    }

                    if (animatedFraction > 0.004f) {
                        for ((bounds, zoneColor) in zones) {
                            val zStart = bounds.first.coerceIn(0f, 1f)
                            val zEnd = bounds.second.coerceAtMost(animatedFraction)
                            if (zEnd > zStart) {
                                drawArc(
                                    color = zoneColor.copy(alpha = 0.95f),
                                    startAngle = startAngle + sweepTotal * zStart,
                                    sweepAngle = sweepTotal * (zEnd - zStart),
                                    useCenter = false,
                                    topLeft = arcTopLeft,
                                    size = arcSize,
                                    style = Stroke(width = arcStroke, cap = StrokeCap.Butt)
                                )
                            }
                        }
                    }

                    val minorPerMajor = 4
                    val totalSteps = ((majorTickCount - 1) * minorPerMajor).coerceAtLeast(4)
                    val tickOuter = arcRadius - arcStroke * 0.72f
                    val majorInner = tickOuter - outerRadius * 0.11f
                    val minorInner = tickOuter - outerRadius * 0.055f
                    val labelRadius = majorInner - outerRadius * 0.16f

                    for (step in 0..totalSteps) {
                        val frac = step.toFloat() / totalSteps.toFloat()
                        val angleDeg = startAngle + sweepTotal * frac
                        val rad = Math.toRadians(angleDeg.toDouble())
                        val isMajor = step % minorPerMajor == 0
                        val tickColor = when {
                            frac >= warnFraction -> palette.zone5
                            frac >= warnFraction * 0.80f -> palette.zone4
                            frac >= 0.58f -> palette.zone3
                            frac <= animatedFraction -> activeZoneColor
                            else -> palette.textSecondary.copy(alpha = if (isMajor) 0.85f else 0.4f)
                        }
                        val innerR = if (isMajor) majorInner else minorInner
                        drawLine(
                            color = tickColor,
                            start = Offset(
                                center.x + (innerR * cos(rad)).toFloat(),
                                center.y + (innerR * sin(rad)).toFloat()
                            ),
                            end = Offset(
                                center.x + (tickOuter * cos(rad)).toFloat(),
                                center.y + (tickOuter * sin(rad)).toFloat()
                            ),
                            strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.1.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        if (isMajor && outerRadius >= 48.dp.toPx()) {
                            val label = "${(maxSpeedScale * frac).roundToInt()}"
                            val styledLabel = scaleTextStyle.copy(
                                color = if (frac >= warnFraction) palette.zone5 else palette.textPrimary
                            )
                            val measured = textMeasurer.measure(label, styledLabel)
                            drawText(
                                textLayoutResult = measured,
                                topLeft = Offset(
                                    x = center.x + (labelRadius * cos(rad)).toFloat() - measured.size.width / 2f,
                                    y = center.y + (labelRadius * sin(rad)).toFloat() - measured.size.height / 2f
                                )
                            )
                        }
                    }

                    val needleRot = -135f + (sweepTotal * animatedFraction)
                    val needleLen = (tickOuter - 2.dp.toPx()).coerceAtLeast(10f)
                    val tailLen = outerRadius * 0.17f
                    val halfW = (outerRadius * 0.042f).coerceIn(2.dp.toPx(), 4.5.dp.toPx())

                    rotate(degrees = needleRot, pivot = center) {
                        val needlePath = Path().apply {
                            moveTo(center.x, center.y - needleLen)
                            lineTo(center.x + halfW, center.y + tailLen)
                            lineTo(center.x - halfW, center.y + tailLen)
                            close()
                        }
                        drawPath(
                            path = needlePath,
                            color = if (speedValue == null) palette.bezelBorder else activeZoneColor.copy(alpha = 0.42f),
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                        drawPath(
                            path = needlePath,
                            color = if (speedValue == null) palette.textSecondary else palette.textPrimary
                        )
                    }

                    val hubR = (outerRadius * 0.11f).coerceIn(6.dp.toPx(), 13.dp.toPx())
                    drawCircle(color = palette.boardBg, radius = hubR, center = center)
                    drawCircle(
                        color = if (speedValue == null) palette.bezelBorder else activeZoneColor,
                        radius = hubR,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = rpmText,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = rpmFontSp,
                        lineHeight = rpmFontSp * 1.05f,
                        color = if (isRedline) palette.zone5 else palette.textPrimary
                    )
                    Text(
                        text = "RPM",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = unitFontSp,
                        color = if (isRedline) palette.zone5 else palette.zone4
                    )
                }
            }
        }
    }
}

/**
 * Multi-color segmented vertical LED bar gauge for Fuel Level (F->E) or Coolant Temp (H->C).
 */
@Composable
fun RaceSegmentedBarGauge(
    title: String,
    topMarker: String,
    bottomMarker: String,
    valueText: String,
    fraction: Float?,
    isWarning: Boolean,
    isCoolantMode: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    val clampedTarget = fraction?.coerceIn(0f, 1f) ?: 0f
    val animatedFraction by animateFloatAsState(
        targetValue = clampedTarget,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
        label = "race_bar_$title"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        border = BorderStroke(
            width = 1.2.dp,
            color = if (isWarning) palette.zone5 else palette.bezelBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = if (isWarning) palette.zone5 else palette.textSecondary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = topMarker,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isCoolantMode) palette.zone5 else palette.zone2
                )

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 3.dp, horizontal = 4.dp)
                ) {
                    val segments = 12
                    val gap = 3.dp.toPx()
                    val segHeight = ((size.height - gap * (segments - 1)) / segments).coerceAtLeast(2f)
                    val activeCount = (animatedFraction * segments).roundToInt()

                    for (idxFromBottom in 0 until segments) {
                        val segColor = if (isCoolantMode) {
                            when {
                                idxFromBottom < 3 -> palette.accentBlue
                                idxFromBottom < 8 -> palette.zone2
                                idxFromBottom < 10 -> palette.zone3
                                else -> palette.zone5
                            }
                        } else {
                            when {
                                idxFromBottom < 2 -> palette.zone5
                                idxFromBottom < 5 -> palette.zone3
                                else -> palette.zone2
                            }
                        }
                        val lit = fraction != null && idxFromBottom < activeCount
                        val y = size.height - ((idxFromBottom + 1) * segHeight + idxFromBottom * gap)
                        drawRoundRect(
                            color = segColor.copy(alpha = if (lit) 0.95f else 0.16f),
                            topLeft = Offset(0f, y),
                            size = Size(size.width, segHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }

                Text(
                    text = bottomMarker,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isCoolantMode) palette.zone1 else palette.zone5
                )
            }

            Text(
                text = valueText,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = when {
                    fraction == null -> palette.textSecondary
                    isWarning -> palette.zone5
                    else -> palette.textPrimary
                }
            )
        }
    }
}
