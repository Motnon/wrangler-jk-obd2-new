package com.example.ui.styles

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ScanRecordEntity
import com.example.obd.AppLanguage
import com.example.obd.AppUiStyle
import com.example.obd.AppVisualTheme
import com.example.obd.BluetoothDeviceItem
import com.example.obd.CachedVehicleBinding
import com.example.obd.ConnectionStatus
import com.example.obd.DetectedVehicle
import com.example.obd.DiagnosticReading
import com.example.obd.DiagnosticTestId
import com.example.obd.DtcCodeInfo
import com.example.obd.JkEngineProfile
import com.example.obd.JkSubsystem
import com.example.obd.LiveTelemetry
import com.example.obd.NightModeSetting
import com.example.obd.RealSpeedSource
import com.example.obd.RigPreferences
import com.example.obd.TerminalEntry
import com.example.obd.TrailSensorState
import com.example.obd.UnitSystem
import com.example.obd.VehicleProfile
import com.example.obd.VinInfo
import com.example.obd.WranglerModel
import com.example.obd.resolveRealSpeed
import com.example.sensors.GpsTelemetryState
import com.example.sensors.PhoneBatteryState
import com.example.sensors.SolarSchedule
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.LiveDiagnosticsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class HudTab {
    HUD,
    PIDS,
    FAULTS,
    TERM,
    RIG
}

