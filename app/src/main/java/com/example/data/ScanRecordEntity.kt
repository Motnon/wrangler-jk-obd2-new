package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_records")
data class ScanRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestampMs: Long = System.currentTimeMillis(),
    val adapterName: String,
    val vin: String,
    val engineProfile: String,
    val milOn: Boolean,
    val activeCodesCsv: String,
    val pendingCodesCsv: String,
    val coolantCelsius: Int?,
    val batteryVolts: Float?,
    val notes: String = ""
) {
    val activeCodes: List<String>
        get() = activeCodesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val pendingCodes: List<String>
        get() = pendingCodesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
