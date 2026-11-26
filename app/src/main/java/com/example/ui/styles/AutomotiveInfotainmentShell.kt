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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.obd.RaceLayoutDefaults
import com.example.obd.RaceWindowGeometry
import com.example.obd.RaceWindowId
import com.example.obd.RealSpeedSource
import com.example.obd.RigPreferences
import com.example.obd.TerminalEntry
import com.example.obd.TrailSensorState
import com.example.obd.TripOdometerState
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
import com.example.ui.screens.RaceScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import java.util.Locale
import kotlin.math.roundToInt

private enum class DriveTab {
    HOME,
    RACE,
    PIDS,
    DTCS,
    TERM,
    SETUP
}

@Composable
fun AutomotiveInfotainmentShell(
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
    onSaveRaceLayout: (Map<RaceWindowId, RaceWindowGeometry>) -> Unit,
    onResetRaceLayout: () -> Unit,
    onSetRaceLayoutLocked: (Boolean) -> Unit,
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
    modifier: Modifier = Modifier,
    dtcLookupMap: Map<String, DtcCodeInfo> = emptyMap(),
    tripOdometer: TripOdometerState = TripOdometerState(),
    onResetTripOdometer: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    var selectedTab by rememberSaveable { mutableStateOf(DriveTab.HOME) }
    val isDayMode = !isNightModeActive && rigPrefs.visualTheme == AppVisualTheme.CLASSIC_WHITE

    LaunchedEffect(selectedTab) {
        onDiagnosticScreenVisibleChanged(selectedTab == DriveTab.PIDS)
    }

    BackHandler(enabled = selectedTab != DriveTab.HOME) {
        selectedTab = DriveTab.HOME
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(palette.boardBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Persistent CarPlay / Android Auto Start-Side Smart Dock
        Surface(
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            color = palette.surface,
            border = BorderStroke(1.dp, palette.bezelBorder),
            modifier = Modifier
                .width(74.dp)
                .fillMaxHeight()
                .padding(vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Bluetooth Status Orb + Battery %
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (connectionStatus == ConnectionStatus.CONNECTED) {
                            palette.zone2.copy(alpha = 0.2f)
                        } else {
                            palette.zone4.copy(alpha = 0.16f)
                        },
                        border = BorderStroke(
                            1.5.dp,
                            if (connectionStatus == ConnectionStatus.CONNECTED) palette.zone2 else palette.zone4
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenBluetoothSheet)
                            .testTag("topbar_bluetooth_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (connectionStatus == ConnectionStatus.CONNECTED) {
                                    Icons.Rounded.BluetoothConnected
                                } else {
                                    Icons.Rounded.Bluetooth
                                },
                                contentDescription = stringResource(R.string.cd_connect_bluetooth),
                                tint = if (connectionStatus == ConnectionStatus.CONNECTED) palette.zone2 else palette.zone4,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (phoneBattery.isCharging) {
                                Icons.Rounded.BatteryChargingFull
                            } else {
                                Icons.Rounded.BatteryStd
                            },
                            contentDescription = null,
                            tint = palette.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${phoneBattery.levelPercent}%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = palette.textSecondary
                        )
                    }
                }

                // Middle: Scrollable Oversized CarPlay Dock Tiles
                val dockItems = listOf(
                    Triple(DriveTab.HOME, strings.driveNavHome, Icons.Rounded.Home),
                    Triple(DriveTab.RACE, strings.driveNavRace, Icons.Rounded.SportsScore),
                    Triple(DriveTab.PIDS, strings.driveNavPids, Icons.Rounded.FactCheck),
                    Triple(DriveTab.DTCS, strings.driveNavFaults, Icons.Rounded.Build),
                    Triple(DriveTab.TERM, strings.driveNavTerm, Icons.Rounded.Terminal),
                    Triple(DriveTab.SETUP, strings.driveNavSetup, Icons.Rounded.DirectionsCar)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    dockItems.forEach { (tab, label, icon) ->
                        val selected = selectedTab == tab
                        val pillBg by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            label = "drive_dock_bg"
                        )
                        val iconTint = if (selected) MaterialTheme.colorScheme.onPrimary else palette.textSecondary

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = pillBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedTab = tab }
                                .testTag("drive_nav_${tab.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = label,
                                    fontFamily = InterFontFamily,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = iconTint,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Bottom: Quick Day/Night + GUI Style Picker Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.podSurface,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSetRaceDayMode(!isDayMode) }
                            .testTag("drive_day_dark_toggle")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isDayMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                contentDescription = stringResource(if (isDayMode) R.string.race_mode_dark else R.string.race_mode_day),
                                tint = palette.zone3,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.zone1.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, palette.zone1.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onOpenUiStyleSheet)
                            .testTag("topbar_ui_style_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DashboardCustomize,
                                contentDescription = strings.cdSwitchUiStyle,
                                tint = palette.zone1,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = strings.driveStyleShort,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = palette.zone1
                            )
                        }
                    }
                }
            }
        }

        // Main Infotainment Split-Deck Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(top = 6.dp, bottom = 6.dp, end = 6.dp, start = 6.dp)
        ) {
            when (selectedTab) {
                DriveTab.HOME -> DriveBentoHomeDeck(
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
                    onOpenRace = { selectedTab = DriveTab.RACE },
                    onOpenPids = { selectedTab = DriveTab.PIDS },
                    onOpenDtcs = { selectedTab = DriveTab.DTCS },
                    modifier = Modifier.fillMaxSize()
                )

                DriveTab.RACE -> RaceScreen(
                    connectionStatus = connectionStatus,
                    connectedDevice = connectedDevice,
                    statusMessage = statusMessage,
                    vinInfo = vinInfo,
                    telemetry = telemetry,
                    sensorState = sensorState,
                    rigPrefs = rigPrefs,
                    isDayMode = isDayMode,
                    onSetRaceDayMode = onSetRaceDayMode,
                    onOpenBluetoothSheet = onOpenBluetoothSheet,
                    onDisconnect = onDisconnect,
                    onTareInclinometer = onTareInclinometer,
                    onSaveRaceLayout = onSaveRaceLayout,
                    onResetRaceLayout = onResetRaceLayout,
                    onSetRaceLayoutLocked = onSetRaceLayoutLocked,
                    detectedVehicle = detectedVehicle,
                    activeProfile = activeProfile,
                    gpsState = gpsState,
                    tripOdometer = tripOdometer,
                    onResetTripOdometer = onResetTripOdometer
                )

                DriveTab.PIDS -> LiveDiagnosticsScreen(
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
                    detectedVehicle = detectedVehicle
                )

                DriveTab.DTCS -> ScannerScreen(
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
                    dtcLookupMap = dtcLookupMap
                )

                DriveTab.TERM -> TerminalScreen(
                    connectionStatus = connectionStatus,
                    terminalLogs = terminalLogs,
                    telemetry = telemetry,
                    rigPrefs = rigPrefs,
                    onSendCommand = onSendTerminalCommand,
                    onOpenBluetoothSheet = onOpenBluetoothSheet,
                    activeProfile = activeProfile,
                    supportedMode01Pids = supportedMode01Pids
                )

                DriveTab.SETUP -> GarageScreen(
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
                    onSelectUiStyle = onSelectUiStyle
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun DriveBentoHomeDeck(
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
    onOpenRace: () -> Unit,
    onOpenPids: () -> Unit,
    onOpenDtcs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    val hasAttention = telemetry.milOn || activeDtcs.isNotEmpty()
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
    val maxRpm = (activeProfile?.maxRpmScale ?: rigPrefs.engineProfile.maxRpmScale).coerceAtLeast(4500f)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Slim Head-Unit Status Strip
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = palette.surface,
            border = BorderStroke(1.dp, palette.bezelBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detectedVehicle?.displayTitle ?: rigPrefs.rigNickname,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append(connectedDevice?.name ?: statusMessage)
                            detectedVehicle?.vin?.let { append(" • $it") }
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (hasAttention) palette.zone5.copy(alpha = 0.16f) else palette.zone2.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, if (hasAttention) palette.zone5 else palette.zone2)
                ) {
                    Text(
                        text = if (hasAttention) strings.driveHealthAttention else strings.driveHealthNominal,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (hasAttention) palette.zone5 else palette.zone2,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Responsive Split-Deck Bento Body
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val wideSplit = maxWidth >= 520.dp
            if (wideSplit) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DriveHeroTelemetryBento(
                        speedKph = calibratedSpeed,
                        speedUnitLabel = speedUnitBadge,
                        rpm = telemetry.rpm,
                        maxRpm = maxRpm,
                        throttlePct = telemetry.throttlePercent,
                        loadPct = telemetry.engineLoadPercent,
                        connectionStatus = connectionStatus,
                        onOpenBluetoothSheet = onOpenBluetoothSheet,
                        onDisconnect = onDisconnect,
                        onOpenRace = onOpenRace,
                        modifier = Modifier.weight(1.05f)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DriveBentoSecondaryTiles(
                            telemetry = telemetry,
                            rigPrefs = rigPrefs,
                            sensorState = sensorState,
                            activeDtcCount = activeDtcs.size,
                            pendingDtcCount = pendingDtcs.size,
                            onTareInclinometer = onTareInclinometer,
                            onOpenPids = onOpenPids,
                            onOpenDtcs = onOpenDtcs
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DriveHeroTelemetryBento(
                        speedKph = calibratedSpeed,
                        speedUnitLabel = speedUnitBadge,
                        rpm = telemetry.rpm,
                        maxRpm = maxRpm,
                        throttlePct = telemetry.throttlePercent,
                        loadPct = telemetry.engineLoadPercent,
                        connectionStatus = connectionStatus,
                        onOpenBluetoothSheet = onOpenBluetoothSheet,
                        onDisconnect = onDisconnect,
                        onOpenRace = onOpenRace,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DriveBentoSecondaryTiles(
                        telemetry = telemetry,
                        rigPrefs = rigPrefs,
                        sensorState = sensorState,
                        activeDtcCount = activeDtcs.size,
                        pendingDtcCount = pendingDtcs.size,
                        onTareInclinometer = onTareInclinometer,
                        onOpenPids = onOpenPids,
                        onOpenDtcs = onOpenDtcs
                    )
                }
            }
        }
    }
}

@Composable
private fun DriveHeroTelemetryBento(
    speedKph: Int?,
    speedUnitLabel: String,
    rpm: Int?,
    maxRpm: Float,
    throttlePct: Float?,
    loadPct: Float?,
    connectionStatus: ConnectionStatus,
    onOpenBluetoothSheet: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenRace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    val rpmFrac by animateFloatAsState(
        targetValue = ((rpm ?: 0) / maxRpm).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "drive_rpm_frac"
    )
    val speedFrac by animateFloatAsState(
        targetValue = ((speedKph ?: 0) / 180f).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "drive_speed_frac"
    )

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = palette.podSurface,
        border = BorderStroke(1.2.dp, palette.zone1.copy(alpha = 0.38f)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.zone1.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dual-Arch Speed & RPM Bento Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeOuter = 13.dp.toPx()
                            val strokeInner = 8.dp.toPx()
                            val diameter = minOf(size.width, size.height) * 0.92f
                            val topLeftOuter = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f + 10.dp.toPx())
                            val inset = 18.dp.toPx()
                            val topLeftInner = Offset(topLeftOuter.x + inset, topLeftOuter.y + inset)
                            val innerDiam = (diameter - inset * 2f).coerceAtLeast(40f)

                            // Outer RPM Track + Sweep
                            drawArc(
                                color = palette.bezelBorder.copy(alpha = 0.5f),
                                startAngle = 145f,
                                sweepAngle = 250f,
                                useCenter = false,
                                topLeft = topLeftOuter,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = when {
                                    rpmFrac > 0.85f -> palette.zone5
                                    rpmFrac > 0.65f -> palette.zone3
                                    else -> palette.zone1
                                },
                                startAngle = 145f,
                                sweepAngle = 250f * rpmFrac,
                                useCenter = false,
                                topLeft = topLeftOuter,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                            )

                            // Inner Speed Track + Sweep
                            drawArc(
                                color = palette.bezelBorder.copy(alpha = 0.35f),
                                startAngle = 145f,
                                sweepAngle = 250f,
                                useCenter = false,
                                topLeft = topLeftInner,
                                size = Size(innerDiam, innerDiam),
                                style = Stroke(width = strokeInner, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = palette.zone2,
                                startAngle = 145f,
                                sweepAngle = 250f * speedFrac,
                                useCenter = false,
                                topLeft = topLeftInner,
                                size = Size(innerDiam, innerDiam),
                                style = Stroke(width = strokeInner, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = speedKph?.toString() ?: "--",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 46.sp,
                            lineHeight = 46.sp,
                            color = palette.textPrimary
                        )
                        Text(
                            text = speedUnitLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = palette.zone2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${rpm ?: "----"} ${strings.rpmUnit}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = palette.zone1
                        )
                    }
                }

                // Throttle & Engine Load Pill Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = strings.miniThrottle,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = palette.textSecondary
                            )
                            Text(
                                text = throttlePct?.let { "${it.roundToInt()}%" } ?: "--%",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = palette.zone2
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = strings.engineLoadTitle,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = palette.textSecondary
                            )
                            Text(
                                text = loadPct?.let { "${it.roundToInt()}%" } ?: "--%",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = palette.zone3
                            )
                        }
                    }
                }

                // Big Thumb-Friendly Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenRace,
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SportsScore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.driveOpenRaceCluster,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (connectionStatus == ConnectionStatus.CONNECTED) onDisconnect() else onOpenBluetoothSheet()
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(
                            text = if (connectionStatus == ConnectionStatus.CONNECTED) {
                                stringResource(R.string.action_disconnect)
                            } else {
                                strings.connectShort
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveBentoSecondaryTiles(
    telemetry: LiveTelemetry,
    rigPrefs: RigPreferences,
    sensorState: TrailSensorState,
    activeDtcCount: Int,
    pendingDtcCount: Int,
    onTareInclinometer: () -> Unit,
    onOpenPids: () -> Unit,
    onOpenDtcs: () -> Unit
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    val coolantHot = (telemetry.coolantCelsius ?: 0) >= rigPrefs.coolantWarnCelsius
    val lowBatt = (telemetry.batteryVolts ?: 14f) < rigPrefs.lowBatteryVolts
    val pitch = (sensorState.pitchDegrees - rigPrefs.pitchTareDeg).roundToInt()
    val roll = (sensorState.rollDegrees - rigPrefs.rollTareDeg).roundToInt()

    // Row 1: Thermals & Power/Fuel Bento Cards
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = palette.podSurface,
            border = BorderStroke(1.dp, if (coolantHot) palette.zone5 else palette.bezelBorder),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Thermostat,
                        contentDescription = null,
                        tint = if (coolantHot) palette.zone5 else palette.zone4,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.driveThermalsTitle,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = palette.textSecondary
                    )
                }
                Text(
                    text = telemetry.coolantCelsius?.let { "${it}°C" } ?: "--°C",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = if (coolantHot) palette.zone5 else palette.textPrimary
                )
                Text(
                    text = telemetry.intakeAirCelsius?.let { "${strings.intakeAirLabel}: ${it}°C" } ?: "--",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = palette.podSurface,
            border = BorderStroke(1.dp, if (lowBatt) palette.zone5 else palette.bezelBorder),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalGasStation,
                        contentDescription = null,
                        tint = palette.zone3,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.driveElectricalTitle,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = palette.textSecondary
                    )
                }
                Text(
                    text = telemetry.batteryVolts?.let { String.format(Locale.US, "%.1fV", it) } ?: "--.-V",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = if (lowBatt) palette.zone5 else palette.textPrimary
                )
                Text(
                    text = telemetry.fuelLevelPercent?.let { "${strings.fuelTitle}: ${it.roundToInt()}%" }
                        ?: "${strings.fuelTitle}: --%",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = palette.zone3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Row 2: Live Trail Tilt Bento Card
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = palette.podSurface,
        border = BorderStroke(1.dp, palette.bezelBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = strings.driveInclinometerTitle,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = palette.zone1
                )
                Text(
                    text = "${strings.rollSuffix} ${roll}°  •  ${strings.pitchSuffix} ${pitch}°",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = palette.textPrimary
                )
            }

            OutlinedButton(
                onClick = onTareInclinometer,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Rounded.GpsFixed,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.action_zero_inclinometer), fontSize = 11.sp)
            }
        }
    }

    // Row 3: Quick Diagnostic & Fault Bento Launchers
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = palette.zone1.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, palette.zone1.copy(alpha = 0.5f)),
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onOpenPids)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FactCheck,
                    contentDescription = null,
                    tint = palette.zone1,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = strings.driveOpenDiagnostics,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        val faultAccent = if (activeDtcCount > 0 || telemetry.milOn) palette.zone5 else palette.zone2
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = faultAccent.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, faultAccent.copy(alpha = 0.5f)),
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onOpenDtcs)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Build,
                        contentDescription = null,
                        tint = faultAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "$activeDtcCount / $pendingDtcCount",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = faultAccent
                    )
                }
                Text(
                    text = strings.driveOpenScanner,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
