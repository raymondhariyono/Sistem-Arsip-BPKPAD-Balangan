package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import com.example.arsipbpkpad.data.remote.dto.ArchiveDto
import com.example.arsipbpkpad.data.remote.dto.ArchiveMetadataDto
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import java.util.UUID

fun ArchiveDto.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id ?: "",
        type = try { DocType.valueOf(type) } catch (e: Exception) { DocType.SP2D },
        documentNumber = documentNumber,
        copyType = try { DocCopyType.valueOf(copyType) } catch (e: Exception) { DocCopyType.ORIGINAL },
        copyCount = copyCount,
        classificationCode = classificationCode,
        description = description,
        nominal = nominal,
        year = year,
        condition = try { DocCondition.valueOf(condition) } catch (e: Exception) { DocCondition.GOOD },
        status = try { DocStatus.valueOf(status) } catch (e: Exception) { DocStatus.AVAILABLE },
        metadata = metadata?.toDomain(),
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
        type = type.name,
        documentNumber = documentNumber,
        copyType = copyType.name,
        copyCount = copyCount,
        classificationCode = classificationCode,
        description = description,
        nominal = nominal,
        year = year,
        condition = condition.name,
        status = status.name,
        metadata = metadata?.toDto(),
        idStorageLocation = if (isValidUuid(idStorageLocation)) idStorageLocation else null,
        bundleId = if (isValidUuid(bundleId)) bundleId else null,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ArchiveMetadataDto.toDomain(): ArchiveMetadata {
    return ArchiveMetadata(
        bankName = bankName,
        accountNumber = accountNumber,
        paymentPurpose = paymentPurpose,
        budgetCode = budgetCode,
        warehouse = warehouse,
        rack = rack,
        boxNumber = boxNumber
    )
}

fun ArchiveMetadata.toDto(): ArchiveMetadataDto {
    return ArchiveMetadataDto(
        bankName = bankName,
        accountNumber = accountNumber,
        paymentPurpose = paymentPurpose,
        budgetCode = budgetCode,
        warehouse = warehouse,
        rack = rack,
        boxNumber = boxNumber
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
