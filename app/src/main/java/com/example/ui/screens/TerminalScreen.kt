package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.obd.ConnectionStatus
import com.example.obd.JkDiagnosticLibrary
import com.example.obd.LiveTelemetry
import com.example.obd.RigPreferences
import com.example.obd.TerminalDirection
import com.example.obd.TerminalEntry
import com.example.obd.UnitSystem
import com.example.obd.applyCalibratedSpeedKph
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.FaultCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OverlandAmber
import com.example.ui.theme.TrailSage
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TerminalScreen(
    connectionStatus: ConnectionStatus,
    terminalLogs: List<TerminalEntry>,
    telemetry: LiveTelemetry,
    rigPrefs: RigPreferences,
    onSendCommand: (String) -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var commandInput by remember { mutableStateOf("") }
    val presetCommands = remember {
        listOf("ATZ", "ATI", "ATRV", "0100", "0105", "010C", "012F", "0142", "03", "0902")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 920.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                        imageVector = Icons.Rounded.Terminal,
                        contentDescription = null,
                        tint = OverlandAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = strings.terminalHeader,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (connectionStatus == ConnectionStatus.CONNECTED) {
                                strings.terminalActiveLink
                            } else {
                                strings.terminalOffline
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (connectionStatus != ConnectionStatus.CONNECTED) {
                    OutlinedButton(onClick = onOpenBluetoothSheet) {
                        Icon(
                            imageVector = Icons.Rounded.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(strings.pairShort)
                    }
                }
            }

            // Preset Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetCommands.forEach { cmd ->
                    AssistChip(
                        onClick = { onSendCommand(cmd) },
                        label = {
                            Text(
                                text = cmd,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.testTag("preset_cmd_$cmd")
                    )
                }
            }

            // Console Log + Supported PID Reference
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = strings.rawTxRxStream,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            terminalLogs.takeLast(35).forEach { entry ->
                                val prefix = when (entry.direction) {
                                    TerminalDirection.TX -> "TX >"
                                    TerminalDirection.RX -> "RX <"
                                    TerminalDirection.INFO -> "SYS "
                                    TerminalDirection.ERROR -> "ERR!"
                                }
                                val entryColor = when (entry.direction) {
                                    TerminalDirection.TX -> OverlandAmber
                                    TerminalDirection.RX -> TrailSage
                                    TerminalDirection.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
                                    TerminalDirection.ERROR -> FaultCrimson
                                }
                                Text(
                                    text = "[${entry.timestamp}] $prefix ${entry.message}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = entryColor
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.supportedPidMonitor,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(JkDiagnosticLibrary.supportedPids, key = { it.pidHex }) { pid ->
                    val isImperial = rigPrefs.unitSystem == UnitSystem.IMPERIAL
                    val formattedVal = formatLivePidValue(
                        pidHex = pid.pidHex,
                        t = telemetry,
                        imperial = isImperial,
                        speedFactor = rigPrefs.speedCalibrationFactor
                    )
                    val unitLabel = when (pid.pidHex) {
                        "0105", "010F", "0146" -> "°C"
                        "010D" -> strings.speedUnitKph
                        else -> if (isImperial) pid.unitImperial else pid.unitMetric
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSendCommand(pid.pidHex) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = pid.pidHex,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OverlandAmber
                                    )
                                    Text(
                                        text = pid.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = pid.jkNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$formattedVal $unitLabel",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (formattedVal == "--") {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    TrailSage
                                }
                            )
                        }
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it.uppercase() },
                    placeholder = { Text(strings.terminalInputPlaceholder) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (commandInput.isNotBlank()) {
                                onSendCommand(commandInput)
                                commandInput = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_command_input")
                )

                IconButton(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            onSendCommand(commandInput)
                            commandInput = ""
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("send_terminal_command_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = stringResource(R.string.cd_send_command)
                    )
                }
            }
        }
    }
}

private fun formatLivePidValue(
    pidHex: String,
    t: LiveTelemetry,
    imperial: Boolean,
    speedFactor: Float
): String {
    return when (pidHex) {
        "010C" -> t.rpm?.toString() ?: "--"
        "0105" -> t.coolantCelsius?.toString() ?: "--"
        "ATRV" -> t.batteryVolts?.let { String.format(Locale.US, "%.1f", it) } ?: "--"
        "010D" -> applyCalibratedSpeedKph(t.speedKmh, speedFactor)?.toString() ?: "--"
        "010F" -> t.intakeAirCelsius?.toString() ?: "--"
        "0111" -> t.throttlePercent?.let { "${it.roundToInt()}" } ?: "--"
        "0104" -> t.engineLoadPercent?.let { "${it.roundToInt()}" } ?: "--"
        "010B" -> t.manifoldKpa?.let { if (imperial) String.format(Locale.US, "%.1f", it * 0.145038f) else "$it" } ?: "--"
        "0106" -> t.shortTermFuelTrimB1?.let { String.format(Locale.US, "%+.1f", it) } ?: "--"
        "0107" -> t.longTermFuelTrimB1?.let { String.format(Locale.US, "%+.1f", it) } ?: "--"
        "012F" -> t.fuelLevelPercent?.let { "${it.roundToInt()}" } ?: "--"
        "0146" -> t.ambientAirCelsius?.toString() ?: "--"
        "0101" -> if (t.lastUpdatedMs > 0L) "${t.ecuDtcCount} (${if (t.milOn) "MIL" else "OK"})" else "--"
        else -> "--"
    }
}
