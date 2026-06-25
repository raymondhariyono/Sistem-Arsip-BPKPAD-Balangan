package com.example.arsipbpkpad.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArchiveDaoSearchTest {
    private lateinit var archiveDao: ArchiveDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        archiveDao = db.archiveDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testSearchByKeyword() = runBlocking {
        val archives = listOf(
            createArchive("1", "SP2D-001", description = "Belanja ATK"),
            createArchive("2", "SPM-002", description = "Gaji Pegawai"),
            createArchive("3", "SP2D-003", description = "Belanja Modal")
        )
        archiveDao.insertArchives(archives)

        // Search by document number
        val resultDoc = archiveDao.getArchivesList("SP2D", emptyList(), true).first()
        assertEquals(2, resultDoc.size)

        // Search by description
        val resultDesc = archiveDao.getArchivesList("ATK", emptyList(), true).first()
        assertEquals(1, resultDesc.size)
        assertEquals("SP2D-001", resultDesc[0].documentNumber)

        // Search by description part
        val resultDescPart = archiveDao.getArchivesList("Belanja", emptyList(), true).first()
        assertEquals(2, resultDescPart.size)
    }

    @Test
    fun testFilterByYear() = runBlocking {
        val archives = listOf(
            createArchive("1", "DOC-1", year = 2024),
            createArchive("2", "DOC-2", year = 2025),
            createArchive("3", "DOC-3", year = 2026)
        )
        archiveDao.insertArchives(archives)

        val result = archiveDao.getArchivesList(null, listOf(2026), false).first()
        assertEquals(1, result.size)
        assertEquals(2026, result[0].year)
    }

    @Test
    fun testSqlInjectionInSearch() = runBlocking {
        val archives = listOf(
            createArchive("1", "DOC-1")
        )
        archiveDao.insertArchives(archives)

        // Try injection payload
        val injectionPayload = "' OR '1'='1"
        val result = archiveDao.getArchivesList(injectionPayload, emptyList(), true).first()
        
        // Should find nothing because it's treated as a literal string
        assertTrue(result.isEmpty())
    }

    private fun createArchive(id: String, docNumber: String, year: Int = 2026, description: String = "Test"): ArchiveEntity {
        return ArchiveEntity(
            id = id,
            type = DocType.SP2D,
            copyType = DocCopyType.ORIGINAL,
            copyCount = 1,
            documentNumber = docNumber,
            description = description,
            nominal = 1000.0,
            year = year,
            condition = DocCondition.GOOD,
            status = DocStatus.AVAILABLE,
            metadata = null,
            syncStatus = "SYNCED"
        )
    }
}
