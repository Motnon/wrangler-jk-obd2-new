package com.example.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.obd.AppLanguage
import com.example.obd.AppUiStyle
import com.example.obd.AppVisualTheme
import com.example.obd.JkEngineProfile
import com.example.obd.JkSubsystem
import com.example.obd.NightModeSetting
import com.example.obd.UnitSystem
import com.example.obd.WranglerModel

data class AppStrings(
    val language: AppLanguage,
    // Common & Banner
    val linkError: String,
    val milOn: String,
    val readyToPairStatus: String,
    // Race Screen & Gauges
    val shiftLightsTitle: String,
    val redlineBadge: String,
    val liveBadge: String,
    val idleBadge: String,
    val obdSpeedLabel: String,
    val speedUnitKph: String,
    val liveSpeedFormat: String,
    val gpsSpeedBadge: String,
    val calSpeedBadge: String,
    val fuelTitle: String,
    val coolantTitle: String,
    val throttleTitle: String,
    val engineRpmTitle: String,
    val rpmUnit: String,
    val manifoldMapTitle: String,
    val engineLoadTitle: String,
    val pitWallPeaksTitle: String,
    val resetPeaksCd: String,
    val peakRpmLabel: String,
    val topSpeedLabelFormat: String,
    val batteryLabel: String,
    val intakeAirLabel: String,
    val fuelTrimLabel: String,
    val vinPrefix: String,
    // Cockpit & Inclinometer
    val cockpitEngineRpm: String,
    val cockpitCoolantTemp: String,
    val cockpitBatteryAlt: String,
    val cockpitVehicleSpeed: String,
    val cockpitIntakeAir: String,
    val cockpitEngineLoad: String,
    val miniThrottle: String,
    val miniMap: String,
    val trailInclinometer: String,
    val liveGravityCompass: String,
    val accelUnavailable: String,
    val rollSuffix: String,
    val pitchSuffix: String,
    val trailHeading: String,
    // PID Scan (LiveDiagnosticsScreen)
    val selectTestsTabFormat: String,
    val liveScanTabFormat: String,
    val filterPidsPlaceholder: String,
    val selectAllShort: String,
    val clearShort: String,
    val unitPrefix: String,
    val testsMarkedFormat: String,
    val applyHint: String,
    val applyCountFormat: String,
    val noTestsChosenTitle: String,
    val noTestsChosenBody: String,
    val chooseTestsButton: String,
    val readyConnectObd: String,
    val scanPausedFormat: String,
    val realTimeScanFormat: String,
    val chosenParamsFormat: String,
    val connectShort: String,
    val pauseShort: String,
    val resumeShort: String,
    val editShort: String,
    // DTC Scanner
    val clearDialogTitle: String,
    val clearDialogBody: String,
    val sendMode04Clear: String,
    val cancel: String,
    val saveSnapshotTitle: String,
    val saveSnapshotBody: String,
    val snapshotNoteLabel: String,
    val snapshotNotePlaceholder: String,
    val save: String,
    val ecuScannerHeader: String,
    val scannerConnectPrompt: String,
    val scannerCleanPrompt: String,
    val scannerCountFormat: String,
    val liveEcuFaultsHeader: String,
    val activeMode03Badge: String,
    val pendingMode07Badge: String,
    val faultEncyclopediaHeader: String,
    val faultEncyclopediaFormat: String,
    val allModelsChip: String,
    val jlSgwBannerTitle: String,
    val jlSgwBannerBody: String,
    val searchDtcPlaceholder: String,
    val trailShopGuidance: String,
    // Terminal
    val terminalHeader: String,
    val terminalActiveLink: String,
    val terminalOffline: String,
    val pairShort: String,
    val rawTxRxStream: String,
    val supportedPidMonitor: String,
    val supportedPidMonitorFormat: String,
    val terminalInputPlaceholder: String,
    // Garage & Settings Cards
    val languageCardHeader: String,
    val languageCardSubtitle: String,
    val languageActiveSummary: String,
    val heroSubtitleFormat: String,
    val powertrainHeader: String,
    val powertrainHeaderFormat: String,
    val wranglerModelLabel: String,
    val rigNameLabel: String,
    val tireSizeLabel: String,
    val axleRatioLabel: String,
    val saveRigProfile: String,
    val visualThemeHeader: String,
    val displayColorTheme: String,
    val nightOffRoadMode: String,
    val nightActiveBadge: String,
    val daylightBadge: String,
    val useGpsSun: String,
    val nightBrightnessLabel: String,
    val solarUsingLocationFormat: String,
    val solarDefaultSunFormat: String,
    val gpsCalibrationHeader: String,
    val oversizedTiresWarningFormat: String,
    val gpsSpeedLabel: String,
    val obdRawCalLabel: String,
    val liveGpsOffsetFormat: String,
    val enableGps: String,
    val stopGps: String,
    val startGps: String,
    val matchGps: String,
    val useTireRatioFormat: String,
    val resetOneX: String,
    val manualSpeedMultiplier: String,
    val unitsAlertsHeader: String,
    val metricOption: String,
    val imperialMapOption: String,
    val coolantWarnLabel: String,
    val batteryWarnLabel: String,
    val resetInclinometerZero: String,
    val ecuDiagLogFormat: String,
    val noSavedSnapshots: String,
    val noSavedSnapshotsBody: String,
    val activeDtcPrefix: String,
    val pendingDtcPrefix: String,
    val cleanScanZeroDtcs: String,
    // Bluetooth Sheet
    val btSheetTitle: String,
    val btSheetSubtitle: String,
    val btPermRequiredTitle: String,
    val btPermRequiredBody: String,
    val grantAccess: String,
    val settingsButton: String,
    val btScanningStop: String,
    val btScanNearby: String,
    val btPairedButton: String,
    val btDisabledWarning: String,
    val btNoAdaptersTitle: String,
    val btNoAdaptersBody: String,
    // Multi-Vehicle Auto-Detection & Profile Override
    val vehicleProfileHeader: String,
    val vehicleProfileSubtitle: String,
    val detectedVehicleLabel: String,
    val activeProtocolLabel: String,
    val supportedPidsCountFormat: String,
    val cachedAdaptersHeader: String,
    val forgetCachedCd: String,
    val profileMonitorFormat: String,
    // GUI Architecture Styles (3 Options)
    val uiStyleCardHeader: String,
    val uiStyleCardSubtitle: String,
    val uiStyleSheetTitle: String,
    val uiStyleSheetSubtitle: String,
    val cdSwitchUiStyle: String,
    val uiStyleActiveBadge: String,
    // Option 2: Avionics HUD
    val hudNavHud: String,
    val hudNavPids: String,
    val hudNavFaults: String,
    val hudNavTerm: String,
    val hudNavRig: String,
    val hudHorizonLabel: String,
    val hudFlightNominal: String,
    val hudFlightAlert: String,
    val hudLaserRpm: String,
    val hudOpticalVel: String,
    // Option 3: Automotive Infotainment Split-Deck
    val driveNavHome: String,
    val driveNavRace: String,
    val driveNavPids: String,
    val driveNavFaults: String,
    val driveNavTerm: String,
    val driveNavSetup: String,
    val driveStyleShort: String,
    val driveThermalsTitle: String,
    val driveElectricalTitle: String,
    val driveInclinometerTitle: String,
    val driveHealthNominal: String,
    val driveHealthAttention: String,
    val driveOpenRaceCluster: String,
    val driveOpenDiagnostics: String,
    val driveOpenScanner: String
) {
    fun uiStyleTitle(style: AppUiStyle): String = when (language) {
        AppLanguage.ENGLISH -> when (style) {
            AppUiStyle.TACTICAL_MOTORSPORT -> "Tactical Motorsport"
            AppUiStyle.AVIONICS_HUD -> "Glass Aero HUD"
            AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> "Drive Split-Deck"
        }
        AppLanguage.HEBREW -> when (style) {
            AppUiStyle.TACTICAL_MOTORSPORT -> "מרוץ טקטי (קלאסי)"
            AppUiStyle.AVIONICS_HUD -> "תצוגה עילית HUD"
            AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> "מסך רכב מפוצל"
        }
    }

    fun uiStyleSubtitle(style: AppUiStyle): String = when (language) {
        AppLanguage.ENGLISH -> when (style) {
            AppUiStyle.TACTICAL_MOTORSPORT -> "6-tab cockpit bar with draggable, resizable analog & digital race gauges"
            AppUiStyle.AVIONICS_HUD -> "Fighter-jet HUD reticle, laser RPM tape, chamfered frames & floating glass dock"
            AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> "CarPlay-style side rail dock with oversized Bento telemetry split tiles"
        }
        AppLanguage.HEBREW -> when (style) {
            AppUiStyle.TACTICAL_MOTORSPORT -> "סרגל 6 לשוניות עם שעוני מרוץ הניתנים לגרירה ושינוי גודל"
            AppUiStyle.AVIONICS_HUD -> "כוונת תצוגה עילית, מד סל\"ד לייזר, מסגרות קטומות וסרגל צף"
            AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> "סרגל צד קבוע בסגנון מולטימדיה ברכב עם אריחי בנטו גדולים"
        }
    }

    fun uiStyleBadge(style: AppUiStyle): String = when (language) {
        AppLanguage.ENGLISH -> when (style) {
            AppUiStyle.TACTICAL_MOTORSPORT -> "OPTION 1 • CLASSIC"
            AppUiStyle.AVIONICS_HUD -> "OPTION 2 • AERO HUD"
            AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> "OPTION 3 • CAR DECK"
        }
        AppLanguage.HEBREW -> when (style) {
            AppUiStyle.TACTICAL_MOTORSPORT -> "אפשרות 1 • קלאסי"
            AppUiStyle.AVIONICS_HUD -> "אפשרות 2 • HUD"
            AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> "אפשרות 3 • מולטימדיה"
        }
    }

    fun profileOverrideLabel(profileId: String): String = when (language) {
        AppLanguage.ENGLISH -> when (profileId) {
            "auto" -> "Auto-Detect (VIN)"
            "jeep_jk" -> "Jeep Wrangler JK"
            "jeep_jl" -> "Jeep Wrangler JL"
            "bmw" -> "BMW E/F/G & MINI"
            else -> "Universal OBD-II"
        }
        AppLanguage.HEBREW -> when (profileId) {
            "auto" -> "זיהוי אוטומטי (VIN)"
            "jeep_jk" -> "ג'יפ רנגלר JK"
            "jeep_jl" -> "ג'יפ רנגלר JL"
            "bmw" -> "BMW סדרה E/F/G ו-MINI"
            else -> "OBD-II אוניברסלי"
        }
    }
    fun modelChipLabel(model: WranglerModel): String = when (language) {
        AppLanguage.ENGLISH -> "${model.displayName} (${model.years})"
        AppLanguage.HEBREW -> when (model) {
            WranglerModel.JK -> "רנגלר JK (2007–2018)"
            WranglerModel.JL -> "רנגלר JL (2018–היום)"
        }
    }

    fun engineChipLabel(profile: JkEngineProfile): String = when (language) {
        AppLanguage.ENGLISH -> when (profile) {
            JkEngineProfile.PENTASTAR_3_6 -> "3.6L Pentastar"
            JkEngineProfile.EGH_3_8 -> "3.8L V6"
            JkEngineProfile.CRD_2_8 -> "2.8L CRD"
            JkEngineProfile.JL_PENTASTAR_3_6_ETORQUE -> "3.6L Pentastar ESS/eTorque"
            JkEngineProfile.JL_TURBO_2_0 -> "2.0L Turbo"
            JkEngineProfile.JL_ECODIESEL_3_0 -> "3.0L EcoDiesel"
            JkEngineProfile.JL_PHEV_4XE -> "2.0T 4xe PHEV"
            JkEngineProfile.JL_HEMI_392 -> "6.4L 392 HEMI"
        }
        AppLanguage.HEBREW -> when (profile) {
            JkEngineProfile.PENTASTAR_3_6 -> "3.6L פנטסטאר"
            JkEngineProfile.EGH_3_8 -> "3.8L V6"
            JkEngineProfile.CRD_2_8 -> "2.8L דיזל CRD"
            JkEngineProfile.JL_PENTASTAR_3_6_ETORQUE -> "3.6L פנטסטאר ESS"
            JkEngineProfile.JL_TURBO_2_0 -> "2.0L טורבו"
            JkEngineProfile.JL_ECODIESEL_3_0 -> "3.0L אקו-דיזל"
            JkEngineProfile.JL_PHEV_4XE -> "2.0T 4xe היברידי"
            JkEngineProfile.JL_HEMI_392 -> "6.4L 392 HEMI"
        }
    }

    fun themeLabel(theme: AppVisualTheme): String = when (language) {
        AppLanguage.ENGLISH -> theme.label
        AppLanguage.HEBREW -> when (theme) {
            AppVisualTheme.NEON_CYAN_AMBER -> "ציאן ניאון וענבר"
            AppVisualTheme.RACING_RED -> "אדום מרוצים"
            AppVisualTheme.DEEP_GREEN -> "ירוק טקטי"
            AppVisualTheme.CLASSIC_WHITE -> "לבן קלאסי (יום)"
        }
    }

    fun nightModeLabel(mode: NightModeSetting): String = when (language) {
        AppLanguage.ENGLISH -> mode.label
        AppLanguage.HEBREW -> when (mode) {
            NightModeSetting.AUTO_SUN -> "שקיעה/זריחה אוטו'"
            NightModeSetting.MANUAL_ON -> "מצב לילה פעיל"
            NightModeSetting.MANUAL_OFF -> "כבוי"
        }
    }

    fun subsystemLabel(subsystem: JkSubsystem, model: WranglerModel? = null): String = when (language) {
        AppLanguage.ENGLISH -> when (subsystem) {
            JkSubsystem.TIPM_NETWORK -> when (model) {
                WranglerModel.JK -> "TIPM / CAN"
                WranglerModel.JL -> "BCM / SGW / CAN"
                null -> subsystem.label
            }
            else -> subsystem.label
        }
        AppLanguage.HEBREW -> when (subsystem) {
            JkSubsystem.ALL -> "הכל"
            JkSubsystem.POWERTRAIN -> "מנוע"
            JkSubsystem.TRANSMISSION -> "תיבת הילוכים"
            JkSubsystem.TIPM_NETWORK -> when (model) {
                WranglerModel.JK -> "TIPM / CAN"
                WranglerModel.JL -> "BCM / SGW / CAN"
                null -> "TIPM / BCM / CAN"
            }
            JkSubsystem.ABS_CHASSIS -> "ABS / 4WD"
            JkSubsystem.EVAP -> "EVAP"
        }
    }

    companion object {
        val English = AppStrings(
            language = AppLanguage.ENGLISH,
            linkError = "LINK ERROR",
            milOn = "MIL ON",
            readyToPairStatus = "Ready to pair ELM327 adapter",
            shiftLightsTitle = "SHIFT LIGHTS",
            redlineBadge = "REDLINE!",
            liveBadge = "LIVE",
            idleBadge = "IDLE",
            obdSpeedLabel = "OBD SPEED",
            speedUnitKph = "KPH",
            liveSpeedFormat = "LIVE KPH",
            gpsSpeedBadge = "GPS KPH",
            calSpeedBadge = "CAL KPH",
            fuelTitle = "FUEL",
            coolantTitle = "TEMP",
            throttleTitle = "THROTTLE (TPS)",
            engineRpmTitle = "ENGINE RPM",
            rpmUnit = "RPM",
            manifoldMapTitle = "MANIFOLD MAP",
            engineLoadTitle = "ENGINE LOAD",
            pitWallPeaksTitle = "PIT-WALL & SESSION PEAKS",
            resetPeaksCd = "Reset session peak telemetry",
            peakRpmLabel = "PEAK RPM",
            topSpeedLabelFormat = "TOP %s",
            batteryLabel = "BATTERY",
            intakeAirLabel = "INTAKE AIR",
            fuelTrimLabel = "STFT B1",
            vinPrefix = "VIN",
            cockpitEngineRpm = "Engine RPM",
            cockpitCoolantTemp = "Coolant Temp",
            cockpitBatteryAlt = "Battery / Alt",
            cockpitVehicleSpeed = "Vehicle Speed",
            cockpitIntakeAir = "Intake Air",
            cockpitEngineLoad = "Engine Load",
            miniThrottle = "THROTTLE",
            miniMap = "MAP",
            trailInclinometer = "TRAIL INCLINOMETER",
            liveGravityCompass = "Live device gravity & compass sensors",
            accelUnavailable = "Accelerometer unavailable on hardware",
            rollSuffix = "ROLL",
            pitchSuffix = "PITCH",
            trailHeading = "TRAIL HEADING",
            selectTestsTabFormat = "Select Tests (%d)",
            liveScanTabFormat = "Live Scan (%d)",
            filterPidsPlaceholder = "Filter %d OBD PIDs…",
            selectAllShort = "All",
            clearShort = "Clear",
            unitPrefix = "Unit: %s",
            testsMarkedFormat = "%d of %d tests marked",
            applyHint = "Press Apply to scan chosen PIDs in real time",
            applyCountFormat = "Apply (%d)",
            noTestsChosenTitle = "No OBD Diagnostic Tests Chosen",
            noTestsChosenBody = "Check the OBD parameters you want to monitor, then tap Apply to start real-time ECU scanning.",
            chooseTestsButton = "Choose OBD Tests",
            readyConnectObd = "READY • CONNECT OBD-II",
            scanPausedFormat = "SCAN PAUSED (%d PIDs)",
            realTimeScanFormat = "REAL-TIME SCAN • CYCLE #%d",
            chosenParamsFormat = "%d chosen parameters",
            connectShort = "Connect",
            pauseShort = "Pause",
            resumeShort = "Resume",
            editShort = "Edit",
            clearDialogTitle = "Clear MIL & Stored DTCs?",
            clearDialogBody = "Mode 04 resets the Check Engine Light, stored codes, and emissions readiness monitors on your Wrangler ECU. Note: 2018+ JL models require a 12+8 SGW bypass cable to clear codes.",
            sendMode04Clear = "Send Mode 04 Clear",
            cancel = "Cancel",
            saveSnapshotTitle = "Save ECU Snapshot",
            saveSnapshotBody = "Stores current MIL state, detected DTCs, coolant temp, and battery voltage to your local Garage log.",
            snapshotNoteLabel = "Trail / Service Note (optional)",
            snapshotNotePlaceholder = "e.g. Cold start after Rubicon trail",
            save = "Save",
            ecuScannerHeader = "ECU DIAGNOSTIC SCANNER",
            scannerConnectPrompt = "Connect ELM327 adapter to read live Mode 03/07 codes",
            scannerCleanPrompt = "No active or pending fault codes reported",
            scannerCountFormat = "%d Active • %d Pending • MIL %s",
            liveEcuFaultsHeader = "LIVE ECU FAULT CODES",
            activeMode03Badge = "ACTIVE (MODE 03)",
            pendingMode07Badge = "PENDING (MODE 07)",
            faultEncyclopediaHeader = "WRANGLER JK / JL FAULT ENCYCLOPEDIA",
            faultEncyclopediaFormat = "WRANGLER %s FAULT ENCYCLOPEDIA",
            allModelsChip = "All Models",
            jlSgwBannerTitle = "Wrangler JL Security Gateway (SGW)",
            jlSgwBannerBody = "Live PIDs and fault scanning (Mode 01/03/07/09) work via the stock OBD-II port. Clearing codes (Mode 04) requires a 12+8 SGW bypass cable or AutoAuth.",
            searchDtcPlaceholder = "Search code or symptom (e.g. P00B7, P0456, 4xe, misfire)",
            trailShopGuidance = "TRAIL & SHOP GUIDANCE",
            terminalHeader = "ELM327 AT / HEX TERMINAL",
            terminalActiveLink = "CAN-Bus ISO 15765-4 link active",
            terminalOffline = "Offline — Connect ELM327 to transmit commands",
            pairShort = "Pair",
            rawTxRxStream = "RAW TX / RX STREAM",
            supportedPidMonitor = "WRANGLER SUPPORTED PID MONITOR",
            supportedPidMonitorFormat = "WRANGLER %s SUPPORTED PID MONITOR",
            terminalInputPlaceholder = "Enter AT or PID (e.g. 010C)",
            languageCardHeader = "APP LANGUAGE",
            languageCardSubtitle = "Choose Hebrew (עברית) or English to update all text and layout",
            languageActiveSummary = "Active: English (Left-to-Right)",
            heroSubtitleFormat = "%s • %s Tires • %s Gears",
            powertrainHeader = "WRANGLER POWERTRAIN & BUILD",
            powertrainHeaderFormat = "WRANGLER %s POWERTRAIN & BUILD",
            wranglerModelLabel = "Wrangler Generation",
            rigNameLabel = "Rig Name",
            tireSizeLabel = "Tire Size",
            axleRatioLabel = "Axle Ratio",
            saveRigProfile = "Save Rig Profile",
            visualThemeHeader = "VISUAL THEME & NIGHT MODE",
            displayColorTheme = "Display Color Theme",
            nightOffRoadMode = "Night Off-Roading Mode",
            nightActiveBadge = "ACTIVE (RED)",
            daylightBadge = "DAYLIGHT",
            useGpsSun = "Use GPS Sun",
            nightBrightnessLabel = "Night Screen Brightness",
            solarUsingLocationFormat = "Local GPS Sun: Sunrise %s • Sunset %s",
            solarDefaultSunFormat = "Sun Schedule: Sunrise %s • Sunset %s (default)",
            gpsCalibrationHeader = "GPS SPEEDOMETER CALIBRATION",
            oversizedTiresWarningFormat = "Oversized tires detected (%s vs %d\" stock). Estimated offset: %s.",
            gpsSpeedLabel = "GPS SPEED",
            obdRawCalLabel = "OBD RAW / CAL",
            liveGpsOffsetFormat = "Live GPS offset: %+.1f KPH • Recommended factor: ×%.3f",
            enableGps = "Enable GPS",
            stopGps = "Stop GPS",
            startGps = "Start GPS",
            matchGps = "Match GPS",
            useTireRatioFormat = "Use %s Ratio",
            resetOneX = "Reset 1.00×",
            manualSpeedMultiplier = "Manual Speed Multiplier",
            unitsAlertsHeader = "UNITS & TRAIL ALERTS",
            metricOption = "Metric (KPH / °C / kPa)",
            imperialMapOption = "MAP PSI (KPH / °C)",
            coolantWarnLabel = "Coolant High-Temp Warning",
            batteryWarnLabel = "Low Battery Voltage Warning",
            resetInclinometerZero = "Reset Inclinometer Factory Zero",
            ecuDiagLogFormat = "ECU DIAGNOSTIC LOG (%d)",
            noSavedSnapshots = "No saved ECU snapshots yet",
            noSavedSnapshotsBody = "Use the DTC Scan tab to save an ECU fault & telemetry snapshot after connecting to your Jeep.",
            activeDtcPrefix = "Active: %s",
            pendingDtcPrefix = "Pending: %s",
            cleanScanZeroDtcs = "Clean Scan (0 DTCs)",
            btSheetTitle = "Pair Vgate iCar Pro / OBD-II",
            btSheetSubtitle = "Dual-mode SPP (ANDROID-VLINK) & BLE 4.0 (IOS-VLINK) — All OBD-II cars",
            btPermRequiredTitle = "Bluetooth Permissions Required",
            btPermRequiredBody = "Grant BLUETOOTH_CONNECT and BLUETOOTH_SCAN so the app can discover and link to your Vgate iCar Pro / ELM327 adapter.",
            grantAccess = "Grant Access",
            settingsButton = "Settings",
            btScanningStop = "Scanning… Tap to Stop",
            btScanNearby = "Scan Nearby Adapters",
            btPairedButton = "Paired",
            btDisabledWarning = "Bluetooth appears disabled in system settings. Enable Bluetooth to connect.",
            btNoAdaptersTitle = "No OBD-II adapters found yet",
            btNoAdaptersBody = "Plug your Vgate iCar Pro (ANDROID-VLINK / IOS-VLINK) or ELM327 dongle into the OBD-II port, turn ignition ON, and tap Scan.",
            vehicleProfileHeader = "VEHICLE AUTO-DETECTION & PROFILE",
            vehicleProfileSubtitle = "Auto-detects make, model & year via ATSP0 + 0902 / 22F190 / 0904, or force a manual profile.",
            detectedVehicleLabel = "Detected Vehicle",
            activeProtocolLabel = "Active Protocol",
            supportedPidsCountFormat = "ECU Bitmask: %d supported Mode 01 PIDs",
            cachedAdaptersHeader = "CACHED ADAPTER PROFILES",
            forgetCachedCd = "Forget cached vehicle binding",
            profileMonitorFormat = "%s • SUPPORTED PID MONITOR",
            uiStyleCardHeader = "GUI INTERFACE LAYOUT",
            uiStyleCardSubtitle = "Switch between 3 completely different cockpit architectures",
            uiStyleSheetTitle = "Choose GUI Experience",
            uiStyleSheetSubtitle = "Each option reshapes navigation, layout geometry, and live gauge presentation.",
            cdSwitchUiStyle = "Switch GUI layout style",
            uiStyleActiveBadge = "ACTIVE",
            hudNavHud = "HUD",
            hudNavPids = "PIDS",
            hudNavFaults = "FAULTS",
            hudNavTerm = "TERM",
            hudNavRig = "RIG",
            hudHorizonLabel = "ATTITUDE HORIZON",
            hudFlightNominal = "SYS NOMINAL",
            hudFlightAlert = "FAULT / MIL",
            hudLaserRpm = "RPM TAPE",
            hudOpticalVel = "OPTICAL VELOCITY",
            driveNavHome = "Home",
            driveNavRace = "Race",
            driveNavPids = "PIDs",
            driveNavFaults = "DTCs",
            driveNavTerm = "Term",
            driveNavSetup = "Setup",
            driveStyleShort = "GUI",
            driveThermalsTitle = "THERMALS",
            driveElectricalTitle = "POWER & FUEL",
            driveInclinometerTitle = "TRAIL TILT",
            driveHealthNominal = "ALL SYSTEMS READY",
            driveHealthAttention = "ECU ATTENTION",
            driveOpenRaceCluster = "Custom Race Gauges",
            driveOpenDiagnostics = "50-PID Monitor",
            driveOpenScanner = "Fault Scanner"
        )

        val Hebrew = AppStrings(
            language = AppLanguage.HEBREW,
            linkError = "שגיאת חיבור",
            milOn = "נורת מנוע",
            readyToPairStatus = "מוכן לצימוד מתאם ELM327",
            shiftLightsTitle = "נורות העברת הילוך",
            redlineBadge = "קו אדום!",
            liveBadge = "חי",
            idleBadge = "ממתין",
            obdSpeedLabel = "מהירות OBD",
            speedUnitKph = "קמ\"ש",
            liveSpeedFormat = "חי קמ\"ש",
            gpsSpeedBadge = "GPS קמ\"ש",
            calSpeedBadge = "מכויל קמ\"ש",
            fuelTitle = "דלק",
            coolantTitle = "חום",
            throttleTitle = "מצערת (TPS)",
            engineRpmTitle = "סל\"ד מנוע",
            rpmUnit = "סל\"ד",
            manifoldMapTitle = "לחץ סעפת MAP",
            engineLoadTitle = "עומס מנוע",
            pitWallPeaksTitle = "שיאי נסיעה ונתוני מנוע",
            resetPeaksCd = "אפס שיאי סשן",
            peakRpmLabel = "שיא סל\"ד",
            topSpeedLabelFormat = "שיא %s",
            batteryLabel = "מצבר",
            intakeAirLabel = "אוויר יניקה",
            fuelTrimLabel = "תיקון דלק",
            vinPrefix = "שלדה VIN",
            cockpitEngineRpm = "סל\"ד מנוע",
            cockpitCoolantTemp = "חום נוזל קירור",
            cockpitBatteryAlt = "מצבר / אלטרנטור",
            cockpitVehicleSpeed = "מהירות רכב",
            cockpitIntakeAir = "טמפ' יניקה",
            cockpitEngineLoad = "עומס מנוע",
            miniThrottle = "מצערת",
            miniMap = "סעפת",
            trailInclinometer = "מד שיפוע שטח",
            liveGravityCompass = "חיישני הטיה ומצפן בזמן אמת",
            accelUnavailable = "מד תאוצה לא זמין במכשיר",
            rollSuffix = "גלגול",
            pitchSuffix = "עילרוד",
            trailHeading = "כיוון מצפן",
            selectTestsTabFormat = "בחירת בדיקות (%d)",
            liveScanTabFormat = "סריקה חיה (%d)",
            filterPidsPlaceholder = "סינון %d חיישני OBD…",
            selectAllShort = "הכל",
            clearShort = "נקה",
            unitPrefix = "יחידה: %s",
            testsMarkedFormat = "סומנו %d מתוך %d בדיקות",
            applyHint = "לחץ החל לסריקת החיישנים בזמן אמת",
            applyCountFormat = "החל (%d)",
            noTestsChosenTitle = "לא נבחרו בדיקות OBD",
            noTestsChosenBody = "סמן את פרמטרי ה-OBD שברצונך לנטר, ולחץ החל להתחלת סריקה בזמן אמת.",
            chooseTestsButton = "בחר בדיקות OBD",
            readyConnectObd = "מוכן • חבר OBD-II",
            scanPausedFormat = "סריקה מושהית (%d חיישנים)",
            realTimeScanFormat = "סריקה חיה • מחזור #%d",
            chosenParamsFormat = "%d פרמטרים נבחרו",
            connectShort = "התחבר",
            pauseShort = "השהה",
            resumeShort = "המשך",
            editShort = "ערוך",
            clearDialogTitle = "לנקות נורת תקלה וקודי DTC?",
            clearDialogBody = "פקודת Mode 04 מאפסת את נורת Check Engine ומוני פליטה במחשב הרנגלר. בדגמי JL (2018+) נדרש כבל עוקף SGW 12+8 לניקוי תקלות.",
            sendMode04Clear = "שלח ניקוי Mode 04",
            cancel = "ביטול",
            saveSnapshotTitle = "שמירת צילום מצב ECU",
            saveSnapshotBody = "שומר את מצב נורת התקלה, קודים שזוהו, טמפ' קירור ומתח מצבר ליומן המוסך.",
            snapshotNoteLabel = "הערת שטח / טיפול (אופציונלי)",
            snapshotNotePlaceholder = "לדוגמה: התנעה קרה אחרי מסלול שטח",
            save = "שמור",
            ecuScannerHeader = "סורק תקלות ECU",
            scannerConnectPrompt = "חבר מתאם ELM327 לקריאת קודי Mode 03/07",
            scannerCleanPrompt = "לא דווחו קודי תקלה פעילים או ממתינים",
            scannerCountFormat = "%d פעילים • %d ממתינים • MIL %s",
            liveEcuFaultsHeader = "קודי תקלה פעילים מהמחשב",
            activeMode03Badge = "פעיל (MODE 03)",
            pendingMode07Badge = "ממתין (MODE 07)",
            faultEncyclopediaHeader = "אנציקלופדיית תקלות רנגלר JK / JL",
            faultEncyclopediaFormat = "אנציקלופדיית תקלות רנגלר %s",
            allModelsChip = "כל הדגמים",
            jlSgwBannerTitle = "שער אבטחה רנגלר JL (SGW)",
            jlSgwBannerBody = "קריאת חיישנים וסריקת תקלות (Mode 01/03/07/09) פועלת ישירות מהשקע. מחיקת תקלות (Mode 04) ב-JL דורשת כבל עוקף 12+8 SGW.",
            searchDtcPlaceholder = "חפש קוד או תסמין (למשל P00B7, P0456, 4xe, misfire)",
            trailShopGuidance = "הנחיות שטח ומוסך",
            terminalHeader = "מסוף פקודות ELM327 AT / HEX",
            terminalActiveLink = "חיבור CAN-Bus ISO 15765-4 פעיל",
            terminalOffline = "מנותק — חבר ELM327 לשליחת פקודות",
            pairShort = "צמד",
            rawTxRxStream = "זרם נתונים TX / RX",
            supportedPidMonitor = "מוניטור PID נתמכים לרנגלר",
            supportedPidMonitorFormat = "מוניטור PID נתמכים לרנגלר %s",
            terminalInputPlaceholder = "הזן פקודת AT או PID (למשל 010C)",
            languageCardHeader = "שפת האפליקציה (LANGUAGE)",
            languageCardSubtitle = "בחר עברית או אנגלית לעדכון מיידי של כל הממשק והכיוון",
            languageActiveSummary = "פעיל: עברית (מימין לשמאל)",
            heroSubtitleFormat = "%s • צמיגים %s • יחס %s",
            powertrainHeader = "מערכת הנעה ופרופיל רנגלר",
            powertrainHeaderFormat = "מערכת הנעה ופרופיל רנגלר %s",
            wranglerModelLabel = "דגם רנגלר (JK / JL)",
            rigNameLabel = "שם הרכב",
            tireSizeLabel = "מידת צמיגים",
            axleRatioLabel = "יחס העברה בסרן",
            saveRigProfile = "שמור פרופיל רכב",
            visualThemeHeader = "ערכת צבעים ומצב לילה",
            displayColorTheme = "ערכת צבעי תצוגה",
            nightOffRoadMode = "מצב לילה לשטח",
            nightActiveBadge = "פעיל (אדום)",
            daylightBadge = "מצב יום",
            useGpsSun = "שמש לפי GPS",
            nightBrightnessLabel = "בהירות מסך בלילה",
            solarUsingLocationFormat = "שמש GPS מקומית: זריחה %s • שקיעה %s",
            solarDefaultSunFormat = "לוח זריחה/שקיעה: זריחה %s • שקיעה %s (ברירת מחדל)",
            gpsCalibrationHeader = "כיול מד מהירות מבוסס GPS",
            oversizedTiresWarningFormat = "זוהו צמיגים מוגדלים (%s לעומת %d\" מקורי). פיצוי משוער: %s.",
            gpsSpeedLabel = "מהירות GPS",
            obdRawCalLabel = "OBD גולמי / מכויל",
            liveGpsOffsetFormat = "סטייה חיה מ-GPS: %+.1f קמ\"ש • מקדם מומלץ: ×%.3f",
            enableGps = "הפעל GPS",
            stopGps = "עצור GPS",
            startGps = "התחל GPS",
            matchGps = "התאם ל-GPS",
            useTireRatioFormat = "השתמש ביחס %s",
            resetOneX = "אפס 1.00×",
            manualSpeedMultiplier = "מכפיל מהירות ידני",
            unitsAlertsHeader = "יחידות והתראות שטח",
            metricOption = "מטרי (קמ\"ש / °C / kPa)",
            imperialMapOption = "לחץ סעפת PSI (קמ\"ש / °C)",
            coolantWarnLabel = "התראת חום מנוע גבוה",
            batteryWarnLabel = "התראת מתח מצבר נמוך",
            resetInclinometerZero = "אפס כיול מד שיפוע",
            ecuDiagLogFormat = "יומן אבחון ECU (%d)",
            noSavedSnapshots = "אין עדיין צילומי מצב שמורים",
            noSavedSnapshotsBody = "השתמש בלשונית קודי תקלה כדי לשמור צילום מצב של תקלות ונתונים מהג'יפ.",
            activeDtcPrefix = "פעיל: %s",
            pendingDtcPrefix = "ממתין: %s",
            cleanScanZeroDtcs = "סריקה נקייה (0 תקלות)",
            btSheetTitle = "צימוד Vgate iCar Pro / מתאם OBD-II",
            btSheetSubtitle = "תומך SPP (ANDROID-VLINK) ו-BLE 4.0 (IOS-VLINK) — לכל רכב OBD-II",
            btPermRequiredTitle = "נדרשות הרשאות בלוטות'",
            btPermRequiredBody = "אשר BLUETOOTH_CONNECT ו-BLUETOOTH_SCAN כדי לאתר ולהתחבר למתאם Vgate iCar Pro / ELM327.",
            grantAccess = "אשר גישה",
            settingsButton = "הגדרות",
            btScanningStop = "סורק… הקש לעצירה",
            btScanNearby = "סרוק מתאמים קרובים",
            btPairedButton = "מצומדים",
            btDisabledWarning = "נראה שהבלוטות' כבוי בהגדרות המכשיר. הפעל בלוטות' להתחברות.",
            btNoAdaptersTitle = "לא נמצאו עדיין מתאמי OBD-II",
            btNoAdaptersBody = "חבר את מתאם ה-Vgate iCar Pro (ANDROID-VLINK / IOS-VLINK) או ELM327 לשקע ה-OBD-II, סובב סוויץ' ל-ON והקש סרוק.",
            vehicleProfileHeader = "זיהוי רכב אוטומטי ופרופיל ECU",
            vehicleProfileSubtitle = "מזהה יצרן, דגם ושנה אוטומטית (ATSP0 + 0902 / 22F190 / 0904), או בחר פרופיל ידני.",
            detectedVehicleLabel = "רכב שזוהה",
            activeProtocolLabel = "פרוטוקול פעיל",
            supportedPidsCountFormat = "מיפוי ECU: %d חיישני Mode 01 נתמכים",
            cachedAdaptersHeader = "מתאמים שמורים במטמון",
            forgetCachedCd = "מחק פרופיל רכב שמור",
            profileMonitorFormat = "%s • מוניטור חיישנים נתמכים",
            uiStyleCardHeader = "סגנון ממשק ועיצוב GUI",
            uiStyleCardSubtitle = "בחר בין 3 ממשקים שונים לחלוטין לתא הנהג",
            uiStyleSheetTitle = "בחר סגנון ממשק (GUI)",
            uiStyleSheetSubtitle = "כל אפשרות משנה לחלוטין את הניווט, המבנה הגרפי ותצוגת השעונים.",
            cdSwitchUiStyle = "החלף סגנון ממשק",
            uiStyleActiveBadge = "פעיל",
            hudNavHud = "HUD",
            hudNavPids = "חיישנים",
            hudNavFaults = "תקלות",
            hudNavTerm = "מסוף",
            hudNavRig = "הגדרות",
            hudHorizonLabel = "אופק מלאכותי",
            hudFlightNominal = "מערכות תקינות",
            hudFlightAlert = "התראה / MIL",
            hudLaserRpm = "סל\"ד לייזר",
            hudOpticalVel = "מהירות אופטית",
            driveNavHome = "ראשי",
            driveNavRace = "מרוץ",
            driveNavPids = "חיישנים",
            driveNavFaults = "תקלות",
            driveNavTerm = "מסוף",
            driveNavSetup = "הגדרות",
            driveStyleShort = "עיצוב",
            driveThermalsTitle = "טמפרטורות",
            driveElectricalTitle = "חשמל ודלק",
            driveInclinometerTitle = "שיפוע שטח",
            driveHealthNominal = "מערכות תקינות",
            driveHealthAttention = "נדרשת בדיקת ECU",
            driveOpenRaceCluster = "שעוני מרוץ",
            driveOpenDiagnostics = "סריקת 50 PID",
            driveOpenScanner = "סורק תקלות"
        )

        fun forLanguage(language: AppLanguage): AppStrings = when (language) {
            AppLanguage.ENGLISH -> English
            AppLanguage.HEBREW -> Hebrew
        }
    }
}

val LocalAppStrings = staticCompositionLocalOf { AppStrings.English }
