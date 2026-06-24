package com.example.arsipbpkpad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoxDto(
    @SerialName("id") val id: String? = null,
    @SerialName("shelf_id") val shelfId: String,
    @SerialName("name") val name: String
)
