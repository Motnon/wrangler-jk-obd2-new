package com.example

import com.example.data.DtcCodeDao
import com.example.data.DtcCodeEntity
import com.example.data.DtcRepository
import com.example.obd.DtcSeverity
import com.example.obd.JkSubsystem
import com.example.obd.WranglerDtcCatalog
import com.example.obd.WranglerModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DtcRepositoryTest {

    private class FakeDtcCodeDao : DtcCodeDao {
        private val store = linkedMapOf<String, DtcCodeEntity>()
        private val snapshotFlow = MutableStateFlow<List<DtcCodeEntity>>(emptyList())
        var insertCallCount = 0
            private set

        private fun publish() {
            snapshotFlow.value = store.values.sortedBy { it.code }
        }

        override fun observeAllDtcs(): Flow<List<DtcCodeEntity>> = snapshotFlow

        override fun searchDtcs(
            query: String,
            subsystemName: String,
            modelCode: String
        ): Flow<List<DtcCodeEntity>> {
            val upperQuery = query.uppercase()
            return snapshotFlow.map { rows ->
                rows.filter { row ->
                    val matchesModel = when (modelCode) {
                        "JK" -> row.supportsJk
                        "JL" -> row.supportsJl
                        else -> true
                    }
                    val matchesSubsystem = subsystemName == "ALL" || row.subsystemName == subsystemName
                    val matchesQuery = upperQuery.isEmpty() ||
                        row.code.uppercase().contains(upperQuery) ||
                        row.title.uppercase().contains(upperQuery) ||
                        row.description.uppercase().contains(upperQuery) ||
                        row.affectedEngines.uppercase().contains(upperQuery)
                    matchesModel && matchesSubsystem && matchesQuery
                }
            }
        }

        override suspend fun findByCode(normalizedCode: String): DtcCodeEntity? {
            return store[normalizedCode]
        }

        override suspend fun count(): Int = store.size

        override suspend fun insertAll(codes: List<DtcCodeEntity>) {
            insertCallCount++
            codes.forEach { entity ->
                store[entity.code] = entity
            }
            publish()
        }
    }

    @Test
    fun `entity mapping round-trips domain DtcCodeInfo and normalizes code`() {
        val sample = WranglerDtcCatalog.allCodes.first { it.code == "P0340" }
        val lowercaseCopy = sample.copy(code = " p0340 ")

        val entity = DtcCodeEntity.fromDomain(lowercaseCopy)
        assertEquals("P0340", entity.code)
        assertTrue(entity.supportsJk)

        val restored = entity.toDomain()
        assertEquals("P0340", restored.code)
        assertEquals(sample.title, restored.title)
        assertEquals(sample.subsystem, restored.subsystem)
        assertEquals(sample.severity, restored.severity)
        assertEquals(sample.affectedEngines, restored.affectedEngines)
        assertEquals(sample.description, restored.description)
        assertEquals(sample.trailGuidance, restored.trailGuidance)
        assertEquals(sample.models, restored.models)
    }

    @Test
    fun `ensurePrepopulated seeds Wrangler JK catalog and is idempotent`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dao = FakeDtcCodeDao()
        val repo = DtcRepository(dao, dispatcher)

        repo.ensurePrepopulated()
        val seeded = repo.observeAllDtcs().first()
        assertEquals(WranglerDtcCatalog.allCodes.size, seeded.size)
        assertEquals(1, dao.insertCallCount)

        val expectedJkCodes = listOf(
            "P0340", "P0344", "P0335", "P0339", "P0507",
            "P0108", "P0455", "P0562", "P2096", "P2098",
            "P0325", "P0715", "C1014", "U110C", "B1630"
        )
        val seededCodes = seeded.map { it.code }.toSet()
        expectedJkCodes.forEach { code ->
            assertTrue("Expected JK DTC $code to be seeded", code in seededCodes)
        }

        // Calling again when already populated should not re-insert
        repo.ensurePrepopulated()
        assertEquals(1, dao.insertCallCount)
    }

    @Test
    fun `searchDtcs filters by JK model subsystem and keyword`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dao = FakeDtcCodeDao()
        val repo = DtcRepository(dao, dispatcher)
        repo.ensurePrepopulated()

        val jkOnly = repo.searchDtcs(
            query = "",
            subsystem = JkSubsystem.ALL,
            modelFilter = WranglerModel.JK
        ).first()
        assertTrue(jkOnly.isNotEmpty())
        assertTrue(jkOnly.all { WranglerModel.JK in it.models })
        assertFalse(jkOnly.any { it.code == "P056E" }) // JL-only hybrid battery code

        val jkEvap = repo.searchDtcs(
            query = "ESIM",
            subsystem = JkSubsystem.EVAP,
            modelFilter = WranglerModel.JK
        ).first()
        assertTrue(jkEvap.any { it.code == "P0456" })
        assertTrue(jkEvap.all { it.subsystem == JkSubsystem.EVAP })

        val jk38Search = repo.searchDtcs(
            query = "3.8L",
            subsystem = JkSubsystem.POWERTRAIN,
            modelFilter = WranglerModel.JK
        ).first()
        assertTrue(jk38Search.any { it.code == "P0300" || it.code == "P0325" })
    }

    @Test
    fun `lookupOffline returns Room entry or generic OBD fallback`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dao = FakeDtcCodeDao()
        val repo = DtcRepository(dao, dispatcher)
        repo.ensurePrepopulated()

        val camFault = repo.lookupOffline(" p0340 ", WranglerModel.JK)
        assertEquals("P0340", camFault.code)
        assertEquals(DtcSeverity.CRITICAL, camFault.severity)
        assertEquals(JkSubsystem.POWERTRAIN, camFault.subsystem)

        val unknownFault = repo.lookupOffline("P1999", WranglerModel.JK)
        assertEquals("P1999", unknownFault.code)
        assertEquals(JkSubsystem.POWERTRAIN, unknownFault.subsystem)
        assertTrue(unknownFault.title.contains("P1999"))
    }
}
