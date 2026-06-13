package com.example.arsipbpkpad.data.remote.dto

import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCopyStatus
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArchiveDto(
    @SerialName("id") val id: String? = null,
    @SerialName("type") val type: DocType,
    @SerialName("copy_status") val copyStatus: DocCopyStatus,
    @SerialName("document_number") val documentNumber: String,
    @SerialName("nominal") val nominal: Double? = null,
    @SerialName("third_party") val thirdParty: String? = null,
    @SerialName("year") val year: Int,
    @SerialName("date_issued") val dateIssued: String? = null,
    @SerialName("status") val status: DocStatus,
    @SerialName("id_storage_location") val idStorageLocation: String? = null,
    @SerialName("metadata") val metadata: ArchiveMetadata? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("verified_by") val verifiedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class StorageLocationDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null
)

@Serializable
data class ArchiveMetadataDto(
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("account_number") val accountNumber: String? = null,
    @SerialName("payment_purpose") val paymentPurpose: String? = null,
    @SerialName("budget_code") val budgetCode: String? = null
)
