package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.obd.AppVisualTheme
import com.example.obd.JkEngineProfile
import com.example.obd.NightModeSetting
import com.example.obd.RigPreferences
import com.example.obd.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.rigDataStore: DataStore<Preferences> by preferencesDataStore(name = "wrangler_jk_rig_prefs")

class RigPreferencesRepository(private val context: Context) {

    private object Keys {
        val RIG_NICKNAME = stringPreferencesKey("rig_nickname")
        val ENGINE_PROFILE = stringPreferencesKey("engine_profile")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val TIRE_SIZE = stringPreferencesKey("tire_size")
        val STOCK_TIRE_INCHES = floatPreferencesKey("stock_tire_inches")
        val AXLE_RATIO = stringPreferencesKey("axle_ratio")
        val COOLANT_WARN_C = intPreferencesKey("coolant_warn_c")
        val LOW_BATT_VOLTS = floatPreferencesKey("low_batt_volts")
        val PITCH_TARE = floatPreferencesKey("pitch_tare")
        val ROLL_TARE = floatPreferencesKey("roll_tare")
        val RACE_LAYOUT_SPEC = stringPreferencesKey("race_layout_spec")
        val RACE_LAYOUT_LOCKED = booleanPreferencesKey("race_layout_locked")
        val VISUAL_THEME = stringPreferencesKey("visual_theme")
        val NIGHT_MODE_SETTING = stringPreferencesKey("night_mode_setting")
        val NIGHT_BRIGHTNESS_PCT = intPreferencesKey("night_brightness_pct")
        val SPEED_CALIBRATION_FACTOR = floatPreferencesKey("speed_calibration_factor")
    }

    val preferencesFlow: Flow<RigPreferences> = context.rigDataStore.data.map { prefs ->
        val engineName = prefs[Keys.ENGINE_PROFILE] ?: JkEngineProfile.PENTASTAR_3_6.name
        val unitName = prefs[Keys.UNIT_SYSTEM] ?: UnitSystem.METRIC.name
        val themeName = prefs[Keys.VISUAL_THEME] ?: AppVisualTheme.NEON_CYAN_AMBER.name
        val nightName = prefs[Keys.NIGHT_MODE_SETTING] ?: NightModeSetting.AUTO_SUN.name
        RigPreferences(
            rigNickname = prefs[Keys.RIG_NICKNAME] ?: "Wrangler JK",
            engineProfile = JkEngineProfile.entries.firstOrNull { it.name == engineName }
                ?: JkEngineProfile.PENTASTAR_3_6,
            unitSystem = UnitSystem.entries.firstOrNull { it.name == unitName }
                ?: UnitSystem.METRIC,
            tireSizeInches = prefs[Keys.TIRE_SIZE] ?: "35\"",
            stockTireInches = prefs[Keys.STOCK_TIRE_INCHES] ?: 32.0f,
            axleGearRatio = prefs[Keys.AXLE_RATIO] ?: "4.10",
            coolantWarnCelsius = prefs[Keys.COOLANT_WARN_C] ?: 108,
            lowBatteryVolts = prefs[Keys.LOW_BATT_VOLTS] ?: 11.8f,
            pitchTareDeg = prefs[Keys.PITCH_TARE] ?: 0f,
            rollTareDeg = prefs[Keys.ROLL_TARE] ?: 0f,
            raceLayoutSpec = prefs[Keys.RACE_LAYOUT_SPEC] ?: "",
            raceLayoutLocked = prefs[Keys.RACE_LAYOUT_LOCKED] ?: false,
            visualTheme = AppVisualTheme.entries.firstOrNull { it.name == themeName }
                ?: AppVisualTheme.NEON_CYAN_AMBER,
            nightModeSetting = NightModeSetting.entries.firstOrNull { it.name == nightName }
                ?: NightModeSetting.AUTO_SUN,
            nightBrightnessPercent = prefs[Keys.NIGHT_BRIGHTNESS_PCT] ?: 20,
            speedCalibrationFactor = prefs[Keys.SPEED_CALIBRATION_FACTOR] ?: 1.0f
        )
    }

    suspend fun updateRigDetails(nickname: String, tireSize: String, axleRatio: String) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.RIG_NICKNAME] = nickname.ifBlank { "Wrangler JK" }
            prefs[Keys.TIRE_SIZE] = tireSize.ifBlank { "33\"" }
            prefs[Keys.AXLE_RATIO] = axleRatio.ifBlank { "3.73" }
        }
    }

    suspend fun setEngineProfile(profile: JkEngineProfile) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.ENGINE_PROFILE] = profile.name
        }
    }

    suspend fun setUnitSystem(unitSystem: UnitSystem) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.UNIT_SYSTEM] = unitSystem.name
        }
    }

    suspend fun setAlertThresholds(coolantWarnCelsius: Int, lowBatteryVolts: Float) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.COOLANT_WARN_C] = coolantWarnCelsius.coerceIn(95, 120)
            prefs[Keys.LOW_BATT_VOLTS] = lowBatteryVolts.coerceIn(10.5f, 12.6f)
        }
    }

    suspend fun setInclinometerTare(pitchDeg: Float, rollDeg: Float) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.PITCH_TARE] = pitchDeg
            prefs[Keys.ROLL_TARE] = rollDeg
        }
    }

    suspend fun setRaceLayoutSpec(spec: String) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.RACE_LAYOUT_SPEC] = spec
        }
    }

    suspend fun setRaceLayoutLocked(locked: Boolean) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.RACE_LAYOUT_LOCKED] = locked
        }
    }

    suspend fun setVisualTheme(theme: AppVisualTheme) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.VISUAL_THEME] = theme.name
        }
    }

    suspend fun setNightModeSetting(setting: NightModeSetting) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.NIGHT_MODE_SETTING] = setting.name
        }
    }

    suspend fun setNightBrightnessPercent(percent: Int) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.NIGHT_BRIGHTNESS_PCT] = percent.coerceIn(5, 70)
        }
    }

    suspend fun setSpeedCalibration(factor: Float, stockTireInches: Float? = null) {
        context.rigDataStore.edit { prefs ->
            prefs[Keys.SPEED_CALIBRATION_FACTOR] = factor.coerceIn(0.70f, 1.45f)
            if (stockTireInches != null) {
                prefs[Keys.STOCK_TIRE_INCHES] = stockTireInches.coerceIn(27f, 36f)
            }
        }
    }
}
