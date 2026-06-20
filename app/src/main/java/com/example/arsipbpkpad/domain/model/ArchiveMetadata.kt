package com.example.arsipbpkpad.domain.model

/**
 * Domain model for archive metadata.
 * Stripped of serialization dependencies.
 */
data class ArchiveMetadata(
    val bankName: String? = null,
    val accountNumber: String? = null,
    val paymentPurpose: String? = null,
    val budgetCode: String? = null,
    val warehouse: String? = null,
    val rack: String? = null,
    val boxNumber: String? = null
)
