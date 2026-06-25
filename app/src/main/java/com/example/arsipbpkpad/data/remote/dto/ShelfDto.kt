package com.example.arsipbpkpad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShelfDto(
    @SerialName("id") val id: String? = null,
    @SerialName("room_id") val roomId: String,
    @SerialName("name") val name: String,
    @SerialName("created_by") val createdBy: String? = null
)
