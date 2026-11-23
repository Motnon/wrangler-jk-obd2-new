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
    fun `default rig preferences use Neon Cyan and Amber with night rotation off`() {
        val defaults = com.example.obd.RigPreferences()
        assertEquals(com.example.obd.AppVisualTheme.NEON_CYAN_AMBER, defaults.visualTheme)
        assertEquals(com.example.obd.AppVisualTheme.NEON_CYAN_AMBER, defaults.lastDarkTheme)
        assertEquals(com.example.obd.NightModeSetting.MANUAL_OFF, defaults.nightModeSetting)
    }
}
