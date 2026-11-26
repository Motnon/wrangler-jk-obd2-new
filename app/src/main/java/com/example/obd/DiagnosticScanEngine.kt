package com.example.obd

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Coordinates real-time OBD-II PID/MID command planning, paired-bank aggregation,
 * unsupported-PID backoff, and human-readable telemetry formatting for all 43 diagnostic tests.
 */
class DiagnosticScanEngine {

    companion object {
        private val CORE_RESILIENT_COMMANDS = setOf("010C", "0105", "010D")
    }

    private var lastCoolantC: Int? = null
    private var lastEngineOilC: Int? = null
    private var lastAmbientC: Int? = null
    private var lastIntakeC: Int? = null
    private var lastChargeAirC: Int? = null
    private var lastCatB1C: Float? = null
    private var lastCatB2C: Float? = null
    private var lastBaroKpa: Int? = null
    private var lastMapKpa: Int? = null
    private var lastBoostKpa: Float? = null

    private var lastThrottlePct: Float? = null
    private var lastRelThrottlePct: Float? = null
    private var lastAbsThrottleBPct: Float? = null
    private var lastAbsThrottleDPct: Float? = null
    private var lastAbsThrottleEPct: Float? = null
    private var lastCmdThrottlePct: Float? = null

    private var lastLtftB1Pct: Float? = null
    private var lastLtftB2Pct: Float? = null
    private var lastStftB1Pct: Float? = null
    private var lastStftB2Pct: Float? = null
    private var lastEquivRatio: Float? = null
    private var lastFuelLevelPct: Float? = null
    private var lastFuelRailKpa: Float? = null

    private var o2VoltageBySlot = mutableMapOf<Int, Float>()
    private var o2StftBySlot = mutableMapOf<Int, Float?>()
    private var o2SlotSeen = mutableSetOf<Int>()

    private var lastTimingDeg: Float? = null
    private val misfireResults = mutableMapOf<Int, Elm327Protocol.Mode06MisfireResult>()

    private var lastCmdEgrPct: Float? = null
    private var lastEgrErrorPct: Float? = null
    private var lastCmdEvapPct: Float? = null

    private var lastRpm: Int? = null
    private var lastRawSpeedKmh: Int? = null
    private var lastEngineLoadPct: Float? = null
    private var lastAbsLoadPct: Float? = null
    private var lastModuleVolts: Float? = null
    private var lastRunTimeSec: Int? = null
    private var lastDistSinceClearKm: Int? = null
    private var lastDistWithMilKm: Int? = null
    private var lastWarmUps: Int? = null
    private var lastHybridBatteryPct: Float? = null

    private val updatedAtByTest = mutableMapOf<DiagnosticTestId, Long>()
    private val unsupportedCommands = mutableSetOf<String>()
    private val noDataStreaks = mutableMapOf<String, Int>()
    private var anyValidEcuReplySeen = false

    fun resetSession() {
        lastCoolantC = null
        lastEngineOilC = null
        lastAmbientC = null
        lastIntakeC = null
        lastChargeAirC = null
        lastCatB1C = null
        lastCatB2C = null
        lastBaroKpa = null
        lastMapKpa = null
        lastBoostKpa = null
        lastThrottlePct = null
        lastRelThrottlePct = null
        lastAbsThrottleBPct = null
        lastAbsThrottleDPct = null
        lastAbsThrottleEPct = null
        lastCmdThrottlePct = null
        lastLtftB1Pct = null
        lastLtftB2Pct = null
        lastStftB1Pct = null
        lastStftB2Pct = null
        lastEquivRatio = null
        lastFuelLevelPct = null
        lastFuelRailKpa = null
        o2VoltageBySlot.clear()
        o2StftBySlot.clear()
        o2SlotSeen.clear()
        lastTimingDeg = null
        misfireResults.clear()
        lastCmdEgrPct = null
        lastEgrErrorPct = null
        lastCmdEvapPct = null
        lastRpm = null
        lastRawSpeedKmh = null
        lastEngineLoadPct = null
        lastAbsLoadPct = null
        lastModuleVolts = null
        lastRunTimeSec = null
        lastDistSinceClearKm = null
        lastDistWithMilKm = null
        lastWarmUps = null
        lastHybridBatteryPct = null
        updatedAtByTest.clear()
        unsupportedCommands.clear()
        noDataStreaks.clear()
        anyValidEcuReplySeen = false
    }

