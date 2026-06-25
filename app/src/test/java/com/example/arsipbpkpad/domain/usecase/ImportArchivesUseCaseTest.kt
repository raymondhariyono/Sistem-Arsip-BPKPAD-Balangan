package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.domain.service.ExcelService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.InputStream

class ImportArchivesUseCaseTest {

    private val archiveRepository = mockk<ArchiveRepository>(relaxed = true)
    private val excelService = mockk<ExcelService>()
    private val storageLocationRepository = mockk<StorageLocationRepository>()
    private val inputStream = mockk<InputStream>()

    private lateinit var importArchivesUseCase: ImportArchivesUseCase

    @Before
    fun setup() {
        importArchivesUseCase = ImportArchivesUseCase(
            archiveRepository,
            excelService,
            storageLocationRepository
        )
    }

    @Test
    fun `invoke returns error if metadata is missing`() = runTest {
        val archive = createArchive("1", "DOC-001", null, null, null).copy(metadata = null)
        coEvery { excelService.importFromExcel(any()) } returns listOf(archive)

        val result = importArchivesUseCase(inputStream)

        assertTrue(result is DomainResult.Error)
        assertEquals("Metadata hilang pada baris 2", (result as DomainResult.Error).message)
    }

    @Test
    fun `invoke returns error if location data is incomplete`() = runTest {
        val archive = createArchive("1", "DOC-001", "G-1", null, "B-1")
        coEvery { excelService.importFromExcel(any()) } returns listOf(archive)

        val result = importArchivesUseCase(inputStream)

        assertTrue(result is DomainResult.Error)
        assertEquals("Data lokasi (Gudang/Rak/Box) tidak lengkap pada baris 2", (result as DomainResult.Error).message)
    }

    @Test
    fun `invoke returns error if getOrCreateLocation fails`() = runTest {
        val archive = createArchive("1", "DOC-001", "G-1", "R-1", "B-1")
        coEvery { excelService.importFromExcel(any()) } returns listOf(archive)
        coEvery { storageLocationRepository.getOrCreateLocation(any(), any(), any(), any()) } returns DomainResult.Error("Repository Error")

        val result = importArchivesUseCase(inputStream)

        assertTrue(result is DomainResult.Error)
        assertTrue((result as DomainResult.Error).message.contains("Gagal memproses lokasi pada baris 2"))
    }

    private fun createArchive(
        id: String,
        docNumber: String,
        warehouse: String?,
        rack: String?,
        box: String?
    ): ArchiveDocument {
        return ArchiveDocument(
            id = id,
            type = DocType.SP2D,
            copyType = DocCopyType.ORIGINAL,
            copyCount = 1,
            documentNumber = docNumber,
            description = "Test",
            nominal = 1000.0,
            year = 2026,
            condition = DocCondition.GOOD,
            status = DocStatus.AVAILABLE,
            metadata = ArchiveMetadata(warehouse = warehouse, rack = rack, boxNumber = box),
            idStorageLocation = null,
            bundleId = null
        )
    }
}
