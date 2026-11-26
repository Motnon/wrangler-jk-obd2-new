package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.obd.CachedVehicleBinding
import com.example.obd.SimpleJson
import com.example.obd.SimpleJson.num
import com.example.obd.SimpleJson.str
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.vehicleCacheDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vehicle_adapter_cache"
)

/**
 * Local DataStore cache storing `{ bluetoothDeviceId: { vin, profileId, displayTitle, protocolName } }`
 * so repeat adapter connections immediately load the right vehicle profile while re-verifying VIN in background.
 */
class VehicleCacheRepository(private val context: Context) {

    private object Keys {
        val BINDINGS_JSON = stringPreferencesKey("bindings_json")
    }

    val bindingsFlow: Flow<Map<String, CachedVehicleBinding>> = context.vehicleCacheDataStore.data.map { prefs ->
        parseBindingsJson(prefs[Keys.BINDINGS_JSON].orEmpty())
    }

    suspend fun getCachedBinding(deviceAddress: String): CachedVehicleBinding? {
        val normalized = deviceAddress.trim().uppercase()
        if (normalized.isEmpty()) return null
        val prefs = context.vehicleCacheDataStore.data.first()
        return parseBindingsJson(prefs[Keys.BINDINGS_JSON].orEmpty())[normalized]
    }

    suspend fun saveBinding(binding: CachedVehicleBinding) {
        val key = binding.deviceAddress.trim().uppercase()
        if (key.isEmpty()) return
        context.vehicleCacheDataStore.edit { prefs ->
            val current = parseBindingsJson(prefs[Keys.BINDINGS_JSON].orEmpty()).toMutableMap()
            current[key] = binding.copy(deviceAddress = key)
            prefs[Keys.BINDINGS_JSON] = encodeBindingsJson(current)
        }
    }

    suspend fun clearBinding(deviceAddress: String) {
        val key = deviceAddress.trim().uppercase()
        context.vehicleCacheDataStore.edit { prefs ->
            val current = parseBindingsJson(prefs[Keys.BINDINGS_JSON].orEmpty()).toMutableMap()
            current.remove(key)
            prefs[Keys.BINDINGS_JSON] = encodeBindingsJson(current)
        }
    }

    companion object {
        internal fun parseBindingsJson(raw: String): Map<String, CachedVehicleBinding> {
            if (raw.isBlank()) return emptyMap()
            val root = runCatching { SimpleJson.parseObject(raw) }.getOrNull() ?: return emptyMap()
            val out = linkedMapOf<String, CachedVehicleBinding>()
            root.forEach { (addr, value) ->
                @Suppress("UNCHECKED_CAST")
                val obj = value as? Map<String, Any?> ?: return@forEach
                val profileId = obj.str("profileId")
                if (profileId.isBlank()) return@forEach
                out[addr.uppercase()] = CachedVehicleBinding(
                    deviceAddress = addr.uppercase(),
                    deviceName = obj.str("deviceName", addr),
                    vin = obj.str("vin").takeIf { it.isNotBlank() },
                    profileId = profileId,
                    displayTitle = obj.str("displayTitle", profileId),
                    protocolName = obj.str("protocolName").takeIf { it.isNotBlank() },
                    lastConnectedMs = obj.num("lastConnectedMs", 0.0).toLong()
                )
            }
            return out
        }

        internal fun encodeBindingsJson(map: Map<String, CachedVehicleBinding>): String {
            val sb = StringBuilder("{")
            map.entries.forEachIndexed { idx, (addr, b) ->
                if (idx > 0) sb.append(',')
                sb.append('"').append(escape(addr)).append("\":{")
                sb.append("\"deviceName\":\"").append(escape(b.deviceName)).append("\",")
                sb.append("\"vin\":\"").append(escape(b.vin.orEmpty())).append("\",")
                sb.append("\"profileId\":\"").append(escape(b.profileId)).append("\",")
                sb.append("\"displayTitle\":\"").append(escape(b.displayTitle)).append("\",")
                sb.append("\"protocolName\":\"").append(escape(b.protocolName.orEmpty())).append("\",")
                sb.append("\"lastConnectedMs\":").append(b.lastConnectedMs)
                sb.append('}')
            }
            sb.append('}')
            return sb.toString()
        }

        private fun escape(text: String): String {
            return text.replace("\\", "\\\\").replace("\"", "\\\"")
        }
    }
}
