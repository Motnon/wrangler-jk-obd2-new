package com.example.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.obd.TrailSensorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Streams real Android hardware sensor orientation for Wrangler JK off-road pitch, roll, and compass azimuth.
 */
class TrailSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravityValues = FloatArray(3)
    private val magneticValues = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var hasGravityReading = false
    private var hasMagneticReading = false

    private val _sensorState = MutableStateFlow(
        TrailSensorState(
            isSensorAvailable = gravitySensor != null,
            hasCompassSensor = magneticSensor != null
        )
    )
    val sensorState: StateFlow<TrailSensorState> = _sensorState.asStateFlow()

    fun start() {
        gravitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magneticSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.2f
        when (event.sensor.type) {
            Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER -> {
                for (i in 0..2) {
                    gravityValues[i] = if (hasGravityReading) {
                        gravityValues[i] + alpha * (event.values[i] - gravityValues[i])
                    } else {
                        event.values[i]
                    }
                }
                hasGravityReading = true
                updatePitchAndRoll()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                for (i in 0..2) {
                    magneticValues[i] = if (hasMagneticReading) {
                        magneticValues[i] + alpha * (event.values[i] - magneticValues[i])
                    } else {
                        event.values[i]
                    }
                }
                hasMagneticReading = true
                updateAzimuth()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun updatePitchAndRoll() {
        val gx = gravityValues[0]
        val gy = gravityValues[1]
        val gz = gravityValues[2]

        // Roll: lateral tilt left/right; Pitch: nose-up / nose-down when mounted vertically in JK dash
        val rollRad = atan2(gx.toDouble(), sqrt((gy * gy + gz * gz).toDouble()))
        val pitchRad = atan2(gz.toDouble(), sqrt((gx * gx + gy * gy).toDouble()))

        val rollDeg = Math.toDegrees(rollRad).toFloat().coerceIn(-60f, 60f)
        val pitchDeg = Math.toDegrees(pitchRad).toFloat().coerceIn(-60f, 60f)

        _sensorState.update {
            it.copy(
                pitchDegrees = pitchDeg,
                rollDegrees = rollDeg
            )
        }
    }

    private fun updateAzimuth() {
        if (!hasGravityReading || !hasMagneticReading) return
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magneticValues)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val rawDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val normalized = (rawDeg + 360f) % 360f
            _sensorState.update { it.copy(azimuthDegrees = normalized) }
        }
    }
}
