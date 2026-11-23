package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obd.BluetoothDeviceItem
import com.example.obd.ConnectionStatus
import com.example.obd.DiagnosticCategory
import com.example.obd.DiagnosticReading
import com.example.obd.DiagnosticReadingStatus
import com.example.obd.DiagnosticTestId
import com.example.obd.LiveTelemetry
import com.example.obd.VinInfo
import com.example.ui.components.EcuStatusBanner
import com.example.ui.i18n.DiagnosticStrings
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import java.util.Locale

@Composable
fun LiveDiagnosticsScreen(
    connectionStatus: ConnectionStatus,
    connectedDevice: BluetoothDeviceItem?,
    statusMessage: String,
    vinInfo: VinInfo?,
    telemetry: LiveTelemetry,
    appliedTests: Set<DiagnosticTestId>,
    readings: Map<DiagnosticTestId, DiagnosticReading>,
    isScanPaused: Boolean,
    cycleCount: Long,
    onApplyTests: (Set<DiagnosticTestId>) -> Unit,
    onTogglePause: () -> Unit,
    onReprobeUnsupported: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSelectionView by rememberSaveable { mutableStateOf(appliedTests.isEmpty()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var draftSelectedKeys by rememberSaveable {
        mutableStateOf(DiagnosticTestId.encodeSet(appliedTests))
    }

    // Keep draft checkboxes synced when persisted appliedTests load initially
    LaunchedEffect(appliedTests) {
        if (draftSelectedKeys.isEmpty() && appliedTests.isNotEmpty()) {
            draftSelectedKeys = DiagnosticTestId.encodeSet(appliedTests)
            showSelectionView = false
        }
    }

    val draftSelected = remember(draftSelectedKeys) {
        DiagnosticTestId.decodeSet(draftSelectedKeys)
    }

    val strings = LocalAppStrings.current
    val isConnected = connectionStatus == ConnectionStatus.CONNECTED

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

            // Mode switcher: Select PIDs (Checkboxes) <-> Live Scan List
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ModeTabButton(
                        selected = showSelectionView,
                        icon = Icons.Rounded.Checklist,
                        label = String.format(Locale.US, strings.selectTestsTabFormat, draftSelected.size),
                        onClick = { showSelectionView = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("diag_mode_select")
                    )
                    ModeTabButton(
                        selected = !showSelectionView,
                        icon = Icons.Rounded.Sensors,
                        label = String.format(Locale.US, strings.liveScanTabFormat, appliedTests.size),
                        onClick = { showSelectionView = false },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("diag_mode_live")
                    )
                }
            }

            if (showSelectionView) {
                ChecklistSelectionView(
                    draftSelected = draftSelected,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onToggleTest = { testId ->
                        val next = draftSelected.toMutableSet()
                        if (!next.add(testId)) next.remove(testId)
                        draftSelectedKeys = DiagnosticTestId.encodeSet(next)
                    },
                    onToggleCategory = { testsInCat ->
                        val allChecked = testsInCat.all { it in draftSelected }
                        val next = draftSelected.toMutableSet()
                        if (allChecked) {
                            next.removeAll(testsInCat.toSet())
                        } else {
                            next.addAll(testsInCat)
                        }
                        draftSelectedKeys = DiagnosticTestId.encodeSet(next)
                    },
                    onSelectAll = {
                        draftSelectedKeys = DiagnosticTestId.encodeSet(DiagnosticTestId.entries.toSet())
                    },
                    onClearAll = {
                        draftSelectedKeys = ""
                    },
                    onApply = {
                        onApplyTests(draftSelected)
                        showSelectionView = false
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LiveScanResultsView(
                    isConnected = isConnected,
                    isScanPaused = isScanPaused,
                    cycleCount = cycleCount,
                    appliedTests = appliedTests,
                    readings = readings,
                    onTogglePause = onTogglePause,
                    onReprobeUnsupported = onReprobeUnsupported,
                    onEditSelection = {
                        draftSelectedKeys = DiagnosticTestId.encodeSet(appliedTests)
                        showSelectionView = true
                    },
                    onOpenBluetoothSheet = onOpenBluetoothSheet,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeTabButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
        },
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChecklistSelectionView(
    draftSelected: Set<DiagnosticTestId>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleTest: (DiagnosticTestId) -> Unit,
    onToggleCategory: (List<DiagnosticTestId>) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val lang = strings.language
    val grouped = remember(searchQuery, lang) {
        val q = searchQuery.trim().lowercase()
        DiagnosticTestId.groupedByCategory().mapNotNull { (cat, list) ->
            val catLocalized = DiagnosticStrings.categoryTitle(cat, lang).lowercase()
            val filtered = if (q.isEmpty()) {
                list
            } else {
                list.filter {
                    it.title.lowercase().contains(q) ||
                        DiagnosticStrings.testTitle(it, lang).lowercase().contains(q) ||
                        it.pidBadge.lowercase().contains(q) ||
                        cat.title.lowercase().contains(q) ||
                        catLocalized.contains(q)
                }
            }
            if (filtered.isEmpty()) null else cat to filtered
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filter & Select All / Clear controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(strings.filterPidsPlaceholder, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = strings.filterPidsPlaceholder,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("diag_search_input")
            )

            OutlinedButton(
                onClick = onSelectAll,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("diag_select_all_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckBox,
                    contentDescription = strings.selectAllShort,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.selectAllShort, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onClearAll,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("diag_clear_all_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.ClearAll,
                    contentDescription = strings.clearShort,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.clearShort, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Scrollable categorized checkbox list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("diag_checklist_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            grouped.forEach { (category, tests) ->
                val checkedInCategory = tests.count { it in draftSelected }
                val toggleState = when (checkedInCategory) {
                    0 -> ToggleableState.Off
                    tests.size -> ToggleableState.On
                    else -> ToggleableState.Indeterminate
                }

                item(key = "header_${category.name}") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleCategory(tests) }
                            .testTag("diag_category_header_${category.number}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TriStateCheckbox(
                                    state = toggleState,
                                    onClick = { onToggleCategory(tests) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                )
                                Text(
                                    text = DiagnosticStrings.categoryTitle(category, lang),
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "$checkedInCategory/${tests.size}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                items(tests, key = { it.key }) { test ->
                    val checked = test in draftSelected
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (checked) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = BorderStroke(
                            1.dp,
                            if (checked) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleTest(test) }
                            .testTag("diag_checkbox_row_${test.key}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { onToggleTest(test) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("diag_checkbox_${test.key}")
                                )
                                Column(modifier = Modifier.padding(end = 8.dp)) {
                                    Text(
                                        text = DiagnosticStrings.testTitle(test, lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = String.format(Locale.US, strings.unitPrefix, test.unit),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = test.pidBadge,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Prominent bottom Apply button
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = String.format(
                            Locale.US,
                            strings.testsMarkedFormat,
                            draftSelected.size,
                            DiagnosticTestId.entries.size
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.applyHint,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApply,
                    enabled = draftSelected.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("diag_apply_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = strings.applyHint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, strings.applyCountFormat, draftSelected.size),
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveScanResultsView(
    isConnected: Boolean,
    isScanPaused: Boolean,
    cycleCount: Long,
    appliedTests: Set<DiagnosticTestId>,
    readings: Map<DiagnosticTestId, DiagnosticReading>,
    onTogglePause: () -> Unit,
    onReprobeUnsupported: () -> Unit,
    onEditSelection: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val lang = strings.language
    if (appliedTests.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Checklist,
                    contentDescription = strings.noTestsChosenTitle,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = strings.noTestsChosenTitle,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.noTestsChosenBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onEditSelection,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("diag_empty_select_button")
                ) {
                    Text(strings.chooseTestsButton)
                }
            }
        }
        return
    }

    val hasUnsupported = readings.values.any { it.status == DiagnosticReadingStatus.UNSUPPORTED }
    val orderedApplied = remember(appliedTests) {
        DiagnosticTestId.groupedByCategory().mapNotNull { (cat, list) ->
            val chosen = list.filter { it in appliedTests }
            if (chosen.isEmpty()) null else cat to chosen
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Real-time scan control bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val palette = LocalRacePalette.current
                    val dotColor = when {
                        !isConnected -> palette.zone3
                        isScanPaused -> palette.zone3
                        else -> palette.zone2
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Column {
                        Text(
                            text = when {
                                !isConnected -> strings.readyConnectObd
                                isScanPaused -> String.format(Locale.US, strings.scanPausedFormat, appliedTests.size)
                                else -> String.format(Locale.US, strings.realTimeScanFormat, cycleCount)
                            },
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.US, strings.chosenParamsFormat, appliedTests.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isConnected) {
                        Button(
                            onClick = onOpenBluetoothSheet,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("diag_connect_button")
                        ) {
                            Text(strings.connectShort, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onTogglePause,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("diag_pause_resume_button")
                        ) {
                            Icon(
                                imageVector = if (isScanPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                contentDescription = if (isScanPaused) strings.resumeShort else strings.pauseShort,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isScanPaused) strings.resumeShort else strings.pauseShort,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (hasUnsupported) {
                            OutlinedButton(
                                onClick = onReprobeUnsupported,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("diag_reprobe_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Re-probe PIDs",
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onEditSelection,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("diag_edit_tests_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = strings.editShort,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.editShort, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live scanned items list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("diag_live_results_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            orderedApplied.forEach { (category, tests) ->
                item(key = "live_cat_${category.name}") {
                    Text(
                        text = DiagnosticStrings.categoryTitle(category, lang).uppercase(),
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                items(tests, key = { "live_${it.key}" }) { test ->
                    val reading = readings[test] ?: DiagnosticReading(
                        testId = test,
                        formattedValue = "--",
                        status = if (isConnected) DiagnosticReadingStatus.SCANNING else DiagnosticReadingStatus.IDLE
                    )
                    LiveDiagnosticTestCard(
                        test = test,
                        reading = reading,
                        modifier = Modifier.testTag("diag_live_card_${test.key}")
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveDiagnosticTestCard(
    test: DiagnosticTestId,
    reading: DiagnosticReading,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    val borderColor = when {
        reading.isAlert -> palette.zone5.copy(alpha = 0.85f)
        reading.status == DiagnosticReadingStatus.LIVE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }
    val valueColor = when {
        reading.isAlert -> palette.zone5
        reading.status == DiagnosticReadingStatus.LIVE -> MaterialTheme.colorScheme.primary
        reading.status == DiagnosticReadingStatus.UNSUPPORTED -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusBadgeText = DiagnosticStrings.statusBadge(
        status = reading.status,
        isAlert = reading.isAlert,
        lang = strings.language
    )
    val statusBadgeColor = when (reading.status) {
        DiagnosticReadingStatus.LIVE -> if (reading.isAlert) palette.zone5 else palette.zone2
        DiagnosticReadingStatus.SCANNING -> MaterialTheme.colorScheme.primary
        DiagnosticReadingStatus.UNSUPPORTED -> MaterialTheme.colorScheme.onSurfaceVariant
        DiagnosticReadingStatus.ERROR -> palette.zone5
        DiagnosticReadingStatus.IDLE -> palette.zone3
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = DiagnosticStrings.testTitle(test, strings.language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "PID ${test.pidBadge}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = statusBadgeColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = statusBadgeText,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = statusBadgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = reading.formattedValue,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = valueColor
            )
        }
    }
}
