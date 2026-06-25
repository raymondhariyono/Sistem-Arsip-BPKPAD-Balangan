package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class ArchiveMapperTest {

    @Test
    fun `MAP_001 - toEntity and toDomain preserves storage location and metadata`() {
        val storageId = UUID.randomUUID().toString()
        val domain = ArchiveDocument(
            id = "1",
            type = DocType.SP2D,
            documentNumber = "DOC-001",
            classificationCode = "900.1.3.1",
            copyType = DocCopyType.ORIGINAL,
            copyCount = 1,
            nominal = 1000.0,
            description = "Test Description",
            year = 2026,
            condition = DocCondition.GOOD,
            status = DocStatus.AVAILABLE,
            idStorageLocation = storageId,
            metadata = ArchiveMetadata(
                warehouse = "Gedung A",
                rack = "Rak 1",
                boxNumber = "Box 1"
            )
        )

        val entity = domain.toEntity()
        assertEquals(storageId, entity.idStorageLocation)
        assertEquals("Gedung A", entity.metadata?.warehouse)
        assertEquals("Rak 1", entity.metadata?.rack)
        assertEquals("Box 1", entity.metadata?.boxNumber)

        val backToDomain = entity.toDomain()
        assertEquals(storageId, backToDomain.idStorageLocation)
        assertEquals("Gedung A", backToDomain.metadata?.warehouse)
        assertEquals("Rak 1", backToDomain.metadata?.rack)
        assertEquals("Box 1", backToDomain.metadata?.boxNumber)
    }

    @Test
    fun `MAP_002 - toDto validates UUID for storage location`() {
        val domain = ArchiveDocument(
            id = UUID.randomUUID().toString(),
            type = DocType.SP2D,
            documentNumber = "DOC-001",
            classificationCode = "900.1.3.1",
            copyType = DocCopyType.ORIGINAL,
            copyCount = 1,
            nominal = 1000.0,
            description = "Test Description",
            year = 2026,
            condition = DocCondition.GOOD,
            status = DocStatus.AVAILABLE,
            idStorageLocation = "invalid-uuid", // Invalid
            bundleId = "also-invalid",
            metadata = null
        )

        val dto = domain.toDto()
        assertNull(dto.idStorageLocation)
        assertNull(dto.bundleId)
        
        val validUuid = UUID.randomUUID().toString()
        val domainValid = domain.copy(idStorageLocation = validUuid)
        val dtoValid = domainValid.toDto()
        assertEquals(validUuid, dtoValid.idStorageLocation)
    }
}
