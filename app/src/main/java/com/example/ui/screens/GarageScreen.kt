package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ScanRecordEntity
import com.example.obd.JkEngineProfile
import com.example.obd.RigPreferences
import com.example.obd.UnitSystem
import com.example.obd.VinInfo
import com.example.ui.theme.FaultCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OverlandAmber
import com.example.ui.theme.TrailSage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GarageScreen(
    rigPrefs: RigPreferences,
    vinInfo: VinInfo?,
    scanHistory: List<ScanRecordEntity>,
    onUpdateRigDetails: (String, String, String) -> Unit,
    onSelectEngine: (JkEngineProfile) -> Unit,
    onSelectUnitSystem: (UnitSystem) -> Unit,
    onUpdateAlertThresholds: (Int, Float) -> Unit,
    onResetInclinometerTare: () -> Unit,
    onDeleteScanRecord: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var nicknameInput by remember(rigPrefs.rigNickname) { mutableStateOf(rigPrefs.rigNickname) }
    var tireInput by remember(rigPrefs.tireSizeInches) { mutableStateOf(rigPrefs.tireSizeInches) }
    var axleInput by remember(rigPrefs.axleGearRatio) { mutableStateOf(rigPrefs.axleGearRatio) }
    var coolantSlider by remember(rigPrefs.coolantWarnCelsius) { mutableIntStateOf(rigPrefs.coolantWarnCelsius) }
    var batterySlider by remember(rigPrefs.lowBatteryVolts) { mutableFloatStateOf(rigPrefs.lowBatteryVolts) }

    val isImperial = rigPrefs.unitSystem == UnitSystem.IMPERIAL
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.US) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 920.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Banner Card
                Surface(
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_jk_trail_hero),
                            contentDescription = stringResource(R.string.cd_hero_banner),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.large)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = rigPrefs.rigNickname.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Text(
                                text = "${rigPrefs.engineProfile.label} • ${rigPrefs.tireSizeInches} Tires • ${rigPrefs.axleGearRatio} Gears",
                                style = MaterialTheme.typography.bodySmall,
                                color = OverlandAmber
                            )
                            if (vinInfo != null) {
                                Text(
                                    text = "VIN ${vinInfo.vin} (${vinInfo.modelYear} • ${vinInfo.plantSummary})",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TrailSage
                                )
                            }
                        }
                    }
                }

                // JK Rig & Powertrain Configuration
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.DirectionsCar,
                                contentDescription = null,
                                tint = OverlandAmber
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "WRANGLER JK POWERTRAIN & BUILD",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            JkEngineProfile.entries.forEach { profile ->
                                FilterChip(
                                    selected = rigPrefs.engineProfile == profile,
                                    onClick = { onSelectEngine(profile) },
                                    label = {
                                        Text(
                                            text = when (profile) {
                                                JkEngineProfile.PENTASTAR_3_6 -> "3.6L Pentastar"
                                                JkEngineProfile.EGH_3_8 -> "3.8L V6"
                                                JkEngineProfile.CRD_2_8 -> "2.8L CRD"
                                            }
                                        )
                                    },
                                    modifier = Modifier.testTag("engine_chip_${profile.name.lowercase()}")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = nicknameInput,
                            onValueChange = { nicknameInput = it },
                            label = { Text("Rig Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = tireInput,
                                onValueChange = { tireInput = it },
                                label = { Text("Tire Size") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = axleInput,
                                onValueChange = { axleInput = it },
                                label = { Text("Axle Ratio") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = { onUpdateRigDetails(nicknameInput, tireInput, axleInput) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_rig_profile_button")
                        ) {
                            Text("Save Rig Profile")
                        }
                    }
                }

                // Units & Alert Thresholds
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                tint = TrailSage
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "UNITS & TRAIL ALERTS",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UnitSystem.entries.forEach { system ->
                                FilterChip(
                                    selected = rigPrefs.unitSystem == system,
                                    onClick = { onSelectUnitSystem(system) },
                                    label = {
                                        Text(
                                            if (system == UnitSystem.IMPERIAL) "Imperial (°F / MPH)" else "Metric (°C / KM/H)"
                                        )
                                    },
                                    modifier = Modifier.testTag("unit_chip_${system.name.lowercase()}")
                                )
                            }
                        }

                        Column {
                            val coolantText = if (isImperial) {
                                "${((coolantSlider * 9f / 5f) + 32f).roundToInt()}°F"
                            } else {
                                "${coolantSlider}°C"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Coolant High-Temp Warning",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = coolantText,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = OverlandAmber
                                )
                            }
                            Slider(
                                value = coolantSlider.toFloat(),
                                onValueChange = { coolantSlider = it.roundToInt() },
                                onValueChangeFinished = {
                                    onUpdateAlertThresholds(coolantSlider, batterySlider)
                                },
                                valueRange = 95f..120f
                            )
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Low Battery Voltage Warning",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1fV", batterySlider),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = OverlandAmber
                                )
                            }
                            Slider(
                                value = batterySlider,
                                onValueChange = { batterySlider = it },
                                onValueChangeFinished = {
                                    onUpdateAlertThresholds(coolantSlider, batterySlider)
                                },
                                valueRange = 10.5f..12.6f
                            )
                        }

                        if (rigPrefs.pitchTareDeg != 0f || rigPrefs.rollTareDeg != 0f) {
                            Button(
                                onClick = onResetInclinometerTare,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset Inclinometer Factory Zero")
                            }
                        }
                    }
                }

                // Scan History Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = OverlandAmber
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "ECU DIAGNOSTIC LOG (${scanHistory.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (scanHistory.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No saved ECU snapshots yet",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Use the DTC Scan tab to save an ECU fault & telemetry snapshot after connecting to your Jeep.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(scanHistory, key = { it.id }) { record ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = dateFormatter.format(Date(record.timestampMs)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${record.adapterName} • ${record.engineProfile}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val codeSummary = when {
                                record.activeCodes.isNotEmpty() ->
                                    "Active: ${record.activeCodes.joinToString(", ")}"
                                record.pendingCodes.isNotEmpty() ->
                                    "Pending: ${record.pendingCodes.joinToString(", ")}"
                                else -> "Clean Scan (0 DTCs)"
                            }
                            Text(
                                text = codeSummary,
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (record.milOn || record.activeCodes.isNotEmpty()) FaultCrimson else TrailSage
                            )
                            if (record.notes.isNotBlank()) {
                                Text(
                                    text = record.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteScanRecord(record.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete scan record",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
