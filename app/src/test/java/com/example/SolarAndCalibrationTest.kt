package com.example

import com.example.obd.applyCalibratedSpeedKph
import com.example.obd.parseTireSizeInches
import com.example.sensors.SolarTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SolarAndCalibrationTest {

    @Test
    fun `solar calculator detects day vs night using local sunrise and sunset`() {
        val tz = TimeZone.getTimeZone("America/Los_Angeles")
        val noon = Calendar.getInstance(tz).apply {
            set(2026, Calendar.JUNE, 21, 12, 30, 0)
        }
        val midnight = Calendar.getInstance(tz).apply {
            set(2026, Calendar.JUNE, 21, 23, 45, 0)
        }

        val daySchedule = SolarTimeCalculator.computeSchedule(34.05, -118.24, noon)
        val nightSchedule = SolarTimeCalculator.computeSchedule(34.05, -118.24, midnight)

        assertTrue(daySchedule.usingLocation)
        assertFalse(daySchedule.isNightNow)
        assertTrue(nightSchedule.isNightNow)
        assertTrue(daySchedule.sunriseMinuteOfDay in 240..480)
        assertTrue(daySchedule.sunsetMinuteOfDay in 1080..1320)
    }

    @Test
    fun `speed calibration and oversized tire parsing work accurately`() {
        val tire = parseTireSizeInches("35\" Mud Terrain")
        assertEquals(35f, tire!!, 0.01f)

        // 35" tires on 32" stock calibration (~1.094x) -> 100 KPH raw reads 109 KPH
        val factor = 35f / 32f
        assertEquals(109, applyCalibratedSpeedKph(100, factor))
        assertEquals(80, applyCalibratedSpeedKph(80, 1.0f))
    }

    @Test
    fun `default rig preferences use Day mode and locked gauges with night rotation off`() {
        val defaults = com.example.obd.RigPreferences()
        assertEquals(com.example.obd.AppVisualTheme.CLASSIC_WHITE, defaults.visualTheme)
        assertEquals(com.example.obd.AppVisualTheme.NEON_CYAN_AMBER, defaults.lastDarkTheme)
        assertEquals(com.example.obd.NightModeSetting.MANUAL_OFF, defaults.nightModeSetting)
        assertTrue(defaults.raceLayoutLocked)
    }

    @Test
    fun `effectiveSpeedFactor auto calculates Wrangler tire ratio and isolates non Wrangler vehicles`() {
        val jkPrefs = com.example.obd.RigPreferences(
            tireSizeInches = "35\"",
            stockTireInches = 32.0f,
            speedCalibrationFactor = 1.0f
        )
        val factorJk = com.example.obd.effectiveSpeedFactor(jkPrefs, null)
        assertEquals(35f / 32f, factorJk, 0.001f)

        val bmwProfile = com.example.obd.VehicleProfileLoader(context = null)
            .loadProfile(com.example.obd.VehicleProfileId.BMW)
        assertEquals(1.0f, com.example.obd.effectiveSpeedFactor(jkPrefs, bmwProfile), 0.001f)

        val manualBmw = jkPrefs.copy(speedCalibrationFactor = 1.045f)
        assertEquals(1.045f, com.example.obd.effectiveSpeedFactor(manualBmw, bmwProfile), 0.001f)
    }

    @Test
    fun `resolveRealSpeed combines OBD calibrated speed, GPS fallback, and zero-VSS protection`() {
        val prefs = com.example.obd.RigPreferences(
            tireSizeInches = "35\"",
            stockTireInches = 32.0f,
            speedCalibrationFactor = 1.0f
        )

        // 1) Calibrated OBD speed (100 raw * 35/32 = 109 KPH) when GPS is off
        val obdCal = com.example.obd.resolveRealSpeed(
            obdRawSpeedKmh = 100,
            gpsSpeedKph = null,
            gpsAccuracyMeters = null,
            rigPrefs = prefs
        )
        assertEquals(109, obdCal.kph)
        assertEquals(com.example.obd.RealSpeedSource.OBD_CALIBRATED, obdCal.source)

        // 2) Live GPS fallback when OBD 010D is unavailable
        val gpsOnly = com.example.obd.resolveRealSpeed(
            obdRawSpeedKmh = null,
            gpsSpeedKph = 78.6f,
            gpsAccuracyMeters = 8f,
            rigPrefs = prefs
        )
        assertEquals(79, gpsOnly.kph)
        assertEquals(com.example.obd.RealSpeedSource.GPS_LIVE, gpsOnly.source)

        // 3) Stationary GPS noise (< 2.5 KPH) clamps to 0 KPH
        val gpsStationaryNoise = com.example.obd.resolveRealSpeed(
            obdRawSpeedKmh = null,
            gpsSpeedKph = 1.8f,
            gpsAccuracyMeters = 10f,
            rigPrefs = prefs
        )
        assertEquals(0, gpsStationaryNoise.kph)

        // 4) Broken/zeroed VSS (0 KPH OBD) while GPS confirms driving (>= 8 KPH) uses GPS speed
        val vssZeroWhileDriving = com.example.obd.resolveRealSpeed(
            obdRawSpeedKmh = 0,
            gpsSpeedKph = 64.2f,
            gpsAccuracyMeters = 12f,
            rigPrefs = prefs
        )
        assertEquals(64, vssZeroWhileDriving.kph)
        assertEquals(com.example.obd.RealSpeedSource.GPS_LIVE, vssZeroWhileDriving.source)

        // 5) Re-geared/inaccurate ECU (>4% drift vs GPS) corrects to live GPS speed when uncalibrated
        val regearedDrift = com.example.obd.resolveRealSpeed(
            obdRawSpeedKmh = 80,
            gpsSpeedKph = 102.4f,
            gpsAccuracyMeters = 9f,
            rigPrefs = prefs
        )
        assertEquals(102, regearedDrift.kph)
        assertEquals(com.example.obd.RealSpeedSource.GPS_LIVE, regearedDrift.source)
    }
}
