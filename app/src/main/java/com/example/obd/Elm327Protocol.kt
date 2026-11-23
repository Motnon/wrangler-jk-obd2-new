package com.example.obd

/**
 * Pure ELM327 / SAE J1979 OBD-II frame parser and math decoder.
 * Handles spaces, carriage returns, line numbers, CAN multi-frame responses, and NO DATA guards.
 */
object Elm327Protocol {

    sealed class DecodedPid {
        data class Rpm(val rpm: Int) : DecodedPid()
        data class Speed(val kmh: Int) : DecodedPid()
        data class CoolantTemp(val celsius: Int) : DecodedPid()
        data class EngineOilTemp(val celsius: Int) : DecodedPid()
        data class IntakeAirTemp(val celsius: Int) : DecodedPid()
        data class AmbientAirTemp(val celsius: Int) : DecodedPid()
        data class ChargeAirCoolerTemp(val celsius: Int) : DecodedPid()
        data class CatalystTempB1(val celsius: Float) : DecodedPid()
        data class CatalystTempB2(val celsius: Float) : DecodedPid()
        data class BarometricPressure(val kpa: Int) : DecodedPid()
        data class FuelPressure(val kpa: Int) : DecodedPid()
        data class FuelRailPressureRel(val kpa: Float) : DecodedPid()
        data class FuelRailPressureGauge(val kpa: Int) : DecodedPid()
        data class BoostPressureControl(val kpa: Float) : DecodedPid()
        data class ThrottlePosition(val percent: Float) : DecodedPid()
        data class RelativeThrottlePosition(val percent: Float) : DecodedPid()
        data class AbsoluteThrottleB(val percent: Float) : DecodedPid()
        data class AbsoluteThrottleD(val percent: Float) : DecodedPid()
        data class AbsoluteThrottleE(val percent: Float) : DecodedPid()
        data class CommandedThrottleActuator(val percent: Float) : DecodedPid()
        data class EngineLoad(val percent: Float) : DecodedPid()
        data class AbsoluteLoad(val percent: Float) : DecodedPid()
        data class ManifoldPressure(val kpa: Int) : DecodedPid()
        data class ShortTermFuelTrim(val percent: Float) : DecodedPid()
        data class LongTermFuelTrim(val percent: Float) : DecodedPid()
        data class ShortTermFuelTrimB2(val percent: Float) : DecodedPid()
        data class LongTermFuelTrimB2(val percent: Float) : DecodedPid()
        data class FuelAirEquivRatio(val lambda: Float) : DecodedPid()
        data class FuelLevel(val percent: Float) : DecodedPid()
        data class FuelType(val typeName: String) : DecodedPid()
        data class HybridBatteryRemaining(val percent: Float) : DecodedPid()
        data class OxygenSensorReading(
            val pid: Int,
            val voltage: Float,
            val stftPercent: Float?
        ) : DecodedPid()
        data class TimingAdvance(val degrees: Float) : DecodedPid()
        data class CommandedEgr(val percent: Float) : DecodedPid()
        data class EgrError(val percent: Float) : DecodedPid()
        data class CommandedEvapPurge(val percent: Float) : DecodedPid()
        data class ControlModuleVoltage(val volts: Float) : DecodedPid()
        data class RunTimeSinceStart(val seconds: Int) : DecodedPid()
        data class DistanceSinceCleared(val km: Int) : DecodedPid()
        data class DistanceWithMil(val km: Int) : DecodedPid()
        data class WarmUpsSinceCleared(val count: Int) : DecodedPid()
        data class MonitorStatus(val milOn: Boolean, val dtcCount: Int) : DecodedPid()
    }

    data class Mode06MisfireResult(
        val cylinder: Int,
        val midHex: Int,
        val currentCycleCount: Int?,
        val ewmaCount: Int?,
        val fallbackCount: Int?,
        val maxLimit: Int?
    ) {
        val displayCount: Int?
            get() = currentCycleCount ?: ewmaCount ?: fallbackCount
    }

