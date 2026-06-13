package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.data.local.entity.StagingArchiveEntity
import com.example.arsipbpkpad.domain.model.ArchiveDocument

fun StagingArchiveEntity.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id,
        type = type,
        copyStatus = copyStatus,
        documentNumber = documentNumber,
        nominal = nominal,
        thirdParty = thirdParty,
        year = year,
        dateIssued = dateIssued,
        status = status,
        idStorageLocation = idStorageLocation,
        metadata = metadata,
        createdBy = createdBy,
        verifiedBy = verifiedBy,
        createdAt = null,
        updatedAt = null
    )
}

fun ArchiveDocument.toStagingEntity(): StagingArchiveEntity {
    return StagingArchiveEntity(
        id = id,
        type = type,
        copyStatus = copyStatus,
        documentNumber = documentNumber,
        nominal = nominal,
        thirdParty = thirdParty,
        year = year,
        dateIssued = dateIssued,
        status = status,
        idStorageLocation = idStorageLocation,
        metadata = metadata,
        createdBy = createdBy,
        verifiedBy = verifiedBy
    )
}
