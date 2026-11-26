package com.example.obd

enum class WranglerModel(
    val code: String,
    val displayName: String,
    val years: String,
    val defaultNickname: String,
    val defaultStockTireInches: Float
) {
    JK("JK", "Wrangler JK", "2007–2018", "Wrangler JK", 32.0f),
    JL("JL", "Wrangler JL", "2018–Present", "Wrangler JL", 33.0f)
}

enum class JkEngineProfile(
    val label: String,
    val years: String,
    val displacement: String,
    val model: WranglerModel,
    val redlineRpm: Float,
    val maxRpmScale: Float
) {
    // Wrangler JK (2007–2018)
    PENTASTAR_3_6("3.6L Pentastar V6", "2012–2018 JK", "3604 cc DOHC", WranglerModel.JK, 6400f, 7000f),
    EGH_3_8("3.8L EGH V6", "2007–2011 JK", "3778 cc OHV", WranglerModel.JK, 5500f, 6000f),
    CRD_2_8("2.8L VM CRD Diesel", "2007–2018 Export", "2777 cc I4 Turbo", WranglerModel.JK, 4200f, 5000f),

    // Wrangler JL (2018–Present)
    JL_PENTASTAR_3_6_ETORQUE("3.6L Pentastar ESS/eTorque", "2018+ JL", "3604 cc V6", WranglerModel.JL, 6400f, 7000f),
    JL_TURBO_2_0("2.0L Hurricane Turbo I4", "2018+ JL", "1995 cc GDI Turbo", WranglerModel.JL, 6000f, 7000f),
    JL_ECODIESEL_3_0("3.0L EcoDiesel V6", "2020–2023 JL", "2987 cc V6 Diesel", WranglerModel.JL, 4400f, 5000f),
    JL_PHEV_4XE("2.0L Turbo 4xe PHEV", "2021+ JL", "1995 cc Turbo + HV", WranglerModel.JL, 6000f, 7000f),
    JL_HEMI_392("6.4L 392 HEMI V8", "2021+ JL", "6417 cc V8", WranglerModel.JL, 6400f, 7000f);

    companion object {
        fun forModel(model: WranglerModel): List<JkEngineProfile> =
            entries.filter { it.model == model }

        fun defaultForModel(model: WranglerModel): JkEngineProfile = when (model) {
            WranglerModel.JK -> PENTASTAR_3_6
            WranglerModel.JL -> JL_PENTASTAR_3_6_ETORQUE
        }
    }
}

enum class UnitSystem(val label: String) {
    METRIC("Metric (°C / KPH / kPa)"),
    IMPERIAL("Imperial MAP (PSI / °C / KPH)")
}

enum class AppVisualTheme(val label: String, val subtitle: String) {
    NEON_CYAN_AMBER("Neon Cyan & Amber", "Signature trail cyan + amber"),
    RACING_RED("Racing Red", "Track crimson + hot orange"),
    DEEP_GREEN("Deep Green", "Tactical forest emerald + lime"),
    CLASSIC_WHITE("Classic White", "Crisp motorsport dial white")
}

enum class AppUiStyle(val id: String) {
    TACTICAL_MOTORSPORT("tactical_motorsport"),
    AVIONICS_HUD("avionics_hud"),
    AUTOMOTIVE_INFOTAINMENT("automotive_infotainment")
}

enum class NightModeSetting(val label: String) {
    AUTO_SUN("Auto Sun"),
    MANUAL_ON("Night On"),
    MANUAL_OFF("Off")
}

enum class AppLanguage(val code: String, val nativeLabel: String) {
    ENGLISH("en", "English"),
    HEBREW("iw", "עברית")
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    INITIALIZING,
    CONNECTED,
    ERROR
}

enum class TransportType(val badge: String) {
    CLASSIC_SPP("SPP / VLINK"),
    BLE_GATT("BLE 4.0")
}

data class BluetoothDeviceItem(
    val name: String,
    val address: String,
    val isPaired: Boolean,
    val transportType: TransportType,
    val rssi: Int? = null
) {
    val isVgateAdapter: Boolean
        get() {
            val u = name.uppercase()
            return u.contains("VLINK") || u.contains("VGATE") || u.contains("ICAR")
        }
}

