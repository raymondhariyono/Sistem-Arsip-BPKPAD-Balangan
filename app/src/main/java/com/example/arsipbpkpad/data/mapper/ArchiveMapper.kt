package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import com.example.arsipbpkpad.data.remote.dto.ArchiveDto
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import java.util.UUID

fun ArchiveDto.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id ?: "",
        type = type,
        documentNumber = documentNumber,
        copyType = copyType,
        copyCount = copyCount,
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
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ArchiveEntity.toDomain(): ArchiveDocument {
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
        verifiedBy = verifiedBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ArchiveDocument.toEntity(syncStatus: String = "SYNCED"): ArchiveEntity {
    return ArchiveEntity(
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
        verifiedBy = verifiedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )
}

fun ArchiveDocument.toDto(): ArchiveDto {
    return ArchiveDto(
        id = if (id.isEmpty()) null else id,
        type = type,
        documentNumber = documentNumber,
        copyType = copyType,
        copyCount = copyCount,
        classificationCode = classificationCode,
        description = description,
        nominal = nominal,
        year = year,
        condition = condition,
        status = status,
        metadata = metadata,
        idStorageLocation = if (isValidUuid(idStorageLocation)) idStorageLocation else null,
        bundleId = if (isValidUuid(bundleId)) bundleId else null,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun isValidUuid(value: String?): Boolean {
    if (value.isNullOrBlank()) return false
    return try {
        UUID.fromString(value)
        true
    } catch (e: Exception) {
        false
    }
}
