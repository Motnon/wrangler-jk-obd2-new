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
import androidx.compose.material.icons.rounded.Shield
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
import com.example.obd.WranglerModel
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FaultCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.TrailSage
import java.util.Locale

@Composable
fun ScannerScreen(
    connectionStatus: ConnectionStatus,
    telemetry: LiveTelemetry,
    activeDtcs: List<String>,
    pendingDtcs: List<String>,
    searchQuery: String,
    wranglerModel: WranglerModel,
    selectedModelFilter: WranglerModel?,
    selectedSubsystem: JkSubsystem,
    catalogCodes: List<DtcCodeInfo>,
    onSearchQueryChange: (String) -> Unit,
    onSelectModelFilter: (WranglerModel?) -> Unit,
    onSelectSubsystem: (JkSubsystem) -> Unit,
    onScanEcu: () -> Unit,
    onClearEcuCodes: () -> Unit,
    onSaveSnapshot: (String) -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    modifier: Modifier = Modifier,
    dtcLookupMap: Map<String, DtcCodeInfo> = emptyMap()
) {
    val strings = LocalAppStrings.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showSaveSnapshotDialog by remember { mutableStateOf(false) }
    var snapshotNote by remember { mutableStateOf("") }
    var expandedCode by remember { mutableStateOf<String?>(null) }

    val isConnected = connectionStatus == ConnectionStatus.CONNECTED

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(strings.clearDialogTitle) },
            text = { Text(strings.clearDialogBody) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearEcuCodes()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FaultCrimson)
                ) {
                    Text(strings.sendMode04Clear)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showSaveSnapshotDialog) {
        AlertDialog(
            onDismissRequest = { showSaveSnapshotDialog = false },
            title = { Text(strings.saveSnapshotTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.saveSnapshotBody,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = snapshotNote,
                        onValueChange = { snapshotNote = it },
                        label = { Text(strings.snapshotNoteLabel) },
                        placeholder = { Text(strings.snapshotNotePlaceholder) },
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
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveSnapshotDialog = false }) {
                    Text(strings.cancel)
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
                                    text = strings.ecuScannerHeader,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when {
                                        !isConnected -> strings.scannerConnectPrompt
                                        activeDtcs.isEmpty() && pendingDtcs.isEmpty() && !telemetry.milOn ->
                                            strings.scannerCleanPrompt
                                        else ->
                                            String.format(
                                                Locale.US,
                                                strings.scannerCountFormat,
                                                activeDtcs.size,
                                                pendingDtcs.size,
                                                if (telemetry.milOn) "ON" else "OFF"
                                            )
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

                if (wranglerModel == WranglerModel.JL) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = CautionYellow.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CautionYellow.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jl_sgw_notice_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = strings.jlSgwBannerTitle,
                                tint = CautionYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = strings.jlSgwBannerTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.jlSgwBannerBody,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Live ECU Detected Codes
                if (activeDtcs.isNotEmpty() || pendingDtcs.isNotEmpty()) {
                    Text(
                        text = strings.liveEcuFaultsHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = FaultCrimson
                    )

                    activeDtcs.forEach { code ->
                        val info = remember(code, wranglerModel, dtcLookupMap) {
                            dtcLookupMap[code.trim().uppercase()]
                                ?: JkDiagnosticLibrary.lookupCode(code, wranglerModel)
                        }
                        DtcCodeCard(
                            info = info,
                            badgeLabel = strings.activeMode03Badge,
                            isExpanded = expandedCode == "live_$code",
                            onToggle = {
                                expandedCode = if (expandedCode == "live_$code") null else "live_$code"
                            }
                        )
                    }
                    pendingDtcs.forEach { code ->
                        val info = remember(code, wranglerModel, dtcLookupMap) {
                            dtcLookupMap[code.trim().uppercase()]
                                ?: JkDiagnosticLibrary.lookupCode(code, wranglerModel)
                        }
                        DtcCodeCard(
                            info = info,
                            badgeLabel = strings.pendingMode07Badge,
                            isExpanded = expandedCode == "pend_$code",
                            onToggle = {
                                expandedCode = if (expandedCode == "pend_$code") null else "pend_$code"
                            }
                        )
                    }
                }

                // Wrangler JK / JL Offline DTC Encyclopedia
                val encyclopediaTitle = if (selectedModelFilter == null) {
                    strings.faultEncyclopediaHeader
                } else {
                    String.format(Locale.US, strings.faultEncyclopediaFormat, selectedModelFilter.code)
                }
                Text(
                    text = encyclopediaTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(strings.searchDtcPlaceholder) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = strings.searchDtcPlaceholder)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dtc_search_input")
                )

                // Model Generation Filter Row (All / JK / JL)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedModelFilter == null,
                        onClick = { onSelectModelFilter(null) },
                        label = { Text(strings.allModelsChip) },
                        modifier = Modifier.testTag("dtc_model_chip_all")
                    )
                    WranglerModel.entries.forEach { model ->
                        FilterChip(
                            selected = selectedModelFilter == model,
                            onClick = { onSelectModelFilter(model) },
                            label = { Text(model.displayName) },
                            modifier = Modifier.testTag("dtc_model_chip_${model.name.lowercase()}")
                        )
                    }
                }

                // Subsystem Filter Row
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
                            label = { Text(strings.subsystemLabel(subsystem, selectedModelFilter ?: wranglerModel)) },
                            modifier = Modifier.testTag("subsystem_chip_${subsystem.name.lowercase()}")
                        )
                    }
                }
            }
        }

        items(catalogCodes, key = { it.code }) { info ->
            DtcCodeCard(
                info = info,
                badgeLabel = strings.subsystemLabel(info.subsystem, selectedModelFilter ?: wranglerModel).uppercase(),
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

                    val modelBadge = when {
                        WranglerModel.JK in info.models && WranglerModel.JL in info.models -> "JK/JL"
                        WranglerModel.JL in info.models -> "JL"
                        else -> "JK"
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "[$modelBadge] $badgeLabel • ${info.affectedEngines}",
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
                                text = LocalAppStrings.current.trailShopGuidance,
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
