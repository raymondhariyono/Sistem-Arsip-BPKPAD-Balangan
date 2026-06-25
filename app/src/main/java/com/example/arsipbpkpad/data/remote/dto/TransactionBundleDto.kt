package com.example.arsipbpkpad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionBundleDto(
    @SerialName("id") val id: String? = null,
    @SerialName("bundle_name") val bundleName: String,
    @SerialName("description") val description: String? = null,
    @SerialName("year") val year: Int,
    @SerialName("created_by") val createdBy: String? = null
)
