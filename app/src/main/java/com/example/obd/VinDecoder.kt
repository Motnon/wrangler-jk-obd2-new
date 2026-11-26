package com.example.obd

import android.content.Context
import com.example.obd.SimpleJson.obj
import com.example.obd.SimpleJson.str
import java.io.File

/**
 * Offline VIN decoder and ECU fingerprint classifier backed by assets/vin_decoder.json.
 * Extracts Make (WMI 1..3), Model/Platform/Engine (VDS 4..8), and Model Year (pos 10).
 */
class VinDecoder(context: Context? = null) {

    data class WmiEntry(
        val make: String,
        val country: String,
        val defaultProfileId: String
    )

    private val wmiMap: Map<String, WmiEntry>
    private val yearMap: Map<Char, String>
    private val bmwVdsModels: Map<String, String>
    private val vagVdsModels: Map<String, String>
    private val genericVdsModels: Map<String, String>

    init {
        val rawJson = loadAssetText(context, "vin_decoder.json")
        val root = if (rawJson != null) SimpleJson.parseObject(rawJson) else emptyMap()

        val wmis = linkedMapOf<String, WmiEntry>()
        root.obj("wmis")?.forEach { (wmi, raw) ->
            @Suppress("UNCHECKED_CAST")
            val entry = raw as? Map<String, Any?> ?: return@forEach
            wmis[wmi.uppercase()] = WmiEntry(
                make = entry.str("make", "Generic"),
                country = entry.str("country", ""),
                defaultProfileId = entry.str("defaultProfileId", VehicleProfileId.UNIVERSAL_OBD2)
            )
        }
        wmiMap = wmis

        val years = linkedMapOf<Char, String>()
        root.obj("yearCodes")?.forEach { (k, v) ->
            val ch = k.firstOrNull()?.uppercaseChar()
            val yr = v as? String
            if (ch != null && yr != null) years[ch] = yr
        }
        yearMap = years

        bmwVdsModels = parseStringMap(root.obj("bmwVdsModels"))
        vagVdsModels = parseStringMap(root.obj("vagVdsModels"))
        genericVdsModels = parseStringMap(root.obj("genericVdsModels"))
    }

    fun decode(
        rawVin: String,
        protocolCode: String? = null,
        protocolName: String? = null,
        calibrationId: String? = null,
        supportedPids: Set<Int> = emptySet(),
        source: DetectionSource = DetectionSource.VIN_STANDARD_0902
    ): DetectedVehicle? {
        val vin = rawVin.trim().uppercase().filter { it in '0'..'9' || it in 'A'..'Z' }
        if (vin.length != 17) return null

        val wmi = vin.substring(0, 3)
        val vds = vin.substring(3, 8)
        val yearChar = vin[9]
        val modelYear = yearMap[yearChar] ?: "Year $yearChar"

        val wmiInfo = wmiMap[wmi]
        val make = wmiInfo?.make ?: inferMakeFallback(wmi)

        var profileId = wmiInfo?.defaultProfileId ?: VehicleProfileId.UNIVERSAL_OBD2
        var model = "OBD-II Vehicle"
        var engineHint: String? = null
        var wranglerModel: WranglerModel? = null
        var plantSummary = wmiInfo?.country?.takeIf { it.isNotBlank() } ?: "WMI $wmi"

        when {
            wmi in setOf("1C4", "1J4", "1J8") -> {
                val isJl = vds.getOrNull(2) == 'X' ||
                    yearChar in listOf('K', 'L', 'M', 'N', 'P', 'R', 'S', 'T')
                val isJk = !isJl && yearChar in listOf('7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J')
                wranglerModel = when {
                    isJl -> WranglerModel.JL
                    isJk -> WranglerModel.JK
                    else -> null
                }
                model = when {
                    isJl -> "Wrangler JL"
                    isJk -> "Wrangler JK"
                    else -> "Chrysler / Jeep"
                }
                profileId = when {
                    isJl -> VehicleProfileId.JEEP_JL
                    isJk -> VehicleProfileId.JEEP_JK
                    else -> VehicleProfileId.UNIVERSAL_OBD2
                }
                engineHint = when (vin.getOrNull(7)) {
                    '1' -> "3.8L V6"
                    'G' -> "3.6L Pentastar V6"
                    'N', 'A', '5' -> "2.0L Turbo I4"
                    'P' -> "2.0L 4xe PHEV"
                    'M' -> "3.0L EcoDiesel V6"
                    'J' -> "6.4L 392 HEMI V8"
                    else -> null
                }
                plantSummary = when {
                    vin.getOrNull(10) == 'L' && isJl -> "Toledo North Assembly, OH"
                    vin.getOrNull(10) == 'L' -> "Toledo South Assembly, OH"
                    else -> "FCA / Stellantis Plant ${vin.getOrNull(10)}"
                }
            }

            wmi in setOf("WBA", "WBS", "WBY", "4US", "5UX", "WMW") -> {
                profileId = VehicleProfileId.BMW
                val vdsKey = vds.take(2)
                model = when {
                    wmi == "WMW" -> "MINI Cooper"
                    wmi == "WBS" -> bmwVdsModels[vdsKey] ?: "M Series"
                    wmi == "WBY" -> "i Series"
                    wmi == "5UX" -> bmwVdsModels[vdsKey] ?: "X Series"
                    else -> bmwVdsModels[vdsKey] ?: "Series (${vdsKey})"
                }
                engineHint = when {
                    wmi == "WBY" -> "Electric / Range Extender"
                    vds.contains("8B") || vds.contains("5R") -> "B48 / B46 2.0L Turbo"
                    vds.contains("8T") -> "S55 / S58 Twin-Turbo I6"
                    else -> "BMW DME"
                }
            }

            wmi in setOf("WVW", "WAU", "TMB", "VSS") -> {
                profileId = VehicleProfileId.UNIVERSAL_OBD2
                val vdsKey = vds.take(2)
                model = vagVdsModels[vdsKey] ?: when (wmi) {
                    "WAU" -> "Audi"
                    "TMB" -> "Skoda"
                    "VSS" -> "SEAT"
                    else -> "Volkswagen"
                }
            }

            else -> {
                profileId = wmiInfo?.defaultProfileId ?: VehicleProfileId.UNIVERSAL_OBD2
                val vdsKey = vds.take(2)
                model = genericVdsModels[vdsKey] ?: if (wmiInfo != null) {
                    "OBD-II"
                } else {
                    "Vehicle ($wmi)"
                }
            }
        }

        val displayTitle = buildString {
            append(modelYear)
            append(' ')
            append(make)
            if (model.isNotBlank() && !model.equals(make, ignoreCase = true) && model != "OBD-II") {
                append(' ')
                append(model)
            }
        }.trim()

        return DetectedVehicle(
            vin = vin,
            make = make,
            model = model,
            modelYear = modelYear,
            engineHint = engineHint,
            profileId = profileId,
            displayTitle = displayTitle,
            protocolCode = protocolCode,
            protocolName = protocolName,
            calibrationId = calibrationId,
            supportedMode01Pids = supportedPids,
            detectionSource = source,
            plantSummary = plantSummary,
            detectedWranglerModel = wranglerModel
        )
    }

