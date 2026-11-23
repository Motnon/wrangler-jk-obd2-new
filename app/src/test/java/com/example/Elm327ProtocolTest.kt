package com.example

import com.example.obd.Elm327Protocol
import com.example.obd.JkDiagnosticLibrary
import com.example.obd.JkSubsystem
import com.example.obd.WranglerDtcCatalog
import com.example.obd.WranglerModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Elm327ProtocolTest {

    @Test
    fun `decodeMode01 parses engine rpm correctly`() {
        // 41 0C 0B B8 -> ((0x0B * 256) + 0xB8) / 4 = 3000 / 4 = 750 RPM
        val decoded = Elm327Protocol.decodeMode01("41 0C 0B B8\r>")
        assertEquals(Elm327Protocol.DecodedPid.Rpm(750), decoded)
    }

    @Test
    fun `decodeMode01 parses coolant temp and speed`() {
        // 41 05 82 -> 0x82 (130) - 40 = 90 C
        val coolant = Elm327Protocol.decodeMode01("41 05 82")
        assertEquals(Elm327Protocol.DecodedPid.CoolantTemp(90), coolant)

        // 41 0D 50 -> 80 km/h
        val speed = Elm327Protocol.decodeMode01("410D50")
        assertEquals(Elm327Protocol.DecodedPid.Speed(80), speed)

        // 41 2F 80 -> (128 * 100) / 255 = 50.196%
        val fuel = Elm327Protocol.decodeMode01("41 2F 80")
        assertTrue(fuel is Elm327Protocol.DecodedPid.FuelLevel)
        assertEquals(50.196f, (fuel as Elm327Protocol.DecodedPid.FuelLevel).percent, 0.1f)
    }

    @Test
    fun `decodeMode01 parses module voltage and ATRV`() {
        val modVolts = Elm327Protocol.decodeMode01("41 42 37 78")
        // 0x3778 = 14200 -> 14.2V
        assertTrue(modVolts is Elm327Protocol.DecodedPid.ControlModuleVoltage)
        assertEquals(14.2f, (modVolts as Elm327Protocol.DecodedPid.ControlModuleVoltage).volts, 0.01f)

        val atrv = Elm327Protocol.parseVoltageResponse("14.1V\r>")
        assertNotNull(atrv)
        assertEquals(14.1f, atrv!!, 0.01f)
    }

    @Test
    fun `decodeMode01 returns null on NO DATA`() {
        assertNull(Elm327Protocol.decodeMode01("NO DATA\r>"))
        assertNull(Elm327Protocol.decodeMode01("SEARCHING...\rUNABLE TO CONNECT\r>"))
    }

    @Test
    fun `decodeDtcResponse decodes P C B U trouble codes from CAN frames`() {
        // Mode 03 response with 2 codes: P0300 (03 00) and P0456 (04 56) plus U0100 (C1 00)
        // Byte 1=0xC1 -> binary 1100_0001 -> 'U' (11), d1=0, d2=1, byte2=0x00 -> U0100
        val dtcs = Elm327Protocol.decodeDtcResponse("43 03 00 04 56 C1 00\r>", 0x43)
        assertEquals(listOf("P0300", "P0456", "U0100"), dtcs)
    }

    @Test
    fun `decodeDtcResponse handles CAN DTC count byte and zero padding`() {
        // 43 [02 count] 01 28 50 20 00 00 -> P0128, C1020
        // 0x50 = 0101_0000 -> 'C' (01), d1=1, d2=0, 0x20 -> C1020
        val dtcs = Elm327Protocol.decodeDtcResponse("43 02 01 28 50 20 00 00", 0x43)
        assertEquals(listOf("P0128", "C1020"), dtcs)
    }

    @Test
    fun `decodeVinResponse decodes 2015 Jeep Wrangler JK VIN`() {
        // 1C4BJWDG5FL123456 in ASCII hex
        val vin = "1C4BJWDG5FL123456"
        val hexPayload = vin.map { it.code.toString(16).uppercase().padStart(2, '0') }.joinToString(" ")
        val rawFrame = "0: 49 02 01 ${hexPayload.take(11)}\n1: ${hexPayload.drop(12)}"
        val decoded = Elm327Protocol.decodeVinResponse(rawFrame)
        assertNotNull(decoded)
        assertEquals(vin, decoded!!.vin)
        assertEquals("2015", decoded.modelYear)
        assertTrue(decoded.isWranglerJk)
    }

    @Test
    fun `decodeMode01 and decodeVinResponse handle stripped CR LF responses`() {
        val rpmAfterSearching = Elm327Protocol.decodeMode01("SEARCHING... 410C0BB8")
        assertEquals(Elm327Protocol.DecodedPid.Rpm(750), rpmAfterSearching)

        val vin = "1C4BJWDG5FL123456"
        val compactHex = vin.map { it.code.toString(16).uppercase().padStart(2, '0') }.joinToString("")
        val strippedVin = "0:490201${compactHex.take(6)} 1:${compactHex.substring(6, 20)} 2:${compactHex.drop(20)}"
        val decodedVin = Elm327Protocol.decodeVinResponse(strippedVin)
        assertNotNull(decodedVin)
        assertEquals(vin, decodedVin!!.vin)

        // CAN header "7E8 03 ..." and echoed PID guard for Coolant (0105) & Fuel Level (012F)
        val canCoolant = Elm327Protocol.decodeMode01("7E8 03 41 05 82", expectedPid = 0x05)
        assertEquals(Elm327Protocol.DecodedPid.CoolantTemp(90), canCoolant)

        val unspacedCanCoolant = Elm327Protocol.decodeMode01("7E803410582", expectedPid = 0x05)
        assertEquals(Elm327Protocol.DecodedPid.CoolantTemp(90), unspacedCanCoolant)

        val echoedFuel = Elm327Protocol.decodeMode01("012F 412F9A", expectedPid = 0x2F)
        assertTrue(echoedFuel is Elm327Protocol.DecodedPid.FuelLevel)
        assertEquals((0x9A * 100f) / 255f, (echoedFuel as Elm327Protocol.DecodedPid.FuelLevel).percent, 0.1f)

        // OBD Speed (010D) in KPH: stationary 0 KPH, CAN header, echo, and multi-ECU tolerance
        assertEquals(
            Elm327Protocol.DecodedPid.Speed(0),
            Elm327Protocol.decodeMode01("41 0D 00", expectedPid = 0x0D)
        )
        assertEquals(
            Elm327Protocol.DecodedPid.Speed(100),
            Elm327Protocol.decodeMode01("7E8 03 41 0D 64", expectedPid = 0x0D)
        )
        assertEquals(
            Elm327Protocol.DecodedPid.Speed(72),
            Elm327Protocol.decodeMode01("010D\r41 0D 48\r>", expectedPid = 0x0D)
        )
        assertEquals(
            Elm327Protocol.DecodedPid.Speed(80),
            Elm327Protocol.decodeMode01("7E8 03 41 0D 50\r7E9 03 7F 01 11", expectedPid = 0x0D)
        )
    }

    @Test
    fun `jk diagnostic library resolves known and unknown codes`() {
        val p0456 = JkDiagnosticLibrary.lookupCode("p0456")
        assertEquals("P0456", p0456.code)
        assertEquals(JkSubsystem.EVAP, p0456.subsystem)

        val unknown = JkDiagnosticLibrary.lookupCode("U1412")
        assertEquals("U1412", unknown.code)
        assertEquals(JkSubsystem.TIPM_NETWORK, unknown.subsystem)

        val jlUnknown = JkDiagnosticLibrary.lookupCode("P1234", WranglerModel.JL)
        assertTrue(jlUnknown.trailGuidance.contains("SGW", ignoreCase = true))
    }

    @Test
    fun `decodeVinResponse decodes 2021 Wrangler JL 2_0T 4xe EcoDiesel and 392 VINs`() {
        // 2021 Wrangler JL 2.0T (char4='J', char5='X', char8='N', char10='M', char11='L')
        val vin20t = "1C4HJXDN9MW654321"
        val hex20t = vin20t.map { it.code.toString(16).uppercase().padStart(2, '0') }.joinToString(" ")
        val decoded20t = Elm327Protocol.decodeVinResponse("0: 49 02 01 $hex20t")
        assertNotNull(decoded20t)
        assertEquals(vin20t, decoded20t!!.vin)
        assertEquals("2021", decoded20t.modelYear)
        assertEquals(WranglerModel.JL, decoded20t.detectedModel)
        assertFalse(decoded20t.isWranglerJk)
        assertTrue(decoded20t.engineHint?.contains("2.0L") == true)

        // 2023 Wrangler JL 392 HEMI (char8='J', char10='P')
        val vin392 = "1C4JJXSJ5PW500001"
        val hex392 = vin392.map { it.code.toString(16).uppercase().padStart(2, '0') }.joinToString("")
        val decoded392 = Elm327Protocol.decodeVinResponse("490201$hex392")
        assertNotNull(decoded392)
        assertEquals("2023", decoded392!!.modelYear)
        assertEquals(WranglerModel.JL, decoded392.detectedModel)
        assertTrue(decoded392.engineHint?.contains("392") == true)

        // 2022 Wrangler JL 4xe (char8='P', char10='N')
        val vin4xe = "1C4JJXEP7NW100001"
        val hex4xe = vin4xe.map { it.code.toString(16).uppercase().padStart(2, '0') }.joinToString(" ")
        val decoded4xe = Elm327Protocol.decodeVinResponse("49 02 01 $hex4xe")
        assertNotNull(decoded4xe)
        assertEquals(WranglerModel.JL, decoded4xe!!.detectedModel)
        assertTrue(decoded4xe.engineHint?.contains("4xe") == true)
    }

    @Test
    fun `elm327 decodes Wrangler JL PIDs and detects SGW NRC 33`() {
        // 015C Engine Oil Temp: 0x8C (140) - 40 = 100 C
        val oil = Elm327Protocol.decodeMode01("41 5C 8C", expectedPid = 0x5C)
        assertEquals(Elm327Protocol.DecodedPid.EngineOilTemp(100), oil)

        // 0123 Fuel Rail Pressure: 0x05 DC = 1500 -> * 10 = 15000 kPa
        val rail = Elm327Protocol.decodeMode01("41 23 05 DC", expectedPid = 0x23)
        assertEquals(Elm327Protocol.DecodedPid.FuelRailPressureGauge(15000), rail)

        // 0122 Rel Fuel Rail: 0x01 00 = 256 -> * 0.079 = 20.224 kPa
        val railRel = Elm327Protocol.decodeMode01("41 22 01 00", expectedPid = 0x22)
        assertTrue(railRel is Elm327Protocol.DecodedPid.FuelRailPressureRel)
        assertEquals(20.224f, (railRel as Elm327Protocol.DecodedPid.FuelRailPressureRel).kpa, 0.1f)

        // 0170 Boost Pressure Control: bytes B,C = 0x05 DC = 1500 -> * 0.03125 = 46.875 kPa
        val boost = Elm327Protocol.decodeMode01("41 70 01 05 DC 00 00", expectedPid = 0x70)
        assertTrue(boost is Elm327Protocol.DecodedPid.BoostPressureControl)
        assertEquals(46.875f, (boost as Elm327Protocol.DecodedPid.BoostPressureControl).kpa, 0.1f)

        // 0177 Charge Air Cooler Temp: byte B = 0x5A (90) - 40 = 50 C
        val cac = Elm327Protocol.decodeMode01("41 77 01 5A 00", expectedPid = 0x77)
        assertEquals(Elm327Protocol.DecodedPid.ChargeAirCoolerTemp(50), cac)

        // 015B Hybrid Battery Remaining: 0xCC (204) -> 204 * 100 / 255 = 80.0%
        val hv = Elm327Protocol.decodeMode01("41 5B CC", expectedPid = 0x5B)
        assertTrue(hv is Elm327Protocol.DecodedPid.HybridBatteryRemaining)
        assertEquals(80.0f, (hv as Elm327Protocol.DecodedPid.HybridBatteryRemaining).percent, 0.1f)

        // 0151 Fuel Type: 0x01 Gasoline, 0x11 Hybrid Gasoline, 0x17 PHEV (4xe)
        val gas = Elm327Protocol.decodeMode01("41 51 01", expectedPid = 0x51) as Elm327Protocol.DecodedPid.FuelType
        assertEquals("Gasoline", gas.typeName)
        val hybrid = Elm327Protocol.decodeMode01("41 51 11", expectedPid = 0x51) as Elm327Protocol.DecodedPid.FuelType
        assertEquals("Hybrid Gasoline", hybrid.typeName)
        val phev = Elm327Protocol.decodeMode01("41 51 17", expectedPid = 0x51) as Elm327Protocol.DecodedPid.FuelType
        assertEquals("PHEV (4xe)", phev.typeName)

        // Security Gateway (SGW) Mode 04 NRC 0x33 detection
        assertTrue(Elm327Protocol.isSecurityAccessDenied("7E8 03 7F 04 33\r>"))
        assertTrue(Elm327Protocol.isSecurityAccessDenied("7F0422"))
        assertFalse(Elm327Protocol.isSecurityAccessDenied("44\r>"))

        // Mode 06 Cyl 7 & Cyl 8 (392 HEMI)
        val cyl7 = Elm327Protocol.decodeMode06Misfire("46 A8 0C 24 00 04 00 00 FF FF", expectedMid = 0xA8)
        assertNotNull(cyl7)
        assertEquals(7, cyl7!!.cylinder)
        assertEquals(4, cyl7.displayCount)

        val cyl8 = Elm327Protocol.decodeMode06Misfire("46 A9 0C 24 00 01 00 00 FF FF", expectedMid = 0xA9)
        assertNotNull(cyl8)
        assertEquals(8, cyl8!!.cylinder)
        assertEquals(1, cyl8.displayCount)
    }

    @Test
    fun `wrangler dtc catalog covers JL and JK specific codes`() {
        val codes = WranglerDtcCatalog.allCodes.associateBy { it.code }
        assertTrue(codes.size >= 50)
        assertTrue(WranglerModel.JL in codes["P00B7"]!!.models)
        assertTrue(WranglerModel.JL in codes["P20EE"]!!.models)
        assertTrue(WranglerModel.JL in codes["P0BBE"]!!.models)
        assertTrue(WranglerModel.JL in codes["U0140"]!!.models)
        assertTrue(WranglerModel.JK in codes["C140F"]!!.models)
        assertEquals(JkSubsystem.ABS_CHASSIS, codes["C140F"]!!.subsystem)
    }
}