    fun clearUnsupportedCache() {
        unsupportedCommands.clear()
        noDataStreaks.clear()
    }

    /**
     * Marks Mode 01 PIDs that are absent from the ECU's `0100`/`0120`/`0140`/`0160` supported-PID
     * bitmasks as unsupported upfront so the real-time scanner never polls unsupported sensors.
     */
    fun seedSupportedMode01Pids(supportedPids: Set<Int>) {
        if (supportedPids.isEmpty()) return
        for (test in DiagnosticTestId.entries) {
            for (cmd in test.primaryCommands) {
                if (cmd.startsWith("01") && cmd.length == 4) {
                    val pidNum = cmd.substring(2, 4).toIntOrNull(16) ?: continue
                    if (pidNum !in supportedPids) {
                        val fb = test.fallbackCommand
                        if (fb == null || !fb.startsWith("01") ||
                            (fb.substring(2, 4).toIntOrNull(16) !in supportedPids)
                        ) {
                            unsupportedCommands.add(cmd)
                        }
                    }
                }
            }
        }
    }

    /**
     * Builds a deduplicated ordered command list for the selected diagnostic tests,
     * applying fallback commands (e.g. 0118/0119 or ATRV) when a primary command is unsupported.
     */
    fun buildCommandPlan(selectedTests: Set<DiagnosticTestId>): List<String> {
        val commands = linkedSetOf<String>()
        for (test in DiagnosticTestId.entries) {
            if (test !in selectedTests) continue
            for (primary in test.primaryCommands) {
                if (primary !in unsupportedCommands) {
                    commands.add(primary)
                } else {
                    val fb = test.fallbackCommand
                    if (fb != null && fb !in unsupportedCommands) {
                        commands.add(fb)
                    }
                }
            }
        }
        return commands.toList()
    }

    fun isCommandUnsupported(command: String): Boolean = command in unsupportedCommands

    fun processCommandResponse(
        command: String,
        rawResponse: String?,
        nowMs: Long = System.currentTimeMillis()
    ): Elm327Protocol.DecodedPid? {
        if (rawResponse == null) return null

        if (Elm327Protocol.isNegativeResponse(rawResponse)) {
            if (command !in CORE_RESILIENT_COMMANDS) {
                unsupportedCommands.add(command)
            } else {
                val negCount = (noDataStreaks["neg_$command"] ?: 0) + 1
                noDataStreaks["neg_$command"] = negCount
                if (negCount >= 5) unsupportedCommands.add(command)
            }
            return null
        }

        if (Elm327Protocol.isErrorResponse(rawResponse)) {
            if (anyValidEcuReplySeen && command !in CORE_RESILIENT_COMMANDS) {
                val count = (noDataStreaks[command] ?: 0) + 1
                noDataStreaks[command] = count
                if (count >= 2) {
                    unsupportedCommands.add(command)
                }
            }
            return null
        }

        if (command.equals("ATRV", ignoreCase = true)) {
            val volts = Elm327Protocol.parseVoltageResponse(rawResponse) ?: return null
            anyValidEcuReplySeen = true
            noDataStreaks.remove(command)
            lastModuleVolts = volts
            updatedAtByTest[DiagnosticTestId.CONTROL_MODULE_VOLTAGE] = nowMs
            return Elm327Protocol.DecodedPid.ControlModuleVoltage(volts)
        }

        if (command.startsWith("06")) {
            val mid = command.drop(2).toIntOrNull(16) ?: return null
            val m06 = Elm327Protocol.decodeMode06Misfire(rawResponse, expectedMid = mid)
            if (m06 == null) {
                if (anyValidEcuReplySeen) {
                    val count = (noDataStreaks[command] ?: 0) + 1
                    noDataStreaks[command] = count
                    if (count >= 2) unsupportedCommands.add(command)
                }
                return null
            }
            anyValidEcuReplySeen = true
            noDataStreaks.remove(command)
            misfireResults[m06.cylinder] = m06
            val testId = when (m06.cylinder) {
                1 -> DiagnosticTestId.MISFIRE_CYL_1
                2 -> DiagnosticTestId.MISFIRE_CYL_2
                3 -> DiagnosticTestId.MISFIRE_CYL_3
                4 -> DiagnosticTestId.MISFIRE_CYL_4
                5 -> DiagnosticTestId.MISFIRE_CYL_5
                6 -> DiagnosticTestId.MISFIRE_CYL_6
                7 -> DiagnosticTestId.MISFIRE_CYL_7
                8 -> DiagnosticTestId.MISFIRE_CYL_8
                else -> null
            }
            if (testId != null) {
                updatedAtByTest[testId] = nowMs
            }
            return null
        }

        val expectedPid = if (command.startsWith("01") && command.length == 4) {
            command.drop(2).toIntOrNull(16)
        } else null

        val decoded = Elm327Protocol.decodeMode01(rawResponse, expectedPid = expectedPid)
        if (decoded == null) {
            if (anyValidEcuReplySeen && command !in CORE_RESILIENT_COMMANDS) {
                val count = (noDataStreaks[command] ?: 0) + 1
                noDataStreaks[command] = count
                if (count >= 2) unsupportedCommands.add(command)
            }
            return null
        }

        anyValidEcuReplySeen = true
        noDataStreaks.remove(command)
        noDataStreaks.remove("neg_$command")
        applyDecodedPid(decoded, nowMs)
        return decoded
    }

