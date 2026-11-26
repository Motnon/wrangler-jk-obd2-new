package com.example.obd

import java.util.Locale
import kotlin.math.roundToInt

data class TripOdometerState(
    val tripDistanceKm: Double = 0.0,
    val activeMovingSeconds: Long = 0L,
    val currentEcuSpeedKph: Int? = null,
    val averageSpeedKph: Double = 0.0,
    val isAccumulating: Boolean = false
)

/**
 * Integrates vehicle distance traveled over time from live ECU vehicle speed telemetry (PID 010D).
 * Uses trapezoidal integration clamped to a safe sample window so brief disconnects or background
 * pauses never create phantom distance spikes.
 */
class TripOdometerTracker {
    private var totalDistanceKm: Double = 0.0
    private var totalMovingMillis: Long = 0L
    private var lastSampleTimestampMs: Long? = null
    private var lastEffectiveSpeedKph: Double? = null
    private var currentSpeedKph: Int? = null
    private var accumulating: Boolean = false

    fun restore(distanceKm: Double, movingSeconds: Long): TripOdometerState {
        totalDistanceKm = distanceKm.coerceAtLeast(0.0)
        totalMovingMillis = movingSeconds.coerceAtLeast(0L) * 1_000L
        return snapshot()
    }

    fun onEcuSpeedSample(
        rawEcuSpeedKmh: Int?,
        speedFactor: Float,
        timestampMs: Long,
        isConnected: Boolean
    ): TripOdometerState {
        if (!isConnected || rawEcuSpeedKmh == null || timestampMs <= 0L) {
            lastSampleTimestampMs = null
            lastEffectiveSpeedKph = null
            currentSpeedKph = null
            accumulating = false
            return snapshot()
        }

        val clampedFactor = speedFactor.coerceIn(0.70f, 1.45f).toDouble()
        val effectiveKph = rawEcuSpeedKmh.coerceAtLeast(0).toDouble() * clampedFactor

        val prevTime = lastSampleTimestampMs
        val prevSpeed = lastEffectiveSpeedKph
        if (prevTime != null && prevSpeed != null) {
            val dtMs = timestampMs - prevTime
            if (dtMs in 1L..MAX_SAMPLE_GAP_MS) {
                val avgSegmentKph = (prevSpeed + effectiveKph) / 2.0
                val deltaKm = avgSegmentKph * (dtMs.toDouble() / MS_PER_HOUR)
                totalDistanceKm += deltaKm
                if (effectiveKph > 0.5 || prevSpeed > 0.5) {
                    totalMovingMillis += dtMs
                }
            }
        }

        lastSampleTimestampMs = timestampMs
        lastEffectiveSpeedKph = effectiveKph
        currentSpeedKph = effectiveKph.roundToInt()
        accumulating = true
        return snapshot()
    }

    fun reset(currentTimestampMs: Long? = null): TripOdometerState {
        totalDistanceKm = 0.0
        totalMovingMillis = 0L
        if (currentTimestampMs != null && lastSampleTimestampMs != null) {
            lastSampleTimestampMs = currentTimestampMs
        } else {
            lastSampleTimestampMs = null
            lastEffectiveSpeedKph = null
        }
        return snapshot()
    }

    fun snapshot(): TripOdometerState {
        val movingSeconds = totalMovingMillis / 1_000L
        val avgKph = if (totalMovingMillis > 0L) {
            totalDistanceKm / (totalMovingMillis.toDouble() / MS_PER_HOUR)
        } else {
            0.0
        }
        return TripOdometerState(
            tripDistanceKm = totalDistanceKm,
            activeMovingSeconds = movingSeconds,
            currentEcuSpeedKph = currentSpeedKph,
            averageSpeedKph = avgKph,
            isAccumulating = accumulating
        )
    }

    companion object {
        private const val MS_PER_HOUR = 3_600_000.0
        private const val MAX_SAMPLE_GAP_MS = 5_000L

        fun formatDistanceKm(distanceKm: Double): String {
            val safe = distanceKm.coerceAtLeast(0.0)
            return if (safe >= 100.0) {
                String.format(Locale.US, "%.1f", safe)
            } else {
                String.format(Locale.US, "%.2f", safe)
            }
        }

        fun formatElapsedHms(totalSeconds: Long): String {
            val safe = totalSeconds.coerceAtLeast(0L)
            val hours = safe / 3600L
            val minutes = (safe % 3600L) / 60L
            val seconds = safe % 60L
            return if (hours > 0L) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
    }
}
