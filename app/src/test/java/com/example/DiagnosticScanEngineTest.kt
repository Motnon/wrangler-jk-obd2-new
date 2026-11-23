package com.example

import com.example.obd.DiagnosticCategory
import com.example.obd.DiagnosticReadingStatus
import com.example.obd.DiagnosticScanEngine
import com.example.obd.DiagnosticTestId
import com.example.obd.Elm327Protocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticScanEngineTest {

    @Test
    fun `catalog contains all 50 tests across 6 categories and round-trips csv`() {
        assertEquals(6, DiagnosticCategory.entries.size)
        assertEquals(50, DiagnosticTestId.entries.size)

        val subset = setOf(
            DiagnosticTestId.CATALYST_TEMP_B1_B2,
            DiagnosticTestId.O2_S1_B1S1_VOLTAGE,
            DiagnosticTestId.MISFIRE_CYL_1,
            DiagnosticTestId.RUN_TIME_SINCE_START
        )
        val csv = DiagnosticTestId.encodeSet(subset)
        val decoded = DiagnosticTestId.decodeSet(csv)
        assertEquals(subset, decoded)
    }

    @Test
    fun `elm327 decodes all extended mode 01 pids accurately`() {
        // 013C Catalyst B1: 0x1B58 = 7000 -> 7000/10 - 40 = 660.0 C
        val catB1 = Elm327Protocol.decodeMode01("41 3C 1B 58", 0x3C) as Elm327Protocol.DecodedPid.CatalystTempB1
        assertEquals(660.0f, catB1.celsius, 0.05f)

        // 0133 Baro: 0x65 = 101 kPa
        val baro = Elm327Protocol.decodeMode01("41 33 65", 0x33) as Elm327Protocol.DecodedPid.BarometricPressure
        assertEquals(101, baro.kpa)

        // 0144 Equiv ratio: 0x8000 = 32768 -> 32768 * 2 / 65536 = 1.000 lambda
        val lambda = Elm327Protocol.decodeMode01("41 44 80 00", 0x44) as Elm327Protocol.DecodedPid.FuelAirEquivRatio
        assertEquals(1.0f, lambda.lambda, 0.001f)

        // 010E Timing advance: 0x9E (158) -> 158 / 2 - 64 = +15.0 deg
        val timing = Elm327Protocol.decodeMode01("41 0E 9E", 0x0E) as Elm327Protocol.DecodedPid.TimingAdvance
        assertEquals(15.0f, timing.degrees, 0.05f)

        // 0114 O2 S1 with trim: 0x90 (144 -> 0.720V), 0x84 (132 -> +3.125%)
        val o2s1 = Elm327Protocol.decodeMode01("41 14 90 84", 0x14) as Elm327Protocol.DecodedPid.OxygenSensorReading
        assertEquals(0.720f, o2s1.voltage, 0.001f)
        assertNotNull(o2s1.stftPercent)
        assertEquals(3.125f, o2s1.stftPercent!!, 0.05f)

        // 0115 O2 S2 with 0xFF (not used in trim)
        val o2s2 = Elm327Protocol.decodeMode01("41 15 5A FF", 0x15) as Elm327Protocol.DecodedPid.OxygenSensorReading
        assertEquals(0.450f, o2s2.voltage, 0.001f)
        assertNull(o2s2.stftPercent)

        // 0143 Absolute load: 0x00FF -> 100.0%
        val absLoad = Elm327Protocol.decodeMode01("41 43 00 FF", 0x43) as Elm327Protocol.DecodedPid.AbsoluteLoad
        assertEquals(100.0f, absLoad.percent, 0.05f)

        // 011F Run time: 0x0258 = 600s
        val runTime = Elm327Protocol.decodeMode01("41 1F 02 58", 0x1F) as Elm327Protocol.DecodedPid.RunTimeSinceStart
        assertEquals(600, runTime.seconds)

        // 0131 Distance since clear: 0x01F4 = 500 km
        val distClear = Elm327Protocol.decodeMode01("41 31 01 F4", 0x31) as Elm327Protocol.DecodedPid.DistanceSinceCleared
        assertEquals(500, distClear.km)

        // 0121 Distance with MIL: 0x000F = 15 km
        val distMil = Elm327Protocol.decodeMode01("41 21 00 0F", 0x21) as Elm327Protocol.DecodedPid.DistanceWithMil
        assertEquals(15, distMil.km)

        // 0130 Warm-ups: 0x0A = 10
        val warmUps = Elm327Protocol.decodeMode01("41 30 0A", 0x30) as Elm327Protocol.DecodedPid.WarmUpsSinceCleared
        assertEquals(10, warmUps.count)

        // 012C/2D/2E EGR & EVAP
        val egrCmd = Elm327Protocol.decodeMode01("41 2C 80", 0x2C) as Elm327Protocol.DecodedPid.CommandedEgr
        assertEquals(50.196f, egrCmd.percent, 0.1f)
        val egrErr = Elm327Protocol.decodeMode01("41 2D 80", 0x2D) as Elm327Protocol.DecodedPid.EgrError
        assertEquals(0.0f, egrErr.percent, 0.05f)
        val evap = Elm327Protocol.decodeMode01("41 2E FF", 0x2E) as Elm327Protocol.DecodedPid.CommandedEvapPurge
        assertEquals(100.0f, evap.percent, 0.05f)

        // 0145/47/49/4A/4C Throttle & Pedal
        assertTrue(Elm327Protocol.decodeMode01("41 45 33", 0x45) is Elm327Protocol.DecodedPid.RelativeThrottlePosition)
        assertTrue(Elm327Protocol.decodeMode01("41 47 33", 0x47) is Elm327Protocol.DecodedPid.AbsoluteThrottleB)
        assertTrue(Elm327Protocol.decodeMode01("41 49 33", 0x49) is Elm327Protocol.DecodedPid.AbsoluteThrottleD)
        assertTrue(Elm327Protocol.decodeMode01("41 4A 33", 0x4A) is Elm327Protocol.DecodedPid.AbsoluteThrottleE)
        assertTrue(Elm327Protocol.decodeMode01("41 4C 33", 0x4C) is Elm327Protocol.DecodedPid.CommandedThrottleActuator)

        // Negative response 7F 01 12 rejected
        assertTrue(Elm327Protocol.isNegativeResponse("7F 01 12\r>"))
        assertNull(Elm327Protocol.decodeMode01("7F 01 12", 0x2C))
    }

    @Test
    fun `elm327 decodes mode 06 multi-frame cylinder misfire counts`() {
        // Multi-frame ISO-TP response for 06A2 (MID 0xA2 = Cyl 1):
        // Record 1: MID A2, TID 0B (EWMA), UASID 24, Val 0001, Min 0000, Max FFFF
        // Record 2: MID A2, TID 0C (Current Cycle), UASID 24, Val 0003, Min 0000, Max FFFF
        val multiFrame = "013 0:46A20B240001 1:0000FFFFA20C24 2:00030000FFFF"
        val result = Elm327Protocol.decodeMode06Misfire(multiFrame, expectedMid = 0xA2)
        assertNotNull(result)
        assertEquals(1, result!!.cylinder)
        assertEquals(1, result.ewmaCount)
        assertEquals(3, result.currentCycleCount)
        assertEquals(3, result.displayCount)
    }

    @Test
    fun `scan engine deduplicates commands, handles fallback, and formats readings`() {
        val engine = DiagnosticScanEngine()
        val chosen = setOf(
            DiagnosticTestId.O2_S1_B1S1_VOLTAGE,
            DiagnosticTestId.O2_S1_B1S1_STFT,
            DiagnosticTestId.O2_S3_B2S1_VOLTAGE,
            DiagnosticTestId.CONTROL_MODULE_VOLTAGE,
            DiagnosticTestId.CATALYST_TEMP_B1_B2,
            DiagnosticTestId.MISFIRE_CYL_2,
            DiagnosticTestId.RUN_TIME_SINCE_START
        )

        // Initial plan should deduplicate 0114
        val plan1 = engine.buildCommandPlan(chosen)
        assertEquals(1, plan1.count { it == "0114" })
        assertTrue("0116" in plan1)
        assertTrue("0142" in plan1)

        // Simulate 0116 & 0142 returning negative response -> should switch to 0118 & ATRV
        engine.processCommandResponse("0116", "7F 01 12")
        engine.processCommandResponse("0142", "7F 01 12")

        val plan2 = engine.buildCommandPlan(chosen)
        assertFalse("0116" in plan2)
        assertTrue("0118" in plan2)
        assertFalse("0142" in plan2)
        assertTrue("ATRV" in plan2)

        // Feed live responses
        engine.processCommandResponse("0114", "41 14 90 80")
        engine.processCommandResponse("0118", "41 18 8C 84")
        engine.processCommandResponse("ATRV", "14.2V")
        engine.processCommandResponse("013C", "41 3C 1B 58")
        engine.processCommandResponse("013D", "41 3D 1A F4")
        engine.processCommandResponse("06A3", "46 A3 0C 24 00 02 00 00 FF FF")
        engine.processCommandResponse("011F", "41 1F 0E 42") // 3650s = 01:00:50

        val readings = engine.buildReadingsMap(chosen, isConnected = true)
        assertEquals("0.720 V", readings[DiagnosticTestId.O2_S1_B1S1_VOLTAGE]?.formattedValue)
        assertEquals("+0.0%", readings[DiagnosticTestId.O2_S1_B1S1_STFT]?.formattedValue)
        assertEquals("0.700 V", readings[DiagnosticTestId.O2_S3_B2S1_VOLTAGE]?.formattedValue)
        assertEquals("14.20 V", readings[DiagnosticTestId.CONTROL_MODULE_VOLTAGE]?.formattedValue)
        assertEquals("B1: 660.0°C  |  B2: 650.0°C", readings[DiagnosticTestId.CATALYST_TEMP_B1_B2]?.formattedValue)
        assertEquals("2", readings[DiagnosticTestId.MISFIRE_CYL_2]?.formattedValue)
        assertTrue(readings[DiagnosticTestId.MISFIRE_CYL_2]?.isAlert == true)
        assertEquals("01:00:50", readings[DiagnosticTestId.RUN_TIME_SINCE_START]?.formattedValue)
        assertEquals(DiagnosticReadingStatus.LIVE, readings[DiagnosticTestId.RUN_TIME_SINCE_START]?.status)
    }

    @Test
    fun `scan engine plans, decodes, and formats Wrangler JL extended tests`() {
        val engine = DiagnosticScanEngine()
        val jlChosen = setOf(
            DiagnosticTestId.ENGINE_OIL_TEMP,
            DiagnosticTestId.TURBO_BOOST_PRESSURE,
            DiagnosticTestId.CHARGE_AIR_COOLER_TEMP,
            DiagnosticTestId.FUEL_RAIL_PRESSURE,
            DiagnosticTestId.HYBRID_BATTERY_LIFE,
            DiagnosticTestId.MISFIRE_CYL_7,
            DiagnosticTestId.MISFIRE_CYL_8
        )

        val plan = engine.buildCommandPlan(jlChosen)
        assertTrue("015C" in plan)
        assertTrue("0170" in plan)
        assertTrue("0177" in plan)
        assertTrue("0123" in plan)
        assertTrue("015B" in plan)
        assertTrue("06A8" in plan)
        assertTrue("06A9" in plan)

        engine.processCommandResponse("015C", "41 5C 8C") // 100 C
        engine.processCommandResponse("0170", "41 70 01 05 DC 00 00") // 46.9 kPa
        engine.processCommandResponse("0177", "41 77 01 5A 00") // 50 C
        engine.processCommandResponse("0123", "41 23 05 DC") // 15,000 kPa
        engine.processCommandResponse("015B", "41 5B CC") // 80.0%
        engine.processCommandResponse("06A8", "46 A8 0C 24 00 00 00 00 FF FF") // 0
        engine.processCommandResponse("06A9", "46 A9 0C 24 00 02 00 00 FF FF") // 2 -> alert

        val readings = engine.buildReadingsMap(jlChosen, isConnected = true)
        assertEquals("100 °C", readings[DiagnosticTestId.ENGINE_OIL_TEMP]?.formattedValue)
        assertEquals("46.9 kPa", readings[DiagnosticTestId.TURBO_BOOST_PRESSURE]?.formattedValue)
        assertEquals("50 °C", readings[DiagnosticTestId.CHARGE_AIR_COOLER_TEMP]?.formattedValue)
        assertEquals("15,000 kPa", readings[DiagnosticTestId.FUEL_RAIL_PRESSURE]?.formattedValue)
        assertEquals("80.0%", readings[DiagnosticTestId.HYBRID_BATTERY_LIFE]?.formattedValue)
        assertEquals("0", readings[DiagnosticTestId.MISFIRE_CYL_7]?.formattedValue)
        assertFalse(readings[DiagnosticTestId.MISFIRE_CYL_7]?.isAlert == true)
        assertEquals("2", readings[DiagnosticTestId.MISFIRE_CYL_8]?.formattedValue)
        assertTrue(readings[DiagnosticTestId.MISFIRE_CYL_8]?.isAlert == true)
    }
}
