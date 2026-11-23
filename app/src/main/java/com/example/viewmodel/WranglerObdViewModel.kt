package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.BluetoothManager
import com.example.data.JkObdDatabase
import com.example.data.RigPreferencesRepository
import com.example.data.ScanHistoryRepository
import com.example.data.ScanRecordEntity
import com.example.obd.AppLanguage
import com.example.obd.AppVisualTheme
import com.example.obd.BluetoothDeviceItem
import com.example.obd.ConnectionStatus
import com.example.obd.DiagnosticReading
import com.example.obd.DiagnosticTestId
import com.example.obd.DtcCodeInfo
import com.example.obd.JkDiagnosticLibrary
import com.example.obd.JkEngineProfile
import com.example.obd.JkSubsystem
import com.example.obd.LiveTelemetry
import com.example.obd.NightModeSetting
import com.example.obd.RaceLayoutDefaults
import com.example.obd.RaceWindowGeometry
import com.example.obd.RaceWindowId
import com.example.obd.RigPreferences
import com.example.obd.TerminalEntry
import com.example.obd.TrailSensorState
import com.example.obd.UnitSystem
import com.example.obd.VinInfo
import com.example.obd.parseTireSizeInches
import com.example.sensors.GpsSpeedManager
import com.example.sensors.GpsTelemetryState
import com.example.sensors.PhoneBatteryMonitor
import com.example.sensors.PhoneBatteryState
import com.example.sensors.SolarSchedule
import com.example.sensors.SolarTimeCalculator
import com.example.sensors.TrailSensorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class WranglerObdViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothManager = BluetoothManager(application.applicationContext)
    private val sensorManager = TrailSensorManager(application.applicationContext)
    private val batteryMonitor = PhoneBatteryMonitor(application.applicationContext)
    private val gpsSpeedManager = GpsSpeedManager(application.applicationContext)
    private val rigPrefsRepo = RigPreferencesRepository(application.applicationContext)
    private val scanHistoryRepo = ScanHistoryRepository(
        JkObdDatabase.getInstance(application.applicationContext).scanRecordDao()
    )

    val bluetoothPermissionsGranted: StateFlow<Boolean> = bluetoothManager.permissionsGranted
    val connectionStatus: StateFlow<ConnectionStatus> = bluetoothManager.connectionStatus
    val connectedDevice: StateFlow<BluetoothDeviceItem?> = bluetoothManager.connectedDevice
    val statusMessage: StateFlow<String> = bluetoothManager.statusMessage
    val availableDevices: StateFlow<List<BluetoothDeviceItem>> = bluetoothManager.availableDevices
    val isScanningBluetooth: StateFlow<Boolean> = bluetoothManager.isScanning
    val telemetry: StateFlow<LiveTelemetry> = bluetoothManager.telemetry
    val activeDtcs: StateFlow<List<String>> = bluetoothManager.activeDtcs
    val pendingDtcs: StateFlow<List<String>> = bluetoothManager.pendingDtcs
    val vinInfo: StateFlow<VinInfo?> = bluetoothManager.vinInfo
    val terminalLogs: StateFlow<List<TerminalEntry>> = bluetoothManager.terminalLogs
    val sensorState: StateFlow<TrailSensorState> = sensorManager.sensorState
    val phoneBatteryState: StateFlow<PhoneBatteryState> = batteryMonitor.batteryState
    val gpsState: StateFlow<GpsTelemetryState> = gpsSpeedManager.gpsState
    val appliedDiagnosticTests: StateFlow<Set<DiagnosticTestId>> = bluetoothManager.appliedDiagnosticTests
    val diagnosticReadings: StateFlow<Map<DiagnosticTestId, DiagnosticReading>> = bluetoothManager.diagnosticReadings
    val isDiagnosticScanPaused: StateFlow<Boolean> = bluetoothManager.isDiagnosticScanPaused
    val diagnosticCycleCount: StateFlow<Long> = bluetoothManager.diagnosticCycleCount

    val rigPreferences: StateFlow<RigPreferences> = rigPrefsRepo.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RigPreferences()
        )

    init {
        viewModelScope.launch {
            rigPrefsRepo.preferencesFlow.collect { prefs ->
                bluetoothManager.updateDiagnosticSettings(
                    speedFactor = prefs.speedCalibrationFactor,
                    coolantWarnC = prefs.coolantWarnCelsius
                )
                val decoded = DiagnosticTestId.decodeSet(prefs.selectedDiagnosticTestsCsv)
                if (bluetoothManager.appliedDiagnosticTests.value != decoded) {
                    bluetoothManager.setAppliedDiagnosticTests(decoded, forceReprobe = false)
                }
            }
        }
    }

    private val minuteTicker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000L)
        }
    }

    val solarSchedule: StateFlow<SolarSchedule> = combine(
        gpsState,
        minuteTicker
    ) { gps, _ ->
        SolarTimeCalculator.computeSchedule(gps.latitude, gps.longitude)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SolarTimeCalculator.computeSchedule(null, null)
    )

    val isNightModeActive: StateFlow<Boolean> = combine(
        rigPreferences,
        solarSchedule
    ) { prefs, solar ->
        when (prefs.nightModeSetting) {
            NightModeSetting.MANUAL_ON -> true
            NightModeSetting.MANUAL_OFF -> false
            NightModeSetting.AUTO_SUN -> solar.isNightNow
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val scanHistory: StateFlow<List<ScanRecordEntity>> = scanHistoryRepo.allScans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _dtcSearchQuery = MutableStateFlow("")
    val dtcSearchQuery: StateFlow<String> = _dtcSearchQuery.asStateFlow()

    private val _selectedSubsystem = MutableStateFlow(JkSubsystem.ALL)
    val selectedSubsystem: StateFlow<JkSubsystem> = _selectedSubsystem.asStateFlow()

    val filteredCatalogCodes: StateFlow<List<DtcCodeInfo>> = combine(
        _dtcSearchQuery,
        _selectedSubsystem
    ) { query, subsystem ->
        val q = query.trim().uppercase()
        JkDiagnosticLibrary.jkDtcCatalog.filter { info ->
            val matchesSubsystem = subsystem == JkSubsystem.ALL || info.subsystem == subsystem
            val matchesQuery = q.isEmpty() ||
                info.code.contains(q) ||
                info.title.uppercase().contains(q) ||
                info.description.uppercase().contains(q) ||
                info.affectedEngines.uppercase().contains(q)
            matchesSubsystem && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = JkDiagnosticLibrary.jkDtcCatalog
    )

    private val _uiFeedbackMessage = MutableStateFlow<String?>(null)
    val uiFeedbackMessage: StateFlow<String?> = _uiFeedbackMessage.asStateFlow()

    fun startSensors() {
        sensorManager.start()
        batteryMonitor.start()
        gpsSpeedManager.refreshLastKnownCoordinates()
    }

    fun stopSensors() {
        sensorManager.stop()
        batteryMonitor.stop()
        gpsSpeedManager.stopCalibrationUpdates()
    }

    fun hasFineLocationPermission(): Boolean = gpsSpeedManager.hasFineLocationPermission()

    fun onLocationPermissionResult(granted: Boolean, startCalibrationAfterGrant: Boolean = true) {
        gpsSpeedManager.refreshLastKnownCoordinates()
        if (granted && startCalibrationAfterGrant) {
            gpsSpeedManager.startCalibrationUpdates()
        } else if (!granted) {
            _uiFeedbackMessage.value = msg(
                en = "Fine location permission is needed for live GPS speed calibration.",
                he = "נדרשת הרשאת מיקום מדויק לכיול מהירות GPS בזמן אמת."
            )
        }
    }

    fun startGpsCalibration() = gpsSpeedManager.startCalibrationUpdates()

    fun stopGpsCalibration() = gpsSpeedManager.stopCalibrationUpdates()

    fun applyLiveGpsSpeedCalibration() {
        val gpsKph = gpsState.value.gpsSpeedKph
        val obdKph = telemetry.value.speedKmh
        if (gpsKph == null || gpsKph < 10f) {
            _uiFeedbackMessage.value = msg(
                en = "Drive at least 10 KPH with active GPS lock to calculate offset.",
                he = "יש לנסוע לפחות 10 קמ\"ש עם קליטת GPS פעילה לחישוב הסטייה."
            )
            return
        }
        if (obdKph == null || obdKph < 10) {
            _uiFeedbackMessage.value = msg(
                en = "Connect OBD-II and drive at least 10 KPH to match ECU speed.",
                he = "חבר מתאם OBD-II וסע לפחות 10 קמ\"ש להשוואת מהירות המחשב."
            )
            return
        }
        val factor = (gpsKph / obdKph.toFloat()).coerceIn(0.70f, 1.45f)
        viewModelScope.launch {
            rigPrefsRepo.setSpeedCalibration(factor)
            val pct = (factor - 1f) * 100f
            _uiFeedbackMessage.value = String.format(
                Locale.US,
                msg(
                    en = "GPS calibration applied: ×%.3f (%+.1f%%)",
                    he = "כיול GPS הוחל: ×%.3f (%+.1f%%)"
                ),
                factor,
                pct
            )
        }
    }

    fun applyTireRatioCalibration(stockTireInches: Float) {
        val actualInches = parseTireSizeInches(rigPreferences.value.tireSizeInches)
        if (actualInches == null || stockTireInches <= 0f) {
            _uiFeedbackMessage.value = msg(
                en = "Enter a valid tire diameter (e.g. 35\") in Rig Profile.",
                he = "הזן קוטר צמיג תקין (למשל 35\") בפרופיל הרכב."
            )
            return
        }
        val factor = (actualInches / stockTireInches).coerceIn(0.70f, 1.45f)
        viewModelScope.launch {
            rigPrefsRepo.setSpeedCalibration(factor, stockTireInches)
            val pct = (factor - 1f) * 100f
            _uiFeedbackMessage.value = String.format(
                Locale.US,
                msg(
                    en = "Tire-size offset applied: ×%.3f (%+.1f%%)",
                    he = "יחס קוטר צמיג הוחל: ×%.3f (%+.1f%%)"
                ),
                factor,
                pct
            )
        }
    }

    fun setSpeedCalibrationFactor(factor: Float, stockTireInches: Float? = null) {
        viewModelScope.launch {
            rigPrefsRepo.setSpeedCalibration(factor, stockTireInches)
        }
    }

    fun requiredBluetoothPermissions(): Array<String> = bluetoothManager.requiredPermissions()

    fun hasBluetoothPermissions(): Boolean = bluetoothManager.hasAllPermissions()

    fun syncBluetoothPermissions(): Boolean = bluetoothManager.refreshPermissionState()

    fun onBluetoothPermissionsResult(results: Map<String, Boolean>) {
        val granted = bluetoothManager.refreshPermissionState()
        if (!granted && results.isNotEmpty()) {
            _uiFeedbackMessage.value = "BLUETOOTH_CONNECT and BLUETOOTH_SCAN permissions are needed to pair ELM327 adapters."
        }
    }

    fun isBluetoothEnabled(): Boolean = bluetoothManager.isBluetoothEnabled()

    fun refreshPairedDevices() = bluetoothManager.refreshPairedDevices()

    fun startBluetoothDiscovery() = bluetoothManager.startDiscovery()

    fun stopBluetoothDiscovery() = bluetoothManager.stopDiscovery()

    fun connectAdapter(device: BluetoothDeviceItem) = bluetoothManager.connect(device)

    fun disconnectAdapter() = bluetoothManager.disconnect()

    fun readDtcsFromEcu() {
        if (connectionStatus.value != ConnectionStatus.CONNECTED) {
            _uiFeedbackMessage.value = msg(
                en = "Connect an ELM327 adapter to scan live ECU codes.",
                he = "חבר מתאם ELM327 לסריקת קודי תקלה מהמחשב."
            )
            return
        }
        bluetoothManager.readDtcs()
    }

    fun clearDtcsOnEcu() {
        if (connectionStatus.value != ConnectionStatus.CONNECTED) {
            _uiFeedbackMessage.value = msg(
                en = "Connect an ELM327 adapter to clear ECU codes.",
                he = "חבר מתאם ELM327 לניקוי קודי תקלה."
            )
            return
        }
        bluetoothManager.clearDtcs()
        _uiFeedbackMessage.value = msg(
            en = "Mode 04 Clear DTC command sent to Wrangler JK ECU.",
            he = "פקודת ניקוי Mode 04 נשלחה למחשב הרכב."
        )
    }

    fun saveScanSnapshot(note: String = "") {
        val currentTelemetry = telemetry.value
        val active = activeDtcs.value
        val pending = pendingDtcs.value
        val adapter = connectedDevice.value?.name ?: "Manual / Offline Snapshot"
        val vin = vinInfo.value?.vin ?: "Not Read"
        val engine = rigPreferences.value.engineProfile.label

        viewModelScope.launch {
            scanHistoryRepo.saveScan(
                ScanRecordEntity(
                    adapterName = adapter,
                    vin = vin,
                    engineProfile = engine,
                    milOn = currentTelemetry.milOn || active.isNotEmpty(),
                    activeCodesCsv = active.joinToString(", "),
                    pendingCodesCsv = pending.joinToString(", "),
                    coolantCelsius = currentTelemetry.coolantCelsius,
                    batteryVolts = currentTelemetry.batteryVolts,
                    notes = note.trim()
                )
            )
            _uiFeedbackMessage.value = msg(
                en = "Diagnostic snapshot saved to Garage log.",
                he = "צילום מצב האבחון נשמר ביומן המוסך."
            )
        }
    }

    fun deleteScanRecord(id: Long) {
        viewModelScope.launch {
            scanHistoryRepo.deleteScan(id)
        }
    }

    fun sendTerminalCommand(command: String) = bluetoothManager.sendManualCommand(command)

    fun updateDtcSearchQuery(query: String) {
        _dtcSearchQuery.value = query
    }

    fun selectSubsystem(subsystem: JkSubsystem) {
        _selectedSubsystem.value = subsystem
    }

    fun tareInclinometer() {
        val current = sensorState.value
        viewModelScope.launch {
            rigPrefsRepo.setInclinometerTare(current.pitchDegrees, current.rollDegrees)
            _uiFeedbackMessage.value = msg(
                en = "Inclinometer zeroed to current rig mount angle.",
                he = "מד השיפוע אופס לזווית ההתקנה הנוכחית."
            )
        }
    }

    fun resetInclinometerTare() {
        viewModelScope.launch {
            rigPrefsRepo.setInclinometerTare(0f, 0f)
        }
    }

    fun updateRigDetails(nickname: String, tireSize: String, axleRatio: String) {
        viewModelScope.launch {
            rigPrefsRepo.updateRigDetails(nickname, tireSize, axleRatio)
            _uiFeedbackMessage.value = msg(
                en = "Wrangler JK rig profile updated.",
                he = "פרופיל הרכב עודכן."
            )
        }
    }

    fun setEngineProfile(profile: JkEngineProfile) {
        viewModelScope.launch {
            rigPrefsRepo.setEngineProfile(profile)
        }
    }

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            rigPrefsRepo.setUnitSystem(unitSystem)
        }
    }

    fun setAlertThresholds(coolantWarnCelsius: Int, lowBatteryVolts: Float) {
        viewModelScope.launch {
            rigPrefsRepo.setAlertThresholds(coolantWarnCelsius, lowBatteryVolts)
        }
    }

    fun setVisualTheme(theme: AppVisualTheme) {
        viewModelScope.launch {
            rigPrefsRepo.setVisualTheme(theme)
        }
    }

    fun setRaceDayMode(enableDayMode: Boolean) {
        viewModelScope.launch {
            val prefs = rigPreferences.value
            if (enableDayMode) {
                rigPrefsRepo.setNightModeSetting(NightModeSetting.MANUAL_OFF)
                rigPrefsRepo.setVisualTheme(AppVisualTheme.CLASSIC_WHITE)
            } else {
                val targetDarkTheme = prefs.lastDarkTheme.takeIf { it != AppVisualTheme.CLASSIC_WHITE }
                    ?: AppVisualTheme.NEON_CYAN_AMBER
                rigPrefsRepo.setVisualTheme(targetDarkTheme)
                if (prefs.nightModeSetting == NightModeSetting.MANUAL_ON) {
                    rigPrefsRepo.setNightModeSetting(NightModeSetting.MANUAL_OFF)
                }
            }
        }
    }

    fun setNightModeSetting(setting: NightModeSetting) {
        viewModelScope.launch {
            rigPrefsRepo.setNightModeSetting(setting)
        }
    }

    fun setNightBrightnessPercent(percent: Int) {
        viewModelScope.launch {
            rigPrefsRepo.setNightBrightnessPercent(percent)
        }
    }

    fun saveRaceLayout(layout: Map<RaceWindowId, RaceWindowGeometry>) {
        viewModelScope.launch {
            rigPrefsRepo.setRaceLayoutSpec(RaceLayoutDefaults.encode(layout))
        }
    }

    fun resetRaceLayout() {
        viewModelScope.launch {
            rigPrefsRepo.setRaceLayoutSpec("")
            _uiFeedbackMessage.value = msg(
                en = "Race gauge layout reset to default.",
                he = "פריסת שעוני המרוץ אופסה לברירת המחדל."
            )
        }
    }

    fun setRaceLayoutLocked(locked: Boolean) {
        viewModelScope.launch {
            rigPrefsRepo.setRaceLayoutLocked(locked)
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            rigPrefsRepo.setAppLanguage(language)
            _uiFeedbackMessage.value = if (language == AppLanguage.HEBREW) {
                "שפת האפליקציה שונתה לעברית"
            } else {
                "App language set to English"
            }
        }
    }

    fun applyDiagnosticTests(selected: Set<DiagnosticTestId>) {
        val csv = DiagnosticTestId.encodeSet(selected)
        bluetoothManager.setAppliedDiagnosticTests(selected, forceReprobe = true)
        viewModelScope.launch {
            rigPrefsRepo.setSelectedDiagnosticTests(csv)
            if (selected.isNotEmpty()) {
                _uiFeedbackMessage.value = msg(
                    en = "Applied ${selected.size} OBD diagnostic tests for real-time scan.",
                    he = "הוחלו ${selected.size} בדיקות OBD לסריקה בזמן אמת."
                )
            }
        }
    }

    fun setDiagnosticScreenVisible(visible: Boolean) {
        bluetoothManager.setDiagnosticScreenActive(visible)
    }

    fun toggleDiagnosticScanPaused() {
        bluetoothManager.toggleDiagnosticScanPaused()
    }

    fun reprobeUnsupportedDiagnostics() {
        bluetoothManager.reprobeUnsupportedDiagnostics()
        _uiFeedbackMessage.value = msg(
            en = "Re-probing ECU for previously unsupported PIDs…",
            he = "סורק מחדש חיישנים שלא זוהו מול מחשב הרכב…"
        )
    }

    fun consumeFeedbackMessage() {
        _uiFeedbackMessage.value = null
    }

    private fun msg(en: String, he: String): String {
        return if (rigPreferences.value.appLanguage == AppLanguage.HEBREW) he else en
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stop()
        batteryMonitor.stop()
        gpsSpeedManager.stopCalibrationUpdates()
        bluetoothManager.release()
    }
}
