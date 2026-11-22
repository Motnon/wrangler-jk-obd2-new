package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.obd.ConnectionStatus
import com.example.obd.DtcCodeInfo
import com.example.obd.DtcSeverity
import com.example.obd.JkDiagnosticLibrary
import com.example.obd.JkSubsystem
import com.example.obd.LiveTelemetry
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FaultCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.TrailSage

@Composable
fun ScannerScreen(
    connectionStatus: ConnectionStatus,
    telemetry: LiveTelemetry,
    activeDtcs: List<String>,
    pendingDtcs: List<String>,
    searchQuery: String,
    selectedSubsystem: JkSubsystem,
    catalogCodes: List<DtcCodeInfo>,
    onSearchQueryChange: (String) -> Unit,
    onSelectSubsystem: (JkSubsystem) -> Unit,
    onScanEcu: () -> Unit,
    onClearEcuCodes: () -> Unit,
    onSaveSnapshot: (String) -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showSaveSnapshotDialog by remember { mutableStateOf(false) }
    var snapshotNote by remember { mutableStateOf("") }
    var expandedCode by remember { mutableStateOf<String?>(null) }

    val isConnected = connectionStatus == ConnectionStatus.CONNECTED

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear MIL & Stored DTCs?") },
            text = {
                Text(
                    "Mode 04 resets the Check Engine Light, stored codes, and emissions readiness monitors on your Wrangler JK ECU. Only clear codes after inspecting the fault."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearEcuCodes()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FaultCrimson)
                ) {
                    Text("Send Mode 04 Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSaveSnapshotDialog) {
        AlertDialog(
            onDismissRequest = { showSaveSnapshotDialog = false },
            title = { Text("Save ECU Snapshot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Stores current MIL state, detected DTCs, coolant temp, and battery voltage to your local Garage log.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = snapshotNote,
                        onValueChange = { snapshotNote = it },
                        label = { Text("Trail / Service Note (optional)") },
                        placeholder = { Text("e.g. Cold start after Rubicon trail") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveSnapshot(snapshotNote)
                        snapshotNote = ""
                        showSaveSnapshotDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveSnapshotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 920.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ECU MIL Status & Scan Actions
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (telemetry.milOn || activeDtcs.isNotEmpty()) {
                            FaultCrimson.copy(alpha = 0.65f)
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ECU DIAGNOSTIC SCANNER",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when {
                                        !isConnected -> "Connect ELM327 adapter to read live Mode 03/07 codes"
                                        activeDtcs.isEmpty() && pendingDtcs.isEmpty() && !telemetry.milOn ->
                                            "No active or pending fault codes reported"
                                        else ->
                                            "${activeDtcs.size} Active • ${pendingDtcs.size} Pending • MIL ${if (telemetry.milOn) "ON" else "OFF"}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = if (telemetry.milOn || activeDtcs.isNotEmpty()) {
                                    Icons.Rounded.Warning
                                } else {
                                    Icons.Rounded.CheckCircle
                                },
                                contentDescription = null,
                                tint = if (!isConnected) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else if (telemetry.milOn || activeDtcs.isNotEmpty()) {
                                    FaultCrimson
                                } else {
                                    TrailSage
                                },
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isConnected) onScanEcu() else onOpenBluetoothSheet()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_ecu_codes_button")
                            ) {
                                Text(
                                    text = if (isConnected) {
                                        stringResource(R.string.action_read_dtcs)
                                    } else {
                                        stringResource(R.string.action_connect_adapter)
                                    }
                                )
                            }

                            OutlinedButton(
                                onClick = { showSaveSnapshotDialog = true },
                                modifier = Modifier.testTag("save_snapshot_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BookmarkAdd,
                                    contentDescription = stringResource(R.string.action_save_snapshot),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            OutlinedButton(
                                onClick = { showClearConfirmDialog = true },
                                enabled = isConnected,
                                modifier = Modifier.testTag("clear_dtc_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteSweep,
                                    contentDescription = stringResource(R.string.cd_clear_codes),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Live ECU Detected Codes
                if (activeDtcs.isNotEmpty() || pendingDtcs.isNotEmpty()) {
                    Text(
                        text = "LIVE ECU FAULT CODES",
                        style = MaterialTheme.typography.labelLarge,
                        color = FaultCrimson
                    )

                    activeDtcs.forEach { code ->
                        val info = remember(code) { JkDiagnosticLibrary.lookupCode(code) }
                        DtcCodeCard(
                            info = info,
                            badgeLabel = "ACTIVE (MODE 03)",
                            isExpanded = expandedCode == "live_$code",
                            onToggle = {
                                expandedCode = if (expandedCode == "live_$code") null else "live_$code"
                            }
                        )
                    }
                    pendingDtcs.forEach { code ->
                        val info = remember(code) { JkDiagnosticLibrary.lookupCode(code) }
                        DtcCodeCard(
                            info = info,
                            badgeLabel = "PENDING (MODE 07)",
                            isExpanded = expandedCode == "pend_$code",
                            onToggle = {
                                expandedCode = if (expandedCode == "pend_$code") null else "pend_$code"
                            }
                        )
                    }
                }

                // Wrangler JK Offline DTC Encyclopedia
                Text(
                    text = "WRANGLER JK FAULT ENCYCLOPEDIA",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search code or symptom (e.g. P0456, TIPM, misfire)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search codes")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dtc_search_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JkSubsystem.entries.forEach { subsystem ->
                        FilterChip(
                            selected = selectedSubsystem == subsystem,
                            onClick = { onSelectSubsystem(subsystem) },
                            label = { Text(subsystem.label) },
                            modifier = Modifier.testTag("subsystem_chip_${subsystem.name.lowercase()}")
                        )
                    }
                }
            }
        }

        items(catalogCodes, key = { it.code }) { info ->
            DtcCodeCard(
                info = info,
                badgeLabel = info.subsystem.label.uppercase(),
                isExpanded = expandedCode == info.code,
                onToggle = {
                    expandedCode = if (expandedCode == info.code) null else info.code
                },
                modifier = Modifier.widthIn(max = 920.dp)
            )
        }
    }
}

@Composable
private fun DtcCodeCard(
    info: DtcCodeInfo,
    badgeLabel: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val severityColor = when (info.severity) {
        DtcSeverity.CRITICAL -> FaultCrimson
        DtcSeverity.WARNING -> CautionYellow
        DtcSeverity.INFO -> SteelBlue
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dtc_card_${info.code}")
            .clickable(onClick = onToggle),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = severityColor.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = info.code,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = severityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$badgeLabel • ${info.affectedEngines}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse code details" else "Expand code details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = info.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "TRAIL & SHOP GUIDANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = info.trailGuidance,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
