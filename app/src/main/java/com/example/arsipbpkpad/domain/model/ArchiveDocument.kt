package com.example.arsipbpkpad.domain.model

data class ArchiveDocument(
    val id: String,
    val boxSessionId: String? = null,
    val type: DocType,
    val documentNumber: String?,
    val copyType: DocCopyType,
    val copyCount: Int,
    val classificationCode: String = "900.1.3.1",
    val description: String?,
    val nominal: Double?,
    val year: Int,
    val condition: DocCondition = DocCondition.GOOD,
    val status: DocStatus,
    val metadata: ArchiveMetadata?,
    val idStorageLocation: String? = null,
    val bundleId: String? = null,
    val createdBy: String? = null,
    val verifiedBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
