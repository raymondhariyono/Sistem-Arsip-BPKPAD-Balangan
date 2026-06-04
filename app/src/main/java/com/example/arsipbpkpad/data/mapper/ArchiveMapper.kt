package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import com.example.arsipbpkpad.data.remote.dto.ArchiveDto
import com.example.arsipbpkpad.domain.model.ArchiveDocument

fun ArchiveDto.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id ?: "",
        type = type,
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
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ArchiveEntity.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id,
        type = type,
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
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ArchiveDocument.toEntity(syncStatus: String = "SYNCED"): ArchiveEntity {
    return ArchiveEntity(
        id = id,
        type = type,
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
        nominal = nominal,
        thirdParty = thirdParty,
        year = year,
        dateIssued = dateIssued,
        status = status,
        idStorageLocation = idStorageLocation,
        metadata = metadata,
        createdBy = createdBy,
        verifiedBy = verifiedBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
