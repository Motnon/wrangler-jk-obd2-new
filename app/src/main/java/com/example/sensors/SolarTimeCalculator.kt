package com.example.sensors

import java.util.Calendar
import java.util.Locale
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class SolarSchedule(
    val sunriseMinuteOfDay: Int,
    val sunsetMinuteOfDay: Int,
    val isNightNow: Boolean,
    val usingLocation: Boolean
) {
    fun formattedSummary(): String {
        val srH = sunriseMinuteOfDay / 60
        val srM = sunriseMinuteOfDay % 60
        val ssH = sunsetMinuteOfDay / 60
        val ssM = sunsetMinuteOfDay % 60
        return String.format(
            Locale.US,
            "Sunrise %02d:%02d • Sunset %02d:%02d%s",
            srH,
            srM,
            ssH,
            ssM,
            if (usingLocation) "" else " (Local Clock)"
        )
    }
}

/**
 * Pure NOAA solar equation calculator for local sunrise/sunset times.
 * Falls back to 06:30 / 18:30 if GPS coordinates are not yet available.
 */
object SolarTimeCalculator {

    private const val ZENITH_DEG = 90.833

    fun computeSchedule(
        latitude: Double?,
        longitude: Double?,
        now: Calendar = Calendar.getInstance()
    ): SolarSchedule {
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        if (latitude == null || longitude == null) {
            val defaultSunrise = 6 * 60 + 30
            val defaultSunset = 18 * 60 + 30
            val isNight = currentMinutes < defaultSunrise || currentMinutes >= defaultSunset
            return SolarSchedule(
                sunriseMinuteOfDay = defaultSunrise,
                sunsetMinuteOfDay = defaultSunset,
                isNightNow = isNight,
                usingLocation = false
            )
        }

        val dayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val tzOffsetHours = (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET)) / 3_600_000.0

        val sunriseMin = calculateEventMinutes(
            dayOfYear = dayOfYear,
            latitude = latitude,
            longitude = longitude,
            tzOffsetHours = tzOffsetHours,
            isSunrise = true
        ) ?: (6 * 60 + 30)

        val sunsetMin = calculateEventMinutes(
            dayOfYear = dayOfYear,
            latitude = latitude,
            longitude = longitude,
            tzOffsetHours = tzOffsetHours,
            isSunrise = false
        ) ?: (18 * 60 + 30)

        val isNight = if (sunriseMin < sunsetMin) {
            currentMinutes < sunriseMin || currentMinutes >= sunsetMin
        } else {
            currentMinutes in sunsetMin until sunriseMin
        }

        return SolarSchedule(
            sunriseMinuteOfDay = sunriseMin,
            sunsetMinuteOfDay = sunsetMin,
            isNightNow = isNight,
            usingLocation = true
        )
    }

    private fun calculateEventMinutes(
        dayOfYear: Int,
        latitude: Double,
        longitude: Double,
        tzOffsetHours: Double,
        isSunrise: Boolean
    ): Int? {
        val gamma = (2.0 * Math.PI / 365.0) * (dayOfYear - 1 + ((if (isSunrise) 6 else 18) - 12.0) / 24.0)
        val eqTime = 229.18 * (
            0.000075 +
                0.001868 * cos(gamma) -
                0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) -
                0.040849 * sin(2 * gamma)
            )
        val decl = 0.006918 -
            0.399912 * cos(gamma) +
            0.070257 * sin(gamma) -
            0.006758 * cos(2 * gamma) +
            0.000907 * sin(2 * gamma) -
            0.002697 * cos(3 * gamma) +
            0.00148 * sin(3 * gamma)

        val latRad = Math.toRadians(latitude)
        val zenithRad = Math.toRadians(ZENITH_DEG)
        val cosHa = (cos(zenithRad) / (cos(latRad) * cos(decl))) - tan(latRad) * tan(decl)
        if (cosHa < -1.0 || cosHa > 1.0) return null

        val haDeg = Math.toDegrees(acos(cosHa))
        val signedHa = if (isSunrise) haDeg else -haDeg
        val utcMinutes = 720.0 - 4.0 * (longitude + signedHa) - eqTime
        val localMinutes = utcMinutes + tzOffsetHours * 60.0
        val wrapped = ((localMinutes % 1440.0) + 1440.0) % 1440.0
        return floor(wrapped).toInt().coerceIn(0, 1439)
    }
}