    fun applyDecodedPid(decoded: Elm327Protocol.DecodedPid, nowMs: Long = System.currentTimeMillis()) {
        when (decoded) {
            is Elm327Protocol.DecodedPid.CoolantTemp -> {
                lastCoolantC = decoded.celsius
                updatedAtByTest[DiagnosticTestId.ENGINE_COOLANT_TEMP] = nowMs
            }
            is Elm327Protocol.DecodedPid.EngineOilTemp -> {
                lastEngineOilC = decoded.celsius
                updatedAtByTest[DiagnosticTestId.ENGINE_OIL_TEMP] = nowMs
            }
            is Elm327Protocol.DecodedPid.AmbientAirTemp -> {
                lastAmbientC = decoded.celsius
                updatedAtByTest[DiagnosticTestId.AMBIENT_AIR_TEMP] = nowMs
            }
            is Elm327Protocol.DecodedPid.IntakeAirTemp -> {
                lastIntakeC = decoded.celsius
                updatedAtByTest[DiagnosticTestId.INTAKE_AIR_TEMP] = nowMs
            }
            is Elm327Protocol.DecodedPid.ChargeAirCoolerTemp -> {
                lastChargeAirC = decoded.celsius
                updatedAtByTest[DiagnosticTestId.CHARGE_AIR_COOLER_TEMP] = nowMs
            }
            is Elm327Protocol.DecodedPid.CatalystTempB1 -> {
                lastCatB1C = decoded.celsius
                updatedAtByTest[DiagnosticTestId.CATALYST_TEMP_B1_B2] = nowMs
            }
            is Elm327Protocol.DecodedPid.CatalystTempB2 -> {
                lastCatB2C = decoded.celsius
                updatedAtByTest[DiagnosticTestId.CATALYST_TEMP_B1_B2] = nowMs
            }
            is Elm327Protocol.DecodedPid.BarometricPressure -> {
                lastBaroKpa = decoded.kpa
                updatedAtByTest[DiagnosticTestId.BAROMETRIC_PRESSURE] = nowMs
            }
            is Elm327Protocol.DecodedPid.ManifoldPressure -> {
                lastMapKpa = decoded.kpa
                updatedAtByTest[DiagnosticTestId.MANIFOLD_MAP] = nowMs
            }
            is Elm327Protocol.DecodedPid.BoostPressureControl -> {
                lastBoostKpa = decoded.kpa
                updatedAtByTest[DiagnosticTestId.TURBO_BOOST_PRESSURE] = nowMs
            }
            is Elm327Protocol.DecodedPid.ThrottlePosition -> {
                lastThrottlePct = decoded.percent
                updatedAtByTest[DiagnosticTestId.THROTTLE_POSITION] = nowMs
            }
            is Elm327Protocol.DecodedPid.RelativeThrottlePosition -> {
                lastRelThrottlePct = decoded.percent
                updatedAtByTest[DiagnosticTestId.RELATIVE_THROTTLE_POSITION] = nowMs
            }
            is Elm327Protocol.DecodedPid.AbsoluteThrottleB -> {
                lastAbsThrottleBPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.ABSOLUTE_THROTTLE_B] = nowMs
            }
            is Elm327Protocol.DecodedPid.AbsoluteThrottleD -> {
                lastAbsThrottleDPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.ABSOLUTE_THROTTLE_D] = nowMs
            }
            is Elm327Protocol.DecodedPid.AbsoluteThrottleE -> {
                lastAbsThrottleEPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.ABSOLUTE_THROTTLE_E] = nowMs
            }
            is Elm327Protocol.DecodedPid.CommandedThrottleActuator -> {
                lastCmdThrottlePct = decoded.percent
                updatedAtByTest[DiagnosticTestId.COMMANDED_THROTTLE_ACTUATOR] = nowMs
            }
            is Elm327Protocol.DecodedPid.LongTermFuelTrim -> {
                lastLtftB1Pct = decoded.percent
                updatedAtByTest[DiagnosticTestId.LONG_TERM_FUEL_TRIM_B1_B2] = nowMs
            }
            is Elm327Protocol.DecodedPid.LongTermFuelTrimB2 -> {
                lastLtftB2Pct = decoded.percent
                updatedAtByTest[DiagnosticTestId.LONG_TERM_FUEL_TRIM_B1_B2] = nowMs
            }
            is Elm327Protocol.DecodedPid.ShortTermFuelTrim -> {
                lastStftB1Pct = decoded.percent
                updatedAtByTest[DiagnosticTestId.SHORT_TERM_FUEL_TRIM_B1_B2] = nowMs
            }
            is Elm327Protocol.DecodedPid.ShortTermFuelTrimB2 -> {
                lastStftB2Pct = decoded.percent
                updatedAtByTest[DiagnosticTestId.SHORT_TERM_FUEL_TRIM_B1_B2] = nowMs
            }
            is Elm327Protocol.DecodedPid.FuelAirEquivRatio -> {
                lastEquivRatio = decoded.lambda
                updatedAtByTest[DiagnosticTestId.COMMANDED_EQUIV_RATIO] = nowMs
            }
            is Elm327Protocol.DecodedPid.FuelLevel -> {
                lastFuelLevelPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.FUEL_TANK_LEVEL] = nowMs
            }
            is Elm327Protocol.DecodedPid.FuelPressure -> {
                lastFuelRailKpa = decoded.kpa.toFloat()
                updatedAtByTest[DiagnosticTestId.FUEL_RAIL_PRESSURE] = nowMs
            }
            is Elm327Protocol.DecodedPid.FuelRailPressureRel -> {
                lastFuelRailKpa = decoded.kpa
                updatedAtByTest[DiagnosticTestId.FUEL_RAIL_PRESSURE] = nowMs
            }
            is Elm327Protocol.DecodedPid.FuelRailPressureGauge -> {
                lastFuelRailKpa = decoded.kpa.toFloat()
                updatedAtByTest[DiagnosticTestId.FUEL_RAIL_PRESSURE] = nowMs
            }
            is Elm327Protocol.DecodedPid.HybridBatteryRemaining -> {
                lastHybridBatteryPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.HYBRID_BATTERY_LIFE] = nowMs
            }
            is Elm327Protocol.DecodedPid.FuelType -> Unit
            is Elm327Protocol.DecodedPid.OxygenSensorReading -> {
                val slot = when (decoded.pid) {
                    0x14 -> 1
                    0x15 -> 2
                    0x16, 0x18 -> 3
                    0x17, 0x19 -> 4
                    else -> null
                } ?: return
                o2SlotSeen.add(slot)
                o2VoltageBySlot[slot] = decoded.voltage
                o2StftBySlot[slot] = decoded.stftPercent
                when (slot) {
                    1 -> {
                        updatedAtByTest[DiagnosticTestId.O2_S1_B1S1_VOLTAGE] = nowMs
                        updatedAtByTest[DiagnosticTestId.O2_S1_B1S1_STFT] = nowMs
                    }
                    2 -> {
                        updatedAtByTest[DiagnosticTestId.O2_S2_B1S2_VOLTAGE] = nowMs
                        updatedAtByTest[DiagnosticTestId.O2_S2_B1S2_STFT] = nowMs
                    }
                    3 -> {
                        updatedAtByTest[DiagnosticTestId.O2_S3_B2S1_VOLTAGE] = nowMs
                        updatedAtByTest[DiagnosticTestId.O2_S3_B2S1_STFT] = nowMs
                    }
                    4 -> {
                        updatedAtByTest[DiagnosticTestId.O2_S4_B2S2_VOLTAGE] = nowMs
                        updatedAtByTest[DiagnosticTestId.O2_S4_B2S2_STFT] = nowMs
                    }
                }
            }
            is Elm327Protocol.DecodedPid.TimingAdvance -> {
                lastTimingDeg = decoded.degrees
                updatedAtByTest[DiagnosticTestId.TIMING_ADVANCE] = nowMs
            }
            is Elm327Protocol.DecodedPid.CommandedEgr -> {
                lastCmdEgrPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.COMMANDED_EGR] = nowMs
            }
            is Elm327Protocol.DecodedPid.EgrError -> {
                lastEgrErrorPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.EGR_ERROR] = nowMs
            }
            is Elm327Protocol.DecodedPid.CommandedEvapPurge -> {
                lastCmdEvapPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.COMMANDED_EVAP_PURGE] = nowMs
            }
            is Elm327Protocol.DecodedPid.Rpm -> {
                lastRpm = decoded.rpm
                updatedAtByTest[DiagnosticTestId.ENGINE_RPM] = nowMs
            }
            is Elm327Protocol.DecodedPid.Speed -> {
                lastRawSpeedKmh = decoded.kmh
                updatedAtByTest[DiagnosticTestId.VEHICLE_SPEED] = nowMs
            }
            is Elm327Protocol.DecodedPid.EngineLoad -> {
                lastEngineLoadPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.CALCULATED_ENGINE_LOAD] = nowMs
            }
            is Elm327Protocol.DecodedPid.AbsoluteLoad -> {
                lastAbsLoadPct = decoded.percent
                updatedAtByTest[DiagnosticTestId.ABSOLUTE_LOAD_VALUE] = nowMs
            }
            is Elm327Protocol.DecodedPid.ControlModuleVoltage -> {
                lastModuleVolts = decoded.volts
                updatedAtByTest[DiagnosticTestId.CONTROL_MODULE_VOLTAGE] = nowMs
            }
            is Elm327Protocol.DecodedPid.RunTimeSinceStart -> {
                lastRunTimeSec = decoded.seconds
                updatedAtByTest[DiagnosticTestId.RUN_TIME_SINCE_START] = nowMs
            }
            is Elm327Protocol.DecodedPid.DistanceSinceCleared -> {
                lastDistSinceClearKm = decoded.km
                updatedAtByTest[DiagnosticTestId.DISTANCE_SINCE_CODES_CLEARED] = nowMs
            }
            is Elm327Protocol.DecodedPid.DistanceWithMil -> {
                lastDistWithMilKm = decoded.km
                updatedAtByTest[DiagnosticTestId.DISTANCE_WITH_MIL_ON] = nowMs
            }
            is Elm327Protocol.DecodedPid.WarmUpsSinceCleared -> {
                lastWarmUps = decoded.count
                updatedAtByTest[DiagnosticTestId.WARM_UPS_SINCE_CODES_CLEARED] = nowMs
            }
            is Elm327Protocol.DecodedPid.MonitorStatus -> Unit
        }
    }

    fun buildReadingsMap(
        selectedTests: Set<DiagnosticTestId>,
        isConnected: Boolean,
        speedCalibrationFactor: Float = 1.0f,
        coolantWarnCelsius: Int = 108
    ): Map<DiagnosticTestId, DiagnosticReading> {
        val out = LinkedHashMap<DiagnosticTestId, DiagnosticReading>(selectedTests.size)
        for (test in DiagnosticTestId.entries) {
            if (test !in selectedTests) continue
            out[test] = buildReadingFor(
                test = test,
                isConnected = isConnected,
                speedCalibrationFactor = speedCalibrationFactor,
                coolantWarnCelsius = coolantWarnCelsius
            )
        }
        return out
    }

    private fun buildReadingFor(
        test: DiagnosticTestId,
        isConnected: Boolean,
        speedCalibrationFactor: Float,
        coolantWarnCelsius: Int
    ): DiagnosticReading {
        val updatedAt = updatedAtByTest[test] ?: 0L
        val allUnsupported = test.primaryCommands.all { cmd ->
            cmd in unsupportedCommands && (test.fallbackCommand == null || test.fallbackCommand in unsupportedCommands)
        }
        val defaultPendingStatus = when {
            allUnsupported -> DiagnosticReadingStatus.UNSUPPORTED
            isConnected -> DiagnosticReadingStatus.SCANNING
            else -> DiagnosticReadingStatus.IDLE
        }

        fun reading(value: String?, alert: Boolean = false): DiagnosticReading {
            return if (value != null) {
                DiagnosticReading(
                    testId = test,
                    formattedValue = value,
                    status = DiagnosticReadingStatus.LIVE,
                    isAlert = alert,
                    updatedAtMs = updatedAt
                )
            } else {
                DiagnosticReading(
                    testId = test,
                    formattedValue = if (defaultPendingStatus == DiagnosticReadingStatus.UNSUPPORTED) "N/A (ECU)" else "--",
                    status = defaultPendingStatus,
                    isAlert = false,
                    updatedAtMs = updatedAt
                )
            }
        }

        return when (test) {
            DiagnosticTestId.ENGINE_COOLANT_TEMP -> reading(
                lastCoolantC?.let { "$it °C" },
                alert = (lastCoolantC ?: Int.MIN_VALUE) >= coolantWarnCelsius
            )
            DiagnosticTestId.AMBIENT_AIR_TEMP -> reading(lastAmbientC?.let { "$it °C" })
            DiagnosticTestId.INTAKE_AIR_TEMP -> reading(lastIntakeC?.let { "$it °C" })
            DiagnosticTestId.CATALYST_TEMP_B1_B2 -> reading(
                formatPairedBankValues("°C", lastCatB1C, lastCatB2C, decimals = 1)
            )
            DiagnosticTestId.BAROMETRIC_PRESSURE -> reading(lastBaroKpa?.let { "$it kPa" })
            DiagnosticTestId.MANIFOLD_MAP -> reading(lastMapKpa?.let { "$it kPa" })
            DiagnosticTestId.ENGINE_OIL_TEMP -> reading(
                lastEngineOilC?.let { "$it °C" },
                alert = (lastEngineOilC ?: Int.MIN_VALUE) >= (coolantWarnCelsius + 16)
            )
            DiagnosticTestId.TURBO_BOOST_PRESSURE -> reading(
                lastBoostKpa?.let { String.format(Locale.US, "%.1f kPa", it) }
            )
            DiagnosticTestId.CHARGE_AIR_COOLER_TEMP -> reading(lastChargeAirC?.let { "$it °C" })
            DiagnosticTestId.THROTTLE_POSITION -> reading(lastThrottlePct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.RELATIVE_THROTTLE_POSITION -> reading(lastRelThrottlePct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.ABSOLUTE_THROTTLE_B -> reading(lastAbsThrottleBPct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.ABSOLUTE_THROTTLE_D -> reading(lastAbsThrottleDPct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.ABSOLUTE_THROTTLE_E -> reading(lastAbsThrottleEPct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.COMMANDED_THROTTLE_ACTUATOR -> reading(lastCmdThrottlePct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.LONG_TERM_FUEL_TRIM_B1_B2 -> reading(
                formatPairedBankSignedPct(lastLtftB1Pct, lastLtftB2Pct),
                alert = kotlin.math.abs(lastLtftB1Pct ?: 0f) >= 12f || kotlin.math.abs(lastLtftB2Pct ?: 0f) >= 12f
            )
            DiagnosticTestId.SHORT_TERM_FUEL_TRIM_B1_B2 -> reading(
                formatPairedBankSignedPct(lastStftB1Pct, lastStftB2Pct),
                alert = kotlin.math.abs(lastStftB1Pct ?: 0f) >= 15f || kotlin.math.abs(lastStftB2Pct ?: 0f) >= 15f
            )
            DiagnosticTestId.COMMANDED_EQUIV_RATIO -> reading(lastEquivRatio?.let { String.format(Locale.US, "%.3f λ", it) })
            DiagnosticTestId.FUEL_TANK_LEVEL -> reading(
                lastFuelLevelPct?.let { "${it.roundToInt()}%" },
                alert = lastFuelLevelPct != null && lastFuelLevelPct!! <= 15f
            )
            DiagnosticTestId.FUEL_RAIL_PRESSURE -> reading(
                lastFuelRailKpa?.let { String.format(Locale.US, "%,.0f kPa", it) }
            )
            DiagnosticTestId.O2_S1_B1S1_VOLTAGE -> reading(o2VoltageBySlot[1]?.let { String.format(Locale.US, "%.3f V", it) })
            DiagnosticTestId.O2_S1_B1S1_STFT -> reading(formatO2Stft(1))
            DiagnosticTestId.O2_S2_B1S2_VOLTAGE -> reading(o2VoltageBySlot[2]?.let { String.format(Locale.US, "%.3f V", it) })
            DiagnosticTestId.O2_S2_B1S2_STFT -> reading(formatO2Stft(2))
            DiagnosticTestId.O2_S3_B2S1_VOLTAGE -> reading(o2VoltageBySlot[3]?.let { String.format(Locale.US, "%.3f V", it) })
            DiagnosticTestId.O2_S3_B2S1_STFT -> reading(formatO2Stft(3))
            DiagnosticTestId.O2_S4_B2S2_VOLTAGE -> reading(o2VoltageBySlot[4]?.let { String.format(Locale.US, "%.3f V", it) })
            DiagnosticTestId.O2_S4_B2S2_STFT -> reading(formatO2Stft(4))
            DiagnosticTestId.TIMING_ADVANCE -> reading(lastTimingDeg?.let { String.format(Locale.US, "%+.1f°", it) })
            DiagnosticTestId.MISFIRE_CYL_1 -> formatMisfireReading(test, 1, defaultPendingStatus, updatedAt)
            DiagnosticTestId.MISFIRE_CYL_2 -> formatMisfireReading(test, 2, defaultPendingStatus, updatedAt)
            DiagnosticTestId.MISFIRE_CYL_3 -> formatMisfireReading(test, 3, defaultPendingStatus, updatedAt)
            DiagnosticTestId.MISFIRE_CYL_4 -> formatMisfireReading(test, 4, defaultPendingStatus, updatedAt)
            DiagnosticTestId.MISFIRE_CYL_5 -> formatMisfireReading(test, 5, defaultPendingStatus, updatedAt)
            DiagnosticTestId.MISFIRE_CYL_6 -> formatMisfireReading(test, 6, defaultPendingStatus, updatedAt)
            DiagnosticTestId.MISFIRE_CYL_7 -> formatMisfireReading(test, 7, defaultPendingStatus, updatedAt)
            DiagnosticTestId.MISFIRE_CYL_8 -> formatMisfireReading(test, 8, defaultPendingStatus, updatedAt)
            DiagnosticTestId.COMMANDED_EGR -> reading(lastCmdEgrPct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.EGR_ERROR -> reading(lastEgrErrorPct?.let { String.format(Locale.US, "%+.1f%%", it) })
            DiagnosticTestId.COMMANDED_EVAP_PURGE -> reading(lastCmdEvapPct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.ENGINE_RPM -> reading(lastRpm?.let { String.format(Locale.US, "%,d RPM", it) })
            DiagnosticTestId.VEHICLE_SPEED -> reading(
                applyCalibratedSpeedKph(lastRawSpeedKmh, speedCalibrationFactor)?.let { "$it KPH" }
            )
            DiagnosticTestId.CALCULATED_ENGINE_LOAD -> reading(lastEngineLoadPct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.ABSOLUTE_LOAD_VALUE -> reading(lastAbsLoadPct?.let { String.format(Locale.US, "%.1f%%", it) })
            DiagnosticTestId.CONTROL_MODULE_VOLTAGE -> reading(
                lastModuleVolts?.let { String.format(Locale.US, "%.2f V", it) },
                alert = lastModuleVolts != null && lastModuleVolts!! < 11.8f
            )
            DiagnosticTestId.RUN_TIME_SINCE_START -> reading(lastRunTimeSec?.let { formatDuration(it) })
            DiagnosticTestId.DISTANCE_SINCE_CODES_CLEARED -> reading(lastDistSinceClearKm?.let { "$it km" })
            DiagnosticTestId.DISTANCE_WITH_MIL_ON -> reading(
                lastDistWithMilKm?.let { "$it km" },
                alert = (lastDistWithMilKm ?: 0) > 0
            )
            DiagnosticTestId.WARM_UPS_SINCE_CODES_CLEARED -> reading(lastWarmUps?.let { "$it" })
            DiagnosticTestId.HYBRID_BATTERY_LIFE -> reading(
                lastHybridBatteryPct?.let { String.format(Locale.US, "%.1f%%", it) }
            )
        }
    }

    private fun formatO2Stft(slot: Int): String? {
        if (slot !in o2SlotSeen) return null
        val pct = o2StftBySlot[slot] ?: return "N/A (S2)"
        return String.format(Locale.US, "%+.1f%%", pct)
    }

    private fun formatMisfireReading(
        test: DiagnosticTestId,
        cylinder: Int,
        defaultPendingStatus: DiagnosticReadingStatus,
        updatedAt: Long
    ): DiagnosticReading {
        val m = misfireResults[cylinder]
        val cnt = m?.displayCount
        return if (m != null && cnt != null) {
            val text = if (m.currentCycleCount != null && m.ewmaCount != null) {
                "${m.currentCycleCount} (EWMA ${m.ewmaCount})"
            } else {
                "$cnt"
            }
            DiagnosticReading(
                testId = test,
                formattedValue = text,
                status = DiagnosticReadingStatus.LIVE,
                isAlert = cnt > 0,
                updatedAtMs = updatedAt
            )
        } else {
            DiagnosticReading(
                testId = test,
                formattedValue = if (defaultPendingStatus == DiagnosticReadingStatus.UNSUPPORTED) "N/A (ECU)" else "--",
                status = defaultPendingStatus,
                isAlert = false,
                updatedAtMs = updatedAt
            )
        }
    }

    private fun formatPairedBankValues(unit: String, b1: Float?, b2: Float?, decimals: Int): String? {
        if (b1 == null && b2 == null) return null
        val fmt = "%.${decimals}f"
        val b1Text = b1?.let { "B1: ${String.format(Locale.US, fmt, it)}$unit" }
        val b2Text = b2?.let { "B2: ${String.format(Locale.US, fmt, it)}$unit" }
        return listOfNotNull(b1Text, b2Text).joinToString("  |  ")
    }

    private fun formatPairedBankSignedPct(b1: Float?, b2: Float?): String? {
        if (b1 == null && b2 == null) return null
        val b1Text = b1?.let { "B1: ${String.format(Locale.US, "%+.1f%%", it)}" }
        val b2Text = b2?.let { "B2: ${String.format(Locale.US, "%+.1f%%", it)}" }
        return listOfNotNull(b1Text, b2Text).joinToString("  |  ")
    }

    private fun formatDuration(totalSeconds: Int): String {
        val hrs = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }
}
