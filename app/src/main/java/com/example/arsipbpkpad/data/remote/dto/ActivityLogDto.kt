package com.example.arsipbpkpad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityLogDto(
    val id: String? = null,
    @SerialName("actor_id") val actorId: String? = null,
    val action: String,
    @SerialName("entity_type") val entityType: String,
    @SerialName("entity_id") val entityId: String,
    val details: String? = null
)
