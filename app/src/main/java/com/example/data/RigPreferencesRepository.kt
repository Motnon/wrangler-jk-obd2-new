package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.obd.JkEngineProfile
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
        val AXLE_RATIO = stringPreferencesKey("axle_ratio")
        val COOLANT_WARN_C = intPreferencesKey("coolant_warn_c")
        val LOW_BATT_VOLTS = floatPreferencesKey("low_batt_volts")
        val PITCH_TARE = floatPreferencesKey("pitch_tare")
        val ROLL_TARE = floatPreferencesKey("roll_tare")
    }

    val preferencesFlow: Flow<RigPreferences> = context.rigDataStore.data.map { prefs ->
        val engineName = prefs[Keys.ENGINE_PROFILE] ?: JkEngineProfile.PENTASTAR_3_6.name
        val unitName = prefs[Keys.UNIT_SYSTEM] ?: UnitSystem.IMPERIAL.name
        RigPreferences(
            rigNickname = prefs[Keys.RIG_NICKNAME] ?: "Wrangler JK",
            engineProfile = JkEngineProfile.entries.firstOrNull { it.name == engineName }
                ?: JkEngineProfile.PENTASTAR_3_6,
            unitSystem = UnitSystem.entries.firstOrNull { it.name == unitName }
                ?: UnitSystem.IMPERIAL,
            tireSizeInches = prefs[Keys.TIRE_SIZE] ?: "35\"",
            axleGearRatio = prefs[Keys.AXLE_RATIO] ?: "4.10",
            coolantWarnCelsius = prefs[Keys.COOLANT_WARN_C] ?: 108,
            lowBatteryVolts = prefs[Keys.LOW_BATT_VOLTS] ?: 11.8f,
            pitchTareDeg = prefs[Keys.PITCH_TARE] ?: 0f,
            rollTareDeg = prefs[Keys.ROLL_TARE] ?: 0f
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
}
