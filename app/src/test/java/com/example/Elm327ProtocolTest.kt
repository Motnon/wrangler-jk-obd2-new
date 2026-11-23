package com.example

import com.example.obd.Elm327Protocol
import com.example.obd.JkDiagnosticLibrary
import com.example.obd.JkSubsystem
import org.junit.Assert.assertEquals
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
    }
}