    fun fingerprintVehicle(
        protocolCode: String?,
        protocolName: String?,
        calibrationId: String?,
        supportedPids: Set<Int>
    ): DetectedVehicle {
        val calUpper = calibrationId?.uppercase().orEmpty()
        val inferredMake: String
        val inferredProfileId: String
        val inferredModel: String

        when {
            calUpper.startsWith("051") || calUpper.startsWith("68") || calUpper.startsWith("560") -> {
                inferredMake = "Jeep / Chrysler"
                inferredModel = "Legacy CAN / PCI"
                inferredProfileId = VehicleProfileId.JEEP_JK
            }
            calUpper.contains("BMW") || calUpper.startsWith("DME") -> {
                inferredMake = "BMW"
                inferredModel = "E/F/G Series"
                inferredProfileId = VehicleProfileId.BMW
            }
            else -> {
                inferredMake = "Generic"
                inferredModel = "OBD-II Vehicle"
                inferredProfileId = VehicleProfileId.UNIVERSAL_OBD2
            }
        }

        val displayTitle = when {
            inferredMake != "Generic" -> "$inferredMake $inferredModel"
            !protocolName.isNullOrBlank() -> "Generic OBD-II Vehicle ($protocolName)"
            else -> "Generic OBD-II Vehicle"
        }

        return DetectedVehicle(
            vin = null,
            make = inferredMake,
            model = inferredModel,
            modelYear = null,
            engineHint = calibrationId?.let { "CAL ID: $it" },
            profileId = inferredProfileId,
            displayTitle = displayTitle,
            protocolCode = protocolCode,
            protocolName = protocolName,
            calibrationId = calibrationId,
            supportedMode01Pids = supportedPids,
            detectionSource = if (calibrationId != null || supportedPids.isNotEmpty()) {
                DetectionSource.ECU_FINGERPRINT
            } else {
                DetectionSource.UNIVERSAL_FALLBACK
            }
        )
    }

    private fun inferMakeFallback(wmi: String): String = when (wmi.firstOrNull()) {
        '1', '4', '5' -> "North American Vehicle"
        '2' -> "Canadian Vehicle"
        '3' -> "Mexican Vehicle"
        'J' -> "Japanese Vehicle"
        'K' -> "Korean Vehicle"
        'W' -> "German Vehicle"
        'S' -> "UK Vehicle"
        'Z' -> "Italian Vehicle"
        else -> "OBD-II"
    }

    private fun parseStringMap(obj: Map<String, Any?>?): Map<String, String> {
        if (obj == null) return emptyMap()
        val out = linkedMapOf<String, String>()
        obj.forEach { (k, v) ->
            if (v is String) out[k.uppercase()] = v
        }
        return out
    }

    companion object {
        internal fun loadAssetText(context: Context?, relativePath: String): String? {
            if (context != null) {
                val fromAssets = runCatching {
                    context.assets.open(relativePath).bufferedReader(Charsets.UTF_8).use { it.readText() }
                }.getOrNull()
                if (fromAssets != null) return fromAssets
            }
            val candidates = listOf(
                File("app/src/main/assets/$relativePath"),
                File("src/main/assets/$relativePath")
            )
            return candidates.firstOrNull { it.exists() }?.readText(Charsets.UTF_8)
        }
    }
}
