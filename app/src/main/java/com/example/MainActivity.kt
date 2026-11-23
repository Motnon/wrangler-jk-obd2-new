package com.example

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.DirectionsCar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import com.example.obd.AppVisualTheme
import com.example.obd.ConnectionStatus
import com.example.sensors.PhoneBatteryState
import com.example.ui.components.BluetoothConnectSheet
import com.example.ui.screens.CockpitScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.RaceScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FaultCrimson
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OverlandAmber
import com.example.ui.theme.TrailSage
import com.example.ui.theme.WranglerObdTheme
import com.example.viewmodel.WranglerObdViewModel

const val ROUTE_COCKPIT = "cockpit"
const val ROUTE_RACE = "race"
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WranglerObdViewModel = viewModel()
            val rigPrefs by viewModel.rigPreferences.collectAsStateWithLifecycle()
            val isNightModeActive by viewModel.isNightModeActive.collectAsStateWithLifecycle()

            WranglerObdTheme(
                visualTheme = rigPrefs.visualTheme,
                isNightMode = isNightModeActive
            ) {
                WranglerObdApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WranglerObdApp(
    viewModel: WranglerObdViewModel = viewModel()
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
    val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
    val sensorState by viewModel.sensorState.collectAsStateWithLifecycle()
    val phoneBattery by viewModel.phoneBatteryState.collectAsStateWithLifecycle()
    val gpsState by viewModel.gpsState.collectAsStateWithLifecycle()
    val solarSchedule by viewModel.solarSchedule.collectAsStateWithLifecycle()
    val isNightModeActive by viewModel.isNightModeActive.collectAsStateWithLifecycle()
    val rigPrefs by viewModel.rigPreferences.collectAsStateWithLifecycle()
    val scanHistory by viewModel.scanHistory.collectAsStateWithLifecycle()
    val dtcSearchQuery by viewModel.dtcSearchQuery.collectAsStateWithLifecycle()
    val selectedSubsystem by viewModel.selectedSubsystem.collectAsStateWithLifecycle()
    val catalogCodes by viewModel.filteredCatalogCodes.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.uiFeedbackMessage.collectAsStateWithLifecycle()

    var showBluetoothSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    LaunchedEffect(isNightModeActive, rigPrefs.nightBrightnessPercent) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
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
                            text = rigPrefs.rigNickname.uppercase(),
                            style = MaterialTheme.typography.titleLarge
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
                                        connectionStatus == ConnectionStatus.CONNECTED -> TrailSage
                                        else -> OverlandAmber
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
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp,
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
                            onSetRaceLayoutLocked = viewModel::setRaceLayoutLocked
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
                            onTareInclinometer = viewModel::tareInclinometer
                        )
                    }
                    composable(ROUTE_SCANNER) {
                        ScannerScreen(
                            connectionStatus = connectionStatus,
                            telemetry = telemetry,
                            activeDtcs = activeDtcs,
                            pendingDtcs = pendingDtcs,
                            searchQuery = dtcSearchQuery,
                            selectedSubsystem = selectedSubsystem,
                            catalogCodes = catalogCodes,
                            onSearchQueryChange = viewModel::updateDtcSearchQuery,
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
                            onOpenBluetoothSheet = { showBluetoothSheet = true }
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
                            onSelectEngine = viewModel::setEngineProfile,
                            onSelectUnitSystem = viewModel::setUnitSystem,
                            onUpdateAlertThresholds = viewModel::setAlertThresholds,
                            onSelectVisualTheme = viewModel::setVisualTheme,
                            onSelectNightMode = viewModel::setNightModeSetting,
                            onNightBrightnessChange = viewModel::setNightBrightnessPercent,
                            onLocationPermissionResult = viewModel::onLocationPermissionResult,
                            onStartGpsCalibration = viewModel::startGpsCalibration,
                            onStopGpsCalibration = viewModel::stopGpsCalibration,
                            onApplyLiveGpsCalibration = viewModel::applyLiveGpsSpeedCalibration,
                            onApplyTireRatioCalibration = viewModel::applyTireRatioCalibration,
                            onSetSpeedCalibrationFactor = { viewModel.setSpeedCalibrationFactor(it) },
                            onResetInclinometerTare = viewModel::resetInclinometerTare,
                            onDeleteScanRecord = viewModel::deleteScanRecord
                        )
                    }
                }
            }
        }
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
            onDismiss = { showBluetoothSheet = false }
        )
    }
}

@Composable
private fun PhoneBatteryStatusIndicator(
    battery: PhoneBatteryState,
    isNightMode: Boolean,
    modifier: Modifier = Modifier
) {
    val isLow = battery.levelPercent <= 20 && !battery.isCharging
    val tintColor = when {
        isNightMode -> MaterialTheme.colorScheme.primary
        battery.isCharging || battery.levelPercent >= 50 -> TrailSage
        isLow -> FaultCrimson
        else -> CautionYellow
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
