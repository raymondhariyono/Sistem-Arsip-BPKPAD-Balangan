package com.example.arsipbpkpad.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveMetadata(
    val bankName: String? = null,
    val accountNumber: String? = null,
    val paymentPurpose: String? = null,
    val budgetCode: String? = null
)
