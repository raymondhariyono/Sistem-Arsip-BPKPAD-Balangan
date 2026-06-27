package com.example.arsipbpkpad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import kotlinx.serialization.json.JsonElement

@Serializable
data class ActivityLogDto(
    val id: String? = null,
    @SerialName("actor_id") val actorId: String? = null,
    val action: String,
    @SerialName("entity_type") val entityType: String? = null,
    @SerialName("entity_id") val entityId: String? = null,
    val metadata: JsonElement? = null,
    @SerialName("created_at") val createdAt: String? = null
)
