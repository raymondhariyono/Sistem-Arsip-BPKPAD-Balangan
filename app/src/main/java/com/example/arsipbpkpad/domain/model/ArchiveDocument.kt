package com.example.arsipbpkpad.domain.model

data class ArchiveDocument(
    val id: String,
    val boxSessionId: String? = null,
    val type: DocType,
    val copyStatus: DocCopyStatus = DocCopyStatus.ORIGINAL,
    val documentNumber: String,
    val nominal: Double?,
    val thirdParty: String?,
    val year: Int,
    val dateIssued: String?,
    val status: DocStatus,
    val idStorageLocation: String?,
    val imageUrl: String? = null,
    val metadata: ArchiveMetadata?,
    val createdBy: String?,
    val verifiedBy: String?,
    val createdAt: String?,
    val updatedAt: String?
)
