package com.example.arsipbpkpad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArchiveMetadataDto(
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("account_number") val accountNumber: String? = null,
    @SerialName("payment_purpose") val paymentPurpose: String? = null,
    @SerialName("budget_code") val budgetCode: String? = null,
    @SerialName("warehouse") val warehouse: String? = null,
    @SerialName("rack") val rack: String? = null,
    @SerialName("box_number") val boxNumber: String? = null
)
