package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.obd.DtcCodeInfo
import com.example.obd.DtcSeverity
import com.example.obd.JkSubsystem
import com.example.obd.WranglerModel

@Entity(tableName = "dtc_codes")
data class DtcCodeEntity(
    @PrimaryKey val code: String,
    val title: String,
    val subsystemName: String,
    val severityName: String,
    val affectedEngines: String,
    val description: String,
    val trailGuidance: String,
    val supportsJk: Boolean,
    val supportsJl: Boolean
) {
    fun toDomain(): DtcCodeInfo {
        val subsystem = JkSubsystem.entries.firstOrNull { it.name == subsystemName }
            ?: JkSubsystem.POWERTRAIN
        val severity = DtcSeverity.entries.firstOrNull { it.name == severityName }
            ?: DtcSeverity.WARNING
        val models = buildSet {
            if (supportsJk) add(WranglerModel.JK)
            if (supportsJl) add(WranglerModel.JL)
            if (isEmpty()) add(WranglerModel.JK)
        }
        return DtcCodeInfo(
            code = code,
            title = title,
            subsystem = subsystem,
            severity = severity,
            affectedEngines = affectedEngines,
            description = description,
            trailGuidance = trailGuidance,
            models = models
        )
    }

    companion object {
        fun fromDomain(info: DtcCodeInfo): DtcCodeEntity {
            return DtcCodeEntity(
                code = info.code.trim().uppercase(),
                title = info.title,
                subsystemName = info.subsystem.name,
                severityName = info.severity.name,
                affectedEngines = info.affectedEngines,
                description = info.description,
                trailGuidance = info.trailGuidance,
                supportsJk = WranglerModel.JK in info.models,
                supportsJl = WranglerModel.JL in info.models
            )
        }
    }
}
