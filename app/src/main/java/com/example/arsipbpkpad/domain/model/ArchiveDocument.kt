package com.example.arsipbpkpad.domain.model

/**
 * Main domain model for an archived document.
 * Enforces use of DomainConstants for default values.
 */
data class ArchiveDocument(
    val id: String,
    val boxSessionId: String? = null,
    val type: DocType,
    val documentNumber: String?,
    val copyType: DocCopyType,
    val copyCount: Int = DomainConstants.DEFAULT_COPY_COUNT,
    val classificationCode: String = DomainConstants.DEFAULT_CLASSIFICATION_CODE,
    val description: String?,
    val nominal: Double?,
    val year: Int = DomainConstants.DEFAULT_YEAR,
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
