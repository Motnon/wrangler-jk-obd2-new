package com.example

import com.example.data.VehicleCacheRepository
import com.example.obd.BluetoothDeviceItem
import com.example.obd.CachedVehicleBinding
import com.example.obd.DetectionSource
import com.example.obd.DiagnosticReadingStatus
import com.example.obd.DiagnosticScanEngine
import com.example.obd.DiagnosticTestId
import com.example.obd.Elm327Protocol
import com.example.obd.TransportType
import com.example.obd.VehicleProfileId
import com.example.obd.VehicleProfileLoader
import com.example.obd.VinDecoder
import com.example.obd.WranglerModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleAutoDetectionTest {

    private fun asciiToHex(text: String, spaced: Boolean = true): String {
        val bytes = text.map { it.code.toString(16).uppercase().padStart(2, '0') }
        return if (spaced) bytes.joinToString(" ") else bytes.joinToString("")
    }

    @Test
    fun `dual-mode Vgate iCar Pro adapters are flagged for SPP and BLE`() {
        val spp = BluetoothDeviceItem(
            name = "ANDROID-VLINK",
            address = "00:1D:A5:68:98:8B",
            isPaired = true,
            transportType = TransportType.CLASSIC_SPP
        )
        val ble = BluetoothDeviceItem(
            name = "IOS-VLINK",
            address = "AA:BB:CC:DD:EE:FF",
            isPaired = false,
            transportType = TransportType.BLE_GATT
        )
        val generic = BluetoothDeviceItem(
            name = "Headset BT",
            address = "11:22:33:44:55:66",
            isPaired = true,
            transportType = TransportType.CLASSIC_SPP
        )

        assertTrue(spp.isVgateAdapter)
        assertEquals("SPP / VLINK", spp.transportType.badge)
        assertTrue(ble.isVgateAdapter)
        assertEquals("BLE 4.0", ble.transportType.badge)
        assertFalse(generic.isVgateAdapter)
    }

    @Test
    fun `vin decoder handles Jeep JK, Jeep JL, BMW, VAG, Toyota, Ford, Honda, Hyundai, Mercedes`() {
        val decoder = VinDecoder(context = null)

        val jk = decoder.decode("1C4BJWDG5FL123456")
        assertNotNull(jk)
        assertEquals("Jeep", jk!!.make)
        assertEquals("Wrangler JK", jk.model)
        assertEquals("2015", jk.modelYear)
        assertEquals(VehicleProfileId.JEEP_JK, jk.profileId)
        assertEquals(WranglerModel.JK, jk.detectedWranglerModel)
        assertEquals("2015 Jeep Wrangler JK", jk.displayTitle)

        val jl = decoder.decode("1C4HJXDN9MW654321")
        assertNotNull(jl)
        assertEquals(VehicleProfileId.JEEP_JL, jl!!.profileId)
        assertEquals(WranglerModel.JL, jl.detectedWranglerModel)
        assertEquals("2021 Jeep Wrangler JL", jl.displayTitle)

        val bmw = decoder.decode("WBA8B9C58JK123456")
        assertNotNull(bmw)
        assertEquals("BMW", bmw!!.make)
        assertEquals("2018", bmw.modelYear)
        assertEquals(VehicleProfileId.BMW, bmw.profileId)
        assertTrue(bmw.displayTitle.startsWith("2018 BMW 330i"))

        val vw = decoder.decode("WVWZZZAUZJW123456")
        assertNotNull(vw)
        assertEquals("Volkswagen", vw!!.make)
        assertEquals("2018", vw.modelYear)
        assertEquals(VehicleProfileId.UNIVERSAL_OBD2, vw.profileId)

        val toyota = decoder.decode("4T1B11HK5JU123456")
        assertNotNull(toyota)
        assertEquals("Toyota", toyota!!.make)
        assertEquals("2018", toyota.modelYear)
        assertEquals(VehicleProfileId.UNIVERSAL_OBD2, toyota.profileId)

        val ford = decoder.decode("1FTFW1E50KFA12345")
        assertNotNull(ford)
        assertEquals("Ford", ford!!.make)
        assertEquals("2019", ford.modelYear)

        val hyundai = decoder.decode("KMHLM4AG5MU123456")
        assertNotNull(hyundai)
        assertEquals("Hyundai", hyundai!!.make)
        assertEquals("2021", hyundai.modelYear)

        val honda = decoder.decode("1HGCV1F30LA123456")
        assertNotNull(honda)
        assertEquals("Honda", honda!!.make)
        assertEquals("2020", honda.modelYear)

        val mercedes = decoder.decode("WDDWF8DB5KR123456")
        assertNotNull(mercedes)
        assertEquals("Mercedes-Benz", mercedes!!.make)
        assertEquals("2019", mercedes.modelYear)
    }

    @Test
    fun `step 2 UDS 22F190 fallback extracts VIN from multi-frame 62F190 response`() {
        val bmwVin = "WBA8B9C58JK654321"
        val hex = asciiToHex(bmwVin, spaced = false) // 34 hex chars = 17 bytes
        val udsRaw = "014 0:62F190${hex.take(8)} 1:${hex.substring(8, 22)} 2:${hex.drop(22)}"

        assertNull(Elm327Protocol.extractStandardVinString("NO DATA\r>"))
        val extracted = Elm327Protocol.extractUdsVinString(udsRaw)
        assertEquals(bmwVin, extracted)

        val vehicle = VinDecoder(context = null).decode(
            rawVin = extracted!!,
            source = DetectionSource.VIN_UDS_22F190
        )
        assertNotNull(vehicle)
        assertEquals(DetectionSource.VIN_UDS_22F190, vehicle!!.detectionSource)
        assertEquals(VehicleProfileId.BMW, vehicle.profileId)
    }

    @Test
    fun `step 3 decodes 0100 bitmasks, 0904 Calibration ID, ATDPN protocol, and fingerprints legacy ECUs`() {
        // 0100 returning support for 0101, 0104, 0105, 010C, 010D, 0111, 0120
        // PIDs 01..08 = 0x98 (bits 7=01, 4=04, 3=05)
        // PIDs 09..16 = 0x18 (bits 4=0C, 3=0D)
        // PIDs 17..24 = 0x80 (bit 7=11)
        // PIDs 25..32 = 0x01 (bit 0=20)
        val supported00 = Elm327Protocol.decodeSupportedPidBitmask("41 00 98 18 80 01", basePid = 0x00)
        assertTrue(0x01 in supported00)
        assertTrue(0x04 in supported00)
        assertTrue(0x05 in supported00)
        assertTrue(0x0C in supported00)
        assertTrue(0x0D in supported00)
        assertTrue(0x11 in supported00)
        assertTrue(0x20 in supported00)
        assertFalse(0x0B in supported00)

        val calText = "68234512AA"
        val calHex = asciiToHex(calText, spaced = true)
        val parsedCal = Elm327Protocol.decodeCalibrationIdResponse("0: 49 04 01 $calHex")
        assertEquals(calText, parsedCal)

        val proto = Elm327Protocol.decodeProtocolNumberResponse("A6\r>")
        assertEquals("A6" to "ISO 15765-4 CAN (11-bit 500k)", proto)

        val protoIso = Elm327Protocol.decodeProtocolNumberResponse("3")
        assertEquals("3" to "ISO 9141-2", protoIso)

        val decoder = VinDecoder(context = null)
        val genericLegacy = decoder.fingerprintVehicle(
            protocolCode = "3",
            protocolName = "ISO 9141-2",
            calibrationId = "TOY987654",
            supportedPids = supported00
        )
        assertEquals(VehicleProfileId.UNIVERSAL_OBD2, genericLegacy.profileId)
        assertEquals(DetectionSource.ECU_FINGERPRINT, genericLegacy.detectionSource)
        assertTrue(genericLegacy.displayTitle.contains("ISO 9141-2"))
    }

    @Test
    fun `vehicle profile loader filters universal Mode 01 PIDs using ECU bitmask and seeds DiagnosticScanEngine`() {
        val loader = VehicleProfileLoader(context = null)
        val jkProfile = loader.loadProfile(VehicleProfileId.JEEP_JK)
        val bmwProfile = loader.loadProfile(VehicleProfileId.BMW)
        val universalProfile = loader.loadProfile(VehicleProfileId.UNIVERSAL_OBD2)

        assertTrue(jkProfile.pids.any { it.cmd == "22B003" })
        assertFalse(jkProfile.useSupportedPidMaskFiltering)
        assertTrue(bmwProfile.pids.any { it.cmd == "015C" })
        assertTrue(universalProfile.useSupportedPidMaskFiltering)

        val ecuBitmask = setOf(0x05, 0x0C, 0x0D, 0x11, 0x42)
        val filtered = loader.resolveActivePids(universalProfile, ecuBitmask)
        val cmds = filtered.map { it.cmd }.toSet()
        assertTrue("010C" in cmds)
        assertTrue("0105" in cmds)
        assertTrue("010D" in cmds)
        assertTrue("0142" in cmds)
        assertTrue("ATRV" in cmds) // non-Mode-01 retained
        assertFalse("0170" in cmds) // unsupported turbo boost filtered out!
        assertFalse("015B" in cmds) // unsupported hybrid SOC filtered out!

        // DiagnosticScanEngine skips unsupported tests cleanly once bitmask seeded
        val scanEngine = DiagnosticScanEngine()
        scanEngine.seedSupportedMode01Pids(ecuBitmask)
        val selected = setOf(DiagnosticTestId.ENGINE_RPM, DiagnosticTestId.COMMANDED_EGR)
        val plan = scanEngine.buildCommandPlan(selected)
        assertTrue("010C" in plan)
        assertFalse("012C" in plan)
        val readings = scanEngine.buildReadingsMap(selected, isConnected = true, speedCalibrationFactor = 1f, coolantWarnCelsius = 108)
        assertEquals(DiagnosticReadingStatus.UNSUPPORTED, readings[DiagnosticTestId.COMMANDED_EGR]?.status)
    }

    @Test
    fun `vehicle cache repository serializes and restores adapter to vehicle bindings`() {
        val sample = mapOf(
            "00:1D:A5:68:98:8B" to CachedVehicleBinding(
                deviceAddress = "00:1D:A5:68:98:8B",
                deviceName = "ANDROID-VLINK",
                vin = "WBA8B9C58JK123456",
                profileId = VehicleProfileId.BMW,
                displayTitle = "2018 BMW 330i",
                protocolName = "ISO 15765-4 CAN (11-bit 500k)",
                lastConnectedMs = 1_700_000_000_000L
            ),
            "AA:BB:CC:DD:EE:FF" to CachedVehicleBinding(
                deviceAddress = "AA:BB:CC:DD:EE:FF",
                deviceName = "IOS-VLINK",
                vin = "1C4BJWDG5FL123456",
                profileId = VehicleProfileId.JEEP_JK,
                displayTitle = "2015 Jeep Wrangler JK",
                protocolName = "ISO 15765-4 CAN (11-bit 500k)",
                lastConnectedMs = 1_700_000_100_000L
            )
        )

        val encoded = VehicleCacheRepository.encodeBindingsJson(sample)
        val decoded = VehicleCacheRepository.parseBindingsJson(encoded)

        assertEquals(2, decoded.size)
        val bmwCached = decoded["00:1D:A5:68:98:8B"]
        assertNotNull(bmwCached)
        assertEquals("2018 BMW 330i", bmwCached!!.displayTitle)
        assertEquals(VehicleProfileId.BMW, bmwCached.profileId)
        assertEquals("WBA8B9C58JK123456", bmwCached.vin)

        val jkCached = decoded["AA:BB:CC:DD:EE:FF"]
        assertNotNull(jkCached)
        assertEquals(VehicleProfileId.JEEP_JK, jkCached!!.profileId)
    }
}
