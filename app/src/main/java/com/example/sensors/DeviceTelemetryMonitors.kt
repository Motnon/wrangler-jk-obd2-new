package com.example.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PhoneBatteryState(
    val levelPercent: Int = 100,
    val isCharging: Boolean = false
)

data class GpsTelemetryState(
    val hasLocationPermission: Boolean = false,
    val isCalibrating: Boolean = false,
    val gpsSpeedKph: Float? = null,
    val accuracyMeters: Float? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val statusNote: String = "Location permission needed for live GPS speed"
)

class PhoneBatteryMonitor(private val context: Context) {

    private val _batteryState = MutableStateFlow(readStickyBattery())
    val batteryState: StateFlow<PhoneBatteryState> = _batteryState.asStateFlow()

    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent != null) {
                _batteryState.value = parseBatteryIntent(intent)
            }
        }
    }

    fun start() {
        if (registered) return
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        if (sticky != null) {
            _batteryState.value = parseBatteryIntent(sticky)
        }
        registered = true
    }

    fun stop() {
        if (!registered) return
        runCatching { context.unregisterReceiver(receiver) }
        registered = false
    }

    private fun readStickyBattery(): PhoneBatteryState {
        val sticky = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return if (sticky != null) parseBatteryIntent(sticky) else PhoneBatteryState()
    }

    private fun parseBatteryIntent(intent: Intent): PhoneBatteryState {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val pct = if (level >= 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else 100
        return PhoneBatteryState(levelPercent = pct, isCharging = charging)
    }
}

class GpsSpeedManager(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _gpsState = MutableStateFlow(GpsTelemetryState())
    val gpsState: StateFlow<GpsTelemetryState> = _gpsState.asStateFlow()

    private var lastFix: Location? = null
    private var activeListening = false
    private var passiveUpdatesActive = false
    private var activeIntervalMs = 0L

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val prev = lastFix
            val computedSpeedKph: Float? = when {
                location.hasSpeed() -> {
                    val kph = location.speed * 3.6f
                    if (kph < 1.2f) 0f else kph
                }
                prev != null && location.time > prev.time -> {
                    val dtSec = (location.time - prev.time) / 1000f
                    if (dtSec >= 0.5f) {
                        val calc = (prev.distanceTo(location) / dtSec) * 3.6f
                        calc.takeIf { it <= 280f }?.let { if (it < 1.2f) 0f else it }
                    } else {
                        null
                    }
                }
                else -> null
            }
            lastFix = location
            _gpsState.value = _gpsState.value.copy(
                hasLocationPermission = true,
                gpsSpeedKph = computedSpeedKph?.coerceAtLeast(0f),
                accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
                latitude = location.latitude,
                longitude = location.longitude,
                statusNote = if (computedSpeedKph != null) {
                    "GPS lock active (±${location.accuracy.toInt()} m)"
                } else {
                    "Acquiring GPS velocity fix…"
                }
            )
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            _gpsState.value = _gpsState.value.copy(
                statusNote = "Enable device GPS / Location for live speed calibration"
            )
        }
    }

    fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAnyLocationPermission(): Boolean {
        return hasFineLocationPermission() || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun refreshLastKnownCoordinates() {
        val hasPerm = hasAnyLocationPermission()
        val mgr = locationManager
        if (!hasPerm || mgr == null) {
            _gpsState.value = _gpsState.value.copy(hasLocationPermission = hasPerm)
            return
        }
        val best = listOfNotNull(
            runCatching { mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull(),
            runCatching { mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
        ).maxByOrNull { it.time }

        _gpsState.value = _gpsState.value.copy(
            hasLocationPermission = true,
            latitude = best?.latitude ?: _gpsState.value.latitude,
            longitude = best?.longitude ?: _gpsState.value.longitude,
            statusNote = if (_gpsState.value.isCalibrating) _gpsState.value.statusNote else "GPS ready"
        )
    }

    @SuppressLint("MissingPermission")
    fun startPassiveSpeedUpdates() {
        val mgr = locationManager
        if (!hasFineLocationPermission() || mgr == null) {
            refreshLastKnownCoordinates()
            return
        }
        passiveUpdatesActive = true
        refreshLastKnownCoordinates()
        requestGpsUpdates(mgr, minTimeMs = 1_000L, isCalibration = _gpsState.value.isCalibrating)
    }

    @SuppressLint("MissingPermission")
    fun startCalibrationUpdates() {
        val mgr = locationManager
        if (!hasFineLocationPermission() || mgr == null) {
            _gpsState.value = _gpsState.value.copy(
                hasLocationPermission = hasAnyLocationPermission(),
                isCalibrating = false,
                statusNote = "Fine location permission required for GPS speed"
            )
            return
        }
        refreshLastKnownCoordinates()
        lastFix = null
        _gpsState.value = _gpsState.value.copy(
            hasLocationPermission = true,
            isCalibrating = true,
            statusNote = "Sampling GPS velocity…"
        )
        requestGpsUpdates(mgr, minTimeMs = 500L, isCalibration = true)
    }

    @SuppressLint("MissingPermission")
    private fun requestGpsUpdates(mgr: LocationManager, minTimeMs: Long, isCalibration: Boolean) {
        if (activeListening && activeIntervalMs <= minTimeMs) return
        if (activeListening) {
            runCatching { mgr.removeUpdates(locationListener) }
            activeListening = false
        }
        activeListening = true
        activeIntervalMs = minTimeMs
        runCatching {
            mgr.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTimeMs,
                0f,
                locationListener,
                Looper.getMainLooper()
            )
        }.onFailure {
            activeListening = false
            activeIntervalMs = 0L
            if (isCalibration) {
                _gpsState.value = _gpsState.value.copy(
                    isCalibrating = false,
                    statusNote = "GPS provider unavailable on this device"
                )
            }
        }
    }

    fun stopCalibrationUpdates() {
        _gpsState.value = _gpsState.value.copy(isCalibrating = false)
        val mgr = locationManager
        if (passiveUpdatesActive && hasFineLocationPermission() && mgr != null) {
            if (activeListening && activeIntervalMs < 1_000L) {
                requestGpsUpdates(mgr, minTimeMs = 1_000L, isCalibration = false)
            }
        } else if (activeListening) {
            runCatching { mgr?.removeUpdates(locationListener) }
            activeListening = false
            activeIntervalMs = 0L
        }
    }

    fun stopAllUpdates() {
        passiveUpdatesActive = false
        if (activeListening) {
            runCatching { locationManager?.removeUpdates(locationListener) }
            activeListening = false
            activeIntervalMs = 0L
        }
        _gpsState.value = _gpsState.value.copy(isCalibrating = false)
    }
}
