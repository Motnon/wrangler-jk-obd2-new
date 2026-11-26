package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DtcCodeDao {
    @Query("SELECT * FROM dtc_codes ORDER BY code ASC")
    fun observeAllDtcs(): Flow<List<DtcCodeEntity>>

    @Query(
        """
        SELECT * FROM dtc_codes
        WHERE (:modelCode = '' OR (:modelCode = 'JK' AND supportsJk = 1) OR (:modelCode = 'JL' AND supportsJl = 1))
          AND (:subsystemName = 'ALL' OR subsystemName = :subsystemName)
          AND (:query = ''
               OR UPPER(code) LIKE '%' || UPPER(:query) || '%'
               OR UPPER(title) LIKE '%' || UPPER(:query) || '%'
               OR UPPER(description) LIKE '%' || UPPER(:query) || '%'
               OR UPPER(affectedEngines) LIKE '%' || UPPER(:query) || '%')
        ORDER BY code ASC
        """
    )
    fun searchDtcs(query: String, subsystemName: String, modelCode: String): Flow<List<DtcCodeEntity>>

    @Query("SELECT * FROM dtc_codes WHERE code = :normalizedCode LIMIT 1")
    suspend fun findByCode(normalizedCode: String): DtcCodeEntity?

    @Query("SELECT COUNT(*) FROM dtc_codes")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(codes: List<DtcCodeEntity>)
}
