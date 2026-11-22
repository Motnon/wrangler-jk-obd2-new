package com.example.obd

enum class JkEngineProfile(val label: String, val years: String, val displacement: String) {
    PENTASTAR_3_6("3.6L Pentastar V6", "2012–2018 JK", "3604 cc DOHC"),
    EGH_3_8("3.8L EGH V6", "2007–2011 JK", "3778 cc OHV"),
    CRD_2_8("2.8L VM CRD Diesel", "2007–2018 Export", "2777 cc I4 Turbo")
}

enum class UnitSystem(val label: String) {
    IMPERIAL("Imperial (°F / MPH / PSI)"),
    METRIC("Metric (°C / KM/H / kPa)")
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    INITIALIZING,
    CONNECTED,
    ERROR
}

enum class TransportType(val badge: String) {
    CLASSIC_SPP("BT SPP"),
    BLE_GATT("BLE")
}

data class BluetoothDeviceItem(
    val name: String,
    val address: String,
    val isPaired: Boolean,
    val transportType: TransportType,
    val rssi: Int? = null
)

data class LiveTelemetry(
    val rpm: Int? = null,
    val speedKmh: Int? = null,
    val coolantCelsius: Int? = null,
    val intakeAirCelsius: Int? = null,
    val ambientAirCelsius: Int? = null,
    val throttlePercent: Float? = null,
    val engineLoadPercent: Float? = null,
    val manifoldKpa: Int? = null,
    val shortTermFuelTrimB1: Float? = null,
    val longTermFuelTrimB1: Float? = null,
    val batteryVolts: Float? = null,
    val milOn: Boolean = false,
    val ecuDtcCount: Int = 0,
    val lastUpdatedMs: Long = 0L
)

data class VinInfo(
    val vin: String,
    val modelYear: String,
    val isWranglerJk: Boolean,
    val plantSummary: String
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
    TIPM_NETWORK("TIPM / CAN"),
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
    val trailGuidance: String
)

data class ObdPidDefinition(
    val pidHex: String,
    val name: String,
    val jkNote: String,
    val unitImperial: String,
    val unitMetric: String
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
    val rigNickname: String = "Wrangler JK",
    val engineProfile: JkEngineProfile = JkEngineProfile.PENTASTAR_3_6,
    val unitSystem: UnitSystem = UnitSystem.IMPERIAL,
    val tireSizeInches: String = "35\"",
    val axleGearRatio: String = "4.10",
    val coolantWarnCelsius: Int = 108, // ~226 F
    val lowBatteryVolts: Float = 11.8f,
    val pitchTareDeg: Float = 0f,
    val rollTareDeg: Float = 0f
)
