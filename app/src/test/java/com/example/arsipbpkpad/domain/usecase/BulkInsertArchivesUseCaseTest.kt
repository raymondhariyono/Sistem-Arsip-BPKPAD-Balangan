package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.*
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.domain.repository.TransactionBundleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class BulkInsertArchivesUseCaseTest {

    private val archiveRepository = mockk<ArchiveRepository>()
    private val stagingRepository = mockk<StagingRepository>()
    private val storageLocationRepository = mockk<StorageLocationRepository>()
    private val transactionBundleRepository = mockk<TransactionBundleRepository>()

    private lateinit var useCase: BulkInsertArchivesUseCase

    @Before
    fun setup() {
        useCase = BulkInsertArchivesUseCase(
            archiveRepository,
            stagingRepository,
            storageLocationRepository,
            transactionBundleRepository
        )
    }

    @Test
    fun `BULK_001 - returns error if session not found`() = runTest {
        coEvery { stagingRepository.getStagedBoxById("invalid") } returns null

        val result = useCase("invalid")

        assertTrue(result is DomainResult.Error)
        assertEquals(DomainConstants.ERROR_SESSION_NOT_FOUND, (result as DomainResult.Error).message)
    }

    @Test
    fun `BULK_002 - returns error if staging is empty`() = runTest {
        val sessionId = "session-123"
        coEvery { stagingRepository.getStagedBoxById(sessionId) } returns StagedBox(sessionId, "Gedung A", "R1", "B1", "2026")
        coEvery { stagingRepository.getStagingArchivesBySession(sessionId) } returns flowOf(emptyList())

        val result = useCase(sessionId)

        assertTrue(result is DomainResult.Error)
        assertEquals(DomainConstants.ERROR_STAGING_EMPTY, (result as DomainResult.Error).message)
    }

    @Test
    fun `BULK_003 - returns error if location creation fails`() = runTest {
        val sessionId = "session-123"
        coEvery { stagingRepository.getStagedBoxById(sessionId) } returns StagedBox(sessionId, "Gedung A", "R1", "B1", "2026")
        coEvery { stagingRepository.getStagingArchivesBySession(sessionId) } returns flowOf(listOf(mockk(relaxed = true)))
        coEvery { storageLocationRepository.getOrCreateLocation(any(), any(), any(), any()) } returns DomainResult.Error("Network Error")

        val result = useCase(sessionId)

        assertTrue(result is DomainResult.Error)
        assertTrue((result as DomainResult.Error).message.contains(DomainConstants.ERROR_LOCATION_INIT_FAILED))
    }

    @Test
    fun `BULK_004 - successful upload maps location id and cleans up`() = runTest {
        val sessionId = "session-123"
        val storageId = "box-uuid-456"
        val stagedDoc = ArchiveDocument(
            id = "doc-1",
            type = DocType.SP2D,
            documentNumber = "SP2D-001",
            copyType = DocCopyType.ORIGINAL,
            description = "Desc",
            nominal = 1000.0,
            year = 2026,
            status = DocStatus.UNVERIFIED,
            metadata = null
        )

        coEvery { stagingRepository.getStagedBoxById(sessionId) } returns StagedBox(sessionId, "Gedung A", "R1", "B1", "2026")
        coEvery { stagingRepository.getStagingArchivesBySession(sessionId) } returns flowOf(listOf(stagedDoc))
        coEvery { storageLocationRepository.getOrCreateLocation(any(), any(), any(), any()) } returns DomainResult.Success(storageId)
        coEvery { archiveRepository.saveArchives(any()) } returns DomainResult.Success(Unit)
        coEvery { stagingRepository.deleteStagedBox(sessionId) } returns Unit

        val result = useCase(sessionId)

        assertTrue(result is DomainResult.Success)
        coVerify {
            archiveRepository.saveArchives(match {
                it.size == 1 && it[0].idStorageLocation == storageId && it[0].status == DocStatus.AVAILABLE
            })
        }
        coVerify { stagingRepository.deleteStagedBox(sessionId) }
    }

    @Test
    fun `BULK_005 - local bundle id is mapped to remote bundle id`() = runTest {
        val sessionId = "session-123"
        val localBundleId = "local-b-1"
        val remoteBundleId = "remote-b-uuid"

        val stagedDoc = ArchiveDocument(
            id = "doc-1",
            type = DocType.SP2D,
            documentNumber = "SP2D-001",
            copyType = DocCopyType.ORIGINAL,
            description = "Desc",
            nominal = 1000.0,
            year = 2026,
            status = DocStatus.UNVERIFIED,
            metadata = null,
            bundleId = localBundleId
        )

        coEvery { stagingRepository.getStagedBoxById(sessionId) } returns StagedBox(sessionId, "Gedung A", "R1", "B1", "2026")
        coEvery { stagingRepository.getStagingArchivesBySession(sessionId) } returns flowOf(listOf(stagedDoc))
        coEvery { storageLocationRepository.getOrCreateLocation(any(), any(), any(), any()) } returns DomainResult.Success("loc-1")
        coEvery { transactionBundleRepository.createBundle(any(), any(), any()) } returns DomainResult.Success(remoteBundleId)
        coEvery { archiveRepository.saveArchives(any()) } returns DomainResult.Success(Unit)
        coEvery { stagingRepository.deleteStagedBox(sessionId) } returns Unit

        val result = useCase(sessionId)

        assertTrue(result is DomainResult.Success)
        coVerify {
            archiveRepository.saveArchives(match {
                it[0].bundleId == remoteBundleId
            })
        }
    }
}
