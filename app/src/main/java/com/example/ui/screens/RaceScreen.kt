package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.obd.BluetoothDeviceItem
import com.example.obd.ConnectionStatus
import com.example.obd.DetectedVehicle
import com.example.obd.JkEngineProfile
import com.example.obd.LiveTelemetry
import androidx.compose.runtime.key
import com.example.obd.RaceLayoutDefaults
import com.example.obd.RaceLayoutSolver
import com.example.obd.RaceWindowGeometry
import com.example.obd.RaceWindowId
import com.example.obd.RealSpeedSource
import com.example.obd.RigPreferences
import com.example.obd.TrailSensorState
import com.example.obd.UnitSystem
import com.example.obd.VehicleProfile
import com.example.obd.VehicleProfileId
import com.example.obd.VinInfo
import com.example.obd.resolveRealSpeed
import com.example.sensors.GpsTelemetryState
import com.example.ui.components.AdjustableRaceWindow
import com.example.ui.components.EcuStatusBanner
import com.example.ui.components.RaceCircularArcPod
import com.example.ui.components.RaceSegmentedBarGauge
import com.example.ui.components.RaceSessionPeaksCard
import com.example.ui.components.RaceShiftLightBar
import com.example.ui.components.RaceSpeedometerDial
import com.example.ui.components.RaceThrottleBarCard
import com.example.ui.components.TrailInclinometerCard
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun RaceScreen(
    connectionStatus: ConnectionStatus,
    connectedDevice: BluetoothDeviceItem?,
    statusMessage: String,
    vinInfo: VinInfo?,
    telemetry: LiveTelemetry,
    sensorState: TrailSensorState,
    rigPrefs: RigPreferences,
    isDayMode: Boolean,
    onSetRaceDayMode: (Boolean) -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onDisconnect: () -> Unit,
    onTareInclinometer: () -> Unit,
    onSaveRaceLayout: (Map<RaceWindowId, RaceWindowGeometry>) -> Unit,
    onResetRaceLayout: () -> Unit,
    onSetRaceLayoutLocked: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    detectedVehicle: DetectedVehicle? = null,
    activeProfile: VehicleProfile? = null,
    gpsState: GpsTelemetryState = GpsTelemetryState()
) {
    val palette = LocalRacePalette.current
    val strings = LocalAppStrings.current
    val isImperialMap = rigPrefs.unitSystem == UnitSystem.IMPERIAL

    val resolvedSpeed = resolveRealSpeed(
        obdRawSpeedKmh = telemetry.speedKmh,
        gpsSpeedKph = gpsState.gpsSpeedKph,
        gpsAccuracyMeters = gpsState.accuracyMeters,
        rigPrefs = rigPrefs,
        activeProfile = activeProfile
    )
    val calibratedSpeedKph = resolvedSpeed.kph
    val speedBadgeText = when (resolvedSpeed.source) {
        RealSpeedSource.GPS_LIVE -> strings.gpsSpeedBadge
        RealSpeedSource.OBD_CALIBRATED -> strings.calSpeedBadge
        RealSpeedSource.OBD_LIVE -> strings.liveSpeedFormat
        RealSpeedSource.NONE -> strings.speedUnitKph
    }

    var peakRpm by remember { mutableStateOf<Int?>(null) }
    var peakSpeedKph by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(telemetry.rpm, calibratedSpeedKph) {
        telemetry.rpm?.let { r ->
            if (peakRpm == null || r > peakRpm!!) peakRpm = r
        }
        calibratedSpeedKph?.let { s ->
            if (peakSpeedKph == null || s > peakSpeedKph!!) peakSpeedKph = s
        }
    }

    val windowMap = remember {
        mutableStateMapOf<RaceWindowId, RaceWindowGeometry>().apply {
            putAll(RaceLayoutDefaults.decode(rigPrefs.raceLayoutSpec))
        }
    }

    LaunchedEffect(rigPrefs.raceLayoutSpec) {
        val decoded = RaceLayoutDefaults.decode(rigPrefs.raceLayoutSpec)
        windowMap.clear()
        windowMap.putAll(decoded)
    }

    val useProfileRpm = activeProfile != null &&
        activeProfile.id != VehicleProfileId.JEEP_JK &&
        activeProfile.id != VehicleProfileId.JEEP_JL
    val maxRpmScale = if (useProfileRpm) activeProfile!!.maxRpmScale else rigPrefs.engineProfile.maxRpmScale
    val rpmRedline = if (useProfileRpm) activeProfile!!.redlineRpm else rigPrefs.engineProfile.redlineRpm

    val speedDisplayValue: Float? = calibratedSpeedKph?.toFloat()
    val speedMax = 200f
    val speedWarn = 155f
    val speedTickCount = 6
    val speedUnit = strings.speedUnitKph
    val speedDisplayText = calibratedSpeedKph?.toString() ?: "--"
    val rpmText = telemetry.rpm?.let { String.format(Locale.US, "%,d", it) } ?: "--"

    val coolantDisplayValue: Float? = telemetry.coolantCelsius?.toFloat()
    val coolantMin = 0f
    val coolantMax = 130f
    val coolantWarn = (activeProfile?.coolantWarnC ?: rigPrefs.coolantWarnCelsius).toFloat()
    val tempUnit = "°C"
    val coolantFraction = coolantDisplayValue?.let {
        ((it - coolantMin) / (coolantMax - coolantMin)).coerceIn(0f, 1f)
    }
    val isCoolantOverheat = coolantDisplayValue != null && coolantDisplayValue >= coolantWarn

    val fuelFraction = telemetry.fuelLevelPercent?.let { (it / 100f).coerceIn(0f, 1f) }
    val isLowFuel = telemetry.fuelLevelPercent != null && telemetry.fuelLevelPercent <= 15f

    val mapDisplayValue: Float? = telemetry.manifoldKpa?.let { kpa ->
        if (isImperialMap) kpa * 0.145038f else kpa.toFloat()
    }
    val mapMax = if (isImperialMap) 35f else 250f
    val mapUnit = if (isImperialMap) "PSI" else "kPa"
    val mapValueText = mapDisplayValue?.let {
        if (isImperialMap) String.format(Locale.US, "%.1f", it) else "${it.roundToInt()}"
    } ?: "--"

    val voltsText = telemetry.batteryVolts?.let {
        String.format(Locale.US, "%.1fV", it)
    } ?: "--"
    val isLowBatt = telemetry.batteryVolts != null && telemetry.batteryVolts < rigPrefs.lowBatteryVolts

    val intakeText = telemetry.intakeAirCelsius?.let { c -> "$c°C" } ?: "--"

    val stftText = telemetry.shortTermFuelTrimB1?.let { trim ->
        String.format(Locale.US, "%+.1f%%", trim)
    } ?: "--"

    val isLocked = rigPrefs.raceLayoutLocked

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.boardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                onDisconnectClick = onDisconnect,
                detectedVehicle = detectedVehicle
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.surface,
                border = BorderStroke(1.dp, palette.bezelBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Day / Dark mode quick selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onSetRaceDayMode(true) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDayMode) palette.zone1 else palette.bezelBorder
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isDayMode) palette.zone1.copy(alpha = 0.16f) else palette.podSurface,
                                    contentColor = if (isDayMode) palette.zone1 else palette.textSecondary
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("race_day_mode_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LightMode,
                                    contentDescription = "Day mode",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.race_mode_day),
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { onSetRaceDayMode(false) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (!isDayMode) palette.zone1 else palette.bezelBorder
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!isDayMode) palette.zone1.copy(alpha = 0.16f) else palette.podSurface,
                                    contentColor = if (!isDayMode) palette.zone1 else palette.textSecondary
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("race_dark_mode_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DarkMode,
                                    contentDescription = stringResource(R.string.race_mode_dark),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.race_mode_dark),
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Lock & Reset layout controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onSetRaceLayoutLocked(!isLocked) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isLocked) palette.bezelBorder else palette.zone1
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isLocked) palette.podSurface else palette.zone1.copy(alpha = 0.14f),
                                    contentColor = if (isLocked) palette.textSecondary else palette.zone1
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("race_layout_lock_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.OpenWith,
                                    contentDescription = if (isLocked) {
                                        stringResource(R.string.race_layout_unlock)
                                    } else {
                                        stringResource(R.string.race_layout_lock)
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isLocked) {
                                        stringResource(R.string.race_layout_unlock)
                                    } else {
                                        stringResource(R.string.race_layout_lock)
                                    },
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val defaults = RaceLayoutDefaults.defaultLayout()
                                    windowMap.clear()
                                    windowMap.putAll(defaults)
                                    onResetRaceLayout()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, palette.bezelBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = palette.podSurface,
                                    contentColor = palette.zone3
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("race_layout_reset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RestartAlt,
                                    contentDescription = stringResource(R.string.race_layout_reset),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.race_layout_reset),
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isLocked) {
                            stringResource(R.string.race_hint_locked)
                        } else {
                            stringResource(R.string.race_hint_unlocked)
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isLocked) palette.textSecondary else palette.zone1
                    )

                    if (detectedVehicle != null || vinInfo != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.podSurface,
                            border = BorderStroke(1.dp, palette.zone2.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("race_vin_badge")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = detectedVehicle?.displayTitle ?: "${strings.vinPrefix}:",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = palette.zone2,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    val rightTag = when {
                                        detectedVehicle?.vin != null -> detectedVehicle.vin
                                        vinInfo != null -> "${vinInfo.vin} • ${vinInfo.modelYear}"
                                        detectedVehicle?.protocolName != null -> detectedVehicle.protocolName
                                        else -> activeProfile?.shortBadge.orEmpty()
                                    }
                                    Text(
                                        text = rightTag,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = palette.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Outer BoxWithConstraints gets exact finite viewport bounds; inner Column scrolls smoothly!
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val density = LocalDensity.current
            val boardWidthDp = maxWidth
            val viewportHeightDp = maxHeight
            val boardWidthPx = with(density) { boardWidthDp.toPx() }.coerceAtLeast(1f)
            val maxBottomDp = windowMap.values.maxOfOrNull { it.yDp + it.heightDp } ?: 1052f
            val totalBoardHeightDp = maxOf(viewportHeightDp.value, maxBottomDp + 48f).dp

            var dragBaseline by remember { mutableStateOf<Map<RaceWindowId, RaceWindowGeometry>?>(null) }

            val bringToFront: (RaceWindowId) -> Unit = { id ->
                val current = windowMap[id]
                if (current != null) {
                    val maxZ = windowMap.values.maxOfOrNull { it.zOrder } ?: 0
                    if (current.zOrder < maxZ) {
                        windowMap[id] = current.copy(zOrder = maxZ + 1)
                    }
                }
                if (dragBaseline == null) {
                    dragBaseline = windowMap.toMap()
                }
            }

            val onMoveDelta: (RaceWindowId, Float, Float) -> Unit = { id, dxPx, dyPx ->
                val g = windowMap[id]
                if (g != null) {
                    val base = dragBaseline ?: windowMap.toMap().also { dragBaseline = it }
                    val dxFrac = dxPx / boardWidthPx
                    val dyDp = with(density) { dyPx.toDp().value }
                    val newX = (g.xFraction + dxFrac).coerceIn(0f, (1f - g.widthFraction).coerceAtLeast(0f))
                    val newY = (g.yDp + dyDp).coerceIn(0f, 2200f)
                    val moved = g.copy(xFraction = newX, yDp = newY)
                    val resolved = RaceLayoutSolver.resolveOverlaps(
                        baseline = base,
                        activeId = id,
                        activeGeometry = moved,
                        boardWidthDp = boardWidthDp.value
                    )
                    windowMap.putAll(resolved)
                }
            }

            val onResizeDelta: (RaceWindowId, Float, Float) -> Unit = { id, dxPx, dyPx ->
                val g = windowMap[id]
                if (g != null) {
                    val base = dragBaseline ?: windowMap.toMap().also { dragBaseline = it }
                    val dwFrac = dxPx / boardWidthPx
                    val dhDp = with(density) { dyPx.toDp().value }
                    val minWFrac = (g.minWidthDp / boardWidthDp.value).coerceIn(0.12f, 0.90f)
                    val maxWFrac = (1f - g.xFraction).coerceAtLeast(minWFrac)
                    val newW = (g.widthFraction + dwFrac).coerceIn(minWFrac, maxWFrac)
                    val newH = (g.heightDp + dhDp).coerceIn(g.minHeightDp, g.maxHeightDp)
                    val resized = g.copy(widthFraction = newW, heightDp = newH)
                    val resolved = RaceLayoutSolver.resolveOverlaps(
                        baseline = base,
                        activeId = id,
                        activeGeometry = resized,
                        boardWidthDp = boardWidthDp.value
                    )
                    windowMap.putAll(resolved)
                }
            }

            val commitGesture: () -> Unit = {
                dragBaseline = null
                onSaveRaceLayout(windowMap.toMap())
            }

            val boardScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(boardScrollState)
                    .testTag("race_adjustable_board")
            ) {
                // Keep the adjustable coordinate board physically LTR so touch drag, bottom-right resize,
                // Fuel-left/Coolant-right placement, and clockwise dials stay consistent under Hebrew RTL.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(totalBoardHeightDp)
                    ) {
                        val orderedEntries = windowMap.entries.sortedBy { it.value.zOrder }
                        for ((id, geom) in orderedEntries) {
                            key(id) {
                                AdjustableRaceWindow(
                                    windowId = id,
                                    geometry = geom,
                                    boardWidthPx = boardWidthPx,
                                    isLocked = isLocked,
                                    onBringToFront = bringToFront,
                                    onMoveDelta = onMoveDelta,
                                    onResizeDelta = onResizeDelta,
                                    onCommitGesture = commitGesture
                                ) {
                                when (id) {
                                    RaceWindowId.SHIFT_LIGHTS -> {
                                        RaceShiftLightBar(
                                            rpm = telemetry.rpm,
                                            rpmRedline = rpmRedline,
                                            modifier = Modifier.testTag("race_shift_light_bar")
                                        )
                                    }

                                    RaceWindowId.FUEL_BAR -> {
                                        RaceSegmentedBarGauge(
                                            title = strings.fuelTitle,
                                            topMarker = "F",
                                            bottomMarker = "E",
                                            valueText = telemetry.fuelLevelPercent?.let { "${it.roundToInt()}%" } ?: "--%",
                                            fraction = fuelFraction,
                                            isWarning = isLowFuel,
                                            isCoolantMode = false,
                                            modifier = Modifier.testTag("race_gauge_fuel")
                                        )
                                    }

                                    RaceWindowId.SPEED_DIAL -> {
                                        RaceSpeedometerDial(
                                            speedValue = speedDisplayValue,
                                            speedDisplayText = speedDisplayText,
                                            speedUnit = speedUnit,
                                            maxSpeedScale = speedMax,
                                            speedWarningThreshold = speedWarn,
                                            majorTickCount = speedTickCount,
                                            speedBadgeText = speedBadgeText,
                                            modifier = Modifier.testTag("race_gauge_speedometer")
                                        )
                                    }

                                RaceWindowId.COOLANT_BAR -> {
                                    RaceSegmentedBarGauge(
                                        title = strings.coolantTitle,
                                        topMarker = "H",
                                        bottomMarker = "C",
                                        valueText = coolantDisplayValue?.let { "${it.roundToInt()}$tempUnit" } ?: "--$tempUnit",
                                        fraction = coolantFraction,
                                        isWarning = isCoolantOverheat,
                                        isCoolantMode = true,
                                        modifier = Modifier.testTag("race_gauge_coolant")
                                    )
                                }

                                RaceWindowId.THROTTLE_BAR -> {
                                    RaceThrottleBarCard(
                                        throttlePercent = telemetry.throttlePercent,
                                        modifier = Modifier.testTag("race_throttle_bar")
                                    )
                                }

                                RaceWindowId.RPM_POD -> {
                                    RaceCircularArcPod(
                                        title = strings.engineRpmTitle,
                                        valueText = rpmText,
                                        unit = strings.rpmUnit,
                                        value = telemetry.rpm?.toFloat(),
                                        minValue = 0f,
                                        maxValue = maxRpmScale,
                                        gradientColors = listOf(palette.zone1, palette.zone2, palette.zone3, palette.zone5),
                                        modifier = Modifier.testTag("race_pod_rpm")
                                    )
                                }

                                RaceWindowId.MAP_POD -> {
                                    RaceCircularArcPod(
                                        title = strings.manifoldMapTitle,
                                        valueText = mapValueText,
                                        unit = mapUnit,
                                        value = mapDisplayValue,
                                        minValue = 0f,
                                        maxValue = mapMax,
                                        gradientColors = listOf(palette.accentBlue, palette.zone1, palette.zone3, palette.zone5),
                                        modifier = Modifier.testTag("race_pod_map")
                                    )
                                }

                                RaceWindowId.LOAD_POD -> {
                                    RaceCircularArcPod(
                                        title = strings.engineLoadTitle,
                                        valueText = telemetry.engineLoadPercent?.let { "${it.roundToInt()}" } ?: "--",
                                        unit = "%",
                                        value = telemetry.engineLoadPercent,
                                        minValue = 0f,
                                        maxValue = 100f,
                                        gradientColors = listOf(palette.zone2, palette.zone3, palette.zone4, palette.zone5),
                                        modifier = Modifier.testTag("race_pod_load")
                                    )
                                }

                                RaceWindowId.PEAKS_CARD -> {
                                    RaceSessionPeaksCard(
                                        peakRpm = peakRpm,
                                        peakSpeedDisplay = peakSpeedKph,
                                        speedUnit = speedUnit,
                                        batteryVoltsText = voltsText,
                                        isLowBattery = isLowBatt,
                                        intakeTempText = intakeText,
                                        fuelTrimText = stftText,
                                        vinText = vinInfo?.vin,
                                        onResetPeaks = {
                                            peakRpm = telemetry.rpm
                                            peakSpeedKph = calibratedSpeedKph
                                        },
                                        modifier = Modifier.testTag("race_peaks_card")
                                    )
                                }

                                RaceWindowId.INCLINOMETER -> {
                                    TrailInclinometerCard(
                                        sensorState = sensorState,
                                        pitchTareDeg = rigPrefs.pitchTareDeg,
                                        rollTareDeg = rigPrefs.rollTareDeg,
                                        onTareClick = onTareInclinometer,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
