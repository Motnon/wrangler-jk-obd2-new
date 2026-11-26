package com.example.obd

import android.content.Context
import com.example.obd.SimpleJson.arr
import com.example.obd.SimpleJson.bool
import com.example.obd.SimpleJson.num
import com.example.obd.SimpleJson.str

/**
 * Loads modular vehicle diagnostic profiles from the assets/profiles directory
 * and filters Universal/Bitmask profiles using the ECU's live 0100/0120/0140/0160 bitmask.
 */
class VehicleProfileLoader(private val context: Context? = null) {

    private val profileCache = linkedMapOf<String, VehicleProfile>()

    fun availableProfiles(): List<VehicleProfile> {
        return listOf(
            loadProfile(VehicleProfileId.JEEP_JK),
            loadProfile(VehicleProfileId.JEEP_JL),
            loadProfile(VehicleProfileId.BMW),
            loadProfile(VehicleProfileId.UNIVERSAL_OBD2)
        )
    }

    fun loadProfile(profileId: String): VehicleProfile {
        val normalized = when (profileId) {
            VehicleProfileId.JEEP_JK,
            VehicleProfileId.JEEP_JL,
            VehicleProfileId.BMW,
            VehicleProfileId.UNIVERSAL_OBD2 -> profileId
            else -> VehicleProfileId.UNIVERSAL_OBD2
        }
        profileCache[normalized]?.let { return it }

        val raw = VinDecoder.loadAssetText(context, "profiles/$normalized.json")
        val parsed = if (raw != null) parseProfileJson(raw, normalized) else fallbackProfile(normalized)
        profileCache[normalized] = parsed
        return parsed
    }

    /**
     * Filters PIDs for the active profile using the ECU's live Mode 01 supported PID bitmask
     * (`0100`/`0120`/`0140`/`0160`) when bitmask filtering is enabled so unsupported sensors are never polled.
     */
    fun resolveActivePids(profile: VehicleProfile, supportedMode01Pids: Set<Int>): List<VehicleProfilePid> {
        if (!profile.useSupportedPidMaskFiltering || supportedMode01Pids.isEmpty()) {
            return profile.pids
        }
        return profile.pids.filter { pid ->
            if (!pid.cmd.startsWith("01") || pid.cmd.length < 4) {
                true // Keep ATRV / non-Mode-01 commands
            } else {
                val pidNum = pid.cmd.substring(2, 4).toIntOrNull(16) ?: return@filter true
                pidNum in supportedMode01Pids
            }
        }
    }

    private fun parseProfileJson(raw: String, expectedId: String): VehicleProfile {
        val root = SimpleJson.parseObject(raw)
        val pidList = root.arr("pids").mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            val m = item as? Map<String, Any?> ?: return@mapNotNull null
            val cmd = m.str("cmd").uppercase()
            if (cmd.isBlank()) return@mapNotNull null
            VehicleProfilePid(
                cmd = cmd,
                name = m.str("name", cmd),
                note = m.str("note", ""),
                unitMetric = m.str("unitMetric", ""),
                unitImperial = m.str("unitImperial", ""),
                mode = m.str("mode", "01"),
                isOptional = m.bool("isOptional", false)
            )
        }
        return VehicleProfile(
            id = root.str("id", expectedId),
            name = root.str("name", expectedId),
            make = root.str("make", "Generic"),
            shortBadge = root.str("shortBadge", "OBD-II"),
            redlineRpm = root.num("redlineRpm", 6500.0).toFloat(),
            maxRpmScale = root.num("maxRpmScale", 7500.0).toFloat(),
            coolantWarnC = root.num("coolantWarnC", 108.0).toInt(),
            lowBatteryVolts = root.num("lowBatteryVolts", 11.8).toFloat(),
            useSupportedPidMaskFiltering = root.bool("useSupportedPidMaskFiltering", expectedId == VehicleProfileId.UNIVERSAL_OBD2),
            hasSecurityGateway = root.bool("hasSecurityGateway", expectedId == VehicleProfileId.JEEP_JL),
            preferredProtocol = root.str("preferredProtocol", "AUTO"),
            pids = pidList
        )
    }

    private fun fallbackProfile(id: String): VehicleProfile = when (id) {
        VehicleProfileId.JEEP_JK -> VehicleProfile(
            id = VehicleProfileId.JEEP_JK,
            name = "Jeep Wrangler JK (2007–2018)",
            make = "Jeep",
            shortBadge = "JK",
            redlineRpm = 6400f,
            maxRpmScale = 7000f,
            coolantWarnC = 108,
            lowBatteryVolts = 11.8f,
            useSupportedPidMaskFiltering = false,
            hasSecurityGateway = false,
            preferredProtocol = "AUTO",
            pids = emptyList()
        )
        VehicleProfileId.JEEP_JL -> VehicleProfile(
            id = VehicleProfileId.JEEP_JL,
            name = "Jeep Wrangler JL (2018+)",
            make = "Jeep",
            shortBadge = "JL",
            redlineRpm = 6400f,
            maxRpmScale = 7000f,
            coolantWarnC = 108,
            lowBatteryVolts = 11.8f,
            useSupportedPidMaskFiltering = false,
            hasSecurityGateway = true,
            preferredProtocol = "AUTO",
            pids = emptyList()
        )
        VehicleProfileId.BMW -> VehicleProfile(
            id = VehicleProfileId.BMW,
            name = "BMW E / F / G Series & MINI",
            make = "BMW",
            shortBadge = "BMW",
            redlineRpm = 6800f,
            maxRpmScale = 7500f,
            coolantWarnC = 112,
            lowBatteryVolts = 11.9f,
            useSupportedPidMaskFiltering = true,
            hasSecurityGateway = false,
            preferredProtocol = "AUTO",
            pids = emptyList()
        )
        else -> VehicleProfile(
            id = VehicleProfileId.UNIVERSAL_OBD2,
            name = "Universal OBD-II (Auto-Bitmask)",
            make = "Generic",
            shortBadge = "OBD-II",
            redlineRpm = 6500f,
            maxRpmScale = 7500f,
            coolantWarnC = 108,
            lowBatteryVolts = 11.8f,
            useSupportedPidMaskFiltering = true,
            hasSecurityGateway = false,
            preferredProtocol = "AUTO",
            pids = emptyList()
        )
    }
}
