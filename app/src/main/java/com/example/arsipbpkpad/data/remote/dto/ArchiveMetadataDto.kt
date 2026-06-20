package com.example.arsipbpkpad.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveMetadataDto(
    val bankName: String? = null,
    val accountNumber: String? = null,
    val paymentPurpose: String? = null,
    val budgetCode: String? = null,
    val warehouse: String? = null,
    val rack: String? = null,
    val boxNumber: String? = null
)
