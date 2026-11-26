package com.example.data

import com.example.obd.DtcCodeInfo
import com.example.obd.JkDiagnosticLibrary
import com.example.obd.JkSubsystem
import com.example.obd.WranglerDtcCatalog
import com.example.obd.WranglerModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DtcRepository(
    private val dtcCodeDao: DtcCodeDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun observeAllDtcs(): Flow<List<DtcCodeInfo>> =
        dtcCodeDao.observeAllDtcs().map { rows -> rows.map { it.toDomain() } }

    fun searchDtcs(
        query: String,
        subsystem: JkSubsystem,
        modelFilter: WranglerModel?
    ): Flow<List<DtcCodeInfo>> {
        val cleanQuery = query.trim()
        val modelCode = modelFilter?.code ?: ""
        return dtcCodeDao.searchDtcs(
            query = cleanQuery,
            subsystemName = subsystem.name,
            modelCode = modelCode
        ).map { rows -> rows.map { it.toDomain() } }
    }

    suspend fun lookupOffline(
        code: String,
        model: WranglerModel = WranglerModel.JK
    ): DtcCodeInfo = withContext(ioDispatcher) {
        val normalized = code.trim().uppercase()
        dtcCodeDao.findByCode(normalized)?.toDomain()
            ?: JkDiagnosticLibrary.lookupCode(normalized, model)
    }

    suspend fun ensurePrepopulated() = withContext(ioDispatcher) {
        val expected = WranglerDtcCatalog.allCodes
        val currentCount = dtcCodeDao.count()
        if (currentCount < expected.size) {
            dtcCodeDao.insertAll(expected.map { DtcCodeEntity.fromDomain(it) })
        }
    }
}
