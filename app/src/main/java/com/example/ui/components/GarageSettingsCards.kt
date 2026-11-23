package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.obd.AppVisualTheme
import com.example.obd.LiveTelemetry
import com.example.obd.NightModeSetting
import com.example.obd.RigPreferences
import com.example.obd.applyCalibratedSpeedKph
import com.example.obd.parseTireSizeInches
import com.example.sensors.GpsTelemetryState
import com.example.sensors.SolarSchedule
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TrailSage
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Personalization card for Visual Color Themes (Neon Cyan & Amber, Racing Red, Deep Green, Classic White)
 * and Automated Night Mode (sunrise/sunset or manual override + screen brightness reduction).
 */
@Composable
fun DisplayAndNightModeCard(
    rigPrefs: RigPreferences,
    solarSchedule: SolarSchedule,
    isNightModeActive: Boolean,
    hasLocationPermission: Boolean,
    onSelectVisualTheme: (AppVisualTheme) -> Unit,
    onSelectNightMode: (NightModeSetting) -> Unit,
    onNightBrightnessChange: (Int) -> Unit,
    onLocationPermissionResult: (Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var brightnessSlider by remember(rigPrefs.nightBrightnessPercent) {
        mutableIntStateOf(rigPrefs.nightBrightnessPercent)
    }

    val sunLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onLocationPermissionResult(granted, false)
    }

    val sunriseStr = String.format(
        Locale.US,
        "%02d:%02d",
        solarSchedule.sunriseMinuteOfDay / 60,
        solarSchedule.sunriseMinuteOfDay % 60
    )
    val sunsetStr = String.format(
        Locale.US,
        "%02d:%02d",
        solarSchedule.sunsetMinuteOfDay / 60,
        solarSchedule.sunsetMinuteOfDay % 60
    )
    val solarText = if (solarSchedule.usingLocation) {
        String.format(Locale.US, strings.solarUsingLocationFormat, sunriseStr, sunsetStr)
    } else {
        String.format(Locale.US, strings.solarDefaultSunFormat, sunriseStr, sunsetStr)
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.visualThemeHeader,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = strings.displayColorTheme,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppVisualTheme.entries.forEach { theme ->
                    FilterChip(
                        selected = rigPrefs.visualTheme == theme,
                        onClick = { onSelectVisualTheme(theme) },
                        label = { Text(strings.themeLabel(theme)) },
                        modifier = Modifier.testTag("theme_chip_${theme.name.lowercase()}")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Brightness4,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.nightOffRoadMode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = if (isNightModeActive) strings.nightActiveBadge else strings.daylightBadge,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNightModeActive) MaterialTheme.colorScheme.primary else TrailSage
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NightModeSetting.entries.forEach { mode ->
                    FilterChip(
                        selected = rigPrefs.nightModeSetting == mode,
                        onClick = { onSelectNightMode(mode) },
                        label = { Text(strings.nightModeLabel(mode)) },
                        modifier = Modifier.testTag("night_mode_chip_${mode.name.lowercase()}")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solarText,
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (!hasLocationPermission && rigPrefs.nightModeSetting == NightModeSetting.AUTO_SUN) {
                    OutlinedButton(
                        onClick = {
                            sunLocationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.testTag("night_detect_sun_location_button")
                    ) {
                        Text(strings.useGpsSun)
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.nightBrightnessLabel,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$brightnessSlider%",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = brightnessSlider.toFloat(),
                    onValueChange = { brightnessSlider = it.roundToInt() },
                    onValueChangeFinished = { onNightBrightnessChange(brightnessSlider) },
                    valueRange = 5f..60f,
                    modifier = Modifier.testTag("night_brightness_slider")
                )
            }
        }
    }
}

/**
 * GPS-based speedometer calibration card that detects oversized tires,
 * compares live GPS KPH against OBD-II KPH, and calculates/applies the speed offset factor.
 */
@Composable
fun GpsSpeedCalibrationCard(
    rigPrefs: RigPreferences,
    telemetry: LiveTelemetry,
    gpsState: GpsTelemetryState,
    hasFineLocationPermission: Boolean,
    onLocationPermissionResult: (Boolean, Boolean) -> Unit,
    onStartGpsCalibration: () -> Unit,
    onStopGpsCalibration: () -> Unit,
    onApplyLiveGpsCalibration: () -> Unit,
    onApplyTireRatioCalibration: (Float) -> Unit,
    onSetCalibrationFactor: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val parsedTire = parseTireSizeInches(rigPrefs.tireSizeInches)
    val oversizedDetected = parsedTire != null && parsedTire > (rigPrefs.stockTireInches + 0.5f)
    val theoreticalFactor = if (parsedTire != null && rigPrefs.stockTireInches > 0f) {
        (parsedTire / rigPrefs.stockTireInches).coerceIn(0.70f, 1.45f)
    } else {
        1.0f
    }

    var manualFactor by remember(rigPrefs.speedCalibrationFactor) {
        mutableFloatStateOf(rigPrefs.speedCalibrationFactor)
    }

    val gpsLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        onLocationPermissionResult(fineGranted, true)
    }

    val rawObdKph = telemetry.speedKmh
    val calibratedKph = applyCalibratedSpeedKph(rawObdKph, rigPrefs.speedCalibrationFactor)
    val gpsKph = gpsState.gpsSpeedKph

    val liveCalculatedFactor = if (gpsKph != null && gpsKph >= 10f && rawObdKph != null && rawObdKph >= 10) {
        (gpsKph / rawObdKph.toFloat()).coerceIn(0.70f, 1.45f)
    } else {
        null
    }

    val activeOffsetPct = (rigPrefs.speedCalibrationFactor - 1f) * 100f

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (oversizedDetected && kotlin.math.abs(activeOffsetPct) < 1.0f) {
                CautionYellow.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.gpsCalibrationHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = String.format(Locale.US, "×%.3f (%+.1f%%)", rigPrefs.speedCalibrationFactor, activeOffsetPct),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (oversizedDetected) {
                val estimatedOffsetStr = String.format(
                    Locale.US,
                    "%+.1f%% (×%.3f)",
                    (theoreticalFactor - 1f) * 100f,
                    theoreticalFactor
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = CautionYellow.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, CautionYellow.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = CautionYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = String.format(
                                Locale.US,
                                strings.oversizedTiresWarningFormat,
                                rigPrefs.tireSizeInches,
                                rigPrefs.stockTireInches.toInt(),
                                estimatedOffsetStr
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = strings.gpsSpeedLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = gpsKph?.let { String.format(Locale.US, "%.1f %s", it, strings.speedUnitKph) }
                                ?: "-- ${strings.speedUnitKph}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = TrailSage
                        )
                    }
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = strings.obdRawCalLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${rawObdKph ?: "--"} / ${calibratedKph ?: "--"} ${strings.speedUnitKph}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Text(
                text = if (liveCalculatedFactor != null) {
                    val diffKph = gpsKph!! - rawObdKph!!.toFloat()
                    String.format(
                        Locale.US,
                        strings.liveGpsOffsetFormat,
                        diffKph,
                        liveCalculatedFactor
                    )
                } else {
                    gpsState.statusNote
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!hasFineLocationPermission) {
                            gpsLocationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else if (gpsState.isCalibrating) {
                            onStopGpsCalibration()
                        } else {
                            onStartGpsCalibration()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (gpsState.isCalibrating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gps_calibration_toggle_button")
                ) {
                    Text(
                        text = when {
                            !hasFineLocationPermission -> strings.enableGps
                            gpsState.isCalibrating -> strings.stopGps
                            else -> strings.startGps
                        }
                    )
                }

                Button(
                    onClick = onApplyLiveGpsCalibration,
                    enabled = liveCalculatedFactor != null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gps_apply_calibration_button")
                ) {
                    Text(strings.matchGps)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (oversizedDetected) {
                    OutlinedButton(
                        onClick = { onApplyTireRatioCalibration(rigPrefs.stockTireInches) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_tire_ratio_button")
                    ) {
                        Text(String.format(Locale.US, strings.useTireRatioFormat, rigPrefs.tireSizeInches))
                    }
                }
                OutlinedButton(
                    onClick = { onSetCalibrationFactor(1.0f) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_speed_calibration_button")
                ) {
                    Text(strings.resetOneX)
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.manualSpeedMultiplier,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = String.format(Locale.US, "×%.3f", manualFactor),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = manualFactor,
                    onValueChange = { manualFactor = it },
                    onValueChangeFinished = { onSetCalibrationFactor(manualFactor) },
                    valueRange = 0.80f..1.35f,
                    modifier = Modifier.testTag("manual_speed_calibration_slider")
                )
            }
        }
    }
}