data class LiveTelemetry(
    val rpm: Int? = null,
    val speedKmh: Int? = null,
    val coolantCelsius: Int? = null,
    val intakeAirCelsius: Int? = null,
    val ambientAirCelsius: Int? = null,
    val engineOilTempCelsius: Int? = null,
    val throttlePercent: Float? = null,
    val engineLoadPercent: Float? = null,
    val manifoldKpa: Int? = null,
    val barometricKpa: Int? = null,
    val fuelRailPressureKpa: Int? = null,
    val boostPressureKpa: Int? = null,
    val hybridBatteryRemainingPct: Float? = null,
    val fuelTypeName: String? = null,
    val shortTermFuelTrimB1: Float? = null,
    val longTermFuelTrimB1: Float? = null,
    val fuelLevelPercent: Float? = null,
    val batteryVolts: Float? = null,
    val milOn: Boolean = false,
    val ecuDtcCount: Int = 0,
    val lastUpdatedMs: Long = 0L
)

data class VinInfo(
    val vin: String,
    val modelYear: String,
    val isWranglerJk: Boolean,
    val plantSummary: String,
    val detectedModel: WranglerModel? = if (isWranglerJk) WranglerModel.JK else null,
    val engineHint: String? = null
)

enum class DtcSeverity(val label: String) {
    CRITICAL("Critical"),
    WARNING("Warning"),
    INFO("Advisory")
}

enum class JkSubsystem(val label: String) {
    ALL("All"),
    POWERTRAIN("Engine"),
    TRANSMISSION("Trans"),
    TIPM_NETWORK("TIPM / BCM / CAN"),
    ABS_CHASSIS("ABS / 4WD"),
    EVAP("EVAP")
}

data class DtcCodeInfo(
    val code: String,
    val title: String,
    val subsystem: JkSubsystem,
    val severity: DtcSeverity,
    val affectedEngines: String,
    val description: String,
    val trailGuidance: String,
    val models: Set<WranglerModel> = setOf(WranglerModel.JK, WranglerModel.JL)
)

data class ObdPidDefinition(
    val pidHex: String,
    val name: String,
    val jkNote: String,
    val unitImperial: String,
    val unitMetric: String,
    val models: Set<WranglerModel> = setOf(WranglerModel.JK, WranglerModel.JL)
)

data class TerminalEntry(
    val id: Long,
    val timestamp: String,
    val direction: TerminalDirection,
    val message: String
)

enum class TerminalDirection {
    TX,
    RX,
    INFO,
    ERROR
}

data class TrailSensorState(
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val azimuthDegrees: Float = 0f,
    val isSensorAvailable: Boolean = true,
    val hasCompassSensor: Boolean = false
)

data class RigPreferences(
    val wranglerModel: WranglerModel = WranglerModel.JK,
    val rigNickname: String = "Wrangler JK",
    val engineProfile: JkEngineProfile = JkEngineProfile.PENTASTAR_3_6,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val tireSizeInches: String = "35\"",
    val stockTireInches: Float = 32.0f,
    val axleGearRatio: String = "4.10",
    val coolantWarnCelsius: Int = 108,
    val lowBatteryVolts: Float = 11.8f,
    val pitchTareDeg: Float = 0f,
    val rollTareDeg: Float = 0f,
    val raceLayoutSpec: String = "",
    val raceLayoutLocked: Boolean = true,
    val visualTheme: AppVisualTheme = AppVisualTheme.CLASSIC_WHITE,
    val lastDarkTheme: AppVisualTheme = AppVisualTheme.NEON_CYAN_AMBER,
    val nightModeSetting: NightModeSetting = NightModeSetting.MANUAL_OFF,
    val nightBrightnessPercent: Int = 20,
    val speedCalibrationFactor: Float = 1.0f,
    val selectedDiagnosticTestsCsv: String = "",
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val selectedProfileOverrideId: String = VehicleProfileId.AUTO,
    val uiStyle: AppUiStyle = AppUiStyle.TACTICAL_MOTORSPORT
)

fun parseTireSizeInches(raw: String): Float? {
    val match = Regex("""(\d+(?:\.\d+)?)""").find(raw)
    return match?.groupValues?.getOrNull(1)?.toFloatOrNull()
}

fun applyCalibratedSpeedKph(rawSpeedKmh: Int?, factor: Float): Int? {
    if (rawSpeedKmh == null) return null
    return kotlin.math.round(rawSpeedKmh * factor.coerceIn(0.70f, 1.45f)).toInt()
}

enum class RealSpeedSource {
    NONE,
    OBD_LIVE,
    OBD_CALIBRATED,
    GPS_LIVE
}

data class RealSpeedReading(
    val kph: Int?,
    val source: RealSpeedSource,
    val factorApplied: Float = 1.0f
)

