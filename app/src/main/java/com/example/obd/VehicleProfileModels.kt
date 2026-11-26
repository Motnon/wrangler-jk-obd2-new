package com.example.obd

object VehicleProfileId {
    const val AUTO = "auto"
    const val JEEP_JK = "jeep_jk"
    const val JEEP_JL = "jeep_jl"
    const val BMW = "bmw"
    const val UNIVERSAL_OBD2 = "universal_obd2"

    val allManualIds: List<String> = listOf(AUTO, JEEP_JK, JEEP_JL, BMW, UNIVERSAL_OBD2)
}

enum class DetectionSource(val badge: String) {
    VIN_STANDARD_0902("VIN 0902"),
    VIN_UDS_22F190("UDS 22F190"),
    ECU_FINGERPRINT("ECU ID"),
    UNIVERSAL_FALLBACK("Universal"),
    CACHED_DEVICE("Cached"),
    MANUAL_OVERRIDE("Manual")
}

data class VehicleProfilePid(
    val cmd: String,
    val name: String,
    val note: String,
    val unitMetric: String,
    val unitImperial: String,
    val mode: String = "01",
    val isOptional: Boolean = false
)

data class VehicleProfile(
    val id: String,
    val name: String,
    val make: String,
    val shortBadge: String,
    val redlineRpm: Float,
    val maxRpmScale: Float,
    val coolantWarnC: Int,
    val lowBatteryVolts: Float,
    val useSupportedPidMaskFiltering: Boolean,
    val hasSecurityGateway: Boolean,
    val preferredProtocol: String,
    val pids: List<VehicleProfilePid>
)

data class DetectedVehicle(
    val vin: String? = null,
    val make: String,
    val model: String,
    val modelYear: String? = null,
    val engineHint: String? = null,
    val profileId: String,
    val displayTitle: String,
    val protocolCode: String? = null,
    val protocolName: String? = null,
    val calibrationId: String? = null,
    val supportedMode01Pids: Set<Int> = emptySet(),
    val detectionSource: DetectionSource = DetectionSource.UNIVERSAL_FALLBACK,
    val plantSummary: String? = null,
    val detectedWranglerModel: WranglerModel? = null
)

data class CachedVehicleBinding(
    val deviceAddress: String,
    val deviceName: String,
    val vin: String? = null,
    val profileId: String,
    val displayTitle: String,
    val protocolName: String? = null,
    val lastConnectedMs: Long = System.currentTimeMillis()
)
