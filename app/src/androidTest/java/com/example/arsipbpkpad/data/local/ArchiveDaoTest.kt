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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ArchiveDaoTest {
    private lateinit var archiveDao: ArchiveDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        archiveDao = db.archiveDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeArchiveAndReadInList() = runBlocking {
        val archive = ArchiveEntity(
            id = "1",
            type = DocType.SP2D,
            copyType = DocCopyType.ORIGINAL,
            copyCount = 1,
            documentNumber = "DOC-001",
            description = "Test Description",
            nominal = 1000000.0,
            year = 2026,
            condition = DocCondition.GOOD,
            status = DocStatus.AVAILABLE,
            metadata = null,
            syncStatus = "DRAFT"
        )
        archiveDao.insertArchive(archive)
        val pending = archiveDao.getPendingArchives()
        assertEquals(1, pending.size)
        assertEquals("DOC-001", pending[0].documentNumber)
        assertEquals("DRAFT", pending[0].syncStatus)
    }
}
