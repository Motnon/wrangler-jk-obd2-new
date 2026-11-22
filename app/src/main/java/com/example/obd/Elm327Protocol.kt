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
        data class IntakeAirTemp(val celsius: Int) : DecodedPid()
        data class AmbientAirTemp(val celsius: Int) : DecodedPid()
        data class ThrottlePosition(val percent: Float) : DecodedPid()
        data class EngineLoad(val percent: Float) : DecodedPid()
        data class ManifoldPressure(val kpa: Int) : DecodedPid()
        data class ShortTermFuelTrim(val percent: Float) : DecodedPid()
        data class LongTermFuelTrim(val percent: Float) : DecodedPid()
        data class FuelLevel(val percent: Float) : DecodedPid()
        data class ControlModuleVoltage(val volts: Float) : DecodedPid()
        data class MonitorStatus(val milOn: Boolean, val dtcCount: Int) : DecodedPid()
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
            if (bytes.size < 3) continue
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
                0x0B -> if (data.isNotEmpty()) {
                    DecodedPid.ManifoldPressure(data[0])
                } else null
                0x0C -> if (data.size >= 2) {
                    DecodedPid.Rpm(((data[0] * 256) + data[1]) / 4)
                } else null
                0x0D -> if (data.isNotEmpty()) {
                    DecodedPid.Speed(data[0])
                } else null
                0x0F -> if (data.isNotEmpty()) {
                    DecodedPid.IntakeAirTemp(data[0] - 40)
                } else null
                0x11 -> if (data.isNotEmpty()) {
                    DecodedPid.ThrottlePosition((data[0] * 100f) / 255f)
                } else null
                0x2F -> if (data.isNotEmpty()) {
                    DecodedPid.FuelLevel((data[0] * 100f) / 255f)
                } else null
                0x42 -> if (data.size >= 2) {
                    DecodedPid.ControlModuleVoltage(((data[0] * 256) + data[1]) / 1000f)
                } else null
                0x46 -> if (data.isNotEmpty()) {
                    DecodedPid.AmbientAirTemp(data[0] - 40)
                } else null
                else -> null
            }
            if (decoded != null) return decoded
        }
        return null
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
            else -> "Year Code $yearChar"
        }
        val isJk = yearChar in listOf('7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J') &&
            (vin.startsWith("1J4") || vin.startsWith("1C4") || vin.startsWith("1J8"))
        val plant = if (vin.getOrNull(10) == 'L') "Toledo South Assembly, OH" else "Chrysler/FCA Plant ${vin.getOrNull(10)}"
        return VinInfo(
            vin = vin,
            modelYear = modelYear,
            isWranglerJk = isJk,
            plantSummary = plant
        )
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