@Composable
fun AvionicsHudShell(
    connectionStatus: ConnectionStatus,
    connectedDevice: BluetoothDeviceItem?,
    statusMessage: String,
    vinInfo: VinInfo?,
    detectedVehicle: DetectedVehicle?,
    activeProfile: VehicleProfile?,
    supportedMode01Pids: Set<Int>,
    cachedBindings: Map<String, CachedVehicleBinding>,
    telemetry: LiveTelemetry,
    activeDtcs: List<String>,
    pendingDtcs: List<String>,
    terminalLogs: List<TerminalEntry>,
    sensorState: TrailSensorState,
    phoneBattery: PhoneBatteryState,
    gpsState: GpsTelemetryState,
    solarSchedule: SolarSchedule,
    isNightModeActive: Boolean,
    rigPrefs: RigPreferences,
    scanHistory: List<ScanRecordEntity>,
    dtcSearchQuery: String,
    selectedDtcModelFilter: WranglerModel?,
    selectedSubsystem: JkSubsystem,
    catalogCodes: List<DtcCodeInfo>,
    appliedDiagnosticTests: Set<DiagnosticTestId>,
    diagnosticReadings: Map<DiagnosticTestId, DiagnosticReading>,
    isDiagnosticScanPaused: Boolean,
    diagnosticCycleCount: Long,
    hasFineLocationPermission: Boolean,
    snackbarHostState: SnackbarHostState,
    onOpenBluetoothSheet: () -> Unit,
    onOpenUiStyleSheet: () -> Unit,
    onDisconnect: () -> Unit,
    onTareInclinometer: () -> Unit,
    onSetRaceDayMode: (Boolean) -> Unit,
    onDiagnosticScreenVisibleChanged: (Boolean) -> Unit,
    onApplyDiagnosticTests: (Set<DiagnosticTestId>) -> Unit,
    onToggleDiagnosticPause: () -> Unit,
    onReprobeUnsupportedDiagnostics: () -> Unit,
    onDtcSearchQueryChange: (String) -> Unit,
    onSelectDtcModelFilter: (WranglerModel?) -> Unit,
    onSelectSubsystem: (JkSubsystem) -> Unit,
    onScanEcu: () -> Unit,
    onClearEcuCodes: () -> Unit,
    onSaveSnapshot: (String) -> Unit,
    onSendTerminalCommand: (String) -> Unit,
    onUpdateRigDetails: (String, String, String) -> Unit,
    onSelectWranglerModel: (WranglerModel) -> Unit,
    onSelectEngine: (JkEngineProfile) -> Unit,
    onSelectUnitSystem: (UnitSystem) -> Unit,
    onUpdateAlertThresholds: (Int, Float) -> Unit,
    onSelectVisualTheme: (AppVisualTheme) -> Unit,
    onSelectNightMode: (NightModeSetting) -> Unit,
    onNightBrightnessChange: (Int) -> Unit,
    onSelectAppLanguage: (AppLanguage) -> Unit,
    onSelectUiStyle: (AppUiStyle) -> Unit,
    onLocationPermissionResult: (Boolean, Boolean) -> Unit,
    onStartGpsCalibration: () -> Unit,
    onStopGpsCalibration: () -> Unit,
    onApplyLiveGpsCalibration: () -> Unit,
    onApplyTireRatioCalibration: (Float) -> Unit,
    onSetSpeedCalibrationFactor: (Float) -> Unit,
    onResetInclinometerTare: () -> Unit,
    onDeleteScanRecord: (Long) -> Unit,
    onSelectProfileOverride: (String) -> Unit,
    onForgetCachedAdapter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    var selectedTab by rememberSaveable { mutableStateOf(HudTab.HUD) }
    val isDayMode = !isNightModeActive && rigPrefs.visualTheme == AppVisualTheme.CLASSIC_WHITE

    LaunchedEffect(selectedTab) {
        onDiagnosticScreenVisibleChanged(selectedTab == HudTab.PIDS)
    }

    BackHandler(enabled = selectedTab != HudTab.HUD) {
        selectedTab = HudTab.HUD
    }

    val hudBgBrush = Brush.verticalGradient(
        colors = listOf(
            palette.boardBg,
            palette.surface.copy(alpha = 0.94f),
            palette.boardBg
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(hudBgBrush)
    ) {
        // Subtle technical grid backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 44.dp.toPx()
            val gridColor = palette.zone1.copy(alpha = 0.045f)
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Avionics Top Telemetry Strip
            Surface(
                shape = CutCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
                color = palette.podSurface.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, palette.zone1.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
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
                            text = (detectedVehicle?.displayTitle ?: rigPrefs.rigNickname).uppercase(),
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val hasFault = telemetry.milOn || activeDtcs.isNotEmpty()
                            Text(
                                text = if (hasFault) strings.hudFlightAlert else strings.hudFlightNominal,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (hasFault) palette.zone5 else palette.zone2
                            )
                            detectedVehicle?.vin?.let { vin ->
                                Text(
                                    text = "• $vin",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = palette.zone1,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${phoneBattery.levelPercent}%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = palette.textSecondary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        IconButton(
                            onClick = { onSetRaceDayMode(!isDayMode) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("hud_day_dark_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDayMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                contentDescription = stringResource(if (isDayMode) R.string.race_mode_dark else R.string.race_mode_day),
                                tint = palette.zone3,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onOpenUiStyleSheet,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("topbar_ui_style_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DashboardCustomize,
                                contentDescription = strings.cdSwitchUiStyle,
                                tint = palette.zone1,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onOpenBluetoothSheet,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("topbar_bluetooth_button")
                        ) {
                            Icon(
                                imageVector = if (connectionStatus == ConnectionStatus.CONNECTED) {
                                    Icons.Rounded.BluetoothConnected
                                } else {
                                    Icons.Rounded.Bluetooth
                                },
                                contentDescription = stringResource(R.string.cd_connect_bluetooth),
                                tint = if (connectionStatus == ConnectionStatus.CONNECTED) palette.zone2 else palette.zone4,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Main Viewport Area (leaves space for floating glass dock)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    HudTab.HUD -> AvionicsPrimaryHudDeck(
                        connectionStatus = connectionStatus,
                        connectedDevice = connectedDevice,
                        statusMessage = statusMessage,
                        telemetry = telemetry,
                        activeDtcs = activeDtcs,
                        pendingDtcs = pendingDtcs,
                        sensorState = sensorState,
                        gpsState = gpsState,
                        rigPrefs = rigPrefs,
                        detectedVehicle = detectedVehicle,
                        activeProfile = activeProfile,
                        onOpenBluetoothSheet = onOpenBluetoothSheet,
                        onDisconnect = onDisconnect,
                        onTareInclinometer = onTareInclinometer,
                        onJumpToPids = { selectedTab = HudTab.PIDS },
                        onJumpToFaults = { selectedTab = HudTab.FAULTS },
                        modifier = Modifier.fillMaxSize()
                    )

                    HudTab.PIDS -> LiveDiagnosticsScreen(
                        connectionStatus = connectionStatus,
                        connectedDevice = connectedDevice,
                        statusMessage = statusMessage,
                        vinInfo = vinInfo,
                        telemetry = telemetry,
                        appliedTests = appliedDiagnosticTests,
                        readings = diagnosticReadings,
                        isScanPaused = isDiagnosticScanPaused,
                        cycleCount = diagnosticCycleCount,
                        onApplyTests = onApplyDiagnosticTests,
                        onTogglePause = onToggleDiagnosticPause,
                        onReprobeUnsupported = onReprobeUnsupportedDiagnostics,
                        onOpenBluetoothSheet = onOpenBluetoothSheet,
                        onDisconnect = onDisconnect,
                        detectedVehicle = detectedVehicle,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 82.dp)
                    )

                    HudTab.FAULTS -> ScannerScreen(
                        connectionStatus = connectionStatus,
                        telemetry = telemetry,
                        activeDtcs = activeDtcs,
                        pendingDtcs = pendingDtcs,
                        searchQuery = dtcSearchQuery,
                        wranglerModel = rigPrefs.wranglerModel,
                        selectedModelFilter = selectedDtcModelFilter,
                        selectedSubsystem = selectedSubsystem,
                        catalogCodes = catalogCodes,
                        onSearchQueryChange = onDtcSearchQueryChange,
                        onSelectModelFilter = onSelectDtcModelFilter,
                        onSelectSubsystem = onSelectSubsystem,
                        onScanEcu = onScanEcu,
                        onClearEcuCodes = onClearEcuCodes,
                        onSaveSnapshot = onSaveSnapshot,
                        onOpenBluetoothSheet = onOpenBluetoothSheet,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 82.dp)
                    )

                    HudTab.TERM -> TerminalScreen(
                        connectionStatus = connectionStatus,
                        terminalLogs = terminalLogs,
                        telemetry = telemetry,
                        rigPrefs = rigPrefs,
                        onSendCommand = onSendTerminalCommand,
                        onOpenBluetoothSheet = onOpenBluetoothSheet,
                        activeProfile = activeProfile,
                        supportedMode01Pids = supportedMode01Pids,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 82.dp)
                    )

                    HudTab.RIG -> GarageScreen(
                        rigPrefs = rigPrefs,
                        vinInfo = vinInfo,
                        telemetry = telemetry,
                        gpsState = gpsState,
                        solarSchedule = solarSchedule,
                        isNightModeActive = isNightModeActive,
                        hasFineLocationPermission = hasFineLocationPermission,
                        scanHistory = scanHistory,
                        onUpdateRigDetails = onUpdateRigDetails,
                        onSelectWranglerModel = onSelectWranglerModel,
                        onSelectEngine = onSelectEngine,
                        onSelectUnitSystem = onSelectUnitSystem,
                        onUpdateAlertThresholds = onUpdateAlertThresholds,
                        onSelectVisualTheme = onSelectVisualTheme,
                        onSelectNightMode = onSelectNightMode,
                        onNightBrightnessChange = onNightBrightnessChange,
                        onSelectAppLanguage = onSelectAppLanguage,
                        onLocationPermissionResult = onLocationPermissionResult,
                        onStartGpsCalibration = onStartGpsCalibration,
                        onStopGpsCalibration = onStopGpsCalibration,
                        onApplyLiveGpsCalibration = onApplyLiveGpsCalibration,
                        onApplyTireRatioCalibration = onApplyTireRatioCalibration,
                        onSetSpeedCalibrationFactor = onSetSpeedCalibrationFactor,
                        onResetInclinometerTare = onResetInclinometerTare,
                        onDeleteScanRecord = onDeleteScanRecord,
                        detectedVehicle = detectedVehicle,
                        activeProfile = activeProfile,
                        supportedMode01Pids = supportedMode01Pids,
                        cachedBindings = cachedBindings,
                        onSelectProfileOverride = onSelectProfileOverride,
                        onForgetCachedAdapter = onForgetCachedAdapter,
                        onSelectUiStyle = onSelectUiStyle,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 82.dp)
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 92.dp, start = 16.dp, end = 16.dp)
                )

                // Floating Translucent Glass HUD Dock
                AvionicsFloatingDock(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun AvionicsFloatingDock(
    selectedTab: HudTab,
    onSelectTab: (HudTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    val items = listOf(
        Triple(HudTab.HUD, strings.hudNavHud, Icons.Rounded.Radar),
        Triple(HudTab.PIDS, strings.hudNavPids, Icons.Rounded.FactCheck),
        Triple(HudTab.FAULTS, strings.hudNavFaults, Icons.Rounded.Build),
        Triple(HudTab.TERM, strings.hudNavTerm, Icons.Rounded.Terminal),
        Triple(HudTab.RIG, strings.hudNavRig, Icons.Rounded.DirectionsCar)
    )

    Surface(
        shape = CutCornerShape(16.dp),
        color = palette.podSurface.copy(alpha = 0.94f),
        border = BorderStroke(1.2.dp, palette.zone1.copy(alpha = 0.55f)),
        shadowElevation = 10.dp,
        modifier = modifier.widthIn(max = 580.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (tab, label, icon) ->
                val isSelected = selectedTab == tab
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) palette.zone1.copy(alpha = 0.22f) else Color.Transparent,
                    label = "hud_tab_bg"
                )
                val fgColor = if (isSelected) palette.zone1 else palette.textSecondary

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(CutCornerShape(10.dp))
                        .background(bgColor)
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 6.dp)
                        .testTag("hud_nav_${tab.name.lowercase()}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = fgColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            color = fgColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvionicsPrimaryHudDeck(
    connectionStatus: ConnectionStatus,
    connectedDevice: BluetoothDeviceItem?,
    statusMessage: String,
    telemetry: LiveTelemetry,
    activeDtcs: List<String>,
    pendingDtcs: List<String>,
    sensorState: TrailSensorState,
    gpsState: GpsTelemetryState,
    rigPrefs: RigPreferences,
    detectedVehicle: DetectedVehicle?,
    activeProfile: VehicleProfile?,
    onOpenBluetoothSheet: () -> Unit,
    onDisconnect: () -> Unit,
    onTareInclinometer: () -> Unit,
    onJumpToPids: () -> Unit,
    onJumpToFaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    val maxRpm = (activeProfile?.maxRpmScale ?: rigPrefs.engineProfile.maxRpmScale).coerceAtLeast(4500f)
    val redlineRpm = activeProfile?.redlineRpm ?: rigPrefs.engineProfile.redlineRpm
    val rpm = telemetry.rpm ?: 0
    val rpmFrac by animateFloatAsState(
        targetValue = (rpm / maxRpm).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "hud_rpm_frac"
    )
    val resolvedSpeed = resolveRealSpeed(
        obdRawSpeedKmh = telemetry.speedKmh,
        gpsSpeedKph = gpsState.gpsSpeedKph,
        gpsAccuracyMeters = gpsState.accuracyMeters,
        rigPrefs = rigPrefs,
        activeProfile = activeProfile
    )
    val calibratedSpeed = resolvedSpeed.kph
    val speedUnitBadge = when (resolvedSpeed.source) {
        RealSpeedSource.GPS_LIVE -> strings.gpsSpeedBadge
        RealSpeedSource.OBD_CALIBRATED -> strings.calSpeedBadge
        RealSpeedSource.OBD_LIVE, RealSpeedSource.NONE -> strings.speedUnitKph
    }
    val pitch = sensorState.pitchDegrees - rigPrefs.pitchTareDeg
    val roll = sensorState.rollDegrees - rigPrefs.rollTareDeg
    val pitchAnim by animateFloatAsState(
        targetValue = pitch,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "hud_pitch"
    )
    val rollAnim by animateFloatAsState(
        targetValue = roll,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "hud_roll"
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
            .padding(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 900.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Link Status & Protocol Bar
            Surface(
                shape = CutCornerShape(10.dp),
                color = palette.surface.copy(alpha = 0.85f),
                border = BorderStroke(
                    1.dp,
                    if (connectionStatus == ConnectionStatus.CONNECTED) palette.zone2.copy(alpha = 0.55f)
                    else palette.zone4.copy(alpha = 0.55f)
                ),
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
                            text = connectedDevice?.name ?: statusMessage,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = detectedVehicle?.protocolName ?: activeProfile?.name ?: statusMessage,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = palette.zone1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            if (connectionStatus == ConnectionStatus.CONNECTED) onDisconnect() else onOpenBluetoothSheet()
                        },
                        shape = CutCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(
                            text = if (connectionStatus == ConnectionStatus.CONNECTED) {
                                stringResource(R.string.action_disconnect)
                            } else {
                                strings.connectShort
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Segmented Laser RPM Tape
            Surface(
                shape = CutCornerShape(14.dp),
                color = palette.podSurface.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, palette.bezelBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = strings.hudLaserRpm,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = palette.zone1
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (rpm >= redlineRpm) {
                                Text(
                                    text = strings.redlineBadge,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = palette.zone5
                                )
                            }
                            Text(
                                text = telemetry.rpm?.toString() ?: "----",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = if (rpm >= redlineRpm) palette.zone5 else palette.textPrimary
                            )
                            Text(
                                text = strings.rpmUnit,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = palette.textSecondary,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                        ) {
                            val segments = 26
                            val gap = 4.dp.toPx()
                            val segW = (size.width - gap * (segments - 1)) / segments
                            val activeSegments = (rpmFrac * segments).roundToInt()
                            for (i in 0 until segments) {
                                val frac = (i + 1) / segments.toFloat()
                                val segColor = when {
                                    frac < 0.45f -> palette.zone1
                                    frac < 0.72f -> palette.zone2
                                    frac < 0.88f -> palette.zone3
                                    else -> palette.zone5
                                }
                                val isLit = i < activeSegments
                                drawRect(
                                    color = if (isLit) segColor else palette.bezelBorder.copy(alpha = 0.45f),
                                    topLeft = Offset(i * (segW + gap), if (isLit) 0f else 4.dp.toPx()),
                                    size = androidx.compose.ui.geometry.Size(
                                        width = segW,
                                        height = if (isLit) size.height else size.height - 8.dp.toPx()
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Asymmetric Split: Optical Speed Reticle + Artificial Attitude Horizon
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val sideBySide = maxWidth >= 340.dp
                if (sideBySide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HudOpticalVelocityCard(
                            speedKph = calibratedSpeed,
                            speedUnitLabel = speedUnitBadge,
                            throttlePct = telemetry.throttlePercent,
                            engineLoadPct = telemetry.engineLoadPercent,
                            modifier = Modifier.weight(0.92f)
                        )
                        HudAttitudeHorizonCard(
                            pitch = pitchAnim,
                            roll = rollAnim,
                            azimuthDeg = sensorState.azimuthDegrees,
                            hasCompass = sensorState.hasCompassSensor,
                            onTare = onTareInclinometer,
                            modifier = Modifier.weight(1.08f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HudOpticalVelocityCard(
                            speedKph = calibratedSpeed,
                            speedUnitLabel = speedUnitBadge,
                            throttlePct = telemetry.throttlePercent,
                            engineLoadPct = telemetry.engineLoadPercent,
                            modifier = Modifier.fillMaxWidth()
                        )
                        HudAttitudeHorizonCard(
                            pitch = pitchAnim,
                            roll = rollAnim,
                            azimuthDeg = sensorState.azimuthDegrees,
                            hasCompass = sensorState.hasCompassSensor,
                            onTare = onTareInclinometer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 4-Cell Avionics Systems Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val coolantAlert = (telemetry.coolantCelsius ?: 0) >= rigPrefs.coolantWarnCelsius
                HudMetricTile(
                    label = strings.coolantTitle,
                    value = telemetry.coolantCelsius?.let { "${it}°C" } ?: "--°C",
                    subValue = telemetry.intakeAirCelsius?.let { "${strings.intakeAirLabel}: ${it}°C" } ?: "--",
                    accent = if (coolantAlert) palette.zone5 else palette.zone2,
                    modifier = Modifier.weight(1f)
                )
                val battAlert = (telemetry.batteryVolts ?: 14f) < rigPrefs.lowBatteryVolts
                HudMetricTile(
                    label = strings.batteryLabel,
                    value = telemetry.batteryVolts?.let { String.format(Locale.US, "%.1fV", it) } ?: "--.-V",
                    subValue = telemetry.shortTermFuelTrimB1?.let {
                        String.format(Locale.US, "%s: %+.1f%%", strings.fuelTrimLabel, it)
                    } ?: "--",
                    accent = if (battAlert) palette.zone5 else palette.zone1,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HudMetricTile(
                    label = strings.fuelTitle,
                    value = telemetry.fuelLevelPercent?.let { "${it.roundToInt()}%" } ?: "--%",
                    subValue = telemetry.boostPressureKpa?.let { "BOOST: ${it} kPa" }
                        ?: telemetry.manifoldKpa?.let { "${strings.miniMap}: $it kPa" }
                        ?: "--",
                    accent = palette.zone3,
                    modifier = Modifier.weight(1f)
                )
                HudMetricTile(
                    label = strings.hudNavFaults,
                    value = "${activeDtcs.size} / ${pendingDtcs.size}",
                    subValue = if (telemetry.milOn || activeDtcs.isNotEmpty()) strings.hudFlightAlert else strings.hudFlightNominal,
                    accent = if (telemetry.milOn || activeDtcs.isNotEmpty()) palette.zone5 else palette.zone2,
                    onClick = onJumpToFaults,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Mission Launchers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CutCornerShape(10.dp),
                    color = palette.zone1.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, palette.zone1.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(CutCornerShape(10.dp))
                        .clickable(onClick = onJumpToPids)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FactCheck,
                            contentDescription = null,
                            tint = palette.zone1,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.driveOpenDiagnostics,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = palette.textPrimary
                        )
                    }
                }

                Surface(
                    shape = CutCornerShape(10.dp),
                    color = palette.zone4.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, palette.zone4.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(CutCornerShape(10.dp))
                        .clickable(onClick = onJumpToFaults)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = palette.zone4,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.driveOpenScanner,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = palette.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudOpticalVelocityCard(
    speedKph: Int?,
    speedUnitLabel: String,
    throttlePct: Float?,
    engineLoadPct: Float?,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current

    Surface(
        shape = CutCornerShape(14.dp),
        color = palette.podSurface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, palette.zone1.copy(alpha = 0.5f)),
        modifier = modifier.height(210.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Corner targeting brackets
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pad = 10.dp.toPx()
                val arm = 16.dp.toPx()
                val path = Path().apply {
                    moveTo(pad, pad + arm)
                    lineTo(pad, pad)
                    lineTo(pad + arm, pad)

                    moveTo(size.width - pad - arm, pad)
                    lineTo(size.width - pad, pad)
                    lineTo(size.width - pad, pad + arm)

                    moveTo(pad, size.height - pad - arm)
                    lineTo(pad, size.height - pad)
                    lineTo(pad + arm, size.height - pad)

                    moveTo(size.width - pad - arm, size.height - pad)
                    lineTo(size.width - pad, size.height - pad)
                    lineTo(size.width - pad, size.height - pad - arm)
                }
                drawPath(path, color = palette.zone1, style = Stroke(width = 2.dp.toPx()))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.hudOpticalVel,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = palette.zone1
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = speedKph?.toString() ?: "--",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 52.sp,
                        lineHeight = 52.sp,
                        color = palette.textPrimary
                    )
                    Text(
                        text = speedUnitLabel,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = palette.zone1
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HudMiniRatioRow(
                        label = strings.miniThrottle,
                        valueText = throttlePct?.let { "${it.roundToInt()}%" } ?: "--%",
                        fraction = ((throttlePct ?: 0f) / 100f).coerceIn(0f, 1f),
                        barColor = palette.zone2
                    )
                    HudMiniRatioRow(
                        label = strings.engineLoadTitle,
                        valueText = engineLoadPct?.let { "${it.roundToInt()}%" } ?: "--%",
                        fraction = ((engineLoadPct ?: 0f) / 100f).coerceIn(0f, 1f),
                        barColor = palette.zone3
                    )
                }
            }
        }
    }
}

@Composable
private fun HudMiniRatioRow(
    label: String,
    valueText: String,
    fraction: Float,
    barColor: Color
) {
    val palette = LocalRacePalette.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = valueText,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = palette.textPrimary
            )
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(palette.bezelBorder.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(4.dp)
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
private fun HudAttitudeHorizonCard(
    pitch: Float,
    roll: Float,
    azimuthDeg: Float,
    hasCompass: Boolean,
    onTare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    val highAngle = abs(roll) >= 20f || abs(pitch) >= 20f
    val horizonColor = if (highAngle) palette.zone5 else palette.zone1

    Surface(
        shape = CutCornerShape(14.dp),
        color = palette.podSurface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, horizonColor.copy(alpha = 0.55f)),
        modifier = modifier.height(210.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.hudHorizonLabel,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = horizonColor
                )
                Text(
                    text = if (hasCompass) "${azimuthDeg.roundToInt()}°" else "---°",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = palette.zone3
                )
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radius = (minOf(size.width, size.height) * 0.44f)

                    // Outer reticle ring
                    drawCircle(
                        color = palette.bezelBorder,
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Rotating artificial horizon
                    val pitchPx = (pitch / 45f).coerceIn(-0.85f, 0.85f) * radius
                    rotate(degrees = -roll.coerceIn(-45f, 45f), pivot = Offset(cx, cy)) {
                        drawLine(
                            color = horizonColor,
                            start = Offset(cx - radius * 0.85f, cy + pitchPx),
                            end = Offset(cx + radius * 0.85f, cy + pitchPx),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // Upper and lower pitch ladder rungs
                        listOf(-0.45f, 0.45f).forEach { rung ->
                            val ry = cy + pitchPx + rung * radius
                            drawLine(
                                color = palette.textSecondary.copy(alpha = 0.65f),
                                start = Offset(cx - radius * 0.35f, ry),
                                end = Offset(cx + radius * 0.35f, ry),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }

                    // Fixed center boresight crosshair
                    drawLine(
                        color = palette.zone3,
                        start = Offset(cx - 10.dp.toPx(), cy),
                        end = Offset(cx - 3.dp.toPx(), cy),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = palette.zone3,
                        start = Offset(cx + 3.dp.toPx(), cy),
                        end = Offset(cx + 10.dp.toPx(), cy),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = palette.zone3,
                        radius = 2.5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.rollSuffix} ${roll.roundToInt()}° • ${strings.pitchSuffix} ${pitch.roundToInt()}°",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = palette.textPrimary
                )
                Surface(
                    shape = CutCornerShape(6.dp),
                    color = palette.zone1.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, palette.zone1.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(CutCornerShape(6.dp))
                        .clickable(onClick = onTare)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GpsFixed,
                            contentDescription = stringResource(R.string.action_zero_inclinometer),
                            tint = palette.zone1,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "0°",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = palette.zone1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudMetricTile(
    label: String,
    value: String,
    subValue: String,
    accent: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    Surface(
        shape = CutCornerShape(12.dp),
        color = palette.podSurface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.48f)),
        modifier = modifier
            .clip(CutCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = palette.textPrimary
            )
            Text(
                text = subValue,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
