package com.example.data

import kotlinx.coroutines.flow.Flow

class ScanHistoryRepository(private val dao: ScanRecordDao) {
    val allScans: Flow<List<ScanRecordEntity>> = dao.observeAllScans()

    suspend fun saveScan(record: ScanRecordEntity) {
        dao.insertScan(record)
    }

    suspend fun deleteScan(id: Long) {
        dao.deleteScanById(id)
    }

    suspend fun clearAll() {
        dao.clearAllScans()
    }
}