    fun sanitizeResponse(raw: String): String {
        return raw
            .replace(">", "")
            .replace("\r", "\n")
            .replace(Regex("""SEARCHING\.{3}""", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("""BUS INIT:\s*\.{3}OK""", RegexOption.IGNORE_CASE), "\n")
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }

    fun isErrorResponse(raw: String): Boolean {
        val upper = raw.uppercase()
        return upper.contains("NO DATA") ||
            upper.contains("UNABLE TO CONNECT") ||
            upper.contains("BUS INIT: ...ERROR") ||
            upper.contains("CAN ERROR") ||
            upper.contains("?") ||
            upper.contains("STOPPED")
    }

    private fun isLineNegativeResponse(bytes: List<Int>): Boolean {
        if (bytes.size >= 3 && bytes[0] == 0x7F) return true
        // ISO-TP single-frame with PCI length byte (1..7): e.g. 03 7F 01 12
        if (bytes.size >= 4 && bytes[0] in 1..7 && bytes[1] == 0x7F) return true
        return false
    }

    fun isNegativeResponse(raw: String): Boolean {
        val lines = sanitizeResponse(raw)
            .replace(Regex("""\b\d+:"""), "\n")
            .lines()
        var sawNegative = false
        var sawPositive = false
        for (line in lines) {
            val bytes = extractHexBytes(line.replace(Regex("""^\d+:"""), "").trim())
            if (bytes.isEmpty()) continue
            if (isLineNegativeResponse(bytes)) {
                sawNegative = true
            } else if (findHeaderIndex(bytes, 0x41) >= 0 || findHeaderIndex(bytes, 0x46) >= 0) {
                sawPositive = true
            }
        }
        return sawNegative && !sawPositive
    }

    /**
     * Detects 2018+ FCA/Stellantis Security Gateway (SGW) denial (NRC 0x33 Security Access Denied
     * or NRC 0x22 Conditions Not Correct on Mode 04 clear / protected diagnostic routines).
     */
    fun isSecurityAccessDenied(raw: String): Boolean {
        val lines = sanitizeResponse(raw)
            .replace(Regex("""\b\d+:"""), "\n")
            .lines()
        for (line in lines) {
            val bytes = extractHexBytes(line.replace(Regex("""^\d+:"""), "").trim())
            if (bytes.size >= 3 && bytes[0] == 0x7F && (bytes[2] == 0x33 || bytes[2] == 0x22)) {
                return true
            }
            if (bytes.size >= 4 && bytes[0] in 1..7 && bytes[1] == 0x7F && (bytes[3] == 0x33 || bytes[3] == 0x22)) {
                return true
            }
        }
        return false
    }

    fun parseVoltageResponse(raw: String): Float? {
        val cleaned = sanitizeResponse(raw)
        val match = Regex("""(\d+(?:\.\d+)?)\s*V""", RegexOption.IGNORE_CASE).find(cleaned)
        return match?.groupValues?.getOrNull(1)?.toFloatOrNull()
    }

    fun extractHexBytes(line: String): List<Int> {
        // Strip optional 3-nibble CAN arbitration header like "7E8 " or "7DF " if ATH0 wasn't honored
        val withoutCanHeader = line.trim()
            .replace(Regex("""^[0-9A-Fa-f]{3}\s+"""), "")
        val tokens = withoutCanHeader.split(Regex("""\s+""")).filter { it.isNotEmpty() }
        var compact = if (tokens.size > 1 && tokens.all { it.length == 2 }) {
            tokens.joinToString("").uppercase()
        } else {
            withoutCanHeader.replace(" ", "").uppercase()
        }
        // Handle rare unspaced CAN header (e.g., "7E803410582") producing an odd-length hex string
        if (compact.length >= 9 && compact.length % 2 == 1 &&
            (compact.startsWith("7E") || compact.startsWith("7DF"))
        ) {
            compact = compact.drop(3)
        }
        if (compact.length < 2 || compact.length % 2 != 0) return emptyList()
        if (!compact.all { it in '0'..'9' || it in 'A'..'F' }) return emptyList()
        return compact.chunked(2).mapNotNull { it.toIntOrNull(16) }
    }

    fun decodeMode01(raw: String, expectedPid: Int? = null): DecodedPid? {
        if (isErrorResponse(raw)) return null
        val lines = sanitizeResponse(raw)
            .replace(Regex("""\b\d+:"""), "\n")
            .lines()
        for (line in lines) {
            val normalizedLine = line.replace(Regex("""^\d+:"""), "").trim()
            val bytes = extractHexBytes(normalizedLine)
            if (bytes.size < 3 || isLineNegativeResponse(bytes)) continue
            val idx = if (expectedPid != null) {
                findHeaderWithPid(bytes, 0x41, expectedPid)
            } else {
                findHeaderIndex(bytes, 0x41)
            }
            if (idx < 0 || idx + 1 >= bytes.size) continue
            val pid = bytes[idx + 1]
            val data = bytes.drop(idx + 2)
            val decoded = when (pid) {
                0x01 -> if (data.isNotEmpty()) {
                    val a = data[0]
                    val mil = (a and 0x80) != 0
                    val count = a and 0x7F
                    DecodedPid.MonitorStatus(mil, count)
                } else null
                0x04 -> if (data.isNotEmpty()) {
                    DecodedPid.EngineLoad((data[0] * 100f) / 255f)
                } else null
                0x05 -> if (data.isNotEmpty()) {
                    DecodedPid.CoolantTemp(data[0] - 40)
                } else null
                0x06 -> if (data.isNotEmpty()) {
                    DecodedPid.ShortTermFuelTrim(((data[0] - 128) * 100f) / 128f)
                } else null
                0x07 -> if (data.isNotEmpty()) {
                    DecodedPid.LongTermFuelTrim(((data[0] - 128) * 100f) / 128f)
                } else null
                0x08 -> if (data.isNotEmpty()) {
                    DecodedPid.ShortTermFuelTrimB2(((data[0] - 128) * 100f) / 128f)
                } else null
                0x09 -> if (data.isNotEmpty()) {
                    DecodedPid.LongTermFuelTrimB2(((data[0] - 128) * 100f) / 128f)
                } else null
                0x0A -> if (data.isNotEmpty()) {
                    DecodedPid.FuelPressure(data[0] * 3)
                } else null
                0x0B -> if (data.isNotEmpty()) {
                    DecodedPid.ManifoldPressure(data[0])
                } else null
                0x0C -> if (data.size >= 2) {
                    DecodedPid.Rpm(((data[0] * 256) + data[1]) / 4)
                } else null
                0x0D -> if (data.isNotEmpty()) {
                    DecodedPid.Speed(data[0])
                } else null
                0x0E -> if (data.isNotEmpty()) {
                    DecodedPid.TimingAdvance((data[0] / 2f) - 64f)
                } else null
                0x0F -> if (data.isNotEmpty()) {
                    DecodedPid.IntakeAirTemp(data[0] - 40)
                } else null
                0x11 -> if (data.isNotEmpty()) {
                    DecodedPid.ThrottlePosition((data[0] * 100f) / 255f)
                } else null
                0x14, 0x15, 0x16, 0x17, 0x18, 0x19 -> if (data.size >= 2) {
                    val voltage = data[0] / 200f
                    val stft = if (data[1] == 0xFF) null else ((data[1] - 128) * 100f) / 128f
                    DecodedPid.OxygenSensorReading(pid = pid, voltage = voltage, stftPercent = stft)
                } else null
                0x1F -> if (data.size >= 2) {
                    DecodedPid.RunTimeSinceStart((data[0] * 256) + data[1])
                } else null
                0x21 -> if (data.size >= 2) {
                    DecodedPid.DistanceWithMil((data[0] * 256) + data[1])
                } else null
                0x22 -> if (data.size >= 2) {
                    DecodedPid.FuelRailPressureRel(((data[0] * 256) + data[1]) * 0.079f)
                } else null
                0x23 -> if (data.size >= 2) {
                    DecodedPid.FuelRailPressureGauge(((data[0] * 256) + data[1]) * 10)
                } else null
                0x2C -> if (data.isNotEmpty()) {
                    DecodedPid.CommandedEgr((data[0] * 100f) / 255f)
                } else null
                0x2D -> if (data.isNotEmpty()) {
                    DecodedPid.EgrError(((data[0] - 128) * 100f) / 128f)
                } else null
                0x2E -> if (data.isNotEmpty()) {
                    DecodedPid.CommandedEvapPurge((data[0] * 100f) / 255f)
                } else null
                0x2F -> if (data.isNotEmpty()) {
                    DecodedPid.FuelLevel((data[0] * 100f) / 255f)
                } else null
                0x30 -> if (data.isNotEmpty()) {
                    DecodedPid.WarmUpsSinceCleared(data[0])
                } else null
                0x31 -> if (data.size >= 2) {
                    DecodedPid.DistanceSinceCleared((data[0] * 256) + data[1])
                } else null
                0x33 -> if (data.isNotEmpty()) {
                    DecodedPid.BarometricPressure(data[0])
                } else null
                0x3C -> if (data.size >= 2) {
                    DecodedPid.CatalystTempB1((((data[0] * 256) + data[1]) / 10f) - 40f)
                } else null
                0x3D -> if (data.size >= 2) {
                    DecodedPid.CatalystTempB2((((data[0] * 256) + data[1]) / 10f) - 40f)
                } else null
                0x42 -> if (data.size >= 2) {
                    DecodedPid.ControlModuleVoltage(((data[0] * 256) + data[1]) / 1000f)
                } else null
                0x43 -> if (data.size >= 2) {
                    DecodedPid.AbsoluteLoad((((data[0] * 256) + data[1]) * 100f) / 255f)
                } else null
                0x44 -> if (data.size >= 2) {
                    DecodedPid.FuelAirEquivRatio(((data[0] * 256) + data[1]) * (2f / 65536f))
                } else null
                0x45 -> if (data.isNotEmpty()) {
                    DecodedPid.RelativeThrottlePosition((data[0] * 100f) / 255f)
                } else null
                0x46 -> if (data.isNotEmpty()) {
                    DecodedPid.AmbientAirTemp(data[0] - 40)
                } else null
                0x47 -> if (data.isNotEmpty()) {
                    DecodedPid.AbsoluteThrottleB((data[0] * 100f) / 255f)
                } else null
                0x49 -> if (data.isNotEmpty()) {
                    DecodedPid.AbsoluteThrottleD((data[0] * 100f) / 255f)
                } else null
                0x4A -> if (data.isNotEmpty()) {
                    DecodedPid.AbsoluteThrottleE((data[0] * 100f) / 255f)
                } else null
                0x4C -> if (data.isNotEmpty()) {
                    DecodedPid.CommandedThrottleActuator((data[0] * 100f) / 255f)
                } else null
                0x51 -> if (data.isNotEmpty()) {
                    DecodedPid.FuelType(decodeFuelType(data[0]))
                } else null
                0x5B -> if (data.isNotEmpty()) {
                    DecodedPid.HybridBatteryRemaining((data[0] * 100f) / 255f)
                } else null
                0x5C -> if (data.isNotEmpty()) {
                    DecodedPid.EngineOilTemp(data[0] - 40)
                } else null
                0x70 -> if (data.size >= 5 && (data[0] and 0x02) != 0) {
                    DecodedPid.BoostPressureControl(((data[3] * 256) + data[4]) * 0.03125f)
                } else if (data.size >= 3) {
                    DecodedPid.BoostPressureControl(((data[1] * 256) + data[2]) * 0.03125f)
                } else null
                0x77 -> if (data.size >= 2) {
                    DecodedPid.ChargeAirCoolerTemp(data[1] - 40)
                } else null
                else -> null
            }
            if (decoded != null) return decoded
        }
        return null
    }

    /**
     * Decodes SAE J1979 CAN Mode 06 Monitor Test Results for Cylinder Misfires (MID 0xA2..0xA7).
     * Handles single-frame and ISO-TP multi-frame responses (e.g., "014\n0:46A2...\n1:...").
     */
    fun decodeMode06Misfire(raw: String, expectedMid: Int): Mode06MisfireResult? {
        if (isErrorResponse(raw) || isNegativeResponse(raw)) return null
        val lines = sanitizeResponse(raw)
            .replace(Regex("""\b\d+:"""), "\n")
            .lines()

        val allBytes = mutableListOf<Int>()
        for (line in lines) {
            val trimmed = line.replace(Regex("""^\d+:"""), "").trim()
            if (trimmed.isEmpty()) continue
            // Skip ISO-TP length header line like "014" or "00E"
            if (Regex("""^[0-9A-Fa-f]{3}$""").matches(trimmed)) continue
            val bytes = extractHexBytes(trimmed)
            if (bytes.isNotEmpty()) {
                allBytes.addAll(bytes)
            }
        }
        if (allBytes.size < 10) return null

        val headerIdx = findHeaderPair(allBytes, 0x46, expectedMid)
        if (headerIdx < 0) return null

        val payload = allBytes.drop(headerIdx + 1)
        var currentCycle: Int? = null
        var ewma: Int? = null
        var fallback: Int? = null
        var maxLimit: Int? = null

        var offset = 0
        while (offset + 9 <= payload.size) {
            val mid = payload[offset]
            val tid = payload[offset + 1]
            // payload[offset + 2] is UASID (typically 0x24 = unsigned counts)
            val valRaw = (payload[offset + 3] shl 8) or payload[offset + 4]
            val maxRaw = (payload[offset + 7] shl 8) or payload[offset + 8]
            offset += 9

            if (mid == expectedMid) {
                when (tid) {
                    0x0C -> {
                        currentCycle = valRaw
                        maxLimit = maxRaw
                    }
                    0x0B -> {
                        ewma = valRaw
                        if (maxLimit == null) maxLimit = maxRaw
                    }
                    else -> {
                        if (fallback == null) {
                            fallback = valRaw
                            if (maxLimit == null) maxLimit = maxRaw
                        }
                    }
                }
            }
        }

        if (currentCycle == null && ewma == null && fallback == null) return null
        return Mode06MisfireResult(
            cylinder = (expectedMid - 0xA1).coerceIn(1, 12),
            midHex = expectedMid,
            currentCycleCount = currentCycle,
            ewmaCount = ewma,
            fallbackCount = fallback,
            maxLimit = maxLimit
        )
    }

    private fun findHeaderWithPid(bytes: List<Int>, header: Int, pid: Int): Int {
        for (i in 0 until bytes.size - 1) {
            if (bytes[i] == header && bytes[i + 1] == pid) return i
        }
        return -1
    }

    /**
     * Decodes Mode 03 (0x43), Mode 07 (0x47), or Mode 0A (0x4A) DTC frames into standard 5-character codes.
     */
    fun decodeDtcResponse(raw: String, modeResponseByte: Int = 0x43): List<String> {
        if (isErrorResponse(raw)) return emptyList()
        val codes = linkedSetOf<String>()
        val lines = sanitizeResponse(raw)
            .replace(Regex("""\b\d+:"""), "\n")
            .lines()
        val payloadBytes = mutableListOf<Int>()

        for (line in lines) {
            val cleaned = line.replace(Regex("""^\d+:"""), "").trim()
            val bytes = extractHexBytes(cleaned)
            if (bytes.isEmpty()) continue
            val headerIdx = bytes.indexOf(modeResponseByte)
            if (headerIdx >= 0) {
                val afterHeader = bytes.drop(headerIdx + 1)
                if (afterHeader.size >= 3 && afterHeader.size % 2 == 1) {
                    payloadBytes.addAll(afterHeader.drop(1))
                } else {
                    payloadBytes.addAll(afterHeader)
                }
            } else if (payloadBytes.isNotEmpty()) {
                payloadBytes.addAll(bytes)
            }
        }

        var i = 0
        while (i + 1 < payloadBytes.size) {
            val b1 = payloadBytes[i]
            val b2 = payloadBytes[i + 1]
            i += 2
            if (b1 == 0 && b2 == 0) continue
            val prefix = when ((b1 ushr 6) and 0x03) {
                0 -> 'P'
                1 -> 'C'
                2 -> 'B'
                else -> 'U'
            }
            val d1 = (b1 ushr 4) and 0x03
            val d2 = b1 and 0x0F
            val d3 = (b2 ushr 4) and 0x0F
            val d4 = b2 and 0x0F
            val code = buildString {
                append(prefix)
                append(d1)
                append(d2.toString(16).uppercase())
                append(d3.toString(16).uppercase())
                append(d4.toString(16).uppercase())
            }
            codes.add(code)
        }
        return codes.toList()
    }

    /**
     * Decodes Mode 09 PID 02 (49 02 ...) multi-frame VIN hex response.
     */
    fun decodeVinResponse(raw: String): VinInfo? {
        if (isErrorResponse(raw)) return null
        val lines = sanitizeResponse(raw)
            .replace(Regex("""\b\d+:"""), "\n")
            .lines()
        val asciiBytes = mutableListOf<Int>()
        for (line in lines) {
            val cleaned = line.replace(Regex("""^\d+:"""), "").trim()
            val bytes = extractHexBytes(cleaned)
            if (bytes.isEmpty()) continue
            val idx = findHeaderPair(bytes, 0x49, 0x02)
            if (idx >= 0) {
                val dropCount = if (idx + 2 < bytes.size && bytes[idx + 2] == 0x01) 3 else 2
                asciiBytes.addAll(bytes.drop(idx + dropCount))
            } else if (asciiBytes.isNotEmpty()) {
                asciiBytes.addAll(bytes)
            }
        }
        val vin = asciiBytes
            .filter { it in 0x30..0x39 || it in 0x41..0x5A }
            .map { it.toChar() }
            .joinToString("")
            .take(17)
        if (vin.length != 17) return null

        val yearChar = vin[9]
        val modelYear = when (yearChar) {
            '7' -> "2007"
            '8' -> "2008"
            '9' -> "2009"
            'A' -> "2010"
            'B' -> "2011"
            'C' -> "2012"
            'D' -> "2013"
            'E' -> "2014"
            'F' -> "2015"
            'G' -> "2016"
            'H' -> "2017"
            'J' -> "2018"
            'K' -> "2019"
            'L' -> "2020"
            'M' -> "2021"
            'N' -> "2022"
            'P' -> "2023"
            'R' -> "2024"
            'S' -> "2025"
            'T' -> "2026"
            else -> "Year Code $yearChar"
        }
        val isJeepPrefix = vin.startsWith("1J4") || vin.startsWith("1C4") || vin.startsWith("1J8")
        val detectedModel: WranglerModel? = when {
            !isJeepPrefix -> null
            vin.getOrNull(5) == 'X' || yearChar in listOf('K', 'L', 'M', 'N', 'P', 'R', 'S', 'T') ->
                WranglerModel.JL
            yearChar in listOf('7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J') ->
                WranglerModel.JK
            else -> null
        }
        val isJk = detectedModel == WranglerModel.JK
        val engineHint = when (vin.getOrNull(7)) {
            '1' -> "3.8L V6"
            'G' -> "3.6L Pentastar V6"
            'N', 'A', '5' -> "2.0L Turbo I4"
            'P' -> "2.0L 4xe PHEV"
            'M' -> "3.0L EcoDiesel V6"
            'J' -> "6.4L 392 HEMI V8"
            else -> null
        }
        val plant = when {
            vin.getOrNull(10) == 'L' && detectedModel == WranglerModel.JL -> "Toledo North Assembly, OH"
            vin.getOrNull(10) == 'L' -> "Toledo South Assembly, OH"
            else -> "FCA / Stellantis Plant ${vin.getOrNull(10)}"
        }
        return VinInfo(
            vin = vin,
            modelYear = modelYear,
            isWranglerJk = isJk,
            plantSummary = plant,
            detectedModel = detectedModel,
            engineHint = engineHint
        )
    }

    private fun decodeFuelType(code: Int): String = when (code) {
        0x01 -> "Gasoline"
        0x04 -> "Diesel"
        0x11 -> "Hybrid Gasoline"
        0x13 -> "Hybrid Diesel"
        0x17 -> "PHEV (4xe)"
        0x08 -> "Electric"
        else -> "Type 0x${code.toString(16).uppercase()}"
    }

    private fun findHeaderIndex(bytes: List<Int>, target: Int): Int {
        return bytes.indexOf(target)
    }

    private fun findHeaderPair(bytes: List<Int>, b1: Int, b2: Int): Int {
        for (i in 0 until bytes.size - 1) {
            if (bytes[i] == b1 && bytes[i + 1] == b2) return i
        }
        return -1
    }
}
