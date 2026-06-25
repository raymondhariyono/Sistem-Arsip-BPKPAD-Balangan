package com.example.arsipbpkpad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("created_by") val createdBy: String? = null
)
