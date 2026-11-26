package com.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.obd.AppLanguage
import com.example.obd.AppUiStyle
import com.example.obd.AppVisualTheme
import com.example.obd.ConnectionStatus
import com.example.sensors.PhoneBatteryState
import com.example.ui.components.BluetoothConnectSheet
import com.example.ui.components.UiStylePickerSheet
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.screens.CockpitScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.LiveDiagnosticsScreen
import com.example.ui.screens.RaceScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.styles.AutomotiveInfotainmentShell
import com.example.ui.styles.AvionicsHudShell
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import com.example.ui.theme.WranglerObdTheme
import com.example.viewmodel.WranglerObdViewModel
import java.util.Locale

const val ROUTE_COCKPIT = "cockpit"
const val ROUTE_RACE = "race"
const val ROUTE_DIAGNOSTICS = "diagnostics"
const val ROUTE_SCANNER = "scanner"
const val ROUTE_TERMINAL = "terminal"
const val ROUTE_GARAGE = "garage"

private data class NavDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private class LocalizedContextWrapper(
    base: Context,
    private val localizedResources: Resources
) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            val viewModel: WranglerObdViewModel = viewModel()
            val rigPrefs by viewModel.rigPreferences.collectAsStateWithLifecycle()
            val isNightModeActive by viewModel.isNightModeActive.collectAsStateWithLifecycle()

            val baseContext = LocalContext.current
            val baseConfig = LocalConfiguration.current
            val appLanguage = rigPrefs.appLanguage

            val localizedEnv = remember(appLanguage, baseConfig) {
                val locale = Locale(appLanguage.code)
                Locale.setDefault(locale)
                val newConfig = Configuration(baseConfig).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
                val configCtx = baseContext.createConfigurationContext(newConfig)
                val wrappedCtx = LocalizedContextWrapper(baseContext, configCtx.resources)
                val layoutDir = if (appLanguage == AppLanguage.HEBREW) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }
                Triple(newConfig, wrappedCtx, layoutDir)
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedEnv.first,
                LocalContext provides localizedEnv.second,
                LocalLayoutDirection provides localizedEnv.third,
                LocalAppStrings provides AppStrings.forLanguage(appLanguage)
            ) {
                WranglerObdTheme(
                    visualTheme = rigPrefs.visualTheme,
                    isNightMode = isNightModeActive,
                    uiStyle = rigPrefs.uiStyle
                ) {
                    WranglerObdApp(
                        viewModel = viewModel,
                        hostActivity = this@MainActivity
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WranglerObdApp(
    viewModel: WranglerObdViewModel = viewModel(),
    hostActivity: Activity? = null
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_RACE

    val bluetoothPermissionsGranted by viewModel.bluetoothPermissionsGranted.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val availableDevices by viewModel.availableDevices.collectAsStateWithLifecycle()
    val isScanningBluetooth by viewModel.isScanningBluetooth.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val activeDtcs by viewModel.activeDtcs.collectAsStateWithLifecycle()
    val pendingDtcs by viewModel.pendingDtcs.collectAsStateWithLifecycle()
    val vinInfo by viewModel.vinInfo.collectAsStateWithLifecycle()
    val detectedVehicle by viewModel.detectedVehicle.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val supportedMode01Pids by viewModel.supportedMode01Pids.collectAsStateWithLifecycle()
    val cachedBindings by viewModel.cachedBindings.collectAsStateWithLifecycle()
    val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
    val sensorState by viewModel.sensorState.collectAsStateWithLifecycle()
    val phoneBattery by viewModel.phoneBatteryState.collectAsStateWithLifecycle()
    val gpsState by viewModel.gpsState.collectAsStateWithLifecycle()
    val solarSchedule by viewModel.solarSchedule.collectAsStateWithLifecycle()
    val isNightModeActive by viewModel.isNightModeActive.collectAsStateWithLifecycle()
    val rigPrefs by viewModel.rigPreferences.collectAsStateWithLifecycle()
    val scanHistory by viewModel.scanHistory.collectAsStateWithLifecycle()
    val dtcSearchQuery by viewModel.dtcSearchQuery.collectAsStateWithLifecycle()
    val selectedDtcModelFilter by viewModel.selectedDtcModelFilter.collectAsStateWithLifecycle()
    val selectedSubsystem by viewModel.selectedSubsystem.collectAsStateWithLifecycle()
    val catalogCodes by viewModel.filteredCatalogCodes.collectAsStateWithLifecycle()
    val appliedDiagnosticTests by viewModel.appliedDiagnosticTests.collectAsStateWithLifecycle()
    val diagnosticReadings by viewModel.diagnosticReadings.collectAsStateWithLifecycle()
    val isDiagnosticScanPaused by viewModel.isDiagnosticScanPaused.collectAsStateWithLifecycle()
    val diagnosticCycleCount by viewModel.diagnosticCycleCount.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.uiFeedbackMessage.collectAsStateWithLifecycle()

    var showBluetoothSheet by remember { mutableStateOf(false) }
    var showUiStyleSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val strings = LocalAppStrings.current

    val context = LocalContext.current
    LaunchedEffect(isNightModeActive, rigPrefs.nightBrightnessPercent) {
        val window = (hostActivity ?: (context as? Activity))?.window ?: return@LaunchedEffect
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val params = window.attributes
        params.screenBrightness = if (isNightModeActive) {
            (rigPrefs.nightBrightnessPercent / 100f).coerceIn(0.05f, 0.70f)
        } else {
            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
        window.attributes = params
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        viewModel.onBluetoothPermissionsResult(results)
    }

    LaunchedEffect(Unit) {
        if (!viewModel.syncBluetoothPermissions()) {
            bluetoothPermissionLauncher.launch(viewModel.requiredBluetoothPermissions())
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.startSensors()
                    viewModel.syncBluetoothPermissions()
                }
                Lifecycle.Event.ON_PAUSE -> viewModel.stopSensors()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopSensors()
        }
    }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeFeedbackMessage()
        }
    }

    val destinations = remember {
        listOf(
            NavDestination(
                route = ROUTE_RACE,
                labelRes = R.string.nav_race,
                selectedIcon = Icons.Rounded.SportsScore,
                unselectedIcon = Icons.Outlined.SportsScore,
                testTag = "nav_tab_race"
            ),
            NavDestination(
                route = ROUTE_DIAGNOSTICS,
                labelRes = R.string.nav_diagnostics,
                selectedIcon = Icons.Rounded.FactCheck,
                unselectedIcon = Icons.Outlined.FactCheck,
                testTag = "nav_tab_diagnostics"
            ),
            NavDestination(
                route = ROUTE_COCKPIT,
                labelRes = R.string.nav_cockpit,
                selectedIcon = Icons.Rounded.Speed,
                unselectedIcon = Icons.Outlined.Speed,
                testTag = "nav_tab_cockpit"
            ),
            NavDestination(
                route = ROUTE_SCANNER,
                labelRes = R.string.nav_scanner,
                selectedIcon = Icons.Rounded.Build,
                unselectedIcon = Icons.Outlined.Build,
                testTag = "nav_tab_scanner"
            ),
            NavDestination(
                route = ROUTE_TERMINAL,
                labelRes = R.string.nav_terminal,
                selectedIcon = Icons.Rounded.Terminal,
                unselectedIcon = Icons.Outlined.Terminal,
                testTag = "nav_tab_terminal"
            ),
            NavDestination(
                route = ROUTE_GARAGE,
                labelRes = R.string.nav_garage,
                selectedIcon = Icons.Rounded.DirectionsCar,
                unselectedIcon = Icons.Outlined.DirectionsCar,
                testTag = "nav_tab_garage"
            )
        )
    }

    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    when (rigPrefs.uiStyle) {
        AppUiStyle.AVIONICS_HUD -> {
            AvionicsHudShell(
                connectionStatus = connectionStatus,
                connectedDevice = connectedDevice,
                statusMessage = statusMessage,
                vinInfo = vinInfo,
                detectedVehicle = detectedVehicle,
                activeProfile = activeProfile,
                supportedMode01Pids = supportedMode01Pids,
                cachedBindings = cachedBindings,
                telemetry = telemetry,
                activeDtcs = activeDtcs,
                pendingDtcs = pendingDtcs,
                terminalLogs = terminalLogs,
                sensorState = sensorState,
                phoneBattery = phoneBattery,
                gpsState = gpsState,
                solarSchedule = solarSchedule,
                isNightModeActive = isNightModeActive,
                rigPrefs = rigPrefs,
                scanHistory = scanHistory,
                dtcSearchQuery = dtcSearchQuery,
                selectedDtcModelFilter = selectedDtcModelFilter,
                selectedSubsystem = selectedSubsystem,
                catalogCodes = catalogCodes,
                appliedDiagnosticTests = appliedDiagnosticTests,
                diagnosticReadings = diagnosticReadings,
                isDiagnosticScanPaused = isDiagnosticScanPaused,
                diagnosticCycleCount = diagnosticCycleCount,
                hasFineLocationPermission = viewModel.hasFineLocationPermission(),
                snackbarHostState = snackbarHostState,
                onOpenBluetoothSheet = { showBluetoothSheet = true },
                onOpenUiStyleSheet = { showUiStyleSheet = true },
                onDisconnect = viewModel::disconnectAdapter,
                onTareInclinometer = viewModel::tareInclinometer,
                onSetRaceDayMode = viewModel::setRaceDayMode,
                onDiagnosticScreenVisibleChanged = viewModel::setDiagnosticScreenVisible,
                onApplyDiagnosticTests = viewModel::applyDiagnosticTests,
                onToggleDiagnosticPause = viewModel::toggleDiagnosticScanPaused,
                onReprobeUnsupportedDiagnostics = viewModel::reprobeUnsupportedDiagnostics,
                onDtcSearchQueryChange = viewModel::updateDtcSearchQuery,
                onSelectDtcModelFilter = viewModel::selectDtcModelFilter,
                onSelectSubsystem = viewModel::selectSubsystem,
                onScanEcu = viewModel::readDtcsFromEcu,
                onClearEcuCodes = viewModel::clearDtcsOnEcu,
                onSaveSnapshot = viewModel::saveScanSnapshot,
                onSendTerminalCommand = viewModel::sendTerminalCommand,
                onUpdateRigDetails = viewModel::updateRigDetails,
                onSelectWranglerModel = viewModel::setWranglerModel,
                onSelectEngine = viewModel::setEngineProfile,
                onSelectUnitSystem = viewModel::setUnitSystem,
                onUpdateAlertThresholds = viewModel::setAlertThresholds,
                onSelectVisualTheme = viewModel::setVisualTheme,
                onSelectNightMode = viewModel::setNightModeSetting,
                onNightBrightnessChange = viewModel::setNightBrightnessPercent,
                onSelectAppLanguage = viewModel::setAppLanguage,
                onSelectUiStyle = viewModel::setUiStyle,
                onLocationPermissionResult = viewModel::onLocationPermissionResult,
                onStartGpsCalibration = viewModel::startGpsCalibration,
                onStopGpsCalibration = viewModel::stopGpsCalibration,
                onApplyLiveGpsCalibration = viewModel::applyLiveGpsSpeedCalibration,
                onApplyTireRatioCalibration = viewModel::applyTireRatioCalibration,
                onSetSpeedCalibrationFactor = { viewModel.setSpeedCalibrationFactor(it) },
                onResetInclinometerTare = viewModel::resetInclinometerTare,
                onDeleteScanRecord = viewModel::deleteScanRecord,
                onSelectProfileOverride = viewModel::setManualProfileOverride,
                onForgetCachedAdapter = viewModel::forgetCachedAdapter
            )
        }

        AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> {
            AutomotiveInfotainmentShell(
                connectionStatus = connectionStatus,
                connectedDevice = connectedDevice,
                statusMessage = statusMessage,
                vinInfo = vinInfo,
                detectedVehicle = detectedVehicle,
                activeProfile = activeProfile,
                supportedMode01Pids = supportedMode01Pids,
                cachedBindings = cachedBindings,
                telemetry = telemetry,
                activeDtcs = activeDtcs,
                pendingDtcs = pendingDtcs,
                terminalLogs = terminalLogs,
                sensorState = sensorState,
                phoneBattery = phoneBattery,
                gpsState = gpsState,
                solarSchedule = solarSchedule,
                isNightModeActive = isNightModeActive,
                rigPrefs = rigPrefs,
                scanHistory = scanHistory,
                dtcSearchQuery = dtcSearchQuery,
                selectedDtcModelFilter = selectedDtcModelFilter,
                selectedSubsystem = selectedSubsystem,
                catalogCodes = catalogCodes,
                appliedDiagnosticTests = appliedDiagnosticTests,
                diagnosticReadings = diagnosticReadings,
                isDiagnosticScanPaused = isDiagnosticScanPaused,
                diagnosticCycleCount = diagnosticCycleCount,
                hasFineLocationPermission = viewModel.hasFineLocationPermission(),
                snackbarHostState = snackbarHostState,
                onOpenBluetoothSheet = { showBluetoothSheet = true },
                onOpenUiStyleSheet = { showUiStyleSheet = true },
                onDisconnect = viewModel::disconnectAdapter,
                onTareInclinometer = viewModel::tareInclinometer,
                onSetRaceDayMode = viewModel::setRaceDayMode,
                onSaveRaceLayout = viewModel::saveRaceLayout,
                onResetRaceLayout = viewModel::resetRaceLayout,
                onSetRaceLayoutLocked = viewModel::setRaceLayoutLocked,
                onDiagnosticScreenVisibleChanged = viewModel::setDiagnosticScreenVisible,
                onApplyDiagnosticTests = viewModel::applyDiagnosticTests,
                onToggleDiagnosticPause = viewModel::toggleDiagnosticScanPaused,
                onReprobeUnsupportedDiagnostics = viewModel::reprobeUnsupportedDiagnostics,
                onDtcSearchQueryChange = viewModel::updateDtcSearchQuery,
                onSelectDtcModelFilter = viewModel::selectDtcModelFilter,
                onSelectSubsystem = viewModel::selectSubsystem,
                onScanEcu = viewModel::readDtcsFromEcu,
                onClearEcuCodes = viewModel::clearDtcsOnEcu,
                onSaveSnapshot = viewModel::saveScanSnapshot,
                onSendTerminalCommand = viewModel::sendTerminalCommand,
                onUpdateRigDetails = viewModel::updateRigDetails,
                onSelectWranglerModel = viewModel::setWranglerModel,
                onSelectEngine = viewModel::setEngineProfile,
                onSelectUnitSystem = viewModel::setUnitSystem,
                onUpdateAlertThresholds = viewModel::setAlertThresholds,
                onSelectVisualTheme = viewModel::setVisualTheme,
                onSelectNightMode = viewModel::setNightModeSetting,
                onNightBrightnessChange = viewModel::setNightBrightnessPercent,
                onSelectAppLanguage = viewModel::setAppLanguage,
                onSelectUiStyle = viewModel::setUiStyle,
                onLocationPermissionResult = viewModel::onLocationPermissionResult,
                onStartGpsCalibration = viewModel::startGpsCalibration,
                onStopGpsCalibration = viewModel::stopGpsCalibration,
                onApplyLiveGpsCalibration = viewModel::applyLiveGpsSpeedCalibration,
                onApplyTireRatioCalibration = viewModel::applyTireRatioCalibration,
                onSetSpeedCalibrationFactor = { viewModel.setSpeedCalibrationFactor(it) },
                onResetInclinometerTare = viewModel::resetInclinometerTare,
                onDeleteScanRecord = viewModel::deleteScanRecord,
                onSelectProfileOverride = viewModel::setManualProfileOverride,
                onForgetCachedAdapter = viewModel::forgetCachedAdapter
            )
        }

        AppUiStyle.TACTICAL_MOTORSPORT -> {
            LaunchedEffect(currentRoute) {
                viewModel.setDiagnosticScreenVisible(currentRoute == ROUTE_DIAGNOSTICS)
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 680.dp

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = (detectedVehicle?.displayTitle ?: rigPrefs.rigNickname).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    PhoneBatteryStatusIndicator(
                                        battery = phoneBattery,
                                        isNightMode = isNightModeActive,
                                        modifier = Modifier.testTag("topbar_phone_battery")
                                    )

                                    IconButton(
                                        onClick = { showUiStyleSheet = true },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("topbar_ui_style_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DashboardCustomize,
                                            contentDescription = strings.cdSwitchUiStyle,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { showBluetoothSheet = true },
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
                                            tint = when {
                                                isNightModeActive -> MaterialTheme.colorScheme.primary
                                                connectionStatus == ConnectionStatus.CONNECTED -> LocalRacePalette.current.zone2
                                                else -> LocalRacePalette.current.zone4
                                            },
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(text = label) },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag(dest.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_RACE,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_RACE) {
                        RaceScreen(
                            connectionStatus = connectionStatus,
                            connectedDevice = connectedDevice,
                            statusMessage = statusMessage,
                            vinInfo = vinInfo,
                            telemetry = telemetry,
                            sensorState = sensorState,
                            rigPrefs = rigPrefs,
                            isDayMode = !isNightModeActive && rigPrefs.visualTheme == AppVisualTheme.CLASSIC_WHITE,
                            onSetRaceDayMode = viewModel::setRaceDayMode,
                            onOpenBluetoothSheet = { showBluetoothSheet = true },
                            onDisconnect = viewModel::disconnectAdapter,
                            onTareInclinometer = viewModel::tareInclinometer,
                            onSaveRaceLayout = viewModel::saveRaceLayout,
                            onResetRaceLayout = viewModel::resetRaceLayout,
                            onSetRaceLayoutLocked = viewModel::setRaceLayoutLocked,
                            detectedVehicle = detectedVehicle,
                            activeProfile = activeProfile,
                            gpsState = gpsState
                        )
                    }
                    composable(ROUTE_DIAGNOSTICS) {
                        LiveDiagnosticsScreen(
                            connectionStatus = connectionStatus,
                            connectedDevice = connectedDevice,
                            statusMessage = statusMessage,
                            vinInfo = vinInfo,
                            telemetry = telemetry,
                            appliedTests = appliedDiagnosticTests,
                            readings = diagnosticReadings,
                            isScanPaused = isDiagnosticScanPaused,
                            cycleCount = diagnosticCycleCount,
                            onApplyTests = viewModel::applyDiagnosticTests,
                            onTogglePause = viewModel::toggleDiagnosticScanPaused,
                            onReprobeUnsupported = viewModel::reprobeUnsupportedDiagnostics,
                            onOpenBluetoothSheet = { showBluetoothSheet = true },
                            onDisconnect = viewModel::disconnectAdapter,
                            detectedVehicle = detectedVehicle
                        )
                    }
                    composable(ROUTE_COCKPIT) {
                        CockpitScreen(
                            connectionStatus = connectionStatus,
                            connectedDevice = connectedDevice,
                            statusMessage = statusMessage,
                            vinInfo = vinInfo,
                            telemetry = telemetry,
                            sensorState = sensorState,
                            rigPrefs = rigPrefs,
                            onOpenBluetoothSheet = { showBluetoothSheet = true },
                            onDisconnect = viewModel::disconnectAdapter,
                            onTareInclinometer = viewModel::tareInclinometer,
                            detectedVehicle = detectedVehicle,
                            activeProfile = activeProfile,
                            gpsState = gpsState
                        )
                    }
                    composable(ROUTE_SCANNER) {
                        ScannerScreen(
                            connectionStatus = connectionStatus,
                            telemetry = telemetry,
                            activeDtcs = activeDtcs,
                            pendingDtcs = pendingDtcs,
                            searchQuery = dtcSearchQuery,
                            wranglerModel = rigPrefs.wranglerModel,
                            selectedModelFilter = selectedDtcModelFilter,
                            selectedSubsystem = selectedSubsystem,
                            catalogCodes = catalogCodes,
                            onSearchQueryChange = viewModel::updateDtcSearchQuery,
                            onSelectModelFilter = viewModel::selectDtcModelFilter,
                            onSelectSubsystem = viewModel::selectSubsystem,
                            onScanEcu = viewModel::readDtcsFromEcu,
                            onClearEcuCodes = viewModel::clearDtcsOnEcu,
                            onSaveSnapshot = viewModel::saveScanSnapshot,
                            onOpenBluetoothSheet = { showBluetoothSheet = true }
                        )
                    }
                    composable(ROUTE_TERMINAL) {
                        TerminalScreen(
                            connectionStatus = connectionStatus,
                            terminalLogs = terminalLogs,
                            telemetry = telemetry,
                            rigPrefs = rigPrefs,
                            onSendCommand = viewModel::sendTerminalCommand,
                            onOpenBluetoothSheet = { showBluetoothSheet = true },
                            activeProfile = activeProfile,
                            supportedMode01Pids = supportedMode01Pids
                        )
                    }
                    composable(ROUTE_GARAGE) {
                        GarageScreen(
                            rigPrefs = rigPrefs,
                            vinInfo = vinInfo,
                            telemetry = telemetry,
                            gpsState = gpsState,
                            solarSchedule = solarSchedule,
                            isNightModeActive = isNightModeActive,
                            hasFineLocationPermission = viewModel.hasFineLocationPermission(),
                            scanHistory = scanHistory,
                            onUpdateRigDetails = viewModel::updateRigDetails,
                            onSelectWranglerModel = viewModel::setWranglerModel,
                            onSelectEngine = viewModel::setEngineProfile,
                            onSelectUnitSystem = viewModel::setUnitSystem,
                            onUpdateAlertThresholds = viewModel::setAlertThresholds,
                            onSelectVisualTheme = viewModel::setVisualTheme,
                            onSelectNightMode = viewModel::setNightModeSetting,
                            onNightBrightnessChange = viewModel::setNightBrightnessPercent,
                            onSelectAppLanguage = viewModel::setAppLanguage,
                            onLocationPermissionResult = viewModel::onLocationPermissionResult,
                            onStartGpsCalibration = viewModel::startGpsCalibration,
                            onStopGpsCalibration = viewModel::stopGpsCalibration,
                            onApplyLiveGpsCalibration = viewModel::applyLiveGpsSpeedCalibration,
                            onApplyTireRatioCalibration = viewModel::applyTireRatioCalibration,
                            onSetSpeedCalibrationFactor = { viewModel.setSpeedCalibrationFactor(it) },
                            onResetInclinometerTare = viewModel::resetInclinometerTare,
                            onDeleteScanRecord = viewModel::deleteScanRecord,
                            detectedVehicle = detectedVehicle,
                            activeProfile = activeProfile,
                            supportedMode01Pids = supportedMode01Pids,
                            cachedBindings = cachedBindings,
                            onSelectProfileOverride = viewModel::setManualProfileOverride,
                            onForgetCachedAdapter = viewModel::forgetCachedAdapter,
                            onSelectUiStyle = viewModel::setUiStyle
                        )
                    }
                }
            }
        }
            }
        }
    }

    if (showUiStyleSheet) {
        UiStylePickerSheet(
            selectedStyle = rigPrefs.uiStyle,
            onSelectStyle = viewModel::setUiStyle,
            onDismiss = { showUiStyleSheet = false }
        )
    }

    if (showBluetoothSheet) {
        BluetoothConnectSheet(
            devices = availableDevices,
            isScanning = isScanningBluetooth,
            hasPermissions = bluetoothPermissionsGranted,
            isBluetoothEnabled = viewModel.isBluetoothEnabled(),
            requiredPermissions = viewModel.requiredBluetoothPermissions(),
            onPermissionsResult = viewModel::onBluetoothPermissionsResult,
            onRefreshPaired = viewModel::refreshPairedDevices,
            onStartScan = viewModel::startBluetoothDiscovery,
            onStopScan = viewModel::stopBluetoothDiscovery,
            onSelectDevice = viewModel::connectAdapter,
            onDismiss = { showBluetoothSheet = false },
            cachedBindings = cachedBindings
        )
    }
}

@Composable
private fun PhoneBatteryStatusIndicator(
    battery: PhoneBatteryState,
    isNightMode: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalRacePalette.current
    val isLow = battery.levelPercent <= 20 && !battery.isCharging
    val tintColor = when {
        isNightMode -> MaterialTheme.colorScheme.primary
        battery.isCharging || battery.levelPercent >= 50 -> palette.zone2
        isLow -> palette.zone5
        else -> palette.zone3
    }
    val icon = when {
        battery.isCharging -> Icons.Rounded.BatteryChargingFull
        isLow -> Icons.Rounded.BatteryAlert
        battery.levelPercent >= 85 -> Icons.Rounded.BatteryFull
        else -> Icons.Rounded.BatteryStd
    }
    val cdText = if (battery.isCharging) {
        "Phone battery ${battery.levelPercent} percent, charging"
    } else {
        "Phone battery ${battery.levelPercent} percent"
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, tintColor.copy(alpha = 0.45f)),
        modifier = modifier.semantics { contentDescription = cdText }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "${battery.levelPercent}%",
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = tintColor
            )
        }
    }
}
