package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.data.local.entity.StagingArchiveEntity
import com.example.arsipbpkpad.data.local.entity.StagingBoxEntity
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.StagedBox

fun StagingArchiveEntity.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id,
        boxSessionId = boxSessionId,
        type = type,
        copyStatus = copyStatus,
        documentNumber = documentNumber,
        nominal = nominal,
        thirdParty = thirdParty,
        year = year,
        dateIssued = dateIssued,
        status = status,
        idStorageLocation = idStorageLocation,
        imageUrl = imageUrl,
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
        boxSessionId = boxSessionId ?: "",
        type = type,
        copyStatus = copyStatus,
        documentNumber = documentNumber,
        nominal = nominal,
        thirdParty = thirdParty,
        year = year,
        dateIssued = dateIssued,
        status = status,
        idStorageLocation = idStorageLocation,
        imageUrl = imageUrl,
        metadata = metadata,
        createdBy = createdBy,
        verifiedBy = verifiedBy
    )
}

fun StagingBoxEntity.toDomain(itemCount: Int = 0): StagedBox {
    return StagedBox(
        sessionId = sessionId,
        warehouse = warehouse,
        rack = rack,
        box = boxNumber,
        year = year,
        itemCount = itemCount
    )
}

fun StagedBox.toEntity(): StagingBoxEntity {
    return StagingBoxEntity(
        sessionId = sessionId,
        warehouse = warehouse,
        rack = rack,
        boxNumber = box,
        year = year
    )
}
