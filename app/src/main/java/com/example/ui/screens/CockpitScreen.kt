package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.obd.BluetoothDeviceItem
import com.example.obd.ConnectionStatus
import com.example.obd.JkEngineProfile
import com.example.obd.LiveTelemetry
import com.example.obd.RigPreferences
import com.example.obd.TrailSensorState
import com.example.obd.UnitSystem
import com.example.obd.VinInfo
import com.example.obd.applyCalibratedSpeedKph
import com.example.ui.components.CircularArcGauge
import com.example.ui.components.EcuStatusBanner
import com.example.ui.components.TrailInclinometerCard
import com.example.ui.components.VitalTelemetryTile
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FaultCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OverlandAmber
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.TrailSage
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CockpitScreen(
    connectionStatus: ConnectionStatus,
    connectedDevice: BluetoothDeviceItem?,
    statusMessage: String,
    vinInfo: VinInfo?,
    telemetry: LiveTelemetry,
    sensorState: TrailSensorState,
    rigPrefs: RigPreferences,
    onOpenBluetoothSheet: () -> Unit,
    onDisconnect: () -> Unit,
    onTareInclinometer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isImperialMap = rigPrefs.unitSystem == UnitSystem.IMPERIAL
    val redlineRpm = rigPrefs.engineProfile.redlineRpm
    val maxRpmScale = rigPrefs.engineProfile.maxRpmScale

    // Coolant temp is always Celsius (°C)
    val coolantDisplayValue: Float? = telemetry.coolantCelsius?.toFloat()
    val coolantThresholdDisplay: Float = rigPrefs.coolantWarnCelsius.toFloat()
    val coolantMin = 25f
    val coolantMax = 130f

    // Speed is always KPH (with GPS tire calibration factor applied)
    val calibratedSpeedKph = applyCalibratedSpeedKph(telemetry.speedKmh, rigPrefs.speedCalibrationFactor)
    val speedDisplayText = calibratedSpeedKph?.toString() ?: "--"

    val intakeDisplayText = telemetry.intakeAirCelsius?.toString() ?: "--"

    val batteryText = telemetry.batteryVolts?.let {
        String.format(Locale.US, "%.1f", it)
    } ?: "--"
    val isLowBattery = telemetry.batteryVolts != null && telemetry.batteryVolts < rigPrefs.lowBatteryVolts

    val loadText = telemetry.engineLoadPercent?.let { "${it.roundToInt()}" } ?: "--"

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 680.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cockpit_scroll_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 920.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    EcuStatusBanner(
                        connectionStatus = connectionStatus,
                        adapterName = connectedDevice?.name,
                        statusMessage = statusMessage,
                        vinInfo = vinInfo,
                        milOn = telemetry.milOn,
                        onConnectClick = onOpenBluetoothSheet,
                        onDisconnectClick = onDisconnect
                    )

                    // Primary Instrument Cluster (RPM & Coolant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularArcGauge(
                            title = strings.cockpitEngineRpm,
                            valueText = telemetry.rpm?.toString() ?: "--",
                            unit = strings.rpmUnit,
                            value = telemetry.rpm?.toFloat(),
                            minValue = 0f,
                            maxValue = maxRpmScale,
                            warningThreshold = redlineRpm,
                            accentColor = OverlandAmber,
                            modifier = Modifier.weight(1f)
                        )
                        CircularArcGauge(
                            title = strings.cockpitCoolantTemp,
                            valueText = coolantDisplayValue?.roundToInt()?.toString() ?: "--",
                            unit = "°C",
                            value = coolantDisplayValue,
                            minValue = coolantMin,
                            maxValue = coolantMax,
                            warningThreshold = coolantThresholdDisplay,
                            accentColor = TrailSage,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                VitalGridSection(
                                    batteryText = batteryText,
                                    isLowBattery = isLowBattery,
                                    speedText = speedDisplayText,
                                    speedUnit = strings.speedUnitKph,
                                    intakeText = intakeDisplayText,
                                    tempUnit = "°C",
                                    loadText = loadText,
                                    telemetry = telemetry,
                                    isImperial = isImperialMap
                                )
                            }
                            TrailInclinometerCard(
                                sensorState = sensorState,
                                pitchTareDeg = rigPrefs.pitchTareDeg,
                                rollTareDeg = rigPrefs.rollTareDeg,
                                onTareClick = onTareInclinometer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        VitalGridSection(
                            batteryText = batteryText,
                            isLowBattery = isLowBattery,
                            speedText = speedDisplayText,
                            speedUnit = strings.speedUnitKph,
                            intakeText = intakeDisplayText,
                            tempUnit = "°C",
                            loadText = loadText,
                            telemetry = telemetry,
                            isImperial = isImperialMap
                        )

                        TrailInclinometerCard(
                            sensorState = sensorState,
                            pitchTareDeg = rigPrefs.pitchTareDeg,
                            rollTareDeg = rigPrefs.rollTareDeg,
                            onTareClick = onTareInclinometer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun VitalGridSection(
    batteryText: String,
    isLowBattery: Boolean,
    speedText: String,
    speedUnit: String,
    intakeText: String,
    tempUnit: String,
    loadText: String,
    telemetry: LiveTelemetry,
    isImperial: Boolean
) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VitalTelemetryTile(
                label = strings.cockpitBatteryAlt,
                valueText = batteryText,
                unit = "V",
                icon = Icons.Rounded.BatteryChargingFull,
                statusColor = if (isLowBattery) FaultCrimson else TrailSage,
                modifier = Modifier.weight(1f)
            )
            VitalTelemetryTile(
                label = strings.cockpitVehicleSpeed,
                valueText = speedText,
                unit = speedUnit,
                icon = Icons.Rounded.Speed,
                statusColor = OverlandAmber,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VitalTelemetryTile(
                label = strings.cockpitIntakeAir,
                valueText = intakeText,
                unit = tempUnit,
                icon = Icons.Rounded.Air,
                statusColor = SteelBlue,
                modifier = Modifier.weight(1f)
            )
            VitalTelemetryTile(
                label = strings.cockpitEngineLoad,
                valueText = loadText,
                unit = "%",
                icon = Icons.Rounded.Tune,
                statusColor = CautionYellow,
                modifier = Modifier.weight(1f)
            )
        }

        // Fuel Trims & Manifold Strip
        val mapValueText = telemetry.manifoldKpa?.let { kpa ->
            if (isImperial) String.format(Locale.US, "%.1f", kpa * 0.145038f) else "$kpa"
        } ?: "--"
        val mapUnit = if (isImperial) "PSI" else "kPa"
        val stftText = telemetry.shortTermFuelTrimB1?.let {
            String.format(Locale.US, "%+.1f%%", it)
        } ?: "--"
        val ltftText = telemetry.longTermFuelTrimB1?.let {
            String.format(Locale.US, "%+.1f%%", it)
        } ?: "--"
        val throttleText = telemetry.throttlePercent?.let { "${it.roundToInt()}%" } ?: "--"

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniMetric(strings.miniThrottle, throttleText, Icons.Rounded.Compress)
                MiniMetric(strings.miniMap, "$mapValueText $mapUnit", Icons.Rounded.Compress)
                MiniMetric(strings.fuelTrimLabel, stftText, Icons.Rounded.LocalGasStation)
                MiniMetric("LTFT B1", ltftText, Icons.Rounded.LocalGasStation)
            }
        }
    }
}

@Composable
private fun MiniMetric(
    label: String,
    value: String,
    @Suppress("UNUSED_PARAMETER") icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