/**
 * Resolves the active speed correction factor:
 * 1) Explicit manual / GPS-matched calibration factor stored in RigPreferences
 * 2) Automatic oversized-tire diameter ratio when running Wrangler JK/JL profiles with modified tires
 * 3) Default 1.0f for stock vehicles or non-Wrangler profiles
 */
fun effectiveSpeedFactor(
    rigPrefs: RigPreferences,
    activeProfile: VehicleProfile? = null
): Float {
    if (kotlin.math.abs(rigPrefs.speedCalibrationFactor - 1.0f) >= 0.005f) {
        return rigPrefs.speedCalibrationFactor.coerceIn(0.70f, 1.45f)
    }
    val isWranglerProfile = activeProfile == null ||
        activeProfile.id == VehicleProfileId.JEEP_JK ||
        activeProfile.id == VehicleProfileId.JEEP_JL
    if (isWranglerProfile) {
        val actualTire = parseTireSizeInches(rigPrefs.tireSizeInches)
        if (actualTire != null &&
            rigPrefs.stockTireInches > 0f &&
            kotlin.math.abs(actualTire - rigPrefs.stockTireInches) >= 0.5f
        ) {
            return (actualTire / rigPrefs.stockTireInches).coerceIn(0.70f, 1.45f)
        }
    }
    return 1.0f
}

/**
 * Computes true vehicle road speed in KM/h combining OBD-II PID 010D, tire/axle ratio calculation,
 * and live GPS velocity fallback/verification.
 */
fun resolveRealSpeed(
    obdRawSpeedKmh: Int?,
    gpsSpeedKph: Float?,
    gpsAccuracyMeters: Float?,
    rigPrefs: RigPreferences,
    activeProfile: VehicleProfile? = null
): RealSpeedReading {
    val factor = effectiveSpeedFactor(rigPrefs, activeProfile)
    val gpsAccurate = gpsSpeedKph != null && (gpsAccuracyMeters == null || gpsAccuracyMeters <= 25f)

    if (obdRawSpeedKmh != null) {
        // Guard against stuck/zeroed VSS (e.g., aftermarket trans/t-case sensor failure while driving)
        if (obdRawSpeedKmh == 0 && gpsAccurate && gpsSpeedKph!! >= 8f) {
            return RealSpeedReading(
                kph = kotlin.math.round(gpsSpeedKph).toInt(),
                source = RealSpeedSource.GPS_LIVE,
                factorApplied = factor
            )
        }
        val calKph = applyCalibratedSpeedKph(obdRawSpeedKmh, factor)!!
        val hasExplicitCalibration = kotlin.math.abs(rigPrefs.speedCalibrationFactor - 1.0f) >= 0.005f
        // If driving >= 10 KPH with strong GPS lock and no explicit manual multiplier overrides,
        // verify OBD speed against real GPS ground speed and correct re-gear/tire discrepancies > 4%.
        if (!hasExplicitCalibration &&
            gpsSpeedKph != null &&
            gpsSpeedKph >= 10f &&
            obdRawSpeedKmh >= 10 &&
            (gpsAccuracyMeters == null || gpsAccuracyMeters <= 20f)
        ) {
            val driftRatio = kotlin.math.abs(gpsSpeedKph - calKph) / gpsSpeedKph.coerceAtLeast(1f)
            if (driftRatio > 0.04f) {
                val liveFactor = (gpsSpeedKph / obdRawSpeedKmh.toFloat()).coerceIn(0.70f, 1.45f)
                return RealSpeedReading(
                    kph = kotlin.math.round(gpsSpeedKph).toInt(),
                    source = RealSpeedSource.GPS_LIVE,
                    factorApplied = liveFactor
                )
            }
        }
        val source = if (kotlin.math.abs(factor - 1.0f) >= 0.005f) {
            RealSpeedSource.OBD_CALIBRATED
        } else {
            RealSpeedSource.OBD_LIVE
        }
        return RealSpeedReading(
            kph = calKph,
            source = source,
            factorApplied = factor
        )
    }

    if (gpsSpeedKph != null) {
        val cleanGps = if (gpsSpeedKph < 2.5f) 0 else kotlin.math.round(gpsSpeedKph).toInt()
        return RealSpeedReading(
            kph = cleanGps,
            source = RealSpeedSource.GPS_LIVE,
            factorApplied = factor
        )
    }

    return RealSpeedReading(
        kph = null,
        source = RealSpeedSource.NONE,
        factorApplied = factor
    )
}

