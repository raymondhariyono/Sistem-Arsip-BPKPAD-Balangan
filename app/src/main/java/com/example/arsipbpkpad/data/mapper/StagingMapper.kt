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
        copyType = copyType,
        copyCount = copyCount,
        documentNumber = documentNumber,
        classificationCode = classificationCode,
        description = description,
        nominal = nominal,
        year = year,
        condition = condition,
        status = status,
        metadata = metadata,
        idStorageLocation = idStorageLocation,
        bundleId = bundleId,
        createdBy = createdBy,
        verifiedBy = verifiedBy
    )
}

fun ArchiveDocument.toStagingEntity(): StagingArchiveEntity {
    return StagingArchiveEntity(
        id = id,
        boxSessionId = boxSessionId ?: "",
        type = type,
        copyType = copyType,
        copyCount = copyCount,
        documentNumber = documentNumber,
        classificationCode = classificationCode,
        description = description,
        nominal = nominal,
        year = year,
        condition = condition,
        status = status,
        metadata = metadata,
        idStorageLocation = idStorageLocation,
        bundleId = bundleId,
        createdBy = createdBy,
        verifiedBy = verifiedBy
    )
}

fun StagingBoxEntity.toDomain(itemCount: Int = 0): StagedBox {
    return StagedBox(
        sessionId = sessionId,
        warehouse = warehouse,
        rack = rack,
        box = box,
        year = year,
        itemCount = itemCount
    )
}

fun StagedBox.toEntity(): StagingBoxEntity {
    return StagingBoxEntity(
        sessionId = sessionId,
        warehouse = warehouse,
        rack = rack,
        box = box,
        year = year
    )
}
