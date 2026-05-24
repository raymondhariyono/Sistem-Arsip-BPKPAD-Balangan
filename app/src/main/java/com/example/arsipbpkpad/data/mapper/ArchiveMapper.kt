package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument

fun ArchiveEntity.toDomain(): ArchiveDocument {
    return ArchiveDocument(
        id = id,
        title = title,
        description = description,
        date = date,
        category = category,
        imageUrl = imageUrl
    )
}

fun ArchiveDocument.toEntity(): ArchiveEntity {
    return ArchiveEntity(
        id = id,
        title = title,
        description = description,
        date = date,
        category = category,
        imageUrl = imageUrl
    )
}
