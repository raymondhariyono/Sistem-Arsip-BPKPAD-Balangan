package com.example.arsipbpkpad.data.remote.dto

 import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArchiveDto(
    @SerialName("id") val id: String? = null,
    @SerialName("document_type") val type: String,
    @SerialName("document_number") val documentNumber: String? = null,
    @SerialName("copy_type") val copyType: String,
    @SerialName("copy_count") val copyCount: Int,
    @SerialName("classification_code") val classificationCode: String = "900.1.3.1",
    @SerialName("description") val description: String? = null,
    @SerialName("nominal") val nominal: Double? = null,
    @SerialName("year") val year: Int,
    @SerialName("condition") val condition: String,
    @SerialName("status") val status: String,
    @SerialName("metadata") val metadata: ArchiveMetadataDto? = null,
    @SerialName("box_id") val idStorageLocation: String? = null,
    @SerialName("bundle_id") val bundleId: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class StorageLocationDto(
    @SerialName("id") val id: String? = null,
    @SerialName("room") val room: String,
    @SerialName("shelf") val shelf: String,
    @SerialName("box_number") val boxNumber: String,
    @SerialName("description") val description: String? = null,
    @SerialName("created_by") val createdBy: String? = null
)
